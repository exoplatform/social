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

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.manager.IdentityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.binding.impl.GroupSpaceBindingServiceImpl;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
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

  /**
   * Test
   * {@link GroupSpaceBindingService#findSpaceBindings(String spaceId, String role)}
   *
   * @throws Exception
   */
  @Test
  public void testFindSpaceBindings() throws Exception {
    // Given
    List<GroupSpaceBinding> groupSpaceBindings = new LinkedList<>();
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroupRole("any");
    binding1.setSpaceRole("member");
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");
    groupSpaceBindings.add(binding1);

    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding2.setId(2);
    binding2.setGroupRole("any");
    binding2.setSpaceRole("member");
    binding2.setGroup("/platform/web-contributors");
    binding2.setSpaceId("1");
    groupSpaceBindings.add(binding2);

    Mockito.when(groupSpaceBindingStorage.findSpaceBindings(Mockito.eq("1"), Mockito.eq("member")))
           .thenReturn(groupSpaceBindings);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams, groupSpaceBindingStorage, orgService, spaceService);
    List<GroupSpaceBinding> results = groupSpaceBindingService.findSpaceBindings("1", "member");
    GroupSpaceBinding result1 = results.get(0);
    GroupSpaceBinding result2 = results.get(1);

    // Then
    assertEquals(2, results.size());

    assertEquals(1, result1.getId());
    assertEquals("/platform/administrators", result1.getGroup());
    assertEquals("any", result1.getGroupRole());
    assertEquals("1", result1.getSpaceId());
    assertEquals("member", result1.getSpaceRole());

    assertEquals(2, result2.getId());
    assertEquals("/platform/web-contributors", result2.getGroup());
    assertEquals("any", result2.getGroupRole());
    assertEquals("1", result2.getSpaceId());
    assertEquals("member", result2.getSpaceRole());
  }

  /**
   * Test {@link GroupSpaceBindingService#findUserBindings(String, String)}
   *
   * @throws Exception
   */
  @Test
  public void testFindUserBindings() throws Exception {
    // Given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroupRole("any");
    binding1.setSpaceRole("member");
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding2.setId(1);
    binding2.setGroupRole("any");
    binding2.setSpaceRole("member");
    binding2.setGroup("/platform/users");
    binding2.setSpaceId("1");

    List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
    UserSpaceBinding ub1 = new UserSpaceBinding();
    ub1.setId(1);
    ub1.setGroupBinding(binding1);
    ub1.setSpaceId("1");
    ub1.setUser("john");
    userSpaceBindings.add(ub1);

    UserSpaceBinding ub2 = new UserSpaceBinding();
    ub2.setId(2);
    ub2.setGroupBinding(binding2);
    ub2.setSpaceId("1");
    ub2.setUser("john");
    userSpaceBindings.add(ub2);

    Mockito.when(groupSpaceBindingStorage.findUserSpaceBindings(Mockito.eq("1"), Mockito.eq("john")))
           .thenReturn(userSpaceBindings);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams, groupSpaceBindingStorage, orgService, spaceService);
    List<UserSpaceBinding> results = groupSpaceBindingService.findUserBindings("1", "john");
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
   * Test {@link GroupSpaceBindingService#deleteUserBinding(UserSpaceBinding)}
   *
   * @throws Exception
   */
  @Test
  public void deleteUserBinding() throws Exception {
    // Given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroupRole("any");
    binding1.setSpaceRole("member");
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
    UserSpaceBinding ub1 = new UserSpaceBinding();
    ub1.setId(1);
    ub1.setGroupBinding(binding1);
    ub1.setSpaceId("1");
    ub1.setUser("john");
    userSpaceBindings.add(ub1);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams, groupSpaceBindingStorage, orgService, spaceService);
    groupSpaceBindingService.deleteUserBinding(ub1);

    // Then
    ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).deleteUserBinding(idCaptor.capture());
    long id = idCaptor.getValue();
    assertEquals(1, id);
  }

  /**
   * Test
   * {@link GroupSpaceBindingService#deleteSpaceBinding(GroupSpaceBinding GroupSpaceBinding)}
   *
   * @throws Exception
   */
  @Test
  public void deleteSpaceBinding() throws Exception {
    // Given
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroupRole("any");
    binding1.setSpaceRole("member");
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams, groupSpaceBindingStorage, orgService, spaceService);
    groupSpaceBindingService.deleteSpaceBinding(binding1);

    // Then
    ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).deleteGroupBinding(idCaptor.capture());
    long id = idCaptor.getValue();
    assertEquals(1, id);
  }

  /**
   * Test
   * {@link GroupSpaceBindingService#deleteAllSpaceBindings(String spaceId, String spaceRole)}
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
    binding1.setGroupRole("any");
    binding1.setSpaceRole("member");
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");
    groupSpaceBindings.add(binding1);
    resultSpaceBindings.add(binding1);

    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding2.setId(2);
    binding2.setGroupRole("any");
    binding2.setSpaceRole("member");
    binding2.setGroup("/platform/web-contributors");
    binding2.setSpaceId("2");
    groupSpaceBindings.add(binding2);

    GroupSpaceBinding binding3 = new GroupSpaceBinding();
    binding3.setId(3);
    binding3.setGroupRole("any");
    binding3.setSpaceRole("manager");
    binding3.setGroup("/platform/web-contributors");
    binding3.setSpaceId("1");
    groupSpaceBindings.add(binding3);

    GroupSpaceBinding binding4 = new GroupSpaceBinding();
    binding4.setId(4);
    binding4.setGroupRole("any");
    binding4.setSpaceRole("member");
    binding4.setGroup("/platform/web-contributors");
    binding4.setSpaceId("1");
    groupSpaceBindings.add(binding4);
    resultSpaceBindings.add(binding4);
    Mockito.when(groupSpaceBindingStorage.findSpaceBindings(Mockito.eq("1"), Mockito.eq("member")))
           .thenReturn(resultSpaceBindings);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams, groupSpaceBindingStorage, orgService, spaceService);
    groupSpaceBindingService.deleteAllSpaceBindings("1", "member");

    // Then
    ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(2)).deleteGroupBinding(idCaptor.capture());
    long id = idCaptor.getValue();
    assertTrue(id == 1 || id == 4);
  }

  /**
   * Test {@link GroupSpaceBindingService#deleteAllUserBindings(String)}
   *
   * @throws Exception
   */
  @Test
  public void deleteAllUserBindings() throws Exception {
    // Given

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams, groupSpaceBindingStorage, orgService, spaceService);
    groupSpaceBindingService.deleteAllUserBindings("john");

    // Then
    ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).deleteAllUserBindings(idCaptor.capture());
    String user = idCaptor.getValue();
    assertTrue(user.equals("john"));
  }

  /**
   * Test {@link GroupSpaceBindingService#saveSpaceBindings(String
   * spaceId,List<GroupSpaceBinding> GroupSpaceBinding)}
   *
   * @throws Exception
   */
  @Test
  public void saveSpaceBindings() throws Exception {
    // Given
    List<GroupSpaceBinding> groupSpaceBindings = new LinkedList<>();
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroupRole("any");
    binding1.setSpaceRole("member");
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");
    groupSpaceBindings.add(binding1);

    GroupSpaceBinding binding2 = new GroupSpaceBinding();
    binding2.setId(2);
    binding2.setGroupRole("any");
    binding2.setSpaceRole("member");
    binding2.setGroup("/platform/web-contributors");
    binding2.setSpaceId("1");
    groupSpaceBindings.add(binding2);

    GroupSpaceBinding binding3 = new GroupSpaceBinding();
    binding3.setId(3);
    binding3.setGroupRole("any");
    binding3.setSpaceRole("manager");
    binding3.setGroup("/platform/web-contributors");
    binding3.setSpaceId("1");
    groupSpaceBindings.add(binding3);

    orgService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    spaceService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams, groupSpaceBindingStorage, orgService, spaceService);
    groupSpaceBindingService.saveSpaceBindings("1", groupSpaceBindings);

    // Then
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupBinding(binding1, true);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupBinding(binding2, true);
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveGroupBinding(binding3, true);
  }

  /**
   * Test {@link GroupSpaceBindingService#saveUserBindings(String, List)}
   *
   * @throws Exception
   */
  @Test
  public void saveUserBindings() throws Exception {
    // Given
    List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
    GroupSpaceBinding binding1 = new GroupSpaceBinding();
    binding1.setId(1);
    binding1.setGroupRole("any");
    binding1.setSpaceRole("member");
    binding1.setGroup("/platform/administrators");
    binding1.setSpaceId("1");

    UserSpaceBinding ub1 = new UserSpaceBinding();
    ub1.setId(1);
    ub1.setGroupBinding(binding1);
    ub1.setSpaceId("1");
    ub1.setUser("john");
    userSpaceBindings.add(ub1);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams, groupSpaceBindingStorage, orgService, spaceService);
    groupSpaceBindingService.saveUserBindings("john", userSpaceBindings);

    // Then
    Mockito.verify(groupSpaceBindingStorage, Mockito.times(1)).saveUserBinding(ub1);
  }

  /**
   * Test {@link GroupSpaceBindingService#hasUserBindings(String, String)}
   *
   * @throws Exception
   */
  @Test
  public void hasUserBindings() throws Exception {
    // Given
    Mockito.when(groupSpaceBindingStorage.hasUserBindings(Mockito.eq("1"), Mockito.eq("john"))).thenReturn(true);

    // When
    GroupSpaceBindingService groupSpaceBindingService = new GroupSpaceBindingServiceImpl(initParams, groupSpaceBindingStorage, orgService, spaceService);

    // Then
    assertEquals(true, groupSpaceBindingService.hasUserBindings("1", "john"));
  }
}
