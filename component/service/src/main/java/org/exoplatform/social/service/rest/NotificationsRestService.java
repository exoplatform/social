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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
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
  
  private static final Log LOG = ExoLogger.getLogger(NotificationsRestService.class);
  
  private static String ACTIVITY_ID_PREFIX = "#activityContainer";
  
  public NotificationsRestService() {
  }
  
  @GET
  @Path("inviteToConnect/{senderId}/{receiverId}")
  public Response inviteToConnect(@Context UriInfo uriInfo,
                                  @PathParam("senderId") String senderId,
                                  @PathParam("receiverId") String receiverId) throws Exception {
    checkAuthenticatedRequest();
    
    Identity sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true); 
    Identity receiver = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    getRelationshipManager().inviteToConnect(sender, receiver);
  
    String targetURL = Util.getBaseUrl() + LinkProvider.getUserProfileUri(receiver.getRemoteId());
    
    // redirect to target page
   return Response.seeOther(URI.create(targetURL)).build();
  }
  
  @GET
  @Path("confirmInvitationToConnect/{senderId}/{receiverId}")
  public Response confirmInvitationToConnect(@Context UriInfo uriInfo,
                                             @PathParam("senderId") String senderId,
                                             @PathParam("receiverId") String receiverId) throws Exception {
    checkAuthenticatedRequest();
    
    Identity sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true); 
    Identity receiver = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    getRelationshipManager().confirm(sender, receiver);
  
    String targetURL = Util.getBaseUrl() + LinkProvider.getUserActivityUri(sender.getRemoteId());
    
    // redirect to target page
   return Response.seeOther(URI.create(targetURL)).build();
  }
  
  @GET
  @Path("ignoreInvitationToConnect/{senderId}/{receiverId}")
  public Response ignoreInvitationToConnect(@Context UriInfo uriInfo,
                                            @PathParam("senderId") String senderId,
                                            @PathParam("receiverId") String receiverId) throws Exception {
    checkAuthenticatedRequest();

    Identity sender = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderId, true);
    Identity receiver = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverId, true);
    getRelationshipManager().deny(sender, receiver);

    String targetURL = Util.getBaseUrl() + LinkProvider.getUserConnectionsYoursUri(receiver.getRemoteId());

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  @GET
  @Path("acceptInvitationToJoinSpace/{spaceId}/{userId}")
  public Response acceptInvitationToJoinSpace(@Context UriInfo uriInfo,
                                              @PathParam("spaceId") String spaceId,
                                              @PathParam("userId") String userId) throws Exception {
    checkAuthenticatedRequest();

    Space space = getSpaceService().getSpaceById(spaceId);
    getSpaceService().addMember(space, userId);

    String targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getGroupId().replace("/spaces/", ""), space.getPrettyName());

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  @GET
  @Path("ignoreInvitationToJoinSpace/{spaceId}/{userId}")
  public Response ignoreInvitationToJoinSpace(@Context UriInfo uriInfo,
                                              @PathParam("spaceId") String spaceId,
                                              @PathParam("userId") String userId) throws Exception {
    checkAuthenticatedRequest();

    Space space = getSpaceService().getSpaceById(spaceId);
    getSpaceService().removeInvitedUser(space, userId);

    String targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getGroupId().replace("/spaces/", ""), space.getPrettyName());
    
    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  @GET
  @Path("validateRequestToJoinSpace/{spaceId}/{userId}")
  public Response validateRequestToJoinSpace(@Context UriInfo uriInfo,
                                             @PathParam("spaceId") String spaceId,
                                             @PathParam("userId") String userId) throws Exception {
    checkAuthenticatedRequest();

    Space space = getSpaceService().getSpaceById(spaceId);
    getSpaceService().addMember(space, userId);

    String targetURL = Util.getBaseUrl() + LinkProvider.getActivityUriForSpace(space.getGroupId().replace("/spaces/", ""), space.getPrettyName()) + "/settings";

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  @GET
  @Path("replyActivity/{activityId}/{userId}")
  public Response replyActivity(@Context UriInfo uriInfo,
                                @PathParam("userId") String userId,
                                @PathParam("activityId") String activityId) throws Exception {
    checkAuthenticatedRequest();

    ExoSocialActivity activity = getActivityManager().getActivity(activityId);
    Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);

    String targetURL = Util.getBaseUrl() + LinkProvider.getUserActivityUri(identity.getRemoteId()) + ACTIVITY_ID_PREFIX + activity.getId();

    // redirect to target page
    return Response.seeOther(URI.create(targetURL)).build();
  }
  
  @GET
  @Path("viewFullDiscussion/{activityId}/{userId}")
  public Response viewFullDiscussion(@Context UriInfo uriInfo,
                                     @PathParam("userId") String userId,
                                     @PathParam("activityId") String activityId) throws Exception {
    checkAuthenticatedRequest();

    ExoSocialActivity activity = getActivityManager().getActivity(activityId);
    Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);

    String targetURL = Util.getBaseUrl() + LinkProvider.getUserActivityUri(identity.getRemoteId()) + ACTIVITY_ID_PREFIX + activity.getId();

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
