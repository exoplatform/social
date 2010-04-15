package org.exoplatform.social.core.activitystream.model;

import java.util.Date;
import java.util.GregorianCalendar;



import junit.framework.TestCase;

public class TestActivity extends TestCase {

  
  public void testOpenSocialCompatibility() {
    
    Activity activity = new Activity();
    activity.setPriority((Integer)null);
    assertNull(activity.getPriority());
    
    activity.setPriority(50);
    assertEquals(0.5F, activity.getPriority());
    activity.setPriority(0.3F);
    assertEquals((Integer)30, activity.getIntPriority());
    
    boolean iaeThrown = false;
    try {
      activity.setPriority(110);
    }
    catch (IllegalArgumentException e) {
      iaeThrown = true;// expected
    }
    if (!iaeThrown) {
      fail("An IllegalArgumentException was expected");
    }
    
    activity.setType("opensocial:foo");
    assertEquals("foo", activity.getAppId());
    assertEquals("opensocial:foo", activity.getType());
    
    activity.setAppId("bar");
    assertEquals("opensocial:bar", activity.getType());
    assertEquals("bar", activity.getAppId());
  
    Date date = new GregorianCalendar(2010, 04, 06, 21, 55).getTime();
    activity.setUpdated(date);
    assertEquals((Long)date.getTime(), activity.getUpdatedTimestamp());
    
    activity.setUpdatedTimestamp(date.getTime());
    assertEquals(activity.getUpdated().getTime(), date.getTime());
    
    
    assertNull(activity.getStreamFaviconUrl());
    assertNull(activity.getStreamSourceUrl());
    assertNull(activity.getStreamTitle());
    assertNull(activity.getStreamUrl());
    
    Stream stream = new Stream();
    stream.setFaviconUrl("favicon");
    stream.setSourceUrl("source");
    stream.setTitle("title");
    stream.setUrl("url");
    activity.setStream(stream);
    
    assertEquals(stream.getFaviconUrl(), activity.getStreamFaviconUrl());
    assertEquals(stream.getSourceUrl(),activity.getStreamSourceUrl());
    assertEquals(stream.getTitle(),activity.getStreamTitle());
    assertEquals(stream.getUrl(),activity.getStreamUrl());
    
    
    assertNull(activity.getTitle());
    assertNull(activity.getTitleId());
    activity.setTitle("foo bar");
    assertEquals("foo bar", activity.getTitle());
    activity.setTitleId("FOO_BAR");
    assertEquals("FOO_BAR", activity.getTitleId());
    
  }
  
}
