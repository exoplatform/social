/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.core.storage.api;

import java.util.List;

import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingQueue;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.storage.GroupSpaceBindingStorageException;

/**
 * Manage the storage (binding group space and space member binding information)
 */

public interface GroupSpaceBindingStorage {

  /**
   * Get the first GroupSpaceBindingQueue to treat
   *
   * @return The GroupSpaceBindingQueue
   */
  GroupSpaceBindingQueue findFirstGroupSpaceBindingQueue() throws GroupSpaceBindingStorageException;

  /**
   * Get a list containing all the groups binding for a space.
   *
   * @param spaceId The space Id.
   * @return The list of binding.
   */
  List<GroupSpaceBinding> findGroupSpaceBindingsBySpace(String spaceId) throws GroupSpaceBindingStorageException;

  /**
   * Get a list containing all the groups binding for a space.
   *
   * @param group The group Id.
   * @return The list of binding.
   */
  List<GroupSpaceBinding> findGroupSpaceBindingsByGroup(String group) throws GroupSpaceBindingStorageException;

  /**
   * Get a list containing all the group binding for a space member.
   *
   * @param spaceId The space Id.
   * @param userName The space member.
   * @return The list of binding.
   */
  List<UserSpaceBinding> findUserSpaceBindingsBySpace(String spaceId, String userName) throws GroupSpaceBindingStorageException;

  /**
   * Get a list containing all the group binding for a user (user/group).
   *
   * @param group The group.
   * @param userName The space member.
   * @return The list of binding.
   */
  List<UserSpaceBinding> findUserSpaceBindingsByGroup(String group, String userName) throws GroupSpaceBindingStorageException;

  /**
   * Get a all the group binding for a user (user/group).
   *
   * @param userName The group.
   * @return The list of binding.
   */
  List<UserSpaceBinding> findUserSpaceBindingsByUser(String userName) throws GroupSpaceBindingStorageException;

  /**
   * Get all user bindings for a group
   *
   * @param group the group
   * @return A list of group bindings
   */
  List<UserSpaceBinding> findUserAllBindingsbyGroup(String group) throws GroupSpaceBindingStorageException;

  /**
   * Saves a new binding.
   *
   * @param binding
   * @throws GroupSpaceBindingStorageException
   */
  GroupSpaceBinding saveGroupSpaceBinding(GroupSpaceBinding binding) throws GroupSpaceBindingStorageException;

  /**
   * Add a Group Space Binding to the binding queue
   *
   * @param bindingQueue
   * @throws GroupSpaceBindingStorageException
   */
  GroupSpaceBindingQueue createGroupSpaceBindingQueue(GroupSpaceBindingQueue bindingQueue) throws GroupSpaceBindingStorageException;

  /**
   * Saves a user binding. binding an saves it.
   *
   * @param binding
   * @throws GroupSpaceBindingStorageException
   */
  UserSpaceBinding saveUserBinding(UserSpaceBinding binding) throws GroupSpaceBindingStorageException;

  /**
   * Deletes a binding by binding id.
   *
   * @param id
   * @throws GroupSpaceBindingStorageException
   */
  void deleteGroupBinding(long id) throws GroupSpaceBindingStorageException;

  /**
   * Deletes a binding by binding id.
   *
   * @param id
   * @throws GroupSpaceBindingStorageException
   */
  void deleteGroupBindingQueue(long id) throws GroupSpaceBindingStorageException;

  /**
   * Delete a user binding by binding id.
   *
   * @param id
   * @throws GroupSpaceBindingStorageException
   */
  void deleteUserBinding(long id) throws GroupSpaceBindingStorageException;

  /**
   * Delete all user bindings by username.
   *
   * @param userName
   * @throws GroupSpaceBindingStorageException
   */
  void deleteAllUserBindings(String userName) throws GroupSpaceBindingStorageException;

  /**
   * Gets user's bindings of the space.
   *
   * @param spaceId The space Id.
   * @param userName
   * @throws GroupSpaceBindingStorageException
   * @return a List of UserSpaceBinding
   */
  List<UserSpaceBinding> getUserBindings(String spaceId, String userName) throws GroupSpaceBindingStorageException;

  /**
   * Count user's bindings of the space.
   *
   * @param spaceId The space Id.
   * @param userName
   * @throws GroupSpaceBindingStorageException
   * @return number of UserSpaceBinding
   */
  long countUserBindings(String spaceId, String userName) throws GroupSpaceBindingStorageException;

  /**
   * Get a list containing UserSpaceBinding of a binding.
   * 
   * @param id
   * @return a list of UserSpaceBindings
   * @throws GroupSpaceBindingStorageException
   */
  List<UserSpaceBinding> findBoundUsersByBindingId(long id) throws GroupSpaceBindingStorageException;

  /**
   * Checks if user is already bound and member of the space.
   * 
   * @param spaceId
   * @param userId
   * @return
   */
  boolean isUserBoundAndMemberBefore(String spaceId, String userId);

  /**
   * Gets GroupSpaceBinding by Id.
   * 
   * @param bindingId
   * @return
   */
  GroupSpaceBinding findGroupSpaceBindingById(String bindingId);

  /**
   * Gets GroupSpaceBindings by action from the queue.
   * 
   * @param action
   * @return
   */
  List<GroupSpaceBinding> getGroupSpaceBindingsFromQueueByAction(String action);
}
