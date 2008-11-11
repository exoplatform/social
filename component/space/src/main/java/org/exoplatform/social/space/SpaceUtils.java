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
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.application.registry.Application;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.portletcontainer.PortletContainerService;
import org.exoplatform.services.portletcontainer.pci.PortletData;
import org.exoplatform.services.portletcontainer.pci.model.Description;
import org.exoplatform.services.portletcontainer.pci.model.DisplayName;


/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 23, 2008          
 */
public class SpaceUtils {
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
  
  public static List<Application> getAllApplications() {
    List<Application> list = new ArrayList<Application>(10) ;
    ExoContainer manager  = ExoContainerContext.getCurrentContainer();
    PortletContainerService pcService =
      (PortletContainerService) manager.getComponentInstanceOfType(PortletContainerService.class) ;
    Map<String, PortletData> allPortletMetaData = pcService.getAllPortletMetaData();
    Iterator<Entry<String, PortletData>> iterator = allPortletMetaData.entrySet().iterator();

    while(iterator.hasNext()) {
      Entry<String, PortletData> entry = iterator.next() ;
      String fullName = entry.getKey();
      String categoryName = fullName.split("/")[0];
      String portletName = fullName.split("/")[1];
      PortletData portlet = entry.getValue();
      Application app = new Application();
      app.setApplicationName(portletName);
      app.setApplicationGroup(categoryName);
      app.setApplicationType(org.exoplatform.web.application.Application.EXO_PORTLET_TYPE);
      app.setDisplayName(getDisplayNameValue(portlet.getDisplayName(), portletName)) ;
      app.setDescription(getDescriptionValue(portlet.getDescription(), portletName));
      app.setAccessPermissions(new ArrayList<String>());
      list.add(app) ;
    }
    return list;
  }
  
  private static String getDisplayNameValue(List<DisplayName> list, String defaultValue) {
    if(list == null || list.isEmpty()) return defaultValue;
    return list.get(0).getDisplayName();
  }
  
  private static String getDescriptionValue(List<Description> list, String defaultValue) {
    if(list == null || list.isEmpty()) return defaultValue;
    return list.get(0).getDescription();
  }
  
  public static String cleanString(String str) {
    StringBuffer cleanedStr = new StringBuffer(str.trim());
    // delete special character
    for(int i = cleanedStr.length()-1; i >= 0; i--) {
      char c = cleanedStr.charAt(i);
      if(!(Character.isLetterOrDigit(c) || c == '_' || c == ' ')) {
        cleanedStr.deleteCharAt(i);
      }
    }
    // replace ' ' character by '_'
    for(int i = cleanedStr.length()-1; i > 0; i--) {
      if(cleanedStr.charAt(i) == ' ') cleanedStr.setCharAt(i, '_');
    }
    // retain '_' character but if there are many instances replace with one
    for(int i = cleanedStr.length()-1; i > 0; i--) {
      if(cleanedStr.charAt(i) == '_' && cleanedStr.charAt(i-1) == '_') cleanedStr.deleteCharAt(i);
    }
    return cleanedStr.toString().toLowerCase();
  }
}