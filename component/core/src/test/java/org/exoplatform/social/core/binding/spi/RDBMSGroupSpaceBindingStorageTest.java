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
import java.util.Date;
import java.util.List;

import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingOperationReport;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingQueue;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingReportAction;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingReportUser;
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
  
    groupSpaceBindingStorage.findAllGroupSpaceBindingReportUser()
                            .stream()
                            .forEach(groupSpaceBindingReportUser -> groupSpaceBindingStorage.deleteGroupBindingReportUser(
                                groupSpaceBindingReportUser.getId()));
    groupSpaceBindingStorage.findAllGroupSpaceBindingReportAction()
                            .stream()
                            .forEach(groupSpaceBindingReport -> groupSpaceBindingStorage.deleteGroupBindingReport(
                                groupSpaceBindingReport.getId()));
    groupSpaceBindingStorage.findAllUserSpaceBinding()
                            .stream()
                            .forEach(userSpaceBinding -> groupSpaceBindingStorage.deleteUserBinding(userSpaceBinding.getId()));
    groupSpaceBindingStorage.findAllGroupSpaceBindingQueue()
                            .stream()
                            .forEach(binding -> groupSpaceBindingStorage.deleteGroupBindingQueue(binding.getId()));
    groupSpaceBindingStorage.findAllGroupSpaceBinding()
                            .stream()
                            .forEach(binding -> groupSpaceBindingStorage.deleteGroupBinding(binding.getId()));
    
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
   * @return an instance of space
   **/
  private GroupSpaceBinding getGroupSpaceBindingInstance(String spaceId, String group) {
    GroupSpaceBinding groupSpaceBinding = new GroupSpaceBinding();
    groupSpaceBinding.setSpaceId(spaceId);
    groupSpaceBinding.setGroup(group);
    return groupSpaceBinding;
  }
  
  
  /**
   * Gets an instance of GroupSpaceBindingReportAction.
   *
   **/
  private GroupSpaceBindingReportAction getGroupSpaceBindingReportActionInstance(long groupSpaceBindingId, String group,
                                                                     String action, long spaceId) {
    GroupSpaceBindingReportAction groupSpaceBindingReportAction = new GroupSpaceBindingReportAction();
    groupSpaceBindingReportAction.setSpaceId(spaceId);
    groupSpaceBindingReportAction.setGroup(group);
    groupSpaceBindingReportAction.setAction(action);
    groupSpaceBindingReportAction.setGroupSpaceBindingId(groupSpaceBindingId);
    return groupSpaceBindingReportAction;
  }
  
  /**
   * Gets an instance of GroupSpaceBindingReportUser.
   *
   **/
  private GroupSpaceBindingReportUser getGroupSpaceBindingReportUserInstance(String action,
                                                                             GroupSpaceBindingReportAction groupSpaceBindingReportAction,
                                                                             boolean stillInSpace, boolean wasPresentBefore,
                                                                             String username) {
    GroupSpaceBindingReportUser groupSpaceBindingReportUser = new GroupSpaceBindingReportUser();
    groupSpaceBindingReportUser.setAction(action);
    groupSpaceBindingReportUser.setGroupSpaceBindingReportAction(groupSpaceBindingReportAction);
    groupSpaceBindingReportUser.setDate(new Date());
    groupSpaceBindingReportUser.setStillInSpace(stillInSpace);
    groupSpaceBindingReportUser.setWasPresentBefore(wasPresentBefore);
    groupSpaceBindingReportUser.setUsername(username);
    return groupSpaceBindingReportUser;
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
   * @return an instance of space
   **/
  private UserSpaceBinding getUserBindingInstance(String userName, GroupSpaceBinding groupSpaceBinding) {
    UserSpaceBinding userSpaceBinding = new UserSpaceBinding();
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
      GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
      groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
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

    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    for (int i = 1; i <= totalBindings; i++) {
      UserSpaceBinding userSpaceBinding = this.getUserBindingInstance("john"+i, groupSpaceBinding);
      groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    }
    assertEquals("groupSpaceBindingStorage.findUserSpaceBindingsBySpace(" + spaceId + ",'john') must return: 1",
                 1,
                 groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, "john1").size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#saveGroupSpaceBinding}
   *
   * @throws Exception
   **/

  public void testSaveGroupBinding() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
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
    GroupSpaceBinding groupSpaceBinding = getGroupSpaceBindingInstance(spaceId,
                                                                       "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    GroupSpaceBindingQueue groupSpaceBindingQueue = new GroupSpaceBindingQueue(groupSpaceBinding,
                                                                               GroupSpaceBindingQueue.ACTION_CREATE);
    groupSpaceBindingQueue=groupSpaceBindingStorage.createGroupSpaceBindingQueue(groupSpaceBindingQueue);
    assertEquals("groupSpaceBindingStorage.findFirstGroupSpaceBindingQueue() must return after creation: " + 1,
                 groupSpaceBindingQueue.getId(),
                 groupSpaceBindingStorage.findFirstGroupSpaceBindingQueue().getId());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#saveUserBinding(UserSpaceBinding)}
   *
   * @throws Exception
   **/

  public void testSaveUserBinding() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance("john", groupSpaceBinding);
    userSpaceBinding=groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    assertEquals("groupSpaceBindingStorage.findUserBindingsbyMember(" + spaceId + ",'member') must return after creation: " + 1,
                 1,
                 groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, "john").size());
    assertEquals("Invalid group binding :" + userSpaceBinding.getGroupBinding().getGroup(),
                 userSpaceBinding.getGroupBinding().getGroup(),
                 "/platform/administrators");
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#deleteGroupBinding(long)}
   *
   * @throws Exception
   **/

  public void testDeleteGroupBinding() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
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
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance("john", groupSpaceBinding);
    userSpaceBinding=groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
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
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    UserSpaceBinding userSpaceBinding1 = this.getUserBindingInstance("john", groupSpaceBinding);
    groupSpaceBindingStorage.saveUserBinding(userSpaceBinding1);
    UserSpaceBinding userSpaceBinding2 = this.getUserBindingInstance("john", groupSpaceBinding);
    groupSpaceBindingStorage.saveUserBinding(userSpaceBinding2);
    UserSpaceBinding userSpaceBinding3 = this.getUserBindingInstance("mary", groupSpaceBinding);
    groupSpaceBindingStorage.saveUserBinding(userSpaceBinding3);
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
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance("john", groupSpaceBinding);
    groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
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
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance( "john", groupSpaceBinding);
    groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    assertEquals("groupSpaceBindingStorage.countUserBindings(" + spaceId + ",'john') must return 1 ",
                 1,
                 groupSpaceBindingStorage.countUserBindings(spaceId, "john"));
    assertEquals("groupSpaceBindingStorage.countUserBindings(" + spaceId + ",'mary') must return 0 ",
                 0,
                 groupSpaceBindingStorage.countUserBindings(spaceId, "mary"));
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#findUserAllBindingsByGroupBinding(GroupSpaceBinding)}
   *
   * @throws Exception
   **/

  public void testfindUserAllBindingsbyGroupMembership() throws Exception {
    int totalBindings = 5;

    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);

    GroupSpaceBinding groupSpaceBinding1 = this.getGroupSpaceBindingInstance(spaceId, "/platform/users");
    groupSpaceBinding1=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding1);

    for (int i = 1; i <= totalBindings; i++) {
      UserSpaceBinding userSpaceBinding = this.getUserBindingInstance("john"+i, groupSpaceBinding);
      groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    }
  
    for (int i = 1; i <= totalBindings; i++) {
      UserSpaceBinding userSpaceBinding = this.getUserBindingInstance("john"+i, groupSpaceBinding1);
      groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    }
  
    assertEquals(totalBindings,groupSpaceBindingStorage.findUserAllBindingsByGroupBinding(groupSpaceBinding).size());
    
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#findUserSpaceBindingsByGroup(String, String)}
   *
   * @throws Exception
   **/

  public void testfindUserSpaceBindingsByGroup() throws Exception {
    int totalBindings = 5;

    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);

    GroupSpaceBinding groupSpaceBinding1 = this.getGroupSpaceBindingInstance(spaceId, "/platform/users");
    groupSpaceBinding1=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding1);

    for (int i = 1; i <= totalBindings; i++) {
      UserSpaceBinding userSpaceBinding = this.getUserBindingInstance("john"+i, groupSpaceBinding);
      groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    }
    assertEquals("findUserAllBindingsbyGroupMembership('/platform/administrators','Any') must return: " + totalBindings,
                 1,
                 groupSpaceBindingStorage.findUserSpaceBindingsByGroup("/platform/administrators", "john1").size());

    assertEquals("findUserAllBindingsbyGroupMembership('/platform/administrators','Any') must return: " + 0,
                 0,
                 groupSpaceBindingStorage.findUserSpaceBindingsByGroup("/platform/users", "john1").size());
  }
  
  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#countUserBindings(String, String)}
   *
   * @throws Exception
   **/
  public void testCountUserBindingsBySpace() throws Exception {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    GroupSpaceBinding groupSpaceBinding2 = this.getGroupSpaceBindingInstance(spaceId, "/platform/developers");
    groupSpaceBinding2=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding2);
  
  
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance("john", groupSpaceBinding);
    groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    UserSpaceBinding userSpaceBinding2 = this.getUserBindingInstance("mary", groupSpaceBinding);
    groupSpaceBindingStorage.saveUserBinding(userSpaceBinding2);
    UserSpaceBinding userSpaceBinding3 = this.getUserBindingInstance("john", groupSpaceBinding2);
    groupSpaceBindingStorage.saveUserBinding(userSpaceBinding3);
    
    assertEquals("groupSpaceBindingStorage.countBoundUsers(" + spaceId + ") must return 2 ",
                 2,
                 groupSpaceBindingStorage.countBoundUsers(spaceId));
  }
  
  /**
   * Test
   * {@link GroupSpaceBindingStorage#findGroupSpaceBindingsByGroup(String)}
   *
   * @throws Exception
   **/
  public void testFindGroupSpaceBindingsByGroup() {
    int totalBindings = 5;
  
    for (int i = 1; i <= totalBindings; i++) {
      GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
      groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    }
    assertEquals(totalBindings,groupSpaceBindingStorage.findGroupSpaceBindingsByGroup("/platform/administrators").size());
  }
  
  /**
   * Test
   * {@link GroupSpaceBindingStorage#findUserSpaceBindingsByUser(String)}
   *
   * @throws Exception
   **/
  public void testFindUserSpaceBindingsByUser() {
    int totalBindings = 5;
  
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    for (int i = 1; i <= totalBindings; i++) {
      UserSpaceBinding userSpaceBinding = this.getUserBindingInstance("john"+i, groupSpaceBinding);
      groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    }
    for (int i = 1; i <= totalBindings; i++) {
      UserSpaceBinding userSpaceBinding = this.getUserBindingInstance("mary"+i, groupSpaceBinding);
      groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    }
    assertEquals(1,groupSpaceBindingStorage.findUserSpaceBindingsByUser("john1").size());
  }
  /**
   * Test
   * {@link GroupSpaceBindingStorage#findReportsForCsv(long, long, String, String)}
   * {@link org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage#saveGroupSpaceBindingReport(GroupSpaceBindingReportAction)}
   *
   *
   * @throws Exception
   **/
  public void testfindGroupSpaceBindingReportsForCSV() throws Exception {
    
    //TODO
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    GroupSpaceBindingReportAction createActionReport = getGroupSpaceBindingReportActionInstance(groupSpaceBinding.getId(),
                                                                                                groupSpaceBinding.getGroup(),
                                                                                                GroupSpaceBindingReportAction.ADD_ACTION,
                                                                                                Long.parseLong(spaceId));
    createActionReport=groupSpaceBindingStorage.saveGroupSpaceBindingReport(createActionReport);
    int totalReports=5;
    for (int i=1;i<=totalReports;i++) {
    
      GroupSpaceBindingReportUser reportUser = getGroupSpaceBindingReportUserInstance(GroupSpaceBindingReportUser.ACTION_ADD_USER,
                                                                                      createActionReport,
                                                                                      false,
                                                                                      false, "user"+i);
      groupSpaceBindingStorage.saveGroupSpaceBindingReportUser(reportUser);
    
    }
    
    Space space2 = this.getSpaceInstance(2);
    spaceStorage.saveSpace(space2, true);
    space2 = spaceStorage.getSpaceByPrettyName(space2.getPrettyName());
    GroupSpaceBinding groupSpaceBinding2 = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding2 = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding2);
    GroupSpaceBindingReportAction createActionReport2 = getGroupSpaceBindingReportActionInstance(groupSpaceBinding2.getId(),
                                                                                                groupSpaceBinding2.getGroup(),
                                                                                                GroupSpaceBindingReportAction.ADD_ACTION,
                                                                                                Long.parseLong(space2.getId()));
    createActionReport2=groupSpaceBindingStorage.saveGroupSpaceBindingReport(createActionReport2);
    for (int i=1;i<=totalReports;i++) {
    
      GroupSpaceBindingReportUser reportUser = getGroupSpaceBindingReportUserInstance(GroupSpaceBindingReportUser.ACTION_ADD_USER,
                                                                                      createActionReport2,
                                                                                      false,
                                                                                      false, "user"+i);
      groupSpaceBindingStorage.saveGroupSpaceBindingReportUser(reportUser);
    
    }
    
    assertEquals(totalReports,
                 groupSpaceBindingStorage.findReportsForCsv(Long.parseLong(spaceId),groupSpaceBinding.getId(),"/platform"
                                                                + "/administrators",
                                                            GroupSpaceBindingReportAction.ADD_ACTION).size());

  }
  
  /**
   * Test
   * {@link GroupSpaceBindingStorage#saveUserBinding(UserSpaceBinding)} ()}
   *
   *
   * @throws Exception
   **/
  public void testSaveSameUserBinding() {
  
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    UserSpaceBinding userSpaceBinding = this.getUserBindingInstance("john", groupSpaceBinding);
    userSpaceBinding = groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    UserSpaceBinding userSpaceBinding2 = this.getUserBindingInstance("john", groupSpaceBinding);
    userSpaceBinding2=groupSpaceBindingStorage.saveUserBinding(userSpaceBinding2);
    assertEquals(1,groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, "john").size());
    assertTrue(userSpaceBinding.getId()==userSpaceBinding2.getId());
    
  }
  
  /**
   * Test
   * {@link GroupSpaceBindingStorage#getGroupSpaceBindingReportOperations()}
   *
   *
   * @throws Exception
   **/
  public void testGetGroupSpaceBindingReportOperations() {
    
    //given
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    //create reports
    GroupSpaceBindingReportAction createActionReport = getGroupSpaceBindingReportActionInstance(groupSpaceBinding.getId(),
                                                                                                groupSpaceBinding.getGroup(),
                                                                                                GroupSpaceBindingReportAction.ADD_ACTION,
                                                                                                Long.parseLong(spaceId));
  
    createActionReport=groupSpaceBindingStorage.saveGroupSpaceBindingReport(createActionReport);
    int totalReports=5;
    for (int i=1;i<=totalReports;i++) {
  
      GroupSpaceBindingReportUser reportUser = getGroupSpaceBindingReportUserInstance(GroupSpaceBindingReportUser.ACTION_ADD_USER,
                                                                                       createActionReport,
                                                                                       false,
                                                                                       false, "user"+i);
      groupSpaceBindingStorage.saveGroupSpaceBindingReportUser(reportUser);
  
    }
    GroupSpaceBindingReportAction createSyncReport = getGroupSpaceBindingReportActionInstance(1L,"/platform/administrators"
        ,GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION,Long.parseLong(spaceId));
  
    createSyncReport=groupSpaceBindingStorage.saveGroupSpaceBindingReport(createSyncReport);
    for (int i=1;i<=totalReports;i++) {
    
      GroupSpaceBindingReportUser reportUser = getGroupSpaceBindingReportUserInstance(GroupSpaceBindingReportUser.ACTION_ADD_USER,
                                                                                      createSyncReport,
                                                                                      false,
                                                                                      false, "user"+i);
      groupSpaceBindingStorage.saveGroupSpaceBindingReportUser(reportUser);
    }
    for (int i=1;i<=totalReports;i++) {
    
      GroupSpaceBindingReportUser reportUser = getGroupSpaceBindingReportUserInstance(GroupSpaceBindingReportUser.ACTION_REMOVE_USER,
                                                                                      createSyncReport,
                                                                                      false,
                                                                                      false, "user"+i);
      groupSpaceBindingStorage.saveGroupSpaceBindingReportUser(reportUser);
    }
  
    GroupSpaceBindingReportAction createRemoveReport = getGroupSpaceBindingReportActionInstance(1L,"/platform"
                                                                                                        + "/administrators"
        ,GroupSpaceBindingReportAction.REMOVE_ACTION,Long.parseLong(spaceId));
  
    createRemoveReport=groupSpaceBindingStorage.saveGroupSpaceBindingReport(createRemoveReport);
    for (int i=1;i<=totalReports;i++) {
    
      GroupSpaceBindingReportUser reportUser = getGroupSpaceBindingReportUserInstance(GroupSpaceBindingReportUser.ACTION_REMOVE_USER,
                                                                                      createRemoveReport,
                                                                                      false,
                                                                                      false, "user"+i);
      groupSpaceBindingStorage.saveGroupSpaceBindingReportUser(reportUser);
    
    }
  
  
    //when
    List<GroupSpaceBindingOperationReport> reports=groupSpaceBindingStorage.getGroupSpaceBindingReportOperations();
    
    //then
    assertEquals(5,
                 reports.stream().filter(groupSpaceBindingOperationReport -> groupSpaceBindingOperationReport.getAction().equals(GroupSpaceBindingReportAction.ADD_ACTION)).findFirst().get().getAddedUsers());
    assertEquals(0,
                 reports.stream().filter(groupSpaceBindingOperationReport -> groupSpaceBindingOperationReport.getAction().equals(GroupSpaceBindingReportAction.ADD_ACTION)).findFirst().get().getRemovedUsers());
    assertEquals(5,
                 reports.stream().filter(groupSpaceBindingOperationReport -> groupSpaceBindingOperationReport.getAction().equals(GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION)).findFirst().get().getAddedUsers());
    assertEquals(5,
                 reports.stream().filter(groupSpaceBindingOperationReport -> groupSpaceBindingOperationReport.getAction().equals(GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION)).findFirst().get().getRemovedUsers());
    assertEquals(0,
                 reports.stream().filter(groupSpaceBindingOperationReport -> groupSpaceBindingOperationReport.getAction().equals(GroupSpaceBindingReportAction.REMOVE_ACTION)).findFirst().get().getAddedUsers());
    assertEquals(5,
                 reports.stream().filter(groupSpaceBindingOperationReport -> groupSpaceBindingOperationReport.getAction().equals(GroupSpaceBindingReportAction.REMOVE_ACTION)).findFirst().get().getRemovedUsers());
  
  }
  
  /**
   * Test
   * {@link GroupSpaceBindingStorage#getGroupSpaceBindingsFromQueueByAction(String)}
   *
   *
   * @throws Exception
   **/
  public void testGetGroupSpaceBindingsFromQueueByAction() {
    GroupSpaceBinding groupSpaceBinding = getGroupSpaceBindingInstance(spaceId,
                                                                       "/platform/administrators");
    groupSpaceBinding=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    GroupSpaceBindingQueue groupSpaceBindingQueue = new GroupSpaceBindingQueue(groupSpaceBinding,
                                                                               GroupSpaceBindingQueue.ACTION_CREATE);
    groupSpaceBindingStorage.createGroupSpaceBindingQueue(groupSpaceBindingQueue);
  
    GroupSpaceBinding groupSpaceBinding2 = getGroupSpaceBindingInstance(spaceId,
                                                                       "/platform/administrators");
    groupSpaceBinding2=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding2);
    GroupSpaceBindingQueue groupSpaceBindingQueue2 = new GroupSpaceBindingQueue(groupSpaceBinding2,
                                                                               GroupSpaceBindingQueue.ACTION_CREATE);
    groupSpaceBindingStorage.createGroupSpaceBindingQueue(groupSpaceBindingQueue2);
  
    GroupSpaceBinding groupSpaceBinding3 = getGroupSpaceBindingInstance(spaceId,
                                                                       "/platform/administrators");
    groupSpaceBinding3=groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding3);
    GroupSpaceBindingQueue groupSpaceBindingQueue3 = new GroupSpaceBindingQueue(groupSpaceBinding3,
                                                                               GroupSpaceBindingQueue.ACTION_REMOVE);
    groupSpaceBindingStorage.createGroupSpaceBindingQueue(groupSpaceBindingQueue3);
    
    assertEquals(1,groupSpaceBindingStorage.getGroupSpaceBindingsFromQueueByAction(GroupSpaceBindingQueue.ACTION_REMOVE).size());
    assertEquals(2,groupSpaceBindingStorage.getGroupSpaceBindingsFromQueueByAction(GroupSpaceBindingQueue.ACTION_CREATE).size());
  }
  
  /**
   * Test
   * {@link GroupSpaceBindingStorage#saveGroupSpaceBindingReport(GroupSpaceBindingReportAction)}
   * {@link GroupSpaceBindingStorage#findGroupSpaceBindingReportAction(long, String)}
   *
   *
   * @throws Exception
   **/
  public void testSaveGroupSpaceBindingReport() {
  
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    
    GroupSpaceBindingReportAction createActionReport = getGroupSpaceBindingReportActionInstance(groupSpaceBinding.getId(),"/platform"
                                                                                                    + "/administrators"
        ,GroupSpaceBindingReportAction.ADD_ACTION,Long.parseLong(spaceId));
  
    groupSpaceBindingStorage.saveGroupSpaceBindingReport(createActionReport);
    assertNotNull(groupSpaceBindingStorage.findGroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                             GroupSpaceBindingReportAction.ADD_ACTION));
  }
  
  /**
   * Test
   * {@link GroupSpaceBindingStorage#updateGroupSpaceBindingReportAction(GroupSpaceBindingReportAction)}
   *
   *
   *
   * @throws Exception
   **/
  public void testUpdateGroupSpaceBindingReportAction() {
    GroupSpaceBinding groupSpaceBinding = this.getGroupSpaceBindingInstance(spaceId, "/platform/administrators");
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
  
    GroupSpaceBindingReportAction createActionReport = getGroupSpaceBindingReportActionInstance(groupSpaceBinding.getId(),"/platform"
                                                                                                    + "/administrators"
        ,GroupSpaceBindingReportAction.ADD_ACTION,Long.parseLong(spaceId));
  
    groupSpaceBindingStorage.saveGroupSpaceBindingReport(createActionReport);
    
    Date endDate = new Date();
    GroupSpaceBindingReportAction actionReport =
        groupSpaceBindingStorage.findGroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                   GroupSpaceBindingReportAction.ADD_ACTION);
    actionReport.setEndDate(endDate);
    groupSpaceBindingStorage.updateGroupSpaceBindingReportAction(actionReport);
  
    GroupSpaceBindingReportAction resultActionReport =
        groupSpaceBindingStorage.findGroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                   GroupSpaceBindingReportAction.ADD_ACTION);
    assertEquals(0,resultActionReport.getEndDate().compareTo(endDate));
    
    
  }
}
