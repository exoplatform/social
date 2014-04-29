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

import javax.servlet.http.HttpServletRequest;
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
 * Provides REST Services for manipulating jobs related to notifications.
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
   * Processes the "Invite to connect" action between two users, sender and receiver, then redirects to the receiver's profile page.
   * 
   * @param senderId The sender's remote Id.
   * @param receiverId The receiver's remote Id. 
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/social/notifications/inviteToConnect/john/root
   * @return Redirects to the receiver's profile page.
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
   * Processes the "Accept the invitation to connect" action between 2 users, then redirects to the sender's activity stream.
   * 
   * @param senderId The sender's remote Id.
   * @param receiverId The receiver's remote Id.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/social/notifications/confirmInvitationToConnect/john/root
   * @return Redirects to the sender's activity stream.
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
   * Processes the "Deny the invitation to connect" action between 2 users, then redirects to the receiver's page of received invitations.
   * A message informing that the receiver ignored the sender's invitation will be displayed.
   * 
   * @param senderId The sender's remote Id.
   * @param receiverId The receiver's remote Id.
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/ignoreInvitationToConnect/john/root
   * @return Redirects to the receiver's page of received invitations and displays a message.
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
   * Processes the "Accept the invitation to join a space" action and redirects to the space homepage.
   * 
   * @param userId The invitee's remote Id.
   * @param spaceId Id of the space.
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/acceptInvitationToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @return Redirects to the space's homepage.
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
   * Processes the "Deny the invitation to join a space" action, then redirects to the page of all spaces.
   * A message informing that the invitee has denied to join the space will be displayed.
   * 
   * @param userId The invitee's remote Id.
   * @param spaceId Id of the space.
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/ignoreInvitationToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @return Redirects to the page of all spaces and displays a message.
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
    String targetURL = Util.getBaseUrl();
    if (getSpaceService().isMember(space, userId)) {
      targetURL = targetURL + LinkProvider.getRedirectUri("all-spaces?feedbackMessage=SpaceInvitationAlreadyMember&spaceId=" + spaceId);
    } else {
      getSpaceService().removeInvitedUser(space, userId);
      //redirect to all spaces and display a feedback message
      targetURL = targetURL + LinkProvider.getRedirectUri("all-spaces?feedbackMessage=SpaceInvitationRefuse&spaceId=" + spaceId);
    }
    
    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  /**
   * Adds a member to a space, then redirects to the space's members page. 
   * A message informing the added user is already member of the space or not will be displayed.
   * This action is only for the space manager. 
   * 
   * @param userId The remote Id of the user who requests for joining the space.
   * @param spaceId Id of the space.
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/validateRequestToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @return Redirects to the space's members page and displays the message.
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
   * Refuses a user's request for joining a space, then redirects to 
   * the space's members page. This action is only for the space manager. 
   * 
   * @param userId The remote Id of the user who requests for joining the space.
   * @param spaceId Id of the space.
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/refuseRequestToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @return Redirects to the space's members page.
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
    String baseUrl = Util.getBaseUrl();
    String spaceHomeUrl = LinkProvider.getActivityUriForSpace(space.getPrettyName(), space.getGroupId().replace("/spaces/", ""));
    StringBuilder targetURL = new StringBuilder().append(baseUrl).append(spaceHomeUrl).append("/settings/members?feedbackMessage=");
    if (getSpaceService().isMember(space, userId)) {
      targetURL.append("SpaceRequestAlreadyMember&spaceId=").append(spaceId).append("&userName=").append(userId);
    } else {
      getSpaceService().removePendingUser(space, userId);
      //redirect to space's members page and display a feedback message
      targetURL.append("SpaceRequestRefuse&userName=").append(userId);
    }

    // redirect to target page
    return Response.seeOther(URI.create(targetURL.toString())).build();
  }
  
  /**
   * Redirects the current user to an associated page, such as user activity stream, portal homepage,
   * space homepage and user profile.
   * 
   * @param type Type of the redirected page.
   * @param objectId Id of the associated type that can be activity Id, space Id, or user remote Id.
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/notifications/redirectUrl/view_full_activity/e1d2870c7f0001014e32114f6ff8a7ab
   * @return Redirects to the associated page.
   * @throws Exception
   */
  @GET
  @Path("redirectUrl/{type}/{objectId}")
  public Response redirectUrl(@Context UriInfo uriInfo,
                              @PathParam("type") String type,
                              @PathParam("objectId") String objectId) throws Exception {
    Space space = null;
    Identity userIdentity = null;
    String targetURL = null;
    
    HttpServletRequest currentServletRequest = Util.getCurrentServletRequest();
    boolean hasLoggedIn = (currentServletRequest.getRemoteUser() != null);
    String redirectLink = null;
    if (!hasLoggedIn) {
      //If user is not authenticated, the query parameter will be removed after login
      //so we will not redirect to an activity with query parameter but with path parameter
      //this new link will be processed on activity stream portlet
      redirectLink = Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "/redirect/" + type + "/" + objectId);
    }
    
    try {
      checkAuthenticatedRequest();
      URL_TYPE urlType = URL_TYPE.valueOf(type);
      switch (urlType) {
        case view_full_activity: {
          targetURL = hasLoggedIn ? Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + objectId) : redirectLink;
          break;
        }
        case view_full_activity_highlight_comment: {
          String activityId = objectId.split("-")[0];
          String commentId = objectId.split("-")[1];
          targetURL = hasLoggedIn ? Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + activityId + "#comment-" + commentId) : redirectLink;
          break;
        }
        case view_likers_activity: {
          targetURL = hasLoggedIn ? Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + objectId + "&likes=1") : redirectLink;
          break;
        }
        case reply_activity: {
          targetURL = hasLoggedIn ? Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + objectId + "&comment=1") : redirectLink;
          break;
        }
        case reply_activity_highlight_comment: {
          String activityId = objectId.split("-")[0];
          String commentId = objectId.split("-")[1];
          targetURL = hasLoggedIn ? Util.getBaseUrl() + LinkProvider.getRedirectUri(ACTIVITY_ID_PREFIX + "?id=" + activityId + "#comment-" + commentId + "&comment=1") : redirectLink;
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
   * Gets a service which manages all things related to spaces.
   * @return The SpaceService.
   * @see SpaceService
   */
  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = (SpaceService) getPortalContainer().getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Gets a service which manages all things related to identities.
   * @return The IdentityManager.
   * @see IdentityManager
   */
  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = (IdentityManager) getPortalContainer().getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }
  
  /**
   * Gets a service which manages all things related to activities.
   * @return The ActivityManager.
   * @see ActivityManager
   */
  private ActivityManager getActivityManager() {
    if (activityManager == null) {
      activityManager = (ActivityManager) getPortalContainer().getComponentInstanceOfType(ActivityManager.class);
    }
    return activityManager;
  }
  
  /**
   * Gets a service which manages all things related to relationship.
   * @return The RelationshipManager.
   * @see RelationshipManager
   */
  private RelationshipManager getRelationshipManager() {
    if (relationshipManager == null) {
      relationshipManager = (RelationshipManager) getPortalContainer().getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
  }
  
  /**
   * Gets a Portal Container instance.
   * @return The PortalContainer.
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
