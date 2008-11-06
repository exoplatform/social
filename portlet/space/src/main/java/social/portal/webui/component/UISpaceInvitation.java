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

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 07, 2008          
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/portal/webui/uiform/UISpaceInvitation.gtmpl",
    events = {
        @EventConfig(listeners = UISpaceInvitation.AcceptActionListener.class ),
        @EventConfig(listeners = UISpaceInvitation.DeclineActionListener.class )
      }
)
public class UISpaceInvitation extends UIForm{
  
  private static String userName;
  private static Space space;
  
  static public class AcceptActionListener extends EventListener<UISpaceInvitation> {
    public void execute(Event<UISpaceInvitation> event) throws Exception {
      UISpaceInvitation uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      SpaceService spaceSrc = uiForm.getApplicationComponent(SpaceService.class);
      String invitedUser = space.getInvitedUser();
      UIApplication uiApp = requestContext.getUIApplication();
      
      // remove user from invited user list
      invitedUser = invitedUser.replace(invitedUser, "");
      if(invitedUser.contains(",,")) invitedUser = invitedUser.replace(",,", ",");
      if(invitedUser.indexOf(",") == 0) invitedUser = invitedUser.substring(1);
      if(invitedUser.equals("")) invitedUser=null;
      space.setInvitedUser(invitedUser);
      spaceSrc.saveSpace(space, false);
      
      // add member
      OrganizationService orgSrc = uiForm.getApplicationComponent(OrganizationService.class);
      UserHandler userHandler = orgSrc.getUserHandler();
      User user = userHandler.findUserByName(userName);
      MembershipType mbShipType = orgSrc.getMembershipTypeHandler().findMembershipType("member");
      MembershipHandler membershipHandler = orgSrc.getMembershipHandler();
      Group spaceGroup = orgSrc.getGroupHandler().findGroupById(space.getGroupId());
      membershipHandler.linkMembership(user, spaceGroup, mbShipType, true);
      
      // back
      UIManageSpacesPortlet uiPortlet = uiForm.getAncestorOfType(UIManageSpacesPortlet.class);
      uiPortlet.getChild(UISpaceSetting.class).setRendered(false);
      uiPortlet.getChild(UISpacesManage.class).setRendered(true);
      uiForm.setRendered(false);
      uiApp.addMessage(new ApplicationMessage("UISpaceInvitation.msg.accept", null));
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      requestContext.addUIComponentToUpdateByAjax(uiPortlet);
      
    }
  }
   
  static public class DeclineActionListener extends EventListener<UISpaceInvitation> {
    public void execute(Event<UISpaceInvitation> event) throws Exception {
      UISpaceInvitation uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      SpaceService spaceSrc = uiForm.getApplicationComponent(SpaceService.class);
      String invitedUser = space.getInvitedUser();
      UIApplication uiApp = requestContext.getUIApplication();
      
      // remove user from invited user list
      invitedUser = invitedUser.replace(invitedUser, "");
      if(invitedUser.contains(",,")) invitedUser = invitedUser.replace(",,", ",");
      if(invitedUser.indexOf(",") == 0) invitedUser = invitedUser.substring(1);
      if(invitedUser.equals("")) invitedUser=null;
      space.setInvitedUser(invitedUser);
      spaceSrc.saveSpace(space, false);
      // back
      UIManageSpacesPortlet uiPortlet = uiForm.getAncestorOfType(UIManageSpacesPortlet.class);
      uiPortlet.getChild(UISpaceSetting.class).setRendered(false);
      uiPortlet.getChild(UISpacesManage.class).setRendered(true);
      uiForm.setRendered(false);
    uiApp.addMessage(new ApplicationMessage("UISpaceInvitation.msg.decline", null));
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      requestContext.addUIComponentToUpdateByAjax(uiPortlet);
    }
  }
  
  public void setValue(String userName, Space space) {
    this.userName = userName;
    this.space = space;
  }
}