/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.service.rest.impl.activity;

import static org.exoplatform.social.service.rest.RestChecker.checkAuthenticatedRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.RestProperties;
import org.exoplatform.social.service.rest.RestUtils;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.ActivitySocialRest;
import org.exoplatform.social.service.rest.api.models.ActivitiesCollections;
import org.exoplatform.social.service.rest.api.models.CommentsCollections;

@Path("v1/social/activities")
public class ActivitySocialRestServiceV1 implements ActivitySocialRest {
  
  /* (non-Javadoc)
   * @see org.exoplatform.social.service.rest.api.ActivitySocialRest#getActivitiesOfCurrentUser(javax.ws.rs.core.UriInfo, boolean, int, int)
   */
  @GET
  public Response getActivitiesOfCurrentUser(@Context UriInfo uriInfo,
                                              @QueryParam("returnSize") boolean returnSize,
                                              @QueryParam("offset") int offset,
                                              @QueryParam("limit") int limit) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    limit = limit <= 0 ? RestUtils.DEFAULT_LIMIT : Math.min(RestUtils.HARD_LIMIT, limit);
    offset = offset < 0 ? RestUtils.DEFAULT_OFFSET : offset;
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivityFeedWithListAccess(currentUser);
    List<ExoSocialActivity> activities = listAccess.loadAsList(offset, limit);
    
    List<Map<String, Object>> activitiesInfo = new ArrayList<Map<String, Object>>();
    for (ExoSocialActivity activity : activities) {
      Map<String, String> as = RestUtils.getActivityStream(authenticatedUser, activity, currentUser);
      if (as == null) continue;
      Map<String, Object> activityInfo = RestUtils.buildEntityFromActivity(activity, uriInfo.getPath());
      activityInfo.put(RestProperties.ACTIVITY_STREAM, as);
      //
      activitiesInfo.add(activityInfo);
    }
    
    ActivitiesCollections activitiesCollections = new ActivitiesCollections(returnSize ? listAccess.getSize() : -1, offset, limit);
    activitiesCollections.setActivities(activitiesInfo);
    
    return Util.getResponse(activitiesCollections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.social.service.rest.api.ActivitySocialRest#getActivityById(javax.ws.rs.core.UriInfo, java.lang.String)
   */
  @GET
  @Path("{id}")
  public Response getActivityById(@Context UriInfo uriInfo,
                                   @PathParam("id") String id) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Map<String, String> as = RestUtils.getActivityStream(authenticatedUser, activity.isComment() ? activityManager.getParentActivity(activity) : activity, currentUser);
    if (as == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Map<String, Object> activityInfo = RestUtils.buildEntityFromActivity(activity, uriInfo.getPath());
    if (! activity.isComment()) {
      activityInfo.put(RestProperties.ACTIVITY_STREAM, as);
    }
    
    return Util.getResponse(activityInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.social.service.rest.api.ActivitySocialRest#updateActivityById(javax.ws.rs.core.UriInfo, java.lang.String, java.lang.String)
   */
  @PUT
  @Path("{id}")
  public Response updateActivityById(@Context UriInfo uriInfo,
                                      @PathParam("id") String id,
                                      @QueryParam("text") String text) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    //update activity's title
    activity.setTitle(text);
    activityManager.updateActivity(activity);
    
    Map<String, String> as = RestUtils.getActivityStream(authenticatedUser, activity, currentUser);
    if (as == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Map<String, Object> activityInfo = RestUtils.buildEntityFromActivity(activity, uriInfo.getPath());
    activityInfo.put(RestProperties.ACTIVITY_STREAM, as);
    
    return Util.getResponse(activityInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.social.service.rest.api.ActivitySocialRest#deleteActivityById(javax.ws.rs.core.UriInfo, java.lang.String)
   */
  @DELETE
  @Path("{id}")
  public Response deleteActivityById(@Context UriInfo uriInfo,
                                      @PathParam("id") String id) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Map<String, String> as = RestUtils.getActivityStream(authenticatedUser, activity, currentUser);
    if (as == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Map<String, Object> activityInfo = RestUtils.buildEntityFromActivity(activity, uriInfo.getPath());
    activityInfo.put(RestProperties.ACTIVITY_STREAM, as);
    
    return Util.getResponse(activityInfo, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.social.service.rest.api.ActivitySocialRest#getCommentsOfActivity(javax.ws.rs.core.UriInfo, java.lang.String, boolean, int, int)
   */
  @GET
  @Path("{id}/comments")
  public Response getCommentsOfActivity(@Context UriInfo uriInfo,
                                         @PathParam("id") String id,
                                         @QueryParam("returnSize") boolean returnSize,
                                         @QueryParam("offset") int offset,
                                         @QueryParam("limit") int limit) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    limit = limit <= 0 ? RestUtils.DEFAULT_LIMIT : Math.min(RestUtils.HARD_LIMIT, limit);
    offset = offset < 0 ? RestUtils.DEFAULT_OFFSET : offset;
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (RestUtils.getActivityStream(authenticatedUser, activity, currentUser) == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getCommentsWithListAccess(activity);
    List<ExoSocialActivity> comments = listAccess.loadAsList(offset, limit);
    
    List<Map<String, Object>> commentsInfo = new ArrayList<Map<String, Object>>();
    for (ExoSocialActivity comment : comments) {
      Map<String, Object> commentInfo = RestUtils.buildEntityFromActivity(comment, uriInfo.getPath());
      //
      commentsInfo.add(commentInfo);
    }
    
    CommentsCollections commentsCollections = new CommentsCollections(returnSize ? listAccess.getSize() : -1, offset, limit);
    commentsCollections.setComments(commentsInfo);
    
    return Util.getResponse(commentsCollections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.social.service.rest.api.ActivitySocialRest#postComment(javax.ws.rs.core.UriInfo, java.lang.String, java.lang.String)
   */
  @POST
  @Path("{id}/comments")
  public Response postComment(@Context UriInfo uriInfo,
                               @PathParam("id") String id,
                               @QueryParam("text") String text) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (RestUtils.getActivityStream(authenticatedUser, activity, currentUser) == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(text);
    comment.setUserId(currentUser.getId());
    activityManager.saveComment(activity, comment);
    
    return Util.getResponse(RestUtils.buildEntityFromActivity(comment, uriInfo.getPath()), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
}
