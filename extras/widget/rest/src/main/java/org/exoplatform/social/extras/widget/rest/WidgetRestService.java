/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.extras.widget.rest;

import java.net.URI;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * The class show out capability to build widget applications that integrated with third party application.
 */
@Path("spaces/{containerName}")
public class WidgetRestService implements ResourceContainer {
  private static Log log = ExoLogger.getLogger(WidgetRestService.class.getName());

  private static final int DEFAULT_LIMIT = 20;
  
  /**
   * Redirects to space with the provided name; in case of space not existing then created before redirecting.
   * 
   * @param containerName The name of container.
   * @param portalName The name of current portal.
   * @param spaceName The name of space to be redirected.
   * @param description The description of target space.
   * @return
   */
  @GET
  @Path("go_to_space")
  public Response goToSpace(@PathParam("containerName") String containerName,
                            @QueryParam("portalName") @DefaultValue("classic") String portalName,
                            @QueryParam("spaceName") String spaceName,
                            @QueryParam("description") String description) {
    ExoContainer pc = ExoContainerContext.getContainerByName(containerName);
    // we make sure we use the right container
    ExoContainerContext.setCurrentContainer(pc);
    RequestLifeCycle.begin(pc);
    try {
      SpaceService service = (SpaceService) pc.getComponentInstanceOfType(SpaceService.class);

      Space space = service.getSpaceByPrettyName(SpaceUtils.cleanString(spaceName));
      String username = ConversationState.getCurrent().getIdentity().getUserId();

      if (space == null) {
        // If the space does not exist, we create it
        space = new Space();
        space.setDisplayName(spaceName);
        space.setPrettyName(space.getDisplayName());
        space.setRegistration(Space.OPEN);
        space.setDescription(description);
        space.setType(DefaultSpaceApplicationHandler.NAME);
        space.setVisibility(Space.PUBLIC);
        space.setPriority(Space.INTERMEDIATE_PRIORITY);
        space = service.createSpace(space, username);
      } else {
        // Otherwise we add the user as a member

        // We verify if the registrations are open to everyone
        if (!service.hasAccessPermission(space, username)) {
          if (space.getRegistration().equals(Space.OPEN)) {
            service.addMember(space, username);
          } else {
            service.addPendingUser(space, username);
          }
        }
      }

      //TODO Find solution to remove the hard code here.
      URI spaceURL = UriBuilder.fromPath("g/:spaces:{spaceURL}/{spaceURL}").build(space.getUrl(), space.getUrl());

      // We need to cleanup the session
      // The parameter portal is not really the portal name but the site name
      // inside the portal
      URI cleanupURL = UriBuilder.fromPath("/{containerName}/{spaceURL}").build(containerName, spaceURL);

      // We could move the "classic" to configuration
      return Response.temporaryRedirect(cleanupURL).build();
    } catch (Exception e) {
      log.error("Error redirecting to a space", e);
      return Response.status(500).build();
    } finally {
      RequestLifeCycle.end();
    }
  }

  /**
   * Get space information and build an output to display space information.
   * 
   * @param containerName The name of container.
   * @param portalName The name of current portal.
   * @param spaceName The name of space to be redirected.
   * @param description The description of target space.
   * @param uriInfo The request URI information.
   * @return
   */
  @GET
  @Path("space_info")
  @Produces("text/html")
  public String spaceInfo(@PathParam("containerName") String containerName,
                          @QueryParam("portalName") @DefaultValue("classic") String portalName,
                          @QueryParam("spaceName") String spaceName,
                          @QueryParam("description") String description,
                          @Context UriInfo uriInfo) {
    ExoContainer pc = ExoContainerContext.getContainerByName(containerName);
    // we make sure we use the right container
    ExoContainerContext.setCurrentContainer(pc);
    SpaceService service = (SpaceService) pc.getComponentInstanceOfType(SpaceService.class);
    IdentityManager identityManager = (IdentityManager) pc.getComponentInstanceOfType(IdentityManager.class);
    ActivityManager activityManager = (ActivityManager) pc.getComponentInstanceOfType(ActivityManager.class);

    // TODO: move this to a groovy template
    StringBuffer response = new StringBuffer();
    response.append("<!DOCTYPE html><html><head><style type=\"text/css\">" +
            "html,body{margin:0;padding:0;font-family:lucida,arial,tahoma,verdana,sans-serif;}")
            .append(" h1,h3 {margin:0px} h3 a {color:#FF9600;font-size:14px;font-weight:bold;} " +
                    "h1{ text-indent:-9000px;height:20px;")
            .append("background:url(\"/socialWidgetResources/img/social-logo.png\") " +
                    "no-repeat scroll 0 0 #FFFFFF; margin-bottom:5px;}</style>")
            .append("</head><body><h1>eXo Social</h1>");

    String spacePrettyName = SpaceUtils.cleanString(spaceName);

    URI goToSpace = uriInfo.getBaseUriBuilder().path("/spaces/{containerName}/go_to_space")
                               .queryParam("spaceName", spaceName)
                               .queryParam("portalName", portalName)
                               .queryParam("description", description)
                               .build(containerName);

    Space space = service.getSpaceByPrettyName(spacePrettyName);
    response.append("<h2 class=\"space_name\"><a href=\"")
            .append(goToSpace.toString())
            .append("\" target=\"_blank\">")
            .append(spaceName)
            .append("</a></h2>");
    if (space != null) {
      String username = ConversationState.getCurrent().getIdentity().getUserId();

      if (service.hasAccessPermission(space, username)) {
        Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                     space.getPrettyName(), true);
        RealtimeListAccess<ExoSocialActivity> spaceActivitiesListAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
        
        List<ExoSocialActivity> activities = spaceActivitiesListAccess.loadAsList(0, DEFAULT_LIMIT);

        if (activities.size() > 0) {
          response.append("<i>" + activities.get(0).getTitle() + "</i>");
        }
      } else {
        response.append("You are not member");
      }
    } else {
      response.append("This space has not been created yet");
    }

    return response + "</body></html>";
  }
}
