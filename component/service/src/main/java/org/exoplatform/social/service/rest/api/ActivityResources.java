/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.service.rest.api;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.DELETE;
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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.service.rest.RestChecker;
import org.exoplatform.social.service.rest.SecurityManager;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api.models.ActivityRestIn;
import org.exoplatform.social.service.rest.api.models.ActivityRestOut;
import org.exoplatform.social.service.rest.api.models.ActivityStreamRestOut;
import org.exoplatform.social.service.rest.api.models.CommentRestIn;
import org.exoplatform.social.service.rest.api.models.CommentRestOut;
import org.exoplatform.social.service.rest.api.models.IdentityRestOut;

/**
 * Activity Resources end point.
 * @author <a href="http://phuonglm.net">PhuongLM</a>
 * @since Jun 15, 2011
 */
@Path("api/social/" + VersionResources.LATEST_VERSION+ "/{portalContainerName}/")
public class ActivityResources implements ResourceContainer {

  private static final String[] SUPPORTED_FORMAT = new String[]{"json"};
  private static final int MAX_NUMBER_OF_COMMENT = 100;
  private static final int MAX_NUMBER_OF_LIKE = 100;

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
  @Path("activity/{activityId}.{format}")
  public Response getActivityById(@Context UriInfo uriInfo,
                                  @PathParam("portalContainerName") String portalContainerName,
                                  @PathParam("activityId") String activityId,
                                  @PathParam("format") String format,
                                  @QueryParam("poster_identity") String showPosterIdentity,
                                  @QueryParam("number_of_comments") int numberOfComments,
                                  @QueryParam("activity_stream") String showActivityStream,
                                  @QueryParam("number_of_likes") int numberOfLikes) {

    RestChecker.checkAuthenticatedRequest();

    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalContainerName);
    
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMAT);

    Identity authenticatedIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    IdentityManager identityManager = Util.getIdentityManager(portalContainerName);
    //
    ExoSocialActivity activity = null;
    
    try{
      activity = activityManager.getActivity(activityId);
    } catch (UndeclaredThrowableException undeclaredThrowableException) {
      if(undeclaredThrowableException.getCause() instanceof ActivityStorageException){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    
    if(activity.isComment()){
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    
    if(!SecurityManager.canAccessActivity(portalContainer,ConversationState.getCurrent().getIdentity().getUserId(),activity)){
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    //
    ActivityRestOut model = new ActivityRestOut(activity, portalContainerName);
    
    model.setNumberOfLikes(numberOfLikes, activity, portalContainerName);
    
    //
    if (isPassed(showPosterIdentity)) {
      model.setPosterIdentity(new IdentityRestOut(identityManager.getIdentity(activity.getUserId(), false)));
    }

    //
    if (isPassed(showActivityStream)) {
      model.setActivityStream(new ActivityStreamRestOut(activity.getActivityStream()));
    }
    
    model.setNumberOfComments(numberOfComments, activity, portalContainerName);

    if(isLikedByIdentity(authenticatedIdentity.getId(),activity)){
      model.setLiked(true);
    } else {
      model.setLiked(false);
    }
    
    return Util.getResponse(model, uriInfo, mediaType, Response.Status.OK);
  }


  /**
   * Creates a new activity.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param format the expected returned format
   * @param identityIdStream the optional identity stream to post this new activity to
   * @param newActivity a new activity instance
   * @return a response object
   */
  @POST
  @Path("activity.{format}")
  public Response createNewActivity(@Context UriInfo uriInfo,
                                    @PathParam("portalContainerName") String portalContainerName,
                                    @PathParam("format") String format,
                                    @QueryParam("identity_id") String identityIdStream,
                                    ActivityRestIn newActivity) {
    RestChecker.checkAuthenticatedRequest();
    
    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalContainerName);
    
    if(newActivity == null || newActivity.getTitle() == null || newActivity.getTitle().trim().equals("")){
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMAT);

    Identity postToIdentity;
    Identity authenticatedUserIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    IdentityManager identityManager =  Util.getIdentityManager(portalContainerName);
    
    if(identityIdStream != null && !identityIdStream.equals("")){
      postToIdentity = identityManager.getIdentity(identityIdStream, false);
      if(postToIdentity == null){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } else {
      postToIdentity = authenticatedUserIdentity;
    }
    
    if(!SecurityManager.canPostActivity(portalContainer, authenticatedUserIdentity, postToIdentity)){
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }  
    ExoSocialActivity activity = new ExoSocialActivityImpl();          
      
    activity.setTitle(newActivity.getTitle());
    activity.setType(newActivity.getType());
    activity.setPriority(newActivity.getPriority());
    activity.setTitleId(newActivity.getTitleId());
    activity.setTemplateParams(newActivity.getTemplateParams());
    
    activity.setUserId(authenticatedUserIdentity.getId());
    activityManager.saveActivityNoReturn(postToIdentity, activity);
    
    //
    ActivityRestOut model = new ActivityRestOut(activity, portalContainerName);
    model.setIdentityId(postToIdentity.getId());
    return Util.getResponse(model, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Deletes an existing activity by DELETE method from a specified activity id. Just returns the deleted activity
   * object.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @return a response object
   */
  @DELETE
  @Path("activity/{activityId}.{format}")
  public Response deleteExistingActivityById(@Context UriInfo uriInfo,
                                            @PathParam("portalContainerName") String portalContainerName,
                                            @PathParam("activityId") String activityId,
                                            @PathParam("format") String format) {
    RestChecker.checkAuthenticatedRequest();
    
    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalContainerName);
    
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMAT);
    
    Identity authenticatedUserIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    ExoSocialActivity existingActivity = null;
    try{
      existingActivity = activityManager.getActivity(activityId);
    } catch (UndeclaredThrowableException undeclaredThrowableException){
      if(undeclaredThrowableException.getCause() instanceof ActivityStorageException){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
      
    if(!SecurityManager.canDeleteActivity(portalContainer, authenticatedUserIdentity, existingActivity)){
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    activityManager.deleteActivity(existingActivity);
    HashMap<String, String> resultJson = new HashMap<String, String>();
    resultJson.put("id", existingActivity.getId());
  
    return Util.getResponse(resultJson, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Deletes an existing activity by POST method from a specified activity id. Just returns the deleted activity
   * object. Deletes by DELETE method is recommended. This API should be used only when DELETE method is not supported
   * by the client.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @return a response object
   */
  @POST
  @Path("activity/destroy/{activityId}.{format}")
  public Response postToDeleteActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format) {
    return deleteExistingActivityById(uriInfo, portalContainerName, activityId, format);
  }

  /**
   * Get Comment from existing activity by GET method from a specified activity id. Just returns the Comment List and total 
   * number of Comment in activity.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @return a response object
   */
  @GET
  @Path("activity/{activityId}/comments.{format}")
  public Response getCommentsByActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format) {
    RestChecker.checkAuthenticatedRequest();
    
    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalContainerName);
    
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMAT);

    Identity authenticatedIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    ExoSocialActivity activity = null;
    try {
      activity = activityManager.getActivity(activityId);
      if(!SecurityManager.canAccessActivity(portalContainer, authenticatedIdentity, activity)){
        throw new WebApplicationException(Response.Status.FORBIDDEN);
      }
      
      int total;
      List<CommentRestOut> commentWrapers = null;
      ListAccess<ExoSocialActivity> comments =  activityManager.getCommentsWithListAccess(activity);
      total = comments.getSize();
      ExoSocialActivity[] commentsLimited =  comments.load(0, total);
      commentWrapers = new ArrayList<CommentRestOut>(total);
      for(int i = 0; i < total; i++){
        CommentRestOut commentRestOut = new CommentRestOut(commentsLimited[i], portalContainerName);
        commentRestOut.setPosterIdentity(commentsLimited[i], portalContainerName);
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
   * Comment an existing activity by POST method from a specified activity id. Just returns the created comment.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @return a response object
   */
  @POST
  @Path("activity/{activityId}/comment.{format}")
  public Response createCommentActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format,
                                           CommentRestIn comment) {
    
    RestChecker.checkAuthenticatedRequest();

    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalContainerName);
    
    if(comment == null || comment.getText() == null || comment.getText().trim().equals("")){
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
      
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMAT);

    Identity authenticatedIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
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
    
    if(!SecurityManager.canCommentToActivity(portalContainer, authenticatedIdentity, activity)){
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
      
    ExoSocialActivity commentActivity = new ExoSocialActivityImpl();
    commentActivity.setTitle(comment.getText());
    commentActivity.setUserId(authenticatedIdentity.getId());
    
    activityManager.saveComment(activity,commentActivity);
    
    CommentRestOut commentOut = new CommentRestOut(commentActivity, portalContainerName);
    
    return Util.getResponse(commentOut, uriInfo, mediaType, Response.Status.OK);

  }
  
  /**
   * Comment an existing activity by POST method from a specified activity id. Just returns the created comment.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @return a response object
   */
  @DELETE
  @Path("activity/{activityId}/comment/{commentId}.{format}")
  public Response deleteCommentById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format,
                                           @PathParam("commentId") String commentId) {
    RestChecker.checkAuthenticatedRequest();
    
    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalContainerName);
    
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMAT);

    Identity authenticatedIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    
    ExoSocialActivity commentActivity = null;
    ExoSocialActivity activity = null;
    
    try{
      activity = activityManager.getActivity(activityId);
      commentActivity = activityManager.getActivity(commentId);
    } catch (UndeclaredThrowableException undeclaredThrowableException){
      if(undeclaredThrowableException.getCause() instanceof ActivityStorageException){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    
    if(!commentActivity.isComment()){
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    if(!SecurityManager.canDeleteActivity(portalContainer, authenticatedIdentity, commentActivity)){
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    activityManager.deleteComment(activity, commentActivity);
    HashMap<String, Object> resultHashmap = new HashMap<String, Object>();
    resultHashmap.put("id", commentActivity.getId());
    return Util.getResponse(resultHashmap, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Delete a Comment in activity spectify by commentId and activityId using DELETE.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @return a response object
   */
  @POST
  @Path("activity/{activityId}/comment/destroy/{commentId}.{format}")
  public Response postDeleteCommentById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format,
                                           @PathParam("commentId") String commentId) {
    return deleteCommentById(uriInfo, portalContainerName, activityId, format, commentId);
  }
  
  
  /** Get liked identities from activityId 
   * 
   * @param uriInfo
   * @param portalContainerName
   * @param activityId
   * @param format
   * @return
   */
  @GET
  @Path("activity/{activityId}/likes.{format}")
  public Response getLikesFromIdentityId(@Context UriInfo uriInfo,
      @PathParam("portalContainerName") String portalContainerName,
      @PathParam("activityId") String activityId,
      @PathParam("format") String format) {
    RestChecker.checkAuthenticatedRequest();

    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalContainerName);     

    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMAT);

    Identity authenticatedIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    //
    ExoSocialActivity activity = null;
    try{
      activity = activityManager.getActivity(activityId);
    } catch (UndeclaredThrowableException undeclaredThrowableException) {
      if(undeclaredThrowableException.getCause() instanceof ActivityStorageException){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    
    if(!SecurityManager.canAccessActivity(portalContainer, authenticatedIdentity, activity)){
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    HashMap<String, Object> hashmapResult = new HashMap<String, Object>();

    if(activity.getLikeIdentityIds() != null && activity.getLikeIdentityIds().length > 0){
      int numberOfLikeLimited = Math.min(MAX_NUMBER_OF_LIKE, activity.getLikeIdentityIds().length);
      
      List<IdentityRestOut> likedIdentitiesLimited = null ;
      String[] getLikeIdentityIds = activity.getLikeIdentityIds();
      likedIdentitiesLimited = new ArrayList<IdentityRestOut>(numberOfLikeLimited);
      for (int i = 0; i < numberOfLikeLimited; i++) {
        likedIdentitiesLimited.add(
            new IdentityRestOut(
                getLikeIdentityIds[getLikeIdentityIds.length - i - 1],
                portalContainerName)
            );
      }
      hashmapResult.put("totalNumberOfLikes", activity.getLikeIdentityIds().length);
      hashmapResult.put("likesByIdentities", likedIdentitiesLimited);
    } else {
      hashmapResult.put("totalNumberOfLikes", 0);
      hashmapResult.put("likesByIdentities", new ArrayList<IdentityRestOut>());
    }
    return Util.getResponse(hashmapResult, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Like an existing activity by POST method from a specified activity id. Just returns {"liked": "true"} if success.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @return a response object
   */
  @POST
  @Path("activity/{activityId}/like.{format}")
  public Response createLikeActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format) {
    RestChecker.checkAuthenticatedRequest();

    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalContainerName);
    
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMAT);

    Identity authenticatedUserIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    ExoSocialActivity activity = null;
    try{
      activity = activityManager.getActivity(activityId);
    } catch (UndeclaredThrowableException undeclaredThrowableException){
      if(undeclaredThrowableException.getCause() instanceof ActivityStorageException){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    
    if(!SecurityManager.canCommentToActivity(portalContainer, authenticatedUserIdentity, activity)){        
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    activityManager.saveLike(activity, authenticatedUserIdentity);
    HashMap resultJson = new HashMap();
    resultJson.put("liked", true);
    return Util.getResponse(resultJson, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Like an existing activity by POST method from a specified activity id. Just returns {"liked": "true"} if success.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @return a response object
   */
  @DELETE
  @Path("activity/{activityId}/like.{format}")
  public Response deleteLikeActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format) {
    RestChecker.checkAuthenticatedRequest();

    PortalContainer portalContainer = RestChecker.checkValidPortalContainerName(portalContainerName);
    
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMAT);

    Identity authenticatedUserIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    ExoSocialActivity activity = null;
    try{
      activity = activityManager.getActivity(activityId);
    } catch (UndeclaredThrowableException undeclaredThrowableException){
      if(undeclaredThrowableException.getCause() instanceof ActivityStorageException){
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    
    if(!SecurityManager.canCommentToActivity(portalContainer, authenticatedUserIdentity, activity)){          
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    activityManager.deleteLike(activity, authenticatedUserIdentity);
    
    HashMap resultJson = new HashMap();
    resultJson.put("liked", false);
    
    return Util.getResponse(resultJson, uriInfo, mediaType, Response.Status.OK);

  }
  
  /**
   * Like an existing activity by POST method from a specified activity id. Just returns {"liked": "true"} if success.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @return a response object
   */
  @POST
  @Path("activity/{activityId}/like/destroy.{format}")
  public Response postDeleteLikeActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format) {
    return deleteLikeActivityById(uriInfo, portalContainerName, activityId, format);
  }
  
  private boolean isLikedByIdentity(String identityID, ExoSocialActivity activity){
    String[] likedIdentityIds = activity.getLikeIdentityIds();
    if(activity.getLikeIdentityIds()!=null && likedIdentityIds.length > 0 ){
      for (int i = 0; i < likedIdentityIds.length; i++) {
        if (identityID.equals(likedIdentityIds[i])){
          return true;
        }
      }
    }
    return false;
  }
  
  private boolean isPassed(String value) {
    return value != null && ("true".equals(value) || "t".equals(value) || "1".equals(value));
  }
}
