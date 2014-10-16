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

public interface SpaceSocialRest extends SocialRest {

  /**
   * Process to return a list of space in json format
   * 
   * @param uriInfo
   * @param q
   * @return
   * @throws Exception
   */
  @GET
  public abstract Response getSpaces(@Context UriInfo uriInfo,
                                       @QueryParam("q") String q) throws Exception;

  /**
   * Process to create a new space
   * 
   * @param uriInfo
   * @param displayName
   * @param description
   * @param visibility
   * @param registration
   * @return
   * @throws Exception
   */
  @POST
  public abstract Response createSpace(@Context UriInfo uriInfo,
                                         @QueryParam("displayName") String displayName,
                                         @QueryParam("description") String description,
                                         @QueryParam("visibility") String visibility,
                                         @QueryParam("registration") String registration) throws Exception;

  /**
   * Process to return a space by id
   * 
   * @param uriInfo
   * @param id
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public abstract Response getSpaceById(@Context UriInfo uriInfo,
                                          @PathParam("id") String id) throws Exception;

  /**
   * Process to update a space by id
   * 
   * @param uriInfo
   * @param id
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}")
  public abstract Response updateSpaceById(@Context UriInfo uriInfo,
                                             @PathParam("id") String id,
                                             @QueryParam("displayName") String displayName,
                                             @QueryParam("description") String description,
                                             @QueryParam("visibility") String visibility,
                                             @QueryParam("registration") String registration) throws Exception;

  /**
   * Process to delete a space by id
   * 
   * @param uriInfo
   * @param id space'id
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}")
  public abstract Response deleteSpaceById(@Context UriInfo uriInfo,
                                             @PathParam("id") String id) throws Exception;

  /**
   * Process to return a space by id
   * 
   * @param uriInfo
   * @param id
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/users")
  public abstract Response getSpaceMembers(@Context UriInfo uriInfo,
                                             @PathParam("id") String id,
                                             @QueryParam("role") String role)
      throws Exception;

  /**
   * Process to return a space by id
   * 
   * @param uriInfo
   * @param id
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/activities")
  public abstract Response getSpaceActivitiesById(@Context UriInfo uriInfo,
                                                    @PathParam("id") String id,
                                                    @QueryParam("after") Long after,
                                                    @QueryParam("before") Long before) throws Exception;

  @POST
  @Path("{id}/activities")
  public abstract Response postActivityOnSpace(@Context UriInfo uriInfo,
                                                 @PathParam("id") String id,
                                                 @QueryParam("text") String text) throws Exception;

}