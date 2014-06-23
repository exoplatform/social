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
package org.exoplatform.social.service.rest;

import static org.exoplatform.social.service.rest.RestChecker.checkAuthenticatedRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api.models.ActivitiesCollections;
import org.exoplatform.social.service.rest.api.models.SpacesCollections;
import org.exoplatform.social.service.rest.api.models.UsersCollections;

@Path(VersionResources.CURRENT_VERSION + "/social/spaces")
public class SpaceRestService implements ResourceContainer {

  public SpaceRestService() {
  }
  
  /**
   * Process to return a list of space in json format
   * 
   * @param uriInfo
   * @param q
   * @param offset
   * @param limit
   * @return
   * @throws Exception
   */
  @GET
  public Response getSpaces(@Context UriInfo uriInfo,
                             @QueryParam("q") String q,
                             @QueryParam("offset") int offset,
                             @QueryParam("limit") int limit) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    limit = limit <= 0 ? RestUtils.DEFAULT_LIMIT : limit;
    offset = offset < 0 ? RestUtils.DEFAULT_OFFSET : offset;
    
    List<Map<String, Object>> spaceInfos = new ArrayList<Map<String, Object>>();
    ListAccess<Space> listAccess = null;
    SpaceFilter spaceFilter = null;
    if (q != null) {
      spaceFilter = new SpaceFilter();
      spaceFilter.setSpaceNameSearchCondition(q);
    }
    if (RestUtils.isMemberOfAdminGroup()) {
      listAccess = spaceService.getAllSpacesByFilter(spaceFilter);
    } else {
      listAccess = spaceService.getAccessibleSpacesByFilter(authenticatedUser, spaceFilter);
    }
    for (Space space : listAccess.load(offset, limit)) {
      Map<String, Object> spaceInfo = RestUtils.buildEntityFromSpace(space, authenticatedUser);
      //
      spaceInfos.add(spaceInfo);
    }
    
    SpacesCollections spaces = new SpacesCollections(listAccess.getSize(), offset, limit);
    spaces.setSpaces(spaceInfos);
    
    return Util.getResponse(spaces, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
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
  public Response createSpace(@Context UriInfo uriInfo,
                               @QueryParam("displayName") String displayName,
                               @QueryParam("description") String description,
                               @QueryParam("visibility") String visibility,
                               @QueryParam("registration") String registration) throws Exception {
    checkAuthenticatedRequest();
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);

    //validate the display name
    if (spaceService.getSpaceByDisplayName(displayName) != null) {
      throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
    }
    
    Space space = new Space();
    space.setDisplayName(displayName.trim());
    space.setPrettyName(space.getDisplayName());
    space.setDescription(StringEscapeUtils.escapeHtml(description));
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(visibility);
    space.setRegistration(registration);
    //
    spaceService.createSpace(space, authenticatedUser);
    
    return Util.getResponse(RestUtils.buildEntityFromSpace(space, authenticatedUser), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
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
  public Response getSpaceById(@Context UriInfo uriInfo,
                                @PathParam("id") String id) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (Space.HIDDEN.equals(space.getVisibility()) && ! spaceService.isMember(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    
    return Util.getResponse(RestUtils.buildEntityFromSpace(space, authenticatedUser), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
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
  public Response updateSpaceById(@Context UriInfo uriInfo,
                                   @PathParam("id") String id,
                                   @QueryParam("displayName") String displayName,
                                   @QueryParam("description") String description,
                                   @QueryParam("visibility") String visibility,
                                   @QueryParam("registration") String registration) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isManager(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    boolean hasUpdate = false;
    if (displayName != null && displayName.length() > 0 && ! displayName.equals(space.getDisplayName())) {
      space.setDisplayName(displayName.trim());
      space.setPrettyName(space.getDisplayName());
      hasUpdate = true;
    }
    if (description != null && description.length() > 0 && ! description.equals(space.getDescription())) {
      space.setDescription(StringEscapeUtils.escapeHtml(description));
      hasUpdate = true;
    }
    if (visibility != null && visibility.length() > 0 && ! visibility.equals(space.getDescription())) {
      space.setVisibility(visibility);
      hasUpdate = true;
    }
    if (registration != null && registration.length() > 0 && ! registration.equals(space.getDescription())) {
      space.setRegistration(registration);
      hasUpdate = true;
    }
    
    if (hasUpdate) {
      spaceService.updateSpace(space);
    }
    
    return Util.getResponse(RestUtils.buildEntityFromSpace(space, authenticatedUser), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
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
  public Response deleteSpaceById(@Context UriInfo uriInfo,
                                   @PathParam("id") String id) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isManager(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    spaceService.deleteSpace(space);
    
    return Util.getResponse(RestUtils.buildEntityFromSpace(space, authenticatedUser), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
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
  public Response getSpaceMembers(@Context UriInfo uriInfo,
                                   @PathParam("id") String id,
                                   @QueryParam("role") String role,
                                   @QueryParam("offset") int offset,
                                   @QueryParam("limit") int limit) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    limit = limit <= 0 ? RestUtils.DEFAULT_LIMIT : limit;
    offset = offset < 0 ? RestUtils.DEFAULT_OFFSET : offset;
    
    String[] users = (role != null && role.equals("manager")) ? space.getManagers() : space.getMembers();
    int size = users.length;
    
    users = Arrays.copyOfRange(users, offset > size - 1 ? size - 1 : offset, (offset + limit > size) ? size : (offset + limit));
    
    List<Map<String, Object>> profileInfos = new ArrayList<Map<String, Object>>();
    for (String user : users) {
      Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, true);
      Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity);
      //
      profileInfos.add(profileInfo);
    }
    
    UsersCollections collections = new UsersCollections(size, offset, limit);
    collections.setUsers(profileInfos);
    
    return Util.getResponse(collections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
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
  public Response getSpaceActivitiesById(@Context UriInfo uriInfo,
                                          @PathParam("id") String id,
                                          @QueryParam("after") Long after,
                                          @QueryParam("before") Long before,
                                          @QueryParam("offset") int offset,
                                          @QueryParam("limit") int limit) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || ! spaceService.isMember(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity spaceIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    RealtimeListAccess<ExoSocialActivity> listAccess = CommonsUtils.getService(ActivityManager.class).getActivitiesOfSpaceWithListAccess(spaceIdentity);
    List<ExoSocialActivity> activities = null;
    if (after != null) {
      activities = listAccess.loadNewer(after, limit);
    } else if (before != null) {
      activities = listAccess.loadOlder(before, limit);
    } else {
      activities = listAccess.loadAsList(offset, limit);
    }
    
    List<Map<String, Object>> activitiesInfo = new ArrayList<Map<String, Object>>();
    //
    Map<String, String> as = new HashMap<String, String>();
    as.put(RestProperties.TYPE, RestUtils.SPACE_ACTIVITY_TYPE);
    as.put(RestProperties.ID, spaceIdentity.getRemoteId());
    //
    for (ExoSocialActivity activity : activities) {
      Map<String, Object> activityInfo = RestUtils.buildEntityFromActivity(activity);
      activityInfo.put(RestProperties.ACTIVITY_STREAM, as);
      //
      activitiesInfo.add(activityInfo);
    }
    
    ActivitiesCollections activitiesCollections = new ActivitiesCollections(listAccess.getSize(), offset, limit);
    activitiesCollections.setActivities(activitiesInfo);
    
    return Util.getResponse(activitiesCollections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @Path("{id}/activities")
  public Response postActivityOnSpace(@Context UriInfo uriInfo,
                                       @PathParam("id") String id,
                                       @QueryParam("text") String text) throws Exception {
    checkAuthenticatedRequest();
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || ! spaceService.isMember(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity spaceIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, false);
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(text);
    activity.setUserId(target.getId());
    CommonsUtils.getService(ActivityManager.class).saveActivityNoReturn(spaceIdentity, activity);
    
    return Util.getResponse(RestUtils.buildEntityFromActivity(activity), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
}
