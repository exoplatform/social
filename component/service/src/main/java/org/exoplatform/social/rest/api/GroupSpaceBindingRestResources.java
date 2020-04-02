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

package org.exoplatform.social.rest.api;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.ApiParam;

/**
 * Provides REST/JSON API to manage the binding between a space and an
 * organization group.
 */

public interface GroupSpaceBindingRestResources extends SocialRest {

  /**
   * Return a list of binding in json format
   *
   * @param uriInfo
   * @param spaceId Id of the space
   * @param offset Bindings list offset
   * @param limit Bindings list limit
   * @param returnSize Return Size of the list?
   * @return List of binding object for this context (space + role)
   * @throws Exception
   */
  @GET
  Response getBindingsBySpaceId(@Context UriInfo uriInfo,
                                @QueryParam("spaceId") String spaceId,
                                @QueryParam("offset") int offset,
                                @QueryParam("limit") int limit,
                                @QueryParam("returnSize") boolean returnSize) throws Exception;

  /**
   * Return a list of binding in json format
   *
   * @param uriInfo
   * @param spaceId Id of the space
   * @param groupNamesList List of group names to be bound to the space
   * @return Status
   * @throws Exception
   */
  @POST
  Response saveGroupSpaceBindings(@Context UriInfo uriInfo,
                                  @PathParam("spaceId") String spaceId,
                                  List<String> groupNamesList) throws Exception;

  /**
   * Delete a binding by id.
   *
   * @param uriInfo
   * @param bindingId Id of the space
   * @return Status
   * @throws Exception
   */
  @DELETE
  Response deleteSpaceBinding(@Context UriInfo uriInfo, @PathParam("bindingId") String bindingId) throws Exception;

  /**
   * Return the groups in a tree structure of json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  Response getGroupsTree(@Context UriInfo uriInfo) throws Exception;

  /**
   * @param uriInfo
   * @param spaceId
   * @param action
   * @param group
   * @param groupBindingId
   * @return
   * @throws Exception
   */
  @GET
  Response getReport(@Context UriInfo uriInfo,
                     @ApiParam(value = "spaceId", required = true) @QueryParam("spaceId") String spaceId,
                     @ApiParam(value = "action", required = true) @QueryParam("action") String action,
                     @ApiParam(value = "group", required = true) @QueryParam("group") String group,
                     @ApiParam(value = "groupBindingId") @QueryParam("groupBindingId") String groupBindingId) throws Exception;

  @GET
  Response getBindingReportOperations(@Context UriInfo uriInfo) throws Exception;
}
