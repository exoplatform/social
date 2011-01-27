/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.core.space;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import junit.framework.TestCase;

/**
 * Unit Test for {@link SpaceUtilsTest}
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jan 27, 2011
 * @since 1.2.0-GA
 */
public class SpaceUtilsTest extends TestCase {

  private static final Log LOG = ExoLogger.getLogger(SpaceUtilsTest.class);

  public void testGetDisplayAppName() throws Exception {

    final String inputPortlet = "ABCPortlet";
    final String outputPortlet = "ABC";

    final String inputPortlet2 = "hello world portlet";
    final String outputPortlet2 = "hello world";

    final String inputGadget = "abc def gadget";
    final String outputGadget = "abc def";

    assertEquals(outputPortlet, SpaceUtils.getDisplayAppName(inputPortlet));
    assertEquals(outputPortlet2, SpaceUtils.getDisplayAppName(inputPortlet2));
    assertEquals(outputGadget, SpaceUtils.getDisplayAppName(inputGadget));

  }

}
