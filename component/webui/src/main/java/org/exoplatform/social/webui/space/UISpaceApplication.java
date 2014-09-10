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
package org.exoplatform.social.webui.space;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;


@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template =  "war:/groovy/social/webui/space/UISpaceApplication.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceApplication.AddApplicationActionListener.class),
    @EventConfig(listeners = UISpaceApplication.RemoveApplicationActionListener.class),
    @EventConfig(listeners = UISpaceApplication.InstallApplicationActionListener.class, phase = Phase.DECODE)
  }
)
public class UISpaceApplication extends UIForm {

  /**
   * The logger.
   */
  private static final Log LOG = ExoLogger.getLogger(UISpaceApplication.class);

  private static final int APPLICATIONS_PER_PAGE = 10;
  private Space space;
  private UIPageIterator iterator;
  private final String iteratorID = "UIIteratorSpaceApplication";


  /**
   * Constructor.
   *
   * @throws Exception
   */
  public UISpaceApplication() throws Exception {
    addChild(UIPopupContainer.class, null, "UIPopupAddApp");
    iterator = createUIComponent(UIPageIterator.class, null, iteratorID);
    addChild(iterator);
  }

  /**
   * Gets application list
   *
   * @return application list
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<Application> getApplications() throws Exception {
    setValue(this.space);
    return iterator.getCurrentPageData();
  }


  /**
   * Gets the application name from the appName (applicationName:appId)
   *
   * @param appName
   */
  public String getApplicationName(String appName) {
    int colonIndex = appName.indexOf(":");
    if (colonIndex > 0) {
      appName = appName.substring(0, colonIndex);
    }
    return appName;
  }

  /**
   * Sets space to work with
   *
   * @param space
   * @throws Exception
   */
  public void setValue(Space space) throws Exception {
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    // Get space to update space's information.
    space = spaceService.getSpaceById(space.getId());
    this.space = space;
    List<String> appIdList = SpaceUtils.getAppIdList(space);
    List<String> notAvailableAppIdList = new ArrayList<String>(appIdList);
    List<Application> installedAppList = new ArrayList<Application>();
    Map<ApplicationCategory, List<Application>> appStore = SpaceUtils.getAppStore(space);
    Iterator<Entry<ApplicationCategory, List<Application>>> appCategoryEntrySetItr = appStore.entrySet().iterator();
    List<Application> appList;
    String appStatus;
    while (appCategoryEntrySetItr.hasNext()) {
      Entry<ApplicationCategory, List<Application>> appCategoryEntrySet = appCategoryEntrySetItr.next();
      appList = appCategoryEntrySet.getValue();
      for (Application app : appList) {
        if (!isExisted(installedAppList, app)) {
          if (appIdList.contains(app.getApplicationName())) {
            appStatus = SpaceUtils.getAppStatus(space, app.getApplicationName());
            if (appStatus.equals(Space.ACTIVE_STATUS)) {
              installedAppList.add(app);
              notAvailableAppIdList.remove(app.getApplicationName());
            }
          }
        }
      }
    }

    List<String> availableAppIdList = new ArrayList<String>();

    for (String appId : appIdList) {
      if (!availableAppIdList.contains(appId)) {
        availableAppIdList.add(appId);
      }
    }
    for (String appId : availableAppIdList) {
      if (SpaceUtils.getAppStatus(space, appId).equals(Space.ACTIVE_STATUS)) {
        installedAppList.add(SpaceUtils.getAppFromPortalContainer(appId));
      }
    }

    // Change name of application fit for issue SOC-739
    String apps = space.getApp();
    String[] listApp = apps.split(",");
    List<Application> installedApps = new ArrayList<Application>();
    String appName;
    String spaceAppName;
    String[] appParts;
    // make unique installedAppList
    // loop and check, if duplicate then create
    Application application;
    for (int index = 0; index < listApp.length; index++) {
      for (int idx = 0; idx < installedAppList.size(); idx++) {
        application = installedAppList.get(idx);
        if (application == null) continue;
        String temporalSpaceName = application.getApplicationName();

        appParts = listApp[index].split(":");
        spaceAppName = appParts[0];
        if (temporalSpaceName.equals(spaceAppName)) {
          String newName = appParts[0] + ":" + appParts[1];
          installedApps.add(setAppName(application, newName));
          break;
        }
      }
    }
    
    int currentPage = iterator.getCurrentPage();
    Collections.sort(installedApps, new ApplicationComparator());
    
    PageList pageList = new ObjectPageList(installedApps, APPLICATIONS_PER_PAGE);
    iterator.setPageList(pageList);
    
    int availablePage = iterator.getAvailablePage();
    
    if (currentPage > availablePage) {
      iterator.setCurrentPage(availablePage);
    } else {
      iterator.setCurrentPage(currentPage);
    }

  }
  
  /**
   * Application comparator.
   *
   * @author hoatle
   */
  private class ApplicationComparator implements Comparator<Application> {
    public int compare(Application app1, Application app2) {
      return app1.getApplicationName().compareToIgnoreCase(app2.getApplicationName());
    }
  }

  /**
   * Gets uiPageIterator.
   *
   * @return uiPageIterator
   */
  public UIPageIterator getUIPageIterator() {
    return iterator;
  }

  /**
   * Checks if an application is removable.
   *
   * @param appId
   * @return true or false
   */
  public boolean isRemovable(String appId) {
    return SpaceUtils.isRemovableApp(space, appId);
  }

  /**
   * Gets application name of space application when the display name of application is changed.<br>
   * - If the label of application is changed then return new label.<br> - Else return display name
   * of application.<br>
   *
   * @param application
   * @return application name depend on the display name is changed or not.
   * @throws Exception 
   */
  public String getAppName(String appId) throws Exception {
    String spaceUrl = Utils.getSpaceUrlByContext();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    Space space = spaceService.getSpaceByUrl(spaceUrl);
    if (space == null) {
      return null;
    }

    String installedApps = space.getApp();
    String[] apps = installedApps.split(",");
    String[] appParts = null;
    String appName = appId.split(":")[0];
    for (String app : apps) {
      if (app.length() != 0) {
        appParts = app.split(":");
        if (appParts[0].equals(appName)) {
          return appParts[1];
        }
      }
    }
    
    return null;
  }

  /**
   * Triggers this action when user clicks on add button
   *
   * @author hoatle
   */
  static public class AddApplicationActionListener extends EventListener<UISpaceApplication> {
    public void execute(Event<UISpaceApplication> event) throws Exception {
      UISpaceApplication uiSpaceApp = event.getSource();
      UIPopupContainer uiPopup = uiSpaceApp.getChild(UIPopupContainer.class);
      UISpaceApplicationInstaller uiSpaceAppInstaller = uiPopup.activate(UISpaceApplicationInstaller.class, 700);
      uiSpaceAppInstaller.setSpace(uiSpaceApp.space);
      uiPopup.getChild(UIPopupWindow.class).setId("UIAddApplication");
      uiPopup.getChild(UIPopupWindow.class).setResizable(false);
      uiPopup.getChild(UIPopupWindow.class).setWindowSize(596, 0);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }

  /**
   * Triggers this action when user clicks on remove button
   *
   * @author hoatle
   */
  static public class RemoveApplicationActionListener extends EventListener<UISpaceApplication> {
    public void execute(Event<UISpaceApplication> event) throws Exception {
      UISpaceApplication uiSpaceApp = event.getSource();
      WebuiRequestContext context = event.getRequestContext();
      String removedAppName = context.getRequestParameter(OBJECTID);
      SpaceService spaceService = uiSpaceApp.getApplicationComponent(SpaceService.class);
      String appId = null;
      String appName = null;
      String[] removedApps = removedAppName.split(":");
      if (removedApps.length == 2) {
        appId = removedApps[0];
        appName = removedApps[1];
      } else {
        appId = removedAppName;
      }

      spaceService.removeApplication(uiSpaceApp.space.getId(), appId, appName);
      UIPopupContainer uiPopup = uiSpaceApp.getChild(UIPopupContainer.class);

      // hanhvq. add removed application into uipopup container if it is displayed
      if (uiPopup.getChild(UIPopupWindow.class).isShow()) {
        UISpaceApplicationInstaller uiSpaceApplicationInstaller = uiPopup.activate(UISpaceApplicationInstaller.class, 700);
        uiSpaceApplicationInstaller.setSpace(uiSpaceApp.space);
        context.addUIComponentToUpdateByAjax(uiPopup);
      }
      
      uiSpaceApp.reloadSpaceNavigationTree();
      SpaceUtils.updateWorkingWorkSpace();
    }
  }

  /**
   * Handles the event broadcasted from {@inheritDoc UISpaceApplicationInstaller}.
   */
  public static class InstallApplicationActionListener extends EventListener<UISpaceApplication> {
    @Override
    public void execute(Event<UISpaceApplication> event) throws Exception {
      //refresh
      UISpaceApplication uiSpaceApplication = event.getSource();
      uiSpaceApplication.setValue(uiSpaceApplication.space);
      UISpaceSetting uiSpaceSetting = uiSpaceApplication.getAncestorOfType(UISpaceSetting.class);
      uiSpaceSetting.setValues(uiSpaceApplication.space); 

      uiSpaceApplication.reloadSpaceNavigationTree();
      SpaceUtils.updateWorkingWorkSpace();
    }
  }

  public void reloadSpaceNavigationTree() throws Exception {
    UITabPane uiTabPane = this.getAncestorOfType(UITabPane.class);
    if (uiTabPane != null) {
      UISpaceNavigationManagement uiSpaceNavigation = uiTabPane.getChild(UISpaceNavigationManagement.class);
      if (uiSpaceNavigation != null)
        uiSpaceNavigation.reloadTreeData();
    }
  }
  
  /**
   * Checks if an application exists in list or not.
   *
   * @param appList List of application
   * @param app    Application for checking
   * @return true or false
   */
  private boolean isExisted(List<Application> appList, Application app) {
    String appName = app.getApplicationName();
    String existedAppName = null;
    for (Application application : appList) {
      existedAppName = application.getApplicationName();
      if (existedAppName.equals(appName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Clone application object with new name.
   *
   * @param application
   * @param appName
   * @return
   */
  private Application setAppName(Application application, String appName) {
    Application app = new Application();

    app.setCategoryName(application.getCategoryName());
    app.setDisplayName(application.getDisplayName());
    app.setDescription(application.getDescription());
    app.setCreatedDate(application.getCreatedDate());
    app.setModifiedDate(application.getModifiedDate());
    app.setAccessPermissions(application.getAccessPermissions());
    app.setApplicationName(appName);
    app.setType(application.getType());
    app.setStorageId(application.getStorageId());
    app.setId(application.getId());
    app.setIconURL(application.getIconURL());
    app.setContentId(application.getContentId());

    return app;

  }

}
