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
package org.exoplatform.social.common.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.exoplatform.social.common.service.ExecutorServiceManager;
import org.exoplatform.social.common.service.thread.DefaultThreadPoolFactory;
import org.exoplatform.social.common.service.thread.SocialThreadFactory;
import org.exoplatform.social.common.service.thread.ThreadPoolConfig;
import org.exoplatform.social.common.service.thread.ThreadPoolFactory;

public class ExecutorServiceManagerImpl implements ExecutorServiceManager {
  
  private ThreadPoolFactory threadPoolFactory = new DefaultThreadPoolFactory();
  private String threadNamePattern;
  private final Map<String, ThreadPoolConfig> threadPoolProfiles = new HashMap<String, ThreadPoolConfig>();
  
  private ThreadPoolConfig defaultConfig;
  
  public ExecutorServiceManagerImpl() {
    defaultConfig = new ThreadPoolConfig();
    
    defaultConfig.setDefaultProfile(true);
    defaultConfig.setPoolSize(10);
    defaultConfig.setMaxPoolSize(20);
    defaultConfig.setKeepAliveTime(10L);
    defaultConfig.setTimeUnit(TimeUnit.SECONDS);
    defaultConfig.setMaxQueueSize(1000);
    defaultConfig.setPriority(Thread.NORM_PRIORITY);
  }

  @Override
  public void setThreadNamePattern(String pattern) throws IllegalArgumentException {
    
  }

  public String getThreadNamePattern() {
    return null;
  }

  @Override
  public Thread newThread(String name, Runnable runnable) {
    return null;
  }

  @Override
  public ExecutorService newDefaultThreadPool(String name) {
    return newThreadPool(name, this.defaultConfig);
  }
  
  
  @Override
  public ThreadPoolConfig getThreadPoolConfig(String id) {
      return threadPoolProfiles.get(id);
  }
  
  @Override
  public ExecutorService newThreadPool(String name, ThreadPoolConfig config) {
    ThreadFactory threadFactory = createThreadFactory(name, true, config.getPriority());
    ExecutorService executorService = threadPoolFactory.newThreadPool(config, threadFactory);

    //
    return executorService;
  }

  private ThreadFactory createThreadFactory(String name, boolean isDaemon, int priority) {
    return new SocialThreadFactory(threadNamePattern, name, isDaemon, priority);
  }

}
