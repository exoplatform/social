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
package org.exoplatform.social.service.rest.api;

import javax.ws.rs.core.MediaType;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.service.rest.api.models.Versions;
import org.exoplatform.social.service.test.AbstractResourceTest;

/**
 * Unit test for {@link VersionResources}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 9, 2011
 */
public class VersionResourcesTest extends AbstractResourceTest {

  /**
   * Adds {@link VersionResources}.
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    addResource(VersionResources.class, null);
  }

  /**
   * Removes {@link VersionResources}.
   */
  @Override
  public void tearDown() throws Exception {
    removeResource(VersionResources.class);
    super.tearDown();
  }

  /**
   * Tests {@link VersionResources#getLatestVersion(javax.ws.rs.core.UriInfo, String)} with json format.
   *
   * @throws Exception
   */
  public void testGetLatestVersionWithJsonFormat() throws Exception {
    ContainerResponse containerResponse = service("GET", "/api/social/version/latest.json", "", null, null);
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
        containerResponse.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
    assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());

    Object entity = containerResponse.getEntity();
    assertNotNull("entity must not be null", entity);

    final String expectedResponse = "{\"version\": \"v1-alpha3\"}";
    assertJsonStringEqualsEntity(expectedResponse, entity);
  }

  /**
   * Tests {@link VersionResources#getLatestVersion(javax.ws.rs.core.UriInfo, String)} with xml format.
   *
   * @throws Exception
   */

  public void testGetLatestVersionWithXmlFormat() throws Exception {
    ContainerResponse containerResponse = service("GET", "/api/social/version/latest.xml", "", null, null);

    //not-supported yet
    assertEquals(415, containerResponse.getStatus());

    /*
    assertEquals("containerResponse.getContentType() must return: MediaType.APPLICATION_XML_TYPE",
            MediaType.APPLICATION_XML_TYPE,
            containerResponse.getContentType());
    assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
    Object entity = containerResponse.getEntity();
    assertNotNull("entity must not be null", entity);

    final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                     "<version>v1-alpha1</version>";
    //assertXmlStringEqualsEntity(expectedResponse, entity);
    */
  }


  /**
   * Tests {@link VersionResources#getSupportedVersions(javax.ws.rs.core.UriInfo, String)} with json format.
   */
  public void testGetSupportedVersionsWithJsonFormat() throws Exception {
    ContainerResponse containerResponse = service("GET", "/api/social/version/supported.json", "", null, null);
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
        containerResponse.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
    assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
    Object entity = containerResponse.getEntity();
    assertNotNull("entity must not be null", entity);

    final String expectedResponse = "{\"versions\": [\"v1-alpha3\"]}";
    assertJsonStringEqualsEntity(expectedResponse, entity);
  }

  /**
   * Tests {@link VersionResources#getSupportedVersions(javax.ws.rs.core.UriInfo, String)} with xml format.
   *
   * @throws Exception
   */
  public void testGetSupportedVersionsWithXmlFormat() throws Exception {
    ContainerResponse containerResponse = service("GET", "/api/social/version/supported.xml", "", null, null);
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
        containerResponse.getContentType().toString().startsWith(MediaType.APPLICATION_XML.toString()));
    assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
    Versions entity = (Versions) containerResponse.getEntity();
    assertNotNull("entity must not be null", entity);

    final String expectedResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<versions><version>v1-alpha3</version></versions>";
    assertXmlStringEqualsEntity(expectedResponse, entity);
  }

}
