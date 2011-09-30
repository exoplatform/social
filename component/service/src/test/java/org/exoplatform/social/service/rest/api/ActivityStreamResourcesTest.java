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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

/**
 * Unit Test for {@link ActivityStreamResources}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since  Sep 26, 2011
 * @since 1.2.3
 */
public class ActivityStreamResourcesTest extends AbstractResourceTest {

  private final String RESOURCE_URL = "/api/social/v1-alpha2/portal/activity_stream/";

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
    testStatusCodeOfResource("demo", "GET", "/api/social/v1-alpha2/wrongPortalContainerName/activity_stream/123456.json",
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
      assertEquals("containerResponse1.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse1.getContentType());
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());

      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);
      // John gets activity stream of Demo's which has 15 activities

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertEquals("containerResponse2.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
              MediaType.APPLICATION_JSON_TYPE, containerResponse2.getContentType());
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
      assertEquals("containerResponse3.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse3.getContentType());
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertEquals("containerResponse4.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse4.getContentType());
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesWithListAccess(spaceIdentity).
                                                                loadAsList(0, 20);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
    }
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
      assertEquals("containerResponse1.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse1.getContentType());
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());

      // Demo creates 10 activities to his stream
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      // John creates 5 activities to Demo's stream
      createActivities(johnIdentity, demoIdentity, 5);
      // John gets activity stream of Demo's which has 15 activities

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return 200", 200, containerResponse2.getStatus());
      assertEquals("containerResponse.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
              MediaType.APPLICATION_JSON_TYPE, containerResponse2.getContentType());
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
      assertEquals("containerResponse3.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse3.getContentType());
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200", 200, containerResponse4.getStatus());
      assertEquals("containerResponse4.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse4.getContentType());
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
      assertEquals("containerResponse1.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse1.getContentType());
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
      assertEquals("containerResponse2.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse2.getContentType());
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
      assertEquals("containerResponse1.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse1.getContentType());
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
      assertEquals("containerResponse2.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse2.getContentType());

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
      assertEquals("containerResponse2.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
              MediaType.APPLICATION_JSON_TYPE, containerResponse2.getContentType());
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
      assertEquals("containerResponse3.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse3.getContentType());
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
      assertEquals("containerResponse4.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
                   MediaType.APPLICATION_JSON_TYPE, containerResponse4.getContentType());
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
      assertEquals("containerResponse2.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
              MediaType.APPLICATION_JSON_TYPE, containerResponse2.getContentType());
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
      assertEquals("containerResponse2.getContentType() must return: " + MediaType.APPLICATION_JSON_TYPE,
              MediaType.APPLICATION_JSON_TYPE, containerResponse2.getContentType());
      compareActivities(demoActivities, (ActivityRestListOut) containerResponse2.getEntity());
      compareNumberOfComments(demoActivities, (ActivityRestListOut) containerResponse2.getEntity(), numberOfComments);
      compareNumberOfLikes(demoActivities, (ActivityRestListOut) containerResponse2.getEntity(), numberOfLikes);
    }
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
