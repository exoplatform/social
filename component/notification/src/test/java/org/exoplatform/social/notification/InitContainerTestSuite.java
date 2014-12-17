package org.exoplatform.social.notification;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.notification.channel.template.ActivityCommentMailBuilderTest;
import org.exoplatform.social.notification.channel.template.ActivityMentionMailBuilderTest;
import org.exoplatform.social.notification.channel.template.LikeMailBuilderTest;
import org.exoplatform.social.notification.channel.template.NewUserMailBuilderTest;
import org.exoplatform.social.notification.channel.template.PostActivityMailBuilderTest;
import org.exoplatform.social.notification.channel.template.ReceiveRequestMailBuilderTest;
import org.exoplatform.social.notification.channel.template.RequestJoinSpaceMailBuilderTest;
import org.exoplatform.social.notification.channel.template.SpaceInvitationMailBuilderTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  ActivityMentionMailBuilderTest.class,
  ActivityCommentMailBuilderTest.class,
  PostActivityMailBuilderTest.class,
  NewUserMailBuilderTest.class,
  ReceiveRequestMailBuilderTest.class,
  RequestJoinSpaceMailBuilderTest.class,
  SpaceInvitationMailBuilderTest.class,
  LikeMailBuilderTest.class,
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
