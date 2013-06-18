/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.task;

import java.util.Arrays;

import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.task.NotificationTask;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.context.NotificationContext;

public abstract class SpaceTask implements NotificationTask<NotificationContext> {
  
  public enum PROVIDER_TYPE {
    INVITED_JOIN_SPACE("InvitedJoinSpace"), REQUEST_JOIN_SPACE("RequestJoinSpace");
    private final String name;

    PROVIDER_TYPE(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  };
  
  @Override
  public void initSupportProvider() {
    // TODO Auto-generated method stub

  }

  @Override
  public void start(NotificationContext ctx) {
  }

  @Override
  public void end(NotificationContext ctx) {
  }
  
  public static SpaceTask SPACE_INVITATION = new SpaceTask() {
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      
      Space space = ctx.getSpace();
      String userId = ctx.getRemoteId();
      
      message.setProviderType(PROVIDER_TYPE.INVITED_JOIN_SPACE.getName())
             .setFrom(space.getId())
             .setSendToUserIds(Arrays.asList(userId));
      
      return message;
    }
  };
  
  public static SpaceTask SPACE_JOIN_REQUEST = new SpaceTask() {
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      
      Space space = ctx.getSpace();
      String userId = ctx.getRemoteId();
      
      message.setProviderType(PROVIDER_TYPE.INVITED_JOIN_SPACE.getName())
             .setFrom(userId)
             .addOwnerParameter("spaceGroupId", space.getGroupId())
             .setSendToUserIds(Arrays.asList(space.getManagers()));
      
      return message;
    }
  };

}
