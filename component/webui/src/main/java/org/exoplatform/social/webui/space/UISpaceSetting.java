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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.common.router.ExoRouter.Route;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfigs({
  @ComponentConfig(
    template = "war:/groovy/social/webui/space/UISpaceSetting.gtmpl"
  ),
  @ComponentConfig(
    type = UITabPane.class,
    id = "UISpaceSettingTabPane",
    template = "war:/groovy/social/webui/space/UISpaceSettingPane.gtmpl",
    events = { @EventConfig(listeners = UITabPane.SelectTabActionListener.class) })
  })
public class UISpaceSetting extends UIContainer {

  private Space space;

  /**
   * constructor
   *
   * @throws Exception
   */
  public UISpaceSetting() throws Exception {
    UITabPane uiTabPane = addChild(UITabPane.class, null, null);
    uiTabPane.setComponentConfig(UITabPane.class, "UISpaceSettingTabPane");
    uiTabPane.addChild(UISpaceInfo.class, null, null);
    uiTabPane.addChild(UISpacePermission.class, null, null);
    uiTabPane.addChild(UISpaceMember.class, null, null);
    uiTabPane.addChild(UISpaceApplication.class, null, null);
    uiTabPane.addChild(UISpaceNavigationManagement.class, null, null);
    uiTabPane.setSelectedTab(1);
    
    String spaceUrl = SpaceUtils.getSpaceUrl();
    Space space  = getApplicationComponent(SpaceService.class).getSpaceByUrl(spaceUrl);
    if (space != null) {
      setValues(space);
    }
  }
  
  public void initTabByContext() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String requestPath = pcontext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    Route route = ExoRouter.route(requestPath);
    if (route != null) {
      String app = route.localArgs.get("appName");
      String path = route.localArgs.get("path");
      if ("settings".equals(app) && "members".equals(path)) {
        getChild(UITabPane.class).setSelectedTab(3);
      }
    }
  }

  /**
   * sets space to work with
   *
   * @param space
   * @throws Exception
   */
  public void setValues(Space space) throws Exception {
    UISpaceInfo uiSpaceInfo = getChild(UITabPane.class).getChild(UISpaceInfo.class);
    uiSpaceInfo.setValue(space);
    UISpaceMember uiSpaceMember = getChild(UITabPane.class).getChild(UISpaceMember.class);
    uiSpaceMember.setValue(space.getId());
    uiSpaceMember.setSpaceURL(space.getUrl());
    UISpaceApplication uiSpaceApplication = getChild(UITabPane.class).getChild(UISpaceApplication.class);
    uiSpaceApplication.setValue(space);
    UISpacePermission uiSpacePermission = getChild(UITabPane.class).getChild(UISpacePermission.class);
    uiSpacePermission.setValue(space);
    this.space = space;
    
    PortalRequestContext pContext = Util.getPortalRequestContext();
    UISpaceNavigationManagement uiSpaceNavigation = getChild(UITabPane.class).getChild(UISpaceNavigationManagement.class);
    uiSpaceNavigation.reloadTreeData();
    UISpaceSetting uiSpaceSetting = (UISpaceSetting)getChild(UITabPane.class).getParent();
    uiSpaceNavigation.setSpace(uiSpaceSetting.getSpace());
    pContext.addUIComponentToUpdateByAjax(uiSpaceNavigation);
  }

  /**
   * Gets space object.
   * 
   * @return
   */
  protected Space getSpace() {
    return this.space;
  }
  
  /**
   * gets space name
   *
   * @return space name
   */
  public String getSpaceName() {
    return space.getDisplayName();
  }

  /**
   * checks if the remote user is leader.
   *
   * @return true or false
   * @throws Exception
   */
  public boolean isLeader() throws Exception {
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    String userId = Util.getPortalRequestContext().getRemoteUser();
    Space currentSpace = spaceSrc.getSpaceById(space.getId());
    if (spaceSrc.hasEditPermission(currentSpace, userId)) {
      return true;
    } else {
      return false;
    }
  }
 
 /**
 * Redirect to home of space page in case accessing to administration pages (such as space setting page) 
 * but the role of logged in user is member only.
 * 
 * @param ctx
 * @since 1.2.8
 */
  protected void redirectToHome(WebuiRequestContext ctx) {
    JavascriptManager jsManager = ctx.getJavascriptManager();
    jsManager.addJavascript("try { window.location.href='" + Utils.getSpaceHomeURL(space) + "' } catch(e) {" +
        "window.location.href('" + Utils.getSpaceHomeURL(space) + "') }");
  }
  
  /**
   * This action listener is activated when user changes tab in space setting.
   * When selecting tabs, deactivates application add popup from application tab
   * @author hoatle
   */
  static public class SelectTabActionListener extends EventListener<UITabPane> {
    public void execute(Event<UITabPane> event) throws Exception {
      UITabPane uiTabPane = event.getSource();
      WebuiRequestContext context = event.getRequestContext();
      String renderTab = context.getRequestParameter(UIComponent.OBJECTID);
      if (renderTab == null)
        return;
      //SOC-1759 when user chooses the Space Navigation Tab.
      //The content must have to refresh.
      if (renderTab.equals(UISpaceNavigationManagement.class.getSimpleName())) {
        UISpaceNavigationManagement uiSpaceNavigation = uiTabPane.getChild(UISpaceNavigationManagement.class);
        uiSpaceNavigation.reloadTreeData();
        UISpaceSetting uiSpaceSetting = (UISpaceSetting)uiTabPane.getParent();
        uiSpaceNavigation.setSpace(uiSpaceSetting.getSpace());
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceNavigation);
      }
     
      if (!renderTab.equals(UISpaceApplication.class.getSimpleName())) {
        UISpaceApplication uiApplication = uiTabPane.getChild(UISpaceApplication.class);
        if (uiApplication != null) {
          UIPopupContainer uiPopupContainer = uiApplication.getChild(UIPopupContainer.class);
          uiPopupContainer.deActivate();
        }
      }
      uiTabPane.setSelectedTab(renderTab);
      //Not a good solution but let's accept it as temporary solution.
      context.addUIComponentToUpdateByAjax(uiTabPane);
    }
  }
}
