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

package org.exoplatform.social.core.binding.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingQueue;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage;

/**
 * {@link org.exoplatform.social.core.binding.spi.GroupSpaceBindingService}
 * implementation.
 */

public class GroupSpaceBindingServiceImpl implements GroupSpaceBindingService {

  private static final Log         LOG                     = ExoLogger.getLogger(GroupSpaceBindingServiceImpl.class);

  private static final int         USERS_TO_BIND_PAGE_SIZE = 20;

  private GroupSpaceBindingStorage groupSpaceBindingStorage;

  private OrganizationService      organizationService;

  private SpaceService             spaceService;

  private static Boolean           requestStarted          = false;

  /**
   * GroupSpaceBindingServiceImpl constructor Initialize
   * 
   * @param params
   * @throws Exception
   */
  public GroupSpaceBindingServiceImpl(InitParams params,
                                      GroupSpaceBindingStorage groupSpaceBindingStorage,
                                      OrganizationService organizationService,
                                      SpaceService spaceService)
      throws Exception {
    this.groupSpaceBindingStorage = groupSpaceBindingStorage;
    this.organizationService = organizationService;
    this.spaceService = spaceService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GroupSpaceBindingQueue findFirstGroupSpaceBindingQueue() {
    LOG.debug("Retrieving First GroupSpaceBindingQueue to treat");
    return groupSpaceBindingStorage.findFirstGroupSpaceBindingQueue();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GroupSpaceBinding> findGroupSpaceBindingsBySpace(String spaceId) {
    LOG.debug("Retrieving group/space bindings for space:" + spaceId);
    return groupSpaceBindingStorage.findGroupSpaceBindingsBySpace(spaceId);
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public List<GroupSpaceBinding> findGroupSpaceBindingsByGroup(String group) {
    LOG.debug("Retrieving group/space bindings for group:" + group);
    return groupSpaceBindingStorage.findGroupSpaceBindingsByGroup(group);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<UserSpaceBinding> findUserSpaceBindingsBySpace(String spaceId, String userName) {
    LOG.debug("Retrieving user bindings for member:" + userName + "/" + spaceId);
    return groupSpaceBindingStorage.findUserSpaceBindingsBySpace(spaceId, userName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<UserSpaceBinding> findUserBindingsByUser(String userName) {
    LOG.debug("Retrieving user bindings for member:" + userName);
    return groupSpaceBindingStorage.findUserSpaceBindingsByUser(userName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<UserSpaceBinding> findUserBindingsByGroup(String group, String userName) {
    LOG.debug("Retrieving user bindings for user : " + userName + " with group : " + group);
    return groupSpaceBindingStorage.findUserSpaceBindingsByGroup(group, userName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveUserBindings(List<UserSpaceBinding> userSpaceBindings) {
    LOG.debug("Saving user bindings, ({}) to save", userSpaceBindings.size());
    try {
      for (UserSpaceBinding userSpaceBinding : userSpaceBindings) {
        groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
      }
    } catch (Exception e) {
      LOG.error("Error Binding" + e);
      throw new RuntimeException("Failed saving user bindings", e);
    }
  }

  @Override
  public void createGroupSpaceBindingQueue(GroupSpaceBindingQueue groupSpaceBindingsQueue) {
    groupSpaceBindingStorage.createGroupSpaceBindingQueue(groupSpaceBindingsQueue);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding) {
    LOG.debug("Delete binding group :" + groupSpaceBinding.getGroup() + " for space :" + groupSpaceBinding.getSpaceId());
    // Call the delete user binding to also update space membership.
    for (UserSpaceBinding userSpaceBinding : groupSpaceBindingStorage.findUserAllBindingsByGroup(groupSpaceBinding.getGroup())) {
      deleteUserBindingAndSpaceMembership(userSpaceBinding);
    }
    // The deletion of the groupSpaceBinding will also remove it from the
    // groupSpaceBindingQueue.
    groupSpaceBindingStorage.deleteGroupBinding(groupSpaceBinding.getId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteUserBindingAndSpaceMembership(UserSpaceBinding userSpaceBinding) {
    LOG.debug("Delete user binding for member : {} from ",
              userSpaceBinding.getUser(),
              userSpaceBinding.getGroupBinding().getSpaceId());
    // Remove user binding.
    groupSpaceBindingStorage.deleteUserBinding(userSpaceBinding.getId());
    // check if the user has other binding to the target space before removing.
    boolean hasOtherBindings = groupSpaceBindingStorage.findUserSpaceBindingsBySpace(
                                                                                     userSpaceBinding.getGroupBinding()
                                                                                                     .getSpaceId(),
                                                                                     userSpaceBinding.getUser())
                                                       .size() > 0;
    if (!hasOtherBindings && !userSpaceBinding.isMemberBefore()) {
      // no binding to the target space in this case remove user from space.
      spaceService.removeMember(spaceService.getSpaceById(userSpaceBinding.getGroupBinding().getSpaceId()),
                                userSpaceBinding.getUser());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteAllSpaceBindingsBySpace(String spaceId) {
    LOG.debug("Delete all bindings for space :" + spaceId);
    for (GroupSpaceBinding groupSpaceBinding : findGroupSpaceBindingsBySpace(spaceId)) {
      deleteGroupSpaceBinding(groupSpaceBinding);
    }
  }

  @Override
  public void deleteAllSpaceBindingsByGroup(String groupId) {
    LOG.debug("Delete all bindings for group :" + groupId);
    for (GroupSpaceBinding groupSpaceBinding : findGroupSpaceBindingsByGroup(groupId)) {
      deleteGroupSpaceBinding(groupSpaceBinding);
    }

  }

  @Override
  public long countUserBindings(String spaceId, String userName) {
    LOG.debug("Count member binding :" + userName + " space:" + spaceId);
    return groupSpaceBindingStorage.countUserBindings(spaceId, userName);

  }

  @Override
  public void saveGroupSpaceBindings(List<GroupSpaceBinding> groupSpaceBindings) {
    LOG.debug("Saving group space binding between spaceId: {} and groups: {}.",
              groupSpaceBindings.get(0).getSpaceId(),
              groupSpaceBindings.toString());
    try {
      List<GroupSpaceBinding> boundGroupsAndSpacesList = new ArrayList<>();
      List<GroupSpaceBindingQueue> bindingQueueList = new ArrayList<>();

      // Save group space bindings.
      groupSpaceBindings.stream()
                        .forEach(groupSpaceBinding -> boundGroupsAndSpacesList.add(groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding)));
      // Generate GroupSpaceBindingQueue List.
      boundGroupsAndSpacesList.stream().forEach(groupSpaceBinding -> {
        bindingQueueList.add(new GroupSpaceBindingQueue(groupSpaceBinding, GroupSpaceBindingQueue.ACTION_CREATE));
      });
      // Add group space bindings to the binding queue.
      bindingQueueList.stream().forEach(groupSpaceBindingQueue -> createGroupSpaceBindingQueue(groupSpaceBindingQueue));
    } catch (Exception e) {
      LOG.error("Error Binding" + e);
      throw new RuntimeException("Failed saving groupSpaceBindings: " + groupSpaceBindings.toString(), e);
    }
  }

  @Override
  public void bindUsersFromGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding) {
    Space space = spaceService.getSpaceById(groupSpaceBinding.getSpaceId());
    String[] members = space.getMembers();
    long count, toBind;
    int limit, offset = 0;
    long startTime = System.currentTimeMillis();

    try {
      List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
      ListAccess<User> groupMembersAccess = organizationService.getUserHandler().findUsersByGroupId(groupSpaceBinding.getGroup());
      List<User> users;
      int totalGroupMembersSize = groupMembersAccess.getSize();
      do {
        long startBunchTime = System.currentTimeMillis();
        toBind = totalGroupMembersSize - offset;
        limit = toBind < USERS_TO_BIND_PAGE_SIZE ? (int) toBind : USERS_TO_BIND_PAGE_SIZE;
        users = Arrays.asList(groupMembersAccess.load(offset, limit));
        count = users.size();
        int currentCount = offset;
        for (User user : users) {
          currentCount++;
          startRequest();
          long startTimeUser = System.currentTimeMillis();

          String userId = user.getUserName();
          UserSpaceBinding userSpaceBinding = new UserSpaceBinding();
          userSpaceBinding.setUser(userId);
          // If user exists in space members before any binding set isMemberBefore to
          // true.
          boolean isUserAlreadyBound = countUserBindings(groupSpaceBinding.getSpaceId(), userId) > 0;
          if (!isUserAlreadyBound) {
            // If user is not already bound then check if is member of the space.
            userSpaceBinding.setIsMemberBefore(ArrayUtils.contains(members, userId));
          } else {
            // If user is already bound then check if is member before.
            userSpaceBinding.setIsMemberBefore(isUserBoundAndMemberBefore(groupSpaceBinding.getSpaceId(), userId));
          }
          userSpaceBinding.setGroupBinding(groupSpaceBinding);
          userSpaceBindings.add(userSpaceBinding);
          spaceService.addMember(space, userId);
          groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
          long endTimeUser = System.currentTimeMillis();
          long totalTimeUser = endTimeUser - startTimeUser;
          LOG.debug("Time to treat user " + userId + " (" + currentCount + "/" + totalGroupMembersSize + ") : " + totalTimeUser
              + " ms");
          endRequest();
        }
        offset += count;
        LOG.info("Binding process: Bound Users({})", offset);
        long endBunchTime = System.currentTimeMillis();
        long totalBunchTime = endBunchTime - startBunchTime;
        LOG.info("Time to treat " + count + " (" + offset + "/" + totalGroupMembersSize + ") users : " + totalBunchTime + " ms");
      } while (offset < totalGroupMembersSize);
    } catch (Exception e) {
      LOG.error("Error Binding" + e);
      throw new RuntimeException("Failed saving groupSpaceBinding", e);
    }
    long endTime = System.currentTimeMillis();

    long totalTime = endTime - startTime;
    LOG.debug("Time to treat all users : (" + offset + ") : " + totalTime + " ms");
  }

  public boolean isUserBoundAndMemberBefore(String spaceId, String userId) {
    return groupSpaceBindingStorage.isUserBoundAndMemberBefore(spaceId, userId);
  }

  @Override
  public boolean isBoundSpace(String spaceId) {
    return groupSpaceBindingStorage.isBoundSpace(spaceId);
  }

  @Override
  public void deleteFromBindingQueue(GroupSpaceBindingQueue bindingQueue) {
    groupSpaceBindingStorage.deleteGroupBindingQueue(bindingQueue.getId());
  }

  private void endRequest() {
    if (requestStarted && organizationService instanceof ComponentRequestLifecycle) {
      try {
        ((ComponentRequestLifecycle) organizationService).endRequest(PortalContainer.getInstance());
      } catch (Exception e) {
        LOG.warn(e.getMessage(), e);
      }
      requestStarted = false;
    }
  }

  private void startRequest() {
    if (organizationService instanceof ComponentRequestLifecycle) {
      ((ComponentRequestLifecycle) organizationService).startRequest(PortalContainer.getInstance());
      requestStarted = true;
    }
  }

  @Override
  public GroupSpaceBinding findGroupSpaceBindingById(String bindingId) {
    return groupSpaceBindingStorage.findGroupSpaceBindingById(bindingId);
  }

  @Override
  public List<GroupSpaceBinding> getGroupSpaceBindingsFromQueueByAction(String action) {
    return groupSpaceBindingStorage.getGroupSpaceBindingsFromQueueByAction(action);
  }

  @Override
  public void saveUserBinding(String userId, GroupSpaceBinding groupSpaceBinding, Space space) {
    String[] members = space.getMembers();
    UserSpaceBinding userSpaceBinding = new UserSpaceBinding(userId, groupSpaceBinding);
    // If user exists in space members before any binding set isMemberBefore to
    // true.
    boolean isUserAlreadyBound = countUserBindings(groupSpaceBinding.getSpaceId(), userId) > 0;
    if (!isUserAlreadyBound) {
      // If user is not already bound then check if is member of the space.
      userSpaceBinding.setIsMemberBefore(ArrayUtils.contains(members, userId));
    } else {
      // If user is already bound then check if is member before.
      userSpaceBinding.setIsMemberBefore(isUserBoundAndMemberBefore(groupSpaceBinding.getSpaceId(), userId));
    }
    spaceService.addMember(space, userId);
    groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
  }

  @Override
  public void deleteUserSpaceBinding(UserSpaceBinding userSpaceBinding) {
    groupSpaceBindingStorage.deleteUserBinding(userSpaceBinding.getId());
  }

}
