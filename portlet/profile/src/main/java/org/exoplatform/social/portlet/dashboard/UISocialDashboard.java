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
package org.exoplatform.social.portlet.dashboard;

import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.dashboard.webui.component.*;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.config.model.Container;

import javax.portlet.PortletPreferences;


@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)
public class UISocialDashboard extends UIContainer implements DashboardParent{



    public UISocialDashboard() throws Exception {
      UIDashboard dashboard = addChild(UIDashboard.class, null, null);

      PortletRequestContext context = (PortletRequestContext) WebuiRequestContext
        .getCurrentInstance();
      PortletPreferences pref = context.getRequest().getPreferences();
      String aggregatorId = pref.getValue("aggregatorId", "socialRssAggregator") ;
      String containerTemplate = pref.getValue("template", "three-columns") ;
      dashboard.setContainerTemplate(containerTemplate) ;
      dashboard.getChild(UIDashboardSelectContainer.class).setAggregatorId(aggregatorId) ;
    }

    public boolean canEdit() {
      PortletRequestContext context = (PortletRequestContext) WebuiRequestContext
          .getCurrentInstance();
      return getDashboardOwner().equals(context.getRemoteUser());
    }

    public void initNewDashboard(Container root) {
      
    }

    public String getDashboardOwner() {
      PortalRequestContext request = Util.getPortalRequestContext() ;
      String uri = request.getNodePath();

      if (uri.endsWith("/dashboard") && uri.startsWith("/people/")) {
        return uri.substring(8, uri.length() - 10);
      } else {
        PortletRequestContext context = (PortletRequestContext) WebuiRequestContext
          .getCurrentInstance();
        return context.getRemoteUser();
      }
    }


    public UIPopupMessages getUIPopupMessages() {
      return getAncestorOfType(UIApplication.class).getUIPopupMessages();
    }



}
