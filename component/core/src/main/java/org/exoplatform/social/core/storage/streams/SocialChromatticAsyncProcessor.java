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
package org.exoplatform.social.core.storage.streams;

import java.util.concurrent.atomic.AtomicBoolean;

import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.common.service.AsyncCallback;
import org.exoplatform.social.common.service.AsyncProcessor;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.SocialServiceContext;

public abstract class SocialChromatticAsyncProcessor implements AsyncProcessor {
  
  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(SocialChromatticAsyncProcessor.class);
  
  protected SocialServiceContext socialContext;
  final String name;
  protected final PortalContainer container;
  protected ChromatticManager manager;
  protected ChromatticLifeCycle lifeCycle;
  private AtomicBoolean startedRequest = new AtomicBoolean(false);

  public SocialChromatticAsyncProcessor(SocialServiceContext socialContext) {
    this("SocialChromatticAsyncProcessor", socialContext);
  }
  
  public SocialChromatticAsyncProcessor(String name, SocialServiceContext socialContext) {
    this.socialContext = socialContext;
    this.name = name;
    
    this.container = PortalContainer.getInstance();
    
    this.manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
    if (manager != null) {
      this.lifeCycle = manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);
    }
  }
  
  @Override
  public ProcessContext process(ProcessContext processContext) {
    
    try {
      start(processContext);
      //execute
      processContext = execute(processContext);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
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
      
      if (processContext.isFailed() == false) {
        callback.done(processContext);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      processContext.setException(e);
      return processContext;
    }
    //end Session
    return processContext;
  }
  
  protected abstract ProcessContext execute(ProcessContext processContext) throws Exception;
  
  @Override
  public void start(ProcessContext processContext) {
    startedRequest.set(startSynchronization());
  }
  
  @Override
  public void end(ProcessContext processContext) {
    stopSynchronization(startedRequest.get());
  }
  
  private boolean startSynchronization() {
    
    if (lifeCycle.getManager().getSynchronization() == null) {
      lifeCycle.getManager().beginRequest();
      return true;
    }
    return false;
  }

  private void stopSynchronization(boolean requestClose) {

    if (requestClose) {
      lifeCycle.getManager().endRequest(true);
    }
  }
  
  @Override
  public String getName() {
    return name;
  }
}

