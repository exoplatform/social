/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.service.rest;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.api.models.IdentityNameList;
import org.exoplatform.social.service.test.AbstractResourceTest;

import java.util.List;

public class SpaceRestServiceTest extends AbstractResourceTest {
  static private SpaceService spaceService;
  
  private IdentityManager identityManager;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    addResource(SpacesRestService.class, null);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);

    //
    assertNotNull(identityManager);

    rootIdentity = new Identity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = new Identity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = new Identity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = new Identity(OrganizationIdentityProvider.NAME, "demo");

    identityManager.saveIdentity(rootIdentity);
    identityManager.saveIdentity(johnIdentity);
    identityManager.saveIdentity(maryIdentity);
    identityManager.saveIdentity(demoIdentity);

    assertNotNull(rootIdentity.getId());
    assertNotNull(johnIdentity.getId());
    assertNotNull(maryIdentity.getId());
    assertNotNull(demoIdentity.getId());
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(SpacesRestService.class);
  }
  
  //
  private void populateData() throws Exception {
    //make testdata
    String apps = "app1:app1:false:installed";
    createSpace("space_0", null);
    createSpace("space_1", apps);
    createSpace("space_2", apps);
    createSpace("space_3", apps);
    createSpace("space_4", apps);
    createSpace("space_5", apps);
    List<Space> spaces = spaceService.getLastAccessedSpace("mary", "app1", 0, 5);
    assertEquals(5, spaces.size());
  }

  public void testGetSpacesLastVisited() throws Exception {
    //
    startSessionAs("mary");
    
    populateData();
    //
    ContainerResponse response = service("GET", "/portal/social/spaces/lastVisitedSpace/list.json?appId=app1&limit=5", "", null, null);
    assertEquals(200, response.getStatus());
    SpacesRestService.SpaceList list = (SpacesRestService.SpaceList) response.getEntity();
    assertNotNull(list);
    assertEquals(5, list.getSpaces().size());
    SpaceRest spaceRest = list.getSpaces().get(0);
    assertEquals("space_1", spaceRest.getDisplayName());
    
    //
    Space space4 = spaceService.getSpaceByPrettyName("space_4");
    assertNotNull(space4);
    spaceService.updateSpaceAccessed("mary", space4);
    List<Space> spaces = spaceService.getLastAccessedSpace("mary", "app1", 0, 5);
    assertEquals(5, spaces.size());
    Space got = spaces.get(0);
    assertEquals("space_4", got.getPrettyName());
    
    response = service("GET", "/portal/social/spaces/lastVisitedSpace/list.json?appId=app1&limit=5", "", null, null);
    assertEquals(200, response.getStatus());
    SpacesRestService.SpaceList gotList = (SpacesRestService.SpaceList) response.getEntity();
    assertNotNull(gotList);
    List<SpaceRest> myList = gotList.getSpaces();
    SpaceRest sRest = myList.get(0);
    assertEquals("space_4", sRest.getName());
    assertTrue(sRest.getAvatarUrl().length() > 0);
    
    
    //
    Space space2 = spaceService.getSpaceByPrettyName("space_2");
    assertNotNull(space2);
    spaceService.updateSpaceAccessed("mary", space2);
    spaces = spaceService.getLastAccessedSpace("mary", "app1", 0, 5);
    assertEquals(5, spaces.size());
    got = spaces.get(0);
    assertEquals("space_2", got.getPrettyName());
    
    response = service("GET", "/portal/social/spaces/lastVisitedSpace/list.json?appId=app1&limit=5", "", null, null);
    assertEquals(200, response.getStatus());
    gotList = (SpacesRestService.SpaceList) response.getEntity();
    assertNotNull(gotList);
    myList = gotList.getSpaces();
    sRest = myList.get(0);
    assertEquals("space_2", sRest.getName());
    assertTrue(sRest.getAvatarUrl().length() > 0);

    //
    endSession();
  }

  public void testGetSpacesLastVisitedAppIdNull() throws Exception {
    //
    startSessionAs("mary");
    
    populateData();

    ContainerResponse response = service("GET", "/portal/social/spaces/lastVisitedSpace/list.json?limit=5", "", null, null);
    assertEquals(200, response.getStatus());
    SpacesRestService.SpaceList gotList = (SpacesRestService.SpaceList) response.getEntity();
    assertNotNull(gotList);
    List<SpaceRest> myList = gotList.getSpaces();
    SpaceRest sRest = myList.get(0);
    assertEquals("space_0", sRest.getName());

    //
    Space space4 = spaceService.getSpaceByPrettyName("space_4");
    assertNotNull(space4);
    spaceService.updateSpaceAccessed("mary", space4);
    List<Space> spaces = spaceService.getLastAccessedSpace("mary", null, 0, 5);
    assertEquals(5, spaces.size());
    Space got = spaces.get(0);
    assertEquals("space_4", got.getPrettyName());

    //
    endSession();
  }

  public void testGetSpaceInfo() throws Exception {
    startSessionAs("root");
    Space space = new Space();
    space.setDisplayName("testspace");
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space ");
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/" + space.getPrettyName());
    String[] managers = new String[]{"root"};
    String[] members = new String[]{"root", "mary"};
    String[] invitedUsers = new String[]{"john"};
    String[] pendingUsers = new String[]{};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl("/space/" + space.getPrettyName());
    spaceService.saveSpace(space, true);
    space = spaceService.getSpaceByPrettyName(space.getPrettyName());
    space.setDisplayName("test1");
    space.setPrettyName("test1");
    spaceService.saveSpace(space, false);
    // Test Rest URL at org.exoplatform.social.service.rest.SpacesRestService.getSpaceInfo
    ContainerResponse response = service("GET", "/portal/social/spaces/spaceInfo/?spaceName=testspace", "", null, null);
    assertEquals(200, response.getStatus());
    response = service("GET", "/portal/social/spaces/spaceInfo/?spaceName=spaceNotExist", "", null, null);
    assertEquals(404, response.getStatus());
  }

  public void testSuggestSpacesOfCurrentUser() throws Exception {
    //
    startSessionAs("mary");

    populateData();
    //
    ContainerResponse response = service("GET", "/portal/social/spaces/suggest.json?currentUser=mary&typeOfRelation=confirmed", "", null, null, "mary");
    assertEquals(200, response.getStatus());
    IdentityNameList list = (IdentityNameList) response.getEntity();
    assertNotNull(list);
    assertNotNull(list.getOptions());
    assertEquals(6, list.getOptions().size());

    //
    endSession();
  }

  public void testSuggestSpacesOfNoUser() throws Exception {
    //
    startSessionAs("mary");

    populateData();
    //
    ContainerResponse response = service("GET", "/portal/social/spaces/suggest.json?typeOfRelation=confirmed", "", null, null, "mary");
    assertEquals(400, response.getStatus());

    //
    endSession();
  }

  public void testSuggestSpacesOfAnotherUser() throws Exception {
    //
    startSessionAs("john");

    populateData();
    //
    ContainerResponse response = service("GET", "/portal/social/spaces/suggest.json?currentUser=mary&typeOfRelation=confirmed", "", null, null, "john");
    assertEquals(401, response.getStatus());

    //
    endSession();
  }

  /**
   * Gets an instance of the space.
   *
   * @param name
   * @param apps
   * @return
   * @throws Exception
   * @since 4.0
   */
  private Space createSpace(String name, String apps) throws Exception {
    Space space = new Space();
    space.setDisplayName(name);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + name);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/" + name);
    space.setApp(apps);
    String[] managers = new String[] {"john", "mary"};
    String[] members = new String[] {"john", "mary","demo"};
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    space.setAvatarUrl("/profile/my_avatar_" + name);
    this.spaceService.createSpace(space, "john");
    return space;
  }
}
