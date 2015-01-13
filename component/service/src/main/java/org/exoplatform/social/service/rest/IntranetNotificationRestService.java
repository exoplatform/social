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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.net.WebNotificationSender;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.impl.AbstractStorage;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Nov 26, 2014  
 */
@Path("social/intranet-notification")
public class IntranetNotificationRestService extends AbstractStorage implements ResourceContainer {
  private static final Log LOG = ExoLogger.getLogger(IntranetNotificationRestService.class);
  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;
  public final static String MESSAGE_JSON_FILE_NAME = "message.json";
  /**
   * Processes the "Accept the invitation to connect" action between 2 users and update notification.
   * 
   * @param senderId The sender's remote Id.
   * @param receiverId The receiver's remote Id.
   * @notificationId of the web notification message
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/social/intranet-notifications/confirmInvitationToConnect/john/root/<notificationId>/message.json
   * @throws Exception
   */
  @GET
  @Path("confirmInvitationToConnect/{senderId}/{receiverId}/{notificationId}/message.{format}")
  public Response confirmInvitationToConnect(@Context UriInfo uriInfo,
                                             @PathParam("senderId") String senderId,
                                             @PathParam("receiverId") String receiverId,
                                             @PathParam("notificationId") String notificationId,
                                             @PathParam("format") String format) throws Exception {
    String[] mediaTypes = new String[] { "json", "xml" };
    MediaType mediaType = Util.getMediaType(format, mediaTypes);
    //update notification
    NotificationInfo info = CommonsUtils.getService(WebNotificationStorage.class).get(notificationId);
    info.key(new PluginKey("RelationshipReceivedRequestPlugin"));
    info.setFrom(senderId);
    info.setTo(receiverId);
    Map<String, String> ownerParameter = new HashMap<String, String>();
    ownerParameter.put("sender", senderId);
    ownerParameter.put("status", "accepted");
    info.setOwnerParameter(ownerParameter);
    MessageInfo messageInfo = sendBackNotif(info);
    if (messageInfo == null) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    //
    Identity sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true); 
    Identity receiver = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getRelationshipManager().confirm(sender, receiver);
    
    
    return Util.getResponse(messageInfo, uriInfo, mediaType, Response.Status.OK);
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
  @Path("ignoreInvitationToConnect/{senderId}/{receiverId}/{notificationId}")
  public void ignoreInvitationToConnect(@PathParam("senderId") String senderId,
                                             @PathParam("receiverId") String receiverId,
                                             @PathParam("notificationId") String notificationId) throws Exception {
    Identity sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    Identity receiver = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getRelationshipManager().deny(sender, receiver);
    CommonsUtils.getService(WebNotificationStorage.class).remove(notificationId);
  }
  
  /**
   * Processes the "Accept the invitation to join a space" action and update notification.
   * 
   * @param userId The invitee's remote Id.
   * @param spaceId Id of the space.
   * @notificationId of the web notification message
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/intranet-notifications/acceptInvitationToJoinSpace/e1cacf067f0001015ac312536462fc6b/john/<notificationId>/message.json
   * @throws Exception
   */
  @GET
  @Path("acceptInvitationToJoinSpace/{spaceId}/{userId}/{notificationId}/message.{format}")
  public Response acceptInvitationToJoinSpace(@Context UriInfo uriInfo,
                                              @PathParam("spaceId") String spaceId,
                                               @PathParam("userId") String userId,
                                               @PathParam("notificationId") String notificationId,
                                               @PathParam("format") String format) throws Exception {
    String[] mediaTypes = new String[] { "json", "xml" };
    MediaType mediaType = Util.getMediaType(format, mediaTypes);
    
    //update notification
    NotificationInfo info = CommonsUtils.getService(WebNotificationStorage.class).get(notificationId);
    info.setTo(userId);
    info.key(new PluginKey("SpaceInvitationPlugin"));
    Map<String, String> ownerParameter = new HashMap<String, String>();
    ownerParameter.put("spaceId", spaceId);
    ownerParameter.put("status", "accepted");
    info.setOwnerParameter(ownerParameter);
    MessageInfo messageInfo = sendBackNotif(info);
    if (messageInfo == null) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }

    //
    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().addMember(space, userId);
    return Util.getResponse(messageInfo, uriInfo, mediaType, Response.Status.OK);
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
  @Path("ignoreInvitationToJoinSpace/{spaceId}/{userId}/{notificationId}")
  public void ignoreInvitationToJoinSpace(@PathParam("spaceId") String spaceId,
                                              @PathParam("userId") String userId,
                                              @PathParam("notificationId") String notificationId) throws Exception {
    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().removeInvitedUser(space, userId);
    //
    CommonsUtils.getService(WebNotificationStorage.class).remove(notificationId);
  }
  
  /**
   * Adds a member to a space and update notification.
   * 
   * @param userId The remote Id of the user who requests for joining the space.
   * @param spaceId Id of the space.
   * @param currentUserId the userId
   * @notificationId of the web notification message
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/intranet-notifications/validateRequestToJoinSpace/e1cacf067f0001015ac312536462fc6b/john/<notificationId>/message.json
   * @throws Exception
   */
  @GET
  @Path("validateRequestToJoinSpace/{spaceId}/{requestUserId}/{currentUserId}/{notificationId}//message.{format}")
  public Response validateRequestToJoinSpace(@Context UriInfo uriInfo,
                                         @PathParam("spaceId") String spaceId,
                                         @PathParam("requestUserId") String requestUserId,
                                         @PathParam("currentUserId") String currentUserId,
                                         @PathParam("notificationId") String notificationId,
                                         @PathParam("format") String format) throws Exception {
    
    String[] mediaTypes = new String[] { "json", "xml" };
    MediaType mediaType = Util.getMediaType(format, mediaTypes);
    //update notification
    NotificationInfo info = CommonsUtils.getService(WebNotificationStorage.class).get(notificationId);
    info.setTo(currentUserId);
    info.key(new PluginKey("RequestJoinSpacePlugin"));
    Map<String, String> ownerParameter = new HashMap<String, String>();
    ownerParameter.put("spaceId", spaceId);
    ownerParameter.put("request_from", requestUserId);
    ownerParameter.put("status", "accepted");
    info.setOwnerParameter(ownerParameter);
    MessageInfo messageInfo = sendBackNotif(info);
    if (messageInfo == null) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    //
    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().addMember(space, requestUserId);
    return Util.getResponse(messageInfo, uriInfo, mediaType, Response.Status.OK);
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
  @Path("refuseRequestToJoinSpace/{spaceId}/{userId}/{notificationId}")
  public void refuseRequestToJoinSpace(@PathParam("spaceId") String spaceId,
                                           @PathParam("userId") String userId,
                                           @PathParam("notificationId") String notificationId) throws Exception {
    Space space = getSpaceService().getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    getSpaceService().removePendingUser(space, userId);
    CommonsUtils.getService(WebNotificationStorage.class).remove(notificationId);
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
  
  private MessageInfo sendBackNotif(NotificationInfo notification) {
    NotificationContext nCtx = NotificationContextImpl.cloneInstance().setNotificationInfo(notification);
    BaseNotificationPlugin plugin = nCtx.getPluginContainer().getPlugin(notification.getKey());
    if (plugin == null) {
      return null;
    }
    try {
      AbstractChannel channel = nCtx.getChannelManager().getChannel(ChannelKey.key(WebChannel.ID));
      AbstractTemplateBuilder builder = channel.getTemplateBuilder(notification.getKey());
      MessageInfo msg = builder.buildMessage(nCtx);
      msg.setMoveTop(false);
      WebNotificationSender.sendJsonMessage(notification.getTo(), msg);
      notification.setTitle(msg.getBody());
      notification.with(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey(), "true")
                  .with(NotificationMessageUtils.READ_PORPERTY.getKey(), "false");
      CommonsUtils.getService(WebNotificationStorage.class).update(notification, false);
      return msg;
    } catch (Exception e) {
      LOG.error("Can not send the message to Intranet.", e.getMessage());
      return null;
    }
  }
}
