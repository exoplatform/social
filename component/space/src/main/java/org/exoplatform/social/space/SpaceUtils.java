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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PageNavigation;
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


public class  SpaceUtils {
  
  private final static String SPACE_GROUP = "/spaces";
  
  @SuppressWarnings("unchecked")
  public static Group createGroupFromExistGroup(Group parrentGroup, Group exitsGroup, String name) throws Exception {
    PortalContainer portalContainer = PortalContainer.getInstance();
    OrganizationService orgSrc = (OrganizationService)portalContainer.getComponentInstanceOfType(OrganizationService.class);
    GroupHandler groupHandler = orgSrc.getGroupHandler();
    MembershipHandler memberShipHandler = orgSrc.getMembershipHandler();
    Group newGroup = groupHandler.createGroupInstance();
    newGroup.setGroupName(name);
    newGroup.setLabel(name);
    newGroup.setDescription(exitsGroup.getDescription());
    groupHandler.addChild(parrentGroup, newGroup, true);
    Collection<Membership> memberShips = memberShipHandler.findMembershipsByGroup(exitsGroup);
    Iterator<Membership> itr = memberShips.iterator();
    while(itr.hasNext()) {
      Membership membership = itr.next();
      User user = orgSrc.getUserHandler().findUserByName(membership.getUserName());
      MembershipType memberShipType = orgSrc.getMembershipTypeHandler().findMembershipType(membership.getMembershipType());
      memberShipHandler.linkMembership(user, newGroup, memberShipType, true);
    }
    return newGroup;
  }
  
  public static List<Application> getAllApplications(String groupId) throws Exception {
    
    List<Application> list = new CopyOnWriteArrayList<Application>();
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
    ApplicationRegistryService appRegistrySrc = (ApplicationRegistryService)exoContainer
                                                  .getComponentInstanceOfType(ApplicationRegistryService.class);
    
    List<ApplicationCategory> listCategory = appRegistrySrc.getApplicationCategories();
    Iterator<ApplicationCategory> cateItr = listCategory.iterator();
    String[] applicationTypes = {org.exoplatform.web.application.Application.EXO_PORTLET_TYPE};
    while(cateItr.hasNext()) {
      ApplicationCategory cate = cateItr.next();
      if(!hasAccessPermission(cate, groupId)){
        cateItr.remove();
        continue;
      }
      List<Application> applications = appRegistrySrc.getApplications(cate, applicationTypes);
      Iterator<Application> appIterator = applications.iterator();
      while (appIterator.hasNext()) {
        Application app = appIterator.next();
        if(!hasAccessPermission(app, groupId)) appIterator.remove();
        else list.add(app);
      }
    }
    return list;
  }
  
  public static String cleanString(String str) {
                                                                
    Transliterator accentsconverter = Transliterator.getInstance("Latin; NFD; [:Nonspacing Mark:] Remove; NFC;");

    str = accentsconverter.transliterate(str); 

    //the character ? seems to not be changed to d by the transliterate function 

    StringBuffer cleanedStr = new StringBuffer(str.trim());
    // delete special character
    for(int i = 0; i < cleanedStr.length(); i++) {
      char c = cleanedStr.charAt(i);
      if(c == ' ') {
        if (i > 0 && cleanedStr.charAt(i - 1) == '_') {
          cleanedStr.deleteCharAt(i--);
        }
        else {
          c = '_';
          cleanedStr.setCharAt(i, c);
        }
        continue;
      }

      if(!(Character.isLetterOrDigit(c) || c == '_')) {
        cleanedStr.deleteCharAt(i--);
        continue;
      }

      if(i > 0 && c == '_' && cleanedStr.charAt(i-1) == '_')
        cleanedStr.deleteCharAt(i--);
    }
    return cleanedStr.toString().toLowerCase();
  }
  
  public static String getSpaceUrl() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String requestUrl = pcontext.getRequestURI();
    String portalUrl = pcontext.getPortalURI();
    String spaceUrl = requestUrl.replace(portalUrl,"");
    if(spaceUrl.contains("/")) spaceUrl = spaceUrl.split("/")[0];
    return spaceUrl;
  }
  
  public static void setNavigation(PageNavigation nav) {
    UIPortal uiPortal = Util.getUIPortal();
    List<PageNavigation> navs = uiPortal.getNavigations();
    for(int i = 0; i < navs.size(); i++) {
      if(navs.get(i).getId() == nav.getId()) {
        navs.set(i, nav);
        return;
      }
    }
  }
  
  public static void updateWorkingWorkSpace() {
    UIPortalApplication uiPortalApplication = Util.getUIPortalApplication();
    UIWorkingWorkspace uiWorkingWS = uiPortalApplication.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
    PortalRequestContext pContext = Util.getPortalRequestContext();
    pContext.addUIComponentToUpdateByAjax(uiWorkingWS);
    pContext.setFullRender(true);
  }
  
  private static boolean hasAccessPermission(Application app, String groupId) throws Exception {
    List<String> permissions = app.getAccessPermissions() ; 
    if(permissions == null) return false ;
    for(String ele : permissions) {
      if(hasViewPermission(ele, groupId)) return true;
    }
    return false;
  }
  private static boolean hasAccessPermission(ApplicationCategory app, String groupId) throws Exception {
    List<String> permissions = app.getAccessPermissions() ; 
    if(permissions == null) return false ;
    for(String ele : permissions) {
      if(hasViewPermission(ele, groupId)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasViewPermission(String expPerm, String groupId) throws Exception {
    if(UserACL.EVERYONE.equals(expPerm)) return true;
    String[] temp = expPerm.split(":") ;
    if(temp.length < 2) return false;
    String tempExp = temp[1].trim();
    if(tempExp.equals(groupId) || tempExp.equals(SPACE_GROUP)) {
      return true;
    }
    return false;
  }
}