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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.common.service.ExecutorServiceManager;
import org.exoplatform.social.common.service.LifecycleService;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.SocialServiceExecutor;
import org.exoplatform.social.common.service.TraceFactory;
import org.exoplatform.social.common.service.thread.ThreadPoolConfig;
import org.exoplatform.social.common.service.utils.TraceList;

public class SocialServiceContextImpl implements SocialServiceContext {
  
  private static ExecutorServiceManager executorServiceManager = new ExecutorServiceManagerImpl();
  
  private final SocialServiceExecutor serviceExecutor;
  
  private List<LifecycleService> lifecycleServices = new ArrayList<LifecycleService>();
  
  private static SocialServiceContext instance;
  
  private final TraceFactory traceFactory;
  
  private TraceList traceList;
  
  private ProcessType isAsyn = ProcessType.SYNC;
  
  private SocialServiceContextImpl() {
    this.traceFactory = TraceFactory.defaultFactory;
    Object obj = PortalContainer.getInstance().getComponentInstanceOfType(ThreadPoolConfig.class);
    if (obj != null) {
      ThreadPoolConfig config = (ThreadPoolConfig)obj;
      boolean async = config.isAsyncMode();
      this.isAsyn = async ? ProcessType.ASYNC : ProcessType.SYNC;
      
      serviceExecutor = new SocialServiceExecutorImpl(executorServiceManager.newThreadPool("Social", config));
    } else {
      serviceExecutor = new SocialServiceExecutorImpl(executorServiceManager.newDefaultThreadPool("Social"));
    }
    
  }
  
  public static SocialServiceContext getInstance() {
    if (instance == null) {
      instance = new SocialServiceContextImpl();
    }
    
    return instance;
  }
  
  public static ProcessContext createProcessContext() {
    return new ProcessorContextImpl(getInstance());
  }
  
  @Override
  public List<LifecycleService> getLifecycleServices() {
    return null;
  }
  
  @Override
  public void addLifecycleService(LifecycleService lifecycleService) {
    lifecycleServices.add(lifecycleService);
  }

  @Override
  public SocialServiceExecutor getServiceExecutor() {
    return serviceExecutor;
  }
  
  public ExecutorServiceManager getExecutorServiceManager() {
    return executorServiceManager;
}

  @Override
  public TraceList getTraceList() {
    if (traceList == null) {
      traceList = traceFactory.make();
    }
    return traceList;
  }

  @Override
  public boolean isTraced() {
    return false;
  }
  
  @Override
  public boolean isAsync() {
    return this.isAsyn == ProcessType.ASYNC;
  }
  
}
