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
    template = "app:/groovy/portal/webui/component/UIManageYourSpace.gtmpl",
    events = {
        @EventConfig(listeners = UIManageYourSpace.ChangeListSpacesActionListener.class),
        @EventConfig(listeners = UIManageYourSpace.LeaveSpaceActionListener.class),
        @EventConfig(listeners = UIManageYourSpace.AcceptUserActionListener.class),
        @EventConfig(listeners = UIManageYourSpace.DenyUserActionListener.class)
    }
)
public class UIManageYourSpace extends UIContainer {

  private UIPageIterator iterator_;
  private UIPageIterator iteratorInvited_;
  private final String iteratorID = "UIIteratorSpaceWorking";
  private final String iteratorInvitedID = "UIIteratorInvitedSpaceWorking";
  private SpaceService spaceSrc = null;
  private String userName = null;
  
  public UIManageYourSpace() throws Exception {
    iterator_ = createUIComponent(UIPageIterator.class, null, iteratorID);
    addChild(iterator_);
    iteratorInvited_ = createUIComponent(UIPageIterator.class, null, iteratorInvitedID);
    addChild(iteratorInvited_);
  }
  
  private SpaceService getSpaceService() {
    if(spaceSrc == null)
      spaceSrc = getApplicationComponent(SpaceService.class);
    return spaceSrc;
  }
  
  private String getRemoteUser () {
    if(userName == null) 
      userName = Util.getPortalRequestContext().getRemoteUser();
    return userName;
  }
  
  public UIPageIterator getUIPageIterator() { return iterator_;}
  public UIPageIterator getUIPageInvitedIterator() { return iteratorInvited_;}
  
  private List<Space> getAllSpaces() throws Exception {
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    String userId = getRemoteUser();
    List<Space> allSpaces = spaceSrc.getAllSpaces();
    Iterator<Space> itr = allSpaces.iterator();
    while(itr.hasNext()) {
      Space space = itr.next();
      if(!(spaceSrc.isMember(space, userId) || spaceSrc.isPending(space, userId))){
        itr.remove();
      }
    }
    return allSpaces;
  }
  
  @SuppressWarnings("unchecked")
  public List<Space> getSpaces() throws Exception {
    int currentPage = iterator_.getCurrentPage();
    PageList pageList = new ObjectPageList(getAllSpaces(),5);
    iterator_.setPageList(pageList);    
    int pageCount = iterator_.getAvailablePage();
    if(pageCount >= currentPage){
      iterator_.setCurrentPage(currentPage);
    }else if(pageCount < currentPage){
      iterator_.setCurrentPage(currentPage-1);
    }
    return iterator_.getCurrentPageData();
  }
  
  @SuppressWarnings("unchecked")
  public List<Space> getInvitedSpaces() throws Exception {
    int currentPage = iteratorInvited_.getCurrentPage();
    SpaceService spaceService = getSpaceService();
    List<Space> allSpaces = spaceService.getAllSpaces();
    Iterator<Space> itr = allSpaces.iterator();
    String userId = getRemoteUser();
    while(itr.hasNext()) {
      Space space = itr.next();
      if(!spaceService.isInvited(space, userId)) itr.remove();
    }
    PageList pageList = new ObjectPageList(allSpaces,5);
    iteratorInvited_.setPageList(pageList);
    int pageCount = iteratorInvited_.getAvailablePage();
    if(pageCount >= currentPage) {
      iteratorInvited_.setCurrentPage(currentPage);
    } else if(pageCount < currentPage) {
      iteratorInvited_.setCurrentPage(currentPage-1);
    }
    return iteratorInvited_.getCurrentPageData();
  }
  
  public int displayAction(String spaceId) throws SpaceException {
    //1: in pendingList, 2: manager, 3: member
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    Space space = spaceSrc.getSpaceById(spaceId);
    
    String userId = getRemoteUser();
    if(spaceSrc.isMember(space, userId)) {
      if(spaceSrc.isLeader(space, userId)) return 2;
      return 3;
    }
    return 1;
  }
  
  public boolean isInInvitedList(Space space) {
    String userId = getRemoteUser();
    SpaceService spaceService = getSpaceService();
    
    if(spaceService.isInvited(space, userId)) return true;
    return false;
  }
  
  static public class ChangeListSpacesActionListener extends EventListener<UIManageYourSpace> {
    public void execute(Event<UIManageYourSpace> event) throws Exception {
      UIManageYourSpace uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIManageSpaceWorkingArea uiWorkingArea = uiForm.getAncestorOfType(UIManageSpaceWorkingArea.class);
      UIManageAllSpace uiManageAllSpace = uiWorkingArea.getChild(UIManageAllSpace.class);
      uiManageAllSpace.setRendered(true);
      uiForm.setRendered(false);
      requestContext.addUIComponentToUpdateByAjax(uiWorkingArea);
    }
  }
  
  static public class LeaveSpaceActionListener extends EventListener<UIManageYourSpace> {
    public void execute(Event<UIManageYourSpace> event) throws Exception {
      UIManageYourSpace uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();

      SpaceService spaceService = uiForm.getSpaceService();
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      String userID = uiForm.getRemoteUser();

      spaceService.leave(spaceId, userID);
      requestContext.addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static public class AcceptUserActionListener extends EventListener<UIManageYourSpace> {
    public void execute(Event<UIManageYourSpace> event) throws Exception {
      UIManageYourSpace uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      
      SpaceService spaceService = uiForm.getSpaceService();

      String userName = uiForm.getRemoteUser();
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      
      try {
        spaceService.acceptInvitation(spaceId, userName);
        uiApp.addMessage(new ApplicationMessage("UISpaceManage.msg.user-accept-invited", null));
        requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      } catch (SpaceException e) {
        if(e.getCode() == SpaceException.Code.USER_NOT_INVITED) {
          uiApp.addMessage(new ApplicationMessage("UISpaceManage.msg.user-revoke", null));
          requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          requestContext.addUIComponentToUpdateByAjax(uiForm);
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
  
  static public class DenyUserActionListener extends EventListener<UIManageYourSpace> {
    public void execute(Event<UIManageYourSpace> event) throws Exception {
      String spaceId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIManageYourSpace uiForm = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = uiForm.getRemoteUser();
      SpaceService spaceService = uiForm.getSpaceService();

      spaceService.denyInvitation(spaceId,  userName);

      requestContext.addUIComponentToUpdateByAjax(uiForm);
    }
  }

}
