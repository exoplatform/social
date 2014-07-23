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
package org.exoplatform.social.service.rest;

import static org.exoplatform.social.service.rest.RestChecker.checkAuthenticatedRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api.models.IdentitiesCollections;

@Path(VersionResources.CURRENT_VERSION + "/social/identities")
public class IdentitiesRestService implements ResourceContainer {
  
  public IdentitiesRestService() {
  }
  
  /**
   * Process to return a list of identities in json format
   * 
   * @param uriInfo
   * @param offset
   * @param limit
   * @return
   * @throws Exception
   */
  @GET
  public Response getIdentities(@Context UriInfo uriInfo,
                                 @QueryParam("type") String type,
                                 @QueryParam("offset") int offset,
                                 @QueryParam("limit") int limit) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    limit = limit <= 0 ? RestUtils.DEFAULT_LIMIT : Math.min(RestUtils.HARD_LIMIT, limit);
    offset = offset < 0 ? RestUtils.DEFAULT_OFFSET : offset;
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    String providerId = (type != null && type.equals("space")) ? SpaceIdentityProvider.NAME : OrganizationIdentityProvider.NAME;
    ListAccess<Identity> listAccess = identityManager.getIdentitiesByProfileFilter(providerId, new ProfileFilter(), true);
    Identity[] identities = listAccess.load(offset, limit);
    
    List<Map<String, Object>> identityInfos = new ArrayList<Map<String, Object>>();
    for (Identity identity : identities) {
      Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity);
      profileInfo.put(RestProperties.REMOTE_ID, identity.getRemoteId());
      profileInfo.put(RestProperties.PROVIDER_ID, providerId);
      profileInfo.put(RestProperties.GLOBAL_ID, identity.getGlobalId());
      //
      identityInfos.add(profileInfo);
    }
    
    IdentitiesCollections collections = new IdentitiesCollections(listAccess.getSize(), offset, limit);
    collections.setIdentities(identityInfos);
    
    return Util.getResponse(collections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * Process to create an identity
   * 
   * @param uriInfo
   * @param remoteId
   * @param providerId
   * @return
   * @throws Exception
   */
  @POST
  public Response createIdentities(@Context UriInfo uriInfo,
                                    @QueryParam("remoteId") String remoteId,
                                    @QueryParam("providerId") String providerId) throws Exception {
    checkAuthenticatedRequest();
    if (Util.isAnonymous() || !RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    if (providerId == null || remoteId == null 
        || (! providerId.equals(SpaceIdentityProvider.NAME) && ! providerId.equals(OrganizationIdentityProvider.NAME))) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    //check if user already exist
    Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);
    if (identity != null) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    identity = new Identity(providerId, remoteId);
    identityManager.saveIdentity(identity);
    
    Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity);
    profileInfo.put(RestProperties.REMOTE_ID, identity.getRemoteId());
    profileInfo.put(RestProperties.PROVIDER_ID, providerId);
    profileInfo.put(RestProperties.GLOBAL_ID, identity.getGlobalId());
    
    return Util.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * Process to return an identity in json format
   * 
   * @param uriInfo
   * @param id
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public Response getIdentityById(@Context UriInfo uriInfo,
                                   @PathParam("id") String id) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, true);
    
    Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity);
    profileInfo.put(RestProperties.REMOTE_ID, identity.getRemoteId());
    profileInfo.put(RestProperties.PROVIDER_ID, identity.getProviderId());
    profileInfo.put(RestProperties.GLOBAL_ID, identity.getGlobalId());
    
    return Util.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * Process to update an identity by id
   * 
   * @param uriInfo
   * @param id
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}")
  public Response updateIdentityById(@Context UriInfo uriInfo,
                                      @PathParam("id") String id) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    //TODO : process to update identity
    
    Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity);
    profileInfo.put(RestProperties.REMOTE_ID, identity.getRemoteId());
    profileInfo.put(RestProperties.PROVIDER_ID, identity.getProviderId());
    profileInfo.put(RestProperties.GLOBAL_ID, identity.getGlobalId());
    
    return Util.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * Process to delete an identity
   * 
   * @param uriInfo
   * @param id
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}")
  public Response deleteIdentityById(@Context UriInfo uriInfo,
                                      @PathParam("id") String id) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    if (RestUtils.isMemberOfAdminGroup()) {
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
    
    Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity);
    profileInfo.put(RestProperties.REMOTE_ID, identity.getRemoteId());
    profileInfo.put(RestProperties.PROVIDER_ID, identity.getProviderId());
    profileInfo.put(RestProperties.GLOBAL_ID, identity.getGlobalId());
    
    return Util.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * Process to return all relationships of an identity in json format
   * 
   * @param uriInfo
   * @param id
   * @param offset
   * @param limit
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/relationships")
  public Response getRelationshipsOfIdentity(@Context UriInfo uriInfo,
                                              @PathParam("id") String id,
                                              @QueryParam("offset") int offset,
                                              @QueryParam("limit") int limit) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getIdentity(id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    
    Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity);
    profileInfo.put(RestProperties.REMOTE_ID, identity.getRemoteId());
    profileInfo.put(RestProperties.PROVIDER_ID, identity.getProviderId());
    profileInfo.put(RestProperties.GLOBAL_ID, identity.getGlobalId());
    
    return Util.getResponse(profileInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
}
