package org.exoplatform.social.notification;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.notification.channel.MailTemplateProviderTest;
import org.exoplatform.social.notification.channel.WebTemplateProviderTest;
import org.exoplatform.social.notification.channel.template.*;

import org.exoplatform.social.notification.web.template.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  ActivityMentionWebBuilderTest.class,
  ActivityMentionMailBuilderTest.class,
  ActivityCommentMailBuilderTest.class,
  ActivityCommentReplyWebBuilderTest.class,
  ActivityCommentReplyMailBuilderTest.class,
  LikeCommentWebBuilderTest.class,
  PostActivityMailBuilderTest.class,
  NewUserMailBuilderTest.class,
  ReceiveRequestMailBuilderTest.class,
  RequestJoinSpaceMailBuilderTest.class,
  SpaceInvitationMailBuilderTest.class,
  LikeMailBuilderTest.class,
  LinkProviderUtilsTest.class,
  MailTemplateProviderTest.class,
  WebTemplateProviderTest.class,
  LikeWebBuilderTest.class,
  EditActivityMailBuilderTest.class,
  EditCommentMailBuilderTest.class,
  EditActivityWebBuilderTest.class,
  EditCommentWebBuilderTest.class
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
