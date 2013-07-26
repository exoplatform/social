/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * 
 * Provides REST Services for manipulating jobs relates to notifications.
 * 
 * @anchor NotificationRestService
 */

@Path("social/notifications")
public class NotificationsRestService implements ResourceContainer {
  
  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;
  
  private static String       ACTIVITY_ID_PREFIX = "activity";

  public enum URL_TYPE {
    user, space, space_members, reply_activity, view_full_activity, view_likers_activity, portal_home,
    all_space, connections, notification_settings, connections_request, space_invitation, user_activity_stream;
  }
  
  public NotificationsRestService() {
  }
  
  /**
   * Process action "inviteToConnect" of sender and receiver then redirect to the page of receiver's profile
   * 
   * @param senderId the remote id of the sender
   * @param receiverId the remote id of the receiver
   * @return
   * @throws Exception
   */
  @GET
  @Path("inviteToConnect/{userId}")
  public Response inviteToConnect(@Context UriInfo uriInfo,
                                  @PathParam("userId") String userId) throws Exception {
    checkAuthenticatedRequest();
    
    Identity sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, ConversationState.getCurrent().getIdentity().getUserId(), true);
    if (sender == null) {
      sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, Util.getViewerId(uriInfo), true);
    }
    Identity receiver = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getRelationshipManager().inviteToConnect(sender, receiver);
  
    String targetURL = Util.getBaseUrl() + LinkProvider.getUserProfileUri(receiver.getRemoteId());
    
    // redirect to target page
   return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * The receiver accept the invitation to connect of the sender then he will be redirected to the page of sender's profile
   * 
   * @param senderId the remote id of the sender
   * @param receiverId the remote id of the receiver
   * @return
   * @throws Exception
   */
  @GET
  @Path("confirmInvitationToConnect/{senderId}/{receiverId}")
  public Response confirmInvitationToConnect(@PathParam("senderId") String senderId,
                                             @PathParam("receiverId") String receiverId) throws Exception {
    checkAuthenticatedRequest();
    
    Identity sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true); 
    Identity receiver = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getRelationshipManager().confirm(sender, receiver);
  
    String targetURL = Util.getBaseUrl() + LinkProvider.getUserActivityUri(sender.getRemoteId());
    
    // redirect to target page
   return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * The receiver deny the invitation to connect of the sender then he will be redirected to the page of his connections
   * 
   * @param senderId the remote id of the sender
   * @param receiverId the remote id of the receiver
   * @return
   * @throws Exception
   */
  @GET
  @Path("ignoreInvitationToConnect/{senderId}/{receiverId}")
  public Response ignoreInvitationToConnect(@PathParam("senderId") String senderId,
                                            @PathParam("receiverId") String receiverId) throws Exception {
    checkAuthenticatedRequest();

    Identity sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    Identity receiver = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getRelationshipManager().deny(sender, receiver);

    String targetURL = Util.getBaseUrl() + LinkProvider.getUserConnectionsYoursUri(receiver.getRemoteId());

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * The user accept the invitation to join space and he will be redirected to the space's home page
   * 
   * @param userId the remote id of the user who is invited to join space
   * @param spaceId the id of space
   * @return
   * @throws Exception
   */
  @GET
  @Path("acceptInvitationToJoinSpace/{spaceId}/{userId}")
  public Response acceptInvitationToJoinSpace(@PathParam("spaceId") String spaceId,
                                              @PathParam("userId") String userId) throws Exception {
    checkAuthenticatedRequest();

    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().addMember(space, userId);

    String targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getGroupId().replace("/spaces/", ""), space.getPrettyName());

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * The user deny the invitation to join space and he will be redirected to the page of all spaces
   * 
   * @param userId the remote id of the user who is invited to join space
   * @param spaceId the id of space
   * @return
   * @throws Exception
   */
  @GET
  @Path("ignoreInvitationToJoinSpace/{spaceId}/{userId}")
  public Response ignoreInvitationToJoinSpace(@PathParam("spaceId") String spaceId,
                                              @PathParam("userId") String userId) throws Exception {
    checkAuthenticatedRequest();

    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().removeInvitedUser(space, userId);

    String targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri("all-spaces");
    
    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * The manager of space validate the request to join space of user and he will be redirected to the space's members page
   * 
   * @param userId the remote id of the user who send the request to join space
   * @param spaceId the id of space
   * @return
   * @throws Exception
   */
  @GET
  @Path("validateRequestToJoinSpace/{spaceId}/{userId}")
  public Response validateRequestToJoinSpace(@PathParam("spaceId") String spaceId,
                                             @PathParam("userId") String userId) throws Exception {
    checkAuthenticatedRequest();

    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().addMember(space, userId);

    String targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getGroupId().replace("/spaces/", ""), space.getPrettyName()) + "/settings/members";

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * The manager of space refuse the request to join space of user and he will be redirected to the space's members page
   * 
   * @param userId the remote id of the user who send the request to join space
   * @param spaceId the id of space
   * @return
   * @throws Exception
   */
  @GET
  @Path("refuseRequestToJoinSpace/{spaceId}/{userId}")
  public Response refuseRequestToJoinSpace(@PathParam("spaceId") String spaceId,
                                           @PathParam("userId") String userId) throws Exception {
    checkAuthenticatedRequest();

    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().removePendingUser(space, userId);

    String targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getGroupId().replace("/spaces/", ""), space.getPrettyName()) + "/settings/members";

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * Depend on the type, user will be redirect to the associated activity, space, profile or notification settings
   * 
   * @param type the type of the page will be redirected : activity, space, profile or notification settings
   * @param objectId id of the associated type
   * @return
   * @throws Exception
   */
  @GET
  @Path("redirectUrl/{type}/{objectId}")
  public Response redirectUrl(@Context UriInfo uriInfo,
                              @PathParam("type") String type,
                              @PathParam("objectId") String objectId) throws Exception {
    Space space = null;
    ExoSocialActivity activity = null;
    Identity userIdentity = null;
    String targetURL = null;
    
    try {
      checkAuthenticatedRequest();
      URL_TYPE urlType = URL_TYPE.valueOf(type);
      switch (urlType) {
        case view_full_activity: {
          activity = getActivityManager().getActivity(objectId);
          userIdentity = getIdentityManager().getIdentity(activity.getPosterId(), true);
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + activity.getId());
          break;
        }
        case view_likers_activity: {
          activity = getActivityManager().getActivity(objectId);
          userIdentity = getIdentityManager().getIdentity(activity.getPosterId(), true);
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + activity.getId() + "&likes=1");
          break;
        }
        case reply_activity: {
          activity = getActivityManager().getActivity(objectId);
          userIdentity = getIdentityManager().getIdentity(activity.getPosterId(), true);
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + activity.getId() + "&comment=1");
          break;
        }
        case user: {
          userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, objectId, true);
          targetURL = Util.getBaseUrl() + LinkProvider.getUserProfileUri(userIdentity.getRemoteId());
          break;
        }
        case user_activity_stream: {
          userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, objectId, true);
          targetURL = Util.getBaseUrl() + LinkProvider.getUserActivityUri(userIdentity.getRemoteId());
          break;
        }
        case space: {
          space = getSpaceService().getSpaceById(objectId);
          targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getGroupId().replace("/spaces/", ""), space.getPrettyName());
          break;
        }
        case space_members: {
          space = getSpaceService().getSpaceById(objectId);
          targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getGroupId().replace("/spaces/", ""), space.getPrettyName()) + "/settings/members";
          break;
        }
        case portal_home: {
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri("");
          break;
        }
        case all_space: {
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri("all-spaces");
          break;
        }
        case connections: {
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri("connexions");
          break;
        }
        case connections_request: {
          userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, objectId, true);
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri("connexions/receivedInvitations/" + userIdentity.getRemoteId());
          break;
        }
        case space_invitation: {
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri("invitationSpace");
          break;
        }
        case notification_settings: {
          userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, objectId, true);
          targetURL = Util.getBaseUrl() + LinkProvider.getUserNotificationSettingUri(userIdentity.getRemoteId());
          break;
        }
        default: {
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri("");
          break;
        }
      }
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = (SpaceService) getPortalContainer().getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Gets identityManager
   * @return
   */
  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = (IdentityManager) getPortalContainer().getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }
  
  /**
   * Gets activity Manager instance.
   * @return activityManager
   * @see ActivityManager
   */
  private ActivityManager getActivityManager() {
    if (activityManager == null) {
      activityManager = (ActivityManager) getPortalContainer().getComponentInstanceOfType(ActivityManager.class);
    }
    return activityManager;
  }
  
  /**
   * Gets identityManager
   * @return
   */
  private RelationshipManager getRelationshipManager() {
    if (relationshipManager == null) {
      relationshipManager = (RelationshipManager) getPortalContainer().getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
  }
  
  /**
   * Gets Portal Container instance.
   * @return portalContainer
   * @see PortalContainer
   */
  private ExoContainer getPortalContainer() {
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
    if (exoContainer == null) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return exoContainer;
  }
}
