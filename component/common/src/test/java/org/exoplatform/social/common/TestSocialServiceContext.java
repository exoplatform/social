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
package org.exoplatform.social.common;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

import org.exoplatform.social.common.service.AsyncProcessor;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.SocialServiceExecutor;
import org.exoplatform.social.common.service.impl.SocialServiceContextImpl;

public abstract class TestSocialServiceContext extends TestCase {

  private SocialServiceContext context;
  private SocialServiceExecutor serviceExecutor;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    createDefaultContext();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    context = null;
    serviceExecutor = null;
  }
  
  protected void createDefaultContext() {
    this.context = SocialServiceContextImpl.getInstance();
    this.serviceExecutor = context.getServiceExecutor();
  }
  
  protected SocialServiceContext getContext() {
    return context;
  }
  
  protected SocialServiceExecutor getExecutor() {
    return serviceExecutor;
  }
  
  protected abstract void config();
  
  protected abstract AsyncProcessor createAsyncProcessor();
  
  protected ProcessContext serviceExecute(ProcessContext processContext) throws ExecutionException, InterruptedException, TimeoutException {
    Future<ProcessContext> future = getExecutor().asyncExecute(createAsyncProcessor(), processContext);
    //
    future.get(10, TimeUnit.SECONDS);
    
    //
    assertTrue(future.isDone());
    return future.get();
  }
  
}
