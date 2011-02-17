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
package org.exoplatform.social.webui.space;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.navigation.UINavigationManagement;
import org.exoplatform.portal.webui.navigation.UINavigationNodeSelector;
import org.exoplatform.portal.webui.page.UIPageNodeForm;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceListAccess;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

/**
 * UIManageMySpaces.java <br />
 * Manage all user's spaces, user can edit, delete, leave space.
 * User can create new space here. <br />
 *
 * Created by The eXo Platform SAS
 * @author hoatle <hoatlevan at gmail dot com>
 * @since Jun 29, 2009
 */
@ComponentConfig(
  template="classpath:groovy/social/webui/space/UIManageMySpaces.gtmpl",
  events = {
    @EventConfig(listeners = UIManageMySpaces.EditSpaceActionListener.class),
    @EventConfig(listeners = UIManageMySpaces.DeleteSpaceActionListener.class, confirm = "UIManageMySpaces.msg.confirm_space_delete"),
    @EventConfig(listeners = UIManageMySpaces.LeaveSpaceActionListener.class),
    @EventConfig(listeners = UIManageMySpaces.SearchActionListener.class , phase = Phase.DECODE)
  }
)
public class UIManageMySpaces extends UIContainer {
  static private final String MSG_WARNING_LEAVE_SPACE = "UIManageMySpaces.msg.warning_leave_space";
  static private final String MSG_ERROR_LEAVE_SPACE = "UIManageMySpaces.msg.error_leave_space";
  static private final String MSG_ERROR_DELETE_SPACE = "UIManageMySpaces.msg.error_delete_space";
  static private final Integer LEADER = 1, MEMBER = 2;
  private static final String SPACE_DELETED_INFO = "UIManageMySpaces.msg.DeletedInfo";
  private static final String MEMBERSHIP_REMOVED_INFO = "UIManageMySpaces.msg.MemberShipRemovedInfo";
  private static final String NAVIGATION_REMOVED_INFO = "UIManageMySpaces.msg.NavigationRemovedInfo";
  private static final String CONFIRMED_STATUS = "confirmed";
  
  /** The first page. */
  private static final int FIRST_PAGE = 1;


  private UIPageIterator iterator;
  private final Integer SPACES_PER_PAGE = 4;
  private final String ITERATOR_ID = "UIIteratorMySpaces";
  private SpaceService spaceService = null;
  private String userId = null;
  private List<PageNavigation> navigations;
  private PageNavigation selectedNavigation;
  private List<Space> spaces; // for search result
  private UISpaceSearch uiSpaceSearch = null;
  
  /**
   * Constructor for initialize UIPopupWindow for adding new space popup.
   * 
   * @throws Exception
   */
  public UIManageMySpaces() throws Exception {
    uiSpaceSearch = createUIComponent(UISpaceSearch.class, null, "UISpaceSearch");
    uiSpaceSearch.setTypeOfRelation(CONFIRMED_STATUS);
    addChild(uiSpaceSearch);
    iterator = addChild(UIPageIterator.class, null, ITERATOR_ID);
  }

  /**
   * Gets uiPageIterator.
   * 
   * @return uiPageIterator
   */
  public UIPageIterator getMySpacesUIPageIterator() {
    return iterator;
  }

  /**
   * Gets all user's spaces.
   * 
   * @return user spaces
   * @throws Exception
   */
  public List<Space> getAllUserSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> userSpaces = spaceService.getAccessibleSpaces(userId);
    //reload navigation BUG #SOC-555
    SpaceUtils.reloadNavigation();
    return SpaceUtils.getOrderedSpaces(userSpaces);
  }

  /**
   * Gets selected navigation.
   * 
   * @return page navigation
   */
  public PageNavigation getSelectedNavigation() {
    return selectedNavigation;
  }

  /**
   * Sets selected navigation.
   * 
   * @param navigation
   */
  public void setSelectedNavigation(PageNavigation navigation) {
    selectedNavigation = navigation;
  }

  /**
   * Gets paginated spaces in which user is member or leader.
   * 
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
   * Gets role of the user in a specific space for displaying in template.
   *
   * @param spaceId
   * @return UIManageMySpaces.LEADER if the remote user is the space's leader <br />
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
   * Checks in case root has membership with current space.
   *
   * @param spaceId
   * @return true or false
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

  /**
   * Sets space list.
   * 
   * @param spaces
   */
  public void setSpaces(List<Space> spaces) {
    this.spaces = spaces;
  }
  /**
   * Gets space list.
   * 
   * @return space list
   */
  public List<Space> getSpaces() {
    return spaces;
  }

  /**
   * Gets image source url.
   * 
   * @param space
   * @return image source url
   * @throws Exception
   */
  public String getImageSource(Space space) throws Exception {
    return space.getAvatarUrl();
  }


  /**
   * This action is triggered when user click on EditSpace
   * Currently, when user click on EditSpace, they will be redirected to /xxx/SpaceSettingPortlet
   * When user click on editSpace, the user is redirected to SpaceSettingPortlet.
   *
   */
  static public class EditSpaceActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiMySpaces = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      SpaceService spaceService = uiMySpaces.getSpaceService();
      Space space = spaceService.getSpaceById(ctx.getRequestParameter(OBJECTID));
      if (space == null) {
        uiApp.addMessage(new ApplicationMessage("UIManageMySpaces.msg.warning_space_not_available", null, ApplicationMessage.WARNING));
      }
      OrganizationService organizationService = SpaceUtils.getOrganizationService();
      Group group = organizationService.getGroupHandler().findGroupById(space.getGroupId());
      if (group == null) {
        uiApp.addMessage(new ApplicationMessage("UIManageMySpaces.msg.group_unable_to_retrieve", null, ApplicationMessage.ERROR));
        return;
      } else {
        String spaceUrl = Util.getPortalRequestContext().getPortalURI();
        String spaceSettingUri = uiMySpaces.getNodeUri(space, "SpaceSettingPortlet");
        String spaceSettingUrl = spaceUrl + spaceSettingUri;
        PortalRequestContext prContext = Util.getPortalRequestContext();
        prContext.setResponseComplete(true);
        prContext.getResponse().sendRedirect(spaceSettingUrl);
      }
    }
  }

  /**
   * This action trigger when user click on back button from UINavigationManagement.
   * 
   * @author hoatle
   *
   */
  static public class BackActionListener extends EventListener<UIPageNodeForm> {

    @Override
    public void execute(Event<UIPageNodeForm> event) throws Exception {
      UIPageNodeForm uiPageNode = event.getSource();
      PageNavigation contextNavigation = uiPageNode.getContextPageNavigation();
      UIManageMySpaces uiMySpaces = uiPageNode.getAncestorOfType(UIManageMySpaces.class);
      UIPopupWindow uiPopup = uiMySpaces.getChild(UIPopupWindow.class);
      UINavigationManagement navigationManager = uiMySpaces.createUIComponent(UINavigationManagement.class, null, null);
      navigationManager.setOwner(contextNavigation.getOwnerId());
      navigationManager.setOwnerType(contextNavigation.getOwnerType());
      UINavigationNodeSelector selector = navigationManager.getChild(UINavigationNodeSelector.class);
      selector.setEdittedNavigation(contextNavigation);
      selector.initTreeData();
      uiPopup.setUIComponent(navigationManager);
      uiPopup.setWindowSize(400, 400);
      uiPopup.setRendered(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMySpaces);
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
      Space space = spaceService.getSpaceById(spaceId);
      String userId = uiMySpaces.getUserId();

      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      if (!spaceService.isMember(space, userId) && !spaceService.hasEditPermission(space, userId)) {
        uiApp.addMessage(new ApplicationMessage(MEMBERSHIP_REMOVED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      try {
        spaceService.deleteSpace(spaceId);
      } catch(SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_DELETE_SPACE, null, ApplicationMessage.ERROR));
      }
      SpaceUtils.updateWorkingWorkSpace();
    }

  }

  /**
   * This action is triggered when user click on LeaveSpace <br />
   * The leaving space will remove that user in the space. <br />
   * If that user is the only leader -> can't not leave that space <br />
   */
  static public class LeaveSpaceActionListener extends EventListener<UIManageMySpaces> {
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiMySpaces = event.getSource();
      SpaceService spaceService = uiMySpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = uiMySpaces.getUserId();
      Space space = spaceService.getSpaceById(spaceId);

      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      if (!spaceService.isMember(space, userId) && !spaceService.hasEditPermission(space, userId)) {
        uiApp.addMessage(new ApplicationMessage(MEMBERSHIP_REMOVED_INFO, null, ApplicationMessage.INFO));
        return;
      }

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
   * Triggers this action when user clicks on the search button.
   * 
   * @author hoatle
   *
   */
  public static class SearchActionListener extends EventListener<UIManageMySpaces> {
    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiForm = event.getSource();
      UISpaceSearch uiSpaceSearch = uiForm.getChild(UISpaceSearch.class);
      List<Space> spaceList = uiSpaceSearch.getSpaceList();
      uiForm.setSpaces(spaceList);
    }
  }

  /**
   * Gets spaceService.
   * 
   * @return spaceService
   * @see SpaceService
   */
  private SpaceService getSpaceService() {
    if(spaceService == null)
      spaceService = getApplicationComponent(SpaceService.class);
    return spaceService;
  }

  /**
   * Gets remote user Id.
   * 
   * @return remote userId
   */
  private String getUserId() {
    if(userId == null)
      userId = Util.getPortalRequestContext().getRemoteUser();
    return userId;
  }
  /**
   * Loads navigations.
   * 
   * @throws Exception
   */
  private void loadNavigations() throws Exception {
  /*navigations = new ArrayList<PageNavigation>();
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
    }*/
    UserPortalConfigService userPortalConfigService = getApplicationComponent(UserPortalConfigService.class);
    navigations = userPortalConfigService.loadEditableNavigations();
  }

  /**
   * Gets my space list.
   * 
   * @return my space list
   * @throws Exception
   */
  private List<Space> getMySpace() throws Exception {
    List<Space> spaceList = getSpaces();
    List<Space> allUserSpace = getAllUserSpaces();
    List<Space> mySpaces = new ArrayList<Space>();
    if (allUserSpace.size() == 0) return allUserSpace;
    if(spaceList != null) {
      Iterator<Space> spaceItr = spaceList.iterator();
      while(spaceItr.hasNext()) {
        Space space = spaceItr.next();
        for(Space userSpace : allUserSpace) {
          if(space.getDisplayName().equalsIgnoreCase(userSpace.getDisplayName())){
            mySpaces.add(userSpace);
            break;
          }
        }
      }

      return mySpaces;
    }

    return allUserSpace;
  }

  /**
   * Gets display my space list.
   * 
   * @param spaces_
   * @param pageIterator_
   * @return display my space list
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private List<Space> getDisplayMySpace(List<Space> spaces_, UIPageIterator pageIterator_) throws Exception {
    int currentPage = pageIterator_.getCurrentPage();
    LazyPageList<Space> pageList = new LazyPageList<Space>(new SpaceListAccess(spaces_), SPACES_PER_PAGE);
    pageIterator_.setPageList(pageList);
    if (this.uiSpaceSearch.isNewSearch()) {
      pageIterator_.setCurrentPage(FIRST_PAGE);
    } else {
      pageIterator_.setCurrentPage(currentPage);
    }
    this.uiSpaceSearch.setNewSearch(false);
    return pageIterator_.getCurrentPageData();
  }

  /**
   * Gets all my space names.
   * 
   * @return my space names
   * @throws Exception
   */
  private List<String> getAllMySpaceNames() throws Exception {
    List<Space> allSpaces = getAllUserSpaces();
    List<String> allSpacesNames = new ArrayList<String>();
    for (Space space : allSpaces){
      allSpacesNames.add(space.getDisplayName());
    }

    return allSpacesNames;
  }

   /**
    * Gets node's name base on application name.
    * 
    * @param space
    * @param appId
    * @throws SpaceException
    */
   private String getNodeUri(Space space, String appId) throws SpaceException {
	 ExoContainer container = ExoContainerContext.getCurrentContainer() ;
	 DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
     try {
       String groupId = space.getGroupId();
       PageNavigation nav = dataStorage.getPageNavigation(PortalConfig.GROUP_TYPE, groupId);
       // return in case group navigation was removed by portal SOC-548
       if (nav == null) return null;
       PageNode homeNode = SpaceUtils.getHomeNode(nav, space.getUrl());
       if (homeNode == null) {
         throw new Exception("homeNode is null!");
       }
       String nodeName = SpaceUtils.getAppNodeName(space, appId);
       PageNode childNode = homeNode.getChild(nodeName);
       //bug from portal, gets by nodeUri instead
       if (childNode != null) {
         return childNode.getUri();
       } else {
         List<PageNode> pageNodes = homeNode.getChildren();
         for (PageNode pageNode : pageNodes) {
           String pageReference = pageNode.getPageReference();
           if (pageReference.contains(nodeName)) {
             return pageNode.getUri();              
           }
         }
       }
       return null;
     } catch (Exception e) {
       throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_APPLICATION, e);
     }
   }
}

