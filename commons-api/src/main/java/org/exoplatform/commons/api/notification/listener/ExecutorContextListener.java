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
package org.exoplatform.commons.api.notification.listener;

import java.util.concurrent.Callable;

import org.exoplatform.commons.api.notification.NotificationDataStorage;
import org.exoplatform.commons.api.notification.service.NotificationContext;
import org.exoplatform.commons.api.notification.service.NotificationService;
import org.exoplatform.container.PortalContainer;

public class ExecutorContextListener implements Callable<NotificationContext>{
  
  private static ExecutorContextListener  instance;
  private NotificationContext  ctx;
  
  @Override
  public NotificationContext call() throws Exception {
    // get all notification
    NotificationDataStorage dataStorage = (NotificationDataStorage)PortalContainer.getInstance()
                                              .getComponentInstanceOfType(NotificationDataStorage.class);
    
    NotificationService notificationService = (NotificationService)PortalContainer.getInstance()
        .getComponentInstanceOfType(NotificationService.class);
    
    notificationService.processNotificationMessages(dataStorage.emails());
    
    return ctx;
  }
  
  public static ExecutorContextListener getInstance(NotificationContext ctx) {
    if (instance == null) {
      synchronized (ExecutorContextListener.class) {
        if (instance == null) {
          instance = new ExecutorContextListener();
        }
      }
    }
    instance.ctx = ctx;

    return instance;
  }

}
