/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.test;

import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.social.core.jpa.updater.utils.IdentityUtil;

public class IdentityUtilTest extends BaseExoTestCase {
  public void testNameConvertion() {
    assertEquals("name", IdentityUtil.getIdentityName("soc:name"));
    assertEquals("test.name", IdentityUtil.getIdentityName("soc:name%02test"));
  }
}
