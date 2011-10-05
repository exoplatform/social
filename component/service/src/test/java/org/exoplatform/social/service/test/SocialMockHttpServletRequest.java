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
package org.exoplatform.social.service.test;

import org.exoplatform.services.test.mock.MockHttpServletRequest;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * The MockHttpServletRequest of Social to override 2 methods of MockHttpServletRequest.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Oct 5, 2011
 * @since 1.2.3
 */
public class SocialMockHttpServletRequest extends MockHttpServletRequest {

  /**
   * Instantiates a new mock http servlet request.
   *
   * @param url     the url
   * @param data    the data
   * @param length  the length
   * @param method  the method
   * @param headers the headers
   */
  public SocialMockHttpServletRequest(String url, InputStream data, int length, String method, Map<String, List<String>> headers) {
    super(url, data, length, method, headers);
  }

   /**
    * {@inheritDoc}
    */
   public String getServerName() {
     try {
      return super.getServerName();
     } catch (Exception e) {

     }
     return "localhost";
   }

     /**
    * {@inheritDoc}
    */
   public int getServerPort() {
     try {
      return super.getServerPort();
     } catch (Exception e) {

     }
     return 8080;
   }

}
