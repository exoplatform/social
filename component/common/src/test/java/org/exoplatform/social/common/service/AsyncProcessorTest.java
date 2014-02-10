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

import java.util.concurrent.atomic.AtomicInteger;

import org.exoplatform.social.common.TestSocialServiceContext;
import org.exoplatform.social.common.service.impl.ProcessorContextImpl;
import org.exoplatform.social.common.service.utils.ConsoleUtils;

public class AsyncProcessorTest extends TestSocialServiceContext {

  private static AtomicInteger currentNumber = new AtomicInteger(0);
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  
  @Override
  protected void config() {
    
  }
  
  @Override
  protected AsyncProcessor createAsyncProcessor() {
    return new AsyncProcessor() {

      @Override
      public ProcessContext process(ProcessContext processContext) {
        processContext.setProperty("result", "done");
        processContext.done(true);
        return processContext;
      }

      @Override
      public String getName() {
        return "AsyncProcessor";
      }

      @Override
      public ProcessContext process(ProcessContext processorContext, AsyncCallback callback) {
        ProcessContext got = processorContext;
        try {
          got = process(processorContext);
          
          Thread.sleep(500);
          
          if (got.isDone()) {
            callback.done(got); 
          }
        } catch (Exception e) {
          processorContext.setException(e);
        }
        return got;
      }

      @Override
      public void start(ProcessContext processContext) {
        processContext.trace(getName(), "start()");
      }

      @Override
      public void end(ProcessContext processContext) {
        processContext.trace(getName(), "end()");
      }
      
    };
  }
  
  public void testServiceContext() throws Exception {
    ProcessContext params = new ProcessorContextImpl(getContext());
    params.setProcessorName("AsyncProcessorTest");
    params.setProperty("test0", "test0");
    params.setProperty("test1", "test1");
    
    
    params = getExecutor().execute(new ServiceContext<ProcessContext>() {
      @Override
      public ProcessContext execute(ProcessContext processContext) {
        processContext.setProperty("result", "done");
        return processContext;
      }
    }, params);
    
    assertEquals("done", params.getProperty("result", String.class));
  }
  
  
  public void testAysncCallback() throws Exception {

    ProcessContext params = new ProcessorContextImpl(getContext());
    params.setProcessorName("AsyncProcessorTest");
    params.setProperty("test0", "test0");
    params.setProperty("test1", "test1");
    params.totalProcesses(80);
    
    for(int i = 0; i < 80; i++) {
      ProcessContext got = serviceExecute(params, createAsyncCallback());
      Thread.sleep(50);
      assertEquals("done", got.getProperty("result", String.class));
    }
    
    Thread.sleep(1000);
  }

  private AsyncCallback createAsyncCallback() {
    return new AsyncCallback() {
      @Override
      public void done(ProcessContext processContext) {
        int value = currentNumber.incrementAndGet();
        int percent = (value *100) / processContext.getTotalProcesses();
        ConsoleUtils.logProgBar(percent);
      }
    };
  }
}
