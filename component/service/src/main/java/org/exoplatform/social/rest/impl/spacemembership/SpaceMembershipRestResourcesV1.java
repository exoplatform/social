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
package org.exoplatform.social.rest.impl.spacemembership;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.api.SpaceMembershipRestResources;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.SpaceMembershipEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

@Path(VersionResources.VERSION_ONE + "/social/spacesMemberships")
public class SpaceMembershipRestResourcesV1 implements SpaceMembershipRestResources {
  
  private static final String SPACE_PREFIX = "/space/";
  
  private enum MembershipType {
    ALL, PENDING, APPROVED
  }
  
  public SpaceMembershipRestResourcesV1(){
  }

  @GET
  @RolesAllowed("users")
  public Response getSpacesMemberships(@Context UriInfo uriInfo) throws Exception {
    //
    int limit = RestUtils.getLimit(uriInfo);
    int offset = RestUtils.getOffset(uriInfo);

    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space givenSpace = null;
    String space= RestUtils.getQueryParam(uriInfo, "space");
    if (space != null) {
      givenSpace = spaceService.getSpaceByDisplayName(space);
      if (givenSpace == null) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }
    }
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = null;
    String user = RestUtils.getQueryParam(uriInfo, "user");
    if (user != null) {
      identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, true);
      if (identity == null) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }
    }
    
    int size;
//    String status = RestUtils.getQueryParam(uriInfo, "status");
    ListAccess<Space> listAccess = null;
    List<Space> spaces = new ArrayList<Space>();
    if (givenSpace == null) {
      listAccess = (identity == null) ? spaceService.getAllSpacesWithListAccess() : spaceService.getMemberSpaces(user);
      spaces = Arrays.asList(listAccess.load(offset, limit));
      size = RestUtils.isReturnSize(uriInfo) ? listAccess.getSize() : -1;
    } else {
      spaces.add(givenSpace);
      size = RestUtils.isReturnSize(uriInfo) ? spaces.size() : -1;
    }
    
    List<DataEntity> spaceMemberships = getSpaceMemberships(spaces, user, uriInfo);
    CollectionEntity spacesMemberships = new CollectionEntity(spaceMemberships, EntityBuilder.SPACES_MEMBERSHIP_TYPE, offset, limit);
    spacesMemberships.setSize(size);
    return EntityBuilder.getResponse(spacesMemberships, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @RolesAllowed("users")
  public Response addSpacesMemberships(@Context UriInfo uriInfo,
                                        SpaceMembershipEntity model) throws Exception {
    if (model == null || model.getUser() == null || model.getSpace() == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    String user = model.getUser();
    String space = model.getSpace();
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    if (space == null || spaceService.getSpaceByDisplayName(space) == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Space givenSpace = spaceService.getSpaceByDisplayName(space);
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    if (user == null || identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, true) == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (RestUtils.isMemberOfAdminGroup() || spaceService.isManager(givenSpace, authenticatedUser)
        || (authenticatedUser.equals(user) && givenSpace.getRegistration().equals(Space.OPEN))) {
      spaceService.addMember(givenSpace, user);
      if ("manager".equals(model.getRole())) {
        spaceService.setManager(givenSpace, user, true);
      }
    } else {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    SpaceMembershipEntity membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(givenSpace, user, "", uriInfo.getPath(),
                                                                                          RestUtils.getQueryParam(uriInfo, "expand"));
    return EntityBuilder.getResponse(membershipEntity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}") //id must have this format spaceName:userName:type
  @RolesAllowed("users")
  public Response getSpaceMembershipById(@Context UriInfo uriInfo) throws Exception {
    String[] idParams = RestUtils.getPathParam(uriInfo, "id").split(":");
    if (idParams.length != 3) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    if (CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, idParams[1], true) == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    String spaceGroupId = SPACE_PREFIX + idParams[0];
    Space space = spaceService.getSpaceByGroupId(spaceGroupId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (! authenticatedUser.equals(idParams[1]) && ! RestUtils.isMemberOfAdminGroup() && ! spaceService.isManager(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    SpaceMembershipEntity membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, idParams[1], idParams[2], uriInfo.getPath(),
                                                                                          RestUtils.getQueryParam(uriInfo, "expand"));
    return EntityBuilder.getResponse(membershipEntity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("{id}")
  @RolesAllowed("users")
  public Response updateSpaceMembershipById(@Context UriInfo uriInfo,
                                            SpaceMembershipEntity model) throws Exception {
    String[] idParams = RestUtils.getPathParam(uriInfo, "id").split(":");
    if (idParams.length != 3) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String targetUser = idParams[1];
    if (CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, targetUser, true) == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String spacePrettyName = idParams[0];
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    String spaceGroupId = SPACE_PREFIX + spacePrettyName;
    Space space = spaceService.getSpaceByGroupId(spaceGroupId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (! RestUtils.isMemberOfAdminGroup() && ! spaceService.isManager(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    space.setEditor(authenticatedUser);
    if (model.getRole() != null && model.getRole().equals("manager") && ! spaceService.isManager(space, targetUser)) {
      spaceService.setManager(space, targetUser, true);
    }
    if (model.getRole() != null && model.getRole().equals("member") && spaceService.isManager(space, targetUser)) {
      spaceService.setManager(space, targetUser, false);
    }
    //
    String role = idParams[2];
    SpaceMembershipEntity membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, targetUser, role, uriInfo.getPath(),
                                                                                          RestUtils.getQueryParam(uriInfo, "expand"));    
    return EntityBuilder.getResponse(membershipEntity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  public Response deleteSpaceMembershipById(@Context UriInfo uriInfo) throws Exception {
    String[] idParams = RestUtils.getPathParam(uriInfo, "id").split(":");
    if (idParams.length != 3) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String targetUser = idParams[1];
    if (CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, targetUser, true) == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String spacePrettyName = idParams[0];
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    String spaceGroupId = SPACE_PREFIX + spacePrettyName;
    Space space = spaceService.getSpaceByGroupId(spaceGroupId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (! authenticatedUser.equals(targetUser) && ! RestUtils.isMemberOfAdminGroup() && ! spaceService.isManager(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String role = idParams[2];
    space.setEditor(authenticatedUser);
    if (role != null && role.equals("manager")) {
      spaceService.setManager(space, targetUser, false);
    }
    if (role != null && role.equals("member")) {
      if (spaceService.isManager(space, targetUser)) {
        spaceService.setManager(space, targetUser, false);
      }
      spaceService.removeMember(space, targetUser);
    }
    //
    SpaceMembershipEntity membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, targetUser, role, uriInfo.getPath(),
                                                                                          RestUtils.getQueryParam(uriInfo, "expand"));    
    return EntityBuilder.getResponse(membershipEntity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  private List<DataEntity> getSpaceMemberships(List<Space> spaces, String userId, UriInfo uriInfo) {
    List<DataEntity> spaceMemberships = new ArrayList<DataEntity>();
    SpaceMembershipEntity membershipEntity = null;
    for (Space space : spaces) {
      if (userId != null) {
        if (ArrayUtils.contains(space.getMembers(), userId)) {
          membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, "member", uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand"));
          spaceMemberships.add(membershipEntity.getDataEntity());
        }
        if (ArrayUtils.contains(space.getManagers(), userId)) {
          membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, "manager", uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand"));
          spaceMemberships.add(membershipEntity.getDataEntity());
        }
      } else {
        for (String user : space.getMembers()) {
          membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, user, "member", uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand"));
          spaceMemberships.add(membershipEntity.getDataEntity());
        }
        for (String user : space.getManagers()) {
          membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, user, "manager", uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand"));
          spaceMemberships.add(membershipEntity.getDataEntity());
        }
      }
    }
    return spaceMemberships;
  }
}
