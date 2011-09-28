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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.test.AbstractServiceTest;


/**
 * Unit Test for {@link Util}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 16, 2011
 */
public class UtilTest extends AbstractServiceTest {

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

  /**
   * Tests {@link org.exoplatform.social.service.rest.Util#getIdentityManager()}
   */
  public void testGetIdentityManager() {
    IdentityManager identityManager = Util.getIdentityManager();
    assertNotNull("identityManager must not be null", identityManager);
  }

  /**
   * Tests {@link Util#getIdentityManager(String)}.
   */
  public void testGetIdentityManagerByPortalContainerName() {
    IdentityManager identityManager = Util.getIdentityManager("portal");
    assertNotNull("identityManager must not be null", identityManager);
  }

  /**
   * Tests {@link Util#getSpaceService()}.
   */
  public void testGetSpaceService() {
    SpaceService spaceService = Util.getSpaceService();
    assertNotNull("spaceService must not be null", spaceService);
  }

  /**
   * Tests {@link Util#getSpaceService(String)}.
   */
  public void testGetSpaceServiceByPortalContainerName() {
    SpaceService spaceService = Util.getSpaceService("portal");
    assertNotNull("spaceService must not be null", spaceService);
  }

  /**
   * Tests {@link Util#getActivityManager()}.
   */
  public void testGetActivityManager() {
    ActivityManager activityManager = Util.getActivityManager();
    assertNotNull("activityManager must not be null", activityManager);
  }

  /**
   * Tests {@link Util#getActivityManager(String)}.
   */
  public void testGetActivityManagerByPortalContainerName() {
    ActivityManager activityManager = Util.getActivityManager("portal");
    assertNotNull("activityManager must not be null", activityManager);
  }

  /**
   * Tests {@link Util#getRelationshipManager()}.
   */
  public void testGetRelationshipManager() {
    RelationshipManager relationshipManager = Util.getRelationshipManager();
    assertNotNull("relationshipManager must not be null", relationshipManager);
  }

  /**
   * Tests {@link Util#getRelationshipManager(String)}.
   */
  public void testGetRelationshipManagerByPortalContainerName() {
    RelationshipManager relationshipManager = Util.getRelationshipManager("portal");
    assertNotNull("relationshipManager must not be null", relationshipManager);
  }

  /**
   * Tests {@link Util#convertTimestampToTimeString(long)}.
   */
  public void testConvertTimestampToTimeString() {
    long timestamp = 1308643759381L;
    //With GTM +07:00: Tue Jun 21 15:09:19 +0700 2011
    if ("GTM +07:00".equals(TimeZone.getDefault().getID())) {
      String expected = "Tue Jun 21 15:09:19 +0700 2011";
      assertEquals(expected, Util.convertTimestampToTimeString(timestamp));
    }
  }

  /**
   * Tests {@link Util#getBaseUrl(UriInfo)}.
   */
  public void testGetBaseUrl() {
    String baseUrl1 = "http://localhost:8080";

    String urlRequest1 = baseUrl1 + "/social/rest/v1/identity/123456.json?fields=fullName,avatarUrl";
    FakeUriInfo fakeUriInfo1 = new FakeUriInfo(urlRequest1);
    String gotBaseUrl1 = Util.getBaseUrl(fakeUriInfo1);
    assertEquals("gotBaseUrl1 must be: " + baseUrl1, baseUrl1, gotBaseUrl1);

    String urlRequest2 = baseUrl1 + "/social/rest/v1/identity/123456.json#id?fields=fullName,avatarUrl&limit=20";
    FakeUriInfo fakeUriInfo2 = new FakeUriInfo(urlRequest2);
    String gotBaseUrl2 = Util.getBaseUrl(fakeUriInfo2);
    assertEquals("gotBaseUrl2 must be: " + baseUrl1, baseUrl1, gotBaseUrl2);

    String baseUrl2 = "http://www.social.demo.exoplatform.org";
    String urlRequest3 = baseUrl2 + "/social/rest/v1/identity/123456.json?fields=fullName,avatarUrl";
    FakeUriInfo fakeUriInfo3 = new FakeUriInfo(urlRequest3);
    String gotBaseUrl3 = Util.getBaseUrl(fakeUriInfo3);
    assertEquals("gotBaseUrl3 must be: " + baseUrl2, baseUrl2, gotBaseUrl3);

    String urlRequest4 = baseUrl2 + "/social/rest/v1/identity/123456.json#id?fields=fullName,avatarUrl&limit=20";
    FakeUriInfo fakeUriInfo4 = new FakeUriInfo(urlRequest4);
    String gotBaseUrl4 = Util.getBaseUrl(fakeUriInfo4);
    assertEquals("gotBaseUrl4 must be: " + baseUrl2, baseUrl2, gotBaseUrl4);

    String baseUrl3 = "http://social.demo.exoplatform.org:80";
    String urlRequest5 = baseUrl3 + "/social/rest/v1/identity/123456#id?fields=fullName,avatarUrl&limit=20";
    FakeUriInfo fakeUriInfo5 = new FakeUriInfo(urlRequest5);
    String gotBaseUrl5 = Util.getBaseUrl(fakeUriInfo5);
    String expected = "http://social.demo.exoplatform.org";
    assertEquals("gotBaseUrl5 must return: " + expected, expected, gotBaseUrl5);
  }


  /**
   * The fake UriInfo implementation.
   */
  private class FakeUriInfo implements UriInfo {
    URI uriRequest;

    public FakeUriInfo(String urlRequest) {
      try {
        uriRequest = new URI(urlRequest);
      } catch (URISyntaxException e) {
        throw new RuntimeException("Can not create FakeUriInfo", e);
      }
    }

    public String getPath() {
      return uriRequest.getPath();
    }

    public String getPath(boolean decode) {
      return uriRequest.getPath();
    }

    public List<PathSegment> getPathSegments() {
      return null;
    }

    public List<PathSegment> getPathSegments(boolean decode) {
      return null;
    }

    public URI getRequestUri() {
      return null;
    }

    public UriBuilder getRequestUriBuilder() {
      return null;
    }

    public URI getAbsolutePath() {
      return null;
    }

    public UriBuilder getAbsolutePathBuilder() {
      return null;
    }

    public URI getBaseUri() {
      return uriRequest;
    }

    public UriBuilder getBaseUriBuilder() {
      return null;
    }

    public MultivaluedMap<String, String> getPathParameters() {
      return null;
    }

    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
      return null;
    }

    public MultivaluedMap<String, String> getQueryParameters() {
      return null;
    }

    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
      return null;
    }

    public List<String> getMatchedURIs() {
      return null;
    }

    public List<String> getMatchedURIs(boolean decode) {
      return null;
    }

    public List<Object> getMatchedResources() {
      return null;
    }
  }



}
