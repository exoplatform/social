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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.social.common.service.AsyncProcessor;
import org.exoplatform.social.common.service.ExecutorServiceManager;
import org.exoplatform.social.common.service.LifecycleService;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.Processor;
import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.SocialServiceExecutor;

public class SocialServiceContextImpl implements SocialServiceContext {
  
  private static final String NAME = "DefaultSocialServiceContext";
  
  private ExecutorServiceManager executorServiceManager;
  
  private SocialServiceExecutor serviceExecutor;
  
  private List<LifecycleService> lifecycleServices = new ArrayList<LifecycleService>();
  
  private static SocialServiceContext instance;
  
  private SocialServiceContextImpl() {
    this.executorServiceManager = new ExecutorServiceManagerImpl(this);
    this.serviceExecutor = new SocialServiceExecutorImpl(this, executorServiceManager.newDefaultThreadPool("Social"));
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
    return this.serviceExecutor;
  }
  
  public ExecutorServiceManager getExecutorServiceManager() {
    return this.executorServiceManager;
}
  
}
