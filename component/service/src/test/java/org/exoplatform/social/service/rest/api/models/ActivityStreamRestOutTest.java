/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.service.rest.api.models;


import junit.framework.TestCase;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ActivityStreamImpl;
import org.exoplatform.social.service.rest.Util;

/**
 * Unit Test for {@link ActivityStreamRestOut}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Sep 29, 2011
 * @since 1.2.3
 */
public class ActivityStreamRestOutTest extends TestCase {

  /**
   * Tests default values
   */
  public void testDefaultValues() {
    ActivityStreamRestOut activityStreamRestOut = new ActivityStreamRestOut();
    assertEquals("", activityStreamRestOut.getType());
    assertEquals("", activityStreamRestOut.getFaviconUrl());
    assertEquals("", activityStreamRestOut.getPrettyId());
    assertEquals("", activityStreamRestOut.getTitle());
    assertEquals("", activityStreamRestOut.getPermaLink());
  }

  /**
   * Tests gets and sets values
   */
  public void testSetValues() {
    String faviconUrl = "/abc/def";
    String prettyId = "demo";
    String title = "Activity Stream of Demo Gtn";
    String permaLink = "/social/u/demo";

    ActivityStream as = new ActivityStreamImpl();
    as.setType(ActivityStream.Type.USER);
    as.setFaviconUrl(faviconUrl);
    as.setPrettyId(prettyId);
    as.setTitle(title);
    as.setPermaLink(permaLink);

//    ActivityStreamRestOut activityStreamRestOut = new ActivityStreamRestOut(as, null);

//    assertEquals(ActivityStream.Type.USER.toString(), activityStreamRestOut.getType());
//    assertEquals(Util.getBaseUrl() + faviconUrl, activityStreamRestOut.getFaviconUrl());
//    assertEquals(prettyId, activityStreamRestOut.getPrettyId());
//    assertEquals(title, activityStreamRestOut.getTitle());
//    assertEquals(Util.getBaseUrl() + permaLink, activityStreamRestOut.getPermaLink());

//    activityStreamRestOut.setType(null);
//    activityStreamRestOut.setFaviconUrl(null);
//    activityStreamRestOut.setPrettyId(null);
//    activityStreamRestOut.setTitle(null);
//    activityStreamRestOut.setPermaLink(null);
//
//    assertEquals("", activityStreamRestOut.getType());
//    assertEquals("", activityStreamRestOut.getFaviconUrl());
//    assertEquals("", activityStreamRestOut.getPrettyId());
//    assertEquals("", activityStreamRestOut.getTitle());
//    assertEquals("", activityStreamRestOut.getPermaLink());

  }

}
