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
package org.exoplatform.social.common.jcr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Value;

import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import junit.framework.TestCase;

/**
 * Unit Test for {@link Util}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Dec 22, 2010
 */
public class UtilTest extends TestCase {

  private final Log LOG = ExoLogger.getLogger(UtilTest.class);

  /**
   * Unit Test for {@link Util#getPropertiesNamePattern(String[])}.
   *
   * @throws Exception
   */
  public void testGetPropertiesNamePattern() throws Exception {

    //test with null and empty arguments
    {
      try {
        Util.getPropertiesNamePattern(null);
      } catch (IllegalArgumentException iae) {
        LOG.info("testGetPropertiesNamePattern(): Passed with null argument");
      }

      try {
        Util.getPropertiesNamePattern(new String[]{});
      } catch (IllegalArgumentException iae) {
        LOG.info("testGetPropertiesNamePattern(): Passed with empty array");
      }
    }

    String[] propertyNames = {"p1", "p2", "p3", "p4"};
    String propertyNamePattern = Util.getPropertiesNamePattern(propertyNames);

    String expectedNamePattern = "p1|p2|p3|p4";
    assertEquals("propertyNamePattern must be: " + expectedNamePattern,
                  expectedNamePattern, propertyNamePattern);
  }

  /**
   * Unit Test for {@link Util#convertValuesToStrings(javax.jcr.Value[])}.
   *
   * @throws Exception
   */
  public void testConvertValuesToStrings() throws Exception {
    Value[] values = getStringValues(15);
    String[] strings = Util.convertValuesToStrings(values);
    assertNotNull("strings must not be null", strings);
    assertEquals("strings.length must be: " + values.length, values.length, strings.length);
    for (int i = 0, length = values.length; i < length; i++) {
      assertEquals("values[" + i + "] must be string["+i+"] : " + strings[i], strings[i], values[i].getString());
    }
  }

  /**
   * Unit Test for {@link Util#convertMapToStrings(java.util.Map)}.
   *
   * @throws Exception
   */
  public void testConvertMapToStrings() throws Exception {
    Map<String, String> map = getStringMap(15);
    String[] strings = Util.convertMapToStrings(map);
    assertNotNull("strings must not be null", strings);
    assertEquals("map.size() must be: " + strings.length, strings.length, map.size());
    Iterator<Map.Entry<String,String>> entrySetItr = map.entrySet().iterator();
    int i = 0;
    while (entrySetItr.hasNext()) {
      Map.Entry<String,String> entry = entrySetItr.next();
      String key = entry.getKey(), value = entry.getValue();
      String stringValue = key + "=" + value;
      assertEquals("stringValue must be: " + strings[i], strings[i], stringValue);
      i++;
    }

  }

  /**
   * Unit Test for {@link Util#convertValuesToMap(javax.jcr.Value[])}.
   *
   * @throws Exception
   */
  public void testConvertValuesToMap() throws Exception {
    final int numberOfValues = 15;
    Value[] values = new Value[numberOfValues];
    for (int i = 0; i < numberOfValues; i++) {
      Value value = new StringValue("key" + i + "=value" + i);
      values[i] = value;
    }

    Map<String, String> map = Util.convertValuesToMap(values);

    assertNotNull("map must not be null", map);
    assertEquals("map.size() must be: " + numberOfValues, numberOfValues, map.size());

    int i = numberOfValues;
    while (i >= 0) {
      map.containsKey("key" + i);
      map.containsValue("value" + i);
      i--;
    }

  }


  private Value[] getStringValues(int numberOfValues) throws Exception {
    Value[] values = new Value[numberOfValues];
    for (int i = 0; i < numberOfValues; i++) {
      Value value = new StringValue("String Value: " + i);
      values[i] = value;
    }
    return values;
  }

  private Map<String, String> getStringMap(int numberOfItems) throws Exception {
    Map<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < numberOfItems; i++) {
      map.put("key:" + i, "value:" + i);
    }
    return map;
  }

}
