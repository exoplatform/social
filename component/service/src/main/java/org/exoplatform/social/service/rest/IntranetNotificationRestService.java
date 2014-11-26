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

import static org.exoplatform.social.service.rest.RestChecker.checkAuthenticatedRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
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
   * Processes the "Accept the invitation to connect" action between 2 users and add new notification
   * 
   * @param senderId The sender's remote Id.
   * @param receiverId The receiver's remote Id.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/social/intranet-notifications/confirmInvitationToConnect/john/root
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
    
    //update or delete notification
    
  
   return Response.ok().build();
  }
  
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
    
    //delete notification

    return Response.ok().build();
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
}
