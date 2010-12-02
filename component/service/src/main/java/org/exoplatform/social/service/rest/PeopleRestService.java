/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.service.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;

/**
 * Gets user names by input text for auto-suggestion.
 * 
 * @author hanhvq@gmail.com
 * @since Nov 22, 2010  
 */

@Path("social/people")
public class PeopleRestService implements ResourceContainer{
  private IdentityManager identityManager;
  
  /** Number of user names is added to suggest list. */
  private static final long SUGGEST_LIMIT = 20;
  
  public PeopleRestService() {
  }

  @GET
  @Path("suggest.{format}")
  public Response suggestUsernames(@Context UriInfo uriInfo,
                    @QueryParam("userName") String name,
                    @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    UserNameList nameList = new UserNameList();
    ProfileFilter filter = new ProfileFilter();
    filter.setName(name);
    filter.setCompany("");
    filter.setGender("");
    filter.setPosition("");
    filter.setSkills("");
    List<Identity> identities = getIdentityManager()
        .getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, 0, SUGGEST_LIMIT);
    
    for (Identity identity : identities) {
      String userName = identity.getProfile().getFullName();
      nameList.addName(userName);
    }
    
    return Util.getResponse(nameList, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Gets identityManager
   * @return
   */
  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getCurrentContainer();
      identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }
  

  @XmlRootElement
  static public class UserNameList {
    private List<String> _names;
    /**
     * Sets user name list
     * @param user name list
     */
    public void setNames(List<String> names) {
      this._names = names; 
    }
    
    /**
     * Gets user name list
     * @return user name list
     */
    public List<String> getNames() { 
      return _names; 
    }
    
    /**
     * Add name to user name list
     * @param user name
     */
    public void addName(String name) {
      if (_names == null) {
        _names = new ArrayList<String>();
      }
      _names.add(name);
    }
  }
  
}
