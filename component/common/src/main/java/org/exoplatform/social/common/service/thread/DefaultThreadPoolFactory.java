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
package org.exoplatform.social.common.service.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultThreadPoolFactory implements ThreadPoolFactory {

  public ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
      return Executors.newCachedThreadPool(threadFactory);
  }
  
  @Override
  public ExecutorService newThreadPool(ThreadPoolConfig config, ThreadFactory factory) {
      return newThreadPool(config.getPoolSize(), 
                           config.getMaxPoolSize(), 
                           config.getKeepAliveTime(),
                           config.getTimeUnit(),
                           config.getMaxQueueSize(), 
                           factory);
  }

  public ExecutorService newThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit,
                                       int maxQueueSize, ThreadFactory threadFactory) throws IllegalArgumentException {

      // the core pool size must be higher than 0
      if (corePoolSize < 1) {
          throw new IllegalArgumentException("CorePoolSize must be >= 1, was " + corePoolSize);
      }

      // validate max >= core
      if (maxPoolSize < corePoolSize) {
          throw new IllegalArgumentException("MaxPoolSize must be >= corePoolSize, was " + maxPoolSize + " >= " + corePoolSize);
      }

      BlockingQueue<Runnable> workQueue;
      if (corePoolSize == 0 && maxQueueSize <= 0) {
          // use a synchronous queue for direct-handover (no tasks stored on the queue)
          workQueue = new SynchronousQueue<Runnable>();
          // and force 1 as pool size to be able to create the thread pool by the JDK
          corePoolSize = 1;
          maxPoolSize = 1;
      } else if (maxQueueSize <= 0) {
          // use a synchronous queue for direct-handover (no tasks stored on the queue)
          workQueue = new SynchronousQueue<Runnable>();
      } else {
          // bounded task queue to store tasks on the queue
          workQueue = new LinkedBlockingQueue<Runnable>(maxQueueSize);
      }

      ThreadPoolExecutor answer = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, timeUnit, workQueue);
      answer.setThreadFactory(threadFactory);
      //sets TRUE : allows terminal if no tasks arrive within the keep-alive time
      //sets FALSE: When false, core threads are never terminated due to lack of incoming tasks.
      answer.allowCoreThreadTimeOut(true);
      answer.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
      return answer;
  }
  
  @Override
  public ScheduledExecutorService newScheduledThreadPool(ThreadPoolConfig config, ThreadFactory threadFactory) {
    return new ScheduledThreadPoolExecutor(config.getPoolSize(), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
  }

}

