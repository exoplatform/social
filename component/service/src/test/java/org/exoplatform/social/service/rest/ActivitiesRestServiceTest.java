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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.service.test.AbstractResourceTest;

/**
 * ActivitiesRestServiceTest.java.
 *
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since  Mar 4, 2010
 * @copyright eXo Platform SAS
 */
public class ActivitiesRestServiceTest extends AbstractResourceTest {
  static private ActivitiesRestService activitiesRestService;
  static private PortalContainer container;
  static private ActivityManager activityManager;
  static private String activityId;

  public void setUp() throws Exception {
    super.setUp();

    activitiesRestService = new ActivitiesRestService();
    registry(activitiesRestService);
    container = PortalContainer.getInstance();
    activityManager = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);
    populateData();
//    startSessionAs("root");
  }

  public void tearDown() throws Exception {
    super.tearDown();

    unregistry(activitiesRestService);
  }

  public void testDestroyActivity() throws Exception {
    ContainerResponse response = service("POST", "/portal/social/activities/destroy/123.json", "", null, null);
    assertEquals(500, response.getStatus());

    response = service("POST", "/portal/social/activities/destroy/123.xml", "", null, null);
    assertEquals(500, response.getStatus());
  }

  public void testShowLikes() throws Exception {

  }

  public void testUpdateLike() throws Exception {

  }

  public void testDestroyLike() throws Exception {

  }

  public void testShowComments() throws Exception {

  }

  public void testUpdateComment() throws Exception {


  }

  public void testDestroyComment() throws Exception {

  }


  private void populateData() throws Exception {

  }
}
