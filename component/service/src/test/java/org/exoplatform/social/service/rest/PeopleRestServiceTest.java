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
package org.exoplatform.social.service.rest;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import junit.framework.AssertionFailedError;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class PeopleRestServiceTest  extends AbstractResourceTest {
  static private PeopleRestService peopleRestService;

  public void setUp() throws Exception {
    super.setUp();
    peopleRestService = new PeopleRestService();
    registry(peopleRestService);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    unregistry(peopleRestService);
  }

  public void testSuggestUsernames() throws Exception {
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String username = "root";
    h.putSingle("username", username);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "/social/people/suggest.json?nameToSearch=R&currentUser=root", "", h, null, writer);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    assertEquals("application/json;charset=utf-8", response.getContentType().toString());
    if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode())
      throw new AssertionFailedError("Service not found");
  }
}
