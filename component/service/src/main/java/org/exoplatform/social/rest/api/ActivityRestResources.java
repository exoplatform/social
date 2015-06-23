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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.rest.entity.DataEntity;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

public interface ActivityRestResources extends SocialRest {

  /**
   * Process to return all activities in json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  public Response getActivitiesOfCurrentUser(@Context UriInfo uriInfo, 
                                             @QueryParam("offset") int offset,
                                             @QueryParam("limit") int limit,
                                             @QueryParam("returnSize") boolean returnSize,
                                             @QueryParam("expand") String expand) throws Exception;

  /**
   * Process to return an activity by id in json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public Response getActivityById(@Context UriInfo uriInfo,
                                  @PathParam("id") String id,
                                  @QueryParam("expand") String expand) throws Exception;

  /**
   * Process to update the title of an activity by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}")
  public Response updateActivityById(@Context UriInfo uriInfo, 
                                     @PathParam("id") String id,
                                     @QueryParam("expand") String expand,
                                     ActivityEntity model) throws Exception;

  /**
   * Process to delete an activity by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}")
  public Response deleteActivityById(@Context UriInfo uriInfo, 
                                     @PathParam("id") String id,
                                     @QueryParam("expand") String expand) throws Exception;

  /**
   * Process to return all comments of an activity in json format
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/comments")
  public Response getCommentsOfActivity(@Context UriInfo uriInfo,
                                        @PathParam("id") String id,
                                        @QueryParam("offset") int offset,
                                        @QueryParam("limit") int limit,
                                        @QueryParam("expand") String expand) throws Exception;

  /**
   * Process to create new comment
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @POST
  @Path("{id}/comments")
  public Response postComment(@Context UriInfo uriInfo, 
                              @PathParam("id") String id,
                              @QueryParam("expand") String expand,
                              CommentEntity model) throws Exception;
  /**
   * Gets all the likes of the activity with the given id.
   * 
   * @param uriInfo
   * @param id
   * @param offset
   * @param limit
   * @param expand
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/likes")
  public Response getLikesOfActivity(@Context UriInfo uriInfo,
                                     @PathParam("id") String id,
                                     @QueryParam("offset") int offset,
                                     @QueryParam("limit") int limit,
                                     @QueryParam("expand") String expand) throws Exception;

  /**
   * Adds a like for the activity with the given id.
   * 
   * @param uriInfo
   * @param id
   * @param expand
   * @return
   * @throws Exception
   */
  @POST
  @Path("{id}/likes")
  @Produces(MediaType.APPLICATION_JSON)
  public Response addLike(@Context UriInfo uriInfo,
                          @PathParam("id") String id,
                          @QueryParam("expand") String expand) throws Exception;
  /**
   * Gets the like of the user with the given username for the activity with the given activity id.
   * 
   * @param uriInfo
   * @param id
   * @param username
   * @param expand
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/likes/{username}")
  public Response getLikesActivityOfUser(@Context UriInfo uriInfo,
                                         @PathParam("id") String id,
                                         @PathParam("username") String username,
                                         @QueryParam("expand") String expand) throws Exception;
  /**
   * Deletes a like from the user with the given username on the activity with the given activity id.
   * 
   * @param uriInfo
   * @param id
   * @param username
   * @param expand
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}/likes/{username}")
  public Response deleteActivityById(@Context UriInfo uriInfo,
                                     @PathParam("id") String id,
                                     @PathParam("username") String username,
                                     @QueryParam("expand") String expand) throws Exception;
}
