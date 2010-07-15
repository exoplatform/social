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
package org.exoplatform.social.core.identity.model;

import junit.framework.TestCase;

public class GlobalIdTest extends TestCase {

  public void testValidContructor() {
    GlobalId id = new GlobalId("foo:bar"); // valid
    assertEquals("foo", id.getDomain());
    assertEquals("bar", id.getLocalId());
  }

  public void testIsValid() {
    assertFalse(GlobalId.isValid(null));
    assertFalse(GlobalId.isValid(":"));
    assertFalse(GlobalId.isValid(":dqsdqsd"));
    assertTrue(GlobalId.isValid("foo:")); // empty localId is allowed
    assertTrue(GlobalId.isValid("foo:bar"));
  }

  public void testMalformedId() {
    try {
      new GlobalId(null);
    } catch (IllegalArgumentException iae) {
      return; // expected
    }
    fail("should have thrown an IllegalArgumentException");
    try {
      new GlobalId(":");
    } catch (IllegalArgumentException iae) {
      return; // expected
    }
    fail("should have thrown an IllegalArgumentException");

    try {
      new GlobalId(":ssss");
    } catch (IllegalArgumentException iae) {
      return; // expected
    }
    fail("should have thrown an IllegalArgumentException");

  }

}
