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
package org.exoplatform.social.common.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.application.PortalApplication;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.filter.Filter;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.core.UIApplication;

/**
 * The filter to invalidate navigation cache.
 *
 * @author <a href="mailto:khoi.nguyen@exoplatform.com">Nguyen Duc Khoi</a>
 * @since Dec 4, 2010
 */
public class NavigationCleanupFilter implements Filter {
  private static final Log LOG = ExoLogger.getLogger(NavigationCleanupFilter.class);

  class ExtendedPortalRequestContext extends PortalRequestContext {
    private String portalName;

    public ExtendedPortalRequestContext(WebuiApplication app, HttpServletRequest req, HttpServletResponse res)
            throws Exception {
      super(app, req, res);
    }

    public void setPortalName(String portalName) {
      this.portalName = portalName;
    }

    public String getPortalOwner() {
      return portalName;
    }

  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                       ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    ExoContainer container = PortalContainer.getInstance();
    WebAppController controller = (WebAppController) container.getComponentInstance(WebAppController.class);
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
      UserPortalConfigService service_ = (UserPortalConfigService) container
              .getComponentInstanceOfType(UserPortalConfigService.class);
      pApp.setUserPortalConfig(service_.getUserPortalConfig(portalSite, remoteUser));
    } catch (Exception e) {
      // just ignore
    }
    finally {
      res.sendRedirect(url);
      RequestLifeCycle.end();
    }
  }
}


