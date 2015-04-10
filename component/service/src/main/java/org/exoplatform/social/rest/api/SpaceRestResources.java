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

import org.exoplatform.social.rest.entity.SpaceEntity;
import org.exoplatform.social.service.rest.api.models.ActivityRestIn;

public interface SpaceRestResources extends SocialRest {

  /**
   * Process to return a list of space in json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  public abstract Response getSpaces(@Context UriInfo uriInfo) throws Exception;

  /**
   * Process to create a new space
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @POST
  public abstract Response createSpace(@Context UriInfo uriInfo, SpaceEntity model) throws Exception;

  /**
   * Process to return a space by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public abstract Response getSpaceById(@Context UriInfo uriInfo) throws Exception;

  /**
   * Process to update a space by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}")
  public abstract Response updateSpaceById(@Context UriInfo uriInfo, SpaceEntity model) throws Exception;

  /**
   * Process to delete a space by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}")
  public abstract Response deleteSpaceById(@Context UriInfo uriInfo) throws Exception;

  /**
   * Process to return a space by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/users")
  public abstract Response getSpaceMembers(@Context UriInfo uriInfo) throws Exception;

  /**
   * Process to return a space by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/activities")
  public abstract Response getSpaceActivitiesById(@Context UriInfo uriInfo) throws Exception;

  @POST
  @Path("{id}/activities")
  public abstract Response postActivityOnSpace(@Context UriInfo uriInfo, ActivityRestIn model) throws Exception;

}