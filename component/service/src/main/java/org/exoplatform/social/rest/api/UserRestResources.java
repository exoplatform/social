/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/

package org.exoplatform.social.rest.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.UserEntity;
import org.exoplatform.social.rest.impl.user.UserRestResourcesV1.ACTIVITY_STREAM_TYPE;
import org.exoplatform.social.service.rest.api.models.ActivityRestIn;

public interface UserRestResources extends SocialRest {

  /**
   * Get all users, filter by name if exists.
   *
   * @param q value that an user's name match
   * @param status filter only online users
   * @param spaceId filter only space members
   * @return List of users in json format.
   * @throws Exception
   */
  @GET
  public abstract Response getUsers(@Context UriInfo uriInfo,
                                    @QueryParam("q") String q,
                                    @QueryParam("status") String status,
                                    @QueryParam("spaceId") String spaceId,
                                    @QueryParam("offset") int offset,
                                    @QueryParam("limit") int limit,
                                    @QueryParam("returnSize") boolean returnSize,
                                    @QueryParam("expand") String expand) throws Exception;

  /**
   * Creates an user
   * 
   * @param uriInfo
   * @return user created in json format
   * @throws Exception
   */
  @POST
  public abstract Response addUser(@Context UriInfo uriInfo, 
                                   @QueryParam("expand") String expand,
                                   UserEntity model) throws Exception;

  @GET
  @Path("{id}")
  public abstract Response getUserById(@Context UriInfo uriInfo,
                                       @PathParam("id") String id,
                                       @QueryParam("expand") String expand) throws Exception;

  @DELETE
  @Path("{id}")
  public abstract Response deleteUserById(@Context UriInfo uriInfo,
                                          @PathParam("id") String id,
                                          @QueryParam("expand") String expand) throws Exception;

  @PUT
  @Path("{id}")
  public abstract Response updateUserById(@Context UriInfo uriInfo,
                                                   @PathParam("id") String id,
                                                   @QueryParam("expand") String expand, 
                                                   UserEntity model) throws Exception;

  @GET
  @Path("{id}/connections")
  public abstract Response getConnectionOfUser(@Context UriInfo uriInfo,
                                               @PathParam("id") String id,
                                               @QueryParam("returnSize") boolean returnSize,
                                               @QueryParam("expand") String expand) throws Exception;

  @GET
  @Path("{id}/spaces")
  public abstract Response getSpacesOfUser(@Context UriInfo uriInfo,
                                           @PathParam("id") String id,
                                           @QueryParam("offset") int offset,
                                           @QueryParam("limit") int limit,
                                           @QueryParam("returnSize") boolean returnSize,
                                           @QueryParam("expand") String expand) throws Exception;

  @GET
  @Path("{id}/activities")
  public abstract Response getActivitiesOfUser(@Context UriInfo uriInfo,
                                               @PathParam("id") String id,
                                               @QueryParam("type") String type,
                                               @QueryParam("offset") int offset,
                                               @QueryParam("limit") int limit,
                                               @QueryParam("before") String before,
                                               @QueryParam("after") String after,
                                               @QueryParam("returnSize") boolean returnSize,
                                               @QueryParam("expand") String expand) throws Exception;

  @POST
  @Path("{id}/activities")
  public abstract Response addActivityByUser(@Context UriInfo uriInfo, 
                                             @PathParam("id") String id,
                                             @QueryParam("expand") String expand, 
                                             ActivityRestIn model) throws Exception;

}