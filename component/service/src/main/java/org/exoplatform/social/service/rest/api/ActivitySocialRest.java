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

public interface ActivitySocialRest extends SocialRest {

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
   * @param id the id of activity
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}")
  public Response getActivityById(@Context UriInfo uriInfo,
                                   @PathParam("id") String id) throws Exception;
  
  /**
   * Process to update the title of an activity by id
   * 
   * @param uriInfo
   * @param id the id of activity
   * @param text the new title of activity
   * @return
   * @throws Exception
   */
  @PUT
  @Path("{id}")
  public Response updateActivityById(@Context UriInfo uriInfo,
                                      @PathParam("id") String id,
                                      @QueryParam("text") String text) throws Exception;
  
  /**
   * Process to delete an activity by id
   * 
   * @param uriInfo
   * @param id the id of activity
   * @return
   * @throws Exception
   */
  @DELETE
  @Path("{id}")
  public Response deleteActivityById(@Context UriInfo uriInfo,
                                      @PathParam("id") String id) throws Exception;
  
  /**
   * Process to return all comments of an activity in json format
   * 
   * @param uriInfo
   * @param id the id of activity
   * @param returnSize true if the response must contain the total size of all comments found
   * @param offset index of the first comment to return 
   * @param limit the maximum number of comments to return
   * @return
   * @throws Exception
   */
  @GET
  @Path("{id}/comments")
  public Response getCommentsOfActivity(@Context UriInfo uriInfo,
                                         @PathParam("id") String id,
                                         @QueryParam("returnSize") boolean returnSize,
                                         @QueryParam("offset") int offset,
                                         @QueryParam("limit") int limit) throws Exception;
  
  /**
   * Process to create new comment
   * 
   * @param uriInfo
   * @param id the id of activity
   * @param text the title of comment to add on activity
   * @return
   * @throws Exception
   */
  @POST
  @Path("{id}/comments")
  public Response postComment(@Context UriInfo uriInfo,
                               @PathParam("id") String id,
                               @QueryParam("text") String text) throws Exception;
  
}
