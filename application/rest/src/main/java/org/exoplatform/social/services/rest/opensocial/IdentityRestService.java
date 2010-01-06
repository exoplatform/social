/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.social.services.rest.opensocial;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.IdentityManager;

/**
 * IdentityRestService.java
 * gets identityId from username
 * @author     hoatle <hoatlevan at gmail dot com>
 * @since      Dec 29, 2009
 * @copyright  eXo Platform SAS 
 */
@Path("social/identity/{username}/id")
public class IdentityRestService implements ResourceContainer {
  private IdentityManager _identityManager;
  public IdentityRestService() {}
  
  /**
   * gets identity by username
   * @param username
   * @return
   * @throws Exception
   */
  @GET
  @Path("show.json")
  @Produces({MediaType.APPLICATION_JSON})
  public UserId getId(@PathParam("username") String username) throws Exception {
      _identityManager = getIdentityManager();
      String id = null;
      try {
        id = _identityManager.getIdentityByRemoteId("organization", username).getId();
      } catch(Exception ex) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
      UserId userId = new UserId(id);
      return userId;
  }
  
  /**
   * gets identityManager
   * @return
   */
  private IdentityManager getIdentityManager() {
    if (_identityManager == null) {
      PortalContainer portalContainer = PortalContainer.getInstance();
      _identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return _identityManager;
  }
  
  /**
   * UserId class to be exposed
   * @author hoatle
   *
   */
  public class UserId {
    private String _id;
    
    public UserId() {
      
    }
    public UserId(String id) {
      _id = id;
    }
    public void setId(String id) {
      _id = id;
    }
    public String getId() {
      return _id;
    }
  }
}
