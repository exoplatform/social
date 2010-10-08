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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.jcr.SocialDataLocation;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.storage.ActivityStorage;

/**
 * This class represents an Activity Manager, also can configure as service in
 * social platform.
 *
 * @see org.exoplatform.social.core.activity.model.Activity
 * @see ActivityStorage
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

  /** cache each activity by its id */
  private ExoCache<String, Activity> activityCache;

  /** cache list of activities by identityId and its segment */
  private ExoCache<String, Map<Segment, List<Activity>>> activityListCache;

  /** cache comments of an activity */
  private ExoCache<String, List<Activity>> commentsCache;

  /**
   * Instantiates a new activity manager.
   *
   * @param dataLocation the data location of activity manager it will
   *          instantiates tree node for this services.
   * @link org.exoplatform.social.space.impl.SoscialDataLocation.
   * @throws Exception exception when can't instantiates tree node.
   */
  public ActivityManager(SocialDataLocation dataLocation, IdentityManager identityManager, CacheService cacheService) {
    this.storage = new ActivityStorage(dataLocation);
    this.processors = new TreeSet<ActivityProcessor>(processorComparator());
    this.identityManager = identityManager;
    this.activityCache = cacheService.getCacheInstance(getClass().getName() + "activityCache");
    this.activityListCache = cacheService.getCacheInstance(getClass().getName() + "activityListCache");
    this.commentsCache = cacheService.getCacheInstance(getClass().getName() + "commentsCache");
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
      cachedActivity = storage.load(activityId);
      if (cachedActivity != null) {
        activityCache.put(activityId, cachedActivity);
      }
    }
    return cachedActivity;
  }

  /**
   * delete activity by its id.
   *
   * @param activityId the activity id
   * @throws Exception the exception
   */
  public void deleteActivity(String activityId) throws Exception {
    Activity activity = storage.load(activityId);
    if (activity != null) {
      Identity streamOwner = identityManager.getIdentity(OrganizationIdentityProvider.NAME, activity.getStreamOwner(), false);
      storage.deleteActivity(activityId);
      activityCache.remove(streamOwner.getId());
    }
  }

  /**
   * Delete comment by its id.
   *
   * @param activityId
   * @param commentId
   * @throws Exception
   */
  public void deleteComment(String activityId, String commentId) {
    storage.deleteComment(activityId, commentId);
    commentsCache.remove(activityId);
  }

  /**
   * Gets the latest activities by identity
   *
   * @param identity the identity
   * @return the activities
   * @throws Exception the exception
   */
  public List<Activity> getActivities(Identity identity) throws Exception {
    return storage.getActivities(identity, 0, 20);
  }

  /**
   * Gets the latest activities by identity, specifying start offset index and limit
   *
   * @param identity the identity
   * @param start offset index
   * @param limit
   * @return the activities
   * @throws Exception the exception
   */
  public List<Activity> getActivities(Identity identity, long start, long limit) throws Exception {
    Segment segment = new Segment(start, limit);
    Map<Segment, List<Activity>> segments = activityListCache.get(identity.getId());
    if (segments == null || segments.get(segment) == null) {
      segments = new HashMap<Segment, List<Activity>>();
      List<Activity> activityList = storage.getActivities(identity, start, limit);
      segments.put(segment, activityList);
      activityListCache.put(identity.getId(), segments);
    }
    return segments.get(segment);
  }

  /**
   * Saves an activity to the stream of a owner.<br/>
   * Note that the Activity.userId will be set to the owner identity if not already set.
   *
   * @param owner owner of the activity stream. Usually a user or space
   * @param activity the activity to save
   * @return the activity saved
   * @throws Exception the exception when error in storage
   */
  public Activity saveActivity(Identity owner, Activity activity) throws Exception {
    // TODO: check the security
    Validate.notNull(owner, "owner must not be null.");
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

    activity = storage.save(owner, activity);

    activityListCache.remove(owner.getId());

    return activity;
  }

  /**
   * Saves activity into the stream for the activity's userId.
   *
   * @see Activity#getUserId()
   * @param activity the activity to save
   * @return the activity
   * @see #saveActivity(Identity, Activity)
   */
  public Activity saveActivity(Activity activity) throws Exception {
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
   * @throws Exception
   */
  public void saveComment(Activity activity, Activity comment) throws Exception {
    Validate.notNull(activity, "activity must not be null.");
    Validate.notNull(comment.getUserId(), "comment.getUserId() must not be null.");
    Validate.notNull(comment.getTitle(), "comment.getTitle() must not be null.");
    if (comment.getId() != null) { // allows users to edit its comment?
      comment.setUpdatedTimestamp(System.currentTimeMillis());
    } else {
      comment.setPostedTime(System.currentTimeMillis());
      comment.setUpdatedTimestamp(System.currentTimeMillis());
    }
    comment.setReplyToId(Activity.IS_COMMENT);
    comment = saveActivity(comment);
    String rawCommentIds = activity.getReplyToId();
    if (rawCommentIds == null) {
      rawCommentIds = "";
    }
    rawCommentIds += "," + comment.getId();
    activity.setReplyToId(rawCommentIds);
    saveActivity(activity);
    activityCache.remove(activity.getId());
    commentsCache.remove(activity.getId());
  }

  /**
   * Saves an identity who likes an activity
   *
   * @param activity
   * @param identity
   * @throws Exception
   */
  public void saveLike(Activity activity, Identity identity) throws Exception {
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
   * @throws Exception
   */
  public void removeLike(Activity activity, Identity identity) throws Exception {
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
   * @return
   */
  public List<Activity> getComments(Activity activity) {
    List<Activity> cachedComments = commentsCache.get(activity.getId());
    if (cachedComments == null) {
      cachedComments = new ArrayList<Activity>();
      String rawCommentIds = activity.getReplyToId();
      // rawCommentIds can be: null || ,a,b,c,d
      if (rawCommentIds != null) {
        String[] commentIds = rawCommentIds.split(",");
        commentIds = (String[]) ArrayUtils.removeElement(commentIds, "");
        for (String commentId : commentIds) {
          cachedComments.add(storage.load(commentId));
        }
        if (cachedComments.size() > 0) {
          commentsCache.put(activity.getId(), cachedComments);
        }
      }
    }
    return cachedComments;
  }

  /**
   * Records an activity
   *
   * @param owner the owner of the target stream for this activity
   * @param type the type of activity (freeform)
   * @param title the title
   * @param body the body
   * @return the stored activity
   * @throws Exception the exception
   */
  public Activity recordActivity(Identity owner, String type, String title, String body) throws Exception {
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
   * Pass an activity through the chain of processors
   *
   * @param activity
   */
  void processActivitiy(Activity activity) {
    Iterator<ActivityProcessor> it = processors.iterator();
    while (it.hasNext()) {
      try {
        it.next().processActivity(activity);
      } catch (Exception e) {
        LOG.warn("activity processing failed " + e.getMessage());
      }
    }
  }

  public int getActivitiesCount(Identity owner) throws Exception {
    return storage.getActivitiesCount(owner);
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