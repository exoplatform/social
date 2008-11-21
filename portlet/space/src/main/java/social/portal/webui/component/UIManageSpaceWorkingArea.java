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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.UIWelcomeComponent;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace.UIControlWSWorkingArea;
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
    template = "app:/groovy/portal/webui/component/UIManageSpaceWorkingArea.gtmpl",
    events = {
        @EventConfig(listeners = UIManageSpaceWorkingArea.RequestJoinActionListener.class),
        @EventConfig(listeners = UIManageSpaceWorkingArea.ChangeListSpacesActionListener.class),
        @EventConfig(listeners = UIManageSpaceWorkingArea.EditSpaceActionListener.class),
        @EventConfig(listeners = UIManageSpaceWorkingArea.LeaveSpaceActionListener.class),
        @EventConfig(listeners = UIManageSpaceWorkingArea.AcceptUserActionListener.class),
        @EventConfig(listeners = UIManageSpaceWorkingArea.DenyUserActionListener.class)
    }
)
public class UIManageSpaceWorkingArea extends UIContainer {

  private boolean isAllSpace = false;
  
  public List<Space> getAllSpaces() throws Exception {
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    List<Space> allSpaces = spaceSrc.getAllSpaces();
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    MembershipHandler memberShipHandler = orgSrc.getMembershipHandler();
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    if(isAllSpace == false) {
      Iterator<Space> itr = allSpaces.iterator();
      while(itr.hasNext()) {
        Space space = itr.next();
        if(memberShipHandler.findMembershipsByUserAndGroup(userName, space.getGroupId()).size() == 0){
          itr.remove();
        }
      }
    }
    return allSpaces;
  }
  
  public boolean isAllSpace() {
    return isAllSpace;
  }
  
  @SuppressWarnings("unchecked")
  public int displayAction(String spaceId) throws Exception {
    // 0: request to join, 1: in pendingList, 2: manager, 3: member
    String user = Util.getPortalRequestContext().getRemoteUser();
    OrganizationService orgService = getApplicationComponent(OrganizationService.class);
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    Space space = spaceSrc.getSpace(spaceId);
    String pendingList = space.getPendingUser();
    MembershipHandler memberShipHandler = orgService.getMembershipHandler();
    Collection<Membership> memberShips= memberShipHandler.findMembershipsByUserAndGroup(user, space.getGroupId());
    Iterator<Membership> itr = memberShips.iterator();
    if(memberShips.size() > 0){
      while (itr.hasNext()) {
        Membership memberShip = itr.next();
        if(memberShip.getMembershipType().equals("manager")) return 2;
      }
      return 3;
    }
    if(pendingList != null && pendingList.contains(user)) return 1;
    return 0;
  }
  
  public boolean isInInvitedList(String invitedList) {
    String user = Util.getPortalRequestContext().getRemoteUser();
    if(invitedList != null && invitedList.contains(user)) return true;
    else return false;
  }
  
  static public class ChangeListSpacesActionListener extends EventListener<UIManageSpaceWorkingArea> {
    public void execute(Event<UIManageSpaceWorkingArea> event) throws Exception {
      UIManageSpaceWorkingArea uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      uiForm.isAllSpace = !uiForm.isAllSpace;
      requestContext.addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static public class EditSpaceActionListener extends EventListener<UIManageSpaceWorkingArea> {
    public void execute(Event<UIManageSpaceWorkingArea> event) throws Exception {
      UIManageSpaceWorkingArea uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIManageSpacesPortlet uiPortlet = uiForm.getAncestorOfType(UIManageSpacesPortlet.class);
      UISpaceSetting uiSpaceSetting = uiPortlet.getChild(UISpaceSetting.class);
      uiPortlet.getChild(UISpacesManage.class).setRendered(false);
      uiSpaceSetting.setRendered(true);
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      SpaceService spaceSrc = uiForm.getApplicationComponent(SpaceService.class);
      Space space = spaceSrc.getSpace(spaceId);
      uiSpaceSetting.setValues(space);
      requestContext.addUIComponentToUpdateByAjax(uiPortlet);
    }
  }
  
  static public class LeaveSpaceActionListener extends EventListener<UIManageSpaceWorkingArea> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIManageSpaceWorkingArea> event) throws Exception {
      UIManageSpaceWorkingArea uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      OrganizationService orgService = uiForm.getApplicationComponent(OrganizationService.class);
      SpaceService spaceSrc = uiForm.getApplicationComponent(SpaceService.class);
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      Space space = spaceSrc.getSpace(spaceId);
      String groupID = space.getGroupId();
      MembershipHandler memberShipHandler = orgService.getMembershipHandler();
      Collection<Membership> memberShips = memberShipHandler.findMembershipsByUserAndGroup(requestContext.getRemoteUser(), groupID);
      Iterator<Membership> itr = memberShips.iterator();
      while(itr.hasNext()) {
        Membership mbShip = itr.next();
        Membership memberShip = memberShipHandler.findMembershipByUserGroupAndType(requestContext.getRemoteUser(), groupID, mbShip.getMembershipType());
        memberShipHandler.removeMembership(memberShip.getId(), true);
      }
      requestContext.addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static public class AcceptUserActionListener extends EventListener<UIManageSpaceWorkingArea> {
    public void execute(Event<UIManageSpaceWorkingArea> event) throws Exception {
      UIManageSpaceWorkingArea uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = requestContext.getRemoteUser();
      SpaceService spaceSrc = uiForm.getApplicationComponent(SpaceService.class);
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      Space space = spaceSrc.getSpace(spaceId);
      
      // remove user from invited user list
      String invitedUser = space.getInvitedUser();
      invitedUser = invitedUser.replace(userName, "");
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
      
////      uiApp.addMessage(new ApplicationMessage("UISpaceInvitation.msg.accept", null));
////      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      requestContext.addUIComponentToUpdateByAjax(uiForm);
      
      // auto reload portal navigation
      UIPortal uiPortal = Util.getUIPortal();
      UserPortalConfigService dataService = uiForm.getApplicationComponent(UserPortalConfigService.class);
      UserPortalConfig portalConfig  = dataService.getUserPortalConfig(uiPortal.getName(), userName);
      uiPortal.setNavigation(portalConfig.getNavigations());
      
      UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
      PortalRequestContext prContext = Util.getPortalRequestContext();
      
      UIControlWorkspace uiControl = uiPortalApp.getChildById(UIPortalApplication.UI_CONTROL_WS_ID);
      prContext.addUIComponentToUpdateByAjax(uiControl);    
      
      UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      prContext.addUIComponentToUpdateByAjax(uiWorkingWS) ;
      prContext.setFullRender(true);
      
      // go to space node manage
      PageNavigation portalNavigation = dataService.getPageNavigation(PortalConfig.PORTAL_TYPE, uiPortal.getName());
      PageNodeEvent<UIPortal> pnevent = 
        new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, Integer.toString(portalNavigation.getId()) + "::spaces") ;
      uiPortal.broadcast(pnevent, Event.Phase.PROCESS) ;
    }
  }
  
  static public class DenyUserActionListener extends EventListener<UIManageSpaceWorkingArea> {
    public void execute(Event<UIManageSpaceWorkingArea> event) throws Exception {
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIManageSpaceWorkingArea uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = requestContext.getRemoteUser();
      SpaceService spaceSrc = uiForm.getApplicationComponent(SpaceService.class);
      Space space = spaceSrc.getSpace(spaceId);
      UIApplication uiApp = requestContext.getUIApplication();
      
      // remove user from invited user list
      String invitedUser = space.getInvitedUser();
      invitedUser = invitedUser.replace(userName, "");
      if(invitedUser.contains(",,")) invitedUser = invitedUser.replace(",,", ",");
      if(invitedUser.indexOf(",") == 0) invitedUser = invitedUser.substring(1);
      if(invitedUser.equals("")) invitedUser=null;
      space.setInvitedUser(invitedUser);
      spaceSrc.saveSpace(space, false);
      
      uiApp.addMessage(new ApplicationMessage("UISpaceInvitation.msg.decline", null));
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      requestContext.addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static public class RequestJoinActionListener extends EventListener<UIManageSpaceWorkingArea> {
    public void execute(Event<UIManageSpaceWorkingArea> event) throws Exception {
      UIManageSpaceWorkingArea workingAllSpaceArea = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = requestContext.getRemoteUser();
      UIApplication uiApp = requestContext.getUIApplication();
      SpaceService spaceSrc = workingAllSpaceArea.getApplicationComponent(SpaceService.class);
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      Space space = spaceSrc.getSpace(spaceId);
      
      String pendingUser = space.getPendingUser();
      if (pendingUser==null) pendingUser = userName;
      else pendingUser += "," + userName;
      space.setPendingUser(pendingUser);
      spaceSrc.saveSpace(space, false);
      uiApp.addMessage(new ApplicationMessage("UIManageSpaceWorkingArea.msg.success-join-user", null,ApplicationMessage.INFO));
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      requestContext.addUIComponentToUpdateByAjax(workingAllSpaceArea);
    }
  }

}
