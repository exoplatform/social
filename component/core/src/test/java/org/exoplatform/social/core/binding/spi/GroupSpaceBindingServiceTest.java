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
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.idm.UserDAOImpl;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.social.core.binding.impl.GroupSpaceBindingServiceImpl;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingQueue;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

@RunWith(MockitoJUnitRunner.class)
public class GroupSpaceBindingServiceTest extends AbstractCoreTest {

  @Mock
  private GroupSpaceBindingStorage groupSpaceBindingStorage;

  @Mock
  private InitParams               initParams;

  @Mock
  private SpaceService             spaceService;

  @Mock
  private OrganizationService      orgService;

  @Mock
  private MembershipHandler        membershipHandler;

  /**
   * Test
   * {@link GroupSpaceBindingService#findGroupSpaceBindingsBySpace(String spaceId)}
   *
   * @throws Exception
   */
  @Test
  public void testFindSpaceBindings() throws Exception {
    // Given
    List<GroupSpaceBinding> groupSpaceBindings = new LinkedList<>();
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");
    groupSpaceBindings.add(binding1);

    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding2.setId(2);
    binding2.setGroup("/platform/web-contributors");
    binding2.setSpaceId("1");
    groupSpaceBindings.add(binding2);

    Mockito.when(groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(Mockito.eq("1"))).thenReturn(groupSpaceBindings);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    List<GroupSpaceBinding> results = groupSpaceBindingService.findGroupSpaceBindingsBySpace("1");
    GroupSpaceBinding result1 = results.get(0);
    GroupSpaceBinding result2 = results.get(1);

    // Then
    assertEquals(2, results.size());

    assertEquals(1, result1.getId());
    assertEquals("/platform/administrators", result1.getGroup());
    assertEquals("1", result1.getSpaceId());

    assertEquals(2, result2.getId());
    assertEquals("/platform/web-contributors", result2.getGroup());
    assertEquals("1", result2.getSpaceId());
  }

  /**
   * Test
   * {@link GroupSpaceBindingService#findUserSpaceBindingsBySpace(String, String)}
   *
   * @throws Exception
   */
  @Test
  public void testFindUserBindings() throws Exception {
    // Given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding2.setId(1);
    binding2.setGroup("/platform/users");
    binding2.setSpaceId("1");

    List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
    UserSpaceBinding ub1 = new UserSpaceBinding();
    ub1.setId(1);
    ub1.setGroupBinding(binding1);
    ub1.setUser("john");
    userSpaceBindings.add(ub1);

    UserSpaceBinding ub2 = new UserSpaceBinding();
    ub2.setId(2);
    ub2.setGroupBinding(binding2);
    ub2.setUser("john");
    userSpaceBindings.add(ub2);

    Mockito.when(groupSpaceBindingStorage.findUserSpaceBindingsBySpace(Mockito.eq("1"), Mockito.eq("john")))
           .thenReturn(userSpaceBindings);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    List<UserSpaceBinding> results = groupSpaceBindingService.findUserSpaceBindingsBySpace("1", "john");
    UserSpaceBinding result1 = results.get(0);
    UserSpaceBinding result2 = results.get(1);

    // Then
    assertEquals(2, results.size());

    assertEquals(1, result1.getId());
    assertEquals("/platform/administrators", result1.getGroupBinding().getGroup());

    assertEquals(2, result2.getId());
    assertEquals("/platform/users", result2.getGroupBinding().getGroup());
  }

  /**
   * Test
   * {@link GroupSpaceBindingService#deleteUserBindingAndSpaceMembership(UserSpaceBinding)}
   *
   * @throws Exception
   */
  @Test
  public void deleteUserBinding() throws Exception {
    // Given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
    UserSpaceBinding ub1 = new UserSpaceBinding();
    ub1.setId(1);
    ub1.setIsMemberBefore(false);
    ub1.setGroupBinding(binding1);
    ub1.setUser("john");
    userSpaceBindings.add(ub1);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    groupSpaceBindingService.deleteUserBindingAndSpaceMembership(ub1);

    // Then
    ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).deleteUserBinding(idCaptor.capture());
    long id = idCaptor.getValue();
    assertEquals(1, id);
  }

  /**
   * Test
   * {@link GroupSpaceBindingService#deleteGroupSpaceBinding(GroupSpaceBinding)}
   *
   * @throws Exception
   */
  @Test
  public void deleteSpaceBinding() throws Exception {
    // Given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");
  
    Space space = new Space();
    space.setId("1");
    space.setDisplayName("space1");
    space.setPrettyName("space1");
    space.setMembers(new String[] {"root"});
    Mockito.when(spaceService.getSpaceById(Mockito.any())).thenReturn(space);
    Mockito.when(groupSpaceBindingStorage.countBoundUsers(Mockito.any())).thenReturn(0L);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    groupSpaceBindingService.deleteGroupSpaceBinding(binding1);

    // Then
    ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).deleteGroupBinding(idCaptor.capture());
    long id = idCaptor.getValue();
    assertEquals(1, id);
  }

  /**
   * Test
   * {@link GroupSpaceBindingService#deleteAllSpaceBindingsBySpace(String spaceId)}
   *
   * @throws Exception
   */
  @Test
  public void deleteAllSpaceBindings() throws Exception {
    // Given
    List<GroupSpaceBinding> groupSpaceBindings = new LinkedList<>();
    List<GroupSpaceBinding> resultSpaceBindings = new LinkedList<>();
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");
    groupSpaceBindings.add(binding1);
    resultSpaceBindings.add(binding1);

    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding2.setId(2);
    binding2.setGroup("/platform/web-contributors");
    binding2.setSpaceId("2");
    groupSpaceBindings.add(binding2);

    GroupSpaceBinding binding3 = new GroupSpaceBinding();
    binding3.setId(3);
    binding3.setGroup("/platform/web-contributors");
    binding3.setSpaceId("1");
    groupSpaceBindings.add(binding3);

    GroupSpaceBinding binding4 = new GroupSpaceBinding();
    binding4.setId(4);
    binding4.setGroup("/platform/web-contributors");
    binding4.setSpaceId("1");
    groupSpaceBindings.add(binding4);
    resultSpaceBindings.add(binding4);
    Mockito.when(groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(Mockito.eq("1"))).thenReturn(resultSpaceBindings);
    
    Space space = new Space();
    space.setId("1");
    space.setDisplayName("space1");
    space.setPrettyName("space1");
    space.setMembers(new String[] {"root"});
    Mockito.when(spaceService.getSpaceById(Mockito.any())).thenReturn(space);
    Mockito.when(groupSpaceBindingStorage.countBoundUsers(Mockito.any())).thenReturn(0L);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    groupSpaceBindingService.deleteAllSpaceBindingsBySpace("1");

    // Then
    ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(2)).deleteGroupBinding(idCaptor.capture());
    long id = idCaptor.getValue();
    assertTrue(id == 1 || id == 4);
  }

  /**
   * Test {@link GroupSpaceBindingService#saveSpaceBindings(String
   * spaceId,List<GroupSpaceBinding> GroupSpaceBinding)}
   *
   * @throws Exception
   */
  /*
   * @Test To fix public void saveSpaceBindings() throws Exception { // Given
   * IdentityStorage identityStorage = (IdentityStorage)
   * getContainer().getComponentInstanceOfType(IdentityStorage.class);
   * spaceService =
   * ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
   * SpaceService.class); Space space = this.getSpaceInstance(1);
   * spaceService.saveSpace(space, true); String spaceId =
   * spaceService.getSpaceByPrettyName("myspacetestbinding1").getId();
   * List<GroupSpaceBinding> groupSpaceBindings = new LinkedList<>();
   * GroupSpaceBinding binding1 = new GroupSpaceBinding(); binding1.setId(1);
   * binding1.setGroupRole("any"); binding1.setSpaceRole("member");
   * binding1.setGroup("/platform/administrators"); binding1.setSpaceId(spaceId);
   * groupSpaceBindings.add(binding1); GroupSpaceBinding binding2 = new
   * GroupSpaceBinding(); binding2.setId(2); binding2.setGroupRole("any");
   * binding2.setSpaceRole("member"); binding2.setGroup("/platform/users");
   * binding2.setSpaceId(spaceId); groupSpaceBindings.add(binding2);
   * GroupSpaceBinding binding3 = new GroupSpaceBinding(); binding3.setId(3);
   * binding3.setGroupRole("any"); binding3.setSpaceRole("member");
   * binding3.setGroup("/platform/users"); binding3.setSpaceId(spaceId);
   * groupSpaceBindings.add(binding3); OrganizationService organisationService =
   * ExoContainerContext.getCurrentContainer()
   * .getComponentInstanceOfType(OrganizationService.class); Identity john; john =
   * new Identity(OrganizationIdentityProvider.NAME, "john");
   * identityStorage.saveIdentity(john); List<UserSpaceBinding>
   * userSpaceBindingsTemp = new LinkedList<>();
   * Mockito.when(groupSpaceBindingStorage.findUserSpaceBindingsBySpace(Mockito.eq
   * (spaceId), Mockito.eq("john"))) .thenReturn(userSpaceBindingsTemp); // When
   * GroupSpaceBindingService groupSpaceBindingService = new
   * GroupSpaceBindingServiceImpl(initParams, groupSpaceBindingStorage,
   * organisationService, spaceService);
   * groupSpaceBindingService.saveSpaceBindings(spaceId, groupSpaceBindings); //
   * Then Mockito.verify(groupSpaceBindingStorage,
   * Mockito.times(1)).saveGroupBinding(binding1, true);
   * Mockito.verify(groupSpaceBindingStorage,
   * Mockito.times(1)).saveGroupBinding(binding2, true);
   * Mockito.verify(groupSpaceBindingStorage,
   * Mockito.times(1)).saveGroupBinding(binding3, true); }
   */

  /**
   * Test {@link GroupSpaceBindingService#saveUserBindings(List)}
   *
   * @throws Exception
   */
  @Test
  public void saveUserBindings() throws Exception {
    // Given
    List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    UserSpaceBinding ub1 = new UserSpaceBinding();
    ub1.setId(1);
    ub1.setGroupBinding(binding1);
    ub1.setUser("john");
    userSpaceBindings.add(ub1);

    // When
    Mockito.when(orgService.getMembershipHandler()).thenReturn(membershipHandler);

    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    groupSpaceBindingService.saveUserBindings(userSpaceBindings);

    // Then
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveUserBinding(ub1);
  }

  /**
   * Test
   * {@link GroupSpaceBindingService#findUserSpaceBindingsBySpace(String, String)}
   *
   * @throws Exception
   */
  @Test
  public void hasUserBindings() throws Exception {
    // Given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    List<UserSpaceBinding> userSpaceBindings = new ArrayList<>();
    userSpaceBindings.add(new UserSpaceBinding());
    Mockito.when(groupSpaceBindingStorage.findUserSpaceBindingsBySpace(Mockito.eq("1"), Mockito.eq("john")))
           .thenReturn(userSpaceBindings);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);

    // Then
    assertEquals(true, groupSpaceBindingService.findUserSpaceBindingsBySpace("1", "john").size() > 0);
  }

  /**
   * Test {@link GroupSpaceBindingService#countUserBindings(String, String)}
   *
   * @throws Exception
   */
  @Test
  public void countUserBindings() throws Exception {
    // Given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    List<UserSpaceBinding> userSpaceBindings = new ArrayList<>();
    userSpaceBindings.add(new UserSpaceBinding());
    Mockito.when(groupSpaceBindingStorage.countUserBindings(Mockito.eq("1"), Mockito.eq("john"))).thenReturn(1L);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);

    // Then
    assertEquals(1, groupSpaceBindingService.countUserBindings("1", "john"));
  }

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
   * Test {@link GroupSpaceBindingService#findFirstGroupSpaceBindingQueue()} ()}
   *
   * @throws Exception
   */
  @Test
  public void testFindFirstGroupSpaceBindingQueue() throws Exception {
    // Given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("2");

    GroupSpaceBindingQueue bindingQueue1 = new GroupSpaceBindingQueue();
    bindingQueue1.setId(1);
    bindingQueue1.setGroupSpaceBinding(binding1);
    bindingQueue1.setAction(GroupSpaceBindingQueue.ACTION_CREATE);

    GroupSpaceBindingQueue bindingQueue2 = new GroupSpaceBindingQueue();
    bindingQueue2.setId(2);
    bindingQueue2.setGroupSpaceBinding(binding2);
    bindingQueue2.setAction(GroupSpaceBindingQueue.ACTION_CREATE);

    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    groupSpaceBindingService.createGroupSpaceBindingQueue(bindingQueue1);
    groupSpaceBindingService.createGroupSpaceBindingQueue(bindingQueue2);

    Mockito.when(groupSpaceBindingStorage.findFirstGroupSpaceBindingQueue()).thenReturn(bindingQueue1, bindingQueue2);

    // When
    GroupSpaceBindingQueue firstGroupSpaceBindingQueue = groupSpaceBindingService.findFirstGroupSpaceBindingQueue();
    assertNotNull(firstGroupSpaceBindingQueue);
    assertEquals(1, firstGroupSpaceBindingQueue.getId());
    GroupSpaceBindingQueue secondGroupSpaceBindingQueue = groupSpaceBindingService.findFirstGroupSpaceBindingQueue();
    assertNotNull(secondGroupSpaceBindingQueue);
    assertEquals(2, secondGroupSpaceBindingQueue.getId());
  }

  @Test
  public void testBindUsersFromGroupSpaceBindingCheckUserNotMemberAndNotBoundBefore() throws Exception {

    // 1) User is not member and not bound before
    // ==> check that he is not memberbefore

    // given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    Space space1 = new Space();
    space1.setPrettyName("space1");
    space1.setId("1");
    space1.setGroupId("/spaces/space1");
    space1.setMembers(new String[] { "root" });

    Mockito.when(spaceService.getSpaceById("1")).thenReturn(space1);

    User user1 = new UserImpl("user1");
    user1.setFirstName("user1");
    user1.setLastName("user1");
    user1.setEmail("user1@acme.com");

    ListAccess<User> userListAccess = new ListAccess<User>() {
      public User[] load(int index, int length) throws Exception {
        List<User> users = new ArrayList();
        users.add(user1);
        User[] result = new User[users.size()];
        return users.toArray(result);
      }

      public int getSize() throws Exception {
        return 1;
      }
    };

    UserDAOImpl userDAO = Mockito.mock(UserDAOImpl.class);
    Mockito.when(userDAO.findUsersByGroupId("/platform/administrators")).thenReturn(userListAccess);
    Mockito.when(orgService.getUserHandler()).thenReturn(userDAO);
    Mockito.when(groupSpaceBindingStorage.isUserBoundAndMemberBefore(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);

    // when
    groupSpaceBindingService.bindUsersFromGroupSpaceBinding(binding1);

    // then
    ArgumentCaptor<UserSpaceBinding> argument = ArgumentCaptor.forClass(UserSpaceBinding.class);
    Mockito.verify(groupSpaceBindingStorage).saveUserBinding(argument.capture());
    assertEquals(false, argument.getValue().isMemberBefore().booleanValue());

  }

  @Test
  public void testBindUsersFromGroupSpaceBindingCheckUserIsMemberAndNotBound() throws Exception {
    // 2) a user is member and not binded
    // check memberbefore=true
    // given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    Space space1 = new Space();
    space1.setPrettyName("space1");
    space1.setId("1");
    space1.setGroupId("/spaces/space1");
    space1.setMembers(new String[] { "user1" });

    Mockito.when(spaceService.getSpaceById("1")).thenReturn(space1);

    User user1 = new UserImpl("user1");
    user1.setFirstName("user1");
    user1.setLastName("user1");
    user1.setEmail("user1@acme.com");

    ListAccess<User> userListAccess = new ListAccess<User>() {
      public User[] load(int index, int length) throws Exception {
        List<User> users = new ArrayList();
        users.add(user1);
        User[] result = new User[users.size()];
        return users.toArray(result);
      }

      public int getSize() throws Exception {
        return 1;
      }
    };

    UserDAOImpl userDAO = Mockito.mock(UserDAOImpl.class);
    Mockito.when(userDAO.findUsersByGroupId("/platform/administrators")).thenReturn(userListAccess);
    Mockito.when(orgService.getUserHandler()).thenReturn(userDAO);
    Mockito.when(groupSpaceBindingStorage.isUserBoundAndMemberBefore(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
    Mockito.when(groupSpaceBindingStorage.countUserBindings(Mockito.anyString(), Mockito.anyString())).thenReturn(0L);
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    // when
    groupSpaceBindingService.bindUsersFromGroupSpaceBinding(binding1);

    // then
    ArgumentCaptor<UserSpaceBinding> argument = ArgumentCaptor.forClass(UserSpaceBinding.class);
    Mockito.verify(groupSpaceBindingStorage).saveUserBinding(argument.capture());
    assertEquals(true, argument.getValue().isMemberBefore().booleanValue());

  }

  @Test
  public void testBindUsersFromGroupSpaceBindingCheckUserIsBoundButNotMemberBefore() throws Exception {
    // 3) a user is bound to the space, but for this userBinding,
    // isMemberBefore=false
    // => isMemberBefore should be false

    // 4) un user member et bindé , mais pour lequel le binding est
    // memberbefore=true => verifier que memberbefore=true

    // given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    Space space1 = new Space();
    space1.setPrettyName("space1");
    space1.setId("1");
    space1.setGroupId("/spaces/space1");
    space1.setMembers(new String[] { "user1" });

    Mockito.when(spaceService.getSpaceById("1")).thenReturn(space1);

    User user1 = new UserImpl("user1");
    user1.setFirstName("user1");
    user1.setLastName("user1");
    user1.setEmail("user1@acme.com");

    ListAccess<User> userListAccess = new ListAccess<User>() {
      public User[] load(int index, int length) throws Exception {
        List<User> users = new ArrayList();
        users.add(user1);
        User[] result = new User[users.size()];
        return users.toArray(result);
      }

      public int getSize() throws Exception {
        return 1;
      }
    };

    UserDAOImpl userDAO = Mockito.mock(UserDAOImpl.class);
    Mockito.when(userDAO.findUsersByGroupId("/platform/administrators")).thenReturn(userListAccess);
    Mockito.when(orgService.getUserHandler()).thenReturn(userDAO);
    Mockito.when(groupSpaceBindingStorage.isUserBoundAndMemberBefore(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
    Mockito.when(groupSpaceBindingStorage.countUserBindings(Mockito.anyString(), Mockito.anyString())).thenReturn(1L);

    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    // when
    groupSpaceBindingService.bindUsersFromGroupSpaceBinding(binding1);

    // then
    ArgumentCaptor<UserSpaceBinding> argument = ArgumentCaptor.forClass(UserSpaceBinding.class);
    Mockito.verify(groupSpaceBindingStorage).saveUserBinding(argument.capture());
    assertEquals(false, argument.getValue().isMemberBefore().booleanValue());

  }

  @Test
  public void testBindUsersFromGroupSpaceBindingCheckUserIsBoundButAndMemberBefore() throws Exception {
    // 4) a user is bound to the space, and for this userBinding,
    // isMemberBefore=true
    // => isMemberBefore should be true

    // given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    Space space1 = new Space();
    space1.setPrettyName("space1");
    space1.setId("1");
    space1.setGroupId("/spaces/space1");
    space1.setMembers(new String[] { "user1" });

    Mockito.when(spaceService.getSpaceById("1")).thenReturn(space1);

    User user1 = new UserImpl("user1");
    user1.setFirstName("user1");
    user1.setLastName("user1");
    user1.setEmail("user1@acme.com");

    ListAccess<User> userListAccess = new ListAccess<User>() {
      public User[] load(int index, int length) throws Exception {
        List<User> users = new ArrayList();
        users.add(user1);
        User[] result = new User[users.size()];
        return users.toArray(result);
      }

      public int getSize() throws Exception {
        return 1;
      }
    };

    UserDAOImpl userDAO = Mockito.mock(UserDAOImpl.class);
    Mockito.when(userDAO.findUsersByGroupId("/platform/administrators")).thenReturn(userListAccess);
    Mockito.when(orgService.getUserHandler()).thenReturn(userDAO);
    Mockito.when(groupSpaceBindingStorage.isUserBoundAndMemberBefore(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    // when
    groupSpaceBindingService.bindUsersFromGroupSpaceBinding(binding1);

    // then
    ArgumentCaptor<UserSpaceBinding> argument = ArgumentCaptor.forClass(UserSpaceBinding.class);
    Mockito.verify(groupSpaceBindingStorage).saveUserBinding(argument.capture());
    assertEquals(true, argument.getValue().isMemberBefore().booleanValue());

  }

}
