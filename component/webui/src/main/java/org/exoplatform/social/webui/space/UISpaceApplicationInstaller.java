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
package org.exoplatform.social.webui.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.UIApplicationCategorySelector;
import org.exoplatform.social.webui.UIApplicationListSelector;
import org.exoplatform.social.webui.space.UISpaceApplicationInstaller.InstallApplicationActionListener;
import org.exoplatform.social.webui.space.UISpaceApplicationInstaller.SelectActionListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

/**
 * The ui component to manage and install applications to a space.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jan 18, 2012
 */
@ComponentConfig(
        template = "war:/groovy/social/webui/space/UISpaceApplicationInstaller.gtmpl",
        events = {
                @EventConfig(listeners = SelectActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = InstallApplicationActionListener.class, phase = Phase.DECODE)
        }
)
public class UISpaceApplicationInstaller extends UIContainer implements UIPopupComponent {

  /**
   * The logger.
   */
  private static final Log LOG = ExoLogger.getLogger(UISpaceApplicationInstaller.class);

  /**
   * The appStore containing a map of application category and its associated application list.
   */
  private Map<ApplicationCategory, List<Application>> appStore;

  /**
   * The current space.
   */
  private String spaceId;

  /**
   * The application category list.
   */
  private List<ApplicationCategory> applicationCategoryList;

  /**
   * The ui application category selector to display application categories.
   */
  private UIApplicationCategorySelector uiApplicationCategorySelector;
  /**
   * The ui application list selector to display applications.
   */
  private UIApplicationListSelector uiApplicationListSelector;


  /**
   * Constructor
   */
  public UISpaceApplicationInstaller() throws Exception {
    uiApplicationCategorySelector = addChild(UIApplicationCategorySelector.class, null, null);
    uiApplicationListSelector = addChild(UIApplicationListSelector.class, null, null);
  }


  /**
   * Triggers callback event when popup ui is activated.
   *
   * @throws Exception
   */
  @Override
  public void activate() {

  }

  /**
   * Triggers callback event when popup ui is deactivated.
   *
   * @throws Exception
   */
  @Override
  public void deActivate() {

  }

  /**
   * Sets the current space.
   *
   * @param newSpace the space.
   * @throws Exception
   */
  public final void setSpace(Space newSpace) throws Exception {
    this.spaceId = newSpace.getId();
    initAppStore();
    initUICategoryAndList();
  }


  /**
   * Event Handler for "Select" action.
   */
  public static class SelectActionListener extends EventListener<UISpaceApplicationInstaller> {

    @Override
    public void execute(Event<UISpaceApplicationInstaller> event) throws Exception {
      UISpaceApplicationInstaller uiSpaceApplicationInstaller = event.getSource();
      uiSpaceApplicationInstaller.setApplicationList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceApplicationInstaller);
    }
  }

  /**
   * Event Handler for "InstallApplication" action. Try to broadcast "InstallApplication" to parent's parent's parent ui.
   */
  public static class InstallApplicationActionListener extends EventListener<UISpaceApplicationInstaller> {
    @Override
    public void execute(Event<UISpaceApplicationInstaller> event) throws Exception {
      UISpaceApplicationInstaller uiSpaceApplicationInstaller = event.getSource();
      uiSpaceApplicationInstaller.installApplication();
      if (uiSpaceApplicationInstaller.getParent() != null &&
              uiSpaceApplicationInstaller.getParent().getParent() != null &&
              uiSpaceApplicationInstaller.getParent().getParent().getParent() != null) {
        Event<UIComponent> installApplicationEvent =
                uiSpaceApplicationInstaller.getParent().getParent().getParent().createEvent("InstallApplication", Phase.DECODE,
                        event.getRequestContext());
        if (installApplicationEvent == null) {
          LOG.warn("Failed to broadcast InstallApplicationActionListener.");
          return;
        }
        installApplicationEvent.broadcast();
      }
    }
  }

  /**
   * Installed the selected application.
   */
  private void installApplication() {
    String appId = uiApplicationListSelector.getSelectedApplication().getApplicationName();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    try {
      spaceService.installApplication(this.spaceId, appId);
      spaceService.activateApplication(this.spaceId, appId);
    } catch (SpaceException e) {
      LOG.warn("Failed to install application: " + appId, e);
    }
  }

  /**
   * Initializes the app store.
   *
   * @throws Exception
   */
  private void initAppStore() throws Exception {
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    Space space = spaceService.getSpaceById(this.spaceId);
    appStore = SpaceUtils.getAppStore(space);
    applicationCategoryList = new ArrayList<ApplicationCategory>(appStore.keySet());
  }

  /**
   * Initializes the category list and application list for the first time.
   */
  private void initUICategoryAndList() {
    uiApplicationCategorySelector.setApplicationCategoryList(applicationCategoryList);
    ApplicationCategory selectedApplicationCategory = applicationCategoryList.get(0);
    List<Application> applicationList = appStore.get(selectedApplicationCategory);
    uiApplicationListSelector.setApplicationList(applicationList);
  }

  /**
   * Sets the application list to be displayed which is associated with selected application category.
   */
  private void setApplicationList() {
    ApplicationCategory selectedApplicationCategory = uiApplicationCategorySelector.getSelectedApplicationCategory();
    List<Application> applicationList = appStore.get(selectedApplicationCategory);
    uiApplicationListSelector.setApplicationList(applicationList);
  }

}
