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
import java.util.Iterator;
import java.util.List;
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
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

import com.ibm.icu.text.Transliterator;

/**
 * SpaceUtils
 * Utility for working with space
 */
public class SpaceUtils {

  static private final String SPACE_GROUP = "/spaces";

  static public final String  MEMBER      = "member";

  static public final String  MANAGER     = "manager";
  
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
   */
  static public List<Application> getApplications(String groupId) throws Exception {

    List<Application> list = new CopyOnWriteArrayList<Application>();
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
    ApplicationRegistryService appRegistrySrc = (ApplicationRegistryService) exoContainer.getComponentInstanceOfType(ApplicationRegistryService.class);

    List<ApplicationCategory> listCategory = appRegistrySrc.getApplicationCategories();
    Iterator<ApplicationCategory> cateItr = listCategory.iterator();
    String[] applicationTypes = { org.exoplatform.web.application.Application.EXO_PORTLET_TYPE };
    while (cateItr.hasNext()) {
      ApplicationCategory cate = cateItr.next();
      if (!hasAccessPermission(cate, groupId)) {
        cateItr.remove();
        continue;
      }
      List<Application> applications = appRegistrySrc.getApplications(cate, applicationTypes);
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
   * @return spaceUrl
   */
  static public String getSpaceUrl() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String requestUrl = pcontext.getRequestURI();
    String portalUrl = pcontext.getPortalURI();
    String spaceUrl = requestUrl.replace(portalUrl, "");
    if (spaceUrl.contains("/"))
      spaceUrl = spaceUrl.split("/")[0];
    return spaceUrl;
  }
  
  /**
   * Utility for setting navigation
   * @param nav
   */
  static public void setNavigation(PageNavigation nav) {
    UIPortal uiPortal = Util.getUIPortal();
    List<PageNavigation> navs = uiPortal.getNavigations();
    for (int i = 0; i < navs.size(); i++) {
      if (navs.get(i).getId() == nav.getId()) {
        navs.set(i, nav);
        return;
      }
    }
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
   * @param space
   * @return
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
      } else if (priority.equals(Space.MIDDLE_PRIORITY)) {
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
   * @return
   * @throws SpaceException
   */
  static public int countMembers(Space space) throws SpaceException {
    PortalContainer portalContainer = PortalContainer.getInstance();
    SpaceService spaceService = (SpaceService)portalContainer.getComponentInstanceOfType(SpaceService.class);
    return spaceService.getMembers(space).size();
  }
  
  /**
   * Utility for getting absolute url
   * @return
   * @throws SpaceException
   */
  static public String getAbsoluteUrl() throws SpaceException {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    HttpServletRequest request = portalRequestContext.getRequest();
    String str = request.getRequestURL().toString();
    return str.substring(0, str.indexOf(portalRequestContext.getRequestContextPath()));
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
  static private OrganizationService getOrganizationService(){
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

}
