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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.service.AsyncCallback;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.impl.SocialServiceContextImpl;
import org.exoplatform.social.common.service.utils.ConsoleUtils;
import org.exoplatform.social.common.service.utils.ObjectHelper;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.streams.SocialChromatticAsyncProcessor;
import org.exoplatform.social.core.storage.streams.StreamProcessContext;

public class UserActivityStreamUpdaterPlugin extends UpgradeProductPlugin {
  private static final Log LOG = ExoLogger.getLogger(UserActivityStreamUpdaterPlugin.class);
  
  private IdentityStorage identityStorage = null;
  
  private static AtomicInteger currentNumber = new AtomicInteger(0);

  public UserActivityStreamUpdaterPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    upgrade();
  }
  
  private IdentityStorage getIdentityStorage() {
    if (this.identityStorage == null) {
       this.identityStorage = (IdentityStorage) PortalContainer.getInstance().getComponentInstanceOfType(IdentityStorage.class);
    }
    
    return identityStorage;
  }
  
  private void upgrade() {
    
    ProfileFilter profileFilter = new ProfileFilter();
    
    long offset = 0;
    long limit = 100;
    int totalSize = getIdentityStorage().getIdentitiesByProfileFilterCount(OrganizationIdentityProvider.NAME, profileFilter);
    
    List<Identity> identities = new ArrayList<Identity>();
    limit = Math.min(limit, totalSize);
    int loaded = loadIdentityRange(profileFilter, offset, limit, identities);
    
    if (limit != totalSize) {
      while (loaded == 100) {
        offset += limit;
        
        //prevent to over totalSize
        if (offset + limit > totalSize) {
          limit = totalSize - offset;
        }
        
        //
        loaded = loadIdentityRange(profileFilter, offset, limit, identities);
      }
    }
    
    int totalOfIdentity = identities.size();
    for(Identity owner : identities) {
      doUpgrade(owner, totalOfIdentity);
    }
  }
  
  private int loadIdentityRange(ProfileFilter filter, long  offset, long limit, List<Identity> result) {
    List<Identity> got = getIdentityStorage().getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, offset, limit, false);
    result.addAll(got);
    //
    return got.size();
  }
  
  private ProcessContext doUpgrade(Identity owner, int total) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(String.format("%s-[%s]", StreamProcessContext.UPGRADE_STREAM_PROCESS, owner.getRemoteId()), ctx);
    processCtx.identity(owner).totalProcesses(total);
    
    try {
      ctx.getServiceExecutor().async(upgradeProcessor(), processCtx, createAsyncCallback());
    } finally {
      if (processCtx.isFailed()) {
        LOG.warn("Failed to migration for Activity Stream.", processCtx.getException());
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
        
        //
        StreamUpgradeProcessor.feed(streamCtx.getIdentity()).upgrade();
        StreamUpgradeProcessor.connection(streamCtx.getIdentity()).upgrade();
        StreamUpgradeProcessor.myspaces(streamCtx.getIdentity()).upgrade();
        StreamUpgradeProcessor.user(streamCtx.getIdentity()).upgrade();
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
        ConsoleUtils.consoleProgBar(percent);
      }
    };
  }
  
  
  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return VersionComparator.isAfter(newVersion, previousVersion);
  }

}