/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.impl.binding.GroupSpaceBindingRestResourcesV1;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class GroupSpaceBindingRestServiceTest extends AbstractResourceTest {

  private IdentityManager                  identityManager;

  private GroupSpaceBindingRestResourcesV1 groupSpaceBindingRestResourcesV1;

  private SpaceService                     spaceService;

  private UserACL                          userACL;

  private GroupSpaceBindingService         groupSpaceBindingService;

  private String                           spaceId1;

  private String                           spaceId2;

  private List<GroupSpaceBinding>          tearDownbindingList = new ArrayList<>();

  public void setUp() throws Exception {
    super.setUp();
    deleteAllSpaces();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull(identityManager);

    identityManager.getOrCreateIdentity("organization", "root", true);
    identityManager.getOrCreateIdentity("organization", "john", true);

    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    userACL = getContainer().getComponentInstanceOfType(UserACL.class);
    groupSpaceBindingService = getContainer().getComponentInstanceOfType(GroupSpaceBindingService.class);
    groupSpaceBindingRestResourcesV1 = new GroupSpaceBindingRestResourcesV1(spaceService, groupSpaceBindingService, userACL);
    registry(groupSpaceBindingRestResourcesV1);
  }

  public void tearDown() throws Exception {
    deleteAllBindings();
    super.tearDown();
    removeResource(groupSpaceBindingRestResourcesV1.getClass());
  }

  protected void deleteAllBindings() {
    if (spaceId1 != null) {
      for (GroupSpaceBinding binding : groupSpaceBindingService.findGroupSpaceBindingsBySpace(spaceId1)) {
        groupSpaceBindingService.deleteGroupSpaceBinding(binding);
      }
    }
  }

  @Test
  public void testGroupSpaceBindings() throws Exception {

    // Given
    startSessionAs("root");
    spaceId1 = createSpace("space1", "").getId();
    // Given
    List<GroupSpaceBinding> groupSpaceBindings = new LinkedList<>();
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId(spaceId1);
    groupSpaceBindings.add(binding1);
    tearDownbindingList.add(binding1);

    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding2.setGroup("/platform/web-contributors");
    binding2.setSpaceId(spaceId1);
    groupSpaceBindings.add(binding2);
    tearDownbindingList.add(binding2);

    GroupSpaceBinding binding3 = new GroupSpaceBinding();
    binding3.setGroup("/platform/web-contributors");
    binding3.setSpaceId(spaceId1);
    groupSpaceBindings.add(binding3);
    tearDownbindingList.add(binding3);
    groupSpaceBindings.stream().forEach(groupSpaceBinding -> groupSpaceBindingService.saveGroupSpaceBinding(groupSpaceBinding));
    // when
    ContainerResponse response = service("GET", getURLResource("spaceGroupBindings/" + spaceId1), "", null, null);
    // then
    assertEquals(200, response.getStatus());

    endSession();
  }
  @Test
  public void testGetBindingReportOperations() throws Exception {
    
    // Given
    startSessionAs("root");
    spaceId1 = createSpace("space1", "").getId();
    // Given
    List<GroupSpaceBinding> groupSpaceBindings = new LinkedList<>();
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId(spaceId1);
    groupSpaceBindings.add(binding1);
    tearDownbindingList.add(binding1);
    
    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding2.setGroup("/platform/web-contributors");
    binding2.setSpaceId(spaceId1);
    groupSpaceBindings.add(binding2);
    tearDownbindingList.add(binding2);
    
    GroupSpaceBinding binding3 = new GroupSpaceBinding();
    binding3.setGroup("/platform/web-contributors");
    binding3.setSpaceId(spaceId1);
    groupSpaceBindings.add(binding3);
    tearDownbindingList.add(binding3);
    groupSpaceBindings.stream().forEach(groupSpaceBinding -> groupSpaceBindingService.saveGroupSpaceBinding(groupSpaceBinding));
    // when
    ContainerResponse response = service("GET", getURLResource("spaceGroupBindings/getBindingReportOperations"), "", null, null);
    // then
    assertEquals(200, response.getStatus());
    
    endSession();
  }
  

  @Test
  public void testDeleteSpaceBinding() throws Exception {

    // Given
    startSessionAs("root");
    spaceId2 = createSpace("space2", "").getId();

    // Given
    List<GroupSpaceBinding> groupSpaceBindings = new LinkedList<>();
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId(spaceId2);
    groupSpaceBindings.add(binding1);
    tearDownbindingList.add(binding1);
    GroupSpaceBinding binding = groupSpaceBindingService.saveGroupSpaceBinding(binding1);

    // when
    ContainerResponse response = service("DELETE",
                                         getURLResource("spaceGroupBindings/removeGroupSpaceBinding/"
                                             + String.valueOf(binding.getId())),
                                         "",
                                         null,
                                         null);
    // then
    assertEquals(200, response.getStatus());

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
    String[] managers = new String[] { "john", "mary" };
    String[] members = new String[] { "john", "mary", "demo" };
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    space.setAvatarUrl("/profile/my_avatar_" + name);
    this.spaceService.createSpace(space, "john");
    return space;
  }
}
