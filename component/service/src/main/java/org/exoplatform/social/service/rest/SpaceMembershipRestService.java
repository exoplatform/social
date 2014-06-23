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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api.models.SpacesCollections;

@Path(VersionResources.CURRENT_VERSION + "/social/spacesMemberships")
public class SpaceMembershipRestService implements ResourceContainer {
  
  public SpaceMembershipRestService(){
  }

  /**
   * Process to return a list of space's membership in json format
   * 
   * @param uriInfo
   * @param q
   * @param offset
   * @param limit
   * @return
   * @throws Exception
   */
  @GET
  public Response getSpaces(@Context UriInfo uriInfo,
                             @QueryParam("status") String status,
                             @QueryParam("user") String user,
                             @QueryParam("space") String space,
                             @QueryParam("offset") int offset,
                             @QueryParam("limit") int limit) throws Exception {
    checkAuthenticatedRequest();
    //Check if no authenticated user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (IdentityConstants.ANONIM.equals(authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    MembershipHandler memberShipHandler = organizationService.getMembershipHandler();
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    
    limit = limit <= 0 ? RestUtils.DEFAULT_LIMIT : limit;
    offset = offset < 0 ? RestUtils.DEFAULT_OFFSET : offset;
    
    List<Map<String, Object>> spaceInfos = new ArrayList<Map<String, Object>>();
    
    SpacesCollections spaces = new SpacesCollections(1, offset, limit);
    spaces.setSpaces(spaceInfos);
    
    return Util.getResponse(spaces, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
}
