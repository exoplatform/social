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

package org.exoplatform.social.service.rest.api_v1alpha1;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.api_v1alpha1.models.ActivityList;
import org.exoplatform.social.service.test.AbstractResourceTest;

import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ActivityStreamResourcesTest extends AbstractResourceTest {

  private final String RESOURCE_URL = "/api/social/v1-alpha1/portal/activity_stream";

  //
  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;

  //
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity spaceIdentity;

  //
  private Space space;

  /**
   * Adds {@link ActivityStreamResources}.
   *
   * @throws Exception
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    addResource(ActivityStreamResources.class, null);

    //
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);

    //
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);

    loadIdentity(rootIdentity, 10, "title");
    loadIdentity(maryIdentity, 5, "mary title");

    //
    relationshipManager.inviteToConnect(rootIdentity, maryIdentity);
    relationshipManager.confirm(maryIdentity, rootIdentity);

    //
    space = new Space();
    space.setDisplayName("foo");
    space.setManagers(new String[]{rootIdentity.getRemoteId()});
    space.setGroupId("/space/foo");
    spaceService.saveSpace(space, true);
    spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());

    loadIdentity(spaceIdentity, 7, "space title");

  }

  /**
   * Removes {@link ActivityStreamResources}.
   *
   * @throws Exception
   */
  @Override
  public void tearDown() throws Exception {
    removeResource(ActivityStreamResources.class);

    //
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(spaceIdentity);

    //
    spaceService.deleteSpace(space);

    super.tearDown();
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamUserDefault(javax.ws.rs.core.UriInfo, String, String, String,
   * String)} with json format.
   */
  public void testgetActivityStreamDefaultNoLimit() throws Exception {
    startSessionAs(rootIdentity.getRemoteId());

    ContainerResponse response = service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/user/default.json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(10, got.getActivities().size());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + johnIdentity.getId() + "/user/default.json", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamUserDefault(javax.ws.rs.core.UriInfo, String, String, String,
   * String)} with json format.
   */
  public void testgetActivityStreamDefaultLimit() throws Exception {
    startSessionAs(rootIdentity.getRemoteId());

    ContainerResponse response = service("GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/default.json?limit=4", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(4, got.getActivities().size());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + johnIdentity.getId() + "/user/default.json?limit=4", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamUserDefault(javax.ws.rs.core.UriInfo, String, String, String,
   * String)} with json format.
   */
  public void testgetActivityStreamDefaultData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    ContainerResponse response =
        service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/user/default.json?limit=3", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(3, got.getActivities().size());
    assertEquals("title 9", got.getActivities().get(0).getTitle());
    assertEquals("title 8", got.getActivities().get(1).getTitle());
    assertEquals("title 7", got.getActivities().get(2).getTitle());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + johnIdentity.getId() + "/user/default.json?limit=3", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamUserNewer(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamNewerNoLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites = activityManager.getActivitiesWithListAccess(rootIdentity).loadAsList(0, 4);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/newer/" + activites.get(3).getId() + ".json", "", null,
        null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(3, got.getActivities().size());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/newer/" + activites.get(3).getId() + ".json", "", null,
        null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamUserNewer(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamNewerLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites = activityManager.getActivitiesWithListAccess(rootIdentity).loadAsList(0, 6);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/newer/" + activites.get(5).getId() + ".json?limit=2", "",
        null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/newer/" + activites.get(5).getId() + ".json?limit=2", "",
        null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamUserNewer(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamNewerData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites = activityManager.getActivitiesWithListAccess(rootIdentity).loadAsList(0, 6);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/newer/" + activites.get(5).getId() + ".json?limit=3", "",
        null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(3, got.getActivities().size());
    assertEquals("title 9", got.getActivities().get(0).getTitle());
    assertEquals("title 8", got.getActivities().get(1).getTitle());
    assertEquals("title 7", got.getActivities().get(2).getTitle());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/newer/" + activites.get(5).getId() + ".json?limit=3", "",
        null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamUserOlder(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamOlderNoLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites = activityManager.getActivitiesWithListAccess(rootIdentity).loadAsList(0, 4);

    ContainerResponse response = service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/older/" + activites.get(3).getId() + ".json", "",
        null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(6, got.getActivities().size());

    endSession();

    response =
      service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/older/" + activites.get(3).getId() + ".json", "",
        null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamUserOlder(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamOlderLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites = activityManager.getActivitiesWithListAccess(rootIdentity).loadAsList(0, 6);

    ContainerResponse response = service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/older/" + activites.get(5).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/older/" + activites.get(5).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamUserOlder(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamOlderData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites = activityManager.getActivitiesWithListAccess(rootIdentity).loadAsList(0, 6);

    ContainerResponse response = service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/older/" + activites.get(5).getId() + ".json?limit=3",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(3, got.getActivities().size());
    assertEquals("title 3", got.getActivities().get(0).getTitle());
    assertEquals("title 2", got.getActivities().get(1).getTitle());
    assertEquals("title 1", got.getActivities().get(2).getTitle());

    endSession();

    response =
      service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/user/older/" + activites.get(5).getId() + ".json?limit=3",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamConnectionsDefault(javax.ws.rs.core.UriInfo, String, String,
   * String, String)} with json format.
   */
  public void testGetActivityStreamConnectionsDefaultNoLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());
    startSessionAs(maryIdentity.getRemoteId());

    this.maryPostActivityToRootWall(1);

    ContainerResponse response =
        service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/connections/default.json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(1, got.getActivities().size());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/connections/default.json", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamConnectionsDefault(javax.ws.rs.core.UriInfo, String, String,
   * String, String)} with json format.
   */
  public void testGetActivityStreamConnectionsDefaultLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());
    startSessionAs(maryIdentity.getRemoteId());

    this.maryPostActivityToRootWall(10);

    ContainerResponse response =
        service("GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/default.json?limit=2", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/default.json?limit=2", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamConnectionsDefault(javax.ws.rs.core.UriInfo, String, String,
   * String, String)} with json format.
   */
  public void testGetActivityStreamConnectionsDefaultData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());
    startSessionAs(maryIdentity.getRemoteId());

    this.maryPostActivityToRootWall(10);

    ContainerResponse response =
        service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/connections/default.json?limit=3", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(3, got.getActivities().size());

    assertEquals("mary posts activiy " + 9 + " to root'wall", got.getActivities().get(0).getTitle());
    assertEquals("mary posts activiy " + 8 + " to root'wall", got.getActivities().get(1).getTitle());
    assertEquals("mary posts activiy " + 7 + " to root'wall", got.getActivities().get(2).getTitle());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/connections/default.json?limit=3", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamConnectionsNewer(javax.ws.rs.core.UriInfo, String, String,
   * String, String, String)} with json format.
   */
  public void testGetActivityStreamConnectionsNewerNoLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());
    startSessionAs(maryIdentity.getRemoteId());

    this.maryPostActivityToRootWall(3);

    List<ExoSocialActivity> activities =
        activityManager.getActivitiesOfConnectionsWithListAccess(rootIdentity).loadAsList(0, 3);

    ContainerResponse response = service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/newer/" + activities.get(2).getId() + ".json",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/newer/" + activities.get(2).getId() + ".json",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamConnectionsNewer(javax.ws.rs.core.UriInfo, String, String,
   * String, String, String)} with json format.
   */
  public void testGetActivityStreamConnectionsNewerLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());
    startSessionAs(maryIdentity.getRemoteId());

    this.maryPostActivityToRootWall(4);

    List<ExoSocialActivity> activities =
        activityManager.getActivitiesOfConnectionsWithListAccess(rootIdentity).loadAsList(0, 4);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/newer/" + activities.get(3).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/newer/" + activities.get(3).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamConnectionsNewer(javax.ws.rs.core.UriInfo, String, String,
   * String, String, String)} with json format.
   */
  public void testGetActivityStreamConnectionsNewerData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());
    startSessionAs(maryIdentity.getRemoteId());

    this.maryPostActivityToRootWall(4);

    List<ExoSocialActivity> activites = activityManager.getActivitiesWithListAccess(rootIdentity).loadAsList(0, 4);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/newer/" + activites.get(3).getId() + ".json?limit=3",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(3, got.getActivities().size());
    assertEquals("mary posts activiy " + 3 + " to root'wall", got.getActivities().get(0).getTitle());
    assertEquals("mary posts activiy " + 2 + " to root'wall", got.getActivities().get(1).getTitle());
    assertEquals("mary posts activiy " + 1 + " to root'wall", got.getActivities().get(2).getTitle());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/newer/" + activites.get(3).getId() + ".json?limit=3",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamConnectionsOlder(javax.ws.rs.core.UriInfo, String, String,
   * String, String, String)} with json format.
   */
  public void testGetActivityStreamConnectionsOlderNoLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());
    startSessionAs(maryIdentity.getRemoteId());

    this.maryPostActivityToRootWall(3);

    List<ExoSocialActivity> activities =
        activityManager.getActivitiesOfConnectionsWithListAccess(rootIdentity).loadAsList(0, 2);

    ContainerResponse response = service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/older/" + activities.get(1).getId() + ".json",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(1, got.getActivities().size());

    endSession();

    response =
      service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/older/" + activities.get(1).getId() + ".json",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamConnectionsOlder(javax.ws.rs.core.UriInfo, String, String,
   * String, String, String)} with json format.
   */
  public void testGetActivityStreamConnectionsOlderLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());
    startSessionAs(maryIdentity.getRemoteId());

    this.maryPostActivityToRootWall(3);

    List<ExoSocialActivity> activites =
        activityManager.getActivitiesOfConnectionsWithListAccess(rootIdentity).loadAsList(0, 2);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/older/" + activites.get(1).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(1, got.getActivities().size());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/older/" + activites.get(1).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamConnectionsOlder(javax.ws.rs.core.UriInfo, String, String,
   * String, String, String)} with json format.
   */
  public void testGetActivityStreamConnectionsOlderData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());
    startSessionAs(maryIdentity.getRemoteId());

    this.maryPostActivityToRootWall(5);

    List<ExoSocialActivity> activities =
        activityManager.getActivitiesOfConnectionsWithListAccess(rootIdentity).loadAsList(0, 2);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/older/" + activities.get(1).getId() + ".json?limit=3",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(3, got.getActivities().size());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/connections/older/" + activities.get(1).getId() + ".json?limit=3",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamSpacesDefault(javax.ws.rs.core.UriInfo, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamSpacesDefaultNoLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    ContainerResponse response =
        service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/spaces/default.json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(7, got.getActivities().size());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/spaces/default.json", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamSpacesDefault(javax.ws.rs.core.UriInfo, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamSpacesDefaultLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    ContainerResponse response =
        service("GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/default.json?limit=2", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/default.json?limit=2", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamSpacesDefault(javax.ws.rs.core.UriInfo, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamSpacesDefaultData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    ContainerResponse response =
        service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/spaces/default.json?limit=3", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(3, got.getActivities().size());
    assertEquals("space title 6", got.getActivities().get(0).getTitle());
    assertEquals("space title 5", got.getActivities().get(1).getTitle());
    assertEquals("space title 4", got.getActivities().get(2).getTitle());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/spaces/default.json?limit=3", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamSpacesNewer(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamSpacesNewerNoLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites =
        activityManager.getActivitiesOfUserSpacesWithListAccess(rootIdentity).loadAsList(0, 3);

    ContainerResponse response =
        service("GET",
            RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/newer/" + activites.get(2).getId() + ".json", "",
            null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service("GET",
            RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/newer/" + activites.get(2).getId() + ".json", "",
            null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamSpacesNewer(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamSpacesNewerLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites =
        activityManager.getActivitiesOfUserSpacesWithListAccess(rootIdentity).loadAsList(0, 4);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/newer/" + activites.get(3).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/newer/" + activites.get(3).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamSpacesNewer(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamSpacesNewerData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites =
        activityManager.getActivitiesOfUserSpacesWithListAccess(rootIdentity).loadAsList(0, 4);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/newer/" + activites.get(3).getId() + ".json?limit=3", "",
        null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(3, got.getActivities().size());
    assertEquals("space title 6", got.getActivities().get(0).getTitle());
    assertEquals("space title 5", got.getActivities().get(1).getTitle());
    assertEquals("space title 4", got.getActivities().get(2).getTitle());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/newer/" + activites.get(3).getId() + ".json?limit=3", "",
        null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamSpacesOlder(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamSpacesOlderNoLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites =
        activityManager.getActivitiesOfUserSpacesWithListAccess(rootIdentity).loadAsList(0, 2);

    ContainerResponse response = service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/older/" + activites.get(1).getId() + ".json",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(5, got.getActivities().size());

    endSession();

    response =
      service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/older/" + activites.get(1).getId() + ".json",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamSpacesOlder(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamSpacesOlderLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites =
        activityManager.getActivitiesOfUserSpacesWithListAccess(rootIdentity).loadAsList(0, 2);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/older/" + activites.get(1).getId() + ".json?limit=2", "",
        null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/older/" + activites.get(1).getId() + ".json?limit=2", "",
        null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamSpacesOlder(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamSpacesOlderData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites =
        activityManager.getActivitiesOfUserSpacesWithListAccess(rootIdentity).loadAsList(0, 2);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/older/" + activites.get(1).getId() + ".json?limit=3", "",
        null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(3, got.getActivities().size());
    assertEquals("space title 4", got.getActivities().get(0).getTitle());
    assertEquals("space title 3", got.getActivities().get(1).getTitle());
    assertEquals("space title 2", got.getActivities().get(2).getTitle());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/spaces/older/" + activites.get(1).getId() + ".json?limit=3", "",
        null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamFeedDefault(javax.ws.rs.core.UriInfo, String, String, String,
   * String)} with json format.
   */
  public void testgetActivityStreamFeedDefaultNoLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    ContainerResponse response =
        service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/feed/default.json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(7, got.getActivities().size());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/feed/default.json", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamFeedDefault(javax.ws.rs.core.UriInfo, String, String, String,
   * String)} with json format.
   */
  public void testgetActivityStreamFeedDefaultLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    ContainerResponse response =
        service("GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/default.json?limit=12", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(7, got.getActivities().size());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/default.json?limit=12", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamFeedDefault(javax.ws.rs.core.UriInfo, String, String, String,
   * String)} with json format.
   */
  public void testgetActivityStreamFeedDefaultData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    ContainerResponse response =
        service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/feed/default.json?limit=8", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(7, got.getActivities().size());
    assertEquals("space title 6", got.getActivities().get(0).getTitle());
    assertEquals("space title 5", got.getActivities().get(1).getTitle());
    assertEquals("space title 4", got.getActivities().get(2).getTitle());
    assertEquals("space title 3", got.getActivities().get(3).getTitle());
    assertEquals("space title 2", got.getActivities().get(4).getTitle());
    assertEquals("space title 1", got.getActivities().get(5).getTitle());
    assertEquals("space title 0", got.getActivities().get(6).getTitle());

    endSession();

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/feed/default.json?limit=8", "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamFeedNewer(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamFeedNewerNoLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites = activityManager.getActivityFeedWithListAccess(rootIdentity).loadAsList(0, 3);

    ContainerResponse response = service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/newer/" + activites.get(2).getId() + ".json", "", null,
        null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service(
        "GET",
        RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/newer/" + activites.get(2).getId() + ".json", "", null,
        null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamFeedNewer(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamFeedNewerLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites = activityManager.getActivityFeedWithListAccess(rootIdentity).loadAsList(0, 4);

    ContainerResponse response = service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/newer/" + activites.get(3).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/newer/" + activites.get(3).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamFeedNewer(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamFeedNewerData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());
    startSessionAs(maryIdentity.getRemoteId());

    this.maryPostActivityToRootWall(10);

    List<ExoSocialActivity> activities = activityManager.getActivityFeedWithListAccess(rootIdentity).loadAsList(0, 10);

    ContainerResponse response = service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/newer/" + activities.get(9).getId() + ".json?limit=8",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(8, got.getActivities().size());

    for (int i = 9, j = 0; i >= 2; i --, j ++) {
      assertEquals("mary posts activiy " + i + " to root'wall", got.getActivities().get(j).getTitle());
    }

    endSession();

    response =
      service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/newer/" + activities.get(9).getId() + ".json?limit=8",
        "", null, null);
    assertEquals(401, response.getStatus());

  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamFeedOlder(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamFeedOlderNoLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites = activityManager.getActivityFeedWithListAccess(rootIdentity).loadAsList(0, 2);

    ContainerResponse response = service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/older/" + activites.get(1).getId() + ".json", "",
        null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(5, got.getActivities().size());

    endSession();

    response =
      service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/older/" + activites.get(1).getId() + ".json", "",
        null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamFeedOlder(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamFeedOlderLimit() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());

    List<ExoSocialActivity> activites = activityManager.getActivityFeedWithListAccess(rootIdentity).loadAsList(0, 2);

    ContainerResponse response = service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/older/" + activites.get(1).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(2, got.getActivities().size());

    endSession();

    response =
      service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/older/" + activites.get(1).getId() + ".json?limit=2",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  /**
   * Tests {@link ActivityStreamResources#getActivityStreamFeedOlder(javax.ws.rs.core.UriInfo, String, String, String,
   * String, String)} with json format.
   */
  public void testgetActivityStreamFeedOlderData() throws Exception {

    startSessionAs(rootIdentity.getRemoteId());
    startSessionAs(maryIdentity.getRemoteId());

    this.maryPostActivityToRootWall(10);

    List<ExoSocialActivity> activities = activityManager.getActivityFeedWithListAccess(rootIdentity).loadAsList(0, 10);

    ContainerResponse response = service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/older/" + activities.get(9).getId() + ".json?limit=8",
        "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof ActivityList)) {
      fail();
    }

    ActivityList got = (ActivityList) response.getEntity();
    assertEquals(7, got.getActivities().size());

    for (int i = 6, j = 0; i >= 0; i --, j ++) {
      assertEquals("space title" + " " + i, got.getActivities().get(j).getTitle());
    }

    endSession();

    response =
      service(
        "GET", RESOURCE_URL + "/" + rootIdentity.getId() +  "/feed/older/" + activities.get(9).getId() + ".json?limit=8",
        "", null, null);
    assertEquals(401, response.getStatus());
  }

  public void testUnsuported() throws Exception {
    ContainerResponse response;

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/user/default.xml", "", null, null);
    assertEquals(415, response.getStatus());
    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/user/newer/none.xml", "", null, null);
    assertEquals(415, response.getStatus());
    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/user/older/none.xml", "", null, null);
    assertEquals(415, response.getStatus());

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/connections/default.xml", "", null, null);
    assertEquals(415, response.getStatus());
    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/connections/newer/none.xml", "", null, null);
    assertEquals(415, response.getStatus());
    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/connections/older/none.xml", "", null, null);
    assertEquals(415, response.getStatus());

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/spaces/default.xml", "", null, null);
    assertEquals(415, response.getStatus());
    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/spaces/newer/none.xml", "", null, null);
    assertEquals(415, response.getStatus());
    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/spaces/older/none.xml", "", null, null);
    assertEquals(415, response.getStatus());

    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/feed/default.xml", "", null, null);
    assertEquals(415, response.getStatus());
    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/feed/newer/none.xml", "", null, null);
    assertEquals(415, response.getStatus());
    response =
      service("GET", RESOURCE_URL + "/" + rootIdentity.getId() + "/feed/older/none.xml", "", null, null);
    assertEquals(415, response.getStatus());
  }

  /**
   * Load some data.
   *
   * @param identity the identity
   * @param nb the number of generated activities
   * @param titlePrefix the title prefix of the activities
   */
  private void loadIdentity(Identity identity, int nb, String titlePrefix) {

    for (int i = 0; i < nb; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(titlePrefix + " " + i);
      activity.setAppId("appId");
      activity.setType("type");
      activity.setPriority(0.5F);
      activity.setTitleId("title id");
      activity.setUserId(johnIdentity.getId());

      Map<String, String> params = new HashMap<String, String>();
      params.put("key1", "value1");
      params.put("key2", "value2");
      activity.setTemplateParams(params);

      //
      activityManager.saveActivityNoReturn(identity, activity);

      ExoSocialActivity comment1 = new ExoSocialActivityImpl();
      comment1.setTitle("comment 1");
      comment1.setUserId(rootIdentity.getId());
      activityManager.saveComment(activity, comment1);

      ExoSocialActivity comment2 = new ExoSocialActivityImpl();
      comment2.setTitle("comment 2");
      comment2.setUserId(rootIdentity.getId());

      activityManager.saveComment(activity, comment2);
    }
  }

  /**
   * Mary posts activities to Root's wall.
   *
   * @param number
   * @since 1.2.2
   */
  private void maryPostActivityToRootWall(int number) {
    for (int i = 0; i < number; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("mary posts activiy " + i + " to root'wall");
      activity.setAppId("appId");
      activity.setType("type");
      activity.setPriority(0.5F);
      activity.setTitleId("title id");
      activity.setUserId(maryIdentity.getId());

      Map<String, String> params = new HashMap<String, String>();
      params.put("key1", "value1");
      params.put("key2", "value2");
      activity.setTemplateParams(params);

      activityManager.saveActivityNoReturn(rootIdentity, activity);
    }
  }
}
