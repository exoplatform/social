package org.exoplatform.social.service.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.service.rest.api.models.ActivitiesCollections;
import org.exoplatform.social.service.rest.api.models.SpacesCollections;
import org.exoplatform.social.service.rest.api.models.UsersCollections;
import org.exoplatform.social.service.rest.impl.user.UserSocialRestServiceV1;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class UsersRestserviceTest extends AbstractResourceTest {
  
  static private UserSocialRestServiceV1 usersRestService;
  
  private IdentityStorage identityStorage;
  private ActivityManager activityManager;
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
    
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    
    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");
    
    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);
    
    usersRestService = new UserSocialRestServiceV1();
    registry(usersRestService);
  }

  public void tearDown() throws Exception {
    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityStorage.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }
    
    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);
    
    super.tearDown();
    unregistry(usersRestService);
  }

  public void testGetAllUsers() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", "/v1/social/users?limit=5&offset=0", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
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
