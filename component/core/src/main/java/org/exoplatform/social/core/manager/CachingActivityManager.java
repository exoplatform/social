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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorage;
import org.exoplatform.social.core.storage.ActivityStorageException;

/**
 * Class CachingActivityManager extends ActivityManagerImpl with caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @since Nov 24, 2010
 * @version 1.2.0-GA
 */
public class CachingActivityManager extends ActivityManagerImpl {
  /** Logger */
  private static final Log                                        LOG = ExoLogger.getLogger(CachingActivityManager.class);

  /** Cache each activity by its id */
  private ExoCache<String, ExoSocialActivity>                     activityCache;

  /** Cache list of activities by identityId and its segment */
  private ExoCache<String, Map<Segment, List<ExoSocialActivity>>> activityListCache;

  /** Cache comments of an activity */
  private ExoCache<String, List<ExoSocialActivity>>               commentsCache;

  /**
   * Instantiates a new caching activity manager.
   *
   * @param activityStorage
   * @param identityManager
   * @param cacheService
   */
  public CachingActivityManager(ActivityStorage activityStorage,
                                IdentityManager identityManager,
                                CacheService cacheService) {
    super(activityStorage, identityManager);
    this.activityCache = cacheService.getCacheInstance(getClass().getName() + "activityCache");
    this.activityListCache = cacheService.getCacheInstance(getClass().getName()
        + "activityListCache");
    this.commentsCache = cacheService.getCacheInstance(getClass().getName() + "commentsCache");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExoSocialActivity getActivity(String activityId) throws ActivityStorageException {
    ExoSocialActivity cachedActivity = activityCache.get(activityId);
    if (cachedActivity == null) {
      cachedActivity = this.getStorage().getActivity(activityId);
      if (cachedActivity != null) {
        this.processActivitiy(cachedActivity);
        activityCache.put(activityId, cachedActivity);
      }
    }
    return cachedActivity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteActivity(String activityId) throws ActivityStorageException {
    ExoSocialActivity activity = this.getStorage().getActivity(activityId);
    if (activity != null) {
      Identity streamOwner = identityManager.getIdentity(activity.getUserId(), false);
      this.getStorage().deleteActivity(activityId);
      try {
        activityCache.remove(activityId);
        activityListCache.remove(streamOwner.getId());
      } catch (Exception e) {
        // Do nothing; just ignore
        LOG.debug("No cache key: " + activityId + " from cache");
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteComment(String activityId, String commentId) throws ActivityStorageException {
    this.getStorage().deleteComment(activityId, commentId);
    activityCache.remove(activityId);
    commentsCache.remove(activityId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
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

    activity = this.getStorage().saveActivity(owner, activity);

    activityListCache.remove(owner.getId());

    return activity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {
    this.getStorage().saveComment(activity, comment);
    activityCache.remove(activity.getId());
    commentsCache.remove(activity.getId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveLike(ExoSocialActivity activity, Identity identity) throws ActivityStorageException {
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
   * {@inheritDoc}
   */
  @Override
  public void removeLike(ExoSocialActivity activity, Identity identity) throws ActivityStorageException {
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
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getActivities(Identity identity, long start, long limit) throws ActivityStorageException {
    Segment segment = new Segment(start, limit);
    Map<Segment, List<ExoSocialActivity>> segments = activityListCache.get(identity.getId());
    if (segments == null || segments.get(segment) == null) {
      segments = new HashMap<Segment, List<ExoSocialActivity>>();
      List<ExoSocialActivity> activityList = this.getStorage()
                                                 .getActivities(identity, start, limit);
      for (ExoSocialActivity activity : activityList) {
        processActivitiy(activity);
      }
      segments.put(segment, activityList);
      activityListCache.put(identity.getId(), segments);
    }
    return segments.get(segment);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getComments(ExoSocialActivity activity) throws ActivityStorageException {
    String activityId = activity.getId();
    List<ExoSocialActivity> cachedComments = commentsCache.get(activityId);
    if (cachedComments == null) {
      // reload activity to make sure to have the most update activity
      activity = getActivity(activityId);
      cachedComments = new ArrayList<ExoSocialActivity>();
      String rawCommentIds = activity.getReplyToId();
      // rawCommentIds can be: null || ,a,b,c,d
      if (rawCommentIds != null) {
        String[] commentIds = rawCommentIds.split(",");
        commentIds = (String[]) ArrayUtils.removeElement(commentIds, "");

        for (String commentId : commentIds) {
          ExoSocialActivity comment = this.getStorage().getActivity(commentId);
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
   * Segment to indicate start and limit for activity list on activitiesCache.
   *
   * @author hoatle
   */
  private class Segment {
    private long start;

    private long limit;

    public Segment(long start, long limit) {
      this.start = start;
      this.limit = limit;
    }

    /*
     * (non-Javadoc)
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

    /*
     * (non-Javadoc)
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
      return CachingActivityManager.this;
    }
  }
}
