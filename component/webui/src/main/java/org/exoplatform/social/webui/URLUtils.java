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
package org.exoplatform.social.webui;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;

/**
 * Processes url and returns the some type of result base on url.
 * 
 */
public class URLUtils {

  /**
   * Gets current user name base on analytic the current url.<br>
   * 
   * @return current user name.
   */
  public static String getCurrentUser() {
    PortalRequestContext request = Util.getPortalRequestContext() ;
    String uri = request.getNodePath();
    String[] els = uri.split("/");
    if (els.length == 3) return els[2];
    
    if (els.length == 4) return els[3];
    
    return null;
  }
}
