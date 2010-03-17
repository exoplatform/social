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
package org.exoplatform.social.space;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.gatein.common.i18n.LocalizedString;
import org.gatein.common.util.Tools;
import org.gatein.mop.api.content.ContentType;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.PortletInfo;

import com.ibm.icu.text.Transliterator;

/**
 * SpaceUtils
 * Utility for working with space
 */
public class SpaceUtils {
  static public final Log logger = ExoLogger.getLogger(SpaceUtils.class);
  static public final String SPACE_GROUP = "/spaces";

  static public final String  MEMBER      = "member";

  static public final String  MANAGER     = "manager";
  static private ExoContainer exoContainer;
  static private SpaceService spaceService;
  static private List<Application> appList = new ArrayList<Application>();
  static private UserPortalConfigService userPortalConfigService;
  static private ApplicationRegistryService appService = null;
  
  private static final String REMOTE_CATEGORY_NAME = "remote";
  /**
   * Create a new group from an existing group.
   * This new group will get all data from existing group except for group name
   * @param parentGroup
   * @param existingGroup
   * @param name
   * @return new Group
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  static public Group createGroupFromExistingGroup(Group parentGroup, Group existingGroup, String name) throws Exception {
    OrganizationService orgSrc = getOrganizationService();
    GroupHandler groupHandler = orgSrc.getGroupHandler();
    MembershipHandler memberShipHandler = orgSrc.getMembershipHandler();
    Group newGroup = groupHandler.createGroupInstance();
    newGroup.setGroupName(name);
    newGroup.setLabel(name);
    newGroup.setDescription(existingGroup.getDescription());
    groupHandler.addChild(parentGroup, newGroup, true);
    Collection<Membership> memberShips = memberShipHandler.findMembershipsByGroup(existingGroup);
    Iterator<Membership> itr = memberShips.iterator();
    while (itr.hasNext()) {
      Membership membership = itr.next();
      User user = orgSrc.getUserHandler().findUserByName(membership.getUserName());
      MembershipType memberShipType = orgSrc.getMembershipTypeHandler()
                                            .findMembershipType(membership.getMembershipType());
      memberShipHandler.linkMembership(user, newGroup, memberShipType, true);
    }
    return newGroup;
  }
  
  /**
   * Get applications that a group have right to access
   * @param groupId
   * @return  applications
   * @throws Exception
   * @Deprecated
   */
  static public List<Application> getApplications(String groupId) throws Exception {

    List<Application> list = new CopyOnWriteArrayList<Application>();
    ApplicationRegistryService appRegistrySrc = getApplicationRegistryService();

    List<ApplicationCategory> listCategory = appRegistrySrc.getApplicationCategories();
    Iterator<ApplicationCategory> cateItr = listCategory.iterator();
    while (cateItr.hasNext()) {
      ApplicationCategory cate = cateItr.next();
      if (!hasAccessPermission(cate, groupId)) {
        cateItr.remove();
        continue;
      }
      ApplicationType<org.exoplatform.portal.pom.spi.portlet.Portlet> portletType = ApplicationType.PORTLET;
      List<Application> applications = appRegistrySrc.getApplications(cate, portletType);
      Iterator<Application> appIterator = applications.iterator();
      while (appIterator.hasNext()) {
        Application app = appIterator.next();
        if (!hasAccessPermission(app, groupId))
          appIterator.remove();
        else
          list.add(app);
      }
    }
    return list;
  }
  
  /**
   * gets appStore of HashMap type with key = ApplicationCategory and value = list of applications
   * @param space 
   * @return appStore
   * @throws Exception
   */
  static public Map<ApplicationCategory, List<Application>> getAppStore(Space space) throws Exception {
    Map<ApplicationCategory, List<Application>> appStore = new HashMap<ApplicationCategory, List<Application>>();
    
    ApplicationRegistryService appRegistryService = getApplicationRegistryService();
    
    String remoteUser = Util.getPortalRequestContext().getRemoteUser();
    if (remoteUser == null || remoteUser.equals(""))
       return appStore;
    List<ApplicationCategory> categoryList = appRegistryService.getApplicationCategories(remoteUser);
    Collections.sort(categoryList, new PortletCategoryComparator());
    Iterator<ApplicationCategory> cateItr = categoryList.iterator();
    while(cateItr.hasNext()) {
      ApplicationCategory appCategory = cateItr.next();
      List<Application> appList = appRegistryService.getApplications(appCategory);
      Collections.sort(appList, new PortletComparator());
      if (appList.size() > 0) {
        appStore.put(appCategory, appList);
      }
    }
    return appStore;
  }
  
  /**
   * Get application from portal container.
   * 
   * @param appId
   * @return An application has name match input appId.
   * @throws Exception
   */
  static public Application getAppFromPortalContainer(String appId) throws Exception {
    if (exoContainer == null) {
      exoContainer = ExoContainerContext.getCurrentContainer();
    }
    
    PortletInvoker portletInvoker = (PortletInvoker)exoContainer.getComponentInstance(PortletInvoker.class);
    Set<Portlet> portlets = portletInvoker.getPortlets();
    
    ApplicationRegistryService appRegistryService = getApplicationRegistryService();
    String remoteUser = Util.getPortalRequestContext().getRemoteUser();
    if (remoteUser == null || remoteUser.equals("")) return null;
    
    for (Portlet portlet : portlets) {
       PortletInfo info = portlet.getInfo();
       String portletApplicationName = info.getApplicationName();
       String portletName = info.getName();

       portletApplicationName = portletApplicationName.replace('/', '_');
       portletName = portletName.replace('/', '_');
       
       if (portletName.equals(appId)) {
         LocalizedString keywordsLS = info.getMeta().getMetaValue(MetaInfo.KEYWORDS);
  
         String[] categoryNames = null;
         if (keywordsLS != null) {
            String keywords = keywordsLS.getDefaultString();
            if (keywords != null && keywords.length() != 0) {
               categoryNames = keywords.split(",");
            }
         }
  
         if (categoryNames == null || categoryNames.length == 0) {
            categoryNames = new String[]{portletApplicationName};
         }
  
         if (portlet.isRemote()) {
            categoryNames = Tools.appendTo(categoryNames, REMOTE_CATEGORY_NAME);
         }
  
         for (String categoryName : categoryNames) {
            ApplicationCategory category;
  
            categoryName = categoryName.trim();
  
            category = appRegistryService.getApplicationCategory(categoryName);
            if (category == null) {
               category = new ApplicationCategory();
               category.setName(categoryName);
               category.setDisplayName(categoryName);
            }
  
            Application app = appRegistryService.getApplication(categoryName + "/" + portletName);
            if (app != null) return app;
            
            ContentType<?> contentType;
            String contentId;
            
            LocalizedString descriptionLS = portlet.getInfo().getMeta().getMetaValue(MetaInfo.DESCRIPTION);
            LocalizedString displayNameLS = portlet.getInfo().getMeta().getMetaValue(MetaInfo.DISPLAY_NAME);
  
            getLocalizedStringValue(descriptionLS, portletName);
  
            app = new Application();
            
            if (portlet.isRemote())
            {
               contentType = WSRP.CONTENT_TYPE;
               contentId = portlet.getContext().getId();
            }
            else
            {
               contentId = info.getApplicationName() + "/" + info.getName();
            }

            app.setContentId(contentId);
            app.setApplicationName(portletName);
            app.setCategoryName(categoryName);
            app.setDisplayName(getLocalizedStringValue(displayNameLS, portletName));
            app.setDescription(getLocalizedStringValue(descriptionLS, portletName));
            appList.add(app);
            return app;
         }
       }
    }
    
    return null;
  }

  /**
   * Get list of application get from portal container.
   */
  static public List<Application> getAppList() {
    return appList;
  }
  /**
   * PortletCategoryComparator
   *
   */
  static class PortletCategoryComparator implements Comparator<ApplicationCategory> {
     public int compare(ApplicationCategory cat1, ApplicationCategory cat2) {
        return cat1.getDisplayName().compareTo(cat2.getDisplayName());
     }
  }

  /**
   * PortletComparator
   *
   */
  static class PortletComparator implements Comparator<Application> {
     public int compare(Application p1, Application p2) {
        return p1.getDisplayName().compareTo(p2.getDisplayName());
     }
  }
  /**
   * Utility for cleaning space name
   * @param str
   * @return cleaned string
   */
  static public String cleanString(String str) {

    Transliterator accentsconverter = Transliterator.getInstance("Latin; NFD; [:Nonspacing Mark:] Remove; NFC;");

    str = accentsconverter.transliterate(str);

    // the character ? seems to not be changed to d by the transliterate
    // function

    StringBuffer cleanedStr = new StringBuffer(str.trim());
    // delete special character
    for (int i = 0; i < cleanedStr.length(); i++) {
      char c = cleanedStr.charAt(i);
      if (c == ' ') {
        if (i > 0 && cleanedStr.charAt(i - 1) == '_') {
          cleanedStr.deleteCharAt(i--);
        } else {
          c = '_';
          cleanedStr.setCharAt(i, c);
        }
        continue;
      }

      if (!(Character.isLetterOrDigit(c) || c == '_')) {
        cleanedStr.deleteCharAt(i--);
        continue;
      }

      if (i > 0 && c == '_' && cleanedStr.charAt(i - 1) == '_')
        cleanedStr.deleteCharAt(i--);
    }
    return cleanedStr.toString().toLowerCase();
  }
  
  /**
   * Utility for getting space url based on url address 
   */
  static public String getSpaceUrl() {
    PageNode selectedNode = null;
    try {
      selectedNode = Util.getUIPortal().getSelectedNode();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    String spaceUrl = selectedNode.getUri();
    if (spaceUrl.contains("/")) {
      spaceUrl = spaceUrl.split("/")[0];
    }
    return spaceUrl;
  }
  
  /**
   * Utility for setting navigation
   * @param nav
   */
  static public void setNavigation(PageNavigation nav) {
    UIPortal uiPortal = Util.getUIPortal();
    try {
      List<PageNavigation> navs = uiPortal.getNavigations();
      boolean alreadyExisted = false;
      for (int i = 0; i < navs.size(); i++) {
        if (navs.get(i).getId() == nav.getId()) {
          navs.set(i, nav);
          alreadyExisted = true;
          return;
        }
      }
      if (!alreadyExisted) {
        navs.add(nav);
        PageNode selectedPageNode = uiPortal.getSelectedNode();
        uiPortal.setNavigation(navs);
        uiPortal.setSelectedNode(selectedPageNode);
      }
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    }
  }
  /**
   * Utility for removing portal navigation
   * @param nav
   * @throws Exception 
   */
  static public void removeNavigation(PageNavigation nav) throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    if (exoContainer == null) {
      exoContainer = ExoContainerContext.getCurrentContainer();
    }
    if (userPortalConfigService == null) {
      userPortalConfigService = (UserPortalConfigService) exoContainer.getComponentInstanceOfType(UserPortalConfigService.class);
    }
    List<PageNavigation> navs = uiPortal.getNavigations();
    
    navs.remove(nav);
    try {
      uiPortal.setNavigation(navs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    //current page not existed, broadcast to default home node
    String portalOwner = Util.getPortalRequestContext().getPortalOwner();
    PageNavigation portalNavigation = null;
    for (PageNavigation pn: navs) {
      if (pn.getOwnerId().equals(portalOwner)) {
        portalNavigation = pn;
        break;
      }
    }
    List<PageNode> nodes = portalNavigation.getNodes();
    PageNode selectedPageNode = nodes.get(0);
    PortalRequestContext prContext = Util.getPortalRequestContext();
    String redirect = prContext.getPortalURI() + selectedPageNode.getUri();
    prContext.getResponse().sendRedirect(redirect);
    prContext.setResponseComplete(true);
  }
  
  /**
   * Reload all portal navigations
   * synchronizing navigations and stores in currentAccessibleSpaces
   * BUG: SOC-406, SOC-134
   * @throws Exception 
   */
  public static void reloadNavigation() throws Exception {
    if (exoContainer == null) {
      exoContainer = ExoContainerContext.getCurrentContainer();
    }
    if (spaceService == null) {
      spaceService = (SpaceService) exoContainer.getComponentInstanceOfType(SpaceService.class);
    }
    String userId = Util.getPortalRequestContext().getRemoteUser();
    List<Space> spaces = spaceService.getAccessibleSpaces(userId);
    List<PageNavigation> navs = Util.getUIPortal().getNavigations();
    List<PageNavigation> spaceNavs = new ArrayList<PageNavigation>();
    String ownerId;
    for (PageNavigation nav: navs) {
      ownerId = nav.getOwnerId();
      try {
        Space space = getSpaceByGroupId(ownerId);
        if (space != null) {
          spaceNavs.add(nav);
        }
      } catch (Exception e) {
        //it does not exist, just ignore
      }
    }
    String groupId;
    //add new space navigation
    for (Space space: spaces) {
      groupId = space.getGroupId();
      boolean spaceContained = false;
      for (PageNavigation nav: spaceNavs) {
        if (groupId.equals(nav.getOwnerId())) {
          spaceContained = true;
          break;
        }
      }
      if (spaceContained == false) {
        setNavigation(getGroupNavigation(groupId));
      }
    }
    //remove deleted space navigation
    if (spaces.size() == 0) {
      //remove all navs
      for (PageNavigation nav: spaceNavs) {
        removeNavigation(nav);
      }
    } else {
      for (PageNavigation nav: spaceNavs) {
        boolean navContained = false;
        for (Space space: spaces) {
          groupId = space.getGroupId();
          if (groupId.equals(nav.getOwnerId())) {
            navContained = true;
            break;
          }
        }
        if (navContained == false) {
          removeNavigation(nav);
        }
      }
    }
  }
  
  /**
   * get space by groupId
   * TODO: Move to SpaceService?
   * @param groupId
   * @return
   * @throws SpaceException
   */
  private static Space getSpaceByGroupId(String groupId) throws SpaceException {
    List<Space> spaces =  spaceService.getAllSpaces();
    for (Space space: spaces) {
      if (space.getGroupId().equals(groupId)) {
        return space;
      }
    }
    return null;
  }
  
  /**
   * Update working work space
   */
  static public void updateWorkingWorkSpace() {
    UIPortalApplication uiPortalApplication = Util.getUIPortalApplication();
    UIWorkingWorkspace uiWorkingWS = uiPortalApplication.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
    PortalRequestContext pContext = Util.getPortalRequestContext();
    pContext.addUIComponentToUpdateByAjax(uiWorkingWS);
    pContext.setFullRender(true);
//    UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
//    UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChild(UIWorkingWorkspace.class);
//    try {
//      uiWorkingWS.updatePortletsByName("UserToolbarGroupPortlet");
//    } catch (Exception e) {
//      // TODO: handle exception
//    }
  }

  /**
   * Create new group in /Spaces node and return groupId
   *
   * @param spaceName String
   * @param creator String
   * @return groupId String
   * @throws SpaceException
   */
  static public String createGroup(String spaceName, String creator) throws SpaceException {
   
    OrganizationService organizationService = getOrganizationService();
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group parentGroup;
    Group newGroup;
    String groupId;
    String shortName;
    try {
      parentGroup = groupHandler.findGroupById(SPACE_GROUP);
      // Create new group
      newGroup = groupHandler.createGroupInstance();
      shortName = SpaceUtils.cleanString(spaceName);
      groupId = parentGroup.getId() + "/" + shortName;
      if (isSpaceNameExisted(spaceName)) {
        throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
      }
      newGroup.setGroupName(shortName);
      newGroup.setLabel(spaceName);
      groupHandler.addChild(parentGroup, newGroup, true);
    } catch (Exception e) {
      if (e instanceof SpaceException) {
        throw (SpaceException) e;
      }
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREATE_GROUP, e);
    }

    try {
      // add user as creator (manager)
      addCreatorToGroup(creator, groupId);
    } catch (Exception e) {
      // TODO:should rollback what has to be rollback here
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_CREATOR, e);
    }
    return groupId;
  }
  
  /**
   * Remove a Group owning a space
   * @param space
   * @throws SpaceException
   */
  static public void removeGroup(Space space) throws SpaceException {
    try {
    OrganizationService organizationService = getOrganizationService();
    GroupHandler groupHandler = organizationService.getGroupHandler();
    Group group = groupHandler.findGroupById(space.getGroupId());
    groupHandler.removeGroup(group, true);
    } catch(Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_GROUP, e);
    }
  }
  
  /**
   * Checking if a space has existed,
   * 
   * @param spaceName
   * @return boolean if existed return true, else return false
   * @throws SpaceException with code INTERNAL_SERVER_ERROR
   */
  static public boolean isSpaceNameExisted(String spaceName) throws SpaceException {
    PortalContainer portalContainer = PortalContainer.getInstance();
    SpaceService spaceService = (SpaceService)portalContainer.getComponentInstanceOfType(SpaceService.class);
    List<Space> spaces = spaceService.getAllSpaces();
    //Checking whether spaceName has existed yet
    for (Space space : spaces) {
      if (space.getName().equalsIgnoreCase(spaceName)) return true;
    }
    return false;
  }
  
  /**
   * When user choose an existing group, that user will added to that group as a manager
   * 
   * @param creator String
   * @param groupId String
   * @throws SpaceException with code UNABLE_TO_ADD_CREATOR
   */
  static public void addCreatorToGroup(String creator, String groupId) throws SpaceException {
    PortalContainer portalContainer = PortalContainer.getInstance();
    OrganizationService organizationService = (OrganizationService) portalContainer.getComponentInstanceOfType(OrganizationService.class);
    try {
      // TODO: check whether user is already manager?
      GroupHandler groupHandler = organizationService.getGroupHandler();
      Group existingGroup = groupHandler.findGroupById(groupId);
      User user = organizationService.getUserHandler().findUserByName(creator);
      MembershipType membershipType = organizationService.getMembershipTypeHandler()
                                                         .findMembershipType(MANAGER);
      organizationService.getMembershipHandler().linkMembership(user,
                                                                existingGroup,
                                                                membershipType,
                                                                true);

    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_CREATOR, e);
    }
  }

  /**
   * Create Group navigation if not existed or 
   * return existing Group navigation based on groupId
   * 
   * @param groupId String
   * @return spaceNav PageNavigation
   * @throws SpaceException
   */
  static public PageNavigation createGroupNavigation(String groupId) throws SpaceException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    UserPortalConfigService configService = (UserPortalConfigService) container.getComponentInstanceOfType(UserPortalConfigService.class);
    //groupId = groupId.substring(1);
    PageNavigation spaceNav;
    try {
      spaceNav = configService.getPageNavigation(PortalConfig.GROUP_TYPE, groupId);
      if (spaceNav == null) {
        // create new space navigation
        spaceNav = new PageNavigation();
        spaceNav.setOwnerType(PortalConfig.GROUP_TYPE);
        spaceNav.setOwnerId(groupId);
        spaceNav.setModifiable(true);

        UIPortal uiPortal = Util.getUIPortal();
        List<PageNavigation> pnavigations = uiPortal.getNavigations();
        pnavigations.add(spaceNav);
        configService.create(spaceNav);
      }
      return spaceNav;
    } catch (Exception e) {
      // TODO:should rollback what has to be rollback here
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREAT_NAV, e);
    }
  }
  
  /**
   * Remove group navigations.
   * @param groupId
   * @throws SpaceException
   */
  static public void removeGroupNavigation(String groupId) throws SpaceException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    UserPortalConfigService configService = (UserPortalConfigService) container.getComponentInstanceOfType(UserPortalConfigService.class);
    PageNavigation spaceNav;
    try {
      spaceNav = configService.getPageNavigation(PortalConfig.GROUP_TYPE, groupId);
      if (spaceNav != null) {
        UIPortal uiPortal = Util.getUIPortal();
        List<PageNavigation> pnavigations = uiPortal.getNavigations();
        pnavigations.remove(spaceNav);
        configService.remove(spaceNav);
      } else {
        throw new Exception("spaceNav is null");
      }
    } catch(Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_NAV, e);
    }
    
  }
  
  /**
   * Get PageNavigation from a space's groupId
   * @param groupId 
   * @return pageNavigation
   * @throws Exception 
   * @throws Exception 
   */
  static public PageNavigation getGroupNavigation(String groupId) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    UserPortalConfigService configService = (UserPortalConfigService) container.getComponentInstanceOfType(UserPortalConfigService.class);
    return (PageNavigation) configService.getPageNavigation(PortalConfig.GROUP_TYPE, groupId);
  }
  
  /**
   * Sort spaces list by priority and alphabet order
   * @param spaces
   * @return ordered spaces list
   */
  static public List<Space> getOrderedSpaces(List<Space> spaces) {
    Iterator<Space> itr = spaces.iterator();
    List<Space> orderedSpaces = new ArrayList<Space>();
    List<Space> middleSpaces = new ArrayList<Space>();
    List<Space> lowSpaces = new ArrayList<Space>();
    Space space = null;
    while(itr.hasNext()) {
      space = itr.next();
      String priority = space.getPriority();
      if (priority.equals(Space.HIGH_PRIORITY)) {
        orderedSpaces.add(space);
      } else if (priority.equals(Space.INTERMEDIATE_PRIORITY)) {
        middleSpaces.add(space);
      } else if (priority.equals(Space.LOW_PRIORITY)) {
        lowSpaces.add(space);
      }
    }
    for (Space sp: middleSpaces) {
      orderedSpaces.add(sp);
    }
    for (Space sp: lowSpaces) {
      orderedSpaces.add(sp);
    }
    return orderedSpaces;
  }
  
  /**
   * Utility for counting the number of members in a space
   * @param space
   * @return the number of members
   * @throws SpaceException
   */
  static public int countMembers(Space space) throws SpaceException {
    PortalContainer portalContainer = PortalContainer.getInstance();
    SpaceService spaceService = (SpaceService)portalContainer.getComponentInstanceOfType(SpaceService.class);
    return spaceService.getMembers(space).size();
  }
  
  /**
   * gets app status in a space
   * @param space
   * @param appId
   * @return appStatus
   */
  static public String getAppStatus(Space space, String appId) {
    String installedApps = space.getApp();
    String[] appSplit = null;
    if (installedApps == null) installedApps = "";
    if (installedApps.contains(appId)) {
      String[] apps = installedApps.split(",");
      for (String app: apps) {
        if (app.contains(appId)) {
          appSplit = app.split(":");
          return appSplit[appSplit.length - 1];
        }
      }
    }
    return null;
  }
  
  /**
   * Check application is removable or not.
   * 
   * @param space
   * @param appId
   * @return Boolean value show one application is removable or not.
   */
  static public boolean isRemovableApp(Space space, String appId) {
    String installedApps = space.getApp();
    boolean removable = false;
    String remove = null;
    appId = appId.split("/")[1];
    String[] appSplit = null;
    
    if (installedApps == null) installedApps = "";
    if (installedApps.contains(appId)) {
      String[] apps = installedApps.split(",");
      for (String app: apps) {
        if (app.contains(appId)) {
           appSplit = app.split(":");
           // Hard-code one application is removable if it is not set in setting
           remove = (appSplit.length > 3) ? appSplit[1] : "true";
           return Boolean.parseBoolean(remove);
        }
      }
    }
    return removable;
  }
  
  /**
   * Utility for getting absolute url
   * @return absolute url
   * @throws SpaceException
   */
  static public String getAbsoluteUrl() throws SpaceException {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    HttpServletRequest request = portalRequestContext.getRequest();
    String str = request.getRequestURL().toString();
    return str.substring(0, str.indexOf(portalRequestContext.getRequestContextPath()));
  }
  
  /**
   * Checks user is removed or not.<br>
   * 
   * @param userName User name for checking.
   * 
   * @throws SpaceEception if user is removed.
   */
  static public void checkUserExisting(String userName) throws SpaceException {
    OrganizationService orgService = getOrganizationService();
    User user = null;
    try {
      user = orgService.getUserHandler().findUserByName(userName);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_RETRIEVING_USER, e);
    }
    
    if (user == null) {
      throw new SpaceException(SpaceException.Code.ERROR_RETRIEVING_USER);
    }
  }
  
  /**
   * Checking whether a group can have access to an application
   * @param app
   * @param groupId
   * @return
   * @throws Exception
   */
  static private boolean hasAccessPermission(Application app, String groupId) throws Exception {
    List<String> permissions = app.getAccessPermissions();
    if (permissions == null)
      return false;
    for (String ele : permissions) {
      if (hasViewPermission(ele, groupId))
        return true;
    }
    return false;
  }
  
  /**
   * Get Organization Service
   * @return
   */
  static public OrganizationService getOrganizationService(){
    PortalContainer portalContainer = PortalContainer.getInstance();
    return (OrganizationService) portalContainer.getComponentInstanceOfType(OrganizationService.class);
  }
  
  /**
   * Checking whether a group have access permission to an application category
   * @param app
   * @param groupId
   * @return true if that group has access permission; otherwise, false
   * @throws Exception
   */
  static private boolean hasAccessPermission(ApplicationCategory app, String groupId) throws Exception {
    List<String> permissions = app.getAccessPermissions();
    if (permissions == null)
      return false;
    for (String ele : permissions) {
      if (hasViewPermission(ele, groupId)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Checking view permission
   * @param expPerm
   * @param groupId
   * @return
   * @throws Exception
   */
  static private boolean hasViewPermission(String expPerm, String groupId) throws Exception {
    if (UserACL.EVERYONE.equals(expPerm))
      return true;
    String[] temp = expPerm.split(":");
    if (temp.length < 2)
      return false;
    String tempExp = temp[1].trim();
    if (tempExp.equals(groupId) || tempExp.equals(SPACE_GROUP)) {
      return true;
    }
    return false;
  }
  
  private static String getLocalizedStringValue(LocalizedString localizedString, String portletName)
  {
     if (localizedString == null || localizedString.getDefaultString() == null)
     {
        return portletName;
     }
     else
     {
        return localizedString.getDefaultString();
     }
  }
  
  private static ApplicationRegistryService getApplicationRegistryService() {
    if(appService == null) {
      PortalContainer portalContainer = PortalContainer.getInstance();
      appService = (ApplicationRegistryService) portalContainer.getComponentInstanceOfType(ApplicationRegistryService.class);
    }
    return appService;
  }
}
