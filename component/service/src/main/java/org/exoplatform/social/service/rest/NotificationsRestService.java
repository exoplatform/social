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

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * 
 * Provides REST Services for manipulating jobs relates to notifications.
 * 
 * @anchor NotificationRestService
 */

@Path("social/notifications")
public class NotificationsRestService implements ResourceContainer {

  public NotificationsRestService() {
  }
  
  @GET
  @Path("{action}")
  public Response actionToInvitation(@Context UriInfo uriInfo,
                                     @PathParam("action") String action) throws Exception {
    // Authentication
  
    String targetURL = null;
    
    // Call services to process and create target redirected url by type of action
  
    // redirect to target page
   return Response.seeOther(URI.create(targetURL)).build();
  }
}
