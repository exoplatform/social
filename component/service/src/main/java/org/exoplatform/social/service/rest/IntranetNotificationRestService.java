/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static org.exoplatform.social.service.rest.RestChecker.checkAuthenticatedUserPermission;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.web.security.csrf.ExoCSRFCheck;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.net.WebNotificationSender;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
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
  private WebNotificationStorage webNotificationStorage;

  public final static String MESSAGE_JSON_FILE_NAME = "message.json";

  public IntranetNotificationRestService(IdentityManager identityManager, RelationshipManager relationshipManager,
                                         SpaceService spaceService, WebNotificationStorage webNotificationStorage) {
    this.identityManager = identityManager;
    this.relationshipManager = relationshipManager;
    this.spaceService = spaceService;
    this.webNotificationStorage = webNotificationStorage;
  }

  /**
   * Processes the "Accept the invitation to connect" action between 2 users and update notification.
   *
   * @param senderId The remote Id of the identity who sent the invitation.
   * @param receiverId The remote Id of the identity who received the invitation.
   * @notificationId Id of the web notification message
   * @authentication
   * @request
   * GET: {@code http://localhost:8080/rest/social/intranet-notifications/confirmInvitationToConnect/john/root/<notificationId>/message.json}
   * @throws Exception
   */
  @GET
  @RolesAllowed("users")
  @ExoCSRFCheck
  @Path("confirmInvitationToConnect/{senderId}/{receiverId}/{notificationId}/message.{format}")
  public Response confirmInvitationToConnect(@Context UriInfo uriInfo,
                                             @PathParam("senderId") String senderId,
                                             @PathParam("receiverId") String receiverId,
                                             @PathParam("notificationId") String notificationId,
                                             @PathParam("format") String format) throws Exception {
    //Check authenticated user
    checkAuthenticatedUserPermission(receiverId);

    Identity sender = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    Identity receiver = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    Relationship invitation = relationshipManager.get(sender, receiver);
    if(invitation == null || !invitation.getStatus().equals(Relationship.Type.PENDING) || !invitation.isReceiver(receiver)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    //
    String[] mediaTypes = new String[] { "json", "xml" };
    MediaType mediaType = Util.getMediaType(format, mediaTypes);
    //update notification
    NotificationInfo info = webNotificationStorage.get(notificationId);
    info.key(new PluginKey("RelationshipReceivedRequestPlugin"));
    info.setFrom(senderId);
    info.setTo(receiverId);
    Map<String, String> ownerParameter = new HashMap<>();
    ownerParameter.put("sender", senderId);
    ownerParameter.put("status", "accepted");
    info.setOwnerParameter(ownerParameter);
    MessageInfo messageInfo = sendBackNotif(info);
    if (messageInfo == null) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }

    relationshipManager.confirm(sender, receiver);

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
  @RolesAllowed("users")
  @ExoCSRFCheck
  @Path("ignoreInvitationToConnect/{senderId}/{receiverId}/{notificationId}/message.{format}")
  public Response ignoreInvitationToConnect(@Context UriInfo uriInfo,
                                          @PathParam("senderId") String senderId,
                                          @PathParam("receiverId") String receiverId,
                                          @PathParam("notificationId") String notificationId,
                                          @PathParam("format") String format) throws Exception {
    //Check authenticated user
    checkAuthenticatedUserPermission(receiverId);
    //
    String[] mediaTypes = new String[] { "json", "xml" };
    MediaType mediaType = Util.getMediaType(format, mediaTypes);
    //
    Identity sender = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    Identity receiver = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    if (sender == null || receiver == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    relationshipManager.deny(sender, receiver);
    webNotificationStorage.remove(notificationId);
    //
    return Util.getResponse(getUserWebNotification(receiverId), uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Processes the "Accept the invitation to join a space" action and update notification.
   *
   * @param userId The invitee's remote Id.
   * @param spaceId Id of the space.
   * @notificationId of the web notification message
   * @authentication
   * @request
   * GET: {@code localhost:8080/rest/social/intranet-notifications/acceptInvitationToJoinSpace/e1cacf067f0001015ac312536462fc6b/john/<notificationId>/message.json}
   * @throws Exception
   */
  @GET
  @RolesAllowed("users")
  @ExoCSRFCheck
  @Path("acceptInvitationToJoinSpace/{spaceId}/{userId}/{notificationId}/message.{format}")
  public Response acceptInvitationToJoinSpace(@Context UriInfo uriInfo,
                                              @PathParam("spaceId") String spaceId,
                                               @PathParam("userId") String userId,
                                               @PathParam("notificationId") String notificationId,
                                               @PathParam("format") String format) throws Exception {
    //Check authenticated user
    checkAuthenticatedUserPermission(userId);
    //
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    List<String> invitedUsers = Arrays.asList(space.getInvitedUsers());
    if (!invitedUsers.contains(userId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    //
    spaceService.addMember(space, userId);
    String[] mediaTypes = new String[] { "json", "xml" };
    MediaType mediaType = Util.getMediaType(format, mediaTypes);

    //update notification
    NotificationInfo info = webNotificationStorage.get(notificationId);
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
  @RolesAllowed("users")
  @ExoCSRFCheck
  @Path("ignoreInvitationToJoinSpace/{spaceId}/{userId}/{notificationId}/message.{format}")
  public Response ignoreInvitationToJoinSpace(@Context UriInfo uriInfo,
                                           @PathParam("spaceId") String spaceId,
                                           @PathParam("userId") String userId,
                                           @PathParam("notificationId") String notificationId,
                                           @PathParam("format") String format) throws Exception {
    //Check authenticated user
    checkAuthenticatedUserPermission(userId);
    //
    String[] mediaTypes = new String[] { "json", "xml" };
    MediaType mediaType = Util.getMediaType(format, mediaTypes);
    //
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    spaceService.removeInvitedUser(space, userId);
    //
    webNotificationStorage.remove(notificationId);
    
    return Util.getResponse(getUserWebNotification(userId), uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Adds a member to a space and update notification.
   *
   * @param uriInfo
   * @param spaceId Id of the space.
   * @param requestUserId The remote Id of the user who requests for joining the space.
   * @param currentUserId the userId
   * @param notificationId
   * @param format
   * @notificationId of the web notification message
   * @authentication
   * @request
   * GET: {@code localhost:8080/rest/social/intranet-notifications/validateRequestToJoinSpace/e1cacf067f0001015ac312536462fc6b/john/<notificationId>/message.json}
   * @throws Exception
   */
  @GET
  @RolesAllowed("users")
  @ExoCSRFCheck
  @Path("validateRequestToJoinSpace/{spaceId}/{requestUserId}/{currentUserId}/{notificationId}/message.{format}")
  public Response validateRequestToJoinSpace(@Context UriInfo uriInfo,
                                         @PathParam("spaceId") String spaceId,
                                         @PathParam("requestUserId") String requestUserId,
                                         @PathParam("currentUserId") String currentUserId,
                                         @PathParam("notificationId") String notificationId,
                                         @PathParam("format") String format) throws Exception {
    //Check authenticated user
    checkAuthenticatedUserPermission(currentUserId);

    //check space existence
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    //check that caller is manager
    List<String> managers = Arrays.asList(space.getManagers());
    if (!managers.contains(currentUserId)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    //check that requestUserId is in the pending user list
    List<String> pendingUsers = Arrays.asList(space.getPendingUsers());
    if (!pendingUsers.contains(requestUserId)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    //
    String[] mediaTypes = new String[] { "json", "xml" };
    MediaType mediaType = Util.getMediaType(format, mediaTypes);
    //update notification
    NotificationInfo info = webNotificationStorage.get(notificationId);
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


    spaceService.addMember(space, requestUserId);
    return Util.getResponse(messageInfo, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Refuses a user's request for joining a space. 
   *
   * @param uriInfo
   * @param spaceId Id of the space.
   * @param requestUserId The remote Id of the user who requests for joining the space.
   * @param currentUserId
   * @param notificationId
   * @param format
   * @authentication
   * @request
   * GET: localhost:8080/rest/social/intranet-notifications/refuseRequestToJoinSpace/e1cacf067f0001015ac312536462fc6b/john
   * @throws Exception
   */
  @GET
  @RolesAllowed("users")
  @ExoCSRFCheck
  @Path("refuseRequestToJoinSpace/{spaceId}/{requestUserId}/{currentUserId}/{notificationId}/message.{format}")
  public Response refuseRequestToJoinSpace(@Context UriInfo uriInfo,
                                        @PathParam("spaceId") String spaceId,
                                        @PathParam("requestUserId") String requestUserId,
                                        @PathParam("currentUserId") String currentUserId,
                                        @PathParam("notificationId") String notificationId,
                                        @PathParam("format") String format) throws Exception {
    //Check authenticated user
    checkAuthenticatedUserPermission(currentUserId);
    //
    String[] mediaTypes = new String[] { "json", "xml" };
    MediaType mediaType = Util.getMediaType(format, mediaTypes);
    //
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    spaceService.removePendingUser(space, requestUserId);
    webNotificationStorage.remove(notificationId);
    //
    return Util.getResponse(getUserWebNotification(currentUserId), uriInfo, mediaType, Response.Status.OK);
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
      webNotificationStorage.update(notification, false);
      return msg;
    } catch (Exception e) {
      LOG.error("Can not send the message to Intranet.", e.getMessage());
      return null;
    }
  }

  private Map<String, Boolean> getUserWebNotification(String userId) throws Exception {
    Map<String, Boolean> data = new HashMap<String, Boolean>();
    List<NotificationInfo> notifications = webNotificationStorage.get(new WebNotificationFilter(userId), 0, 1);
    data.put("showViewAll", (notifications.size() > 0));
    return data;
  }

}
