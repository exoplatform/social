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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
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
import org.exoplatform.social.service.rest.api.models.ActivityRestOut.Field;
import org.exoplatform.social.service.rest.api.models.ActivityStreamRestOut;
import org.exoplatform.social.service.rest.api.models.CommentRestOut;
import org.exoplatform.social.service.rest.api.models.IdentityRestOut;

/**
 *
 * Provides services for manipulating activities (like/unlike, comment and delete).
 * @anchor ActivitiesRestService
 *
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
   * Destroys an activity by its provided Id.
   * 
   * @param uriInfo The requested URI information.
   * @param portalName The name of current portal.
   * @param activityId The Id of the target activity which is destroyed.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @return The response contains a returned result.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor ActivitiesRestService.destroyActivity
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
   * Gets a list of users who like the activity.
   *
   * @param uriInfo The requested URI information.
   * @param portalName The name of the current portal.
   * @param activityId The Id of the target activity.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @return The response contains a returned result.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor ActivitiesRestService.showLikes
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
   * Updates the "like" information of the activity.
   *
   * @param uriInfo The requested URI information.
   * @param portalName The name of the current portal.
   * @param activityId The Id of the target activity.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @param like The object containing the information of users who like the activity.
   * @return The response contains a returned result.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor ActivitiesRestService.updateLike
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
    LikeList likeList = updateLike(activityId, like);
    return Util.getResponse(likeList, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Removes the "like" information of the activity.
   *
   * @param uriInfo The requested URI information.
   * @param portalName The name of the current portal.
   * @param activityId The Id of the target activity.
   * @param identityId The information of a user who unlikes the activity.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @return The response contains a returned result.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor ActivitiesRestService.destroyLike
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
   * Gets comments on the activity.
   * 
   * @param uriInfo The requested URI information.
   * @param portalName The name of the current portal.
   * @param activityId The Id of target activity.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @return The response contains a returned result.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor ActivitiesRestService.showComments
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
   * Gets a limit number of comments on the activity.
   * 
   * @param uriInfo The requested URI information.
   * @param portalName The name of the current portal.
   * @param activityId The Id of the target activity.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @param offset The starting point of the range to get comments. It must be greater than or equal to 0.
   * @param limit The ending point of the range to get comments.
   * @return The response contains a returned result.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor ActivitiesRestService.showComments
   */
  @GET
  @Path("{activityId}/comments.{format}")
  public Response showComments(@Context UriInfo uriInfo,
                               @PathParam("portalName") String portalName,
                               @PathParam("activityId") String activityId,
                               @PathParam("format") String format,
                               @QueryParam("offset") Integer offset,
                               @QueryParam("limit") Integer limit) throws Exception {
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
   * Gets an activity by its Id.
   *
   * @param uriInfo  The requested URI information.
   * @param portalName The name of the current portal.
   * @param portalContainerName The associated portal container name.
   * @param activityId The Id of the target activity.
   * @param format The format of the returned result, for example: JSON, or XML.
   * @param showPosterIdentity The identity information of the poster.
   * @param numberOfComments The number of comments will be returned.
   * @param showActivityStream Specifies the stream of an activity.
   * @param numberOfLikes The number of likes will be returned.
   * @return The response contains a returned result.
   * @LevelAPI Platform
   * @anchor ActivitiesRestService.getActivityById
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
    model.setLastUpdatedTime(activity.getUpdated().getTime());
    model.setCreatedAt(Util.convertTimestampToTimeString(activity.getPostedTime()));
    model.setTitleId(activity.getTitleId());
    model.setTemplateParams(activity.getTemplateParams());
    
    if(activity.getLikeIdentityIds() != null){
      model.setTotalNumberOfLikes(activity.getLikeIdentityIds().length);
    } else {
      model.setTotalNumberOfLikes(null);
    }
    
    if(Util.isLikedByIdentity(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, Util.getViewerId(uriInfo),
                                                                  true).getId(),activity)){
      model.setLiked(true);
    } else {
      model.setLiked(false);
    }
    
    RealtimeListAccess<ExoSocialActivity> commentRealtimeListAccess = Util.getActivityManager(portalContainerName).
                                                                           getCommentsWithListAccess(activity);
    model.setTotalNumberOfComments(commentRealtimeListAccess.getSize());
    
    Identity streamOwnerIdentity = Util.getOwnerIdentityIdFromActivity(portalContainerName, activity);
    if(streamOwnerIdentity != null){
      model.put(Field.IDENTITY_ID.toString(),streamOwnerIdentity.getId());
    }
    
    
    
    if (isPassed(showPosterIdentity)) {
      model.setPosterIdentity(new IdentityRestOut(identityManager.getIdentity(activity.getUserId(), false)));
    }
    
    if (isPassed(showActivityStream)) {
      model.setActivityStream(new ActivityStreamRestOut(activity.getActivityStream(), portalContainerName));
    }
    
    model.setNumberOfComments(numberOfComments, activity, portalContainerName);
    
    return Util.getResponse(model, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Updates the comment information on the activity.
   * 
   * @param uriInfo The requested URI information.
   * @param portalName The name of the current portal.
   * @param activityId The Id of the target comment which is updated.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @param comment The comment to be updated.
   * @return response The response contains a returned result.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor ActivitiesRestService.updateComment
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
   * Add comments to an existing activity.
   *
   * @param uriInfo The requested URI information.
   * @param portalName The associated portal container name.
   * @param activityId The Id of the activity.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @param text The content of the comment.
   * @return The response contains a returned result.
   * @LevelAPI Platform
   * @anchor ActivitiesRestService.createCommentActivityById
   */
  @GET
  @Path("{activityId}/comments/create.{format}")
  public Response createCommentActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalName") String portalName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format,
                                           @QueryParam("text") String text) {
    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalName);
    
    if(text == null || text.trim().equals("")){
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
      
    MediaType mediaType = RestChecker.checkSupportedFormat(format, new String[]{"json"});

    Identity currentIdentity = Util.getIdentityManager(portalName).getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                                       Util.getViewerId(uriInfo), false);
    ActivityManager activityManager = Util.getActivityManager(portalName);
    ExoSocialActivity activity = null;
    
    try {
      activity = activityManager.getActivity(activityId);
    } catch (UndeclaredThrowableException undeclaredThrowableException){
      if(undeclaredThrowableException.getCause() instanceof ActivityStorageException){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    
    ExoSocialActivity commentActivity = new ExoSocialActivityImpl();
    commentActivity.setTitle(text);
    commentActivity.setUserId(currentIdentity.getId());
    
    activityManager.saveComment(activity,commentActivity);
    
    CommentRestOut commentOut = new CommentRestOut(commentActivity, portalName);
    
    return Util.getResponse(commentOut, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Removes comments on the activity.
   * 
   * @param uriInfo The requested URI information.
   * @param portalName The name of the current portal.
   * @param activityId The activity Id.
   * @param commentId The Id of the target comment which is destroyed.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @return response The response contains a returned result.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor ActivitiesRestService.destroyComment
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
   * Destroys an activity by activityId.
   * If any comments of that activity are detected, they will be destroyed, too.
   * @param activityId
   * @anchor ActivitiesRestService
   * @return activity
   */
  private ExoSocialActivity destroyActivity(String activityId) {
    _activityManager = getActivityManager();
    ExoSocialActivity activity = _activityManager.getActivity(activityId);

    if (activity == null) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    try {
      _activityManager.deleteActivity(activityId);
    } catch(Exception ex) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    return activity;
  }
  
  /**
   * Show list of like by activityId.
   * @param activityId
   * @return
   * @throws Exception
   */
  private LikeList showLikes(String activityId) {
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

    //
    LikeList likeList = new LikeList();
    likeList.setActivityId(activityId);
    likeList.setLikes(getLikes(activity.getLikeIdentityIds()));
    return likeList;
  }
  
  /**
   * Update like of an activity.
   * @param activityId
   * @param like
   * @throws Exception
   */
  private LikeList updateLike(String activityId, Like like) throws Exception {

    _activityManager = getActivityManager();
    _identityManager = getIdentityManager();

    //
    ExoSocialActivity activity = _activityManager.getActivity(activityId);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    //
    String identityId = like.getIdentityId();
    if (identityId == null) {
      throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
    }
    Identity identity = _identityManager.getIdentity(like.getIdentityId(), false);

    //
    _activityManager.saveLike(activity, identity);

    //
    LikeList likeList = new LikeList();
    likeList.setActivityId(activityId);
    likeList.setLikes(getLikes(activity.getLikeIdentityIds()));
    return likeList;
  }

  /**
   * Destroy like from an activity.
   * @param activityId
   * @param identityId
   */
  private LikeList destroyLike(String activityId, String identityId) {

    _activityManager = getActivityManager();
    ExoSocialActivity activity = null;
    //
    try {
      activity = _activityManager.getActivity(activityId);
    } catch (ActivityStorageException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    if (activity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    //
    if (identityId == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    try {
      Identity user = getIdentityManager().getIdentity(identityId, false);
      _activityManager.deleteLike(activity, user);
    } catch(Exception ex) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }

    //
    LikeList likeList = new LikeList();
    likeList.setActivityId(activityId);
    likeList.setLikes(getLikes(activity.getLikeIdentityIds()));

    return likeList;
  }
  
  /**
   * Show comment list of an activity from its activityId.
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
   * Create or update comment to an activity by its activityId.
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
      identity = getIdentityManager(portalName).getOrCreateIdentity(OrganizationIdentityProvider.NAME, Util.getViewerId(uriInfo),
                                                                    false);
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
   * Get identityManager.
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
   * Destroy a comment (by its commentId) from an activity (by its activityId).
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
    Profile profile;
    Identity identity;
    Like like;
    List<Like> likes = new ArrayList<Like>();
    _identityManager = getIdentityManager();
    try {
      for (String identityId : identityIds) {
        identity = _identityManager.getIdentity(identityId, false);
        profile = identity.getProfile();
        like = new Like();
        like.setIdentityId(identityId);
        like.setUsername((String) profile.getProperty(Profile.USERNAME));
        like.setFullName(profile.getFullName());
        like.setThumbnail(profile.getAvatarUrl());
        likes.add(like);
      }
    } catch (Exception ex) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    return likes;
  }
  
  private boolean isPassed(String value) {
    return value != null && ("true".equals(value) || "t".equals(value) || "1".equals(value));
  }
}
