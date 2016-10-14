/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.rest.IdentityAvatarRestService;
import org.exoplatform.social.core.jpa.search.XSpaceFilter;
import org.exoplatform.social.core.jpa.storage.dao.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceMemberDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.query.SpaceQueryBuilder;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.SpaceStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.impl.SpaceStorageImpl;

public class RDBMSSpaceStorageImpl extends SpaceStorageImpl implements SpaceStorage {

  /** Logger */
  private static final Log     LOG = ExoLogger.getLogger(RDBMSSpaceStorageImpl.class);

  private SpaceDAO spaceDAO;

  private SpaceMemberDAO       spaceMemberDAO;

  private IdentityStorage      identityStorage;

  public RDBMSSpaceStorageImpl(SpaceDAO spaceDAO,
                               SpaceMemberDAO spaceMemberDAO,
                               RDBMSIdentityStorageImpl identityStorage) {
    super(identityStorage, null);
    this.spaceDAO = spaceDAO;
    this.identityStorage = identityStorage;
    this.spaceMemberDAO = spaceMemberDAO;
  }

  @Override
  @ExoTransactional
  public void deleteSpace(String id) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.find(Long.parseLong(id));
    if (entity != null) {
      // spaceMemberDAO.deleteBySpace(entity);
      spaceDAO.delete(entity);

      LOG.debug("Space {} removed", entity.getPrettyName());
    }
  }

  @Override
  public List<Space> getAccessibleSpaces(String userId) throws SpaceStorageException {
    return getAccessibleSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getAccessibleSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getAccessibleSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getAccessibleSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, Arrays.asList(SpaceMemberEntity.Status.MANAGER, SpaceMemberEntity.Status.MEMBER), spaceFilter, offset, limit);
  }

  @Override
  public int getAccessibleSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, Arrays.asList(SpaceMemberEntity.Status.MANAGER, SpaceMemberEntity.Status.MEMBER), spaceFilter);
  }

  @Override
  public int getAccessibleSpacesCount(String userId) throws SpaceStorageException {
    return getAccessibleSpacesByFilterCount(userId, null);
  }

  @Override
  public List<Space> getAllSpaces() throws SpaceStorageException {
    return getSpaces(0, -1);
  }

  @Override
  public int getAllSpacesByFilterCount(SpaceFilter spaceFilter) {
    return getSpacesCount(null, null, spaceFilter);
  }

  @Override
  public int getAllSpacesCount() throws SpaceStorageException {
    return getAllSpacesByFilterCount(null);
  }

  @Override
  public List<Space> getEditableSpaces(String userId) throws SpaceStorageException {
    return getEditableSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getEditableSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getEditableSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getEditableSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, Arrays.asList(SpaceMemberEntity.Status.MANAGER), spaceFilter, offset, limit);
  }

  @Override
  public int getEditableSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, Arrays.asList(SpaceMemberEntity.Status.MANAGER), spaceFilter);
  }

  @Override
  public int getEditableSpacesCount(String userId) throws SpaceStorageException {
    return getEditableSpacesByFilterCount(userId, null);
  }

  @Override
  public List<Space> getInvitedSpaces(String userId) throws SpaceStorageException {
    return getInvitedSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getInvitedSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getInvitedSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getInvitedSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, Arrays.asList(SpaceMemberEntity.Status.INVITED), spaceFilter, offset, limit);
  }

  @Override
  public int getInvitedSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, Arrays.asList(SpaceMemberEntity.Status.INVITED), spaceFilter);
  }

  @Override
  public int getInvitedSpacesCount(String userId) throws SpaceStorageException {
    return getInvitedSpacesByFilterCount(userId, null);
  }

  @Override
  public List<Space> getLastAccessedSpace(SpaceFilter spaceFilter, int offset, int limit) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter);
    xFilter.setLastAccess(true);
    return getMemberSpacesByFilter(spaceFilter.getRemoteId(), xFilter, offset, limit);
  }

  @Override
  public int getLastAccessedSpaceCount(SpaceFilter spaceFilter) throws SpaceStorageException {
    return getMemberSpacesByFilterCount(spaceFilter.getRemoteId(), spaceFilter);
  }

  @Override
  public List<Space> getLastSpaces(int limit) {
    List<SpaceEntity> entities = spaceDAO.getLastSpaces(limit);
    return buildList(entities);
  }

  @Override
  public List<String> getMemberSpaceIds(String identityId, int offset, int limit) throws SpaceStorageException {
    Identity identity = identityStorage.findIdentityById(identityId);
    List<Space> spaces = getMemberSpaces(identity.getRemoteId(), offset, limit);

    List<String> ids = new LinkedList<>();
    for (Space space : spaces) {
      Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        ids.add(spaceIdentity.getId());
      }
    }
    return ids;
  }

  @Override
  public List<Space> getMemberSpaces(String userId) throws SpaceStorageException {
    return getMemberSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getMemberSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getMemberSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getMemberSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, Arrays.asList(SpaceMemberEntity.Status.MEMBER), spaceFilter, offset, limit);
  }

  @Override
  public int getMemberSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, Arrays.asList(SpaceMemberEntity.Status.MEMBER), spaceFilter);
  }

  @Override
  public int getMemberSpacesCount(String userId) throws SpaceStorageException {
    return getMemberSpacesByFilterCount(userId, null);
  }

  @Override
  public int getNumberOfMemberPublicSpaces(String userId) {
    XSpaceFilter filter = new XSpaceFilter();
    filter.setNotHidden(true);
    return getSpacesCount(userId, Arrays.asList(SpaceMemberEntity.Status.MEMBER), filter);
  }

  @Override
  public List<Space> getPendingSpaces(String userId) throws SpaceStorageException {
    return getPendingSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getPendingSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getPendingSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getPendingSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(userId, Arrays.asList(SpaceMemberEntity.Status.PENDING), spaceFilter, offset, limit);
  }

  @Override
  public int getPendingSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getSpacesCount(userId, Arrays.asList(SpaceMemberEntity.Status.PENDING), spaceFilter);
  }

  @Override
  public int getPendingSpacesCount(String userId) throws SpaceStorageException {
    return getPendingSpacesByFilterCount(userId, null);
  }

  @Override
  public List<Space> getPublicSpaces(String userId) throws SpaceStorageException {
    return getPublicSpaces(userId, 0, -1);
  }

  @Override
  public List<Space> getPublicSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    return getPublicSpacesByFilter(userId, null, offset, limit);
  }

  @Override
  public List<Space> getPublicSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    XSpaceFilter filter = new XSpaceFilter();
    filter.setSpaceFilter(spaceFilter);
    filter.setPublic(userId);
    return getSpacesByFilter(filter, offset, limit);
  }

  @Override
  public int getPublicSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    XSpaceFilter filter = new XSpaceFilter();
    filter.setSpaceFilter(spaceFilter);
    filter.setPublic(userId);
    return getSpacesCount(null, null, filter);
  }

  @Override
  public int getPublicSpacesCount(String userId) throws SpaceStorageException {
    return getPublicSpacesByFilterCount(userId, null);
  }

  @Override
  public Space getSpaceByDisplayName(String spaceDisplayName) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByDisplayName(spaceDisplayName);
    return fillSpaceFromEntity(entity);
  }

  @Override
  public Space getSpaceByGroupId(String groupId) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByGroupId(groupId);
    return fillSpaceFromEntity(entity);
  }

  @Override
  @ExoTransactional
  public Space getSpaceById(String id) throws SpaceStorageException {
    Long spaceId;
    try {
      spaceId = Long.parseLong(id);
    } catch (Exception ex) {
      return null;
    }
    SpaceEntity entity = spaceDAO.find(spaceId);
    return fillSpaceFromEntity(entity);
  }

  @Override
  @ExoTransactional
  public Space getSpaceByPrettyName(String spacePrettyName) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByPrettyName(spacePrettyName);
    return fillSpaceFromEntity(entity);
  }

  @Override
  public Space getSpaceByUrl(String url) throws SpaceStorageException {
    SpaceEntity entity = spaceDAO.getSpaceByURL(url);
    return fillSpaceFromEntity(entity);
  }

  @Override
  public Space getSpaceSimpleById(String id) throws SpaceStorageException {
    Long spaceId;
    try {
      spaceId = Long.parseLong(id);
    } catch (Exception ex) {
      return null;
    }
    SpaceEntity entity = spaceDAO.find(spaceId);
    Space space = new Space();
    return fillSpaceSimpleFromEntity(entity, space);
  }

  @Override
  public List<Space> getSpaces(long offset, long limit) throws SpaceStorageException {
    return getSpacesByFilter(null, offset, limit);
  }

  @Override
  public List<Space> getSpacesByFilter(SpaceFilter spaceFilter, long offset, long limit) {
    return getSpaces(null, null, spaceFilter, offset, limit);
  }

  @Override
  public List<Space> getUnifiedSearchSpaces(String userId,
                                            SpaceFilter spaceFilter,
                                            long offset,
                                            long limit) throws SpaceStorageException {
//    XSpaceFilter xFilter = new XSpaceFilter();
//    xFilter.setSpaceFilter(spaceFilter).setUnifiedSearch(true);
//    return getSpacesByFilter(xFilter, offset, limit);
    throw new UnsupportedOperationException();
  }

  @Override
  public int getUnifiedSearchSpacesCount(String userId, SpaceFilter spaceFilter) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter).setUnifiedSearch(true);
    return getSpacesCount(null, null, xFilter);
  }

  @Override
  public List<Space> getVisibleSpaces(String userId, SpaceFilter spaceFilter) throws SpaceStorageException {
    return getVisibleSpaces(userId, spaceFilter, 0, -1);
  }

  @Override
  public List<Space> getVisibleSpaces(String userId,
                                      SpaceFilter spaceFilter,
                                      long offset,
                                      long limit) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter).setRemoteId(userId);
    xFilter.addStatus(SpaceMemberEntity.Status.MEMBER, SpaceMemberEntity.Status.MANAGER, SpaceMemberEntity.Status.INVITED);
    xFilter.setIncludePrivate(true);
    return getSpacesByFilter(xFilter, offset, limit);
  }

  @Override
  public int getVisibleSpacesCount(String userId, SpaceFilter spaceFilter) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter).setRemoteId(userId);
    xFilter.addStatus(SpaceMemberEntity.Status.MEMBER, SpaceMemberEntity.Status.MANAGER, SpaceMemberEntity.Status.INVITED);
    xFilter.setIncludePrivate(true);
    return getSpacesCount(userId, null, xFilter);
  }

  @Override
  public List<Space> getVisitedSpaces(SpaceFilter spaceFilter, int offset, int limit) throws SpaceStorageException {
    XSpaceFilter xFilter = new XSpaceFilter();
    xFilter.setSpaceFilter(spaceFilter);
    xFilter.setVisited(true);
    return getMemberSpacesByFilter(spaceFilter.getRemoteId(), xFilter, offset, limit);
  }

  @Override
  @ExoTransactional
  public void renameSpace(Space space, String newDisplayName) throws SpaceStorageException {
    renameSpace(null, space, newDisplayName);
  }

  @Override
  @ExoTransactional
  public void renameSpace(String remoteId, Space space, String newDisplayName) throws SpaceStorageException {
    SpaceEntity entity;

    try {
      String oldPrettyName = space.getPrettyName();

      space.setDisplayName(newDisplayName);
      space.setPrettyName(space.getDisplayName());
      space.setUrl(SpaceUtils.cleanString(newDisplayName));

      entity = spaceDAO.find(Long.parseLong(space.getId()));
      entity.buildFrom(space);

      // change profile of space
      Identity identitySpace = identityStorage.findIdentity(SpaceIdentityProvider.NAME, oldPrettyName);

      if (identitySpace != null) {
        Profile profileSpace = identitySpace.getProfile();
        profileSpace.setProperty(Profile.FIRST_NAME, space.getDisplayName());
        profileSpace.setProperty(Profile.USERNAME, space.getPrettyName());
        // profileSpace.setProperty(Profile.AVATAR_URL, space.getAvatarUrl());
        profileSpace.setProperty(Profile.URL, space.getUrl());

        identityStorage.saveProfile(profileSpace);

        identitySpace.setRemoteId(space.getPrettyName());
        // TODO remove this after finish RDBMSIdentityStorage
        renameIdentity(identitySpace);
      }

      //
      LOG.debug(String.format("Space %s (%s) saved", space.getPrettyName(), space.getId()));

    } catch (NodeNotFoundException e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_RENAME_SPACE, e.getMessage(), e);
    }
  }

  @Override
  @ExoTransactional
  public void saveSpace(Space space, boolean isNew) throws SpaceStorageException {
    if (isNew) {
      SpaceEntity entity = new SpaceEntity();
      entity = entity.buildFrom(space);

      //
      spaceDAO.create(entity);
      space.setId(String.valueOf(entity.getId()));
    } else {
      Long id = Long.parseLong(space.getId());
      SpaceEntity entity = spaceDAO.find(id);

      if (entity != null) {
        entity = entity.buildFrom(space);
        //
        spaceDAO.update(entity);
      } else {
        throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_SAVE_SPACE);
      }
    }

    LOG.debug("Space {} ({}) saved", space.getPrettyName(), space.getId());
  }

  @Override
  @ExoTransactional
  public void updateSpaceAccessed(String remoteId, Space space) throws SpaceStorageException {
    SpaceMemberEntity member = spaceMemberDAO.getMember(remoteId, Long.parseLong(space.getId()));
    if (member != null) {
      member.setLastAccess(new Date());
      // consider visited if access after create time more than 2s
      if (!member.isVisited()) {
        member.setVisited((member.getLastAccess().getTime() - member.getSpace().getCreatedDate().getTime()) >= 2000);
      }
    }
    spaceMemberDAO.update(member);
  }

  /**
   * Fills {@link Space}'s properties to {@link SpaceEntity}'s.
   *
   * @param entity the space entity
   * @param space the space pojo for services
   */
  private Space fillSpaceFromEntity(SpaceEntity entity) {
    if (entity == null) {
      return null;
    }
    Space space = new Space();
    fillSpaceSimpleFromEntity(entity, space);

    space.setPendingUsers(entity.getPendingMembersId());
    space.setInvitedUsers(entity.getInvitedMembersId());

    //
    String[] members = entity.getMembersId();
    String[] managers = entity.getManagerMembersId();

    //
    Set<String> membersList = new HashSet<String>();
    if (members != null)
      membersList.addAll(Arrays.asList(members));
    if (managers != null)
      membersList.addAll(Arrays.asList(managers));

    //
    space.setMembers(membersList.toArray(new String[] {}));
    space.setManagers(entity.getManagerMembersId());
    return space;
  }

  /**
   * Add this method to resolve SOC-3439
   * 
   * @param identity
   * @throws NodeNotFoundException
   */
  private void renameIdentity(Identity identity) throws NodeNotFoundException {
    identityStorage.saveIdentity(identity);
    /*
     * ProviderEntity providerEntity =
     * getProviderRoot().getProvider(identity.getProviderId()); // Move identity
     * IdentityEntity identityEntity = _findById(IdentityEntity.class,
     * identity.getId());
     * providerEntity.getIdentities().put(identity.getRemoteId(),
     * identityEntity); identityEntity.setRemoteId(identity.getRemoteId());
     */
  }

  private List<Space> getSpaces(String userId, List<SpaceMemberEntity.Status> status, SpaceFilter spaceFilter, long offset, long limit) {
    XSpaceFilter filter = new XSpaceFilter();
    filter.setSpaceFilter(spaceFilter);
    if (userId != null && status != null) {
      filter.setRemoteId(userId);
      filter.addStatus(status.toArray(new SpaceMemberEntity.Status[status.size()]));
    }

    if (filter.isUnifiedSearch()) {
      //return spaceSearchConnector.search(filter, offset, limit);
      throw new UnsupportedOperationException();
    } else {
      SpaceQueryBuilder query = SpaceQueryBuilder.builder().filter(filter).offset(offset).limit(limit);
      List<SpaceEntity> entities = query.build().getResultList();
      return buildList(entities);
    }
  }

  private int getSpacesCount(String userId, List<SpaceMemberEntity.Status> status, SpaceFilter spaceFilter) {
    XSpaceFilter filter = new XSpaceFilter();
    filter.setSpaceFilter(spaceFilter);
    if (userId != null && status != null) {
      filter.setRemoteId(userId);
      filter.addStatus(status.toArray(new SpaceMemberEntity.Status[status.size()]));
    }

    if (filter.isUnifiedSearch()) {
//      return spaceSearchConnector.count(filter);
      throw new UnsupportedOperationException();
    } else {
      SpaceQueryBuilder query = SpaceQueryBuilder.builder().filter(filter);
      return query.buildCount().getSingleResult().intValue();
    }
  }

  private List<Space> buildList(List<SpaceEntity> spaceEntities) {
    List<Space> spaces = new LinkedList<>();
    if (spaceEntities != null) {
      for (SpaceEntity entity : spaceEntities) {
        Space space = fillSpaceFromEntity(entity);
        spaces.add(space);
      }
    }
    return spaces;
  }

  /**
   * Fills {@link Space}'s properties to {@link SpaceEntity}'s.
   *
   * @param entity the space entity from chromattic
   * @param space the space pojo for services
   */
  private Space fillSpaceSimpleFromEntity(SpaceEntity entity, Space space) {
    space.setApp(StringUtils.join(entity.getApp(), ","));
    space.setId(String.valueOf(entity.getId()));
    space.setDisplayName(entity.getDisplayName());
    space.setPrettyName(entity.getPrettyName());
    if (entity.getRegistration() != null) {
      space.setRegistration(entity.getRegistration().name().toLowerCase());      
    }
    space.setDescription(entity.getDescription());
    space.setType(DefaultSpaceApplicationHandler.NAME);
    if (entity.getVisibility() != null) {
      space.setVisibility(entity.getVisibility().name().toLowerCase());
    }
    if (entity.getPriority() != null) {
      switch (entity.getPriority()) {
      case HIGH:
        space.setPriority(Space.HIGH_PRIORITY);
        break;
      case INTERMEDIATE:
        space.setPriority(Space.INTERMEDIATE_PRIORITY);
        break;
      case LOW:
        space.setPriority(Space.LOW_PRIORITY);
        break;
      default:
        space.setPriority(null);
      }
    }
    space.setGroupId(entity.getGroupId());
    space.setUrl(entity.getUrl());
    space.setCreatedTime(entity.getCreatedDate().getTime());

    if (entity.getAvatarLastUpdated() != null) {
      try {
        space.setAvatarUrl(IdentityAvatarRestService.buildAvatarURL(SpaceIdentityProvider.NAME, space.getPrettyName()));
      } catch (Exception e) {
        LOG.warn("Failed to build avatar url: " + e.getMessage());
      }
    }
    if (entity.getAvatarLastUpdated() != null) {
      space.setAvatarLastUpdated(entity.getAvatarLastUpdated().getTime());
    }      
    return space;
  }

  public void setIdentityStorage(IdentityStorage identityStorage) {
    this.identityStorage = identityStorage;
  }

}
