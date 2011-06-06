/*
* Copyright (C) 2003-2009 eXo Platform SAS.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.exoplatform.social.core.storage;

import org.apache.commons.lang.Validate;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ActivityStreamImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.chromattic.entity.ActivityDayEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityListEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityMonthEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityParameters;
import org.exoplatform.social.core.chromattic.entity.ActivityYearEntity;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.query.Order;
import org.exoplatform.social.core.storage.query.WhereExpression;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ActivityStorage extends AbstractStorage {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(ActivityStorage.class);

  /*
   * Internal
   */

  protected void _createActivity(Identity owner, ExoSocialActivity activity) throws NodeNotFoundException {

    IdentityEntity identityEntity = _findById(IdentityEntity.class, owner.getId());

    // Get ActivityList
    ActivityListEntity activityListEntity = identityEntity.getActivityList();

    ActivityDayEntity activityDayEntity = getCurrentActivityDay(activityListEntity);

    // Create activity
    long currentMillis = System.currentTimeMillis();
    ActivityEntity activityEntity = activityDayEntity.createActivity(String.valueOf(currentMillis));
    activityDayEntity.getActivities().add(0, activityEntity);
    activityEntity.setIdentity(identityEntity);
    activityEntity.setComment(Boolean.FALSE);
    activityEntity.setPostedTime(currentMillis);

    activity.setId(activityEntity.getId());
    activity.setStreamOwner(identityEntity.getRemoteId());

    fillActivityEntityFromActivity(activity, activityEntity);

    activityDayEntity.inc();
  }

  protected void _saveActivity(ExoSocialActivity activity) throws NodeNotFoundException {

    ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
    fillActivityEntityFromActivity(activity, activityEntity);

  }

  /*
   * Private
   */
  private ActivityDayEntity getCurrentActivityDay(ActivityListEntity activityListEntity) {

    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);

    String year = String.valueOf(calendar.get(Calendar.YEAR));
    String month = MONTH_NAME[calendar.get(Calendar.MONTH)];
    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    return activityListEntity.getYear(year).getMonth(month).getDay(day);
    
  }

  private void fillActivityEntityFromActivity(ExoSocialActivity activity, ActivityEntity activityEntity) {

    activityEntity.setTitle(activity.getTitle());
    activityEntity.setBody(activity.getBody());
    activityEntity.setLikes(activity.getLikeIdentityIds());

    //
    Map<String, String> params = activity.getTemplateParams();
    if (params != null) {
      activityEntity.putParams(params);
    }

    //
    fillStream(activityEntity, activity);
    
  }

  private void fillActivityFromEntity(ActivityEntity activityEntity, ExoSocialActivity activity) {

    //
    activity.setId(activityEntity.getId());
    activity.setTitle(activityEntity.getTitle());
    activity.setBody(activityEntity.getBody());
    activity.setUserId(activityEntity.getIdentity().getId());
    activity.setPostedTime(activityEntity.getPostedTime());

    //
    String computeCommentid = "";
    for (ActivityEntity commentEntity : activityEntity.getComments()) {
      computeCommentid += "," + commentEntity.getId();
    }

    //
    activity.setReplyToId(computeCommentid);
    String[] likes = activityEntity.getLikes();
    if (likes != null) {
      activity.setLikeIdentityIds(activityEntity.getLikes());
    }

    //
    ActivityParameters params = activityEntity.getParams();
    if (params != null) {
      activity.setTemplateParams(new HashMap<String, String>(params.getParams()));
    }
    else {
      activity.setTemplateParams(new HashMap<String, String>());
    }

    //
    fillStream(activityEntity, activity);
    
  }

  private void fillStream(ActivityEntity activityEntity, ExoSocialActivity activity) {

    //
    ActivityStream stream = new ActivityStreamImpl();
    IdentityEntity identityEntity = activityEntity.getIdentity();

    //
    stream.setId(identityEntity.getId());
    stream.setPrettyId(identityEntity.getRemoteId());
    stream.setType(identityEntity.getProviderId());
    stream.setPermaLink(LinkProvider.getActivityUri(identityEntity.getProviderId(), identityEntity.getRemoteId()));

    //
    activity.setActivityStream(stream);
    activity.setStreamId(stream.getId());
    activity.setStreamOwner(stream.getPrettyId());

  }

  /*
   * Public
   */

  /**
   * Load an activity by its id.
   *
   * @param activityId the id of the activity. An UUID.
   * @return the activity
   */
  public ExoSocialActivity getActivity(String activityId) throws ActivityStorageException {

    try {

      //
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activityId);
      ExoSocialActivity activity = new ExoSocialActivityImpl();

      //
      activity.setId(activityEntity.getId());
      fillActivityFromEntity(activityEntity, activity);

      //
      return activity;

    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_GET_ACTIVITY, e.getMessage(), e);
    }
  }

  /**
   * Gets all the activities by identity.
   *
   * @param owner the identity
   * @return the activities
   */
  public List<ExoSocialActivity> getActivities(Identity owner) throws ActivityStorageException {

    return getActivities(owner, 0, 0);
    
  }

  /**
   * Gets the activities by identity.
   *
   * Access a user's activity stream by specifying the offset and limit.
   *
   * @param owner the identity
   * @param offset
   * @param limit
   * @return the activities
   */
  public List<ExoSocialActivity> getActivities(Identity owner, long offset, long limit) throws ActivityStorageException {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    int nb = 0;

    try {
      IdentityEntity identityEntity = _findById(IdentityEntity.class, owner.getId());

      int allNumber = identityEntity.getActivityList().getNumber();
      if (offset >= allNumber) {
        return activities;
      }

      for (ActivityYearEntity year : identityEntity.getActivityList().getYears().values()) {

        int yearNumber = year.getNumber();
        if (offset > yearNumber) {
          offset -= yearNumber;
          continue;
        }

        for (ActivityMonthEntity month : year.getMonths().values()) {

          int monthNumber = month.getNumber();
          if (offset > monthNumber) {
            offset -= monthNumber;
            continue;
          }

          for (ActivityDayEntity day : month.getDays().values()) {

            int dayNumber = day.getNumber();
            if (offset > dayNumber) {
              offset -= dayNumber;
              continue;
            }
            
            for (ActivityEntity activityEntity : day.getActivities()) {

              //
              ExoSocialActivity newActivity = new ExoSocialActivityImpl();

              //
              fillActivityFromEntity(activityEntity, newActivity);

              //
              activities.add(newActivity);
              if (++nb == limit) {
                return activities;
              }

            }
          }
        }
      }
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_GET_ACTIVITY, e.getMessage());
    }

    return activities;
  }
  
  /**
   * Save comment to an activity.
   * activity's ownerstream has to be the same as ownerStream param here.
   *
   * @param activity
   * @param comment
   * @since 1.1.1
   */
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {

    try {

      //
      long currentMillis = System.currentTimeMillis();
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
      ActivityEntity commentEntity = activityEntity.createComment(String.valueOf(currentMillis));

      //
      activityEntity.getComments().add(commentEntity);
      commentEntity.setTitle(comment.getTitle());
      commentEntity.setBody(comment.getBody());
      commentEntity.setIdentity(_findById(IdentityEntity.class, comment.getUserId()));
      commentEntity.setComment(Boolean.TRUE);
      commentEntity.setPostedTime(currentMillis);
      comment.setId(commentEntity.getId());

      //
      activity.setReplyToId(activity.getReplyToId() + "," + commentEntity.getId());

    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_COMMENT, e.getMessage(), e);
    }

    //
    LOG.debug(String.format(
        "Comment %s by %s (%s) created",
        comment.getTitle(),
        comment.getUserId(),
        comment.getId()
    ));
  }

  /**
     * Saves an activity into a stream.
     * Note that the field {@link org.exoplatform.social.core.activity.model.ExoSocialActivity#setUserId(String)}
     * should be the id of an identity {@link Identity#getId()}
     * @param owner owner of the stream where this activity is bound.
     *              Usually a user or space identity
     * @param activity the activity to save
     * @return stored activity
     * @throws ActivityStorageException activity storage exception with type: ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY
     * @since 1.1.1
     */
  public ExoSocialActivity saveActivity(Identity owner, ExoSocialActivity activity) throws ActivityStorageException {
    try {
      Validate.notNull(owner, "owner must not be null.");
      Validate.notNull(activity, "activity must not be null.");
      Validate.notNull(activity.getUpdated(), "Activity.getUpdated() must not be null.");
      Validate.notNull(activity.getPostedTime(), "Activity.getPostedTime() must not be null.");
      Validate.notNull(activity.getTitle(), "Activity.getTitle() must not be null.");
    } catch (IllegalArgumentException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.ILLEGAL_ARGUMENTS, e.getMessage(), e);
    }

    try {

      if (activity.getId() == null) {

        _createActivity(owner, activity);
        
      }
      else {

        _saveActivity(activity);

      }

      //
      getSession().save();

      //
      LOG.debug(String.format(
          "Activity %s by %s (%s) saved",
          activity.getTitle(),
          activity.getUserId(),
          activity.getId()
      ));

      //
      return activity;

    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY, e.getMessage(), e);
    }
  }

  /**
   * Deletes activity by its id.
   * This will delete comments from this activity first, then delete the activity.
   *
   * @param activityId the activity id
   */
  public void deleteActivity(String activityId) throws ActivityStorageException {

    try {

      //
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activityId);
      ActivityDayEntity dayEntity = activityEntity.getDay();

      // For logging
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(activityEntity.getTitle());
      activity.setUserId(activityEntity.getIdentity().getId());
      activity.setId(activityEntity.getId());

      //
      _removeById(ActivityEntity.class, activityId);

      //
      if (dayEntity != null) { // False when activity is a comment
        dayEntity.desc();
      }

      //
      getSession().save();

      //
      LOG.debug(String.format(
          "Activity or comment %s by %s (%s) removed",
          activity.getTitle(),
          activity.getUserId(),
          activity.getId()
      ));

    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_DELETE_ACTIVITY, e.getMessage(), e);
    }
  }

  /**
   * Delete comment by its id.
   *
   * @param activityId
   * @param commentId
   */
  public void deleteComment(String activityId, String commentId) throws ActivityStorageException {

    deleteActivity(commentId);

  }

  /**
   * Gets the activities for a list of identities.
   *
   * Access a activity stream of a list of identities by specifying the offset and limit.
   *
   * @param connectionList the list of connections for which we want to get
   * the latest activities
   * @param offset
   * @param limit
   * @return the activities related to the list of connections
   * @since 1.2.0-GA
   */
  public List<ExoSocialActivity> getActivitiesOfConnections(List<Identity> connectionList,
                                                            long offset, long limit) throws ActivityStorageException {

    //
    if (connectionList.size() == 0) {
      return new ArrayList<ExoSocialActivity>();
    }

    QueryBuilder<ActivityEntity> builder = getSession().createQueryBuilder(ActivityEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    boolean first = true;

    for (Identity currentIdentity : connectionList) {

      if (first) {
        first = false;
      }
      else {
        whereExpression.or();
      }

      whereExpression.equals(ActivityEntity.identity, currentIdentity.getId());
    }

    whereExpression.and().equals(ActivityEntity.isComment, Boolean.FALSE);
    whereExpression.orderBy(ActivityEntity.postedTime, Order.DESC);

    QueryResult<ActivityEntity> results = builder.where(whereExpression.toString()).get().objects(offset, limit);

    List<ExoSocialActivity> activities =  new ArrayList<ExoSocialActivity>();

    while(results.hasNext()) {
      ExoSocialActivity newActivity = new ExoSocialActivityImpl();
      fillActivityFromEntity(results.next(), newActivity);
      activities.add(newActivity);
    }

    return activities;
  }

  /**
   * Count the number of activities from an ownerIdentity
   *
   * @param owner
   * @return the number of activities
   */
  public int getActivitiesCount(Identity owner) throws ActivityStorageException {

    try {

      IdentityEntity identityEntity = _findById(IdentityEntity.class, owner.getId());
      return identityEntity.getActivityList().getNumber();

    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_GET_ACTIVITIES_COUNT, e.getMessage(), e);
    }

  }
}
