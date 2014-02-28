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
package org.exoplatform.social.core.updater;

import java.util.concurrent.atomic.AtomicInteger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.service.AsyncCallback;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.impl.SocialServiceContextImpl;
import org.exoplatform.social.common.service.utils.ConsoleUtils;
import org.exoplatform.social.common.service.utils.ObjectHelper;
import org.exoplatform.social.core.chromattic.entity.ProfileEntity;
import org.exoplatform.social.core.chromattic.entity.ProviderEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.impl.AbstractStorage;
import org.exoplatform.social.core.storage.impl.StorageUtils;
import org.exoplatform.social.core.storage.query.JCRProperties;
import org.exoplatform.social.core.storage.streams.SocialChromatticAsyncProcessor;
import org.exoplatform.social.core.storage.streams.StreamProcessContext;

public class SpaceActivityStreamMigration extends AbstractStorage {
  
 private static final Log LOG = ExoLogger.getLogger(UserActivityStreamUpdaterPlugin.class);
  
 private IdentityStorage identityStorage = null;
 
 private static AtomicInteger currentNumber = new AtomicInteger(0);
 
 private static final int BATCH_FLUSH_LIMIT = 40;

  private IdentityStorage getIdentityStorage() {
    if (this.identityStorage == null) {
       this.identityStorage = (IdentityStorage) PortalContainer.getInstance().getComponentInstanceOfType(IdentityStorage.class);
    }
    
    return identityStorage;
  }
  
  public void upgrade(int limit) {
    StringBuffer sb = new StringBuffer().append("SELECT * FROM soc:identitydefinition WHERE ");
    ProviderEntity provider = getProviderRoot().getProviders().get(SpaceIdentityProvider.NAME);
    //nothing needs to migrate.
    if (provider == null) {
      return;
    }
    
    sb.append(JCRProperties.path.getName()).append(" LIKE '").append(provider.getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR);
    sb.append("' AND NOT ").append(ProfileEntity.deleted.getName()).append(" = ").append("true");
    
    LOG.warn("SQL : " + sb.toString());
    NodeIterator it = nodes(sb.toString());
    long totalOfIdentity = it.getSize();
    Identity owner = null; 
    Node node = null;
    int batchIndex = 0;
    int offset = 0;
    try {
      while (it.hasNext()) {
        node = (Node) it.next();
        owner = getIdentityStorage().findIdentityById(node.getUUID());
        doUpgrade(owner, totalOfIdentity, limit);
        batchIndex++;
        offset++;
        
        //
        if (batchIndex == BATCH_FLUSH_LIMIT) {
          LOG.warn("UPGRAGE SESSION FLUSH: " + offset);
          StorageUtils.persistJCR(true);
          it = nodes(sb.toString());
          _skip(it, offset);
          batchIndex = 0;
        }
      }
    } catch (Exception e) {
      LOG.warn("Failed to migration for Space Activity Stream.");
    } finally {
      StorageUtils.persistJCR(false);
      StorageUtils.endRequest();
    }
  }
  
  private ProcessContext doUpgrade(Identity owner, long total, int limit) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(String.format("%s-[%s limit=%s]", StreamProcessContext.UPGRADE_STREAM_PROCESS, owner.getRemoteId(), limit), ctx);
    processCtx.identity(owner).limit(limit).totalProcesses((int)total);
    
    try {
      //ctx.getServiceExecutor().async(upgradeProcessor(), processCtx, createAsyncCallback());
      processCtx.getTraceElement().start();
      upgradeProcessor().start(processCtx);
      upgradeProcessor().process(processCtx);
      upgradeProcessor().end(processCtx);
      processCtx.getTraceElement().end();
      createAsyncCallback().done(processCtx);
    } catch (Exception e) {
      processCtx.setException(e);
    } finally {
      if (processCtx.isFailed()) {
        LOG.warn("Failed to migration for Space Activity Stream.", processCtx.getException());
      } else {
        LOG.info(processCtx.getTraceLog());
      }
    }
    
    return processCtx;
  }
  
  private SocialChromatticAsyncProcessor upgradeProcessor() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        int limit = streamCtx.getLimit();
        
        //
        StreamUpgradeProcessor.space(streamCtx.getIdentity()).upgrade(0, limit);
        return processContext;
      }

    };
  }
  
  private AsyncCallback createAsyncCallback() {
    return new AsyncCallback() {
      @Override
      public void done(ProcessContext processContext) {
        int value = currentNumber.incrementAndGet();
        int percent = (value*100) / processContext.getTotalProcesses();
        ConsoleUtils.logProgBar(percent);
      }
    };
  }
  
}