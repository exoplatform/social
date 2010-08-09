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
package org.exoplatform.social.service.rest;

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
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * ActivitiesRestService.java <br />
 *
 * Provides rest services for activity gadget: like/unlike; comment; delete activity. <br />
 * apis: <br />
 * GET:  /restContextName/social/activities/{activityId}/likes/show.{format} <br />
 * POST: /restContextName/social/activities/{activityId}/likes/update.{format} <br />
 * POST: /restContextName/social/activities/{activityId}/likes/destroy/{identity}.{format} <br />
 * ... <br />
 * See methods for more api details.
 *
 *
 * @author     hoatle <hoatlevan at gmail dot com>
 * @since      Dec 29, 2009
 * @copyright  eXo Platform SEA
 */
@Path("{portalName}/social/activities")
public class ActivitiesRestService implements ResourceContainer {
  private ActivityManager _activityManager;
  private IdentityManager _identityManager;
  private String portalName_;
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
    try {
      _activityManager.deleteActivity(activityId);
    } catch(Exception ex) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    return activity;
  }

  /**
   * destroys activity and gets json/xml return format
   * @param uriInfo
   * @param activityId
   * @param format
   * @return response
   * @throws Exception
   */
  @POST
  @Path("destroy/{activityId}.{format}")
  public Response destroyActivity(@Context UriInfo uriInfo,
                                  @PathParam("portalName") String portalName,
                                  @PathParam("activityId") String activityId,
                                  @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    portalName_ = portalName;
    Activity activity = destroyActivity(activityId);
    return Util.getResponse(activity, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Shows list of like by activityId
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
   * Updates like of an activity
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
        Identity user = getIdentityManager().getIdentity(activity.getUserId());
        _activityManager.saveActivity(user, activity);
      } catch (Exception ex) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    } else {
      //TODO hoatle let it run smoothly or informs that user already liked the activity?
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
          Identity user = getIdentityManager().getIdentity(activity.getUserId());
          _activityManager.saveActivity(user, activity);
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
   * shows list of like by activityId and returns json/xml format
   * @param uriInfo
   * @param activityId
   * @param format
   * @return response
   * @throws Exception
   */
  @GET
  @Path("{activityId}/likes/show.{format}")
  public Response showLikes(@Context UriInfo uriInfo,
                            @PathParam("portalName") String portalName,
                            @PathParam("activityId") String activityId,
                            @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    portalName_ = portalName;
    LikeList likeList = null;
    likeList = showLikes(activityId);
    return Util.getResponse(likeList, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * updates like by json/xml format
   * @param uriInfo
   * @param activityId
   * @param format
   * @param like
   * @return response
   * @throws Exception
   */
  @POST
  @Path("{activityId}/likes/update.{format}")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response updateLike(@Context UriInfo uriInfo,
                             @PathParam("portalName") String portalName,
                             @PathParam("activityId") String activityId,
                             @PathParam("format") String format,
                             Like like) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    portalName_ = portalName;
    LikeList likeList = null;
    likeList = updateLike(activityId, like);
    return Util.getResponse(likeList, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * destroys like by identityId and gets json/xml return format
   * @param uriInfo
   * @param activityId
   * @param identityId
   * @param format
   * @return response
   * @throws Exception
   */
  @POST
  @Path("{activityId}/likes/destroy/{identityId}.{format}")
  public Response destroyLike(@Context UriInfo uriInfo,
                              @PathParam("portalName") String portalName,
                              @PathParam("activityId") String activityId,
                              @PathParam("identityId") String identityId,
                              @PathParam("format") String format) throws Exception{
    MediaType mediaType = Util.getMediaType(format);
    portalName_ = portalName;
    LikeList likeList =  null;
    likeList = destroyLike(activityId, identityId);
    return Util.getResponse(likeList, uriInfo, mediaType, Response.Status.OK);
  }


  /**
   * Shows comment list of an activity from its activityId.
   *
   * @param activityId
   * @return commentList
   * @see CommentList
   */
  private CommentList showComments(String activityId) {
    CommentList commentList = new CommentList();
    commentList.setActivityId(activityId);
    _activityManager = getActivityManager();
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String rawCommentIds = activity.getReplyToId();
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
   * Creates or updates comment to an activity by its activityId
   * @param activityId
   * @param comment
   * @return commentList
   * @see CommentList
   */
  private CommentList updateComment(String activityId, Activity comment, UriInfo uriInfo, String portalName) {
    CommentList commentList = new CommentList();
    commentList.setActivityId(activityId);
    Activity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    _activityManager = getActivityManager();
    //TODO hoatle set current userId from authentication context instead of getting userId from comment
//    if (comment.getUserId() == null) {
//      throw new WebApplicationException(Response.Status.BAD_REQUEST);
//    }
    ConversationState state = ConversationState.getCurrent();
    String userId = null;
    if (state != null) {
      userId = state.getIdentity().getUserId();
    } else {
      try {
        userId = getRemoteId(uriInfo, portalName);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    Identity identity = null;
    try {
      identity = getIdentityManager(portalName).getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
     //TODO hoatle set current userId from authentication context instead of getting userId from comment
     if (comment.getUserId() == null) {
       throw new WebApplicationException(Response.Status.BAD_REQUEST);
    } else {
      comment.setUserId(identity.getId());
    }


    try {
      _activityManager.saveComment(activity, comment);
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    commentList.addComment(comment);
    return commentList;
  }

  private String getRemoteId(UriInfo uriInfo, String portalName) throws Exception {
    String viewerId = Util.getViewerId(uriInfo);
    Identity identity = getIdentityManager(portalName).getIdentity(viewerId);
    return identity.getRemoteId();
  }

  /**
     * gets identityManager
     * @return
     */
    private IdentityManager getIdentityManager(String portalName) {
      if (_identityManager == null) {
        PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getContainerByName(portalName);
        _identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
      }
      return _identityManager;
    }


  /**
   * Destroys a comment (by its commentId) from an activity (by its activityId).
   *
   * @param activityId
   * @param commentId
   * @return commentList
   * @see CommentList
   */
  private CommentList destroyComment(String activityId, String commentId) {

    CommentList commentList = new CommentList();
    commentList.setActivityId(activityId);

    _activityManager = getActivityManager();

    Activity activity = _activityManager.getActivity(activityId);
    Activity comment = _activityManager.getActivity(commentId);
    if (activity == null || comment == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    commentList.addComment(comment);
    _activityManager.deleteComment(activityId, commentId);
    /*
    String rawCommentIds = activity.getReplyToId();
    try {
      if (rawCommentIds.contains(commentId)) {
        Activity comment = _activityManager.getActivity(commentId);
        if (activity == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        commentList.addComment(comment);
        _activityManager.deleteActivity(commentId);
        commentId = "," + commentId;
        rawCommentIds = rawCommentIds.replace(commentId, "");
        activity.setReplyToId(rawCommentIds);

        Identity user = getIdentityManager().getIdentity(activity.getUserId());
        _activityManager.saveActivity(user, activity);
      } else {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }
    } catch(Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    */
    return commentList;
  }


  /**
   * shows comment list by json/xml format
   * @param uriInfo
   * @param activityId
   * @param format
   * @return response
   * @throws Exception
   */
  @GET
  @Path("{activityId}/comments/show.{format}")
  public Response showComments(@Context UriInfo uriInfo,
                               @PathParam("portalName") String portalName,
                               @PathParam("activityId") String activityId,
                               @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    portalName_ = portalName;
    CommentList commentList = null;
    commentList = showComments(activityId);
    return Util.getResponse(commentList, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * updates comment by json/xml format
   * @param uriInfo
   * @param activityId
   * @param format
   * @param comment
   * @return response
   * @throws Exception
   */
  @POST
  @Path("{activityId}/comments/update.{format}")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public Response updateComment(@Context UriInfo uriInfo,
                                @PathParam("portalName") String portalName,
                                @PathParam("activityId") String activityId,
                                @PathParam("format") String format,
                                Activity comment) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    portalName_ = portalName;
    CommentList commentList = null;
    commentList = updateComment(activityId, comment, uriInfo, portalName);
    return Util.getResponse(commentList, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * destroys comments and returns json/xml format
   * @param uriInfo
   * @param activityId
   * @param commentId
   * @param format
   * @return response
   * @throws Exception
   */
  @POST
  @Path("{activityId}/comments/destroy/{commentId}.{format}")
  public Response destroyComment(@Context UriInfo uriInfo,
                                 @PathParam("portalName") String portalName,
                                 @PathParam("activityId") String activityId,
                                 @PathParam("commentId") String commentId,
                                 @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    portalName_ = portalName;
    CommentList commentList = null;
    commentList = destroyComment(activityId, commentId);
    return Util.getResponse(commentList, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * LikeList model
   * @author hoatle
   */
  @XmlRootElement
  static public class LikeList {
    private String _activityId;
    private List<Like> _likes;
    /**
     * sets activityId
     * @param activityId
     */
    public void setActivityId(String activityId) {
      _activityId = activityId;
    }
    /**
     * gets activityId
     * @return
     */
    public String getActivityId() {
      return _activityId;
    }
    /**
     * sets like list
     * @param likes like list
     */
    public void setLikes(List<Like> likes) {
      _likes = likes;
    }

    /**
     * gets like list
     * @return like list
     */
    public List<Like> getLikes() {
      return _likes;
    }

    /**
     * adds like to like list
     * @param like
     */
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
  @XmlRootElement
  static public class CommentList {
    private String _activityId;
    private List<Activity> _comments;
    /**
     * sets activityId
     * @param activityId
     */
    public void setActivityId(String activityId) {
      _activityId = activityId;
    }
    /**
     * gets activityId
     * @return activityId
     */
    public String getActivityId() {
      return _activityId;
    }
    /**
     * sets comment list
     * @param comments comment list
     */
    public void setComments(List<Activity> comments) {
      _comments = comments;
    }
    /**
     * gets comment list
     * @return comments
     */
    public List<Activity> getComments() {
      return _comments;
    }
    /**
     * add comment to comment List
     * @param activity comment
     */
    public void addComment(Activity activity) {
      if (_comments == null) {
        _comments = new ArrayList<Activity>();
      }
      _comments.add(activity);
    }
  }

  /**
   * gets activityManager
   * @return activityManager
   * @see ActivityManager
   */
  private ActivityManager getActivityManager() {
    if (_activityManager == null) {
      PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getContainerByName(portalName_);
      if (portalContainer == null) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
      _activityManager = (ActivityManager) portalContainer.getComponentInstanceOfType(ActivityManager.class);
    }
    return _activityManager;
  }

  /**
   * gets identityManger
   * @return
   * @see IdentityManager
   */
  private IdentityManager getIdentityManager() {
    if (_identityManager == null) {
      PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getContainerByName(portalName_);
      if (portalContainer == null) {
        throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
      }
      _identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return _identityManager;
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
    Like like;
    List<Like> likes = new ArrayList<Like>();
    _identityManager = getIdentityManager();
    try {
      for (String identityId : identityIds) {
        identity = _identityManager.getIdentity(identityId);
        profile = identity.getProfile();
        username = (String) profile.getProperty(Profile.USERNAME);
        fullName = profile.getFullName();
        thumbnail = profile.getAvatarImageSource((PortalContainer) ExoContainerContext.getContainerByName(portalName_));
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
