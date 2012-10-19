/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.webui.space.access;

import java.util.List;

import javax.servlet.ServletContext;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalApplication;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.picocontainer.Startable;


/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Oct 17, 2012  
 */
public class SpaceAccessService implements WebAppListener, Startable {

  /** . */
  private final Logger log = LoggerFactory.getLogger(SpaceAccessService.class);
  
  @Override
  public void start() {
    DefaultServletContainerFactory.getInstance().getServletContainer().addWebAppListener(this);
  }

  @Override
  public void stop() {
    DefaultServletContainerFactory.getInstance().getServletContainer().removeWebAppListener(this);
  }

  @Override
  public void onEvent(WebAppEvent webAppEvent) {
    if (webAppEvent instanceof WebAppLifeCycleEvent) {
      WebAppLifeCycleEvent lfEvent = (WebAppLifeCycleEvent) webAppEvent;
      if (lfEvent.getType() == WebAppLifeCycleEvent.ADDED) {
        WebApp webApp = webAppEvent.getWebApp();
        ServletContext scontext = webApp.getServletContext();
        

        final String contextPath = scontext.getContextPath();
        //final to initialize Root WebApp
        if ("".equals(contextPath)) {
          handle();
        }

      }
    }
  }
  
  private void handle() {
    try {

      WebAppController controller = (WebAppController) PortalContainer.getInstance()
                                                                      .getComponentInstanceOfType(WebAppController.class);
      PortalApplication app = controller.getApplication(PortalApplication.PORTAL_APPLICATION_ID);

      List<ApplicationLifecycle> lifecyces = app.getApplicationLifecycle();

      lifecyces.add(new SpaceAccessApplicationLifecycle());

      app.setApplicationLifecycle(lifecyces);

    } catch (Exception e) {
      log.error("Could not inject SpaceAccessApplicationLifecycle class.", e);
    }
  }

}
