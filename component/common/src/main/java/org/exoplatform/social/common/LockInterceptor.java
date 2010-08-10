/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 */
package org.exoplatform.social.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.gatein.pc.api.invocation.InvocationException;

public abstract class LockInterceptor<T extends Invocation> {

  /** . */
  private Map<Object, InternalLock> map     = new HashMap<Object, InternalLock>();

  /** . */
  private Lock                      mapLock = new ReentrantLock();

  public static class InternalLock {

    /** . */
    private final Object id;

    /** . */
    private final Lock   lock    = new ReentrantLock();

    /** . */
    private int          waiters = 0;

    public InternalLock(Object id) {
      this.id = id;
    }

    Object invoke(Invocation invocation) throws Exception, InvocationException {
      lock.lock();
      try {
        return invocation.invokeNext();
      } finally {
        lock.unlock();
      }
    }
  }

  protected InternalLock acquire(Object lockId) {
    mapLock.lock();
    try {
      InternalLock lock;
      if (map.containsKey(lockId)) {
        lock = (InternalLock) map.get(lockId);
      } else {
        lock = new InternalLock(lockId);
        map.put(lockId, lock);
      }
      lock.waiters++;
      return lock;
    } finally {
      mapLock.unlock();
    }
  }

  protected void release(InternalLock internalLock) {
    mapLock.lock();
    try {
      if (--internalLock.waiters == 0) {
        map.remove(internalLock.id);
      }
    } finally {
      mapLock.unlock();
    }
  }

  protected abstract Object getLockId(T invocation) throws InvocationException;

  public Object invoke(T invocation) throws Exception, InvocationException {
    Object lockId = getLockId(invocation);

    //
    if (lockId != null) {
      InternalLock internalLock = acquire(lockId);
      try {
        return internalLock.invoke(invocation);
      } finally {
        release(internalLock);
      }
    } else {
      return invocation.invokeNext();
    }
  }
}
