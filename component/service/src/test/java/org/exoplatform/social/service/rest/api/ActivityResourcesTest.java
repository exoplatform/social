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

import org.exoplatform.social.service.test.AbstractResourceTest;

/**
 * Unit Test for {@link ActivityResources}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 16, 2011
 */
public class ActivityResourcesTest extends AbstractResourceTest {

  private final String RESOURCE_URL = "/api/social/v1-alpha1/portal/activity";

  /**
   * Adds {@link ActivityResources}.
   *
   * @throws Exception
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    addResource(ActivityResources.class, null);
  }

  /**
   * Removes {@link ActivityResources}.
   *
   * @throws Exception
   */
  @Override
  public void tearDown() throws Exception {
    removeResource(ActivityResources.class);
    super.tearDown();
  }

  /**
   * Tests access permission to get an activity.
   *
   * @throws Exception
   */
  public void testGetActivityByIdForAccess() throws Exception {
    // unauthorized
    //testGetResourceWithoutAuth("GET", RESOURCE_URL+"/1a2b3c4d5e.json", null);
    //not found

    //forbidden
  }


  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithJsonFormat() {
    //TODO complete this
  }

  /**
   * Tests access permission to create a new activity.
   *
   * @throws Exception
   */
  public void testCreateNewActivityForAccess() throws Exception {
    //final byte[] data = "{\"title\":\"hello world!\"}".getBytes("UTF-8");
    //unauthorized
    //testGetResourceWithoutAuth("POST", RESOURCE_URL+".json", data);
    //forbidden
  }

  /**
   * Tests {@link ActivityResources#createNewActivity(javax.ws.rs.core.UriInfo, String, String, String,
   * org.apache.shindig.social.opensocial.model.Activity)} with json format.
   */
  public void testCreateNewActivityWithJsonFormat() {

  }

}
