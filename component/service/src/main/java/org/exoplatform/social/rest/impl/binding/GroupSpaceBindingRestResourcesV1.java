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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.binding.model.GroupNode;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingQueue;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.GroupSpaceBindingRestResources;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.GroupNodeEntity;
import org.exoplatform.social.rest.entity.GroupSpaceBindingEntity;
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

  private UserACL                  userACL;

  public GroupSpaceBindingRestResourcesV1(GroupSpaceBindingService groupSpaceBindingService, UserACL userACL) {
    this.groupSpaceBindingService = groupSpaceBindingService;
    this.userACL = userACL;
  }

  /**
   * {@inheritDoc}
   */
  @GET
  @RolesAllowed("administrators")
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{spaceId}")
  @ApiOperation(value = "Gets list of binding for a space.", httpMethod = "GET", response = Response.class, notes = "Returns a list of bindings in the following cases if the authenticated user is an administrator.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 400, message = "Invalid query input") })
  public Response getBindingsBySpaceId(@Context UriInfo uriInfo,
                                       @ApiParam(value = "Space id", required = true) @PathParam("spaceId") String spaceId,
                                       @ApiParam(value = "Offset", defaultValue = "0") @QueryParam("offset") int offset,
                                       @ApiParam(value = "Limit", defaultValue = "10") @QueryParam("limit") int limit,
                                       @ApiParam(value = "Returning the number of spaces found or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize) throws Exception {

    if (!userACL.isSuperUser() && !userACL.isUserInGroup(userACL.getAdminGroups())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    // Retrieve all removed bindings ids.
    List<Long> removedSpaceBindingsIds =
                                       groupSpaceBindingService.getGroupSpaceBindingsFromQueueByAction(GroupSpaceBindingQueue.ACTION_REMOVE)
                                                               .stream()
                                                               .map(groupSpaceBinding -> groupSpaceBinding.getId())
                                                               .collect(Collectors.toList());

    List<GroupSpaceBinding> spaceBindings = groupSpaceBindingService.findGroupSpaceBindingsBySpace(spaceId);

    // Get rid of removed bindings.
    if (removedSpaceBindingsIds.size() > 0 && spaceBindings.size() > 0) {
      spaceBindings.removeIf(spaceBinding -> removedSpaceBindingsIds.contains(Long.valueOf(spaceBinding.getId())));
    }

    if (spaceBindings.size() == 0) {
      return EntityBuilder.getResponse(new CollectionEntity(new ArrayList<>(),
                                                            EntityBuilder.GROUP_SPACE_BINDING_TYPE,
                                                            offset,
                                                            limit),
                                       uriInfo,
                                       RestUtils.getJsonMediaType(),
                                       Response.Status.OK);
    }

    List<DataEntity> bindingEntities = new ArrayList<>();

    for (GroupSpaceBinding binding : spaceBindings) {
      GroupSpaceBindingEntity bindingEntity = EntityBuilder.buildEntityFromGroupSpaceBinding(binding);
      bindingEntities.add(bindingEntity.getDataEntity());
    }

    CollectionEntity collectionBinding = new CollectionEntity(bindingEntities,
                                                              EntityBuilder.GROUP_SPACE_BINDING_TYPE,
                                                              offset,
                                                              limit);
    if (returnSize) {
      collectionBinding.setSize(bindingEntities.size());
    }

    return EntityBuilder.getResponse(collectionBinding, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  /**
   * {@inheritDoc}
   */
  @POST
  @RolesAllowed("administrators")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("saveGroupsSpaceBindings/{spaceId}")
  @ApiOperation(value = "Save space group bindings", httpMethod = "POST", response = Response.class, notes = "This method set bindings for a specific space with a list of groups if the authenticated user is an administrator.")
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
    // Get already bound groups to the space.
    List<String> spaceBoundGroups = groupSpaceBindingService.findGroupSpaceBindingsBySpace(spaceId)
                                                            .stream()
                                                            .map(groupSpaceBinding -> groupSpaceBinding.getGroup())
                                                            .collect(Collectors.toList());
    // Get bound groups to the space that are already removed bindings.
    List<String> spaceRemovedBoundGroups =
                                         groupSpaceBindingService.getGroupSpaceBindingsFromQueueByAction(GroupSpaceBindingQueue.ACTION_REMOVE)
                                                                 .stream()
                                                                 .filter(groupSpaceBinding -> groupSpaceBinding.getSpaceId()
                                                                                                               .equals(spaceId))
                                                                 .map(groupSpaceBinding -> groupSpaceBinding.getGroup())
                                                                 .collect(Collectors.toList());
    // Get rid of only bound groups to the space that are not of removed bindings.
    spaceBoundGroups.removeAll(spaceRemovedBoundGroups);
    groupNames.removeAll(spaceBoundGroups);
    if (groupNames.size() == 0) {
      return Response.ok("Already bound!").build();
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
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  @Path("removeGroupSpaceBinding/{bindingId}")
  @ApiOperation(value = "Deletes a binding.", httpMethod = "DELETE", response = Response.class, notes = "This method deletes a binding in the following cases the authenticated user is an administrator.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 400, message = "Invalid query input") })
  public Response deleteSpaceBinding(@Context UriInfo uriInfo,
                                     @ApiParam(value = "spaceId", required = true) @PathParam("bindingId") String bindingId) throws Exception {

    if (!userACL.isSuperUser() && !userACL.isUserInGroup(userACL.getAdminGroups())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    GroupSpaceBinding binding;
    binding = groupSpaceBindingService.findGroupSpaceBindingById(bindingId);
    if (binding != null) {
      GroupSpaceBindingQueue bindingQueue = new GroupSpaceBindingQueue(binding, GroupSpaceBindingQueue.ACTION_REMOVE);
      groupSpaceBindingService.createGroupSpaceBindingQueue(bindingQueue);
    }
    return Response.ok().build();
  }

  @GET
  @RolesAllowed("administrators")
  @Produces(MediaType.APPLICATION_JSON)
  @Path("getGroupsTree")
  @ApiOperation(value = "Gets list of groups entities from the parent group root.", httpMethod = "GET", response = Response.class, notes = "Returns a list of group entities in the following cases if the authenticated user is an administrator.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 400, message = "Invalid query input") })
  public Response getGroupsTree(@Context UriInfo uriInfo) throws Exception {

    if (!userACL.isSuperUser() && !userACL.isUserInGroup(userACL.getAdminGroups())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    List<DataEntity> groupNodesDataEntities = buildGroupTree();

    CollectionEntity collectionBinding =
                                       new CollectionEntity(groupNodesDataEntities, EntityBuilder.ORGANIZATION_GROUP_TYPE, 0, 10);

    return EntityBuilder.getResponse(collectionBinding, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  public List<DataEntity> buildGroupTree() throws Exception {
    OrganizationService organizationService = CommonsUtils.getOrganizationService();
    Collection<Group> allGroups = organizationService.getGroupHandler().getAllGroups();
    // get rid of space groups
    List<Group> groups = allGroups.stream().filter(group -> !group.getId().startsWith("/spaces")).collect(Collectors.toList());

    // Find nodes with highest level
    int highestLevel = 0;
    for (Group group : groups) {
      int level = StringUtils.countMatches(group.getId(), "/");
      highestLevel = highestLevel > level ? highestLevel : level;
    }

    // Convert groups to group nodes.
    List<GroupNode> allGroupNodes = new ArrayList<>();
    groups.stream().forEach(group -> allGroupNodes.add(new GroupNode(group.getId(), group.getLabel(), group.getParentId())));

    // convert to group node entities
    List<GroupNodeEntity> allGroupNodesEntities = new ArrayList<>();
    for (GroupNode groupNode : allGroupNodes) {
      GroupNodeEntity groupNodeEntity = EntityBuilder.buildEntityFromGroupNode(groupNode);
      allGroupNodesEntities.add(groupNodeEntity);
    }

    // Get root child groups. entities
    List<GroupNodeEntity> rootChildrenEntities = allGroupNodesEntities.stream()
                                                                      .filter(child -> child.getParentId().equals("root"))
                                                                      .collect(Collectors.toList());
    allGroupNodesEntities.removeAll(rootChildrenEntities);

    for (int i = highestLevel; i > 1; i--) {

      // Get bottom group node entities
      List<GroupNodeEntity> bottomGroupNodesEntities = getBottomGroupEntities(allGroupNodesEntities, i);
      List<GroupNodeEntity> bottomEntities = new ArrayList<>();
      bottomEntities.addAll(bottomGroupNodesEntities);

      // Build from children entities.
      for (GroupNodeEntity groupNodeEntity : bottomGroupNodesEntities) {
        if (bottomEntities.contains(groupNodeEntity)) {
          GroupNodeEntity parentEntity = getParentEntityOf(groupNodeEntity, allGroupNodesEntities);
          // If parent is null then its a direct child of a rootChild.
          if (parentEntity != null) {
            allGroupNodesEntities.remove(parentEntity);
            List<GroupNodeEntity> childrenEntities = getChildrenEntitiesOf(parentEntity, allGroupNodesEntities);
            parentEntity.setChildGroupNodesEntities(convertToChildrenDataEntities(childrenEntities));
            // replaceParent
            allGroupNodesEntities.add(parentEntity);
            allGroupNodesEntities.removeAll(childrenEntities);
            bottomEntities.removeAll(childrenEntities);
          }
        }
      }
    }

    // Finally set rootChildren's children.
    List<GroupNodeEntity> rootChildGroupNodesEntities = new ArrayList<>();
    for (GroupNodeEntity rootChildEntity : rootChildrenEntities) {
      List<GroupNodeEntity> childrenEntities = getChildrenEntitiesOf(rootChildEntity, allGroupNodesEntities);
      rootChildEntity.setChildGroupNodesEntities(convertToChildrenDataEntities(childrenEntities));
      rootChildGroupNodesEntities.add(rootChildEntity);
    }

    // Return list of data entities
    List<DataEntity> groupNodesDataEntities = new ArrayList<>();
    for (GroupNodeEntity entity : rootChildGroupNodesEntities) {
      groupNodesDataEntities.add(entity.getDataEntity());
    }

    return groupNodesDataEntities;

  }

  private List<GroupNodeEntity> getBottomGroupEntities(List<GroupNodeEntity> allGroupNodesEntities, int highestLevel) {
    List<GroupNodeEntity> bottomGroupNodesEntities = new ArrayList<>();
    for (GroupNodeEntity groupNodeEntity : allGroupNodesEntities) {
      if (StringUtils.countMatches(groupNodeEntity.getId(), "/") == highestLevel) {
        bottomGroupNodesEntities.add(groupNodeEntity);
      }
    }
    return bottomGroupNodesEntities;
  }

  private GroupNodeEntity getParentEntityOf(GroupNodeEntity groupNodeEntity, List<GroupNodeEntity> groupNodesEntities) {
    GroupNodeEntity parentNodeEntity = null;
    for (GroupNodeEntity parentEntity : groupNodesEntities) {
      if (parentEntity.getId().equals(groupNodeEntity.getParentId())) {
        parentNodeEntity = parentEntity;
      }
    }
    return parentNodeEntity;
  }

  private List<GroupNodeEntity> getChildrenEntitiesOf(GroupNodeEntity groupNodeEntity, List<GroupNodeEntity> groupNodeEntities) {
    List<GroupNodeEntity> childrenEntities = new ArrayList<>();
    for (GroupNodeEntity childEntity : groupNodeEntities) {
      if (childEntity.getParentId().equals(groupNodeEntity.getId())) {
        childrenEntities.add(childEntity);
      }
    }
    return childrenEntities;
  }

  private List<DataEntity> convertToChildrenDataEntities(List<GroupNodeEntity> childrenEntities) {
    List<DataEntity> childrenDataEntities = new ArrayList<>();
    for (GroupNodeEntity childGroupNodeEntity : childrenEntities) {
      childrenDataEntities.add(childGroupNodeEntity.getDataEntity());
    }
    return childrenDataEntities;
  }

}
