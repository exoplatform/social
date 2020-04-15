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

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingQueue;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.jpa.storage.dao.GroupSpaceBindingDAO;
import org.exoplatform.social.core.jpa.storage.dao.GroupSpaceBindingQueueDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.dao.UserSpaceBindingDAO;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingEntity;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingQueueEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.UserSpaceBindingEntity;
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

  public RDBMSGroupSpaceBindingStorageImpl(GroupSpaceBindingDAO groupSpaceBindingDAO,
                                           GroupSpaceBindingQueueDAO groupSpaceBindingQueueDAO,
                                           UserSpaceBindingDAO userSpaceBindingDAO,
                                           SpaceDAO spaceDAO) {
    this.groupSpaceBindingDAO = groupSpaceBindingDAO;
    this.groupSpaceBindingQueueDAO = groupSpaceBindingQueueDAO;
    this.userSpaceBindingDAO = userSpaceBindingDAO;
    this.spaceDAO = spaceDAO;
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
  public List<UserSpaceBinding> findUserAllBindingsByGroup(String group) {
    return buildUserBindingListFromEntities(userSpaceBindingDAO.findUserAllBindingsByGroup(group));
  }

  @ExoTransactional
  public List<UserSpaceBinding> findUserSpaceBindingsByUser(String userName) throws GroupSpaceBindingStorageException {
    return buildUserBindingListFromEntities(userSpaceBindingDAO.findUserAllBindingsByUser(userName));
  }

  @ExoTransactional
  public GroupSpaceBinding saveGroupSpaceBinding(GroupSpaceBinding binding) throws GroupSpaceBindingStorageException {
    GroupSpaceBindingEntity bindingEntity = buildEntityGroupBindingFrom(binding);
    bindingEntity.setId(0);
    bindingEntity = groupSpaceBindingDAO.create(bindingEntity);
    return fillGroupBindingFromEntity(bindingEntity);
  }

  @ExoTransactional
  public GroupSpaceBindingQueue createGroupSpaceBindingQueue(GroupSpaceBindingQueue bindingQueue) throws GroupSpaceBindingStorageException {
    GroupSpaceBindingQueueEntity entity = groupSpaceBindingQueueDAO.create(buildEntityGroupBindingQueueFrom(bindingQueue));
    return fillGroupBindingQueueFromEntity(entity);
  }

  @ExoTransactional
  public UserSpaceBinding saveUserBinding(UserSpaceBinding userSpaceBinding) throws GroupSpaceBindingStorageException {
    UserSpaceBindingEntity entity;
    entity = userSpaceBindingDAO.create(buildEntityUserBindingFrom(userSpaceBinding));
    return fillUserBindingFromEntity(entity);
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
