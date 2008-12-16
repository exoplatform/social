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
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.space.SpaceUtils;
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
    addChild(UIDashboard.class, null, null);
  }

  public boolean canEdit() {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext
    .getCurrentInstance();
    String remoteUser = context.getRemoteUser();
    //TODO: dang.tung - should use SpaceService to check isLeader
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    MembershipHandler memberShipHandler = orgSrc.getMembershipHandler();
    String spaceName = SpaceUtils.getShortSpaceName();
    try {
      if(memberShipHandler.findMembershipByUserGroupAndType(remoteUser, "/spaces/" + spaceName, "manager") != null) return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public String getDashboardOwner() {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext
    .getCurrentInstance();
    return context.getRemoteUser();
  }
}
