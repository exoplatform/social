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

import java.util.concurrent.ExecutorService;

import org.exoplatform.social.common.service.thread.ThreadPoolConfig;

public interface ExecutorServiceManager {

  /**
   * Sets the thread name pattern used for creating the full thread name.
   * <p/>
   * @param pattern the pattern
   * @throws IllegalArgumentException if the pattern is invalid.
   */
  void setThreadNamePattern(String pattern) throws IllegalArgumentException;

  /**
   * Gets the thread name patter to use
   *
   * @return the pattern
   */
  String getThreadNamePattern();
  /**
   * Creates a new daemon thread with the given name.
   *
   * @param name     name which is appended to the thread name
   * @param runnable a runnable to be executed by new thread instance
   * @return the created thread
   */
  Thread newThread(String name, Runnable runnable);
  
  /**
   * Creates a new thread pool using the default thread pool profile.
   *
   * @param source the source object, usually it should be <tt>this</tt> passed in as parameter
   * @param name   name which is appended to the thread name
   * @return the created thread pool
   */
  ExecutorService newDefaultThreadPool(String name);
  
  ThreadPoolConfig getThreadPoolConfig(String id);
  
  ExecutorService newThreadPool(String name, ThreadPoolConfig config);
  
}
