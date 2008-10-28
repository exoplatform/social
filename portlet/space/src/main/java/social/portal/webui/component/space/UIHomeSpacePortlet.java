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
package social.portal.webui.component.space;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.dashboard.webui.component.DashboardParent;
import org.exoplatform.dashboard.webui.component.UIDashboard;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Oct 23, 2008          
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class, 
    template = "app:/groovy/portal/webui/space/UIHomeSpacePortlet.gtmpl"
)
public class UIHomeSpacePortlet extends UIPortletApplication implements DashboardParent {

  public UIHomeSpacePortlet() throws Exception {
    addChild(UIHomeSpaceControlArea.class, null, null);
    addChild(UIDashboard.class, null, null);
  }

  public boolean canEdit() {
    return false;
  }
  
  public List<PageNode> getApps() throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    HttpServletRequest request = pcontext.getRequest();
    String url = request.getRequestURL().toString();
    String spaceName = url.substring(url.lastIndexOf("/")+1);
    int spaceNav = (PortalConfig.GROUP_TYPE + "::spaces/" + spaceName).hashCode();
    PageNavigation pageNav = uiPortal.getPageNavigation(spaceNav);
    PageNode homeNode = pageNav.getNode(spaceName);
    List<PageNode> list = homeNode.getChildren();
    if(list == null) list = new ArrayList<PageNode>();
    return list;
  }
  
  public String getSpaceName() {
    UIPortal uiPortal = Util.getUIPortal();
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    HttpServletRequest request = pcontext.getRequest();
    String url = request.getRequestURL().toString();
    return url.substring(url.lastIndexOf("/")+1);
  }

  public String getDashboardOwner() {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext
    .getCurrentInstance();
    return context.getRemoteUser();
  }
}
