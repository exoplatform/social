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
package org.exoplatform.social.core.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runners.Suite.SuiteClasses;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.core.application.*;
import org.exoplatform.social.core.feature.SpaceLastVisitedTest;
import org.exoplatform.social.core.feature.WhatsHotTest;
import org.exoplatform.social.core.listeners.SocialUserProfileEventListenerImplTest;
import org.exoplatform.social.core.manager.*;
import org.exoplatform.social.core.processor.*;
import org.exoplatform.social.core.search.SortingTest;
import org.exoplatform.social.core.service.LinkProviderTest;
import org.exoplatform.social.core.space.*;
import org.exoplatform.social.core.space.spi.SpaceServiceTest;
import org.exoplatform.social.core.space.spi.SpaceTemplateServiceTest;
import org.exoplatform.social.core.storage.*;
import org.exoplatform.social.core.storage.impl.ActivityStorageImplTestCase;

@SuiteClasses({
//FIXME regression JCR to RDBMS migration
//    SpaceLastVisitedTest.class,
//FIXME regression JCR to RDBMS migration
//    WhatsHotTest.class,
    ActivityManagerTest.class,
//FIXME regression JCR to RDBMS migration
//    ActivityStorageTest.class,
//FIXME regression JCR to RDBMS migration
//    ActivityStorageImplTestCase.class,
    IdentityManagerTest.class,
//FIXME regression JCR to RDBMS migration
//    IdentityStorageTest.class,
    SpaceServiceTest.class,
//FIXME regression JCR to RDBMS migration
//    SpaceStorageTest.class,
    RelationshipManagerTest.class,
//FIXME regression JCR to RDBMS migration
//    RelationshipStorageTest.class,
//FIXME regression JCR to RDBMS migration
//    RelationshipPublisherTest.class,
//FIXME regression JCR to RDBMS migration
//    SpaceUtilsRestTest.class,
    SpaceUtilsTest.class,
    SpaceActivityPublisherTest.class,
    SpaceLifeCycleTest.class,
    SocialUserProfileEventListenerImplTest.class,
    OSHtmlSanitizerProcessorTest.class,
    TemplateParamsProcessorTest.class,
//FIXME regression JCR to RDBMS migration
//    ProfileUpdatesPublisherTest.class,
//FIXME regression JCR to RDBMS migration
//    MentionsProcessorTest.class,
    LinkProviderTest.class,
//FIXME regression JCR to RDBMS migration
//    SpaceUtilsWildCardMembershipTest.class,
    SpaceTemplateServiceTest.class,
    SortingTest.class,
})
@ConfigTestCase(AbstractCoreTest.class)
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
