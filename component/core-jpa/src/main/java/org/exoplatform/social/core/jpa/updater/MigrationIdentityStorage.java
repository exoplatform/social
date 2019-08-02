/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.core.jpa.updater;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.exoplatform.social.core.jpa.storage.RDBMSIdentityStorageImpl;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess;
import org.exoplatform.social.core.identity.model.ActiveIdentityFilter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.IdentityWithRelationship;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;

/**
 * This class is introduced because there are many other service which start and run in parallel with migration service.
 * These services will load/create identity via api @{IdentityManger#getOrCreateIdentity(String providerId, String remoteId, boolean isProfileLoaded)}
 *
 * This class will be used in replace of RDBMSIdentityStorageImpl during the migration time.
 * It will take care to migrate all identity which is used before the migration service migrate that one
 *
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class MigrationIdentityStorage implements IdentityStorage {

  private final IdentityStorageImpl jcrStorage;
  private final RDBMSIdentityStorageImpl jpaStorage;
  private final IdentityMigrationService migrationService;

  public MigrationIdentityStorage(IdentityStorageImpl jcrStorage, RDBMSIdentityStorageImpl jpaStorage, IdentityMigrationService migrationService) {
    this.jcrStorage = jcrStorage;
    this.jpaStorage = jpaStorage;
    this.migrationService = migrationService;
  }

  @Override
  public void saveIdentity(Identity identity) throws IdentityStorageException {
    jpaStorage.saveIdentity(identity);
  }

  @Override
  public Identity updateIdentity(Identity identity) throws IdentityStorageException {
    return jpaStorage.updateIdentity(identity);
  }

  @Override
  public void updateIdentityMembership(String remoteId) throws IdentityStorageException {
    jpaStorage.updateIdentityMembership(remoteId);
  }

  @Override
  public Identity findIdentityById(String nodeId) throws IdentityStorageException {
    Identity identity = jpaStorage.findIdentityById(nodeId);
    if (identity == null) {
      if (jcrStorage.findIdentityById(nodeId) != null) {
        identity = migrationService.migrateIdentity(nodeId);
      }
    }
    return identity;
  }

  @Override
  public void deleteIdentity(Identity identity) throws IdentityStorageException {
    jpaStorage.deleteIdentity(identity);
  }

  @Override
  public void hardDeleteIdentity(Identity identity) throws IdentityStorageException {
    jpaStorage.hardDeleteIdentity(identity);
  }

  @Override
  public Profile loadProfile(Profile profile) throws IdentityStorageException {
    return jpaStorage.loadProfile(profile);
  }

  @Override
  public Identity findIdentity(String providerId, String remoteId) throws IdentityStorageException {
    Identity identity = jpaStorage.findIdentity(providerId, remoteId);
    if (identity == null) {
      identity = jcrStorage.findIdentity(providerId, remoteId);
      if (identity != null) {
        identity = migrationService.migrateIdentity(identity.getId());
      }
    }
    return identity;
  }

  @Override
  public void saveProfile(Profile profile) throws IdentityStorageException {
    jpaStorage.saveProfile(profile);
  }

  @Override
  public void updateProfile(Profile profile) throws IdentityStorageException {
    jpaStorage.updateProfile(profile);
  }

  @Override
  public int getIdentitiesCount(String providerId) throws IdentityStorageException {
    return jpaStorage.getIdentitiesCount(providerId);
  }

  @Override
  public List<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter, long offset, long limit, boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    return jpaStorage.getIdentitiesByProfileFilter(providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);
  }

  @Override
  public List<Identity> getIdentitiesForMentions(String providerId, ProfileFilter profileFilter, Type type, long offset, long limit, boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    return jpaStorage.getIdentitiesForMentions(providerId, profileFilter, type, offset, limit, forceLoadOrReloadProfile);
  }

  @Override
  public List<Identity> getIdentitiesForUnifiedSearch(String providerId, ProfileFilter profileFilter, long offset, long limit) throws IdentityStorageException {
    return jpaStorage.getIdentitiesForUnifiedSearch(providerId, profileFilter, offset, limit);
  }

  @Override
  public int getIdentitiesByProfileFilterCount(String providerId, ProfileFilter profileFilter) throws IdentityStorageException {
    return jpaStorage.getIdentitiesByProfileFilterCount(providerId, profileFilter);
  }

  @Override
  public int getIdentitiesByFirstCharacterOfNameCount(String providerId, ProfileFilter profileFilter) throws IdentityStorageException {
    return jpaStorage.getIdentitiesByFirstCharacterOfNameCount(providerId, profileFilter);
  }

  @Override
  public List<Identity> getIdentitiesByFirstCharacterOfName(String providerId, ProfileFilter profileFilter, long offset, long limit, boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    return jpaStorage.getIdentitiesByFirstCharacterOfName(providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);
  }

  @Override
  public String getType(String nodetype, String property) {
    return jpaStorage.getType(nodetype, property);
  }

  @Override
  public void addOrModifyProfileProperties(Profile profile) throws IdentityStorageException {
    jpaStorage.addOrModifyProfileProperties(profile);
  }

  @Override
  public List<Identity> getSpaceMemberIdentitiesByProfileFilter(Space space, ProfileFilter profileFilter, SpaceMemberFilterListAccess.Type type, long offset, long limit) throws IdentityStorageException {
    return jpaStorage.getSpaceMemberIdentitiesByProfileFilter(space, profileFilter, type, offset, limit);
  }

  @Override
  public void updateProfileActivityId(Identity identity, String activityId, Profile.AttachedActivityType type) {
    jpaStorage.updateProfileActivityId(identity, activityId, type);
  }

  @Override
  public String getProfileActivityId(Profile profile, Profile.AttachedActivityType type) {
    return jpaStorage.getProfileActivityId(profile, type);
  }

  @Override
  public Set<String> getActiveUsers(ActiveIdentityFilter filter) {
    return jpaStorage.getActiveUsers(filter);
  }

  @Override
  public void processEnabledIdentity(Identity identity, boolean isEnable) {
    jpaStorage.processEnabledIdentity(identity, isEnable);
  }

  @Override
  public List<IdentityWithRelationship> getIdentitiesWithRelationships(String identityId, int offset, int limit) {
    return jpaStorage.getIdentitiesWithRelationships(identityId, offset, limit);
  }

  @Override
  public int countIdentitiesWithRelationships(String identityId) throws Exception {
    return jpaStorage.countIdentitiesWithRelationships(identityId);
  }
  
  @Override
  public InputStream getAvatarInputStreamById(Identity identity) throws IOException {
    return jpaStorage.getAvatarInputStreamById(identity);
  }

  @Override
  public InputStream getBannerInputStreamById(Identity identity) throws IOException {
    return jpaStorage.getBannerInputStreamById(identity);
  }
  
  @Override
  public int getIdentitiesForMentionsCount(String providerId, ProfileFilter profileFilter, Type type) throws IdentityStorageException {
    return jpaStorage.getIdentitiesForMentionsCount(providerId, profileFilter, type);
  }

  @Override
  public int countSpaceMemberIdentitiesByProfileFilter(Space space,
                                                       ProfileFilter profileFilter,
                                                       org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type type) {
    return jpaStorage.countSpaceMemberIdentitiesByProfileFilter(space, profileFilter, type);
  }

  @Override
  public List<Identity> getIdentities(String providerId, long offset, long limit) {
    return jpaStorage.getIdentities(providerId, offset, limit);
  }

  @Override
  public List<String> sortIdentities(List<String> identityRemoteIds, String firstCharacterFieldName,
                                     char firstCharacter,
                                     String sortField,
                                     String sortDirection) {
    return jpaStorage.sortIdentities(identityRemoteIds, firstCharacterFieldName, firstCharacter, sortField, sortDirection);
  }
}
