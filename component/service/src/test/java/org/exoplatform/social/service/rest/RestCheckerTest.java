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
import javax.ws.rs.core.Response;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.service.test.AbstractServiceTest;

import static org.exoplatform.social.service.rest.RestChecker.checkAuthenticatedRequest;
import static org.exoplatform.social.service.rest.RestChecker.checkSupportedFormat;
import static org.exoplatform.social.service.rest.RestChecker.checkValidPortalContainerName;

/**
 * Unit test for {@link RestChecker}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since  Sep 29, 2011
 * @since  1.2.3
 */
public class RestCheckerTest extends AbstractServiceTest {

  /**
   * Tests {@link RestChecker#checkAuthenticatedRequest()}.
   */
  public void testCheckAuthenticatedRequest() {
    try {
      checkAuthenticatedRequest();
      fail("Expecting WebApplicationException");
    } catch (WebApplicationException wae) {
      assertEquals("wae.getResponse().getStatus() must return: " + Response.Status.UNAUTHORIZED.getStatusCode(),
                   Response.Status.UNAUTHORIZED.getStatusCode(), wae.getResponse().getStatus());
    }

    startSessionAs("demo");
    checkAuthenticatedRequest();
  }

  /**
   * Tests {@link RestChecker#checkValidPortalContainerName(String)}.
   */
  public void testCheckValidPortalContainerName() {
    try {
      checkValidPortalContainerName("wrong");
      fail("Expecting WebApplicationException");
    } catch (WebApplicationException wae) {
      assertEquals("wae.getResponse().getStatus() must return: " + Response.Status.BAD_REQUEST.getStatusCode(),
                   Response.Status.BAD_REQUEST.getStatusCode(), wae.getResponse().getStatus());
    }

    PortalContainer portalContainer = checkValidPortalContainerName("portal");
    assertNotNull("portalContainer must not be null", portalContainer);
  }

  /**
   * Tests {@link RestChecker#checkSupportedFormat(String, String[])}.
   */
  public void testCheckSupportedFormat() {
    String[] supportedFormats = new String[]{"json", "xml"};
    try {
      checkSupportedFormat("atom", supportedFormats);
      fail("Expecting WebApplicationException");
    } catch (WebApplicationException wae) {
      assertEquals("wae.getResponse().getStatus() must return: "+Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(),
                   Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), wae.getResponse().getStatus());
    }

    MediaType jsonMediaType = checkSupportedFormat("json", supportedFormats);
    assertNotNull("jsonMediaType must not be null", jsonMediaType);
    assertEquals("jsonMediaType.getType() must return: " + MediaType.APPLICATION_JSON_TYPE.getType(),
            MediaType.APPLICATION_JSON_TYPE.getType(), jsonMediaType.getType());
    assertEquals("jsonMediaType.getSubtype() must return: " + MediaType.APPLICATION_JSON_TYPE.getSubtype(),
                 MediaType.APPLICATION_JSON_TYPE.getSubtype(), jsonMediaType.getSubtype());

    MediaType xmlMediaType = checkSupportedFormat("xml", supportedFormats);
    assertNotNull("xmlMediaType must not be null", xmlMediaType);
    assertEquals("xmlMediaType.getType() must return: " + MediaType.APPLICATION_XML_TYPE.getType(),
                 MediaType.APPLICATION_XML_TYPE.getType(), xmlMediaType.getType());
    assertEquals("xmlMediaType.getSubtype() must return: " + MediaType.APPLICATION_XML_TYPE.getSubtype(),
                 MediaType.APPLICATION_XML_TYPE.getSubtype(), xmlMediaType.getSubtype());
  }

}
