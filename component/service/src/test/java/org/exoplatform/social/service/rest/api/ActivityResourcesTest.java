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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.exoplatform.social.service.rest.api.models.ActivityStream;
import org.exoplatform.social.service.rest.api.models.Comment;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.api.models.Activity;
import org.exoplatform.social.service.test.AbstractResourceTest;
import org.json.JSONWriter;

/**
 * Unit Test for {@link ActivityResources}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 16, 2011
 */
public class ActivityResourcesTest extends AbstractResourceTest {

  private final String RESOURCE_URL = "/api/social/v1-alpha1/portal/activity";

  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;

  private Identity rootIdentity, johnIdentity, maryIdentity, demoIdentity;

  private List<Identity> tearDownIdentityList;
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Relationship> tearDownRelationshipList;
  private List<Space> tearDownSpaceList;

  /**
   * Adds {@link ActivityResources}.
   *
   * @throws Exception
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();

    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);

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
    tearDownRelationshipList = new ArrayList<Relationship>();
    tearDownSpaceList = new ArrayList<Space>();


    addResource(ActivityResources.class, null);
  }

  /**
   * Removes {@link ActivityResources}.
   *
   * @throws Exception
   */
  @Override
  public void tearDown() throws Exception {

    //Removing the relationships
    for (Relationship relationship: tearDownRelationshipList) {
      relationshipManager.delete(relationship);
    }
    
    //Removing the activitys
    for (ExoSocialActivity activity: tearDownActivityList) {
      activityManager.deleteActivity(activity);
    }

    //Removing the spaces
    for (Space space: tearDownSpaceList) {
      spaceService.deleteSpace(space);
    }
    for (Identity identity: tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
    }
    removeResource(ActivityResources.class);

    super.tearDown();
  }

  /**
   * Tests access permission to get an activity.
   *
   * @throws Exception
   */
  public void testGetActivityByIdForAccess() throws Exception {
    String resourceUrl = RESOURCE_URL+"/1a2b3c4d5e.json";
    // unauthorized
    testAccessResourceAsAnonymous("GET", resourceUrl, null);
    //not found
    testAccessNotFoundResourceWithAuthentication("demo", "GET", resourceUrl, null);
    // TODO : forbidden
  }


  /**
   * Tests
   * {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String, String)}
   * with json format.
   */
  public void testGetActivityByIdWithJsonFormat() throws Exception {
    createActivities(demoIdentity, demoIdentity, 1);
    ExoSocialActivity demoActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    String resourceUrl = RESOURCE_URL+"/" + demoActivity.getId() + ".json";
    { // get activity by id without any query param
      startSessionAs("demo");
      ContainerResponse containerResponse = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return " + 200, 200, containerResponse.getStatus());
      assertEquals(MediaType.APPLICATION_JSON_TYPE, containerResponse.getContentType());
      Activity entity = (Activity) containerResponse.getEntity();
      assertNotNull("entity must not be null", entity);
      assertNotNull("entity.getId() must not be null", entity.getId());
      assertEquals("entity.getTitle() must return: " + demoActivity.getTitle(),
                    demoActivity.getTitle(), entity.getTitle());
      assertEquals("entity.getAppId() must return: " + demoActivity.getAppId(),
              demoActivity.getAppId(), entity.getAppId());
      assertEquals("entity.getType() must return: " + demoActivity.getType(),
                    demoActivity.getType(), entity.getType());
      assertEquals("entity.getPriority() must return: " + demoActivity.getPriority(),
                    demoActivity.getPriority(), entity.getPriority());
      assertEquals("entity.getTemplateParams() must return: " + demoActivity.getTemplateParams(),
                    demoActivity.getTemplateParams(),
                    entity.getTemplateParams());
      assertEquals("entity.getTitleId() must return: " + demoActivity.getTitleId(),
                    demoActivity.getTitleId(),
                    entity.getTitleId());
      assertEquals("entity.getIdentityId() must return: " + demoActivity.getUserId(),
                    demoActivity.getUserId(),
                    entity.getIdentityId());
      assertEquals("entity.isLiked() must be false", false, entity.isLiked());
      assertNull("entity.getLikedByIdentities() must be null", entity.getLikedByIdentities());
      assertNull("entity.getPosterIdentity() must be null", entity.getPosterIdentity());
      assertNull("entity.getComments() must be null", entity.getComments());
      assertEquals("entity.getNumberOfComments() must return: " + 0, 0, entity.getNumberOfComments());
      assertNull("entity.getActivityStream() must be null", entity.getActivityStream());
    }

    {//gets activity by specifying posterIdentity
      String posterIdentityResourceUrl = resourceUrl + "?poster_identity=true";
      startSessionAs("john");
      ContainerResponse containerResponse = service("GET", posterIdentityResourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
      Activity entity = (Activity) containerResponse.getEntity();
      assertNotNull("entity must not be null", entity);
      /*
      org.exoplatform.social.service.rest.api.models.Identity posterIdentity = entity.getPosterIdentity();
      assertNotNull("posterIdentity must not be null", posterIdentity);
      assertEquals("posterIdentity.getProviderId() must return: " + OrganizationIdentityProvider.NAME,
                   OrganizationIdentityProvider.NAME,
                   posterIdentity.getProviderId());
      assertEquals("posterIdentity.getRemoteId() must return: demo", "demo", posterIdentity.getRemoteId());
      */
    }

    {//gets activity by specifying numberOfComments
      //creating the comments for unit testing.
      startSessionAs("john");
      createComment(demoActivity, johnIdentity, 30);
      String numberOfCommentsResourceUrl = resourceUrl + "?number_of_comments=20";
      ContainerResponse containerResponse = service("GET", numberOfCommentsResourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
      Activity entity = (Activity) containerResponse.getEntity();
      assertNotNull("entity must not be null", entity);
      assertEquals("numberOfComments must be equals 30", 30, entity.getNumberOfComments());
      Comment[] gotComments = entity.getComments();
      assertEquals("content of comments must be equals 20", 20, gotComments.length);
      
    }

    {//gets activity by specifying activityStream
      String posterIdentityResourceUrl = resourceUrl + "?activity_stream=1";
      startSessionAs("john");
      ContainerResponse containerResponse = service("GET", posterIdentityResourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
      Activity entity = (Activity) containerResponse.getEntity();
      assertNotNull("entity must not be null", entity);
      ActivityStream activityStream = entity.getActivityStream();
      assertNotNull("activityStream must not be null", activityStream);
      assertEquals("activityStream.getType() must return: " + demoActivity.getActivityStream().getType(),
                    demoActivity.getActivityStream().getType(),
                    activityStream.getType());
      assertEquals("activityStream.getPrettyId() must return: " + demoActivity.getActivityStream().getPrettyId(),
                   demoActivity.getActivityStream().getPrettyId(),
                   activityStream.getPrettyId());
    }

    {//Tests with full optional params
      startSessionAs("john");
      String allOfOptionalParamsResourceUrl = resourceUrl + "?poster_identity=true&number_of_comments=20&activity_stream=1";
      ContainerResponse containerResponse = service("GET", allOfOptionalParamsResourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
      Activity entity = (Activity) containerResponse.getEntity();
      assertNotNull("entity must not be null", entity);
      //Assert the activity's fields.
      assertNotNull("entity.getId() must not be null", entity.getId());
      assertEquals("entity.getTitle() must return: " + demoActivity.getTitle(),
                    demoActivity.getTitle(), entity.getTitle());
      assertEquals("entity.getAppId() must return: " + demoActivity.getAppId(),
                   demoActivity.getAppId(), entity.getAppId());
      assertEquals("entity.getType() must return: " + demoActivity.getType(),
                         demoActivity.getType(), entity.getType());
      //assert the comments
      assertEquals("numberOfComments must be equals 30", 30, entity.getNumberOfComments());
      Comment[] commentArray = entity.getComments();
      assertEquals("content of comments must be equals 20", 20, commentArray.length);
      
      //assert the activityStream
      ActivityStream activityStream = entity.getActivityStream();
      assertNotNull("activityStream must not be null", activityStream);
      assertEquals("activityStream.getType() must return: " + demoActivity.getActivityStream().getType(),
                    demoActivity.getActivityStream().getType(),
                    activityStream.getType());
      assertEquals("activityStream.getPrettyId() mu" +
          "st return: " + demoActivity.getActivityStream().getPrettyId(),
                   demoActivity.getActivityStream().getPrettyId(),
                   activityStream.getPrettyId());
    }
  }


  /**
   * Tests
   * {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,String)}
   * with json format.
   */
  public void testGetActivityByIdWithComment1WithJsonFormat() throws Exception {
    startSessionAs("demo");
    createActivities(demoIdentity, rootIdentity, 1);
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(rootIdentity).load(0, 1)[0];
    createComment(expectedActivity, rootIdentity, 1);
    ContainerResponse response =
        service("GET", RESOURCE_URL+"/" + expectedActivity.getId() + ".json?number_of_comments=1", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(expectedActivity.getId(), got.getId());
    assertEquals(expectedActivity.getTitle(), got.getTitle());
    assertEquals(expectedActivity.getPriority(), got.getPriority());
    assertEquals(expectedActivity.getAppId(), got.getAppId());
    assertEquals(expectedActivity.getType(), got.getType());
    assertEquals(expectedActivity.getTitleId(), got.getTitleId());
    assertEquals(expectedActivity.getTemplateParams().size(), got.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(1, got.getComments().length);
    assertEquals(demoIdentity.getId(), got.getIdentityId());
    assertEquals(null, got.getPosterIdentity());
    assertEquals(null, got.getActivityStream());
  }

  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithComment10WithJsonFormat() throws Exception {
    startSessionAs("demo");
    createActivities(demoIdentity, rootIdentity, 1);
    
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(rootIdentity).load(0, 1)[0];
    createComment(expectedActivity, rootIdentity, 2);
    ContainerResponse response =
        service("GET", RESOURCE_URL+"/" + expectedActivity.getId() + ".json?number_of_comments=10", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) { 
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(expectedActivity.getId(), got.getId());
    assertEquals(expectedActivity.getTitle(), got.getTitle());
    assertEquals(expectedActivity.getPriority(), got.getPriority());
    assertEquals(expectedActivity.getAppId(), got.getAppId());
    assertEquals(expectedActivity.getType(), got.getType());
    assertEquals(expectedActivity.getTitleId(), got.getTitleId());
    assertEquals(expectedActivity.getTemplateParams().size(), got.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(2, got.getComments().length);
    assertEquals(demoIdentity.getId(), got.getIdentityId());
    assertEquals(null, got.getPosterIdentity());
    assertEquals(null, got.getActivityStream());
  }

  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithPosterIdTrueWithJsonFormat() throws Exception {
    startSessionAs("demo");
    createActivities(demoIdentity, johnIdentity, 1);
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(johnIdentity).load(0, 1)[0];
    
    ContainerResponse response =
        service("GET", RESOURCE_URL+"/" + expectedActivity.getId() + ".json?poster_identity=true", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(expectedActivity.getId(), got.getId());
    assertEquals(expectedActivity.getTitle(), got.getTitle());
    assertEquals(expectedActivity.getPriority(), got.getPriority());
    assertEquals(expectedActivity.getAppId(), got.getAppId());
    assertEquals(expectedActivity.getType(), got.getType());
    assertEquals(expectedActivity.getTitleId(), got.getTitleId());
    assertEquals(expectedActivity.getTemplateParams().size(), got.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(null, got.getComments());
    assertEquals(demoIdentity.getId(), got.getIdentityId());
    assertEquals(demoIdentity.getId(), got.getPosterIdentity().getId());
    assertEquals(null, got.getActivityStream());
  }

  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithPosterIdTWithJsonFormat() throws Exception {
    startSessionAs("demo");
    createActivities(demoIdentity, rootIdentity, 1);
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(rootIdentity).load(0, 1)[0];
    
    ContainerResponse response =
        service("GET", RESOURCE_URL+"/" + expectedActivity.getId() + ".json?poster_identity=t", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(expectedActivity.getId(), got.getId());
    assertEquals(expectedActivity.getTitle(), got.getTitle());
    assertEquals(expectedActivity.getPriority(), got.getPriority());
    assertEquals(expectedActivity.getAppId(), got.getAppId());
    assertEquals(expectedActivity.getType(), got.getType());
    assertEquals(expectedActivity.getTitleId(), got.getTitleId());
    assertEquals(expectedActivity.getTemplateParams().size(), got.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(null, got.getComments());
    assertEquals(demoIdentity.getId(), got.getIdentityId());
    assertEquals(demoIdentity.getId(), got.getPosterIdentity().getId());
    assertEquals(null, got.getActivityStream());
  }

  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithPosterId1WithJsonFormat() throws Exception {
    startSessionAs("john");
    connectIdentities(demoIdentity, johnIdentity, true);
    createActivities(johnIdentity, demoIdentity, 1);
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    
    ContainerResponse response =
        service("GET", RESOURCE_URL+"/" + expectedActivity.getId() + ".json?poster_identity=1", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(expectedActivity.getId(), got.getId());
    assertEquals(expectedActivity.getTitle(), got.getTitle());
    assertEquals(expectedActivity.getPriority(), got.getPriority());
    assertEquals(expectedActivity.getAppId(), got.getAppId());
    assertEquals(expectedActivity.getType(), got.getType());
    assertEquals(expectedActivity.getTitleId(), got.getTitleId());
    assertEquals(expectedActivity.getTemplateParams().size(), got.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(null, got.getComments());
    assertEquals(johnIdentity.getId(), got.getIdentityId());
    assertEquals(johnIdentity.getId(), got.getPosterIdentity().getId());
    assertEquals(null, got.getActivityStream());
  }

  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithStreamWithJsonFormat() throws Exception {
    startSessionAs("demo");
    createActivities(demoIdentity, rootIdentity, 1);
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(rootIdentity).load(0, 1)[0];
    
    ContainerResponse response =
        service("GET", RESOURCE_URL+"/" + expectedActivity.getId() + ".json?activity_stream=true", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(expectedActivity.getId(), got.getId());
    assertEquals(expectedActivity.getTitle(), got.getTitle());
    assertEquals(expectedActivity.getPriority(), got.getPriority());
    assertEquals(expectedActivity.getAppId(), got.getAppId());
    assertEquals(expectedActivity.getType(), got.getType());
    assertEquals(expectedActivity.getTitleId(), got.getTitleId());
    assertEquals(expectedActivity.getTemplateParams().size(), got.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(null, got.getComments());
    assertEquals(demoIdentity.getId(), got.getIdentityId());
    assertEquals(null, got.getPosterIdentity());
    assertEquals(expectedActivity.getStreamTitle(), got.getActivityStream().getTitle());
    assertEquals(expectedActivity.getStreamOwner(), got.getActivityStream().getPrettyId());
    assertEquals(expectedActivity.getStreamFaviconUrl(), got.getActivityStream().getFaviconUrl());
    assertEquals(expectedActivity.getStreamUrl(), got.getActivityStream().getPermalink());
  }

  /**
   * Tests access permission to create a new activity.
   *
   * @throws Exception
   */
  public void testCreateNewActivityForAccess() throws Exception {
    //final byte[] data = "{\"title\" : \"hello world!\"}".getBytes();
    //unauthorized
    //testAccessResourceAsAnonymous("POST", RESOURCE_URL + ".json", data);
    //forbidden

    // TODO : unauthorized
    // TODO : forbidden

  }

  /**
   * Tests {@link ActivityResources#deleteExistingActivityById(javax.ws.rs.core.UriInfo, String, String, String)}
   * with json format.
   */
  public void testDeleteDELETEActivityWithJsonFormat() throws Exception {
    startSessionAs("demo");
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title");
    activity.setAppId("appId");
    activity.setType("type");
    activity.setPriority(0.5F);
    activity.setTitleId("title id");
    activity.setUserId(demoIdentity.getId());

    Map<String, String> params = new HashMap<String, String>();
    params.put("key1", "value1");
    params.put("key2", "value2");
    activity.setTemplateParams(params);
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    ExoSocialActivity got1 = activityManager.getActivity(activity.getId());
    assertNotNull(got1);

    ContainerResponse response = service("DELETE", RESOURCE_URL+"/" + activity.getId() + ".json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    try {
      ExoSocialActivity got2 = activityManager.getActivity(activity.getId());
      fail();
    }
    catch (Exception e) {
      // ok
    }

  }

  /**
   * Tests {@link ActivityResources#deleteExistingActivityById(javax.ws.rs.core.UriInfo, String, String, String)}
   * with json format.
   */
  public void testDeletePOSTActivityWithJsonFormat() throws Exception {

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title");
    activity.setAppId("appId");
    activity.setType("type");
    activity.setPriority(0.5F);
    activity.setTitleId("title id");
    activity.setUserId(johnIdentity.getId());

    Map<String, String> params = new HashMap<String, String>();
    params.put("key1", "value1");
    params.put("key2", "value2");
    activity.setTemplateParams(params);
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    ExoSocialActivity got1 = activityManager.getActivity(activity.getId());
    assertNotNull(got1);
    
    startSessionAs("john");
    connectIdentities(johnIdentity, rootIdentity, true);
     
    ContainerResponse response = service("POST", RESOURCE_URL+"/destroy/" + activity.getId() + ".json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    try {
      ExoSocialActivity got2 = activityManager.getActivity(activity.getId());
      fail();
    }
    catch (Exception e) {
      // ok
    }

  }

  /**
   * Tests {@link ActivityResources#createNewActivity(javax.ws.rs.core.UriInfo, String, String, String,
   * org.exoplatform.social.service.rest.api.models.Activity)}
   * with json format.
   */
  public void testCreateNewActivityWithJsonFormat() throws Exception {
    startSessionAs("john");
    String title = "hello !";
    StringWriter writer = new StringWriter();
    JSONWriter jsonWriter = new JSONWriter(writer);
    jsonWriter
        .object()
        .key("title")
        .value(title)
        .endObject();
    byte[] data = writer.getBuffer().toString().getBytes("UTF-8");

    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "" + data.length);

    //
    ContainerResponse response =
        service("POST", RESOURCE_URL + ".json?identity_id=" + johnIdentity.getId(), "", h, data);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    //
    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }
    Activity got = (Activity) response.getEntity();

    assertNotNull(got.getId());
    assertEquals(title, got.getTitle());

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
      activity.setType("exosocial:core");
      activity.setTitle("title " + i);
      activity.setUserId(posterIdentity.getId());
      activity = activityManager.saveActivity(identityStream, activity);
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
      comment = activityManager.getComments(existingActivity).get(0);
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
      space.setGroupId("/space/space" + number);
      String[] managers = new String[]{"demo", "tom"};
      String[] members = new String[]{"raul", "ghost", "dragon", "demo", "mary"};
      String[] invitedUsers = new String[]{"register1", "john"};
      String[] pendingUsers = new String[]{"jame", "paul", "hacker"};
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
   * @param receiverIdentity the identity who receives connnection request
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