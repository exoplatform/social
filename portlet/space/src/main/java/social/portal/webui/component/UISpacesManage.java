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
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * August 27, 2008          
 */

@ComponentConfig(
    template = "app:/groovy/portal/webui/component/UISpacesManage.gtmpl",
    events = {
        @EventConfig(listeners = UISpacesManage.JoinSpaceActionListener.class)
    }
)
public class UISpacesManage extends UIContainer {

  public UISpacesManage() throws Exception {
    addChild(ManageSpaceControlArea.class,null,null);
    addChild(UIManageSpaceWorkingArea.class, null, null);
    addChild(UISpaceInvitation.class,null,null).setRendered(false);
  }
  
  static public class JoinSpaceActionListener extends EventListener<UISpacesManage> {
    public void execute(Event<UISpacesManage> event) throws Exception {
      UISpacesManage uiSpaceManage = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      
      UIManageSpacesPortlet uiPortlet = uiSpaceManage.getAncestorOfType(UIManageSpacesPortlet.class);
      UISpaceInvitation uiSpaceInvitation = uiSpaceManage.getChild(UISpaceInvitation.class);
      uiPortlet.getChild(UISpaceSetting.class).setRendered(false);
      uiSpaceManage.setRendered(true);
      
      String leader = requestContext.getRequestParameter("leader");
      String spaceId = requestContext.getRequestParameter("space");
      
      SpaceService spaceSrc = uiSpaceManage.getApplicationComponent(SpaceService.class);
      Space space = spaceSrc.getSpace(spaceId);
      String userName = requestContext.getRemoteUser();
      
      if(!spaceSrc.isInvited(space, userName)) {
        uiApp.addMessage(new ApplicationMessage("UISpaceManage.msg.user-revoke", null));
        requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      } else {
        uiSpaceInvitation.setValue(leader, space);
        uiSpaceInvitation.setRendered(true);
      }
      
      requestContext.addUIComponentToUpdateByAjax(uiPortlet);
    }
  }
}
