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
 */package org.exoplatform.social.core.activitystream;

import java.util.List;

import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.impl.SocialDataLocation;


public class ActivityManager {
  private JCRStorage storage;

  public ActivityManager(SocialDataLocation dataLocation) throws Exception {
    this.storage = new JCRStorage(dataLocation);
  }

  //TODO should also filter by appID
  public Activity getActivity(String activityId) {
    return storage.load(activityId);
  }
  
  /**
   * delete activity by its id
   * @param activityId
   * @throws Exception
   */
  public void deleteActivity(String activityId) throws Exception {
    storage.deleteActivity(activityId);
  }
  
//TODO should also filter by appID
  public List<Activity> getActivities(Identity identity) throws Exception {
    return storage.getActivities(identity);
  }

  public List<Activity> getContactsActivities(Identity identity) {
    return null;
  }

  public Activity saveActivity(String userId, Activity activity) throws Exception {
    //TODO: check the security
    //TODO: should publish the activity in a different thread to improve performance
    if(userId == null)
      return null;
      
    if (activity.getId() == null) {
      activity.setPostedTime(System.currentTimeMillis());
    }
    activity.setUpdated(System.currentTimeMillis());
    activity.setUserId(userId);


    return storage.save(userId, activity);
  }
  
  public Activity saveActivity(Activity activity) throws Exception {
    activity.setUpdated(System.currentTimeMillis());
    return storage.save(activity.getUserId(), activity);
  }

  public Activity recordActivity(String userId, String type, String title, String body) throws Exception {
    Activity activity = new Activity(userId, type, title, body);

    return saveActivity(userId, activity);
  }
  
}
