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

import org.exoplatform.social.core.binding.model.GroupSpaceBinding;

import java.util.List;

/**
 * Provides methods to manage the binding between a space and an organization group.
 */

public interface GroupSpaceBindingService {

  /**
   * Gets a list containing all the groups binding for a space/role.
   *
   * @param spaceId The space Id.
   * @param role The role in the space (manager or member).
   * @return The list of binding.
   */
  List<GroupSpaceBinding> findSpaceBindings(String spaceId, String role);

    /**
     * Creates a new space: creating a group, its group navigation with pages for installing space applications.
     *
     * @param groupSpaceBinding The binding to be created.
     * @param isNew New binding?

    void saveBinding(GroupSpaceBinding groupSpaceBinding, Boolean isNew);*/

    /**
     * Deletes a space. When a space is deleted, all of its page navigations and its group will be deleted.
     *
     * @param groupSpaceBinding The binding to be deleted.

    void deleteBinding(GroupSpaceBinding groupSpaceBinding);*/
}
