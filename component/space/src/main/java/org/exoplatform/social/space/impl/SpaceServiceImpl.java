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
package org.exoplatform.social.space.impl;

import java.util.*;

import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.*;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.social.application.SpaceApplicationHandler;
import org.exoplatform.social.application.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.webui.event.Event;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * August 29, 2008          
 */
public class SpaceServiceImpl implements SpaceService{
  final static public String SPACE_PARENT = "/spaces";


  private JCRStorage storage;
  private Map<String, SpaceApplicationHandler> spaceApplicationHandlers = null;

  public SpaceServiceImpl(NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    storage = new JCRStorage(nodeHierarchyCreator);

  }

  public Space createSpace(String spaceName, String creator) throws SpaceException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    OrganizationService orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);

    GroupHandler groupHandler = orgService.getGroupHandler();
    Group groupParent;
    Group newGroup;
    String spaceNameCleaned;
    try {

      groupParent = groupHandler.findGroupById(SPACE_PARENT);
      //Create new group
      newGroup = groupHandler.createGroupInstance();

      spaceNameCleaned = SpaceUtils.cleanString(spaceName);
      String groupId = groupParent.getId() + "/" + spaceNameCleaned;
      if(groupHandler.findGroupById(groupId) != null) {
        throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
      }
      newGroup.setGroupName(spaceNameCleaned);
      newGroup.setLabel(spaceName);
      groupHandler.addChild(groupParent, newGroup, true);
    } catch (Exception e) {
      if(e instanceof SpaceException) {
        throw (SpaceException)e;
      }
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREATE_GROUP, e);
    }

    try {
      // add user as creator (manager)
      User user = orgService.getUserHandler().findUserByName(creator);
      MembershipType mbShipType = orgService.getMembershipTypeHandler().findMembershipType("manager");
      orgService.getMembershipHandler().linkMembership(user, newGroup, mbShipType, true);
    } catch (Exception e) {
      //TODO:should rollback what has to be rollback here
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_CREATOR, e);
    }

    // Store space to database
    Space space = new Space();
    space.setName(spaceName);
    space.setGroupId(newGroup.getId());
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setDescription("edit this description to explain what your space is about");
    space.setTag("");
    saveSpace(space, true);
    

    try {
      // create the new page and node to new group
      // the template page id
      String tempPageId= "group::platform/user::dashboard";

      //create the name and uri of the new pages
      String newPageName = spaceNameCleaned;

      // create new space navigation
      UserPortalConfigService dataService = (UserPortalConfigService) container.getComponentInstanceOfType(UserPortalConfigService.class);

      PageNavigation spaceNav = new PageNavigation();
      spaceNav.setOwnerType(PortalConfig.GROUP_TYPE);
      spaceNav.setOwnerId(newGroup.getId().substring(1));
      spaceNav.setModifiable(true);
      dataService.create(spaceNav);
      UIPortal uiPortal = Util.getUIPortal();
      List<PageNavigation> pnavigations = uiPortal.getNavigations();
      SpaceUtils.setNavigation(pnavigations, spaceNav);
      pnavigations.add(spaceNav) ;
      PageNode node = dataService.createNodeFromPageTemplate(newPageName, newPageName, tempPageId, PortalConfig.GROUP_TYPE, spaceNameCleaned,null) ;
      node.setUri(spaceNameCleaned) ;
      spaceNav.addNode(node) ;
      dataService.update(spaceNav) ;
      SpaceUtils.setNavigation(uiPortal.getNavigations(), spaceNav) ;
    } catch (Exception e) {
      //TODO:should rollback what has to be rollback here
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREAT_NAV, e);
    }
    // add user list to default application
    installApplication(space, "UserListPortlet");
    activateApplication(space, "UserListPortlet");
    return space;
  }

  public void installApplication(Space space, String appId) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.installApplication(space, appId);
    setApp(space, appId, Space.INSTALL_STATUS);
  }

  public void installApplication(String spaceId, String appId) throws SpaceException {
    installApplication(getSpace(spaceId), appId);  
  }

  public void deactiveApplication(Space space, String appId) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.deactiveApplication(space, appId);
    setApp(space, appId, Space.DEACTIVE_STATUS);
  }

  public void activateApplication(Space space, String appId) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.activateApplication(space, appId);
    setApp(space, appId, Space.ACTIVE_STATUS);
  }

  public void activateApplication(String spaceId, String appId) throws SpaceException {
    activateApplication(getSpace(spaceId), appId);
  }

  public void removeApplication(Space space, String appId) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.removeApplication(space, appId);
    removeApp(space, appId);
  }

  public List<Space> getAllSpaces() throws SpaceException {
    try {
      return storage.getAllSpaces();
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  public Space getSpace(String id) throws SpaceException {
    try {
      return storage.getSpace(id);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  public void saveSpace(Space space, boolean isNew) throws SpaceException {
    try {
      storage.saveSpace(space, isNew);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }


  public void leave(String spaceId, String userId) throws SpaceException {
    leave(getSpace(spaceId), userId);
  }

  public void leave(Space space, String userId) throws SpaceException {
    try {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      OrganizationService orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);

      String groupID = space.getGroupId();
      MembershipHandler memberShipHandler = orgService.getMembershipHandler();
      Collection<Membership> memberships = memberShipHandler.findMembershipsByUserAndGroup(userId, groupID);
      if (memberships.size() == 0) {
        throw new SpaceException(SpaceException.Code.USER_NOT_MEMBER);
      }

      Iterator<Membership> itr = memberships.iterator();
      while(itr.hasNext()) {
        Membership mbShip = itr.next();
        //Membership memberShip = memberShipHandler.findMembershipByUserGroupAndType(userId, groupID, mbShip.getMembershipType());
        memberShipHandler.removeMembership(mbShip.getId(), true);
      }
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_USER, e);
    }
  }

  public void acceptInvitation(String spaceId, String userId) throws SpaceException {
    acceptInvitation(getSpace(spaceId), userId);
  }

  public void acceptInvitation(Space space, String userId) throws SpaceException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    OrganizationService orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);

    // remove user from invited user list
    String invitedUser = space.getInvitedUser();

    if (!invitedUser.contains(userId)) {
      throw new SpaceException(SpaceException.Code.USER_NOT_INVITED);
    }

    invitedUser = invitedUser.replace(userId, "");
    if(invitedUser.contains(",,"))
      invitedUser = invitedUser.replace(",,", ",");
    if(invitedUser.indexOf(",") == 0)
      invitedUser = invitedUser.substring(1);
    if(invitedUser.equals(""))
      invitedUser=null;
    space.setInvitedUser(invitedUser);
    saveSpace(space, false);

    // add member
    try {
      UserHandler userHandler = orgService.getUserHandler();
      User user = userHandler.findUserByName(userId);
      MembershipType mbShipType = orgService.getMembershipTypeHandler().findMembershipType("member");
      MembershipHandler membershipHandler = orgService.getMembershipHandler();
      Group spaceGroup = orgService.getGroupHandler().findGroupById(space.getGroupId());
      membershipHandler.linkMembership(user, spaceGroup, mbShipType, true);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_USER, e);
    }
    
  }

  public void denyInvitation(Space space, String userId) throws SpaceException {
          // remove user from invited user list
      String invitedUser = space.getInvitedUser();
      invitedUser = invitedUser.replace(userId, "");
      if(invitedUser.contains(",,")) invitedUser = invitedUser.replace(",,", ",");
      if(invitedUser.indexOf(",") == 0) invitedUser = invitedUser.substring(1);
      if(invitedUser.equals("")) invitedUser=null;
      space.setInvitedUser(invitedUser);
      saveSpace(space, false);
  }

  public List getMembers(Space space) {
    return null;
  }

  private Map<String, SpaceApplicationHandler> getSpaceApplicationHandlers() {
    if(this.spaceApplicationHandlers == null) {
      this.spaceApplicationHandlers = new HashMap();

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      SpaceApplicationHandler appHandler = (DefaultSpaceApplicationHandler) container.getComponentInstanceOfType(DefaultSpaceApplicationHandler.class);
      this.spaceApplicationHandlers.put(appHandler.getName(), appHandler);
    }
    return this.spaceApplicationHandlers;
  }

  private SpaceApplicationHandler getSpaceApplicationHandler(Space space) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandlers().get(space.getType());
    if (appHandler == null)
      throw new SpaceException(SpaceException.Code.UNKNOWN_SPACE_TYPE);
    return appHandler;
  }

  private void setApp(Space space, String appId, String status) throws SpaceException {
    String apps = space.getApp();
    if(apps == null) apps = appId + ":" + status;
    else {
      if(status.equals(Space.INSTALL_STATUS)) apps = apps + "," + appId + ":" + status;
      else {
        String oldStatus = apps.substring(apps.indexOf(appId));
        if(oldStatus.indexOf(",") != -1) oldStatus = oldStatus.substring(0, oldStatus.indexOf(",")-1);
        apps = apps.replaceFirst(oldStatus, appId + ":" + status);
      }
    }
    space.setApp(apps);
    saveSpace(space, false);
  }

  private void removeApp(Space space, String appId) throws SpaceException {
    String apps = space.getApp();
    String oldStatus = apps.substring(apps.indexOf(appId));
    if(oldStatus.indexOf(",") != -1) oldStatus = oldStatus.substring(0,oldStatus.indexOf(","));
    apps = apps.replaceFirst(oldStatus, "");
    if(apps.equals("")) apps = null;
    space.setApp(apps);
    saveSpace(space, false);
  }
}