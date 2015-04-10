package org.exoplatform.social.rest.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.social.rest.entity.SpaceMembershipRestIn;

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
  public abstract Response addSpacesMemberships(@Context UriInfo uriInfo,
                                                  SpaceMembershipRestIn model) throws Exception;

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
  public abstract Response updateSpaceMembershipById(@Context UriInfo uriInfo,
                                                       SpaceMembershipRestIn model) throws Exception;

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