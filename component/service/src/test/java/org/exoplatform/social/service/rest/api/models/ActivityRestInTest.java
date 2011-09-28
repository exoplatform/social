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

import java.util.HashMap;

import junit.framework.TestCase;

/**
 * Unit Test for {@link ActivityRestIn}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Sep 28, 2011
 * @since 1.2.3
 */
public class ActivityRestInTest extends TestCase {

  /**
   * Test default values to check.
   */
  public void testDefaultValues() {
    ActivityRestIn activityRestIn = new ActivityRestIn();
    assertNull("activityRestIn.getTitle() must be null", activityRestIn.getTitle());
    assertNull("activityRestIn.getType() must be null", activityRestIn.getType());
    assertNull("activityRestIn.getPriority() must be null", activityRestIn.getPriority());
    assertNull("activityRestIn.getTemplateParams() must be null", activityRestIn.getTemplateParams());
  }

  /**
   * Test set values to check.
   */
  public void testSetValues() {
    ActivityRestIn activityRestIn = new ActivityRestIn();
    String title = "title";
    activityRestIn.setTitle(title);
    String type = "DEFAULT_ACTIVITY";
    activityRestIn.setType(type);
    Float priority = new Float(0.5);
    activityRestIn.setPriority(priority);
    HashMap<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("test", "foo");
    activityRestIn.setTemplateParams(templateParams);

    assertEquals("activityRestIn.getTitle() must return: " + title, title, activityRestIn.getTitle());
    assertEquals("activityRestIn.getType() must return: " + type, type, activityRestIn.getType());
    assertEquals("activityRestIn.getPriority() must return: " + priority, priority, activityRestIn.getPriority());
    assertEquals("activityRestIn.getTemplateParams() must return: " + templateParams, templateParams, activityRestIn.getTemplateParams());
  }

  /**
   * Tests to check if valid or not
   */
  public void testIsValid() {
    ActivityRestIn activityRestIn = new ActivityRestIn();
    assertFalse("activityRestIn.isValid() must be false", activityRestIn.isValid());
    activityRestIn.setTitle("Hello World!");
    assertTrue("activityRestIn.isValid() must be true", activityRestIn.isValid());
  }

}
