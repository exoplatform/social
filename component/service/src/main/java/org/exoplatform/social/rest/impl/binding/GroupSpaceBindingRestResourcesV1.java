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
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.GroupSpaceBindingRestResources;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.GroupSpaceBindingEntity;
import org.exoplatform.social.rest.entity.SpaceEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.annotations.*;

/**
 * {@link org.exoplatform.social.rest.api.GroupSpaceBindingRestResources}
 * implementation.
 */

@Path(VersionResources.VERSION_ONE + "/social/groupspacebindings")
@Api(tags = VersionResources.VERSION_ONE + "/social/groupspacebindings", value = VersionResources.VERSION_ONE
    + "/social/groupspacebinding", description = "API  to manage the binding between a space and an organization group")
public class GroupSpaceBindingRestResourcesV1 implements GroupSpaceBindingRestResources {

  /**
   * {@inheritDoc}
   */
  @RolesAllowed("administrators")
    @ApiOperation(value = "Gets list of binding for a space context (space + role in space)",
            httpMethod = "GET",
            response = Response.class,
            notes = "Returns a list of bindings in the following cases if the authenticated user is a member of space administrator group")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Request fulfilled"),
            @ApiResponse (code = 500, message = "Internal server error"),
            @ApiResponse (code = 400, message = "Invalid query input") })
    public Response getBindingsBySpaceContext(@Context UriInfo uriInfo,
                                              @ApiParam(value = "Space id", required = true) @QueryParam("spaceId") String spaceId,
                                              @ApiParam(value = "Role in space (member/manager", required = true) @QueryParam("spaceRole") String spaceRole,
                                              @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                              @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                              @ApiParam(value = "Returning the number of spaces found or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize) throws Exception {

        GroupSpaceBindingService groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);

        List<GroupSpaceBinding> list = null;

        list = groupSpaceBindingService.findSpaceBindings(spaceId,spaceRole);

        List<DataEntity> bindings = new ArrayList<DataEntity>();

        for(GroupSpaceBinding binding:list)
        {
            GroupSpaceBindingEntity bindingEntity = EntityBuilder.buildEntityFromGroupSpaceBinding(binding);
            bindings.add(bindingEntity.getDataEntity());
        }

        CollectionEntity collectionBinding = new CollectionEntity(bindings, EntityBuilder.GROUP_SPACE_BINDING_TYPE, offset, limit);
        if (returnSize) {
            collectionBinding.setSize( bindings.size());
        }

        return EntityBuilder.getResponse(collectionBinding, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
    }

}
