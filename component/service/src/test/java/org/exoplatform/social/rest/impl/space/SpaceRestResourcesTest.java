package org.exoplatform.social.rest.impl.space;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.*;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ActivityFile;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.mock.MockUploadService;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.upload.UploadService;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.SpaceEntity;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class SpaceRestResourcesTest extends AbstractResourceTest {
  private IdentityManager identityManager;
  private UserACL userACL;
  private ActivityManager activityManager;
  private SpaceService spaceService;

  private SpaceRestResourcesV1 spaceRestResources;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  private MockUploadService    uploadService;

  public void setUp() throws Exception {
    super.setUp();

    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    uploadService = (MockUploadService) getContainer().getComponentInstanceOfType(UploadService.class);

    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);

    spaceRestResources = new SpaceRestResourcesV1(identityManager);
    registry(spaceRestResources);
  }

  public void tearDown() throws Exception {
    // TODO
    /*
    for (ExoSocialActivity activity : tearDownActivitiesList) {
      activityManager.deleteActivity(activity);
    }
    */

    super.tearDown();
    removeResource(spaceRestResources.getClass());
  }

  public void testSpaceVisibilityUpdateWithDifferentCases () throws Exception {
    startSessionAs("root");
    /*
    *
    * Test of 'private' and 'hidden' fields with a mix of upper/lower cases
    */

    Space space = getSpaceInstance(1, "root");

    Map<String,String> listOfResponses = new HashMap<String,String>() {{
      put("{\"visibility\":PRIVATE}", Space.PRIVATE);
      put("{\"visibility\":private}", Space.PRIVATE);
      put("{\"visibility\":PriVatE}", Space.PRIVATE);
      put("{\"visibility\":HIDDEN}", Space.HIDDEN);
      put("{\"visibility\":hidden}", Space.HIDDEN);
      put("{\"visibility\":HiDdEn}", Space.HIDDEN);
    }};

    ContainerResponse response = null;

    for (Map.Entry<String, String> entry : listOfResponses.entrySet()) {
      String input = entry.getKey();
      String expectedOutput = entry.getValue();
      response = getResponse("PUT", getURLResource("spaces/" + space.getId()), input);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
      assertEquals(expectedOutput, spaceEntity.getVisibility());
    }
  }

  public void testGetSpaces() throws Exception {
    getSpaceInstance(1, "root");
    getSpaceInstance(2, "john");
    getSpaceInstance(3, "demo");

    startSessionAs("demo");
    ContainerResponse response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", null, null);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    // demo is member of only one space then he got just 1 result
    assertEquals(1, collections.getEntities().size());

    HashSet<MembershipEntry> ms = new HashSet<MembershipEntry>();
    ms.add(new MembershipEntry("/platform/administrators"));
    startSessionAs("john", ms);
    response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    // Only the super user can see all the spaces.
    startSessionAs("root", ms);
    response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(3, collections.getEntities().size());

    response = service("GET", getURLResource("spaces?limit=5&offset=1"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
  }

  public void testShouldUseCacheWhenSpacesDidNotChanged() throws Exception {
    getSpaceInstance(1, "root");
    getSpaceInstance(2, "john");
    getSpaceInstance(3, "demo");

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(3, collections.getEntities().size());
    EntityTag eTag = (EntityTag) response.getHttpHeaders().getFirst("ETAG");
    assertNotNull(eTag);

    MultivaluedMap<String,String> headers = new MultivaluedMapImpl();
    headers.putSingle("If-None-Match", "\"" + eTag.getValue() + "\"");
    response = service("GET", getURLResource("spaces?limit=5&offset=0"), "", headers, null);
    assertNotNull(response);
    assertEquals(304, response.getStatus());
  }

  public void testCreateSpace() throws Exception {
    startSessionAs("root");
    String input = "{\"displayName\":social}";
    //root try to update demo activity
    ContainerResponse response = getResponse("POST", getURLResource("spaces/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    Space space = spaceService.getSpaceById(spaceEntity.getId());
    assertNotNull(space);
    assertEquals("social", space.getDisplayName());
  }

  public void testGetSpace() throws Exception {
    startSessionAs("root");
    String input = "{\"displayName\":\"test space\"}";
    //root creates a space
    ContainerResponse response = getResponse("POST", getURLResource("spaces/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    Space space = spaceService.getSpaceById(spaceEntity.getId());
    assertNotNull(space);

    // Get space by its id
    response = service("GET", getURLResource("spaces/" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertNotNull(spaceEntity);
    assertEquals("test space", spaceEntity.getDisplayName());
  }

  public void testGetUpdateDeleteSpaceById() throws Exception {
    //root creates 1 spaces
    Space space = getSpaceInstance(1, "root");
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces/" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    SpaceEntity spaceEntity = getBaseEntity(response.getEntity(), SpaceEntity.class);
    assertEquals("space1", spaceEntity.getDisplayName());
    assertEquals(Space.PRIVATE, spaceEntity.getVisibility());

    //root update space's description and name
    String spaceId = spaceEntity.getId();
    String input = "{\"displayName\":displayName_updated, \"description\":description_updated}";
    response = getResponse("PUT", getURLResource("spaces/" + spaceId), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    space = spaceService.getSpaceById(spaceId);
    assertEquals("displayName_updated", space.getDisplayName());
    assertEquals("description_updated", space.getDescription());

    //root delete his space
    response = service("DELETE", getURLResource("spaces/" + space.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    space = spaceService.getSpaceById(spaceId);
    assertNull(space);
  }

  public void testGetUsersSpaceById() throws Exception {
    //root creates 1 spaces
    Space space = getSpaceInstance(1, "root");
    space.setMembers(new String[] {"root", "john", "mary", "demo"});
    space.setManagers(new String[] {"root", "john"});
    spaceService.updateSpace(space);

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces/" + space.getId() + "/users"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(4, collections.getEntities().size());

    response = service("GET", getURLResource("spaces/" + space.getId() + "/users?role=manager"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
  }

  public void testGetSpaceByIdWithDeletedDisableUsers() throws Exception {
    //root creates 1 spaces
    Space space = getSpaceInstance(1, "root");
    space.setMembers(new String[] {"root", "john", "mary", "demo"});
    space.setManagers(new String[] {"root", "john"});
    spaceService.updateSpace(space);

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces/" + space.getId() + "/users"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    List<DataEntity> dataEntities = (List<DataEntity>) collections.getEntities();
    assertEquals(4, dataEntities.size());
    // Make sure properties 'deleted' and 'enabled' are added to the dataEntity.
    assertEquals(9, dataEntities.get(0).size());
    assertEquals(true, dataEntities.get(0).containsKey("deleted"));
    assertEquals(true, dataEntities.get(0).containsKey("enabled"));
    assertEquals(rootIdentity.isDeleted(), dataEntities.get(0).get("deleted"));
    assertEquals(rootIdentity.isEnable(), dataEntities.get(0).get("enabled"));

    response = service("GET", getURLResource("spaces/" + space.getId() + "/users?role=manager"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    dataEntities = (List<DataEntity>) collections.getEntities();
    // Make sure properties 'deleted' and 'enabled' are added to the dataEntity.
    assertEquals(2, dataEntities.size());
    assertEquals(9, dataEntities.get(0).size());
    assertEquals(true, dataEntities.get(0).containsKey("deleted"));
    assertEquals(true, dataEntities.get(0).containsKey("enabled"));
    assertEquals(johnIdentity.isDeleted(), dataEntities.get(0).get("deleted"));
    assertEquals(johnIdentity.isEnable(), dataEntities.get(0).get("enabled"));
  }

  public void testGetActivitiesSpaceById() throws Exception {
    //root creates 1 spaces and post 5 activities on it
    Space space = getSpaceInstance(1, "root");
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    for (int i = 0; i < 5 ; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activity.setUserId(rootIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
    }

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spaces/" + space.getId() + "/activities"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity activitiesCollections = (CollectionEntity) response.getEntity();
    assertEquals(6, activitiesCollections.getEntities().size());

    //root posts another activity
    String input = "{\"title\":title6}";
    response = getResponse("POST", getURLResource("spaces/" + space.getId() + "/activities"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
    assertEquals(7, listAccess.getSize());
    ExoSocialActivity activity = listAccess.load(0, 10)[0];
    assertEquals("title6", activity.getTitle());
  }

  public void testGetSpaceActivityFileByFileId() throws Exception {
    // Given
    startSessionAs("root");
    Space space = getSpaceInstance(1, "root");
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    try {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      Map<String, String> templateParams = new HashMap<>();
      activity.setType("DOC_ACTIVITY");
      activity.setTitle("Activity Title");
      activity.setBody("Activity Content");
      activity.setTemplateParams(templateParams);
      activityManager.saveActivityNoReturn(spaceIdentity, activity);

      // When
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
      ExoSocialActivity createdActivity = activityManager.getActivity(activity.getId());

      // Then
      assertEquals(0 , activityManager.getActivityFilesIds(activity).size());
      ContainerResponse response = service("GET",
                                           getURLResource("spaces/" + createdActivity.getId() + "/files/1"),
                                           "",
                                           null,
                                           null);
      assertNotNull(response);
      assertEquals(404, response.getStatus());
    } finally {
      if (space != null) {
        spaceService.deleteSpace(space);
      }
    }
  }

  private Space getSpaceInstance(int number, String creator) throws Exception {
    Space space = new Space();
    space.setDisplayName("space" + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    this.spaceService.createSpace(space, creator);
    return space;
  }

  private List<ExoSocialActivity> getCreatedSpaceActivities(Space space) {
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
    return listAccess.loadAsList(0, 10);
  }
}
