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

import org.exoplatform.social.rest.entity.SpaceMembershipEntity;

public interface SpaceMembershipRestResources extends SocialRest {

  /**
   * Process to return a list of space's membership in json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  public abstract Response getSpacesMemberships(@Context UriInfo uriInfo) throws Exception;

  @POST
  public abstract Response addSpacesMemberships(@Context UriInfo uriInfo, SpaceMembershipEntity model) throws Exception;

  /**
   * Process to return a spaceMembership by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/{spacesPrefix}/{spacePrettyName}")
  public abstract Response getSpaceMembershipById(@Context UriInfo uriInfo) throws Exception;

  /**
   * Process to update a spaceMembership by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}/{spacesPrefix}/{spacePrettyName}")
  public abstract Response updateSpaceMembershipById(@Context UriInfo uriInfo, SpaceMembershipEntity model) throws Exception;

  /**
   * Process to delete a spaceMembership by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}/{spacesPrefix}/{spacePrettyName}")
  public abstract Response deleteSpaceMembershipById(@Context UriInfo uriInfo) throws Exception;

}