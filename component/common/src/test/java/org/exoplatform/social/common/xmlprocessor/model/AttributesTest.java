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
package org.exoplatform.social.common.xmlprocessor.model;

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Unit test for {@link Attributes}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 */
public class AttributesTest extends TestCase {

  /**
   * Tests {@link Attributes#get(String)}, {@link Attributes#put(String, String)}.
   */
  public void testGetAndPut() {
    Attributes attributes = new Attributes();
    assertEquals("attributes.get(\"foo\") must be \"\"", "", attributes.get("foo"));
    attributes.put("foo", "bar");
    assertEquals("attributes.get(\"foo\") must return \"bar\"", "bar", attributes.get("foo"));
  }

  /**
   * Tests {@link Attributes#put(String, String)}.
   */
  public void testRemove() {
    Attributes attributes = new Attributes();
    attributes.put("foo", "bar");
    attributes.remove("foo");
    assertEquals("attributes.get(\"foo\") must be empty string", "", attributes.get("foo"));
  }

  /**
   * Tests {@link Attributes#hasKey(String)}
   */
  public void testHasKey() {
    Attributes attributes = new Attributes();
    assertFalse("attributes.hasKey(\"foo\") must return false", attributes.hasKey("foo"));
    attributes.put("foo", "bar");
    assertTrue("attributes.hasKey(\"foo\") must return true", attributes.hasKey("foo"));
  }

  /**
   * Tests {@link org.exoplatform.social.common.xmlprocessor.model.Attributes#size()}.
   */
  public void testSize() {
    Attributes attributes = new Attributes();
    assertEquals("attributes.size() must return 0", 0, attributes.size());
    attributes.put("foo", "bar");
    assertEquals("attributes.size() must return 1", 1, attributes.size());
  }

  /**
   * Tests {@link Attributes#getKeyIterator()}.
   */
  public void testGetKeyIterator() {
    Attributes attributes = new Attributes();
    Iterator<String> keyIterator = attributes.getKeyIterator();
    assertFalse("keyIterator.hasNext() must return false", keyIterator.hasNext());
    attributes.put("foo", "bar");
    keyIterator = attributes.getKeyIterator();
    assertTrue("keyIterator.hasNext() must return true", keyIterator.hasNext());
    assertEquals("keyIterator.next() must be \"foo\"", "foo", keyIterator.next());
  }

  /**
   * Tests {@link Attributes#toString()}.
   */
  public void testToString() {
    Attributes attributes = new Attributes();
    assertEquals("attributes.xml() must be empty string", "", attributes.toString());
    attributes.put("foo", "bar");
    assertEquals("attributes.xml() must be foo=\"bar\"", " foo=\"bar\"", attributes.toString());
    attributes.put("foo", "bar1");
    assertEquals("attributes.xml() must be foo=\"bar1\"", " foo=\"bar1\"", attributes.toString());
    attributes.put("foo1", "bar1");
    assertEquals("attributes.xml() must be foo=\"bar1\" foo1=\"bar1\"", " foo=\"bar1\" foo1=\"bar1\"", attributes.toString());
  }

}
