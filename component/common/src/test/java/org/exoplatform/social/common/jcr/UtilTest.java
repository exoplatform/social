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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.AbstractCommonTest;

import junit.framework.TestCase;

/**
 * Unit Test for {@link Util}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Dec 22, 2010
 */
public class UtilTest extends AbstractCommonTest {

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

  /**
   * Unit Test for {@link Util#convertListToArray(java.util.List, Class)}.
   * 
   * @throws Exception
   */
  public void testConvertListToArray() throws Exception {
    List<String> stringList = new ArrayList<String>();
    assertNotNull(Util.convertListToArray(stringList, String.class));
    String[] stringArray = new String[] {"Element0", "Element1","Element2", "Element3", "Element4"};
    for (int i = 0; i < stringArray.length; i++) {
      stringList.add("Element" + i);
    }

    String[] checkedStringArray = Util.convertListToArray(stringList, String.class);
    for (int i = 0; i < stringArray.length; i++) {
      assertEquals(stringArray[i], checkedStringArray[i]);
    }
  }

  /**
   * Unit Test for {@link Util#createNodes(javax.jcr.Node, String)}.
   */
  public void testCreateNodes() {

    try {
      Util.createNodes(null, "abc");
      fail("Expecting IllegalArgumentException.");
    } catch (IllegalArgumentException iae) {
      assertEquals("rootNode must not be null", iae.getMessage());
    }

    try {
      Util.createNodes(getRootNode(), null);
      fail("Expecting IllegalArgumentException.");
    } catch (IllegalArgumentException iae) {
      assertEquals("path must not be null", iae.getMessage());
    }

    try {
      Util.createNodes(null, null);
      fail("Expecting IllegalArgumentException.");
    } catch (IllegalArgumentException iae) {
      assertEquals("rootNode must not be null", iae.getMessage());
    }

    createNodes("a", "a", false);
    createNodes("a", "a", false);
    createNodes("b/c/d", "b/c/d", false);
    createNodes("b/c/d", "b/c/d", false);
    createNodes("t/u/", "t/u", false);
    //does not support these paths
    try {
      createNodes("/e/f", "/e/f", true);
      fail("RuntimeException is expected with the relPath: /e/f");
    } catch (RuntimeException re) {

    }

    //tearDown
    try {
      getRootNode().getNode("a").remove();
      getRootNode().getNode("b/c/d").remove();
    } catch (RepositoryException re) {
      throw new RuntimeException(re);
    } finally {
      sessionManager.closeSession(true);
    }
  }

  private void createNodes(String providedPath, String expectedPath, boolean expectedToFail) {
    Node rootNode = getRootNode();
    Util.createNodes(rootNode, providedPath);
    if (expectedToFail) {
      return;
    }
    try {
      Node foundNode = rootNode.getNode(expectedPath);
      assertNotNull("foundNode must not be null", foundNode);
    } catch (RepositoryException e) {
      fail("Failed to get path: /" + providedPath);
    }
  }

  private Node getRootNode() {
    Session session = sessionManager.getOrOpenSession();
    Node rootNode = null;
    try {
      rootNode = session.getRootNode();
    } catch (RepositoryException e) {
      fail(e.getMessage());
    }
    assertNotNull("rootNode must not be null", rootNode);
    return rootNode;
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
