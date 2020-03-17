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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.binding.model.*;
import org.exoplatform.social.core.jpa.storage.dao.*;
import org.exoplatform.social.core.jpa.storage.entity.*;
import org.exoplatform.social.core.storage.GroupSpaceBindingStorageException;
import org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage;

/**
 * {@link GroupSpaceBindingStorage} implementation.
 */

public class RDBMSGroupSpaceBindingStorageImpl implements GroupSpaceBindingStorage {

  /**
   * Logger
   */
  private static final Log                                           LOG =
                                                                         ExoLogger.getLogger(RDBMSGroupSpaceBindingStorageImpl.class);

  private final org.exoplatform.social.core.jpa.storage.dao.SpaceDAO spaceDAO;

  private GroupSpaceBindingDAO                                       groupSpaceBindingDAO;

  private GroupSpaceBindingQueueDAO                                  groupSpaceBindingQueueDAO;

  private UserSpaceBindingDAO                                        userSpaceBindingDAO;

  private GroupSpaceBindingReportActionDAO                           groupSpaceBindingReportActionDAO;

  private GroupSpaceBindingReportUserDAO                             groupSpaceBindingReportUserDAO;

  public RDBMSGroupSpaceBindingStorageImpl(SpaceDAO spaceDAO,
                                           GroupSpaceBindingDAO groupSpaceBindingDAO,
                                           GroupSpaceBindingQueueDAO groupSpaceBindingQueueDAO,
                                           UserSpaceBindingDAO userSpaceBindingDAO,
                                           GroupSpaceBindingReportActionDAO groupSpaceBindingReportActionDAO,
                                           GroupSpaceBindingReportUserDAO groupSpaceBindingReportUserDAO) {
    this.spaceDAO = spaceDAO;
    this.groupSpaceBindingDAO = groupSpaceBindingDAO;
    this.groupSpaceBindingQueueDAO = groupSpaceBindingQueueDAO;
    this.userSpaceBindingDAO = userSpaceBindingDAO;
    this.groupSpaceBindingReportActionDAO = groupSpaceBindingReportActionDAO;
    this.groupSpaceBindingReportUserDAO = groupSpaceBindingReportUserDAO;
  }

  @ExoTransactional
  public GroupSpaceBindingQueue findFirstGroupSpaceBindingQueue() throws GroupSpaceBindingStorageException {
    return fillGroupBindingQueueFromEntity(groupSpaceBindingQueueDAO.findFirstGroupSpaceBindingQueue());
  }

  @ExoTransactional
  public List<GroupSpaceBinding> findGroupSpaceBindingsBySpace(String spaceId) throws GroupSpaceBindingStorageException {
    return buildGroupBindingListFromEntities(groupSpaceBindingDAO.findGroupSpaceBindingsBySpace(Long.parseLong(spaceId)));
  }

  @ExoTransactional
  public List<GroupSpaceBinding> findGroupSpaceBindingsByGroup(String group) throws GroupSpaceBindingStorageException {
    return buildGroupBindingListFromEntities(groupSpaceBindingDAO.findGroupSpaceBindingsByGroup(group));
  }

  @ExoTransactional
  public List<UserSpaceBinding> findUserSpaceBindingsBySpace(String spaceId,
                                                             String username) throws GroupSpaceBindingStorageException {
    return buildUserBindingListFromEntities(userSpaceBindingDAO.findUserSpaceBindingsBySpace(Long.parseLong(spaceId), username));
  }

  @ExoTransactional
  public List<UserSpaceBinding> findUserSpaceBindingsByGroup(String group,
                                                             String userName) throws GroupSpaceBindingStorageException {
    return buildUserBindingListFromEntities(userSpaceBindingDAO.findUserBindingsByGroup(group, userName));
  }

  @ExoTransactional
  public List<UserSpaceBinding> findUserAllBindingsByGroupBinding(GroupSpaceBinding binding) {
    return buildUserBindingListFromEntities(userSpaceBindingDAO.findBoundUsersByBindingId(binding.getId()));
  }

  @ExoTransactional
  public List<UserSpaceBinding> findUserSpaceBindingsByUser(String userName) throws GroupSpaceBindingStorageException {
    return buildUserBindingListFromEntities(userSpaceBindingDAO.findUserAllBindingsByUser(userName));
  }

  @ExoTransactional
  public GroupSpaceBinding saveGroupSpaceBinding(GroupSpaceBinding binding) throws GroupSpaceBindingStorageException {
    GroupSpaceBindingEntity bindingEntity = buildEntityGroupBindingFrom(binding);
    GroupSpaceBindingEntity entity = groupSpaceBindingDAO.create(bindingEntity);
    return fillGroupBindingFromEntity(entity);
  }

  @ExoTransactional
  public GroupSpaceBindingQueue createGroupSpaceBindingQueue(GroupSpaceBindingQueue bindingQueue) throws GroupSpaceBindingStorageException {
    GroupSpaceBindingQueueEntity entity=groupSpaceBindingQueueDAO.create(buildEntityGroupBindingQueueFrom(bindingQueue));
    return fillGroupBindingQueueFromEntity(entity);
  }

  @ExoTransactional
  public UserSpaceBinding saveUserBinding(UserSpaceBinding userSpaceBinding) throws GroupSpaceBindingStorageException {
    UserSpaceBindingEntity entity=
        userSpaceBindingDAO.findUserBindingByGroupBindingIdAndUsername(userSpaceBinding.getGroupBinding().getId(),
                                                                    userSpaceBinding.getUser());
    if (entity==null) {
      entity = userSpaceBindingDAO.create(buildEntityUserBindingFrom(userSpaceBinding));
    }
    return fillUserBindingFromEntity(entity);
  }

  @ExoTransactional
  public GroupSpaceBindingReportAction saveGroupSpaceBindingReport(GroupSpaceBindingReportAction groupSpaceBindingReportAction) throws GroupSpaceBindingStorageException {
    GroupSpaceBindingReportActionEntity entity =
        groupSpaceBindingReportActionDAO.create(buildEntityGroupSpaceBindingReportActionFrom(groupSpaceBindingReportAction));
    return fillGroupBindingReportActionFromEntity(entity);
  }

  @Override
  public void saveGroupSpaceBindingReportUser(GroupSpaceBindingReportUser groupSpaceBindingReportUser) {
    groupSpaceBindingReportUserDAO.create(buildEntityGroupSpaceBindingReportUserFrom(groupSpaceBindingReportUser));
  }

  @Override
  public void updateGroupSpaceBindingReportAction(GroupSpaceBindingReportAction bindingReportAction) {
    GroupSpaceBindingReportActionEntity reportActionEntity = groupSpaceBindingReportActionDAO.find(bindingReportAction.getId());
    reportActionEntity.setEndDate(bindingReportAction.getEndDate());
    groupSpaceBindingReportActionDAO.update(reportActionEntity);
  }

  @Override
  public GroupSpaceBindingReportAction findGroupSpaceBindingReportAction(long bindingId, String action) {
    return fillGroupBindingReportActionFromEntity(groupSpaceBindingReportActionDAO.findGroupSpaceBindingReportAction(bindingId,
                                                                                                                     action));
  }

  @Override
  public List<GroupSpaceBindingQueue> getAllFromBindingQueue() {
    List<GroupSpaceBindingQueueEntity> bindingQueueEntities = groupSpaceBindingQueueDAO.getAllFromBindingQueue();
    return buildGroupSpaceBindingQueueListFromEntities(bindingQueueEntities);
  }
  
  @Override
  public List<GroupSpaceBinding> findAllGroupSpaceBinding() {
    return groupSpaceBindingDAO.findAll()
                               .stream()
                               .map(groupSpaceBindingEntity -> fillGroupBindingFromEntity(groupSpaceBindingEntity))
                               .collect(Collectors.toList());
  }
  
  @Override
  public List<UserSpaceBinding> findAllUserSpaceBinding() {
    return userSpaceBindingDAO.findAll()
                               .stream()
                               .map(userSpaceBindingEntity -> fillUserBindingFromEntity(userSpaceBindingEntity))
                               .collect(Collectors.toList());
  }
  
  @Override
  public List<GroupSpaceBindingQueue> findAllGroupSpaceBindingQueue() {
    return groupSpaceBindingQueueDAO.findAll()
                                    .stream()
                                    .map(groupSpaceBindingQueueEntity -> fillGroupBindingQueueFromEntity(groupSpaceBindingQueueEntity))
                                    .collect(Collectors.toList());
  }
  
  @Override
  public List<GroupSpaceBindingReportAction> findAllGroupSpaceBindingReportAction() {
    return groupSpaceBindingReportActionDAO.findAll()
                                          .stream()
                                          .map(groupSpaceBindingReportActionEntity -> fillGroupBindingReportActionFromEntity(groupSpaceBindingReportActionEntity))
                                          .collect(Collectors.toList());
  }
  
  @Override
  public List<GroupSpaceBindingReportUser> findAllGroupSpaceBindingReportUser() {
    return groupSpaceBindingReportUserDAO.findAll()
                                           .stream()
                                           .map(groupSpaceBindingReportUserEntity -> fillGroupBindingReportUserFromEntity(groupSpaceBindingReportUserEntity))
                                           .collect(Collectors.toList());
  }
  
  @ExoTransactional
  public void deleteGroupBinding(long id) throws GroupSpaceBindingStorageException {
    GroupSpaceBindingEntity bindingEntity = groupSpaceBindingDAO.find(id);
    if (bindingEntity != null) {
      groupSpaceBindingDAO.delete(bindingEntity);
    } else {
      LOG.warn("The GroupSpaceBinding's {} not found.", id);
    }
  }

  @Override
  public void deleteGroupBindingReport(long id) throws GroupSpaceBindingStorageException {
    groupSpaceBindingReportActionDAO.delete(groupSpaceBindingReportActionDAO.find(id));

  }
  @Override
  public void deleteGroupBindingReportUser(long id) throws GroupSpaceBindingStorageException {
    groupSpaceBindingReportUserDAO.delete(groupSpaceBindingReportUserDAO.find(id));
    
  }
  @ExoTransactional
  public void deleteGroupBindingQueue(long id) throws GroupSpaceBindingStorageException {
    groupSpaceBindingQueueDAO.delete(groupSpaceBindingQueueDAO.find(id));
  }

  @ExoTransactional
  public void deleteUserBinding(long id) throws GroupSpaceBindingStorageException {
    userSpaceBindingDAO.delete(userSpaceBindingDAO.find(id));
  }

  @ExoTransactional
  public void deleteAllUserBindings(String userName) throws GroupSpaceBindingStorageException {
    userSpaceBindingDAO.deleteAllUserBindings(userName);
  }

  @Override
  public long countUserBindings(String spaceId, String userName) throws GroupSpaceBindingStorageException {
    return userSpaceBindingDAO.countUserBindings(Long.parseLong(spaceId), userName);
  }

  @Override
  public List<UserSpaceBinding> findBoundUsersByBindingId(long id) {
    return buildUserBindingListFromEntities(userSpaceBindingDAO.findBoundUsersByBindingId(id));
  }

  @Override
  public boolean isUserBoundAndMemberBefore(String spaceId, String userId) {
    return userSpaceBindingDAO.isUserBoundAndMemberBefore(Long.parseLong(spaceId), userId);
  }

  @Override
  public GroupSpaceBinding findGroupSpaceBindingById(String bindingId) {
    GroupSpaceBindingEntity entity;
    entity = groupSpaceBindingDAO.find(Long.parseLong(bindingId));
    return fillGroupBindingFromEntity(entity);
  }

  @Override
  public List<GroupSpaceBinding> getGroupSpaceBindingsFromQueueByAction(String action) {
    return buildGroupBindingListFromEntities(groupSpaceBindingQueueDAO.getGroupSpaceBindingsFromQueueByAction(action));
  }

  @Override
  public boolean isBoundSpace(String spaceId) {
    long countSpaceRemovedBindings =
                                   groupSpaceBindingQueueDAO.getGroupSpaceBindingsFromQueueByAction(GroupSpaceBindingQueue.ACTION_REMOVE)
                                                            .stream()
                                                            .filter(groupSpaceBindingEntity -> groupSpaceBindingEntity.getSpace()
                                                                                                                      .getId()
                                                                                                                      .equals(Long.parseLong(spaceId)))
                                                            .count();
    return groupSpaceBindingDAO.findGroupSpaceBindingsBySpace(Long.parseLong(spaceId)).size() > countSpaceRemovedBindings;
  }

  @Override
  public long countBoundUsers(String spaceId) {
    return userSpaceBindingDAO.countBoundUsers(Long.parseLong(spaceId));
  }

  public List<GroupSpaceBindingReportUser> findReportsForCsv(long spaceId,
                                                               long groupSpaceBindingId,
                                                               String group,
                                                               String action) {

    return buildGroupBindingReportUserListFromEntities(groupSpaceBindingReportUserDAO.findReportsForCSV(spaceId,
                                                                                                      groupSpaceBindingId,
                                                                                                      group,
                                                                                                      action));
  }

  @Override
  public List<GroupSpaceBindingOperationReport> getGroupSpaceBindingReportOperations() {
    List<GroupSpaceBindingOperationReport> bindingOperationReports = new ArrayList<>();
    List<GroupSpaceBindingReportAction> bindingReportActions =
                                                             buildGroupBindingReportListFromEntities(groupSpaceBindingReportActionDAO.getGroupSpaceBindingReportActionsOrderedByEndDate());
    for (GroupSpaceBindingReportAction bindingReportAction : bindingReportActions) {
      List<GroupSpaceBindingReportUser> bindingReportUsers =
                                                           buildGroupBindingReportUserListFromEntities(groupSpaceBindingReportUserDAO.findBindingReportUsersByBindingReportAction(bindingReportAction.getId()));
      long addedUsers = 0;
      long removedUsers = 0;
      switch (bindingReportAction.getAction()) {
      case GroupSpaceBindingReportAction.ADD_ACTION:
        addedUsers = bindingReportUsers.size();
        removedUsers = 0;
        bindingOperationReports.add(new GroupSpaceBindingOperationReport(bindingReportAction.getSpaceId(),
                                                                         bindingReportAction.getGroup(),
                                                                         bindingReportAction.getAction(),
                                                                         bindingReportAction.getGroupSpaceBindingId(),
                                                                         addedUsers,
                                                                         removedUsers,
                                                                         bindingReportAction.getStartDate(),
                                                                         bindingReportAction.getEndDate()));
        break;
      case GroupSpaceBindingReportAction.REMOVE_ACTION:
        addedUsers = 0;
        removedUsers = bindingReportUsers.size();
        bindingOperationReports.add(new GroupSpaceBindingOperationReport(bindingReportAction.getSpaceId(),
                                                                         bindingReportAction.getGroup(),
                                                                         bindingReportAction.getAction(),
                                                                         bindingReportAction.getGroupSpaceBindingId(),
                                                                         addedUsers,
                                                                         removedUsers,
                                                                         bindingReportAction.getStartDate(),
                                                                         bindingReportAction.getEndDate()));
        break;
      default:
        addedUsers =
                   bindingReportUsers.stream()
                                     .filter(bindingReportUser -> bindingReportUser.getAction()
                                                                                   .equals(GroupSpaceBindingReportUser.ACTION_ADD_USER))
                                     .count();
        removedUsers =
                     bindingReportUsers.stream()
                                       .filter(bindingReportUser -> bindingReportUser.getAction()
                                                                                     .equals(GroupSpaceBindingReportUser.ACTION_REMOVE_USER))
                                       .count();

        bindingOperationReports.add(new GroupSpaceBindingOperationReport(bindingReportAction.getSpaceId(),
                                                                         bindingReportAction.getGroup(),
                                                                         bindingReportAction.getAction(),
                                                                         bindingReportAction.getGroupSpaceBindingId(),
                                                                         addedUsers,
                                                                         removedUsers,
                                                                         bindingReportAction.getStartDate(),
                                                                         bindingReportAction.getEndDate()));
        break;
      }
    }
    return bindingOperationReports;
  }

  private List<GroupSpaceBindingReportAction> buildGroupBindingReportListFromEntities(List<GroupSpaceBindingReportActionEntity> entities) {
    List<GroupSpaceBindingReportAction> groupSpaceBindingsReports;
    groupSpaceBindingsReports =
                              entities.stream()
                                      .map(groupSpaceBindingReportActionEntity -> fillGroupBindingReportActionFromEntity(groupSpaceBindingReportActionEntity))
                                      .collect(Collectors.toList());
    return groupSpaceBindingsReports;
  }

  private List<GroupSpaceBindingReportUser> buildGroupBindingReportUserListFromEntities(List<GroupSpaceBindingReportUserEntity> bindingReportUserEntities) {
    List<GroupSpaceBindingReportUser> bindingReportUsers =
                                                         bindingReportUserEntities.stream()
                                                                                  .map(groupSpaceBindingReportUserEntity -> fillGroupBindingReportUserFromEntity(groupSpaceBindingReportUserEntity))
                                                                                  .collect(Collectors.toList());
    return bindingReportUsers;
  }

  private List<GroupSpaceBindingQueue> buildGroupSpaceBindingQueueListFromEntities(List<GroupSpaceBindingQueueEntity> bindingQueueEntities) {
    List<GroupSpaceBindingQueue> bindingQueueList =
                                                  bindingQueueEntities.stream()
                                                                      .map(bindingQueueEntity -> fillGroupBindingQueueFromEntity(bindingQueueEntity))
                                                                      .collect(Collectors.toList());
    return bindingQueueList;
  }

  private GroupSpaceBindingReportActionEntity buildEntityGroupSpaceBindingReportActionFrom(GroupSpaceBindingReportAction groupSpaceBindingReportAction) {
    GroupSpaceBindingReportActionEntity groupSpaceBindingReportActionEntity = new GroupSpaceBindingReportActionEntity();
    groupSpaceBindingReportActionEntity.setGroupSpaceBindingId(groupSpaceBindingReportAction.getGroupSpaceBindingId());
    SpaceEntity spaceEntity = spaceDAO.find(groupSpaceBindingReportAction.getSpaceId());
    groupSpaceBindingReportActionEntity.setSpace(spaceEntity);
    groupSpaceBindingReportActionEntity.setGroup(groupSpaceBindingReportAction.getGroup());
    groupSpaceBindingReportActionEntity.setAction(groupSpaceBindingReportAction.getAction());
    groupSpaceBindingReportActionEntity.setStartDate(groupSpaceBindingReportAction.getStartDate());
    groupSpaceBindingReportActionEntity.setEndDate(groupSpaceBindingReportAction.getEndDate());
    return groupSpaceBindingReportActionEntity;
  }

  private GroupSpaceBindingReportUserEntity buildEntityGroupSpaceBindingReportUserFrom(GroupSpaceBindingReportUser groupSpaceBindingReportUser) {
    GroupSpaceBindingReportUserEntity groupSpaceBindingReportUserEntity = new GroupSpaceBindingReportUserEntity();
    groupSpaceBindingReportUserEntity.setGroupSpaceBindingReportAction(groupSpaceBindingReportActionDAO.find(groupSpaceBindingReportUser.getGroupSpaceBindingReportAction()
                                                                                                                                        .getId()));
    groupSpaceBindingReportUserEntity.setUser(groupSpaceBindingReportUser.getUsername());
    groupSpaceBindingReportUserEntity.setAction(groupSpaceBindingReportUser.getAction());
    groupSpaceBindingReportUserEntity.setWasPresentBefore(groupSpaceBindingReportUser.isWasPresentBefore());
    groupSpaceBindingReportUserEntity.setStillInSpace(groupSpaceBindingReportUser.isStillInSpace());
    groupSpaceBindingReportUserEntity.setDate(groupSpaceBindingReportUser.getDate());
    return groupSpaceBindingReportUserEntity;
  }

  private GroupSpaceBindingReportAction fillGroupBindingReportActionFromEntity(GroupSpaceBindingReportActionEntity entity) {
    if (entity == null) {
      return null;
    }
    GroupSpaceBindingReportAction groupSpaceBindingReportAction =
                                                                new GroupSpaceBindingReportAction(entity.getGroupSpaceBindingId(),
                                                                                                  entity.getSpace().getId(),
                                                                                                  entity.getGroup(),
                                                                                                  entity.getAction());

    groupSpaceBindingReportAction.setId(entity.getId());
    groupSpaceBindingReportAction.setStartDate(entity.getStartDate());
    groupSpaceBindingReportAction.setEndDate(entity.getEndDate());
    return groupSpaceBindingReportAction;
  }

  private GroupSpaceBindingReportUser fillGroupBindingReportUserFromEntity(GroupSpaceBindingReportUserEntity entity) {
    if (entity == null) {
      return null;
    }
    GroupSpaceBindingReportUser groupSpaceBindingReportUser = new GroupSpaceBindingReportUser();
    groupSpaceBindingReportUser.setId(entity.getId());
    groupSpaceBindingReportUser.setGroupSpaceBindingReportAction(fillGroupBindingReportActionFromEntity(entity.getGroupSpaceBindingReportAction()));
    groupSpaceBindingReportUser.setUsername(entity.getUser());
    groupSpaceBindingReportUser.setAction(entity.getAction());
    groupSpaceBindingReportUser.setWasPresentBefore(entity.isWasPresentBefore());
    groupSpaceBindingReportUser.setStillInSpace(entity.isStillInSpace());
    groupSpaceBindingReportUser.setDate(entity.getDate());
    return groupSpaceBindingReportUser;
  }

  /**
   * Fills {@link GroupSpaceBinding}'s properties to
   * {@link GroupSpaceBindingEntity}'s.
   *
   * @param entity the GroupSpaceBinding entity
   */
  private GroupSpaceBinding fillGroupBindingFromEntity(GroupSpaceBindingEntity entity) {
    if (entity == null) {
      return null;
    }
    GroupSpaceBinding groupSpaceBinding = new GroupSpaceBinding();
    groupSpaceBinding.setId(entity.getId());
    String spaceId = Long.toString(entity.getSpace().getId());
    groupSpaceBinding.setSpaceId(spaceId);
    groupSpaceBinding.setGroup(entity.getGroup());
    return groupSpaceBinding;
  }

  /**
   * Fills {@link GroupSpaceBinding}'s properties to
   * {@link GroupSpaceBindingEntity}'s.
   *
   * @param bindingQueueEntity the GroupSpaceBinding entity
   */
  private GroupSpaceBindingQueue fillGroupBindingQueueFromEntity(GroupSpaceBindingQueueEntity bindingQueueEntity) {
    if (bindingQueueEntity == null) {
      return null;
    }
    GroupSpaceBindingQueue groupSpaceBindingQueue = new GroupSpaceBindingQueue();
    groupSpaceBindingQueue.setId(bindingQueueEntity.getId());
    groupSpaceBindingQueue.setGroupSpaceBinding(fillGroupBindingFromEntity(bindingQueueEntity.getGroupSpaceBindingEntity()));
    groupSpaceBindingQueue.setAction(bindingQueueEntity.getAction());
    return groupSpaceBindingQueue;
  }

  /**
   * Fills {@link UserSpaceBinding}'s properties to
   * {@link UserSpaceBindingEntity}'s.
   *
   * @param entity the UserSpaceBinding entity
   */
  private UserSpaceBinding fillUserBindingFromEntity(UserSpaceBindingEntity entity) {
    if (entity == null) {
      return null;
    }
    UserSpaceBinding userSpaceBinding = new UserSpaceBinding();
    userSpaceBinding.setId(entity.getId());
    userSpaceBinding.setUser(entity.getUser());
    userSpaceBinding.setIsMemberBefore(entity.isMemberBefore());
    userSpaceBinding.setGroupBinding(fillGroupBindingFromEntity(entity.getGroupSpaceBinding()));
    return userSpaceBinding;
  }

  /**
   * build {@link GroupSpaceBinding}'s list from {@link GroupSpaceBindingEntity}'s
   * list.
   *
   * @param entities the list of entities
   */
  private List<GroupSpaceBinding> buildGroupBindingListFromEntities(List<GroupSpaceBindingEntity> entities) {
    List<GroupSpaceBinding> groupSpaceBindings = new LinkedList<>();
    for (GroupSpaceBindingEntity entity : entities) {
      GroupSpaceBinding groupSpaceBinding = fillGroupBindingFromEntity(entity);
      groupSpaceBindings.add(groupSpaceBinding);
    }
    return groupSpaceBindings;
  }

  /**
   * build {@link UserSpaceBinding}'s list from {@link UserSpaceBindingEntity}'s
   * list.
   *
   * @param entities the list of entities
   */
  private List<UserSpaceBinding> buildUserBindingListFromEntities(List<UserSpaceBindingEntity> entities) {
    List<UserSpaceBinding> userSpaceBindings = new LinkedList<>();
    for (UserSpaceBindingEntity entity : entities) {
      UserSpaceBinding userSpaceBinding = fillUserBindingFromEntity(entity);
      userSpaceBindings.add(userSpaceBinding);
    }
    return userSpaceBindings;
  }

  /**
   * build {@link UserSpaceBinding}'s list from {@link UserSpaceBindingEntity}'s
   * list.
   * 
   * @param userSpaceBindingEntities
   * @return
   */
  private List<UserSpaceBinding> buildUserSpaceBindingsListFromEntities(List<UserSpaceBindingEntity> userSpaceBindingEntities) {
    List<UserSpaceBinding> userSpaceBindings = new ArrayList<>();
    userSpaceBindingEntities.stream().forEach(entity -> userSpaceBindings.add(fillUserBindingFromEntity(entity)));
    return userSpaceBindings;
  }

  /**
   * build {@link GroupSpaceBindingEntity} from {@link GroupSpaceBinding} object.
   *
   * @param groupSpaceBinding the GroupSpaceBinding object
   */
  private GroupSpaceBindingEntity buildEntityGroupBindingFrom(GroupSpaceBinding groupSpaceBinding) {
    GroupSpaceBindingEntity groupSpaceBindingEntity = new GroupSpaceBindingEntity();
    groupSpaceBindingEntity.setId(groupSpaceBinding.getId());
    groupSpaceBindingEntity.setGroup(groupSpaceBinding.getGroup());
    Long spaceId = Long.parseLong(groupSpaceBinding.getSpaceId());
    SpaceEntity spaceEntity = spaceDAO.find(spaceId);
    groupSpaceBindingEntity.setSpace(spaceEntity);
    return groupSpaceBindingEntity;
  }

  /**
   * build {@link GroupSpaceBindingQueueEntity} from
   * {@link GroupSpaceBindingQueue} object.
   *
   * @param groupSpaceBindingQueue the GroupSpaceBinding object
   */
  private GroupSpaceBindingQueueEntity buildEntityGroupBindingQueueFrom(GroupSpaceBindingQueue groupSpaceBindingQueue) {
    GroupSpaceBindingQueueEntity groupSpaceBindingQueueEntity = new GroupSpaceBindingQueueEntity();
    groupSpaceBindingQueueEntity.setGroupSpaceBindingEntity(buildEntityGroupBindingFrom(groupSpaceBindingQueue.getGroupSpaceBinding()));
    groupSpaceBindingQueueEntity.setAction(groupSpaceBindingQueue.getAction());
    return groupSpaceBindingQueueEntity;
  }

  /**
   * build {@link UserSpaceBindingEntity} from {@link UserSpaceBinding} object.
   *
   * @param userSpaceBinding the UserSpaceBinding object
   */
  private UserSpaceBindingEntity buildEntityUserBindingFrom(UserSpaceBinding userSpaceBinding) {
    UserSpaceBindingEntity userSpaceBindingEntity = new UserSpaceBindingEntity();
    userSpaceBindingEntity.setUser(userSpaceBinding.getUser());
    userSpaceBindingEntity.setIsMemberBefore(userSpaceBinding.isMemberBefore());
    GroupSpaceBindingEntity groupBindingEntity = groupSpaceBindingDAO.find(userSpaceBinding.getGroupBinding().getId());
    userSpaceBindingEntity.setGroupSpaceBinding(groupBindingEntity);
    return userSpaceBindingEntity;
  }

}
