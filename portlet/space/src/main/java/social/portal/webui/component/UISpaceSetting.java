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

import org.exoplatform.social.space.Space;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 12, 2008          
 */

@ComponentConfig(
    template =  "app:/groovy/portal/webui/uiform/UISpaceSetting.gtmpl",
    events = @EventConfig(listeners = UISpaceSetting.BackActionListener.class)
)
public class UISpaceSetting extends UIContainer {

  final private static String SPACE_INFO = "info";
  final private static String SPACE_APP = "app";
  final private static String SPACE_MEMBER = "members";
  private String spaceName;
  
  public UISpaceSetting() throws Exception {
    UITabPane uiTabPane = addChild(UITabPane.class, null, null);
    uiTabPane.addChild(UISpaceInfo.class, null, SPACE_INFO);
    uiTabPane.addChild(UISpaceMember.class, null, SPACE_MEMBER);
    uiTabPane.addChild(UISpaceApplication.class, null, SPACE_APP);
    uiTabPane.setSelectedTab(SPACE_INFO);
  }
  
  
  public void setValues(Space space) throws Exception {
    UISpaceInfo uiSpaceInfo = getChild(UITabPane.class).getChild(UISpaceInfo.class);
    uiSpaceInfo.setValue(space);
    UISpaceMember uiSpaceMember = getChild(UITabPane.class).getChild(UISpaceMember.class);
    uiSpaceMember.setValue(space);
    UISpaceApplication uiSpaceApplication = getChild(UITabPane.class).getChild(UISpaceApplication.class);
    uiSpaceApplication.setValue(space);
    this.spaceName = space.getName();
  } 
  
  public String getSpaceName() {
    return spaceName;
  }
  
  static public class BackActionListener extends EventListener<UISpaceSetting> {
    public void execute(Event<UISpaceSetting> event) throws Exception {
      UISpaceSetting uiSpaceSetting = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIManageSpacesPortlet uiPortlet = uiSpaceSetting.getAncestorOfType(UIManageSpacesPortlet.class);
      uiPortlet.getChild(UISpacesManage.class).setRendered(true);
      uiSpaceSetting.setRendered(false);
      requestContext.addUIComponentToUpdateByAjax(uiPortlet);
    }
  }
  
}



