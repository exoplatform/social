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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.service.rest.api.models.ActivityRestOut;
import org.exoplatform.social.service.rest.api.models.ActivityStreamRestOut;
import org.exoplatform.social.service.rest.api.models.CommentRestOut;
import org.exoplatform.social.service.rest.api.models.IdentityRestOut;
import org.exoplatform.social.service.rest.api.models.ActivityRestOut.Field;

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

  /**
   * The logger.
   */
  private static final Log LOG = ExoLogger.getLogger(ActivitiesRestService.class);

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
  private ExoSocialActivity destroyActivity(String activityId) {
    _activityManager = getActivityManager();
    ExoSocialActivity activity = null;
    try {
      activity = _activityManager.getActivity(activityId);
    } catch (ActivityStorageException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
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
    ExoSocialActivity activity = destroyActivity(activityId);
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
    ExoSocialActivity activity = null;
    try {
      activity = _activityManager.getActivity(activityId);
    } catch (ActivityStorageException e) {
      throw new WebApplicationException((Response.Status.INTERNAL_SERVER_ERROR));
    }
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
    ExoSocialActivity activity = _activityManager.getActivity(activityId);
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
        //Identity user = getIdentityManager().getIdentity(activity.getUserId(),true);
        _activityManager.updateActivity(activity);
        activity = _activityManager.getActivity(activityId);
      } catch (Exception ex) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    } else {
      //TODO hoatle let it run smoothly or informs that user already liked the activity?
    }
    likeList.setLikes(getLikes(activity.getLikeIdentityIds()));
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
    ExoSocialActivity activity = null;
    try {
      activity = _activityManager.getActivity(activityId);
    } catch (ActivityStorageException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    String[] identityIds = activity.getLikeIdentityIds();
    if (identityIds.length == 0) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    boolean alreadyLiked = true;
    for (String id : identityIds) {
      if (id.equals(identityId)) {
        identityIds = removeItemFromArray(identityIds, identityId);
        if (identityIds == null) {
          identityIds = new String [] {};
        }

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
    ExoSocialActivity activity = null;
    try {
      activity = _activityManager.getActivity(activityId);
      if (activity == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      String[] commentIds = activity.getReplyToId();
      if (commentIds == null) {
        commentList.setComments(new ArrayList<ExoSocialActivity>());
      } else {
        for (String commentId: commentIds) {
          if (commentId.length() > 0) {
            commentList.addComment(_activityManager.getActivity(commentId));
          }
        }
      }
    } catch (ActivityStorageException e) {
      throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
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
  private CommentList updateComment(String activityId, ExoSocialActivity comment, UriInfo uriInfo, String portalName) {
    CommentList commentList = new CommentList();
    commentList.setActivityId(activityId);
    ExoSocialActivity activity = null;
    try {
      activity = _activityManager.getActivity(activityId);
    } catch (ActivityStorageException e) {
      throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
    }
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
        userId = Util.getViewerId(uriInfo);
      } catch (Exception e) {
        LOG.warn(e.getMessage(), e);
      }
    }
    Identity identity = null;
    try {
      identity = getIdentityManager(portalName).getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
    } catch (Exception e1) {
      LOG.warn(e1.getMessage(), e1);
    }
    
    if (identity == null) {
      identity = getIdentityManager(portalName).getOrCreateIdentity(OrganizationIdentityProvider.NAME, Util.getViewerId(uriInfo), false);  
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

    ExoSocialActivity activity = null;
    try {
      activity = _activityManager.getActivity(activityId);
      ExoSocialActivity comment = _activityManager.getActivity(commentId);
      if (activity == null || comment == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      commentList.addComment(comment);
      _activityManager.deleteComment(activityId, commentId);
    } catch (ActivityStorageException e) {
      throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
    }
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
   * Shows comment list by json/xml format with limit and offset.
   * 
   * @param uriInfo
   * @param activityId
   * @param format
   * @param offset
   * @param limit
   * @return response
   * @throws Exception
   */
  @GET
  @Path("{activityId}/comments.{format}")
  public Response showComments(@Context UriInfo uriInfo,
                               @PathParam("portalName") String portalName,
                               @PathParam("activityId") String activityId,
                               @PathParam("format") String format,
                               @QueryParam("offset") Integer offset,
                               @QueryParam("limit") Integer limit) throws Exception {
    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalName);
    MediaType mediaType = RestChecker.checkSupportedFormat(format, new String[]{"json"});
    
    ActivityManager activityManager = Util.getActivityManager(portalName);
    
    if(offset == null || limit == null ){
      offset = 0;
      limit = 10;
    }
    
    if(offset < 0 || limit < 0){
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    ExoSocialActivity activity = null;
    try {
      activity = activityManager.getActivity(activityId);
      
      int total;
      List<CommentRestOut> commentWrapers = null;
      ListAccess<ExoSocialActivity> comments =  activityManager.getCommentsWithListAccess(activity);
      
      if(offset > comments.getSize()){
        offset = 0;
        limit = 0;
      } else if(offset + limit > comments.getSize()){
        limit = comments.getSize() - offset;
      }
      
      total = limit;
      
      ExoSocialActivity[] commentsLimited =  comments.load(offset, total + offset);
      commentWrapers = new ArrayList<CommentRestOut>(total);
      for(int i = 0; i < total; i++){
        CommentRestOut commentRestOut = new CommentRestOut(commentsLimited[i], portalName);
        commentRestOut.setPosterIdentity(commentsLimited[i], portalName);
        commentWrapers.add(commentRestOut);      
      }
      
      HashMap<String, Object> resultJson = new HashMap<String, Object>();
      resultJson.put("totalNumberOfComments", commentWrapers.size());
      resultJson.put("comments", commentWrapers);
      return Util.getResponse(resultJson, uriInfo, mediaType, Response.Status.OK);
      
    } catch (UndeclaredThrowableException undeclaredThrowableException){
      if(undeclaredThrowableException.getCause() instanceof ActivityStorageException){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    } catch (Exception e){
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Gets an activity by its id.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity Id
   * @param format the expected returned format
   * @return a response object
   */
  @GET
  @Path("{activityId}.{format}")
  public Response getActivityById(@Context UriInfo uriInfo,
                                  @PathParam("portalName") String portalContainerName,
                                  @PathParam("activityId") String activityId,
                                  @PathParam("format") String format,
                                  @QueryParam("poster_identity") String showPosterIdentity,
                                  @QueryParam("number_of_comments") int numberOfComments,
                                  @QueryParam("activity_stream") String showActivityStream,
                                  @QueryParam("number_of_likes") int numberOfLikes) {
    
    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalContainerName);
    MediaType mediaType = RestChecker.checkSupportedFormat(format, new String[]{"json"});
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    IdentityManager identityManager = Util.getIdentityManager(portalContainerName);
    
    ExoSocialActivity activity = null;
    try {
      activity = activityManager.getActivity(activityId);
    } catch (UndeclaredThrowableException undeclaredThrowableException) {
      if(undeclaredThrowableException.getCause() instanceof ActivityStorageException){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    
    if(activity.isComment()) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    ActivityRestOut model = new ActivityRestOut();
    
    
    model.setId(activity.getId());
    model.setTitle(activity.getTitle());
    model.setPriority(activity.getPriority());
    model.setAppId(activity.getAppId());
    model.setType(activity.getType());
    model.setPostedTime(activity.getPostedTime());
    model.setCreatedAt(Util.convertTimestampToTimeString(activity.getPostedTime()));
    model.setTitleId(activity.getTitleId());
    model.setTemplateParams(activity.getTemplateParams());
    
    if(activity.getLikeIdentityIds() != null){
      model.setTotalNumberOfLikes(activity.getLikeIdentityIds().length);
    } else {
      model.setTotalNumberOfLikes(null);
    }
    
    if(Util.isLikedByIdentity(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, Util.getViewerId(uriInfo), true).getId(),activity)){
      model.setLiked(true);
    } else {
      model.setLiked(false);
    }
    
    RealtimeListAccess<ExoSocialActivity> commentRealtimeListAccess = Util.getActivityManager(portalContainerName).getCommentsWithListAccess(activity);
    model.setTotalNumberOfComments(commentRealtimeListAccess.getSize());
    
    Identity streamOwnerIdentity = Util.getOwnerIdentityIdFromActivity(portalContainerName, activity);
    if(streamOwnerIdentity != null){
      model.put(Field.IDENTITY_ID.toString(),streamOwnerIdentity.getId());
    }
    
    
    
    if (isPassed(showPosterIdentity)) {
      model.setPosterIdentity(new IdentityRestOut(identityManager.getIdentity(activity.getUserId(), false)));
    }
    
    if (isPassed(showActivityStream)) {
      model.setActivityStream(new ActivityStreamRestOut(activity.getActivityStream()));
    }
    
    model.setNumberOfComments(numberOfComments, activity, portalContainerName);
    
    return Util.getResponse(model, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Updates comment by json/xml format.
   * 
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
                                ExoSocialActivityImpl comment) throws Exception {
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
    private List<ExoSocialActivity> _comments;
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
    public void setComments(List<ExoSocialActivity> comments) {
      _comments = comments;
    }
    /**
     * gets comment list
     * @return comments
     */
    public List<ExoSocialActivity> getComments() {
      return _comments;
    }
    /**
     * add comment to comment List
     * @param activity comment
     */
    public void addComment(ExoSocialActivity activity) {
      if (_comments == null) {
        _comments = new ArrayList<ExoSocialActivity>();
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
        thumbnail = profile.getAvatarUrl();
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
   * @param array
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
  
  private boolean isPassed(String value) {
    return value != null && ("true".equals(value) || "t".equals(value) || "1".equals(value));
  }
}
