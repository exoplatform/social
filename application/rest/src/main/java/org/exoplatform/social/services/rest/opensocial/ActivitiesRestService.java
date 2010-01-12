/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
import org.exoplatform.social.services.rest.Util;

/**
 * ActivitiesRestService.java
 *
 * @author     hoatle <hoatlevan at gmail dot com>
 * @since      Dec 29, 2009
 * @copyright  eXo Platform SEA
 */
@Path("social/activities")
public class ActivitiesRestService implements ResourceContainer {
  private ActivityManager _activityManager;
  private IdentityManager _identityManager;
  
  /**
   * constructor
   */
  public ActivitiesRestService() {}
  
  /**
   * destroys activity by activityId
   * if detects any comments of that activity, destroys these comments, too.
   * @param activityId
   * @return activity
   */
  private Activity destroyActivity(String activityId) {
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    String rawCommentIds = activity.getExternalId();
    //rawCommentIds can be: null || ,a,b,c,d
    if (rawCommentIds != null) {
      String[] commentIds = rawCommentIds.split(",");
      for (String commentId : commentIds) {
        try {
          _activityManager.deleteActivity(commentId);
        } catch(Exception ex) {
          //TODO hoatle LOG
          //TODO hoatle handles or ignores?
        }
      }
    }
    try {
      _activityManager.deleteActivity(activityId);
    } catch(Exception ex) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    return activity;
  }
  
  /**
   * destroys activity and gets json return format
   * @param uriInfo
   * @param activityId
   * @return
   * @throws Exception
   */
  @POST
  @Path("destroy/{activityId}.json")
  public Response jsonDestroyActivity(@Context UriInfo uriInfo,
                                      @PathParam("activityId") String activityId) throws Exception {    
    Activity activity = destroyActivity(activityId);
    return Util.getResponse(activity, uriInfo, MediaType.APPLICATION_JSON_TYPE, Response.Status.OK);
  }
  
  /**
   * destroys activity and gets xml return format
   * @param uriInfo
   * @param activityId
   * @return
   * @throws Exception
   */
  @POST
  @Path("destroy/{activityId}.xml")
  public Response xmlDestroyActivity(@Context UriInfo uriInfo,
                                     @PathParam("activityId") String activityId) throws Exception {
    Activity activity = destroyActivity(activityId);
    return Util.getResponse(activity, uriInfo, MediaType.APPLICATION_XML_TYPE, Response.Status.OK);
  }
  
  /**
   * show list of like by activityId
   * @param activityId  
   * @return
   * @throws Exception
   */
  private LikeList showLikes(String activityId) {
    LikeList likeList = new LikeList();
    likeList.setActivityId(activityId);
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String[] identityIds = activity.getLikeIdentityIds();
    if (identityIds == null) {
      likeList.setLikes(new ArrayList<Like>());
    } else {
      likeList.setLikes(getLikes(identityIds));
    }
    return likeList;
  }
  
  /**
   * updates like of an activity
   * @param activityId
   * @param like
   * @throws Exception
   */
  private LikeList updateLike(String activityId, Like like) throws Exception {
    LikeList likeList = new LikeList();
    likeList.setActivityId(activityId);
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String[] identityIds = activity.getLikeIdentityIds();
    String identityId = like.getIdentityId();
    boolean alreadyLiked = false;
    if (identityId == null) {
      throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
    }
    if (identityIds != null) {
      for (String id : identityIds) {
        if (id.equals(identityId)) {
          alreadyLiked = true;
        }
      }
    }
    if (!alreadyLiked) {
      identityIds = addItemToArray(identityIds, identityId);
      activity.setLikeIdentityIds(identityIds);
      try {
        _activityManager.saveActivity(activity);
      } catch (Exception ex) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    } else {
      //TODO hoatle let's it run smoothly or informs that user already liked the activity?
    }
    likeList.setLikes(getLikes(identityIds));
    return likeList;
  }
  
  /**
   * destroys like from an activity 
   * @param activityId
   * @param identityId
   */
  private LikeList destroyLike(String activityId, String identityId) {
    LikeList likeList = new LikeList();
    likeList.setActivityId(activityId);
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String[] identityIds = activity.getLikeIdentityIds();
    if (identityIds == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    boolean alreadyLiked = true;
    for (String id : identityIds) {
      if (id.equals(identityId)) {
        identityIds = removeItemFromArray(identityIds, identityId);
        activity.setLikeIdentityIds(identityIds);
        try {
          _activityManager.saveActivity(activity);
        } catch(Exception ex) {
          throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        alreadyLiked = false;
      }
    }
    if (alreadyLiked) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    if (identityIds == null) {
      likeList.setLikes(new ArrayList<Like>());
    } else {
      likeList.setLikes(getLikes(identityIds));
    }
    return likeList;
  }
  
  /**
   * shows list of like by activityId and returns json format
   * @param uriInfo
   * @param activityId
   * @return
   * @throws Exception
   */
  @GET
  @Path("{activityId}/likes/show.json")
  public Response jsonShowLikes(@Context UriInfo uriInfo,
                                @PathParam("activityId") String activityId) throws Exception {
    LikeList likeList = null;
    likeList = showLikes(activityId);
    return Util.getResponse(likeList, uriInfo, MediaType.APPLICATION_JSON_TYPE, Response.Status.OK);
  }
  
  /**
   * shows list of like by activityId and return xml format
   * @param uriInfo
   * @param activityId
   * @return
   * @throws Exception
   */
  @GET
  @Path("{activityId}/likes/show.xml")
  public Response xmlShowLikes(@Context UriInfo uriInfo,
                               @PathParam("activityId") String activityId) throws Exception {
    LikeList likeList = null;
    likeList = showLikes(activityId);
    return Util.getResponse(likeList, uriInfo, MediaType.APPLICATION_XML_TYPE, Response.Status.OK);
  }
  
  /**
   * updates like by json format
   * @param uriInfo
   * @param activityId
   * @param like
   * @throws Exception 
   */
  @POST
  @Path("{activityId}/likes/update.json")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response jsonUpdateLike(@Context UriInfo uriInfo,
                                  @PathParam("activityId") String activityId,
                                  Like like) throws Exception {
    LikeList likeList = null;
    likeList = updateLike(activityId, like);
    return Util.getResponse(likeList, uriInfo, MediaType.APPLICATION_JSON_TYPE, Response.Status.OK);
  }
  
  /**
   * updates like by xml format
   * @param uriInfo
   * @param activityId
   * @param like
   * @return
   * @throws Exception
   */
  @POST
  @Path("{activityId}/likes/update.xml")
  @Consumes({MediaType.APPLICATION_XML})
  public Response xmlUpdateLike(@Context UriInfo uriInfo,
                                 @PathParam("activityId") String activityId,
                                 Like like) throws Exception{
    LikeList likeList = null;
    likeList =  updateLike(activityId, like);
    return Util.getResponse(likeList, uriInfo, MediaType.APPLICATION_XML_TYPE, Response.Status.OK);
  }
 
  
  /**
   * destroys like by identityId and gets json return format
   * @param uriInfo
   * @param activityId
   * @param identityId
   * @return
   * @throws Exception
   */
  @POST
  @Path("{activityId}/likes/destroy/{identityId}.json")
  public Response jsonDestroyLike(@Context UriInfo uriInfo,
                                   @PathParam("activityId") String activityId,
                                   @PathParam("identityId") String identityId) throws Exception{
    LikeList likeList =  null;
    likeList = destroyLike(activityId, identityId);
    return Util.getResponse(likeList, uriInfo, MediaType.APPLICATION_JSON_TYPE, Response.Status.OK);
  }
  
  /**
   * destroys like by identityId and gets xml return format
   * @param uriInfo
   * @param activityId
   * @param identityId
   * @return
   * @throws Exception
   */
  @POST
  @Path("{activityId}/likes/destroy/{identityId}.xml")
  public Response xmlDestroyLike(@Context UriInfo uriInfo,
                                 @PathParam("activityId") String activityId,
                                 @PathParam("identityId") String identityId) throws Exception {
    LikeList likeList = null;
    likeList = destroyLike(activityId, identityId);
    return Util.getResponse(likeList, uriInfo, MediaType.APPLICATION_XML_TYPE, Response.Status.OK);
  }
  
  
  /**
   * shows comment list by activityId
   * @param activityId
   * @return
   */
  private CommentList showComments(String activityId) {
    CommentList commentList = new CommentList();
    commentList.setActivityId(activityId);
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String rawCommentIds = activity.getExternalId();
    //rawCommentIds can be: null || ,a,b,c,d
    if (rawCommentIds == null) {
      commentList.setComments(new ArrayList<Activity>());
    } else {
      String[] commentIds = rawCommentIds.split(",");
      for (String commentId: commentIds) {
        if (commentId.length() > 0) {
          commentList.addComment(_activityManager.getActivity(commentId));
        }
      }
    }
    return commentList;
  }
  
  /**
   * updates comment by activityId
   * @param activityId
   * @param comment
   * @return
   */
  private CommentList updateComment(String activityId, Activity comment) {
    CommentList commentList = new CommentList();
    commentList.setActivityId(activityId);
    _activityManager = getActivityManager();
    //TODO hoatle set current userId from authentication context instead of getting userId from comment
    if (comment.getUserId() == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    comment.setPostedTime(System.currentTimeMillis());
    comment.setExternalId(Activity.IS_COMMENT);
    try {
      comment = _activityManager.saveActivity(comment);
      Activity activity = _activityManager.getActivity(activityId);
      String rawCommentIds = activity.getExternalId();
      if (rawCommentIds == null) rawCommentIds = "";
      rawCommentIds += "," + comment.getId();
      activity.setExternalId(rawCommentIds);
      _activityManager.saveActivity(activity);
    } catch(Exception ex) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    commentList.addComment(comment);
    return commentList;
  }
  
  /**
   * destroys comment by activityId and commentId
   * @param activityId
   * @param commentId
   * @return
   */
  private CommentList destroyComment(String activityId, String commentId) {
    CommentList commentList = new CommentList();
    commentList.setActivityId(activityId);
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String rawCommentIds = activity.getExternalId();
    try {
      _activityManager.deleteActivity(commentId);
      commentId = "," + commentId;
      if (rawCommentIds.contains(commentId)) {
        rawCommentIds = rawCommentIds.replace(commentId, "");
        activity.setExternalId(rawCommentIds);
        _activityManager.saveActivity(activity);
      } else {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }
    } catch(Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    return commentList;
  }
  

  /**
   * shows comment list by json format
   * @param uriInfo
   * @param activityId
   * @return
   * @throws Exception
   */
  @GET
  @Path("{activityId}/comments/show.json")
  public Response jsonShowComments(@Context UriInfo uriInfo,
                                   @PathParam("activityId") String activityId) throws Exception {
    CommentList commentList = null;
    commentList = showComments(activityId);
    return Util.getResponse(commentList, uriInfo, MediaType.APPLICATION_JSON_TYPE, Response.Status.OK);
  }
  
  /**
   * shows comment list by xml format
   * @param uriInfo
   * @param activityId
   * @return
   * @throws Exception
   */
  @GET
  @Path("{activityId}/comments/show.xml")
  public Response xmlShowComments(@Context UriInfo uriInfo,
                                  @PathParam("activityId") String activityId) throws Exception {
    CommentList commentList = null;
    commentList = showComments(activityId);
    return Util.getResponse(commentList, uriInfo, MediaType.APPLICATION_XML_TYPE, Response.Status.OK);
  }
  
  /**
   * updates comment by json format
   * @param uriInfo
   * @param activityId
   * @param comment
   * @return
   * @throws Exception
   */
  @POST
  @Path("{activityId}/comments/update.json")
  @Consumes({MediaType.APPLICATION_JSON})
  public Response jsonUpdateComment(@Context UriInfo uriInfo,
                                    @PathParam("activityId") String activityId,
                                    Activity comment) throws Exception {
    CommentList commentList = null;
    commentList = updateComment(activityId, comment);
    return Util.getResponse(commentList, uriInfo, MediaType.APPLICATION_JSON_TYPE, Response.Status.OK);
  }
  
  /**
   * updates comment by xml format
   * @param uriInfo
   * @param activityId
   * @param comment
   * @return
   * @throws Exception
   */
  @POST
  @Path("{activityId}/comments/update.xml")
  @Consumes({MediaType.APPLICATION_XML})
  public Response xmlUpdateComment(@Context UriInfo uriInfo,
                                   @PathParam("activityId") String activityId,
                                   Activity comment) throws Exception {
    CommentList commentList = null;
    commentList = updateComment(activityId, comment);
    return Util.getResponse(commentList, uriInfo, MediaType.APPLICATION_XML_TYPE, Response.Status.OK);
  }
  
  /**
   * destroys comments and returns json format
   * @param uriInfo
   * @param activityId
   * @param commentId
   * @return
   * @throws Exception
   */
  @POST
  @Path("{activityId}/comments/destroy/{commentId}.json")
  public Response jsonDestroyComment(@Context UriInfo uriInfo,
                                     @PathParam("activityId") String activityId,
                                     @PathParam("commentId") String commentId) throws Exception {
    CommentList commentList = null;
    commentList = destroyComment(activityId, commentId);
    return Util.getResponse(commentList, uriInfo, MediaType.APPLICATION_JSON_TYPE, Response.Status.OK);
  }
  
  /**
   * destroys comments and returns xml format
   * @param uriInfo
   * @param activityId
   * @param commentId
   * @return
   * @throws Exception
   */
  @POST
  @Path("{activityId}/comments/destroy/{commentId}.xml")
  public Response xmlDestroyComment(@Context UriInfo uriInfo,
                                    @PathParam("activityId") String activityId,
                                    @PathParam("commentId") String commentId) throws Exception {
    CommentList commentList = null;
    commentList = destroyComment(activityId, commentId);
    return Util.getResponse(commentList, uriInfo, MediaType.APPLICATION_XML_TYPE, Response.Status.OK);
  }
  
  /**
   * LikeList model
   * @author hoatle
   */
  public class LikeList {
    private String _activityId;
    private List<Like> _likes;
    
    public void setActivityId(String activityId) {
      _activityId = activityId;
    }
    public String getActivityId() {
      return _activityId;
    }
    public void setLikes(List<Like> likes) {
      _likes = likes;
    }
    
    public List<Like> getLikes() {
      return _likes;
    }
    
    public void addLike(Like like) {
      if (_likes == null) {
        _likes = new ArrayList<Like>();
      }
      _likes.add(like);
    }
  }
  
  /**
   * CommentList model
   * @author hoatle
   *
   */
  public class CommentList {
    private String _activityId;
    private List<Activity> _comments;
    public void setActivityId(String activityId) {
      _activityId = activityId;
    }
    public String getActivityId() {
      return _activityId;
    }
    public void setComments(List<Activity> comments) {
      _comments = comments;
    }
    public List<Activity> getComments() {
      return _comments;
    }
    
    public void addComment(Activity activity) {
      if (_comments == null) {
        _comments = new ArrayList<Activity>();
      }
      _comments.add(activity);
    }
  }
  
  /**
   * gets activityManager
   * @return
   */
  private ActivityManager getActivityManager() {
    if (_activityManager == null) {
      PortalContainer portalContainer = PortalContainer.getInstance();
      _activityManager = (ActivityManager) portalContainer.getComponentInstanceOfType(ActivityManager.class);
    }
    return _activityManager;
  }
  
  /**
   * gets identityManger
   * @return
   */
  private IdentityManager getIdentityManager() {
    if (_identityManager == null) {
      PortalContainer portalContainer = PortalContainer.getInstance();
      _identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return _identityManager;
  }
  
  /**
   * gets repository
   * @return
   * @throws Exception
   */
  private String getRepository() throws Exception {
    PortalContainer portalContainer = PortalContainer.getInstance();
    RepositoryService repositoryService = (RepositoryService) portalContainer.getComponentInstanceOfType(RepositoryService.class);
    return repositoryService.getCurrentRepository().getConfiguration().getName();
  }
  
  /**
   * gets portalName
   * @return
   */
  private String getPortalName() {
    PortalContainer portalContainer = PortalContainer.getInstance();
    return portalContainer.getPortalContainerInfo().getContainerName();
  }
  
  /**
   * gets like list
   * @param identityIds
   * @return
   */
  private List<Like> getLikes(String[] identityIds) {
    String username, fullName, thumbnail;
    Profile profile;
    Identity identity;
    ProfileAttachment profileAttachment;
    Like like;
    List<Like> likes = new ArrayList<Like>();
    _identityManager = getIdentityManager();
    try {
      for (String identityId : identityIds) {
        identity = _identityManager.getIdentityById(identityId);
        profile = identity.getProfile();
        username = (String) profile.getProperty("username");
        fullName = profile.getFullName();
        profileAttachment = (ProfileAttachment)profile.getProperty("avatar");
        thumbnail = null;
        if (profileAttachment != null) {
          thumbnail = "/" + getPortalName() + "/rest/jcr/" + getRepository() + "/" + profileAttachment.getWorkspace() +
                      profileAttachment.getDataPath() + "/?rnd=" + System.currentTimeMillis();
        }
        like = new Like();
        like.setIdentityId(identityId);
        like.setUsername(username);
        like.setFullName(fullName);
        like.setThumbnail(thumbnail);
        likes.add(like);
      }
    } catch (Exception ex) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    return likes;
  }
  
  /**
   * removes an item from an array
   * @param arrays
   * @param str
   * @return new array
   */
  private String[] removeItemFromArray(String[] array, String str) {
    List<String> list = new ArrayList<String>();
    list.addAll(Arrays.asList(array));
    list.remove(str);
    if(list.size() > 0) return list.toArray(new String[list.size()]);
    else return null;
  }
  
  /**
   * adds an item to an array
   * @param array
   * @param str
   * @return new array
   */
  private String[] addItemToArray(String[] array, String str) {
    List<String> list = new ArrayList<String>();
    if(array != null && array.length > 0) {
      list.addAll(Arrays.asList(array));
      list.add(str);
      return list.toArray(new String[list.size()]);
    } else return new String[] {str};
  }
}
