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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.rest.api.CommentRestResources;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

@Path(VersionResources.VERSION_ONE + "/social/comments")
@Api(value=VersionResources.VERSION_ONE + "/social/comments")
public class CommentRestResourcesV1 implements CommentRestResources {
  
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a comment with the given id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request comment found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to get comment.") })
  public Response getCommentById(@Context UriInfo uriInfo,
                                 @ApiParam(value = "comment id", required = true) @PathParam("id") String id,
                                 @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {

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
  @ApiOperation(value = "Updates a comment with the given id",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request comment updated successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to update comment") })
  public Response updateCommentById(@Context UriInfo uriInfo,
                                    @ApiParam(value = "comment id", required = true) @PathParam("id") String id,
                                    @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand,
                                    @ApiParam(value = "Comment object for updating. Title of comment is required.", required = true) ActivityEntity model) throws Exception {
  
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
  @ApiOperation(value = "Deletes a comment with the given id",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request comment deleted successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to delete comment.") })
  public Response deleteCommentById(@Context UriInfo uriInfo,
                                    @ApiParam(value = "comment id", required = true) @PathParam("id") String id,
                                    @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
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
}