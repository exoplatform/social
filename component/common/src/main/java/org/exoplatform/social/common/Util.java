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
package org.exoplatform.social.common;

import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author : hanhvq@exolpatform.com
 * May 10, 2011 
 * @since 1.2.0-GA 
 */
public class Util {
  private static final Pattern URL_PATTERN = Pattern
          .compile("^(?i)" +
          "(" +
            "((?:(?:ht)tp(?:s?)\\:\\/\\/)?" +                                                       // protolcol
            "(?:\\w+:\\w+@)?" +                                                                       // username password
            "(((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +  // IPAddress
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))|" +     // IPAddress
            "(?:(?:[-\\p{L}\\p{Digit}\\+\\$\\-\\*\\=]+\\.)+" +
            "(?:com|org|net|edu|gov|mil|biz|info|mobi|name|aero|jobs|museum|travel|asia|cat|coop|int|pro|tel|xxx|[a-z]{2}))))|" + //Domain
            "(?:(?:(?:ht)tp(?:s?)\\:\\/\\/)(?:\\w+:\\w+@)?(?:[-\\p{L}\\p{Digit}\\+\\$\\-\\*\\=]+))" + // Protocol with hostname
          ")" +
          "(?::[\\d]{1,5})?" +                                                                        // port
          "(?:[\\/|\\?|\\#].*)?$");                                                               // path and query
  
  
  /**
   * Checks a url is in a valid form or not.
   * 
   * @param link
   * @return
   */
  public static boolean isValidURL(String link) {
      if (link == null || link.length() == 0) return false;
      return URL_PATTERN.matcher(link).matches();
  }
}
