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
package org.exoplatform.social.service.rest.impl.spacemembership;

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

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.RestUtils;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.AbstractSocialRestService;
import org.exoplatform.social.service.rest.api.SpaceMembershipSocialRest;
import org.exoplatform.social.service.rest.api.models.SpaceMembershipsCollections;

@Path("v1/social/spacesMemberships")
public class SpaceMembershipRestServiceV1 extends AbstractSocialRestService implements SpaceMembershipSocialRest {
  
  public SpaceMembershipRestServiceV1(){
  }

  @GET
  public Response getSpacesMemberships(@Context UriInfo uriInfo,
                                        @QueryParam("status") String status,
                                        @QueryParam("user") String user,
                                        @QueryParam("space") String space) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    List<String> returnedProperties = new ArrayList<String>();
    String fields = getQueryValueFields(uriInfo);
    if (fields != null && fields.length() > 0) {
      returnedProperties.addAll(Arrays.asList(fields.split(",")));
    }
    
    //
    int limit = getQueryValueLimit(uriInfo);
    int offset = getQueryValueOffset(uriInfo);

    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space givenSpace = null;
    if (space != null) {
      givenSpace = spaceService.getSpaceByDisplayName(space);
      if (givenSpace == null) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }
    }
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = null;
    if (user != null) {
      identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, true);
      if (identity == null) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }
    }
    
    int size = 0;
    ListAccess<Space> listAccess = null;
    List<Space> spaces = new ArrayList<Space>();
    if (givenSpace == null) {
      listAccess = (identity == null) ? spaceService.getAllSpacesWithListAccess() : spaceService.getMemberSpaces(user);
      spaces = Arrays.asList(listAccess.load(offset, limit));
      size = listAccess.getSize();
    } else {
      spaces.add(givenSpace);
      size = spaces.size();
    }
    
    List<Map<String, Object>> spaceMemberships = new ArrayList<Map<String, Object>>();
    setSpaceMemberships(spaceMemberships, spaces, user, uriInfo, returnedProperties);
    
    SpaceMembershipsCollections membershipsCollections = new SpaceMembershipsCollections(getQueryValueReturnSize(uriInfo) ? size : -1, offset, limit);
    membershipsCollections.setSpaceMemberships(spaceMemberships);
    
    return Util.getResponse(membershipsCollections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  public Response addSpacesMemberships(@Context UriInfo uriInfo,
                                        @QueryParam("user") String user,
                                        @QueryParam("space") String space) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    if (space == null || spaceService.getSpaceByDisplayName(space) == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    Space givenSpace = spaceService.getSpaceByDisplayName(space);
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    if (user == null || identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, true) == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    if (RestUtils.isMemberOfAdminGroup() || spaceService.isManager(givenSpace, authenticatedUser)
        || (authenticatedUser.equals(user) && givenSpace.getRegistration().equals(Space.OPEN))) {
      spaceService.addMember(givenSpace, user);
    } else {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    return Util.getResponse(RestUtils.buildEntityFromSpaceMembership(givenSpace, user, "", uriInfo.getPath(), getQueryValueExpand(uriInfo)), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/{spacesPrefix}/{spacePrettyName}")
  public Response getSpaceMembershipById(@Context UriInfo uriInfo,
                                          @PathParam("id") String id,
                                          @PathParam("spacesPrefix") String spacesPrefix,
                                          @PathParam("spacePrettyName") String spacePrettyName) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    MembershipHandler handler = organizationService.getMembershipHandler();
    Membership membership;
    try {
      id = id + "/" + spacesPrefix + "/" + spacePrettyName;
      membership = handler.findMembership(id);
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceByGroupId(membership.getGroupId());
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    return Util.getResponse(RestUtils.buildEntityFromSpaceMembership(space, membership.getUserName(), membership.getMembershipType(), uriInfo.getPath(), getQueryValueExpand(uriInfo)), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("{id}/{spacesPrefix}/{spacePrettyName}")
  public Response updateSpaceMembershipById(@Context UriInfo uriInfo,
                                             @PathParam("id") String id,
                                             @PathParam("spacesPrefix") String spacesPrefix,
                                             @PathParam("spacePrettyName") String spacePrettyName,
                                             @QueryParam("type") String type) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    MembershipHandler handler = organizationService.getMembershipHandler();
    Membership membership;
    id = id + "/" + spacesPrefix + "/" + spacePrettyName;
    try {
      membership = handler.findMembership(id);
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceByGroupId(membership.getGroupId());
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    space.setEditor(authenticatedUser);
    if (type.equals("manager") && ! spaceService.isManager(space, membership.getUserName())) {
      spaceService.setManager(space, membership.getUserName(), true);
    }
    if (type.equals("member") && spaceService.isManager(space, membership.getUserName())) {
      spaceService.setManager(space, membership.getUserName(), false);
    }
    
    membership = handler.findMembershipByUserGroupAndType(membership.getUserName(), membership.getGroupId(), type);
    
    return Util.getResponse(RestUtils.buildEntityFromSpaceMembership(space, membership.getUserName(), membership.getMembershipType(), uriInfo.getPath(), getQueryValueExpand(uriInfo)), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @Path("{id}/{spacesPrefix}/{spacePrettyName}")
  public Response deleteSpaceMembershipById(@Context UriInfo uriInfo,
                                             @PathParam("id") String id,
                                             @PathParam("spacesPrefix") String spacesPrefix,
                                             @PathParam("spacePrettyName") String spacePrettyName) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    MembershipHandler handler = organizationService.getMembershipHandler();
    Membership membership;
    id = id + "/" + spacesPrefix + "/" + spacePrettyName;
    try {
      membership = handler.findMembership(id);
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceByGroupId(membership.getGroupId());
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    //remove user from space
    space.setEditor(authenticatedUser);
    spaceService.setManager(space, membership.getUserName(), false);
    spaceService.removeMember(space, membership.getUserName());
    
    return Util.getResponse(RestUtils.buildEntityFromSpaceMembership(space, membership.getUserName(), membership.getMembershipType(), uriInfo.getPath(), getQueryValueExpand(uriInfo)), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  private void setSpaceMemberships(List<Map<String, Object>> spaceMemberships, List<Space> spaces, 
      String userId, UriInfo uriInfo, List<String> returnedProperties) {
    for (Space space : spaces) {
      if (userId != null) {
        if (ArrayUtils.contains(space.getMembers(), userId)) {
          spaceMemberships.add(RestUtils.buildEntityFromSpaceMembership(space, userId, "member", uriInfo.getPath(), getQueryValueExpand(uriInfo)));
        }
        if (ArrayUtils.contains(space.getManagers(), userId)) {
          spaceMemberships.add(RestUtils.buildEntityFromSpaceMembership(space, userId, "manager", uriInfo.getPath(), getQueryValueExpand(uriInfo)));
        }
      } else {
        for (String user : space.getMembers()) {
          spaceMemberships.add(RestUtils.buildEntityFromSpaceMembership(space, user, "member", uriInfo.getPath(), getQueryValueExpand(uriInfo)));
        }
        for (String user : space.getManagers()) {
          spaceMemberships.add(RestUtils.buildEntityFromSpaceMembership(space, user, "manager", uriInfo.getPath(), getQueryValueExpand(uriInfo)));
        }
      }
    }
  }
  
}
