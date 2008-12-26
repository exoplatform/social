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
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 12, 2008          
 */

@ComponentConfigs ( {
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
 ),
 @ComponentConfig(
    type = UIPopupWindow.class,
    id = "SearchUser",
    template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
    events = {
      @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
      @EventConfig(listeners = UISpaceMember.CloseActionListener.class, name = "Close", phase = Phase.DECODE)  ,
      @EventConfig(listeners = UISpaceMember.AddActionListener.class, name = "Add", phase = Phase.DECODE)
    }
 )
})



public class UISpaceMember extends UIForm {

  private Space space;
  private final static String user = "user";
  
  public UISpaceMember() throws Exception {
    addUIFormInput(new UIFormStringInput(user,null,null)
                    .addValidator(MandatoryValidator.class)
                    .addValidator(ExpressionValidator.class, "^\\p{L}[\\p{L}\\d._,]+\\p{L}$", "UISpaceMember.msg.Invalid-char"));
    UIPopupWindow searchUserPopup = addChild(UIPopupWindow.class, "SearchUser", "SearchUser");
    searchUserPopup.setWindowSize(640, 0); 
  }
  
  public void setValue(Space space) {
    this.space = space;
  }
  
  public List<String> getPenddingUsers() {
    List<String> pendingUsersList = new ArrayList<String>();
    String[] pendingUsers = space.getPendingUsers();
    if(pendingUsers != null) {
      pendingUsersList.addAll(Arrays.asList(pendingUsers));
    }
    return pendingUsersList;
  }
  
  public List<String> getInvitedUsers() {
    List<String> invitedUsersList = new ArrayList<String>();
    String[] invitedUsers = space.getInvitedUsers();
    if(invitedUsers != null) {
      invitedUsersList.addAll(Arrays.asList(invitedUsers));
    }
    return invitedUsersList;
  }
  
  
  public List<String> getExistingUsers() throws SpaceException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);

    return spaceService.getMembers(space);
  }

  public void setUsersName(String userName) {
    getUIStringInput(user).setValue(userName); 
  }
  
  public String getUsersName() {
    return getUIStringInput(user).getValue(); 
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
      String[] invitedUserList = uiSpaceMember.getUsersName().split(",");      
      SpaceService spaceService = uiSpaceMember.getApplicationComponent(SpaceService.class);
      String usersNotExist = null;
      String usersIsInvited = null;
      String usersIsMember = null;
      for(String invitedUser : invitedUserList){
        try {
          spaceService.invite(uiSpaceMember.space, invitedUser);
        } catch (SpaceException e) {
          if(e.getCode() == SpaceException.Code.USER_NOT_EXIST) {
            if(usersNotExist == null) usersNotExist = invitedUser;
            else usersNotExist += "," +invitedUser; 
          } else if (e.getCode() == SpaceException.Code.USER_ALREADY_INVITED) {
            if(usersIsInvited == null) usersIsInvited = invitedUser;
            else usersIsInvited += "," +invitedUser;
          } else if(e.getCode() == SpaceException.Code.USER_ALREADY_MEMBER) {
            if(usersIsMember == null) usersIsMember = invitedUser;
            else usersIsMember += "," +invitedUser;
          }
        }
      }
      String remainUsers = null;
      if(usersNotExist != null){
        remainUsers = usersNotExist;
        uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-not-exist", new String[] {usersNotExist},ApplicationMessage.WARNING));
      }
      if(usersIsInvited != null){
        if(remainUsers == null) remainUsers = usersIsInvited;
        else remainUsers += "," + usersIsInvited;
        uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-is-invited",new String[] {usersIsInvited},ApplicationMessage.WARNING));
      }
      if(usersIsMember != null){
        if(remainUsers == null) remainUsers = usersIsMember;
        else remainUsers += "," + usersIsMember;
        uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-is-member",new String[] {usersIsMember},ApplicationMessage.WARNING));
      }     
      uiSpaceMember.setUsersName(remainUsers);    
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    }
  }
  
  static public class SearchUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      UIPopupWindow searchUserPopup = uiSpaceMember.getChild(UIPopupWindow.class);
      UIUserSelector userSelector = uiSpaceMember.createUIComponent(UIUserSelector.class, null, null);
      userSelector.setShowSearchGroup(false);
      searchUserPopup.setUIComponent(userSelector);
      searchUserPopup.setShow(true);
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
      spaceService.declineRequest(uiSpaceMember.space, userName);
      
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
      spaceService.validateRequest(uiSpaceMember.space, userName);

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
  
  static public class AddActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiForm = event.getSource();
      UISpaceMember uiSpaceMember = uiForm.getAncestorOfType(UISpaceMember.class);
      uiSpaceMember.setUsersName(uiForm.getSelectedUsers());
      UIPopupWindow uiPopup = uiSpaceMember.getChild(UIPopupWindow.class);
      uiPopup.setUIComponent(null);
      uiPopup.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class CloseActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {            
      UIUserSelector uiForm = event.getSource();
      UISpaceMember uiSpaceMember = uiForm.getAncestorOfType(UISpaceMember.class);
      UIPopupWindow uiPopup = uiSpaceMember.getChild(UIPopupWindow.class);
      uiPopup.setUIComponent(null);
      uiPopup.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
}
