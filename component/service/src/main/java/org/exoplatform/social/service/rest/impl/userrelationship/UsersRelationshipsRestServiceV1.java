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
package org.exoplatform.social.service.rest.impl.userrelationship;

import static org.exoplatform.social.service.rest.RestChecker.checkAuthenticatedRequest;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.service.rest.RestUtils;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.AbstractSocialRestService;
import org.exoplatform.social.service.rest.api.UserRelationshipSocialRest;
import org.exoplatform.social.service.rest.api.models.RelationshipsCollections;

@Path("v1/social/usersRelationships")
public class UsersRelationshipsRestServiceV1 extends AbstractSocialRestService implements UserRelationshipSocialRest {

  public UsersRelationshipsRestServiceV1() {
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.social.service.rest.impl.userrelationship.UserRelationshipSocialRest#getUsersRelationships(javax.ws.rs.core.UriInfo, java.lang.String, java.lang.String, int, int)
   */
  @Override
  @GET
  public Response getUsersRelationships(@Context UriInfo uriInfo,
                                         @QueryParam("status") String status,
                                         @QueryParam("user") String user,
                                         @QueryParam("offset") int offset,
                                         @QueryParam("limit") int limit,
                                         @QueryParam("returnSize") boolean returnSize,
                                         @QueryParam("fields") String fields) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    List<String> returnedProperties = new ArrayList<String>();
    if (fields != null && fields.length() > 0) {
      returnedProperties.addAll(Arrays.asList(fields.split(",")));
    }
    
    limit = limit <= 0 ? RestUtils.DEFAULT_LIMIT : Math.min(RestUtils.HARD_LIMIT, limit);
    offset = offset < 0 ? RestUtils.DEFAULT_OFFSET : offset;
    
    Identity givenUser = (user == null) ? null : CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, true);
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    if (givenUser != null && ! RestUtils.isMemberOfAdminGroup()) {
      Relationship relationship = relationshipManager.get(givenUser, authenticatedUser);
      RelationshipsCollections collections = new RelationshipsCollections(1, offset, limit);
      Map<String, Object> map = RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath());
      collections.setRelationships(Arrays.asList(map));
      return Util.getResponse(collections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
    }
    
    Relationship.Type type;
    try {
      type = Relationship.Type.valueOf(status.toUpperCase());
    } catch (Exception e) {
      type = Relationship.Type.ALL;
    }
    
    List<Relationship> relationships = new ArrayList<Relationship>();
    int size = 0;
    if (givenUser == null & RestUtils.isMemberOfAdminGroup()) {
      //gets all relationships from database by status
      relationships = relationshipManager.getRelationshipsByStatus(null, type, offset, limit);
      size = relationshipManager.getRelationshipsCountByStatus(null, type);
    } else {
      if (givenUser == null) {
        relationships = relationshipManager.getRelationshipsByStatus(authenticatedUser, type, offset, limit);
        size = relationshipManager.getRelationshipsCountByStatus(authenticatedUser, type);
      } else {
        relationships = relationshipManager.getRelationshipsByStatus(givenUser, type, offset, limit);
        size = relationshipManager.getRelationshipsCountByStatus(givenUser, type);
      }
    }
    
    RelationshipsCollections collections = new RelationshipsCollections(returnSize ? size : -1, offset, limit);
    collections.setRelationships(buildRelationshipsCollections(relationships, uriInfo));
    
    return Util.getResponse(collections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.social.service.rest.impl.userrelationship.UserRelationshipSocialRest#createUsersRelationships(javax.ws.rs.core.UriInfo, java.lang.String, java.lang.String)
   */
  @Override
  @POST
  public Response createUsersRelationships(@Context UriInfo uriInfo,
                                            @QueryParam("status") String status,
                                            @QueryParam("user") String user) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    return Util.getResponse("", uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.social.service.rest.impl.userrelationship.UserRelationshipSocialRest#getUsersRelationshipsById(javax.ws.rs.core.UriInfo, java.lang.String)
   */
  @Override
  @GET
  @Path("{id}")
  public Response getUsersRelationshipsById(@Context UriInfo uriInfo,
                                             @PathParam("id") String id) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null || ! hasPermissionOnRelationship(authenticatedUser, relationship)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return Util.getResponse(RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath()), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.social.service.rest.impl.userrelationship.UserRelationshipSocialRest#updateUsersRelationshipsById(javax.ws.rs.core.UriInfo, java.lang.String)
   */
  @Override
  @PUT
  @Path("{id}")
  public Response updateUsersRelationshipsById(@Context UriInfo uriInfo,
                                                @PathParam("id") String id) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null || ! hasPermissionOnRelationship(authenticatedUser, relationship)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    //update relationship
    relationshipManager.update(relationship);
    
    return Util.getResponse(RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath()), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.social.service.rest.impl.userrelationship.UserRelationshipSocialRest#deleteUsersRelationshipsById(javax.ws.rs.core.UriInfo, java.lang.String)
   */
  @Override
  @DELETE
  @Path("{id}")
  public Response deleteUsersRelationshipsById(@Context UriInfo uriInfo,
                                                @PathParam("id") String id) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null || ! hasPermissionOnRelationship(authenticatedUser, relationship)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //delete the relationship
    relationshipManager.delete(relationship);
    
    return Util.getResponse(RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath()), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
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
  
  private List<Map<String, Object>> buildRelationshipsCollections(List<Relationship> relationships, UriInfo uriInfo) {
    List<Map<String, Object>> infos = new ArrayList<Map<String, Object>>();
    for (Relationship relationship : relationships) {
      Map<String, Object> map = RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath());
      //
      infos.add(map);
    }
    return infos;
  }
}
