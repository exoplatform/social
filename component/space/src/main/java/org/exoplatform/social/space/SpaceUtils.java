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
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

import com.ibm.icu.text.Transliterator;


public class  SpaceUtils {
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
  
  public static List<Application> getAllApplications(String spaceId) throws Exception {
    List<Application> list = new CopyOnWriteArrayList<Application>() ;
    PortalContainer portalContainer = PortalContainer.getInstance();
    ApplicationRegistryService appRegistrySrc = (ApplicationRegistryService)portalContainer.getComponentInstanceOfType(ApplicationRegistryService.class);
    String[] applicationTypes = {org.exoplatform.web.application.Application.EXO_PORTLET_TYPE};
    List<ApplicationCategory> listCategory = appRegistrySrc.getApplicationCategories(Util.getPortalRequestContext().getRemoteUser(), applicationTypes);
    Iterator<ApplicationCategory> cateItr = listCategory.iterator() ;
    while (cateItr.hasNext()) {
      ApplicationCategory cate = cateItr.next();
      List<Application> applications = cate.getApplications();
      Iterator<Application> appIterator = applications.iterator() ;
      while (appIterator.hasNext()) {
        list.add(appIterator.next());
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

  public static void setNavigation(List<PageNavigation> navs, PageNavigation nav) {
    for(int i = 0; i < navs.size(); i++) {
      if(navs.get(i).getId() == nav.getId()) {
        navs.set(i, nav);
        return;
      }
    }
  }
  
  public static String getShortSpaceName() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String requestUrl = pcontext.getRequestURI();
    String portalUrl = pcontext.getPortalURI();
    String spaceName = requestUrl.replace(portalUrl,"");
    if(spaceName.contains("/")) spaceName = spaceName.split("/")[0];
    return spaceName;
  }
}