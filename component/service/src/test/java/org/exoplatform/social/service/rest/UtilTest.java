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
package org.exoplatform.social.service.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;


/**
 * Unit Test for {@link Util}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 16, 2011
 */
public class UtilTest extends TestCase {

  /**
   * Tests {@link Util#getMediaType(String, String[])}.
   */
  public void testGetMediaType() {
    // unsupported media type
    try {
      Util.getMediaType("xml", new String[] {"json"});
      fail("Expecting WebApplicationException: 415 status");
    } catch (WebApplicationException wae) {
      assertEquals(415, wae.getResponse().getStatus());
    }

    MediaType jsonMediaType = Util.getMediaType("json", new String[]{"json"});
    assertEquals(MediaType.APPLICATION_JSON_TYPE, jsonMediaType);


    MediaType xmlMediaType = Util.getMediaType("xml", new String[]{"json", "xml", "atom", "rss"});
    assertEquals(MediaType.APPLICATION_XML_TYPE, xmlMediaType);

    try {
      Util.getMediaType("rss", new String[]{"json", "xml", "rss"});
      fail("Expecting WebApplicationException: 406 status");
    } catch (WebApplicationException wae) {
      assertEquals(406, wae.getResponse().getStatus());
    }
  }

}
