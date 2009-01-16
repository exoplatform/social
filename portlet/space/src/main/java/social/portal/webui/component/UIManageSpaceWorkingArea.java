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

import java.util.Iterator;
import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
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
  private UIPageIterator iterator_;
  private final String iteratorID = "UIIteratorSpaceWorking";
  
  public UIManageSpaceWorkingArea() throws Exception {
    iterator_ = createUIComponent(UIPageIterator.class, null, iteratorID);
    addChild(iterator_);    
    initSpacesList();
  }
  
  public UIPageIterator getUIPageIterator() { return iterator_;}
  
  private List<Space> getAllSpaces() throws Exception {
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
  
  @SuppressWarnings("unchecked")
  public List<Space> getSpaces() throws Exception {
    List<Space> listSpaces;
    int currentPage = iterator_.getCurrentPage();
    listSpaces = getAllSpaces();
    PageList pageList = new ObjectPageList(listSpaces,5);
    iterator_.setPageList(pageList);
    iterator_.setCurrentPage(currentPage);
    List<Space> lists;
    lists = iterator_.getCurrentPageData();
    return lists;
  }
  
  public void initSpacesList() throws Exception{
    /*List<Space> listSpaces;
    listSpaces = getAllSpaces();
    PageList pageList = new ObjectPageList(listSpaces,5);
    iterator_.setPageList(pageList);*/
  }
  
  public boolean isAllSpace() {
    return isAllSpace;
  }
  
  public int displayAction(String spaceId) throws SpaceException {
    // 0: request to join, 1: in pendingList, 2: manager, 3: member
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    Space space = spaceSrc.getSpaceById(spaceId);
    
    String userId = Util.getPortalRequestContext().getRemoteUser();
    if(spaceSrc.isMember(space, userId)) {
      if(spaceSrc.isLeader(space, userId)) return 2;
      return 3;
    } else if (spaceSrc.isPending(space, userId)) return 1;
    return 0;
  }
  
  public boolean isInInvitedList(Space space) {
    String userId = Util.getPortalRequestContext().getRemoteUser();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    
    if(spaceService.isInvited(space, userId)) return true;
    return false;
  }
  
  static public class ChangeListSpacesActionListener extends EventListener<UIManageSpaceWorkingArea> {
    public void execute(Event<UIManageSpaceWorkingArea> event) throws Exception {
      UIManageSpaceWorkingArea uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();      
      uiForm.isAllSpace = !uiForm.isAllSpace;
      uiForm.initSpacesList();
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
      SpaceService spaceService = uiForm.getApplicationComponent(SpaceService.class);
      Space space = spaceService.getSpaceById(spaceId);
      uiSpaceSetting.setValues(space);
      requestContext.addUIComponentToUpdateByAjax(uiPortlet);
    }
  }
  
  static public class LeaveSpaceActionListener extends EventListener<UIManageSpaceWorkingArea> {
    public void execute(Event<UIManageSpaceWorkingArea> event) throws Exception {
      UIManageSpaceWorkingArea uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();

      SpaceService spaceService = uiForm.getApplicationComponent(SpaceService.class);
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      String userID = requestContext.getRemoteUser();

      spaceService.leave(spaceId, userID);
      uiForm.initSpacesList();
      requestContext.addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static public class AcceptUserActionListener extends EventListener<UIManageSpaceWorkingArea> {
    public void execute(Event<UIManageSpaceWorkingArea> event) throws Exception {
      UIManageSpaceWorkingArea uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      
      SpaceService spaceService = uiForm.getApplicationComponent(SpaceService.class);

      String userName = requestContext.getRemoteUser();
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      
      try {
        spaceService.acceptInvitation(spaceId, userName);
      } catch (SpaceException e) {
        if(e.getCode() == SpaceException.Code.USER_NOT_INVITED) {
          uiApp.addMessage(new ApplicationMessage("UISpaceManage.msg.user-revoke", null));
          requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }

      // auto reload portal navigation
      UIPortal uiPortal = Util.getUIPortal();
      UserPortalConfigService dataService = uiForm.getApplicationComponent(UserPortalConfigService.class);
      UserPortalConfig portalConfig  = dataService.getUserPortalConfig(uiPortal.getName(), userName);
      uiPortal.setNavigation(portalConfig.getNavigations());

      UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
      PortalRequestContext prContext = Util.getPortalRequestContext();

      UIControlWorkspace uiControl = uiPortalApp.getChildById(UIPortalApplication.UI_CONTROL_WS_ID);
      if(uiControl != null) prContext.addUIComponentToUpdateByAjax(uiControl);

      UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      uiForm.initSpacesList();
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
      SpaceService spaceService = uiForm.getApplicationComponent(SpaceService.class);

      spaceService.denyInvitation(spaceId,  userName);

      //UIApplication uiApp = requestContext.getUIApplication();
      //uiApp.addMessage(new ApplicationMessage("UISpaceInvitation.msg.decline", null));
      //requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      uiForm.initSpacesList();
      requestContext.addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static public class RequestJoinActionListener extends EventListener<UIManageSpaceWorkingArea> {
    public void execute(Event<UIManageSpaceWorkingArea> event) throws Exception {
      UIManageSpaceWorkingArea workingAllSpaceArea = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();      
      SpaceService spaceService = workingAllSpaceArea.getApplicationComponent(SpaceService.class);

      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      String userName = requestContext.getRemoteUser();      
      spaceService.requestJoin(spaceId, userName);

      UIApplication uiApp = requestContext.getUIApplication();
      uiApp.addMessage(new ApplicationMessage("UIManageSpaceWorkingArea.msg.success-join-user", null,ApplicationMessage.INFO));
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      requestContext.addUIComponentToUpdateByAjax(workingAllSpaceArea);
    }
  }

}
