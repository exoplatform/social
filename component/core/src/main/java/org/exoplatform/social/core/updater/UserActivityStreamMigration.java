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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.service.AsyncCallback;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.impl.SocialServiceContextImpl;
import org.exoplatform.social.common.service.utils.ConsoleUtils;
import org.exoplatform.social.common.service.utils.ObjectHelper;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.ProfileEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.impl.AbstractStorage;
import org.exoplatform.social.core.storage.impl.StorageUtils;
import org.exoplatform.social.core.storage.query.JCRProperties;
import org.exoplatform.social.core.storage.streams.SocialChromatticSyncProcessor;
import org.exoplatform.social.core.storage.streams.StreamProcessContext;

public class UserActivityStreamMigration extends AbstractStorage {
  
 private static final Log LOG = ExoLogger.getLogger(UserActivityStreamUpdaterPlugin.class);
 private static final int BATCH_FLUSH_LIMIT = 10;
  
 private IdentityStorage identityStorage = null;
 
 private static AtomicInteger currentNumber = new AtomicInteger(0);

  private IdentityStorage getIdentityStorage() {
    if (this.identityStorage == null) {
       this.identityStorage = (IdentityStorage) PortalContainer.getInstance().getComponentInstanceOfType(IdentityStorage.class);
    }
    
    return identityStorage;
  }
  /**
   * Upgrade base on range
   * @param from
   * @param to
   * @param prefix
   * @param numberOfActivities
   */
  public void upgrade(int limit, int from, int to, String prefix) {
    StringBuffer sb = new StringBuffer().append("SELECT * FROM soc:identitydefinition WHERE ");
    sb.append(JCRProperties.path.getName()).append(" LIKE '").append(getProviderRoot().getProviders().get(OrganizationIdentityProvider.NAME).getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR);
    sb.append("' AND NOT ").append(ProfileEntity.deleted.getName()).append(" = ").append("true");
    
    
    
    boolean hasGroup = false;
    //
    if ((to - from)> 0) {
      sb.append(" AND (");
      hasGroup = true;
    }
    
    String name;
    boolean hasOR = false;
    int i = from;
    for(;i <= to; i++) {
      
      
      if (hasOR) {
        sb.append(" OR ");
      }
      
      name = prefix + i;
      sb.append(IdentityEntity.remoteId.getName()).append(" = '").append(name).append("'");
      
      hasOR = true;
    }
    
    if (hasGroup) {
      sb.append(") ");
    }
    
    LOG.warn("SQL : " + sb.toString());
    
    NodeIterator it = nodes(sb.toString());
    long totalOfIdentity = to - from;
    Identity owner = null; 
    Node node = null;
    try {
      while (it.hasNext()) {
        node = (Node) it.next();
        owner = getIdentityStorage().findIdentityById(node.getUUID());

        doUpgrade(owner, totalOfIdentity, limit);
      }
    } catch (Exception e) {
      LOG.warn("Failed to migration for Activity Stream.");
    }
  }
  
  public void upgrade(int limit) {
    StringBuffer sb = new StringBuffer().append("SELECT * FROM soc:identitydefinition WHERE ");
    sb.append(JCRProperties.path.getName()).append(" LIKE '").append(getProviderRoot().getProviders().get(OrganizationIdentityProvider.NAME).getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR);
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
      LOG.error("Failed to migration for Activity Stream.", e);
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
      upgradeProcessor().start(processCtx);
      upgradeProcessor().process(processCtx);
      upgradeProcessor().end(processCtx);
      createAsyncCallback().done(processCtx);
    } catch (Exception e) {
      processCtx.setException(e);
    } finally {
      if (processCtx.isFailed()) {
        LOG.warn("Failed to migration for Activity Stream.", processCtx.getException());
      } else {
        LOG.info(processCtx.getTraceLog());
      }
    }
    
    return processCtx;
  }
  
  private SocialChromatticSyncProcessor upgradeProcessor() {
    return new SocialChromatticSyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        int limit = streamCtx.getLimit();
        
        //
        StreamUpgradeProcessor.feed(streamCtx.getIdentity()).upgrade(0, limit);
        StreamUpgradeProcessor.connection(streamCtx.getIdentity()).upgrade(0, limit);
        StreamUpgradeProcessor.myspaces(streamCtx.getIdentity()).upgrade(0, limit);
        StreamUpgradeProcessor.user(streamCtx.getIdentity()).upgrade(0, limit);
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
