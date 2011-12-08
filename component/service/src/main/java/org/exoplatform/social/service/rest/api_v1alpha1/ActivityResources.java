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
package org.exoplatform.social.service.rest.api_v1alpha1;

import java.util.HashMap;

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
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.StorageException;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.SecurityManager;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api_v1alpha1.models.Activity;
import org.exoplatform.social.service.rest.api_v1alpha1.models.ActivityStream;
import org.exoplatform.social.service.rest.api_v1alpha1.models.Comment;


/**
 * Activity Resources end point.
 * @author <a href="http://phuonglm.net">PhuongLM</a>
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 15, 2011
 * @title Activity
 * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.Activity
 */
@Path("api/social/" + VersionResources.V1_ALPHA1+ "/{portalContainerName}/")
public class ActivityResources implements ResourceContainer {

  private static final String[] SUPPORTED_FORMAT = new String[]{"json"};
  private ActivityManager activityManager;
  private IdentityManager identityManager;

  /**
   * Gets an activity object from a specified activity Id.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity Id
   * @param format the expected returned format
   * @param showPosterIdentity When this parameter is set to true, t or 1, the returned activity will provide more
   * information for the user who posted this activity.
   * @param numberOfComments Specifies the number of comments to be displayed along with this activity. By default,
   * number_of_comments=0. If number_of_comments is a positive number, this number is considered as a limit number that
   * must be equal or less than 100. If the actual number of comments is less than the provided positive number, the
   * number of actual comments must be returned. If the total number of comments is more than 100, it is recommended to
   * use activity/:id/comments.format instead.
   * @param showActivityStream When this parameter is set to true, t or 1, the returned activity will provide more
   * information for the activity stream that this activity belongs to.
   * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.Activity.Get
   * @authentication
   * @request GET: http://platform35.demo.exoplatform.org/rest/private/api/social/v1-alpha1/portal/activity/1a2b3c4d5e6f7g8h9i.json
   * @response {
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
   *   "type": "user", // or "space"
   *   "prettyId": "root", // or space_abcde
   *   "faviconURL": "http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *   "title": "Activity Stream of Root Root",
   *   "permaLink": "http://platform35.demo.exoplatform.org/profile/root"
   * } //optional
   * }
   * @return a response object
   *
   */
  @GET
  @Path("activity/{activityId}.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getActivityById(@Context UriInfo uriInfo,
                                  @PathParam("portalContainerName") String portalContainerName,
                                  @PathParam("activityId") String activityId,
                                  @PathParam("format") String format,
                                  @QueryParam("poster_identity") String showPosterIdentity,
                                  @QueryParam("number_of_comments") String numberOfComments,
                                  @QueryParam("activity_stream") String showActivityStream) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if(activityId !=null && !activityId.equals("")){
      PortalContainer portalContainer = getPortalContainer(portalContainerName);
      activityManager = Util.getActivityManager();
      identityManager = Util.getIdentityManager();
      
      if(isAuthenticated()){
        try {
          //
          ActivityManager manager = Util.getActivityManager();
          ExoSocialActivity activity = manager.getActivity(activityId);
          if(!activity.isComment()){
            if(SecurityManager.canAccessActivity(portalContainer,ConversationState.getCurrent().getIdentity().getUserId(),activity)){
              //
              Activity model = new Activity(activity);
        
              //
              if (isPassed(showPosterIdentity)) {
                model.setPosterIdentity(new org.exoplatform.social.service.rest.api_v1alpha1.models.Identity(
                    identityManager.getIdentity(activity.getUserId(), false)));
              }
        
              //
              if (isPassed(showActivityStream)) {
                model.setActivityStream(new ActivityStream(activity.getActivityStream()));
              }
        
              //
              if (numberOfComments != null) {
        
                int commentNumber = activity.getReplyToId().length;
                int number = Integer.parseInt(numberOfComments);
        
                if (number > 100) {
                  number = 100;
                }
                
                if (number > commentNumber) {
                  number = commentNumber;
                }
                ListAccess<ExoSocialActivity> comments =  activityManager.getCommentsWithListAccess(activity);
                ExoSocialActivity[] commentsLimited =  comments.load(0, number);
                Comment[] commentWrapers = new Comment[number];
                for(int i = 0; i < number; i++){
                  commentWrapers[i] = new Comment(commentsLimited[i]);      
                }
                model.setComments(commentWrapers);
                
                model.setComments(commentWrapers);
              }
              model.setTotalNumberOfComments(activity.getReplyToId().length);
              
              if(isLikedByIdentity(authenticatedUserIdentity().getId(),activity)){
                model.setLiked(true);
              } else {
                model.setLiked(false);
              }
              
              return Util.getResponse(model, uriInfo, mediaType, Response.Status.OK);
            } else {
              throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }
          } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
          }
        }
        catch (Exception e) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      } else {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
  }


  /**
   * Creates an activity to an identity's activity stream. If no identity_id is specified, the activity will be created
   * to the authenticated identity's activity stream.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param format the expected returned format
   * @param identityIdStream the optional identity stream to post this new activity to
   * @param newActivity a new activity instance
   * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.Activity.Post
   * @authentication
   * @request POST: http://platform35.demo.exoplatform.org/rest/private/api/social/v1-alpha1/portal/activity.json
   * BODY: {"title": "Hello World!!!"}
   * @response {
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
   */
  @POST
  @Path("activity.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response createNewActivity(@Context UriInfo uriInfo,
                                    @PathParam("portalContainerName") String portalContainerName,
                                    @PathParam("format") String format,
                                    @QueryParam("identity_id") String identityIdStream,
                                    Activity newActivity) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);
    
    if(newActivity !=null && newActivity.getTitle()!=null && !newActivity.getTitle().equals("")){
        try {
          ActivityManager activityManager = Util.getActivityManager();
          IdentityManager identityManager =  Util.getIdentityManager();
          PortalContainer portalContainer = getPortalContainer(portalContainerName);
          

          Identity authenticatedIdentity = authenticatedUserIdentity();
          Identity postToIdentity;

          if(identityIdStream != null && !identityIdStream.equals("")){
            postToIdentity = identityManager.getIdentity(identityIdStream, false);
            if(postToIdentity == null){
              throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
          } else {
            postToIdentity = authenticatedIdentity;
          }
          
          if(authenticatedIdentity != null && SecurityManager.canPostActivity(portalContainer, authenticatedIdentity, postToIdentity)){
            ExoSocialActivity activity = new ExoSocialActivityImpl();          

            activity.setId(newActivity.getId());
            activity.setTitle(newActivity.getTitle());
            activity.setType(newActivity.getType());
            activity.setPriority(newActivity.getPriority());
            activity.setTitleId(newActivity.getTitleId());
            activity.setTemplateParams(newActivity.getTemplateParams());
            
            newActivity.setIdentityId(authenticatedIdentity.getId());
            activityManager.saveActivityNoReturn(postToIdentity, activity);
            
            //
            Activity model = new Activity(activity);
            model.setIdentityId(postToIdentity.getId());
      
            return Util.getResponse(model, uriInfo, mediaType, Response.Status.OK);
          } else {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
          }
        }
        catch (StorageException e) {
          throw new WebApplicationException( Response.Status.INTERNAL_SERVER_ERROR);
        }
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  /**
   * Deletes an existing activity by its Id using the DELETE method. The deleted activity information will be returned
   * in the JSON format.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.Activity.Delete
   * @authentication
   * @request DELETE: http://platform35.demo.exoplatform.org/rest/private/api/social/v1-alpha1/portal/activity/1a2b3c4d5e6f7g8h9i.json
   * @response {
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
   *     "permaLink": "http://platform35.demo.exoplatform.org/profile/root"
   *   } //optional
   * }
   * @return a response object
   */
  @DELETE
  @Path("activity/{activityId}.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteExistingActivityById(@Context UriInfo uriInfo,
                                            @PathParam("portalContainerName") String portalContainerName,
                                            @PathParam("activityId") String activityId,
                                            @PathParam("format") String format) {
    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT); 
    if(activityId!=null && portalContainerName!=null ){      
      activityManager = Util.getActivityManager();
      identityManager = Util.getIdentityManager();
      PortalContainer portalContainer = getPortalContainer(portalContainerName);
      ExoSocialActivity existingActivity = activityManager.getActivity(activityId);
      Identity authenticatedIdentity = authenticatedUserIdentity();
      
      if(authenticatedIdentity != null){
        if(SecurityManager.canDeleteActivity(portalContainer, authenticatedIdentity, existingActivity)){
          activityManager.deleteActivity(existingActivity);
          Activity model = new Activity(existingActivity);
          return Util.getResponse(model, uriInfo, mediaType, Response.Status.OK);
        } else {
          throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
      } else {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  /**
   * Deletes an existing activity by its Id using the POST method. The deleted activity information will be returned in
   * the JSON format. It is recommended to use the DELETE method, except the case that clients cannot make request via
   * this method.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.Activity.PostDelete
   * @authentication
   * @request POST: http://platform35.demo.exoplatform.org/rest/private/api/social/v1-alpha1/portal/activity/destroy/1a2b3c4d5e6f7g8h9i.json
   * @response {
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
   *     "permaLink": "http://platform35.demo.exoplatform.org/profile/root"
   *   } //optional
   * }
   * @return a response object
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
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.Activitycomments.Get
   * @authentication
   * @request GET: http://platform35.demo.exoplatform.org/rest/private/api/social/v1-alpha1/portal/activity/1a2b3c4d5e/comments.json
   * @response {
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
   */
  @GET
  @Path("activity/{activityId}/comments.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCommentsByActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format) {
    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if(activityId !=null && !activityId.equals("")){
      PortalContainer portalContainer = getPortalContainer(portalContainerName);
      activityManager = Util.getActivityManager();
      identityManager = Util.getIdentityManager();
      Identity authenticatedUserIdentity = authenticatedUserIdentity();
      ExoSocialActivity activity = activityManager.getActivity(activityId);
      if(authenticatedUserIdentity != null){
        if(SecurityManager.canAccessActivity(portalContainer, authenticatedUserIdentity, activity)){
          int total;
          Comment[] commentWrapers = null;
          try {
            ListAccess<ExoSocialActivity> comments =  activityManager.getCommentsWithListAccess(activity);
            total = comments.getSize();
            ExoSocialActivity[] commentsLimited =  comments.load(0, total);
            commentWrapers = new Comment[total];
            for(int i = 0; i < total; i++){
              commentWrapers[i] = new Comment(commentsLimited[i]);      
            }
          } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
          }
          
          HashMap resultJson = new HashMap();
          resultJson.put("total", commentWrapers.length);
          resultJson.put("comments", commentWrapers);
          return Util.getResponse(resultJson, uriInfo, mediaType, Response.Status.OK);
        } else {
          throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
      } else {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }
  
  /**
   * Posts a new comment on an existing activity. The poster of this comment is an authenticated identity.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.Activitycomments.Post
   * @authentication
   * @request POST: http://platform35.demo.exoplatform.org/rest/private/api/social/v1-alpha1/portal/activity/1a2b3c4d5e/comment.json
   * BODY: {"text": "My comment here!!!"}
   * @response {
   *   "id": "123456"
   *   "identityId": "12345abcde",
   *   "text": "My comment here!!!",
   *   "postedTime": 123456789,
   *   "createdAt": "Fri Jun 17 06:42:26 +0000 2011"
   * }
   * @return a response object
   */
  @POST
  @Path("activity/{activityId}/comment.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response createCommentActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format,
                                           Comment comment) {
    
    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if(comment !=null && comment.getText() != null && !comment.getText().equals("")){
      PortalContainer portalContainer = getPortalContainer(portalContainerName);
      activityManager = Util.getActivityManager();
      identityManager = Util.getIdentityManager();
      Identity authenticatedUserIdentity = authenticatedUserIdentity();
      ExoSocialActivity activity = activityManager.getActivity(activityId);
      if(authenticatedUserIdentity != null){
        if(SecurityManager.canCommentToActivity(portalContainer, authenticatedUserIdentity, activity)){
          
          ExoSocialActivity commentActivity = new ExoSocialActivityImpl();
          commentActivity.setTitle(comment.getText());
          commentActivity.setUserId(authenticatedUserIdentity.getId());
          
          activityManager.saveComment(activity,commentActivity);
          
          comment = new Comment(commentActivity);
          
          return Util.getResponse(comment, uriInfo, mediaType, Response.Status.OK);
        } else {
          throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
      } else {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }
  
  /**
   * Deletes an existing comment by its Id.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.Activitycomments.Delete
   * @authenticated
   * @request DELETE: http://platform35.demo.exoplatform.org/rest/private/api/social/v1-alpha1/portal/activity/1a2b3c4d5e/comment/123456.json
   * @response {
   *   "id": "123456"
   *   "identityId": "12345abcde",
   *   "text": "My comment here!!!",
   *   "postedTime": 123456789,
   *   "createdAt": "Fri Jun 17 06:42:26 +0000 2011"
   * }
   * @return a response object
   */
  @DELETE
  @Path("activity/{activityId}/comment/{commentId}.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteCommentById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format,
                                           @PathParam("commentId") String commentId) {
    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if(commentId !=null && !commentId.equals("")){
      PortalContainer portalContainer = getPortalContainer(portalContainerName);
      activityManager = Util.getActivityManager();
      identityManager = Util.getIdentityManager();
      Identity authenticatedUserIdentity = authenticatedUserIdentity();
      ExoSocialActivity commentActivity = activityManager.getActivity(commentId);
      ExoSocialActivity activity = activityManager.getActivity(activityId);
      if(commentActivity.isComment()){
        if(authenticatedUserIdentity != null){
          if(SecurityManager.canDeleteActivity(portalContainer, authenticatedUserIdentity, commentActivity)){
            activityManager.deleteComment(activity, commentActivity);
            Comment resultComment = new Comment(commentActivity);
            return Util.getResponse(resultComment, uriInfo, mediaType, Response.Status.OK);
          } else {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
          }
        } else {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
      } else {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }
  
  /**
   * Deletes an existing comment by its Id using the POST method. The deleted activity information will be returned in the JSON format. It is recommended to use the POST method, except the case that clients cannot make request via this method.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.Activitycomments.PostDelete
   * @authenticated
   * @request POST: http://platform35.demo.exoplatform.org/rest/private/api/social/v1-alpha1/portal/activity/1a2b3c4d5e/comment/destroy/123456.json
   * @response {
   *   "id": "123456"
   *   "identityId": "12345abcde",
   *   "text": "My comment here!!!",
   *   "postedTime": 123456789,
   *   "createdAt": "Fri Jun 17 06:42:26 +0000 2011"
   * }
   * @return a response object
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
   * Allows an authenticated identity to do the "like" action on an existing activity.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.ActivityLike.Post
   * @authentication
   * @request POST: http://platform35.demo.exoplatform.org/rest/private/api/social/v1-alpha1/portal/activity/1a2b3c4d5e/like.json
   * @response {
   *   "liked": true
   * }
   * @return a response object
   */
  @POST
  @Path("activity/{activityId}/like.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response createLikeActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format) {
    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if(activityId !=null && !activityId.equals("")){
      PortalContainer portalContainer = getPortalContainer(portalContainerName);
      activityManager = Util.getActivityManager();
      identityManager = Util.getIdentityManager();
      Identity authenticatedUserIdentity = authenticatedUserIdentity();
      ExoSocialActivity activity = activityManager.getActivity(activityId);
      if(authenticatedUserIdentity != null){
        if(SecurityManager.canCommentToActivity(portalContainer, authenticatedUserIdentity, activity)){        
          
          activityManager.saveLike(activity, authenticatedUserIdentity());
          
          HashMap resultJson = new HashMap();
          resultJson.put("like", true);
          
          return Util.getResponse(resultJson, uriInfo, mediaType, Response.Status.OK);
        } else {
          throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
      } else {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }

  /**
   * Allows an identity to remove his "like" action on an activity.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.ActivityLike.Delete
   * @authentication
   * @request DELETE: http://platform35.demo.exoplatform.org/rest/private/api/social/v1-alpha1/portal/activity/1a2b3c4d5e/like.json
   * @response {
   *   "liked": false
   * }
   * @return a response object
   */
  @DELETE
  @Path("activity/{activityId}/like.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteLikeActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format) {
    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if(activityId !=null && !activityId.equals("")){
      PortalContainer portalContainer = getPortalContainer(portalContainerName);
      activityManager = Util.getActivityManager();
      identityManager = Util.getIdentityManager();
      Identity authenticatedUserIdentity = authenticatedUserIdentity();
      ExoSocialActivity activity = activityManager.getActivity(activityId);
      if(authenticatedUserIdentity != null){
        if(SecurityManager.canCommentToActivity(portalContainer, authenticatedUserIdentity, activity)){        
          
          activityManager.deleteLike(activity, authenticatedUserIdentity());
          
          HashMap resultJson = new HashMap();
          resultJson.put("like", false);
          
          return Util.getResponse(resultJson, uriInfo, mediaType, Response.Status.OK);
        } else {
          throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
      } else {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }
  
  /**
   * Allows an identity to remove his "like" action on an activity. It is recommended to use the DELETE method, except the case that clients cannot make request via this method.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @anchor SOCref.DevelopersReferences.RestService_APIs_v1alpha1.ActivityResource.ActivityLike.Postdestroy
   * @authentication
   * @request POST: http://platform35.demo.exoplatform.org/rest/private/api/social/v1-alpha1/portal/activity/1a2b3c4d5e/like/destroy.json
   * @response {
   *   "liked": false
   * }
   * @return a response object
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
  
  private boolean isAuthenticated() {
    return ConversationState.getCurrent()!=null && ConversationState.getCurrent().getIdentity() != null &&
              ConversationState.getCurrent().getIdentity().getUserId() != null;
  }
  
  private Identity authenticatedUserIdentity() {
    if(ConversationState.getCurrent()!=null && ConversationState.getCurrent().getIdentity() != null &&
              ConversationState.getCurrent().getIdentity().getUserId() != null){
      IdentityManager identityManager =  Util.getIdentityManager();
      String authenticatedUserRemoteID = ConversationState.getCurrent().getIdentity().getUserId(); 
      return identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUserRemoteID, false);
    } else {
      return null;
    }
  }
  
  private boolean isPassed(String value) {
    return value != null && ("true".equals(value) || "t".equals(value) || "1".equals(value));
  }

  private PortalContainer getPortalContainer(String name) {
    return (PortalContainer) ExoContainerContext.getContainerByName(name);
  }
}
