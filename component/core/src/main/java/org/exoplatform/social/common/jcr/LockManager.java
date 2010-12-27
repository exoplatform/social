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
 * along with this program; if not, see&lt;http://www.gnu.org/licenses/&gt;.
 */
package org.exoplatform.social.common.jcr;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * This class is used to manage the locks of the application. Those locks
 * will allow us to prevent any concurrent modifications on the same resource
 * that can only be identified by a String Id.
 *
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * Dec 1, 2010
 */
public class LockManager {

  /**
   * The logger
   */
  private static final Log LOG = ExoLogger.getExoLogger(LockManager.class);

  /**
   * "concurrency-level".
   */
  public static final String INIT_PARAM_CONCURRENCY_LEVEL = "concurrency-level";

  /**
   * Default concurrency level.
   */
  private static final int DEFAULT_CONCURRENCY_LEVEL = 64;

  /**
   * The map that contains all the locks
   */
  private final ConcurrentMap<String, Lock> locks;

  /**
   * @param concurrencyLevel The concurrency level to use
   */
  private LockManager(int concurrencyLevel) {
    locks = new ConcurrentHashMap<String, Lock>(concurrencyLevel, 0.75f, concurrencyLevel);
  }

  /**
   * The default constructor that uses <code>DEFAULT_CONCURRENCY_LEVEL</code> as concurrency level
   */
  public LockManager() {
    this(DEFAULT_CONCURRENCY_LEVEL);
  }

  /**
   * @param params the {@link InitParams} from which the concurrency level will be extracted
   */
  public LockManager(InitParams params) {
    this(getConcurrencyLevel(params));
  }

  /**
   * Extracts the concurrency level from the given {@link InitParams}
   * @param ip the {@link InitParams} from which it will extract the concurrency level
   * @return the value of the {@link ValueParam} <code>INIT_PARAM_CONCURRENCY_LEVEL</code> if it exists
   * <code>DEFAULT_CONCURRENCY_LEVEL</code> otherwise.
   */
  private static int getConcurrencyLevel(InitParams ip) {
    try {
      return Integer.valueOf(ip.getValueParam(INIT_PARAM_CONCURRENCY_LEVEL).getValue());
    }
    catch (NullPointerException e) {
      LOG.debug("Parameter " + INIT_PARAM_CONCURRENCY_LEVEL + " was not found in configuration, default "
              + DEFAULT_CONCURRENCY_LEVEL + " will be used.");
      return DEFAULT_CONCURRENCY_LEVEL;
    }
    catch (Exception e) {
      LOG.error("Can't parse parameter " + INIT_PARAM_CONCURRENCY_LEVEL, e);
      return DEFAULT_CONCURRENCY_LEVEL;
    }
  }

  /**
   * Gives the lock related to the given type and given id
   * @param type the type of the object for which we want a lock
   * @param id the unique id of the object for which we want a lock
   * @return the existing lock if a lock exist for the given id and given type
   * otherwise a new lock
   */
  public Lock getLock(String type, String id) {
    String fullId = new StringBuilder(type.length() + id.length() + 1).append(type).
            append('-').append(id).toString();
    Lock lock = locks.get(fullId);
    if (lock != null) {
      return lock;
    }
    lock = new InternalLock(fullId);
    Lock prevLock = locks.putIfAbsent(fullId, lock);
    if (prevLock != null)
    {
      lock = prevLock;
    }
    return lock;
  }

  /**
   * This kind of locks can self unregister from the map of locks
   */
  private class InternalLock extends ReentrantLock {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -3362387346368015145L;

    /**
     * The id corresponding to the lock in the map
     */
    private final String fullId;

    /**
     * The default constructor
     * @param fullId the id corresponding to the lock in the map
     */
    public InternalLock(String fullId) {
      super();
      this.fullId = fullId;
    }

    @Override
    public void unlock() {
      if (!hasQueuedThreads()) {
        // No thread is currently waiting for this lock
        // The lock will then be removed
        locks.remove(fullId, this);
      }
      super.unlock();
    }
  }
}
