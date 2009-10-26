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
package org.exoplatform.social.portlet;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 */

@ComponentConfig(
 lifecycle = UIApplicationLifecycle.class,
 template = "app:/groovy/portal/webui/component/UIRelationNavigationPortlet.gtmpl"
)
public class UIRelationNavigationPortlet extends UIPortletApplication {

  public UIRelationNavigationPortlet() throws Exception {
  }
  
  public String getSelectedNode() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String requestUrl = pcontext.getRequestURI();
   String[] split = requestUrl.split("/");
   
    return split[split.length-2];
  }
}
