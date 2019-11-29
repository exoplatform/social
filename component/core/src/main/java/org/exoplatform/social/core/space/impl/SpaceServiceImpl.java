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
package org.exoplatform.social.core.space.impl;

import java.io.InputStream;
import java.util.*;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.application.PortletPreferenceRequiredPlugin;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.space.*;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.model.Space.UpdatedField;
import org.exoplatform.social.core.space.spi.SpaceApplicationHandler;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.space.spi.SpaceTemplateService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;

/**
 * {@link org.exoplatform.social.core.space.spi.SpaceService} implementation.
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since  August 29, 2008
 */
public class SpaceServiceImpl implements SpaceService {

  private static final Log                     LOG                   = ExoLogger.getLogger(SpaceServiceImpl.class.getName());

  public static final String                   MEMBER                   = "member";

  public static final String                   MANAGER                  = "manager";

  private IdentityRegistry                     identityRegistry;

  private SpaceStorage                         spaceStorage;

  private IdentityManager                      identityManager;

  private IdentityStorage                      identityStorage;

  private OrganizationService                  orgService               = null;

  private UserACL                              userACL                  = null;

  private SpaceLifecycle                       spaceLifeCycle           = new SpaceLifecycle();

  List<String>                                 portletPrefsRequired = null;

  /** The offset for list access loading. */
  private static final int                   OFFSET = 0;

  /** The limit for list access loading. */
  private static final int                   LIMIT = 200;

  private WebNotificationService webNotificationService;

  private SpacesAdministrationService spacesAdministrationService;

  private SpaceTemplateService spaceTemplateService;

  private ConfigurationManager configurationManager;

  public SpaceServiceImpl(SpaceStorage spaceStorage,
                          IdentityStorage identityStorage,
                          IdentityManager identityManager,
                          UserACL userACL,
                          ConfigurationManager configurationManager,
                          IdentityRegistry identityRegistry,
                          WebNotificationService webNotificationService,
                          SpacesAdministrationService spacesAdministrationService,
                          SpaceTemplateService spaceTemplateService) {
    this.spaceStorage = spaceStorage;
    this.identityManager = identityManager;
    this.identityStorage = identityStorage;
    this.identityRegistry = identityRegistry;
    this.userACL = userACL;
    this.webNotificationService = webNotificationService;
    this.spacesAdministrationService = spacesAdministrationService;
    this.spaceTemplateService = spaceTemplateService;
    this.configurationManager = configurationManager;
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getAllSpaces() throws SpaceException {
    try {
      return Arrays.asList(this.getAllSpacesWithListAccess().load(OFFSET, LIMIT));
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Space> getAllSpacesWithListAccess() {
    return new SpaceListAccess(this.spaceStorage, SpaceListAccess.Type.ALL);
  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByDisplayName(String spaceDisplayName) {
    return spaceStorage.getSpaceByDisplayName(spaceDisplayName);
  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByName(String spaceName) {
    return getSpaceByPrettyName(spaceName);
  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByPrettyName(String spacePrettyName) {
    return spaceStorage.getSpaceByPrettyName(spacePrettyName);
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getSpacesByFirstCharacterOfName(String firstCharacterOfName) throws SpaceException {
    try {
      return Arrays.asList(this.getAllSpacesByFilter(new SpaceFilter(firstCharacterOfName.charAt(0))).load(OFFSET, LIMIT));
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getSpacesBySearchCondition(String searchCondition) throws SpaceException {
    try {
      return Arrays.asList(this.getAllSpacesByFilter(new SpaceFilter(searchCondition)).load(OFFSET, LIMIT));
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByGroupId(String groupId) {
    return spaceStorage.getSpaceByGroupId(groupId);
  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceById(String id) {
    return spaceStorage.getSpaceById(id);
  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByUrl(String url) {
    return spaceStorage.getSpaceByUrl(url);
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getSpaces(String userId) throws SpaceException {
    try {
      return Arrays.asList(this.getMemberSpaces(userId).load(OFFSET, LIMIT));
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getAccessibleSpaces(String userId) throws SpaceException {
    try {
      return Arrays.asList(this.getAccessibleSpacesWithListAccess(userId).load(OFFSET, LIMIT));
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public SpaceListAccess getAccessibleSpacesWithListAccess(String userId) {
    return new SpaceListAccess(this.spaceStorage, userId, SpaceListAccess.Type.ACCESSIBLE);
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getVisibleSpaces(String userId, SpaceFilter spaceFilter) throws SpaceException {
    try {
      return Arrays.asList(this.getVisibleSpacesWithListAccess(userId, spaceFilter).load(OFFSET, LIMIT));
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public SpaceListAccess getVisibleSpacesWithListAccess(String userId, SpaceFilter spaceFilter) {
    if (isSuperManager(userId)) {
      if (spaceFilter == null)
        return new SpaceListAccess(this.spaceStorage, userId, spaceFilter, SpaceListAccess.Type.ALL);
      else
        return new SpaceListAccess(this.spaceStorage, userId, spaceFilter, SpaceListAccess.Type.ALL_FILTER);
    } else {
      return new SpaceListAccess(this.spaceStorage, userId, spaceFilter,SpaceListAccess.Type.VISIBLE);
    }
  }

  /**
   * {@inheritDoc}
   */
  public SpaceListAccess getUnifiedSearchSpacesWithListAccess(String userId, SpaceFilter spaceFilter) {
    return new SpaceListAccess(this.spaceStorage, userId, spaceFilter,SpaceListAccess.Type.UNIFIED_SEARCH);
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getEditableSpaces(String userId)  throws SpaceException {
    try {
      return Arrays.asList(this.getSettingableSpaces(userId).load(OFFSET, LIMIT));
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getInvitedSpaces(String userId) throws SpaceException {
    try {
      return Arrays.asList(this.getInvitedSpacesWithListAccess(userId).load(OFFSET, LIMIT));
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getPublicSpaces(String userId) throws SpaceException {
    try {
      return Arrays.asList(this.getPublicSpacesWithListAccess(userId).load(OFFSET, LIMIT));
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public SpaceListAccess getPublicSpacesWithListAccess(String userId) {
    if (isSuperManager(userId)) {
      return new SpaceListAccess(this.spaceStorage, SpaceListAccess.Type.PUBLIC_SUPER_USER);
    } else {
      return new SpaceListAccess(this.spaceStorage, userId, SpaceListAccess.Type.PUBLIC);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getPendingSpaces(String userId) throws SpaceException {
    try {
      return Arrays.asList(this.getPendingSpacesWithListAccess(userId).load(OFFSET, LIMIT));
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.ERROR_DATASTORE, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public SpaceListAccess getPendingSpacesWithListAccess(String userId) {
    return new SpaceListAccess(this.spaceStorage, userId, SpaceListAccess.Type.PENDING);
  }

  /**
   * {@inheritDoc}
   */
  public Space createSpace(Space space, String creator) {
    return createSpace(space, creator, (String) null);
  }

  /**
   * {@inheritDoc}
   */
  @Deprecated
  public Space createSpace(Space space, String creator, String invitedGroupId) {
    List<Identity> invitedIdentities = new ArrayList<Identity>();
    if (invitedGroupId != null) { // Invites users in group to join the new created space
      // Gets identities of users in group and then invites them to join into space.
      OrganizationService org = getOrgService();
      try {
        ListAccess<User> groupMembersAccess = org.getUserHandler().findUsersByGroupId(invitedGroupId);
        User [] users = groupMembersAccess.load(0, groupMembersAccess.getSize());
        for (User user : users) {
          String userId = user.getUserName();
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
          if (identity != null) {
            invitedIdentities.add(identity);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Failed to invite users from group " + invitedGroupId, e);
      }
    }
    return createSpace(space, creator, invitedIdentities);
  }

  /**
   * {@inheritDoc}
   */
  public Space createSpace(Space space, String creator, List<Identity> identitiesToInvite) {

    if (space.getDisplayName().length() > LIMIT) {
      throw new RuntimeException("Error while creating the space " + space.getDisplayName() + ": space name cannot exceed 200 characters");
    }

    if (!SpaceUtils.isValidSpaceName(space.getDisplayName())) {
      throw new RuntimeException("Error while creating the space " + space.getDisplayName()+ ": space name can only contain letters, digits or space characters only");
    }

    if(!spacesAdministrationService.canCreateSpace(creator)) {
      throw new RuntimeException("User does not have permissions to create a space.");
    }

    // Add creator as a manager and a member to this space
    String[] managers = space.getManagers();
    String[] members = space.getMembers();
    managers = (String[]) ArrayUtils.add(managers,creator);
    members = (String[]) ArrayUtils.add(members,creator);
    space.setManagers(managers);
    space.setMembers(members);

    // Creates new space by creating new group
    String groupId = null;
    try {
      groupId = SpaceUtils.createGroup(space.getDisplayName(), space.getPrettyName(), creator);
    } catch (SpaceException e) {
      throw new RuntimeException("Error while creating group for space " + space.getPrettyName(), e);
    }

    List<String> inviteds = new ArrayList<String>();
    if (identitiesToInvite != null) {
      Set<String> userIds = getUsersToInvite(identitiesToInvite);
      userIds.remove(creator);
      for (String userId : userIds) {
        String[] invitedUsers = space.getInvitedUsers();
        if (isSuperManager(userId)) {
          members = space.getMembers();
          if (!ArrayUtils.contains(members, userId)) {
            members = (String[]) ArrayUtils.add(members, userId);
            space.setMembers(members);
          }
        } else if (!ArrayUtils.contains(invitedUsers, userId)) {
          invitedUsers = (String[]) ArrayUtils.add(invitedUsers, userId);
          inviteds.add(userId);
          space.setInvitedUsers(invitedUsers);
        }
      }
    }

    String prettyName = groupId.split("/")[2];

    if (!prettyName.equals(space.getPrettyName())) {
      //work around for SOC-2366
      space.setPrettyName(groupId.split("/")[2]);
    }


    space.setGroupId(groupId);
    space.setUrl(space.getPrettyName());

    try {
      String spaceType = space.getTemplate();
      SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplateByName(spaceType);
      if (spaceTemplate == null) {
        LOG.warn("could not find space template of type {}, will use Default template", spaceType);
        String defaultTemplate = spaceTemplateService.getDefaultSpaceTemplate();
        spaceTemplate = spaceTemplateService.getSpaceTemplateByName(defaultTemplate);
        space.setTemplate(defaultTemplate);
      }
      String bannerPath = spaceTemplate.getBannerPath();
      if (StringUtils.isNotBlank(bannerPath)) {
        try {
          InputStream bannerStream = configurationManager.getInputStream(bannerPath);
          if (bannerStream != null) {
            BannerAttachment bannerAttachment = new BannerAttachment(null, "banner", "png", bannerStream, null, System.currentTimeMillis());
            space.setBannerAttachment(bannerAttachment);
          }
        } catch (Exception e) {
          LOG.warn("No file found for space banner at path {}", bannerPath);
        }
      }
      SpaceApplicationHandler applicationHandler = getSpaceApplicationHandler(space);
      spaceTemplateService.initSpaceApplications(space, applicationHandler);
    } catch (Exception e) {
      throw new RuntimeException("Failed to init apps for space " + space.getPrettyName(), e);
    }

    saveSpace(space, true);
    spaceLifeCycle.spaceCreated(space, creator);

    //process invited user list
    for (String invited : inviteds) {
      spaceLifeCycle.addInvitedUser(space, invited);
    }
    updateSpaceBanner(space);
    return space;
  }

  private Set<String> getUsersToInvite(List<Identity> identities) {
    Set<String> invitedUserIds = new HashSet<>();
    for (Identity identity : identities) {
      String providerId = identity.getProviderId();
      String remoteId = identity.getRemoteId();
      // If it's a space
      if (SpaceIdentityProvider.NAME.equals(providerId)) {
        Space space = getSpaceByPrettyName(remoteId);
        if (space != null) {
          String[] users = space.getMembers();
          invitedUserIds.addAll(Arrays.asList(users));
        }
      } else { // Otherwise, it's an user
        invitedUserIds.add(remoteId);
      }
    }
    return invitedUserIds;
  }

  /**
   * {@inheritDoc}
   */
  public void saveSpace(Space space, boolean isNew) {
    Space oldSpace = getSpaceById(space.getId());
    spaceStorage.saveSpace(space, isNew);
    if (!isNew) {
      if (!oldSpace.getVisibility().equals(space.getVisibility())) {
        spaceLifeCycle.spaceAccessEdited(space, space.getEditor());
      }

      String oldRegistration = oldSpace.getRegistration();
      String registration = space.getRegistration();
      if ((oldRegistration == null && registration != null)
          || (oldRegistration != null && !oldRegistration.equals(registration))) {
        spaceLifeCycle.spaceRegistrationEdited(space, space.getEditor());
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void renameSpace(Space space, String newDisplayName) {
    spaceStorage.renameSpace(space, newDisplayName);
    spaceLifeCycle.spaceRenamed(space, space.getEditor());
  }

  /**
   * {@inheritDoc}
   */
  public void renameSpace(String remoteId, Space space, String newDisplayName) {

    if (remoteId != null &&
        isSuperManager(remoteId) &&
        isMember(space, remoteId) == false) {

      spaceStorage.renameSpace(remoteId, space, newDisplayName);

    } else {

      spaceStorage.renameSpace(space, newDisplayName);
    }

    //
    spaceLifeCycle.spaceRenamed(space, space.getEditor());
  }

  /**
   * {@inheritDoc}
   */
  public void deleteSpace(Space space) {
    try {
      Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());

      if (spaceIdentity != null) {
        identityStorage.hardDeleteIdentity(spaceIdentity);
      }

      // remove memberships of users with deleted space.
      SpaceUtils.removeMembershipFromGroup(space);

      spaceStorage.deleteSpace(space.getId());

      OrganizationService orgService = getOrgService();
      UserACL acl = getUserACL();
      GroupHandler groupHandler = orgService.getGroupHandler();
      Group deletedGroup = groupHandler.findGroupById(space.getGroupId());
      List<String> mandatories = acl.getMandatoryGroups();
      if (deletedGroup != null) {
        if (!isMandatory(groupHandler, deletedGroup, mandatories)) {
          SpaceUtils.removeGroup(space);
        }
      } else {
        LOG.warn("deletedGroup is null");
      }

      //remove pages and group navigation of space
      SpaceUtils.removePagesAndGroupNavigation(space);

    } catch (Exception e) {
      LOG.error("Unable delete space: {}. Cause: {}", space.getPrettyName(), e.getMessage());
    }
    spaceLifeCycle.spaceRemoved(space, null);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteSpace(String spaceId) {
    deleteSpace(getSpaceById(spaceId));
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated Uses {@link #initApps(Space)} instead.
   */
  public void initApp(Space space) throws SpaceException {
    LOG.warn("Does nothing, just for compatible. It will be removed at 1.3.x");
    return;
  }

  /**
   * {@inheritDoc}
   */
  public void initApps(Space space) throws SpaceException {
    LOG.warn("Does nothing, just for compatible. It will be removed at 1.3.x");
    return;
  }

  /**
   * {@inheritDoc}
   */
  public void deInitApps(Space space) throws SpaceException {
    LOG.warn("Does nothing, just for compatible. It will be removed at 1.3.x");
    return;
  }

  /**
   * {@inheritDoc}
   */
  public void addMember(Space space, String userId) {
    String[] members = space.getMembers();
    space = this.removeInvited(space, userId);
    space = this.removePending(space, userId);
    if (!ArrayUtils.contains(members, userId)) {
      members = (String[]) ArrayUtils.add(members, userId);
      space.setMembers(members);
      this.updateSpace(space);
      SpaceUtils.addUserToGroupWithMemberMembership(userId, space.getGroupId());
      spaceLifeCycle.memberJoined(space, userId);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addMember(String spaceId, String userId) {
    addMember(getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public void removeMember(Space space, String userId) {
    Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
    if (spaceIdentity != null && spaceIdentity.isDeleted()) {
      return;
    }
    String[] members = space.getMembers();
    if (ArrayUtils.contains(members, userId)) {
      members = (String[]) ArrayUtils.removeElement(members, userId);
      space.setMembers(members);
      this.updateSpace(space);
      SpaceUtils.removeUserFromGroupWithMemberMembership(userId, space.getGroupId());
      spaceLifeCycle.memberLeft(space, userId);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void removeMember(String spaceId, String userId) {
    removeMember(getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  private Space addPending(Space space, String userId) {
    String[] pendingUsers = space.getPendingUsers();
    if (!ArrayUtils.contains(pendingUsers, userId)) {
      pendingUsers = (String[]) ArrayUtils.add(pendingUsers, userId);
      space.setPendingUsers(pendingUsers);
    }
    return space;
  }

  /**
   * {@inheritDoc}
   */
  private Space removePending(Space space, String userId) {
    String[] pendingUsers = space.getPendingUsers();
    if (ArrayUtils.contains(pendingUsers, userId)) {
      pendingUsers = (String[]) ArrayUtils.removeElement(pendingUsers, userId);
      space.setPendingUsers(pendingUsers);
    }
    return space;
  }

  /**
   * {@inheritDoc}
   */
  private Space addInvited(Space space, String userId) {
    String[] invitedUsers = space.getInvitedUsers();
    if (!ArrayUtils.contains(invitedUsers, userId)) {
      invitedUsers = (String[]) ArrayUtils.add(invitedUsers, userId);
      space.setInvitedUsers(invitedUsers);
    }
    return space;
  }

  /**
   * {@inheritDoc}
   */
  private Space removeInvited(Space space, String userId) {
    String[] invitedUsers = space.getInvitedUsers();
    if (ArrayUtils.contains(invitedUsers, userId)) {
      invitedUsers = (String[]) ArrayUtils.removeElement(invitedUsers, userId);
      space.setInvitedUsers(invitedUsers);
    }
    return space;
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getMembers(Space space) {
    if (space.getMembers() != null) {
      return Arrays.asList(space.getMembers());
    }
    return new ArrayList<String> ();
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getMembers(String spaceId) {
    return getMembers(getSpaceById(spaceId));
  }

  /**
   * {@inheritDoc}
   * If isLeader == true, that user will be assigned "manager" membership and the "member" memberhship will be removed.
   * Otherwise, that user will be assigned "member" membership and the "manager" membership will be removed.
   * However, if that user is the only manager, that user is not allowed to be removed from the manager membership.
   */
  public void setLeader(Space space, String userId, boolean isLeader) {
    this.setManager(space, userId, isLeader);
  }

  /**
   * {@inheritDoc}
   *
   * If isLeader == true, that user will be assigned "manager" membership and the "member" membership will be removed.
   * Otherwise, that user will be assigned "member" membership and the "manager" membership will be removed.
   */
  public void setLeader(String spaceId, String userId, boolean isLeader) {
    this.setManager(this.getSpaceById(spaceId), userId, isLeader);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isLeader(Space space, String userId) {
    return this.isManager(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isLeader(String spaceId, String userId) {
    return this.isManager(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isOnlyLeader(Space space, String userId) {
    return this.isOnlyManager(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isOnlyLeader(String spaceId, String userId) {
    return this.isOnlyManager(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isMember(Space space, String userId) {
    return ArrayUtils.contains(space.getMembers(), userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isMember(String spaceId, String userId) {
    return isMember(getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasAccessPermission(Space space, String userId) {
    if (isSuperManager(userId)
        || (ArrayUtils.contains(space.getMembers(), userId))
        || (ArrayUtils.contains(space.getManagers(), userId))) {
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasAccessPermission(String spaceId, String userId) {
    return hasAccessPermission(getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasEditPermission(Space space, String userId) {
    return this.hasSettingPermission(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasEditPermission(String spaceId, String userId) {
    return this.hasSettingPermission(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isInvited(Space space, String userId) {
    return this.isInvitedUser(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isInvited(String spaceId, String userId) {
    return this.isInvitedUser(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isPending(Space space, String userId) {
    return this.isPendingUser(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isPending(String spaceId, String userId) {
    return this.isPendingUser(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isIgnored(Space space, String userId) {
    boolean ignoredMember = spaceStorage.isSpaceIgnored(space.getId(), userId);
    return ignoredMember;
  }

  /**
   * {@inheritDoc}
   */
  public void setIgnored(String spaceId, String userId) {
    spaceStorage.ignoreSpace(spaceId, userId);
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
    // String appStatus = SpaceUtils.getAppStatus(space, appId);
    // if (appStatus != null) {
    // if (appStatus.equals(Space.INSTALL_STATUS)) return;
    // }
    // SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    // appHandler.installApplication(space, appId); // not implement yet
    // setApp(space, appId, appId, SpaceUtils.isRemovableApp(space, appId),
    // Space.INSTALL_STATUS);
    spaceLifeCycle.addApplication(space, getPortletId(appId));
  }

  /**
   * {@inheritDoc}
   */
  public void activateApplication(Space space, String appId) throws SpaceException {
    // String appStatus = SpaceUtils.getAppStatus(space, appId);
    // if (appStatus != null) {
    // if (appStatus.equals(Space.ACTIVE_STATUS)) return;
    // }
    String appName = null;
    if (SpaceUtils.isInstalledApp(space, appId)) {
      appName = appId + System.currentTimeMillis();
    } else {
      appName = appId;
    }
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.activateApplication(space, appId, appName);
    // Default is removable, or must be added by configuration or support setting for applications.
    spaceTemplateService.setApp(space, appId, appName, true, Space.ACTIVE_STATUS);
    saveSpace(space, false);
    // Use portletId instead of appId for fixing SOC-1633.
    spaceLifeCycle.activateApplication(space, getPortletId(appId));
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
    String appStatus = SpaceUtils.getAppStatus(space, appId);
    if (appStatus == null) {
      LOG.warn("appStatus is null!");
      return;
    }
    if (appStatus.equals(Space.DEACTIVE_STATUS))
      return;
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.deactiveApplication(space, appId);
    spaceTemplateService.setApp(space, appId, appId, SpaceUtils.isRemovableApp(space, appId), Space.DEACTIVE_STATUS);
    saveSpace(space, false);
    spaceLifeCycle.deactivateApplication(space, getPortletId(appId));
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
  public void removeApplication(Space space, String appId, String appName) throws SpaceException {
    String appStatus = SpaceUtils.getAppStatus(space, appId);
    if (appStatus == null)
      return;
    SpaceApplicationHandler appHandler = getSpaceApplicationHandler(space);
    appHandler.removeApplication(space, appId, appName);
    removeApp(space, appId, appName);
    spaceLifeCycle.removeApplication(space, getPortletId(appId));
  }

  /**
   * {@inheritDoc}
   */
  public void removeApplication(String spaceId, String appId, String appName) throws SpaceException {
    removeApplication(getSpaceById(spaceId), appId, appName);
  }

  /**
   * {@inheritDoc}
   */
  public void requestJoin(String spaceId, String userId) {
    this.addPendingUser(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public void requestJoin(Space space, String userId) {
    this.addPendingUser(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public void revokeRequestJoin(Space space, String userId) {
    this.removePendingUser(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public void revokeRequestJoin(String spaceId, String userId) {
    this.removePending(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public void inviteMember(Space space, String userId) {
    this.addInvitedUser(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public void inviteMember(String spaceId, String userId) {
    this.addInvitedUser(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public void revokeInvitation(Space space, String userId) {
    this.removeInvitedUser(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public void revokeInvitation(String spaceId, String userId) {
    this.removeInvitedUser(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public void acceptInvitation(Space space, String userId) throws SpaceException {
    this.addMember(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public void acceptInvitation(String spaceId, String userId) throws SpaceException {
    this.addMember(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public void denyInvitation(String spaceId, String userId) {
    this.removeInvitedUser(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public void denyInvitation(Space space, String userId) {
    this.removeInvitedUser(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public void validateRequest(Space space, String userId) {
    this.addMember(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public void validateRequest(String spaceId, String userId) {
    this.addMember(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public void declineRequest(Space space, String userId) {
    this.removePendingUser(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public void declineRequest(String spaceId, String userId) {
    this.removePendingUser(this.getSpaceById(spaceId), userId);
  }

  /**
   * {@inheritDoc}
   */
  public void registerSpaceLifeCycleListener(SpaceLifeCycleListener listener) {
    spaceLifeCycle.addListener(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void unregisterSpaceLifeCycleListener(SpaceLifeCycleListener listener) {
    spaceLifeCycle.removeListener(listener);
  }

  public void addSpaceListener(SpaceListenerPlugin plugin) {
    registerSpaceLifeCycleListener(plugin);
  }

  /**
   * Set portlet preferences from plug-in into local variable.
   */
  public void setPortletsPrefsRequired(PortletPreferenceRequiredPlugin portletPrefsRequiredPlugin) {
    List<String> portletPrefs = portletPrefsRequiredPlugin.getPortletPrefs();
    if (portletPrefsRequired == null) {
      portletPrefsRequired = new ArrayList<String>();
    }
    portletPrefsRequired.addAll(portletPrefs);
  }

  /**
   * Get portlet preferences required for using in create portlet application.
   */
  public String [] getPortletsPrefsRequired() {
    return this.portletPrefsRequired.toArray(new String[this.portletPrefsRequired.size()]);
  }

  /**
   * Gets OrganizationService
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
   * Gets UserACL
   *
   * @return userACL
   */
  private UserACL getUserACL() {
    return userACL;
  }

  /**
   * Gets space application handler
   *
   * @param space
   * @return
   * @throws SpaceException
   */
  private SpaceApplicationHandler getSpaceApplicationHandler(Space space) throws SpaceException {
    String spaceTemplate = space.getTemplate();
    SpaceApplicationHandler appHandler = spaceTemplateService.getSpaceApplicationHandlers().get(spaceTemplate);
    if (appHandler == null) {
      LOG.debug("No space application handler was defined for template with name {}. Default will be used.", spaceTemplate);
      String defaultTemplate = spaceTemplateService.getDefaultSpaceTemplate();
      appHandler = spaceTemplateService.getSpaceApplicationHandlers().get(defaultTemplate);
      if (appHandler == null) {
        throw new SpaceException(SpaceException.Code.UNKNOWN_SPACE_TYPE);
      }
    }
    return appHandler;
  }

  /**
   * Removes application from a space
   *
   * @param space
   * @param appId
   * @throws SpaceException
   */
  private void removeApp(Space space, String appId, String appName) throws SpaceException {
    String apps = space.getApp();
    StringBuffer remainApp = new StringBuffer();
    String[] listApp = apps.split(",");
    String[] appPart;
    String app;
    for (int idx = 0; idx < listApp.length; idx++) {
      app = listApp[idx];
      appPart = app.split(":");
      if (!appPart[1].equals(appName)) {
        if (remainApp.length() != 0)
          remainApp.append(",");
        remainApp.append(app);
      }
    }

    space.setApp(remainApp.toString());
    saveSpace(space, false);
  }

  private boolean isMandatory(GroupHandler groupHandler, Group group, List<String> mandatories) throws Exception {
    if (mandatories.contains(group.getId()))
      return true;
    Collection<Group> children = groupHandler.findGroups(group);
    for (Group g : children) {
      if (isMandatory(groupHandler, g, mandatories))
        return true;
    }
    return false;
  }

  /**
   * @return
   * @deprecated To be removed at 1.3.x.
   */
  public SpaceStorage getStorage() {
    return spaceStorage;
  }

  /**
   *
   * @param storage
   * @deprecated  To be removed at 1.3.x
   */
  public void setStorage(SpaceStorage storage) {
    this.spaceStorage = storage;
  }

  /**
   * {@inheritDoc}
   */
  public void addInvitedUser(Space space, String userId) {

    if (ArrayUtils.contains(space.getMembers(),userId)) {
      //user is already member. Do nothing
      return;
    }

    if (isPending(space, userId)) {
      space = removePending(space, userId);
      addMember(space, userId);
    } else {
      space = addInvited(space, userId);
    }
    this.updateSpace(space);
    spaceLifeCycle.addInvitedUser(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public void addPendingUser(Space space, String userId) {
    if (ArrayUtils.contains(space.getMembers(),userId)) {
      //user is already member. Do nothing
      return;
    }

    if (ArrayUtils.contains(space.getPendingUsers(), userId)) {
      this.addMember(space, userId);
      space = removeInvited(space, userId);
      this.updateSpace(space);
      return;
    }

    String registration = space.getRegistration();
    String visibility = space.getVisibility();
    if (visibility.equals(Space.HIDDEN) && registration.equals(Space.CLOSE)) {
      LOG.warn("Unable request to join hidden");
      return;
    }
    if (registration.equals(Space.OPEN)) {
      addMember(space, userId);
    } else if (registration.equals(Space.VALIDATION)) {
      space = addPending(space, userId);
      saveSpace(space, false);
    } else {
      LOG.warn("Unable request to join");
    }
    spaceLifeCycle.addPendingUser(space, userId);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Space> getAccessibleSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    if (isSuperManager(userId)
        && (spaceFilter == null || spaceFilter.getAppId() == null)) {
      if(spaceFilter == null) {
        return new SpaceListAccess(this.spaceStorage, spaceFilter, SpaceListAccess.Type.ALL);
      } else {
        return new SpaceListAccess(this.spaceStorage, spaceFilter, SpaceListAccess.Type.ALL_FILTER);
      }
    } else {
      return new SpaceListAccess(this.spaceStorage, userId, spaceFilter, SpaceListAccess.Type.ACCESSIBLE_FILTER);
    }
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Space> getAllSpacesByFilter(SpaceFilter spaceFilter) {
    return new SpaceListAccess(this.spaceStorage, spaceFilter, SpaceListAccess.Type.ALL_FILTER);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Space> getInvitedSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    return new SpaceListAccess(this.spaceStorage, userId, spaceFilter, SpaceListAccess.Type.INVITED_FILTER);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Space> getMemberSpaces(String userId) {
    return new SpaceListAccess(this.spaceStorage, userId, SpaceListAccess.Type.MEMBER);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Space> getMemberSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    return new SpaceListAccess(this.spaceStorage, userId, spaceFilter, SpaceListAccess.Type.MEMBER_FILTER);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Space> getPendingSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    return new SpaceListAccess(this.spaceStorage, userId, spaceFilter, SpaceListAccess.Type.PENDING_FILTER);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Space> getPublicSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    if (isSuperManager(userId)) {
      return new SpaceListAccess(this.spaceStorage, SpaceListAccess.Type.PUBLIC_SUPER_USER);
    } else {
      return new SpaceListAccess(this.spaceStorage, userId, spaceFilter, SpaceListAccess.Type.PUBLIC_FILTER);
    }
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Space> getSettingableSpaces(String userId) {
    if (isSuperManager(userId)) {
      return new SpaceListAccess(this.spaceStorage, SpaceListAccess.Type.ALL);
    } else {
      return new SpaceListAccess(this.spaceStorage, userId, SpaceListAccess.Type.SETTING);
    }
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Space> getSettingabledSpacesByFilter(String userId, SpaceFilter spaceFilter) {
    if (isSuperManager(userId)) {
      return new SpaceListAccess(this.spaceStorage, spaceFilter, SpaceListAccess.Type.ALL_FILTER);
    } else {
      return new SpaceListAccess(this.spaceStorage, userId, spaceFilter, SpaceListAccess.Type.SETTING_FILTER);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasSettingPermission(Space space, String userId) {
    return isSuperManager(userId) || ArrayUtils.contains(space.getManagers(), userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isInvitedUser(Space space, String userId) {
    return ArrayUtils.contains(space.getInvitedUsers(), userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isManager(Space space, String userId) {
    return ArrayUtils.contains(space.getManagers(), userId);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isOnlyManager(Space space, String userId) {
    if (space.getManagers() != null && space.getManagers().length == 1 && ArrayUtils.contains(space.getManagers(), userId)) {
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isPendingUser(Space space, String userId) {
    return ArrayUtils.contains(space.getPendingUsers(), userId);
  }

  /**
   * {@inheritDoc}
   */
  public void registerSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin) {
    spaceLifeCycle.addListener(spaceListenerPlugin);
  }

  /**
   * {@inheritDoc}
   */
  public void removeInvitedUser(Space space, String userId) {
    if (ArrayUtils.contains(space.getInvitedUsers(), userId)) {
      space = this.removeInvited(space, userId);
      this.updateSpace(space);
      removeWebNotifications(space.getId(), userId);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void removePendingUser(Space space, String userId) {
    if (ArrayUtils.contains(space.getPendingUsers(), userId)) {
      space = this.removePending(space, userId);
      this.updateSpace(space);
      removeWebNotifications(space.getId(), userId);
    }
  }

  private void removeWebNotifications(String spaceId, String userId) {
    if (webNotificationService != null) {
      WebNotificationFilter filter = new WebNotificationFilter(userId);
      filter.setParameter("spaceId", spaceId);
      PluginKey pluginKey = new PluginKey("SpaceInvitationPlugin");
      filter.setPluginKey(pluginKey);
      try {
        for (NotificationInfo notificationInfo : webNotificationService.getNotificationInfos(filter, 0, -1)) {
          webNotificationService.remove(notificationInfo.getId());
        }
      } catch (Exception e) {
        LOG.error("Cannot remove web notifications for user: " + userId + " and space id: " + spaceId, e);
      }
    } else {
      LOG.error("Cannot update web notfication. WebNotificationService is null");
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setManager(Space space, String userId, boolean isManager) {
    String[] managers = space.getManagers();
    if (isManager) {
      if (!ArrayUtils.contains(managers, userId)) {
        managers = (String[]) ArrayUtils.add(managers, userId);
        space.setManagers(managers);
        this.updateSpace(space);
        SpaceUtils.addUserToGroupWithManagerMembership(userId, space.getGroupId());
        spaceLifeCycle.grantedLead(space, userId);
      }
    } else {
      if (ArrayUtils.contains(managers, userId)) {
        managers = (String[]) ArrayUtils.removeElement(managers, userId);
        space.setManagers(managers);
        this.updateSpace(space);
        SpaceUtils.removeUserFromGroupWithManagerMembership(userId, space.getGroupId());
        Space updatedSpace = getSpaceById(space.getId());
        if (isMember(updatedSpace, userId)) {
          spaceLifeCycle.revokedLead(space, userId);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void unregisterSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin) {
    spaceLifeCycle.removeListener(spaceListenerPlugin);
  }

  @Override
  @Deprecated
  public void setSpaceApplicationConfigPlugin(SpaceApplicationConfigPlugin spaceApplicationConfigPlugin) {
    LOG.warn("setSpaceApplicationConfigPlugin method has been deprecated, please use addSpaceTemplateConfigPlugin method from SpaceTemplateConfigPlugin class");
  }

  @Override
  @Deprecated
  public SpaceApplicationConfigPlugin getSpaceApplicationConfigPlugin() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Space updateSpace(Space existingSpace) {
    spaceStorage.saveSpace(existingSpace, false);
    if (UpdatedField.DESCRIPTION.equals(existingSpace.getField())) {
      spaceLifeCycle.spaceDescriptionEdited(existingSpace, existingSpace.getEditor());
    }
    return existingSpace;
  }

  public Space updateSpaceAvatar(Space existingSpace) {
    checkSpaceEditorPermissions(existingSpace);

    Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, existingSpace.getPrettyName());
    Profile profile = spaceIdentity.getProfile();
    if(existingSpace.getAvatarAttachment() != null) {
      profile.setProperty(Profile.AVATAR, existingSpace.getAvatarAttachment());
    } else {
      profile.removeProperty(Profile.AVATAR);
      profile.setAvatarUrl(null);
      profile.setAvatarLastUpdated(null);
    }
    identityStorage.updateProfile(profile);
    spaceLifeCycle.spaceAvatarEdited(existingSpace, existingSpace.getEditor());
    return existingSpace;
  }

  public Space updateSpaceBanner(Space existingSpace) {
    checkSpaceEditorPermissions(existingSpace);

    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, existingSpace.getPrettyName());
    if (spaceIdentity != null) {
      Profile profile = spaceIdentity.getProfile();
      if (existingSpace.getBannerAttachment() != null) {
        profile.setProperty(Profile.BANNER, existingSpace.getBannerAttachment());
      } else {
        profile.removeProperty(Profile.BANNER);
      }
      identityStorage.updateProfile(profile);
      spaceLifeCycle.spaceBannerEdited(existingSpace, existingSpace.getEditor());
    } else {
      throw new IllegalStateException("Can not update space banner. Space identity " + existingSpace.getPrettyName() + " not found");
    }
    return existingSpace;
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Space> getInvitedSpacesWithListAccess(String userId) {
    return new SpaceListAccess(this.spaceStorage, userId, SpaceListAccess.Type.INVITED);
  }

  /**
   * Returns portlet id.
   */
  private String getPortletId(String appId) {
    final char SEPARATOR = '.';

    if (appId.indexOf(SEPARATOR) != -1) {
      int beginIndex = appId.lastIndexOf(SEPARATOR) + 1;
      int endIndex = appId.length();

      return appId.substring(beginIndex, endIndex);
    }

    return appId;
  }

  @Override
  public void updateSpaceAccessed(String remoteId, Space space) throws SpaceException {
    if (isMember(space, remoteId)) {
      spaceStorage.updateSpaceAccessed(remoteId, space);
    }
  }

  @Override
  public List<Space> getLastAccessedSpace(String remoteId, String appId, int offset, int limit) throws SpaceException {
    SpaceFilter filter = new SpaceFilter(remoteId, appId);
    return spaceStorage.getLastAccessedSpace(filter, offset, limit);
  }

  public List<Space> getLastSpaces(int limit) {
    return spaceStorage.getLastSpaces(limit);
  }

  @Override
  public ListAccess<Space> getLastAccessedSpace(String remoteId, String appId) {
    return new SpaceListAccess(this.spaceStorage, remoteId, appId, SpaceListAccess.Type.LASTEST_ACCESSED);
  }

  public ListAccess<Space> getVisitedSpaces(String remoteId, String appId) {
    return new SpaceListAccess(this.spaceStorage, remoteId, appId, SpaceListAccess.Type.VISITED);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSuperManager(String userId) {
    if (StringUtils.isBlank(userId) || IdentityConstants.ANONIM.equals(userId) || IdentityConstants.SYSTEM.equals(userId)) {
      return false;
    }
    if (userId.equals(getUserACL().getSuperUser())) {
      return true;
    }
    org.exoplatform.services.security.Identity identity = identityRegistry.getIdentity(userId);
    if (identity == null) {
      Collection<Membership> memberships;
      try {
        memberships = getOrgService().getMembershipHandler().findMembershipsByUser(userId);
      } catch (Exception e) {
        throw new RuntimeException("Can't get user '" + userId + "' memberships", e);
      }
      List<MembershipEntry> entries = new ArrayList<>();
      for (Membership membership : memberships) {
        entries.add(new MembershipEntry(membership.getGroupId(), membership.getMembershipType()));
      }
      identity = new org.exoplatform.services.security.Identity(userId, entries);
    }
    List<MembershipEntry> superManagersMemberships = spacesAdministrationService.getSpacesAdministratorsMemberships();
    if (superManagersMemberships != null && !superManagersMemberships.isEmpty()) {
      for (MembershipEntry superManagerMembership : superManagersMemberships) {
        if (identity.isMemberOf(superManagerMembership)) {
          return true;
        }
      }
    }
    return false;
  }

  private String checkSpaceEditorPermissions(Space space) {
    String editor = space.getEditor();
    // TODO if the space editor is null, an exception should be thrown too
    if (StringUtils.isNotBlank(editor) && !hasEditPermission(space, editor)) {
      throw new IllegalStateException("User " + editor + " is not authorized to change space.");
    }
    return editor;
  }
}