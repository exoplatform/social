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
  public List<GroupSpaceBinding> findSpaceBindings(String spaceId, String spaceRole) {
    LOG.info("Retrieving space bindings for space:" + spaceId + "/" + spaceRole);
    return groupSpaceBindingStorage.findSpaceBindings(spaceId, spaceRole);
  }

  /**
   * {@inheritDoc}
   */
  public List<UserSpaceBinding> findUserBindings(String spaceId, String userName) {
    LOG.info("Retrieving user bindings for member:" + userName + "/" + spaceId);
    return groupSpaceBindingStorage.findUserSpaceBindings(spaceId, userName);
  }

  /**
   * {@inheritDoc}
   */
  public void saveSpaceBindings(String spaceId, List<GroupSpaceBinding> groupSpaceBindings) {
    LOG.info("Saving space bindings:" + spaceId);
    try {
      for (GroupSpaceBinding groupSpaceBinding : groupSpaceBindings) {
          groupSpaceBindingStorage.saveGroupBinding(groupSpaceBinding, true);
          ListAccess<User> groupMembersAccess = organizationService.getUserHandler().findUsersByGroupId(groupSpaceBinding.getGroup());
          User [] users = groupMembersAccess.load(0, groupMembersAccess.getSize());
          for (User user : users) {
              // Check if user has the correct membership in group
              if ( organizationService.getMembershipHandler().findMembershipByUserGroupAndType(user.getUserName(),groupSpaceBinding.getGroup(),groupSpaceBinding.getGroupRole())!=null)
               {
                   // add user to space
                   if (!spaceService.isMember(spaceService.getSpaceById(spaceId),user.getUserName())) spaceService.addMember(spaceService.getSpaceById(spaceId),user.getUserName());

                   // Delete previous binding if exist
                   for (UserSpaceBinding userSpaceBinding :groupSpaceBindingStorage.findUserSpaceBindings(spaceId,user.getUserName()))
                   {
                       groupSpaceBindingStorage.deleteUserBinding(userSpaceBinding.getId());
                   }

                   // add user binding for this space*
                   UserSpaceBinding userSpaceBinding = new UserSpaceBinding();
                   userSpaceBinding.setGroupBinding(groupSpaceBinding);
                   userSpaceBinding.setUser(user.getUserName());
                   userSpaceBinding.setSpaceId(spaceId);
                   groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
               }
          }
          }
      } catch (Exception e) {
        throw new RuntimeException("Failed bing space " + spaceId, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserBindings(String userName, List<UserSpaceBinding> userSpaceBindings) {
    LOG.info("Saving user bindings for user :" + userName);
    for (UserSpaceBinding userSpaceBinding : userSpaceBindings) {
      groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteSpaceBinding(GroupSpaceBinding groupSpaceBinding) {
    LOG.info("Delete binding group :" + groupSpaceBinding.getGroup() + "/" + groupSpaceBinding.getGroupRole() + " for space :"
        + groupSpaceBinding.getSpaceId() + "/" + groupSpaceBinding.getSpaceRole());
    groupSpaceBindingStorage.deleteGroupBinding(groupSpaceBinding.getId());
  }

  /**
   * {@inheritDoc}
   */
  public void deleteUserBinding(UserSpaceBinding userSpaceBinding) {
    LOG.info("Delete user binding for member :" + userSpaceBinding.getUser() + "/" + userSpaceBinding.getSpaceId());
    groupSpaceBindingStorage.deleteUserBinding(userSpaceBinding.getId());
  }

  /**
   * {@inheritDoc}
   */
  public void deleteAllSpaceBindings(String spaceId, String spaceRole) {
    LOG.info("Delete all bindings for space :" + spaceId + "/" + spaceRole);
    for (GroupSpaceBinding groupSpaceBinding : findSpaceBindings(spaceId, spaceRole)) {
      deleteSpaceBinding(groupSpaceBinding);
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
}
