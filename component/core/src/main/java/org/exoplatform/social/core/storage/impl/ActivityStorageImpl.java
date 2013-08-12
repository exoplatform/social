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

package org.exoplatform.social.core.storage.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.chromattic.api.query.Ordering;
import org.chromattic.api.query.Query;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.activity.filter.ActivityCounter;
import org.exoplatform.social.core.activity.filter.ActivityFilter;
import org.exoplatform.social.core.activity.filter.ActivityIterator;
import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter;
import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter.ActivityFilterType;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ActivityStreamImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.chromattic.entity.ActivityDayEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityListEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityParameters;
import org.exoplatform.social.core.chromattic.entity.HidableEntity;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.LockableEntity;
import org.exoplatform.social.core.chromattic.filter.JCRFilterLiteral;
import org.exoplatform.social.core.chromattic.utils.ActivityList;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.impl.ActivityStreamStorageImpl.ActivityRefType;
import org.exoplatform.social.core.storage.query.WhereExpression;
import org.exoplatform.social.core.storage.streams.StreamInvocationHelper;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ActivityStorageImpl extends AbstractStorage implements ActivityStorage {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(ActivityStorageImpl.class);
  private static final Pattern MENTION_PATTERN = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");
  public static final Pattern USER_NAME_VALIDATOR_REGEX = Pattern.compile("^[\\p{L}][\\p{L}._\\-\\d]+$");
  private ActivityStorage activityStorage;

  private final SortedSet<ActivityProcessor> activityProcessors;

  private final RelationshipStorage relationshipStorage;
  private final IdentityStorage identityStorage;
  private final SpaceStorage spaceStorage;
  private final ActivityStreamStorage streamStorage;
  //sets value to tell this storage to inject Streams or not
  private boolean mustInjectStreams = true;

  public ActivityStorageImpl(
      final RelationshipStorage relationshipStorage,
      final IdentityStorage identityStorage,
      final SpaceStorage spaceStorage,
      final ActivityStreamStorage streamStorage) {

    this.relationshipStorage = relationshipStorage;
    this.identityStorage = identityStorage;
    this.spaceStorage = spaceStorage;
    this.streamStorage = streamStorage;
    this.activityProcessors = new TreeSet<ActivityProcessor>(processorComparator());
  }
  
  /**
   * Sets value to tell this storage to inject Streams or not
   * @param mustInject
   */
  public void setInjectStreams(boolean mustInject) {
    this.mustInjectStreams = mustInject;
  }

  /*
   * Internal
   */
  protected String[] _createActivity(Identity owner, ExoSocialActivity activity) throws NodeNotFoundException {

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
    long activityMillis = (activity.getPostedTime() != null ? activity.getPostedTime() : currentMillis);
    ActivityEntity activityEntity = activityListEntity.createActivity(String.valueOf(activityMillis));
    entities.add(activityEntity);
    activityEntity.setIdentity(identityEntity);
    activityEntity.setComment(Boolean.FALSE);
    activityEntity.setPostedTime(activityMillis);
    activityEntity.setLastUpdated(activityMillis);
    activityEntity.setPosterIdentity(posterIdentityEntity);
    

    // Fill activity model
    activity.setId(activityEntity.getId());
    activity.setStreamOwner(identityEntity.getRemoteId());
    activity.setPostedTime(activityMillis);
    activity.setReplyToId(new String[]{});
    activity.setUpdated(activityMillis);
    
    //records activity for mention case.
    
    List<String> mentioners = new ArrayList<String>();
    activity.setMentionedIds(processMentions(activity.getMentionedIds(), activity.getTitle(), mentioners, true));
    
    activity.setPosterId(activity.getUserId() != null ? activity.getUserId() : owner.getId());
      
    fillActivityEntityFromActivity(activity, activityEntity);
    return mentioners.toArray(new String[0]);
  }

  protected void _saveActivity(ExoSocialActivity activity) throws NodeNotFoundException {

    ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
    
    String[] removedLikes = StorageUtils.sub(activityEntity.getLikes(), activity.getLikeIdentityIds());
    String[] addedLikes = StorageUtils.sub(activity.getLikeIdentityIds(), activityEntity.getLikes());
    
    if (removedLikes.length > 0 || addedLikes.length > 0) {
      //process likes activity
      manageActivityLikes(addedLikes, removedLikes, activity);
    }
    
    fillActivityEntityFromActivity(activity, activityEntity);
    StorageUtils.persist();
  }
  
  private void manageActivityLikes(String[] addedLikes, String[] removedLikes, ExoSocialActivity activity) {

    if (addedLikes != null) {
      for (String id : addedLikes) {
        Identity identity = identityStorage.findIdentityById(id);
        if (mustInjectStreams) {
          StreamInvocationHelper.like(identity, activity);
        }
      }
    }

    if (removedLikes != null) {
      for (String id : removedLikes) {
        Identity removedLiker = identityStorage.findIdentityById(id);
        if (mustInjectStreams) {
          StreamInvocationHelper.unLike(removedLiker, activity);
        }
      }
    }
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
    activityEntity.setLastUpdated(activity.getUpdated().getTime());
    //
    HidableEntity hidable = _getMixin(activityEntity, HidableEntity.class, true);
    hidable.setHidden(activity.isHidden());
    LockableEntity lockable = _getMixin(activityEntity, LockableEntity.class, true);
    lockable.setLocked(activity.isLocked());
    activityEntity.setMentioners(activity.getMentionedIds());
    activityEntity.setCommenters(activity.getCommentedIds());

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
    activity.setUpdated(activityEntity.getLastUpdated());
    activity.setType(activityEntity.getType());
    activity.setAppId(activityEntity.getAppId());
    activity.setExternalId(activityEntity.getExternalId());
    activity.setUrl(activityEntity.getUrl());
    activity.setPriority(activityEntity.getPriority());
    activity.isComment(activityEntity.isComment());
    activity.setPosterId(activityEntity.getPosterIdentity().getId());

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
    
    String[] mentioners = activityEntity.getMentioners();
    if (mentioners != null) {
      activity.setMentionedIds(activityEntity.getMentioners());
    }

    String[] commenters = activityEntity.getCommenters();
    if (commenters != null) {
      activity.setCommentedIds(activityEntity.getCommenters());
    }
    
    //
    ActivityParameters params = activityEntity.getParams();
    if (params != null) {
      activity.setTemplateParams(new LinkedHashMap<String, String>(params.getParams()));
    }
    else {
      activity.setTemplateParams(new HashMap<String, String>());
    }
    
    //
    LockableEntity lockable = _getMixin(activityEntity, LockableEntity.class, false);
    if (lockable != null) {
      activity.isLocked(lockable.getLocked());
    }
    HidableEntity hidable = _getMixin(activityEntity, HidableEntity.class, false);
    if (hidable != null) {
      activity.isHidden(hidable.getHidden());
    }
    

    //
    fillStream(activityEntity, activity);
    
  }

  private void fillStream(ActivityEntity activityEntity, ExoSocialActivity activity) {

    //
    ActivityStream stream = new ActivityStreamImpl();
    
    IdentityEntity identityEntity = null;

    //update new stream owner
    try {
      Identity streamOwnerIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner());
      IdentityEntity streamOwnerEntity = _findById(IdentityEntity.class, streamOwnerIdentity.getId());
      identityEntity = streamOwnerEntity;
      activityEntity.setIdentity(streamOwnerEntity);
    } catch (Exception e) {
      identityEntity = activityEntity.getIdentity();
    }
    //
    stream.setId(identityEntity.getId());
    stream.setPrettyId(identityEntity.getRemoteId());
    stream.setType(identityEntity.getProviderId());
    
    //Identity identity = identityStorage.findIdentityById(identityEntity.getId());
    if (identityEntity != null && SpaceIdentityProvider.NAME.equals(identityEntity.getProviderId())) {
      Space space = spaceStorage.getSpaceByPrettyName(identityEntity.getRemoteId());
      //work-around for SOC-2366 when rename space's display name.
      if (space != null) {
        String groupId = space.getGroupId().split("/")[2];
        stream.setPermaLink(LinkProvider.getActivityUriForSpace(identityEntity.getRemoteId(), groupId));
      }
    } else {
      stream.setPermaLink(LinkProvider.getActivityUri(identityEntity.getProviderId(), identityEntity.getRemoteId()));
    }
    //
    activity.setActivityStream(stream);
    activity.setStreamId(stream.getId());
    activity.setStreamOwner(stream.getPrettyId());

  }

  private List<Identity> getSpacesId(Identity ownerIdentity) {

    List<Identity> identitiesId = new ArrayList<Identity>();
    long offset = 0;
    long limit = 30;
    int loaded = loadIdRange(ownerIdentity, offset, offset + limit, identitiesId);
    while (loaded == limit) {
      loaded = loadIdRange(ownerIdentity, offset, offset + limit, identitiesId);
      offset += limit;
    }

    return identitiesId;

  }

  private int loadIdRange(Identity ownerIdentity, long offset, long limit, List<Identity> result) {
    List<Space> spaces = spaceStorage.getAccessibleSpaces(ownerIdentity.getRemoteId(), offset, limit);
    Identity identity = null;
    for (Space space : spaces) {
      identity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (identity != null) {
        result.add(identity);
      }
      
    }
    return spaces.size();
  }
  
  private Map<String, Identity> getSpacesIdOfIdentity(Identity identity) {

    Map<String, Identity> identitiesId = new HashMap<String, Identity>();
    List<Space> spaces = spaceStorage.getAccessibleSpaces(identity.getRemoteId());
    for (Space space : spaces) {
      identitiesId.put(space.getPrettyName(), identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName()));
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

  private ActivityStorage getStorage() {
    if (activityStorage == null) {
      activityStorage = (ActivityStorage) PortalContainer.getInstance().getComponentInstanceOfType(ActivityStorage.class);
    }
    
    return activityStorage;
  }

  /*
   * Public
   */

  /**
   * {@inheritDoc}
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
      //throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_GET_ACTIVITY, e.getMessage(), e);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getUserActivities(Identity owner) throws ActivityStorageException {

    return getUserActivities(owner, 0, getNumberOfUserActivities(owner));

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getUserActivities(Identity owner, long offset, long limit) throws ActivityStorageException {
    
    List<ExoSocialActivity> got = streamStorage.getMyActivities(owner, (int)offset, (int)limit);
    //
    if (got.size() == limit) {
      return got;
    }
    //
    long remaind = limit - got.size();
    try {
      if (remaind > 0) {

        IdentityEntity identity = _findById(IdentityEntity.class, owner.getId());
        long lastMigration = identity.getStreams() != null ? identity.getStreams()
                                                                     .getOwner()
                                                                     .getLastMigration() : 0;

        if (lastMigration > 0) {
          List<ExoSocialActivity> origin = getOlderUserActivities(owner,
                                                                  lastMigration,
                                                                  (int)limit);
          
          //fill to enough limit
          long i = remaind;
          for (ExoSocialActivity activity : origin) {
            got.add(activity);
            if (--i == 0) {
              break;
            }
          }

          StreamInvocationHelper.createMyActivitiesActivityRef(owner, origin);
        }
      }
      //Streams/MyActivities stream is empty
      if (remaind == limit) {
        List<ExoSocialActivity> origin = getUserActivitiesForUpgrade(owner, offset, limit);
        got.addAll(origin);
        StreamInvocationHelper.createMyActivitiesActivityRef(owner, origin);
      }

    } catch (NodeNotFoundException e) {
      LOG.warn("getUserActivities processing failed " + e.getMessage());
    }

    return got;
  }
  
  /**
   * {@inheritDoc}
   */
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {

    try {

      //
      long currentMillis = System.currentTimeMillis();
      long commentMillis = (comment.getPostedTime() != null ? comment.getPostedTime() : currentMillis);
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
      ActivityEntity commentEntity = activityEntity.createComment(String.valueOf(commentMillis));

      //
      List<String> mentioners = new ArrayList<String>();
      activityEntity.setMentioners(processMentions(activity.getMentionedIds(), comment.getTitle(), mentioners, true));
      
      //activityEntity.setMentioners(processMentions(activity.getMentionedIds(), comment.getTitle(), true));
      
      //
      
      List<String> commenters = new ArrayList<String>();
      activityEntity.setCommenters(processCommenters(activity.getCommentedIds(), comment.getUserId(), commenters, true));
      
      //
      long oldUpdated = activityEntity.getLastUpdated();
      activityEntity.getComments().add(commentEntity);
      activityEntity.setLastUpdated(currentMillis);
      commentEntity.setTitle(comment.getTitle());
      commentEntity.setType(comment.getType());
      commentEntity.setTitleId(comment.getTitleId());
      commentEntity.setBody(comment.getBody());
      commentEntity.setIdentity(activityEntity.getIdentity());
      commentEntity.setPosterIdentity(_findById(IdentityEntity.class, comment.getUserId()));
      commentEntity.setComment(Boolean.TRUE);
      commentEntity.setPostedTime(commentMillis);
      commentEntity.setLastUpdated(commentMillis);
      
      HidableEntity hidable = _getMixin(commentEntity, HidableEntity.class, true);
      hidable.setHidden(comment.isHidden());
      
      comment.setId(commentEntity.getId());

      Map<String, String> params = comment.getTemplateParams();
      if (params != null) {
        commentEntity.putParams(params);
      }
      
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
      
      //
      activity.setUpdated(currentMillis);
      
      //
      if (mustInjectStreams) {
        Identity identity = identityStorage.findIdentityById(comment.getUserId());
        StreamInvocationHelper.updateCommenter(identity, activity, commenters.toArray(new String[0]));
        StreamInvocationHelper.update(activity, mentioners.toArray(new String[0]));
      }
    }  
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_COMMENT, e.getMessage(), e);
    }
    
    StorageUtils.persist();
    
    //
    LOG.debug(String.format(
        "Comment %s by %s (%s) created",
        comment.getTitle(),
        comment.getUserId(),
        comment.getId()
    ));
  }

  /**
   * {@inheritDoc}
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

        String[] mentioners = _createActivity(owner, activity);
        //create refs
        //streamStorage.save(owner, activity);
        if (mustInjectStreams) {
          //run synchronous
          StreamInvocationHelper.savePoster(owner, activity);
          //run asynchronous
          StreamInvocationHelper.save(owner, activity, mentioners);
        }
      }
      else {
        _saveActivity(activity);
      }

      StorageUtils.persist();

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
   * {@inheritDoc}
   */
  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) throws ActivityStorageException {

    try {

      ActivityEntity commentEntity = _findById(ActivityEntity.class, comment.getId());
      ActivityEntity parentActivityEntity = commentEntity.getParentActivity();

      return getStorage().getActivity(parentActivityEntity.getId());
      
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_GET_ACTIVITY, e.getMessage(), e);
    }

  }

  /**
   * {@inheritDoc}
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

      // remove mentions information
      if (activityEntity.isComment()) {
        ActivityEntity activityEntityOfComment = activityEntity.getParentActivity();
        
        //
        List<String> mentioners = new ArrayList<String>();
        activityEntityOfComment.setMentioners(processMentions(activityEntityOfComment.getMentioners(), activityEntity.getTitle(), mentioners, false));
        
        
        //
        List<String> commenters = new ArrayList<String>();
        activityEntityOfComment.setCommenters(processCommenters(activityEntityOfComment.getCommenters(), activityEntity.getPosterIdentity().getId(), commenters, false));
        //
        if (mustInjectStreams) {
          ExoSocialActivity parentActivity = getActivity(activityEntityOfComment.getId());
          StreamInvocationHelper.deleteComment(parentActivity, mentioners.toArray(new String[0]), commenters.toArray(new String[0]));
        }
        
      } else {
        List<String> mentioners = new ArrayList<String>();
        activityEntity.setMentioners(processMentions(activityEntity.getMentioners(), activityEntity.getTitle(), mentioners, false));
        //
        if (mustInjectStreams) {
          StreamInvocationHelper.deleteComment(activity, mentioners.toArray(new String[0]), null);
        }
      }
      
      //create refs
      streamStorage.delete(activityId);

      //
      _removeById(ActivityEntity.class, activityId);

      //
      if (dayEntity != null) { // False when activity is a comment
        dayEntity.desc();
      }

      //
      //getSession().save();
      StorageUtils.persist();

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
   * {@inheritDoc}
   */
  public void deleteComment(String activityId, String commentId) throws ActivityStorageException {
    deleteActivity(commentId);
  }
  
  /**
   * {@inheritDoc}
   */
  private ExoSocialActivity getActivityById(String activityId) throws ActivityStorageException {

    try {

      //
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activityId);
      ExoSocialActivity activity = new ExoSocialActivityImpl();

      //
      activity.setId(activityEntity.getId());
      //
      return activity;

    }
    catch (NodeNotFoundException e) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivitiesOfIdentities(List<Identity> connectionList,
                                                           long offset, long limit) throws ActivityStorageException {

    return getActivitiesOfIdentities(connectionList, null, offset, limit);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivitiesOfIdentities(
      List<Identity> connectionList, TimestampType type, long offset, long limit)
      throws ActivityStorageException {

    //
    if (connectionList.size() == 0) {
      return Collections.emptyList();
    }

    QueryResult<ActivityEntity> results = getActivitiesOfIdentitiesQuery(connectionList, type).objects(offset, limit);

    List<ExoSocialActivity> activities =  new ArrayList<ExoSocialActivity>();

    while(results.hasNext()) {
      activities.add(getStorage().getActivity(results.next().getId()));
    }

    return activities;
  }

  /**
   * {@inheritDoc}
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

    builder.where(whereExpression.toString());
    if (TimestampType.UPDATED.equals(type)) {
      builder.orderBy(ActivityEntity.lastUpdated.getName(), Ordering.DESC).
              orderBy(ActivityEntity.postedTime.getName(), Ordering.DESC);
    } else {
      builder.orderBy(ActivityEntity.postedTime.getName(), Ordering.DESC);
    }

    return builder.get();
  }
  
  /**
   * {@inheritDoc}
   */
  public int getNumberOfUserActivities(Identity owner) throws ActivityStorageException {

    boolean hasSize = streamStorage.hasSizeOfMyActivities(owner);
    
    //migration lazily
    if (hasSize == false) {
      int size = getNumberOfUserActivitiesForUpgrade(owner);
      streamStorage.migrateStreamSize(owner, size, ActivityRefType.MY_ACTIVITIES);
      return size;
    }
    
    return streamStorage.getNumberOfMyActivities(owner);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    if (ownerIdentity == null) {
      return 0;
    }
    
    //
    ActivityFilter filter = ActivityFilter.older();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(baseActivity.getPostedTime()));
    
    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().owners(ownerIdentity), filter).objects().size();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity,
                                                          int limit) {

    if (ownerIdentity == null) {
      return Collections.emptyList();
    }
    
    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(baseActivity.getUpdated().getTime()));
    
    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(ownerIdentity), filter, 0, limit);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {


    if (ownerIdentity == null) {
      return 0;
    }
    
    //
    ActivityFilter filter = ActivityFilter.older();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(baseActivity.getUpdated().getTime()));
    
    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(ownerIdentity), filter).objects().size();
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity,
                                                          int limit) {

    if (ownerIdentity == null) {
      return Collections.emptyList();
    }
    
    //
    ActivityFilter filter = ActivityFilter.older();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(baseActivity.getUpdated().getTime()));
    
    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(ownerIdentity), filter, 0, limit);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivityFeed(Identity ownerIdentity, int offset, int limit) {
    List<ExoSocialActivity> got = streamStorage.getFeed(ownerIdentity, offset, limit);

    if (got.size() == limit) {
      return got;
    }
    
    
    int remaind = limit - got.size();
    try {
      if (remaind > 0) {

        IdentityEntity identity = _findById(IdentityEntity.class, ownerIdentity.getId());
        long lastMigration = identity.getStreams() != null ? identity.getStreams()
                                                                     .getAll()
                                                                     .getLastMigration() : 0;

        if (lastMigration > 0) {
          int i = remaind;
          List<ExoSocialActivity> origin = getOlderFeedActivities(ownerIdentity,
                                                                  lastMigration,
                                                                  limit);

          //fill to enough limit
          for (ExoSocialActivity activity : origin) {
            got.add(activity);
            if (--i == 0) {
              break;
            }
          }

          StreamInvocationHelper.createFeedActivityRef(ownerIdentity, origin);
        }
      }
      //Streams/Feed is empty
      if (remaind == limit) {
        List<ExoSocialActivity> origin = getActivityFeedForUpgrade(ownerIdentity, offset, limit);
        got.addAll(origin);
        StreamInvocationHelper.createFeedActivityRef(ownerIdentity, origin);
      }

    } catch (NodeNotFoundException e) {
      LOG.warn("getActivityFeed processing failed " + e.getMessage());
    }

    return got;
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfActivitesOnActivityFeed(Identity ownerIdentity) {

    boolean hasSize = streamStorage.hasSizeOfFeed(ownerIdentity);
    
    //migration lazily
    if (hasSize == false) {
      int size = getNumberOfActivitesOnActivityFeedForUpgrade(ownerIdentity);
      streamStorage.migrateStreamSize(ownerIdentity, size, ActivityRefType.FEED);
      return size;
    }
    
    return streamStorage.getNumberOfFeed(ownerIdentity);

  }

  @Override
  public int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, Long sinceTime) {
    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);

    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(sinceTime));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(identities), filter).objects().size();

  }
  
  @Override
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, Long sinceTime) {
    //
    if (ownerIdentity == null) {
      return 0;
    }

    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(sinceTime));

    //
    return getActivitiesOfIdentitiesQuery (ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(ownerIdentity), filter).objects().size();
                                      
  }

  @Override
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, Long sinceTime) {
    //
    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);
    connectionList.add(ownerIdentity);

    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(sinceTime));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().owners(connectionList), filter).objects().size();
  }

  @Override
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, Long sinceTime) {
    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);

    //
    if (spaceList.size() == 0) {
      return 0;
    }
    
    //
    ActivityFilter filter = ActivityFilter.spaceNewer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(sinceTime));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.space().owners(spaceList), filter).objects().size();

  }
  
  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);

    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(identities), filter).objects().size();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity,
                                                         int limit) {
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);
    
    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(identities), filter, 0, limit);

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);

    //
    ActivityFilter filter = ActivityFilter.older();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(identities), filter).objects().size();
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity,
                                                         int limit) {
    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);

    //
    ActivityFilter filter = ActivityFilter.older();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(identities), filter, 0, limit);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity, int offset, int limit) {
    List<ExoSocialActivity> got = streamStorage.getConnections(ownerIdentity, offset, limit);
    //
    if (got.size() == limit) {
      return got;
    }
    //
    int remaind = limit - got.size();
    try {
      if (remaind > 0) {

        IdentityEntity identity = _findById(IdentityEntity.class, ownerIdentity.getId());
        long lastMigration = identity.getStreams() != null ? identity.getStreams()
                                                                     .getConnections()
                                                                     .getLastMigration() : 0;

        if (lastMigration > 0) {
          List<ExoSocialActivity> origin = getOlderActivitiesOfConnections(ownerIdentity,
                                                                  lastMigration,
                                                                  limit);
          
          //fill to enough limit
          int i = remaind;
          for (ExoSocialActivity activity : origin) {
            got.add(activity);
            if (--i == 0) {
              break;
            }
          }

          StreamInvocationHelper.createConnectionsActivityRef(ownerIdentity, origin);
        }
      }
      //Streams/Connections stream is empty
      if (remaind == limit) {
        List<ExoSocialActivity> origin = getActivitiesOfConnectionsForUpgrade(ownerIdentity, offset, limit);
        got.addAll(origin);
        StreamInvocationHelper.createConnectionsActivityRef(ownerIdentity, origin);
      }

    } catch (NodeNotFoundException e) {
      LOG.warn("getActivityConnections processing failed " + e.getMessage());
    }

    return got;
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfActivitiesOfConnections(Identity ownerIdentity) {

    boolean hasSize = streamStorage.hasSizeOfConnections(ownerIdentity);
    
    //migration lazily
    if (hasSize == false) {
      int size = getNumberOfActivitiesOfConnectionsForUpgrade(ownerIdentity);
      streamStorage.migrateStreamSize(ownerIdentity, size, ActivityRefType.CONNECTION);
      return size;
    }
    
    return streamStorage.getNumberOfConnections(ownerIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivitiesOfIdentity(Identity ownerIdentity, long offset, long limit)
      throws ActivityStorageException {

    return getUserActivities(ownerIdentity, offset, limit);
    
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);
    connectionList.add(ownerIdentity);

    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().owners(connectionList), filter).objects().size();
    
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnActivitiesOfConnections(
      Identity ownerIdentity, ExoSocialActivity baseActivity, long limit) {

    //
    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);
    connectionList.add(ownerIdentity);

    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().owners(connectionList), filter, 0, limit);

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);
    connectionList.add(ownerIdentity);
    
    //
    ActivityFilter filter = ActivityFilter.older();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().owners(connectionList), filter).objects().size();
    
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnActivitiesOfConnections(Identity ownerIdentity,
                                                                  ExoSocialActivity baseActivity, int limit) {

    //
    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);
    connectionList.add(ownerIdentity);

    //
    ActivityFilter filter = ActivityFilter.older();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().owners(connectionList), filter, 0, limit);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getUserSpacesActivities(Identity ownerIdentity, int offset, int limit) {
    List<ExoSocialActivity> got = streamStorage.getMySpaces(ownerIdentity, offset, limit);
    //
    if (got.size() == limit) {
      return got;
    }
    //
    int remaind = limit - got.size();
    try {
      if (remaind > 0) {

        IdentityEntity identity = _findById(IdentityEntity.class, ownerIdentity.getId());
        long lastMigration = identity.getStreams() != null ? identity.getStreams()
                                                                     .getMySpaces()
                                                                     .getLastMigration() : 0;

        if (lastMigration > 0) {
          List<ExoSocialActivity> origin = getOlderUserSpacesActivities(ownerIdentity,
                                                                  lastMigration,
                                                                  limit);
          
          //fill to enough limit
          int i = remaind;
          for (ExoSocialActivity activity : origin) {
            got.add(activity);
            if (--i == 0) {
              break;
            }
          }

          StreamInvocationHelper.createMySpacesActivityRef(ownerIdentity, origin);
        }
      }
      //Streams/Connections stream is empty
      if (remaind == limit) {
        List<ExoSocialActivity> origin = getUserSpacesActivitiesForUpgrade(ownerIdentity, offset, limit);
        got.addAll(origin);
        StreamInvocationHelper.createMySpacesActivityRef(ownerIdentity, origin);
      }

    } catch (NodeNotFoundException e) {
      LOG.warn("getUserSpacesActivities processing failed " + e.getMessage());
    }

    return got;
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfUserSpacesActivities(Identity ownerIdentity) {

   boolean hasSize = streamStorage.hasSizeOfMySpaces(ownerIdentity);
    
    //migration lazily
    if (hasSize == false) {
      int size = getNumberOfUserSpacesActivitiesForUpgrade(ownerIdentity);
      streamStorage.migrateStreamSize(ownerIdentity, size, ActivityRefType.MY_SPACES);
      return size;
    }
    
    return streamStorage.getNumberOfMySpaces(ownerIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);

    //
    if (spaceList.size() == 0) {
      return 0;
    }
    
    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().owners(spaceList), filter).objects().size();
    
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerOnUserSpacesActivities(Identity ownerIdentity,
                                                                ExoSocialActivity baseActivity, int limit) {

    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);
    
    //
    if (spaceList.size() == 0) {
      return Collections.emptyList();
    }
    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().owners(spaceList), filter, 0, limit);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {

    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);

    //
    if (spaceList.size() == 0) {
      return 0;
    }

    //
    ActivityFilter filter = ActivityFilter.older();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().owners(spaceList), filter).objects().size();
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderOnUserSpacesActivities(Identity ownerIdentity,
                                                                ExoSocialActivity baseActivity, int limit) {

    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);
    
    //
    if (spaceList.size() == 0) {
      return Collections.emptyList();
    }

    //
    ActivityFilter filter = ActivityFilter.older();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().owners(spaceList), filter, 0, limit);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getComments(ExoSocialActivity existingActivity, int offset, int limit) {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    String[] commentIds = getStorage().getActivity(existingActivity.getId()).getReplyToId();
    
    //
    limit = (limit > commentIds.length ? commentIds.length : limit);
    
    for (int i = offset ; i < commentIds.length ; i++) {
      if (isHidden(commentIds[i]) == false) {
        activities.add(getStorage().getActivity(commentIds[i]));
        
        //
        if (activities.size() == limit) {
          break;
        }
      }
      
      
    }
    return activities;
  }
  
  /**
   * Checks specified comment is hidden or not
   * @param commentId
   * @return TRUE hidden comment/ FALSE otherwise
   */
  private boolean isHidden(String commentId) {

    try {
      ActivityEntity activityEntity = _findById(ActivityEntity.class, commentId);
      HidableEntity hidable = _getMixin(activityEntity, HidableEntity.class, false);
      if (hidable != null) {
        return hidable.getHidden();
      }
    } catch (NodeNotFoundException e) {
      return true;
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfComments(ExoSocialActivity existingActivity) {
    //return getStorage().getActivity(existingActivity.getId()).getReplyToId().length;
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();

    //
    List<String> commentIds = Arrays.asList(getStorage().getActivity(existingActivity.getId()).getReplyToId());

    //
    for(String commentId : commentIds) {
      ExoSocialActivity comment = getActivity(commentId);
      if (!comment.isHidden())
        activities.add(getStorage().getActivity(commentId));
    }

    //
    return activities.size();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment) {

    //List<String> commentId = Arrays.asList(getStorage().getActivity(existingActivity.getId()).getReplyToId());
    //return commentId.indexOf(baseComment.getId());

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();

    //
    List<String> commentIds = Arrays.asList(getStorage().getActivity(existingActivity.getId()).getReplyToId());
    int baseIndex = commentIds.indexOf(baseComment.getId());
    if (baseIndex > commentIds.size()) {
      baseIndex = commentIds.size();
    }

    //
    for(String commentId : commentIds.subList(0, baseIndex)) {
      ExoSocialActivity comment = getActivity(commentId);
      if (!comment.isHidden())
        activities.add(getStorage().getActivity(commentId));
    }

    //
    return activities.size();
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getNewerComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment,
                                           int limit) {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();

    //
    List<String> commentIds = Arrays.asList(getStorage().getActivity(existingActivity.getId()).getReplyToId());
    int baseIndex = commentIds.indexOf(baseComment.getId());
    if (baseIndex > limit) {
      baseIndex = limit;
    }

    //
    for(String commentId : commentIds.subList(0, baseIndex)) {
      ExoSocialActivity comment = getActivity(commentId);
      if (!comment.isHidden())
        activities.add(getStorage().getActivity(commentId));
    }

    //
    return activities;

  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment) {

    /*List<String> commentIds = Arrays.asList(getStorage().getActivity(existingActivity.getId()).getReplyToId());
    int index = commentIds.indexOf(baseComment.getId());

    return (commentIds.size() - index - 1);*/
    
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();

    //
    List<String> commentIds = Arrays.asList(getStorage().getActivity(existingActivity.getId()).getReplyToId());
    int baseIndex = commentIds.indexOf(baseComment.getId());

    //
    for(String commentId : commentIds.subList(baseIndex + 1, commentIds.size())) {
      ExoSocialActivity comment = getActivity(commentId);
      if (!comment.isHidden())
        activities.add(getStorage().getActivity(commentId));
    }

    //
    return activities.size();

  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOlderComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment,
                                           int limit) {

    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();

    //
    List<String> commentIds = Arrays.asList(getStorage().getActivity(existingActivity.getId()).getReplyToId());
    int baseIndex = commentIds.indexOf(baseComment.getId());

    //
    for(String commentId : commentIds.subList(baseIndex + 1, limit)) {
      ExoSocialActivity comment = getActivity(commentId);
      if (!comment.isHidden())
        activities.add(getStorage().getActivity(commentId));
    }

    //
    return activities;

  }

  /**
   * {@inheritDoc}
   */
  public SortedSet<ActivityProcessor> getActivityProcessors() {
    return activityProcessors;
  }

  /**
   * {@inheritDoc}
   */
  public void updateActivity(ExoSocialActivity changedActivity) throws ActivityStorageException {

    try {
      ActivityEntity activityEntity = _findById(ActivityEntity.class, changedActivity.getId());

      if (changedActivity.getTitle() == null) changedActivity.setTitle(activityEntity.getTitle());
      if (changedActivity.getBody() == null) changedActivity.setBody(activityEntity.getBody());
      
      _saveActivity(changedActivity);
      
      getSession().save();
      
    }
    catch (NodeNotFoundException e) {
      throw new ActivityStorageException(
          ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY,
          e.getMessage()
      );
    }

  }
  
  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivitiesOfIdentities(ActivityBuilderWhere where, ActivityFilter filter,
                                                           long offset, long limit) throws ActivityStorageException {

    QueryResult<ActivityEntity> results = getActivitiesOfIdentitiesQuery(where, filter).objects(offset, limit);

    List<ExoSocialActivity> activities =  new ArrayList<ExoSocialActivity>();

    while(results.hasNext()) {
      activities.add(getStorage().getActivity(results.next().getId()));
    }

    return activities;
  }
  
  /**
   * {@inheritDoc}
   */
  private Query<ActivityEntity> getActivitiesOfIdentitiesQuery(ActivityBuilderWhere whereBuilder,
                                                               JCRFilterLiteral filter) throws ActivityStorageException {

    QueryBuilder<ActivityEntity> builder = getSession().createQueryBuilder(ActivityEntity.class);

    builder.where(whereBuilder.build(filter));
    whereBuilder.orderBy(builder, filter);

    return builder.get();
  }
  
  

  /**
   * {@inheritDoc}
   */
  public void setStorage(final ActivityStorage storage) {
    this.activityStorage = storage;
  }

  /**
   * Processes Mentioners who mention via the Activity.
   * 
   * @param mentionerIds
   * @param title
   * @param isAdded
   * @return list of added IdentityIds who mentioned
   */
  private String[] processMentions(String[] mentionerIds, String title, List<String> addedOrRemovedIds, boolean isAdded) {
    if (title == null || title.length() == 0) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }
    
    Matcher matcher = MENTION_PATTERN.matcher(title);
    while (matcher.find()) {
      String remoteId = matcher.group().substring(1);
      if (!USER_NAME_VALIDATOR_REGEX.matcher(remoteId).matches()) {
        continue;
      }
      Identity identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, remoteId);
      // if not the right mention then ignore
      if (identity != null) { 
        String mentionStr = identity.getId() + MENTION_CHAR; // identityId@
        mentionerIds = isAdded ? add(mentionerIds, mentionStr, addedOrRemovedIds) : remove(mentionerIds, mentionStr, addedOrRemovedIds);
      }
    }
    return mentionerIds;
  }
  
  private String[] processCommenters(String[] commenters, String commenter, List<String> addedOrRemovedIds, boolean isAdded) {
    if (commenter == null || commenter.length() == 0) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }
    
    String newCommenter = commenter + MENTION_CHAR; 
    commenters = isAdded ? add(commenters, newCommenter, addedOrRemovedIds) : remove(commenters, newCommenter, addedOrRemovedIds);
    
    return commenters;
  }

  private String[] add(String[] mentionerIds, String mentionStr, List<String> addedOrRemovedIds) {
    if (ArrayUtils.toString(mentionerIds).indexOf(mentionStr) == -1) { // the first mention
      addedOrRemovedIds.add(mentionStr.replace(MENTION_CHAR, ""));
      return (String[]) ArrayUtils.add(mentionerIds, mentionStr + 1);
    }
    
    String storedId = null;
    for (String mentionerId : mentionerIds) {
      if (mentionerId.indexOf(mentionStr) != -1) {
        mentionerIds = (String[]) ArrayUtils.removeElement(mentionerIds, mentionerId);
        storedId = mentionStr + (Integer.parseInt(mentionerId.split(MENTION_CHAR)[1]) + 1);
        break;
      }
    }
    

    addedOrRemovedIds.add(mentionStr.replace(MENTION_CHAR, ""));
    mentionerIds = (String[]) ArrayUtils.add(mentionerIds, storedId);
    return mentionerIds;
  }

  private String[] remove(String[] mentionerIds, String mentionStr, List<String> addedOrRemovedIds) {
    for (String mentionerId : mentionerIds) {
      if (mentionerId.indexOf(mentionStr) != -1) {
        int numStored = Integer.parseInt(mentionerId.split(MENTION_CHAR)[1]) - 1;
        
        if (numStored == 0) {
          addedOrRemovedIds.add(mentionStr.replace(MENTION_CHAR, ""));
          return (String[]) ArrayUtils.removeElement(mentionerIds, mentionerId);
        }

        mentionerIds = (String[]) ArrayUtils.removeElement(mentionerIds, mentionerId);
        mentionerIds = (String[]) ArrayUtils.add(mentionerIds, mentionStr + numStored);
        addedOrRemovedIds.add(mentionStr.replace(MENTION_CHAR, ""));
        break;
      }
    }
    return mentionerIds;
  }

  @Override
  public int getNumberOfSpaceActivities(Identity spaceIdentity) {
    boolean hasSize = streamStorage.hasSizeOfSpaceStream(spaceIdentity);
    
    //migration lazily
    if (hasSize == false) {
      int size = getNumberOfSpaceActivitiesForUpgrade(spaceIdentity);
      streamStorage.migrateStreamSize(spaceIdentity, size, ActivityRefType.SPACE_STREAM);
      return size;
    }
    
    return streamStorage.getNumberOfSpaceStream(spaceIdentity);
  }
  
  @Override
  public int getNumberOfSpaceActivitiesForUpgrade(Identity spaceIdentity) {
    //
    if (spaceIdentity == null) {
      return 0;
    }

    //
    ActivityFilter filter = ActivityFilter.space();

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.space().owners(spaceIdentity), filter).objects().size();
  }

  @Override
  public List<ExoSocialActivity> getSpaceActivities(Identity spaceIdentity, int index, int limit) {
    //return streamStorage.getSpaceStream(spaceIdentity, index, limit);
    
    List<ExoSocialActivity> got = streamStorage.getSpaceStream(spaceIdentity, index, limit);

    if (got.size() == limit) {
      return got;
    }
    
    
    int remaind = limit - got.size();
    try {
      if (remaind > 0) {

        IdentityEntity identity = _findById(IdentityEntity.class, spaceIdentity.getId());
        long lastMigration = identity.getStreams() != null ? identity.getStreams()
                                                                     .getSpace()
                                                                     .getLastMigration() : 0;

        if (lastMigration > 0) {
          int i = remaind;
          List<ExoSocialActivity> origin = getOlderSpaceActivities(spaceIdentity,
                                                                  lastMigration,
                                                                  limit);

          //fill to enough limit
          for (ExoSocialActivity activity : origin) {
            got.add(activity);
            if (--i == 0) {
              break;
            }
          }

          StreamInvocationHelper.createSpaceActivityRef(spaceIdentity, origin);
        }
      }
      //Streams/Space is empty
      if (remaind == limit) {
        List<ExoSocialActivity> origin = getActivityFeedForUpgrade(spaceIdentity, index, limit);
        got.addAll(origin);
        StreamInvocationHelper.createSpaceActivityRef(spaceIdentity, origin);
      }

    } catch (NodeNotFoundException e) {
      LOG.warn("getSpaceActivities processing failed " + e.getMessage());
    }

    return got;
  }
  
  @Override
  public List<ExoSocialActivity> getSpaceActivitiesForUpgrade(Identity spaceIdentity, int index, int limit) {
    //
    if (spaceIdentity == null) {
      return Collections.emptyList();
    }
  
    //
    ActivityFilter filter = ActivityFilter.space();
  
    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.space().owners(spaceIdentity), filter, 0, limit);
  
  }

  @Override
  public List<ExoSocialActivity> getNewerOnSpaceActivities(Identity spaceIdentity,
                                                           ExoSocialActivity baseActivity,
                                                           int limit) {
    if (spaceIdentity == null) {
      return Collections.emptyList();
    }
    
    //
    ActivityFilter filter = ActivityFilter.spaceNewer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.space().owners(spaceIdentity), filter, 0, limit);
  }

  @Override
  public int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity,
                                               ExoSocialActivity baseActivity) {
    //
    if (spaceIdentity == null) {
      return 0;
    }
    
    //
    ActivityFilter filter = ActivityFilter.spaceNewer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.space().owners(spaceIdentity), filter).objects().size();
  }

  @Override
  public List<ExoSocialActivity> getOlderOnSpaceActivities(Identity spaceIdentity,
                                                            ExoSocialActivity baseActivity,
                                                            int limit) {
    //
    List<Identity> spaceList = getSpacesId(spaceIdentity);
    
    //
    if (spaceList.size() == 0) {
      return Collections.emptyList();
    }

    //
    ActivityFilter filter = ActivityFilter.spaceOlder();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.space().owners(spaceList), filter, 0, limit);
  }

  @Override
  public int getNumberOfOlderOnSpaceActivities(Identity spaceIdentity,
                                               ExoSocialActivity baseActivity) {
    //
    if (spaceIdentity == null) {
      return 0;
    }

    //
    ActivityFilter filter = ActivityFilter.spaceOlder();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(baseActivity.getUpdated().getTime()));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.space().owners(spaceIdentity), filter).objects().size();
  }

  @Override
  public int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity, Long sinceTime) {
    //
    if (spaceIdentity == null) {
      return 0;
    }
    
    //
    ActivityFilter filter = ActivityFilter.spaceNewer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(sinceTime));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.space().owners(spaceIdentity), filter).objects().size();
  }
  
  @Override
  public int getNumberOfMultiUpdated(Identity owner, Map<String, Long> sinceTimes) {
    //
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    List<String> activityIds = new ArrayList<String>();
    
    if ( sinceTimes.get("CONNECTIONS") != null ) {
      List<ExoSocialActivity> connectionsActivities =  getNewerActivitiesOfConnections(owner, sinceTimes.get("CONNECTIONS"), 100);
      activities.addAll(connectionsActivities);
      for ( ExoSocialActivity connectionsActivity : connectionsActivities ) {
        activityIds.add(connectionsActivity.getId());
      }
    }
    
    if ( sinceTimes.get("MY_SPACE") != null ) {
      List<ExoSocialActivity> mySpaceActivities = getNewerUserSpacesActivities(owner, sinceTimes.get("MY_SPACE"), 100);  
      for ( ExoSocialActivity mySpaceActivity : mySpaceActivities ) {
        if ( !activityIds.contains(mySpaceActivity.getId()) ) {
          activities.add(mySpaceActivity);
          activityIds.add(mySpaceActivity.getId());
        }
      }
    }
    
    if ( sinceTimes.get("MY_ACTIVITIES") != null ) {
      List<ExoSocialActivity> myActivities = getNewerUserActivities(owner, sinceTimes.get("MY_ACTIVITIES"), 100);
      for ( ExoSocialActivity myActivity : myActivities ) {
        if ( !activityIds.contains(myActivity.getId()) ) {
          activities.add(myActivity);
          activityIds.add(myActivity.getId());
        }
      }
    }
    
    return activities.size();
  }
  
  //
  @Override
  public List<ExoSocialActivity> getNewerFeedActivities(Identity owner, Long sinceTime, int limit) {
    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(owner));
    identities.addAll(getSpacesId(owner));
    identities.add(owner);
    
    if ( identities.size() == 0 ) {
      return Collections.emptyList();
    }
    
    //
    JCRFilterLiteral filter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(sinceTime));
    
    return getActivitiesFromQueryResults(getActivitiesOfIdentitiesQuery(ActivityBuilderWhere
      .updated().owners(identities), filter).objects((long)0, (long)limit));
  }
  
  //
  @Override
  public List<ExoSocialActivity> getNewerUserActivities(Identity owner, Long sinceTime, int limit) {
    //
    JCRFilterLiteral filter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(sinceTime));

    //
    return getActivitiesFromQueryResults(getActivitiesOfIdentitiesQuery(
      ActivityBuilderWhere.updated().mentioner(owner).owners(owner), filter).objects((long)0, (long)limit));
  }
  
  @Override
  public List<ExoSocialActivity> getNewerUserSpacesActivities(Identity owner, Long sinceTime, int limit) {
    //
    List<Identity> spaceList = getSpacesId(owner);
    
    if (spaceList.size() == 0) {
      return new ArrayList<ExoSocialActivity>();
    }
    
    JCRFilterLiteral filter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(sinceTime));

    return getActivitiesFromQueryResults(getActivitiesOfIdentitiesQuery(
      ActivityBuilderWhere.updated().owners(spaceList), filter).objects((long)0, (long)limit));
  }
  
  @Override
  public List<ExoSocialActivity> getNewerActivitiesOfConnections(Identity owner, Long sinceTime, int limit) {
    List<Identity> connectionList = relationshipStorage.getConnections(owner);

    if (connectionList.size() == 0) {
      return new ArrayList<ExoSocialActivity>();
    }
    
    //
    JCRFilterLiteral filter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(sinceTime));

    //
    return getActivitiesFromQueryResults(getActivitiesOfIdentitiesQuery(
      ActivityBuilderWhere.updated().owners(connectionList), filter).objects((long)0, (long)limit));
  }
  
  //
  @Override
  public List<ExoSocialActivity> getNewerSpaceActivities(Identity owner, Long sinceTime, int limit) {
    //
    JCRFilterLiteral filter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(sinceTime));

    //
    return getActivitiesFromQueryResults(getActivitiesOfIdentitiesQuery(ActivityBuilderWhere
      .updated().owners(owner), filter).objects((long)0, (long)limit));
  }
  
  private List<ExoSocialActivity> getActivitiesFromQueryResults(QueryResult<ActivityEntity> results) {
    List<ExoSocialActivity> activities =  new ArrayList<ExoSocialActivity>();

    while(results.hasNext()) {
      activities.add(getStorage().getActivity(results.next().getId()));
    }

    return activities;
  }
  //
  
  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, int offset, int limit) {
    return getActivitiesByPoster(posterIdentity, offset, limit, new String[0]);
  }
  
  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, int offset, int limit, String ...activityTypes) {
    //make sure that limit < 10 then get 10 the lastest activites for process.
    int newLimit = Math.max(limit, 10);
    List<ExoSocialActivity> got = getUserActivities(posterIdentity, offset, newLimit);
    List<ExoSocialActivity> result = new ArrayList<ExoSocialActivity>();
    if (got.size() == 0) return result;
    
    int added = 0;
    boolean loadMore = true;
    int newOffset = offset;
    
    //process the code what only retrieves these activities by PosterIdentity
    while(loadMore) {
      //put the activity to result
      for(ExoSocialActivity a : got) {
        if (a.getPosterId() == posterIdentity.getId() && matchActivityTypes(a, activityTypes)) {
          result.add(a);
          if (++added == limit) {
            loadMore = false;
            break;
          }
        }
      }
      
      //load more?
      if (loadMore) {
        newOffset = offset + newLimit;
        got = getUserActivities(posterIdentity, newOffset, newLimit);
        
        //the load more is empty, break
        if (got.size() == 0) {
          loadMore = false;
        }
      }
    }
    
    return result;
  }
  
  /**
   * Determines the activity's type
   * 
   * @param a
   * @param activityTypes
   * @return
   */
  private boolean matchActivityTypes(ExoSocialActivity a, String ...activityTypes) {
    if (activityTypes == null || activityTypes.length == 0) return true;
    
    return ArrayUtils.contains(activityTypes, a.getType());
        
  }
  
  @Override
  public int getNumberOfActivitiesByPoster(Identity posterIdentity) {
    ActivityFilter filter = new ActivityFilter(){};

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().owners(posterIdentity).poster(posterIdentity), filter).objects().size();
  }
  
  @Override
  public int getNumberOfUpdatedOnActivityFeed(Identity owner, ActivityUpdateFilter filter) {
    
    //
    List<Identity> identities = new ArrayList<Identity>();
    
    List<Identity> relationships = relationshipStorage.getConnections(owner);

    identities.addAll(relationships);
    identities.addAll(getSpacesId(owner));
    //identities.add(owner);
    
    if ( identities.size() == 0 ) {
      return 0;
    }
    //
    String[] excludedSpaceActivities = getNumberOfViewedOfActivities(owner, filter.spaceActivitiesType());
    filter.addExcludedActivities(excludedSpaceActivities);
    
    //
    String[] excludedUserSpaceActivities = getNumberOfViewedOfActivities(owner, filter.userSpaceActivitiesType());
    filter.addExcludedActivities(excludedUserSpaceActivities);
    
    //
    String[] excludedConnections = getNumberOfViewedOfActivities(owner, filter.connectionType());
    filter.addExcludedActivities(excludedConnections);
    
    //
    String[] excludedUserActivities = getNumberOfViewedOfActivities(owner, filter.userActivitiesType());
    filter.addExcludedActivities(excludedUserActivities);
    
    //
    long compareTime = filter.activityFeedType().toSinceTime();
    
    //
    JCRFilterLiteral jcrfilter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    jcrfilter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(compareTime));

    //
    Query<ActivityEntity> query = getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.updated().owners(identities).mentioner(owner)
                                                                 .posters(relationships).excludedActivities(filter.excludedActivities()), jcrfilter);
    
    //calculate here
    int gotNumber = counter(query);
    
    if (filter.isRefreshTab() && gotNumber == filter.activityFeedType().lastNumberOfUpdated()) {
      gotNumber = 0;
    }
    
    return gotNumber;
  }
  
  /**
   * Counter of activies what is updated
   * @param query
   * @return
   */
  private int counter(Query<ActivityEntity> query) {
    
    QueryResult<ActivityEntity> results = query.objects();
    
    ActivityEntity entity = null;
    ActivityCounter counter = new ActivityCounter();
    
    //
    while (results.hasNext()) {
      entity = results.next();

      //
      if (entity.isComment()) {
        entity = entity.getParentActivity();
      }

      counter.add(getStorage().getActivity(entity.getId()));

    }
    
    return counter.size();
  }
  
  @Override
  public int getNumberOfUpdatedOnUserActivities(Identity owner, ActivityUpdateFilter filter) {
    
    List<Identity> relationships = relationshipStorage.getConnections(owner);
    //
    String[] excludedSpaceActivities = getNumberOfViewedOfActivities(owner, filter.spaceActivitiesType());
    filter.addExcludedActivities(excludedSpaceActivities);
    
    //
    String[] excludedUserSpaceActivities = getNumberOfViewedOfActivities(owner, filter.userSpaceActivitiesType());
    filter.addExcludedActivities(excludedUserSpaceActivities);
    
    //
    String[] excludedConnections = getNumberOfViewedOfActivities(owner, filter.connectionType());
    filter.addExcludedActivities(excludedConnections);
    
    //
    //long compareTime = filter.isRefreshTab() ? filter.userActivitiesType().fromSinceTime() : filter.userActivitiesType().toSinceTime();
    long compareTime = filter.userActivitiesType().toSinceTime();
    //
    JCRFilterLiteral jcrfilter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    jcrfilter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(compareTime));
    
    //
    Query<ActivityEntity> query = getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.updated().mentioner(owner)
                                                                 .posters(relationships).excludedActivities(filter.excludedActivities()), jcrfilter);
    int gotNumber = counter(query);
    
    if (filter.isRefreshTab() && gotNumber == filter.userActivitiesType().lastNumberOfUpdated()) {
      gotNumber = 0;
    }
    
    return gotNumber;
  }
  
  @Override
  public int getNumberOfUpdatedOnUserSpacesActivities(Identity owner, ActivityUpdateFilter filter) {
    //
    List<Identity> spaceList = getSpacesId(owner);
    
    if (spaceList.size() == 0) {
      return 0;
    }
    
    //
    String[] excludedSpaceActivities = getNumberOfViewedOfActivities(owner, filter.spaceActivitiesType());
    filter.addExcludedActivities(excludedSpaceActivities);
    
    //
    String[] excludedUserActivities = getNumberOfViewedOfActivities(owner, filter.userActivitiesType());
    filter.addExcludedActivities(excludedUserActivities);
    
    //
    String[] excludedConnections = getNumberOfViewedOfActivities(owner, filter.connectionType());
    filter.addExcludedActivities(excludedConnections);
    
    
    //
    //long compareTime = filter.isRefreshTab() ? filter.userSpaceActivitiesType().fromSinceTime() : filter.userSpaceActivitiesType().toSinceTime();
    long compareTime = filter.userSpaceActivitiesType().toSinceTime();
    
    //
    JCRFilterLiteral jcrfilter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    jcrfilter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(compareTime));

    //
    int gotNumber = getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.updated().owners(spaceList)
                                          .excludedActivities(filter.excludedActivities()), jcrfilter).objects().size();
    
    if (filter.isRefreshTab() && gotNumber == filter.userSpaceActivitiesType().lastNumberOfUpdated()) {
      gotNumber = 0;
    }
    
    return gotNumber;
  }
  
  @Override
  public int getNumberOfUpdatedOnActivitiesOfConnections(Identity owner, ActivityUpdateFilter filter) {
    List<Identity> relationships = relationshipStorage.getConnections(owner);

    if (relationships.size() == 0) {
      return 0;
    }
    
    //
    String[] excludedSpaceActivities = getNumberOfViewedOfActivities(owner, filter.spaceActivitiesType());
    filter.addExcludedActivities(excludedSpaceActivities);
    
    //
    String[] excludedUserActivities = getNumberOfViewedOfActivities(owner, filter.userActivitiesType());
    filter.addExcludedActivities(excludedUserActivities);
    
    //
    String[] excludedUserSpaceActivities = getNumberOfViewedOfActivities(owner, filter.userSpaceActivitiesType());
    filter.addExcludedActivities(excludedUserSpaceActivities);
    
    
    //
    long compareTime = filter.connectionType().toSinceTime();
    
    //
    JCRFilterLiteral jcrfilter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    jcrfilter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(compareTime));

    //
    Query<ActivityEntity> query = getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.updated().owners(relationships).posters(relationships)
                                                                 .excludedActivities(filter.excludedActivities()), jcrfilter);
    
    //calculate here
    int gotNumber = counter(query);
    
    if (filter.isRefreshTab() && gotNumber == filter.connectionType().lastNumberOfUpdated()) {
      gotNumber = 0;
    }
    
    return gotNumber;
    
  }
  
  @Override
  public int getNumberOfUpdatedOnSpaceActivities(Identity owner, ActivityUpdateFilter filter) {
    
    //
    String[] excludedConnections = getNumberOfViewedOfActivities(owner, filter.connectionType());
    filter.addExcludedActivities(excludedConnections);
    
    //
    String[] excludedUserActivities = getNumberOfViewedOfActivities(owner, filter.userActivitiesType());
    filter.addExcludedActivities(excludedUserActivities);
    
    //
    String[] excludedUserSpaceActivities = getNumberOfViewedOfActivities(owner, filter.userSpaceActivitiesType());
    filter.addExcludedActivities(excludedUserSpaceActivities);
    
    //
    //long compareTime = filter.isRefreshTab() ? filter.spaceActivitiesType().fromSinceTime() : filter.spaceActivitiesType().toSinceTime();
    long compareTime = filter.spaceActivitiesType().toSinceTime();
    
    //
    JCRFilterLiteral jcrfilter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    jcrfilter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(compareTime));

    //
    int gotNumber = getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.updated().owners(owner)
                                                              .excludedActivities(filter.excludedActivities()), jcrfilter).objects().size();
    
    if (filter.isRefreshTab() && gotNumber == filter.spaceActivitiesType().lastNumberOfUpdated()) {
      gotNumber = 0;
    }
    
    return gotNumber;
  }
  
  
  private String[] getNumberOfViewedOfActivities(Identity owner, ActivityFilterType type) {
    
    if (type.fromSinceTime() == type.toSinceTime()) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }
    
    List<Identity> identities = new ArrayList<Identity>();
    identities.add(owner);
    
    ActivityBuilderWhere where = ActivityBuilderWhere.viewedRange();
    
    switch(type) {
    case CONNECTIONS_ACTIVITIES :
      identities.addAll(relationshipStorage.getConnections(owner));
      break;
    case USER_ACTIVITIES :
      where.mentioner(owner);
      //identities.addAll(relationshipStorage.getConnections(owner));
      break;
    case USER_SPACE_ACTIVITIES :
      identities.addAll(getSpacesId(owner));
      break;
    case SPACE_ACTIVITIES :
      break;
    }
    
    //
    JCRFilterLiteral jcrfilter = ActivityFilter.ACTIVITY_VIEWED_RANGE_FILTER;
    jcrfilter.with(ActivityFilter.ACTIVITY_FROM_UPDATED_POINT_FIELD).value(TimestampType.NEWER.from(type.oldFromSinceTime()));
    jcrfilter.with(ActivityFilter.ACTIVITY_TO_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(type.toSinceTime()));

    //
    QueryResult<ActivityEntity> result = getActivitiesOfIdentitiesQuery(where.owners(identities), jcrfilter).objects();
    String[] excludedActivities = new String[0];
    
    //
    while(result.hasNext()) {
      excludedActivities = (String[]) ArrayUtils.add(excludedActivities, result.next().getId());
    }
    
    return excludedActivities;
  }

  @Override
  public List<ExoSocialActivity> getActivities(Identity owner,
                                               Identity viewer,
                                               long offset,
                                               long limit) throws ActivityStorageException {
    
    List<Identity> queryIdentities = new ArrayList<Identity>();
    queryIdentities.add(owner);
    
    //
    if (viewer != null 
              && owner.getId().equals(viewer.getId()) == false) {
      //
      Relationship rel = relationshipStorage.getRelationship(owner, viewer);
      
      //
      boolean hasRelationship = false;
      if (rel != null && rel.getStatus() == Type.CONFIRMED) {
        hasRelationship = true;
      }
      
      //
      if (hasRelationship) {
        queryIdentities.add(viewer);
      }
    }
    
    //
    List<Identity> spaceIdentityOfOwner = getSpacesId(owner);
    Map<String, Identity> spaceIdentityOfViewer = getSpacesIdOfIdentity(viewer);
    for(Identity identity : spaceIdentityOfOwner) {
      if (spaceIdentityOfViewer.containsKey(identity.getRemoteId())) {
        queryIdentities.add(identity);
      }
    }
    
    //
    ActivityFilter filter = new ActivityFilter(){};

    //
    return getOwnerActivitiesOfIdentities(ActivityBuilderWhere.owner().owners(queryIdentities).mentioner(owner).poster(owner), filter, offset, limit);
  }
  
  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getOwnerActivitiesOfIdentities(ActivityBuilderWhere where, ActivityFilter filter,
                                                           long offset, long limit) throws ActivityStorageException {

    Query<ActivityEntity> query = getActivitiesOfIdentitiesQuery(where, filter);
    
    QueryResult<ActivityEntity> results = query.objects();
    
    ActivityEntity entity = null;
    long totalSize = results.size();
    
    ActivityIterator activityIt = new ActivityIterator(offset, limit, totalSize);
      
    //
    while (results.hasNext()) {
      entity = results.next();

      //
      if (entity.isComment()) {
        entity = entity.getParentActivity();
      }

      activityIt.add(getStorage().getActivity(entity.getId()));

      //
      if (activityIt.addMore() == false) {
        break;
      }
    }
    
    return activityIt.result();
  }
  
  //
  @Override
  public List<ExoSocialActivity> getOlderFeedActivities(Identity owner, Long sinceTime, int limit) {
    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(owner));
    identities.addAll(getSpacesId(owner));
    identities.add(owner);
    
    if ( identities.size() == 0 ) {
      return Collections.emptyList();
    }
    
    //
    JCRFilterLiteral filter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(sinceTime));
    
    return getActivitiesFromQueryResults(getActivitiesOfIdentitiesQuery(ActivityBuilderWhere
      .updated().owners(identities), filter).objects((long)0, (long)limit));
  }
  
  //
  @Override
  public List<ExoSocialActivity> getOlderUserActivities(Identity owner, Long sinceTime, int limit) {
    //
    JCRFilterLiteral filter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(sinceTime));

    //
    return getActivitiesFromQueryResults(getActivitiesOfIdentitiesQuery(
      ActivityBuilderWhere.updated().mentioner(owner).owners(owner), filter).objects((long)0, (long)limit));
  }
  
  @Override
  public List<ExoSocialActivity> getOlderUserSpacesActivities(Identity owner, Long sinceTime, int limit) {
    //
    List<Identity> spaceList = getSpacesId(owner);
    
    if (spaceList.size() == 0) {
      return new ArrayList<ExoSocialActivity>();
    }
    
    JCRFilterLiteral filter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(sinceTime));

    return getActivitiesFromQueryResults(getActivitiesOfIdentitiesQuery(
      ActivityBuilderWhere.updated().owners(spaceList), filter).objects((long)0, (long)limit));
  }
  
  @Override
  public List<ExoSocialActivity> getOlderActivitiesOfConnections(Identity owner, Long sinceTime, int limit) {
    List<Identity> connectionList = relationshipStorage.getConnections(owner);

    if (connectionList.size() == 0) {
      return new ArrayList<ExoSocialActivity>();
    }
    
    //
    JCRFilterLiteral filter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(sinceTime));

    //
    return getActivitiesFromQueryResults(getActivitiesOfIdentitiesQuery(
      ActivityBuilderWhere.updated().owners(connectionList), filter).objects((long)0, (long)limit));
  }
  
  //
  @Override
  public List<ExoSocialActivity> getOlderSpaceActivities(Identity owner, Long sinceTime, int limit) {
    //
    JCRFilterLiteral filter = ActivityFilter.ACTIVITY_NEW_UPDATED_FILTER;
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(sinceTime));

    //
    return getActivitiesFromQueryResults(getActivitiesOfIdentitiesQuery(ActivityBuilderWhere
      .updated().owners(owner), filter).objects((long)0, (long)limit));
  }
  
  @Override
  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, Long sinceTime) {
    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);

    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(sinceTime));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(identities), filter).objects().size();

  }
  
  @Override
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, Long sinceTime) {
    //
    if (ownerIdentity == null) {
      return 0;
    }

    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(sinceTime));

    //
    return getActivitiesOfIdentitiesQuery (ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(ownerIdentity), filter).objects().size();
                                      
  }

  @Override
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, Long sinceTime) {
    //
    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);
    connectionList.add(ownerIdentity);

    //
    ActivityFilter filter = ActivityFilter.newer();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(sinceTime));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().owners(connectionList), filter).objects().size();
  }

  @Override
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, Long sinceTime) {
    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);

    //
    if (spaceList.size() == 0) {
      return 0;
    }
    
    //
    ActivityFilter filter = ActivityFilter.spaceOlder();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(sinceTime));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.space().owners(spaceList), filter).objects().size();
  }
  
  @Override
  public int getNumberOfOlderOnSpaceActivities(Identity spaceIdentity, Long sinceTime) {
    //
    if (spaceIdentity == null) {
      return 0;
    }
    
    //
    ActivityFilter filter = ActivityFilter.spaceOlder();
    filter.with(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).value(TimestampType.OLDER.from(sinceTime));

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.space().owners(spaceIdentity), filter).objects().size();
  }
  
  @Override
  public List<ExoSocialActivity> getNewerComments(ExoSocialActivity existingActivity,
                                                  Long sinceTime,
                                                  int limit) {
    
    String[] commentIds = getStorage().getActivity(existingActivity.getId()).getReplyToId();

    long totalSize = commentIds.length;
    
    ActivityIterator activityIt = new ActivityIterator(0, limit, totalSize);
    
    int i = 0;
    while (i < totalSize) {
      ExoSocialActivity comment = getActivity(commentIds[i]);
      if (!comment.isHidden() && comment.getUpdated().getTime() > sinceTime)
        activityIt.add(comment);
      if (activityIt.addMore() == false) {
        break;
      }
      i++;
    }
    
    return activityIt.result();
  }

  @Override
  public List<ExoSocialActivity> getOlderComments(ExoSocialActivity existingActivity,
                                                  Long sinceTime,
                                                  int limit) {
    String[] commentIds = getStorage().getActivity(existingActivity.getId()).getReplyToId();

    long totalSize = commentIds.length;
    
    ActivityIterator activityIt = new ActivityIterator(0, limit, totalSize);
    
    int i = 0;
    while (i < totalSize) {
      ExoSocialActivity comment = getActivity(commentIds[i]);
      if (!comment.isHidden() && comment.getUpdated().getTime() < sinceTime)
        activityIt.add(comment);
      if (activityIt.addMore() == false) {
        break;
      }
      i++;
    }
    
    return activityIt.result();
  }
  
  @Override
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, Long sinceTime) {
    String[] commentIds = getStorage().getActivity(existingActivity.getId()).getReplyToId();
    int nb = 0;
    long totalSize = commentIds.length;
    
    int i = 0;
    while (i < totalSize) {
      ExoSocialActivity comment = getActivity(commentIds[i]);
      if (!comment.isHidden() && comment.getUpdated().getTime() > sinceTime)
        nb++;
      i++;
    }
    
    return nb;
  }

  @Override
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, Long sinceTime) {
    String[] commentIds = getStorage().getActivity(existingActivity.getId()).getReplyToId();
    int nb = 0;
    long totalSize = commentIds.length;
    
    int i = 0;
    while (i < totalSize) {
      ExoSocialActivity comment = getActivity(commentIds[i]);
      if (!comment.isHidden() && comment.getUpdated().getTime() < sinceTime)
        nb++;
      i++;
    }
    
    return nb;
  }

  @Override
  public List<ExoSocialActivity> getUserActivitiesForUpgrade(Identity owner, long offset, long limit) throws ActivityStorageException {
    if (owner == null) {
      return Collections.emptyList();
    }


    ActivityFilter filter = new ActivityFilter(){};
    //
    return getActivitiesOfIdentities (ActivityBuilderWhere.simple().poster(owner).mentioner(owner).commenter(owner).liker(owner).owners(owner), filter, offset, limit);
  }

  @Override
  public int getNumberOfUserActivitiesForUpgrade(Identity owner) throws ActivityStorageException {
    if (owner == null) {
      return 0;
    }

    ActivityFilter filter = new ActivityFilter() {
    };
    //
    return getActivitiesOfIdentities (ActivityBuilderWhere.simple().mentioner(owner).owners(owner), filter, 0, 0).size();
  }

  @Override
  public List<ExoSocialActivity> getActivityFeedForUpgrade(Identity ownerIdentity,
                                                           int offset,
                                                           int limit) {
  
    
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);
    
    ActivityFilter filter = new ActivityFilter(){};
    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(identities), filter, offset, limit);
  }

  @Override
  public int getNumberOfActivitesOnActivityFeedForUpgrade(Identity ownerIdentity) {
    //
    List<Identity> identities = new ArrayList<Identity>();

    identities.addAll(relationshipStorage.getConnections(ownerIdentity));
    identities.addAll(getSpacesId(ownerIdentity));
    identities.add(ownerIdentity);
    
    ActivityFilter filter = new ActivityFilter(){};

    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().mentioner(ownerIdentity).owners(identities), filter).objects().size();
  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfConnectionsForUpgrade(Identity ownerIdentity,
                                                                      int offset,
                                                                      int limit) {
    List<Identity> connections = relationshipStorage.getConnections(ownerIdentity);

    if (connections.size() <= 0) {
      return Collections.emptyList();
    }

    //
    ActivityFilter filter = new ActivityFilter() {};

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().owners(connections), filter, offset, limit);
  }

  @Override
  public int getNumberOfActivitiesOfConnectionsForUpgrade(Identity ownerIdentity) {
    List<Identity> connectionList = relationshipStorage.getConnections(ownerIdentity);
  
    if (connectionList.size() <= 0) {
      return 0;
    }

    //
    ActivityFilter filter = new ActivityFilter(){};

    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().owners(connectionList), filter).objects().size();
  }

  @Override
  public List<ExoSocialActivity> getUserSpacesActivitiesForUpgrade(Identity ownerIdentity,
                                                                   int offset,
                                                                   int limit) {
    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);

    //
    if (spaceList.size() == 0) {
      return Collections.emptyList();
    }

    //
    ActivityFilter filter = new ActivityFilter(){};

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().owners(spaceList), filter, 0, limit);
  }

  @Override
  public int getNumberOfUserSpacesActivitiesForUpgrade(Identity ownerIdentity) {
    //
    List<Identity> spaceList = getSpacesId(ownerIdentity);
  
    //
    if (spaceList.size() == 0) {
      return 0;
    }

    //
    ActivityFilter filter = new ActivityFilter(){};
  
    //
    return getActivitiesOfIdentitiesQuery(ActivityBuilderWhere.simple().owners(spaceList), filter).objects().size();
  }
}
