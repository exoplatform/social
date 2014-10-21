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
package org.exoplatform.social.service.rest.impl.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
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
import org.exoplatform.social.service.rest.RestProperties;
import org.exoplatform.social.service.rest.RestUtils;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.AbstractSocialRestService;
import org.exoplatform.social.service.rest.api.IdentitySocialRest;
import org.exoplatform.social.service.rest.api.models.IdentitiesCollections;
import org.exoplatform.social.service.rest.api.models.RelationshipsCollections;

@Path("v1/social/identities")
public class IdentitySocialRestServiceV1 extends AbstractSocialRestService implements IdentitySocialRest {
  

  /**
   * {@inheritDoc}
   */
  @GET
  @RolesAllowed("users")
  public Response getIdentities(@Context UriInfo uriInfo) throws Exception {
    String type = getQueryParam("type");
    
    int limit = getQueryValueLimit(uriInfo);
    int offset = getQueryValueOffset(uriInfo);
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    String providerId = (type != null && type.equals("space")) ? SpaceIdentityProvider.NAME : OrganizationIdentityProvider.NAME;
    ListAccess<Identity> listAccess = identityManager.getIdentitiesByProfileFilter(providerId, new ProfileFilter(), true);
    Identity[] identities = listAccess.load(offset, limit);
    
    List<Map<String, Object>> identityInfos = new ArrayList<Map<String, Object>>();
    for (Identity identity : identities) {
      Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryValueExpand(uriInfo));
      profileInfo.put(RestProperties.REMOTE_ID, identity.getRemoteId());
      profileInfo.put(RestProperties.PROVIDER_ID, providerId);
      profileInfo.put(RestProperties.GLOBAL_ID, identity.getGlobalId());
      
      identityInfos.add(profileInfo);
    }
    
    IdentitiesCollections collections = new IdentitiesCollections(getQueryValueReturnSize(uriInfo) ? listAccess.getSize() : -1, offset, limit);
    collections.setIdentities(identityInfos);
    
    return Util.getResponse(collections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  

  /**
   * {@inheritDoc}
   */
  @POST
  @RolesAllowed("users")
  public Response createIdentities(@Context UriInfo uriInfo) throws Exception {
    String remoteId = getQueryParam("remoteId");
    String providerId = getQueryParam("providerId"); 
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    if (providerId == null || remoteId == null 
        || (! providerId.equals(SpaceIdentityProvider.NAME) && ! providerId.equals(OrganizationIdentityProvider.NAME))) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    //check if user already exist
    Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);
    if (identity.isDeleted()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryValueExpand(uriInfo));
    profileInfo.put(RestProperties.REMOTE_ID, identity.getRemoteId());
    profileInfo.put(RestProperties.PROVIDER_ID, providerId);
    profileInfo.put(RestProperties.GLOBAL_ID, identity.getGlobalId());
    
    return Util.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  

  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  public Response getIdentityById(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, true);
    
    Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryValueExpand(uriInfo));
    profileInfo.put(RestProperties.REMOTE_ID, identity.getRemoteId());
    profileInfo.put(RestProperties.PROVIDER_ID, identity.getProviderId());
    profileInfo.put(RestProperties.GLOBAL_ID, identity.getGlobalId());
    
    return Util.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  

  /**
   * {@inheritDoc}
   */
  @PUT
  @Path("{id}")
  @RolesAllowed("users")
  public Response updateIdentityById(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    //TODO : process to update identity
    
    Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryValueExpand(uriInfo));
    profileInfo.put(RestProperties.REMOTE_ID, identity.getRemoteId());
    profileInfo.put(RestProperties.PROVIDER_ID, identity.getProviderId());
    profileInfo.put(RestProperties.GLOBAL_ID, identity.getGlobalId());
    
    return Util.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  

  /**
   * {@inheritDoc}
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  public Response deleteIdentityById(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
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
    
    Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryValueExpand(uriInfo));
    profileInfo.put(RestProperties.REMOTE_ID, identity.getRemoteId());
    profileInfo.put(RestProperties.PROVIDER_ID, identity.getProviderId());
    profileInfo.put(RestProperties.GLOBAL_ID, identity.getGlobalId());
    
    return Util.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  

  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}/relationships")
  @RolesAllowed("users")
  public Response getRelationshipsOfIdentity(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    String with = getQueryParam("with");
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    
    if (with != null && with.length() > 0) {
      Identity withUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, with, true);
      if (withUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      //
      Relationship relationship = relationshipManager.get(identity, withUser);
      if (relationship == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      return Util.getResponse(RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath(), getQueryValueExpand(uriInfo), false), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
    }
    
    int limit = getQueryValueLimit(uriInfo);
    int offset = getQueryValueOffset(uriInfo);
    
    List<Relationship> relationships = relationshipManager.getRelationshipsByStatus(identity, Relationship.Type.ALL, offset, limit);
    int size = getQueryValueReturnSize(uriInfo) ? relationshipManager.getRelationshipsCountByStatus(identity, Relationship.Type.ALL) : -1;
    
    RelationshipsCollections collections = new RelationshipsCollections(size, offset, limit);
    collections.setRelationships(buildRelationshipsCollections(relationships, uriInfo));
    
    return Util.getResponse(collections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  private List<Map<String, Object>> buildRelationshipsCollections(List<Relationship> relationships, UriInfo uriInfo) {
    List<Map<String, Object>> infos = new ArrayList<Map<String, Object>>();
    for (Relationship relationship : relationships) {
      Map<String, Object> map = RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath(), getQueryParam("expand"), true);
      //
      infos.add(map);
    }
    return infos;
  }
}
