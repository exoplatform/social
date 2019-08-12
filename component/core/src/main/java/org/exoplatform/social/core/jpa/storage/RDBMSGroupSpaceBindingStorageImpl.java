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

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.jpa.storage.dao.GroupSpaceBindingDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.dao.UserSpaceBindingDAO;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingEntity;
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

  private UserSpaceBindingDAO                                        userSpaceBindingDAO;

  public RDBMSGroupSpaceBindingStorageImpl(GroupSpaceBindingDAO groupSpaceBindingDAO,
                                           UserSpaceBindingDAO userSpaceBindingDAO,
                                           SpaceDAO spaceDAO) {
    this.groupSpaceBindingDAO = groupSpaceBindingDAO;
    this.userSpaceBindingDAO = userSpaceBindingDAO;
    this.spaceDAO = spaceDAO;
  }

  public List<GroupSpaceBinding> findGroupSpaceBindingsBySpace(String spaceId, String role) throws GroupSpaceBindingStorageException {
    return buildGroupBindingListFromEntities(groupSpaceBindingDAO.findGroupSpaceBindingsBySpace(Long.parseLong(spaceId), role));
  }

  public List<GroupSpaceBinding> findGroupSpaceBindingsByGroup(String group, String role) throws GroupSpaceBindingStorageException {
    return buildGroupBindingListFromEntities(groupSpaceBindingDAO.findGroupSpaceBindingsByGroup(group, role));
  }

  public List<UserSpaceBinding> findUserSpaceBindingsBySpace(String spaceId, String username) throws GroupSpaceBindingStorageException {
    return buildUserBindingListFromEntities(userSpaceBindingDAO.findUserBindingsBySpace(Long.parseLong(spaceId), username));
  }

  public List<UserSpaceBinding> findUserSpaceBindingsByGroup(String group,
                                                             String groupRole,
                                                             String userName) throws GroupSpaceBindingStorageException {
    return buildUserBindingListFromEntities(userSpaceBindingDAO.findUserBindingsByGroup(group, groupRole, userName));
  }

  public List<UserSpaceBinding> findUserAllBindingsbyGroupMembership(String group, String groupRole) {
    return buildUserBindingListFromEntities(userSpaceBindingDAO.findUserAllBindingsByGroupMembership(group, groupRole));
  }

  public List<UserSpaceBinding> findUserSpaceBindingsByUser(String userName) throws GroupSpaceBindingStorageException {
    return buildUserBindingListFromEntities(userSpaceBindingDAO.findUserAllBindingsByUser(userName));
  }

  @ExoTransactional
  public GroupSpaceBinding saveGroupBinding(GroupSpaceBinding binding, boolean isNew) throws GroupSpaceBindingStorageException {
    GroupSpaceBindingEntity entity;
    if (isNew) {
      entity = groupSpaceBindingDAO.create(buildEntityGroupBindingFrom(binding));
    } else {
      Long id = binding.getId();
      entity = groupSpaceBindingDAO.find(id);
      if (entity != null) {
        entity = buildEntityGroupBindingFrom(binding);
        entity.setId(id);
        groupSpaceBindingDAO.update(entity);
      }
    }
    return fillGroupBindingFromEntity(entity);
  }

  @ExoTransactional
  public UserSpaceBinding saveUserBinding(UserSpaceBinding binding) throws GroupSpaceBindingStorageException {
    UserSpaceBindingEntity entity;
    entity = userSpaceBindingDAO.create(buildEntityUserBindingFrom(binding));
    return fillUserBindingFromEntity(entity);
  }

  @ExoTransactional
  public void deleteGroupBinding(long id) throws GroupSpaceBindingStorageException {
    groupSpaceBindingDAO.delete(groupSpaceBindingDAO.find(id));
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
  public boolean hasUserBindings(String spaceId, String userName) {
    return userSpaceBindingDAO.hasUserBindings(Long.parseLong(spaceId), userName);
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
    groupSpaceBinding.setGroup(entity.getGroup());
    groupSpaceBinding.setGroupRole(entity.getGroupRole());
    String spaceId = Long.toString(entity.getSpace().getId());
    groupSpaceBinding.setSpaceId(spaceId);
    groupSpaceBinding.setSpaceRole(entity.getSpaceRole());
    return groupSpaceBinding;
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
    userSpaceBinding.setGroupBinding(fillGroupBindingFromEntity(entity.getGroupSpaceBinding()));
    String spaceId = Long.toString(entity.getSpace().getId());
    userSpaceBinding.setSpaceId(spaceId);
    userSpaceBinding.setUser(entity.getUser());
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
   * build {@link GroupSpaceBindingEntity} from {@link GroupSpaceBinding} object
   * list.
   *
   * @param groupSpaceBinding the GroupSpaceBinding object
   */
  private GroupSpaceBindingEntity buildEntityGroupBindingFrom(GroupSpaceBinding groupSpaceBinding) {
    GroupSpaceBindingEntity groupSpaceBindingEntity = new GroupSpaceBindingEntity();
    groupSpaceBindingEntity.setGroup(groupSpaceBinding.getGroup());
    groupSpaceBindingEntity.setGroupRole(groupSpaceBinding.getGroupRole());
    Long spaceId = Long.parseLong(groupSpaceBinding.getSpaceId());
    SpaceEntity entity = spaceDAO.find(spaceId);
    groupSpaceBindingEntity.setSpace(entity);
    groupSpaceBindingEntity.setSpaceRole(groupSpaceBinding.getSpaceRole());
    return groupSpaceBindingEntity;
  }

  /**
   * build {@link UserSpaceBindingEntity} from {@link UserSpaceBinding} object
   * list.
   *
   * @param userSpaceBinding the UserSpaceBinding object
   */
  private UserSpaceBindingEntity buildEntityUserBindingFrom(UserSpaceBinding userSpaceBinding) {
    UserSpaceBindingEntity userSpaceBindingEntity = new UserSpaceBindingEntity();
    userSpaceBindingEntity.setUser(userSpaceBinding.getUser());
    GroupSpaceBindingEntity groupBindingEntity = groupSpaceBindingDAO.find(userSpaceBinding.getGroupBinding().getId());
    userSpaceBindingEntity.setGroupSpaceBinding(groupBindingEntity);
    Long spaceId = Long.parseLong(userSpaceBinding.getSpaceId());
    SpaceEntity spaceEntity = spaceDAO.find(spaceId);
    userSpaceBindingEntity.setSpace(spaceEntity);
    return userSpaceBindingEntity;
  }

}
