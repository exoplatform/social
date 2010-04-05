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

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
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

/**
 * {@link UISpaceSetting} used to manage space info, permission and members. <br />
 * Created by The eXo Platform SARL
 * 
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Sep 12, 2008
 */
@ComponentConfigs({
  @ComponentConfig(
    template = "app:/groovy/portal/webui/uiform/UISpaceSetting.gtmpl"
  ),
  @ComponentConfig(
    type = UITabPane.class,
    id = "UISpaceSettingTabPane",
    template = "system:/groovy/webui/core/UITabPane_New.gtmpl",
    events = { @EventConfig(listeners = UISpaceSetting.SelectTabActionListener.class) })
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
    uiTabPane.setSelectedTab(1);
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
    UISpaceApplication uiSpaceApplication = getChild(UITabPane.class).getChild(UISpaceApplication.class);
    uiSpaceApplication.setValue(space);
    UISpacePermission uiSpacePermission = getChild(UITabPane.class).getChild(UISpacePermission.class);
    uiSpacePermission.setValue(space);
    this.space = space;
  }

  /**
   * gets space name
   * 
   * @return space name
   */
  public String getSpaceName() {
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    try {
      return spaceSrc.getSpaceById(space.getId()).getName();
    } catch (SpaceException e) {
      return null;
    }
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
    if (spaceSrc.hasEditPermission(space, userId)) {
      return true;
    } else {
      return false;
    }
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
      if (!renderTab.equals(UISpaceApplication.class.getSimpleName())) {
        UISpaceApplication uiApplication = uiTabPane.getChild(UISpaceApplication.class);
        if (uiApplication != null) {
          UIPopupContainer uiPopupContainer = uiApplication.getChild(UIPopupContainer.class);
          uiPopupContainer.deActivate();
        }
      }
      uiTabPane.setSelectedTab(renderTab);
      context.setResponseComplete(true);
    }
  }
}
