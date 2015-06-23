/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.social.rest.impl.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.rest.api.ActivityRestResources;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(VersionResources.VERSION_ONE + "/social/activities")
@Api(value=VersionResources.VERSION_ONE + "/social/activities")
public class ActivityRestResourcesV1 implements ActivityRestResources {
  
  @GET
  @RolesAllowed("users")
  @ApiOperation(value = "Get activities of current user",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request activities found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find activities.") })
  public Response getActivitiesOfCurrentUser(@Context UriInfo uriInfo,
                                             @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                             @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                             @ApiParam(value = "Size of returned result list.", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                             @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);

    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivityFeedWithListAccess(currentUser);
    List<ExoSocialActivity> activities = listAccess.loadAsList(offset, limit);
    
    List<DataEntity> activityEntities = new ArrayList<DataEntity>();
    for (ExoSocialActivity activity : activities) {
      DataEntity as = EntityBuilder.getActivityStream(activity, currentUser);
      if (as == null) continue;
      ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand);
      activityEntity.setActivityStream(as);
      //
      activityEntities.add(activityEntity.getDataEntity()); 
    }
    CollectionEntity collectionActivity = new CollectionEntity(activityEntities, EntityBuilder.ACTIVITIES_TYPE,  offset, limit);
    if(returnSize) {
      collectionActivity.setSize(listAccess.getSize());
    }

    return EntityBuilder.getResponse(collectionActivity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Get activity by Id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request activity found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to get activity.") })
  public Response getActivityById(@Context UriInfo uriInfo,
                                  @ApiParam(value = "activity id", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    DataEntity as = EntityBuilder.getActivityStream(activity.isComment() ? activityManager.getParentActivity(activity) : activity, currentUser);
    if (as == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand);
    if (!activity.isComment()) {
      activityEntity.setActivityStream(as);
    }

    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Update activity by Id",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request activity updated successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to update activity") })
  public Response updateActivityById(@Context UriInfo uriInfo,
                                     @ApiParam(value = "activity id", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand,
                                     @ApiParam(value = "Activity object for updating", required = true) ActivityEntity model) throws Exception {
  
    if (model == null || model.getTitle() == null || model.getTitle().length() == 0) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null || ! activity.getPosterId().equals(currentUser.getId())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    //update activity's title
    activity.setTitle(model.getTitle());
    activityManager.updateActivity(activity);
    
    DataEntity as = EntityBuilder.getActivityStream(activity, currentUser);
    ActivityEntity activityInfo = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand);
    activityInfo.setActivityStream(as);
    
    return EntityBuilder.getResponse(activityInfo.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Delete acitivty y Id",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request activity deleted successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to delete activity.") })
  public Response deleteActivityById(@Context UriInfo uriInfo,
                                     @ApiParam(value = "activity id", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null || ! activity.getPosterId().equals(currentUser.getId()) || ! activity.getStreamOwner().equals(currentUser.getRemoteId())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    DataEntity as = EntityBuilder.getActivityStream(activity, currentUser);
    ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand);
    activityEntity.setActivityStream(as);
    //
    activityManager.deleteActivity(activity);
    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/comments")
  @RolesAllowed("users")
  @ApiOperation(value = "Get comments of an activity",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request comments of an activity found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find comments of an activity.") })
  public Response getCommentsOfActivity(@Context UriInfo uriInfo,
                                        @ApiParam(value = "activity id", required = true) @PathParam("id") String id,
                                        @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                        @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                        @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (EntityBuilder.getActivityStream(activity, currentUser) == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    List<DataEntity> commentsEntity = EntityBuilder.buildEntityFromComment(activity, uriInfo.getPath(), expand, offset, limit);
    CollectionEntity collectionComment = new CollectionEntity(commentsEntity, EntityBuilder.COMMENTS_TYPE, offset, limit);    
    //
    return EntityBuilder.getResponse(collectionComment, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @Path("{id}/comments")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Post comment on activity",
                httpMethod = "POST",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request to post comment successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to post comment.") })
  public Response postComment(@Context UriInfo uriInfo,
                              @ApiParam(value = "activity id", required = true) @PathParam("id") String id,
                              @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand,
                              @ApiParam(value = "Comment object to post. Ex: { \"body\" : \"post comment on activity\"}", required = true) CommentEntity model) throws Exception {
    
    if (model == null || model.getBody() == null || model.getBody().length() == 0) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (EntityBuilder.getActivityStream(activity, currentUser) == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(model.getBody());
    comment.setBody(model.getBody());
    comment.setUserId(currentUser.getId());
    activityManager.saveComment(activity, comment);
    
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromActivity(activityManager.getActivity(comment.getId()), uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/likes")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets all the likes of the activity with the given id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request likes of an activity found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find likes of an activity.") })
  public Response getLikesOfActivity(@Context UriInfo uriInfo,
                                     @ApiParam(value = "activity id", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                     @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                     @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (EntityBuilder.getActivityStream(activity, currentUser) == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    List<DataEntity> likesEntity = EntityBuilder.buildEntityFromLike(activity, uriInfo.getPath(), expand, offset, limit);
    CollectionEntity collectionLike = new CollectionEntity(likesEntity, EntityBuilder.LIKES_TYPE, offset, limit);    
    //
    return EntityBuilder.getResponse(collectionLike, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @Path("{id}/likes")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Add a like for the activity with the given id",
                httpMethod = "POST",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request to add a like successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to add like.") })
  public Response addLike(@Context UriInfo uriInfo,
                          @ApiParam(value = "activity id", required = true) @PathParam("id") String id,
                          @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (EntityBuilder.getActivityStream(activity, currentUser) == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    List<String> likerIds = Arrays.asList(activity.getLikeIdentityIds());
    if (!likerIds.contains(currentUser.getId())) {
      likerIds.add(currentUser.getId());
      String[] identityIds = new String[likerIds.size()];
      activity.setLikeIdentityIds(likerIds.toArray(identityIds));
      activityManager.updateActivity(activity);
    }
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromActivity(activityManager.getActivity(id), uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/likes/{username}")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets the like of the user with the given username for the activity with the given activity id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request likes of an activity of an user found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find likes of an activity of an user.") })
  public Response getLikesActivityOfUser(@Context UriInfo uriInfo,
                                         @ApiParam(value = "activity id", required = true) @PathParam("id") String id,
                                         @ApiParam(value = "username", required = true) @PathParam("username") String username,
                                         @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    Identity userToGetLike = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    List<String> likerIds = Arrays.asList(activity.getLikeIdentityIds());
    List<DataEntity> likesEntity = new ArrayList<DataEntity>();
    if (likerIds.contains(userToGetLike.getId())) {
      likesEntity.add(EntityBuilder.buildEntityProfile(username, uriInfo.getPath(), expand).getDataEntity());
    }

    CollectionEntity collectionLike = new CollectionEntity(likesEntity, EntityBuilder.LIKES_TYPE, 0, 1);
    
    //
    return EntityBuilder.getResponse(collectionLike, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @Path("{id}/likes/{username}")
  @RolesAllowed("users")
  @ApiOperation(value = "Delete a like from the user with the given username on the activity with the given activity id",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request like of activity of user deleted successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to delete like of activity of user.") })
  public Response deleteActivityById(@Context UriInfo uriInfo,
                                     @ApiParam(value = "activity id", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "username", required = true) @PathParam("username") String username,
                                     @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (authenticatedUser.equals(username)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);

    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    List<String> likerIds = Arrays.asList(activity.getLikeIdentityIds());
    if (likerIds.contains(currentUser.getId())) {
      likerIds.remove(currentUser.getId());
      String[] identityIds = new String[likerIds.size()];
      activity.setLikeIdentityIds(likerIds.toArray(identityIds));
      activityManager.updateActivity(activity);
    }
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromActivity(activityManager.getActivity(id), uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
}