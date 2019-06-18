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

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.core.application.ProfileUpdatesPublisherTest;
import org.exoplatform.social.core.application.RelationshipPublisherTest;
import org.exoplatform.social.core.application.SpaceActivityPublisherTest;
import org.exoplatform.social.core.feature.SpaceLastVisitedTest;
import org.exoplatform.social.core.feature.WhatsHotTest;
import org.exoplatform.social.core.listeners.SocialUserProfileEventListenerImplTest;
import org.exoplatform.social.core.manager.ActivityManagerTest;
import org.exoplatform.social.core.manager.IdentityManagerTest;
import org.exoplatform.social.core.manager.RelationshipManagerTest;
import org.exoplatform.social.core.processor.MentionsProcessorTest;
import org.exoplatform.social.core.processor.OSHtmlSanitizerProcessorTest;
import org.exoplatform.social.core.processor.TemplateParamsProcessorTest;
import org.exoplatform.social.core.search.*;
import org.exoplatform.social.core.service.LinkProviderTest;
import org.exoplatform.social.core.space.SpaceLifeCycleTest;
import org.exoplatform.social.core.space.SpaceUtilsRestTest;
import org.exoplatform.social.core.space.SpaceUtilsTest;
import org.exoplatform.social.core.space.SpaceUtilsWildCardMembershipTest;
import org.exoplatform.social.core.space.spi.SpaceServiceTest;
import org.exoplatform.social.core.storage.*;
import org.exoplatform.social.core.storage.cache.JCRCachedActivityStorageTestCase;
import org.exoplatform.social.core.storage.cache.JCRCachedIdentityStorageTestCase;
import org.exoplatform.social.core.storage.cache.JCRCachedRelationshipStorageTestCase;
import org.exoplatform.social.core.storage.cache.JCRCachedSpaceStorageTestCase;
import org.exoplatform.social.core.storage.impl.ActivityStorageImplTestCase;
import org.exoplatform.social.core.storage.impl.IdentityStorageImplTestCase;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImplTestCase;
import org.exoplatform.social.core.updater.SpaceActivityStreamUpdaterTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({
  LazyActivityStorageTest.class,
  SpaceActivityStreamUpdaterTest.class,
  SpaceLastVisitedTest.class,
  WhatsHotTest.class,
  ActivityManagerTest.class,
  ActivityStreamStorageTest.class,
  ActivityStorageTest.class,
  ActivityStorageImplTestCase.class,
  JCRCachedActivityStorageTestCase.class,
  IdentityManagerTest.class,
  IdentityStorageImplTestCase.class,
  IdentityStorageTest.class,
  JCRCachedIdentityStorageTestCase.class,
  SpaceServiceTest.class,
  JCRCachedSpaceStorageTestCase.class,
  SpaceStorageTest.class,
  RelationshipManagerTest.class,
  JCRCachedRelationshipStorageTestCase.class,
  RelationshipStorageTest.class,
  RelationshipPublisherTest.class,
  RelationshipStorageImplTestCase.class,
  SpaceUtilsRestTest.class,
  SpaceUtilsTest.class,
  SpaceActivityPublisherTest.class,
  SpaceLifeCycleTest.class,
  SocialUserProfileEventListenerImplTest.class,
  OSHtmlSanitizerProcessorTest.class,
  TemplateParamsProcessorTest.class,
  ProfileUpdatesPublisherTest.class,
  MentionsProcessorTest.class,
  LinkProviderTest.class,
  PeopleSearchConnectorTestCase.class,
  SpaceSearchConnectorTestCase.class,
  SpaceUtilsWildCardMembershipTest.class,
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
