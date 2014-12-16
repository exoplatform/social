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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.net.WebSocketBootstrap;
import org.exoplatform.commons.notification.net.WebSocketServer;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Nov 26, 2014  
 */
@Path("social/intranet-notification")
public class IntranetNotificationRestService implements ResourceContainer {
  
  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;

  /**
   * Processes the "Accept the invitation to connect" action between 2 users and update notification.
   * 
   * @param senderId The sender's remote Id.
   * @param receiverId The receiver's remote Id.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/social/intranet-notifications/confirmInvitationToConnect/john/root
   * @throws Exception
   */
  @GET
  @Path("confirmInvitationToConnect/{senderId}/{receiverId}")
  public void confirmInvitationToConnect(@PathParam("senderId") String senderId,
                                           @PathParam("receiverId") String receiverId) throws Exception {
    //update notification
    NotificationInfo info = new NotificationInfo();
    info.key(new PluginKey("RelationshipReceivedRequestPlugin"));
    info.setFrom(senderId);
    info.setTo(receiverId);
    Map<String, String> ownerParameter = new HashMap<String, String>();
    ownerParameter.put("sender", senderId);
    ownerParameter.put("status", "accepted");
    info.setOwnerParameter(ownerParameter);
    sendBackNotif(info);

    //
    Identity sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true); 
    Identity receiver = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getRelationshipManager().confirm(sender, receiver);
  }
  
  /**
   * Processes the "Deny the invitation to connect" action between 2 users
   * 
   * @param senderId The sender's remote Id.
   * @param receiverId The receiver's remote Id.
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/intranet-notifications/ignoreInvitationToConnect/john/root
   * @throws Exception
   */
  @GET
  @Path("ignoreInvitationToConnect/{senderId}/{receiverId}")
  public void ignoreInvitationToConnect(@PathParam("senderId") String senderId,
                                             @PathParam("receiverId") String receiverId) throws Exception {
    Identity sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    Identity receiver = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getRelationshipManager().deny(sender, receiver);
  }
  
  /**
   * Processes the "Accept the invitation to join a space" action and update notification.
   * 
   * @param userId The invitee's remote Id.
   * @param spaceId Id of the space.
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/intranet-notifications/acceptInvitationToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @throws Exception
   */
  @GET
  @Path("acceptInvitationToJoinSpace/{spaceId}/{userId}")
  public void acceptInvitationToJoinSpace(@PathParam("spaceId") String spaceId,
                                               @PathParam("userId") String userId) throws Exception {
    //update notification
    NotificationInfo info = new NotificationInfo();
    info.setTo(userId);
    info.key(new PluginKey("SpaceInvitationPlugin"));
    Map<String, String> ownerParameter = new HashMap<String, String>();
    ownerParameter.put("spaceId", spaceId);
    ownerParameter.put("status", "accepted");
    info.setOwnerParameter(ownerParameter);
    sendBackNotif(info);
    
    //
    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().addMember(space, userId);
  }
  
  /**
   * Processes the "Deny the invitation to join a space" action.
   * 
   * @param userId The invitee's remote Id.
   * @param spaceId Id of the space.
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/intranet-notifications/ignoreInvitationToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @throws Exception
   */
  @GET
  @Path("ignoreInvitationToJoinSpace/{spaceId}/{userId}")
  public void ignoreInvitationToJoinSpace(@PathParam("spaceId") String spaceId,
                                              @PathParam("userId") String userId) throws Exception {
    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().removeInvitedUser(space, userId);
  }
  
  /**
   * Adds a member to a space and update notification.
   * 
   * @param userId The remote Id of the user who requests for joining the space.
   * @param spaceId Id of the space.
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/intranet-notifications/validateRequestToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @throws Exception
   */
  @GET
  @Path("validateRequestToJoinSpace/{spaceId}/{userId}")
  public void validateRequestToJoinSpace(@PathParam("spaceId") String spaceId,
                                              @PathParam("userId") String userId) throws Exception {
    //update notification
    NotificationInfo info = new NotificationInfo();
    info.setTo(ConversationState.getCurrent().getIdentity().getUserId());
    info.key(new PluginKey("RequestJoinSpacePlugin"));
    Map<String, String> ownerParameter = new HashMap<String, String>();
    ownerParameter.put("spaceId", spaceId);
    ownerParameter.put("request_from", userId);
    ownerParameter.put("status", "accepted");
    info.setOwnerParameter(ownerParameter);
    sendBackNotif(info);
    
    //
    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().addMember(space, userId);
  }
  
  /**
   * Refuses a user's request for joining a space. 
   * 
   * @param userId The remote Id of the user who requests for joining the space.
   * @param spaceId Id of the space.
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/intranet-notifications/refuseRequestToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @throws Exception
   */
  @GET
  @Path("refuseRequestToJoinSpace/{spaceId}/{userId}")
  public void refuseRequestToJoinSpace(@PathParam("spaceId") String spaceId,
                                           @PathParam("userId") String userId) throws Exception {
    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().removePendingUser(space, userId);
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
  
  private void sendBackNotif(NotificationInfo notification) {
    NotificationContext nCtx = NotificationContextImpl.cloneInstance();
    AbstractNotificationPlugin plugin = nCtx.getPluginContainer().getPlugin(notification.getKey());
    if (plugin == null) {
      return;
    }
    try {
      notification.setLastModifiedDate(Calendar.getInstance());
      nCtx.setNotificationInfo(notification);
      String message = plugin.buildUIMessage(nCtx);
      WebSocketBootstrap.sendJsonMessage(WebSocketServer.NOTIFICATION_WEB_IDENTIFIER, notification.getTo(), message);
    } catch (Exception e) {
      System.out.println("error : " + e.getMessage());
    }
  }
}
