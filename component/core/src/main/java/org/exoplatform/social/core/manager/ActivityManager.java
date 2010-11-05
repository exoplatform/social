/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.activity.model.Util;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorage;

/**
 * This class represents an Activity Manager, also can configure as service in
 * social platform.
 *
 * @see org.exoplatform.social.core.activity.model.Activity
 * @see ActivityStorage
 * @see IdentityManager
 */
public class ActivityManager {
  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(ActivityManager.class);

  /** The storage. */
  private ActivityStorage storage;

  /** the set of activity processors which will be called to process each activity before outputting */
  private SortedSet<ActivityProcessor> processors;

  /** identityManager to get identity for saving and getting activities */
  private IdentityManager identityManager;

  /**
   * spaceService
   */
  private SpaceService spaceService;

  /** cache each activity by its id */
  private ExoCache<String, Activity> activityCache;

  /** cache list of activities by identityId and its segment */
  private ExoCache<String, Map<Segment, List<Activity>>> activityListCache;

  /** cache comments of an activity */
  private ExoCache<String, List<Activity>> commentsCache;

  /**
   * Instantiates a new activity manager.
   * @param activityStorage
   * @param identityManager
   * @param cacheService
   */
  public ActivityManager(ActivityStorage activityStorage, IdentityManager identityManager, CacheService cacheService) {
    this.storage = activityStorage;
    this.processors = new TreeSet<ActivityProcessor>(processorComparator());
    this.identityManager = identityManager;
    this.activityCache = cacheService.getCacheInstance(getClass().getName() + "activityCache");
    this.activityListCache = cacheService.getCacheInstance(getClass().getName() + "activityListCache");
    this.commentsCache = cacheService.getCacheInstance(getClass().getName() + "commentsCache");
  }

  /**
   * Saves an activity to the stream of an owner.<br/>
   * Note that the Activity.userId will be set to the owner identity if not already set.
   *
   * @param owner owner of the activity stream. Usually a user or space
   * @param activity the activity to save
   * @return the activity saved
   */
  public Activity saveActivity(Identity owner, Activity activity) {
    // TODO: check the security
    Validate.notNull(owner, "owner must not be null.");
    Validate.notNull(owner.getId(), "owner.getId() must not be null");
    // posted now
    long now = System.currentTimeMillis();
    if (activity.getId() == null) {
      activity.setPostedTime(now);
    }
    activity.setUpdatedTimestamp(now);

    // if not given, the activity is from the stream owner
    if (activity.getUserId() == null) {
      activity.setUserId(owner.getId());
    }

    activity = storage.saveActivity(owner, activity);

    activityListCache.remove(owner.getId());

    return activity;
  }


  // TODO should also filter by appID
  /**
   * Gets the activity by activity Id.
   *
   * @param activityId the activity id
   * @return the activity
   */
  public Activity getActivity(String activityId) {
    Activity cachedActivity = activityCache.get(activityId);
    if (cachedActivity == null) {
      cachedActivity = storage.getActivity(activityId);
      if (cachedActivity != null) {
        processActivitiy(cachedActivity);
        activityCache.put(activityId, cachedActivity);
      }
    }
    return cachedActivity;
  }

  /**
   * delete activity by its id.
   *
   * @param activityId the activity id
   */
  public void deleteActivity(String activityId) {
    Activity activity = storage.getActivity(activityId);
    if (activity != null) {
      Identity streamOwner = identityManager.getIdentity(activity.getStreamOwner(), false);
      storage.deleteActivity(activityId);
      try {
        activityCache.remove(streamOwner.getId());
        activityListCache.remove(streamOwner.getId());
      } catch(Exception e) {
        //Do nothing; just ignore
      }
    }
  }

  /**
   * Deletes a stored activity (id != null)
   *
   * @param activity
   * @since 1.1.1
   */
  public void deleteActivity(Activity activity) {
    Validate.notNull("activity.getId() must not be null", activity.getId());
    deleteActivity(activity.getId());
  }

  /**
   * Delete comment by its id.
   *
   * @param activityId
   * @param commentId
   */
  public void deleteComment(String activityId, String commentId) {
    storage.deleteComment(activityId, commentId);
    activityCache.remove(activityId);
    commentsCache.remove(activityId);
  }

  /**
   * Gets the latest activities by identity with the default limit of 20 latest activities.
   *
   * @param identity the identity
   * @return the activities
   * @see #getActivities(Identity, long, long)
   */
  public List<Activity> getActivities(Identity identity) {
    return storage.getActivities(identity, 0, 20);
  }

  /**
   * Gets the latest activities by identity, specifying start offset index and limit
   *
   * @param identity the identity
   * @param start offset index
   * @param limit
   * @return the activities
   */
  public List<Activity> getActivities(Identity identity, long start, long limit) {
    Segment segment = new Segment(start, limit);
    Map<Segment, List<Activity>> segments = activityListCache.get(identity.getId());
    if (segments == null || segments.get(segment) == null) {
      segments = new HashMap<Segment, List<Activity>>();
      List<Activity> activityList = storage.getActivities(identity, start, limit);
      for (Activity activity : activityList) {
        processActivitiy(activity);
      }
      segments.put(segment, activityList);
      activityListCache.put(identity.getId(), segments);
    }
    return segments.get(segment);
  }

  /**
   * Gets activities of connections from an identity. The activities are sorted by time.
   * Though by using cache, this still can be considered as the cause of the biggest performance problem.
   *
   * @param ownerIdentity
   * @return activityList
   * @since 1.1.1
   */
  //TODO Find way to improve its performance
  public List<Activity> getActivitiesOfConnections(Identity ownerIdentity) {
    List<Identity> connectionList = null;
    try {
      connectionList = identityManager.getConnections(ownerIdentity);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    List<Activity> activityList = new ArrayList<Activity>();
    String identityId;
    for (Identity identity : connectionList) {
      //default 20 activities each identity
      List<Activity> tempActivityList = getActivities(identity);
      identityId = identity.getId();
      for (Activity activity : tempActivityList) {
        if (activity.getUserId().equals(identityId)) {
          activityList.add(activity);
        }
      }
    }
    Collections.sort(activityList, Util.activityComparator());
    return activityList;
  }

  /**
   * Gets the activities from all user's spaces.
   * By default, the activity list is composed of all spaces' activities.
   * Each space's activity list contains 20 activities max and are sorted by time.
   *
   * @param ownerIdentity
   * @return list of activities
   * @since 1.1.1
   */
  public List<Activity> getActivitiesOfUserSpaces(Identity ownerIdentity) {
    spaceService = getSpaceService();
    List<Activity> activityList = new ArrayList<Activity>();
    List<Space> accessibleSpaceList = null;
    try {
      accessibleSpaceList = spaceService.getAccessibleSpaces(ownerIdentity.getRemoteId());
    } catch (SpaceException e1) {
      LOG.warn(e1.getMessage(), e1);
    }
    for (Space space : accessibleSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId());
      try {
        activityList.addAll(getActivities(spaceIdentity));
      } catch (Exception e) {
        LOG.warn(e.getMessage(), e);
      }
    }
    Collections.sort(activityList, Util.activityComparator());
    return activityList;
  }

  /**
   * Gets the activity feed of an identity. This feed is the combination of all
   * the activities of his own activities, his connections' activities and his
   * spaces' activities which are sorted by time. The latest activity is the
   * first item in the activity list.
   * 
   * @param identity
   * @return all related activities of identity such as his activities, his
   *         connections's activities, his spaces's activities
   */
  public List<Activity> getActivityFeed(Identity identity)
  {
    List<Activity> activityList = new ArrayList<Activity>();
    activityList.addAll(getActivitiesOfConnections(identity));
    activityList.addAll(getActivitiesOfUserSpaces(identity));
    activityList.addAll(getActivities(identity));
    Collections.sort(activityList, Util.activityComparator());
    return activityList;
  }

  /**
   * Saves activity into the stream for the activity's userId.
   * The userId must be set and this field is used to indicate the owner stream.
   *
   * @param activity the activity to save
   * @return the activity
   * @see #saveActivity(Identity, Activity)
   * @see Activity#getUserId()
   */
  public Activity saveActivity(Activity activity) {
    Validate.notNull(activity.getUserId(), "activity.getUserId() must not be null.");
    Identity owner = identityManager.getIdentity(activity.getUserId());
    return saveActivity(owner, activity);
  }

  /**
   * Save new or updates comment to an activity comment is an instance of
   * Activity with mandatory properties: userId, title.
   *
   * @param activity
   * @param comment
   */
  public void saveComment(Activity activity, Activity comment) {
    storage.saveComment(activity, comment);
    activityCache.remove(activity.getId());
    commentsCache.remove(activity.getId());
  }

  /**
   * Saves an identity who likes an activity
   *
   * @param activity
   * @param identity
   */
  public void saveLike(Activity activity, Identity identity) {
    String[] identityIds = activity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      LOG.warn("activity is already liked by identity: " + identity);
      return;
    }
    identityIds = (String[]) ArrayUtils.add(identityIds, identity.getId());
    activity.setLikeIdentityIds(identityIds);
    saveActivity(activity);
    activityCache.remove(activity.getId());
  }

  /**
   * Removes activity like, if this activity liked, remove; else does nothing
   *
   * @param activity
   * @param identity user that unlikes the activity
   */
  public void removeLike(Activity activity, Identity identity) {
    String[] identityIds = activity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      identityIds = (String[]) ArrayUtils.removeElement(identityIds, identity.getId());
      activity.setLikeIdentityIds(identityIds);
      saveActivity(activity);
      activityCache.remove(activity.getId());
    } else {
      LOG.warn("activity is not liked by identity: " + identity);
    }
  }

  /**
   * Gets an activity's commentList
   *
   * @param activity
   * @return commentList
   */
  public List<Activity> getComments(Activity activity) {
    String activityId = activity.getId();
    List<Activity> cachedComments = commentsCache.get(activityId);
    if (cachedComments == null) {
      //reload activity to make sure to have the most update activity
      activity = getActivity(activityId);
      cachedComments = new ArrayList<Activity>();
      String rawCommentIds = activity.getReplyToId();
      // rawCommentIds can be: null || ,a,b,c,d
      if (rawCommentIds != null) {
        String[] commentIds = rawCommentIds.split(",");
        commentIds = (String[]) ArrayUtils.removeElement(commentIds, "");

        for (String commentId : commentIds) {
          Activity comment = storage.getActivity(commentId);
          processActivitiy(comment);
          cachedComments.add(comment);
        }
        if (cachedComments.size() > 0) {
          commentsCache.put(activityId, cachedComments);
        }
      }
    }
    return cachedComments;
  }

  /**
   * Records an activity.
   *
   * @param owner the owner of the target stream for this activity
   * @param type the type of activity which will be used to use custom ui for rendering
   * @param title the title
   * @param body the body
   * @return the stored activity
   */
  public Activity recordActivity(Identity owner, String type, String title, String body) {
    String userId = owner.getId();
    Activity activity = new Activity(userId, type, title, body);
    return saveActivity(owner, activity);
  }

  /**
   * Saves an activity
   *
   * @param owner
   * @param activity
   * @return the stored activity
   * @throws Exception
   * @deprecated use {@link #saveActivity(Identity, Activity)} instead. Will be removed by 1.2.x
   */
  public Activity recordActivity(Identity owner, Activity activity) throws Exception {
    return saveActivity(owner, activity);
  }
  /**
   * Adds a new processor
   * @param processor
   */
  public void addProcessor(ActivityProcessor processor) {
    processors.add(processor);
    LOG.info("added activity processor " + processor.getClass());
  }

  /**
   * adds a new processor plugin
   * @param plugin
   */
  public void addProcessorPlugin(BaseActivityProcessorPlugin plugin) {
    addProcessor(plugin);
  }


  /**
   * Comparator used to order the processors by priority
   * @return
   */
  private static Comparator<ActivityProcessor> processorComparator() {
    return new Comparator<ActivityProcessor>() {

      public int compare(ActivityProcessor p1, ActivityProcessor p2) {
        if (p1 == null || p1 == null) {
          throw new IllegalArgumentException("Cannot compare null ActivityProcessor");
        }
        return p1.getPriority() - p2.getPriority();
      }
    };
  }


  /**
   * Gets the number of activity from a stream owner.
   *
   * @param owner
   * @return the number
   */
  public int getActivitiesCount(Identity owner) {
    return storage.getActivitiesCount(owner);
  }

  /**
   * Pass an activity through the chain of processors
   *
   * @param activity
   */
  public void processActivitiy(Activity activity) {
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
   * Gets spaceService
   *
   * @return spaceService
   */
  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = (SpaceService) PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Segment to indicate start and limit for activity list on activitiesCache
   *
   * @author hoatle
   *
   */
  private class Segment {
    private long start;
    private long limit;

    public Segment(long start, long limit) {
      this.start = start;
      this.limit = limit;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + (int) (limit ^ (limit >>> 32));
      result = prime * result + (int) (start ^ (start >>> 32));
      return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof Segment)) {
        return false;
      }
      Segment other = (Segment) obj;
      if (!getOuterType().equals(other.getOuterType())) {
        return false;
      }
      if (limit != other.limit) {
        return false;
      }
      if (start != other.start) {
        return false;
      }
      return true;
    }

    private ActivityManager getOuterType() {
      return ActivityManager.this;
    }

  }
}