/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.navigation.UINavigationManagement;
import org.exoplatform.portal.webui.navigation.UINavigationNodeSelector;
import org.exoplatform.portal.webui.page.UIPageNodeForm2;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceListAccess;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIRepeater;
import org.exoplatform.webui.core.UIVirtualList;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * UIManageMySpaces
 * Manage all user's spaces, user can edit, delete, leave space.
 * User can create new space here.
 * 
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 * Jun 29, 2009  
 */
@ComponentConfigs({
  @ComponentConfig(
    template="app:/groovy/portal/webui/component/UIManageMySpaces.gtmpl",
    events = {
      @EventConfig(listeners = UIManageMySpaces.EditSpaceActionListener.class), 
      @EventConfig(listeners = UIManageMySpaces.EditSpaceNavigationActionListener.class),
      @EventConfig(listeners = UIManageMySpaces.DeleteSpaceActionListener.class, confirm = "UIManageMySpace.msg.confirm_space_delete"),
      @EventConfig(listeners = UIManageMySpaces.LeaveSpaceActionListener.class),
      @EventConfig(listeners = UIManageMySpaces.AddSpaceActionListener.class),
      @EventConfig(listeners = UIManageMySpaces.AcceptActionListener.class),
      @EventConfig(listeners = UIManageMySpaces.DenyActionListener.class)
    }
  ),
  @ComponentConfig(  
    type = UIPageNodeForm2.class,
    lifecycle = UIFormLifecycle.class,
      template = "system:/groovy/webui/form/UIFormTabPane.gtmpl" ,    
      events = {
        @EventConfig(listeners = UIPageNodeForm2.SaveActionListener.class ),
        @EventConfig(listeners = UIPageNodeForm2.BackActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIPageNodeForm2.SwitchPublicationDateActionListener.class, phase = Phase.DECODE ),
        @EventConfig(listeners = UIPageNodeForm2.ClearPageActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIPageNodeForm2.CreatePageActionListener.class, phase = Phase.DECODE)
      }
  ),
  @ComponentConfig(
    id = "UIInvitedSpaces",
    type = UIRepeater.class,
    template = "app:/groovy/portal/webui/component/UIInvitedSpaces.gtmpl"
  )
})
public class UIManageMySpaces extends UIContainer {
  //Message Bundle
  static private final String MSG_WARNING_LEAVE_SPACE = "UIManageMySpaces.msg.warning_leave_space";
  static private final String MSG_ERROR_LEAVE_SPACE = "UIManageMySpaces.msg.error_leave_space";
  static private final String MSG_LEAVE_SPACE_SUCCESS = "UIManageMySpaces.msg.leave_space_success";
  
  static private final String MSG_ERROR_ACCEPT_INVITATION = "UIManageMySpaces.msg.error_accept_invitation";
  static private final String MSG_ACCEPT_INVITATION_SUCCESS = "UIManageMySpaces.msg.accept_invitation_success";
  
  static private final String MSG_ERROR_DENY_INVITATION = "UIManageMySpaces.msg.error_deny_invitation";
  static private final String MSG_DENY_INVITATION_SUCCESS = "UIManageMySpaces.msg.deny_invitation_success";
  
  static public final Integer LEADER = 1, MEMBER = 2;
  
  
  private final String POPUP_ADD_SPACE = "UIPopupAddSpace";
  private final Integer INVITED_SPACES_PER_PAGE = 4;
  private UIPageIterator iterator;
  private final Integer SPACES_PER_PAGE = 4;
  private final String ITERATOR_ID = "UIIteratorMySpaces";
  private SpaceService spaceService = null;
  private String userId = null;
  private List<PageNavigation> navigations;
  
  private UIVirtualList uiVirtualList;
  /**
   * Constructor for initialize UIPopupWindow for adding new space popup
   * @throws Exception
   */
  public UIManageMySpaces() throws Exception {
    uiVirtualList = addChild(UIVirtualList.class, null, null);
    UIRepeater uiInvitedSpaces = createUIComponent(UIRepeater.class, "UIInvitedSpaces", "UIInvitedSpaces");
    uiVirtualList.setPageSize(INVITED_SPACES_PER_PAGE);
    uiVirtualList.setUIComponent(uiInvitedSpaces);
    iterator = addChild(UIPageIterator.class, null, ITERATOR_ID);
    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, null, POPUP_ADD_SPACE);
    uiPopup.setShow(false);
    uiPopup.setWindowSize(400, 0);
    addChild(uiPopup);
  }
  /**
   * Get UIPageIterator
   * @return
   */
  public UIPageIterator getMySpacesUIPageIterator() {
    return iterator;
  }
  
  /**
   * Load InvitedSpaces
   * @return
   * @throws Exception 
   */
  public void loadInvitedSpaces() throws Exception {
    uiVirtualList.dataBind(getInvitedPageList());
  }
  
  /**
   * Get SpaceService
   * @return spaceService
   */
  private SpaceService getSpaceService() {
    if(spaceService == null)
      spaceService = getApplicationComponent(SpaceService.class);
    return spaceService;
  }
  
  /**
   * Get remote user Id
   * @return userId
   */
  private String getUserId() {
    if(userId == null) 
      userId = Util.getPortalRequestContext().getRemoteUser();
    return userId;
  }
  /**
   * Load Navigations
   * @throws Exception
   */
  private void loadNavigations() throws Exception {
    navigations = new ArrayList<PageNavigation>();
    UserACL userACL = getApplicationComponent(UserACL.class);
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    // load all navigation that user has edit permission
    Query<PageNavigation> query = new Query<PageNavigation>(PortalConfig.GROUP_TYPE,
                                                            null,
                                                            PageNavigation.class);
    List<PageNavigation> navis = dataStorage.find(query, new Comparator<PageNavigation>(){
      public int compare(PageNavigation pconfig1, PageNavigation pconfig2) {
        return pconfig1.getOwnerId().compareTo(pconfig2.getOwnerId());
      }
    }).getAll();
    for (PageNavigation ele : navis) {
      if (userACL.hasEditPermission(ele)) {
        navigations.add(ele);
      }
    }
  }
  
  /**
   * Get all user's spaces
   * @return
   * @throws Exception
   */
  private List<Space> getAllUserSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> userSpaces = spaceService.getSpaces(userId);
    return SpaceUtils.getOrderedSpaces(userSpaces);
  }
  /**
   * Get paginated spaces in which user is member or leader
   * 
   * @return paginated spaces list
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<Space> getUserSpaces() throws Exception {
    int currentPage = iterator.getCurrentPage();
    List<Space> spaceList = getAllUserSpaces();
    LazyPageList<Space> pageList = new LazyPageList<Space>(new SpaceListAccess(spaceList), SPACES_PER_PAGE);
    iterator.setPageList(pageList);
    int pageCount = iterator.getAvailablePage();
    if (pageCount >= currentPage) {
      iterator.setCurrentPage(currentPage);
    } else if (pageCount < currentPage) {
      iterator.setCurrentPage(currentPage - 1);
    }
    return iterator.getCurrentPageData();
  }
  
  private PageList<Space> getInvitedPageList() throws Exception {
    List<Space> invitedList = getAllInvitedSpaces();
    //return (PageList<Space>) new ObjectPageList<Space>(invitedList, invitedList.size());
    Integer invitedListSize = 0;
    if (invitedList.size() == 0) {
      invitedListSize = 1;
    } else {
      invitedListSize = invitedList.size();
    }
    return new LazyPageList<Space>(new SpaceListAccess(invitedList), invitedListSize); 
  }
  
  /**
   * Get all spaces which user is invited to join
   * @return
   * @throws Exception
   */
  public List<Space> getAllInvitedSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> invitedSpaces = spaceService.getInvitedSpaces(userId);
    return SpaceUtils.getOrderedSpaces(invitedSpaces);
  }
  
  /**
   * Get role of the user in a specific space for displaying in template
   * 
   * @param spaceId
   * @return UIManageMySpaces.LEADER if the remote user is the space's leader
   *         UIManageMySpaces.MEMBER if the remote user is the space's member
   * @throws SpaceException 
   */
  public int getRole(String spaceId) throws SpaceException {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    if(spaceService.isLeader(spaceId, userId)) {
      return LEADER;
    }
    return MEMBER;
  }
  
  /**
   * This action is triggered when user click on EditSpace
   * Currently, when user click on EditSpace, they will be redirected to /xxx/SpaceSettingPortlet
   * When user click on editSpace, the user is redirected to SpaceSettingPortlet
   *
   */
  static public class EditSpaceActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO hoatle EditSpaceActionListener
    }
  }
  
  /**
   * This action is triggered when user click on EditSpaceNavigation
   * 
   * A Navigation popup for user to edit space navigation.
   *
   */
  static public class EditSpaceNavigationActionListener extends EventListener<UIManageMySpaces> {
    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiMySpaces = event.getSource();
      uiMySpaces.loadNavigations();
      SpaceService spaceService = uiMySpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      Space space = spaceService.getSpaceById(ctx.getRequestParameter(OBJECTID));
      PageNavigation groupNav = SpaceUtils.getGroupNavigation(space.getGroupId());
      UIPopupWindow uiPopup = uiMySpaces.getChild(UIPopupWindow.class);
      UINavigationManagement pageManager = uiPopup.createUIComponent(UINavigationManagement.class,
                                                                   null,
                                                                   null,
                                                                   uiPopup);
      pageManager.setOwner(groupNav.getOwnerId());
      pageManager.setOwnerType(groupNav.getOwnerType());
      
      UINavigationNodeSelector selector = pageManager.getChild(UINavigationNodeSelector.class);
      selector.loadNavigationByNavId(groupNav.getId(), uiMySpaces.navigations);
      uiPopup.setUIComponent(pageManager);
      //uiPopup.setWindowSize(400, 0);
      uiPopup.setShow(true);
    }
    
  }
  
  /**
   * This action is triggered when user click on DeleteSpace
   * a prompt popup is display for confirmation, if yes delete that space; otherwise, do nothing.
   *
   */
  static public class DeleteSpaceActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO hoatle DeleteSpaceActionListener
    }
    
  }
  
  /**
   * This action is triggered when user click on LeaveSpace
   * The leaving space will remove that user in the space.
   * If that user is the only leader -> can't not leave that space
   */
  static public class LeaveSpaceActionListener extends EventListener<UIManageMySpaces> {
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiMySpaces = event.getSource();
      SpaceService spaceService = uiMySpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = uiMySpaces.getUserId();
      if (spaceService.isOnlyLeader(spaceId, userId)) {
        uiApp.addMessage(new ApplicationMessage(MSG_WARNING_LEAVE_SPACE, null, ApplicationMessage.WARNING));
        return;
      }
      try {
        spaceService.removeMember(spaceId, userId);
      } catch(SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_LEAVE_SPACE, null, ApplicationMessage.ERROR));
        return;
      }
      uiApp.addMessage(new ApplicationMessage(MSG_LEAVE_SPACE_SUCCESS, null, ApplicationMessage.INFO));
    }
  }
  
  /**
   * This action is triggered when user clicks on AddSpace
   * 
   * UIAddSpaceForm will be displayed in a popup window
   */
  static public class AddSpaceActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiManageMySpaces = event.getSource();
      UIPopupWindow uiPopup = uiManageMySpaces.getChild(UIPopupWindow.class);
      UISpaceAddForm uiAddSpaceForm = uiManageMySpaces.createUIComponent(UISpaceAddForm.class,
                                                                         null,
                                                                         null);
      uiPopup.setUIComponent(uiAddSpaceForm);
      uiPopup.setWindowSize(500, 0);
      uiPopup.setShow(true);
    }
    
  }
  
  /**
   * This action is triggered when user clicks on Accept Space Invitation
   * When accepting, that user will be the member of the space
   */
  static public class AcceptActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiMySpaces = event.getSource();
      SpaceService spaceService = uiMySpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = uiMySpaces.getUserId();
      try {
        spaceService.acceptInvitation(spaceId, userId);
      } catch(SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_ACCEPT_INVITATION, null, ApplicationMessage.ERROR));
        return;
      }
      uiApp.addMessage(new ApplicationMessage(MSG_ACCEPT_INVITATION_SUCCESS, null, ApplicationMessage.INFO));
    }
  }
  
  /**
   * This action is triggered when user clicks on Deny Space Invitation
   * When denying, that space will remove the user from pending
   */
  static public class DenyActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiMySpaces = event.getSource();
      SpaceService spaceService = uiMySpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = uiMySpaces.getUserId();
      try {
        spaceService.denyInvitation(spaceId, userId);
      } catch(SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_DENY_INVITATION, null, ApplicationMessage.ERROR));
      }
      uiApp.addMessage(new ApplicationMessage(MSG_DENY_INVITATION_SUCCESS, null, ApplicationMessage.INFO));    
    }    
  }
}
