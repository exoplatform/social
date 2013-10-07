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
package org.exoplatform.social.common.service.env;

import org.exoplatform.social.common.service.AsyncCallback;
import org.exoplatform.social.common.service.AsyncProcessor;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.SocialServiceContext;

public abstract class SocialChromatticAsyncProcessor implements AsyncProcessor {

  protected SocialServiceContext socialContext;
  final String name;
  
  public SocialChromatticAsyncProcessor(SocialServiceContext socialContext) {
    this.socialContext = socialContext;
    this.name = this.getName();
  }
  
  public SocialChromatticAsyncProcessor(String name, SocialServiceContext socialContext) {
    this.socialContext = socialContext;
    this.name = name;
  }
  
  @Override
  public ProcessContext process(ProcessContext processContext) {
    start(processContext);
    try {
      //execute
      processContext = execute(processContext);
    } catch (Exception e) {
      processContext.setException(e);
    } finally {
    //end Session
      end(processContext);
    }
    return processContext;
  }
  
  @Override
  public ProcessContext process(ProcessContext processContext, AsyncCallback callback) {
    //execute
    try {
      processContext = execute(processContext);
      
      if (processContext.isDone()) {
        callback.done(processContext);
      }
    } catch (Exception e) {
      processContext.setException(e);
      return processContext;
    }
    //end Session
    return processContext;
  }
  
  protected abstract ProcessContext execute(ProcessContext processContext) throws Exception;
  
  @Override
  public void start(ProcessContext processContext) {
    processContext.trace(getName(), "start chromattic session<==="); 
  }
  
  @Override
  public void end(ProcessContext processContext) {
    processContext.trace(getName(), "end chromattic session<===");
  }
  
  @Override
  public String getName() {
    return name;
  }
}
