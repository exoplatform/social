/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.core.activity.filter;

import org.apache.commons.lang.ArrayUtils;

public class ActivityUpdateFilter {

  private String[] excludedActivities = new String[0];
  /**User only refresh current tab, doesn't switch other tab. */
  private boolean refreshTab = false;
  
  public enum ActivityFilterType {
    
    ACTIVITY_FEED,
    CONNECTIONS_ACTIVITIES,
    USER_ACTIVITIES,
    USER_SPACE_ACTIVITIES,
    SPACE_ACTIVITIES;
    
    
    private Long oldFromSinceTime;
    private Long fromSinceTime;
    private Long toSinceTime;
    private long lastNumberOfUpdated;
    
    public ActivityFilterType fromSinceTime(Long fromSinceTime) {
      this.fromSinceTime = fromSinceTime;
      return this;
    }
    
    public ActivityFilterType oldFromSinceTime(Long oldFromSinceTime) {
      this.oldFromSinceTime = oldFromSinceTime;
      return this;
    }
    
    public ActivityFilterType toSinceTime(Long toSinceTime) {
      this.toSinceTime = toSinceTime;
      return this;
    }
    
    public ActivityFilterType lastNumberOfUpdated(Long lastNumberOfUpdated) {
      this.lastNumberOfUpdated = lastNumberOfUpdated;
      return this;
    }
    
    public Long lastNumberOfUpdated() {
      return lastNumberOfUpdated;
    }
    
    public Long toSinceTime() {
      return toSinceTime;
    }
    
    public Long fromSinceTime() {
      return fromSinceTime;
    }
    
    public Long oldFromSinceTime() {
      return oldFromSinceTime;
    }
  }
  
  public ActivityUpdateFilter() {
    this.refreshTab = false;
  }
  
  public ActivityUpdateFilter(boolean refreshTab) {
    this.refreshTab = refreshTab;
  }
  
  public ActivityFilterType connectionType() {
    return ActivityFilterType.CONNECTIONS_ACTIVITIES;
  }
  
  public ActivityFilterType userActivitiesType() {
    return ActivityFilterType.USER_ACTIVITIES;
  }
  
  public ActivityFilterType userSpaceActivitiesType() {
    return ActivityFilterType.USER_SPACE_ACTIVITIES;
  }
  
  public ActivityFilterType spaceActivitiesType() {
    return ActivityFilterType.SPACE_ACTIVITIES;
  }
  
  public ActivityFilterType activityFeedType() {
    return ActivityFilterType.ACTIVITY_FEED;
  }
  
  public String[] excludedActivities() {
    return excludedActivities;
  }

  public void addExcludedActivities(String...activityIds) {
    if (activityIds == null) return;
    
    //
    for(String id : activityIds) {
      if (ArrayUtils.contains(excludedActivities, id) == false) {
        excludedActivities = (String[])ArrayUtils.add(excludedActivities, id);
      }
    }
    
  }

  /**
   * User only refresh current tab, doesn't switch other tab. 
   * @return TRUE: Refreshed, FALSE : switched new tab
   */
  public boolean isRefreshTab() {
    return refreshTab;
  }
  
}
