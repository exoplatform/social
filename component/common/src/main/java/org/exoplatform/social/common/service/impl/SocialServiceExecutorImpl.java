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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.exoplatform.social.common.service.AsyncProcessor;
import org.exoplatform.social.common.service.LogWatchCallable;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.Processor;
import org.exoplatform.social.common.service.ServiceContext;
import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.SocialServiceExecutor;
import org.exoplatform.social.common.service.utils.AsyncProcessorTool;

public class SocialServiceExecutorImpl implements SocialServiceExecutor {

  private final SocialServiceContext context;
  private ExecutorService executor;
  
  public SocialServiceExecutorImpl(SocialServiceContext context) {
    this.context = context;
  }
  
  public SocialServiceExecutorImpl(SocialServiceContext context, ExecutorService executor) {
    this.context = context;
    this.executor = executor;
  }
  
  @Override
  public SocialServiceContext getSocialServiceContext() {
    return context;
  }

  @Override
  public ProcessContext execute(ServiceContext<ProcessContext> serviceContext, ProcessContext processorContext) {
    return serviceContext.execute(processorContext);
  }

  @Override
  public void setExecutorService(ExecutorService executorService) {
    this.executor = executorService;
  }
  
  @Override
  public Future<ProcessContext> asyncProcess(final AsyncProcessor asyncProcessor, final ProcessContext processorContext) {
    Callable<ProcessContext> task = new Callable<ProcessContext>() {
      public ProcessContext call() throws Exception {
          return process(asyncProcessor, processorContext);
      }
    };
    
    if (this.context.isTraced()) {
      task = new LogWatchCallable<ProcessContext>(task, processorContext.getTraceElement());
    }
    
    
    return getExecutorService().submit(task);
  }
  
  
  protected ProcessContext process(AsyncProcessor asyncProcessor,
                                     ProcessContext processorContext) throws Exception {
    AsyncProcessorTool.process(asyncProcessor, processorContext);
    return processorContext;
  }

  private ExecutorService getExecutorService() {
    if (executor != null) {
        return executor;
    }

    // create a default executor which must be synchronized
    synchronized (this) {
        if (executor != null) {
            return executor;
        }
        executor = context.getExecutorServiceManager().newDefaultThreadPool("DefaultSocialExecutor");
    }

    return executor;
  }

  @Override
  public ProcessContext async(AsyncProcessor asyncProcessor, ProcessContext processContext) {
    Future<ProcessContext> future = asyncProcess(asyncProcessor, processContext);
    //
    try {
      
      future.get(10, TimeUnit.SECONDS);
      
      //
      return future.get();
    } catch (InterruptedException e) {
      processContext.setException(e);
    } catch (ExecutionException e) {
      processContext.setException(e);
    } catch (TimeoutException e) {
      processContext.setException(e);
    }
    
    return processContext;
  }

}
