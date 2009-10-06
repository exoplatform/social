/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.social.services.rest.opensocial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Oct 01, 2009          
 */
@Path("/social/activities/")
public class ActivitiesRestService implements ResourceContainer { 
  
  private ActivityManager activityManager = null;
  
  /**
   * Return request with JSON body which represent identities id.<br>
   * 
   * @return identities id.
   * @throws Exception
   */
  @GET
  @Path("/getLikeIds/{activityId}")
  @Produces({MediaType.APPLICATION_JSON})
  public ListIdentitiesId getIdentitiesId(@PathParam("activityId") String activityId) throws Exception {
    ListIdentitiesId identitiesId = new ListIdentitiesId();
    List<String> ids = new ArrayList<String>();
    PortalContainer portalContainer = PortalContainer.getInstance();
    ActivityManager activityManager = (ActivityManager) portalContainer.getComponentInstanceOfType(ActivityManager.class);
    Activity activity = activityManager.getActivity(activityId);
    String[] likeIdentitiesId = activity.getLikeIdentitiesId();
    if(likeIdentitiesId != null) Collections.addAll(ids, likeIdentitiesId);
    identitiesId.setIds(ids);
    identitiesId.setActivityId(activityId);
    return identitiesId;
  }
  
  /**
   * Return request with JSON body which represent identities id<br>
   * 
   * @return identities id.
   * @throws Exception
   */
  @GET
  @Path("/setLikeId/{activityId}/{identityId}")
  @Produces({MediaType.APPLICATION_JSON})
  public ListIdentitiesId setIdentitiesId(@PathParam("activityId") String activityId, 
                                          @PathParam("identityId") String identityId) throws Exception {
    ListIdentitiesId identitiesId = new ListIdentitiesId();
    List<String> ids = new ArrayList<String>();
    ActivityManager activityManager = getActivityManager();
    Activity activity = activityManager.getActivity(activityId);
    String[] likeIdentitiesId = activity.getLikeIdentitiesId();
    likeIdentitiesId = addItemToArray(likeIdentitiesId, identityId);
    activity.setLikeIdentitiesId(likeIdentitiesId);
    activityManager.saveActivity(activity);
    Collections.addAll(ids, likeIdentitiesId);
    identitiesId.setIds(ids);
    identitiesId.setActivityId(activityId);
    return identitiesId;
  }
  
  /**
   * Return request with JSON body which represent identities id<br>
   * 
   * @return identities id.
   * @throws Exception
   */
  @GET
  @Path("/removeLikeId/{activityId}/{identityId}")
  @Produces({MediaType.APPLICATION_JSON})
  public ListIdentitiesId removeIdentitiesId(@PathParam("activityId") String activityId, 
                                          @PathParam("identityId") String identityId) throws Exception {
    ListIdentitiesId identitiesId = new ListIdentitiesId();
    List<String> ids = new ArrayList<String>();
    ActivityManager activityManager = getActivityManager();
    Activity activity = activityManager.getActivity(activityId);
    String[] likeIdentitiesId = activity.getLikeIdentitiesId();
    likeIdentitiesId = removeItemFromArray(likeIdentitiesId, identityId);
    activity.setLikeIdentitiesId(likeIdentitiesId);
    activityManager.saveActivity(activity);
    if(likeIdentitiesId != null) Collections.addAll(ids, likeIdentitiesId);
    identitiesId.setIds(ids);
    identitiesId.setActivityId(activityId);
    return identitiesId;
  }
  /**
   * List IdentititesId like an activities
   */
  public class ListIdentitiesId {
    /** ids list variable */
    private List<String> ids_;
    /** activityId */
    private String activityId_;
    
    public void setActivityId(String activityId) { activityId_ = activityId;}
    public String getActivityId() {return activityId_;}
    public void setIds(List<String> ids) { ids_ = ids; }
    public List<String> getIds() { return ids_; }
  }
  
  /**
   * Remove an item from an array
   * @param arrays
   * @param str
   * @return new array
   */
  private String[] removeItemFromArray(String[] arrays, String str) {
    List<String> list = new ArrayList<String>();
    list.addAll(Arrays.asList(arrays));
    list.remove(str);
    if(list.size() > 0) return list.toArray(new String[list.size()]);
    else return null;
  }
  
  /**
   * Add an item to an array
   * @param arrays
   * @param str
   * @return new array
   */
  private String[] addItemToArray(String[] arrays, String str) {
    List<String> list = new ArrayList<String>();
    if(arrays != null && arrays.length > 0) {
      list.addAll(Arrays.asList(arrays));
      list.add(str);
      return list.toArray(new String[list.size()]);
    } else return new String[] {str};
  }
  
  private ActivityManager getActivityManager() {
    if(activityManager == null) {
      PortalContainer portalContainer = PortalContainer.getInstance();
      activityManager = (ActivityManager) portalContainer.getComponentInstanceOfType(ActivityManager.class);
    }
    return activityManager;
  }
}
