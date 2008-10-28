package org.exoplatform.social.core.activitystream;

import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

import java.util.List;


public class ActivityManager {
  private JCRStorage storage;

  public ActivityManager(NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    this.storage = new JCRStorage(nodeHierarchyCreator);
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

  public Activity recordActivity(String userId, String type, String title, String body) throws Exception {
    Activity activity = new Activity(userId, type, title, body);

    return saveActivity(userId, activity);
  }
  
}
