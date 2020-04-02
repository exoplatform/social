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

import org.exoplatform.social.core.binding.model.*;
import org.exoplatform.social.core.space.model.Space;

/**
 * Provides methods to manage the binding between a space and an organization
 * group.
 */

public interface GroupSpaceBindingService {

  String LOG_SERVICE_NAME          = "group-binding";

  String LOG_NEW_OPERATION_NAME    = "new-binding";

  String LOG_REMOVE_OPERATION_NAME = "remove-binding";

  String LOG_UPDATE_OPERATION_NAME = "update-binding";

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
  List<UserSpaceBinding> findUserSpaceBindingsBySpace(String spaceId, String userName);

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
   * Get the binding report for generate the csv file
   *
   * @param spaceId
   * @param groupSpaceBindingId
   * @param group
   * @param action
   * @return
   */
  List<GroupSpaceBindingReportUser> findReportsForCsv(long spaceId, long groupSpaceBindingId, String group, String action);

  /**
   * Gets all the GroupSpaceBindingOperations Report.
   * 
   * @return
   */
  List<GroupSpaceBindingOperationReport> getGroupSpaceBindingReportOperations();

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
   * Prepare the group binding deletion : create the reportAction, and create the GroupBindingQueue
   *
   * @param groupSpaceBinding The binding to be prepared.
   */
  void prepareDeleteGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding);

  /**
   * Delete a user binding. When a binding is deleted
   * 
   * @param userSpaceBinding The user binding to be deleted.
   * @param bindingReportAction : the action which lead to the deletion (for the
   */
  void deleteUserBinding(UserSpaceBinding userSpaceBinding, GroupSpaceBindingReportAction bindingReportAction);

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
   * Count user's bindings for the space.
   *
   * @param spaceId The space Id.
   * @param userName The username of the member.
   * @return a List of UserSpaceBinding.
   */
  long countUserBindings(String spaceId, String userName);

  /**
   * Count number of bound users for the space.
   *
   * @param spaceId The space Id.
   * @return number of bound users.
   */
  long countBoundUsers(String spaceId);

  /**
   * Checks if user is already bound and member of the space.
   * 
   * @param spaceId
   * @param userName
   * @return
   */
  boolean isUserBoundAndMemberBefore(String spaceId, String userName);

  /**
   * Checks if the space has bindings.
   * 
   * @param spaceId
   * @return
   */
  boolean isBoundSpace(String spaceId);

  /**
   * Saves a list of group binding.
   * 
   * @param groupSpaceBindings
   */
  void saveGroupSpaceBindings(List<GroupSpaceBinding> groupSpaceBindings);

  /**
   * Save a group space binding.
   * 
   * @param groupSpaceBinding
   * @return
   */
  GroupSpaceBinding saveGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding);

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

  /**
   * Save a user Binding given a space, a binding and a user name.
   * 
   * @param userId
   * @param groupSpaceBinding
   * @param space
   * @param bindingReportAction
   */
  void saveUserBinding(String userId,
                       GroupSpaceBinding groupSpaceBinding,
                       Space space,
                       GroupSpaceBindingReportAction bindingReportAction);
  
  GroupSpaceBindingReportAction saveGroupSpaceBindingReport(GroupSpaceBindingReportAction groupSpaceBindingReportAction);

  GroupSpaceBindingReportAction findGroupSpaceBindingReportAction(long bindingId, String action);

  void updateGroupSpaceBindingReportAction(GroupSpaceBindingReportAction groupSpaceBindingReportAction);

  List<GroupSpaceBindingQueue> getAllFromBindingQueue();
}
