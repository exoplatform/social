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
import java.util.Iterator;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.navigation.UINavigationManagement;
import org.exoplatform.portal.webui.navigation.UINavigationNodeSelector;
import org.exoplatform.portal.webui.page.UIPageNodeForm2;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceAttachment;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceListAccess;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.social.webui.UISpaceSearch;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
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
      @EventConfig(listeners = UIManageMySpaces.SearchActionListener.class , phase = Phase.DECODE)
    }
  ),
  @ComponentConfig(  
    type = UIPageNodeForm2.class,
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",    
    events = {
      @EventConfig(listeners = UIPageNodeForm2.SaveActionListener.class),
      @EventConfig(listeners = UIManageMySpaces.BackActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm2.SwitchPublicationDateActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm2.ClearPageActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm2.CreatePageActionListener.class, phase = Phase.DECODE)
    }
  )
})
public class UIManageMySpaces extends UIContainer {
  static private final String MSG_WARNING_LEAVE_SPACE = "UIManageMySpaces.msg.warning_leave_space";
  static private final String MSG_ERROR_LEAVE_SPACE = "UIManageMySpaces.msg.error_leave_space";
  static private final String MSG_ERROR_DELETE_SPACE = "UIManageMySpaces.msg.error_delete_space";
  static private final Integer LEADER = 1, MEMBER = 2;
  
  private final String POPUP_ADD_SPACE = "UIPopupAddSpace";
  private UIPageIterator iterator;
  private final Integer SPACES_PER_PAGE = 4;
  private final String ITERATOR_ID = "UIIteratorMySpaces";
  private SpaceService spaceService = null;
  private String userId = null;
  private List<PageNavigation> navigations;
  private PageNavigation selectedNavigation;
  private List<Space> spaces_; // for search result
  private UISpaceSearch uiSpaceSearch = null;
  
  /**
   * Constructor for initialize UIPopupWindow for adding new space popup
   * @throws Exception
   */
  public UIManageMySpaces() throws Exception {
    uiSpaceSearch = createUIComponent(UISpaceSearch.class, null, "UISpaceSearch");
    addChild(uiSpaceSearch);
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
  public List<Space> getAllUserSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> userSpaces = spaceService.getAccessibleSpaces(userId);
    return SpaceUtils.getOrderedSpaces(userSpaces);
  }
  
  /**
   * Get selected navigation
   * @return
   */
  public PageNavigation getSelectedNavigation() {
    return selectedNavigation;
  }
  
  /**
   * Set selected navigation
   * @param navigation
   */
  public void setSelectedNavigation(PageNavigation navigation) {
    selectedNavigation = navigation;
  }
  
  /**
   * Get paginated spaces in which user is member or leader
   * 
   * @return paginated spaces list
   * @throws Exception
   */
  public List<Space> getUserSpaces() throws Exception {
    List<Space> listSpace = getMySpace();
    uiSpaceSearch.setSpaceNameForAutoSuggest(getAllMySpaceNames());
    return getDisplayMySpace(listSpace, iterator);
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
    if(spaceService.hasEditPermission(spaceId, userId)) {
      return LEADER;
    }
    return MEMBER;
  }
  
  /**
   * Check in case root has membership with current space.
   * 
   * @param spaceId
   * @return
   * @throws SpaceException
   */
  public boolean hasMembership(String spaceId) throws SpaceException {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    if(spaceService.isMember(spaceId, userId)) {
      return true;
    }
    
    return false;
    
  }
  
  public void setSpaces_(List<Space> spaces_) {
    this.spaces_ = spaces_;
  }
  public List<Space> getSpaces_() {
    return spaces_;
  }
  
  private List<Space> getMySpace() throws Exception {
    List<Space> spaceList = getSpaces_();
    List<Space> allUserSpace = getAllUserSpaces();
    List<Space> mySpaces = new ArrayList<Space>();
    if (allUserSpace.size() == 0) return allUserSpace;
    if(spaceList != null) {
      Iterator<Space> spaceItr = spaceList.iterator();
      while(spaceItr.hasNext()) {
        Space space = spaceItr.next();
        for(Space userSpace : allUserSpace) {
          if(space.getName().equalsIgnoreCase(userSpace.getName())){
            mySpaces.add(userSpace);
            break;
          }
        }
      }
    
      return mySpaces;
    }
    
    return allUserSpace;
  }
  
  @SuppressWarnings("unchecked")
  private List<Space> getDisplayMySpace(List<Space> spaces_, UIPageIterator pageIterator_) throws Exception {
    int currentPage = pageIterator_.getCurrentPage();
    LazyPageList<Space> pageList = new LazyPageList<Space>(new SpaceListAccess(spaces_), SPACES_PER_PAGE);
    pageIterator_.setPageList(pageList);
    int pageCount = pageIterator_.getAvailablePage();
    if (pageCount >= currentPage) {
      pageIterator_.setCurrentPage(currentPage);
    } else if (pageCount < currentPage) {
      pageIterator_.setCurrentPage(currentPage - 1);
    }
    return pageIterator_.getCurrentPageData();
  }

  public String getImageSource(Space space) throws Exception {
    SpaceAttachment spaceAtt = (SpaceAttachment) space.getSpaceAttachment();
    if (spaceAtt != null) {
      return "/" + getPortalName()+"/rest/jcr/" + getRepository()+ "/" + spaceAtt.getWorkspace()
              + spaceAtt.getDataPath() + "/?rnd=" + System.currentTimeMillis();
    }
    return null;
  }
  
  private List<String> getAllMySpaceNames() throws Exception {
    List<Space> allSpaces = getAllUserSpaces();
    List<String> allSpacesNames = new ArrayList<String>();
    for (Space space : allSpaces){
      allSpacesNames.add(space.getName());
    }
    
    return allSpacesNames;
  } 
  
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
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
      // Currently, not used yet
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
      uiMySpaces.setSelectedNavigation(groupNav);
      UIPopupWindow uiPopup = uiMySpaces.getChild(UIPopupWindow.class);
      UINavigationManagement pageManager = uiPopup.createUIComponent(UINavigationManagement.class,
                                                                   null,
                                                                   null,
                                                                   uiPopup);
      pageManager.setOwner(groupNav.getOwnerId());
      pageManager.setOwnerType(groupNav.getOwnerType());
      
      UINavigationNodeSelector selector = pageManager.getChild(UINavigationNodeSelector.class);
      ArrayList<PageNavigation> list = new ArrayList<PageNavigation>();
      list.add(groupNav);
      selector.initNavigations(list);
      uiPopup.setUIComponent(pageManager);
      uiPopup.setWindowSize(400, 400);
      uiPopup.setShow(true);
      ctx.addUIComponentToUpdateByAjax(uiMySpaces);
    }
    
  }
  
  /**
   * This action trigger when user click on back button from UINavigationManagement
   * @author hoatle
   *
   */
  static public class BackActionListener extends EventListener<UIPageNodeForm2> {

    @Override
    public void execute(Event<UIPageNodeForm2> event) throws Exception {
      UIPageNodeForm2 uiPageNode = event.getSource();
      UIManageMySpaces uiMySpaces = uiPageNode.getAncestorOfType(UIManageMySpaces.class);
      PageNavigation selectedNavigation = uiMySpaces.getSelectedNavigation();
      UIPopupWindow uiPopup = uiMySpaces.getChild(UIPopupWindow.class);
      UINavigationManagement pageManager = uiMySpaces.createUIComponent(UINavigationManagement.class, null, null);
      pageManager.setOwner(selectedNavigation.getOwnerId());
      UINavigationNodeSelector selector = pageManager.getChild(UINavigationNodeSelector.class);
      ArrayList<PageNavigation> list = new ArrayList<PageNavigation>();
      list.add(selectedNavigation);
      selector.initNavigations(list);
      uiPopup.setUIComponent(pageManager);
      uiPopup.setWindowSize(400, 400);
      uiPopup.setRendered(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
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
      UIManageMySpaces uiMySpaces = event.getSource();
      SpaceService spaceService = uiMySpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      try {
        spaceService.deleteSpace(spaceId);
      } catch(SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_DELETE_SPACE, null, ApplicationMessage.ERROR));
      }
      SpaceUtils.updateWorkingWorkSpace();
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
      SpaceUtils.updateWorkingWorkSpace();
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManageMySpaces);
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
    
  }
  
  public static class SearchActionListener extends EventListener<UIManageMySpaces> {
    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiForm = event.getSource();
      UISpaceSearch uiSpaceSearch = uiForm.getChild(UISpaceSearch.class);
      List<Space> spaceList = uiSpaceSearch.getSpaceList();
      uiForm.setSpaces_(spaceList);
    }
  }
}
