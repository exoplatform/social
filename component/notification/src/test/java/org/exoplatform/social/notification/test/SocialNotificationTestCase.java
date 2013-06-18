/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.test;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManagerImpl;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImpl;
import org.exoplatform.social.notification.AbstractCoreTest;
import org.exoplatform.social.notification.MaxQueryNumber;
import org.exoplatform.social.notification.QueryNumberTest;
import org.exoplatform.social.notification.SocialEmailUtils;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@QueryNumberTest
public class SocialNotificationTestCase extends AbstractCoreTest {
  private IdentityStorage identityStorage;
  private ActivityManagerImpl activityManager;
  private RelationshipStorageImpl relationshipStorage;
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Space>  tearDownSpaceList;
  private SpaceStorage spaceStorage;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    identityStorage = SocialEmailUtils.getService(IdentityStorage.class);
    activityManager = SocialEmailUtils.getService(ActivityManagerImpl.class);
    relationshipStorage = SocialEmailUtils.getService(RelationshipStorageImpl.class);
    spaceStorage = SocialEmailUtils.getService(SpaceStorage.class);
    
    assertNotNull(identityStorage);
    assertNotNull(activityManager);
    assertNotNull(relationshipStorage);

    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");

    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    assertNotNull(rootIdentity.getId());
    assertNotNull(johnIdentity.getId());
    assertNotNull(maryIdentity.getId());
    assertNotNull(demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();
  }

  @Override
  protected void tearDown() throws Exception {

    for (ExoSocialActivity activity : tearDownActivityList) {
      activityManager.deleteActivity(activity.getId());
    }

    for (Space sp : tearDownSpaceList) {
      spaceStorage.deleteSpace(sp.getId());
    }

    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);

    super.tearDown();
  }

  @MaxQueryNumber(100)
  public void testSaveActivity() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title ");
    activityManager.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());
    
    assertEquals(3, SocialEmailUtils.getSocialEmailStorage().emails().size());
    
    //
    ExoSocialActivity got = activityManager.getActivity(activity.getId());
    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());

    //
    ExoSocialActivity act = new ExoSocialActivityImpl();
    act.setTitle("@#$%^&*(()_+:<>?");
    activityManager.saveActivity(rootIdentity, act);
    assertNotNull(act.getId());
  }
}
