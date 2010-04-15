package org.exoplatform.social.activity;

import junit.framework.TestCase;

import org.exoplatform.social.core.LinkProvider;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.mockito.Mockito;

public class TestUsernameProcessor extends TestCase {

  public TestUsernameProcessor() throws Exception {
    super();
  }

  public void testSubstituteUsernames() throws Exception {

    LinkProvider linkProvider = Mockito.mock(LinkProvider.class);
    Mockito.when(linkProvider.getProfileLink(Mockito.anyString())).thenReturn("FOO", "BAR", "ZED", "JOE", "CAR");

    UsernameProcessor processor = new UsernameProcessor(null, linkProvider);

    Activity activity = null;
    processor.processActivity(activity);
    assertNull(activity);

    activity = new Activity();
    processor.processActivity(activity);
    assertNull(activity.getTitle());
    assertNull(activity.getBody());

    activity.setTitle("single @root substitution");
    processor.processActivity(activity);
    assertEquals(activity.getTitle(), "single FOO substitution");
    assertNull(activity.getBody());

    activity.setTitle("@root and @john title");
    activity.setBody("body with @root and @john");
    processor.processActivity(activity);
    assertEquals(activity.getTitle(), "BAR and ZED title");
    assertEquals(activity.getBody(), "body with JOE and CAR");

  }
}
