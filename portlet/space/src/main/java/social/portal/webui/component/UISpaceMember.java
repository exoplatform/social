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
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 12, 2008          
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/uiform/UISpaceMember.gtmpl",
    events = {
        @EventConfig(listeners = UISpaceMember.InviteActionListener.class),
        @EventConfig(listeners = UISpaceMember.SearchUserActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UISpaceMember.RevokeInvitedUserActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UISpaceMember.DeclineUserActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UISpaceMember.ValidateUserActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UISpaceMember.RemoveUserActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UISpaceMember.RemoveLeaderActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UISpaceMember.MakeLeaderActionListener.class, phase=Phase.DECODE)
      }
)
public class UISpaceMember extends UIForm {

  private Space space;
  private final static String user = "user";
  
  public UISpaceMember() throws Exception {
    addUIFormInput(new UIFormStringInput(user,null,null).addValidator(MandatoryValidator.class));
  }

  public String getInvitedUser() {
    return getUIStringInput(user).getValue();
  }
  
  public void setInvitedUser(String invitedUser) {
    getUIStringInput(user).setValue(invitedUser);
  }
  
  public void setValue(Space space) {
    this.space = space;
  }
  
  public List<String> getPenddingUsers() {
    List<String> pendingUsersList = new ArrayList<String>();
    String pendingUsers = space.getPendingUser();
    if(pendingUsers != null) {
      String[] tmpStrArr = pendingUsers.split(",");
      for(int i=0; i<tmpStrArr.length; i++) 
        pendingUsersList.add(tmpStrArr[i]);
    }
    return pendingUsersList;
  }
  
  public List<String> getInvitedUsers() {
    List<String> invitedUsersList = new ArrayList<String>();
    String invitedUsers = space.getInvitedUser();
    if(invitedUsers != null) {
      String[] tmpStrArr = invitedUsers.split(",");
      for(int i=0; i<tmpStrArr.length; i++) 
        invitedUsersList.add(tmpStrArr[i]);
    }
    return invitedUsersList;
  }
  
  
  public List<String> getExistingUsers() throws SpaceException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);

    return spaceService.getMembers(space);
  }

  
  public boolean isLeader(String userName) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);

    return spaceService.isLeader(space, userName);
  }
  
  static public class InviteActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();

      String invitedUser = uiSpaceMember.getInvitedUser();
      
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);

      try {
        spaceService.invite(uiSpaceMember.space, invitedUser);
      } catch (SpaceException e) {
        if(e.getCode() == SpaceException.Code.USER_NOT_EXIST) {
          uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.select-user", null));
          requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        } else if (e.getCode() == SpaceException.Code.USER_ALREADY_INVITED) {
          uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-invited-exist", null));
          requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        } else if(e.getCode() == SpaceException.Code.USER_ALREADY_MEMBER) {
          uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-exist", null));
          requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        }

        return;
      }
      uiSpaceMember.setInvitedUser(null);
      
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class SearchUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIManageSpacesPortlet uiPortlet = uiSpaceMember.getAncestorOfType(UIManageSpacesPortlet.class);
      UIPopupContainer uiPopup = uiPortlet.getChild(UIPopupContainer.class);
      uiPopup.activate(UIInviteUsers.class, 600);
      uiPopup.getChild(UIPopupWindow.class).setId("InviteUsers");
      requestContext.addUIComponentToUpdateByAjax(uiPopup);
    }
  }
  
  static public class RevokeInvitedUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);

      spaceService.revokeInvitation(uiSpaceMember.space, userName);

      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class DeclineUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);
      SpaceService spaceService = uiSpaceMember.getApplicationComponent(SpaceService.class);

      spaceService.denyInvitation(uiSpaceMember.space, userName);
      
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class RemoveUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);
      SpaceService spaceService = uiSpaceMember.getApplicationComponent(SpaceService.class);

      spaceService.removeMember(uiSpaceMember.space, userName);

      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class ValidateUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getApplicationComponent(SpaceService.class);
      spaceService.acceptInvitation(uiSpaceMember.space, userName);

      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class RemoveLeaderActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getApplicationComponent(SpaceService.class);
      spaceService.setLeader(uiSpaceMember.space, userName, false);

      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class MakeLeaderActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getApplicationComponent(SpaceService.class);
      spaceService.setLeader(uiSpaceMember.space, userName, true);

      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
}
