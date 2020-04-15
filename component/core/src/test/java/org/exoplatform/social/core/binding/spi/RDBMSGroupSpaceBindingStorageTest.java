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

package org.exoplatform.social.core.binding.spi;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingQueue;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;

/**
 * Unit Tests for
 * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage}
 */

public class RDBMSGroupSpaceBindingStorageTest extends AbstractCoreTest {

  private List<GroupSpaceBinding>      tearDownGroupbindingList      = new ArrayList<>();

  private List<GroupSpaceBindingQueue> tearDownGroupbindingQueueList = new ArrayList<>();

  private List<UserSpaceBinding>       tearDownUserbindingList       = new ArrayList<>();

  private SpaceStorage                 spaceStorage;

  private IdentityStorage              identityStorage;

  private GroupSpaceBindingStorage     groupSpaceBindingStorage;

  private Identity                     demo;

  private Identity                     mary;

  private Identity                     jame;

  private Identity                     root;

  private Identity                     john;

  private String                       spaceId;

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

    Space space = this.getSpaceInstance(1);
    spaceStorage.saveSpace(space, true);
    spaceId = spaceStorage.getSpaceByPrettyName("myspacetestbinding1").getId();
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
    for (UserSpaceBinding userSpaceBinding : tearDownUserbindingList) {
      groupSpaceBindingStorage.deleteUserBinding(userSpaceBinding.getId());
    }
    for (GroupSpaceBinding binding : tearDownGroupbindingList) {
      groupSpaceBindingStorage.deleteGroupBinding(binding.getId());
    }
    for (GroupSpaceBindingQueue binding : tearDownGroupbindingQueueList) {
      groupSpaceBindingStorage.deleteGroupBindingQueue(binding.getId());
    }
    tearDownGroupbindingList = new ArrayList<>();
    tearDownUserbindingList = new ArrayList<>();
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
    space.setDisplayName("myspacetestbinding" + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/spaces/space" + number);
    String[] managers = new String[] { "demo" };
    String[] members = new String[] { "john", "root" };
    String[] invitedUsers = new String[] { "mary" };
    String[] pendingUsers = new String[] { "jame" };
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
   **/
  private GroupSpaceBinding getGroupSpaceBindingInstance(long id, String spaceId, String group) {
    GroupSpaceBinding groupSpaceBinding = new GroupSpaceBinding();
    groupSpaceBinding.setId(id);
    groupSpaceBinding.setSpaceId(spaceId);
    groupSpaceBinding.setGroup(group);
    return groupSpaceBinding;
  }

  /**
   * Gets an instance of GroupSpaceBindingQueue.
   *
   * @return an instance of GgroupSpaceBindingQueue
   **/
  private GroupSpaceBindingQueue getGroupSpaceBindingQueueInstance(GroupSpaceBinding groupSpaceBinding, String action) {
    GroupSpaceBindingQueue groupSpaceBindingQueue = new GroupSpaceBindingQueue();
    groupSpaceBindingQueue.setGroupSpaceBinding(groupSpaceBinding);
    groupSpaceBindingQueue.setAction(action);
    return groupSpaceBindingQueue;
  }

  /**
   * Gets an instance of UserBinding.
   *
   * @param id
   * @return an instance of space
   **/
  private UserSpaceBinding getUserBindingInstance(long id, String userName, GroupSpaceBinding groupSpaceBinding) {
    UserSpaceBinding userSpaceBinding = new UserSpaceBinding();
    userSpaceBinding.setId(id);
    userSpaceBinding.setUser(userName);
    userSpaceBinding.setGroupBinding(groupSpaceBinding);
    return userSpaceBinding;
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#findGroupSpaceBindingsBySpace(String)}
   *
   * @throws Exception
   **/

  public void testFindSpaceBindings() throws Exception {
    int totalBindings = 5;

    for (int i = 1; i <= totalBindings; i++) {
      GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(i, spaceId, "/platform/administrators");
      groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
      tearDownGroupbindingList.add(groupSpaceBinding);
    }
    assertEquals("groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(" + spaceId + ",'member') must return: " + totalBindings,
                 totalBindings,
                 groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(spaceId).size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#findUserSpaceBindingsBySpace(String, String)}
   *
   * @throws Exception
   **/

  public void testFindUserBindings() throws Exception {
    int totalBindings = 5;

    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    tearDownGroupbindingList.add(groupSpaceBinding);
    for (int i = 1; i <= totalBindings; i++) {
      UserSpaceBinding userSpaceBinding = this.getUserBindingInstance(1, "john", groupSpaceBinding);
      userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
      tearDownUserbindingList.add(userSpaceBinding);
    }
    assertEquals("groupSpaceBindingStorage.findUserSpaceBindingsBySpace(" + spaceId + ",'john') must return: " + totalBindings,
                 totalBindings,
                 groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, "john").size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#saveGroupSpaceBinding}
   *
   * @throws Exception
   **/

  public void testSaveGroupBinding() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    tearDownGroupbindingList.add(groupSpaceBinding);
    assertEquals("groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(" + spaceId + ",'member') must return after creation: "
        + 1, 1, groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(spaceId).size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#createGroupSpaceBindingQueue(GroupSpaceBindingQueue)}
   *
   * @throws Exception
   **/

  public void testSaveGroupBindingQueue() throws Exception {
    GroupSpaceBinding groupSpaceBinding =
                                        groupSpaceBindingStorage.saveGroupSpaceBinding(getGroupSpaceBindingInstance(0,
                                                                                                                    spaceId,
                                                                                                                    "/platform/administrators"));
    GroupSpaceBindingQueue groupSpaceBindingQueue = new GroupSpaceBindingQueue(groupSpaceBinding,
                                                                               GroupSpaceBindingQueue.ACTION_CREATE);
    groupSpaceBindingQueue = groupSpaceBindingStorage.createGroupSpaceBindingQueue(groupSpaceBindingQueue);
    tearDownGroupbindingQueueList.add(groupSpaceBindingQueue);
    assertEquals("groupSpaceBindingStorage.findFirstGroupSpaceBindingQueue() must return after creation: " + 1,
                 1,
                 groupSpaceBindingStorage.findFirstGroupSpaceBindingQueue().getId());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#saveUserBinding(UserSpaceBinding)}
   *
   * @throws Exception
   **/

  public void testSaveUserBinding() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    tearDownGroupbindingList.add(groupSpaceBinding);
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance(1, "john", groupSpaceBinding);
    userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    tearDownUserbindingList.add(userSpaceBinding);
    assertEquals("groupSpaceBindingStorage.findUserBindingsbyMember(" + spaceId + ",'member') must return after creation: " + 1,
                 1,
                 groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, "john").size());
    assertEquals("Invalid group binding :" + userSpaceBinding.getGroupBinding().getGroup(),
                 userSpaceBinding.getGroupBinding().getGroup(),
                 "/platform/administrators");
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#saveGroupSpaceBinding(GroupSpaceBinding)}
   *
   * @throws Exception
   **/

  public void testUpdateGroupBinding() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    tearDownGroupbindingList.add(groupSpaceBinding);
    groupSpaceBinding = this.getGroupSpaceBindingInstance(groupSpaceBinding.getId(), spaceId, "/platform/users");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    assertEquals("groupSpaceBindingStorage.findGroupSpaceBindingsBySpace('1','member') must return after update: " + 1,
                 2,
                 groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(spaceId).size());
    assertEquals("Updated binding group must be: " + 1, "/platform/users", groupSpaceBinding.getGroup());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#deleteGroupBinding(long)}
   *
   * @throws Exception
   **/

  public void testDeleteGroupBinding() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    groupSpaceBindingStorage.deleteGroupBinding(groupSpaceBinding.getId());
    assertEquals("groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(" + groupSpaceBinding.getId()
        + ") must return after deletion: " + 0, 0, groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(spaceId).size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#deleteUserBinding(long)}
   *
   * @throws Exception
   **/

  public void testDeleteUserBinding() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    tearDownGroupbindingList.add(groupSpaceBinding);
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance(1, "john", groupSpaceBinding);
    userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    groupSpaceBindingStorage.deleteUserBinding(userSpaceBinding.getId());
    assertEquals("groupSpaceBindingStorage.findUserBindingsbyMember(" + spaceId + ",'john') must return after deletion: " + 0,
                 0,
                 groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, "john").size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#deleteAllUserBindings(String)}
   *
   * @throws Exception
   **/

  public void testDeleteAllUserBindings() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    tearDownGroupbindingList.add(groupSpaceBinding);
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance(1, "john", groupSpaceBinding);
    userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    userSpaceBinding = this.getUserBindingInstance(2, "john", groupSpaceBinding);
    userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    userSpaceBinding = this.getUserBindingInstance(3, "mary", groupSpaceBinding);
    userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    tearDownUserbindingList.add(userSpaceBinding);
    groupSpaceBindingStorage.deleteAllUserBindings("john");
    assertEquals("groupSpaceBindingStorage.findUserBindingsbyMember(" + spaceId + ",'john') must return after deletion: " + 0,
                 0,
                 groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, "john").size());
    assertEquals("groupSpaceBindingStorage.findUserBindingsbyMember(" + spaceId + ",'mary') must return after deletion: " + 1,
                 1,
                 groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, "mary").size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#findUserSpaceBindingsBySpace(String, String)}
   *
   * @throws Exception
   **/

  public void testHasUserBindings() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    tearDownGroupbindingList.add(groupSpaceBinding);
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance(1, "john", groupSpaceBinding);
    userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    tearDownUserbindingList.add(userSpaceBinding);
    assertEquals("groupSpaceBindingStorage.findUserSpaceBindingsBySpace(" + spaceId + ",'john') must return true ",
                 true,
                 groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, "john").size() > 0);
    assertEquals("groupSpaceBindingStorage.findUserSpaceBindingsBySpace(" + spaceId + ",'mary') must return false ",
                 false,
                 groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, "mary").size() > 0);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#findUserSpaceBindingsBySpace(String, String)}
   *
   * @throws Exception
   **/
  public void testCountUserBindings() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    tearDownGroupbindingList.add(groupSpaceBinding);
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance(1, "john", groupSpaceBinding);
    userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    tearDownUserbindingList.add(userSpaceBinding);
    assertEquals("groupSpaceBindingStorage.countUserBindings(" + spaceId + ",'john') must return 1 ",
                 1,
                 groupSpaceBindingStorage.countUserBindings(spaceId, "john"));
    assertEquals("groupSpaceBindingStorage.countUserBindings(" + spaceId + ",'mary') must return 0 ",
                 0,
                 groupSpaceBindingStorage.countUserBindings(spaceId, "mary"));
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#findUserAllBindingsByGroup(String)}
   *
   * @throws Exception
   **/

  public void testfindUserAllBindingsbyGroupMembership() throws Exception {
    int totalBindings = 5;

    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    tearDownGroupbindingList.add(groupSpaceBinding);

    GroupSpaceBinding groupSpaceBinding1 = this.getGroupSpaceBindingInstance(2, spaceId, "/platform/users");
    groupSpaceBinding1 = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding1);
    tearDownGroupbindingList.add(groupSpaceBinding1);

    for (int i = 1; i <= totalBindings; i++) {
      UserSpaceBinding userSpaceBinding = this.getUserBindingInstance(1, "john", groupSpaceBinding);
      userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
      tearDownUserbindingList.add(userSpaceBinding);
    }
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#findUserSpaceBindingsByGroup(String, String)}
   *
   * @throws Exception
   **/

  public void testfindUserSpaceBindingsByGroup() throws Exception {
    int totalBindings = 5;

    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    tearDownGroupbindingList.add(groupSpaceBinding);

    GroupSpaceBinding groupSpaceBinding1 = this.getGroupSpaceBindingInstance(2, spaceId, "/platform/users");
    groupSpaceBinding1 = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding1);
    tearDownGroupbindingList.add(groupSpaceBinding1);

    for (int i = 1; i <= totalBindings; i++) {
      UserSpaceBinding userSpaceBinding = this.getUserBindingInstance(1, "john", groupSpaceBinding);
      userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
      tearDownUserbindingList.add(userSpaceBinding);
    }
    assertEquals("findUserAllBindingsbyGroupMembership('/platform/administrators','Any') must return: " + totalBindings,
                 totalBindings,
                 groupSpaceBindingStorage.findUserSpaceBindingsByGroup("/platform/administrators", "john").size());

    assertEquals("findUserAllBindingsbyGroupMembership('/platform/administrators','Any') must return: " + 0,
                 0,
                 groupSpaceBindingStorage.findUserSpaceBindingsByGroup("/platform/users", "john").size());
  }
  
  public void testCountUserBindingsBySpace() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(1, spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    GroupSpaceBinding groupSpaceBinding2 = this.getGroupSpaceBindingInstance(2, spaceId, "/platform/developers");
    groupSpaceBinding2 = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding2);
  
  
    tearDownGroupbindingList.add(groupSpaceBinding);
    tearDownGroupbindingList.add(groupSpaceBinding2);
    
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance(1, "john", groupSpaceBinding);
    userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    UserSpaceBinding userSpaceBinding2 = this.getUserBindingInstance(2, "mary", groupSpaceBinding);
    userSpaceBinding2 = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding2);
    UserSpaceBinding userSpaceBinding3 = this.getUserBindingInstance(1, "john", groupSpaceBinding2);
    userSpaceBinding3 = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding3);
    
    tearDownUserbindingList.add(userSpaceBinding);
    tearDownUserbindingList.add(userSpaceBinding2);
    tearDownUserbindingList.add(userSpaceBinding3);
    
    
    assertEquals("groupSpaceBindingStorage.countBoundUsers(" + spaceId + ") must return 2 ",
                 2,
                 groupSpaceBindingStorage.countBoundUsers(spaceId));
  }
}
