package org.exoplatform.social.rest.impl.spacemembership;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class SpaceMembershipRestResourcesTest extends AbstractResourceTest {
  private IdentityManager identityManager;
  private SpaceService spaceService;
  
  private SpaceMembershipRestResourcesV1 membershipRestResources;
  
  private List<Space> tearDownSpaceList;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");
    tearDownSpaceList = new ArrayList<Space>();
    
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    
    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);
    
    membershipRestResources = new SpaceMembershipRestResourcesV1();
    registry(membershipRestResources);
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
    removeResource(membershipRestResources.getClass());
  }

  public void testGetSpaceMembersShip() throws Exception {
    //root creates 2 spaces, john 1 and mary 1
    getSpaceInstance(1, "root");
    getSpaceInstance(2, "root");
    getSpaceInstance(3, "john");
    getSpaceInstance(4, "mary");
    
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("spacesMemberships"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(8, collections.getEntities().size());
    
    response = service("GET", getURLResource("spacesMemberships?user=root"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(4, collections.getEntities().size());
    
    response = service("GET", getURLResource("spacesMemberships?space=space3"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
  }
  
  public void testAddSpaceMemberShip() throws Exception {
    //root creates 1 space
    Space space = getSpaceInstance(1, "root");
    
    //root add demo as member of his space
    startSessionAs("root");
    String input = "{\"space\":space1, \"user\":demo}";
    ContainerResponse response = getResponse("POST", getURLResource("spacesMemberships"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    space = spaceService.getSpaceById(space.getId());
    assertTrue(ArrayUtils.contains(space.getMembers(), "demo"));
    
    //demo add mary as member of space1 but has no permission
    startSessionAs("demo");
    input = "{\"space\":space1, \"user\":mary}";
    response = getResponse("POST", getURLResource("spacesMemberships"), input);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }
  
  public void testGetUpdateDeleteSpaceMembership() throws Exception {
    //root creates 1 space
    Space space = getSpaceInstance(1, "root");
    spaceService.addMember(space, "demo");
    
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
    assertTrue(spaceService.isManager(spaceService.getSpaceById(space.getId()), "demo"));
    
    //delete membership of demo from space1
    response = service("DELETE", getURLResource("spacesMemberships/" + id), "", null, null);
    assertEquals(200, response.getStatus());
    assertFalse(spaceService.isMember(spaceService.getSpaceById(space.getId()), "demo"));
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
