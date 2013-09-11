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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.service.rest.SpacesRestService.SpaceList;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class SpaceRestServiceTest extends AbstractResourceTest {
  static private SpaceService spaceService;
  
  private IdentityStorage identityStorage;
  
  private List<Space> tearDownSpaceList;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    addResource(SpacesRestService.class, null);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    tearDownSpaceList = new ArrayList<Space>();

    //
    assertNotNull(identityStorage);
    assertNotNull(tearDownSpaceList);

    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");

    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    assertNotNull(rootIdentity.getId());
    assertNotNull(johnIdentity.getId());
    assertNotNull(maryIdentity.getId());
    assertNotNull(demoIdentity.getId());
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
    removeResource(SpacesRestService.class);
  }
  
  //
  private void populateData() throws Exception {
    //make testdata
    int numberOfSpaces = 10;
    String apps = "";
    Space s = null;
    for(int i = 0; i < numberOfSpaces; i++) {
      apps += String.format("app%s,", i);
      s = getSpaceInstance(i, apps);
      tearDownSpaceList.add(s);
    }
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
    SpaceList list = (SpaceList) response.getEntity();
    assertNotNull(list);
    assertEquals(5, list.getSpaces().size());
    
    //
    Space space4 = spaceService.getSpaceByPrettyName("space_4");
    assertNotNull(space4);
    spaceService.updateSpaceAccessed("mary", space4);
    List<Space> spaces = spaceService.getLastAccessedSpace("mary", "app1", 0, 5);
    assertEquals(5, spaces.size());
    Space got = spaces.get(0);
    assertEquals("space_1", got.getPrettyName());
    
    response = service("GET", "/portal/social/spaces/lastVisitedSpace/list.json?appId=app1&limit=5", "", null, null);
    assertEquals(200, response.getStatus());
    SpaceList gotList = (SpaceList) response.getEntity();
    assertNotNull(gotList);
    List<SpaceRest> myList = gotList.getSpaces();
    SpaceRest sRest = myList.get(0);
    assertEquals("space_1", sRest.getName());
    assertTrue(sRest.getAvatarUrl().length() > 0);
    
    
    //
    Space space2 = spaceService.getSpaceByPrettyName("space_2");
    assertNotNull(space2);
    spaceService.updateSpaceAccessed("mary", space2);
    spaces = spaceService.getLastAccessedSpace("mary", "app1", 0, 5);
    assertEquals(5, spaces.size());
    got = spaces.get(0);
    assertEquals("space_1", got.getPrettyName());
    
    response = service("GET", "/portal/social/spaces/lastVisitedSpace/list.json?appId=app1&limit=5", "", null, null);
    assertEquals(200, response.getStatus());
    gotList = (SpaceList) response.getEntity();
    assertNotNull(gotList);
    myList = gotList.getSpaces();
    sRest = myList.get(0);
    assertEquals("space_1", sRest.getName());
    assertTrue(sRest.getAvatarUrl().length() > 0);
    
    //appIdNull
    response = service("GET", "/portal/social/spaces/lastVisitedSpace/list.json?limit=5", "", null, null);
    assertEquals(200, response.getStatus());
    gotList = (SpaceList) response.getEntity();
    assertNotNull(gotList);
    myList = gotList.getSpaces();
    sRest = myList.get(0);
    assertEquals("space_0", sRest.getName());
    assertTrue(sRest.getAvatarUrl().length() > 0);

    //
    endSession();
  }
  
  public void testGetSpacesLastVisitedAppIdNull() throws Exception {
    //
    startSessionAs("mary");
    
    populateData();
    //
    ContainerResponse response = service("GET", "/portal/social/spaces/lastVisitedSpace/list.json?appId=app1&limit=5", "", null, null);
    assertEquals(200, response.getStatus());
    SpaceList list = (SpaceList) response.getEntity();
    assertNotNull(list);
    assertEquals(5, list.getSpaces().size());
     
    //
    Space space2 = spaceService.getSpaceByPrettyName("space_2");
    assertNotNull(space2);
    spaceService.updateSpaceAccessed("mary", space2);
    List<Space> spaces = spaceService.getLastAccessedSpace("mary", "app1", 0, 5);
    assertEquals(5, spaces.size());
    Space got = spaces.get(0);
    assertEquals("space_1", got.getPrettyName());
    
    response = service("GET", "/portal/social/spaces/lastVisitedSpace/list.json?appId=app1&limit=5", "", null, null);
    assertEquals(200, response.getStatus());
    SpaceList gotList = (SpaceList) response.getEntity();
    assertNotNull(gotList);
    List<SpaceRest> myList = gotList.getSpaces();
    SpaceRest sRest = myList.get(0);
    assertEquals("space_1", sRest.getName());
    
    //appIdNull
    response = service("GET", "/portal/social/spaces/lastVisitedSpace/list.json?limit=5", "", null, null);
    assertEquals(200, response.getStatus());
    gotList = (SpaceList) response.getEntity();
    assertNotNull(gotList);
    myList = gotList.getSpaces();
    sRest = myList.get(0);
    assertEquals("space_0", sRest.getName());

    //
    endSession();
  }
  
  /**
   * Gets an instance of the space.
   *
   * @param number
   * @param apps
   * @return
   * @throws Exception
   * @since 4.0
   */
  private Space getSpaceInstance(int number, String apps) throws Exception {
    Space space = new Space();
    space.setDisplayName("space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setApp(apps);
    String[] managers = new String[] {"john", "mary"};
    String[] members = new String[] {"john", "mary","demo"};
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    space.setAvatarUrl("/profile/my_avatar_" + number);
    this.spaceService.saveSpace(space, true);
    return space;
  }
}
