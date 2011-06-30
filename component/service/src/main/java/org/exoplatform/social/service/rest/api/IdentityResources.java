/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.service.rest.api;

import java.util.HashMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.core.manager.IdentityManager;


/**
 * Unit test for {@link VersionResources}.
 *
 * @author <a href="http://phuonglm.net">PhuongLM</a>
 * @since Jun 29, 2011
 */
@Path("api/social/" + VersionResources.LATEST_VERSION+ "/{portalContainerName}/identity/")
public class IdentityResources implements ResourceContainer {
  private static final String[] SUPPORTED_FORMAT = new String[]{"json"};
  private IdentityManager identityManager;
  /**
   * Get Comment from existing activity by GET method from a specified activity id. Just returns the Comment List and total number of Comment
   * in activity.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param identityId the specified identityId
   * @param format the expected returned format
   * @return a response object
   */
  @GET
  @Path("{identityId}.{format}")
  public Response getIdentityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("identityId") String identityId,
                                           @PathParam("format") String format) {
    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if(identityId !=null && !identityId.equals("")){
      PortalContainer portalContainer = getPortalContainer(portalContainerName);
      identityManager = Util.getIdentityManager();
      Identity authenticatedUserIdentity = authenticatedUserIdentity();
      Identity identity = identityManager.getIdentity(identityId, true);
      if(authenticatedUserIdentity != null){
        if(portalContainer!=null && identity!=null){
          HashMap resultHashMap = new HashMap();
          HashMap resultProfileHashMap = new HashMap();
          resultHashMap.put("id", identity.getId());
          resultHashMap.put("providerId",identity.getProviderId());
          resultHashMap.put("remoteId",identity.getRemoteId());
          
          Profile profile = identity.getProfile();
          resultProfileHashMap.put("fullName", profile.getFullName());
          resultProfileHashMap.put("avatarUrl", profile.getAvatarUrl());
          
          resultHashMap.put("profile",resultProfileHashMap);
        
          return Util.getResponse(resultHashMap, uriInfo, mediaType, Response.Status.OK);
        } else {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      } else {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    } else {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
  }
  
  private Identity authenticatedUserIdentity() {
    if(ConversationState.getCurrent()!=null && ConversationState.getCurrent().getIdentity() != null &&
              ConversationState.getCurrent().getIdentity().getUserId() != null){
      IdentityManager identityManager =  Util.getIdentityManager();
      String authenticatedUserRemoteID = ConversationState.getCurrent().getIdentity().getUserId(); 
      return identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUserRemoteID, false);
    } else {
      return null;
    }
  }  
  private PortalContainer getPortalContainer(String name) {
    return (PortalContainer) ExoContainerContext.getContainerByName(name);
  }
}
