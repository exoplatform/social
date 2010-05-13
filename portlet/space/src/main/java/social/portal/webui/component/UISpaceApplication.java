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
package social.portal.webui.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
/**
 * UISpaceApplication.java used for adding/ removing applications<br />
 * Created by The eXo Platform SARL
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Sep 12, 2008
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template =  "app:/groovy/portal/webui/uiform/UISpaceApplication.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceApplication.AddApplicationActionListener.class),
    @EventConfig(listeners = UISpaceApplication.RemoveApplicationActionListener.class)
  }
)
public class UISpaceApplication extends UIForm {

  private Space space;
  private UIPageIterator iterator;
  private final String iteratorID = "UIIteratorSpaceApplication";

  
  /**
   * constructor
   * @throws Exception
   */
  public UISpaceApplication() throws Exception {
    addChild(UIPopupContainer.class, null, "UIPopupAddApp");
    iterator = createUIComponent(UIPageIterator.class, null, iteratorID);
    addChild(iterator);
  }
  /**
   * Gets application list
   * @return application list
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<Application> getApplications() throws Exception {
    return iterator.getCurrentPageData();
  }
  
  /**
   * Sets space to work with
   * @param space
   * @throws Exception
   */
  public void setValue(Space space) throws Exception {
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
      for (Application app: appList) {
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
      if (!availableAppIdList.contains(appId)) availableAppIdList.add(appId); 
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
    for (int index = 0; index < listApp.length; index++){
      for (int idx = 0; idx <installedAppList.size(); idx++) {
        application = installedAppList.get(idx);	
        String temporalSpaceName = application.getApplicationName();
      
    	//Application application = installedAppList.get(idx);
        
    	//appName = application.getApplicationName();
    	appParts = listApp[index].split(":");
    	spaceAppName = appParts[0];
    	  if (temporalSpaceName.equals(spaceAppName)) {
    		String newName = appParts[0] + ":" + appParts[1];
//    		application.setApplicationName(appParts[0] + ":" + appParts[1]);
    		installedApps.add(setAppName(application, newName));
    		break;
    	  }
      }
    }
    PageList pageList = new ObjectPageList(installedApps, 3);
//    PageList pageList = new ObjectPageList(installedAppList, 3);
    iterator.setPageList(pageList);
  }
  
  /**
   * gets uiPageIterator
   * @return uiPageIterator
   */
  public UIPageIterator getUIPageIterator() { return iterator;}
  
  /**
   * Checks if an application is removable.
   * @param appId
   * @return true or false
   */
  public boolean isRemovable(String appId) {
    return SpaceUtils.isRemovableApp(space, appId);
  }
  
  /**
   * Gets application name of space application when the display name of application is changed.<br>
   * - If the label of application is changed then return new label.<br>
   * - Else return display name of application.<br> 
   * 
   * @param application
   * @return application name depend on the display name is changed or not.
   * @throws SpaceException
   */
  public String getAppName(Application application) throws SpaceException {
    String spaceUrl = SpaceUtils.getSpaceUrl();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    Space space = spaceService.getSpaceByUrl(spaceUrl);
    if (space == null) {
      return null;
    }
    PageNavigation pageNav = null;
    try {
      pageNav = SpaceUtils.getGroupNavigation(space.getGroupId());
    } catch (Exception e1) {
    // TODO Auto-generated catch block
     e1.printStackTrace();
    }
    
    PageNode homeNode = SpaceUtils.getHomeNode(pageNav, spaceUrl);
    if (homeNode == null) {
      return null;
    }
    List<PageNode> nodes = homeNode.getChildren();
    String applicationName = application.getApplicationName();
    String appName = applicationName.split(":")[1];
    String appNodeName;
    for (PageNode node : nodes) {
      String nodeUri = node.getUri();
      appNodeName = nodeUri.substring(nodeUri.indexOf("/") + 1);
      if (appNodeName.equals(appName)) return node.getResolvedLabel();
    }
    return application.getDisplayName();
  }
  
  /**
   * Triggers this action when user clicks on add button
   * @author hoatle
   *
   */
  static public class AddApplicationActionListener extends EventListener<UISpaceApplication> {
    public void execute(Event<UISpaceApplication> event) throws Exception {
      UISpaceApplication uiSpaceApp = event.getSource();
      UIPopupContainer uiPopup = uiSpaceApp.getChild(UIPopupContainer.class);
      UISpaceApplicationList uiSpaceAppList = (UISpaceApplicationList) uiPopup.activate(UISpaceApplicationList.class, 400);
      uiSpaceAppList.setSpace(uiSpaceApp.space);
      uiPopup.getChild(UIPopupWindow.class).setId("AddApplication");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }
  
  /**
   * Triggers this action when user clicks on remove button
   * @author hoatle
   *
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
      uiSpaceApp.setValue(spaceService.getSpaceById(uiSpaceApp.space.getId()));
      UIPopupContainer uiPopup = uiSpaceApp.getChild(UIPopupContainer.class);
      
      // hanhvq. add removed application into uipopup container if it is displayed 
      if (uiPopup.getChild(UIPopupWindow.class).isShow()) {
        UISpaceApplicationList uiSpaceAppList = (UISpaceApplicationList) uiPopup.activate(UISpaceApplicationList.class, 400);
        uiSpaceAppList.setSpace(uiSpaceApp.space);
        context.addUIComponentToUpdateByAjax(uiPopup);
      }
      SpaceUtils.updateWorkingWorkSpace();
    }
  }
  
  /**
   * Checks if an application exists in list or not.
   * 
   * @param appLst List of application
   * @param app Application for checking
   * @return true or false
   */
  private boolean isExisted(List<Application> appList, Application app) {
    String appName = app.getApplicationName();
    String existedAppName = null;
    for (Application application : appList) {
      existedAppName = application.getApplicationName();
      if (existedAppName.equals(appName)) return true; 
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
