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

import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.services.organization.Group;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;

public class BindGroupEventListener extends GroupEventListener {

    private GroupSpaceBindingService groupSpaceBindingService ;

    public BindGroupEventListener(GroupSpaceBindingService groupSpaceBindingService) throws Exception {
        this.groupSpaceBindingService = groupSpaceBindingService;
    }

    public void postSave(Group group, boolean isNew) throws Exception {
    }

    // delete all bindings for this group and remove member
    public void postDelete(Group group) throws Exception {
        // To do
        //groupSpaceBindingService.deleteSpaceBinding(groupSpaceBinding);
    }
}
