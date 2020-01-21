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

import static org.exoplatform.social.service.rest.RestChecker.checkAuthenticatedUserPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.Arrays;
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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.api.SpaceMembershipRestResources;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.SpaceMembershipEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.utils.LogUtils;

@Path(VersionResources.VERSION_ONE + "/social/spacesMemberships")
@Api(tags = VersionResources.VERSION_ONE + "/social/spacesMemberships", value = VersionResources.VERSION_ONE + "/social/spacesMemberships", description = "Managing memberships of users in a space")
public class SpaceMembershipRestResourcesV1 implements SpaceMembershipRestResources {
  
  private static final String SPACE_PREFIX = "/spaces/";

  private SpaceService spaceService;

  private IdentityManager identityManager;

  private enum MembershipType {
    ALL, PENDING, APPROVED, IGNORED, INVITED
  }
  
  public SpaceMembershipRestResourcesV1(SpaceService spaceService, IdentityManager identityManager) {
    this.spaceService = spaceService;
    this.identityManager = identityManager;
  }

  @GET
  @RolesAllowed("users")
  @ApiOperation(value = "Gets space memberships",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns space memberships in the following cases: <br/><ul><li>the sender of the space membership is the authenticated user</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is the super user</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 404, message = "Resource not found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getSpacesMemberships(@Context UriInfo uriInfo,
                                       @ApiParam(value = "Space display name to get membership, ex: my space", required = false) @QueryParam("space") String spaceDisplayName,
                                       @ApiParam(value = "User name to filter only memberships of the given user", required = false) @QueryParam("user") String user,
                                       @ApiParam(value = "Type of membership to get (All, Pending, Approved, Invited)", required = false) @QueryParam("status") String status,
                                       @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                       @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                       @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand,
                                       @ApiParam(value = "Returning the number of memberships or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize) throws Exception {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (user == null) {
      user = authenticatedUser;
    }

    if(!spaceService.isSuperManager(authenticatedUser)) {
      if (StringUtils.isNotEmpty(spaceDisplayName)) {
        Space space = spaceService.getSpaceByDisplayName(spaceDisplayName);
        if(space == null || !spaceService.isManager(space, authenticatedUser)) {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
      } else if (!user.equals(authenticatedUser)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    }

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    MembershipType membershipType;
    try {
      membershipType = MembershipType.valueOf(status.toUpperCase());
    } catch (Exception e) {
      membershipType = MembershipType.ALL;
    }

    ListAccess<Space> listAccess = null;
    
    switch (membershipType) {
      case PENDING: {
        listAccess = spaceDisplayName != null ? spaceService.getPendingSpacesByFilter(
          user, new SpaceFilter(spaceDisplayName)) : spaceService.getPendingSpacesWithListAccess(user);
        break;
      }

      case APPROVED: {
        listAccess = spaceDisplayName != null ? spaceService.getAccessibleSpacesByFilter(
          user, new SpaceFilter(spaceDisplayName)) : spaceService.getAccessibleSpacesWithListAccess(user);
        break;
      }
      case INVITED: {
        listAccess = spaceDisplayName != null ? spaceService.getInvitedSpacesByFilter(
                user, new SpaceFilter(spaceDisplayName)) : spaceService.getInvitedSpacesWithListAccess(user);
        break;
      }

      default:
        SpaceFilter spaceFilter = new SpaceFilter();
        if (spaceDisplayName != null) {
          spaceFilter.setSpaceNameSearchCondition(spaceDisplayName);
        }
        spaceFilter.setRemoteId(user);
        listAccess = spaceService.getAllSpacesByFilter(spaceFilter);

        break;
    }
    
    List<DataEntity> spaceMemberships = getSpaceMemberships(Arrays.asList(listAccess.load(offset, limit)), user, uriInfo.getPath(), expand);
    CollectionEntity spacesMemberships = new CollectionEntity(spaceMemberships, EntityBuilder.SPACES_MEMBERSHIP_TYPE, offset, limit);
    
    if (returnSize) {
      spacesMemberships.setSize(listAccess.getSize());
    }
    //
    Response.ResponseBuilder builder = EntityBuilder.getResponseBuilder(spacesMemberships, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
    CacheControl cc = new CacheControl();
    cc.setNoStore(true);
    builder.cacheControl(cc);
    
    return builder.build();
  }
  
  @POST
  @RolesAllowed("users")
  @ApiOperation(value = "Creates a space membership for a specific user",
                httpMethod = "POST",
                response = Response.class,
                notes = "This creates the space membership in the following cases: <br/><ul><li>the sender of the space membership is the authenticated user and the space subscription is open</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response addSpacesMemberships(@Context UriInfo uriInfo,
                                       @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand,
                                       @ApiParam(value = "Space membership object to be created, ex:<br />{" +
                                                                                               "<br />\"role\": \"manager\"," +
                                                                                               "<br />\"user\": \"john\"," +
                                                                                               "<br />\"space\": \"my space\"" +
                                                                                               "<br />}" 
                                                 , required = true) SpaceMembershipEntity model) throws Exception {

    if (model == null || model.getUser() == null || model.getSpace() == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Response response;
    String user = model.getUser();
    String space = model.getSpace();
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    if (space == null || spaceService.getSpaceByDisplayName(space) == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Space givenSpace = spaceService.getSpaceByDisplayName(space);
    if (!(MembershipType.IGNORED.name().equals(model.getStatus()))) {
      if (user == null || identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, true) == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      if (spaceService.isSuperManager(authenticatedUser) || spaceService.isManager(givenSpace, authenticatedUser)
          || (authenticatedUser.equals(user) && givenSpace.getRegistration().equals(Space.OPEN))) {
        spaceService.addMember(givenSpace, user);
        if ("manager".equals(model.getRole())) {
          spaceService.setManager(givenSpace, user, true);
        }
      } else {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      SpaceMembershipEntity membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(givenSpace, user, "", uriInfo.getPath(), expand);
      response = EntityBuilder.getResponse(membershipEntity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
    } else {
      SpaceMembershipEntity membershipEntity = EntityBuilder.createSpaceMembershipForIgnoredStatus(givenSpace, user, "", uriInfo.getPath(), expand);
      spaceService.setIgnored(givenSpace.getId(), user);
      response = EntityBuilder.getResponse(membershipEntity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
    }
    return response;
  }
  
  @GET
  @Path("{id}") //id must have this format spaceName:userName:type
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a specific space membership by id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns the space membership in the following cases: <br/><ul><li>the user of the space membership is the authenticated user</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 404, message = "Resource not found"),
    @ApiResponse (code = 500, message = "Internal server error due to data encoding") })
  public Response getSpaceMembershipById(@Context UriInfo uriInfo,
                                         @ApiParam(value = "Space membership id which is in format spaceName:userName:role, ex: my_space:root:manager", required = true) @PathParam("id") String id,
                                         @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    String[] idParams = RestUtils.getPathParam(uriInfo, "id").split(":");
    if (idParams.length != 3) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    if (identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, idParams[1], true) == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String spaceGroupId = SPACE_PREFIX + idParams[0];
    Space space = spaceService.getSpaceByGroupId(spaceGroupId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (! authenticatedUser.equals(idParams[1]) && ! spaceService.isSuperManager(authenticatedUser) && ! spaceService.isManager(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    SpaceMembershipEntity membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, idParams[1], idParams[2], uriInfo.getPath(),
                                                                                          expand);
    return EntityBuilder.getResponse(membershipEntity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Updates a specific space membership by id",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This updates the space membership in the following cases: <br/><ul><li>the user of the space membership is the authenticated user but he cannot update his own membership to \"approved\" for a space with a \"validation\" subscription</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is a spaces super manager</li><li>the user of the space membership is the authenticated user, he can update his own membership to \"approved\" or \"ignored\" for a space with a \"closed\" subscription</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 500, message = "Internal server error due to data encoding") })
  public Response updateSpaceMembershipById(@Context UriInfo uriInfo,
                                            @ApiParam(value = "Space membership id which is in format spaceName:userName:role, ex: my_space:root:manager", required = true) @PathParam("id") String id,
                                            @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand,
                                            @ApiParam(value = "Space membership object to be updated", required = true) SpaceMembershipEntity model) throws Exception {
    String[] idParams = RestUtils.getPathParam(uriInfo, "id").split(":");
    if (idParams.length != 3) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String targetUser = idParams[1];
    if (identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, targetUser, true) == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String spacePrettyName = idParams[0];
    Space space = spaceService.getSpaceByPrettyName(spacePrettyName);
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (model.getRole() != null) {
      if (!spaceService.isSuperManager(authenticatedUser) && ! spaceService.isManager(space, authenticatedUser)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      space.setEditor(authenticatedUser);
      if (model.getRole().equals("manager") && ! spaceService.isManager(space, targetUser)) {
        spaceService.setManager(space, targetUser, true);
      }
      if (model.getRole().equals("member") && spaceService.isManager(space, targetUser)) {
        spaceService.setManager(space, targetUser, false);
      }
    }
    String role = idParams[2];
    if (role.equalsIgnoreCase(MembershipType.INVITED.name())) {
      //Check authenticated user
      checkAuthenticatedUserPermission(targetUser);
      if (!spaceService.isInvitedUser(space, targetUser)) {
        throw new WebApplicationException(Response.Status.FORBIDDEN);
      }
      if (model.getStatus() != null) {
        if (model.getStatus().equalsIgnoreCase(MembershipType.APPROVED.name())) {
          spaceService.addMember(space, targetUser);
          role = MembershipType.APPROVED.name();
          LogUtils.logInfo("spaceMembership", "approve-space-invitation", "space_name:" + spacePrettyName + ",receiver:" + targetUser, this.getClass());
        }
        else if (model.getStatus().equalsIgnoreCase(MembershipType.IGNORED.name())) {
          spaceService.removeInvitedUser(space, targetUser);
          role = MembershipType.IGNORED.name();
          LogUtils.logInfo("spaceMembership", "ignore-space-invitation", "space_name:" + spacePrettyName + ",receiver:" + targetUser, this.getClass());
        }
      }
    }
    //
    
    SpaceMembershipEntity membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, targetUser, role, uriInfo.getPath(),
                                                                                          expand);    
    return EntityBuilder.getResponse(membershipEntity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a specific space membership by id",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This deletes the space membership in the following cases: <br/><ul><li>the user of the space membership is the authenticated user</li><li>the authenticated user is a manager of the space</li><li>the authenticated user is a spaces super manager</li></ul>")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 404, message = "Resource not found"),
    @ApiResponse (code = 412, message = "Precondition is not acceptable. For instance, the last manager membership could not be removed."),
    @ApiResponse (code = 500, message = "Internal server error due to data encoding") })
  public Response deleteSpaceMembershipById(@Context UriInfo uriInfo,
                                            @ApiParam(value = "Space membership id which is in format spaceName:userName:role, ex: my_space:root:manager", required = true) @PathParam("id") String id,
                                            @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    String[] idParams = RestUtils.getPathParam(uriInfo, "id").split(":");
    if (idParams.length != 3) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String targetUser = idParams[1];
    if (identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, targetUser, true) == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String spacePrettyName = idParams[0];
    String spaceGroupId = SPACE_PREFIX + spacePrettyName;
    Space space = spaceService.getSpaceByGroupId(spaceGroupId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (! authenticatedUser.equals(targetUser) && ! spaceService.isSuperManager(authenticatedUser) && ! spaceService.isManager(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    if (spaceService.isOnlyManager(space, targetUser)) {
      throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
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
                                                                                          expand);    
    return EntityBuilder.getResponse(membershipEntity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  private List<DataEntity> getSpaceMemberships(List<Space> spaces, String userId, String path, String expand) {
    List<DataEntity> spaceMemberships = new ArrayList<DataEntity>();
    SpaceMembershipEntity membershipEntity = null;
    for (Space space : spaces) {
      if (userId != null) {
        if (ArrayUtils.contains(space.getMembers(), userId)) {
          membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, "member", path, expand);
          spaceMemberships.add(membershipEntity.getDataEntity());
        }
        if (ArrayUtils.contains(space.getManagers(), userId)) {
          membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, "manager", path, expand);
          spaceMemberships.add(membershipEntity.getDataEntity());
        }
        if (ArrayUtils.contains(space.getInvitedUsers(), userId)) {
          membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, "invited", path, expand);
          spaceMemberships.add(membershipEntity.getDataEntity());
        } 
        if (ArrayUtils.contains(space.getPendingUsers(), userId)) {
          membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, userId, "pending", path, expand);
          spaceMemberships.add(membershipEntity.getDataEntity());
        }
      } else {
        for (String user : space.getMembers()) {
          membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, user, "member", path, expand);
          spaceMemberships.add(membershipEntity.getDataEntity());
        }
        for (String user : space.getManagers()) {
          membershipEntity = EntityBuilder.buildEntityFromSpaceMembership(space, user, "manager", path, expand);
          spaceMemberships.add(membershipEntity.getDataEntity());
        }
      }
    }
    return spaceMemberships;
  }
}
