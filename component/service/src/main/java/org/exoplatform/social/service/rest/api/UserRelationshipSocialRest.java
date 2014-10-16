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

import org.exoplatform.social.service.rest.api.SocialRest;

public interface UserRelationshipSocialRest extends SocialRest {

  /**
   * @param uriInfo
   * @param status
   * @param user
   * @return
   * @throws Exception
   */
  @GET
  public abstract Response getUsersRelationships(@Context UriInfo uriInfo,
                                                   @QueryParam("status") String status,
                                                   @QueryParam("user") String user) throws Exception;

  /**
   * @param uriInfo
   * @param status
   * @param user
   * @return
   * @throws Exception
   */
  @POST
  public abstract Response createUsersRelationships(@Context UriInfo uriInfo,
                                                      @QueryParam("status") String status,
                                                      @QueryParam("user") String user) throws Exception;

  /**
   * Get a relationship by id
   * 
   * @param uriInfo
   * @param id
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public abstract Response getUsersRelationshipsById(@Context UriInfo uriInfo,
                                                       @PathParam("id") String id) throws Exception;

  /**
   * Process to update a relationship by id
   * 
   * @param uriInfo
   * @param id
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}")
  public abstract Response updateUsersRelationshipsById(@Context UriInfo uriInfo,
                                                          @PathParam("id") String id) throws Exception;

  /**
   * Process to delete a relationship by id
   * 
   * @param uriInfo
   * @param id
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}")
  public abstract Response deleteUsersRelationshipsById(@Context UriInfo uriInfo,
                                                          @PathParam("id") String id) throws Exception;

}