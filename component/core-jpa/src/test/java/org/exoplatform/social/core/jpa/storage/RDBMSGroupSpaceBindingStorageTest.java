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

package org.exoplatform.social.core.jpa.storage;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.StorageUtils;

/**
 * Unit Tests for
 * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage}
 */

public class RDBMSGroupSpaceBindingStorageTest extends AbstractCoreTest {

  private List<GroupSpaceBinding>  tearDownbindingList = new ArrayList<>();

  private SpaceStorage             spaceStorage;

  private IdentityStorage          identityStorage;

  private GroupSpaceBindingStorage groupSpaceBindingStorage;

  private Identity                 demo;

  private Identity                 mary;

  private Identity                 jame;

  private Identity                 root;

  private Identity                 john;


  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = this.getContainer().getComponentInstanceOfType(SpaceStorage.class);
    identityStorage = this.getContainer().getComponentInstanceOfType(IdentityStorage.class);
    groupSpaceBindingStorage = this.getContainer().getComponentInstanceOfType(GroupSpaceBindingStorage.class);

    root = new Identity("organization", "root");
    john = new Identity("organization", "john");
    demo = new Identity("organization", "demo");
    mary = new Identity("organization", "mary");
    jame = new Identity("organization", "jame");

    identityStorage.saveIdentity(root);
    identityStorage.saveIdentity(john);
    identityStorage.saveIdentity(demo);
    identityStorage.saveIdentity(mary);
    identityStorage.saveIdentity(jame);
  }

  /**
   * Cleans up.
   */
  @Override
  protected void tearDown() throws Exception {
    deleteAllBindings();
    super.tearDown();
  }

  protected void deleteAllBindings() {
    for (GroupSpaceBinding binding : tearDownbindingList) {
            groupSpaceBindingStorage.deleteBinding(Long.toString(binding.getId()));
        }
  }

  /**
   * Gets an instance of Space.
   *
   * @param number
   * @return an instance of space
   */
  private Space getSpaceInstance(int number) {
    Space space = new Space();
    space.setApp("app1,app2");
    space.setDisplayName("myspace"+ number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/spaces/space" + number);
    String[] managers = new String[] { "demo"};
    String[] members = new String[] { "john", "root"};
    String[] invitedUsers = new String[] { "mary" };
    String[] pendingUsers = new String[] { "jame"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    return space;
  }

  /**
   * Gets an instance of GroupSpaceBinding.
   *
   * @param id
   * @return an instance of space
   */
  private GroupSpaceBinding getGroupSpaceBindingInstance(long id,
                                                         String spaceId,
                                                         String spaceRole,
                                                         String group,
                                                         String groupRole) {
    GroupSpaceBinding groupSpaceBinding = new GroupSpaceBinding();
    groupSpaceBinding.setId(id);
    groupSpaceBinding.setSpaceId(spaceId);
    groupSpaceBinding.setSpaceRole(spaceRole);
    groupSpaceBinding.setGroup(group);
    groupSpaceBinding.setGroupRole(groupRole);
    return groupSpaceBinding;
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#findSpaceBindings}
   *
   * @throws Exception
   */
  public void testFindSpaceBindings() throws Exception {
    int totalBindings = 5;

     Space space = this.getSpaceInstance(1);
     spaceStorage.saveSpace(space, true);
     StorageUtils.persist();
     String spaceId = spaceStorage.getSpaceByPrettyName("myspace1").getId();

    for (int i = 1; i <= totalBindings; i++) {
        System.out.println("findSpaceBindings Space1 :"+spaceId);
        GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(i, spaceId, "member", "/platform/administrators", "Any");
      groupSpaceBindingStorage.saveBinding(groupSpaceBinding, true);
      tearDownbindingList.add(groupSpaceBinding);
      StorageUtils.persist();
    }
    assertEquals("groupSpaceBindingStorage.findSpaceBindings('1','manager') must return: " + totalBindings,
                 totalBindings,
                 groupSpaceBindingStorage.findSpaceBindings(spaceId, "member").size());
  }

}
