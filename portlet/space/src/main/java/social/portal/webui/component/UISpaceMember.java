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
import org.exoplatform.services.organization.Membership;
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
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
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
    template =  "app:/groovy/portal/webui/uiform/UISpaceMember.gtmpl",
    events = {
        @EventConfig(listeners = UISpaceMember.InviteActionListener.class),
        @EventConfig(listeners = UISpaceMember.SearchUserActionListener.class),
        @EventConfig(listeners = UISpaceMember.RevokeInvitedUserActionListener.class),
        @EventConfig(listeners = UISpaceMember.DeclineUserActionListener.class),
        @EventConfig(listeners = UISpaceMember.ValidateUserActionListener.class),
        @EventConfig(listeners = UISpaceMember.RemoveUserActionListener.class)
      }
)
public class UISpaceMember extends UIForm {

  private String invitedUser;
  private Space space;
  
  public UISpaceMember() throws Exception {
  }

  public String getInvitedUser() {
    return invitedUser;
  }
  
  public void setInvitedUser(String invitedUser) {
    this.invitedUser = invitedUser;
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
  
  
  public List<String> getExistingUsers() throws Exception {
    List<String> existingUsersList = new ArrayList<String>();
    List<User> users = getUsersInSpace(space.getGroupId());
    for(User obj : users) existingUsersList.add(obj.getUserName());
    return existingUsersList;
  }
  
  @SuppressWarnings("unchecked")
  private List<User> getUsersInSpace(String groupId) throws Exception{
    List<User> users = new ArrayList<User>();
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    PageList usersPageList = orgSrc.getUserHandler().findUsersByGroup(groupId);
    users = usersPageList.currentPage();
    return users;
  }
  
  static public class InviteActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String invitedUser = uiSpaceMember.getInvitedUser();
      UIApplication uiApp = requestContext.getUIApplication();
      List<String> invitedUsers = uiSpaceMember.getInvitedUsers();
      List<String> existingUsers = uiSpaceMember.getExistingUsers();
      if(invitedUser==null) {
        uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.select-user", null));
        requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(invitedUsers.contains(invitedUser)) {
        uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-invited-exist", null));
        requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } else if (existingUsers.contains(invitedUser)) {
        uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-exist", null));
        requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if(invitedUsers.size() > 0) uiSpaceMember.space.setInvitedUser(uiSpaceMember.space.getInvitedUser() + "," + invitedUser);
      else uiSpaceMember.space.setInvitedUser(invitedUser);
      SpaceService spaceSrc = uiSpaceMember.getApplicationComponent(SpaceService.class);
      spaceSrc.saveSpace(uiSpaceMember.space, false);
      uiSpaceMember.setInvitedUser(null);
      
      // we'll sent a email to invite user
      MailService mailSrc = uiSpaceMember.getApplicationComponent(MailService.class);
      OrganizationService orgSrc = uiSpaceMember.getApplicationComponent(OrganizationService.class);
      ResourceBundle res = requestContext.getApplicationResourceBundle() ;
      String email = orgSrc.getUserHandler().findUserByName(invitedUser).getEmail();
      PortalRequestContext portalRequest = Util.getPortalRequestContext();
      String url = portalRequest.getRequest().getRequestURL().toString();
      String headerMail = res.getString(uiSpaceMember.getId()+ ".mail.header") + "\n\n";
      String footerMail = "\n\n\n" + res.getString(uiSpaceMember.getId()+ ".mail.footer");
      String activeLink = url + "?portal:componentId=managespaces&portal:type=action&portal:isSecure=false&uicomponent=UISpacesManage&op=JoinSpace&user="+invitedUser+"&space="+uiSpaceMember.space.getId();
      activeLink = headerMail + activeLink + footerMail;
      mailSrc.sendMessage("exoservice@gmail.com",email, "Invite to join space " + uiSpaceMember.space.getName(), activeLink);
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
      List<String> invitedUsers = uiSpaceMember.getInvitedUsers();
      invitedUsers.remove(userName);
      String temp="";
      for(String obj : invitedUsers) {
        temp += obj + ",";
      }
      if(!temp.equals("")) temp = temp.substring(0, temp.length()-1);
      else temp = null;
      uiSpaceMember.space.setInvitedUser(temp);
      SpaceService spaceSrc = uiSpaceMember.getApplicationComponent(SpaceService.class);
      spaceSrc.saveSpace(uiSpaceMember.space, false);
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class DeclineUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> pendingUsers = uiSpaceMember.getPenddingUsers();
      pendingUsers.remove(userName);
      String temp="";
      for(String obj : pendingUsers) {
        temp += obj + ",";
      }
      if(!temp.equals("")) temp = temp.substring(0, temp.length()-1);
      else temp = null;
      uiSpaceMember.space.setPendingUser(temp);
      SpaceService spaceSrc = uiSpaceMember.getApplicationComponent(SpaceService.class);
      spaceSrc.saveSpace(uiSpaceMember.space, false);
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class RemoveUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
      OrganizationService orgSrc = uiSpaceMember.getApplicationComponent(OrganizationService.class);
      UserHandler userHandler = orgSrc.getUserHandler();
      User user = userHandler.findUserByName(userName);
      MembershipHandler membershipHandler = orgSrc.getMembershipHandler();
      Membership memberShip = membershipHandler.findMembershipByUserGroupAndType(user.getUserName(), uiSpaceMember.space.getGroupId(), "member");
      membershipHandler.removeMembership(memberShip.getId(), true);
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
  static public class ValidateUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);
      OrganizationService orgSrc = uiSpaceMember.getApplicationComponent(OrganizationService.class);
      List<String> existingUsers = uiSpaceMember.getExistingUsers();
      List<String> pendingUsers = uiSpaceMember.getPenddingUsers();
      if (existingUsers.contains(userName)) {
        uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-exist", null));
        requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      existingUsers.add(userName);
      pendingUsers.remove(userName);
      String temp="";
      for(String obj : pendingUsers) {
        temp += obj + ",";
      }
      if(!temp.equals("")) temp = temp.substring(0, temp.length()-1);
      else temp = null;
      uiSpaceMember.space.setPendingUser(temp);
      SpaceService spaceSrc = uiSpaceMember.getApplicationComponent(SpaceService.class);
      spaceSrc.saveSpace(uiSpaceMember.space, false);
      // add member
      UserHandler userHandler = orgSrc.getUserHandler();
      User user = userHandler.findUserByName(userName);
      MembershipType mbShipType = orgSrc.getMembershipTypeHandler().findMembershipType("member");
      MembershipHandler membershipHandler = orgSrc.getMembershipHandler();
      Group spaceGroup = orgSrc.getGroupHandler().findGroupById(uiSpaceMember.space.getGroupId());
      membershipHandler.linkMembership(user, spaceGroup, mbShipType, true);
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }
  
}
