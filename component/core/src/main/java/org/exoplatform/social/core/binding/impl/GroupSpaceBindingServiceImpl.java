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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage;

/**
 * {@link org.exoplatform.social.core.binding.spi.GroupSpaceBindingService}
 * implementation.
 */

public class GroupSpaceBindingServiceImpl implements GroupSpaceBindingService {

  public static final String       MEMBER  = "member";

  public static final String       MANAGER = "manager";

  private GroupSpaceBindingStorage groupSpaceBindingStorage;

  private static final Log         LOG     = ExoLogger.getLogger(GroupSpaceBindingServiceImpl.class);

  /**
   * GroupSpaceBindingServiceImpl constructor Initialize
   * 
   * @param params
   * @throws Exception
   */
  public GroupSpaceBindingServiceImpl(InitParams params, GroupSpaceBindingStorage groupSpaceBindingStorage) throws Exception {
    this.groupSpaceBindingStorage = groupSpaceBindingStorage;
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
    for (GroupSpaceBinding groupSpaceBinding : groupSpaceBindings) {
      groupSpaceBindingStorage.saveGroupBinding(groupSpaceBinding, true);
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
}
