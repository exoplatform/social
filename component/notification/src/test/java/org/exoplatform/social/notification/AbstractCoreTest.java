/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManagerImpl;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManagerImpl;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.mock.MockNotificationService;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.common.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.notification.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml")
})
public abstract class AbstractCoreTest extends BaseExoTestCase {

  protected IdentityManager identityManager;
  protected ActivityManagerImpl activityManager;
  protected SpaceService spaceService;
  protected RelationshipManagerImpl relationshipManager;
  protected PluginContainer pluginService;
  protected MockNotificationService notificationService;
  protected PluginSettingService pluginSettingService;
  protected ExoFeatureService exoFeatureService;
  
  protected Session session;
  
  protected Identity rootIdentity;
  protected Identity johnIdentity;
  protected Identity maryIdentity;
  protected Identity demoIdentity;
  
  @Override
  protected void setUp() throws Exception {
    begin();
    session = getSession();

    //
    pluginService = Utils.getService(PluginContainer.class);
    identityManager = Utils.getService(IdentityManager.class);
    activityManager = Utils.getService(ActivityManagerImpl.class);
    spaceService = Utils.getService(SpaceService.class);
    relationshipManager = Utils.getService(RelationshipManagerImpl.class);
    notificationService = (MockNotificationService) Utils.getService(NotificationService.class);
    pluginSettingService = Utils.getService(PluginSettingService.class);
    exoFeatureService = Utils.getService(ExoFeatureService.class);
    
    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);
    
    Collection<NotificationInfo> messages = notificationService.emails();
    assertEquals(4, messages.size());
  }

  @Override
  protected void tearDown() throws Exception {
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    
    session = null;
    notificationService.clear();
    end();
  }
  
  private Session getSession() throws RepositoryException {
    PortalContainer container = PortalContainer.getInstance();
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstance(RepositoryService.class);
    ManageableRepository repository = repositoryService.getCurrentRepository();
    return repository.getSystemSession("portal-test");
  }

}
