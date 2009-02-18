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
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
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
    template = "app:/groovy/portal/webui/component/UIManageAllSpace.gtmpl",
    events = {
        @EventConfig(listeners = UIManageAllSpace.RequestJoinActionListener.class),
        @EventConfig(listeners = UIManageAllSpace.ChangeListSpacesActionListener.class),
        @EventConfig(listeners = UIManageAllSpace.LeaveSpaceActionListener.class),
        @EventConfig(listeners = UIManageAllSpace.AcceptUserActionListener.class),
        @EventConfig(listeners = UIManageAllSpace.DenyUserActionListener.class)
    }
)
public class UIManageAllSpace extends UIContainer {

  private UIPageIterator iterator_;
  private final String iteratorID = "UIIteratorSpaceWorking";
  
  public UIManageAllSpace() throws Exception {
    iterator_ = createUIComponent(UIPageIterator.class, null, iteratorID);
    addChild(iterator_);    
  }
  
  public UIPageIterator getUIPageIterator() { return iterator_;}
  
  private List<Space> getAllSpaces() throws Exception {
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    String userId = Util.getPortalRequestContext().getRemoteUser();
    return spaceSrc.getAllSpaces(userId);
  }
  
  @SuppressWarnings("unchecked")
  public List<Space> getSpaces() throws Exception {
    List<Space> listSpaces;
    int currentPage = iterator_.getCurrentPage();
    listSpaces = getAllSpaces();
    PageList pageList = new ObjectPageList(listSpaces,5);
    iterator_.setPageList(pageList);    
    int pageCount = iterator_.getAvailablePage();
    if(pageCount >= currentPage){
      iterator_.setCurrentPage(currentPage);
    }else if(pageCount < currentPage){
      iterator_.setCurrentPage(currentPage-1);
    }
    List<Space> lists;
    lists = iterator_.getCurrentPageData();
    return lists;
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
  
  static public class ChangeListSpacesActionListener extends EventListener<UIManageAllSpace> {
    public void execute(Event<UIManageAllSpace> event) throws Exception {
      UIManageAllSpace uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIManageSpaceWorkingArea uiWorkingArea = uiForm.getAncestorOfType(UIManageSpaceWorkingArea.class);
      UIManageYourSpace uiManageYourSpace = uiWorkingArea.getChild(UIManageYourSpace.class);
      uiForm.setRendered(false);
      uiManageYourSpace.setRendered(true);
      requestContext.addUIComponentToUpdateByAjax(uiWorkingArea);
    }
  }
  
  static public class LeaveSpaceActionListener extends EventListener<UIManageAllSpace> {
    public void execute(Event<UIManageAllSpace> event) throws Exception {
      UIManageAllSpace uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();

      SpaceService spaceService = uiForm.getApplicationComponent(SpaceService.class);
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      String userID = requestContext.getRemoteUser();

      spaceService.leave(spaceId, userID);
      requestContext.addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static public class AcceptUserActionListener extends EventListener<UIManageAllSpace> {
    public void execute(Event<UIManageAllSpace> event) throws Exception {
      UIManageAllSpace uiForm = event.getSource();
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
      prContext.addUIComponentToUpdateByAjax(uiWorkingWS) ;
      prContext.setFullRender(true);

      // go to space node manage
      PageNavigation portalNavigation = dataService.getPageNavigation(PortalConfig.PORTAL_TYPE, uiPortal.getName());
      PageNodeEvent<UIPortal> pnevent =
        new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, Integer.toString(portalNavigation.getId()) + "::spaces") ;
      uiPortal.broadcast(pnevent, Event.Phase.PROCESS) ;

    }
  }
  
  static public class DenyUserActionListener extends EventListener<UIManageAllSpace> {
    public void execute(Event<UIManageAllSpace> event) throws Exception {
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIManageAllSpace uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = requestContext.getRemoteUser();
      SpaceService spaceService = uiForm.getApplicationComponent(SpaceService.class);

      spaceService.denyInvitation(spaceId,  userName);

      requestContext.addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static public class RequestJoinActionListener extends EventListener<UIManageAllSpace> {
    public void execute(Event<UIManageAllSpace> event) throws Exception {
      UIManageAllSpace workingAllSpaceArea = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();      
      SpaceService spaceService = workingAllSpaceArea.getApplicationComponent(SpaceService.class);

      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      String userName = requestContext.getRemoteUser();      
      spaceService.requestJoin(spaceId, userName);

      UIApplication uiApp = requestContext.getUIApplication();
      uiApp.addMessage(new ApplicationMessage("UIManageAllSpace.msg.success-join-user", null,ApplicationMessage.INFO));
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      requestContext.addUIComponentToUpdateByAjax(workingAllSpaceArea);
    }
  }

}
