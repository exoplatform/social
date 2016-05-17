package org.exoplatform.social.rest.impl.space;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.SpaceEntity;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class SpaceRestResourcesTest extends AbstractResourceTest {
  private IdentityManager identityManager;
  private UserACL userACL;
  private ActivityManager activityManager;
  private SpaceService spaceService;
  
  private SpaceRestResourcesV1 spaceRestResources;
  
  private List<Space> tearDownSpaceList;
  private List<ExoSocialActivity> tearDownActivitiesList;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");
    tearDownSpaceList = new ArrayList<Space>();
    tearDownActivitiesList = new ArrayList<ExoSocialActivity>();
    
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    userACL = (UserACL) getContainer().getComponentInstanceOfType(UserACL.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    
    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);
    
    spaceRestResources = new SpaceRestResourcesV1(userACL);
    registry(spaceRestResources);
  }

  public void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivitiesList) {
      activityManager.deleteActivity(activity);
    }
    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
      if (spaceIdentity != null) {
        identityManager.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    
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
    //demo is member of only one space then he got just 1 result
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
    
    //
    tearDownSpaceList.add(space);
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
    
    //
    tearDownSpaceList.remove(0);
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
  
  public void testGetActivitiesSpaceById() throws Exception {
    //root creates 1 spaces and post 5 activities on it
    Space space = getSpaceInstance(1, "root");
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    for (int i = 0; i < 5 ; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activity.setUserId(rootIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
      tearDownActivitiesList.add(activity);
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
    
    //
    tearDownActivitiesList.add(activity);
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
    tearDownSpaceList.add(space);
    tearDownActivitiesList.addAll(getCreatedSpaceActivities(space));
    return space;
  }
  
  private List<ExoSocialActivity> getCreatedSpaceActivities(Space space) {
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
    return listAccess.loadAsList(0, 10);
  }
}
