/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.webui;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * Processes url and returns the some type of result base on url.
 * 
 */
public class URLUtils {

  /**
   * Gets current user name base on analytic the current url.<br>
   * 
   * @return current user name.
   */
  public static String getCurrentUser() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager idm = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    PortalRequestContext request = Util.getPortalRequestContext() ;
    String uri = request.getNodePath();
    String[] els = uri.split("/");
    String currentUserName = null;
    
    if (els.length == 3) {
      currentUserName = els[2];
    } else if (els.length == 4) {
      currentUserName = els[3];
    }
    
    if (currentUserName != null) {
      Identity id = idm.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUserName);
      if (id != null) return currentUserName;
    }
    
    return null;
  }
}
