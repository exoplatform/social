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
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
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

  /**
   * GroupSpaceBindingServiceImpl constructor Initialize
   * 
   * @param params
   * @throws Exception
   */
  public GroupSpaceBindingServiceImpl(InitParams params, GroupSpaceBindingStorage groupSpaceBindingStorage) throws Exception {
    this.groupSpaceBindingStorage = groupSpaceBindingStorage;
  }

  public List<GroupSpaceBinding> findSpaceBindings(String spaceId, String role) {
    return groupSpaceBindingStorage.findSpaceBindings(spaceId, role);
  }

  /**public void saveBinding(GroupSpaceBinding groupSpaceBinding, Boolean isNew) {
    groupSpaceBindingStorage.saveBinding(groupSpaceBinding, true);
  }

  public void deleteBinding(GroupSpaceBinding groupSpaceBinding) {
    groupSpaceBindingStorage.deleteBinding(Long.toString(groupSpaceBinding.getId()));
  }*/
}
