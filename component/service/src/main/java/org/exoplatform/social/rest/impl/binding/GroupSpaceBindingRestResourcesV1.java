/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

package org.exoplatform.social.rest.impl.binding;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.MediaType;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.GroupSpaceBindingRestResources;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.annotations.*;

/**
 * {@link org.exoplatform.social.rest.api.GroupSpaceBindingRestResources}
 * implementation.
 */

@Path(VersionResources.VERSION_ONE + "/social/spaceGroupBindings")
@Api(tags = VersionResources.VERSION_ONE + "/social/groupSpaceBindings", value = VersionResources.VERSION_ONE
    + "/social/groupSpaceBindings", description = "API  to manage the binding between a space and an organization group")
public class GroupSpaceBindingRestResourcesV1 implements GroupSpaceBindingRestResources {

  private GroupSpaceBindingService groupSpaceBindingService;

  private UserACL userACL;

  public GroupSpaceBindingRestResourcesV1(GroupSpaceBindingService groupSpaceBindingService, UserACL userACL) {
    this.groupSpaceBindingService = groupSpaceBindingService;
    this.userACL = userACL;
  }

  /**
   * {@inheritDoc}
   */
  @GET
  @Path("getSpaceBindings/{spaceId}")
  @RolesAllowed("administrators")
  @ApiOperation(value = "Gets list of binding for a space.", httpMethod = "GET", response = Response.class, notes = "Returns a list of bindings in the following cases if the authenticated user is a member of space administrator group.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 400, message = "Invalid query input") })
  public Response getBindingsBySpaceId(@Context UriInfo uriInfo,
                                       @ApiParam(value = "Space id", required = true) @PathParam("spaceId") String spaceId,
                                       @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                       @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                       @ApiParam(value = "Returning the number of spaces found or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize) throws Exception {

    if(!userACL.isSuperUser() && !userACL.isUserInGroup(userACL.getAdminGroups())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    List<GroupSpaceBinding> spaceBindings;

    spaceBindings = groupSpaceBindingService.findGroupSpaceBindingsBySpace(spaceId);

    List<DataEntity> bindingEntities = new ArrayList<>();

    for (GroupSpaceBinding binding : spaceBindings) {
      GroupSpaceBindingEntity bindingEntity = EntityBuilder.buildEntityFromGroupSpaceBinding(binding);
      bindingEntities.add(bindingEntity.getDataEntity());
    }

    CollectionEntity collectionBinding = new CollectionEntity(bindingEntities, EntityBuilder.GROUP_SPACE_BINDING_TYPE, offset, limit);
    if (returnSize) {
      collectionBinding.setSize(bindingEntities.size());
    }

    return EntityBuilder.getResponse(collectionBinding, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  /**
   * {@inheritDoc}
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("saveGroupsSpaceBindings/{spaceId}")
  @RolesAllowed("administrators")
  @ApiOperation(value = "Save space group bindings", httpMethod = "POST", response = Response.class, notes = "This method update bindings for a specific space if the authenticated user is a spaces super manager")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error due to data encoding") })
  public Response saveGroupSpaceBindings(@Context UriInfo uriInfo,
                                         @ApiParam(value = "SpaceId of the space", required = true) @PathParam("spaceId") String spaceId,
                                         @ApiParam(value = "List of group names to be bound to the space", required = true) List<String> groupNames) {
    if (!userACL.isSuperUser() && !userACL.isUserInGroup(userACL.getAdminGroups())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    if (groupNames == null || groupNames.isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    List<GroupSpaceBinding> groupSpaceBindings = new ArrayList<>();
    groupNames.stream().forEach(groupName -> groupSpaceBindings.add(new GroupSpaceBinding(spaceId, groupName)));

    groupSpaceBindingService.saveGroupSpaceBindings(groupSpaceBindings);

    return Response.ok().build();
  }

  /**
   * {@inheritDoc}
   */
  @DELETE
  @Path("{spaceId}/{spaceRole}")
  @RolesAllowed("administrators")
  @ApiOperation(value = "Deletes all the  binding by space/space role", httpMethod = "DELETE", response = Response.class, notes = "This method delete all the bindings in the following cases the authenticated user is a spaces super manager")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 400, message = "Invalid query input") })
  public Response deleteSpaceBindings(@Context UriInfo uriInfo,
                                      @ApiParam(value = "spaceId", required = true) @PathParam("spaceId") String spaceId) throws Exception {

    if(!userACL.isSuperUser() && !userACL.isUserInGroup(userACL.getAdminGroups())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    groupSpaceBindingService.deleteAllSpaceBindingsBySpace(spaceId);

    return Response.ok().build();
  }

}
