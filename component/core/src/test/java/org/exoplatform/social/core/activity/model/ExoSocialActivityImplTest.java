/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.activity.model;

import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

/**
 * Unit Tests for {@link org.exoplatform.social.core.activity.model.ExoSocialActivityImpl}
 *
 */
public class ExoSocialActivityImplTest extends TestCase {

  /**
   *
   */
  public void testOpenSocialCompatibility() {

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setPriority(null);
    assertNull(activity.getPriority());

    activity.setPriority((float)0.5);
    assertEquals("activity.getPriority() must be: " + 0.5F, 0.5F, activity.getPriority());
    activity.setPriority(0.3F);
    //assertEquals((Integer)30, activity.getIntPriority());

    activity.setType("opensocial:foo");
    assertEquals("opensocial:foo", activity.getType());

    activity.setAppId("bar");
    assertEquals("bar", activity.getAppId());

    Date date = new GregorianCalendar(2010, 04, 06, 21, 55).getTime();
    activity.setUpdated(date.getTime());
    assertEquals(date, activity.getUpdated());

    activity.setUpdated(date.getTime());
    assertEquals(activity.getUpdated().getTime(), date.getTime());

    assertNull(activity.getStreamFaviconUrl());
    assertNull(activity.getStreamSourceUrl());
    assertNull(activity.getStreamTitle());
    assertNull(activity.getStreamUrl());

    ActivityStreamImpl activityStream = new ActivityStreamImpl();
    activityStream.setFaviconUrl("favicon");
    activityStream.setPermaLink("source");
    activityStream.setTitle("title");
    activity.setActivityStream(activityStream);

    assertEquals(activityStream.getFaviconUrl(), activity.getStreamFaviconUrl());
    assertEquals(activityStream.getPermaLink(),activity.getStreamSourceUrl());
    assertEquals(activityStream.getTitle(),activity.getStreamTitle());

    assertNull(activity.getTitle());
    assertNull(activity.getTitleId());

    activity.setTitle("foo bar");
    assertEquals("foo bar", activity.getTitle());

    activity.setTitleId("FOO_BAR");
    assertEquals("FOO_BAR", activity.getTitleId());
  }
}