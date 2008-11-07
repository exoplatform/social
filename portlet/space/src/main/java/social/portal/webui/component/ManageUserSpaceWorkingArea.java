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

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
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
    template = "app:/groovy/portal/webui/component/ManageUserSpaceWorkingArea.gtmpl",
    events = {
        @EventConfig(listeners = ManageUserSpaceWorkingArea.LeaveSpaceActionListener.class),
        @EventConfig(listeners = ManageUserSpaceWorkingArea.ChangeListSpacesActionListener.class),
        @EventConfig(listeners = ManageUserSpaceWorkingArea.EditSpaceActionListener.class)
    }
)
public class ManageUserSpaceWorkingArea extends UIContainer {

  public List<Space> getAllSpaces() throws Exception {
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    MembershipHandler memberShipHandler = orgSrc.getMembershipHandler();
    String userName = Util.getPortalRequestContext().getRemoteUser() ;
    List<Space> allSpaces = spaceSrc.getAllSpaces();
    Iterator<Space> itr = allSpaces.iterator();
    while(itr.hasNext()) {
      Space space = itr.next();
      if(memberShipHandler.findMembershipsByUserAndGroup(userName, space.getGroupId()).size() == 0){
        itr.remove();
      }
    }
    return allSpaces;
  }
  
  // will be remove in future - dang tung
  public boolean isEdit(String groupId) throws Exception {
    String user = Util.getPortalRequestContext().getRemoteUser();
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    MembershipHandler memberShipHandler = orgSrc.getMembershipHandler();
    if(memberShipHandler.findMembershipByUserGroupAndType(user, groupId, "manager") != null) return true;
    return false;
  }
  
  static public class EditSpaceActionListener extends EventListener<ManageUserSpaceWorkingArea> {
    public void execute(Event<ManageUserSpaceWorkingArea> event) throws Exception {
      ManageUserSpaceWorkingArea workingUserArea = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      UIManageSpacesPortlet uiPortlet = workingUserArea.getAncestorOfType(UIManageSpacesPortlet.class);
      UISpaceSetting uiSpaceSetting = uiPortlet.getChild(UISpaceSetting.class);
      uiPortlet.getChild(UISpacesManage.class).setRendered(false);
      uiSpaceSetting.setRendered(true);
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      SpaceService spaceSrc = workingUserArea.getApplicationComponent(SpaceService.class);
      Space space = spaceSrc.getSpace(spaceId);
      if(!workingUserArea.isEdit(space.getGroupId())) {
        uiApp.addMessage(new ApplicationMessage("ManageUserSpaceWorkingArea.msg.user.not-have-permission", null, ApplicationMessage.WARNING));
        requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      uiSpaceSetting.setValues(space);
      requestContext.addUIComponentToUpdateByAjax(uiPortlet);
    }
  }
  
  static public class ChangeListSpacesActionListener extends EventListener<ManageUserSpaceWorkingArea> {
    public void execute(Event<ManageUserSpaceWorkingArea> event) throws Exception {
      ManageUserSpaceWorkingArea workingUserArea = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UISpacesManage uiSpacesManage = workingUserArea.getParent();
      ManageAllSpaceWorkingArea workingAllSpaceArea = uiSpacesManage.getChild(ManageAllSpaceWorkingArea.class);
      workingAllSpaceArea.setRendered(true);
      workingUserArea.setRendered(false);
      requestContext.addUIComponentToUpdateByAjax(uiSpacesManage);
    }
  }
  
  static public class LeaveSpaceActionListener extends EventListener<ManageUserSpaceWorkingArea> {
    @SuppressWarnings("unchecked")
    public void execute(Event<ManageUserSpaceWorkingArea> event) throws Exception {
      ManageUserSpaceWorkingArea workingArea = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      OrganizationService orgService = workingArea.getApplicationComponent(OrganizationService.class);
      SpaceService spaceSrc = workingArea.getApplicationComponent(SpaceService.class);
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
      requestContext.addUIComponentToUpdateByAjax(workingArea);
    }
  }

}
