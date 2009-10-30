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

import java.net.URLDecoder;
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
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Oct 01, 2009          
 */
@Path("/social/activities/")
public class ActivitiesRestService implements ResourceContainer { 
  
  private ActivityManager activityManager = null;
  private IdentityManager identityManager = null;
  
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
    List<LikeInfoModel> likeInfos = new ArrayList<LikeInfoModel>();
    Activity activity = activityManager.getActivity(activityId);
    String[] likeIdentitiesId = activity.getLikeIdentitiesId();
    if(likeIdentitiesId != null) Collections.addAll(ids, likeIdentitiesId);
    likeInfos = getLikeInfos(ids);
    
    identitiesId.setActivityId(activityId);
    identitiesId.setIds(ids);
    identitiesId.setLikeInfos(likeInfos);
    
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
  
  @GET
  @Path("/linkshare/{link}/{lang}")
  @Produces({MediaType.APPLICATION_JSON})
  public LinkShare getLinkShare(@PathParam("link") String link, @PathParam("lang") String lang) throws Exception {
    link = URLDecoder.decode(link, "utf-8");
    LinkShare ls;
    if (lang != null) {
      ls = LinkShare.getInstance(link, lang);
    } else {
      ls = LinkShare.getInstance(link);
    }
    return ls;
  }
  
  /**
   * List IdentititesId like an activities
   */
  public class ListIdentitiesId {
    /** ids list variable */
    private List<String> ids_;
    /** like information model */
    private List<LikeInfoModel> likeInfos_;
    /** activityId */
    private String activityId_;
    
    public void setActivityId(String activityId) { activityId_ = activityId;}
    public String getActivityId() {return activityId_;}

    public void setIds(List<String> ids) { ids_ = ids; }
    public List<String> getIds() { return ids_; }
    public List<LikeInfoModel> getLikeInfos() { return likeInfos_;}
    public void setLikeInfos(List<LikeInfoModel> likeInfos) { likeInfos_ = likeInfos; }
  }
  
  /**
   * Model contain like detail information.
   *
   */
  public class LikeInfoModel {
    /** thumbnail list variable */
    private String thumbnail_;
    /** user name list variable */
    private String userName_;
    /** full name */
    private String fullName_;
    /** activityId */
    private String likeIdentityId_;
    
    public void setLikeIdentityId(String likeIdentityId) { likeIdentityId_ = likeIdentityId;}
    public String getLikeIdentityId() {return likeIdentityId_;}
    public String getThumbnail() { return thumbnail_;}
    public void setThumbnail(String thumbnail) { thumbnail_ = thumbnail;}
    public String getUserName() { return userName_;}
    public void setUserName(String userName) { userName_ = userName;}
    public String getFullName() { return fullName_;}
    public void setFullName(String fullName) { fullName_ = fullName;}
  }
  
  /**
   * Get all like information and add to list of model.
   * @param ids
   * @return List of like information model.
   * @throws Exception
   */
  private List<LikeInfoModel> getLikeInfos(List<String> ids) throws Exception {
    String thumbnail = null;
    String userName = null;
    String fullName = null;
    Profile profile = null;
    ProfileAttachment att = null;
    Identity identity = null;
    LikeInfoModel likeInfo = null;
    IdentityManager im = getIdentityManager();
    List<LikeInfoModel> likeInfos = new ArrayList<LikeInfoModel>();
    for (String id : ids) {
      identity = im.getIdentityById(id);
      profile = identity.getProfile();
      userName =(String) profile.getProperty("username");
      fullName = profile.getFullName();
      att = (ProfileAttachment)profile.getProperty("avatar");
      if (att != null) {
        thumbnail = "/" + getPortalName()+"/rest/jcr/" + getRepository() + "/" + att.getWorkspace();
        thumbnail = thumbnail + att.getDataPath() + "/?rnd=" + System.currentTimeMillis();
      }
      
      likeInfo = new LikeInfoModel();
      likeInfo.setLikeIdentityId(id);
      likeInfo.setUserName(userName);
      likeInfo.setFullName(fullName);
      likeInfo.setThumbnail(thumbnail);
      likeInfos.add(likeInfo);
    }
    
    return likeInfos;
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
  
  private IdentityManager getIdentityManager () {
    if(identityManager == null) {
      PortalContainer portalContainer = PortalContainer.getInstance();
      identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }
  
  private String getRepository() throws Exception {
    PortalContainer portalContainer = PortalContainer.getInstance();
    RepositoryService rService = (RepositoryService) portalContainer.getComponentInstanceOfType(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();  
  }
}
