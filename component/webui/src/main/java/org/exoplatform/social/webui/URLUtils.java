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
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.common.router.ExoRouter.Route;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * Processes url and returns the some type of result base on url.
 * 
 */
public class URLUtils {

  private static Log LOG = ExoLogger.getLogger(UISocialGroupSelector.class);
  private static String ROOT_NODE_NAME = "default";
  
  /**
   * Gets current user name base on analytic the current url.<br>
   * 
   * @return current user name.
   */
  public static String getCurrentUser() {
    PortalRequestContext pcontext = Util.getPortalRequestContext() ;
    String requestPath = "/" + pcontext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    Route route = ExoRouter.route(requestPath);
    if (route == null) { 
      return null;
    }
    
    String currentUserName = route.localArgs.get("streamOwnerId");
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager idm = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);

    try {
      if (currentUserName != null) {
        Identity id = idm.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUserName, false);
        if (id != null) return currentUserName;
      }
    } catch (Exception e) {
      if(LOG.isDebugEnabled()) {
        LOG.debug("Could not found Identity of user " + currentUserName);
      }
      return null;
    }
    return null;
  }
  
  /**
   * Gets current requested node.
   * 
   * @return
   * @throws Exception
   */
  public static String getRequestedNode() throws Exception {
    UserNode selectedUserNode = Util.getUIPortal().getSelectedUserNode();
    UserNode prevParent = selectedUserNode.getParent();
    UserNode parent = prevParent;
    boolean isRoot = true;
    while (!ROOT_NODE_NAME.equals(parent.getName())) {
      prevParent = parent;
      parent = prevParent.getParent();
      isRoot = false;
    } 
    
    return isRoot ? selectedUserNode.getName() : prevParent.getName();  
  }
  
  
}
