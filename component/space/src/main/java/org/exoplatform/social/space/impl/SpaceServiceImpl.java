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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.application.SpaceApplicationHandler;
import org.exoplatform.social.application.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;

import java.util.Collections;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * August 29, 2008          
 */
public class SpaceServiceImpl implements SpaceService{
  final static public String SPACE_PARENT = "/spaces";
  final static public String MEMBER = "member";
  final static public String MANAGER = "manager";
   
  private String visibility;
  private String registration;
  private JCRStorage storage;
  private OrganizationService orgService = null;
  private Map<String, SpaceApplicationHandler> spaceApplicationHandlers = null;

  public SpaceServiceImpl(InitParams params, NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    storage = new JCRStorage(nodeHierarchyCreator);
    PropertiesParam properties = params.getPropertiesParam("space");
    if(properties == null) throw new Exception("the 'space' properties parameter is expected.");
    visibility = properties.getProperty("visibility");
    registration = properties.getProperty("registration");
  }

  private OrganizationService getOrgService() {
    if (orgService == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    }
    return orgService;
  }

  public Space createSpace(Space space, String creator) throws SpaceException {
    OrganizationService orgService = getOrgService();

    GroupHandler groupHandler = orgService.getGroupHandler();
    Group groupParent;
    Group newGroup;
    String shortName;
    try {

      groupParent = groupHandler.findGroupById(SPACE_PARENT);
      //Create new group
      newGroup = groupHandler.createGroupInstance();
      String spaceName = space.getName();
      shortName = SpaceUtils.cleanString(spaceName);
      String groupId = groupParent.getId() + "/" + shortName;
      if(groupHandler.findGroupById(groupId) != null) {
        throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
      }
      newGroup.setGroupName(shortName);
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
      MembershipType mbShipType = orgService.getMembershipTypeHandler().findMembershipType(MANAGER);
      orgService.getMembershipHandler().linkMembership(user, newGroup, mbShipType, true);
    } catch (Exception e) {
      //TODO:should rollback what has to be rollback here
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_CREATOR, e);
    }

    // TODO: dang.tung we'll remove it to the UI, don't use in service
    // Store space to database
    if(space.getVisibility().equals("")) space.setVisibility(visibility);
    space.setRegistration(registration);
    space.setGroupId(newGroup.getId());
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setDescription("edit this description to explain what your space is about");
    //TODO: dang.tung: in future when we improve parent of space we have to modify
    //                 url of space = parent's short name + space's short name 
    space.setUrl(shortName);
    saveSpace(space, true);
    //-------------------------------------------------------------------------------
    
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.initSpace(space);

    // add user list to default application
    installApplication(space, "UserListPortlet");
    activateApplication(space, "UserListPortlet");
    // add user list to default application
    installApplication(space, "SpaceSettingPortlet");
    activateApplication(space, "SpaceSettingPortlet");
    return space;
  }

  public void installApplication(Space space, String appId) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.installApplication(space, appId);
    setApp(space, appId, Space.INSTALL_STATUS);
  }

  public void installApplication(String spaceId, String appId) throws SpaceException {
    installApplication(getSpaceById(spaceId), appId);  
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
    activateApplication(getSpaceById(spaceId), appId);
  }

  public void removeApplication(String spaceId, String appId) throws SpaceException {
    removeApplication(getSpaceById(spaceId), appId);
  }
  
  public void removeApplication(Space space, String appId) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.removeApplication(space, appId);
    removeApp(space, appId);
  }

  public List<Space> getAllSpaces() throws SpaceException {
    try {
      List<Space> spaces = storage.getAllSpaces();
      Collections.sort(spaces, new SpaceComparator());
      return spaces;
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }
  
  public List<Space> getAllSpaces(String userId) throws SpaceException {
    try {
      List<Space> spaces = getAllSpaces();
      Iterator<Space> itr = spaces.iterator();
      while(itr.hasNext()) {
        Space space = itr.next();
        if(space.getVisibility().equals(Space.HIDDEN) && !isMember(space, userId))
          itr.remove();
      }
      return spaces;
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  public Space getSpaceById(String id) throws SpaceException {
    try {
      return storage.getSpaceById(id);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }
  
  public Space getSpaceByUrl(String url) throws SpaceException {
    try {
      return storage.getSpaceByUrl(url);
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
    leave(getSpaceById(spaceId), userId);
  }

  @SuppressWarnings("unchecked")
  public void leave(Space space, String userId) throws SpaceException {
    try {
      OrganizationService orgService = getOrgService();

      String groupID = space.getGroupId();
      MembershipHandler memberShipHandler = orgService.getMembershipHandler();
      Collection<Membership> memberships = memberShipHandler.findMembershipsByUserAndGroup(userId, groupID);
      if (memberships.size() == 0) {
        throw new SpaceException(SpaceException.Code.USER_NOT_MEMBER);
      }

      Iterator<Membership> itr = memberships.iterator();
      while(itr.hasNext()) {
        Membership mbShip = itr.next();
        memberShipHandler.removeMembership(mbShip.getId(), true);
      }
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_USER, e);
    }
  }


  public void invite(Space space, String userId) throws SpaceException {
    OrganizationService orgService = getOrgService();

    try {
      User user = orgService.getUserHandler().findUserByName(userId);
      if(user == null) {
        throw new SpaceException(SpaceException.Code.USER_NOT_EXIST);
      }
    } catch (Exception e) {
      if(e instanceof SpaceException)
        throw (SpaceException)e;
      throw new SpaceException(SpaceException.Code.ERROR_RETRIEVING_USER, e);
    }

    if(isInvited(space, userId)) {
      throw new SpaceException(SpaceException.Code.USER_ALREADY_INVITED);
    } else if (isMember(space, userId)) {
      throw new SpaceException(SpaceException.Code.USER_ALREADY_MEMBER);
    }
    if(isPending(space, userId)) {
      String[] pendingUsers = space.getPendingUsers();
      space.setPendingUsers(removeItemFromArray(pendingUsers, userId));
      addMember(space, userId);
    } else {
      String[] invitedUsers = space.getInvitedUsers();
      space.setInvitedUsers(addItemToArray(invitedUsers, userId));
    }
    
    saveSpace(space, false);
    

    // we'll sent a email to invite user
    // TODO: This should be done in a Service in a separated thread
    //TODO: need to be redone
    try {
    /*MailService mailService = (MailService) container.getComponentInstanceOfType(MailService.class);

    ResourceBundle res = requestContext.getApplicationResourceBundle();
    String email = orgService.getUserHandler().findUserByName(userId).getEmail();
    PortalRequestContext portalRequest = Util.getPortalRequestContext();
    String url = portalRequest.getRequest().getRequestURL().toString();
    String headerMail = res.getString(uiSpaceMember.getId()+ ".mail.header") + "\n\n";
    String footerMail = "\n\n\n" + res.getString(uiSpaceMember.getId()+ ".mail.footer");
    String activeLink = url + "?portal:componentId=managespaces&portal:type=action&portal:isSecure=false&uicomponent=UISpacesManage&op=JoinSpace&leader="+requestContext.getRemoteUser()+"&space="+uiSpaceMember.space.getId();
    activeLink = headerMail + activeLink + footerMail;
    mailService.sendMessage("exoservice@gmail.com",email, "Invite to join space " + uiSpaceMember.space.getName(), activeLink);
    */
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_SENDING_CONFIRMATION_EMAIL, e);
    }
  }

  public void acceptInvitation(String spaceId, String userId) throws SpaceException {
    acceptInvitation(getSpaceById(spaceId), userId);
  }

  public void acceptInvitation(Space space, String userId) throws SpaceException {
    String[] invitedUser = space.getInvitedUsers();
    boolean check = false;
    if(invitedUser != null) {
      for(String user : invitedUser) {
        if(user.equals(userId)) {
          check = true;
          break;
        }
      }
    }
    if(!check) throw new SpaceException(SpaceException.Code.USER_NOT_INVITED);
    space.setInvitedUsers(removeItemFromArray(invitedUser, userId));
    saveSpace(space, false);
    addMember(space, userId);
  }

  public void addMember(Space space, String userId) throws SpaceException {
    OrganizationService orgService = getOrgService();

    try {
      UserHandler userHandler = orgService.getUserHandler();
      User user = userHandler.findUserByName(userId);
      MembershipType mbShipType = orgService.getMembershipTypeHandler().findMembershipType(MEMBER);
      MembershipHandler membershipHandler = orgService.getMembershipHandler();
      Group spaceGroup = orgService.getGroupHandler().findGroupById(space.getGroupId());
      membershipHandler.linkMembership(user, spaceGroup, mbShipType, true);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_USER, e);
    }
  }

  public void removeMember(Space space, String userId) throws SpaceException {
    OrganizationService orgService = getOrgService();
    UserHandler userHandler = orgService.getUserHandler();

    try {
      User user = userHandler.findUserByName(userId);
      MembershipHandler membershipHandler = orgService.getMembershipHandler();
      Membership memberShip = membershipHandler.findMembershipByUserGroupAndType(user.getUserName(), space.getGroupId(), MEMBER);
      if(memberShip == null)
        memberShip = membershipHandler.findMembershipByUserGroupAndType(user.getUserName(), space.getGroupId(), MANAGER);
      membershipHandler.removeMembership(memberShip.getId(), true);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_USER, e);
    }
  }

  public void denyInvitation(String spaceId, String userId) throws SpaceException {
    denyInvitation(getSpaceById(spaceId), userId);
  }

  public void denyInvitation(Space space, String userId) throws SpaceException {
    String[] invitedUsers = space.getInvitedUsers();
    if(isInvited(space, userId)) space.setInvitedUsers(removeItemFromArray(invitedUsers, userId));
    saveSpace(space, false);
  }

  public void revokeInvitation(String spaceId, String userId) throws SpaceException {
    revokeInvitation(getSpaceById(spaceId), userId);
  }

  public void revokeInvitation(Space space, String userId) throws SpaceException {
    denyInvitation(space, userId);
  }

  public void requestJoin(String spaceId, String userId) throws SpaceException {
    requestJoin(getSpaceById(spaceId), userId);
  }

  public void requestJoin(Space space, String userId) throws SpaceException {
    if(isInvited(space, userId)) {
      addMember(space, userId);
      String[] invitedUsers = space.getInvitedUsers();
      space.setInvitedUsers(removeItemFromArray(invitedUsers, userId));
      saveSpace(space, false);
      return;
    }
    String registration = space.getRegistration();
    if(registration.equals(Space.OPEN)) {
      addMember(space, userId);
    } else if (registration.equals(Space.VALIDATION)) {
      String[] pendingUsers = space.getPendingUsers();
      space.setPendingUsers(addItemToArray(pendingUsers, userId));
      saveSpace(space, false);
    } else {
      throw new SpaceException(SpaceException.Code.UNABLE_REQUEST_TO_JOIN);
    }
  }
  
  public void declineRequest(String spaceId, String userId) throws SpaceException {
    declineRequest(getSpaceById(spaceId), userId);
  }
  
  public void declineRequest(Space space, String userId) throws SpaceException {
    String[] pendingUsers = space.getPendingUsers();
    space.setPendingUsers(removeItemFromArray(pendingUsers, userId));
    saveSpace(space, false);
  }

  public void validateRequest(String spaceId, String userId) throws SpaceException {
    validateRequest(getSpaceById(spaceId), userId);
  }
  
  public void validateRequest(Space space, String userId) throws SpaceException {
    String[] pendingUsers = space.getPendingUsers();
    space.setPendingUsers(removeItemFromArray(pendingUsers, userId));
    saveSpace(space, false);
    addMember(space, userId);
  }

  @SuppressWarnings("unchecked")
  public List<String> getMembers(Space space) throws SpaceException {
    try {
      OrganizationService orgService = getOrgService();
      PageList usersPageList = orgService.getUserHandler().findUsersByGroup(space.getGroupId());

      List<User> users = usersPageList.getAll();

      List<String> usernames = new ArrayList<String>();

      for(User obj : users)
        usernames.add(obj.getUserName());
      return usernames;
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_RETRIEVING_MEMBER_LIST, e);
    }
  }

  public boolean isLeader(Space space, String userId) throws SpaceException {
    try {
      OrganizationService orgService = getOrgService();
      MembershipHandler memberShipHandler = orgService.getMembershipHandler();

      return(memberShipHandler.findMembershipByUserGroupAndType(userId, space.getGroupId(), MANAGER) != null);

    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_RETRIEVING_MEMBER_LIST, e); 
    }
  }

  public void setLeader(Space space, String userId, boolean status) throws SpaceException {
    try {
      OrganizationService orgService = getOrgService();

      UserHandler userHandler = orgService.getUserHandler();
      User user = userHandler.findUserByName(userId);
      MembershipHandler membershipHandler = orgService.getMembershipHandler();
      if(status) {
        Membership memberShipMember = membershipHandler.findMembershipByUserGroupAndType(user.getUserName(), space.getGroupId(), MEMBER);
        membershipHandler.removeMembership(memberShipMember.getId(), true);
        MembershipType mbshipTypeManager = orgService.getMembershipTypeHandler().findMembershipType(MANAGER);
        GroupHandler groupHandler = orgService.getGroupHandler();
        membershipHandler.linkMembership(user, groupHandler.findGroupById(space.getGroupId()), mbshipTypeManager, true);
      } else {
        Membership memberShip = membershipHandler.findMembershipByUserGroupAndType(user.getUserName(), space.getGroupId(), MANAGER);
        membershipHandler.removeMembership(memberShip.getId(), true);
        MembershipType mbShipTypeMember = orgService.getMembershipTypeHandler().findMembershipType(MEMBER);
        GroupHandler groupHandler = orgService.getGroupHandler();
        membershipHandler.linkMembership(user, groupHandler.findGroupById(space.getGroupId()), mbShipTypeMember, true);
      }
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_SETTING_LEADER_STATUS, e); 
    }
  }

  /**
   *
   * @param space
   * @param userId
   * @return true if the user is member or leader of the space.
   * @throws SpaceException
   */
  public boolean isMember(Space space, String userId) throws SpaceException {
    try {
      OrganizationService orgService = getOrgService();
      MembershipHandler memberShipHandler = orgService.getMembershipHandler();

      return(memberShipHandler.findMembershipsByUserAndGroup(userId, space.getGroupId()).size() > 0);

    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_RETRIEVING_MEMBER_LIST, e);
    }
  }

  public boolean isInvited(Space space, String userId) {
    String[] invitedUsers = space.getInvitedUsers();
    if(invitedUsers == null)
      return false;
    for(String user : invitedUsers) {
      if(user.equals(userId)) return true;
    }
    return false;
  }
  
  public boolean isPending(Space space, String userId) {
    String[] pendingUsers = space.getPendingUsers();
    if(pendingUsers == null)
      return false;
    for(String user : pendingUsers) {
      if(user.equals(userId)) return true;
    }
    return false;
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
  
  private String[] removeItemFromArray(String[] arrays, String str) {
    List<String> list = new ArrayList<String>();
    list.addAll(Arrays.asList(arrays));
    list.remove(str);
    if(list.size() > 0) return list.toArray(new String[list.size()]);
    else return null;
  }
  
  private String[] addItemToArray(String[] arrays, String str) {
    List<String> list = new ArrayList<String>();
    if(arrays != null && arrays.length > 0) {
      list.addAll(Arrays.asList(arrays));
      list.add(str);
      return list.toArray(new String[list.size()]);
    } else return new String[] {str};
  }
  
  private class SpaceComparator implements Comparator<Space> {
    public int compare(Space space1, Space space2) {
      return space1.getName().compareToIgnoreCase(space2.getName());
    }
  }
}