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
import java.util.Map;
import java.util.Set;

import javax.jcr.Value;

import org.apache.commons.lang.Validate;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Provides utility for working with jcr
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Dec 22, 2010
 * @since 1.2.0-GA
 */
public class Util {

  /**
   * The logger
   */
  private static final Log LOG = ExoLogger.getLogger(Util.class);

  /**
   * Gets properties name pattern from an array of property names.
   *
   * @param propertyNames
   * @return properties name pattern.
   */
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
   * Converts array of Value to array of string.
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


  /**
   * Convert a map of string key and string value into a string array
   * where values are in the form key=value.
   *
   * @param templateParams
   * @return
   */
  public static String[] convertMapToStrings(Map<String, String> templateParams) {
    if (templateParams == null) {
      return null;
    }
    Set<String> keys = templateParams.keySet();
    String [] result = new String[keys.size()];
    int i = 0;
    for (String key : keys) {
      result[i++] = key + "=" + templateParams.get(key);
    }
    return result;
  }

  /**
   * Converts an array of {@link Value} into a map of string.
   * The values are expected to be of string type and in the form key=value
   *
   * @param values
   * @return
   */
  public static Map<String, String> convertValuesToMap(Value[] values) {
    if (values == null) {
      return null;
    }
    Map<String, String> result = new HashMap<String, String>();
    for (Value value : values) {
      try {
        String val = value.getString();
        int equalIndex = val.indexOf("=");
        if (equalIndex > 0) {
          result.put(val.split("=")[0], val.substring(equalIndex + 1));
        }
      } catch (Exception e) {
        LOG.warn(e.getMessage(), e);
      }
    }
    return result;
  }

}
