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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.api.models.ActivityRestListOut;
import org.exoplatform.social.service.test.AbstractResourceTest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit Test for {@link ActivityStreamResources}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since  Sep 26, 2011
 * @since 1.2.3
 */
public class ActivityStreamResourcesTest extends AbstractResourceTest {

  private final String RESOURCE_URL = "/api/social/v1-alpha3/portal/activity_stream/";

  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private SpaceService spaceService;
  private RelationshipManager relationshipManager;

  private Identity rootIdentity, johnIdentity, maryIdentity, demoIdentity;

  private List<Identity> tearDownIdentityList;
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Space> tearDownSpaceList;
  private List<Relationship> tearDownRelationshipList;
  /**
   * Adds {@link ActivityStreamResources}.
   *
   * @throws Exception
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();

    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);

    tearDownIdentityList = new ArrayList<Identity>();
    tearDownIdentityList.add(rootIdentity);
    tearDownIdentityList.add(johnIdentity);
    tearDownIdentityList.add(maryIdentity);
    tearDownIdentityList.add(demoIdentity);

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();
    tearDownRelationshipList = new ArrayList<Relationship>();

    addResource(ActivityStreamResources.class, null);
  }

  /**
   * Removes {@link ActivityStreamResources}.
   *
   * @throws Exception
   */
  @Override
  public void tearDown() throws Exception {
    for (Relationship relationship : tearDownRelationshipList) {
      relationshipManager.delete(relationship);
    }

    for (ExoSocialActivity activity : tearDownActivityList) {
      activityManager.deleteActivity(activity);
    }

    for (Space space : tearDownSpaceList) {
      spaceService.deleteSpace(space);
    }

    for (Identity identity : tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
    }
    removeResource(ActivityStreamResources.class);

    super.tearDown();
  }


  /**
   * General test cases:
   * - Not authenticated
   * - Wrong portal container
   * - Supported format
   *
   * @throws Exception
   */
  public void testGetActivityStreamByIdentityIdGeneralCase() throws Exception {
    String resourceUrl = RESOURCE_URL + "123456.json";
    testAccessResourceAsAnonymous("GET", resourceUrl, null, null);
    testAccessNotFoundResourceWithAuthentication("demo", "GET", resourceUrl, null);

    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "123456.xml", null, null,
            Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "123456.rss", null, null,
            Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "123456.atom", null, null,
            Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    testStatusCodeOfResource("demo", "GET", "/api/social/v1-alpha3/wrongPortalContainerName/activity_stream/123456.json",
                             null, null, Response.Status.BAD_REQUEST.getStatusCode());

  }

  /**
   * Tests default get activity stream without any optional query parameters.
   * - Test with a user identity
   * - Test with a space identity
   *
   * @throws Exception
   */
  public void testDefaultGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    // user identity
    {
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + ".json";
      // Johns gets activity stream of Demo which has 0 activities
      startSessionAs("john");
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return: " + 200, 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());

      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);
      // John gets activity stream of Demo's which has 15 activities

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, 20);
      compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + spaceIdentity.getId() + ".json";
      testAccessResourceWithoutPermission("john", "GET", resourceUrl, null);

      // Demo gets activity stream of space
      startSessionAs("demo");

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse3.getStatus() must return 200", 200, containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse4.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesWithListAccess(spaceIdentity).
                                                                loadAsList(0, 20);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
    }
  }

  /**
   * Tests get activity stream with short hand "me" param as the authenticated user who makes the request.
   *
   * @throws Exception
   */
  public void testMeParamGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    String resourceUrl = RESOURCE_URL + "me.json";
    startSessionAs("demo");

    // Demo creates 10 activities to his stream
    createActivities(demoIdentity, demoIdentity, 10);
    connectIdentities(demoIdentity, johnIdentity, true);
    // John creates 5 activities to Demo's stream
    createActivities(johnIdentity, demoIdentity, 5);
    // Demo gets activity stream of his which has 15 activities
    ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
    assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
        containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
    List<ExoSocialActivity> demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, 20);
    compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
  }

  /**
   * Tests: get activity stream with "limit" query parameter.
   *
   * @throws Exception
   */
  public void testLimitGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    int limit = 10;
    // user identity
    {
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + ".json?limit=" + limit;
      // Johns gets activity stream of Demo which has 0 activities
      startSessionAs("john");
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return: " + 200, 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());

      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);
      // John gets activity stream of Demo's which has 15 activities

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, limit);
      compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + spaceIdentity.getId() + ".json?limit="+limit;
      testAccessResourceWithoutPermission("john", "GET", resourceUrl, null);

      // Demo gets activity stream of space
      startSessionAs("demo");

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse3.getStatus() must return 200", 200, containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse4.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesWithListAccess(spaceIdentity).
                                                                loadAsList(0, limit);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
    }

  }

  public void testSinceIdGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    // user identity
    {
      //Wrong since_id => not found
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + ".json?since_id=" + 123456;

      testAccessNotFoundResourceWithAuthentication("john", "GET", resourceUrl, null);

      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      ExoSocialActivity baseActivity = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, 20).get(0);
      resourceUrl = RESOURCE_URL + demoIdentity.getId() + ".json?since_id=" + baseActivity.getId();
      // John creates 5 activities to Demo's stream
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);
      // John gets activity stream of Demo's which has 15 activities

      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return 200", 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> newerActivities = activityManager.getActivitiesWithListAccess(demoIdentity).
                                                                loadNewer(baseActivity, 10);

      compareActivities(newerActivities, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + spaceIdentity.getId() + ".json?since_id=" + 123456;
      testAccessResourceWithoutPermission("john", "GET", resourceUrl, null);
      testAccessNotFoundResourceWithAuthentication("demo", "GET", resourceUrl, null);

      // Demo gets activity stream of space
      startSessionAs("demo");

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      ExoSocialActivity baseActivity = activityManager.getActivitiesWithListAccess(spaceIdentity).
              loadAsList(0, 1).get(0);
      resourceUrl = RESOURCE_URL + spaceIdentity.getId() + ".json?since_id=" + baseActivity.getId();
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      List<ExoSocialActivity> newerActivities = activityManager.getActivitiesWithListAccess(spaceIdentity).
                                                                loadNewer(baseActivity, 10);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return: " + 200, 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(newerActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }
  }


  public void testMaxIdGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    // user identity
    {
      //Wrong since_id => not found
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + ".json?max_id=" + 123456;

      testAccessNotFoundResourceWithAuthentication("john", "GET", resourceUrl, null);

      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      // John creates 5 activities to Demo's stream
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);
      ExoSocialActivity baseActivity = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, 5).get(4);
      resourceUrl = RESOURCE_URL + demoIdentity.getId() + ".json?max_id=" + baseActivity.getId();
      // John gets activity stream of Demo's which has 15 activities

      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return 200", 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> olderActivities = activityManager.getActivitiesWithListAccess(demoIdentity).
                                                                loadOlder(baseActivity, 20);

      compareActivities(olderActivities, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + spaceIdentity.getId() + ".json?max_id=" + 123456;
      testAccessResourceWithoutPermission("john", "GET", resourceUrl, null);
      testAccessNotFoundResourceWithAuthentication("demo", "GET", resourceUrl, null);

      // Demo gets activity stream of space
      startSessionAs("demo");

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);
      ExoSocialActivity baseActivity = activityManager.getActivitiesWithListAccess(spaceIdentity).
              loadAsList(0, 5).get(4);
      resourceUrl = RESOURCE_URL + spaceIdentity.getId() + ".json?max_id=" + baseActivity.getId();

      List<ExoSocialActivity> olderActivities = activityManager.getActivitiesWithListAccess(spaceIdentity).
                                                                loadOlder(baseActivity, 20);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return: " + 200, 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));

      compareActivities(olderActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }
  }


  public void testNumberOfCommentsGetActivityStreamByIdentityWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    // user identity
    int numberOfComments = 3;
    {
      startSessionAs("john");
      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      // John creates 5 activities to Demo's stream
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);
      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, 20);
      createComment(demoActivities.get(0), demoIdentity, numberOfComments + 1);
      createComment(demoActivities.get(0), johnIdentity, 1);
      createComment(demoActivities.get(3), johnIdentity, 5);
      createComment(demoActivities.get(7), demoIdentity, 6);
      // John gets activity stream of Demo's which has 15 activities
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + ".json?number_of_comments=" + numberOfComments;
      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      
      //need to query with new order base on What's hot pattern.
      demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, 20);
      compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
      compareNumberOfComments(demoActivities, (ActivityRestListOut) containerResponse2.getEntity(), numberOfComments);
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + spaceIdentity.getId() + ".json?number_of_comments=" + numberOfComments;
      testAccessResourceWithoutPermission("john", "GET", resourceUrl, null);

      // Demo gets activity stream of space
      startSessionAs("demo");

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse3.getStatus() must return 200", 200, containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesWithListAccess(spaceIdentity).
                                                                loadAsList(0, 20);
      createComment(spaceActivities.get(1), demoIdentity, numberOfComments + 2);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse4.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      
      //need to query with new order base on What's hot pattern.
      spaceActivities = activityManager.getActivitiesWithListAccess(spaceIdentity).loadAsList(0, 20);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
      compareNumberOfComments(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity(), numberOfComments);
    }
  }

  public void testNumberOfLikesGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    int numberOfLikes = 2;
    // user identity
    {
      startSessionAs("john");
      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      // John creates 5 activities to Demo's stream
      connectIdentities(demoIdentity, johnIdentity, true);
      connectIdentities(demoIdentity, maryIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);
      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, 20);
      ExoSocialActivity likeActivity1 = demoActivities.get(0);
      activityManager.saveLike(likeActivity1, demoIdentity);
      activityManager.saveLike(likeActivity1, johnIdentity);
      activityManager.saveLike(likeActivity1, maryIdentity);

      ExoSocialActivity likeActivity2 = demoActivities.get(3);
      activityManager.saveLike(likeActivity2, demoIdentity);
      activityManager.saveLike(likeActivity2, johnIdentity);

      ExoSocialActivity likeActivity3 = demoActivities.get(4);
      activityManager.saveLike(likeActivity3, johnIdentity);

      // John gets activity stream of Demo's which has 15 activities
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + ".json?number_of_likes=" + numberOfLikes;
      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, 20);
      compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
      compareNumberOfLikes(demoActivities, (ActivityRestListOut) containerResponse2.getEntity(), numberOfLikes);
    }
  }

  /**
   * Tests get activity stream with all query parameters
   *
   * @throws Exception
   */
  public void testAllQueryParamsGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    int limit = 10;
    int numberOfComments = 2;
    int numberOfLikes = 2;

    // user identity
    {
      startSessionAs("john");
      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      // John creates 5 activities to Demo's stream
      connectIdentities(demoIdentity, johnIdentity, true);
      connectIdentities(demoIdentity, maryIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);
      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity).
                                               loadAsList(0, limit);
      createComment(demoActivities.get(3), demoIdentity, 2);
      createComment(demoActivities.get(3), johnIdentity, 2);
      createComment(demoActivities.get(3), maryIdentity, 2);

      createComment(demoActivities.get(4), johnIdentity, 1);

      createComment(demoActivities.get(6), maryIdentity, 7);
      createComment(demoActivities.get(6), johnIdentity, 4);

      activityManager.saveLike(demoActivities.get(3), demoIdentity);
      activityManager.saveLike(demoActivities.get(3), johnIdentity);
      activityManager.saveLike(demoActivities.get(3), maryIdentity);

      activityManager.saveLike(demoActivities.get(5), maryIdentity);
      // John gets activity stream of Demo's which has 15 activities
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + ".json?limit=" + limit +
                                                                        "&number_of_likes=" + numberOfLikes +
                                                                        "&number_of_comments=" + numberOfComments;
      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      
      //need to query with new order base on What's hot pattern.
      demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, limit);
      compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
      compareNumberOfComments(demoActivities, (ActivityRestListOut) containerResponse2.getEntity(), numberOfComments);
      compareNumberOfLikes(demoActivities, (ActivityRestListOut) containerResponse2.getEntity(), numberOfLikes);
    }
  }

  /**
   * Tests get activity stream with all query parameters
   *
   * @throws Exception
   */
  public void testAllInvalidOptionalQueryParamsGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    // user identity
    {
      startSessionAs("john");
      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      // John creates 5 activities to Demo's stream
      connectIdentities(demoIdentity, johnIdentity, true);
      connectIdentities(demoIdentity, maryIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);
      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity).
                                               loadAsList(0, 20);
      createComment(demoActivities.get(3), demoIdentity, 2);
      createComment(demoActivities.get(3), johnIdentity, 2);
      createComment(demoActivities.get(3), maryIdentity, 2);

      createComment(demoActivities.get(4), johnIdentity, 1);

      createComment(demoActivities.get(6), maryIdentity, 7);
      createComment(demoActivities.get(6), johnIdentity, 4);

      activityManager.saveLike(demoActivities.get(3), demoIdentity);
      activityManager.saveLike(demoActivities.get(3), johnIdentity);
      activityManager.saveLike(demoActivities.get(3), maryIdentity);

      activityManager.saveLike(demoActivities.get(5), maryIdentity);
      // John gets activity stream of Demo's which has 15 activities
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + ".json?limit=-1" +
                                                                        "&number_of_likes=-1" +
                                                                        "&number_of_comments=-1";
      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      //need to query with new order base on What's hot pattern.
      demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, 20);
      compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
      compareNumberOfComments(demoActivities, (ActivityRestListOut) containerResponse2.getEntity(), 0);
      compareNumberOfLikes(demoActivities, (ActivityRestListOut) containerResponse2.getEntity(), 0);
    }
  }

  /**
   * Test {@link ActivityStreamResources#getActivityFeedOfAuthenticated(UriInfo, String, String, int, String, String, int, int)}
   *
   * @throws Exception
   */
  public void testGetActivityFeedOfAuthenticatedGeneralCase() throws Exception {
    String resourceUrl = RESOURCE_URL + "feed.json";
    testAccessResourceAsAnonymous("GET", resourceUrl, null, null);
    
    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "feed.xml", null, null,
                             Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "feed.rss", null, null,
                             Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "feed.atom", null, null,
                             Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    testStatusCodeOfResource("demo", "GET", "/api/social/v1-alpha3/wrongPortalContainerName/activity_stream/feed.json",
                             null, null, Response.Status.BAD_REQUEST.getStatusCode());
  }
  
  /**
   * Test {@link ActivityStreamResources#getActivityFeedOfAuthenticated(javax.ws.rs.core.UriInfo, String, String, int, String, String, int, int)}
   * 
   * @throws Exception
   */
  public void testDefaultGetActivityFeedOfAuthenticatedWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    String resourceUrl = RESOURCE_URL + "feed.json";
    // user identity
    {
      startSessionAs("demo");

      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return: " + 200, 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());
      
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 15);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity());
    }

    // space identity
    {
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse3.getStatus() must return 200", 200, containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 40);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse3.getEntity());

      createActivities(spaceIdentity, spaceIdentity, 10);
      
      Space space = spaceService.getSpaceByPrettyName("my_space_1");
      spaceService.addMember(space, "demo");
      
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse4.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).
                                                                loadAsList(0, 40);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse4.getEntity());
    }
  }
  
  /**
   * Test {@link ActivityStreamResources#getActivityFeedOfAuthenticated(javax.ws.rs.core.UriInfo, String, String, int, String, String, int, int)}
   * 
   * @throws Exception
   */
  public void testLimitGetActivityFeedOfAuthenticatedWithJsonFormat() throws Exception {
    int limit = 10;
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    String resourceUrl = RESOURCE_URL + "feed.json?limit=" + limit;
    
    // user identity
    {
      startSessionAs("demo");

      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return: " + 200, 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());
      
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, limit);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity());
    }

    // space identity
    {
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse3.getStatus() must return 200", 200, containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, limit);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse3.getEntity());

      createActivities(spaceIdentity, spaceIdentity, 10);
      
      Space space = spaceService.getSpaceByPrettyName("my_space_1");

      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse4.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).
                                                                loadAsList(0, limit);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse4.getEntity());
    }
  }
  
  /**
   * Test {@link ActivityStreamResources#getActivityFeedOfAuthenticated(javax.ws.rs.core.UriInfo, String, String, int, String, String, int, int)}
   * 
   * @throws Exception
   */
  public void testSinceIdGetActivityFeedOfAuthenticatedWithJsonFormat() throws Exception {
    // user identity
    {
      startSessionAs("demo");
      
      //Wrong since_id => not found
      String resourceUrl = RESOURCE_URL + "feed.json?since_id=" + 123456;
      testAccessNotFoundResourceWithAuthentication("demo", "GET", resourceUrl, null);
      
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);
      
      ExoSocialActivity baseActivity = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                      .loadAsList(0, 20).get(14);
      resourceUrl = RESOURCE_URL + "feed.json?since_id=" + baseActivity.getId();

      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return 200", 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                                  .loadNewer(baseActivity, 20);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      ExoSocialActivity baseActivity = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                      .loadAsList(0, 20).get(14);
      String resourceUrl = RESOURCE_URL + "feed.json?since_id=" + baseActivity.getId();
      
      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadNewer(baseActivity, 20);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity());

      createActivities(spaceIdentity, spaceIdentity, 10);
      
      Space space = spaceService.getSpaceByPrettyName("my_space_1");

      createActivities(demoIdentity, spaceIdentity, 5);

      baseActivity = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                    .loadAsList(0, 40).get(29);
      
      resourceUrl = RESOURCE_URL + "feed.json?since_id=" + baseActivity.getId();
      
      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse3.getStatus() must return 200", 200, containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadNewer(baseActivity, 40);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse3.getEntity());
    }
  }
  
  /**
   * Test {@link ActivityStreamResources#getActivityFeedOfAuthenticated(javax.ws.rs.core.UriInfo, String, String, int, String, String, int, int)}
   * 
   * @throws Exception
   */
  public void testMaxIdGetActivityFeedOfAuthenticatedWithJsonFormat() throws Exception {
    // user identity
    {
      startSessionAs("demo");
      
      //Wrong since_id => not found
      String resourceUrl = RESOURCE_URL + "feed.json?max_id=" + 123456;
      testAccessNotFoundResourceWithAuthentication("demo", "GET", resourceUrl, null);
      
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);

      ExoSocialActivity baseActivity = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                      .loadAsList(0, 20).get(0);
      
      resourceUrl = RESOURCE_URL + "feed.json?max_id=" + baseActivity.getId();
      
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return 200", 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                                  .loadOlder(baseActivity, 20);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      createActivities(spaceIdentity, spaceIdentity, 10);
      
      Space space = spaceService.getSpaceByPrettyName("my_space_1");

      createActivities(demoIdentity, spaceIdentity, 5);

      ExoSocialActivity baseActivity = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 40).get(0);
      String resourceUrl = RESOURCE_URL + "feed.json?max_id=" + baseActivity.getId();
      
      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadOlder(baseActivity, 40);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity());
    }
  }
  
  /**
   * Test {@link ActivityStreamResources#getActivityFeedOfAuthenticated(javax.ws.rs.core.UriInfo, String, String, int, String, String, int, int)}
   * 
   * @throws Exception
   */
  public void testNumberOfCommentsGetActivityFeedOfAuthenticatedWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    int numberOfComments = 5;
    String resourceUrl = RESOURCE_URL + "feed.json?number_of_comments=" + numberOfComments;
    // user identity
    {
      startSessionAs("demo");
      
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return: " + 200, 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());

      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);

      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 20);
      for (ExoSocialActivity activity : demoActivitiesFeed) {
        this.createComment(activity, demoIdentity, 10);
      }
      
      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 20);
      
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity());
      compareNumberOfComments(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity(),
              numberOfComments);
    }

    // space identity
    {
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse3.getStatus() must return 200", 200, containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 40);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse3.getEntity());

      createActivities(spaceIdentity, spaceIdentity, 10);
      
      Space space = spaceService.getSpaceByPrettyName("my_space_1");

      createActivities(demoIdentity, spaceIdentity, 5);
      
      ListAccess<ExoSocialActivity> spaceActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
      for (ExoSocialActivity activity : spaceActivities.load(0, 20)) {
        this.createComment(activity, demoIdentity, 10);
      }
      
      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse4.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).
                                                                loadAsList(0, 40);
      
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse4.getEntity());
      compareNumberOfComments(demoActivitiesFeed, (ActivityRestListOut) containerResponse4.getEntity(),
              numberOfComments);
    }
  }
  
  /**
   * Test {@link ActivityStreamResources#getActivityFeedOfAuthenticated(javax.ws.rs.core.UriInfo, String, String, int, String, String, int, int)}
   * 
   * @throws Exception
   */
  public void testNumberOfLikesGetActivityFeedOfAuthenticatedWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    int numberOfLikes = 2;
    String resourceUrl = RESOURCE_URL + "feed.json?number_of_likes=" + numberOfLikes;
    // user identity
    {
      startSessionAs("demo");
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return: " + 200, 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());

      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      connectIdentities(demoIdentity, maryIdentity, true);
      connectIdentities(demoIdentity, rootIdentity, true);
      connectIdentities(johnIdentity, maryIdentity, true);
      connectIdentities(johnIdentity, rootIdentity, true);
      
      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);
      // John gets activity stream of Demo's which has 15 activities

      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 20);
      for (ExoSocialActivity activity : demoActivitiesFeed) {
        activityManager.saveLike(activity, demoIdentity);
        activityManager.saveLike(activity, johnIdentity);
        activityManager.saveLike(activity, maryIdentity);
        activityManager.saveLike(activity, rootIdentity);
      }
      
      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 20);
      
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity());
      compareNumberOfLikes(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity(), 
                           numberOfLikes);
    }
    
    // space identity
    {
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse3.getStatus() must return 200", 200, containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 40);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse3.getEntity());

      createActivities(spaceIdentity, spaceIdentity, 10);
      
      Space space = spaceService.getSpaceByPrettyName("my_space_1");

      createActivities(demoIdentity, spaceIdentity, 5);
      
      ListAccess<ExoSocialActivity> demoSpaceActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
      for (ExoSocialActivity activity : demoSpaceActivities.load(0, 20)) {
        activityManager.saveLike(activity, demoIdentity);
        activityManager.saveLike(activity, johnIdentity);
        activityManager.saveLike(activity, maryIdentity);
        activityManager.saveLike(activity, rootIdentity);
      }
      
      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse4.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).
                                                                loadAsList(0, 40);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse4.getEntity());
      compareNumberOfLikes(demoActivitiesFeed, (ActivityRestListOut) containerResponse4.getEntity(), 
                           numberOfLikes);
    }
  }
  
  /**
   * Test {@link ActivityStreamResources#getActivityFeedOfAuthenticated(javax.ws.rs.core.UriInfo, String, String, int, String, String, int, int)}
   * 
   * @throws Exception
   */
  public void testAllQueryParamsGetActivityFeedOfAuthenticatedWithJsonFormat() throws Exception {
    int limit = 10;
    int since_id = 123456;
    int numberOfComments = 5;
    int numberOfLikes = 2;
    String resourceUrl = RESOURCE_URL + "feed.json?limit=" + limit
                         + "&since_id=" + since_id + "&number_of_comments=" + numberOfComments
                         + "@number_of_likes=" + numberOfLikes;
    // user identity
    {
      startSessionAs("demo");
      
      //Wrong since_id => not found
      testAccessNotFoundResourceWithAuthentication("demo", "GET", resourceUrl, null);

      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      connectIdentities(demoIdentity, maryIdentity, true);
      connectIdentities(demoIdentity, rootIdentity, true);
      connectIdentities(johnIdentity, maryIdentity, true);
      connectIdentities(johnIdentity, rootIdentity, true);
      
      createActivities(johnIdentity, demoIdentity, 5);

      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 20);

      for (ExoSocialActivity activity : demoActivitiesFeed) {
        this.createComment(activity, demoIdentity, 10);
        
        activityManager.saveLike(activity, demoIdentity);
        activityManager.saveLike(activity, johnIdentity);
        activityManager.saveLike(activity, maryIdentity);
        activityManager.saveLike(activity, rootIdentity);
      }
      
      ExoSocialActivity baseActivity = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                      .loadAsList(0, 20).get(14);
      resourceUrl = RESOURCE_URL + "feed.json?limit=" + limit
                                 + "&since_id=" + baseActivity.getId() + "&number_of_comments=" + numberOfComments
                                 + "&number_of_likes=" + numberOfLikes;
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return 200", 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity).loadNewer(baseActivity, limit);
      
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse1.getEntity());
      compareNumberOfComments(demoActivitiesFeed, (ActivityRestListOut) containerResponse1.getEntity(), 
                              numberOfComments);
      compareNumberOfLikes(demoActivitiesFeed, (ActivityRestListOut) containerResponse1.getEntity(), 
                           numberOfLikes);
    }

    // space identity
    {
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      createActivities(spaceIdentity, spaceIdentity, 10);
      
      Space space = spaceService.getSpaceByPrettyName("my_space_1");

      createActivities(demoIdentity, spaceIdentity, 5);

      ListAccess<ExoSocialActivity> spaceActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
      for (ExoSocialActivity activity : spaceActivities.load(0, 20)) {
        this.createComment(activity, demoIdentity, 10);
        activityManager.saveLike(activity, demoIdentity);
        activityManager.saveLike(activity, johnIdentity);
        activityManager.saveLike(activity, maryIdentity);
        activityManager.saveLike(activity, rootIdentity);
      }
      
      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                          .loadAsList(0, limit);
      
      compareNumberOfComments(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity(), 
                              numberOfComments);
      compareNumberOfLikes(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity(), 
                           numberOfLikes);
    }
  }

  /**
   * General test cases:
   * - Not authenticated
   * - Wrong portal container
   * - UnSupported format
   * - Normal with the right format (JSON)
   *
   * @throws Exception
   */
  public void testGeneralCaseGetActivityConnectionsOfAuthenticated() throws Exception {
    String resourceUrl = RESOURCE_URL + "xyz.json";
    testAccessNotFoundResourceWithAuthentication("demo", "GET", resourceUrl, null);
    
    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "connections.xml", null, null,
            Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "connections.rss", null, null,
            Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "connections.atom", null, null,
            Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    
    testStatusCodeOfResource("demo", "GET", "/api/social/v1-alpha3/wrongPortalContainerName/activity_stream/" + 
    		"connections.json", null, null, Response.Status.BAD_REQUEST.getStatusCode());
    
    // Johns gets activity stream of Demo which has 0 activities
    resourceUrl = RESOURCE_URL + "connections.json";
    
    startSessionAs("john");
    
    // Make connection between demo and john
    connectIdentities(demoIdentity, johnIdentity, true);
    
    ContainerResponse rsp = service("GET", resourceUrl, "", null, null);
    assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(), Response.Status
    		.OK.getStatusCode(), rsp.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
        rsp.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
    
    compareActivities(new ArrayList<ExoSocialActivity>(), (ActivityRestListOut)rsp.getEntity());
    endSession();
  }

  /**
   * Tests default get activity stream of connection without any optional query parameters.
   *
   * @throws Exception
   */
  public void testDefaultGetActivityConnectionsOfAuthenticatedWithJsonFormat() throws Exception {
    // user identity
    {
      String resourceUrl = RESOURCE_URL + "connections.json";
      
      // John gets activity stream of Demo which has 0 activities
      startSessionAs("demo");

      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      
      // Make connection between demo and john
      connectIdentities(demoIdentity, johnIdentity, true);
      
      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);
      
      // John gets activity stream of Demo's which has 15 activities
      ContainerResponse rsp1 = service("GET", resourceUrl, "", null, null);
      assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(),
                   Response.Status.OK.getStatusCode(), rsp1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          rsp1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      
      List<ExoSocialActivity> demoActivities = activityManager
        .getActivitiesOfConnectionsWithListAccess(demoIdentity).loadAsList(0, 20);
      compareActivities(demoActivities, (ActivityRestListOut) rsp1.getEntity());
      
      endSession();
    }

  }

  /**
   * Tests: get activity stream of connection with limit parameter.
   *
   * @throws Exception
   */
  public void testLimitGetActivityConnectionsOfAuthenticatedWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    int limit = 10;

    String resourceUrl = RESOURCE_URL + "connections.json?limit=" + limit;

    // Johns gets activity stream of Demo which has 0 activities
    startSessionAs("demo");
    ContainerResponse rsp = service("GET", resourceUrl, "", null, null);
    assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(), Response.Status
            .OK.getStatusCode(), rsp.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
        rsp.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
    compareActivities(emptyList, (ActivityRestListOut) rsp.getEntity());

    // Demo creates 10 activities to his stream
    createActivities(demoIdentity, demoIdentity, 10);

    // Make connection between demo and john
    connectIdentities(demoIdentity, johnIdentity, true);

    // John creates 5 activities to Demo's stream
    createActivities(johnIdentity, demoIdentity, 5);

    // John gets activity stream of Demo's which has 15 activities
    ContainerResponse rsp1 = service("GET", resourceUrl, "", null, null);
    assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(), Response.Status
            .OK.getStatusCode(), rsp1.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
        rsp1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));

    List<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity).loadAsList(0, limit);
    compareActivities(demoActivities, (ActivityRestListOut) rsp1.getEntity());

    limit = 3;
    resourceUrl = RESOURCE_URL + "connections.json?limit=" + limit;
    rsp1 = service("GET", resourceUrl, "", null, null);
    assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(), Response.Status
            .OK.getStatusCode(), rsp1.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
        rsp1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
    demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity).loadAsList(0, limit);
    compareActivities(demoActivities, (ActivityRestListOut) rsp1.getEntity());

    endSession();
  }

  /**
   * Tests get activity of connection with since_id parameter.
   *
   * @throws Exception
   */
  public void testSinceIdGetActivityConnectionsOfAuthenticatedWithJsonFormat() throws Exception {
      //Wrong since_id => not found
      String resourceUrl = RESOURCE_URL + "connections.json?since_id=" + 123456;

      testAccessNotFoundResourceWithAuthentication("john", "GET", resourceUrl, null);

      startSessionAs("demo");
      
      // Make connection between demo and john
      connectIdentities(demoIdentity, johnIdentity, true);
      
      // Make connection between demo and mary
      connectIdentities(demoIdentity, maryIdentity, true);
      
      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      
      // John creates 10 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 10);
      
      // Mary creates 10 activities to Demo's stream
      createActivities(maryIdentity, demoIdentity, 10);
      
      ExoSocialActivity baseActivity = activityManager.getActivitiesOfConnectionsWithListAccess(johnIdentity).loadAsList(0, 20).get(0);
      resourceUrl = RESOURCE_URL + "connections.json?since_id=" + baseActivity.getId();
      
      // Demo gets activities in of his connections base on the first connection's activity => 20
      ContainerResponse rsp = service("GET", resourceUrl, "", null, null);
      assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(), Response.Status
      		.OK.getStatusCode(), rsp.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          rsp.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      
      List<ExoSocialActivity> newerActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity).
                                                                loadNewer(baseActivity, 20);

      compareActivities(newerActivities, (ActivityRestListOut) rsp.getEntity());
      
      endSession();
  }

  /**
   * Tests get activity of connection with max_id parameter.
   *
   * @throws Exception
   */
  public void testMaxIdGetActivityConnectionsOfAuthenticatedWithJsonFormat() throws Exception {
      //Wrong since_id => not found
      String resourceUrl = RESOURCE_URL + "connections.json?max_id=" + 123456;

      testAccessNotFoundResourceWithAuthentication("john", "GET", resourceUrl, null);

      startSessionAs("demo");
      
      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      
      // Make connection between demo and john
      connectIdentities(demoIdentity, johnIdentity, true);
      
      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);
      
      ExoSocialActivity baseActivity = activityManager.getActivitiesOfConnectionsWithListAccess(johnIdentity).loadAsList(0, 5).get(4);
      resourceUrl = RESOURCE_URL + "connections.json?max_id=" + baseActivity.getId();

      ContainerResponse rsp = service("GET", resourceUrl, "", null, null);
      assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(), Response.Status
      		.OK.getStatusCode(), rsp.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          rsp.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> olderActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity).
                                                                loadOlder(baseActivity, 20);

      compareActivities(olderActivities, (ActivityRestListOut) rsp.getEntity());

      endSession();
  }

  /**
   * Tests get activity of connection with number_of_comments parameter.
   *
   * @throws Exception
   */
  public void testNumberOfCommentsGetActivityConnectionsOfAuthenticatedWithJsonFormat() throws Exception {
      int numberOfComments = 3;
      startSessionAs("demo");
      
      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      
      // Make connection between demo and john
      connectIdentities(demoIdentity, johnIdentity, true);
      
      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);
      
      // Demo comments on john's activities
      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity).loadAsList(0, 20);
      for (ExoSocialActivity act : demoActivities) {
        createComment(act, johnIdentity, 5);
      }
      
      String resourceUrl = RESOURCE_URL + "connections.json?number_of_comments=" + numberOfComments;
      ContainerResponse rsp1 = service("GET", resourceUrl, "", null, null);
      assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(), Response.Status
      		.OK.getStatusCode(), rsp1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          rsp1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity).loadAsList(0, 20);
      compareActivities(demoActivities, (ActivityRestListOut) rsp1.getEntity());
      compareNumberOfComments(demoActivities, (ActivityRestListOut) rsp1.getEntity(), numberOfComments);
	  
      endSession();
  }

  /**
   * Tests get activity of connection with number_of_likes parameter.
   *
   * @throws Exception
   */
  public void testNumberOfLikesGetActivityConnectionsOfAuthenticatedWithJsonFormat() throws Exception {
	  int numberOfLikes = 3;
      startSessionAs("demo");
      
      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      
      // Make connection between demo and john
      connectIdentities(demoIdentity, johnIdentity, true);
      
      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);
      
      // Demo comments on john's activities
      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity).loadAsList(0, 20);
      for (ExoSocialActivity act : demoActivities) {
        activityManager.saveLike(act, demoIdentity);
        activityManager.saveLike(act, johnIdentity);
        activityManager.saveLike(act, maryIdentity);
        activityManager.saveLike(act, rootIdentity);
      }
      
      String resourceUrl = RESOURCE_URL + "connections.json?number_of_likes=" + numberOfLikes;
      ContainerResponse rsp1 = service("GET", resourceUrl, "", null, null);
      assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(), Response.Status
      		.OK.getStatusCode(), rsp1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          rsp1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity).loadAsList(0, 20);
      compareActivities(demoActivities, (ActivityRestListOut) rsp1.getEntity());
      compareNumberOfLikes(demoActivities, (ActivityRestListOut) rsp1.getEntity(), numberOfLikes);
	  
      endSession();
  }

  /**
   * Tests get activity of connection with full parameters in url request.
   *
   * @throws Exception
   */
  public void testFullParamsGetActivityConnectionsOfAuthenticatedWithJsonFormat() throws Exception {
    int limit = 3;
    int numberOfComments = 3;
    int numberOfLikes = 3;

    startSessionAs("demo");

    // Demo creates 10 activities to his stream
    createActivities(demoIdentity, demoIdentity, 10);

    // Make connection between demo and john
    connectIdentities(demoIdentity, johnIdentity, true);

    // John creates 5 activities to Demo's stream
    createActivities(johnIdentity, demoIdentity, 5);

    // Demo comments on john's activities
    List<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity).loadAsList(0, 20);
    for (ExoSocialActivity act : demoActivities) {
      createComment(act, johnIdentity, 5);
      activityManager.saveLike(act, demoIdentity);
      activityManager.saveLike(act, johnIdentity);
      activityManager.saveLike(act, maryIdentity);
      activityManager.saveLike(act, rootIdentity);
    }

    ExoSocialActivity baseSinceIdActivity = activityManager.getActivitiesOfConnectionsWithListAccess(johnIdentity).loadAsList(0, 5).get(4);
    ExoSocialActivity baseMaxIdActivity = activityManager.getActivitiesOfConnectionsWithListAccess(johnIdentity).loadAsList(0, 5).get(4);

    String resourceUrl = RESOURCE_URL + "connections.json?limit=" + limit + "&since_id=" + baseSinceIdActivity.getId()
            + "&max_id=" + baseMaxIdActivity.getId() + "&number_of_comments=" + numberOfComments
            + "&number_of_likes=" + numberOfLikes;
    ContainerResponse rsp1 = service("GET", resourceUrl, "", null, null);
    assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(), Response.Status
            .OK.getStatusCode(), rsp1.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
        rsp1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
    demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity).loadNewer(baseSinceIdActivity, limit);

    compareActivities(demoActivities, (ActivityRestListOut) rsp1.getEntity());
    compareNumberOfComments(demoActivities, (ActivityRestListOut) rsp1.getEntity(), numberOfComments);
    compareNumberOfLikes(demoActivities, (ActivityRestListOut) rsp1.getEntity(), numberOfLikes);

    endSession();
  }

  /**
   * General test cases:
   * - Not authenticated
   * - Wrong portal container
   * - Supported format
   *
   * @throws Exception
   */
  public void testGetActivitySpacesOfAuthenticatedGeneralCase() throws Exception {
    String resourceUrl = RESOURCE_URL + "12356/spaces.json";
    //testAccessResourceAsAnonymous("GET", resourceUrl, null, null);
    
    testAccessNotFoundResourceWithAuthentication("demo", "GET", resourceUrl, null);

    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "spaces.xml", null, null,
            Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "spaces.rss", null, null,
            Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    testStatusCodeOfResource("demo", "GET", RESOURCE_URL + "spaces.atom", null, null,
            Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    testStatusCodeOfResource("demo", "GET", "/api/social/v1-alpha3/wrongPortalContainerName/activity_stream/spaces.json",
                            null, null, Response.Status.BAD_REQUEST.getStatusCode());

  }


  /**
   * Tests default get activity stream without any optional query parameters.
   * - Test with a user identity
   * - Test with a space identity
  *
   * @throws Exception
   */
  public void testDefaultGetActivitySpacesOfAuthenticatedWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    // user identity
    {
      String resourceUrl = RESOURCE_URL + "spaces.json";
      // Johns gets activity stream of Demo which has 0 activities
      startSessionAs("john");
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return: " + 200, 200,
                   containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + "spaces.json";

      // Demo gets activity stream of space
      startSessionAs("demo");

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse3.getStatus() must return 200", 200, containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse4.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity).loadAsList(0, 20);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
    }
    endSession();
  }
  
  /**
   * Tests: get activity stream with "limit" query parameter.
   *
   * @throws Exception
   */
  public void testLimitGetActivitySpacesOfAuthenticatedWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    int limit = 10;
    // user identity
    {
      String resourceUrl = RESOURCE_URL + "spaces.json?limit=" + limit;
      // Johns gets activity stream of Demo which has 0 activities
      startSessionAs("john");
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return: " + 200, 200, containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse1.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + "spaces.json?limit=" + limit;

      // Demo gets activity stream of space
      startSessionAs("demo");

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse3.getStatus() must return 200", 200, containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse4.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity).loadAsList(0, limit);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
    }
    endSession();

  }

  public void testSinceIdGetActivitySpacesOfAuthenticatedWithJsonFormat() throws Exception {
    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      // Demo gets activity stream of space
      startSessionAs("demo");

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      ExoSocialActivity baseActivity = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                                      .loadAsList(0, 1)
                                                      .get(0);
      String resourceUrl = RESOURCE_URL + "spaces.json?since_id=" + baseActivity.getId();
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      List<ExoSocialActivity> newerActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                                               .loadNewer(baseActivity, 10);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return: " + 200, 200,
                   containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(newerActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }
    endSession();
  }


  public void testMaxIdGetActivitySpacesOfAuthenticatedWithJsonFormat() throws Exception {

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      // Demo gets activity stream of space
      startSessionAs("demo");

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);
      ExoSocialActivity baseActivity = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                                      .loadAsList(0, 5)
                                                      .get(4);
      String resourceUrl = RESOURCE_URL + "spaces.json?max_id=" + baseActivity.getId();

      List<ExoSocialActivity> olderActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                                               .loadOlder(baseActivity, 20);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return: " + 200, 200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));

      compareActivities(olderActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }
    endSession();
  }


  public void testNumberOfCommentsGetActivitySpacesOfAuthenticatedWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    // user identity
    int numberOfComments = 3;
    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + "spaces.json?number_of_comments=" + numberOfComments;

      // Demo gets activity stream of space
      startSessionAs("demo");

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse3.getStatus() must return 200",
                   200,
                   containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity).loadAsList(0, 20);
      createComment(spaceActivities.get(1), demoIdentity, numberOfComments + 2);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse3.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      
      //need to query with new order base on What's hot pattern.
      spaceActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity).loadAsList(0, 20);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
      compareNumberOfComments(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity(), numberOfComments);
    }
    endSession();
  }

  public void testNumberOfLikesGetActivitySpacesOfAuthenticatedWithJsonFormat() throws Exception {
    int numberOfLikes = 2;
    // user identity
    {

      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1", false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + "spaces.json?number_of_likes=" + numberOfLikes;

      // Demo gets activity stream of space
      startSessionAs("demo");

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);
      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                                              .loadAsList(0, 20);

      ExoSocialActivity likeActivity = demoActivities.get(0);
      activityManager.saveLike(likeActivity, demoIdentity);
      activityManager.saveLike(likeActivity, johnIdentity);
      activityManager.saveLike(likeActivity, maryIdentity);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200,
                   containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse2.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
      compareNumberOfLikes(demoActivities, (ActivityRestListOut) containerResponse2.getEntity(), numberOfLikes);
    }
  }
  
  /**
   * Test
   * {@link ActivityStreamResources#getActivityFeedByTimestampOfAuthenticated(javax.ws.rs.core.UriInfo, String, String, int, Long, Long, int, int)}
   * 
   * @throws Exception
   */
  public void testDefaultGetActivityFeedByTimestampOfAuthenticatedWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    String resourceUrl = RESOURCE_URL + "feedByTimestamp.json";
    // user identity
    {
      startSessionAs("demo");

      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse1.getStatus());
      assertTrue(containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());
      System.out.println(containerResponse1.getResponse().toString());
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse2.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                                  .loadAsList(0, 15);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity());
    }

    // space identity
    {
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals(200, containerResponse3.getStatus());
      assertTrue(containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                                  .loadAsList(0, 40);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse3.getEntity());

      createActivities(spaceIdentity, spaceIdentity, 10);

      Space space = spaceService.getSpaceByPrettyName("my_space_1");
      spaceService.addMember(space, "demo");

      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse4.getStatus());
      assertTrue(containerResponse4.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                          .loadAsList(0, 40);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse4.getEntity());
    }
  }

  /**
   * Test
   * {@link ActivityStreamResources#getActivityFeedByTimestampOfAuthenticated(javax.ws.rs.core.UriInfo, String, String, int, Long, Long, int, int)}
   * 
   * @throws Exception
   */
  public void testLimitGetActivityFeedByTimestampOfAuthenticatedWithJsonFormat() throws Exception {
    int limit = 10;
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    String resourceUrl = RESOURCE_URL + "feedByTimestamp.json?limit=" + limit;

    // user identity
    {
      startSessionAs("demo");

      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse1.getStatus());
      assertTrue(containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());

      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse2.getStatus());
      assertTrue(containerResponse2.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                                  .loadAsList(0, limit);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity());
    }

    // space identity
    {
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals(200, containerResponse3.getStatus());
      assertTrue(containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                                  .loadAsList(0, limit);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse3.getEntity());

      createActivities(spaceIdentity, spaceIdentity, 10);

      Space space = spaceService.getSpaceByPrettyName("my_space_1");

      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse4.getStatus());
      assertTrue(containerResponse4.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                          .loadAsList(0, limit);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse4.getEntity());
    }
  }

  /**
   * Test
   * {@link ActivityStreamResources#getActivityFeedByTimestampOfAuthenticated(javax.ws.rs.core.UriInfo, String, String, int, Long, Long, int, int)}
   * 
   * @throws Exception
   */
  public void testSinceTimeGetActivityFeedByTimestampOfAuthenticatedWithJsonFormat() throws Exception {
    // user identity
    {
      startSessionAs("demo");

      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);

      Long sinceTime = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                      .loadAsList(0, 20)
                                      .get(14)
                                      .getPostedTime();
      String resourceUrl = RESOURCE_URL + "feedByTimestamp.json?since_time=" + sinceTime;

      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return 200",
                   200,
                   containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                                  .loadNewer(sinceTime, 20);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      Long sinceTime = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                      .loadAsList(0, 20)
                                      .get(14)
                                      .getPostedTime();
      String resourceUrl = RESOURCE_URL + "feedByTimestamp.json?since_time=" + sinceTime;

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);

      assertEquals("containerResponse2.getStatus() must return 200",
                   200,
                   containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse2.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                                  .loadNewer(sinceTime, 20);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity());

      createActivities(spaceIdentity, spaceIdentity, 10);

      Space space = spaceService.getSpaceByPrettyName("my_space_1");

      createActivities(demoIdentity, spaceIdentity, 5);

      sinceTime = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                 .loadAsList(0, 40)
                                 .get(29)
                                 .getPostedTime();

      resourceUrl = RESOURCE_URL + "feedByTimestamp.json?since_time=" + sinceTime;

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse3.getStatus() must return 200",
                   200,
                   containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                          .loadNewer(sinceTime, 40);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse3.getEntity());
    }
  }

  /**
   * Test
   * {@link ActivityStreamResources#getActivityFeedByTimestampOfAuthenticated(javax.ws.rs.core.UriInfo, String, String, int, Long, Long, int, int)}
   * 
   * @throws Exception
   */
  public void testMaxTimeGetActivityFeedByTimestampOfAuthenticatedWithJsonFormat() throws Exception {
    // user identity
    {
      startSessionAs("demo");

      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);

      Long maxTime = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                    .loadAsList(0, 20)
                                    .get(0)
                                    .getPostedTime();

      String resourceUrl = RESOURCE_URL + "feedByTimestamp.json?max_time=" + maxTime;

      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return 200",
                   200,
                   containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                                  .loadOlder(maxTime, 20);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);
      createActivities(spaceIdentity, spaceIdentity, 10);
      Space space = spaceService.getSpaceByPrettyName("my_space_1");
      createActivities(demoIdentity, spaceIdentity, 5);

      Long maxTime = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                    .loadAsList(0, 40)
                                    .get(0)
                                    .getPostedTime();
      String resourceUrl = RESOURCE_URL + "feedByTimestamp.json?max_time=" + maxTime;

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200",
                   200,
                   containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse2.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                                  .loadOlder(maxTime, 40);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse2.getEntity());
    }
  }

  /**
   * Tests default get activity stream without any optional query parameters. -
   * Test with a user identity - Test with a space identity
   * 
   * @throws Exception
   */
  public void testDefaultGetActivityStreamByIdentityIdWithJsonFormatWithTimestamp() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    // user identity
    {
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + "ByTimestamp.json";
      // Johns gets activity stream of Demo which has 0 activities
      startSessionAs("john");
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse1.getStatus() must return: " + 200,
                   200,
                   containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());

      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);
      // John gets activity stream of Demo's which has 15 activities

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse2.getStatus());
      assertTrue(containerResponse2.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity)
                                                              .loadAsList(0, 20);
      compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + spaceIdentity.getId() + "ByTimestamp.json";
      testAccessResourceWithoutPermission("john", "GET", resourceUrl, null);

      // Demo gets activity stream of space
      startSessionAs("demo");

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals(200, containerResponse3.getStatus());
      assertTrue(containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse4.getStatus());
      assertTrue(containerResponse4.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesWithListAccess(spaceIdentity)
                                                               .loadAsList(0, 20);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
    }
  }

  /**
   * Tests get activity stream with short hand "me" param as the authenticated
   * user who makes the request.
   * 
   * @throws Exception
   */
  public void testMeParamGetActivityStreamByIdentityIdWithJsonFormatWithTimestamp() throws Exception {
    String resourceUrl = RESOURCE_URL + "meByTimestamp.json";
    startSessionAs("demo");

    // Demo creates 10 activities to his stream
    createActivities(demoIdentity, demoIdentity, 10);
    connectIdentities(demoIdentity, johnIdentity, true);
    // John creates 5 activities to Demo's stream
    createActivities(johnIdentity, demoIdentity, 5);
    // Demo gets activity stream of his which has 15 activities
    ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
    assertEquals(200, containerResponse2.getStatus());
    assertTrue(containerResponse2.getContentType()
                                 .toString()
                                 .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
    List<ExoSocialActivity> demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity)
                                                            .loadAsList(0, 20);
    compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
  }

  /**
   * Tests: get activity stream with "limit" query parameter.
   * 
   * @throws Exception
   */
  public void testLimitGetActivityStreamByIdentityIdWithJsonFormatWithTimestamp() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    int limit = 10;
    // user identity
    {
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + "ByTimestamp.json?limit=" + limit;
      // Johns gets activity stream of Demo which has 0 activities
      startSessionAs("john");
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse1.getStatus());
      assertTrue( containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());

      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);
      // John gets activity stream of Demo's which has 15 activities

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse2.getStatus());
      assertTrue(containerResponse2.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity)
                                                              .loadAsList(0, limit);
      compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + spaceIdentity.getId() + "ByTimestamp.json?limit=" + limit;
      testAccessResourceWithoutPermission("john", "GET", resourceUrl, null);

      // Demo gets activity stream of space
      startSessionAs("demo");

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals(200, containerResponse3.getStatus());
      assertTrue(containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse4.getStatus());
      assertTrue(containerResponse4.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesWithListAccess(spaceIdentity)
                                                               .loadAsList(0, limit);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
    }

  }

  /**
   * 
   * @throws Exception
   */
  public void testSinceTimeGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    // user identity
    {
      startSessionAs("demo");
      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      Long sinceTime = activityManager.getActivitiesWithListAccess(demoIdentity)
                                      .loadAsList(0, 20)
                                      .get(0)
                                      .getPostedTime();
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + "ByTimestamp.json?since_time="
          + sinceTime;
      // John creates 5 activities to Demo's stream
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);
      // John gets activity stream of Demo's which has 15 activities

      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse1.getStatus());
      assertTrue(containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> newerActivities = activityManager.getActivitiesWithListAccess(demoIdentity)
                                                               .loadNewer(sinceTime, 10);

      compareActivities(newerActivities, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      // Demo gets activity stream of space
      startSessionAs("demo");

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      Long sinceTime = activityManager.getActivitiesWithListAccess(spaceIdentity)
                                      .loadAsList(0, 1)
                                      .get(0)
                                      .getPostedTime();
      String resourceUrl = RESOURCE_URL + spaceIdentity.getId() + "ByTimestamp.json?since_time="
          + sinceTime;
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      List<ExoSocialActivity> newerActivities = activityManager.getActivitiesWithListAccess(spaceIdentity)
                                                               .loadNewer(sinceTime, 10);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse2.getStatus());
      assertTrue(containerResponse2.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(newerActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }
  }

  public void testMaxTimeGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    // user identity
    {
      startSessionAs("demo");
      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      // John creates 5 activities to Demo's stream
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);
      Long maxTime = activityManager.getActivitiesWithListAccess(demoIdentity)
                                    .loadAsList(0, 5)
                                    .get(4)
                                    .getPostedTime();
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + "ByTimestamp.json?max_time="
          + maxTime;
      // John gets activity stream of Demo's which has 15 activities

      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals(200,
                   containerResponse1.getStatus());
      assertTrue(containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> olderActivities = activityManager.getActivitiesWithListAccess(demoIdentity)
                                                               .loadOlder(maxTime, 20);

      compareActivities(olderActivities, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      // Demo gets activity stream of space
      startSessionAs("demo");

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);
      Long maxTime = activityManager.getActivitiesWithListAccess(spaceIdentity)
                                    .loadAsList(0, 5)
                                    .get(4)
                                    .getPostedTime();
      String resourceUrl = RESOURCE_URL + spaceIdentity.getId() + "ByTimestamp.json?max_time="
          + maxTime;

      List<ExoSocialActivity> olderActivities = activityManager.getActivitiesWithListAccess(spaceIdentity)
                                                               .loadOlder(maxTime, 20);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse2.getStatus());
      assertTrue(containerResponse2.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));

      compareActivities(olderActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }
  }

  /**
   * Tests default get activity stream of connection without any optional query
   * parameters.
   * 
   * @throws Exception
   */
  public void testDefaultGetActivityConnectionsOfAuthenticatedWithJsonFormatWithTimestamp() throws Exception {
    // user identity
    {
      String resourceUrl = RESOURCE_URL + "connectionsByTimestamp.json";

      // John gets activity stream of Demo which has 0 activities
      startSessionAs("demo");

      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);

      // Make connection between demo and john
      connectIdentities(demoIdentity, johnIdentity, true);

      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);

      // John gets activity stream of Demo's which has 15 activities
      ContainerResponse rsp1 = service("GET", resourceUrl, "", null, null);
      assertEquals(Response.Status.OK.getStatusCode(), rsp1.getStatus());
      assertTrue(rsp1.getContentType()
                     .toString()
                     .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));

      List<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity)
                                                              .loadAsList(0, 20);
      compareActivities(demoActivities, (ActivityRestListOut) rsp1.getEntity());

      endSession();
    }

  }

  /**
   * Tests: get activity stream of connection with limit parameter.
   * 
   * @throws Exception
   */
  public void testLimitGetActivityConnectionsOfAuthenticatedWithJsonFormatWithTimestamp() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    int limit = 10;

    String resourceUrl = RESOURCE_URL + "connectionsByTimestamp.json?limit=" + limit;

    // Johns gets activity stream of Demo which has 0 activities
    startSessionAs("demo");
    ContainerResponse rsp = service("GET", resourceUrl, "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), rsp.getStatus());
    assertTrue(rsp.getContentType()
                  .toString()
                  .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
    compareActivities(emptyList, (ActivityRestListOut) rsp.getEntity());

    // Demo creates 10 activities to his stream
    createActivities(demoIdentity, demoIdentity, 10);

    // Make connection between demo and john
    connectIdentities(demoIdentity, johnIdentity, true);

    // John creates 5 activities to Demo's stream
    createActivities(johnIdentity, demoIdentity, 5);

    // John gets activity stream of Demo's which has 15 activities
    ContainerResponse rsp1 = service("GET", resourceUrl, "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(),
                 rsp1.getStatus());
    assertTrue(rsp1.getContentType()
                   .toString()
                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));

    List<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity)
                                                            .loadAsList(0, limit);
    compareActivities(demoActivities, (ActivityRestListOut) rsp1.getEntity());

    limit = 3;
    resourceUrl = RESOURCE_URL + "connectionsByTimestamp.json?limit=" + limit;
    rsp1 = service("GET", resourceUrl, "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(),
                 rsp1.getStatus());
    assertTrue(rsp1.getContentType()
                   .toString()
                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
    demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity)
                                    .loadAsList(0, limit);
    compareActivities(demoActivities, (ActivityRestListOut) rsp1.getEntity());

    endSession();
  }

  /**
   * Tests get activity of connection with sinceTime parameter.
   * 
   * @throws Exception
   */
  public void testSinceTimeGetActivityConnectionsOfAuthenticatedWithJsonFormat() throws Exception {

    startSessionAs("demo");

    // Make connection between demo and john
    connectIdentities(demoIdentity, johnIdentity, true);

    // Make connection between demo and mary
    connectIdentities(demoIdentity, maryIdentity, true);

    // Demo creates 10 activities to his stream
    createActivities(demoIdentity, demoIdentity, 10);

    // John creates 10 activities to Demo's stream
    createActivities(johnIdentity, demoIdentity, 10);

    // Mary creates 10 activities to Demo's stream
    createActivities(maryIdentity, demoIdentity, 10);

    Long sinceTime = activityManager.getActivitiesOfConnectionsWithListAccess(johnIdentity)
                                    .loadAsList(0, 20)
                                    .get(0)
                                    .getPostedTime();
    String resourceUrl = RESOURCE_URL + "connectionsByTimestamp.json?since_time=" + sinceTime;

    // Demo gets activities in of his connections base on the first connection's
    // activity => 20
    ContainerResponse rsp = service("GET", resourceUrl, "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), rsp.getStatus());
    assertTrue( rsp.getContentType()
                  .toString()
                  .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));

    List<ExoSocialActivity> newerActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity)
                                                             .loadNewer(sinceTime, 20);

    compareActivities(newerActivities, (ActivityRestListOut) rsp.getEntity());

    endSession();
  }

  /**
   * Tests get activity of connection with maxTime parameter.
   * 
   * @throws Exception
   */
  public void testMaxTimeGetActivityConnectionsOfAuthenticatedWithJsonFormat() throws Exception {

    startSessionAs("demo");

    // Demo creates 10 activities to his stream
    createActivities(demoIdentity, demoIdentity, 10);

    // Make connection between demo and john
    connectIdentities(demoIdentity, johnIdentity, true);

    // John creates 5 activities to Demo's stream
    createActivities(johnIdentity, demoIdentity, 5);

    Long maxTime = activityManager.getActivitiesOfConnectionsWithListAccess(johnIdentity)
                                  .loadAsList(0, 5)
                                  .get(4)
                                  .getPostedTime();
    String resourceUrl = RESOURCE_URL + "connectionsByTimestamp.json?max_time=" + maxTime;

    ContainerResponse rsp = service("GET", resourceUrl, "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(),
                 rsp.getStatus());
    assertTrue(rsp.getContentType()
                  .toString()
                  .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
    List<ExoSocialActivity> olderActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity)
                                                             .loadOlder(maxTime, 20);

    compareActivities(olderActivities, (ActivityRestListOut) rsp.getEntity());

    endSession();
  }

  /**
   * Tests default get activity stream without any optional query parameters. -
   * Test with a user identity - Test with a space identity
   * 
   * 
   * @throws Exception
   */
  public void testDefaultGetActivitySpacesOfAuthenticatedWithJsonFormatWithTimestamp() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    // user identity
    {
      String resourceUrl = RESOURCE_URL + "spacesByTimestamp.json";
      // Johns gets activity stream of Demo which has 0 activities
      startSessionAs("john");
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse1.getStatus());
      assertTrue(containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + "spacesByTimestamp.json";

      // Demo gets activity stream of space
      startSessionAs("demo");

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals(200, containerResponse3.getStatus());
      assertTrue(containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse4.getStatus());
      assertTrue(containerResponse4.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                                               .loadAsList(0, 20);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
    }
    endSession();
  }

  /**
   * Tests: get activity stream with "limit" query parameter.
   * 
   * @throws Exception
   */
  public void testLimitGetActivitySpacesOfAuthenticatedWithJsonFormatWithTimestamp() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    int limit = 10;
    // user identity
    {
      String resourceUrl = RESOURCE_URL + "spacesByTimestamp.json?limit=" + limit;
      // Johns gets activity stream of Demo which has 0 activities
      startSessionAs("john");
      ContainerResponse containerResponse1 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse1.getStatus());
      assertTrue(containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());
    }

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      // John tries to gets space's activity stream => 403
      String resourceUrl = RESOURCE_URL + "spacesByTimestamp.json?limit=" + limit;

      // Demo gets activity stream of space
      startSessionAs("demo");

      ContainerResponse containerResponse3 = service("GET", resourceUrl, "", null, null);

      assertEquals(200, containerResponse3.getStatus());
      assertTrue(containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse4.getStatus());
      assertTrue(containerResponse4.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                                               .loadAsList(0, limit);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
    }
    endSession();

  }

  /**
   * 
   * @throws Exception
   */
  public void testSinceTimeGetActivitySpacesOfAuthenticatedWithJsonFormat() throws Exception {
    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      // Demo gets activity stream of space
      startSessionAs("demo");

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      Long sinceTime = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                      .loadAsList(0, 1)
                                      .get(0)
                                      .getPostedTime();
      String resourceUrl = RESOURCE_URL + "spacesByTimestamp.json?since_time=" + sinceTime;
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      List<ExoSocialActivity> newerActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                                               .loadNewer(sinceTime, 10);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse2.getStatus());
      assertTrue(containerResponse2.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(newerActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }
    endSession();
  }

  public void testMaxTimeGetActivitySpacesOfAuthenticatedWithJsonFormat() throws Exception {

    // space identity
    {
      // Creates a space with 0 activities
      createSpaces(1);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   "my_space_1",
                                                                   false);

      // Demo gets activity stream of space
      startSessionAs("demo");

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);
      Long maxTime = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                    .loadAsList(0, 5)
                                    .get(4)
                                    .getPostedTime();
      String resourceUrl = RESOURCE_URL + "spacesByTimestamp.json?max_time=" + maxTime;

      List<ExoSocialActivity> olderActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                                               .loadOlder(maxTime, 20);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals(200, containerResponse2.getStatus());
      assertTrue(containerResponse2.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));

      compareActivities(olderActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }
    endSession();
  }
  
  /**
   * An identity posts an activity to an identity's activity stream with a number of activities.
   *
   * @param posterIdentity the identity who posts activity
   * @param identityStream the identity who has activity stream to be posted.
   * @param number the number of activities
   */
  private void createActivities(Identity posterIdentity, Identity identityStream, int number) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activity.setUserId(posterIdentity.getId());
      activityManager.saveActivityNoReturn(identityStream, activity);
      tearDownActivityList.add(activity);
    }
  }

  /**
   * Creates a comment to an existing activity.
   *
   * @param existingActivity the existing activity
   * @param posterIdentity the identity who comments
   * @param number the number of comments
   */
  private void createComment(ExoSocialActivity existingActivity, Identity posterIdentity, int number) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment " + i);
      comment.setUserId(posterIdentity.getId());
      activityManager.saveComment(existingActivity, comment);
    }
  }

  /**
   * Gets an instance of the space.
   *
   * @param number the number to be created
   */
  private void createSpaces(int number) {
    for (int i = 0; i < number; i++) {
      Space space = new Space();
      space.setDisplayName("my space " + number);
      space.setPrettyName(space.getDisplayName());
      space.setRegistration(Space.OPEN);
      space.setDescription("add new space " + number);
      space.setType(DefaultSpaceApplicationHandler.NAME);
      space.setVisibility(Space.PUBLIC);
      space.setRegistration(Space.VALIDATION);
      space.setPriority(Space.INTERMEDIATE_PRIORITY);
      space.setGroupId("/spaces/my_space_" + number);
      String[] managers = new String[]{"demo"};
      String[] members = new String[]{"mary", "demo"};
      String[] invitedUsers = new String[]{"john"};
      String[] pendingUsers = new String[]{};
      space.setInvitedUsers(invitedUsers);
      space.setPendingUsers(pendingUsers);
      space.setManagers(managers);
      space.setMembers(members);
      space.setUrl("/space/" + space.getPrettyName());
      try {
        spaceService.saveSpace(space, true);
        tearDownSpaceList.add(space);
      } catch (SpaceException e) {
        fail("Could not create a new space");
      }
    }
  }

  /**
   * Connects 2 identities, if toConfirm = true, they're connected. If false, in pending connection type.
   *
   * @param senderIdentity the identity who sends connection request
   * @param receiverIdentity the identity who receives connection request
   * @param beConfirmed boolean value
   */
  private void connectIdentities(Identity senderIdentity, Identity receiverIdentity, boolean beConfirmed) {
    relationshipManager.inviteToConnect(senderIdentity, receiverIdentity);
    if (beConfirmed) {
      relationshipManager.confirm(receiverIdentity, senderIdentity);
    }

    tearDownRelationshipList.add(relationshipManager.get(senderIdentity, receiverIdentity));
  }

}
