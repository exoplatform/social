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
import org.exoplatform.social.core.search.PeopleSearchConnectorTestCase;
import org.exoplatform.social.core.search.SpaceSearchConnectorTestCase;
import org.exoplatform.social.core.service.LinkProviderTest;
import org.exoplatform.social.core.space.SpaceLifeCycleTest;
import org.exoplatform.social.core.space.SpaceUtilsRestTest;
import org.exoplatform.social.core.space.SpaceUtilsWildCardMembershipTest;
import org.exoplatform.social.core.space.spi.SpaceServiceTest;
import org.exoplatform.social.core.storage.ActivityStorageTest;
import org.exoplatform.social.core.storage.ActivityStreamStorageTest;
import org.exoplatform.social.core.storage.IdentityStorageTest;
import org.exoplatform.social.core.storage.LazyActivityStorageTest;
import org.exoplatform.social.core.storage.RelationshipStorageTest;
import org.exoplatform.social.core.storage.SpaceStorageTest;
import org.exoplatform.social.core.storage.cache.CachedActivityStorageTestCase;
import org.exoplatform.social.core.storage.cache.CachedIdentityStorageTestCase;
import org.exoplatform.social.core.storage.cache.CachedRelationshipStorageTestCase;
import org.exoplatform.social.core.storage.cache.CachedSpaceStorageTestCase;
import org.exoplatform.social.core.storage.impl.ActivityStorageImplTestCase;
import org.exoplatform.social.core.storage.impl.IdentityStorageImplTestCase;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImplTestCase;
import org.exoplatform.social.core.updater.ActivityStreamUpdaterTest;
import org.exoplatform.social.core.updater.SpaceActivityStreamUpdaterTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({
  ActivityStreamUpdaterTest.class,
  LazyActivityStorageTest.class,
  SpaceActivityStreamUpdaterTest.class,
  SpaceLastVisitedTest.class,
  WhatsHotTest.class,
  ActivityManagerTest.class,
  ActivityStreamStorageTest.class,
  ActivityStorageTest.class,
  ActivityStorageImplTestCase.class,
  CachedActivityStorageTestCase.class,
  IdentityManagerTest.class,
  IdentityStorageImplTestCase.class,
  IdentityStorageTest.class,
  CachedIdentityStorageTestCase.class,
  SpaceServiceTest.class,
  CachedSpaceStorageTestCase.class,
  SpaceStorageTest.class,
  RelationshipManagerTest.class,
  CachedRelationshipStorageTestCase.class,
  RelationshipStorageTest.class,
  RelationshipPublisherTest.class,
  RelationshipStorageImplTestCase.class,
  SpaceUtilsRestTest.class,
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
  SpaceUtilsWildCardMembershipTest.class
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
