/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.core.storage;

import org.apache.commons.lang.Validate;
import org.chromattic.api.query.Query;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ActivityStreamImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.chromattic.entity.ActivityDayEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityListEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityParameters;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.utils.ActivityIterator;
import org.exoplatform.social.core.chromattic.utils.ActivityList;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.query.Order;
import org.exoplatform.social.core.storage.query.WhereExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ActivityStorage extends AbstractStorage {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(ActivityStorage.class);

  private final SortedSet<ActivityProcessor> activityProcessors;

  private final RelationshipStorage relationshipStorage;
  private final IdentityStorage identityStorage;
  private final SpaceStorage spaceStorage;

  public ActivityStorage(
      final RelationshipStorage relationshipStorage,
      final IdentityStorage identityStorage,
      final SpaceStorage spaceStorage) {

    this.relationshipStorage = relationshipStorage;
    this.identityStorage = identityStorage;
    this.spaceStorage = spaceStorage;
    this.activityProcessors = new TreeSet<ActivityProcessor>(processorComparator());

  }

  private enum TimestampType {
    NEWER, OLDER;

    private Long number;

    public TimestampType from(Long number) {
      this.number = number;
      return this;
    }

    public Long get() {
      return number;
    }
  }

  /*
   * Internal
   */

  protected void _createActivity(Identity owner, ExoSocialActivity activity) throws NodeNotFoundException {

    IdentityEntity identityEntity = _findById(IdentityEntity.class, owner.getId());

    IdentityEntity posterIdentityEntity;
    if (activity.getUserId() != null) {
      posterIdentityEntity = _findById(IdentityEntity.class, activity.getUserId());
    }
    else {
      posterIdentityEntity = identityEntity;
    }

    // Get ActivityList
    ActivityListEntity activityListEntity = identityEntity.getActivityList();

    //
    Collection<ActivityEntity> entities = new ActivityList(activityListEntity);

    // Create activity
    long currentMillis = System.currentTimeMillis();
    ActivityEntity activityEntity = activityListEntity.createActivity(String.valueOf(currentMillis));
    entities.add(activityEntity);
    activityEntity.setIdentity(identityEntity);
    activityEntity.setComment(Boolean.FALSE);
    activityEntity.setPostedTime(currentMillis);
    activityEntity.setPosterIdentity(posterIdentityEntity);

    // Fill activity model
    activity.setId(activityEntity.getId());
    activity.setStreamOwner(identityEntity.getRemoteId());
    activity.setPostedTime(currentMillis);
    activity.setReplyToId(new String[]{});

    //
    fillActivityEntityFromActivity(activity, activityEntity);
  }

  protected void _saveActivity(ExoSocialActivity activity) throws NodeNotFoundException {

    ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
    fillActivityEntityFromActivity(activity, activityEntity);

  }

  /*
   * Private
   */

  private void fillActivityEntityFromActivity(ExoSocialActivity activity, ActivityEntity activityEntity) {

    activityEntity.setTitle(activity.getTitle());
    activityEntity.setTitleId(activity.getTitleId());
    activityEntity.setBody(activity.getBody());
    activityEntity.setBodyId(activity.getBodyId());
    activityEntity.setLikes(activity.getLikeIdentityIds());
    activityEntity.setType(activity.getType());
    activityEntity.setAppId(activity.getAppId());
    activityEntity.setExternalId(activity.getExternalId());
    activityEntity.setUrl(activity.getUrl());
    activityEntity.setPriority(activity.getPriority());

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
    activity.setTitleId(activityEntity.getTitleId());
    activity.setBody(activityEntity.getBody());
    activity.setBodyId(activityEntity.getBodyId());
    activity.setUserId(activityEntity.getPosterIdentity().getId());
    activity.setPostedTime(activityEntity.getPostedTime());
    activity.setType(activityEntity.getType());
    activity.setAppId(activityEntity.getAppId());
    activity.setExternalId(activityEntity.getExternalId());
    activity.setUrl(activityEntity.getUrl());
    activity.setPriority(activityEntity.getPriority());
    activity.isComment(activityEntity.isComment());
    
    //
    List<String> computeCommentid = new ArrayList<String>();
    for (ActivityEntity commentEntity : activityEntity.getComments()) {
      computeCommentid.add(commentEntity.getId());
    }

    //
    activity.setReplyToId(computeCommentid.toArray(new String[]{}));
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

    private List<Identity> getSpacesId(Identity ownerIdentity) {

    List<Identity> identitiesId = new ArrayList<Identity>();
    List<Space> spaces = spaceStorage.getAccessibleSpaces(ownerIdentity.getRemoteId());
    for (Space space : spaces) {
      identitiesId.add(identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName()));
    }

    return identitiesId;

  }

  private static Comparator<ActivityProcessor> processorComparator() {
    return new Comparator<ActivityProcessor>() {

      public int compare(ActivityProcessor p1, ActivityProcessor p2) {
        if (p1 == null || p2 == null) {
          throw new IllegalArgumentException("Cannot compare null ActivityProcessor");
        }
        return p1.getPriority() - p2.getPriority();
      }
    };
  }

  private void processActivity(ExoSocialActivity existingActivity) {
    Iterator<ActivityProcessor> it = activityProcessors.iterator();
    while (it.hasNext()) {
      try {
        it.next().processActivity(existingActivity);
      } catch (Exception e) {
        LOG.warn("activity processing failed " + e.getMessage());
      }
    }
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

      processActivity(activity);

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
  public List<ExoSocialActivity> getUserActivities(Identity owner) throws ActivityStorageException {

    return getUserActivities(owner, 0, 0);

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
  public List<ExoSocialActivity> getUserActivities(Identity owner, long offset, long limit)
      throws ActivityStorageException {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    int nb = 0;

    try {
      IdentityEntity identityEntity = _findById(IdentityEntity.class, owner.getId());

      ActivityList activityList = new ActivityList(identityEntity.getActivityList());

      Iterator<ActivityEntity> it = activityList.iterator();

      _skip(it, offset);

      while (it.hasNext()) {
        ActivityEntity current = it.next();

        //
        activities.add(getActivity(current.getId()));

        if (++nb == limit) {
          return activities;
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
      commentEntity.setIdentity(activityEntity.getIdentity());
      commentEntity.setPosterIdentity(_findById(IdentityEntity.class, comment.getUserId()));
      commentEntity.setComment(Boolean.TRUE);
      commentEntity.setPostedTime(currentMillis);
      comment.setId(commentEntity.getId());

      //
      String[] ids = activity.getReplyToId();
      List<String> listIds;
      if (ids != null) {
        listIds = new ArrayList<String>(Arrays.asList(ids));
      }
      else {
        listIds = new ArrayList<String>();
      }
      listIds.add(commentEntity.getId());
      activity.setReplyToId(listIds.toArray(new String[]{}));

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
     * @throws ActivityStorageException activity storage exception with type:
     * ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY
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

  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) throws ActivityStorageException {

    try {

      ActivityEntity commentEntity = _findById(ActivityEntity.class, comment.getId());
      ActivityEntity parentActivityEntity = commentEntity.getParentActivity();

      return getActivity(parentActivityEntity.getId());
      
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_GET_ACTIVITY, e.getMessage(), e);
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
  public List<ExoSocialActivity> getActivitiesOfIdentities(List<Identity> connectionList,
                                                           long offset, long limit) throws ActivityStorageException {

    return getActivitiesOfIdentities(connectionList, null, offset, limit);
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
  public List<ExoSocialActivity> getActivitiesOfIdentities(
      List<Identity> connectionList, TimestampType type, long offset, long limit)
      throws ActivityStorageException {

    //
    if (connectionList.size() == 0) {
      return new ArrayList<ExoSocialActivity>();
    }

    QueryResult<ActivityEntity> results = getActivitiesOfIdentitiesQuery(connectionList, type).objects(offset, limit);

    List<ExoSocialActivity> activities =  new ArrayList<ExoSocialActivity>();

    while(results.hasNext()) {
      activities.add(getActivity(results.next().getId()));
    }

    return activities;
  }

  /**
   * Gets the activities for a list of identities.
   *
   * Access a activity stream of a list of identities by specifying the offset and limit.
   *
   * @param connectionList the list of connections for which we want to get
   * the latest activities
   * @param type
   * @return the activities related to the list of connections
   * @since 1.2.0-GA
   */
  private Query<ActivityEntity> getActivitiesOfIdentitiesQuery(
      List<Identity> connectionList, TimestampType type)
      throws ActivityStorageException {

    QueryBuilder<ActivityEntity> builder = getSession().createQueryBuilder(ActivityEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    boolean first = true;

    whereExpression.startGroup();
    for (Identity currentIdentity : connectionList) {

      if (first) {
        first = false;
      }
      else {
        whereExpression.or();
      }

      whereExpression.equals(ActivityEntity.identity, currentIdentity.getId());
    }
    whereExpression.endGroup();

    whereExpression.and().equals(ActivityEntity.isComment, Boolean.FALSE);

    if (type != null) {
      switch (type) {
        case NEWER:
          whereExpression.and().greater(ActivityEntity.postedTime, type.get());
          break;
        case OLDER:
          whereExpression.and().lesser(ActivityEntity.postedTime, type.get());
          break;
      }
    }

    whereExpression.orderBy(ActivityEntity.postedTime, Order.DESC);

    return builder.where(whereExpression.toString()).get();


  }

  /**
   * Count the number of activities from an ownerIdentity
   *
   * @param owner
   * @return the number of activities
   */
  public int getNumberOfUserActivities(Identity owner) throws ActivityStorageException {

    try {

      IdentityEntity identityEntity = _findById(IdentityEntity.class, owner.getId());
      return identityEntity.getActivityList().getNumber();

    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(
          ActivityStorageException.Type.FAILED_TO_GET_ACTIVITIES_COUNT,
          e.getMessage(), e);
    }

  }

  /**
   * Gets the number of newer activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   */
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    int nb = 0;

    try {

      //
      IdentityEntity identity = _findById(IdentityEntity.class, ownerIdentity.getId());
      ActivityEntity activity = _findById(ActivityEntity.class, baseActivity.getId());

      //
      Long targetTimestamp = activity.getPostedTime();

      for (ActivityEntity current : new ActivityList(identity.getActivityList())) {
        //
        if (current.getPostedTime() <= targetTimestamp) {
          return nb;
        }

        //
        ++nb;
      }

      return nb;

    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(
          ActivityStorageException.Type.FAILED_TO_GET_ACTIVITIES_COUNT,
          e.getMessage());
    }

  }

  /**
   * Gets the list of newer activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getNewerOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity,
                                                          int limit) {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    int nb = 0;

    try {

      //
      IdentityEntity identity = _findById(IdentityEntity.class, ownerIdentity.getId());
      ActivityEntity activity = _findById(ActivityEntity.class, baseActivity.getId());

      //
      Long targetTimestamp = activity.getPostedTime();

      for (ActivityEntity current : new ActivityList(identity.getActivityList())) {

        ExoSocialActivity a = getActivity(current.getId());

        if (targetTimestamp >= a.getPostedTime() || nb == limit) {
          return activities;
        }
        else {
          activities.add(a);
        }

        //
        ++nb;
      }

      return activities;

    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(
          ActivityStorageException.Type.FAILED_TO_GET_ACTIVITIES_COUNT,
          e.getMessage());
    }
  }

  /**
   * Gets the number of older activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   */
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {


    try {

      IdentityEntity identity = _findById(IdentityEntity.class, ownerIdentity.getId());
      ActivityEntity activity = _findById(ActivityEntity.class, baseActivity.getId());

      //
      ActivityList list = new ActivityList(identity.getActivityList());
      ActivityIterator it = list.iterator();
      int nb = it.moveTo(activity);

      return identity.getActivityList().getNumber() - nb - 1;
      
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(
          ActivityStorageException.Type.FAILED_TO_GET_ACTIVITIES_COUNT,
          e.getMessage());
    }
  }

  /**
   * Gets the list of older activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getOlderOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity,
                                                          int limit) {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    int nb = 0;

    //
    try {

      IdentityEntity identity = _findById(IdentityEntity.class, ownerIdentity.getId());
      ActivityEntity activity = _findById(ActivityEntity.class, baseActivity.getId());

      //
      ActivityList list = new ActivityList(identity.getActivityList());
      ActivityIterator it = list.iterator();
      it.moveTo(activity);

      //
      while(it.hasNext()) {
        activities.add(getActivity(it.next().getId()));
        ++nb;

        if (nb >= limit) {
          return activities;
        }
      }

    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(
          ActivityStorageException.Type.FAILED_TO_GET_ACTIVITIES_COUNT,
          e.getMessage());
    }

    return activities;
  }

  /**
   * Gets activity feed from an identity.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getActivityFeed(Identity ownerIdentity, int offset, int limit) {

    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);

    //
    return getActivitiesOfIdentities(identities, offset, limit);
  }

  public int getNumberOfActivitesOnActivityFeed(Identity ownerIdentity) {

    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);

    //
    return getActivitiesOfIdentitiesQuery(identities, null).objects().size();

  }

  public int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);

    //
    return getActivitiesOfIdentitiesQuery(identities, TimestampType.NEWER.from(baseActivity.getPostedTime()))
        .objects().size();

  }

  public List<ExoSocialActivity> getNewerOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity,
                                                         int limit) {
    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);

    //
    return getActivitiesOfIdentities(identities, TimestampType.NEWER.from(baseActivity.getPostedTime()), 0, limit);

  }

  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);

    //
    return getActivitiesOfIdentitiesQuery(identities, TimestampType.OLDER.from(baseActivity.getPostedTime()))
        .objects().size();

  }

  public List<ExoSocialActivity> getOlderOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity,
                                                         int limit) {
    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);

    //
    return getActivitiesOfIdentities(identities, TimestampType.OLDER.from(baseActivity.getPostedTime()), 0, limit);
  }

  /**
   * Gets activities of connections of an identity with offset, limit.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity, int offset, int limit) {

    List<Identity> connections = relationshipStorage.getConnections(ownerIdentity);
    return getActivitiesOfIdentities(connections, offset, limit);

  }

  /**
   * Gets the count of the activities of connections who connected with an identity.
   *
   * @param ownerIdentity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfActivitiesOfConnections(Identity ownerIdentity) {

    //

    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);

    if (connectionList.size() == 0) {
      return 0;
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

    QueryResult<ActivityEntity> results = builder.where(whereExpression.toString()).get().objects();

    return results.size();

  }

  /**
   * Gets the activities of the identity with offset, limit.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @throws ActivityStorageException
   * @since 1.2.0-Beta3
    */
  public List<ExoSocialActivity> getActivitiesOfIdentity(Identity ownerIdentity, long offset, long limit)
      throws ActivityStorageException {

    return getUserActivities(ownerIdentity, offset, limit);
    
  }

  /**
   * Gets the number of newer activities based on base activity of connections.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);

    //
    if (connectionList.size() == 0) {
      return 0;
    }

    //
    return getActivitiesOfIdentitiesQuery(connectionList, TimestampType.NEWER.from(baseActivity.getPostedTime()))
        .objects().size();
    
  }

  /**
   * Gets the newer activities of connections based on baseActivity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getNewerOnActivitiesOfConnections(
      Identity ownerIdentity, ExoSocialActivity baseActivity, long limit) {

    //
    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);

    //
    return getActivitiesOfIdentities(connectionList, TimestampType.NEWER.from(baseActivity.getPostedTime()), 0, limit);

  }

  /**
   * Gets the number of older activities of connections based on baseActivity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);

    //
    if (connectionList.size() == 0) {
      return 0;
    }

    //
    return getActivitiesOfIdentitiesQuery(connectionList, TimestampType.OLDER.from(baseActivity.getPostedTime()))
        .objects().size();
    
  }

  /**
   * Gets the older of activities of connections based on base an activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getOlderOnActivitiesOfConnections(Identity ownerIdentity,
                                                                  ExoSocialActivity baseActivity, int limit) {

    //
    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);

    //
    return getActivitiesOfIdentities(connectionList, TimestampType.OLDER.from(baseActivity.getPostedTime()), 0, limit);

  }

  /**
   * Gets the activities of spaces where identity can access (manager or member).
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getUserSpacesActivities(Identity ownerIdentity, int offset, int limit) {

    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);

    //
    return getActivitiesOfIdentities(spaceList, null, offset, limit);

  }

  /**
   * Gets the number of activities of spaces where identity can access (manager or member).
   *
   * @param ownerIdentity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfUserSpacesActivities(Identity ownerIdentity) {

    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);

    //
    if (spaceList.size() == 0) {
      return 0;
    }

    //
    return getActivitiesOfIdentitiesQuery(spaceList, null).objects().size();

  }

  /**
   * Gets the number of newer activities of spaces where the identity can access, based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);

    //
    if (spaceList.size() == 0) {
      return 0;
    }

    //
    return getActivitiesOfIdentitiesQuery(spaceList, TimestampType.NEWER.from(baseActivity.getPostedTime()))
        .objects().size();

  }

  /**
   * Gets the newer activities of spaces where identity can access (manager or member), based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getNewerOnUserSpacesActivities(Identity ownerIdentity,
                                                                ExoSocialActivity baseActivity, int limit) {

    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);

    //
    return getActivitiesOfIdentities(spaceList, TimestampType.NEWER.from(baseActivity.getPostedTime()), 0, limit);

  }

  /**
   * Gets the number of newer activities of spaces where the identity can access, based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);

    //
    if (spaceList.size() == 0) {
      return 0;
    }

    //
    return getActivitiesOfIdentitiesQuery(spaceList, TimestampType.OLDER.from(baseActivity.getPostedTime()))
        .objects().size();

  }

  /**
   * Gets the older activities of spaces where identity can access (manager or member), based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getOlderOnUserSpacesActivities(Identity ownerIdentity,
                                                                ExoSocialActivity baseActivity, int limit) {

    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);

    //
    return getActivitiesOfIdentities(spaceList, TimestampType.OLDER.from(baseActivity.getPostedTime()), 0, limit);

  }

  /**
   * Gets the comments of an activity with offset, limit.
   *
   * @param existingActivity
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getComments(ExoSocialActivity existingActivity, int offset, int limit) {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();

    //
    List<String> commentIds = Arrays.asList(getActivity(existingActivity.getId()).getReplyToId());

    //
    limit = (limit > commentIds.size() ? commentIds.size() : limit);
    for(String commentId : commentIds.subList(offset, limit)) {
      activities.add(getActivity(commentId));
    }

    //
    return activities;
  }

  /**
   * Gets the number of comments of an activity.
   *
   * @param existingActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfComments(ExoSocialActivity existingActivity) {
    return getActivity(existingActivity.getId()).getReplyToId().length;
  }

  /**
   * Gets the number of newer comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment) {

    List<String> commentId = Arrays.asList(getActivity(existingActivity.getId()).getReplyToId());
    return commentId.indexOf(baseComment.getId());

  }

  /**
   * Gets the newer comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getNewerComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment,
                                           int limit) {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();

    //
    List<String> commentIds = Arrays.asList(getActivity(existingActivity.getId()).getReplyToId());
    int baseIndex = commentIds.indexOf(baseComment.getId());
    if (baseIndex > limit) {
      baseIndex = limit;
    }

    //
    for(String commentId : commentIds.subList(0, baseIndex)) {
      activities.add(getActivity(commentId));
    }

    //
    return activities;

  }

  /**
   * Gets the number of newer comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment) {

    List<String> commentIds = Arrays.asList(getActivity(existingActivity.getId()).getReplyToId());
    int index = commentIds.indexOf(baseComment.getId());

    return (commentIds.size() - index - 1);

  }

  /**
   * Gets the older comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getOlderComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment,
                                           int limit) {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();

    //
    List<String> commentIds = Arrays.asList(getActivity(existingActivity.getId()).getReplyToId());
    int baseIndex = commentIds.indexOf(baseComment.getId());

    //
    for(String commentId : commentIds.subList(baseIndex + 1, limit)) {
      activities.add(getActivity(commentId));
    }

    //
    return activities;

  }

  /**
   * Gets the activity processors.
   *
   * @return 1.2.0-GA
   */
  public SortedSet<ActivityProcessor> getActivityProcessors() {
    return activityProcessors;
  }

  /**
   * Updates an existing activity.
   *
   * @param existingActivity
   * @throws ActivityStorageException
   */
  public void updateActivity(ExoSocialActivity existingActivity) throws ActivityStorageException {

    try {
      _saveActivity(existingActivity);
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(
          ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY,
          e.getMessage()
      );
    }

  }
}