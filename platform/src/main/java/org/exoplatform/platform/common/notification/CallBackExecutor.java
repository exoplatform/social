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
package org.exoplatform.platform.common.notification;

import java.util.concurrent.Callable;

import org.exoplatform.commons.api.notification.service.NotificationContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class CallBackExecutor implements Callable<NotificationContext>{
  private static final Log LOG = ExoLogger.getExoLogger(CallBackExecutor.class);
  
  private static CallBackExecutor  instance;
  
  @Override
  public NotificationContext call() throws Exception {
    LOG.info("The callback successfully ... ");
    return null;
  }
  
  public static CallBackExecutor getInstance() {
    if (instance == null) {
      synchronized (CallBackExecutor.class) {
        if (instance == null) {
          instance = new CallBackExecutor();
        }
      }
    }

    return instance;
  }

}
