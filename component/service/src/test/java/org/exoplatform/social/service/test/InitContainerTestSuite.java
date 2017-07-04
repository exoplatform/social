/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.service.test;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.rest.impl.activity.ActivityRestResourcesTest;
import org.exoplatform.social.rest.impl.identity.IdentityRestResourcesTest;
import org.exoplatform.social.rest.impl.relationship.RelationshipsRestResourcesTest;
import org.exoplatform.social.rest.impl.space.SpaceRestResourcesTest;
import org.exoplatform.social.rest.impl.spacemembership.SpaceMembershipRestResourcesTest;
import org.exoplatform.social.rest.impl.userrelationship.UsersRelationshipsRestResourcesTest;
import org.exoplatform.social.rest.impl.users.UserRestResourcesTest;
import org.exoplatform.social.service.rest.ActivitiesRestServiceTest;
import org.exoplatform.social.service.rest.IdentityRestServiceTest;
import org.exoplatform.social.service.rest.IntranetNotificationsRestServiceTest;
import org.exoplatform.social.service.rest.LinkShareRestServiceTest;
import org.exoplatform.social.service.rest.NotificationsRestServiceTest;
import org.exoplatform.social.service.rest.PeopleRestServiceTest;
import org.exoplatform.social.service.rest.RestCheckerTest;
import org.exoplatform.social.service.rest.SecurityManagerTest;
import org.exoplatform.social.service.rest.SpaceRestServiceTest;
import org.exoplatform.social.service.rest.UtilTest;
import org.exoplatform.social.service.rest.api.ActivityResourcesTest;
import org.exoplatform.social.service.rest.api.ActivityStreamResourcesTest;
import org.exoplatform.social.service.rest.api.IdentityResourcesTest;
import org.exoplatform.social.service.rest.api.VersionResourcesTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  SpaceRestServiceTest.class,
  ActivityResourcesTest.class,
  ActivityStreamResourcesTest.class,
  IdentityResourcesTest.class,
  VersionResourcesTest.class,
  ActivitiesRestServiceTest.class,
  IdentityRestServiceTest.class,
  LinkShareRestServiceTest.class,
  //PeopleRestServiceTest.class,  //skipped until add integration test
  RestCheckerTest.class,
  SecurityManagerTest.class,
  UtilTest.class,
  IntranetNotificationsRestServiceTest.class,
  NotificationsRestServiceTest.class,
  ActivityRestResourcesTest.class,
  IdentityRestResourcesTest.class,
  RelationshipsRestResourcesTest.class,
  SpaceRestResourcesTest.class,
  SpaceMembershipRestResourcesTest.class,
  UsersRelationshipsRestResourcesTest.class,
  UserRestResourcesTest.class
  })
@ConfigTestCase(AbstractServiceTest.class)
public class InitContainerTestSuite extends BaseExoContainerTestSuite {
  
  @BeforeClass
  public static void setUp() throws Exception {
    initConfiguration(InitContainerTestSuite.class);
    beforeSetup();
  }

  @AfterClass
  public static void tearDown() {
    afterTearDown();
  }
}
