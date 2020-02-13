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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
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
import org.exoplatform.social.rest.entity.ProfileEntity;
import org.exoplatform.social.service.rest.api.VersionResources;


@Path(VersionResources.VERSION_ONE + "/social/identities")
@Api(tags = VersionResources.VERSION_ONE + "/social/identities", value = VersionResources.VERSION_ONE + "/social/identities", description = "Managing identities")
public class IdentityRestResourcesV1 implements IdentityRestResources {

  private IdentityManager identityManager;
  
  public IdentityRestResourcesV1(IdentityManager identityManager) {
    this.identityManager = identityManager;
  }
  /**
   * {@inheritDoc}
   */
  @GET
  @RolesAllowed("users")
  @ApiOperation(value = "Gets all identities",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns a list of identities in the following cases: <br/><ul><li>the authenticated user has permissions to view the object linked to these identities</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getIdentities(@Context UriInfo uriInfo,
                                @ApiParam(value = "Provider type: space or organization", required = false, defaultValue="organization") @QueryParam("type") String type,
                                @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                @ApiParam(value = "Returning the number of identities or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    try {
      offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
      limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
      
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
    } catch (Exception e) {
   	  return EntityBuilder.getResponse(new CollectionEntity(new ArrayList<DataEntity>(), EntityBuilder.IDENTITIES_TYPE, offset, limit), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
	}
  }

  /**
   * {@inheritDoc}
   */
  @POST
  @RolesAllowed("users")
  @ApiOperation(value = "Creates an identity",
                httpMethod = "POST",
                response = Response.class,
                notes = "This creates the identity if the authenticated user is in the group /platform/administrators")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response createIdentities(@Context UriInfo uriInfo,
                                   @ApiParam(value = "Remote id of the identity", required = true) @QueryParam("remoteId") String remoteId,
                                   @ApiParam(value = "Provider type: space or organization", required = true) @QueryParam("providerId") String providerId,
                                   @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    if ((! providerId.equals(SpaceIdentityProvider.NAME) && ! providerId.equals(OrganizationIdentityProvider.NAME))) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    //check if user already exist
    Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);
    if (identity == null) {
      identity = new Identity(providerId, remoteId);
      identityManager.updateIdentity(identity);
      identityManager.getProfile(identity);
    } else if (identity.isDeleted()) {
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
  @ApiOperation(value = "Gets an identity by id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns the identity if the authenticated user has permissions to view the object linked to this identity.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getIdentityById(@Context UriInfo uriInfo,
                                  @ApiParam(value = "Identity id which is a UUID such as 40487b7e7f00010104499b339f056aa4", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    IdentityEntity profileInfo = EntityBuilder.buildEntityIdentity(identity, uriInfo.getPath(), expand);
    return EntityBuilder.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   *
   * @param uriInfo
   * @param id
   * @return
   * @throws IOException
   */
  @GET
  @Path("{id}/avatar")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets an identity avatar by id",
          httpMethod = "GET",
          response = Response.class,
          notes = "This can only be done by the logged in user.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 500, message = "Internal server error"),
          @ApiResponse (code = 400, message = "Invalid query input"),
          @ApiResponse (code = 404, message = "Resource not found")})
  public Response getIdentityAvatarById(@Context UriInfo uriInfo,
                                        @Context Request request,
                                        @ApiParam(value = "Identity id which is a UUID", required = true)@PathParam("id") String id) throws IOException {
  
    Identity identity = identityManager.getIdentity(id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    //
    Profile profile = identity.getProfile();
    Long lastUpdated = null;
    if (profile != null) {
      lastUpdated = profile.getAvatarLastUpdated();
    }
    EntityTag eTag = null;
    if (lastUpdated != null) {
      eTag = new EntityTag(Integer.toString(lastUpdated.hashCode()));
    }
    //
    Response.ResponseBuilder builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
    if (builder == null) {
      InputStream stream = identityManager.getAvatarInputStream(identity);
      if (stream == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      /* As recommended in the the RFC1341 (https://www.w3.org/Protocols/rfc1341/4_Content-Type.html),
      we set the avatar content-type to "image/png". So, its data  would be recognized as "image" by the user-agent.
     */
      builder = Response.ok(stream, "image/png");
      builder.tag(eTag);
    }
    CacheControl cc = new CacheControl();
    cc.setMaxAge(86400);
    builder.cacheControl(cc);
    return builder.cacheControl(cc).build();
  }

  /**
   *
   * @param uriInfo
   * @param id
   * @return
   * @throws IOException
   */
  @GET
  @Path("{id}/banner")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets an identity banner by id",
          httpMethod = "GET",
          response = Response.class,
          notes = "This can only be done by the logged in user.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 500, message = "Internal server error"),
          @ApiResponse (code = 400, message = "Invalid query input"),
          @ApiResponse (code = 404, message = "Resource not found")})
  public Response getIdentityBannerById(@Context UriInfo uriInfo,
                                        @Context Request request,
                                        @ApiParam(value = "Identity id which is a UUID", required = true)@PathParam("id") String id) throws IOException {

    Identity identity = identityManager.getIdentity(id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    //
    Profile profile = identity.getProfile();
    Long lastUpdated = null;
    if (profile != null) {
      lastUpdated = profile.getBannerLastUpdated();
    }
    EntityTag eTag = null;
    if (lastUpdated != null) {
      eTag = new EntityTag(Integer.toString(lastUpdated.hashCode()));
    }
    //
    Response.ResponseBuilder builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
    if (builder == null) {
      InputStream stream = identityManager.getBannerInputStream(identity);
      if (stream == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      /* As recommended in the the RFC1341 (https://www.w3.org/Protocols/rfc1341/4_Content-Type.html),
      we set the banner content-type to "image/png". So, its data  would be recognized as "image" by the user-agent.
     */
      builder = Response.ok(stream, "image/png");
      builder.tag(eTag);
    }
    CacheControl cc = new CacheControl();
    cc.setMaxAge(86400);
    builder.cacheControl(cc);
    return builder.cacheControl(cc).build();
  }

  /**
   * {@inheritDoc}
   */
  @PUT
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Updates an identity by id",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This updates the identity if the authenticated user has permissions to view the object linked to this identity.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response updateIdentityById(@Context UriInfo uriInfo,
                                     @ApiParam(value = "Identity id which is a UUID such as 40487b7e7f00010104499b339f056aa4", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand,
                                     @ApiParam(value = "Updated profile object.", required = false) ProfileEntity model) throws Exception {
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    identityManager.updateProfile(fillProfileFromEntity(model, identity));
    
    IdentityEntity identityInfo = EntityBuilder.buildEntityIdentity(identity, uriInfo.getPath(), expand);
    
    return EntityBuilder.getResponse(identityInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  private Profile fillProfileFromEntity(ProfileEntity model, Identity identity) {
    Profile profile = identity.getProfile();
    setProperty(profile, Profile.FIRST_NAME, model.getFirstname());
    setProperty(profile, Profile.LAST_NAME, model.getLastname());
    setProperty(profile, Profile.EMAIL, model.getEmail());
    setProperty(profile, Profile.POSITION, model.getPosition());
    setProperty(profile, Profile.GENDER, model.getGender());
//    setProperty(profile, Profile.CONTACT_PHONES, model.getPhones());
//    setProperty(profile, Profile.CONTACT_IMS, model.getIMs());
//    setProperty(profile, Profile.CONTACT_URLS, model.getUrls());
    setProperty(profile, Profile.DELETED, model.getDeleted());
    return profile;
  }

  private Profile setProperty(Profile profile, String key, Object value) {
    if (value != null) {
      profile.setProperty(key, value);
    }
    return profile;
  }
  
  /**
   * {@inheritDoc}
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes an identity by id",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This deletes the identity if the authenticated user has permissions to view the object linked to this identity.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response deleteIdentityById(@Context UriInfo uriInfo,
                                     @ApiParam(value = "Identity id which is a UUID such as 40487b7e7f00010104499b339f056aa4", required = true) @PathParam("id") String id,
                                     @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {

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
  @ApiOperation(value = "Gets relationships of a specific identity",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns a list of relationships if the authenticated user can view the object linked to the identity.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getRelationshipsOfIdentity(@Context UriInfo uriInfo,
                                             @ApiParam(value = "The given identity id", required = true) @PathParam("id") String id,
                                             @ApiParam(value = "The other identity id to get the relationship with the given one") @QueryParam("with") String with,
                                             @ApiParam(value = "Returning the number of relationships or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                             @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                             @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                             @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
    
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

  @GET
  @Path("{id}/commonConnections")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets common connections with identity",
      httpMethod = "GET",
      response = Response.class,
      notes = "This returns the common connections between a the authenticated user and a given identity.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error"),
      @ApiResponse(code = 400, message = "Invalid query input") })
  public Response getCommonConnectionsWithIdentity(@Context UriInfo uriInfo,
                                                   @ApiParam(value = "The given identity id", required = true) @PathParam("id") String id,
                                                   @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                                   @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                                   @ApiParam(value = "Returning the number of common connections or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                                   @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {

    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);

    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class)
                                             .getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                  ConversationState.getCurrent().getIdentity().getUserId());

    List<Identity> currentUserConnections = Arrays.asList(relationshipManager.getConnections(authenticatedUser).load(0, 0));

    Identity withIdentity = CommonsUtils.getService(IdentityManager.class).getIdentity(id, true);
    List<Identity> withConnections = Arrays.asList(relationshipManager.getConnections(withIdentity).load(0, 0));

    List<Identity> commonConnections = currentUserConnections.stream()
                                                             .filter(withConnections::contains)
                                                             .collect(Collectors.toList());

    List<DataEntity> identityEntities = new ArrayList<DataEntity>();
    for (Identity identity : commonConnections) {
      identityEntities.add(EntityBuilder.buildEntityIdentity(identity, uriInfo.getPath(), expand).getDataEntity());
    }
    CollectionEntity collectionIdentity = new CollectionEntity(identityEntities, EntityBuilder.IDENTITIES_TYPE, offset, limit);
    if (returnSize) {
      collectionIdentity.setSize(commonConnections.size());
    }
    return EntityBuilder.getResponse(collectionIdentity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
}
