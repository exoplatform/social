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

import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.Processor;
import org.exoplatform.social.common.service.SocialServiceContext;

public abstract class SocialChromatticSyncProcessor implements Processor {

  protected SocialServiceContext socialContext;
  final String name;
  protected final PortalContainer container;
  protected ChromatticManager manager;
  protected ChromatticLifeCycle lifeCycle;

  public SocialChromatticSyncProcessor(SocialServiceContext socialContext) {
    this("SocialChromatticSyncProcessor", socialContext);
  }
  
  public SocialChromatticSyncProcessor(String name, SocialServiceContext socialContext) {
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
      processContext.setException(e);
    } finally {
    //end Session
      end(processContext);
    }
    
    return processContext;
  }
  
  protected abstract ProcessContext execute(ProcessContext processContext) throws Exception;
  
  @Override
  public void start(ProcessContext processContext) {
    processContext.getTraceElement().start();
    //SpaceUtils.endSyn(true);
  }
  
  @Override
  public void end(ProcessContext processContext) {
    processContext.getTraceElement().end();
    //SpaceUtils.endSyn(true);
  }
  
  @Override
  public String getName() {
    return name;
  }
}