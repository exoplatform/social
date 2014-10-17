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

import static org.exoplatform.social.service.rest.RestChecker.checkAuthenticatedRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
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
  public Response getUsers(@Context UriInfo uriInfo) throws Exception {
    String q = getQueryParam("q");
    checkAuthenticatedRequest();
    
    int limit = getQueryValueLimit(uriInfo);
    int offset = getQueryValueOffset(uriInfo);
    
    ProfileFilter filter = new ProfileFilter();
    filter.setName(q == null || q.isEmpty() ? "" : q);
    filter.setCompany("");
    filter.setPosition("");
    filter.setSkills("");
    
    ListAccess<Identity> list = CommonsUtils.getService(IdentityManager.class).getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, false);
    
    Identity[] identities = list.load(offset, limit);
    List<Map<String, Object>> profileInfos = new ArrayList<Map<String, Object>>();
    for (Identity identity : identities) {
      Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryValueExpand(uriInfo));
      //
      profileInfos.add(profileInfo);
    }
    
    UsersCollections users = new UsersCollections(getQueryValueReturnSize(uriInfo) ? list.getSize() : -1, offset, limit);
    users.setUsers(profileInfos);
    
    return Util.getResponse(users, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  public Response addUser(@Context UriInfo uriInfo) throws Exception {
    String userName = getQueryParam("userName");
    String firstName = getQueryParam("firstName");
    String lastName = getQueryParam("lastName"); 
    String password = getQueryParam("password");
    String email = getQueryParam("email");
    
    checkAuthenticatedRequest();
    //Check permission of current user
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, true);
    if (identity != null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    //Create new user
    UserHandler userHandler = CommonsUtils.getService(OrganizationService.class).getUserHandler();
    User user = userHandler.createUserInstance(userName);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setPassword(password == null || password.isEmpty() ? "exo" : password);
    userHandler.createUser(user, true);
    
    identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, true);
    //
    return Util.getResponse(RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryValueExpand(uriInfo)), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}")
  public Response getUserById(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    checkAuthenticatedRequest();
    
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    //
    return Util.getResponse(RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryValueExpand(uriInfo)), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @Path("{id}")
  public Response deleteUserById(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    checkAuthenticatedRequest();
    //Check permission of current user
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    CommonsUtils.getService(IdentityManager.class).hardDeleteIdentity(identity);
    identity.setDeleted(true);
    //
    return Util.getResponse(RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryValueExpand(uriInfo)), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("{id}")
  public Response updateUserById(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    checkAuthenticatedRequest();
    Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    //Check if the current user is the authenticated user
    if (!ConversationState.getCurrent().getIdentity().getUserId().equals(id)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    //TODO : what information can be updated?
    
    //
    return Util.getResponse(RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryValueExpand(uriInfo)), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/connections")
  public Response getConnectionOfUser(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    checkAuthenticatedRequest();
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    int limit = getQueryValueLimit(uriInfo);
    int offset = getQueryValueOffset(uriInfo);
    
    List<Map<String, Object>> profileInfos = new ArrayList<Map<String, Object>>();
    ListAccess<Identity> listAccess = CommonsUtils.getService(RelationshipManager.class).getConnectionsByFilter(target, new ProfileFilter());
    for (Identity identity : listAccess.load(offset, limit)) {
      Map<String, Object> profileInfo = RestUtils.buildEntityFromIdentity(identity, uriInfo.getPath(), getQueryValueExpand(uriInfo));
      //
      profileInfos.add(profileInfo);
    }
    
    UsersCollections users = new UsersCollections(getQueryValueReturnSize(uriInfo) ? listAccess.getSize() : -1, offset, limit);
    users.setUsers(profileInfos);
    
    return Util.getResponse(users, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/spaces")
  public Response getSpacesOfUser(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    checkAuthenticatedRequest();
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    //Check if the given user exists
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    //Check permission of authenticated user : he must be an admin or he is the given user
    if (!RestUtils.isMemberOfAdminGroup() && !ConversationState.getCurrent().getIdentity().getUserId().equals(id) ) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    int limit = getQueryValueLimit(uriInfo);
    int offset = getQueryValueOffset(uriInfo);
    
    List<Map<String, Object>> spaceInfos = new ArrayList<Map<String, Object>>();
    ListAccess<Space> listAccess = CommonsUtils.getService(SpaceService.class).getMemberSpaces(id);
    for (Space space : listAccess.load(offset, limit)) {
      Map<String, Object> spaceInfo = RestUtils.buildEntityFromSpace(space, id, uriInfo.getPath(), getQueryValueExpand(uriInfo));
      //
      spaceInfos.add(spaceInfo);
    }
    
    SpacesCollections spaces = new SpacesCollections(getQueryValueReturnSize(uriInfo) ? listAccess.getSize() : -1, offset, limit);
    spaces.setSpaces(spaceInfos);
    
    return Util.getResponse(spaces, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/activities")
  public Response getActivitiesOfUser(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    String type = getQueryParam("type");
    Long after = Long.parseLong(getQueryParam("after")); 
    Long before = Long.parseLong(getQueryParam("before"));
    
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    //Check if the given user doesn't exist
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    ACTIVITY_STREAM_TYPE streamType;
    try {
      streamType = ACTIVITY_STREAM_TYPE.valueOf(type);
    } catch (Exception e) {
      streamType = ACTIVITY_STREAM_TYPE.all;
    }
    
    int limit = getQueryValueLimit(uriInfo);
    int offset = getQueryValueOffset(uriInfo);
    
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
    if (after != null) {
      activities = listAccess.loadNewer(after, limit);
    } else if (before != null) {
      activities = listAccess.loadOlder(before, limit);
    } else {
      activities = listAccess.loadAsList(offset, limit);
    }
    
    List<Map<String, Object>> activitiesInfo = new ArrayList<Map<String, Object>>();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    for (ExoSocialActivity activity : activities) {
      Map<String, String> as = RestUtils.getActivityStream(activity, currentUser);
      if (as == null) continue;
      Map<String, Object> activityInfo = RestUtils.buildEntityFromActivity(activity, uriInfo.getPath(), getQueryValueExpand(uriInfo));
      activityInfo.put(RestProperties.ACTIVITY_STREAM, as);
      //
      activitiesInfo.add(activityInfo);
    }
    
    ActivitiesCollections activitiesCollections = new ActivitiesCollections(getQueryValueReturnSize(uriInfo) ? listAccess.getSize() : -1, offset, limit);
    activitiesCollections.setActivities(activitiesInfo);
    
    return Util.getResponse(activitiesCollections, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @Path("{id}/activities")
  public Response addActivityByUser(@Context UriInfo uriInfo) throws Exception {
    String id = getPathParam("id");
    String text = getQueryParam("text");
    checkAuthenticatedRequest();
    //Check if no authenticated user
    if (Util.isAnonymous()) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    //Check if the given user doesn't exist
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (target == null || !ConversationState.getCurrent().getIdentity().getUserId().equals(id)) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(text);
    CommonsUtils.getService(ActivityManager.class).saveActivityNoReturn(target, activity);
    
    return Util.getResponse(RestUtils.buildEntityFromActivity(activity, uriInfo.getPath(), getQueryValueExpand(uriInfo)), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
}
