package org.exoplatform.social.service.rest.api;

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

public interface UserSocialRest extends SocialRest {

  /**
   * Get all users, filter by name if exists.
   * 
   * @param q value that an user's name match
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/social/notifications/inviteToConnect/john/root
   * @return List of users in json format.
   * @throws Exception
   */
  @GET
  public abstract Response getUsers(@Context UriInfo uriInfo,
                                      @QueryParam("q") String q) throws Exception;

  /**
   * Creates an user
   * 
   * @param uriInfo
   * @param userName
   * @param firstName 
   * @param lastName
   * @param email
   * @return user created in json format
   * @throws Exception
   */
  @POST
  public abstract Response addUser(@Context UriInfo uriInfo,
                                     @QueryParam("userName") String userName,
                                     @QueryParam("firstName") String firstName,
                                     @QueryParam("lastName") String lastName,
                                     @QueryParam("password") String password,
                                     @QueryParam("email") String email) throws Exception;

  @GET
  @Path("{id}")
  public abstract Response getUserById(@Context UriInfo uriInfo,
                                         @PathParam("id") String id) throws Exception;

  @DELETE
  @Path("{id}")
  public abstract Response deleteUserById(@Context UriInfo uriInfo,
                                            @PathParam("id") String id) throws Exception;

  @PUT
  @Path("{id}")
  public abstract Response updateUserById(@Context UriInfo uriInfo,
                                            @PathParam("id") String id) throws Exception;

  @GET
  @Path("{id}/connections")
  public abstract Response getConnectionOfUser(@Context UriInfo uriInfo,
                                                 @PathParam("id") String id) throws Exception;

  @GET
  @Path("{id}/spaces")
  public abstract Response getSpacesOfUser(@Context UriInfo uriInfo,
                                             @PathParam("id") String id) throws Exception;

  @GET
  @Path("{id}/activities")
  public abstract Response getActivitiesOfUser(@Context UriInfo uriInfo,
                                                 @PathParam("id") String id,
                                                 @QueryParam("type") String type,
                                                 @QueryParam("after") Long after,
                                                 @QueryParam("before") Long before) throws Exception;

  @POST
  @Path("{id}/activities")
  public abstract Response addActivityByUser(@Context UriInfo uriInfo,
                                               @PathParam("id") String id,
                                               @QueryParam("text") String text) throws Exception;

}