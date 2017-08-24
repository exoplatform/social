/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.rest.impl.comment;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.rest.api.CommentRestResources;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.VersionResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path(VersionResources.VERSION_ONE + "/social/comments")
@Api(tags = VersionResources.VERSION_ONE + "/social/comments", value = VersionResources.VERSION_ONE + "/social/comments", description = "Operations on a comment")
public class CommentRestResourcesV1 implements CommentRestResources {
  
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a specific comment by id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns the comment if the authenticated user has permissions to see the related activity.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getCommentById(@Context UriInfo uriInfo,
                                 @ApiParam(value = "Comment id", required = true) @PathParam("id") String id,
                                 @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {

    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity act = activityManager.getActivity(id);
    
    if (act == null || !act.isComment()) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    CommentEntity commentEntity = EntityBuilder.buildEntityFromComment(act, uriInfo.getPath(), expand, false);

    return EntityBuilder.getResponse(commentEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Updates a specific comment by id",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This updates the comment in the following cases: <br/><ul><li>the authenticated user is the owner of the comment</li><li>the authenticated user is the super user</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response updateCommentById(@Context UriInfo uriInfo,
                                    @ApiParam(value = "Comment id", required = true) @PathParam("id") String id,
                                    @ApiParam(value = "Asking for a full representation of a subresource if any", required = false) @QueryParam("expand") String expand,
                                    @ApiParam(value = "Comment object to be updated, in which the title of comment is required.", required = true) ActivityEntity model) throws Exception {
  
    if (model == null || model.getTitle() == null || model.getTitle().length() == 0) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity act = activityManager.getActivity(id);
    if (act == null || ! act.getPosterId().equals(currentUser.getId())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    } else if (!act.isComment()) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    //update comment's title
    act.setTitle(model.getTitle());
    activityManager.updateActivity(act);
    
    ActivityEntity activityInfo = EntityBuilder.buildEntityFromActivity(act, uriInfo.getPath(), expand);
    
    return EntityBuilder.getResponse(activityInfo.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a specific comment by id",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This deletes the comment in the following cases: <br/><ul><li>the authenticated user is the owner of the comment</li><li>the authenticated user is the super user</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response deleteCommentById(@Context UriInfo uriInfo,
                                    @ApiParam(value = "Comment id", required = true) @PathParam("id") String id,
                                    @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity act = activityManager.getActivity(id);
    if (act == null || !act.isComment() || ! act.getPosterId().equals(currentUser.getId())) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(act, uriInfo.getPath(), expand);

    activityManager.deleteActivity(act);
    
    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @GET
  @Path("{id}/likes")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets likes of a specific comment",
          httpMethod = "GET",
          response = Response.class,
          notes = "This returns a list of likes if the authenticated user has permissions to see the comment.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 500, message = "Internal server error"),
          @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getLikesOfComment(@Context UriInfo uriInfo,
                                     @ApiParam(value = "Comment id", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                     @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                     @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);

    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity comment = activityManager.getActivity(id);
    if (comment == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    if (comment == null || !comment.isComment()) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    ExoSocialActivity activity = getActivityOfComment(comment);

    if (EntityBuilder.getActivityStream(activity, currentUser) == null && !Util.hasMentioned(activity, currentUser.getRemoteId())) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    List<DataEntity> likesEntity = EntityBuilder.buildEntityFromLike(comment, uriInfo.getPath(), expand, offset, limit);
    CollectionEntity collectionLike = new CollectionEntity(likesEntity, EntityBuilder.LIKES_TYPE, offset, limit);
    //
    return EntityBuilder.getResponse(collectionLike, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @POST
  @Path("{id}/likes")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Adds a like to a specific comment",
          httpMethod = "POST",
          response = Response.class,
          notes = "This adds the like if the authenticated user has permissions to see the comment.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 500, message = "Internal server error"),
          @ApiResponse (code = 400, message = "Invalid query input") })
  public Response addLikeOnComment(@Context UriInfo uriInfo,
                          @ApiParam(value = "Comment id", required = true) @PathParam("id") String id,
                          @ApiParam(value = "Asking for a full representation of a subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);

    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity comment = activityManager.getActivity(id);
    if (comment == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    ExoSocialActivity activity = getActivityOfComment(comment);

    if (EntityBuilder.getActivityStream(activity, currentUser) == null && !Util.hasMentioned(activity, currentUser.getRemoteId())) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    List<String> likerIds = new ArrayList<String>(Arrays.asList(comment.getLikeIdentityIds()));
    if (!likerIds.contains(currentUser.getId())) {
      likerIds.add(currentUser.getId());
      String[] identityIds = new String[likerIds.size()];
      comment.setLikeIdentityIds(likerIds.toArray(identityIds));
      activityManager.updateActivity(comment);
    }
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromComment(comment, uriInfo.getPath(), expand, true), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);

  }

  @DELETE
  @Path("{id}/likes/{username}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a like of a specific user for a given comment",
          httpMethod = "DELETE",
          response = Response.class,
          notes = "This deletes the like if the authenticated user is the given user or the super user.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 500, message = "Internal server error"),
          @ApiResponse (code = 400, message = "Invalid query input") })
  public Response deleteLikeOnComment(@Context UriInfo uriInfo,
                             @ApiParam(value = "Comment id", required = true) @PathParam("id") String id,
                             @ApiParam(value = "User name", required = true) @PathParam("username") String username,
                             @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (!authenticatedUser.equals(username)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);

    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity comment = activityManager.getActivity(id);
    if (comment == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    List<String> likerIds = new ArrayList<String>(Arrays.asList(comment.getLikeIdentityIds()));
    if (likerIds.contains(currentUser.getId())) {
      likerIds.remove(currentUser.getId());
      String[] identityIds = new String[likerIds.size()];
      comment.setLikeIdentityIds(likerIds.toArray(identityIds));
      activityManager.updateActivity(comment);
    }

    return Response.ok().build();
  }

    /**
     * Get the activity related to the given comment
     * @param comment The comment entity
     * @return The activity related to the given comment
     */
  private ExoSocialActivity getActivityOfComment(ExoSocialActivity comment) {
    ExoSocialActivity activity = comment;
    while (activity != null && activity.isComment()) {
      String parentId = activity.getParentId();
      ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
      activity = activityManager.getActivity(parentId);
    }
    return activity;

  }
}