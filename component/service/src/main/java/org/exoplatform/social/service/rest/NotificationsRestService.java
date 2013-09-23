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
    user, space, space_members, reply_activity, reply_activity_highlight_comment, view_full_activity,
    view_full_activity_highlight_comment, view_likers_activity, portal_home, all_space,
    connections, notification_settings, connections_request, space_invitation, user_activity_stream;
  }
  
  public NotificationsRestService() {
  }
  
  /**
   * Process action "invite to connect" between two users, sender and receiver, then redirect to the page of receiver's profile
   * 
   * @param senderId the remote id of the sender
   * @param receiverId the remote id of the receiver
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/social/notifications/inviteToConnect/john/root
   * @return redirect to the page of receiver's profile
   * @throws Exception
   */
  @GET
  @Path("inviteToConnect/{receiverId}/{senderId}")
  public Response inviteToConnect(@Context UriInfo uriInfo,
                                  @PathParam("receiverId") String receiverId,
                                  @PathParam("senderId") String senderId) throws Exception {
    checkAuthenticatedRequest();
    
    Identity sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, (senderId != null ? senderId : ConversationState.getCurrent().getIdentity().getUserId()), true);
    Identity receiver = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getRelationshipManager().inviteToConnect(sender, receiver);
  
    String targetURL = Util.getBaseUrl() + LinkProvider.getUserProfileUri(receiver.getRemoteId());
    
    // redirect to target page
   return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * Process action "accept the invitation to connect" between 2 users, then redirect to the activity stream of the sender
   * 
   * @param senderId the remote id of the sender
   * @param receiverId the remote id of the receiver
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/social/notifications/confirmInvitationToConnect/john/root
   * @return redirect to the activity stream of the sender
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
   * Process action "deny the invitation to connect" between 2 users, then redirect to the connection requested page
   * of the receiver and display a message to inform that the receiver ignored the invitation of the sender
   * 
   * @param senderId the remote id of the sender
   * @param receiverId the remote id of the receiver
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/ignoreInvitationToConnect/john/root
   * @return redirect to the connection requested page of the receiver and display a message
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

    //redirect to the requesters list and display a feedback message
    String targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri("connexions/receivedInvitations/" + receiver.getRemoteId() + "?feedbackMessage=ConnectionRequestRefuse&userName=" + sender.getRemoteId());

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * Process action "accept the invitation to join space" and redirect user to the space's home page
   * 
   * @param userId the remote id of the user who is invited to join space
   * @param spaceId the id of space
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/acceptInvitationToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @return redirect to the space's home page
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

    String targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getPrettyName(), space.getGroupId().replace("/spaces/", ""));

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * Process action "deny the invitation to join space", redirect user to the page of all spaces and display a message
   * that he just denied to join space
   * 
   * @param userId the remote id of the user who is invited to join space
   * @param spaceId the id of space
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/ignoreInvitationToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @return redirect user to the page of all spaces and display a message
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

    //redirect to all spaces and display a feedback message
    String targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri("all-spaces?feedbackMessage=SpaceInvitationRefuse&spaceId=" + spaceId);
    
    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * This action is only for a manager of a space. This will add a user become a member of a space and redirect the 
   * manager to the members's page of the space. Following the status of the user, he is already member of space or not,
   * a message associated will be display to inform the event  
   * 
   * @param userId the remote id of the user who send the request to join space
   * @param spaceId the id of space
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/validateRequestToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @return redirect the manager to the members's page of the space and display a message associated
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
    
    StringBuilder sb = new StringBuilder().append("?feedbackMessage=");
    if (getSpaceService().isMember(space, userId)) {
      sb.append("SpaceRequestAlreadyMember&spaceId=").append(spaceId);
    } else {
      sb.append("SpaceRequestApprove");
    }
    sb.append("&userName=").append(userId);

    getSpaceService().addMember(space, userId);

    //redirect to space's members page and display a feedback message
    String targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getPrettyName(), space.getGroupId().replace("/spaces/", "")) + "/settings/members" + sb.toString();

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * This action is only for a manager of a space. This will refuse a user become a member of a space and redirect the 
   * manager to the members's page of this space.
   * 
   * @param userId the remote id of the user who send the request to join space
   * @param spaceId the id of space
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/refuseRequestToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @return redirect the manager to the members's page of the space.
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

    //redirect to space's members page and display a feedback message
    String targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getPrettyName(), space.getGroupId().replace("/spaces/", "")) + "/settings/members?feedbackMessage=SpaceRequestRefuse&userName=" + userId;

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * Depend on the type, the current user will be redirect to the associated place : user activity stream, portal home page,
   * space home page, user profile ...
   * 
   * @param type the type of the page will be redirected to: user --> user's activity stream, space --> space's home page
   * view_full_activity --> display only one activity and expand all of its comments ...
   * @param objectId id of the associated type, can be an activity's id, space's id, user's remote id ...
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/redirectUrl/view_full_activity/e1d2870c7f0001014e32114f6ff8a7ab
   * @return redirect to the associated place
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
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + objectId);
          break;
        }
        case view_full_activity_highlight_comment: {
          String activityId = objectId.split("-")[0];
          String commentId = objectId.split("-")[1];
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + activityId + "#comment-" + commentId);
          break;
        }
        case view_likers_activity: {
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + objectId + "&likes=1");
          break;
        }
        case reply_activity: {
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + objectId + "&comment=1");
          break;
        }
        case reply_activity_highlight_comment: {
          String activityId = objectId.split("-")[0];
          String commentId = objectId.split("-")[1];
          targetURL = Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + activityId + "#comment-" + commentId + "&comment=1");
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
          targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getPrettyName(), space.getGroupId().replace("/spaces/", ""));
          break;
        }
        case space_members: {
          space = getSpaceService().getSpaceById(objectId);
          targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getPrettyName(), space.getGroupId().replace("/spaces/", "")) + "/settings/members";
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
  
  /**
   * Gets the spaceService
   * @return spaceService
   * @see SpaceService
   */
  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = (SpaceService) getPortalContainer().getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Gets identityManager
   * @return identityManager
   * @see IdentityManager
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
   * @return identityManager
   * @see RelationshipManager
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
