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

import org.exoplatform.commons.api.notification.ArgumentLiteral;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.task.NotificationTask;
import org.exoplatform.social.core.space.model.Space;

public abstract class SpaceTask implements NotificationTask<NotificationContext> {
  
  public final static ArgumentLiteral<Space> SPACE = new ArgumentLiteral<Space>(Space.class, "space");
  public final static ArgumentLiteral<String> REMOTE_ID = new ArgumentLiteral<String>(String.class, "remoteId");
  
  @Override
  public void start(NotificationContext ctx) {
  }

  @Override
  public void end(NotificationContext ctx) {
  }
  
  public static SpaceTask SPACE_INVITATION = new SpaceTask() {
    private final String TASK_NAME = "SPACE_INVITATION";

    @Override
    public String getId() {
      return TASK_NAME;
    }
    
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      
      Space space = ctx.value(SPACE);
      String userId = ctx.value(REMOTE_ID);
      
      message.setProviderType(TASK_NAME)
             .setFrom(space.getPrettyName())
             .addOwnerParameter("spaceId", space.getId())
             .setSendToUserIds(Arrays.asList(userId));
      
      return message;
    }

    @Override
    public boolean isValid(NotificationContext ctx) {
      return true;
    }
  };
  
  public static SpaceTask SPACE_JOIN_REQUEST = new SpaceTask() {
    private final String TASK_NAME = "SPACE_JOIN_REQUEST";

    @Override
    public String getId() {
      return TASK_NAME;
    }
    
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      
      Space space = ctx.value(SPACE);
      String userId = ctx.value(REMOTE_ID);
      
      message.setProviderType(TASK_NAME)
             .setFrom(userId)
             .addOwnerParameter("spaceId", space.getId())
             .setSendToUserIds(Arrays.asList(space.getManagers()));
      
      return message;
    }
    
    @Override
    public boolean isValid(NotificationContext ctx) {
      return true;
    }
  };

}
