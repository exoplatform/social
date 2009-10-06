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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.portal.config.UserACL;
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

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * August 29, 2008          
 */
public class SpaceServiceImpl implements SpaceService {
  final static public String SPACE_PARENT = "/spaces";
  final static public String MEMBER = "member";
  final static public String MANAGER = "manager";
  
  private final String propertiesParamName = "space";
  private final String homeNodeAppProperty = "homeNodeApp";
  private final String appsProperty = "apps";
  private String homeNodeApp;
  private String[] apps;
  private JCRStorage storage;
  private OrganizationService orgService = null;
  private UserACL userACL = null;
  private Map<String, SpaceApplicationHandler> spaceApplicationHandlers = null;
  /**
   * SpaceServiceImpl constructor
   * Initialize <tt>org.exoplatform.social.space.impl.JCRStorage</tt>
   * @param params
   * @param nodeHierarchyCreator
   * @throws Exception
   */
  public SpaceServiceImpl(InitParams params, NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    storage = new JCRStorage(nodeHierarchyCreator);
    PropertiesParam properties = params.getPropertiesParam(propertiesParamName);
    homeNodeApp = properties.getProperty(homeNodeAppProperty);
    apps = properties.getProperty(appsProperty).split(",");
    if(properties == null) throw new Exception("the 'space' properties parameter is expected.");
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Space> getAllSpaces() throws SpaceException {
    try {
      List<Space> spaces = storage.getAllSpaces();
      Collections.sort(spaces, new SpaceComparator());
      return spaces;
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public Space getSpaceById(String id) throws SpaceException {
    try {
      return storage.getSpaceById(id);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public Space getSpaceByUrl(String url) throws SpaceException {
    try {
      return storage.getSpaceByUrl(url);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Space> getSpaces(String userId) throws SpaceException {
    
    List<Space> userSpaces = getAllSpaces();
    Iterator<Space> itr = userSpaces.iterator();
    while(itr.hasNext()) {
      Space space = itr.next();
      if(!isMember(space, userId)){
        itr.remove();
      }
    }
    return userSpaces;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Space> getAccessibleSpaces(String userId) throws SpaceException {
    List<Space> accessiableSpaces = getAllSpaces();
    Iterator<Space> itr = accessiableSpaces.iterator();
    while (itr.hasNext()) {
      Space space = itr.next();
      if (!hasAccessPermission(space, userId)) itr.remove();
    }
    return accessiableSpaces;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Space> getEditableSpaces(String userId) throws SpaceException {
    List<Space> editableSpaces = getAllSpaces();
    Iterator<Space> itr = editableSpaces.iterator();
    while (itr.hasNext()) {
      Space space = itr.next();
      if (!hasEditPermission(space, userId)) itr.remove();
    }
    return editableSpaces;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Space> getInvitedSpaces(String userId) throws SpaceException {
    List<Space> spaces = getAllSpaces();
    Iterator<Space> itr = spaces.iterator();
    while(itr.hasNext()) {
      Space space = itr.next();
      if (!isInvited(space, userId)) {
        itr.remove();
      }
    }
    return spaces;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Space> getPublicSpaces(String userId) throws SpaceException {
    List<Space> spaces = getAllSpaces();
    Iterator<Space> itr = spaces.iterator();
    while(itr.hasNext()) {
      Space space = itr.next();
      if(space.getVisibility().equals(Space.HIDDEN) || isMember(space, userId) || isInvited(space, userId) || isPending(space, userId)) {
        itr.remove();
      }
    }
    return spaces;    
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Space> getPendingSpaces(String userId) throws SpaceException {
    List<Space> spaces = getAllSpaces();
    Iterator<Space> itr = spaces.iterator();
    while (itr.hasNext()) {
      Space space = itr.next();
      if (!isPending(space, userId)) {
        itr.remove();
      }
    }
    return spaces;
  }
  
  /**
   * {@inheritDoc}
   */
  public Space createSpace(Space space, String creator) throws SpaceException {
    return createSpace(space, creator, null);
  }
  
  /**
   * {@inheritDoc}
   */
  public Space createSpace(Space space, String creator, String groupId) throws SpaceException {
    if (groupId == null) { // Create new space by creating new group
      groupId = SpaceUtils.createGroup(space.getName(), creator);
    } else { // Create new space from an existing group
      if (SpaceUtils.isSpaceNameExisted(space.getName())) {
        throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
      }
      SpaceUtils.addCreatorToGroup(creator, groupId);
    }
    space.setGroupId(groupId);
    space.setUrl(SpaceUtils.cleanString(space.getName()));
    return space;
  }
  
  /**
   * {@inheritDoc}
   */
  public void saveSpace(Space space, boolean isNew) throws SpaceException {
    try {
      storage.saveSpace(space, isNew);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void deleteSpace(Space space) throws SpaceException {
    try {
      SpaceApplicationHandler appHander = getSpaceApplicationHandler(space);
      appHander.removeApplications(space);
      appHander.deInitApp(space);
      storage.deleteSpace(space.getId());
      SpaceUtils.removeGroup(space);
    } catch(Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_DELETE_SPACE, e);
    }
  }
  
  public void deleteSpace(String spaceId) throws SpaceException {
    deleteSpace(getSpaceById(spaceId));
  }
  
  /**
   * {@inheritDoc}
   */
  public void initApp(Space space) throws SpaceException {
    SpaceApplicationHandler spaceAppHandler = getSpaceApplicationHandler(space);
    spaceAppHandler.initApp(space, homeNodeApp, apps);
  }
  
  /**
   * {@inheritDoc}
   */
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
  
  /**
   * {@inheritDoc}
   */
  public void addMember(String spaceId, String userId) throws SpaceException {
    addMember(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public void removeMember(Space space, String userId) throws SpaceException {
    try {
      OrganizationService orgService = getOrgService();

      String groupID = space.getGroupId();
      MembershipHandler memberShipHandler = orgService.getMembershipHandler();
      Collection<Membership> memberships = memberShipHandler.findMembershipsByUserAndGroup(userId, groupID);
      if (memberships.size() == 0) {
        throw new SpaceException(SpaceException.Code.USER_NOT_MEMBER);
      }
      /*
      if (isOnlyLeader(space, userId)) {
        throw new SpaceException(SpaceException.Code.USER_ONLY_LEADER);
      }
      */

      Iterator<Membership> itr = memberships.iterator();
      while(itr.hasNext()) {
        Membership mbShip = itr.next();
        memberShipHandler.removeMembership(mbShip.getId(), true);
      }
    } catch (Exception e) {
      if (e instanceof SpaceException) {
        throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_USER, e);
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void removeMember(String spaceId, String userId) throws SpaceException {
    removeMember(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  private Space addPending(Space space, String userId) throws SpaceException {
    space.setPendingUsers(addItemToArray(space.getPendingUsers(), userId));
    return space;
  }
  
  /**
   * {@inheritDoc}
   */
  private Space removePending(Space space, String userId) throws SpaceException {
    space.setPendingUsers(removeItemFromArray(space.getPendingUsers(), userId));
    return space;
  }
  
  /**
   * {@inheritDoc}
   */
  private Space addInvited(Space space, String userId) throws SpaceException {
    space.setInvitedUsers(addItemToArray(space.getInvitedUsers(), userId));
    return space;
  }
  
  /**
   * {@inheritDoc}
   */
  private Space removeInvited(Space space, String userId) throws SpaceException {
    space.setInvitedUsers(removeItemFromArray(space.getInvitedUsers(), userId));
    return space;
  }
  
  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  public List<String> getMembers(Space space) throws SpaceException {
    try {
      OrganizationService orgService = getOrgService();
      //TODO tung.dang: temp change for using from gatein - have to roll back
      //--------------------------------------------------------------------
      GroupHandler groupHandler = orgService.getGroupHandler();
      Group group = groupHandler.findGroupById(space.getGroupId());
      MembershipHandler memberShipHandler = orgService.getMembershipHandler();
      Collection list = memberShipHandler.findMembershipsByGroup(group);
      List<String> usernames = new ArrayList<String>();
      for (Object obj : list) {
        Membership membership = (Membership) obj;
        usernames.add(membership.getUserName());
      }
//      PageList usersPageList = orgService.getUserHandler().findUsersByGroup(space.getGroupId());
//      List<User> users = usersPageList.getAll();
      //----------------------------------------------------------------------
      return usernames;
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_RETRIEVING_MEMBER_LIST, e);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public List<String> getMembers(String spaceId) throws SpaceException {
    return getMembers(getSpaceById(spaceId));
  }
  
  /**
   * {@inheritDoc}
   */
  public void setLeader(Space space, String userId, boolean isLeader) throws SpaceException {
    try {
      OrganizationService orgService = getOrgService();
      UserHandler userHandler = orgService.getUserHandler();
      User user = userHandler.findUserByName(userId);
      MembershipHandler membershipHandler = orgService.getMembershipHandler();
      if(isLeader) {
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
   * {@inheritDoc}
   */
  public void setLeader(String spaceId, String userId, boolean isLeader) throws SpaceException {
    setLeader(getSpaceById(spaceId), userId, isLeader);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isLeader(Space space, String userId) throws SpaceException {
    try {
      OrganizationService orgService = getOrgService();
      MembershipHandler memberShipHandler = orgService.getMembershipHandler();

      return(memberShipHandler.findMembershipByUserGroupAndType(userId, space.getGroupId(), MANAGER) != null);

    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_RETRIEVING_MEMBER_LIST, e); 
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isLeader(String spaceId, String userId) throws SpaceException {
    return isLeader(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isOnlyLeader(Space space, String userId) throws SpaceException {
    boolean isOnlyLeader = true;
    if (!isLeader(space, userId)) {
      return false;
    }
    List<String> members = getMembers(space);
    members.remove(userId);
    Iterator<String> itr = members.iterator();
    while (itr.hasNext()) {
      userId = itr.next();
      if (isLeader(space, userId)) {
        return false;
      }
    }
    return isOnlyLeader;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isOnlyLeader(String spaceId, String userId) throws SpaceException {
    return isOnlyLeader(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
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
  
  /**
   * {@inheritDoc}
   */
  public boolean isMember(String spaceId, String userId) throws SpaceException {
    return isMember(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean hasAccessPermission(Space space, String userId) throws SpaceException {
    if (userId.equals(getUserACL().getSuperUser())) return true;
    if (isMember(space, userId)) return true;    
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean hasAccessPermission(String spaceId, String userId) throws SpaceException {
    return hasAccessPermission(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean hasEditPermission(Space space, String userId) throws SpaceException {
    if (userId.equals(getUserACL().getSuperUser())) return true;
    if (isLeader(space, userId)) return true;
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean hasEditPermission(String spaceId, String userId) throws SpaceException {
    return hasEditPermission(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isInvited(Space space, String userId) {
    String[] invitedUsers = space.getInvitedUsers();
    if(invitedUsers == null)
      return false;
    for(String user : invitedUsers) {
      if(user.equals(userId)) return true;
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isInvited(String spaceId, String userId) throws SpaceException {
    return isInvited(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isPending(Space space, String userId) {
    String[] pendingUsers = space.getPendingUsers();
    if(pendingUsers == null)
      return false;
    for(String user : pendingUsers) {
      if(user.equals(userId)) return true;
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isPending(String spaceId, String userId) throws SpaceException {
    return isPending(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void installApplication(String spaceId, String appId) throws SpaceException {
    installApplication(getSpaceById(spaceId), appId);  
  }
  
  /**
   * {@inheritDoc}
   */
  public void installApplication(Space space, String appId) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.installApplication(space, appId);
    setApp(space, appId, Space.INSTALL_STATUS);
  }
  
  /**
   * {@inheritDoc}
   */
  public void activateApplication(Space space, String appId) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.activateApplication(space, appId);
    setApp(space, appId, Space.ACTIVE_STATUS);
  }
  
  /**
   * {@inheritDoc}
   */
  public void activateApplication(String spaceId, String appId) throws SpaceException {
    activateApplication(getSpaceById(spaceId), appId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void deactivateApplication(Space space, String appId) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.deactiveApplication(space, appId);
    setApp(space, appId, Space.DEACTIVE_STATUS);
  }
  
  /**
   * {@inheritDoc}
   */
  public void deactivateApplication(String spaceId, String appId) throws SpaceException {
    deactivateApplication(getSpaceById(spaceId), appId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void removeApplication(Space space, String appId) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.removeApplication(space, appId);
    removeApp(space, appId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void removeApplication(String spaceId, String appId) throws SpaceException {
    removeApplication(getSpaceById(spaceId), appId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void requestJoin(String spaceId, String userId) throws SpaceException {
    requestJoin(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void requestJoin(Space space, String userId) throws SpaceException {
    if(isInvited(space, userId)) {
      addMember(space, userId);
      space = removeInvited(space, userId);
      saveSpace(space, false);
      return;
    }
    String registration = space.getRegistration();
    String visibility = space.getVisibility();
    if(visibility.equals(Space.HIDDEN)) {
      throw new SpaceException(SpaceException.Code.UNABLE_REQUEST_TO_JOIN_HIDDEN);
    }
    if(registration.equals(Space.OPEN)) {
      addMember(space, userId);
    } else if (registration.equals(Space.VALIDATION)) {
      space = addPending(space, userId);
      saveSpace(space, false);
    } else {
      throw new SpaceException(SpaceException.Code.UNABLE_REQUEST_TO_JOIN);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void revokeRequestJoin(Space space, String userId) throws SpaceException {
    if (isPending(space, userId)) {
      space = removePending(space, userId);
      saveSpace(space, false);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void revokeRequestJoin(String spaceId, String userId) throws SpaceException {
    revokeRequestJoin(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void inviteMember(Space space, String userId) throws SpaceException {
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
    } else if (isMember(space, userId) && !userId.equals(getUserACL().getSuperUser())) {
      throw new SpaceException(SpaceException.Code.USER_ALREADY_MEMBER);
    }
    if(isPending(space, userId)) {
      space = removePending(space, userId);
      addMember(space, userId);
    } else {
      space = addInvited(space, userId);
    }
    
    saveSpace(space, false);
    

    // we'll sent a email to invite user
    //TODO: This should be done in a Service in a separated thread
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

  /**
   * {@inheritDoc}
   */
  public void inviteMember(String spaceId, String userId) throws SpaceException {
    inviteMember(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void revokeInvitation(Space space, String userId) throws SpaceException {
    if (isInvited(space, userId)) {
      space = removeInvited(space, userId);
      saveSpace(space, false);
    } else {
      throw new SpaceException(SpaceException.Code.USER_NOT_INVITED);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void revokeInvitation(String spaceId, String userId) throws SpaceException {
    revokeInvitation(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void acceptInvitation(Space space, String userId) throws SpaceException {
    if (isInvited(space, userId)) {
      space = removeInvited(space, userId);
      saveSpace(space, false);
      addMember(space, userId);
    } else {
      throw new SpaceException(SpaceException.Code.USER_NOT_INVITED);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void acceptInvitation(String spaceId, String userId) throws SpaceException {
    acceptInvitation(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void denyInvitation(String spaceId, String userId) throws SpaceException {
    denyInvitation(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void denyInvitation(Space space, String userId) throws SpaceException {
    if (isInvited(space, userId)) {
      space = removeInvited(space, userId);
      saveSpace(space, false);
    } else {
      throw new SpaceException(SpaceException.Code.USER_NOT_INVITED);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void validateRequest(Space space, String userId) throws SpaceException {
    space = removePending(space, userId);
    saveSpace(space, false);
    addMember(space, userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void validateRequest(String spaceId, String userId) throws SpaceException {
    validateRequest(getSpaceById(spaceId), userId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void declineRequest(Space space, String userId) throws SpaceException {
    space = removePending(space, userId);
    saveSpace(space, false);
  }
  
  /**
   * {@inheritDoc}
   */
  public void declineRequest(String spaceId, String userId) throws SpaceException {
    declineRequest(getSpaceById(spaceId), userId);
  }
    
  /**
   * Get OrganizationService
   * 
   * @return organizationService
   */
  private OrganizationService getOrgService() {
    if (orgService == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    }
    return orgService;
  }
  
  /**
   * Get UserACL
   * 
   * @return userACL
   */
  private UserACL getUserACL() {
    if (userACL == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      return (UserACL) container.getComponentInstanceOfType(UserACL.class);
    }
    return userACL;
  }
  
  /**
   * Get space application handlers
   * @return
   */
  @SuppressWarnings("unchecked")
  private Map<String, SpaceApplicationHandler> getSpaceApplicationHandlers() {
    if(this.spaceApplicationHandlers == null) {
      this.spaceApplicationHandlers = new HashMap();

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      SpaceApplicationHandler appHandler = (DefaultSpaceApplicationHandler) container.getComponentInstanceOfType(DefaultSpaceApplicationHandler.class);
      this.spaceApplicationHandlers.put(appHandler.getName(), appHandler);
    }
    return this.spaceApplicationHandlers;
  }
  
  /**
   * Get space application handler
   * @param space
   * @return
   * @throws SpaceException
   */
  private SpaceApplicationHandler getSpaceApplicationHandler(Space space) throws SpaceException {
    SpaceApplicationHandler appHandler = getSpaceApplicationHandlers().get(space.getType());
    if (appHandler == null)
      throw new SpaceException(SpaceException.Code.UNKNOWN_SPACE_TYPE);
    return appHandler;
  }
  
  /**
   * Set application status to a space
   * @param space
   * @param appId
   * @param status
   * @throws SpaceException
   */
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
  
  /**
   * Remove application from a space
   * @param space
   * @param appId
   * @throws SpaceException
   */
  private void removeApp(Space space, String appId) throws SpaceException {
    String apps = space.getApp();
    String oldStatus = apps.substring(apps.indexOf(appId));
    if(oldStatus.indexOf(",") != -1) oldStatus = oldStatus.substring(0,oldStatus.indexOf(","));
    apps = apps.replaceFirst(oldStatus, "");
    if(apps.equals("")) apps = null;
    space.setApp(apps);
    saveSpace(space, false);
  }
  
  /**
   * Remove an item from an array
   * @param arrays
   * @param str
   * @return new array
   */
  private String[] removeItemFromArray(String[] arrays, String str) {
    List<String> list = new ArrayList<String>();
    list.addAll(Arrays.asList(arrays));
    list.remove(str);
    if(list.size() > 0) return list.toArray(new String[list.size()]);
    else return null;
  }
  
  /**
   * Add an item to an array
   * @param arrays
   * @param str
   * @return new array
   */
  private String[] addItemToArray(String[] arrays, String str) {
    List<String> list = new ArrayList<String>();
    if(arrays != null && arrays.length > 0) {
      list.addAll(Arrays.asList(arrays));
      list.add(str);
      return list.toArray(new String[list.size()]);
    } else return new String[] {str};
  }
  

  private class SpaceComparator implements Comparator<Space> {
    /**
     * Compare 2 spaces by name
     * @return
     */
    public int compare(Space space1, Space space2) {
      return space1.getName().compareToIgnoreCase(space2.getName());
    }
  }
  
}