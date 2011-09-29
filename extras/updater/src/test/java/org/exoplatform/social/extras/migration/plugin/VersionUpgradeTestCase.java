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

package org.exoplatform.social.extras.migration.plugin;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class VersionUpgradeTestCase extends TestCase {

  public void testNone_120GA() throws Exception {
    assertTrue(VersionUpgrade.from11xTo12x("1.2.0-GA", "0"));
  }

  public void test106_120GA() throws Exception {
    assertTrue(VersionUpgrade.from11xTo12x("1.2.0-GA", "1.1.6"));
  }

  public void test106_120() throws Exception {
    assertTrue(VersionUpgrade.from11xTo12x("1.2.0", "1.1.6"));
  }

  public void test106SNAPSHOT_120SNAPSHOT() throws Exception {
    assertTrue(VersionUpgrade.from11xTo12x("1.2.0-SNAPSHOT", "1.1.6-SNAPSHOT"));
  }

  public void test10_123() throws Exception {
    assertTrue(VersionUpgrade.from11xTo12x("1.2.3", "1.1"));
  }

}
