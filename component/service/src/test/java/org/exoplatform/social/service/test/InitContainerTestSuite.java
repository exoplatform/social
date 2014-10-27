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
import org.exoplatform.social.service.rest.impl.activity.ActivitySocialRestServiceTest;
import org.exoplatform.social.service.rest.impl.identity.IdentitySocialRestServiceTest;
import org.exoplatform.social.service.rest.impl.relationship.RelationshipsRestServiceTest;
import org.exoplatform.social.service.rest.impl.space.SpaceSocialRestServiceTest;
import org.exoplatform.social.service.rest.impl.spacemembership.SpaceMembershipRestServiceTest;
import org.exoplatform.social.service.rest.impl.users.UsersRestserviceTest;
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
  PeopleRestServiceTest.class,
  RestCheckerTest.class,
  SecurityManagerTest.class,
  UtilTest.class,
  IntranetNotificationsRestServiceTest.class,
  NotificationsRestServiceTest.class,
  ActivitySocialRestServiceTest.class,
  IdentitySocialRestServiceTest.class,
  RelationshipsRestServiceTest.class,
  SpaceSocialRestServiceTest.class,
  SpaceMembershipRestServiceTest.class,
  //UserRelationshipsRestServiceTest.class,
  UsersRestserviceTest.class
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
