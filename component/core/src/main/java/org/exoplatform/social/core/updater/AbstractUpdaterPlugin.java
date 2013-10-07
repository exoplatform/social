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

import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.core.chromattic.entity.ProviderRootEntity;

public abstract class AbstractUpdaterPlugin extends UpgradeProductPlugin {
  
  private static final Log LOG = ExoLogger.getLogger(AbstractUpdaterPlugin.class);
  
  //
  protected final SocialChromatticLifeCycle lifeCycle;
  protected static final String NODETYPE_PROVIDERS = "soc:providers";

  public AbstractUpdaterPlugin(InitParams initParams) {
    super(initParams);
    this.lifeCycle = lifecycleLookup();
  }
  
  protected ProviderRootEntity getProviderRoot() {
    if (lifeCycle.getProviderRoot().get() == null) {
      lifeCycle.getProviderRoot().set(getRoot(NODETYPE_PROVIDERS, ProviderRootEntity.class));
    }
    return (ProviderRootEntity) lifeCycle.getProviderRoot().get();
  }
  
  private <T> T getRoot(String nodetypeName, Class<T> t) {
    T got = getSession().findByPath(t, nodetypeName);
    if (got == null) {
      got = getSession().insert(t, nodetypeName);
    }
    return got;
  }
  
  
  /**
   * Gets NodeIterator with Statement with offset and limit
   * 
   * @param statement
   * @param offset
   * @param limit
   * @return
   * @throws Exception
   */
  protected NodeIterator nodes(String statement) {
    //
    if (statement == null) return null;
    
    //
    try {
      QueryManager queryMgr = getJCRSession().getWorkspace().getQueryManager();
      Query query = queryMgr.createQuery(statement, Query.SQL);
      if (query instanceof QueryImpl) {
        QueryImpl impl = (QueryImpl) query;
        
        return impl.execute().getNodes();
      }
      
      //
      return query.execute().getNodes();
    } catch (Exception ex) {
      LOG.error(ex);
    }
    
    return null;
  }
  
  /**
   * Gets NodeIterator with Statement with offset and limit
   * 
   * @param statement
   * @param offset
   * @param limit
   * @return
   * @throws Exception
   */
  protected NodeIterator nodes(String statement, long offset, long limit) {
    //
    if (statement == null) return null;
    
    //
    try {
      QueryManager queryMgr = getJCRSession().getWorkspace().getQueryManager();
      Query query = queryMgr.createQuery(statement, Query.SQL);
      if (query instanceof QueryImpl) {
        QueryImpl impl = (QueryImpl) query;
        
        //
        impl.setOffset(offset);
        impl.setLimit(limit);
        
        return impl.execute().getNodes();
      }
      
      //
      return query.execute().getNodes();
    } catch (Exception ex) {
      LOG.error(ex);
    }
    
    return null;
  }
  
  /**
   * 
   * @return
   */
  private SocialChromatticLifeCycle lifecycleLookup() {

    PortalContainer container = PortalContainer.getInstance();
    ChromatticManager manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
    return (SocialChromatticLifeCycle) manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);

  }
  
  private Session getJCRSession() {
    return lifeCycle.getSession().getJCRSession();
  }
  
  private ChromatticSession getSession() {
    return lifeCycle.getSession();
  }

}
