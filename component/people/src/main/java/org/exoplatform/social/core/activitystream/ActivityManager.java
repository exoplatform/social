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
package org.exoplatform.social.core.activitystream;

import java.util.List;

import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.impl.SocialDataLocation;

// TODO: Auto-generated Javadoc

/**
 * This class represents an Activity Manager, also can configure as service
 * in social platform.
 * @see	org.exoplatform.social.core.activitystream.model.Activity
 * @see org.exoplatform.social.core.activitystream.JCRStorage
 */
public class ActivityManager {
  
  /** The storage. */
  private JCRStorage storage;
  
  
  /**
   * Instantiates a new activity manager.
   * 
   * @param dataLocation the data location of activity manager
   * 		it will instantiates tree node for this services.
   * @see 	org.exoplatform.social.space.impl.SoscialDataLocation.
   * @throws Exception exception when can't instantiates tree node.
   */
  public ActivityManager(SocialDataLocation dataLocation) throws Exception {
    this.storage = new JCRStorage(dataLocation);
  }

  //TODO should also filter by appID
  /**
   * Gets the activity by activity Id.
   * 
   * @param activityId the activity id
   * @return the activity
   */
  public Activity getActivity(String activityId) {
    return storage.load(activityId);
  }
  
  /**
   * delete activity by its id.
   * 
   * @param activityId the activity id
   * @throws Exception the exception
   */
  public void deleteActivity(String activityId) throws Exception {
    storage.deleteActivity(activityId);
  }
  

  /**
 * Gets the activities by identity
 * 
 * @param identity the identity
 * @return the activities
 * @throws Exception the exception
 */
public List<Activity> getActivities(Identity identity) throws Exception {
    String id = identity.getId();
    return storage.getActivities(id);
  }

  /**
   * Save activity based on user and his activity
   * 
   * @param identityId the identity Id such as obtained by {@link Identity#getId()}
   * @param activity the activity
   * @return the activity
   * @throws Exception the exception when error in storage
   */
  public Activity saveActivity(String identityId, Activity activity) throws Exception {
    //TODO: check the security
    //TODO: should publish the activity in a different thread to improve performance
    if(identityId == null)
      return null;
      
    if (activity.getId() == null) {
      activity.setPostedTime(System.currentTimeMillis());
    }
    activity.setUpdated(System.currentTimeMillis());
    activity.setUserId(identityId);


    return storage.save(identityId, activity);
  }
  
  /**
   * Save activity.
   * 
   * @param activity the activity
   * @return the activity
   * @throws Exception the exception
   */
  public Activity saveActivity(Activity activity) throws Exception {
    activity.setUpdated(System.currentTimeMillis());
    if (activity.getId() == null) {
      activity.setPostedTime(System.currentTimeMillis());
    }
    return storage.save(activity.getUserId(), activity);
  }

  /**
   * Record activity based on userId, type, title, and his body
   * 
   * @param identityId the Id such as obtained by {@link Identity#getId()}
   * @param type the type
   * @param title the title
   * @param body the body
   * @return the activity
   * @throws Exception the exception
   */
  public Activity recordActivity(String identityId, String type, String title, String body) throws Exception {
    Activity activity = new Activity(identityId, type, title, body);

    return saveActivity(identityId, activity);
  }
  
}
