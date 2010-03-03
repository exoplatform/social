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
package org.exoplatform.social.opensocial.spi;

import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.spi.*;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.core.model.ActivityImpl;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.webui.core.UIComponent;

import java.util.concurrent.Future;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.List;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;

// TODO: Auto-generated Javadoc
/**
 * The Class ExoActivityService.
 */
public class ExoActivityService extends ExoService implements ActivityService {
  
  /** The Constant OPENSOCIAL_PREFIX. */
  public final static String OPENSOCIAL_PREFIX = "opensocial:";
  
  /** The Constant OPENSOCIAL_PREFIX_LENGTH. */
  public final static int OPENSOCIAL_PREFIX_LENGTH = OPENSOCIAL_PREFIX.length();

  /* (non-Javadoc)
   * @see org.apache.shindig.social.opensocial.spi.ActivityService#getActivities(java.util.Set, org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String, java.util.Set, org.apache.shindig.social.opensocial.spi.CollectionOptions, org.apache.shindig.auth.SecurityToken)
   */
  public Future<RestfulCollection<Activity>> getActivities(Set<UserId> userIds, GroupId groupId, String appId, Set<String> fields, CollectionOptions options, SecurityToken token) throws SocialSpiException {
    List<Activity> result = Lists.newArrayList();

    //PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
    PortalContainer pc = PortalContainer.getInstance();
    ActivityManager am = (ActivityManager) pc.getComponentInstanceOfType(ActivityManager.class);

    try {
      Set<Identity> idSet = getIdSet(userIds, groupId, token);
      for (Identity id : idSet) {
        //TODO filter by appID
        result.addAll(convertToOSActivities(am.getActivities(id), fields));
      }
      // last time go first.
//      Collections.reverse(result);
      sortActivity(result);
      // Add for applying paging.
      int totalSize = result.size();
      int last = options.getFirst() + options.getMax();
      result = result.subList(options.getFirst(), Math.min(last, totalSize));
      
      return ImmediateFuture.newInstance(new RestfulCollection<Activity>(result, 0, totalSize));
    } catch (Exception je) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, je.getMessage(), je);
    }
  }

  /* (non-Javadoc)
   * @see org.apache.shindig.social.opensocial.spi.ActivityService#getActivities(org.apache.shindig.social.opensocial.spi.UserId, org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String, java.util.Set, org.apache.shindig.social.opensocial.spi.CollectionOptions, java.util.Set, org.apache.shindig.auth.SecurityToken)
   */
  public Future<RestfulCollection<Activity>> getActivities(UserId userId, GroupId groupId, String appId, Set<String> fields, CollectionOptions options, Set<String> activityIds, SecurityToken token) throws SocialSpiException {
    List<Activity> result = Lists.newArrayList();
    try {
      String user = userId.getUserId(token);
      Identity id = getIdentity(user);

      //PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");\
      PortalContainer pc = PortalContainer.getInstance();
      ActivityManager am = (ActivityManager) pc.getComponentInstanceOfType(ActivityManager.class);

      List<org.exoplatform.social.core.activitystream.model.Activity> exoActivities = am.getActivities(id);

      //TODO : this is not efficient, this should be done by the JCR
      for (org.exoplatform.social.core.activitystream.model.Activity exoActivity : exoActivities) {
        if (exoActivity.getType() != null && exoActivity.getType().startsWith(OPENSOCIAL_PREFIX)) {
          if(activityIds.contains(exoActivity.getType().substring(OPENSOCIAL_PREFIX_LENGTH)));
            result.add(convertToOSActivity(exoActivity, fields));
        }
      }

      // Add for applying paging.
      int totalSize = result.size();
      int last = options.getFirst() + options.getMax();
      result = result.subList(options.getFirst(), Math.min(last, totalSize));
      
      return ImmediateFuture.newInstance(new RestfulCollection<Activity>(result, 0, totalSize));
    } catch (Exception je) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, je.getMessage(), je);
    }
  }

  /* (non-Javadoc)
   * @see org.apache.shindig.social.opensocial.spi.ActivityService#getActivity(org.apache.shindig.social.opensocial.spi.UserId, org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String, java.util.Set, java.lang.String, org.apache.shindig.auth.SecurityToken)
   */
  public Future<Activity> getActivity(UserId userId, GroupId groupId, String appId, Set<String> fields, String activityId, SecurityToken token) throws SocialSpiException {
    throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, null);
  }

  /* (non-Javadoc)
   * @see org.apache.shindig.social.opensocial.spi.ActivityService#deleteActivities(org.apache.shindig.social.opensocial.spi.UserId, org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String, java.util.Set, org.apache.shindig.auth.SecurityToken)
   */
  public Future<Void> deleteActivities(UserId userId, GroupId groupId, String appId, Set<String> activityIds, SecurityToken token) throws SocialSpiException {
    throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, null);
  }

  /* (non-Javadoc)
   * @see org.apache.shindig.social.opensocial.spi.ActivityService#createActivity(org.apache.shindig.social.opensocial.spi.UserId, org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String, java.util.Set, org.apache.shindig.social.opensocial.model.Activity, org.apache.shindig.auth.SecurityToken)
   */
  public Future<Void> createActivity(UserId userId, GroupId groupId, String appId, Set<String> fields, Activity activity, SecurityToken token) throws SocialSpiException {
    try {
      org.exoplatform.social.core.activitystream.model.Activity exoActivity = convertFromOSActivity(activity, fields);

      //PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
      PortalContainer pc = PortalContainer.getInstance();
      ActivityManager am = (ActivityManager) pc.getComponentInstanceOfType(ActivityManager.class);

      am.saveActivity(userId.getUserId(token), exoActivity);

      return ImmediateFuture.newInstance(null);
    } catch (Exception e) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

    /**
     * Convert from os activity.
     * 
     * @param activity the activity
     * @param fields the fields
     * @return the org.exoplatform.social.core.activitystream.model. activity
     */
    private org.exoplatform.social.core.activitystream.model.Activity convertFromOSActivity(Activity activity, Set<String> fields) {
      org.exoplatform.social.core.activitystream.model.Activity exoActivity = new org.exoplatform.social.core.activitystream.model.Activity();
      //TODO filter the fields
      exoActivity.setBody(activity.getBody());
      exoActivity.setExternalId(activity.getExternalId());
      exoActivity.setId(activity.getId());
      if(activity.getPriority() != null)
        exoActivity.setPriority((new Float((activity.getPriority() * 100))).intValue());
      exoActivity.setTitle(activity.getTitle());
      exoActivity.setType("opensocial:" + activity.getAppId());
      exoActivity.setUrl(activity.getUrl());
      exoActivity.setUserId(activity.getUserId());
      return exoActivity;
    }
    
    /**
     * Convert to os activity.
     * 
     * @param exoActivity the exo activity
     * @param fields the fields
     * @return the activity
     */
    private Activity convertToOSActivity(org.exoplatform.social.core.activitystream.model.Activity exoActivity, Set<String> fields) {
      Activity activity = new ActivityImpl();
      //TODO filter the fields
      activity.setBody(exoActivity.getBody());
      activity.setExternalId(exoActivity.getExternalId());
      activity.setId(exoActivity.getId());
      if(exoActivity.getPriority() != null)
        activity.setPriority(((float)exoActivity.getPriority()) / 100);
      activity.setTitle(exoActivity.getTitle());
      if (exoActivity.getType() != null && exoActivity.getType().startsWith(OPENSOCIAL_PREFIX))
        activity.setAppId(exoActivity.getType().substring(OPENSOCIAL_PREFIX_LENGTH));
      activity.setUrl(exoActivity.getUrl());
      activity.setUserId(exoActivity.getUserId());
      activity.setUpdated(new Date(exoActivity.getUpdated()));
      activity.setPostedTime(exoActivity.getPostedTime());
      return activity;
    }

  /**
   * Convert to os activities.
   * 
   * @param activities the activities
   * @param fields the fields
   * @return the list
   */
  private List<Activity> convertToOSActivities(List<org.exoplatform.social.core.activitystream.model.Activity> activities, Set<String> fields) {
    List<Activity> res = Lists.newArrayList();
    for (org.exoplatform.social.core.activitystream.model.Activity activity : activities) {
      res.add(convertToOSActivity(activity, fields));
    }
    return res;
  }
  
  /**
   * Sort a list in increase order of posted time.<br>
   * 
   * @param lstActivities List for sorting.
   * @return A sorted array in increase order.
   */
  private List<Activity> sortActivity(List<Activity> lstActivities) {    
    Collections.sort(lstActivities, new ActivityComparator());       
    return lstActivities;
  }

  /**
   *   Implement ActivityComparator class for sorting in increase order of posted time.<br>
   *
   */
  private class ActivityComparator implements Comparator<Activity> {
    
    /**
     * Compare 2 activity by posted time.
     * 
     * @param act1 the act1
     * @param act2 the act2
     * @return the int
     */
    public int compare(Activity act1, Activity act2) {
      return (int) (act2.getPostedTime() - act1.getPostedTime());
    }
  }
}
