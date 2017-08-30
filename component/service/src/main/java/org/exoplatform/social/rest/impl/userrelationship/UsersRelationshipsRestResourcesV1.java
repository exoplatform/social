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
package org.exoplatform.social.rest.impl.userrelationship;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.api.UsersRelationshipsRestResources;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.RelationshipEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

@Path(VersionResources.VERSION_ONE + "/social/usersRelationships")
@Api(tags = VersionResources.VERSION_ONE + "/social/usersRelationships", value = VersionResources.VERSION_ONE + "/social/usersRelationships", description = "Managing relationships of users")
public class UsersRelationshipsRestResourcesV1 implements UsersRelationshipsRestResources {

  public UsersRelationshipsRestResourcesV1() {
  }
  
  @RolesAllowed("users")
  @GET
  @ApiOperation(value = "Gets all user relationships",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns a list of relationships in the following cases: <br/><ul>" +
                        "<li>if the query param \"user\" is not defined: returns the relationships of the authenticated user</li>" +
                        "<li>if the \"user\" is defined and the authenticated user is not an administrator: returns the relationships of the authenticated user</li>" +
                        "<li>if the \"user\" is defined and the authenticated user is an administrator: returns the relationships of the defined user</li>" +
                        "<li>if the \"others\" is defined: returns the relationships between the user and the users defined in \"others\" only</li></ul>")
  @ApiResponses(value = {
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getUsersRelationships(@Context UriInfo uriInfo,
                                        @ApiParam(value = "Specific status of relationships: pending, confirmed or all", defaultValue = "all") @QueryParam("status") String status,
                                        @ApiParam(value = "User name to get relationships") @QueryParam("user") String user,
                                        @ApiParam(value = "Usernames of the others users to get relationships with the given user") @QueryParam("others") String others,
                                        @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                        @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                        @ApiParam(value = "Returning the number of relationships or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                        @ApiParam(value = "Asking for a full representation of a specific subresource, ex: sender or receiver", required = false) @QueryParam("expand") String expand) throws Exception {
    
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    final Relationship.Type type = StringUtils.isNotEmpty(status) && Arrays.asList(Relationship.Type.values()).contains(status.toUpperCase()) ? Relationship.Type.valueOf(status.toUpperCase()) : Relationship.Type.ALL;

    List<Relationship> relationships;

    String username = user;
    if (username == null || !RestUtils.isMemberOfAdminGroup()) {
      username = ConversationState.getCurrent().getIdentity().getUserId();
    }
    Identity givenUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);

    if(StringUtils.isNotEmpty(others)) {
      String[] othersUsernames = others.split(",");
      relationships = Arrays.stream(othersUsernames)
              .map(other -> identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, other, true))
              .map(otherIdentity -> relationshipManager.get(givenUser, otherIdentity))
              .filter(Objects::nonNull)
              .filter(relationship -> type.equals(Relationship.Type.ALL) || type.equals(relationship.getStatus()))
              .collect(Collectors.toList());
    } else {
      relationships = relationshipManager.getRelationshipsByStatus(givenUser, type, offset, limit);
    }
    int size = returnSize ? relationshipManager.getRelationshipsCountByStatus(givenUser, type) : -1;

    List<DataEntity> relationshipEntities = EntityBuilder.buildRelationshipEntities(relationships, uriInfo);
    CollectionEntity collectionRelationship = new CollectionEntity(relationshipEntities, EntityBuilder.USERS_RELATIONSHIP_TYPE, offset, limit);
    if (returnSize) {
      collectionRelationship.setSize(size);
    }    
    return EntityBuilder.getResponse(collectionRelationship, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @RolesAllowed("users")
  @ApiOperation(value = "Creates a relationship between two specific users",
                httpMethod = "POST",
                response = Response.class,
                notes = "This creates the relationship in the following cases: <br/><ul><li>the sender or the receiver of the user relationship is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response createUsersRelationships(@Context UriInfo uriInfo,
                                           @ApiParam(value = "Asking for a full representation of a specific subresource, ex: sender or receiver", required = false) @QueryParam("expand") String expand,
                                           @ApiParam(value = "Relationship object to be created, required fields: <br/>sender - user name of the sender,<br/>receiver - user name of the receiver,<br/>status - pending or confirmed", required = true) RelationshipEntity model) throws Exception {
    if (model == null || model.getReceiver() == null || model.getSender() == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (! RestUtils.isMemberOfAdminGroup() && !model.getReceiver().equals(authenticatedUser) && !model.getSender().equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity sender = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, model.getSender(), true);
    Identity receiver = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, model.getReceiver(), true);
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    Relationship.Type status = null;
    if (model.getStatus() != null) {
      try {
        status = Relationship.Type.valueOf(model.getStatus().toUpperCase());
      } catch (Exception e) {
        throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
      }
    }
    
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    if (relationshipManager.get(sender, receiver) != null && !Relationship.Type.CONFIRMED.equals(status)) {
      throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
    }
    Relationship relationship = createRelationshipByStatus(sender, receiver, status);
    
    return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @RolesAllowed("users")
  @Path("{id}")
  @ApiOperation(value = "Gets a specific relationship of user by id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns the relationship in the following cases: <br/><ul><li>the sender or the receiver of the user relationship is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getUsersRelationshipsById(@Context UriInfo uriInfo,
                                            @ApiParam(value = "Relationship id", required = true) @PathParam("id") String id,
                                            @ApiParam(value = "Asking for a full representation of a specific subresource, ex: sender or receiver", required = false) @QueryParam("expand") String expand) throws Exception {
    
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null || ! hasPermissionOnRelationship(authenticatedUser, relationship)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @RolesAllowed("users")
  @Path("{id}")
  @ApiOperation(value = "Updates a specific relationship of user by id",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This updates the relationship in the following cases: <br/><ul><li>the sender or the receiver of the user relationship is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response updateUsersRelationshipsById(@Context UriInfo uriInfo,
                                               @ApiParam(value = "Relationship id", required = true) @PathParam("id") String id,
                                               @ApiParam(value = "Asking for a full representation of a specific subresource, ex: sender or receiver", required = false) @QueryParam("expand") String expand,
                                               @ApiParam(value = "Relationship object to be updated", required = true) RelationshipEntity model) throws Exception {
    if (model == null || model.getStatus() == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null || ! hasPermissionOnRelationship(authenticatedUser, relationship)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Relationship.Type status;
    try {
      status = Relationship.Type.valueOf(model.getStatus().toUpperCase());
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (Relationship.Type.CONFIRMED.equals(status)) {
      if (!RestUtils.isMemberOfAdminGroup() && !authenticatedUser.getId().equals(relationship.getReceiver().getId())) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    }
    
    //update relationship by status
    updateRelationshipByStatus(relationship, status, relationshipManager);
    
    return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @RolesAllowed("users")
  @Path("{id}")
  @ApiOperation(value = "Deletes a specific relationship of user by id",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This deletes the relationship in the following cases: <br/><ul><li>the sender or the receiver of the user relationship is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response deleteUsersRelationshipsById(@Context UriInfo uriInfo,
                                               @ApiParam(value = "Relationship id", required = true) @PathParam("id") String id,
                                               @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null || ! hasPermissionOnRelationship(authenticatedUser, relationship)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //delete the relationship
    relationshipManager.delete(relationship);
    
    return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  /**
   * Check if the viewer is an administrator or the receiver or the sender of relationship
   * 
   * @param authenticatedUser
   * @param relationship
   * @return
   */
  private boolean hasPermissionOnRelationship(Identity authenticatedUser, Relationship relationship) {
    if (RestUtils.isMemberOfAdminGroup()) return true;
    if (authenticatedUser.getId().equals(relationship.getSender().getId())
        || authenticatedUser.getId().equals(relationship.getReceiver().getId())) {
      return true;
    }
    return false;
  }
  
  private void updateRelationshipByStatus(Relationship relationship, Relationship.Type status, RelationshipManager relationshipManager) {
    switch (status) {
      case IGNORED: {//from confirm or pending to ignore
        relationshipManager.delete(relationship);
        break;
      }
      case PENDING: {//from confirm to pending but this case doesn't exist
        break;
      }
      case CONFIRMED: {//from pending to confirm
        relationship.setStatus(status);
        relationshipManager.confirm(relationship.getReceiver(), relationship.getSender());
        break;
      }
      default:
        break;
      }
  }
  
  private Relationship createRelationshipByStatus(Identity sender, Identity receiver, Relationship.Type status) {
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);

    switch (status) {
      case IGNORED: {
        relationshipManager.ignore(sender, receiver);
        break;
      }
      case PENDING: {//from confirm to pending but this case doesn't exist
        return relationshipManager.inviteToConnect(sender, receiver);
      }
      case CONFIRMED: {//from pending to confirm
        Relationship relationship = relationshipManager.get(sender, receiver);
        if (relationship == null) {
          relationshipManager.inviteToConnect(sender, receiver);
          relationshipManager.confirm(receiver, sender);
        } else {
          relationshipManager.confirm(receiver, sender);
        }
        return relationshipManager.get(sender, receiver);
      }
      default:
        break;
      }
    
    return new Relationship(sender, receiver, status);
  }
}
