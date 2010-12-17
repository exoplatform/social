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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.activity.model.Util;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorage;
import org.exoplatform.social.core.storage.ActivityStorageException;

/**
 * Class ActivityManagerImpl implements ActivityManager without caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @since Nov 24, 2010
 * @version 1.2.0-GA
 */
public class ActivityManagerImpl implements ActivityManager {
  /** Logger */
  private static final Log               LOG = ExoLogger.getLogger(ActivityManagerImpl.class);

  /** The storage. */
  protected ActivityStorage              storage;

  /**
   * The set of activity processors which will be called to process each
   * activity before outputting.
   */
  protected SortedSet<ActivityProcessor> processors;

  /** identityManager to get identity for saving and getting activities */
  protected IdentityManager              identityManager;

  /** spaceService */
  protected SpaceService                 spaceService;

  /**
   * Instantiates a new activity manager.
   *
   * @param activityStorage
   * @param identityManager
   */
  public ActivityManagerImpl(ActivityStorage activityStorage, IdentityManager identityManager) {
    this.storage = activityStorage;
    this.processors = new TreeSet<ActivityProcessor>(processorComparator());
    this.identityManager = identityManager;
  }

  /**
   * {@inheritDoc}
   */
  public void addProcessor(ActivityProcessor processor) {
    processors.add(processor);
    LOG.info("added activity processor " + processor.getClass());
  }

  /**
   * {@inheritDoc}
   */
  public void addProcessorPlugin(BaseActivityProcessorPlugin plugin) {
    this.addProcessor(plugin);
  }

  // TODO should also filter by appID
  /**
   * {@inheritDoc}
   */
  public void deleteActivity(String activityId) throws ActivityStorageException {
    ExoSocialActivity activity = storage.getActivity(activityId);
    if (activity != null) {
      storage.deleteActivity(activityId);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteActivity(ExoSocialActivity activity) throws ActivityStorageException {
    Validate.notNull("activity.getId() must not be null", activity.getId());
    deleteActivity(activity.getId());
  }

  /**
   * {@inheritDoc}
   */
  public void deleteComment(String activityId, String commentId) throws ActivityStorageException {
    storage.deleteComment(activityId, commentId);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivities(Identity identity) throws ActivityStorageException {
    return storage.getActivities(identity, 0, 20);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivities(Identity identity, long start, long limit) throws ActivityStorageException {
    List<ExoSocialActivity> activityList = storage.getActivities(identity, start, limit);
    for (ExoSocialActivity activity : activityList) {
      processActivitiy(activity);
    }
    return activityList;
  }

  /**
   * {@inheritDoc}
   */
  public int getActivitiesCount(Identity owner) throws ActivityStorageException {
    return storage.getActivitiesCount(owner);
  }

  // TODO Find way to improve its performance
  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity) throws ActivityStorageException {
    List<Identity> connectionList = null;
    List<ExoSocialActivity> activityList = new ArrayList<ExoSocialActivity>();
    try {
      connectionList = identityManager.getConnections(ownerIdentity);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    if (connectionList == null || connectionList.size() == 0) {
      return activityList;
    }
    for (Identity identity : connectionList) {
      // default 20 activities each identity
      List<ExoSocialActivity> tempActivityList = getActivities(identity);
      if (tempActivityList == null || tempActivityList.size() == 0) {
        continue;
      }
      String identityId = identity.getId();
      for (ExoSocialActivity activity : tempActivityList) {
        if (activity.getUserId().equals(identityId)) {
          activityList.add(activity);
        }
      }
    }
    Collections.sort(activityList, Util.activityComparator());
    return activityList;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivitiesOfUserSpaces(Identity ownerIdentity) {
    spaceService = this.getSpaceService();
    List<ExoSocialActivity> activityList = new ArrayList<ExoSocialActivity>();
    List<Space> accessibleSpaceList = null;
    try {
      accessibleSpaceList = spaceService.getAccessibleSpaces(ownerIdentity.getRemoteId());
    } catch (SpaceException e1) {
      LOG.warn(e1.getMessage(), e1);
    }
    if (accessibleSpaceList == null || accessibleSpaceList.size() == 0) {
      return activityList;
    }
    for (Space space : accessibleSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   space.getName());
      try {
        activityList.addAll(getActivities(spaceIdentity));
      } catch (Exception e) {
        LOG.warn(e.getMessage(), e);
      }
    }
    Collections.sort(activityList, Util.activityComparator());
    return activityList;
  }

  // TODO should also filter by appID
  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity getActivity(String activityId) throws ActivityStorageException {
    ExoSocialActivity returnActivity = null;
    returnActivity = storage.getActivity(activityId);
    if (returnActivity == null) {
      this.processActivitiy(returnActivity);
    }
    return returnActivity;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivityFeed(Identity identity) throws ActivityStorageException {
    List<ExoSocialActivity> activityList = new ArrayList<ExoSocialActivity>();
    activityList.addAll(getActivitiesOfConnections(identity));
    activityList.addAll(getActivitiesOfUserSpaces(identity));
    activityList.addAll(getActivities(identity));
    Collections.sort(activityList, Util.activityComparator());
    return activityList;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getComments(ExoSocialActivity activity) throws ActivityStorageException {
    String activityId = activity.getId();
    List<ExoSocialActivity> returnComments = new ArrayList<ExoSocialActivity>();
    // reload activity to make sure to have the most update activity
    activity = getActivity(activityId);
    String rawCommentIds = activity.getReplyToId();
    // rawCommentIds can be: null || ,a,b,c,d
    if (rawCommentIds != null) {
      String[] commentIds = rawCommentIds.split(",");
      commentIds = (String[]) ArrayUtils.removeElement(commentIds, "");
      for (String commentId : commentIds) {
        ExoSocialActivity comment = storage.getActivity(commentId);
        processActivitiy(comment);
        returnComments.add(comment);
      }
    }
    return returnComments;
  }

  /**
   * {@inheritDoc}
   */
  public void processActivitiy(ExoSocialActivity activity) {
    Iterator<ActivityProcessor> it = processors.iterator();
    while (it.hasNext()) {
      try {
        it.next().processActivity(activity);
      } catch (Exception e) {
        LOG.warn("activity processing failed " + e.getMessage());
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity recordActivity(Identity owner, String type, String title) throws ActivityStorageException {
    return saveActivity(owner, new ExoSocialActivityImpl(owner.getId(), type, title));
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity recordActivity(Identity owner, ExoSocialActivity activity) throws Exception {
    return saveActivity(owner, activity);
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity recordActivity(Identity owner, String type, String title, String body) throws ActivityStorageException {
    String userId = owner.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl(userId, type, title, body);
    return saveActivity(owner, activity);
  }

  /**
   * {@inheritDoc}
   */
  public void removeLike(ExoSocialActivity activity, Identity identity) throws ActivityStorageException {
    String[] identityIds = activity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      identityIds = (String[]) ArrayUtils.removeElement(identityIds, identity.getId());
      activity.setLikeIdentityIds(identityIds);
      saveActivity(activity);
    } else {
      LOG.warn("activity is not liked by identity: " + identity);
    }
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity saveActivity(Identity owner, ExoSocialActivity activity) throws ActivityStorageException {
    // TODO: check the security
    Validate.notNull(owner, "owner must not be null.");
    Validate.notNull(owner.getId(), "owner.getId() must not be null");
    // posted now
    long now = System.currentTimeMillis();
    if (activity.getId() == null) {
      activity.setPostedTime(now);
    }
    activity.setUpdated(new Date(now));
    // if not given, the activity is from the stream owner
    if (activity.getUserId() == null) {
      activity.setUserId(owner.getId());
    }
    activity = storage.saveActivity(owner, activity);
    return activity;
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity saveActivity(ExoSocialActivity activity) throws ActivityStorageException {
    Validate.notNull(activity.getUserId(), "activity.getUserId() must not be null.");
    Identity owner = identityManager.getIdentity(activity.getUserId());
    return saveActivity(owner, activity);
  }

  /**
   * {@inheritDoc}
   */
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {
    storage.saveComment(activity, comment);
  }

  /**
   * {@inheritDoc}
   */
  public void saveLike(ExoSocialActivity activity, Identity identity) throws ActivityStorageException {
    String[] identityIds = activity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      LOG.warn("activity is already liked by identity: " + identity);
      return;
    }
    identityIds = (String[]) ArrayUtils.add(identityIds, identity.getId());
    activity.setLikeIdentityIds(identityIds);
    saveActivity(activity);
  }

  /**
   * Gets spaceService.
   *
   * @return spaceService
   */
  protected SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = (SpaceService) PortalContainer.getInstance()
                                                   .getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Comparator used to order the processors by priority.
   *
   * @return
   */
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

  /**
   * Gets storage.
   *
   * @return storage
   */
  protected ActivityStorage getStorage() {
    return this.storage;
  }

  /**
   * Gets identityManager.
   *
   * @return identityManager
   */
  protected IdentityManager getIdentityManager() {
    return this.identityManager;
  }
}