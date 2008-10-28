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

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
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
 * Sep 11, 2008          
 */

@ComponentConfig(
    template = "app:/groovy/portal/webui/component/ManageAllSpaceWorkingArea.gtmpl",
    events = {
        @EventConfig(listeners = ManageAllSpaceWorkingArea.RequestJoinActionListener.class),
        @EventConfig(listeners = ManageAllSpaceWorkingArea.ChangeListSpacesActionListener.class)
    }
)
public class ManageAllSpaceWorkingArea extends UIContainer {

  public ManageAllSpaceWorkingArea() throws Exception {
  }

  public List<Space> getAllSpaces() throws Exception {
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    List<Space> allSpaces = spaceSrc.getAllSpaces();
    return allSpaces;
  }
  
  static public class ChangeListSpacesActionListener extends EventListener<ManageAllSpaceWorkingArea> {
    public void execute(Event<ManageAllSpaceWorkingArea> event) throws Exception {
      ManageAllSpaceWorkingArea workingAllSpaceArea = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UISpacesManage uiSpacesManage = workingAllSpaceArea.getParent();
      ManageUserSpaceWorkingArea workingUserArea = uiSpacesManage.getChild(ManageUserSpaceWorkingArea.class);
      workingAllSpaceArea.setRendered(false);
      workingUserArea.setRendered(true);
      requestContext.addUIComponentToUpdateByAjax(uiSpacesManage);
    }
  }
  
  static public class RequestJoinActionListener extends EventListener<ManageAllSpaceWorkingArea> {
    public void execute(Event<ManageAllSpaceWorkingArea> event) throws Exception {
      ManageAllSpaceWorkingArea workingAllSpaceArea = event.getSource();
      OrganizationService orgSrc = workingAllSpaceArea.getApplicationComponent(OrganizationService.class);
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = requestContext.getRemoteUser();
      UIApplication uiApp = requestContext.getUIApplication();
      SpaceService spaceSrc = workingAllSpaceArea.getApplicationComponent(SpaceService.class);
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      Space space = spaceSrc.getSpace(spaceId);
      String groupId = space.getGroupId();
      
      List userList = orgSrc.getUserHandler().findUsersByGroup(groupId).currentPage();
      for(Object obj : userList) {
        User user = (User)obj;
        if(user.getUserName().equals(userName)) {
          uiApp.addMessage(new ApplicationMessage("ManageAllSpaceWorkingArea.msg.exist-user", null));
          requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }
      
      String pendingUser = space.getPendingUser();
      if(pendingUser != null) {
        String[] pendingList = pendingUser.split(",");
        for(int i=0; i<pendingList.length; i++) {
          if(pendingList[i].equals(userName)) {
            uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.pedding-exist-user", null));
            requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
      }
      if (pendingUser==null) pendingUser = userName;
      else pendingUser += "," + userName;
      space.setPendingUser(pendingUser);
      spaceSrc.saveSpace(space, false);
      uiApp.addMessage(new ApplicationMessage("ManageAllSpaceWorkingArea.msg.success-join-user", null,ApplicationMessage.INFO));
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    }
  }

}
