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

import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UITabPane;
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
    template =  "app:/groovy/portal/webui/uiform/UIAddApplicationSpace.gtmpl",
    events = {
        @EventConfig(listeners = UIAddApplicationSpace.CloseActionListener.class),
        @EventConfig(listeners = UIAddApplicationSpace.InstallActionListener.class)
      }
)
public class UIAddApplicationSpace extends UIForm implements UIPopupComponent {

  private UIPageIterator iterator_;
  private String spaceId; 
  
  public UIAddApplicationSpace() throws Exception {
    iterator_ = createUIComponent(UIPageIterator.class, null, null);
    addChild(iterator_);
  }
  
  public void setSpaceId(String spaceId) throws Exception {
    this.spaceId = spaceId;
    List<Application> list;
    list = SpaceUtils.getAllApplications(spaceId);
    // remove installed app
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    Space space = spaceSrc.getSpaceById(spaceId);
    String appList = space.getApp();
    if(appList != null) {
      for(Application app : list) {
        String appName = app.getApplicationName();
        if(appList.contains(appName)){
          list.remove(app);
        }
      }
    }
    PageList pageList = new ObjectPageList(list,3);
    iterator_.setPageList(pageList);
  }
  
  @SuppressWarnings("unchecked")
  public List<Application> getApplications() throws Exception {
    List<Application> lists;
    lists = iterator_.getCurrentPageData();
    return lists;
  }
 
  public UIPageIterator getUIPageIterator() { return iterator_;}

  static public class CloseActionListener extends EventListener<UIAddApplicationSpace> {
    public void execute(Event<UIAddApplicationSpace> event) throws Exception {
      UIAddApplicationSpace uiSpaceApp = event.getSource();
      UIManageSpacesPortlet uiPortlet = (UIManageSpacesPortlet)uiSpaceApp.getAncestorOfType(UIManageSpacesPortlet.class);
      UIPopupContainer uiPopup = uiPortlet.getChild(UIPopupContainer.class);
      uiPopup.cancelPopupAction();
    }
  }
  
  static public class InstallActionListener extends EventListener<UIAddApplicationSpace> {
    public void execute(Event<UIAddApplicationSpace> event) throws Exception {
      UIAddApplicationSpace uiform = event.getSource();
      WebuiRequestContext request = event.getRequestContext();
      UIApplication uiApp = request.getUIApplication();
      SpaceService spaceService = uiform.getApplicationComponent(SpaceService.class);
      SpaceService spaceSrc = uiform.getApplicationComponent(SpaceService.class);
      String appId = event.getRequestContext().getRequestParameter(OBJECTID);
      Space space = spaceSrc.getSpaceById(uiform.spaceId);
      if(space.getApp() != null && space.getApp().indexOf(appId) != -1) {
        uiApp.addMessage(new ApplicationMessage("UIAddApplicationSpace.msg.app-exist", null));
        request.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      spaceService.installApplication(uiform.spaceId, appId);
      spaceService.activateApplication(uiform.spaceId, appId);
      UIManageSpacesPortlet uiPortlet = (UIManageSpacesPortlet)uiform.getAncestorOfType(UIManageSpacesPortlet.class);
      UISpaceApplication uiSpaceApp = uiPortlet.getChild(UISpaceSetting.class).getChild(UITabPane.class).getChild(UISpaceApplication.class);
      uiSpaceApp.setValue(spaceSrc.getSpaceById(uiform.spaceId));
      request.addUIComponentToUpdateByAjax(uiSpaceApp);
      UIPopupContainer uiPopup = uiPortlet.getChild(UIPopupContainer.class);
      uiPopup.cancelPopupAction();
    }
  }

  public void activate() throws Exception {}

  public void deActivate() throws Exception {}
}
