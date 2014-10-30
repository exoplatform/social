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

import java.util.ArrayList;
import java.util.Arrays;
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
import org.exoplatform.social.service.rest.api.models.RelationshipRestIn;
import org.exoplatform.social.service.rest.api.models.RelationshipsCollections;

@Path("v1/social/usersRelationships")
public class UsersRelationshipsRestServiceV1 extends AbstractSocialRestService implements UserRelationshipSocialRest {

  public UsersRelationshipsRestServiceV1() {
  }
  
  @RolesAllowed("users")
  @GET
  public Response getUsersRelationships(@Context UriInfo uriInfo) throws Exception {
    String status = getQueryParam("status");
    String user = getQueryParam("user");
    
    int limit = getQueryValueLimit();
    int offset = getQueryValueOffset();
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity givenUser = (user == null) ? null : identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, true);
    Identity authenticatedUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    if (givenUser != null && ! RestUtils.isMemberOfAdminGroup()) {
      Relationship relationship = relationshipManager.get(givenUser, authenticatedUser);
      RelationshipsCollections collections = new RelationshipsCollections(1, offset, limit);
      Map<String, Object> map = RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath(), getQueryParam("expand"), false);
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
      size = getQueryValueReturnSize() ? relationshipManager.getRelationshipsCountByStatus(null, type) : -1;
    } else {
      if (givenUser == null) {
        relationships = relationshipManager.getRelationshipsByStatus(authenticatedUser, type, offset, limit);
        size = getQueryValueReturnSize() ? relationshipManager.getRelationshipsCountByStatus(authenticatedUser, type) : -1;
      } else {
        relationships = relationshipManager.getRelationshipsByStatus(givenUser, type, offset, limit);
        size = getQueryValueReturnSize() ? relationshipManager.getRelationshipsCountByStatus(givenUser, type) : -1;
      }
    }
    
    RelationshipsCollections collections = new RelationshipsCollections(size, offset, limit);
    collections.setRelationships(buildRelationshipsCollections(relationships, uriInfo));
    
    return Util.getResponse(collections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @RolesAllowed("users")
  public Response createUsersRelationships(@Context UriInfo uriInfo,
                                            RelationshipRestIn model) throws Exception {
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
    
    Relationship.Type status = model.getStatus() != null && model.getStatus().equalsIgnoreCase("pending") ? Relationship.Type.PENDING : Relationship.Type.CONFIRMED;
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    if (relationshipManager.get(sender, receiver) != null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Relationship relationship = new Relationship(sender, receiver, status);
    relationshipManager.update(relationship);
    
    return Util.getResponse(RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath(), getQueryParam("expand"), false), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @RolesAllowed("users")
  @Path("{id}")
  public Response getUsersRelationshipsById(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null || ! hasPermissionOnRelationship(authenticatedUser, relationship)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return Util.getResponse(RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath(), getQueryParam("expand"), false), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @RolesAllowed("users")
  @Path("{id}")
  public Response updateUsersRelationshipsById(@Context UriInfo uriInfo,
                                                RelationshipRestIn model) throws Exception {
    if (model == null || model.getStatus() == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String id = getPathParam("id");
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null || ! hasPermissionOnRelationship(authenticatedUser, relationship)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Relationship.Type status;
    try {
      status = Relationship.Type.valueOf(model.getStatus());
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //update relationship by status
    updateRelationshipByStatus(relationship, status, relationshipManager);
    
    return Util.getResponse(RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath(), getQueryParam("expand"), false), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @RolesAllowed("users")
  @Path("{id}")
  public Response deleteUsersRelationshipsById(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    
    Identity authenticatedUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    RelationshipManager relationshipManager = CommonsUtils.getService(RelationshipManager.class);
    Relationship relationship = relationshipManager.get(id);
    if (relationship == null || ! hasPermissionOnRelationship(authenticatedUser, relationship)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //delete the relationship
    relationshipManager.delete(relationship);
    
    return Util.getResponse(RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath(), getQueryParam("expand"), false), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
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
      Map<String, Object> map = RestUtils.buildEntityFromRelationship(relationship, uriInfo.getPath(), getQueryParam("expand"), false);
      //
      infos.add(map);
    }
    return infos;
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
        relationshipManager.update(relationship);
        break;
      }
      default:
        break;
      }
  }
}
