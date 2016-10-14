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
import org.exoplatform.social.core.jpa.updater.utils.StringUtil;

public class TestStringUtils extends BaseExoTestCase {  
  
  public void testRemoveLongUTF() {
    String str = "az😀↷♠️®️©️𩸽𝌆𝞢09";
    //remove 4 bytes utf-8 characters
    assertEquals("az↷♠️®️©️09", StringUtil.removeLongUTF(str));
  }
}
