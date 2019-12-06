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
package org.exoplatform.commons.search.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.data.ModelChange;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.mockito.Mockito;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Mar 29, 2013  
 */
public class MockDataStorage implements DataStorage,Startable {

  private static final Log      LOG                  = ExoLogger.getLogger(MockDataStorage.class);

  static private HashMap<String, Page> pageCollections      = new HashMap<String, Page>();

  public static final String[]  PORTAL_CLASSIC__WIKI = new String[] { "portal::classic::wiki", PortalConfig.PORTAL_TYPE, "classic" };

  public static final String[]  USER_MARY_WIKI       = new String[] { "user::mary::wiki", PortalConfig.USER_TYPE, "mary" };

  public static final String[]  GROUP_USER_WIKI      = new String[] { "group::/platform/users::wiki", PortalConfig.GROUP_TYPE,
      "/platform/users"                             };

  public static final String[]  SPACE_EXO_WIKI       = new String[] { "group::/space/exo::wiki", PortalConfig.GROUP_TYPE, "/space/exo" };

  private List<String[]>        pageDatas            = Arrays.asList(new String[][] { PORTAL_CLASSIC__WIKI, USER_MARY_WIKI,
      GROUP_USER_WIKI, SPACE_EXO_WIKI               });

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#create(org.exoplatform.portal.config.model.PortalConfig)
   */
  @Override
  public void create(PortalConfig config) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#save(org.exoplatform.portal.config.model.PortalConfig)
   */
  @Override
  public void save(PortalConfig config) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#getPortalConfig(java.lang.String)
   */
  @Override
  public PortalConfig getPortalConfig(String portalName) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#getPortalConfig(java.lang.String, java.lang.String)
   */
  @Override
  public PortalConfig getPortalConfig(String ownerType, String portalName) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#remove(org.exoplatform.portal.config.model.PortalConfig)
   */
  @Override
  public void remove(PortalConfig config) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#getPage(java.lang.String)
   */
  @Override
  public Page getPage(String pageId) throws Exception {
    return pageCollections.get(pageId);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#clonePage(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page clonePage(String pageId, String clonedOwnerType, String clonedOwnerId, String clonedName) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#remove(org.exoplatform.portal.config.model.Page)
   */
  @Override
  public void remove(Page page) {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#create(org.exoplatform.portal.config.model.Page)
   */
  @Override
  public void create(Page page) {
    pageCollections.put(page.getId(), page);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#save(org.exoplatform.portal.config.model.Page)
   */
  @Override
  public List<ModelChange> save(Page page) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#save(org.exoplatform.portal.application.PortletPreferences)
   */
  public void save(PortletPreferences portletPreferences) throws Exception {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#getId(org.exoplatform.portal.config.model.ApplicationState)
   */
  @Override
  public <S> String getId(ApplicationState<S> state) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#load(org.exoplatform.portal.config.model.ApplicationState, org.exoplatform.portal.config.model.ApplicationType)
   */
  @Override
  public <S> S load(ApplicationState<S> state, ApplicationType<S> type) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#save(org.exoplatform.portal.config.model.ApplicationState, java.lang.Object)
   */
  @Override
  public <S> ApplicationState<S> save(ApplicationState<S> state, S preferences) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#getPortletPreferences(java.lang.String)
   */
  public PortletPreferences getPortletPreferences(String windowID) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#find(org.exoplatform.portal.config.Query)
   */
  @Override
  public <T> LazyPageList<T> find(Query<T> q) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#find(org.exoplatform.portal.config.Query, java.util.Comparator)
   */
  @Override
  public <T> LazyPageList<T> find(Query<T> q, Comparator<T> sortComparator) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#find2(org.exoplatform.portal.config.Query)
   */
  @Override
  public <T> ListAccess<T> find2(Query<T> q) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#find2(org.exoplatform.portal.config.Query, java.util.Comparator)
   */
  @Override
  public <T> ListAccess<T> find2(Query<T> q, Comparator<T> sortComparator) throws Exception {
    return null;
  }

  @Override
  public Container getSharedLayout(String siteName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#getAllPortalNames()
   */
  @Override
  public List<String> getAllPortalNames() throws Exception {
    return Arrays.asList("intranet", "acme");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#getAllGroupNames()
   */
  @Override
  public List<String> getAllGroupNames() throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#adapt(org.exoplatform.portal.config.model.ModelObject, java.lang.Class)
   */
  @Override
  public <A> A adapt(ModelObject modelObject, Class<A> type) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#adapt(org.exoplatform.portal.config.model.ModelObject, java.lang.Class, boolean)
   */
  @Override
  public <A> A adapt(ModelObject modelObject, Class<A> type, boolean create) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  @Override
  public void start() {
    try {
      for (String[] item : pageDatas) {
        Page page = Mockito.mock(Page.class);
        Mockito.when(page.getId()).thenReturn(item[0]);
        Mockito.when(page.getOwnerType()).thenReturn(item[1]);
        Mockito.when(page.getOwnerId()).thenReturn(item[2]);
        create(page);
      }
    } catch (Exception e) {
      LOG.warn("Could not create default page data for test", e);
    }
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  @Override
  public void stop() {
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.config.DataStorage#save()
   */
  @Override
  public void save() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String[] getSiteInfo(String applicationStorageId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <S> Application<S> getApplicationModel(String applicationStorageId)
      throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
