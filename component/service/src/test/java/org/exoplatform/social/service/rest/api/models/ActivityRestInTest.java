/*
 * Copyright (C) 2011 hoatle.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  public void testValid() {
    ActivityRestIn activityRestIn = new ActivityRestIn();
    assertFalse("activityRestIn.isValid() must be false", activityRestIn.isValid());
    activityRestIn.setTitle("Hello World!");
    assertTrue("activityRestIn.isValid() must be true", activityRestIn.isValid());
  }

}
