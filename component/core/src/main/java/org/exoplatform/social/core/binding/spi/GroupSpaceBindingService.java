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
import org.exoplatform.social.core.binding.model.GroupSpaceBindingQueue;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;

/**
 * Provides methods to manage the binding between a space and an organization
 * group.
 */

public interface GroupSpaceBindingService {

  /**
   * Get the first GroupSpaceBindingQueue to treat
   *
   * @return The list of binding.
   */
  GroupSpaceBindingQueue findFirstGroupSpaceBindingQueue();

  /**
   * Get a list containing all the groups binding for a space.
   *
   * @param spaceId The space Id.
   * @return The list of binding.
   */
  List<GroupSpaceBinding> findGroupSpaceBindingsBySpace(String spaceId);

  /**
   * Get a list containing all the groups binding for a group.
   *
   * @param group The group Id.
   * @return The list of binding.
   */
  List<GroupSpaceBinding> findGroupSpaceBindingsByGroup(String group);

  /**
   * Get a list containing all the groups binding for a space.
   *
   * @param spaceId The space Id.
   * @param userName The space member's username.
   * @return The list of users binding for this space member.
   */
  List<UserSpaceBinding> findUserBindingsBySpace(String spaceId, String userName);

  /**
   * Get user bindings in space
   *
   * @param group the group
   * @param userName Member in the space
   * @return A list of group bindings
   */
  List<UserSpaceBinding> findUserBindingsByGroup(String group, String userName);

  /**
   * Get user bindings for a user
   *
   * @param userName the user
   * @return A list of group bindings
   */
  List<UserSpaceBinding> findUserBindingsByUser(String userName);

  /**
   * Saves a list of user bindings
   *
   * @param userSpaceBindings The list of user bindings to be created
   */
  void saveUserBindings(List<UserSpaceBinding> userSpaceBindings);

  /**
   * Saves a group space binding queue
   *
   * @param groupSpaceBindingsQueue The group space binding queue to save
   */
  void createGroupSpaceBindingQueue(GroupSpaceBindingQueue groupSpaceBindingsQueue);

  /**
   * Delete a group binding. When a binding is deleted, all user in the group will
   * be remove from space.
   *
   * @param groupSpaceBinding The binding to be deleted.
   */
  void deleteGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding);

  /**
   * Delete a user binding. When a binding is deleted
   *
   * @param userSpaceBinding The user binding to be deleted.
   */
  void deleteUserBinding(UserSpaceBinding userSpaceBinding);

  /**
   * Delete all group bindings for a specific space. When bindings is deleted, all
   * users in the group will be remove from space.
   * 
   * @param spaceId The space Id.
   */
  void deleteAllSpaceBindingsBySpace(String spaceId);

  /**
   * Delete all group bindings for a specific group. When bindings is deleted, all
   * users in the group will be remove from associated space.
   *
   * @param group The group Id.
   */
  void deleteAllSpaceBindingsByGroup(String group);

  /**
   * Gets user's bindings for the space.
   *
   * @param spaceId The space Id.
   * @param userName The username of the member.
   * @return a List of UserSpaceBinding.
   */
  List<UserSpaceBinding> getUserBindings(String spaceId, String userName);
  
  /**
   * Count user's bindings for the space.
   *
   * @param spaceId The space Id.
   * @param userName The username of the member.
   * @return a List of UserSpaceBinding.
   */
  long countUserBindings(String spaceId, String userName);
  
  /**
   * Checks if user is already bound and returns true if is member of the space,
   * false if not. else returns null.
   * 
   * @param spaceId
   * @param userName
   * @return
   */
  Boolean isUserBoundAndMemberBefore(String spaceId, String userName);

  /**
   * Saves a list of group binding.
   * 
   * @param groupSpaceBindings
   */
  void saveGroupSpaceBindings(List<GroupSpaceBinding> groupSpaceBindings);

  /**
   * Save a UserSpaceBinding for each user of the group
   * 
   * @param groupSpaceBinding
   */
  void bindUsersFromGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding);

  /**
   * Delete a bindingQueue when its groupSpaceBinding is totally proceeded
   * 
   * @param bindingQueue
   */
  void deleteFromBindingQueue(GroupSpaceBindingQueue bindingQueue);

}
