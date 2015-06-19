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
package org.exoplatform.social.rest.impl.identity;

import java.util.ArrayList;
import java.util.List;

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

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.IdentityRestResources;
import org.exoplatform.social.rest.api.RestProperties;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.IdentityEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path(VersionResources.VERSION_ONE + "/social/identities")
@Api(value=VersionResources.VERSION_ONE + "/social/identities", description = "Operations eXo social identities.")
public class IdentityRestResourcesV1 implements IdentityRestResources {

  /**
   * {@inheritDoc}
   */
  @GET
  @RolesAllowed("users")
  @ApiOperation(value = "Get identities",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request identities found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find relationships.") })
  public Response getIdentities(@Context UriInfo uriInfo,
                                @ApiParam(value = "Provider type (space, organization...)", required = false, defaultValue="organization") @QueryParam("type") String type,
                                @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                @ApiParam(value = "Size of returned result list.", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    String providerId = (type != null && type.equals("space")) ? SpaceIdentityProvider.NAME : OrganizationIdentityProvider.NAME;
    ListAccess<Identity> listAccess = identityManager.getIdentitiesByProfileFilter(providerId, new ProfileFilter(), true);
    Identity[] identities = listAccess.load(offset, limit);
    List<DataEntity> identityEntities = new ArrayList<DataEntity>();
    for (Identity identity : identities) {
      identityEntities.add(EntityBuilder.buildEntityIdentity(identity, uriInfo.getPath(), expand).getDataEntity());
    }
    CollectionEntity collectionIdentity = new CollectionEntity(identityEntities, EntityBuilder.IDENTITIES_TYPE, offset, limit);
    if(returnSize) {
      collectionIdentity.setSize(listAccess.getSize());
    }

    return EntityBuilder.getResponse(collectionIdentity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  /**
   * {@inheritDoc}
   */
  @POST
  @RolesAllowed("users")
  @ApiOperation(value = "Create identity",
                httpMethod = "POST",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request created successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find relationships.") })
  public Response createIdentities(@Context UriInfo uriInfo,
                                   @ApiParam(value = "Remote user Id", required = true) @QueryParam("remoteId") String remoteId,
                                   @ApiParam(value = "Provider Id", required = true) @QueryParam("providerId") String providerId,
                                   @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    if ((! providerId.equals(SpaceIdentityProvider.NAME) && ! providerId.equals(OrganizationIdentityProvider.NAME))) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    //check if user already exist
    Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);
    if (identity.isDeleted()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    IdentityEntity identityInfo = EntityBuilder.buildEntityIdentity(identity, uriInfo.getPath(), expand);
    return EntityBuilder.getResponse(identityInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  

  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Get an identity by Id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request identity found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to get identity.") })
  public Response getIdentityById(@Context UriInfo uriInfo,
                                  @ApiParam(value = "identity id", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    IdentityEntity profileInfo = EntityBuilder.buildEntityIdentity(identity, uriInfo.getPath(), expand);
    return EntityBuilder.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  

  /**
   * {@inheritDoc}
   */
  @PUT
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Update an indentity by Id",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request updated successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to update identity.") })
  public Response updateIdentityById(@Context UriInfo uriInfo,
                                     @ApiParam(value = "identity id", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    //TODO : process to update identity
    
    IdentityEntity profileInfo = EntityBuilder.buildEntityIdentity(identity, uriInfo.getPath(), expand);
    
    return EntityBuilder.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  

  /**
   * {@inheritDoc}
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Delete an identity by Id",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request identity deleted successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to delete identity.") })
  public Response deleteIdentityById(@Context UriInfo uriInfo,
                                     @ApiParam(value = "identity id", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {

    if (! RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, false);
    
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    //delete identity
    identityManager.hardDeleteIdentity(identity);
    identity = identityManager.getIdentity(id, true);
    IdentityEntity profileInfo = EntityBuilder.buildEntityIdentity(identity, uriInfo.getPath(), expand);

    return EntityBuilder.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}/relationships")
  @RolesAllowed("users")
  @ApiOperation(value = "Get relationships",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request relationships found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find relationships.") })
  public Response getRelationshipsOfIdentity(@Context UriInfo uriInfo,
                                             @ApiParam(value = "identity id", required = true) @PathParam("id") String id,
                                             @ApiParam(value = "id identity to get relationship") @QueryParam("with") String with,
                                             @ApiParam(value = "Size of returned result list.", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                             @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                             @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                             @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    
    if (with != null && with.length() > 0) {
      Identity withUser = identityManager.getIdentity(with, true);
      if (withUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      //
      Relationship relationship = relationshipManager.get(identity, withUser);
      if (relationship == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      return EntityBuilder.getResponse(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(),
                                                                             expand, false),
                                                                             uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
    }

    List<Relationship> relationships = relationshipManager.getRelationshipsByStatus(identity, Relationship.Type.ALL, offset, limit);
    List<DataEntity> relationshipEntities = EntityBuilder.buildRelationshipEntities(relationships, uriInfo);
    CollectionEntity collectionRelationship = new CollectionEntity(relationshipEntities, RestProperties.RELATIONSHIPS, offset, limit);
    if (returnSize) {
      collectionRelationship.setSize(relationshipManager.getRelationshipsCountByStatus(identity, Relationship.Type.ALL));
    }
    return EntityBuilder.getResponse(collectionRelationship, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
}
