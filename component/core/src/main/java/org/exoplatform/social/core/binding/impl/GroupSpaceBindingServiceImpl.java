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
import java.util.Date;
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
import org.exoplatform.social.core.binding.model.*;
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

  @Override
  public List<GroupSpaceBindingReportUser> findReportsForCsv(long spaceId,
                                                               long groupSpaceBindingId,
                                                               String group,
                                                               String action) {
    LOG.debug("Retrieving GroupSpaceBindingReports for space={}, groupSpaceBinding={}, group={}, actions={}",
              spaceId,
              groupSpaceBindingId,
              group,
              action);
    return groupSpaceBindingStorage.findReportsForCsv(spaceId, groupSpaceBindingId, group, action);
  }

  @Override
  public List<GroupSpaceBindingOperationReport> getGroupSpaceBindingReportOperations() {
    return groupSpaceBindingStorage.getGroupSpaceBindingReportOperations();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<UserSpaceBinding> findUserBindingsByGroup(String group, String userName) {
    LOG.debug("Retrieving user bindings for user : " + userName + " with group : " + group);
    return groupSpaceBindingStorage.findUserSpaceBindingsByGroup(group, userName);
  }

  @Override
  public void createGroupSpaceBindingQueue(GroupSpaceBindingQueue groupSpaceBindingsQueue) {
    groupSpaceBindingStorage.createGroupSpaceBindingQueue(groupSpaceBindingsQueue);
  }
  
  @Override
  public void prepareDeleteGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding) {
  
    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                             Long.parseLong(groupSpaceBinding.getSpaceId()),
                                                                             groupSpaceBinding.getGroup(),
                                                                             GroupSpaceBindingReportAction.REMOVE_ACTION);
  
  
    if (groupSpaceBindingStorage.findGroupSpaceBindingReportAction(report.getGroupSpaceBindingId(),report.getAction())==null) {
      groupSpaceBindingStorage.saveGroupSpaceBindingReport(report);
    }
    
    GroupSpaceBindingQueue bindingQueue = new GroupSpaceBindingQueue(groupSpaceBinding, GroupSpaceBindingQueue.ACTION_REMOVE);
    groupSpaceBindingStorage.createGroupSpaceBindingQueue(bindingQueue);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding) {
    LOG.debug("Delete binding group :" + groupSpaceBinding.getGroup() + " for space :" + groupSpaceBinding.getSpaceId());
    long startTime = System.currentTimeMillis();
    Space space = spaceService.getSpaceById(groupSpaceBinding.getSpaceId());

    
    GroupSpaceBindingReportAction bindingReportRemoveAction =
        groupSpaceBindingStorage.findGroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                   GroupSpaceBindingReportAction.REMOVE_ACTION);
    if (bindingReportRemoveAction==null) {
      GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                               Long.parseLong(groupSpaceBinding.getSpaceId()),
                                                                               groupSpaceBinding.getGroup(),
                                                                               GroupSpaceBindingReportAction.REMOVE_ACTION);
      bindingReportRemoveAction=groupSpaceBindingStorage.saveGroupSpaceBindingReport(report);
    }
  
    // Call the delete user binding to also update space membership.
    for (UserSpaceBinding userSpaceBinding : groupSpaceBindingStorage.findUserAllBindingsByGroupBinding(groupSpaceBinding)) {
      deleteUserBinding(userSpaceBinding, bindingReportRemoveAction);
    }
    // Finally save the end date for the bindingReportAction.
    bindingReportRemoveAction.setEndDate(new Date());
    groupSpaceBindingStorage.updateGroupSpaceBindingReportAction(bindingReportRemoveAction);

    // The deletion of the groupSpaceBinding will also remove it from the
    // groupSpaceBindingQueue.
    groupSpaceBindingStorage.deleteGroupBinding(groupSpaceBinding.getId());

    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    LOG.info("service={} operation={} parameters=\"space:{},totalSpaceMembers:{},boundSpaceMembers:{}\" status=ok "
        + "duration_ms={}",
             LOG_SERVICE_NAME,
             LOG_REMOVE_OPERATION_NAME,
             space.getPrettyName(),
             space.getMembers().length,
             countBoundUsers(space.getId()),
             totalTime);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public void deleteUserBinding(UserSpaceBinding userSpaceBinding,
                                                       GroupSpaceBindingReportAction bindingReportAction) {
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
    boolean isStillPresent = true;
    boolean shouldBeRemovedFromSpace = !hasOtherBindings;
    if (bindingReportAction.getAction().equals(GroupSpaceBindingReportAction.REMOVE_ACTION)) {
      // we remove the binding, so we need to check
      // 1) if the user was memberBefore
      // 2) if he is still bound by another binding
      shouldBeRemovedFromSpace = shouldBeRemovedFromSpace && !userSpaceBinding.isMemberBefore();

    }
    // else
    // action is UPDATE_REMOVE
    // so we remove the user from the space even if he was present before. We only
    // need to check if he is bound by another
    // binding
    if (shouldBeRemovedFromSpace) {
      // remove membership from space even if he was member before the binding
      // occurred
      spaceService.removeMember(spaceService.getSpaceById(userSpaceBinding.getGroupBinding().getSpaceId()),
                                userSpaceBinding.getUser());
      isStillPresent = false;
    }

    GroupSpaceBindingReportUser report = new GroupSpaceBindingReportUser(bindingReportAction,
                                                                         userSpaceBinding.getUser(),
                                                                         GroupSpaceBindingReportUser.ACTION_REMOVE_USER);
    report.setStillInSpace(isStillPresent);
    report.setWasPresentBefore(userSpaceBinding.isMemberBefore());
    groupSpaceBindingStorage.saveGroupSpaceBindingReportUser(report);
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
  public long countBoundUsers(String spaceId) {
    LOG.debug("Count bound users for space:" + spaceId);
    return groupSpaceBindingStorage.countBoundUsers(spaceId);
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
                        .forEach(groupSpaceBinding -> boundGroupsAndSpacesList.add(saveGroupSpaceBinding(groupSpaceBinding)));
      // Generate GroupSpaceBindingQueue List.
      boundGroupsAndSpacesList.stream().forEach(groupSpaceBinding -> {
        bindingQueueList.add(new GroupSpaceBindingQueue(groupSpaceBinding, GroupSpaceBindingQueue.ACTION_CREATE));
      });
      // Add group space bindings to the binding queue.
      bindingQueueList.stream().forEach(groupSpaceBindingQueue -> createGroupSpaceBindingQueue(groupSpaceBindingQueue));
    } catch (Exception e) {
      LOG.error("Error when saving group space binding " + groupSpaceBindings, e);
      throw new RuntimeException("Failed saving groupSpaceBindings: " + groupSpaceBindings.toString(), e);
    }
  }

  @Override
  public GroupSpaceBinding saveGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding) {
    
    groupSpaceBinding = groupSpaceBindingStorage.saveGroupSpaceBinding(groupSpaceBinding);
    GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                             Long.parseLong(groupSpaceBinding.getSpaceId()),
                                                                             groupSpaceBinding.getGroup(),
                                                                             GroupSpaceBindingReportAction.ADD_ACTION);
    if (groupSpaceBindingStorage.findGroupSpaceBindingReportAction(report.getGroupSpaceBindingId(),report.getAction())==null) {
      groupSpaceBindingStorage.saveGroupSpaceBindingReport(report);
    }
    return groupSpaceBinding;
  }

  @Override
  public void bindUsersFromGroupSpaceBinding(GroupSpaceBinding groupSpaceBinding) {
    Space space = spaceService.getSpaceById(groupSpaceBinding.getSpaceId());
    long count, toBind;
    int limit, offset = 0;
    long startTime = System.currentTimeMillis();

    try {
      ListAccess<User> groupMembersAccess = organizationService.getUserHandler().findUsersByGroupId(groupSpaceBinding.getGroup());
      List<User> users;
      int totalGroupMembersSize = groupMembersAccess.getSize();
      GroupSpaceBindingReportAction bindingReportAddAction=
          groupSpaceBindingStorage.findGroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                     GroupSpaceBindingReportAction.ADD_ACTION);
      if (bindingReportAddAction==null) {
        GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                                 Long.parseLong(groupSpaceBinding.getSpaceId()),
                                                                                 groupSpaceBinding.getGroup(),
                                                                                 GroupSpaceBindingReportAction.ADD_ACTION);
        bindingReportAddAction=groupSpaceBindingStorage.saveGroupSpaceBindingReport(report);
      }
     
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
          saveUserBinding(userId, groupSpaceBinding, space, bindingReportAddAction);

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
      // Finally save the end date for the bindingReportAction.
      bindingReportAddAction.setEndDate(new Date());
      groupSpaceBindingStorage.updateGroupSpaceBindingReportAction(bindingReportAddAction);

    } catch (Exception e) {
      LOG.error("Error when binding users from group " + groupSpaceBinding.getGroup() + ", to space "
          + groupSpaceBinding.getSpaceId(), e);
      long endTime = System.currentTimeMillis();
      long totalTime = endTime - startTime;
      LOG.info("service={} operation={} parameters=\"space:{},totalSpaceMembers:{},boundSpaceMembers:{}\" status=ko "
          + "duration_ms={}",
               LOG_SERVICE_NAME,
               LOG_NEW_OPERATION_NAME,
               space.getPrettyName(),
               space.getMembers().length,
               countBoundUsers(space.getId()),
               totalTime);
      throw new RuntimeException("Failed saving groupSpaceBinding", e);
    }
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    LOG.info("service={} operation={} parameters=\"space:{},totalSpaceMembers:{},boundSpaceMembers:{}\" status=ok "
        + "duration_ms={}",
             LOG_SERVICE_NAME,
             LOG_NEW_OPERATION_NAME,
             space.getPrettyName(),
             space.getMembers().length,
             countBoundUsers(space.getId()),
             totalTime);
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
  public void saveUserBinding(String userId,
                             GroupSpaceBinding groupSpaceBinding,
                             Space space,
                             GroupSpaceBindingReportAction bindingReportAction) {
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
    GroupSpaceBindingReportUser groupSpaceBindingReportUser =
                                                            new GroupSpaceBindingReportUser(bindingReportAction,
                                                                                            userId,
                                                                                            GroupSpaceBindingReportUser.ACTION_ADD_USER);
    groupSpaceBindingReportUser.setWasPresentBefore(userSpaceBinding.isMemberBefore());
    spaceService.addMember(space, userId);
    groupSpaceBindingStorage.saveUserBinding(userSpaceBinding);
    groupSpaceBindingStorage.saveGroupSpaceBindingReportUser(groupSpaceBindingReportUser);
  }

  @Override
  public GroupSpaceBindingReportAction saveGroupSpaceBindingReport(GroupSpaceBindingReportAction groupSpaceBindingReportAction) {
    return groupSpaceBindingStorage.saveGroupSpaceBindingReport(groupSpaceBindingReportAction);
  }

  @Override
  public GroupSpaceBindingReportAction findGroupSpaceBindingReportAction(long bindingId, String action) {
    return groupSpaceBindingStorage.findGroupSpaceBindingReportAction(bindingId, action);
  }

  @Override
  public void updateGroupSpaceBindingReportAction(GroupSpaceBindingReportAction groupSpaceBindingReportAction) {
    groupSpaceBindingStorage.updateGroupSpaceBindingReportAction(groupSpaceBindingReportAction);
  }

  @Override
  public List<GroupSpaceBindingQueue> getAllFromBindingQueue() {
    return groupSpaceBindingStorage.getAllFromBindingQueue();
  }

}
