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

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationMessage;
import org.exoplatform.commons.api.notification.task.AbstractNotificationTask;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;


public class NotificationExecutor {
  private static final Log LOG = ExoLogger.getLogger(NotificationExecutor.class);
  
  public static NotificationMessage execute(NotificationContext ctx, AbstractNotificationTask<NotificationContext> task) {
    NotificationMessage got = null;

    if (task.isSuperValid(ctx) == false || task.isValid(ctx) == false) {
      return got;
    }
    
    //
    task.start(ctx);

    //
    try {
      LOG.info("Make message notification of " + task.getClass().getName());
      got = task.execute(ctx);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.debug("Can not execute task...", e);
    } finally {
      task.end(ctx);
    }

    //
    return got;
  }

  public static Collection<NotificationMessage> execute(NotificationContext ctx, AbstractNotificationTask<NotificationContext>... tasks) {
    Collection<NotificationMessage> gots = new ArrayList<NotificationMessage>();

    NotificationMessage got;

    for (int i = 0; i < tasks.length; ++i) {
      if (tasks[i] == null || !tasks[i].isValid(ctx)) {
        continue;
      }

      //
      got = execute(ctx,tasks[i]);
      if (got != null) {
        gots.add(got);
      }
    }
    //
    return gots;
  }
  
}
