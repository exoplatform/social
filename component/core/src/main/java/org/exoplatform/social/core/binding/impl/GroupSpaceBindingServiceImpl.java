/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.core.binding.impl;

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage;

/**
 * {@link org.exoplatform.social.core.binding.spi.GroupSpaceBindingService}
 * implementation.
 */

public class GroupSpaceBindingServiceImpl implements GroupSpaceBindingService {

  public static final String       MEMBER  = "member";

  public static final String       MANAGER = "manager";

  private GroupSpaceBindingStorage groupSpaceBindingStorage;

  private OrganizationService      organizationService;

  private SpaceService             spaceService;

  private static final Log         LOG     = ExoLogger.getLogger(GroupSpaceBindingServiceImpl.class);

  /**
   * GroupSpaceBindingServiceImpl constructor Initialize
   * 
   * @param params
   * @throws Exception
   */
  public GroupSpaceBindingServiceImpl(InitParams params,
                                      GroupSpaceBindingStorage groupSpaceBindingStorage,
                                      OrganizationService organizationService,
                                      SpaceService spaceService)
      throws Exception {
    this.groupSpaceBindingStorage = groupSpaceBindingStorage;
    this.organizationService = organizationService;
    this.spaceService = spaceService;
  }

  /**
   * {@inheritDoc}
   */
  public List<GroupSpaceBinding> findGroupSpaceBindingsBySpace(String spaceId, String spaceRole) {
    LOG.info("Retrieving group/space bindings for space:" + spaceId + "/" + spaceRole);
    return groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(spaceId, spaceRole);
  }

  /**
  * {@inheritDoc}
  */
  public List<GroupSpaceBinding> findGroupSpaceBindingsByGroup(String group, String role) {
    LOG.info("Retrieving group/space bindings for group:" + group + "/" + role);
    return groupSpaceBindingStorage.findGroupSpaceBindingsByGroup(group, role);
  }

  /**
   * {@inheritDoc}
   */
  public List<UserSpaceBinding> findUserBindingsBySpace(String spaceId, String userName) {
    LOG.info("Retrieving user bindings for member:" + userName + "/" + spaceId);
    return groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, userName);
  }

  /**
  * {@inheritDoc}
  */
  public List<UserSpaceBinding> findUserBindingsByUser(String userName) {
    LOG.info("Retrieving user bindings for member:" + userName);
    return groupSpaceBindingStorage.findUserSpaceBindingsByUser(userName);
  }

  /**
   * {@inheritDoc}
   */
  public List<UserSpaceBinding> findUserBindingsByGroup(String group, String groupRole, String userName) {
    LOG.info("Retrieving user bindings for user :" + userName + "with membership :" + group + ":" + groupRole);
    return groupSpaceBindingStorage.findUserSpaceBindingsByGroup(group, groupRole, userName);
  }

  /**
   * {@inheritDoc}
   */
  public void saveSpaceBindings(String spaceId, List<GroupSpaceBinding> groupSpaceBindings) {
    LOG.info("Saving space bindings:" + spaceId);
    try {
      for (GroupSpaceBinding groupSpaceBinding : groupSpaceBindings) {
        groupSpaceBinding = groupSpaceBindingStorage.saveGroupBinding(groupSpaceBinding, groupSpaceBinding.getId()==-1 );
        ListAccess<User> groupMembersAccess =
                                            organizationService.getUserHandler().findUsersByGroupId(groupSpaceBinding.getGroup());
        User[] users = groupMembersAccess.load(0, groupMembersAccess.getSize());
        for (User user : users) {
            // add user binding for this space
            List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
            UserSpaceBinding userSpaceBinding = new UserSpaceBinding();
            userSpaceBinding.setGroupBinding(groupSpaceBinding);
            userSpaceBinding.setUser(user.getUserName());
            userSpaceBinding.setSpaceId(spaceId);
            userSpaceBindings.add(userSpaceBinding);
            this.saveUserBindings(userSpaceBinding.getUser(),userSpaceBindings);
          }
        }
    } catch (Exception e) {
      LOG.error("Error Binding" + e);
      throw new RuntimeException("Failed binding space " + spaceId, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserBindings(String userName, List<UserSpaceBinding> userSpaceBindings) {
    LOG.info("Saving user bindings for user :" + userName);
    try {
    for (UserSpaceBinding userSpaceBinding : userSpaceBindings) {
     this.bindUserToSpace(userSpaceBinding);
    }
    } catch (Exception e) {
        LOG.error("Error Binding" + e);
        throw new RuntimeException("Failed binding user " + userName, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteAllSpaceBindingsByGroup(GroupSpaceBinding groupSpaceBinding) {
    LOG.info("Delete binding group :" + groupSpaceBinding.getGroup() + "/" + groupSpaceBinding.getGroupRole() + " for space :"
        + groupSpaceBinding.getSpaceId() + "/" + groupSpaceBinding.getSpaceRole());
    // Call the delete user binding to also update space membership
    for (UserSpaceBinding userSpaceBinding: groupSpaceBindingStorage.findUserAllBindingsbyGroupMembership(groupSpaceBinding.getGroup(),groupSpaceBinding.getGroupRole()))
         {
                 deleteUserBinding(userSpaceBinding);
         }
    groupSpaceBindingStorage.deleteGroupBinding(groupSpaceBinding.getId());
  }

  /**
   * {@inheritDoc}
   */
  public void deleteUserBinding(UserSpaceBinding userSpaceBinding) {
    LOG.info("Delete user binding for member :" + userSpaceBinding.getUser() + "/" + userSpaceBinding.getSpaceId());
    groupSpaceBindingStorage.deleteUserBinding(userSpaceBinding.getId());
    // check if the user has other binding to the target space before removing
    // membership
    if (groupSpaceBindingStorage.hasUserBindings(userSpaceBinding.getSpaceId(), userSpaceBinding.getUser())) {
      // Manage the case of a new binding with role manager
      if (userSpaceBinding.getGroupBinding().getSpaceRole().equals("manager")) {
          boolean hasManagerRole = false;
          for (UserSpaceBinding userSpaceBinding1 : groupSpaceBindingStorage.findUserSpaceBindingsBySpace(userSpaceBinding.getSpaceId(),
                  userSpaceBinding.getUser())) {
              if (userSpaceBinding1.getGroupBinding().getSpaceRole().equals("manager"))
                  hasManagerRole = true;
          }
          // if the user has a no binding to manager role remove the manager permission
          if (!hasManagerRole) {
              spaceService.setManager(spaceService.getSpaceById(userSpaceBinding.getSpaceId()), userSpaceBinding.getUser(), false);
          }
      }
    } else {
      // no binding to the target space in this case remove user from group
      spaceService.removeMember(spaceService.getSpaceById(userSpaceBinding.getSpaceId()), userSpaceBinding.getUser());
    }

  }

  /**
   * {@inheritDoc}
   */
  public void deleteAllSpaceBindingsBySpace(String spaceId, String spaceRole) {
    LOG.info("Delete all bindings for space :" + spaceId + "/" + spaceRole);
    for (GroupSpaceBinding groupSpaceBinding : findGroupSpaceBindingsBySpace(spaceId, spaceRole)) {
      deleteAllSpaceBindingsByGroup(groupSpaceBinding);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteAllUserBindings(String userName) {
    LOG.info("Delete all user bindings for user :" + userName);
    groupSpaceBindingStorage.deleteAllUserBindings(userName);
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasUserBindings(String spaceId, String userName) {
    LOG.info("Checking if member has binding :" + userName + " space:" + spaceId);
    return groupSpaceBindingStorage.hasUserBindings(spaceId, userName);
  }

  private void bindUserToSpace(UserSpaceBinding userSpaceBinding) throws Exception {
      // Check if user has the correct membership in group
      if (organizationService.getMembershipHandler()
              .findMembershipByUserGroupAndType(userSpaceBinding.getUser(),
                      userSpaceBinding.getGroupBinding().getGroup(),
                      userSpaceBinding.getGroupBinding().getGroupRole()) != null) {
          // add user to space
          if (!spaceService.isMember(spaceService.getSpaceById(userSpaceBinding.getSpaceId()), userSpaceBinding.getUser()))
              spaceService.addMember(spaceService.getSpaceById(userSpaceBinding.getSpaceId()), userSpaceBinding.getUser());

          // set manager
          if (!spaceService.isManager(spaceService.getSpaceById(userSpaceBinding.getSpaceId()), userSpaceBinding.getUser())
                  && userSpaceBinding.getGroupBinding().getSpaceRole().equals("manager"))
              spaceService.setManager(spaceService.getSpaceById(userSpaceBinding.getSpaceId()), userSpaceBinding.getUser(), true);
      }
      groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
  }
}
