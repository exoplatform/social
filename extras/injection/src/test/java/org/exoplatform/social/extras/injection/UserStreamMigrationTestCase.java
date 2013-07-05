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
package org.exoplatform.social.extras.injection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml")
})
public class UserStreamMigrationTestCase extends AbstractKernelTest {

  private IdentityInjector identityInjector;
  private ActivityInjector activityInjector;
  private UserStreamMigrationInjector streamInjector;
  private IdentityManager identityManager;
  private ActivityStorage activityStorage;
  private ActivityStreamStorage streamStorage;
  private ActivityManager activityManager;
  private List<ExoSocialActivity> tearDownActivityList;

  private HashMap<String, String> params;
  
  @Override
  public void setUp() throws Exception {
    begin();
    
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    
    activityStorage = (ActivityStorage) getContainer().getComponentInstanceOfType(ActivityStorage.class);
    streamStorage = (ActivityStreamStorage) getContainer().getComponentInstanceOfType(ActivityStreamStorage.class);
    identityInjector = (IdentityInjector) getContainer().getComponentInstanceOfType(IdentityInjector.class);
    activityInjector = (ActivityInjector) getContainer().getComponentInstanceOfType(ActivityInjector.class);
    streamInjector = (UserStreamMigrationInjector) getContainer().getComponentInstanceOfType(UserStreamMigrationInjector.class);
    
    activityStorage.setInjectStreams(false);
    
    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    params = new HashMap<String, String>();
  }

  @Override
  public void tearDown() throws Exception {

    for (ExoSocialActivity activity : tearDownActivityList) {
      activityStorage.deleteActivity(activity.getId());
    }
    
    activityStorage.setInjectStreams(true);
    //
    end();
    
  }
  
  public void testMigration() throws Exception {
    
    String userBaseName = "bench.user";
    performActivityInjection(null);
    
    params.clear();
    params.put("number", "20");
    params.put("fromUser", "0");
    params.put("toUser", "4");
    params.put("userPrefix", userBaseName);
    streamInjector.inject(params);
    
    Identity user0 = identityManager.getOrCreateIdentity("organization", userBaseName + "0", false);
    Identity user1 = identityManager.getOrCreateIdentity("organization", userBaseName + "1", false);
    
    
    List<ExoSocialActivity> got = activityStorage.getActivityFeed(user0, 0, 100);
    assertEquals(0, got.size());
    
    got = activityStorage.getActivityFeed(user1, 0, 100);
    assertEquals(0, got.size());
    
    
    params.clear();
    params.put("number", "2");
    params.put("fromUser", "5");
    params.put("toUser", "9");
    params.put("userPrefix", userBaseName);
    streamInjector.inject(params);
    
    Identity user5 = identityManager.getOrCreateIdentity("organization", userBaseName + "5", false);
    Identity user9 = identityManager.getOrCreateIdentity("organization", userBaseName + "9", false);
    
    got = streamStorage.getFeed(user5, 0, 100);
    assertEquals(2, got.size());
    
    got = streamStorage.getFeed(user9, 0, 100);
    assertEquals(2, got.size());
  }
  
  /**
   * Inject data for Unit Testing
   * @param userPrefix
   * @throws Exception
   */
  private void performActivityInjection(String userPrefix) throws Exception {

    //
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    //
    params.put("number", "10");
    params.put("prefix", userBaseName);
    
    identityInjector.inject(params);
    
    //
    params.put("number", "5");
    params.put("fromUser", "5");
    params.put("toUser", "9");
    params.put("type", "user");
    params.put("userPrefix", userBaseName);
    activityInjector.inject(params);
    
    Identity user5 = identityManager.getOrCreateIdentity("organization", userBaseName + "5", false);
    assertEquals(5, activityStorage.getNumberOfActivitesOnActivityFeedForUpgrade(user5));

    Identity user9 = identityManager.getOrCreateIdentity("organization", userBaseName + "9", false);
    assertEquals(5, activityStorage.getNumberOfActivitesOnActivityFeedForUpgrade(user9));
  }
}
