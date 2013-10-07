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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class ActivityStreamUpdaterTest extends AbstractCoreTest {
  
  private IdentityStorage identityStorage;
  private ActivityStorage activityStorage;
  private ActivityStreamStorage streamStorage;
  private List<ExoSocialActivity> tearDownActivityList;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;
  private Identity ghostIdentity;
  private Identity paulIdentity;
  private Identity raulIdentity;
  private Identity jameIdentity;
 
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityStorage = (ActivityStorage) getContainer().getComponentInstanceOfType(ActivityStorage.class);
    streamStorage = (ActivityStreamStorage) getContainer().getComponentInstanceOfType(ActivityStreamStorage.class);
    
    activityStorage.setInjectStreams(false);
    
    //
    assertNotNull("identityManager must not be null", identityStorage);
    assertNotNull("activityStorage must not be null", activityStorage);
    rootIdentity = new Identity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = new Identity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = new Identity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = new Identity(OrganizationIdentityProvider.NAME, "demo");
    ghostIdentity = new Identity(OrganizationIdentityProvider.NAME, "ghost");
    paulIdentity = new Identity(OrganizationIdentityProvider.NAME, "paul");
    raulIdentity = new Identity(OrganizationIdentityProvider.NAME, "raul");
    jameIdentity = new Identity(OrganizationIdentityProvider.NAME, "jame");
    
    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);
    identityStorage.saveIdentity(ghostIdentity);
    identityStorage.saveIdentity(paulIdentity);
    identityStorage.saveIdentity(raulIdentity);
    identityStorage.saveIdentity(jameIdentity);

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
  }

  @Override
  protected void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      activityStorage.deleteActivity(activity.getId());
    }
    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);
    identityStorage.deleteIdentity(ghostIdentity);
    identityStorage.deleteIdentity(paulIdentity);
    identityStorage.deleteIdentity(raulIdentity);
    identityStorage.deleteIdentity(jameIdentity);
    
    activityStorage.setInjectStreams(true);
    super.tearDown();
  }

  
  public void testFeedUpdater() throws Exception {
    final String activityTitle = "activity Title";
    
    for(int i = 0; i < 100; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle + i);
      activityStorage.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
    }
    
    for(int i = 0; i < 100; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle + i);
      activityStorage.saveActivity(demoIdentity, activity);
      tearDownActivityList.add(activity);
    }
    
    for(int i = 0; i < 100; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle + i);
      activityStorage.saveActivity(maryIdentity, activity);
      tearDownActivityList.add(activity);
    }
    
    for(int i = 0; i < 100; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle + i);
      activityStorage.saveActivity(johnIdentity, activity);
      tearDownActivityList.add(activity);
    }
    
    ValueParam param = new ValueParam();
    param.setName("limit");
    param.setValue("20");
    InitParams params = new InitParams();
    params.addParameter(param);
    
    UserActivityStreamUpdaterPlugin updaterPlugin = new UserActivityStreamUpdaterPlugin(params);
    
    assertNotNull(updaterPlugin);
    updaterPlugin.processUpgrade("1.2.x", "4.0");
    
    assertEquals(100, streamStorage.getNumberOfFeed(rootIdentity));
    assertEquals(100, streamStorage.getNumberOfFeed(demoIdentity));
    assertEquals(100, streamStorage.getNumberOfFeed(maryIdentity));
    assertEquals(100, streamStorage.getNumberOfFeed(johnIdentity));
    
    List<ExoSocialActivity> got = activityStorage.getActivityFeed(rootIdentity, 0, 100);
    assertEquals(100, got.size());
  }
}
