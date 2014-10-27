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
package org.exoplatform.social.service.rest.impl.space;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.security.ConversationState;
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
import org.exoplatform.social.service.rest.RestProperties;
import org.exoplatform.social.service.rest.RestUtils;
import org.exoplatform.social.service.rest.SpaceRestIn;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.AbstractSocialRestService;
import org.exoplatform.social.service.rest.api.SpaceSocialRest;
import org.exoplatform.social.service.rest.api.models.ActivitiesCollections;
import org.exoplatform.social.service.rest.api.models.ActivityRestIn;
import org.exoplatform.social.service.rest.api.models.SpacesCollections;
import org.exoplatform.social.service.rest.api.models.UsersCollections;

@Path("v1/social/spaces")
public class SpaceSocialRestServiceV1 extends AbstractSocialRestService implements SpaceSocialRest {

  public SpaceSocialRestServiceV1() {
  }
  
  /**
   * {@inheritDoc}
   */
  @RolesAllowed("users")
  public Response getSpaces(@Context UriInfo uriInfo) throws Exception {
    String q = getQueryParam("q");
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    int limit = getQueryValueLimit();
    int offset = getQueryValueOffset();
    
    List<Map<String, Object>> spaceInfos = new ArrayList<Map<String, Object>>();
    ListAccess<Space> listAccess = null;
    SpaceFilter spaceFilter = null;
    if (q != null) {
      spaceFilter = new SpaceFilter();
      spaceFilter.setSpaceNameSearchCondition(q);
    }
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (RestUtils.isMemberOfAdminGroup()) {
      listAccess = spaceService.getAllSpacesByFilter(spaceFilter);
    } else {
      listAccess = spaceService.getAccessibleSpacesByFilter(authenticatedUser, spaceFilter);
    }
    for (Space space : listAccess.load(offset, limit)) {
      Map<String, Object> spaceInfo = RestUtils.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), getQueryParam("expand"));
      //
      spaceInfos.add(spaceInfo); 
    }
    
    SpacesCollections spaces = new SpacesCollections(getQueryValueReturnSize() ? listAccess.getSize() : -1, offset, limit);
    spaces.setSpaces(spaceInfos);
    
    return Util.getResponse(spaces, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response createSpace(@Context UriInfo uriInfo,
                               SpaceRestIn model) throws Exception {
    if (model == null || model.getDisplayName() == null || model.getDisplayName().length() == 0) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    //validate the display name
    if (spaceService.getSpaceByDisplayName(model.getDisplayName()) != null) {
      throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
    }
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    Space space = new Space();
    fillSpaceFromModel(space, model);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + space.getPrettyName());
    space.setType(DefaultSpaceApplicationHandler.NAME);
    String[] managers = new String[] {authenticatedUser};
    String[] members = new String[] {authenticatedUser};
    space.setManagers(managers);
    space.setMembers(members);
    //
    spaceService.saveSpace(space, true);
    
    return Util.getResponse(RestUtils.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), getQueryParam("expand")), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  public Response getSpaceById(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (Space.HIDDEN.equals(space.getVisibility()) && ! spaceService.isMember(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return Util.getResponse(RestUtils.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), getQueryParam("expand")), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response updateSpaceById(@Context UriInfo uriInfo,
                                  SpaceRestIn model) throws Exception {
    
    if (model == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    String id = getPathParam("id");
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isManager(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    fillSpaceFromModel(space, model);
    spaceService.updateSpace(space);
    
    return Util.getResponse(RestUtils.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), getQueryParam("expand")), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  public Response deleteSpaceById(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isManager(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    spaceService.deleteSpace(space);
    
    return Util.getResponse(RestUtils.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), getQueryParam("expand")), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}/users")
  @RolesAllowed("users")
  public Response getSpaceMembers(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    String role = getQueryParam("role");
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    int limit = getQueryValueLimit();
    int offset = getQueryValueOffset();
    
    String[] users = (role != null && role.equals("manager")) ? space.getManagers() : space.getMembers();
    int size = users.length;
    
    users = Arrays.copyOfRange(users, offset > size - 1 ? size - 1 : offset, (offset + limit > size) ? size : (offset + limit));
    
    List<Map<String, Object>> profileInfos = new ArrayList<Map<String, Object>>();
    for (String user : users) {
      Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, true);
      Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryParam("expand"));
      //
      profileInfos.add(profileInfo);
    }
    
    UsersCollections collections = new UsersCollections(size, offset, limit);
    collections.setUsers(profileInfos);
    
    return Util.getResponse(collections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}/activities")
  @RolesAllowed("users")
  public Response getSpaceActivitiesById(@Context UriInfo uriInfo) throws Exception {
    
    String id = getPathParam("id");
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || ! spaceService.isMember(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    int limit = getQueryValueLimit();
    int offset = getQueryValueOffset();
    
    Identity spaceIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    RealtimeListAccess<ExoSocialActivity> listAccess = CommonsUtils.getService(ActivityManager.class).getActivitiesOfSpaceWithListAccess(spaceIdentity);
    Integer after = getIntegerValue("after"); 
    Integer before = getIntegerValue("before");
    List<ExoSocialActivity> activities = null;
    if (after != null) {
      activities = listAccess.loadNewer(after.longValue(), limit);
    } else if (before != null) {
      activities = listAccess.loadOlder(before.longValue(), limit);
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
      Map<String, Object> activityInfo = RestUtils.buildEntityFromActivity(activity, uriInfo.getPath(), getQueryParam("expand"));
      activityInfo.put(RestProperties.ACTIVITY_STREAM, as);
      //
      activitiesInfo.add(activityInfo);
    }
    
    ActivitiesCollections activitiesCollections = new ActivitiesCollections(listAccess.getSize(), offset, limit);
    activitiesCollections.setActivities(activitiesInfo);
    
    return Util.getResponse(activitiesCollections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @POST
  @Path("{id}/activities")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response postActivityOnSpace(@Context UriInfo uriInfo,
                                       ActivityRestIn model) throws Exception {
    if (model == null || model.getTitle() == null || model.getTitle().length() == 0) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String id = getPathParam("id");
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || ! spaceService.isMember(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity spaceIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    Identity poster = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, false);
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(model.getTitle());
    activity.setUserId(poster.getId());
    CommonsUtils.getService(ActivityManager.class).saveActivityNoReturn(spaceIdentity, activity);
    
    return Util.getResponse(RestUtils.buildEntityFromActivity(activity, uriInfo.getPath(), getQueryParam("expand")), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  private void fillSpaceFromModel(Space space, SpaceRestIn model) {
    if (model.getDisplayName() != null && model.getDisplayName().length() > 0) {
      space.setDisplayName(model.getDisplayName());
    }
    if (model.getDescription() != null && model.getDescription().length() > 0) {
      space.setDescription(StringEscapeUtils.escapeHtml(model.getDescription()));
    }
    space.setPrettyName(space.getDisplayName());
    
    if (model.getVisibility() != null && (model.getVisibility().equals(Space.HIDDEN) 
        || model.getVisibility().equals(Space.PRIVATE))) {
      space.setVisibility(model.getVisibility());
    } else if (space.getVisibility() == null || space.getVisibility().length() == 0) {
      space.setVisibility(Space.PRIVATE);
    }
    
    if (model.getRegistration() != null && (model.getRegistration().equals(Space.OPEN) 
        || model.getRegistration().equals(Space.CLOSE) || model.getRegistration().equals(Space.VALIDATION))) {
      space.setRegistration(model.getRegistration());
    } else if (space.getRegistration() == null || space.getRegistration().length() == 0) {
      space.setRegistration(Space.VALIDATION);
    }
  }
}
