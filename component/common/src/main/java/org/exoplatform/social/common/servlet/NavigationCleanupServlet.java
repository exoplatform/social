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
package org.exoplatform.social.common.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.web.AbstractHttpServlet;
import org.exoplatform.portal.application.PortalApplication;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.WebAppController;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.core.UIApplication;

/**
 * @author <a href="mailto:trongtt@gmail.com">Tran The Trong</a>
 * @version $Revision$
 */
public class NavigationCleanupServlet extends AbstractHttpServlet {

  class ExtendedPortalRequestContext extends PortalRequestContext {
    private String portalName;

    public void setPortalName(String name) {
      portalName = name;
    }

    public String getPortalOwner() {
      return portalName;
    }

    public ExtendedPortalRequestContext(WebuiApplication app,
                                        HttpServletRequest req,
                                        HttpServletResponse res) throws Exception {
      super(app, req, res);
    }

  }

  @Override
  protected void onService(ExoContainer container, HttpServletRequest req, HttpServletResponse res) throws ServletException,
                                                                                                   IOException {
    WebAppController controller = (WebAppController) container.getComponentInstanceOfType(WebAppController.class);
    PortalApplication app = controller.getApplication(PortalApplication.PORTAL_APPLICATION_ID);
    String url = req.getParameter("url");
    String portalSite = req.getParameter("portal");
    try {
      RequestLifeCycle.begin(container);
      ExtendedPortalRequestContext context = new ExtendedPortalRequestContext(app, req, res);
      context.setPortalName(portalSite);
      UIApplication uiApp = app.getStateManager().restoreUIRootComponent(context);
      UIPortalApplication pApp = (UIPortalApplication) uiApp;
      String remoteUser = context.getRemoteUser();
      UserPortalConfigService service_ = (UserPortalConfigService) container.getComponentInstanceOfType(UserPortalConfigService.class);
      pApp.setUserPortalConfig(service_.getUserPortalConfig(portalSite, remoteUser));
      res.sendRedirect(url);
    } catch (Exception e) {

    } finally {
      RequestLifeCycle.end();
    }
  }
}
