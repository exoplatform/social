/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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

import junit.framework.TestCase;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;

public class I18NActivityUtilsTest extends TestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }


  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testGetParamValues() throws Exception {
    String input = "v1#v2#v3";
    
    String[] got = I18NActivityUtils.getParamValues(input);
    
    assertEquals(3, got.length);
    
    input = "#v2#v3";
    
    got = I18NActivityUtils.getParamValues(input);
    
    assertEquals(3, got.length);
    
  }
  
  public void testGetParamValuesWithNULL() throws Exception {
    String input = null;
    String[] got = I18NActivityUtils.getParamValues(input);
    assertNull(got);
  }
  
  public void testAddResourceKey() throws Exception {
    ExoSocialActivity a = createActivity();
    I18NActivityUtils.addResourceKey(a, "key1", "val1");
    
    assertNotNull(a.getTemplateParams());
    
    String[] values = I18NActivityUtils.getResourceValues(a);
    assertEquals(1, values.length);
    assertEquals("key1", a.getTitleId());
    String[] keys = I18NActivityUtils.getResourceKeys(a);
    assertEquals(1, keys.length);
    assertEquals("key1", keys[0]);
    
    //
    I18NActivityUtils.addResourceKey(a, "key2", "val1");
    
    values = I18NActivityUtils.getResourceValues(a);
    assertEquals(2, values.length);
    assertEquals(a.getTitleId(), "key1,key2");
    
    keys = I18NActivityUtils.getResourceKeys(a);
    assertEquals(2, keys.length);
    assertEquals("key1", keys[0]);
    assertEquals("key2", keys[1]);
    
    //
    I18NActivityUtils.addResourceKey(a, "key3", "val1", "val2");
    
    values = I18NActivityUtils.getResourceValues(a);
    assertEquals(3, values.length);
    assertEquals("val1#val2", values[2]);
    assertEquals(a.getTitleId(), "key1,key2,key3");
    
    keys = I18NActivityUtils.getResourceKeys(a);
    assertEquals(3, keys.length);
    assertEquals("key1", keys[0]);
    assertEquals("key2", keys[1]);
    assertEquals("key3", keys[2]);
  }
  
  public void testAddResourceKeyWithNullValue() throws Exception {
    ExoSocialActivity a = createActivity();
    I18NActivityUtils.addResourceKey(a, "key1", "");
    
    assertNotNull(a.getTemplateParams());
    
    String[] values = I18NActivityUtils.getResourceValues(a);
    assertEquals(1, values.length);
    assertEquals("key1", a.getTitleId());
    String[] keys = I18NActivityUtils.getResourceKeys(a);
    assertEquals(1, keys.length);
    assertEquals("", values[0]);
    
    //
    I18NActivityUtils.addResourceKey(a, "key2", null);
    
    values = I18NActivityUtils.getResourceValues(a);
    keys = I18NActivityUtils.getResourceKeys(a);
    assertEquals(2, keys.length);
    assertEquals(a.getTitleId(), "key1,key2");
    assertEquals(2, keys.length);
    assertEquals("key1", keys[0]);
    assertEquals("key2", keys[1]);
    assertEquals("", values[0]);
    assertEquals("", values[1]);
    
    //
    I18NActivityUtils.addResourceKey(a, "key3", "", null);
    
    values = I18NActivityUtils.getResourceValues(a);
    assertEquals(3, values.length);
    assertEquals("", values[0]);
    assertEquals("", values[1]);
    assertEquals("#", values[2]);
    assertEquals(a.getTitleId(), "key1,key2,key3");
    
    keys = I18NActivityUtils.getResourceKeys(a);
    assertEquals(3, keys.length);
    assertEquals("key1", keys[0]);
    assertEquals("key2", keys[1]);
    assertEquals("key3", keys[2]);
  }
  
  private ExoSocialActivity createActivity() {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title value");
    activity.setBody("body value");
    activity.isComment(false);
    activity.setType("ks-forum:spaces");
    
    return activity;
  }
}
