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
import org.exoplatform.social.rest.entity.CommentEntity;

public interface ActivityRestResources extends SocialRest {

  /**
   * Process to return all activities in json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  public Response getActivitiesOfCurrentUser(@Context UriInfo uriInfo) throws Exception;

  /**
   * Process to return an activity by id in json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public Response getActivityById(@Context UriInfo uriInfo) throws Exception;

  /**
   * Process to update the title of an activity by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}")
  public Response updateActivityById(@Context UriInfo uriInfo, ActivityEntity model) throws Exception;

  /**
   * Process to delete an activity by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}")
  public Response deleteActivityById(@Context UriInfo uriInfo) throws Exception;

  /**
   * Process to return all comments of an activity in json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/comments")
  public Response getCommentsOfActivity(@Context UriInfo uriInfo) throws Exception;

  /**
   * Process to create new comment
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @POST
  @Path("{id}/comments")
  public Response postComment(@Context UriInfo uriInfo, CommentEntity model) throws Exception;

}
