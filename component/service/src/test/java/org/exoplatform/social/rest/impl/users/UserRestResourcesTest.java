package org.exoplatform.social.rest.impl.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.entity.ActivitiesCollections;
import org.exoplatform.social.rest.entity.SpacesCollections;
import org.exoplatform.social.rest.entity.UsersCollections;
import org.exoplatform.social.rest.impl.user.UserRestResourcesV1;
import org.exoplatform.social.service.rest.RestProperties;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class UserRestResourcesTest extends AbstractResourceTest {
  
  static private UserRestResourcesV1 usersRestService;
  
  private ActivityManager activityManager;
  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;
  
  private List<Space> tearDownSpaceList;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");
    tearDownSpaceList = new ArrayList<Space>();
    
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    
    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);
    
    usersRestService = new UserRestResourcesV1();
    registry(usersRestService);
  }

  public void tearDown() throws Exception {
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
    unregistry(usersRestService);
  }

  public void testGetAllUsers() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", "/v1/social/users?limit=5&offset=0", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    UsersCollections users = (UsersCollections) response.getEntity();
    assertEquals(4, users.getUsers().size());
  }
  
  public void testGetUserById() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", "/v1/social/users/john", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Map<String, Object> result = (Map<String, Object>) response.getEntity();
    assertEquals(result.get(RestProperties.USER_NAME), "john");
  }
  
  public void testGetConnectionsOfUser() throws Exception {
    startSessionAs("root");
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);
    
    ContainerResponse response = service("GET", "/v1/social/users/root/connections?limit=5&offset=0", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    UsersCollections collections = (UsersCollections) response.getEntity();
    assertEquals(1, collections.getUsers().size());
    Map<String, Object> result = collections.getUsers().get(0);
    assertEquals("demo", result.get(RestProperties.USER_NAME));
  }
  
  public void testGetActivitiesOfUser() throws Exception {
    startSessionAs("root");
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);
    
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    
    ExoSocialActivity maryActivity = new ExoSocialActivityImpl();
    maryActivity.setTitle("mary activity");
    activityManager.saveActivityNoReturn(maryIdentity, maryActivity);
    
    ContainerResponse response = service("GET", "/v1/social/users/root/activities?limit=5&offset=0", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    ActivitiesCollections collections = (ActivitiesCollections) response.getEntity();
    //must return one activity of root and one of demo
    assertEquals(2, collections.getActivities().size());
    Map<String, Object> result = collections.getActivities().get(0);
    assertEquals("demo activity", result.get(RestProperties.TITLE));
    result = collections.getActivities().get(1);
    assertEquals("root activity", result.get(RestProperties.TITLE));
    
    activityManager.deleteActivity(maryActivity);
    activityManager.deleteActivity(demoActivity);
    activityManager.deleteActivity(rootActivity);
  }
  
  public void testGetSpacesOfUser() throws Exception {
    startSessionAs("root");
    Space space = getSpaceInstance(0, "root");
    tearDownSpaceList.add(space);
    
    ContainerResponse response = service("GET", "/v1/social/users/root/spaces?limit=5&offset=0", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    SpacesCollections collections = (SpacesCollections) response.getEntity();
    assertEquals(1, collections.getSpaces().size());
    Map<String, Object> result = collections.getSpaces().get(0);
    assertEquals(space.getDisplayName(), result.get(RestProperties.DISPLAY_NAME));
  }
  
  private Space getSpaceInstance(int number, String creator) throws Exception {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    String[] managers = new String[] {creator};
    String[] members = new String[] {creator};
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    this.spaceService.saveSpace(space, true);
    return space;
  }
}
