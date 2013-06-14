/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.context;

import java.util.ArrayList;
import java.util.Collection;

import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.task.NotificationTask;


public class NotificationExecutor {

  
  public static NotificationMessage execute(NotificationTask<NotificationContext> task, NotificationContext ctx) {
    
    task.start(ctx);
    
    //
    NotificationMessage got = task.execute(ctx);
    
    //
    task.end(ctx);
    
    //
    return got;
  }

  public static Collection<NotificationMessage> execute(NotificationContext ctx, NotificationTask<NotificationContext>... tasks) {
    Collection<NotificationMessage> gots = new ArrayList<NotificationMessage>();

    for (int i = 0; i < tasks.length; ++i) {
      if (tasks[i] == null) { 
        continue;
      }
      //
      gots.add(execute(tasks[i], ctx));
    }
    //
    return gots;
  }
  
}
