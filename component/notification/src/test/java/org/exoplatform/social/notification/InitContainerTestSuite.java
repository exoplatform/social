package org.exoplatform.social.notification;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.notification.plugin.ActivityCommentPluginTest;
import org.exoplatform.social.notification.plugin.ActivityMentionPluginTest;
import org.exoplatform.social.notification.plugin.LikePluginTest;
import org.exoplatform.social.notification.plugin.RelationshipReceivedRequestPluginTest;
import org.exoplatform.social.notification.plugin.RequestJoinSpacePluginTest;
import org.exoplatform.social.notification.plugin.SpaceInvitationPluginTest;
import org.exoplatform.social.notification.plugin.NewUserPluginTest;
import org.exoplatform.social.notification.plugin.PostActivityPluginTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  PostActivityPluginTest.class,
  NewUserPluginTest.class,
  RelationshipReceivedRequestPluginTest.class,
  RequestJoinSpacePluginTest.class,
  SpaceInvitationPluginTest.class,
  LikePluginTest.class,
  ActivityMentionPluginTest.class,
  ActivityCommentPluginTest.class,
  LinkProviderUtilsTest.class
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
