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
   * Unit Test for {@link Util#getPropertiesNamePattern(String[])}
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

}
