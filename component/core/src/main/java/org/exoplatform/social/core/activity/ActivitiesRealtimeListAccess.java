/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.core.activity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.common.jcr.Util;
import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.api.ActivityStorage;

/**
 * The real time list access for activities.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Apr 8, 2011
 */
public class ActivitiesRealtimeListAccess implements RealtimeListAccess<ExoSocialActivity> {

  public static enum ActivityType {
    ACTIVITY_FEED,
    USER_ACTIVITIES,
    VIEW_USER_ACTIVITIES,
    CONNECTIONS_ACTIVITIES,
    USER_SPACE_ACTIVITIES,
    SPACE_ACTIVITIES,
    POSTER_ACTIVITIES,
    POSTER_AND_TYPES_ACTIVITIES
  }

  /**
   * The activity activityStorage.
   */
  private ActivityStorage activityStorage;


  /**
   * The chosen activity type.
   */
  private ActivityType activityType;

  /**
   * The chosen identity.
   */
  private Identity ownerIdentity;
  
  /**
   * The viewer identity.
   */
  private Identity viewerIdentity;
  
  private String[] activityTypes;

  /**
   * Constructor.
   *
   * @param existingActivityStorage
   * @param chosenActivityType
   * @param chosenOwnerIdentity
   */
  public ActivitiesRealtimeListAccess(final ActivityStorage existingActivityStorage,
                                      final ActivityType chosenActivityType,
                                      final Identity chosenOwnerIdentity) {
    this.activityStorage = existingActivityStorage;
    this.activityType = chosenActivityType;
    this.ownerIdentity = chosenOwnerIdentity;
  }
  
  /**
   * Constructor.
   *
   * @param existingActivityStorage
   * @param chosenActivityType
   * @param chosenOwnerIdentity
   * @param activityTypes
   */
  public ActivitiesRealtimeListAccess(final ActivityStorage existingActivityStorage,
                                      final ActivityType chosenActivityType,
                                      final Identity chosenOwnerIdentity,
                                      final String... activityTypes) {
    this.activityStorage = existingActivityStorage;
    this.activityType = chosenActivityType;
    this.ownerIdentity = chosenOwnerIdentity;
    this.activityTypes = activityTypes;
  }
  
  /**
   * Constructor.
   *
   * @param existingActivityStorage
   * @param chosenActivityType
   * @param chosenOwnerIdentity
   * @param viewerIndentity viewer gets and show other owner stream
   */
  public ActivitiesRealtimeListAccess(final ActivityStorage existingActivityStorage,
                                      final ActivityType chosenActivityType,
                                      final Identity chosenOwnerIdentity,
                                      final Identity viewerIndentity) {
    this.activityStorage = existingActivityStorage;
    this.activityType = chosenActivityType;
    this.ownerIdentity = chosenOwnerIdentity;
    this.viewerIdentity = viewerIndentity;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadAsList(int index, int limit) {
    switch (activityType) {
      case ACTIVITY_FEED: {
        return activityStorage.getActivityFeed(ownerIdentity, index, limit);
      }
      case USER_ACTIVITIES: {
        return activityStorage.getUserActivities(ownerIdentity, index, limit);
      }
      case VIEW_USER_ACTIVITIES: {
        return activityStorage.getActivities(ownerIdentity, viewerIdentity, index, limit);
      }
      case CONNECTIONS_ACTIVITIES: {
        return activityStorage.getActivitiesOfConnections(ownerIdentity, index, limit);
      }
      case USER_SPACE_ACTIVITIES: {
        return activityStorage.getUserSpacesActivities(ownerIdentity, index, limit);
      }
      case SPACE_ACTIVITIES: {
        return activityStorage.getSpaceActivities(ownerIdentity, index, limit);
      }
      case POSTER_ACTIVITIES: {
        return activityStorage.getActivitiesByPoster(ownerIdentity, index, limit);
      } 
      case POSTER_AND_TYPES_ACTIVITIES: {
        return activityStorage.getActivitiesByPoster(ownerIdentity, index, limit, activityTypes);
      }
    }
    return Collections.emptyList();
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity[] load(int index, int limit) {
    return Util.convertListToArray(loadAsList(index, limit), ExoSocialActivity.class);
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() {
    switch (activityType) {
      case ACTIVITY_FEED: {
        return activityStorage.getNumberOfActivitesOnActivityFeed(ownerIdentity);
      }
      case USER_ACTIVITIES: {
        return activityStorage.getNumberOfUserActivities(ownerIdentity);
      }
      case CONNECTIONS_ACTIVITIES: {
        return activityStorage.getNumberOfActivitiesOfConnections(ownerIdentity);
      }
      case USER_SPACE_ACTIVITIES: {
        return activityStorage.getNumberOfUserSpacesActivities(ownerIdentity);
      }
      case SPACE_ACTIVITIES: {
        return activityStorage.getNumberOfSpaceActivities(ownerIdentity);
      }
      case POSTER_ACTIVITIES: {
        return activityStorage.getNumberOfActivitiesByPoster(ownerIdentity);
      }
      case POSTER_AND_TYPES_ACTIVITIES: {
        return 0;
      }
      case VIEW_USER_ACTIVITIES: {
        return activityStorage.getNumberOfActivitiesByPoster(ownerIdentity, viewerIdentity);
      }
    }
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadNewer(ExoSocialActivity baseActivity, int length) {
    switch (activityType) {
      case ACTIVITY_FEED: {
        return activityStorage.getNewerOnActivityFeed(ownerIdentity, baseActivity, length);
      }
      case USER_ACTIVITIES: {
        return activityStorage.getNewerOnUserActivities(ownerIdentity, baseActivity, length);
      }
      case CONNECTIONS_ACTIVITIES: {
        return activityStorage.getNewerOnActivitiesOfConnections(ownerIdentity, baseActivity, length);
      }
      case USER_SPACE_ACTIVITIES: {
        return activityStorage.getNewerOnUserSpacesActivities(ownerIdentity, baseActivity, length);
      }
      case SPACE_ACTIVITIES: {
        return activityStorage.getNewerOnSpaceActivities(ownerIdentity, baseActivity, length);
      }
    }
    return Collections.emptyList();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewer(ExoSocialActivity baseActivity) {
     switch (activityType) {
      case ACTIVITY_FEED: {
        return activityStorage.getNumberOfNewerOnActivityFeed(ownerIdentity, baseActivity);
      }
      case USER_ACTIVITIES: {
        return activityStorage.getNumberOfNewerOnUserActivities(ownerIdentity, baseActivity);
      }
      case CONNECTIONS_ACTIVITIES: {
        return activityStorage.getNumberOfNewerOnActivitiesOfConnections(ownerIdentity, baseActivity);
      }
      case USER_SPACE_ACTIVITIES: {
        return activityStorage.getNumberOfNewerOnUserSpacesActivities(ownerIdentity, baseActivity);
      }
      case SPACE_ACTIVITIES: {
        return activityStorage.getNumberOfNewerOnSpaceActivities(ownerIdentity, baseActivity);
      }
    }
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadOlder(ExoSocialActivity baseActivity, int length) {
    switch (activityType) {
      case ACTIVITY_FEED: {
        return activityStorage.getOlderOnActivityFeed(ownerIdentity, baseActivity, length);
      }
      case USER_ACTIVITIES: {
        return activityStorage.getOlderOnUserActivities(ownerIdentity, baseActivity, length);
      }
      case CONNECTIONS_ACTIVITIES: {
        return activityStorage.getOlderOnActivitiesOfConnections(ownerIdentity, baseActivity, length);
      }
      case USER_SPACE_ACTIVITIES: {
        return activityStorage.getOlderOnUserSpacesActivities(ownerIdentity, baseActivity, length);
      }
      case SPACE_ACTIVITIES: {
        return activityStorage.getOlderOnSpaceActivities(ownerIdentity, baseActivity, length);
      }
    }
    return Collections.emptyList();
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlder(ExoSocialActivity baseActivity) {
     switch (activityType) {
      case ACTIVITY_FEED: {
        return activityStorage.getNumberOfOlderOnActivityFeed(ownerIdentity, baseActivity);
      }
      case USER_ACTIVITIES: {
        return activityStorage.getNumberOfOlderOnUserActivities(ownerIdentity, baseActivity);
      }
      case CONNECTIONS_ACTIVITIES: {
        return activityStorage.getNumberOfOlderOnActivitiesOfConnections(ownerIdentity, baseActivity);
      }
      case USER_SPACE_ACTIVITIES: {
        return activityStorage.getNumberOfOlderOnUserSpacesActivities(ownerIdentity, baseActivity);
      }
      case SPACE_ACTIVITIES: {
        return activityStorage.getNumberOfOlderOnSpaceActivities(ownerIdentity, baseActivity);
      }
    }
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewer(Long sinceTime) {
    switch (activityType) {
    case ACTIVITY_FEED: {
      return activityStorage.getNumberOfNewerOnActivityFeed(ownerIdentity, sinceTime);
    }
    case USER_ACTIVITIES: {
      return activityStorage.getNumberOfNewerOnUserActivities(ownerIdentity, sinceTime);
    }
    case CONNECTIONS_ACTIVITIES: {
      return activityStorage.getNumberOfNewerOnActivitiesOfConnections(ownerIdentity, sinceTime);
    }
    case USER_SPACE_ACTIVITIES: {
      return activityStorage.getNumberOfNewerOnUserSpacesActivities(ownerIdentity, sinceTime);
    }
    case SPACE_ACTIVITIES: {
      return activityStorage.getNumberOfNewerOnSpaceActivities(ownerIdentity, sinceTime);
    }
    }
    return 0;
  }

  @Override
  public int getNumberOfMultiUpdated(Map<String, Long> sinceTimes) {
    return activityStorage.getNumberOfMultiUpdated(ownerIdentity, sinceTimes);
  }
  
  public List<ExoSocialActivity> getUpadtedActivities(Long sinceTime, int limit) {
    switch (activityType) {
      case ACTIVITY_FEED: {
        return activityStorage.getNewerFeedActivities(ownerIdentity, sinceTime, limit);
      }
      case USER_ACTIVITIES: {
        return activityStorage.getNewerUserActivities(ownerIdentity, sinceTime, limit);
      }
      case CONNECTIONS_ACTIVITIES: {
        return activityStorage.getNewerActivitiesOfConnections(ownerIdentity, sinceTime, limit);
      }
      case USER_SPACE_ACTIVITIES: {
        return activityStorage.getNewerUserSpacesActivities(ownerIdentity, sinceTime, limit);
      }
      case SPACE_ACTIVITIES: {
        return activityStorage.getNewerSpaceActivities(ownerIdentity, sinceTime, limit);
      }
    }
    return Collections.emptyList();
  }
  
  public int getNumberOfUpdated(ActivityUpdateFilter filter) {
    switch (activityType) {
      case ACTIVITY_FEED: {
        return activityStorage.getNumberOfUpdatedOnActivityFeed(ownerIdentity, filter);
      }
      case USER_ACTIVITIES: {
        return activityStorage.getNumberOfUpdatedOnUserActivities(ownerIdentity, filter);
      }
      case CONNECTIONS_ACTIVITIES: {
        return activityStorage.getNumberOfUpdatedOnActivitiesOfConnections(ownerIdentity, filter);
      }
      case USER_SPACE_ACTIVITIES: {
        return activityStorage.getNumberOfUpdatedOnUserSpacesActivities(ownerIdentity, filter);
      }
      case SPACE_ACTIVITIES: {
        return activityStorage.getNumberOfUpdatedOnSpaceActivities(ownerIdentity, filter);
      }
    }
    return 0;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadNewer(Long sinceTime, int limit) {
    switch (activityType) {
    case ACTIVITY_FEED: {
      return activityStorage.getNewerFeedActivities(ownerIdentity, sinceTime, limit);
    }
    case USER_ACTIVITIES: {
      return activityStorage.getNewerUserActivities(ownerIdentity, sinceTime, limit);
    }
    case CONNECTIONS_ACTIVITIES: {
      return activityStorage.getNewerActivitiesOfConnections(ownerIdentity, sinceTime, limit);
    }
    case USER_SPACE_ACTIVITIES: {
      return activityStorage.getNewerUserSpacesActivities(ownerIdentity, sinceTime, limit);
    }
    case SPACE_ACTIVITIES: {
      return activityStorage.getNewerSpaceActivities(ownerIdentity, sinceTime, limit);
    }
    }
    return Collections.emptyList();
  }
  
  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadOlder(Long sinceTime, int limit) {
    switch (activityType) {
    case ACTIVITY_FEED: {
      return activityStorage.getOlderFeedActivities(ownerIdentity, sinceTime, limit);
    }
    case USER_ACTIVITIES: {
      return activityStorage.getOlderUserActivities(ownerIdentity, sinceTime, limit);
    }
    case CONNECTIONS_ACTIVITIES: {
      return activityStorage.getOlderActivitiesOfConnections(ownerIdentity, sinceTime, limit);
    }
    case USER_SPACE_ACTIVITIES: {
      return activityStorage.getOlderUserSpacesActivities(ownerIdentity, sinceTime, limit);
    }
    case SPACE_ACTIVITIES: {
      return activityStorage.getOlderSpaceActivities(ownerIdentity, sinceTime, limit);
    }
    }
    return Collections.emptyList();
  }
  
  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlder(Long sinceTime) {
    switch (activityType) {
    case ACTIVITY_FEED: {
      return activityStorage.getNumberOfOlderOnActivityFeed(ownerIdentity, sinceTime);
    }
    case USER_ACTIVITIES: {
      return activityStorage.getNumberOfOlderOnUserActivities(ownerIdentity, sinceTime);
    }
    case CONNECTIONS_ACTIVITIES: {
      return activityStorage.getNumberOfOlderOnActivitiesOfConnections(ownerIdentity, sinceTime);
    }
    case USER_SPACE_ACTIVITIES: {
      return activityStorage.getNumberOfOlderOnUserSpacesActivities(ownerIdentity, sinceTime);
    }
    case SPACE_ACTIVITIES: {
      return activityStorage.getNumberOfOlderOnSpaceActivities(ownerIdentity, sinceTime);
    }
    }
    return 0;
  }
  
  /**
   * {@inheritDoc}
   */
  public int getNumberOfUpgrade() {
    switch (activityType) {
    case ACTIVITY_FEED: {
      return activityStorage.getNumberOfActivitesOnActivityFeedForUpgrade(ownerIdentity);
    }
    case USER_ACTIVITIES: {
      return activityStorage.getNumberOfUserActivitiesForUpgrade(ownerIdentity);
    }
    case CONNECTIONS_ACTIVITIES: {
      return activityStorage.getNumberOfActivitiesOfConnectionsForUpgrade(ownerIdentity);
    }
    case USER_SPACE_ACTIVITIES: {
      return activityStorage.getNumberOfUserSpacesActivitiesForUpgrade(ownerIdentity);
    }
    case SPACE_ACTIVITIES: {
      return activityStorage.getNumberOfSpaceActivitiesForUpgrade(ownerIdentity);
    }
    case POSTER_ACTIVITIES: {
      return activityStorage.getNumberOfActivitiesByPoster(ownerIdentity);
    }
    case POSTER_AND_TYPES_ACTIVITIES: {
      return 0;
    }
    case VIEW_USER_ACTIVITIES: {
      return activityStorage.getNumberOfActivitiesByPoster(ownerIdentity, viewerIdentity);
    }
  }
  return 0;
  }

}
