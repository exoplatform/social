package org.exoplatform.social.rest.impl.spacemembership;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.service.test.AbstractResourceTest;

import javax.ws.rs.core.Response;

public class SpaceMembershipRestResourcesTest extends AbstractResourceTest {
  private IdentityManager identityManager;
  private SpaceService spaceService;
  
  private SpaceMembershipRestResourcesV1 membershipRestResources;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    
    identityManager.getOrCreateIdentity("organization", "root", true);
    identityManager.getOrCreateIdentity("organization", "john", true);
    identityManager.getOrCreateIdentity("organization", "mary", true);
    identityManager.getOrCreateIdentity("organization", "demo", true);

    //root creates 2 spaces, john 1 and mary 1
    createSpaceIfNotExist(1, "root");
    createSpaceIfNotExist(2, "root");
    createSpaceIfNotExist(3, "john");
    createSpaceIfNotExist(4, "mary");

    membershipRestResources = new SpaceMembershipRestResourcesV1(spaceService, identityManager);
    registry(membershipRestResources);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(membershipRestResources.getClass());
  }

  public void testGetSpaceMembersShipOfCurrentUser() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spacesMemberships"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(4, collections.getEntities().size());
    
    response = service("GET", getURLResource("spacesMemberships?user=root"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(4, collections.getEntities().size());
    
    startSessionAs("john");
    response = service("GET", getURLResource("spacesMemberships?space=space3"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
  }

  public void testGetSpaceMembershipsOfAnotherUserAsANonSpacesAdministrator() throws Exception {
    startSessionAs("mary");
    ContainerResponse response = service("GET", getURLResource("spacesMemberships?user=john"), "", null, null, "mary");
    assertNotNull(response);
    assertEquals(401, response.getStatus());
  }

  public void testGetSpaceMembershipsOfAnotherUserAsASpacesAdministrator() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spacesMemberships?user=mary"), "", null, null, "root");
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
  }

  public void testGetSpaceMembershipsOfASpaceAsANonMember() throws Exception {
    startSessionAs("mary");
    ContainerResponse response = service("GET", getURLResource("spacesMemberships?space=space1"), "", null, null, "mary");
    assertNotNull(response);
    assertEquals(401, response.getStatus());
  }

  public void testGetSpaceMembershipsOfASpaceAsAManager() throws Exception {
    startSessionAs("mary");
    ContainerResponse response = service("GET", getURLResource("spacesMemberships?space=space4"), "", null, null, "mary");
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
  }

  public void testAddSpaceMemberShip() throws Exception {
    //root add demo as member of his space
    startSessionAs("root");
    String input = "{\"space\":space1, \"user\":demo}";
    ContainerResponse response = getResponse("POST", getURLResource("spacesMemberships"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    Space space = spaceService.getSpaceByPrettyName("space1");
    assertTrue(ArrayUtils.contains(space.getMembers(), "demo"));
    
    //demo add mary as member of space1 but has no permission
    startSessionAs("demo");
    input = "{\"space\":space1, \"user\":mary}";
    response = getResponse("POST", getURLResource("spacesMemberships"), input);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }
  
  public void testGetUpdateDeleteSpaceMembership() throws Exception {
    //root creates 1 space
    spaceService.addMember(spaceService.getSpaceByPrettyName("space1"), "demo");
    
    //root add demo as member of his space
    startSessionAs("root");
    String id = "space1:demo:member";
    ContainerResponse response = service("GET", getURLResource("spacesMemberships/" + id), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    //update demo to manager
    String input = "{\"role\":manager}";
    response = getResponse("PUT", getURLResource("spacesMemberships/" + id), input);
    assertEquals(200, response.getStatus());
    assertTrue(spaceService.isManager(spaceService.getSpaceByPrettyName("space1"), "demo"));
    
    //delete membership of demo from space1
    response = service("DELETE", getURLResource("spacesMemberships/" + id), "", null, null);
    assertEquals(200, response.getStatus());
    assertFalse(spaceService.isMember(spaceService.getSpaceByPrettyName("space1"), "demo"));
  }
  
  private void createSpaceIfNotExist(int number, String creator) throws Exception {
    String spaceName = "space" + number;
    if(spaceService.getSpaceByPrettyName(spaceName) == null) {
      Space space = new Space();
      space.setDisplayName(spaceName);
      space.setPrettyName(space.getDisplayName());
      space.setRegistration(Space.OPEN);
      space.setDescription("add new space " + number);
      space.setType(DefaultSpaceApplicationHandler.NAME);
      space.setVisibility(Space.PRIVATE);
      space.setRegistration(Space.VALIDATION);
      space.setPriority(Space.INTERMEDIATE_PRIORITY);
      this.spaceService.createSpace(space, creator);
    }
  }
}
