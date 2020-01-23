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
package org.exoplatform.social.rest.impl.relationship;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RelationshipsRestResources;
import org.exoplatform.social.rest.api.RestProperties;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.RelationshipEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.utils.LogUtils;

@Path(VersionResources.VERSION_ONE + "/social/relationships")
@Api(tags = VersionResources.VERSION_ONE + "/social/relationships", value = VersionResources.VERSION_ONE + "/social/relationships", description = "Managing relationships of identities")
public class RelationshipsRestResourcesV1 implements RelationshipsRestResources {

  public RelationshipsRestResourcesV1() {
  }
  
  @GET
  @RolesAllowed("users")
  @ApiOperation(value = "Gets relationships of identities",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns a list of relationships in the following cases: <br/><ul><li>the authenticated user has permissions to view the 2 objects linked to the 2 identities</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input"),
    @ApiResponse (code = 412, message = "Precondition failed, check your input params")})
  public Response getRelationships(@Context UriInfo uriInfo,
                                   @ApiParam(value = "Status of the target relationship: pending, confirmed or all") @QueryParam("status") String status,
                                   @ApiParam(value = "Identity id which is a UUID such as 40487b7e7f00010104499b339f056aa4") @QueryParam("identityId") String identityId,
                                   @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                   @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                   @ApiParam(value = "Returning the number of relationships or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize) throws Exception {

    //
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship.Type type;
    try {
      type = Relationship.Type.valueOf(status.toUpperCase());
    } catch (Exception e) {
      type = Relationship.Type.ALL;
    }

    List<Relationship> relationships = new ArrayList<Relationship>();
    int size = 0;
    if (identityId != null & RestUtils.isMemberOfAdminGroup()) {
      Identity identity = CommonsUtils.getService(IdentityManager.class).getIdentity(identityId, false);
      if (identity == null) {
        throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
      }
      relationships = relationshipManager.getRelationshipsByStatus(identity, type, offset, limit);
      size = relationshipManager.getRelationshipsCountByStatus(identity, type);
    } else {
      String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
      Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class)
                                               .getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser , true);
      relationships = relationshipManager.getRelationshipsByStatus(authenticatedUser, type, offset, limit);
      size = relationshipManager.getRelationshipsCountByStatus(authenticatedUser, type);
    }
    List<DataEntity> relationshipEntities = EntityBuilder.buildRelationshipEntities(relationships, uriInfo);
    CollectionEntity collectionRelationship = new CollectionEntity(relationshipEntities, RestProperties.RELATIONSHIPS, offset, limit);
    if (returnSize) {
      collectionRelationship.setSize(size);
    }
    //
    Response.ResponseBuilder builder = EntityBuilder.getResponseBuilder(collectionRelationship, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
    CacheControl cc = new CacheControl();
    cc.setNoStore(true);
    builder.cacheControl(cc);
    
    return builder.build();
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Creates a relationship",
                httpMethod = "POST",
                response = Response.class,
                notes = "This creates the relationship in the following cases: <br/><ul><li>the authenticated user has permissions to view the 2 objects linked to the 2 identities</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response createRelationship(@Context UriInfo uriInfo,
                                     @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand,
                                     @ApiParam(value = "Relationship object to be created", required = true)  RelationshipEntity model) throws Exception {
    
    String senderRemoteId, receiverRemoteId;
    if (model == null || (senderRemoteId = model.getSender()) == null || senderRemoteId.isEmpty()
                      || (receiverRemoteId = model.getReceiver()) == null || receiverRemoteId.isEmpty()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (! RestUtils.isMemberOfAdminGroup() && ! authenticatedUser.equals(senderRemoteId) && ! authenticatedUser.equals(receiverRemoteId)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    Relationship.Type type;
    try {
      type = Relationship.Type.valueOf(model.getStatus().toUpperCase());
    } catch (Exception e) {
      type = Relationship.Type.ALL;
    }
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity receiver = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverRemoteId, true);
    Identity sender = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderRemoteId, true);

    Relationship relationship = relationshipManager.get(sender, receiver);
    if (relationship != null) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    relationship = new Relationship(sender, receiver, type);
    relationshipManager.update(relationship);

    return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, true), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a specific relationship by id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns the relationship if the authenticated user has permissions to view the objects linked to this relationship.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getRelationshipById(@Context UriInfo uriInfo,
                                      @ApiParam(value = "Relationship id", required = true) @PathParam("id") String id,
                                      @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null || ! hasPermissionOnRelationship(authenticatedUser, relationship)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, true), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Updates a specific relationship by id",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This updates the relationship if the authenticated user has permissions to view the objects linked to this relationship.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response updateRelationshipById(@Context UriInfo uriInfo,
                                         @ApiParam(value = "Relationship id", required = true) @PathParam("id") String id,
                                         @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand,
                                         @ApiParam(value = "Relationship object to be updated", required = true) RelationshipEntity model) throws Exception {
    
    if(model == null || model.getStatus() == null || model.getStatus().length() == 0) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null || ! hasPermissionOnRelationship(authenticatedUser, relationship)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Relationship.Type type;
    try {
      type = Relationship.Type.valueOf(model.getStatus().toUpperCase());
    } catch (Exception e) {
      type = null;
    }
    
    if (type != null && ! type.equals(Relationship.Type.ALL)) {
      if (type.equals(Relationship.Type.IGNORED)) {
        relationshipManager.delete(relationship);
        LogUtils.logInfo("relationships", "ignore-connection-request", "sender:" + relationship.getSender().getRemoteId() + ",receiver:" + relationship.getReceiver().getRemoteId(), this.getClass());
      } else {
        relationship.setStatus(type);
        relationshipManager.update(relationship);
        if (type.equals(Relationship.Type.CONFIRMED)) {
          LogUtils.logInfo("relationships", "confirm-connection-request", "sender:" + relationship.getSender().getRemoteId() + ",receiver:" + relationship.getReceiver().getRemoteId(), this.getClass());
        }
      }
    }
    
    return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), expand, false), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a specific relationship by id",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This deletes the relationship if the authenticated user has permissions to view the objects linked to this relationship.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response deleteRelationshipById(@Context UriInfo uriInfo,
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
}
