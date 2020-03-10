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
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;

public class SpaceBindingUserEventListener extends UserEventListener {
  private static final Log         LOG = ExoLogger.getLogger(SpaceBindingUserEventListener.class);

  private GroupSpaceBindingService groupSpaceBindingService;

  @Override
  public void postDelete(User user) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try {
      groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
      // Get all user bindings.
      List<UserSpaceBinding> userSpaceBindings = groupSpaceBindingService.findUserBindingsByUser(user.getUserName());
      // Remove all user's bindings.
      for (UserSpaceBinding userSpaceBinding : userSpaceBindings) {
        groupSpaceBindingService.deleteUserBindingAndSpaceMembership(userSpaceBinding);
      }
    } catch (Exception e) {
      LOG.warn("Problem occurred when removing user bindings for user ({}): ", user.getUserName(), e);
    } finally {
      RequestLifeCycle.end();
    }
  }
}
