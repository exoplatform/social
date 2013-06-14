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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common.service;

import java.util.concurrent.Callable;

import org.exoplatform.social.common.service.utils.TraceElement;

public class LogWatchCallable<V> implements Callable<V> {

  private Callable<V> wrappedTask;

  private TraceElement trace;

  public LogWatchCallable(Callable<V> task, TraceElement trace) {
    this.wrappedTask = task;
    this.trace = trace;
  }

  public Callable<V> getWrappedTask() {
    return wrappedTask;
  }

  public TraceElement getTraceElement() {
    return trace;
  }

  public V call() throws Exception {
    try {
      trace.start();
      return wrappedTask.call();
    } finally {
      trace.end();
    }
  }
}
