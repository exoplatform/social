/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.portlet;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * TODO should become a service
 */
public class URLUtils {
  public static final String MODULE = "m";
  public static final String USERNAME = "u";
  public static final String APPLICATION = "a";

  public static String getCurrentUser() {
    PortalRequestContext request = Util.getPortalRequestContext() ;
    String uri = request.getNodePath();
    String[] els = uri.split("/");
    // check first if we are on the page of a user
    if (els.length > 3) {
      if ((els.length == 4) && els[3].equals("activities"))
         return els[2];
      return els[3];
    }
      
    return null;
  }

  public static String getCurrentApplication() {
    PortalRequestContext request = Util.getPortalRequestContext() ;
    String uri = request.getNodePath();
    String[] els = uri.split("/");

    // check first if we are on the page of a user
    if (els.length >= 4) {
      if (els[els.length - 1].equals("activities"))
        return els[els.length - 1];
    }
    return null;
  }

  public static Map<String, String> decodeURL() {
    Map<String, String> res = Maps.newHashMap();
    PortalRequestContext request = Util.getPortalRequestContext() ;
    String uri = request.getNodePath();
    String[] els = uri.split("/");
    if (els.length >= 2)
      res.put(MODULE, els[1]);
    if (els.length >= 3)
      res.put(USERNAME, els[2]);
    if (els.length >= 4)
      res.put(APPLICATION, els[3]);
    return res;
  }

  public static String generateURL(String module, String username, String application) {
    StringBuffer res = new StringBuffer();
    res.append(Util.getPortalRequestContext().getPortalURI());
    if(module != null) {
      res.append(module);
      if(username != null) {
        res.append("/").append(username);
        if(application != null)
          res.append("/").append(application);
      }
    }
    return res.toString();
  }
}
