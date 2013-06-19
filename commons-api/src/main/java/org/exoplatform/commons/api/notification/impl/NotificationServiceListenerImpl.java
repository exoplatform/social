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
package org.exoplatform.commons.api.notification.impl;

import java.util.Calendar;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.exoplatform.commons.api.notification.listener.ExecutorContextListener;
import org.exoplatform.commons.api.notification.service.NotificationContext;
import org.exoplatform.commons.api.notification.service.NotificationServiceListener;

public class NotificationServiceListenerImpl implements NotificationServiceListener<NotificationContext> {

  private final static int TIME_PEDDING = 2; // minutes
  private final static int NUMBER_NOTIFICATION = 10; // number of notification pedding

  private ExecutorService executor;
  
  public NotificationServiceListenerImpl() {
    executor = Executors.newFixedThreadPool(1);
  }

  @Override
  public void processListener(NotificationContext ctx) {
    long currentTime = Calendar.getInstance().getTimeInMillis();
    if (ctx.getSize() > NUMBER_NOTIFICATION || (currentTime - ctx.getTime() > TIME_PEDDING * 60 * 1000)) {
      CompletionService<NotificationContext> cs = new ExecutorCompletionService<NotificationContext>(executor);
      cs.submit(ExecutorContextListener.getInstance(ctx));
    }
  }

}
