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
package org.exoplatform.social.webui;


import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.webui.UIApplicationListSelector.InstallApplicationActionListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

/**
 * This ui component is used to display a list of applications and broadcasts the
 * install application listener to parent ui.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since  1.2.8
 * @since Jan 17, 2012
 */
@ComponentConfig(
  template = "war:/groovy/social/webui/UIApplicationListSelector.gtmpl",
  events = {
    @EventConfig(listeners = InstallApplicationActionListener.class)
  }
)
public class UIApplicationListSelector extends UIContainer {

  /**
   * The logger.
   */
  private static final Log LOG = ExoLogger.getLogger(UIApplicationListSelector.class);

  /**
   * The list of applications.
   */
  private List<Application> applicationList;

  /**
   * The selected application.
   */
  private Application selectedApplication;

  /**
   * Sets the list of applications.
   *
   * @param newApplicationList the list of applications.
   */
  public void setApplicationList(List<Application> newApplicationList) {
    this.applicationList = newApplicationList;
  }

  /**
   * Gets the list of applications.
   *
   * @return the list of applications.
   */
  public List<Application> getApplicationList() {
    return this.applicationList;
  }

  /**
   * Sets the selected application.
   *
   * @param newSelectedApplication the selected application.
   */
  public void setSelectedApplication(Application newSelectedApplication) {
    this.selectedApplication = newSelectedApplication;
  }

  /**
   * Gets the selected application.
   *
   * @return the selected application.
   */
  public Application getSelectedApplication() {
    if (selectedApplication == null) {
      this.selectedApplication = applicationList.get(0);
    }
    return this.selectedApplication;
  }


  /**
   * Broadcasts this event listener so that parent ui component could handle.
   */
  public static class InstallApplicationActionListener extends EventListener<UIApplicationListSelector> {

    @Override
    public void execute(Event<UIApplicationListSelector> event) throws Exception {
      String applicationName = event.getRequestContext().getRequestParameter(OBJECTID);
      UIApplicationListSelector uiApplicationListSelector = event.getSource();
      uiApplicationListSelector.setSelectedApplication(applicationName);
      if (uiApplicationListSelector.getParent() != null) {
        Event<UIComponent> installApplicationEvent =
                uiApplicationListSelector.getParent().createEvent("InstallApplication", Phase.DECODE, event.getRequestContext());
        if (installApplicationEvent == null) {
          LOG.warn("Failed to broadcast InstallApplicationActionListener.");
          return;
        }
        installApplicationEvent.broadcast();
      }
    }
  }


  /**
   * Sets selected application by application name.
   *
   * @param applicationName the application name.
   */
  private void setSelectedApplication(String applicationName) {
    if (getSelectedApplication().getApplicationName().equals(applicationName)) {
      return;
    }
    for (Application application : applicationList) {
      if (application.getApplicationName().equals(applicationName)) {
        setSelectedApplication(application);
        break;
      }
    }
  }
}
