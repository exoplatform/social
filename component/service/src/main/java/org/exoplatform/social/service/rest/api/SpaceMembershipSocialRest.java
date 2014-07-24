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

public interface SpaceMembershipSocialRest extends SocialRest {

  /**
   * Process to return a list of space's membership in json format
   * 
   * @param uriInfo
   * @param status
   * @param user
   * @param space
   * @param returnSize
   * @param offset
   * @param limit
   * @return
   * @throws Exception
   */
  @GET
  public abstract Response getSpacesMemberships(@Context UriInfo uriInfo,
      @QueryParam("status") String status, @QueryParam("user") String user,
      @QueryParam("space") String space,
      @QueryParam("returnSize") boolean returnSize,
      @QueryParam("offset") int offset, @QueryParam("limit") int limit)
      throws Exception;

  @POST
  public abstract Response addSpacesMemberships(@Context UriInfo uriInfo,
      @QueryParam("user") String user, @QueryParam("space") String space)
      throws Exception;

  /**
   * Process to return a spaceMembership by id
   * 
   * @param uriInfo
   * @param id membership id
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/{spacesPrefix}/{spacePrettyName}")
  public abstract Response getSpaceMembershipById(@Context UriInfo uriInfo,
      @PathParam("id") String id,
      @PathParam("spacesPrefix") String spacesPrefix,
      @PathParam("spacePrettyName") String spacePrettyName) throws Exception;

  /**
   * Process to update a spaceMembership by id
   * 
   * @param uriInfo
   * @param id membership id
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}/{spacesPrefix}/{spacePrettyName}")
  public abstract Response updateSpaceMembershipById(@Context UriInfo uriInfo,
      @PathParam("id") String id,
      @PathParam("spacesPrefix") String spacesPrefix,
      @PathParam("spacePrettyName") String spacePrettyName,
      @QueryParam("type") String type) throws Exception;

  /**
   * Process to delete a spaceMembership by id
   * 
   * @param uriInfo
   * @param id membership id
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}/{spacesPrefix}/{spacePrettyName}")
  public abstract Response deleteSpaceMembershipById(@Context UriInfo uriInfo,
      @PathParam("id") String id,
      @PathParam("spacesPrefix") String spacesPrefix,
      @PathParam("spacePrettyName") String spacePrettyName) throws Exception;

}