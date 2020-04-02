/***************************************************************************	
 * Copyright (C) 2003-2019 eXo Platform SAS.	
 *
 * This program is free software; you can redistribute it and/or	
 * modify it under the terms of the GNU Affero General Public License	
 * as published by the Free Software Foundation; either version 3	
 * of the License, or (at your option) any later version.	
 *
 * This program is distributed in the hope that it will be useful,	
 * but WITHOUT ANY WARRANTY; without even the implied warranty of	
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the	
 * GNU General Public License for more details.	
 *
 * You should have received a copy of the GNU General Public License	
 * along with this program; if not, see<http://www.gnu.org/licenses/>.	
 *
 **************************************************************************/

package org.exoplatform.social.core.binding.listener;

import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;

public class SpaceBindingGroupEventListener extends GroupEventListener {
  private static final Log         LOG = ExoLogger.getLogger(SpaceBindingGroupEventListener.class);

  private GroupSpaceBindingService groupSpaceBindingService;

  public void postDelete(Group group) throws Exception {
    if (!group.getId().startsWith("/spaces")) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try {
        groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
        // Retrieve all bindings of the deleted group.
        List<GroupSpaceBinding> groupSpaceBindings = groupSpaceBindingService.findGroupSpaceBindingsByGroup(group.getId());
        // For each binding create a binding queue with remove action.
        groupSpaceBindings.stream()
                          .forEach(groupSpaceBinding -> groupSpaceBindingService.prepareDeleteGroupSpaceBinding(groupSpaceBinding));

      } catch (Exception e) {
        LOG.warn("Problem occurred when removing all bindings for removed group ({}): ", group.getGroupName(), e);
      } finally {
        RequestLifeCycle.end();
      }
    }
  }
}
