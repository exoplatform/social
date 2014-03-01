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

import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "war:/groovy/social/webui/space/UIAddApplicationSpace.gtmpl",
  events = {
    @EventConfig(listeners = UIAddApplicationSpace.CloseActionListener.class),
    @EventConfig(listeners = UIAddApplicationSpace.InstallActionListener.class)
  }
)
public class UIAddApplicationSpace extends UIForm implements UIPopupComponent {

  private UIPageIterator iterator_;
  private String spaceId;
  private static final String iteratorID = "UIIteratorAddSpaceApplication";
  private final String HOME_APPLICATION = "HomeSpacePortlet";

  /**
   * constructor
   *
   * @throws Exception
   */
  public UIAddApplicationSpace() throws Exception {
    iterator_ = createUIComponent(UIPageIterator.class, null, iteratorID);
    addChild(iterator_);
  }

  /**
   * sets spaceId for current space
   *
   * @param spaceId
   * @throws Exception
   */
  public void setSpaceId(String spaceId) throws Exception {
    this.spaceId = spaceId;
    List<Application> list;
    Space space = Utils.getSpaceService().getSpaceById(spaceId);
    list = SpaceUtils.getApplications(space.getGroupId());
    // remove installed app
    String appList = space.getApp();
    if (appList != null) {
      for (Application app : list) {
        String appName = app.getApplicationName();
        if (appList.contains(appName) || appName.equals(HOME_APPLICATION)) {
          list.remove(app);
        }
      }
    }
  }

  /**
   * gets application list
   *
   * @return application list
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<Application> getApplications() throws Exception {
    List<Application> lists;
    lists = iterator_.getCurrentPageData();
    return lists;
  }

  /**
   * gets uiPageIterator
   *
   * @return uiPageIterator
   */
  public UIPageIterator getUIPageIterator() {
    return iterator_;
  }

  /**
   * triggers this action when user clicks on close button.
   *
   * @author hoatle
   */
  public static class CloseActionListener extends EventListener<UIAddApplicationSpace> {
    public void execute(Event<UIAddApplicationSpace> event) throws Exception {
      UIAddApplicationSpace uiSpaceApp = event.getSource();
      UISpaceApplication uiForm = (UISpaceApplication) uiSpaceApp.getAncestorOfType(UISpaceApplication.class);
      UIPopupContainer uiPopup = uiForm.getChild(UIPopupContainer.class);
      uiPopup.cancelPopupAction();
    }
  }

  /**
   * triggers this action when user clicks on install button.
   *
   * @author hoatle
   */
  public static class InstallActionListener extends EventListener<UIAddApplicationSpace> {
    public void execute(Event<UIAddApplicationSpace> event) throws Exception {
      UIAddApplicationSpace uiform = event.getSource();
      SpaceService spaceService = uiform.getApplicationComponent(SpaceService.class);

      String appId = event.getRequestContext().getRequestParameter(OBJECTID);
      spaceService.installApplication(uiform.spaceId, appId);
      spaceService.activateApplication(uiform.spaceId, appId);

      UISpaceApplication uiForm = uiform.getAncestorOfType(UISpaceApplication.class);
      Space space = spaceService.getSpaceById(uiform.spaceId);
      uiForm.setValue(space);
      SpaceUtils.updateWorkingWorkSpace();
      UIPopupContainer uiPopup = uiForm.getChild(UIPopupContainer.class);
      uiPopup.cancelPopupAction();

    }
  }

  public void activate() {
  }

  public void deActivate() {
  }
}
