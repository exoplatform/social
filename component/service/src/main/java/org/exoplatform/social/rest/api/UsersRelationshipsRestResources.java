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

import org.exoplatform.social.rest.entity.RelationshipEntity;

public interface UsersRelationshipsRestResources extends SocialRest {

  /**
   * Get relationships of an user
   *
   * @param uriInfo
   * @param status Specific status of relationships: pending, confirmed or all
   * @param user User name to get relationships
   * @param others Usernames of the others users to get relationships with the given user
   * @param offset Offset
   * @param limit Limit
   * @param returnSize Returning the number of relationships or not
   * @param expand Asking for a full representation of a specific subresource, ex: sender or receiver
   * @return The relationships of the given user
   * @throws Exception
   */
  @GET
  public abstract Response getUsersRelationships(@Context UriInfo uriInfo,
                                                 @QueryParam("status") String status,
                                                 @QueryParam("user") String user,
                                                 @QueryParam("others") String others,
                                                 @QueryParam("offset") int offset,
                                                 @QueryParam("limit") int limit,
                                                 @QueryParam("returnSize") boolean returnSize,
                                                 @QueryParam("expand") String expand) throws Exception;

  /**
   * Create a relationship between 2 users
   *
   * @param uriInfo
   * @param expand Asking for a full representation of a specific subresource, ex: sender or receiver
   * @param model Relationship entity to create
   * @return
   * @throws Exception
   */
  @POST
  public abstract Response createUsersRelationships(@Context UriInfo uriInfo,
                                                    @QueryParam("expand") String expand,
                                                    RelationshipEntity model) throws Exception;

  /**
   * Get a relationship by id
   * 
   * @param uriInfo
   * @param id Id of the relationship
   * @param expand Asking for a full representation of a specific subresource, ex: sender or receiver
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public abstract Response getUsersRelationshipsById(@Context UriInfo uriInfo,
                                                     @PathParam("id") String id,
                                                     @QueryParam("expand") String expand) throws Exception;

  /**
   * Update a relationship by id
   * 
   * @param uriInfo
   * @param id Id of the relationship to update
   * @param expand Asking for a full representation of a specific subresource, ex: sender or receiver
   * @param model Relationship entity to create
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}")
  public abstract Response updateUsersRelationshipsById(@Context UriInfo uriInfo, 
                                                        @PathParam("id") String id,
                                                        @QueryParam("expand") String expand,
                                                        RelationshipEntity model) throws Exception;

  /**
   * Delete a relationship by id
   * 
   * @param uriInfo
   * @param id Id of the relationship to delete
   * @param expand Asking for a full representation of a specific subresource, ex: sender or receiver
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}")
  public abstract Response deleteUsersRelationshipsById(@Context UriInfo uriInfo,
                                                        @PathParam("id") String id,
                                                        @QueryParam("expand") String expand) throws Exception;

}