package org.exoplatform.social.service.rest.impl.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.rest.impl.ContainerResponse;
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
import org.exoplatform.social.service.rest.RestProperties;
import org.exoplatform.social.service.rest.api.models.ActivitiesCollections;
import org.exoplatform.social.service.rest.api.models.SpacesCollections;
import org.exoplatform.social.service.rest.api.models.UsersCollections;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class SpaceSocialRestServiceTest extends AbstractResourceTest {
  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private SpaceService spaceService;
  
  private SpaceSocialRestServiceV1 spaceSocialRestService;
  
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
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    
    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);
    
    spaceSocialRestService = new SpaceSocialRestServiceV1();
    registry(spaceSocialRestService);
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
    unregistry(spaceSocialRestService);
  }

  public void testGetSpaces() throws Exception {
    //root creates 2 spaces
    getSpaceInstance(1, "root");
    getSpaceInstance(2, "root");
    startSessionAs("root");
    ContainerResponse response = service("GET", "/v1/social/spaces?limit=5&offset=0", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    SpacesCollections spaces = (SpacesCollections) response.getEntity();
    assertEquals(2, spaces.getSpaces().size());
    
    //demo creates 1 space
    startSessionAs("demo");
    getSpaceInstance(3, "demo");
    response = service("GET", "/v1/social/spaces?limit=5&offset=0", "", null, null);
    assertEquals(200, response.getStatus());
    spaces = (SpacesCollections) response.getEntity();
    //demo is member of only one space then he got just 1 result
    assertEquals(1, spaces.getSpaces().size());
  }
  
  public void testCreateSpace() throws Exception {
    startSessionAs("root");
    String input = "{\"displayName\":social}";
    //root try to update demo activity
    ContainerResponse response = getResponse("POST", "/v1/social/spaces/", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    Map<String, Object> result = (Map<String, Object>) response.getEntity();
    Space space = spaceService.getSpaceById(result.get(RestProperties.ID).toString());
    assertNotNull(space);
    assertEquals("social", space.getDisplayName());
    
    //
    tearDownSpaceList.add(space);
  }
  
  public void testGetUpdateDeleteSpaceById() throws Exception {
    //root creates 1 spaces
    Space space = getSpaceInstance(1, "root");
    startSessionAs("root");
    ContainerResponse response = service("GET", "/v1/social/spaces/" + space.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Map<String, Object> result = (Map<String, Object>) response.getEntity();
    assertEquals("space1", result.get(RestProperties.DISPLAY_NAME).toString());
    assertEquals(Space.PRIVATE, result.get(RestProperties.VISIBILITY).toString());
    
    //root update space's description and name
    String spaceId = result.get(RestProperties.ID).toString();
    String input = "{\"displayName\":displayName_updated, \"description\":description_updated}";
    response = getResponse("PUT", "/v1/social/spaces/" + spaceId, input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    space = spaceService.getSpaceById(spaceId);
    assertEquals("displayName_updated", space.getDisplayName());
    assertEquals("description_updated", space.getDescription());
    
    //root delete his space
    response = service("DELETE", "/v1/social/spaces/" + space.getId(), "", null, null);
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
    ContainerResponse response = service("GET", "/v1/social/spaces/" + space.getId() + "/users", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    UsersCollections collections = (UsersCollections) response.getEntity();
    assertEquals(4, collections.getUsers().size());
    
    response = service("GET", "/v1/social/spaces/" + space.getId() + "/users?role=manager", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (UsersCollections) response.getEntity();
    assertEquals(2, collections.getUsers().size());
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
    ContainerResponse response = service("GET", "/v1/social/spaces/" + space.getId() + "/activities", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    ActivitiesCollections activitiesCollections = (ActivitiesCollections) response.getEntity();
    assertEquals(5, activitiesCollections.getActivities().size());
    
    //root posts another activity
    String input = "{\"title\":title6}";
    response = getResponse("POST", "/v1/social/spaces/" + space.getId() + "/activities", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
    assertEquals(6, listAccess.getSize());
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
    space.setGroupId("/space/space" + number);
    String[] managers = new String[] {creator};
    String[] members = new String[] {creator};
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    this.spaceService.saveSpace(space, true);
    tearDownSpaceList.add(space);
    return space;
  }
}
