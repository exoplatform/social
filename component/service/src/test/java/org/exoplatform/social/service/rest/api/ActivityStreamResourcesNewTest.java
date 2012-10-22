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

public class ActivityStreamResourcesNewTest extends AbstractResourceTest {

  private final String            RESOURCE_URL = "/api/social/v1-alpha3/portal/activity_stream/";

  private IdentityManager         identityManager;

  private ActivityManager         activityManager;

  private SpaceService            spaceService;

  private RelationshipManager     relationshipManager;

  private Identity                rootIdentity, johnIdentity, maryIdentity, demoIdentity;

  private List<Identity>          tearDownIdentityList;

  private List<ExoSocialActivity> tearDownActivityList;

  private List<Space>             tearDownSpaceList;

  private List<Relationship>      tearDownRelationshipList;

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
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                       "root",
                                                       false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                       "john",
                                                       false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                       "mary",
                                                       false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                       "demo",
                                                       false);

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
   * An identity posts an activity to an identity's activity stream with a
   * number of activities.
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
   * Connects 2 identities, if toConfirm = true, they're connected. If false, in
   * pending connection type.
   * 
   * @param senderIdentity the identity who sends connection request
   * @param receiverIdentity the identity who receives connection request
   * @param beConfirmed boolean value
   */
  private void connectIdentities(Identity senderIdentity,
                                 Identity receiverIdentity,
                                 boolean beConfirmed) {
    relationshipManager.inviteToConnect(senderIdentity, receiverIdentity);
    if (beConfirmed) {
      relationshipManager.confirm(receiverIdentity, senderIdentity);
    }

    tearDownRelationshipList.add(relationshipManager.get(senderIdentity, receiverIdentity));
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
      String[] managers = new String[] { "demo" };
      String[] members = new String[] { "mary", "demo" };
      String[] invitedUsers = new String[] { "john" };
      String[] pendingUsers = new String[] {};
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
      assertEquals("containerResponse1.getStatus() must return: " + 200,
                   200,
                   containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());
      System.out.println(containerResponse1.getResponse().toString());
      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200",
                   200,
                   containerResponse2.getStatus());
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

      assertEquals("containerResponse3.getStatus() must return 200",
                   200,
                   containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse3.getContentType()
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
      assertEquals("containerResponse4.getStatus() must return 200",
                   200,
                   containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse4.getContentType()
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
      assertEquals("containerResponse1.getStatus() must return: " + 200,
                   200,
                   containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse1.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse1.getEntity());

      createActivities(demoIdentity, demoIdentity, 10);
      connectIdentities(demoIdentity, johnIdentity, true);
      createActivities(johnIdentity, demoIdentity, 5);

      ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse2.getStatus() must return 200",
                   200,
                   containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse2.getContentType()
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

      assertEquals("containerResponse3.getStatus() must return 200",
                   200,
                   containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> demoActivitiesFeed = activityManager.getActivityFeedWithListAccess(demoIdentity)
                                                                  .loadAsList(0, limit);
      compareActivities(demoActivitiesFeed, (ActivityRestListOut) containerResponse3.getEntity());

      createActivities(spaceIdentity, spaceIdentity, 10);

      Space space = spaceService.getSpaceByPrettyName("my_space_1");

      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200",
                   200,
                   containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse4.getContentType()
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
  public void testDefaultGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
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
      assertEquals("containerResponse2.getStatus() must return 200",
                   200,
                   containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse2.getContentType()
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

      assertEquals("containerResponse3.getStatus() must return 200",
                   200,
                   containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200",
                   200,
                   containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse4.getContentType()
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
  public void testMeParamGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    String resourceUrl = RESOURCE_URL + "meByTimestamp.json";
    startSessionAs("demo");

    // Demo creates 10 activities to his stream
    createActivities(demoIdentity, demoIdentity, 10);
    connectIdentities(demoIdentity, johnIdentity, true);
    // John creates 5 activities to Demo's stream
    createActivities(johnIdentity, demoIdentity, 5);
    // Demo gets activity stream of his which has 15 activities
    ContainerResponse containerResponse2 = service("GET", resourceUrl, "", null, null);
    assertEquals("containerResponse2.getStatus() must return 200",
                 200,
                 containerResponse2.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
               containerResponse2.getContentType()
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
  public void testLimitGetActivityStreamByIdentityIdWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    int limit = 10;
    // user identity
    {
      String resourceUrl = RESOURCE_URL + demoIdentity.getId() + "ByTimestamp.json?limit=" + limit;
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
      assertEquals("containerResponse.getStatus() must return 200",
                   200,
                   containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse2.getContentType()
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

      assertEquals("containerResponse3.getStatus() must return 200",
                   200,
                   containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200",
                   200,
                   containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse4.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesWithListAccess(spaceIdentity)
                                                               .loadAsList(0, limit);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
    }

  }

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
      assertEquals("containerResponse1.getStatus() must return 200",
                   200,
                   containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse1.getContentType()
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
      assertEquals("containerResponse2.getStatus() must return: " + 200,
                   200,
                   containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse2.getContentType()
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
      assertEquals("containerResponse1.getStatus() must return 200",
                   200,
                   containerResponse1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse1.getContentType()
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
      assertEquals("containerResponse2.getStatus() must return: " + 200,
                   200,
                   containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse2.getContentType()
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
  public void testDefaultGetActivityConnectionsOfAuthenticatedWithJsonFormat() throws Exception {
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
      assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(),
                   Response.Status.OK.getStatusCode(),
                   rsp1.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 rsp1.getContentType()
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
  public void testLimitGetActivityConnectionsOfAuthenticatedWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    int limit = 10;

    String resourceUrl = RESOURCE_URL + "connectionsByTimestamp.json?limit=" + limit;

    // Johns gets activity stream of Demo which has 0 activities
    startSessionAs("demo");
    ContainerResponse rsp = service("GET", resourceUrl, "", null, null);
    assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(),
                 Response.Status.OK.getStatusCode(),
                 rsp.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
               rsp.getContentType()
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
    assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(),
                 Response.Status.OK.getStatusCode(),
                 rsp1.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
               rsp1.getContentType()
                   .toString()
                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));

    List<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity)
                                                            .loadAsList(0, limit);
    compareActivities(demoActivities, (ActivityRestListOut) rsp1.getEntity());

    limit = 3;
    resourceUrl = RESOURCE_URL + "connectionsByTimestamp.json?limit=" + limit;
    rsp1 = service("GET", resourceUrl, "", null, null);
    assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(),
                 Response.Status.OK.getStatusCode(),
                 rsp1.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
               rsp1.getContentType()
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
    assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(),
                 Response.Status.OK.getStatusCode(),
                 rsp.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
               rsp.getContentType()
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
    assertEquals("Response's status must be: " + Response.Status.OK.getStatusCode(),
                 Response.Status.OK.getStatusCode(),
                 rsp.getStatus());
    assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
               rsp.getContentType()
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
   * @throws Exception
   */
  public void testDefaultGetActivitySpacesOfAuthenticatedWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    // user identity
    {
      String resourceUrl = RESOURCE_URL + "spacesByTimestamp.json";
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

      assertEquals("containerResponse3.getStatus() must return 200",
                   200,
                   containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200",
                   200,
                   containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse4.getContentType()
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
  public void testLimitGetActivitySpacesOfAuthenticatedWithJsonFormat() throws Exception {
    List<ExoSocialActivity> emptyList = new ArrayList<ExoSocialActivity>();
    int limit = 10;
    // user identity
    {
      String resourceUrl = RESOURCE_URL + "spacesByTimestamp.json?limit=" + limit;
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

      assertEquals("containerResponse3.getStatus() must return 200",
                   200,
                   containerResponse3.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse3.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      compareActivities(emptyList, (ActivityRestListOut) containerResponse3.getEntity());

      // Create 10 activities to that space with the poster as that space
      createActivities(spaceIdentity, spaceIdentity, 10);
      // Demo creates 5 activities to space activity stream
      createActivities(demoIdentity, spaceIdentity, 5);

      ContainerResponse containerResponse4 = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse4.getStatus() must return 200",
                   200,
                   containerResponse4.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse4.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      List<ExoSocialActivity> spaceActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity)
                                                               .loadAsList(0, limit);
      compareActivities(spaceActivities, (ActivityRestListOut) containerResponse4.getEntity());
    }
    endSession();

  }

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
      assertEquals("containerResponse2.getStatus() must return: " + 200,
                   200,
                   containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse2.getContentType()
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
      assertEquals("containerResponse2.getStatus() must return: " + 200,
                   200,
                   containerResponse2.getStatus());
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
                 containerResponse2.getContentType()
                                   .toString()
                                   .startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));

      compareActivities(olderActivities, (ActivityRestListOut) containerResponse2.getEntity());
    }
    endSession();
  }
}
