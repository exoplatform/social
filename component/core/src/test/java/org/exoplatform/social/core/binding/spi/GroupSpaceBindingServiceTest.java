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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.social.core.binding.model.GroupSpaceBindingReportUser;
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
import org.exoplatform.social.core.binding.model.GroupSpaceBindingReportAction;
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
   * {@link GroupSpaceBindingService#deleteUserBinding(UserSpaceBinding, GroupSpaceBindingReportAction)}
   * (UserSpaceBinding)}
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

    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding2.setId(2);
    binding2.setGroup("/platform/developers");
    binding2.setSpaceId("1");

    List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
    UserSpaceBinding ub1 = new UserSpaceBinding();
    ub1.setId(1);
    ub1.setIsMemberBefore(false);
    ub1.setGroupBinding(binding1);
    ub1.setUser("john");
    userSpaceBindings.add(ub1);

    UserSpaceBinding ub2 = new UserSpaceBinding();
    ub2.setId(2);
    ub2.setIsMemberBefore(false);
    ub2.setGroupBinding(binding2);
    ub2.setUser("john");
    userSpaceBindings.add(ub2);

    Mockito.when(groupSpaceBindingStorage.findUserSpaceBindingsBySpace("1", "john"))
           .thenReturn(Arrays.asList(ub2), new ArrayList<>());

    // When remove first binding
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);

    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(ub1.getGroupBinding().getId(),
                                                                             Long.parseLong(ub1.getGroupBinding().getSpaceId()),
                                                                             ub1.getGroupBinding().getGroup(),
                                                                             GroupSpaceBindingReportAction.REMOVE_ACTION);
    groupSpaceBindingService.deleteUserBinding(ub1, report);

    // Then
    ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
    Mockito.verify(groupSpaceBindingStorage).deleteUserBinding(idCaptor.capture());
    long id = idCaptor.getValue();
    assertEquals(1, id);

    ArgumentCaptor<GroupSpaceBindingReportUser> reportCaptur = ArgumentCaptor.forClass(GroupSpaceBindingReportUser.class);
    Mockito.verify(groupSpaceBindingStorage).saveGroupSpaceBindingReportUser(reportCaptur.capture());
    GroupSpaceBindingReportUser capturedReport = reportCaptur.getValue();
    assertEquals(GroupSpaceBindingReportUser.ACTION_REMOVE_USER, capturedReport.getAction());
    assertEquals(true,capturedReport.isStillInSpace());
    assertEquals(false,capturedReport.isWasPresentBefore());

    // When remove second binding
    GroupSpaceBindingReportAction report1 = new GroupSpaceBindingReportAction(ub2.getGroupBinding().getId(),
                                                                              Long.parseLong(ub2.getGroupBinding().getSpaceId()),
                                                                              ub2.getGroupBinding().getGroup(),
                                                                              GroupSpaceBindingReportAction.REMOVE_ACTION);
    groupSpaceBindingService.deleteUserBinding(ub2, report1);
    // Then
    ArgumentCaptor<Long> idCaptor2 = ArgumentCaptor.forClass(Long.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(2)).deleteUserBinding(idCaptor2.capture());
    long id2 = idCaptor2.getValue();
    assertEquals(2, id2);

    ArgumentCaptor<GroupSpaceBindingReportUser> reportCaptur2 = ArgumentCaptor.forClass(GroupSpaceBindingReportUser.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(2)).saveGroupSpaceBindingReportUser(reportCaptur2.capture());
    GroupSpaceBindingReportUser capturedReport2 = reportCaptur2.getValue();
    assertEquals(GroupSpaceBindingReportUser.ACTION_REMOVE_USER, capturedReport2.getAction());
    assertEquals(false, capturedReport2.isStillInSpace());
    assertEquals(false, capturedReport2.isWasPresentBefore());
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
    space.setMembers(new String[] { "root" });
    Mockito.when(spaceService.getSpaceById(Mockito.any())).thenReturn(space);
    Mockito.when(groupSpaceBindingStorage.countBoundUsers(Mockito.any())).thenReturn(0L);
    
    
    UserSpaceBinding userSpaceBinding1 = new UserSpaceBinding();
    userSpaceBinding1.setId(1);
    userSpaceBinding1.setGroupBinding(binding1);
    userSpaceBinding1.setUser("user1");
    userSpaceBinding1.setIsMemberBefore(false);
  
    UserSpaceBinding userSpaceBinding2 = new UserSpaceBinding();
    userSpaceBinding2.setId(2);
    userSpaceBinding2.setGroupBinding(binding1);
    userSpaceBinding2.setIsMemberBefore(false);
    userSpaceBinding2.setUser("user2");
    
    List<UserSpaceBinding> userSpaceBindings = new ArrayList<>();
    userSpaceBindings.add(userSpaceBinding1);
    userSpaceBindings.add(userSpaceBinding2);
    Mockito.when(groupSpaceBindingStorage.findUserAllBindingsByGroupBinding(binding1)).thenReturn(userSpaceBindings);
    
  
    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(binding1.getId(),
                                                                              Long.parseLong(binding1.getSpaceId()),
                                                                              binding1.getGroup(),
                                                                              GroupSpaceBindingReportAction.REMOVE_ACTION);
    Mockito.when(groupSpaceBindingStorage.saveGroupSpaceBindingReport(Mockito.any())).thenReturn(report);
  
  
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
    
    
    ArgumentCaptor<GroupSpaceBindingReportAction> reportActionCaptor=ArgumentCaptor.forClass(GroupSpaceBindingReportAction.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBindingReport(reportActionCaptor.capture());
    assertEquals(GroupSpaceBindingReportAction.REMOVE_ACTION,reportActionCaptor.getValue().getAction());
  
    ArgumentCaptor<GroupSpaceBindingReportUser> reportUserCaptor=ArgumentCaptor.forClass(GroupSpaceBindingReportUser.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(2)).saveGroupSpaceBindingReportUser(reportUserCaptor.capture());
    assertEquals(GroupSpaceBindingReportUser.ACTION_REMOVE_USER,reportUserCaptor.getAllValues().get(0).getAction());
    assertEquals("user1",reportUserCaptor.getAllValues().get(0).getUsername());
    assertEquals(GroupSpaceBindingReportUser.ACTION_REMOVE_USER,reportUserCaptor.getAllValues().get(1).getAction());
    assertEquals("user2",reportUserCaptor.getAllValues().get(1).getUsername());
  
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
    binding3.setSpaceId("3");
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
    space.setMembers(new String[] { "root" });
    Mockito.when(spaceService.getSpaceById(Mockito.any())).thenReturn(space);
    Mockito.when(groupSpaceBindingStorage.countBoundUsers(Mockito.any())).thenReturn(0L);
  
  
    GroupSpaceBindingReportAction report1 = new GroupSpaceBindingReportAction(binding1.getId(),
                                                                             Long.parseLong(binding1.getSpaceId()),
                                                                              binding1.getGroup(),
                                                                             GroupSpaceBindingReportAction.REMOVE_ACTION);
    GroupSpaceBindingReportAction report2 = new GroupSpaceBindingReportAction(binding4.getId(),
                                                                              Long.parseLong(binding4.getSpaceId()),
                                                                              binding4.getGroup(),
                                                                              GroupSpaceBindingReportAction.REMOVE_ACTION);
    Mockito.when(groupSpaceBindingStorage.saveGroupSpaceBindingReport(Mockito.any())).thenReturn(report1,report2);
  
  
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
   * Test {@link GroupSpaceBindingService#saveGroupSpaceBindings(List)}
   * *
   * @throws Exception
   */
  @Test
  public void testSaveGroupSpaceBindings() throws Exception {
    List<GroupSpaceBinding> groupSpaceBindings = new LinkedList<>();
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");
    groupSpaceBindings.add(binding1);
  
    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding2.setGroup("/platform/web-contributors");
    binding2.setSpaceId("2");
    groupSpaceBindings.add(binding2);
  
    GroupSpaceBinding binding3 = new GroupSpaceBinding();
    binding3.setGroup("/platform/web-contributors");
    binding3.setSpaceId("3");
    groupSpaceBindings.add(binding3);
  
    GroupSpaceBinding binding4 = new GroupSpaceBinding();
    binding4.setGroup("/platform/web-contributors");
    binding4.setSpaceId("1");
    groupSpaceBindings.add(binding4);
  
  
  
    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    Mockito.when(groupSpaceBindingStorage.saveGroupSpaceBinding(Mockito.any())).thenReturn(binding1,binding2,binding3,binding4);
    groupSpaceBindingService.saveGroupSpaceBindings(groupSpaceBindings);
  
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(4)).saveGroupSpaceBinding(Mockito.any());
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(4)).createGroupSpaceBindingQueue(Mockito.any());
  
  }
  
  /**
   * Test {@link GroupSpaceBindingService#saveGroupSpaceBindings(List)}
   * *
   * @throws Exception
   */
  @Test
  public void testSaveGroupSpaceBinding() throws Exception {
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");
    
    
    
    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    Mockito.when(groupSpaceBindingStorage.saveGroupSpaceBinding(Mockito.any())).thenReturn(binding1);
  
    groupSpaceBindingService.saveGroupSpaceBinding(binding1);
    
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBinding(Mockito.any());
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBindingReport(Mockito.any());
    
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
    // ==> check that report is generated

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
  
    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(binding1.getId(),
                                                                             Long.parseLong(binding1.getSpaceId()),
                                                                             binding1.getGroup(),
                                                                             GroupSpaceBindingReportAction.ADD_ACTION);
    Mockito.when(groupSpaceBindingStorage.saveGroupSpaceBindingReport(Mockito.any())).thenReturn(report);
    
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

    ArgumentCaptor<GroupSpaceBindingReportAction> reportCaptur = ArgumentCaptor.forClass(GroupSpaceBindingReportAction.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBindingReport(reportCaptur.capture());
    GroupSpaceBindingReportAction capturedReport = reportCaptur.getValue();
    assertEquals(GroupSpaceBindingReportAction.ADD_ACTION, capturedReport.getAction());
  
    ArgumentCaptor<GroupSpaceBindingReportUser> reportUserCaptur = ArgumentCaptor.forClass(GroupSpaceBindingReportUser.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBindingReportUser(reportUserCaptur.capture());
    GroupSpaceBindingReportUser capturedUserReport = reportUserCaptur.getValue();
    assertEquals(GroupSpaceBindingReportUser.ACTION_ADD_USER, capturedUserReport.getAction());
    assertEquals(false, capturedUserReport.isWasPresentBefore());
    assertEquals(false, capturedUserReport.isStillInSpace());

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
    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(binding1.getId(),
                                                                             Long.parseLong(binding1.getSpaceId()),
                                                                             binding1.getGroup(),
                                                                             GroupSpaceBindingReportAction.ADD_ACTION);
    Mockito.when(groupSpaceBindingStorage.saveGroupSpaceBindingReport(Mockito.any())).thenReturn(report);
    
    
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

    ArgumentCaptor<GroupSpaceBindingReportAction> reportCaptur = ArgumentCaptor.forClass(GroupSpaceBindingReportAction.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBindingReport(reportCaptur.capture());
    GroupSpaceBindingReportAction capturedReport = reportCaptur.getValue();
    assertEquals(GroupSpaceBindingReportAction.ADD_ACTION, capturedReport.getAction());
  
    ArgumentCaptor<GroupSpaceBindingReportUser> reportUserCaptur = ArgumentCaptor.forClass(GroupSpaceBindingReportUser.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBindingReportUser(reportUserCaptur.capture());
    GroupSpaceBindingReportUser capturedUserReport = reportUserCaptur.getValue();
    assertEquals(GroupSpaceBindingReportUser.ACTION_ADD_USER, capturedUserReport.getAction());
    assertEquals(true, capturedUserReport.isWasPresentBefore());
    assertEquals(false, capturedUserReport.isStillInSpace());

  }

  @Test
  public void testBindUsersFromGroupSpaceBindingCheckUserIsBoundButNotMemberBefore() throws Exception {
    // 3) a user is bound to the space, but for this userBinding,
    // isMemberBefore=false
    // => isMemberBefore should be false

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
  
    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(binding1.getId(),
                                                                             Long.parseLong(binding1.getSpaceId()),
                                                                             binding1.getGroup(),
                                                                             GroupSpaceBindingReportAction.ADD_ACTION);
    Mockito.when(groupSpaceBindingStorage.saveGroupSpaceBindingReport(Mockito.any())).thenReturn(report);
    
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

    ArgumentCaptor<GroupSpaceBindingReportAction> reportCaptur = ArgumentCaptor.forClass(GroupSpaceBindingReportAction.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBindingReport(reportCaptur.capture());
    GroupSpaceBindingReportAction capturedReport = reportCaptur.getValue();
    assertEquals(GroupSpaceBindingReportAction.ADD_ACTION, capturedReport.getAction());
  
    ArgumentCaptor<GroupSpaceBindingReportUser> reportUserCaptur = ArgumentCaptor.forClass(GroupSpaceBindingReportUser.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBindingReportUser(reportUserCaptur.capture());
    GroupSpaceBindingReportUser capturedUserReport = reportUserCaptur.getValue();
    assertEquals(GroupSpaceBindingReportUser.ACTION_ADD_USER, capturedUserReport.getAction());
    assertEquals(false, capturedUserReport.isWasPresentBefore());
    assertEquals(false, capturedUserReport.isStillInSpace());

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
    Mockito.when(groupSpaceBindingStorage.countUserBindings(Mockito.anyString(), Mockito.anyString())).thenReturn(1L);
  
    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(binding1.getId(),
                                                                             Long.parseLong(binding1.getSpaceId()),
                                                                             binding1.getGroup(),
                                                                             GroupSpaceBindingReportAction.ADD_ACTION);
    Mockito.when(groupSpaceBindingStorage.saveGroupSpaceBindingReport(Mockito.any())).thenReturn(report);
    
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

    ArgumentCaptor<GroupSpaceBindingReportAction> reportCaptur = ArgumentCaptor.forClass(GroupSpaceBindingReportAction.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBindingReport(reportCaptur.capture());
    GroupSpaceBindingReportAction capturedReport = reportCaptur.getValue();
    assertEquals(GroupSpaceBindingReportAction.ADD_ACTION, capturedReport.getAction());
  
    ArgumentCaptor<GroupSpaceBindingReportUser> reportUserCaptur = ArgumentCaptor.forClass(GroupSpaceBindingReportUser.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBindingReportUser(reportUserCaptur.capture());
    GroupSpaceBindingReportUser capturedUserReport = reportUserCaptur.getValue();
    assertEquals(GroupSpaceBindingReportUser.ACTION_ADD_USER, capturedUserReport.getAction());
    assertEquals(true, capturedUserReport.isWasPresentBefore());
    assertEquals(false, capturedUserReport.isStillInSpace());

  }

  /**
   * Test
   * {@link GroupSpaceBindingService#bindUsersFromGroupSpaceBinding(GroupSpaceBinding)}
   *
   * @throws Exception
   */
  public void testBindUsersFromGroupSpaceBinding() throws Exception {

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
    
    User user2 = new UserImpl("user2");
    user2.setFirstName("user2");
    user2.setLastName("user2");
    user2.setEmail("user2@acme.com");
    User user3 = new UserImpl("user3");
    user3.setFirstName("user3");
    user3.setLastName("user3");
    user3.setEmail("user3@acme.com");

    ListAccess<User> userListAccess = new ListAccess<User>() {
      public User[] load(int index, int length) throws Exception {
        List<User> users = new ArrayList();
        users.add(user2);
        users.add(user3);
        User[] result = new User[users.size()];
        return users.toArray(result);
      }

      public int getSize() throws Exception {
        return 2;
      }
    };

    UserDAOImpl userDAO = Mockito.mock(UserDAOImpl.class);
    Mockito.when(userDAO.findUsersByGroupId("/platform/administrators")).thenReturn(userListAccess);
    Mockito.when(orgService.getUserHandler()).thenReturn(userDAO);
  
    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(binding1.getId(),
                                                                             Long.parseLong(binding1.getSpaceId()),
                                                                             binding1.getGroup(),
                                                                             GroupSpaceBindingReportAction.ADD_ACTION);
    Mockito.when(groupSpaceBindingStorage.saveGroupSpaceBindingReport(Mockito.any())).thenReturn(report);
    

    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    groupSpaceBindingService.bindUsersFromGroupSpaceBinding(binding1);
    assertEquals(2,
                 groupSpaceBindingService.findReportsForCsv(Long.parseLong(space1.getId()),
                                                            binding1.getId(),
                                                            binding1.getGroup(),
                                                            GroupSpaceBindingReportAction.ADD_ACTION));
    //todo check reports
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
   * Test
   * {@link GroupSpaceBindingService#bindUsersFromGroupSpaceBinding(GroupSpaceBinding)}
   *
   * @throws Exception
   */
  @Test
  public void testSaveUserBinding() throws Exception {
    // given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");
  
    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(binding1.getId(),
                                                                             Long.parseLong(binding1.getSpaceId()),
                                                                             binding1.getGroup(),
                                                                             GroupSpaceBindingReportAction.ADD_ACTION);
  
    Space space = new Space();
    space.setId("1");
    space.setDisplayName("space1");
    space.setPrettyName("space1");
    space.setMembers(new String[] { "root" });
    Mockito.when(spaceService.getSpaceById(Mockito.any())).thenReturn(space);
    Mockito.when(groupSpaceBindingStorage.countBoundUsers(Mockito.any())).thenReturn(0L);
  
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams,
                                                                                         groupSpaceBindingStorage,
                                                                                         orgService,
                                                                                         spaceService);
    
    groupSpaceBindingService.saveUserBinding("user1",binding1,space,report);
  
  
    // then
    ArgumentCaptor<UserSpaceBinding> argument = ArgumentCaptor.forClass(UserSpaceBinding.class);
    Mockito.verify(groupSpaceBindingStorage).saveUserBinding(argument.capture());
    assertEquals(false, argument.getValue().isMemberBefore().booleanValue());
    assertEquals("user1", argument.getValue().getUser());
  
    ArgumentCaptor<GroupSpaceBindingReportUser> reportUserCaptur = ArgumentCaptor.forClass(GroupSpaceBindingReportUser.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupSpaceBindingReportUser(reportUserCaptur.capture());
    GroupSpaceBindingReportUser capturedUserReport = reportUserCaptur.getValue();
    assertEquals(GroupSpaceBindingReportUser.ACTION_ADD_USER, capturedUserReport.getAction());
  
  }
  

}
