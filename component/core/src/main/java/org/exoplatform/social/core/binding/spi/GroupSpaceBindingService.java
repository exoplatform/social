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

package org.exoplatform.social.core.binding.spi;

import java.util.List;

import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;

/**
 * Provides methods to manage the binding between a space and an organization
 * group.
 */

public interface GroupSpaceBindingService {

  /**
   * Get a list containing all the groups binding for a space/role.
   *
   * @param spaceId The space Id.
   * @param spaceRole The role in the space (manager or member).
   * @return The list of binding.
   */
  List<GroupSpaceBinding> findGroupSpaceBindingsBySpace(String spaceId, String spaceRole);

  /**
  * Get a list containing all the groups binding for a space/role.
  *
  * @param group The group Id.
  * @param role The role in group (manager,member...).
  * @return The list of binding.
  */
  List<GroupSpaceBinding> findGroupSpaceBindingsByGroup(String group, String role);

  /**
   * Get a list containing all the groups binding for a space/role.
   *
   * @param spaceId The space Id.
   * @param userName The space member's username.
   * @return The list of users binding for this space member.
   */
  List<UserSpaceBinding> findUserBindingsBySpace(String spaceId, String userName);

  /**
   * Get user bindings for a membership (group+role) in space
   *
   * @param group the group
   * @param groupRole the role in group
   * @param userName Member in the space
   * @return A list of group+membership bindings
   */
  List<UserSpaceBinding> findUserBindingsByGroup(String group, String groupRole, String userName);

  /**
  * Get user bindings for a user
  *
  * @param userName the user
  * @return A list of group+membership bindings
  */
  List<UserSpaceBinding> findUserBindingsByUser(String userName);

  /**
   * Saves a list of group binding for a specific space.
   *
   * @param spaceId The space Id.
   * @param groupSpaceBindings The list of bindings to be created for the space.
   */
  void saveSpaceBindings(String spaceId, List<GroupSpaceBinding> groupSpaceBindings);

  /**
   * Saves a list of group binding for a specific space.
   *
   * @param userName The userName.
   * @param userSpaceBindings The list of user bindings to be created for the
   *          member.
   */
  void saveUserBindings(String userName, List<UserSpaceBinding> userSpaceBindings);

  /**
   * Delete a group binding. When a binding is deleted, all user in the group will
   * be remove from space.
   *
   * @param groupSpaceBinding The binding to be deleted.
   */
  void deleteAllSpaceBindingsByGroup(GroupSpaceBinding groupSpaceBinding);

  /**
   * Delete a user binding. When a binding is deleted
   *
   * @param userSpaceBinding The user binding to be deleted.
   */
  void deleteUserBinding(UserSpaceBinding userSpaceBinding);

  /**
   * Delete all the binding of a user
   *
   * @param user The user .
   */
  void deleteAllUserBindings(String user);

  /**
   * Delete all group bindings for a specific space / role. When bindings is
   * deleted, all users in the group will be remove from space.
   * 
   * @param spaceId The space Id.
   * @param spaceRole The role in the space (manager or member).
   */
  void deleteAllSpaceBindingsBySpace(String spaceId, String spaceRole);

  /**
   * Check if member has binding for this space
   *
   * @param spaceId The space Id.
   * @param userName The username of the member.
   * @return true if the member has binding for this space.
   */
  boolean hasUserBindings(String spaceId, String userName);

}
