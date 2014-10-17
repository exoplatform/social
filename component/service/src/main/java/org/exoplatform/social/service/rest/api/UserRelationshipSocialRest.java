package org.exoplatform.social.service.rest.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.social.service.rest.api.SocialRest;

public interface UserRelationshipSocialRest extends SocialRest {

  /**
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  public abstract Response getUsersRelationships(@Context UriInfo uriInfo) throws Exception;

  /**
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @POST
  public abstract Response createUsersRelationships(@Context UriInfo uriInfo) throws Exception;

  /**
   * Get a relationship by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public abstract Response getUsersRelationshipsById(@Context UriInfo uriInfo) throws Exception;

  /**
   * Process to update a relationship by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}")
  public abstract Response updateUsersRelationshipsById(@Context UriInfo uriInfo) throws Exception;

  /**
   * Process to delete a relationship by id
   * 
   * @param uriInfo
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}")
  public abstract Response deleteUsersRelationshipsById(@Context UriInfo uriInfo) throws Exception;

}