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
package org.exoplatform.social.service.rest.impl.user;

import java.util.ArrayList;
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

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.RestProperties;
import org.exoplatform.social.service.rest.RestUtils;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.AbstractSocialRestService;
import org.exoplatform.social.service.rest.api.UserSocialRest;
import org.exoplatform.social.service.rest.api.models.ActivitiesCollections;
import org.exoplatform.social.service.rest.api.models.ActivityRestIn;
import org.exoplatform.social.service.rest.api.models.ProfileRestIn;
import org.exoplatform.social.service.rest.api.models.SpacesCollections;
import org.exoplatform.social.service.rest.api.models.UsersCollections;

/**
 * 
 * Provides REST Services for manipulating jobs related to users.
 * 
 * @anchor UsersRestService
 */

@Path("v1/social/users")
public class UserSocialRestServiceV1 extends AbstractSocialRestService implements UserSocialRest {
  
  public static enum ACTIVITY_STREAM_TYPE {
    all, owner, connections, spaces
  }
  
  public UserSocialRestServiceV1() {
    
  }
  
  @GET
  @RolesAllowed("users")
  public Response getUsers(@Context UriInfo uriInfo) throws Exception {
    String q = getQueryParam("q");
    
    int limit = getQueryValueLimit();
    int offset = getQueryValueOffset();
    
    ProfileFilter filter = new ProfileFilter();
    filter.setName(q == null || q.isEmpty() ? "" : q);
    
    ListAccess<Identity> list = CommonsUtils.getService(IdentityManager.class).getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, false);
    
    Identity[] identities = list.load(offset, limit);
    List<Map<String, Object>> profileInfos = new ArrayList<Map<String, Object>>();
    for (Identity identity : identities) {
      Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryParam("expand"));
      //
      profileInfos.add(profileInfo);
    }
    
    UsersCollections users = new UsersCollections(getQueryValueReturnSize() ? list.getSize() : -1, offset, limit);
    users.setUsers(profileInfos);
    
    return Util.getResponse(users, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response addUser(@Context UriInfo uriInfo,
                           ProfileRestIn model) throws Exception {
    if (model.isNotValid()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    //Check permission of current user
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    //check if the user is already exist
    Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, model.getUserName(), true);
    if (identity != null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    //Create new user
    UserHandler userHandler = CommonsUtils.getService(OrganizationService.class).getUserHandler();
    User user = userHandler.createUserInstance(model.getUserName());
    user.setFirstName(model.getFirstName());
    user.setLastName(model.getLastName());
    user.setEmail(model.getEmail());
    user.setPassword(model.getPassword() == null || model.getPassword().isEmpty() ? "exo" : model.getPassword());
    userHandler.createUser(user, true);
    
    identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, model.getUserName(), true);
    //
    return Util.getResponse(RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryParam("expand")), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  public Response getUserById(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    //
    if (identity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    return Util.getResponse(RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryParam("expand")), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  public Response deleteUserById(@Context UriInfo uriInfo) throws Exception {
    //Check permission of current user
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    String id = getPathParam("id");
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    identityManager.hardDeleteIdentity(identity);
    identity.setDeleted(true);
    //
    return Util.getResponse(RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryParam("expand")), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response updateUserById(@Context UriInfo uriInfo,
                                  ProfileRestIn model) throws Exception {
    String id = getPathParam("id");
    UserHandler userHandler = CommonsUtils.getService(OrganizationService.class).getUserHandler();
    User user = userHandler.findUserByName(id);
    if (user == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    //Check if the current user is the authenticated user
    if (!ConversationState.getCurrent().getIdentity().getUserId().equals(id)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    fillUserFromModel(user, model);
    userHandler.saveUser(user, true);
    
    Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    //
    return Util.getResponse(RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryParam("expand")), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/connections")
  @RolesAllowed("users")
  public Response getConnectionOfUser(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    int limit = getQueryValueLimit();
    int offset = getQueryValueOffset();
    
    List<Map<String, Object>> profileInfos = new ArrayList<Map<String, Object>>();
    ListAccess<Identity> listAccess = CommonsUtils.getService(RelationshipManager.class).getConnectionsByFilter(target, new ProfileFilter());
    for (Identity identity : listAccess.load(offset, limit)) {
      Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryParam("expand"));
      //
      profileInfos.add(profileInfo);
    }
    
    UsersCollections users = new UsersCollections(getQueryValueReturnSize() ? listAccess.getSize() : -1, offset, limit);
    users.setUsers(profileInfos);
    
    return Util.getResponse(users, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/spaces")
  @RolesAllowed("users")
  public Response getSpacesOfUser(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    //Check if the given user exists
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    //Check permission of authenticated user : he must be an admin or he is the given user
    if (!RestUtils.isMemberOfAdminGroup() && !ConversationState.getCurrent().getIdentity().getUserId().equals(id) ) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    int limit = getQueryValueLimit();
    int offset = getQueryValueOffset();
    
    List<Map<String, Object>> spaceInfos = new ArrayList<Map<String, Object>>();
    ListAccess<Space> listAccess = CommonsUtils.getService(SpaceService.class).getMemberSpaces(id);
    for (Space space : listAccess.load(offset, limit)) {
      Map<String, Object> spaceInfo = RestUtils.buildEntityFromSpace(space, id, uriInfo.getPath(), getQueryParam("expand"));
      //
      spaceInfos.add(spaceInfo);
    }
    
    SpacesCollections spaces = new SpacesCollections(getQueryValueReturnSize() ? listAccess.getSize() : -1, offset, limit);
    spaces.setSpaces(spaceInfos);
    
    return Util.getResponse(spaces, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/activities")
  @RolesAllowed("users")
  public Response getActivitiesOfUser(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //Check if the given user doesn't exist
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    String type = getQueryParam("type");
    ACTIVITY_STREAM_TYPE streamType;
    try {
      streamType = ACTIVITY_STREAM_TYPE.valueOf(type);
    } catch (Exception e) {
      streamType = ACTIVITY_STREAM_TYPE.all;
    }
    
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    RealtimeListAccess<ExoSocialActivity> listAccess = null;
    List<ExoSocialActivity> activities = null;
    switch (streamType) {
      case all: {
        listAccess = activityManager.getActivityFeedWithListAccess(target);
        break;
      }
      case owner: {
        listAccess = activityManager.getActivitiesWithListAccess(target);
        break;
      }
      case connections: {
        listAccess = activityManager.getActivitiesOfConnectionsWithListAccess(target);
        break;
      }
      case spaces: {
        listAccess = activityManager.getActivitiesOfUserSpacesWithListAccess(target);
        break;
      }
  
      default:
        break;
    }
    //
    int limit = getQueryValueLimit();
    int offset = getQueryValueOffset();
    Integer after = getIntegerValue("after"); 
    Integer before = getIntegerValue("before");
    if (after != null) {
      activities = listAccess.loadNewer(after.longValue(), limit);
    } else if (before != null) {
      activities = listAccess.loadOlder(before.longValue(), limit);
    } else {
      activities = listAccess.loadAsList(offset, limit);
    }
    
    List<Map<String, Object>> activitiesInfo = new ArrayList<Map<String, Object>>();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    for (ExoSocialActivity activity : activities) {
      Map<String, String> as = RestUtils.getActivityStream(activity, currentUser);
      if (as == null) continue;
      Map<String, Object> activityInfo = RestUtils.buildEntityFromActivity(activity, uriInfo.getPath(), getQueryParam("expand"));
      activityInfo.put(RestProperties.ACTIVITY_STREAM, as);
      //
      activitiesInfo.add(activityInfo);
    }
    
    ActivitiesCollections activitiesCollections = new ActivitiesCollections(getQueryValueReturnSize() ? listAccess.getSize() : -1, offset, limit);
    activitiesCollections.setActivities(activitiesInfo);
    
    return Util.getResponse(activitiesCollections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @Path("{id}/activities")
  @RolesAllowed("users")
  public Response addActivityByUser(@Context UriInfo uriInfo,
                                     ActivityRestIn model) throws Exception {
    if (model == null || model.getTitle() == null || model.getTitle().length() ==0) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    String id = getPathParam("id");
    //Check if the given user doesn't exist
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (target == null || !ConversationState.getCurrent().getIdentity().getUserId().equals(id)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(model.getTitle());
    CommonsUtils.getService(ActivityManager.class).saveActivityNoReturn(target, activity);
    
    return Util.getResponse(RestUtils.buildEntityFromActivity(activity, uriInfo.getPath(), getQueryParam("expand")), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  private void fillUserFromModel(User user, ProfileRestIn model) {
    if (model.getFirstName() != null && model.getFirstName().length() > 0) {
      user.setFirstName(model.getFirstName());
    }
    if (model.getLastName() != null && model.getLastName().length() > 0) {
      user.setLastName(model.getLastName());
    }
    if (model.getEmail() != null && model.getEmail().length() > 0) {
      user.setEmail(model.getEmail());
    }
    if (model.getPassword() != null && model.getPassword().length() > 0) {
      user.setPassword(model.getPassword());
    }
  }
}
