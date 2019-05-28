/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.user.UserStateModel;
import org.exoplatform.services.user.UserStateService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess;
import org.exoplatform.social.core.identity.model.ActiveIdentityFilter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.IdentityWithRelationship;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.search.ExtendProfileFilter;
import org.exoplatform.social.core.jpa.search.ProfileSearchConnector;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceMemberDAO;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.jpa.storage.entity.ProfileExperienceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity.Status;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.impl.StorageUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 5, 2015  
 */
public class RDBMSIdentityStorageImpl implements IdentityStorage {

  private static final char NULL_CHARACTER = '\u0000';

  private static final Log LOG = ExoLogger.getLogger(RDBMSIdentityStorageImpl.class);

  private static final int     BATCH_SIZE = 100;

  private static final String socialNameSpace = "social";

  private final IdentityDAO identityDAO;
  private final SpaceMemberDAO spaceMemberDAO;

  private final FileService fileService;

  private final OrganizationService orgService;

  private ProfileSearchConnector profileSearchConnector;

  private IdentityStorage cachedIdentityStorage;

  private ActivityStorage activityStorage;

  private Map<String, IdentityProvider<?>> identityProviders = null;

  public RDBMSIdentityStorageImpl(IdentityDAO identityDAO,
                                  SpaceMemberDAO spaceMemberDAO,
                                  FileService fileService,
                                  ProfileSearchConnector profileSearchConnector, OrganizationService orgService) {
    this.identityDAO = identityDAO;
    this.spaceMemberDAO = spaceMemberDAO;
    this.profileSearchConnector = profileSearchConnector;
    this.orgService = orgService;
    this.fileService = fileService;
  }

  private IdentityDAO getIdentityDAO() {
    return identityDAO;
  }

  public void setProfileSearchConnector(ProfileSearchConnector profileSearchConnector) {
    this.profileSearchConnector = profileSearchConnector;
  }



  private void mapToProfileEntity(Profile profile, IdentityEntity identityEntity) {
    Map<String, String> entityProperties = identityEntity.getProperties();
    if (entityProperties == null) {
      entityProperties = new HashMap<>();
    }

    String providerId = profile.getIdentity().getProviderId();
    if (!OrganizationIdentityProvider.NAME.equals(providerId) && !SpaceIdentityProvider.NAME.equals(providerId)) {
      entityProperties.put(Profile.URL, profile.getUrl());
    }

    boolean hasBanner = false;
    boolean hasAvatar = false;
    Map<String, Object> properties = profile.getProperties();
    for (Entry<String, Object> profileProperty : properties.entrySet()) {
      if (Profile.AVATAR.equalsIgnoreCase(profileProperty.getKey())) {
        hasAvatar = true;
        AvatarAttachment attachment = (AvatarAttachment) profileProperty.getValue();
        byte[] bytes = attachment.getImageBytes();
        String fileName = attachment.getFileName();
        if (fileName == null) {
          fileName = identityEntity.getRemoteId() + "_avatar";
        }

        try {
          Long avatarId = identityEntity.getAvatarFileId();
          FileItem fileItem;
          if(avatarId != null){//update avatar file
            fileItem = new FileItem(avatarId,
                    fileName,
                    attachment.getMimeType(),
                    socialNameSpace,
                    bytes.length,
                    new Date(),
                    identityEntity.getRemoteId(),
                    false,
                    new ByteArrayInputStream(bytes));
            fileService.updateFile(fileItem);
          }
          else{//create new  avatar file
            fileItem = new FileItem(null,
                    fileName,
                    attachment.getMimeType(),
                    socialNameSpace,
                    bytes.length,
                    new Date(),
                    identityEntity.getRemoteId(),
                    false,
                    new ByteArrayInputStream(bytes));
            fileItem = fileService.writeFile(fileItem);
            identityEntity.setAvatarFileId(fileItem.getFileInfo().getId());
          }
        } catch (Exception ex) {
          LOG.error("Can not store avatar for " + identityEntity.getProviderId() + " " + identityEntity.getRemoteId(), ex);
        }

      } else if (Profile.BANNER.equalsIgnoreCase(profileProperty.getKey())) {
        hasBanner = true;
        BannerAttachment attachment = (BannerAttachment) profileProperty.getValue();
        byte[] bytes = attachment.getImageBytes();
        String fileName = attachment.getFileName();
        if (fileName == null) {
          fileName = identityEntity.getRemoteId() + "_banner";
        }

        try {
          Long bannerId = identityEntity.getBannerFileId();
          FileItem fileItem;
          if(bannerId != null){//update banner file
            fileItem = new FileItem(bannerId,
                    fileName,
                    attachment.getMimeType(),
                    socialNameSpace,
                    bytes.length,
                    new Date(),
                    identityEntity.getRemoteId(),
                    false,
                    new ByteArrayInputStream(bytes));
            fileService.updateFile(fileItem);
          }
          else{//create new  banner file
            fileItem = new FileItem(null,
                    fileName,
                    attachment.getMimeType(),
                    socialNameSpace,
                    bytes.length,
                    new Date(),
                    identityEntity.getRemoteId(),
                    false,
                    new ByteArrayInputStream(bytes));
            fileItem = fileService.writeFile(fileItem);
            identityEntity.setBannerFileId(fileItem.getFileInfo().getId());
          }
        } catch (Exception ex) {
          LOG.error("Can not store banner for " + identityEntity.getProviderId() + " " + identityEntity.getRemoteId(), ex);
        }
      } else if (Profile.EXPERIENCES.equalsIgnoreCase(profileProperty.getKey())) {

        List<Map<String, String>> newExperiences = (List<Map<String, String>>)profileProperty.getValue();

        identityEntity.getExperiences().clear();

        for (Map<String, String> newExperience : newExperiences) {
          ProfileExperienceEntity profileExperienceEntity = new ProfileExperienceEntity();
          profileExperienceEntity.setIdentity(identityEntity);
          String experienceId = newExperience.get(Profile.EXPERIENCES_ID);
          if(StringUtils.isNotBlank(experienceId)) {
            profileExperienceEntity.setId(Long.valueOf(experienceId));
          }
          profileExperienceEntity.setCompany(newExperience.get(Profile.EXPERIENCES_COMPANY));
          profileExperienceEntity.setPosition(newExperience.get(Profile.EXPERIENCES_POSITION));
          profileExperienceEntity.setStartDate(newExperience.get(Profile.EXPERIENCES_START_DATE));
          profileExperienceEntity.setEndDate(newExperience.get(Profile.EXPERIENCES_END_DATE));
          profileExperienceEntity.setSkills(newExperience.get(Profile.EXPERIENCES_SKILLS));
          profileExperienceEntity.setDescription(newExperience.get(Profile.EXPERIENCES_DESCRIPTION));

          identityEntity.getExperiences().add(profileExperienceEntity);
        }
      } else if (Profile.CONTACT_IMS.equals(profileProperty.getKey())
              || Profile.CONTACT_PHONES.equals(profileProperty.getKey())
              || Profile.CONTACT_URLS.equals(profileProperty.getKey())) {

        List<Map<String, String>> list = (List<Map<String, String>>) profileProperty.getValue();
        JSONArray arr = new JSONArray();
        for (Map<String, String> map : list) {
          JSONObject json = new JSONObject(map);
          arr.put(json);
        }

        entityProperties.put(profileProperty.getKey(), arr.toString());

      } else if (!Profile.EXPERIENCES_SKILLS.equals(profileProperty.getKey())) {
        Object val = profileProperty.getValue();
        entityProperties.put(profileProperty.getKey(), val != null ? String.valueOf(val) : null);
      }
    }

    if (identityEntity.getBannerFileId() != null && !hasBanner
            && profile.getBannerUrl() == null) {
      fileService.deleteFile(identityEntity.getBannerFileId());
      identityEntity.setBannerFileId(null);
    }
    
    if (identityEntity.getAvatarFileId() != null && !hasAvatar
        && profile.getAvatarUrl() == null) {
      fileService.deleteFile(identityEntity.getAvatarFileId());
      identityEntity.setAvatarFileId(null);
    }
    
    identityEntity.setProperties(entityProperties);

    Date created = profile.getCreatedTime() <= 0 ? new Date() : new Date(profile.getCreatedTime());
    identityEntity.setCreatedDate(created);
  }
  /**
   * Saves identity.
   *
   * @param identity the identity
   * @throws IdentityStorageException if has any error
   */
  public void saveIdentity(final Identity identity) throws IdentityStorageException {
    long id = EntityConverterUtils.parseId(identity.getId());

    IdentityEntity entity = null;
    if (id > 0) {
      entity = getIdentityDAO().find(id);
    } else {
      entity = getIdentityDAO().findByProviderAndRemoteId(identity.getProviderId(), identity.getRemoteId());
    }

    if (entity == null) {
      entity = new IdentityEntity();
    }
    EntityConverterUtils.mapToEntity(identity, entity);

    if (entity.getId() > 0) {
      getIdentityDAO().update(entity);
    } else {
      if (identity.getProfile() != null) {
        mapToProfileEntity(identity.getProfile(), entity);
      }
      entity = getIdentityDAO().create(entity);
    }
    Profile profile = EntityConverterUtils.convertToProfile(entity, identity);
    if (id <= 0) {
      profile.setId(null);      
    }
    identity.setProfile(profile);
    identity.setId(entity.getStringId());
  }

  /**
   * Updates existing identity's properties.
   *
   * @param identity the identity to be updated.
   * @return the updated identity.
   * @throws IdentityStorageException if has any error
   * @since  1.2.0-GA
   */
  public Identity updateIdentity(final Identity identity) throws IdentityStorageException {
    long id = EntityConverterUtils.parseId(identity.getId());

    IdentityEntity entity = null;
    if (id > 0) {
      entity = getIdentityDAO().find(id);
    }

    if (entity == null) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_UPDATE_IDENTITY, "The identity does not exist on DB");
    }

    EntityConverterUtils.mapToEntity(identity, entity);
    entity = getIdentityDAO().update(entity);

    return EntityConverterUtils.convertToIdentity(entity, true);
  }

  /**
   * Updates existing identity's membership in OrganizationService.
   *
   * @param remoteId the remoteId to be updated membership.
   * @throws IdentityStorageException if has any error
   * @since  4.0.0
   */
  public void updateIdentityMembership(final String remoteId) throws IdentityStorageException {
    // We do not need to implement this method,
    // only clear Identity Caching when user updated Group, what raised by Organization Service
  }

  /**
   * Gets the identity by his id.
   *
   * @param nodeId the id of identity
   * @return the identity
   * @throws IdentityStorageException if has any error
   */
  @ExoTransactional
  public Identity findIdentityById(final String nodeId) throws IdentityStorageException {
    long id = EntityConverterUtils.parseId(nodeId);
    IdentityEntity entity = getIdentityDAO().find(id);

    if (entity != null) {
      return EntityConverterUtils.convertToIdentity(entity);
    } else {
      return null;
    }
  }

  /**
   * Deletes an identity
   *
   * @param identity the Identity to be deleted
   * @throws IdentityStorageException if has any error
   */
  public void deleteIdentity(final Identity identity) throws IdentityStorageException {
    this.hardDeleteIdentity(identity);
  }

  /**
   * Hard delete an identity
   *
   * @param identity the identity to be deleted
   * @throws IdentityStorageException if has any error
   */
  @ExoTransactional
  public void hardDeleteIdentity(final Identity identity) throws IdentityStorageException {
    long id = EntityConverterUtils.parseId(identity.getId());
    String username = identity.getRemoteId();
    String provider = identity.getProviderId();

    IdentityEntity entity = getIdentityDAO().find(id);
    if (entity != null) {
      entity.setDeleted(true);
      getIdentityDAO().update(entity);

      if (entity.getAvatarFileId() != null && entity.getAvatarFileId() > 0) {
        fileService.deleteFile(entity.getAvatarFileId());
      }
      if (entity.getBannerFileId() != null && entity.getBannerFileId() > 0) {
        fileService.deleteFile(entity.getBannerFileId());
      }
    }

    EntityManager em = CommonsUtils.getService(EntityManagerService.class).getEntityManager();
    Query query;

    // Delete all connection
    query = em.createNamedQuery("SocConnection.deleteConnectionByIdentity");
    query.setParameter("identityId", id);
    query.executeUpdate();

    if(OrganizationIdentityProvider.NAME.equals(provider)) {
      // Delete space-member
      query = em.createNamedQuery("SpaceMember.deleteByUsername");
      query.setParameter("username", username);
      query.executeUpdate();
    }
  }

  /**
   * Load profile.
   *
   * @param profile the profile
   * @throws IdentityStorageException if has any error
   */
  @ExoTransactional
  public Profile loadProfile(Profile profile) throws IdentityStorageException {
    long identityId = EntityConverterUtils.parseId(profile.getIdentity().getId());    
    IdentityEntity entity = identityDAO.find(identityId);

    if (entity == null) {
      return null;
    } else {
      profile.setId(String.valueOf(entity.getId()));
      EntityConverterUtils.mapToProfile(entity, profile);
      profile.clearHasChanged();
      return profile;
    }
  }


  /**
   * Gets the identity by remote id.
   *
   * @param providerId the identity provider
   * @param remoteId   the id
   * @return the identity by remote id
   * @throws IdentityStorageException if has any error
   */
  @ExoTransactional
  public Identity findIdentity(final String providerId, final String remoteId) throws IdentityStorageException {
    try {
      IdentityEntity entity = getIdentityDAO().findByProviderAndRemoteId(providerId, remoteId);
      if (entity == null) {
        return null;
      }

      return EntityConverterUtils.convertToIdentity(entity);

    } catch (Exception ex) {

      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_FIND_IDENTITY, "Can not load identity", ex);
    }
  }

  /**
   * Saves profile.
   *
   * @param profile the profile
   * @throws IdentityStorageException if has any error
   */
  public void saveProfile(final Profile profile) throws IdentityStorageException {
    long id = EntityConverterUtils.parseId(profile.getIdentity().getId());
    IdentityEntity entity = (id == 0 ? null : identityDAO.find(id));
    if (entity == null) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_UPDATE_PROFILE, "Profile does not exist on RDBMS");
    } else {
      mapToProfileEntity(profile, entity);
      identityDAO.update(entity);
    }
    profile.setId(entity.getStringId());
    profile.clearHasChanged();
  }
  
  /**
   * Updates profile.
   *
   * @param profile the profile
   * @throws IdentityStorageException if has any error
   * @since 1.2.0-GA
   */
  public void updateProfile(final Profile profile) throws IdentityStorageException {
    long id = EntityConverterUtils.parseId(profile.getIdentity().getId());
    IdentityEntity entity = identityDAO.find(id);
    if (entity == null) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_UPDATE_PROFILE, "Profile does not exist on RDBMS");
    } else {
      mapToProfileEntity(profile, entity);
      identityDAO.update(entity);
    }    
  }

  /**
   * Gets total number of identities in storage depend on providerId.
   * @throws IdentityStorageException if has any error
   */
  public int getIdentitiesCount (final String providerId) throws IdentityStorageException {
    return (int)getIdentityDAO().countIdentityByProvider(providerId);
  }

  /**
   * Gets the type.
   *
   * @param nodetype the nodetype
   * @param property the property
   * @return the type
   * @throws IdentityStorageException if has any error
   */
  public String getType(final String nodetype, final String property) {
    // This is not JCR implementation, so nodetype does not exist.
    return "undefined";
  }

  /**
   * Add or modify properties of profile and persist to database. Profile parameter is a lightweight that
   * contains only the property that you want to add or modify. NOTE: The method will
   * not delete the properties on old profile when the param profile have not those keys.
   *
   * @param profile the profile
   * @throws IdentityStorageException if has any error
   */
  public void addOrModifyProfileProperties(final Profile profile) throws IdentityStorageException {
    updateProfile(profile);
  }



  /**
   * Updates profile activity id by type.
   *
   * @param identity the identity
   * @param activityId the activity id
   * @param type Type of activity id to get.
   * @since 4.0.0.Alpha1
   */
  public void updateProfileActivityId(Identity identity, String activityId, Profile.AttachedActivityType type) {
    // Do not need to update in this case
  }

  /**
   * Gets profile activity id by type.
   *
   * @param profile the Profile
   * @param type Type of activity id to get.
   * @return Profile activity id.
   * @since 4.0.0.Alpha1
   */
  public String getProfileActivityId(Profile profile, Profile.AttachedActivityType type) {
    String t = "SPACE_ACTIVITY";
    if (type == Profile.AttachedActivityType.USER) {
      t = "USER_PROFILE_ACTIVITY";
    } else if (type == Profile.AttachedActivityType.RELATIONSHIP) {
      t = "USER_ACTIVITIES_FOR_RELATIONSHIP";
    }
    List<ExoSocialActivity> activitiesByPoster = getActivityStorage().getActivitiesByPoster(profile.getIdentity(), 0, 1, t);
    if (activitiesByPoster != null && activitiesByPoster.size() > 0) {
      return String.valueOf(activitiesByPoster.get(0).getId());
    } else {
      return null;
    }
  }

  /**
   * Gets the active user list base on the given ActiveIdentityFilter.
   * 1. N days who last login less than N days.
   * 2. UserGroup who belongs to this group.
   *
   * @param filter the filter
   * @return set of identity ids
   * @since 4.1.0
   */
  public Set<String> getActiveUsers(ActiveIdentityFilter filter) {
    Set<String> activeUsers = new HashSet<String>();
    //by userGroups
    if (filter.getUserGroups() != null) {
      StringTokenizer stringToken = new StringTokenizer(filter.getUserGroups(), ActiveIdentityFilter.COMMA_SEPARATOR);
      try {
        while(stringToken.hasMoreTokens()) {
          try {
            ListAccess<User> listAccess = orgService.getUserHandler().findUsersByGroupId(stringToken.nextToken().trim());
            User[] users = listAccess.load(0, listAccess.getSize());
            //
            for(User u : users) {
              activeUsers.add(u.getUserName());
            }
          } catch (Exception e) {
            LOG.error(e.getMessage(), e);
          }
        }
      } catch (Exception e) {
        LOG.error(e.getMessage());
      }
    }

    //by N days
    if (filter.getDays() > 0) {
      activeUsers = StorageUtils.getLastLogin(filter.getDays());
    }

    //Gets online users and push to activate users
    if (CommonsUtils.getService(UserStateService.class) != null) {
      List<UserStateModel> onlines = CommonsUtils.getService(UserStateService.class).online();
      for (UserStateModel user : onlines) {
        activeUsers.add(user.getUserId());
      }
    }


    return activeUsers;
  }

  /**
   * Process enable/disable Identity
   *
   * @param identity The Identity enable
   * @param isEnable true if the user is enable, false if not
   * @since 4.2.x
   */
  public void processEnabledIdentity(Identity identity, boolean isEnable) {
    long id = EntityConverterUtils.parseId(identity.getId());
    IdentityEntity entity = getIdentityDAO().find(id);
    if (entity == null) {
      throw new IllegalArgumentException("Identity does not exists");
    }
    entity.setEnabled(isEnable);
    getIdentityDAO().update(entity);
  }

  @Override
  public List<Identity> getIdentitiesByFirstCharacterOfName(String providerId,
                                                            ProfileFilter profileFilter,
                                                            long offset,
                                                            long limit,
                                                            boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    return getIdentitiesByProfileFilter(providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);
  }

  @Override
  public List<Identity> getIdentitiesForMentions(String providerId,
                                                 ProfileFilter profileFilter,
                                                 Type type,
                                                 long offset,
                                                 long limit,
                                                 boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    Identity identity = null;
    if (profileFilter.getViewerIdentity() != null) {
      identity = profileFilter.getViewerIdentity();
    }
    if (OrganizationIdentityProvider.NAME.equals(providerId)) {
      return profileSearchConnector.search(identity, profileFilter, type, offset, limit);
    } else {
      throw new IllegalStateException("Can't search identities with provider id = " + providerId);
    }
  }

  @Override
  public int getIdentitiesForMentionsCount(String providerId, ProfileFilter profileFilter, Type type) throws IdentityStorageException {
    Identity identity = null;
    if(profileFilter.getViewerIdentity() != null) {
      identity = profileFilter.getViewerIdentity();
    }
    if (OrganizationIdentityProvider.NAME.equals(providerId)) {
      return profileSearchConnector.count(identity, profileFilter, type);
    } else {
      throw new IllegalStateException("Can't search identities with provider id = " + providerId);
    }
  }

  @Override
  public int getIdentitiesByProfileFilterCount(String providerId, ProfileFilter profileFilter) throws IdentityStorageException {
    ExtendProfileFilter xFilter = new ExtendProfileFilter(profileFilter);
    xFilter.setProviderId(providerId);
    ListAccess<IdentityEntity> list = getIdentityDAO().findIdentities(xFilter);

    try {
      return list.getSize();
    } catch (Exception e) {
      return 0;
    }
  }

  @Override
  public int getIdentitiesByFirstCharacterOfNameCount(String providerId, ProfileFilter profileFilter) throws IdentityStorageException {
    ExtendProfileFilter xFilter = new ExtendProfileFilter(profileFilter);
    xFilter.setProviderId(providerId);

    ListAccess<IdentityEntity> list = getIdentityDAO().findIdentities(xFilter);
    try {
      return list.getSize();
    } catch (Exception ex) {
      return 0;
    }
  }

  public List<Identity> getIdentitiesForUnifiedSearch(final String providerId,
                                                      final ProfileFilter profileFilter,
                                                      long offset, long limit) throws IdentityStorageException {
    return profileSearchConnector.search(null, profileFilter, null, offset, limit);
  }

  public List<Identity> getSpaceMemberIdentitiesByProfileFilter(final Space space,
                                                                ProfileFilter profileFilter,
                                                                SpaceMemberFilterListAccess.Type type,
                                                                long offset, long limit) throws IdentityStorageException {
    if (space == null) {
      throw new IllegalArgumentException("Space shouldn't be null");
    }
    List<String> excludedMembers = new ArrayList<>();
    if (profileFilter != null && profileFilter.getExcludedIdentityList() != null) {
      for (Identity identity : profileFilter.getExcludedIdentityList()) {
        excludedMembers.add(identity.getRemoteId());
      }
    }
    List<String> spaceMembers = null;
    switch (type) {
      case MEMBER:
        spaceMembers = getSpaceMembers(space.getId(), Status.MEMBER);
        if(spaceMembers == null || spaceMembers.isEmpty()) {
          return Collections.emptyList();
        }
        spaceMembers = spaceMembers.stream()
            .filter(username -> !excludedMembers.contains(username))
            .collect(Collectors.toList());
        break;
      case MANAGER:
        spaceMembers = getSpaceMembers(space.getId(), Status.MANAGER);
        if(spaceMembers == null || spaceMembers.isEmpty()) {
          return Collections.emptyList();
        }
        spaceMembers = spaceMembers.stream()
            .filter(username -> !excludedMembers.contains(username))
            .collect(Collectors.toList());
        break;
    }
    if (profileFilter != null && profileFilter.getExcludedIdentityList() != null) {
      for (Identity identity : profileFilter.getExcludedIdentityList()) {
        spaceMembers.remove(identity.getRemoteId());
      }
    }
    if (profileFilter == null || profileFilter.isEmpty()) {
      // Retrive space members from DB

      List<Identity> identities = new ArrayList<>();
      if (profileFilter != null && profileFilter.getSorting() != null
          && Sorting.SortBy.TITLE.equals(profileFilter.getSorting().sortBy)) {
        spaceMembers = sortIdentities(spaceMembers, Profile.FULL_NAME);
      }
      int i = (int) offset;
      long indexLimit = offset + limit;
      while (i < spaceMembers.size() && i < indexLimit) {
        String spaceMemberUserName = spaceMembers.get(i++);
        Identity identity = getOrCreateUserIdentityUsingCache(OrganizationIdentityProvider.NAME, spaceMemberUserName);
        if (identity != null) {
          identities.add(identity);
        }
      }
      return identities;
    } else {
      // Retrive space members from ES

      try {
        profileFilter = profileFilter.clone();
      } catch (CloneNotSupportedException e) {
        LOG.warn("Error while cloning profile filter", e);
      }
      profileFilter.setRemoteIds(spaceMembers);
      return profileSearchConnector.search(null, profileFilter, null, offset, limit);
    }
  }

  @Override
  public int countSpaceMemberIdentitiesByProfileFilter(Space space,
                                                       ProfileFilter profileFilter,
                                                       org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type type) {
    if (space == null) {
      throw new IllegalArgumentException("Space shouldn't be null");
    }
    List<String> excludedMembers = new ArrayList<>();
    if (profileFilter != null && profileFilter.getExcludedIdentityList() != null) {
      for (Identity identity : profileFilter.getExcludedIdentityList()) {
        excludedMembers.add(identity.getRemoteId());
      }
    }
    List<String> spaceMembers = null;
    switch (type) {
      case MEMBER:
        if(space.getMembers() == null || space.getMembers().length == 0) {
          return 0;
        }
        spaceMembers = Arrays.stream(space.getMembers())
                             .filter(username -> !excludedMembers.contains(username))
                             .collect(Collectors.toList());
        break;
      case MANAGER:
        if(space.getManagers() == null || space.getManagers().length == 0) {
          return 0;
        }
        spaceMembers = Arrays.stream(space.getManagers())
            .filter(username -> !excludedMembers.contains(username))
            .collect(Collectors.toList());
        break;
    }
    if (profileFilter == null || profileFilter.isEmpty()) {
      return spaceMembers.size();
    } else {
      try {
        profileFilter = profileFilter.clone();
      } catch (CloneNotSupportedException e) {
        LOG.warn("Error while cloning profile filter", e);
      }
      profileFilter.setRemoteIds(spaceMembers);
      return profileSearchConnector.count(null, profileFilter, null);
    }
  }

  public List<Identity> getIdentitiesByProfileFilter(final String providerId,
                                                     final ProfileFilter profileFilter, long offset, long limit,
                                                     boolean forceLoadOrReloadProfile)  throws IdentityStorageException {
    ExtendProfileFilter xFilter = new ExtendProfileFilter(profileFilter);
    xFilter.setProviderId(providerId);
    xFilter.setForceLoadProfile(forceLoadOrReloadProfile);

    ListAccess<IdentityEntity> list = getIdentityDAO().findIdentities(xFilter);

    return EntityConverterUtils.convertToIdentities(list, offset, limit);
  }

  @Override
  public List<Identity> getIdentities(String providerId,
                                      String firstCharacterFieldName,
                                      char firstCharacter,
                                      String sortField,
                                      String sortDirection,
                                      long offset,
                                      long limit) {
    List<String> usernames = getIdentityDAO().getAllIdsByProviderSorted(providerId, firstCharacterFieldName, firstCharacter, sortField, sortDirection, offset, limit);
    List<Identity> identities = new ArrayList<>();
    if (usernames != null && !usernames.isEmpty()) {
      for (String username : usernames) {
        Identity identity = getOrCreateUserIdentityUsingCache(providerId, username);
        if (identity != null) {
          identities.add(identity);
        }
      }
    }
    return identities;
  }

  public List<Identity> getIdentities(final String providerId, long offset, long limit) throws IdentityStorageException {
    return this.getIdentities(providerId, null, NULL_CHARACTER, null, null, offset, limit);
  }

  @Override
  public List<IdentityWithRelationship> getIdentitiesWithRelationships(final String identityId, int offset, int limit)  throws IdentityStorageException {
    return getIdentitiesWithRelationships(identityId, null, NULL_CHARACTER, null, null, offset, limit);
  }

  @Override
  public List<IdentityWithRelationship> getIdentitiesWithRelationships(String identityId, String firstCharFieldName, char firstChar, String sortFieldName, String sortDirection, int offset, int limit) {
    ListAccess<Entry<IdentityEntity, ConnectionEntity>> list = getIdentityDAO().findAllIdentitiesWithConnections(Long.valueOf(identityId), firstCharFieldName, firstChar, sortFieldName, sortDirection);
    return EntityConverterUtils.convertToIdentitiesWithRelationship(list, offset, limit);
  }

  @Override
  public int countIdentitiesWithRelationships(String identityId) throws Exception {
    ListAccess<Entry<IdentityEntity, ConnectionEntity>> list = getIdentityDAO().findAllIdentitiesWithConnections(Long.valueOf(identityId), null, NULL_CHARACTER, null, null);
    return list.getSize();
  }

  public ListAccess<Identity> findByFilter(ExtendProfileFilter filter) {
    final ListAccess<IdentityEntity> list = getIdentityDAO().findIdentities(filter);

    return new ListAccess<Identity>() {
      @Override
      public Identity[] load(int offset, int size) throws Exception, IllegalArgumentException {
        IdentityEntity[] entities = list.load(offset, size);
        if (entities == null || entities.length == 0) {
          return new Identity[0];
        } else {
          Identity[] identities = new Identity[entities.length];
          for (int i = 0; i < entities.length; i++) {
            identities[i] = EntityConverterUtils.convertToIdentity(entities[i]);
          }
          return identities;
        }
      }

      @Override
      public int getSize() throws Exception {
        return list.getSize();
      }
    };
  }

  /**
   * This method is introduced to clean totally identity from database
   * It's used in unit test
   * @param identity the Identity
   */
  @ExoTransactional
  public void removeIdentity(Identity identity) {
    long id = EntityConverterUtils.parseId(identity.getId());
    String username = identity.getRemoteId();
    String provider = identity.getProviderId();

    IdentityEntity entity = getIdentityDAO().find(id);

    EntityManager em = CommonsUtils.getService(EntityManagerService.class).getEntityManager();
    Query query;

    // Delete all connection
    query = em.createNamedQuery("SocConnection.deleteConnectionByIdentity");
    query.setParameter("identityId", id);
    query.executeUpdate();

    if(OrganizationIdentityProvider.NAME.equals(provider)) {
      // Delete space-member
      query = em.createNamedQuery("SpaceMember.deleteByUsername");
      query.setParameter("username", username);
      query.executeUpdate();
    }

    if (entity != null) {
      getIdentityDAO().delete(entity);
    }
  }
  
  @Override
  public InputStream getAvatarInputStreamById(Identity identity) throws IOException {
    FileItem file = null;
    IdentityEntity entity = identityDAO.findByProviderAndRemoteId(identity.getProviderId(), identity.getRemoteId());
    if (entity == null) {
      return null;
    }
    Long avatarId = entity.getAvatarFileId();
    if (avatarId == null) {
      return null;
    }
    try {
      file = fileService.getFile(avatarId);
    } catch (FileStorageException e) {
      return null;
    }
  
    if (file == null) {
      return null;
    }
    return file.getAsStream();
  }

  @Override
  public InputStream getBannerInputStreamById(Identity identity) throws IOException {
    FileItem file = null;
    IdentityEntity entity = identityDAO.findByProviderAndRemoteId(identity.getProviderId(), identity.getRemoteId());
    if (entity == null) {
      return null;
    }
    Long bannerId = entity.getBannerFileId();
    if (bannerId == null) {
      return null;
    }
    try {
      file = fileService.getFile(bannerId);
    } catch (FileStorageException e) {
      return null;
    }

    if (file == null) {
      return null;
    }
    return file.getAsStream();
  }

  private List<String> getSpaceMembers(String spaceIdString, Status status) {
    long spaceId = Long.parseLong(spaceIdString);
    int countSpaceMembers = spaceMemberDAO.countSpaceMembers(spaceId, status);
    if (countSpaceMembers == 0) {
      return Collections.emptyList();
    }
    List<String> members = new ArrayList<>();
    int offset = 0;
    while (offset < countSpaceMembers) {
      Collection<String> spaceMembers = spaceMemberDAO.getSpaceMembers(spaceId, status, offset, BATCH_SIZE);
      for (String username : spaceMembers) {
        members.add(username);
      }
      offset += BATCH_SIZE;
    }
    return members;
  }

  public IdentityProvider<?> getIdentityProvider(String providerId) {
    if (identityProviders == null || identityProviders.isEmpty()) {
      identityProviders = new HashMap<>();
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      if (container == null) {
        container = PortalContainer.getInstance();
      }
      for (ComponentAdapter<?> componentAdapter : container.getComponentAdaptersOfType(IdentityProvider.class)) {
          if (componentAdapter != null) {
              Object key = componentAdapter.getComponentKey();
              IdentityProvider<?> service = (IdentityProvider<?>) container.getComponentInstance(key);
              identityProviders.put(service.getName(), service);
          }
      }
    }
    return identityProviders.get(providerId);
  }

  public IdentityStorage getRDBMSCachedIdentityStorage() {
    if (cachedIdentityStorage == null) {
      cachedIdentityStorage = CommonsUtils.getService(IdentityStorage.class);
    }
    return cachedIdentityStorage;
  }

  public ActivityStorage getActivityStorage() {
    if (activityStorage == null) {
      activityStorage = CommonsUtils.getService(ActivityStorage.class);
    }
    return activityStorage;
  }

  /**
   * Get the identity from cache implementation of IdentityStorage instead of DB
   * The solution is not ideal since we refer to the cached version directly in the
   * wrapped class, but we have no choice because of this wrap.
   * 
   * @param providerId
   * @param userId
   * @return
   */
  private Identity getOrCreateUserIdentityUsingCache(String providerId, String userId) {
    Identity identity = getRDBMSCachedIdentityStorage().findIdentity(providerId, userId);
    if (identity == null) {
      identity = getIdentityProvider(providerId).getIdentityByRemoteId(userId);
      if (identity == null) {
        LOG.warn("Can't find identity for space member '{}'. The identity will not be retrieved.", userId);
      } else {
        LOG.info("User identity for space member '{}' wasn't found. Creating the identity.", userId);
        try {
          saveIdentity(identity);
          saveProfile(identity.getProfile());
        } catch (Exception e) {
          LOG.warn("Can't create user identity for space member '" + userId
              + "'. The identity will not be retrieved.", e);
          identity = null;
        }
      }
    }
    return identity;
  }

  @Override
  public List<String> sortIdentities(List<String> identityRemoteIds, String sortField) {
    return spaceMemberDAO.sortSpaceMembers(identityRemoteIds, sortField);
  }
}
