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

import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;

import java.util.LinkedList;
import java.util.List;

public class SpaceBindingMembershipGroupEventListener extends MembershipEventListener {

  private GroupSpaceBindingService groupSpaceBindingService;

  public SpaceBindingMembershipGroupEventListener(GroupSpaceBindingService groupSpaceBindingService) {
    this.groupSpaceBindingService = groupSpaceBindingService;
  }

  @Override
  public void postSave(Membership m, boolean isNew) throws Exception {
      for (GroupSpaceBinding groupSpaceBinding : groupSpaceBindingService.findGroupSpaceBindingsByGroup(m.getGroupId())) {
           List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
           UserSpaceBinding ub1 = new UserSpaceBinding();
           ub1.setGroupBinding(groupSpaceBinding);
           ub1.setSpaceId(groupSpaceBinding.getSpaceId());
           ub1.setUser(m.getUserName());
           userSpaceBindings.add(ub1);
           groupSpaceBindingService.saveUserBindings(m.getUserName(), userSpaceBindings);
    }
  }

  @Override
  public void postDelete(Membership m) throws Exception {
    for (UserSpaceBinding userSpaceBinding : groupSpaceBindingService.findUserBindingsByGroup(m.getGroupId(),
                                                                                              m.getUserName())) {
      groupSpaceBindingService.deleteUserBinding(userSpaceBinding);
    }
  }

}
