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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.service.rest.RestChecker;
import org.exoplatform.social.service.rest.SecurityManager;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.models.ActivityRestIn;
import org.exoplatform.social.service.rest.api.models.ActivityRestOut;
import org.exoplatform.social.service.rest.api.models.ActivityStreamRestOut;
import org.exoplatform.social.service.rest.api.models.CommentRestIn;
import org.exoplatform.social.service.rest.api.models.CommentRestOut;
import org.exoplatform.social.service.rest.api.models.IdentityRestOut;

/**
 * Activity Resources end point.
 * @anchor ActivityResources
 *
 */
@Path("api/social/" + VersionResources.LATEST_VERSION+ "/{portalContainerName}/")
public class ActivityResources implements ResourceContainer {

  private static final String[] SUPPORTED_FORMAT = new String[]{"json"};
  private static final int MAX_NUMBER_OF_LIKE = 100;

  /**
   * Gets an activity object from a specified activity Id.
   *
   * @param uriInfo The uri request info.
   * @param portalContainerName The associated portal container name.
   * @param activityId The specified activity Id.
   * @param format The expected returned format.
   * @param showPosterIdentity When this parameter is set to true, t or 1, the returned activity will provide more
   * information for the user who posted this activity.
   * @param numberOfComments Specify the number of comments to be displayed along with this activity. By default,
   * _number\_of\_comments=0_. If _number_of_comments_ is a positive number, this number is considered as a limit number that
   * must be equal or less than 100. If the actual number of comments is less than the provided positive number, the
   * number of actual comments must be returned. If the total number of comments is more than 100, it is recommended to
   * use _activity/\:id/comments.format_ instead.
   * @param numberOfLikes Specify the number of latest detailed likes to be returned along with this activity. By
   * default, _number\_of\_likes=0_. If _number\_of\_likes_ is a positive number, this number is considered as a limit
   * number that must be equal or less than 100. If the total number of likes is less than the provided positive number,
   * the number of actual likes must be returned. If the total number of likes is more than 100, it is recommended to
   * use _activity/\:activityId/likes.format_ instead.
   * @param showActivityStream When this parameter is set to true, t or 1, the returned activity will provide more
   * information for the activity stream that this activity belongs to.
   * @authentication
   * @request
   * GET http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/1a2b3c4d5e6f7g8h9i.json
   * @response
   * {
   *   "id": "1a2b3c4d5e6f7g8h9j",
   *   "title": "Hello World!!!",
   *   "appId": "",
   *   "type": "exosocial:core",
   *   "postedTime": 123456789, //timestamp
   *   "createdAt": "Fri Jun 17 06:42:26 +0000 2011", //The Date follows ISO 8601
   *   "priority": 0.5, //between 0.0 and 1.0, higher value => higher priority.
   *   "templateParams": {},
   *   "titleId": "",
   *   "identityId": "123456789abcdefghi", //the identity id of the user who created this activity
   *   "liked": true, //is liked (favorites) by this authenticated identity
   *   "likedByIdentities": ["identityId1", "identityId2"],
   *   "posterIdentity": {}, //optional
   *   "comments": [{}, {}, {}], //optional
   *   "totalNumberOfComments": 1234,
   *   "activityStream": {
   *   	"type": "user", // or "space"
   *   	"prettyId": "root", // or space_abcde
   *   	"faviconURL": "http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *   	"title": "Activity Stream of Root Root",
   *   	"permaLink": "http://localhost:8080/profile/root"
   * 	} //optional
   * }
   * @return a response object
   * @LevelAPI Platform
   * @anchor ActivityResource.getActivityById
   */
  @GET
  @Path("activity/{activityId}.{format}")
  @Produces(MediaType.APPLICATION_JSON)
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
      if(activity == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } catch (UndeclaredThrowableException undeclaredThrowableException) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
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
      model.setActivityStream(new ActivityStreamRestOut(activity.getActivityStream(), portalContainerName));
    }
    
    model.setNumberOfComments(numberOfComments, activity, portalContainerName);
    
    return Util.getResponse(model, uriInfo, mediaType, Response.Status.OK);
  }


  /**
   * Creates an activity to an identity's activity stream. If no _identity\_id_ is specified, the activity will be created
   * to the authenticated identity's activity stream.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName The associated portal container name.
   * @param format The expected returned format.
   * @param identityIdStream The optional identity stream to post this new activity to.
   * @param newActivity A new activity instance.
   * @authentication
   * @request
   * http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity.json
   * BODY: {"title": "Hello World!!!"}
   * @response
   * {
   *   "id": "1a2b3c4d5e6f7g8h9j",
   *   "title": "Hello World!!!",
   *   "appId": "",
   *   "type": "exosocial:core",
   *   "postedTime": 123456789, //timestamp
   *   "createdAt": "Fri Jun 17 06:42:26 +0000 2011",
   *   "priority": 0.5, //between 0.0 and 1.0, higher value => higher priority.
   *   "templateParams": {},
   *   "titleId": "",
   *   "identityId": "123456789abcdefghi" //the identity id of the user who created this activity
   * }
   * @return a response object
   * @LevelAPI Platform
   * @anchor ActivityResource.createNewActivity
   */
  @POST
  @Path("activity.{format}")
  @Produces(MediaType.APPLICATION_JSON)
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
   * Deletes an existing activity by its Id using the DELETE method. The deleted activity information will be returned in the JSON format.
   * @param uriInfo the uri request uri
   * @param portalContainerName The associated portal container name.
   * @param activityId The specified activity Id.
   * @param format The expected returned format.
   * @authentication
   * @request
   * DELETE: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/1a2b3c4d5e6f7g8h9i.json
   * @response
   * {
   *   "id": "1a2b3c4d5e6f7g8h9j",
   *   "title": "Hello World!!!",
   *   "appId": "",
   *   "type": "exosocial:core",
   *   "postedTime": 123456789, //timestamp
   *   "createdAt": "Fri Jun 17 06:42:26 +0000 2011", //The Date follows ISO 8601
   *   "priority": 0.5, //between 0.0 and 1.0, higher value => higher priority.
   *   "templateParams": {},
   *   "titleId": "",
   *   "identityId": "123456789abcdefghi", //the identity id of the user who created this activity
   *   "liked": true, //is liked (favorites) by this authenticated identity
   *   "likedByIdentities": ["identityId1", "identityId2"],
   *   "posterIdentity": {}, //optional
   *   "comments": [{}, {}, {}], //optional
   *   "totalNumberOfComments": 1234, //if comments is required, the total number of comments
   *   "activityStream": {
   *     "type": "user", // or "space"
   *     "prettyId": "root", // or space_abcde
   *     "faviconURL": "http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *     "title": "Activity Stream of Root Root",
   *     "permaLink": "http://localhost:8080/profile/root"
   *   } //optional
   * }
   *
   * @return a response object
   * @LevelAPI Platform
   * @anchor ActivityResource.deleteExistingActivityById
   */
  @DELETE
  @Path("activity/{activityId}.{format}")
  @Produces(MediaType.APPLICATION_JSON)
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
      if (existingActivity == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } catch (UndeclaredThrowableException undeclaredThrowableException){
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
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
   * Deletes an existing activity by its Id using the POST method. The deleted activity information will be returned in
   * the JSON format. It is recommended to use the DELETE method, except the case that clients cannot make request via
   * this method.
   *
   * @param uriInfo The uri request uri
   * @param portalContainerName The associated portal container name.
   * @param activityId The specified activity Id.
   * @param format The expected returned format.
   * @authentication
   * @request
   * POST: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/destroy/1a2b3c4d5e6f7g8h9i.json
   * @response
   * {
   *   "id": "1a2b3c4d5e6f7g8h9j",
   *   "title": "Hello World!!!",
   *   "appId": "",
   *   "type": "exosocial:core",
   *   "postedTime": 123456789, //timestamp
   *   "createdAt": "Fri Jun 17 06:42:26 +0000 2011", //The Date follows ISO 8601
   *   "priority": 0.5, //between 0.0 and 1.0, higher value => higher priority.
   *   "templateParams": {},
   *   "titleId": "",
   *   "identityId": "123456789abcdefghi", //the identity id of the user who created this activity
   *   "liked": true, //is liked (favorites) by this authenticated identity
   *   "likedByIdentities": ["identityId1", "identityId2"],
   *   "posterIdentity": {}, //optional
   *   "comments": [{}, {}, {}], //optional
   *   "totalNumberOfComments": 1234, //if comments is required, the total number of comments
   *   "activityStream": {
   *     "type": "user", // or "space"
   *     "prettyId": "root", // or space_abcde
   *     "faviconURL": "http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *     "title": "Activity Stream of Root Root",
   *     "permaLink": "http://localhost:8080/profile/root"
   *   } //optional
   * }
   * @return a response object
   * @LevelAPI Platform
   * @anchor ActivityResource.postToDeleteActivityById
   */
  @POST
  @Path("activity/destroy/{activityId}.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response postToDeleteActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format) {
    return deleteExistingActivityById(uriInfo, portalContainerName, activityId, format);
  }

  /**
   * Gets the comments on an activity.
   *
   * @param uriInfo The uri request uri
   * @param portalContainerName The associated portal container name.
   * @param activityId The specified activity Id.
   * @param format The expected returned format.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/comments.json
   * @response
   * {
   *   total: 10,
   *   comments: [
   *   {
   *     "id": "123456"
   *     "identityId": "12345abcde",
   *     "text": "Comment there!",
   *     "postedTime": 123456789,
   *     "createdAt": "Fri Jun 17 06:42:26 +0000 2011"
   *   },
   *   {
   *     "id" : "234567"
   *     "identityId": "12345abcde",
   *     "text": "Comment there 2!",
   *     "postedTime": 123456789,
   *     "createdAt": "Fri Jun 17 06:42:26 +0000 2011"
   *   }
   *   ]
   * }
   * @return a response object
   * @LevelAPI Platform
   * @anchor ActivityResource.getCommentsByActivityById
   */
  @GET
  @Path("activity/{activityId}/comments.{format}")
  @Produces(MediaType.APPLICATION_JSON)
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
      if (activity == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      
      //
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
      
    } catch (WebApplicationException wex){
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } catch (Exception e){
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
  
  /**
   * Posts a new comment on an existing activity. The poster of this comment is an authenticated identity.
   *
   * @param uriInfo The uri request uri
   * @param portalContainerName The associated portal container name.
   * @param activityId The specified activity Id.
   * @param format The expected returned format.
   * @authentication
   * @request
   * POST: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/comment.json
   * BODY: {"text": "My comment here!!!"}
   * @response
   * {
   *   "id": "123456"
   *   "identityId": "12345abcde",
   *   "text": "My comment here!!!",
   *   "postedTime": 123456789,
   *   "createdAt": "Fri Jun 17 06:42:26 +0000 2011"
   * }
   * @return a response object
   * @LevelAPI Platform
   * @anchor ActivityResource.createCommentActivityById
   */
  @POST
  @Path("activity/{activityId}/comment.{format}")
  @Produces(MediaType.APPLICATION_JSON)
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
      if (activity == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } catch (UndeclaredThrowableException undeclaredThrowableException){
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
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
   * Deletes an existing comment by its Id.
   *
   * @param uriInfo The uri request uri
   * @param portalContainerName The associated portal container name.
   * @param activityId The specified activity Id.
   * @param format The expected returned format.
   * @param commentId The specified comment Id.
   * @authentication
   * @request
   * DELETE: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/comment/123456.json
   * @response
   * {
   *   "id": "123456"
   *   "identityId": "12345abcde",
   *   "text": "My comment here!!!",
   *   "postedTime": 123456789,
   *   "createdAt": "Fri Jun 17 06:42:26 +0000 2011"
   * }
   * @return a response object
   * @LevelAPI Platform
   * @anchor ActivityResource.deleteCommentById
   */
  @DELETE
  @Path("activity/{activityId}/comment/{commentId}.{format}")
  @Produces(MediaType.APPLICATION_JSON)
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
      if (commentActivity == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } catch (UndeclaredThrowableException undeclaredThrowableException){
      if(undeclaredThrowableException.getCause() instanceof ActivityStorageException){
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    
    if(!commentActivity.isComment()){
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    //remove if(!SecurityManager.canDeleteActivity(portalContainer, authenticatedIdentity, commentActivity))
    // because it's comment then don't care Stream owner of comment.
    if(!SecurityManager.canDeleteComment(portalContainer, authenticatedIdentity, commentActivity)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    activityManager.deleteComment(activity, commentActivity);
    HashMap<String, Object> resultHashmap = new HashMap<String, Object>();
    resultHashmap.put("id", commentActivity.getId());
    return Util.getResponse(resultHashmap, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Deletes an existing comment by its Id using the POST method. The deleted activity information will be returned in the JSON format. It is recommended to use the POST method, except the case that clients cannot make request via this method.
   *
   * @param uriInfo The uri request uri
   * @param portalContainerName The associated portal container name.
   * @param activityId The specified activity Id.
   * @param commentId The specified comment Id.
   * @param format The expected returned format.
   * @authentication
   * @request
   * POST: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/comment/destroy/123456.json
   * @response
   * {
   *   "id": "123456"
   *   "identityId": "12345abcde",
   *   "text": "My comment here!!!",
   *   "postedTime": 123456789,
   *   "createdAt": "Fri Jun 17 06:42:26 +0000 2011"
   * }
   * @return a response object
   * @LevelAPI Platform
   * @anchor ActivityResource.postDeleteCommentById
   */
  @POST
  @Path("activity/{activityId}/comment/destroy/{commentId}.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response postDeleteCommentById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format,
                                           @PathParam("commentId") String commentId) {
    return deleteCommentById(uriInfo, portalContainerName, activityId, format, commentId);
  }
  
  
  /**
   * Gets all the identities who like an existing activity.
   * 
   * @param uriInfo The uri request uri
   * @param portalContainerName The associated portal container name.
   * @param activityId The specified activity Id.
   * @param format The expected returned format.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/likes.json
   * @response
   * {
   *   totalNumberOfLikes: 2,
   *   likesByIdentities: [
   *     {
   *       "id":1234567,
   *       "providerId":"organization",
   *       "remoteId":"demo",
   *       "profile": {
   *         "fullName":"Demo GTN",
   *         "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *       }
   *     },
   *     {
   *       "id":23456,
   *       "providerId":"organization",
   *       "remoteId":"root",
   *       "profile": {
   *         "fullName":"Root GTN",
   *         "avatarUrl":"http://localhost:8080/profile/u/root/avatar.jpg?u=12345"
   *       }
   *     },
   *   ]
   * }
   * @return
   * @LevelAPI Platform
   * @anchor ActivityResource.getLikesFromIdentityId
   */
  @GET
  @Path("activity/{activityId}/likes.{format}")
  @Produces(MediaType.APPLICATION_JSON)
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
   * Allows an authenticated identity to do the "like" action on an existing activity.
   *
   * @param uriInfo The uri request uri
   * @param portalContainerName The associated portal container name.
   * @param activityId The specified activity Id.
   * @param format The expected returned format.

   * @authentication
   * @request
   * POST: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/like.json
   * @response
   * {
   *   "liked": true
   * }
   * @return a response object
   * @anchor ActivityResource.createLikeActivityById
   * @LevelAPI Platform
   */
  @POST
  @Path("activity/{activityId}/like.{format}")
  @Produces(MediaType.APPLICATION_JSON)
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
   * Allows an identity to remove his "like" action on an activity.
   *
   * @param uriInfo The uri request uri
   * @param portalContainerName The associated portal container name.
   * @param activityId The specified activity Id.
   * @param format The expected returned format.
   * @authentication
   * @request
   * DELETE: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/like.json
   * @response
   * {
   *   "liked": false
   * }
   * @return a response object
   * @LevelAPI Platform
   * @anchor ActivityResource.deleteLikeActivityById
   */
  @DELETE
  @Path("activity/{activityId}/like.{format}")
  @Produces(MediaType.APPLICATION_JSON)
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
      if (activity == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } catch (UndeclaredThrowableException undeclaredThrowableException){
      if(undeclaredThrowableException.getCause() instanceof ActivityStorageException) {
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
   * Allows an identity to remove his "like" action on an activity. It is recommended to use the DELETE method, except the case that clients cannot make request via this method.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName The associated portal container name.
   * @param activityId The specified activity Id.
   * @param format The expected returned format.
   * @authentication
   * @request
   * POST: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/like/destroy.json
   * @response
   * {
   *   "liked": false
   * }
   * @return a response object
   * @LevelAPI Platform
   * @anchor ActivityResource.postDeleteLikeActivityById
   */
  @POST
  @Path("activity/{activityId}/like/destroy.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response postDeleteLikeActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format) {
    return deleteLikeActivityById(uriInfo, portalContainerName, activityId, format);
  }
  
  private boolean isPassed(String value) {
    return value != null && ("true".equals(value) || "t".equals(value) || "1".equals(value));
  }
}
