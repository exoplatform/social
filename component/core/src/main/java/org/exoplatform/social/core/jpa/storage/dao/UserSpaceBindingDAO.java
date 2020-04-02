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

package org.exoplatform.social.core.jpa.storage.dao;

import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.jpa.storage.entity.UserSpaceBindingEntity;

public interface UserSpaceBindingDAO extends GenericDAO<UserSpaceBindingEntity, Long> {

  /**
   * Get user bindings of a user in a space
   *
   * @param group the group
   * @param userName Member in the space
   * @return A list of group bindings
   */
  List<UserSpaceBindingEntity> findUserBindingsByGroup(String group, String userName);

  /**
   * Get all user bindings
   *
   * @param group the group
   * @return A list of group bindings
   */
  List<UserSpaceBindingEntity> findUserAllBindingsByGroup(String group);

  /**
   * Get user bindings of a user
   *
   * @param userName the user
   * @return A list of group bindings
   */
  List<UserSpaceBindingEntity> findUserAllBindingsByUser(String userName);

  /**
   * Delete all the bindings of the user
   *
   * @param userName Member in the space
   */
  void deleteAllUserBindings(String userName);

  /**
   * Gets user's bindings for this space
   * 
   * @param spaceId The space Id.
   * @param userName The space Id.
   * @return a List of UserSpaceBindingEntity
   */
  List<UserSpaceBindingEntity> findUserSpaceBindingsBySpace(Long spaceId, String userName);

  /**
   * Count user's bindings for this space
   * 
   * @param spaceId The space Id.
   * @param userName The space Id.
   * @return number UserSpaceBindingEntity
   */
  long countUserBindings(Long spaceId, String userName);

  /**
   * Get bound users by a binding.
   * 
   * @param id
   * @return a list of UserSpaceBindingEntities
   */
  List<UserSpaceBindingEntity> findBoundUsersByBindingId(long id);
  
  /**
   * Get user space  binding by groupBindingId and username
   *
   * @param groupBindingId
   * @param username
   * @return a list of UserSpaceBindingEntities
   */
  UserSpaceBindingEntity findUserBindingByGroupBindingIdAndUsername(long groupBindingId, String username);
  
  
  
  /**
   * Checks if user is already bound and member of the space.
   * 
   * @param spaceId
   * @param userId
   * @return
   */
  boolean isUserBoundAndMemberBefore(Long spaceId, String userId);
  
  /**
   * Count the number of bound users in a space
   *
   * @param spaceId
   * @return number of bound users
   */
  long countBoundUsers(Long spaceId);
}
