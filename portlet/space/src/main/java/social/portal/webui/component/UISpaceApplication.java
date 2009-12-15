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

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.social.space.Space;
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
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 12, 2008          
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

  private Space space_;
  private UIPageIterator iterator_;
  private final String iteratorID = "UIIteratorSpaceApplication";
  
  public UISpaceApplication() throws Exception {
    addChild(UIPopupContainer.class, null, "UIPopupAddApp");
    iterator_ = createUIComponent(UIPageIterator.class, null, iteratorID);
    addChild(iterator_);
  }
  
  @SuppressWarnings("unchecked")
  public List<Application> getApplications() throws Exception {
    return iterator_.getCurrentPageData();
  }
  
  /**
   * sets space
   * @param space
   * @throws Exception
   */
  public void setValue(Space space) throws Exception {
    space_ = space;
    List<Application> lists = new ArrayList<Application>();
    List<Application> apps = new ArrayList<Application>();
    String installedApps = space.getApp();
    if (installedApps != null) { 
      Map<ApplicationCategory, List<Application>> appStore;
      appStore = SpaceUtils.getAppStore(space);
      Iterator<ApplicationCategory> appCategoryItr = appStore.keySet().iterator();
      while (appCategoryItr.hasNext()) {
        ApplicationCategory category = appCategoryItr.next();
        List<Application> appList = appStore.get(category);
        Iterator<Application> appListItr = appList.iterator();
        while (appListItr.hasNext()) {
          Application app = appListItr.next();
          if (!apps.contains(app)) {
            apps.add(app);
            String appStatus = SpaceUtils.getAppStatus(space, app.getApplicationName());
            if (appStatus != null) {
              if (appStatus.equals(Space.ACTIVE_STATUS)) {
                lists.add(app);
              }
            }
          }
        }
      }
      // 
      if (lists.size() == 0) {
        lists.addAll(SpaceUtils.getAppList());
      }
    }
    
    PageList pageList = new ObjectPageList(lists,3);
    iterator_.setPageList(pageList);
  }
  
  public UIPageIterator getUIPageIterator() { return iterator_;}
  
  static public class AddApplicationActionListener extends EventListener<UISpaceApplication> {
    public void execute(Event<UISpaceApplication> event) throws Exception {
      UISpaceApplication uiSpaceApp = event.getSource();
      UIPopupContainer uiPopup = uiSpaceApp.getChild(UIPopupContainer.class);
      UISpaceApplicationList uiSpaceAppList = (UISpaceApplicationList) uiPopup.activate(UISpaceApplicationList.class, 400);
      uiSpaceAppList.setSpace(uiSpaceApp.space_);
      uiPopup.getChild(UIPopupWindow.class).setId("AddApplication");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }
  
  static public class RemoveApplicationActionListener extends EventListener<UISpaceApplication> {
    public void execute(Event<UISpaceApplication> event) throws Exception {
      UISpaceApplication uiSpaceApp = event.getSource();
      WebuiRequestContext context = event.getRequestContext();
      String appId = context.getRequestParameter(OBJECTID);
      SpaceService spaceService = uiSpaceApp.getApplicationComponent(SpaceService.class);
      spaceService.removeApplication(uiSpaceApp.space_.getId(), appId);
      uiSpaceApp.setValue(spaceService.getSpaceById(uiSpaceApp.space_.getId()));
      
      SpaceUtils.updateWorkingWorkSpace();
    }
  }
}
