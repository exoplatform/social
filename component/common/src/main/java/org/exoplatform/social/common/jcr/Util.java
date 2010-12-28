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

import javax.jcr.Value;

import org.apache.commons.lang.Validate;

/**
 * Provides utility for working with jcr
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Dec 22, 2010
 * @since 1.2.0-GA
 */
public class Util {

  public static String getPropertiesNamePattern(String[] propertyNames) {
    Validate.notEmpty(propertyNames, "propertyNames must not be empty");

    StringBuilder sb = new StringBuilder(256);
    sb.append(propertyNames[0]);
    for (int i = 1; i < propertyNames.length; i++) {
      sb.append('|').append(propertyNames[i]);
    }
    return sb.toString();
  }

  /**
   * Converts array of Value to array of String.
   * Make sure these Values can getString().
   *
   * @param values
   * @return
   * @throws Exception
   */
  public static String[] convertValuesToStrings(Value[] values) throws Exception {
    if (values.length == 1) {
      return new String[]{values[0].getString()};
    }
    String[] strArray = new String[values.length];
    for (int i = 0; i < values.length; ++i) {
      strArray[i] = values[i].getString();
    }
    return strArray;
  }

}
