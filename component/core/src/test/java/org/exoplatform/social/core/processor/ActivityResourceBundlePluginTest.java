/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.core.processor;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;

import junit.framework.TestCase;

/**
 * Unit Test for {@link ActivityResourceBundlePlugin}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Feb 6, 2012
 */
public class ActivityResourceBundlePluginTest extends TestCase {

  private static final String ACTIVITY_TYPE = "activity:fake";
  private static final String RESOURCE_BUNDLE_KEY_FILE = "FakeResourceBundle";

  private Map<String, String> activityKeyTypeMapping;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    activityKeyTypeMapping = new HashMap<String, String>();
    activityKeyTypeMapping.put("hello", "FakeResourceBundle.hello");
  }

  public void testGetActivityKeyType() throws Exception {
    ObjectParameter objectParameter = getObjectParameter(RESOURCE_BUNDLE_KEY_FILE, activityKeyTypeMapping);
    InitParams initParams = new InitParams();
    initParams.addParam(objectParameter);
    ActivityResourceBundlePlugin activityResourceBundlePlugin = new ActivityResourceBundlePlugin(initParams);
    activityResourceBundlePlugin.setActivityType(ACTIVITY_TYPE);
    assertEquals(ACTIVITY_TYPE, activityResourceBundlePlugin.getActivityType());
  }

  public void testGetActivityKeyTypeMapping() throws Exception {
    ObjectParameter objectParameter = getObjectParameter(RESOURCE_BUNDLE_KEY_FILE, activityKeyTypeMapping);
    InitParams initParams = new InitParams();
    initParams.addParam(objectParameter);
    ActivityResourceBundlePlugin activityResourceBundlePlugin = new ActivityResourceBundlePlugin(initParams);
    activityResourceBundlePlugin.setActivityType(ACTIVITY_TYPE);
    assertEquals(activityKeyTypeMapping, activityResourceBundlePlugin.getActivityKeyTypeMapping());
  }

  public void testGetResourceBundleKeyFile() throws Exception {
    ObjectParameter objectParameter = getObjectParameter(RESOURCE_BUNDLE_KEY_FILE, activityKeyTypeMapping);
    InitParams initParams = new InitParams();
    initParams.addParam(objectParameter);
    ActivityResourceBundlePlugin activityResourceBundlePlugin = new ActivityResourceBundlePlugin(initParams);
    activityResourceBundlePlugin.setActivityType(ACTIVITY_TYPE);
    assertEquals(RESOURCE_BUNDLE_KEY_FILE, activityResourceBundlePlugin.getResourceBundleKeyFile());
  }

  public void testHasMessageBundleKey() throws Exception {
    ObjectParameter objectParameter = getObjectParameter(RESOURCE_BUNDLE_KEY_FILE, activityKeyTypeMapping);
    InitParams initParams = new InitParams();
    initParams.addParam(objectParameter);
    ActivityResourceBundlePlugin activityResourceBundlePlugin = new ActivityResourceBundlePlugin(initParams);
    activityResourceBundlePlugin.setActivityType(ACTIVITY_TYPE);
    assertFalse(activityResourceBundlePlugin.hasMessageBundleKey("hell"));
    assertTrue(activityResourceBundlePlugin.hasMessageBundleKey("hello"));
  }

  public void testGetMessageBundleKey() throws Exception {
    ObjectParameter objectParameter = getObjectParameter(RESOURCE_BUNDLE_KEY_FILE, activityKeyTypeMapping);
    InitParams initParams = new InitParams();
    initParams.addParam(objectParameter);
    ActivityResourceBundlePlugin activityResourceBundlePlugin = new ActivityResourceBundlePlugin(initParams);
    activityResourceBundlePlugin.setActivityType(ACTIVITY_TYPE);
    assertNull(activityResourceBundlePlugin.getMessageBundleKey("hell"));
    assertEquals("FakeResourceBundle.hello", activityResourceBundlePlugin.getMessageBundleKey("hello"));
  }

  public void testIsValid() throws Exception {
    ActivityResourceBundlePlugin activityResourceBundlePlugin = new ActivityResourceBundlePlugin(null);
    assertFalse(activityResourceBundlePlugin.isValid());

    activityResourceBundlePlugin = new ActivityResourceBundlePlugin(new InitParams());
    assertFalse(activityResourceBundlePlugin.isValid());

    InitParams initParams = new InitParams();
    initParams.addParam(getObjectParameter(null, null));
    activityResourceBundlePlugin = new ActivityResourceBundlePlugin(initParams);
    activityResourceBundlePlugin.setActivityType(null);
    assertFalse(activityResourceBundlePlugin.isValid());

    initParams = new InitParams();
    initParams.addParam(getObjectParameter(RESOURCE_BUNDLE_KEY_FILE, null));
    activityResourceBundlePlugin = new ActivityResourceBundlePlugin(initParams);
    activityResourceBundlePlugin.setActivityType(null);
    assertFalse(activityResourceBundlePlugin.isValid());

    initParams = new InitParams();
    initParams.addParam(getObjectParameter(RESOURCE_BUNDLE_KEY_FILE, activityKeyTypeMapping));
    activityResourceBundlePlugin = new ActivityResourceBundlePlugin(initParams);
    activityResourceBundlePlugin.setActivityType(null);
    assertFalse(activityResourceBundlePlugin.isValid());

    initParams = new InitParams();
    initParams.addParam(getObjectParameter(RESOURCE_BUNDLE_KEY_FILE, activityKeyTypeMapping));
    activityResourceBundlePlugin = new ActivityResourceBundlePlugin(initParams);
    activityResourceBundlePlugin.setActivityType(ACTIVITY_TYPE);
    assertTrue(activityResourceBundlePlugin.isValid());
  }

  private ObjectParameter getObjectParameter(String resourceBundleKeyFile, Map<String, String> activityKeyTypeMapping) {
    ObjectParameter objectParameter = new ObjectParameter();
    objectParameter.setName(resourceBundleKeyFile);
    ActivityResourceBundlePlugin pluginConfig = new ActivityResourceBundlePlugin();
    pluginConfig.setActivityKeyTypeMapping(activityKeyTypeMapping);
    objectParameter.setObject(pluginConfig);
    return objectParameter;
  }
}
