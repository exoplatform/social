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
package org.exoplatform.social.core.identity;

import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.page.PageUtils;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.application.WebuiRequestContext;

public class PageCreator extends UserEventListener {


  public void preSave(User user, boolean isNew) throws Exception {
    if (WebuiRequestContext.getCurrentInstance() == null) {
      System.out.println("PageCreator: there is no request context");
      return;
    }
    UIPortal uiPortal = Util.getUIPortal();
    String portalName = PortalConfig.PORTAL_TYPE + "::classic";
    String pageId = "portal::classic::" + user.getUserName() + "Profile";
    String nodeLabel = user.getUserName() + " Profile";
    String nodeName = user.getUserName() + "Profile";

    System.out.println("trying to create a page for "+ nodeLabel);
 /*    
    PageNavigation userNavi = uiPortal.getPageNavigation(portalName) ;    

   PageUtils.createNodeFromPageTemplate(nodeName, nodeLabel, pageId, null, userNavi);
   */
  }

}
