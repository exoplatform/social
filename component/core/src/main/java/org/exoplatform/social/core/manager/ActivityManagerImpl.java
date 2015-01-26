/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.manager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.ActivitiesRealtimeListAccess;
import org.exoplatform.social.core.activity.ActivitiesRealtimeListAccess.ActivityType;
import org.exoplatform.social.core.activity.ActivityLifeCycle;
import org.exoplatform.social.core.activity.ActivityListener;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.CommentsRealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;

/**
 * Class ActivityManagerImpl implements ActivityManager without caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @author <a href="hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Nov 24, 2010
 * @since 1.2.0-GA
 */
public class ActivityManagerImpl implements ActivityManager {
  /** Logger */
  private static final Log               LOG = ExoLogger.getLogger(ActivityManagerImpl.class);

  /** The activityStorage. */
  protected ActivityStorage activityStorage;

  /** identityManager to get identity for saving and getting activities */
  protected IdentityManager              identityManager;

  /** spaceService */
  protected SpaceService                 spaceService;
  
  protected ActivityLifeCycle            activityLifeCycle = new ActivityLifeCycle();

  /**
   * Default limit for deprecated methods to get maximum number of activities.
   */
  private static final int DEFAULT_LIMIT = 20;

  /**
   * Instantiates a new activity manager.
   *
   * @param activityStorage
   * @param identityManager
   */
  public ActivityManagerImpl(ActivityStorage activityStorage, IdentityManager identityManager) {
    this.activityStorage = activityStorage;
    this.identityManager = identityManager;
  }

  /**
   * {@inheritDoc}
   */
  public void saveActivityNoReturn(Identity streamOwner, ExoSocialActivity newActivity) {
    activityStorage.saveActivity(streamOwner, newActivity);
    activityLifeCycle.saveActivity(getActivity(newActivity.getId()));
  }

  /**
   * {@inheritDoc}
   */
  public void saveActivityNoReturn(ExoSocialActivity newActivity) {
    Identity owner = getStreamOwner(newActivity);
    saveActivityNoReturn(owner, newActivity);
  }

  /**
   * {@inheritDoc}
   */
  public void saveActivity(Identity streamOwner, String activityType, String activityTitle) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setType(activityType);
    activity.setTitle(activityTitle);
    saveActivity(streamOwner, activity);
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity getActivity(String activityId) {
    return activityStorage.getActivity(activityId);
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) {
    return activityStorage.getParentActivity(comment);
  }

  /**
   * {@inheritDoc}
   */
  public void updateActivity(ExoSocialActivity existingActivity) {
    activityStorage.updateActivity(existingActivity);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteActivity(ExoSocialActivity existingActivity) {
    Validate.notNull(existingActivity.getId(), "existingActivity.getId() must not be null!");
    deleteActivity(existingActivity.getId());
  }

  /**
   * {@inheritDoc}
   */
  public void deleteActivity(String activityId) {
    activityStorage.deleteActivity(activityId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveComment(ExoSocialActivity existingActivity, ExoSocialActivity newComment) throws
          ActivityStorageException {
    activityStorage.saveComment(existingActivity, newComment);
    activityLifeCycle.saveComment(activityStorage.getActivity(newComment.getId()));
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity existingActivity) {
    return new CommentsRealtimeListAccess(activityStorage, existingActivity);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteComment(String activityId, String commentId) {
    activityStorage.deleteComment(activityId, commentId);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteComment(ExoSocialActivity existingActivity, ExoSocialActivity existingComment) {
    deleteComment(existingActivity.getId(), existingComment.getId());
  }


  /**
   * {@inheritDoc}
   */
  public void saveLike(ExoSocialActivity existingActivity, Identity identity) {

    existingActivity.setTitle(null);
    existingActivity.setBody(null);
    existingActivity.setTemplateParams(null);

    String[] identityIds = existingActivity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      LOG.warn("activity is already liked by identity: " + identity);
      return;
    }
    identityIds = (String[]) ArrayUtils.add(identityIds, identity.getId());
    existingActivity.setLikeIdentityIds(identityIds);
    updateActivity(existingActivity);
    activityLifeCycle.likeActivity(existingActivity);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteLike(ExoSocialActivity activity, Identity identity) {
    activity.setTitle(null);
    activity.setBody(null);
    activity.setTemplateParams(null);
    String[] identityIds = activity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      identityIds = (String[]) ArrayUtils.removeElement(identityIds, identity.getId());
      activity.setLikeIdentityIds(identityIds);
      updateActivity(activity);
    } else {
      LOG.warn("activity is not liked by identity: " + identity);
    }
  }

  @Override
  public void addActivityEventListener(ActivityListenerPlugin activityListenerPlugin) {
    registerActivityListener(activityListenerPlugin);   
  }
  
  /**
   * {@inheritDoc}
   */
  public void registerActivityListener(ActivityListener listener) {
    activityLifeCycle.addListener(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void unregisterActivityListener(ActivityListener listener) {
    activityLifeCycle.removeListener(listener);
  }


  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.USER_ACTIVITIES, existingIdentity);
  }
  
  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity ownerIdentity, Identity viewerIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.VIEW_USER_ACTIVITIES, ownerIdentity, viewerIdentity);
  }


  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfConnectionsWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.CONNECTIONS_ACTIVITIES, existingIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfUserSpacesWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.USER_SPACE_ACTIVITIES, existingIdentity);
  }
  
  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfSpaceWithListAccess(Identity existingSpaceIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.SPACE_ACTIVITIES, existingSpaceIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivityFeedWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.ACTIVITY_FEED, existingIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.POSTER_ACTIVITIES, posterIdentity);
  }
  
  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, String ... activityTypes) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.POSTER_AND_TYPES_ACTIVITIES, posterIdentity, activityTypes);
  }
  
  /**
   * {@inheritDoc}
   */
  public void addProcessor(ActivityProcessor processor) {
    activityStorage.getActivityProcessors().add(processor);
    LOG.debug("added activity processor " + processor.getClass());
  }

  /**
   * {@inheritDoc}
   */
  public void addProcessorPlugin(BaseActivityProcessorPlugin plugin) {
    this.addProcessor(plugin);
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity saveActivity(Identity streamOwner, ExoSocialActivity newActivity) {
    ExoSocialActivity created = activityStorage.saveActivity(streamOwner, newActivity);
    activityLifeCycle.saveActivity(getActivity(created.getId()));
    return created;
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity saveActivity(ExoSocialActivity newActivity) {
    saveActivityNoReturn(newActivity);
    return newActivity;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivities(Identity identity) throws ActivityStorageException {
    List<ExoSocialActivity> activityList = Collections.emptyList();
    try {
      ExoSocialActivity[] activities = getActivitiesWithListAccess(identity).load(0, DEFAULT_LIMIT);
      activityList = Arrays.asList(activities);
    } catch (Exception e) {
      LOG.warn("Failed to get activities by identity: " + identity);
    }
    return activityList;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivities(Identity identity,
                                               long start, long limit) throws ActivityStorageException {
    //validateStartLimit(start, limit);
    return activityStorage.getUserActivities(identity, start, limit);
  }

  /**
   * {@inheritDoc}
   * The result list is returned with 30 maximum activities.
   */
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity) throws ActivityStorageException {
    List<ExoSocialActivity> activityList = Collections.emptyList();
    try {
      ExoSocialActivity[] activities = getActivitiesOfConnectionsWithListAccess(ownerIdentity).load(0, 30);
      activityList = Arrays.asList(activities);
    } catch (Exception e) {
      LOG.warn("Failed to get activities of connections!");
    }
    return activityList;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity,
                                                            int offset, int limit) throws ActivityStorageException {
    validateStartLimit(offset, limit);
    List<Identity> connectionList = null;
    try {
      ListAccess<Identity> connectionsWithListAccess = identityManager.getConnectionsWithListAccess(ownerIdentity);
      connectionList = Arrays.asList(connectionsWithListAccess.load(0, connectionsWithListAccess.getSize()));
    } catch (Exception e) {
      LOG.error("Failed to getActivitiesOfIdentities of: " + ownerIdentity.getRemoteId(), e);
    }
    return activityStorage.getActivitiesOfIdentities(connectionList, offset, limit);
  }


  /**
   * {@inheritDoc}
   * By default, the activity list is composed of all spaces' activities.
   * Each activity list of the space contains maximum 20 activities
   * and are returned sorted starting from the most recent.
   */
  public List<ExoSocialActivity> getActivitiesOfUserSpaces(Identity ownerIdentity) {
    return getActivitiesOfUserSpacesWithListAccess(ownerIdentity).loadAsList(0, DEFAULT_LIMIT);
  }


  /**
   * {@inheritDoc}
   * Return maximum number of activities: 40
   */
  public List<ExoSocialActivity> getActivityFeed(Identity identity) {
    return getActivityFeedWithListAccess(identity).loadAsList(0, DEFAULT_LIMIT * 2);
  }

  /**
   * {@inheritDoc}
   */
  public void removeLike(ExoSocialActivity existingActivity, Identity existingIdentity) {
    deleteLike(existingActivity, existingIdentity);
  }

/**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getComments(ExoSocialActivity existingActivity) {
    return getCommentsWithListAccess(existingActivity).loadAsList(0, DEFAULT_LIMIT * 2);
    /*
    String activityId = existingActivity.getId();
    List<ExoSocialActivity> returnComments = new ArrayList<ExoSocialActivity>();
    // reload activity to make sure to have the most update activity
    existingActivity = getActivity(activityId);
    String rawCommentIds = existingActivity.getReplyToId();
    // rawCommentIds can be: null || ,a,b,c,d
    if (rawCommentIds != null) {
      String[] commentIds = rawCommentIds.split(",");
      commentIds = (String[]) ArrayUtils.removeElement(commentIds, "");
      for (String commentId : commentIds) {
        ExoSocialActivity comment = activityStorage.getActivity(commentId);
        processActivitiy(comment);
        returnComments.add(comment);
      }
    }
    return returnComments;
    */
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity recordActivity(Identity owner, String type, String title) throws ActivityStorageException {
    ExoSocialActivity newActivity = new ExoSocialActivityImpl(owner.getId(), type, title);
    saveActivity(owner, newActivity);
    return newActivity;
  }

  /**
   * {@inheritDoc}
   */
  public int getActivitiesCount(Identity owner) throws ActivityStorageException {
    return activityStorage.getNumberOfUserActivities(owner);
  }


  /**
   * {@inheritDoc}
   */
  public void processActivitiy(ExoSocialActivity activity) {
    return;
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity recordActivity(Identity owner, ExoSocialActivity activity) throws Exception {
    saveActivity(owner, activity);
    return activity;
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity recordActivity(Identity owner, String type,
                                          String title, String body) throws ActivityStorageException {
    String userId = owner.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl(userId, type, title, body);
    saveActivity(owner, activity);
    return activity;
  }


  /**
   * Validates the start and limit for duplicated method.
   * The limit must be greater than or equal to start.
   * The limit must be equal to or greater than start by {@link #DEFAULT_LIMIT}
   *
   * @param start
   * @param limit
   */
  private void validateStartLimit(long start, long limit) {
    Validate.isTrue(limit >= start, "'limit' must be greater than or equal to 'start'");
    Validate.isTrue(limit - start <= DEFAULT_LIMIT, "'limit - start' must be less than or equal to " + DEFAULT_LIMIT);
  }

  /**
   * Gets stream owner from identityId = newActivity.userId.
   * @param newActivity the new activity
   * @return the identity stream owner
   */
  private Identity getStreamOwner(ExoSocialActivity newActivity) {
    Validate.notNull(newActivity.getUserId(), "activity.getUserId() must not be null!");
    return identityManager.getIdentity(newActivity.getUserId(), false);
  }

}
