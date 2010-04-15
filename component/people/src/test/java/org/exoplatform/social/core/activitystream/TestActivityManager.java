package org.exoplatform.social.core.activitystream;

import junit.framework.TestCase;

import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.space.impl.SocialDataLocation;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class TestActivityManager extends TestCase {

  public TestActivityManager() throws Exception {
    super();
  }

  @Test
  public void testAddProviders() throws Exception {
    SocialDataLocation data = Mockito.mock(SocialDataLocation.class);

    ActivityManager activityManager = new ActivityManager(data); // getComponent(ActivityManager.class);
    activityManager.addProcessor(new FakeProcessor(10));
    activityManager.addProcessor(new FakeProcessor(2));
    activityManager.addProcessor(new FakeProcessor(1));
    Activity activity = new Activity();
    activityManager.processActivitiy(activity);

    // just verify that we run in priority order
    assertEquals("null-1-2-10", activity.getTitle());

  }

  class FakeProcessor extends BaseActivityProcessorPlugin {

    public FakeProcessor(int priority) {
      super(null);
      super.priority = priority;
    }

    public void processActivity(Activity activity) {
      activity.setTitle(activity.getTitle() + "-" + priority);
    }

  }

}
