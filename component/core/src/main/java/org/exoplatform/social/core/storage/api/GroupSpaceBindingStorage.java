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
import org.exoplatform.social.core.storage.GroupSpaceBindingStorageException;

/**
 * Manage the storage (binding group space and space member binding information)
 */

public interface GroupSpaceBindingStorage {

  /**
   * Gets a list containing all the groups binding for a space/role.
   *
   * @param spaceId The space Id.
   * @param role The role in the space (manager or member).
   * @return The list of binding.
   */
  List<GroupSpaceBinding> findSpaceBindings(String spaceId, String role) throws GroupSpaceBindingStorageException;

  /**
   * Saves a binding. If isNew is true, creates new binding. If not only updates
   * binding an saves it.
   *
   * @param binding
   * @param isNew
   * @throws GroupSpaceBindingStorageException
   */
  void saveBinding(GroupSpaceBinding binding, boolean isNew) throws GroupSpaceBindingStorageException;

  /**
   * Deletes a binding by binding id.
   *
   * @param id
   * @throws GroupSpaceBindingStorageException
   */
  void deleteBinding(String id) throws GroupSpaceBindingStorageException;
}
