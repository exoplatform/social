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
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
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
  private final String iteratorID = "UIIteratorAddSpaceApplication";
  private final String HOME_APPLICATION = "HomeSpacePortlet";
  
  public UIAddApplicationSpace() throws Exception {
    iterator_ = createUIComponent(UIPageIterator.class, null, iteratorID);
    addChild(iterator_);
  }
  
  public void setSpaceId(String spaceId) throws Exception {
    this.spaceId = spaceId;
    List<Application> list;
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    Space space = spaceSrc.getSpaceById(spaceId);
    list = SpaceUtils.getApplications(space.getGroupId());
    // remove installed app
    String appList = space.getApp();
    if(appList != null) {
      for(Application app : list) {
        String appName = app.getApplicationName();
        if(appList.contains(appName) || appName.equals(HOME_APPLICATION)){
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
      UISpaceApplication uiForm = (UISpaceApplication)uiSpaceApp.getAncestorOfType(UISpaceApplication.class);
      UIPopupContainer uiPopup = uiForm.getChild(UIPopupContainer.class);
      uiPopup.cancelPopupAction();
    }
  }
  
  static public class InstallActionListener extends EventListener<UIAddApplicationSpace> {
    public void execute(Event<UIAddApplicationSpace> event) throws Exception {
      UIAddApplicationSpace uiform = event.getSource();
      SpaceService spaceService = uiform.getApplicationComponent(SpaceService.class);
      
      String appId = event.getRequestContext().getRequestParameter(OBJECTID);
      spaceService.installApplication(uiform.spaceId, appId);
      spaceService.activateApplication(uiform.spaceId, appId);
      
      UISpaceApplication uiForm = (UISpaceApplication)uiform.getAncestorOfType(UISpaceApplication.class);
      Space space = spaceService.getSpaceById(uiform.spaceId);
      uiForm.setValue(space);
      SpaceUtils.updateWorkingWorkSpace();
      UIPopupContainer uiPopup = uiForm.getChild(UIPopupContainer.class);
      uiPopup.cancelPopupAction();
      
    }
  }

  public void activate() throws Exception {}

  public void deActivate() throws Exception {}
}
