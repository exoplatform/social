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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.UserEntity;

public interface UserRestResources extends SocialRest {

  /**
   * Get all users, filter by name if exists.
   * 
   * @param q value that an user's name match
   * @authentication
   * @request GET:
   *          http://localhost:8080/rest/social/notifications/inviteToConnect
   *          /john/root
   * @return List of users in json format.
   * @throws Exception
   */
  @GET
  public abstract Response getUsers(@Context UriInfo uriInfo) throws Exception;

  /**
   * Creates an user
   * 
   * @param uriInfo
   * @return user created in json format
   * @throws Exception
   */
  @POST
  public abstract Response addUser(@Context UriInfo uriInfo, UserEntity model) throws Exception;

  @GET
  @Path("{id}")
  public abstract Response getUserById(@Context UriInfo uriInfo) throws Exception;

  @DELETE
  @Path("{id}")
  public abstract Response deleteUserById(@Context UriInfo uriInfo) throws Exception;

  @PUT
  @Path("{id}")
  public abstract Response updateUserById(@Context UriInfo uriInfo, UserEntity model) throws Exception;

  @GET
  @Path("{id}/connections")
  public abstract Response getConnectionOfUser(@Context UriInfo uriInfo) throws Exception;

  @GET
  @Path("{id}/spaces")
  public abstract Response getSpacesOfUser(@Context UriInfo uriInfo) throws Exception;

  @GET
  @Path("{id}/activities")
  public abstract Response getActivitiesOfUser(@Context UriInfo uriInfo) throws Exception;

  @POST
  @Path("{id}/activities")
  public abstract Response addActivityByUser(@Context UriInfo uriInfo, ActivityEntity model) throws Exception;

}