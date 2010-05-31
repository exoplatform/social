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
package org.exoplatform.social.services.rest.test;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.social.services.rest.AbstractResourceTest;
import org.exoplatform.social.services.rest.SpacesRestService;
import org.exoplatform.social.services.rest.SpacesRestService.SpaceList;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;

/**
 * SpacesRestServiceTest.java
 *
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since	 Mar 4, 2010
 * @copyright eXo Platform SAS
 */
public class SpacesRestServiceTest extends AbstractResourceTest {
  static private PortalContainer container;
  static private SpaceService spaceService;
  static private SpacesRestService spacesRestService;
  static private SpaceList rootSpaceList, rootPendingSpaceList;

  public void setUp() throws Exception {
    super.setUp();
    //startSessionAs("root");
    spacesRestService = new SpacesRestService();
    registry(spacesRestService);
    container = PortalContainer.getInstance();
    spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
    rootSpaceList = getMySpaceList("root");
    rootPendingSpaceList = getPendingSpaceList("root");
  }

  public void tearDown() throws Exception {
    super.tearDown();

    unregistry(spacesRestService);
  }

  public void testJsonShowMySpaceList() throws Exception {
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "/portal/social/spaces/root/mySpaces/show.json", "", null, null, writer);
    assertEquals(200, response.getStatus());
    SpaceList spaceList = (SpaceList) response.getEntity();
    assertEquals("application/json", response.getContentType().toString());
    assertEquals(rootSpaceList.getSpaces().size(), spaceList.getSpaces().size());
  }

  public void testJsonShowPendingSpaceList() throws Exception {
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "/portal/social/spaces/root/pendingSpaces/show.json", "", null, null, writer);
    assertEquals(200, response.getStatus());
    assertEquals("application/json", response.getContentType().toString());
    SpaceList spaceList = (SpaceList) response.getEntity();
    assertEquals(rootPendingSpaceList.getSpaces().size(), spaceList.getSpaces().size());
  }

  public void testXmlShowMySpaceList() throws Exception {
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "/portal/social/spaces/root/mySpaces/show.xml", "", null, null, writer);
    assertEquals(200, response.getStatus());
    assertEquals("application/xml", response.getContentType().toString());
    SpaceList spaceList = (SpaceList) response.getEntity();
    assertEquals(rootSpaceList.getSpaces().size(), spaceList.getSpaces().size());
  }

  public void testXmlShowPendingSpaceList() throws Exception {
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "/portal/social/spaces/root/pendingSpaces/show.xml", "", null, null, writer);
    assertEquals(200, response.getStatus());
    assertEquals("application/xml", response.getContentType().toString());
    SpaceList spaceList = (SpaceList) response.getEntity();
    assertEquals(rootPendingSpaceList.getSpaces().size(), spaceList.getSpaces().size());
  }

  private SpaceList getMySpaceList(String userId) throws Exception {
    SpaceList spaceList = new SpaceList();
    List<Space> mySpaces;
    try {
      mySpaces = spaceService.getSpaces(userId);
    } catch (SpaceException e) {
      throw new RuntimeException("can not getMySpaceList", e);
    }
    spaceList.setSpaces(mySpaces);
    return spaceList;
  }

  private SpaceList getPendingSpaceList(String userId) throws Exception {
    SpaceList spaceList = new SpaceList();
    List<Space> pendingSpaces;
    try {
      pendingSpaces = spaceService.getSpaces(userId);
    } catch (SpaceException e) {
      throw new RuntimeException("can not getPendingSpaceList", e);
    }
    spaceList.setSpaces(pendingSpaces);
    return spaceList;
  }
}
