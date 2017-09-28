package org.exoplatform.social.rest.impl.users;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.ProfileEntity;
import org.exoplatform.social.rest.impl.user.UserRestResourcesV1;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class UserRestResourcesTest extends AbstractResourceTest {
  
  private ActivityManager activityManager;
  private IdentityManager identityManager;
  private UserACL userACL;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");

    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    userACL = getContainer().getComponentInstanceOfType(UserACL.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);

    rootIdentity = new Identity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = new Identity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = new Identity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = new Identity(OrganizationIdentityProvider.NAME, "demo");

    identityManager.saveIdentity(rootIdentity);
    identityManager.saveIdentity(johnIdentity);
    identityManager.saveIdentity(maryIdentity);
    identityManager.saveIdentity(demoIdentity);

    addResource(UserRestResourcesV1.class, null);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(UserRestResourcesV1.class);
  }

  public void testGetAllUsers() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("users?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(4, collections.getEntities().size());
  }

  public void testGetUserById() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("users/john"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    ProfileEntity userEntity = getBaseEntity(response.getEntity(), ProfileEntity.class);
    assertEquals("john", userEntity.getUsername());
  }

  public void testGetConnectionsOfUser() throws Exception {
    startSessionAs("root");
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);

    ContainerResponse response = service("GET", getURLResource("users/root/connections?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
    ProfileEntity userEntity = getBaseEntity(collections.getEntities().get(0), ProfileEntity.class);
    assertEquals("demo", userEntity.getUsername());
  }

  public void testGetActivitiesOfUser() throws Exception {
    startSessionAs("root");
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);

    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    //wait to make sure the order of activities
    Thread.sleep(10);
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    
    ExoSocialActivity maryActivity = new ExoSocialActivityImpl();
    maryActivity.setTitle("mary activity");
    activityManager.saveActivityNoReturn(maryIdentity, maryActivity);
    
    ContainerResponse response = service("GET", getURLResource("users/root/activities?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    //must return one activity of root and one of demo
    assertEquals(2, collections.getEntities().size());
    ActivityEntity activityEntity = getBaseEntity(collections.getEntities().get(0), ActivityEntity.class);
    assertEquals("demo activity", activityEntity.getTitle());
    activityEntity = getBaseEntity(collections.getEntities().get(1), ActivityEntity.class);
    assertEquals("root activity", activityEntity.getTitle());
    
    activityManager.deleteActivity(maryActivity);
    activityManager.deleteActivity(demoActivity);
    activityManager.deleteActivity(rootActivity);
  }

  public void testGetSpacesOfUser() throws Exception {
    getSpaceInstance(0, "root");
    getSpaceInstance(1, "john");
    getSpaceInstance(2, "demo");
    getSpaceInstance(3, "demo");
    
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("users/root/spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    startSessionAs("john");
    response = service("GET", getURLResource("users/john/spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    startSessionAs("demo");
    response = service("GET", getURLResource("users/demo/spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());

    startSessionAs("john");
    response = service("GET", getURLResource("users/demo/spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(403, response.getStatus());

    startSessionAs("root");
    response = service("GET", getURLResource("users/demo/spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
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
    space.setGroupId("/spaces/space" + number);
    String[] managers = new String[] {creator};
    String[] members = new String[] {creator};
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    this.spaceService.createSpace(space, creator);
    return space;
  }
}
