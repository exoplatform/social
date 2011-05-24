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
  private static final String SPACE_STRING = " ";
  private static final String HTTP = "http";
  private static final String HTTP_PRTOCOL = "http://";
  private static final Pattern URL_PATTERN = Pattern.compile("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)"
    + "(\\?([^#]*))?(#(.*))?");
  
  private static final int DOMAIN = 4;
  
  /**
   * Checks a url is in a valid form or not.
   * 
   * @param link
   * @return
   */
  public static boolean isValidURL(String link) {
    try {
      if ((link == null) || (link.length() == 0)) return false;
      
      // Check the case that url has the domain name in the right form. Exg: .com, .org... or not.
      if (link.indexOf('.') == -1) return false;
      
      if (!URL_PATTERN.matcher(link).matches()) return false;
      
      if (hasValidDomain(link)) return true;
      
      URI uri = null;
      uri = new URI(IDN.toUnicode(link));

      String scheme = uri.getScheme();
      if (scheme == null) {
        link = HTTP_PRTOCOL + link;
        uri = new URI(IDN.toUnicode(link));
      }
      
      String host = uri.getHost();
      if ((host != null) && (host.contains(SPACE_STRING))) return false;
      
      uri.toURL();
    } catch (URISyntaxException e) {
      return false;
    } catch (MalformedURLException e) {
      return false;
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }
  
  private static boolean hasValidDomain(String link) {
    
    if (!link.startsWith(HTTP)) link = HTTP_PRTOCOL + link;
    
    Matcher m = URL_PATTERN.matcher(link);
    
    if (m.matches()) {
        String domain = m.group(DOMAIN);
        if ((domain != null) && (!domain.contains(SPACE_STRING))) return true;
    }
    return false;
  }
}
