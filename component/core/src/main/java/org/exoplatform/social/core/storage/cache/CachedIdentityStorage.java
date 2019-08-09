/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.core.storage.cache;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.user.UserStateModel;
import org.exoplatform.services.user.UserStateService;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type;
import org.exoplatform.social.core.identity.model.ActiveIdentityFilter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.IdentityWithRelationship;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.AttachedActivityType;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profile.ProfileLoader;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.ActiveIdentitiesData;
import org.exoplatform.social.core.storage.cache.model.data.IdentityData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.data.ListIdentitiesData;
import org.exoplatform.social.core.storage.cache.model.data.ProfileData;
import org.exoplatform.social.core.storage.cache.model.key.ActiveIdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityCompositeKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.ListIdentitiesKey;
import org.exoplatform.social.core.storage.cache.model.key.ListSpaceMembersKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceKey;
import org.exoplatform.social.core.storage.cache.selector.IdentityCacheSelector;

/**
 * Cache support for IdentityStorage.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedIdentityStorage implements IdentityStorage {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(CachedIdentityStorage.class);

  private final ExoCache<IdentityKey, IdentityData> exoIdentityCache;
  private final ExoCache<IdentityCompositeKey, IdentityKey> exoIdentityIndexCache;
  private final ExoCache<IdentityKey, ProfileData> exoProfileCache;
  private final ExoCache<IdentityFilterKey, IntegerData> exoIdentitiesCountCache;
  private final ExoCache<ListIdentitiesKey, ListIdentitiesData> exoIdentitiesCache;
  private final ExoCache<ActiveIdentityKey, ActiveIdentitiesData> exoActiveIdentitiesCache;

  private final FutureExoCache<IdentityKey, IdentityData, ServiceContext<IdentityData>> identityCache;
  private final FutureExoCache<IdentityCompositeKey, IdentityKey, ServiceContext<IdentityKey>> identityIndexCache;
  private final FutureExoCache<IdentityKey, ProfileData, ServiceContext<ProfileData>> profileCache;
  private final FutureExoCache<IdentityFilterKey, IntegerData, ServiceContext<IntegerData>> identitiesCountCache;
  private final FutureExoCache<ListIdentitiesKey, ListIdentitiesData, ServiceContext<ListIdentitiesData>> identitiesCache;
  private final FutureExoCache<ActiveIdentityKey, ActiveIdentitiesData, ServiceContext<ActiveIdentitiesData>> activeIdentitiesCache;

  private final IdentityStorage storage;
  private CachedRelationshipStorage cachedRelationshipStorage;

  void clearCache() {

    try {
      exoIdentitiesCache.select(new IdentityCacheSelector(OrganizationIdentityProvider.NAME));
      exoIdentitiesCountCache.select(new IdentityCacheSelector(OrganizationIdentityProvider.NAME));
    }
    catch (Exception e) {
      LOG.error("Error when clearing cache", e);
    }

  }
  
  private CachedRelationshipStorage getCachedRelationshipStorage() {
    if (cachedRelationshipStorage == null) {
      cachedRelationshipStorage = (CachedRelationshipStorage) 
          PortalContainer.getInstance().getComponentInstanceOfType(CachedRelationshipStorage.class);
    }
    return cachedRelationshipStorage;
  }

  /**
   * Build the identity list from the caches Ids.
   *
   * @param data ids
   * @return identities
   */
  private List<Identity> buildIdentities(ListIdentitiesData data) {

    List<Identity> identities = new ArrayList<Identity>();
    for (IdentityKey k : data.getIds()) {
      Identity gotIdentity = findIdentityById(k.getId());
      gotIdentity.setProfile(loadProfile(gotIdentity.getProfile()));
      identities.add(gotIdentity);
    }
    return identities;

  }

  /**
   * Build the ids from the identitiy list.
   *
   * @param identities identities
   * @return ids
   */
  private ListIdentitiesData buildIds(List<Identity> identities) {

    List<IdentityKey> data = new ArrayList<IdentityKey>();
    for (Identity i : identities) {
      IdentityKey k = new IdentityKey(i);
      if(exoIdentityCache.get(k) == null) {
        exoIdentityCache.putLocal(k, new IdentityData(i));
      }
      if(exoProfileCache.get(k) == null) {
        exoProfileCache.putLocal(k, new ProfileData(i.getProfile()));
      }
      data.add(new IdentityKey(i));
    }
    return new ListIdentitiesData(data);
  }

  public CachedIdentityStorage(final IdentityStorage storage, final SocialStorageCacheService cacheService) {

    //
    this.storage = storage;

    //
    this.exoIdentityCache = cacheService.getIdentityCache();
    this.exoIdentityIndexCache = cacheService.getIdentityIndexCache();
    this.exoProfileCache = cacheService.getProfileCache();
    this.exoIdentitiesCountCache = cacheService.getCountIdentitiesCache();
    this.exoIdentitiesCache = cacheService.getIdentitiesCache();
    this.exoActiveIdentitiesCache = cacheService.getActiveIdentitiesCache();

    //
    this.identityCache = CacheType.IDENTITY.createFutureCache(exoIdentityCache);
    this.identityIndexCache = CacheType.IDENTITY_INDEX.createFutureCache(exoIdentityIndexCache);
    this.profileCache = CacheType.PROFILE.createFutureCache(exoProfileCache);
    this.identitiesCountCache = CacheType.IDENTITIES_COUNT.createFutureCache(exoIdentitiesCountCache);
    this.identitiesCache = CacheType.IDENTITIES.createFutureCache(exoIdentitiesCache);
    this.activeIdentitiesCache = CacheType.ACTIVE_IDENTITIES.createFutureCache(exoActiveIdentitiesCache);

  }

  /**
   * {@inheritDoc}
   */
  public void saveIdentity(final Identity identity) throws IdentityStorageException {

    //
    storage.saveIdentity(identity);

    //
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    exoIdentityCache.put(key, new IdentityData(identity));
    exoIdentityIndexCache.put(new IdentityCompositeKey(identity.getProviderId(), identity.getRemoteId()), key);

    clearCache();
  }

  /**
   * {@inheritDoc}
   */
  public Identity updateIdentity(final Identity identity) throws IdentityStorageException {

    //
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    exoIdentityCache.remove(key);
    exoProfileCache.remove(key);
    exoIdentityIndexCache.remove(key);
    clearCache();

    //
    return storage.updateIdentity(identity);
  }
  
  /**
   * {@inheritDoc}
   */
  public void updateIdentityMembership(final String remoteId) throws IdentityStorageException {
    clearCache();
  }

  /**
   * {@inheritDoc}
   */
  public Identity findIdentityById(final String nodeId) throws IdentityStorageException {

    IdentityKey key = new IdentityKey(new Identity(nodeId));
    final Identity i = identityCache.get(
        new ServiceContext<IdentityData>() {

          public IdentityData execute() {
            return new IdentityData(storage.findIdentityById(nodeId));
          }
        },
        key)
        .build();

    //
    if (i != null) {
      ProfileLoader loader = new ProfileLoader() {
        public Profile load() throws IdentityStorageException {
          Profile profile = new Profile(i);
          return loadProfile(profile);
        }
      };
      i.setProfileLoader(loader);
    }

    //
    return i;

  }

  /**
   * {@inheritDoc}
   */
  public void deleteIdentity(final Identity identity) throws IdentityStorageException {

    //
    storage.deleteIdentity(identity);

    //
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    IdentityData data = exoIdentityCache.remove(key);
    IdentityCompositeKey compositeKey = new IdentityCompositeKey(identity.getProviderId(), identity.getRemoteId());
    if (exoIdentityIndexCache.get(compositeKey) != null) {
      exoIdentityIndexCache.remove(compositeKey);
    }
    exoProfileCache.remove(key);
    clearCache();

  }

  /**
   * {@inheritDoc}
   */
  public void hardDeleteIdentity(final Identity identity) throws IdentityStorageException {

    //
    storage.hardDeleteIdentity(identity);

    //
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    IdentityData data = exoIdentityCache.remove(key);
    IdentityCompositeKey compositeKey = new IdentityCompositeKey(identity.getProviderId(), identity.getRemoteId());
    if (exoIdentityIndexCache.get(compositeKey) != null) {
      exoIdentityIndexCache.remove(compositeKey);
    }
    exoProfileCache.remove(key);
    clearCache();

  }

  
  /**
   * {@inheritDoc}
   */
  public Profile loadProfile(final Profile profile) throws IdentityStorageException {
    
    IdentityKey key = new IdentityKey(new Identity(profile.getIdentity().getId()));
    ProfileData profileData = profileCache.get(
        new ServiceContext<ProfileData>() {

          public ProfileData execute() {
            Profile loadedProfile = storage.loadProfile(profile);
            if(loadedProfile == null) {
              LOG.warn("Null profile for identity: " + profile.getIdentity().getRemoteId());
              return ProfileData.NULL_OBJECT;
            } else {
              return new ProfileData(loadedProfile);
            }
          }
        },
        key);
    if(profileData == null || profileData.getProfileId() == null) {
      return profile;
    } else {
      return profileData
          .build();
    }
  }

  /**
   * Clear identity cache.
   * 
   * @param identity
   * @param oldRemoteId
   * @since 1.2.8
   */
  public void clearIdentityCached(Identity identity, String oldRemoteId) {
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    IdentityData data = exoIdentityCache.remove(key);
    IdentityCompositeKey compositeKey = new IdentityCompositeKey(identity.getProviderId(), oldRemoteId);
    if (exoIdentityIndexCache.get(compositeKey) != null) {
      exoIdentityIndexCache.remove(compositeKey);
    }
    exoProfileCache.remove(key);
    clearCache();
  }
  
  /**
   * {@inheritDoc}
   */
  public Identity findIdentity(final String providerId, final String remoteId) throws IdentityStorageException {

    //
    IdentityCompositeKey key = new IdentityCompositeKey(providerId, remoteId);

    //
    IdentityKey k = identityIndexCache.get(
        new ServiceContext<IdentityKey>() {

          public IdentityKey execute() {
            Identity i = storage.findIdentity(providerId, remoteId);
            IdentityKey key = null;
            if (i == null) {
              key = new IdentityKey(null);
            } else {
              key = new IdentityKey(i);
              if(exoIdentityCache.get(key) == null) {
                exoIdentityCache.putLocal(key, new IdentityData(i));
              }
            }
            return key;
          }
        },
        key);

    //
    if (k != null && k.getId() != null) {
      return findIdentityById(k.getId());
    }
    else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveProfile(final Profile profile) throws IdentityStorageException {

    //
    storage.saveProfile(profile);

    //
    IdentityKey key = new IdentityKey(new Identity(profile.getIdentity().getId()));
    exoProfileCache.remove(key);

  }

  /**
   * {@inheritDoc}
   */
  public void updateProfile(final Profile profile) throws IdentityStorageException {

    //
    storage.updateProfile(profile);

    //
    IdentityKey key = new IdentityKey(new Identity(profile.getIdentity().getId()));
    exoProfileCache.remove(key);
    clearCache();

  }

  /**
   * {@inheritDoc}
   */
  public int getIdentitiesCount(final String providerId) throws IdentityStorageException {

    return storage.getIdentitiesCount(providerId);

  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByProfileFilter(final String providerId, final ProfileFilter profileFilter,
      final long offset, final long limit, final boolean forceLoadOrReloadProfile) throws IdentityStorageException {

    //
    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);
    ListIdentitiesKey listKey = new ListIdentitiesKey(key, offset, limit);

    //
    ListIdentitiesData keys = identitiesCache.get(
        new ServiceContext<ListIdentitiesData>() {
          public ListIdentitiesData execute() {
            List<Identity> got = storage.getIdentitiesByProfileFilter(
                providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildIdentities(keys);
    
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<Identity> getIdentitiesForMentions(final String providerId, final ProfileFilter profileFilter, final org.exoplatform.social.core.relationship.model.Relationship.Type type,
      final long offset, final long limit, final boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    // Avoid using cache when requesting indexes
    return storage.getIdentitiesForMentions(providerId, profileFilter, type, offset, limit, forceLoadOrReloadProfile);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getIdentitiesForMentionsCount(String providerId,
                                           ProfileFilter profileFilter,
                                           org.exoplatform.social.core.relationship.model.Relationship.Type type) throws IdentityStorageException {
    return storage.getIdentitiesForMentionsCount(providerId, profileFilter, type);
  }

  /**
   * {@inheritDoc}
   */
  public int getIdentitiesByProfileFilterCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    //
    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);

    //
    return identitiesCountCache.get(
        new ServiceContext<IntegerData>() {

          public IntegerData execute() {
            return new IntegerData(storage.getIdentitiesByProfileFilterCount(providerId, profileFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getIdentitiesByFirstCharacterOfNameCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    //
    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);

    //
    return identitiesCountCache.get(
        new ServiceContext<IntegerData>() {

          public IntegerData execute() {
            return new IntegerData(storage.getIdentitiesByFirstCharacterOfNameCount(providerId, profileFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByFirstCharacterOfName(final String providerId, final ProfileFilter profileFilter,
      final long offset, final long limit, final boolean forceLoadOrReloadProfile) throws IdentityStorageException {

    //
    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);
    ListIdentitiesKey listKey = new ListIdentitiesKey(key, offset, limit);

    //
    ListIdentitiesData keys = identitiesCache.get(
        new ServiceContext<ListIdentitiesData>() {
          public ListIdentitiesData execute() {
            List<Identity> got = storage.getIdentitiesByFirstCharacterOfName(
                providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);
            return buildIds(got);
          }
        },
        listKey);

    //
    LOG.trace("getIdentitiesByFirstCharacterOfName:: return " + keys.getIds().size());
    return buildIdentities(keys);

  }

  /**
   * {@inheritDoc}
   */
  public String getType(final String nodetype, final String property) {

    return storage.getType(nodetype, property);

  }

  /**
   * {@inheritDoc}
   */
  public void addOrModifyProfileProperties(final Profile profile) throws IdentityStorageException {

    storage.addOrModifyProfileProperties(profile);
    
  }

  public List<Identity> getSpaceMemberIdentitiesByProfileFilter(final Space space,
      final ProfileFilter profileFilter, final Type type, final long offset, final long limit)
      throws IdentityStorageException {

    SpaceKey spaceKey = new SpaceKey(space.getId());
    IdentityFilterKey identityKey = new IdentityFilterKey(SpaceIdentityProvider.NAME, profileFilter);
    ListSpaceMembersKey listKey = new ListSpaceMembersKey(spaceKey, identityKey, offset, limit);

    ListIdentitiesData keys = identitiesCache.get(
        new ServiceContext<ListIdentitiesData>() {
          public ListIdentitiesData execute() {
            List<Identity> got = storage.getSpaceMemberIdentitiesByProfileFilter(space , profileFilter, type, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    return buildIdentities(keys);

  }

  public void updateProfileActivityId(Identity identity, String activityId, AttachedActivityType type) {
    storage.updateProfileActivityId(identity, activityId, type);
    //
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    exoProfileCache.remove(key);
    clearCache();
  }

  public String getProfileActivityId(Profile profile, AttachedActivityType type) {
    return storage.getProfileActivityId(profile, type);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesForUnifiedSearch(final String providerId,
                                                      final ProfileFilter profileFilter,
                                                      final long offset,
                                                      final long limit) throws IdentityStorageException {
    // Avoid using cache when requesting ES
    return storage.getIdentitiesForUnifiedSearch(providerId, profileFilter, offset, limit);
  }
  
  @Override
  public Set<String> getActiveUsers(final ActiveIdentityFilter filter) {
    ActiveIdentityKey key = new ActiveIdentityKey(filter);

    boolean isExistFromCache = activeIdentitiesCache.get(key) != null;
    ActiveIdentitiesData data = activeIdentitiesCache.get(
          new ServiceContext<ActiveIdentitiesData>() {
            public ActiveIdentitiesData execute() {
              Set<String> got = storage.getActiveUsers(filter);
              return new ActiveIdentitiesData(got);
            }
          },
          key);

    Set<String> users = data.build();
    if (isExistFromCache) {
    //Gets online users and push to activate users
      if (CommonsUtils.getService(UserStateService.class) != null) {
        List<UserStateModel> onlines = CommonsUtils.getService(UserStateService.class).online();
        for (UserStateModel user : onlines) {
          users.add(user.getUserId());
        }
      }
    }
    return users;
  }
  /**
   * {@inheritDoc}
   */
  public void processEnabledIdentity(Identity identity, boolean isEnable) {
    storage.processEnabledIdentity(identity, isEnable);
    //
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    exoIdentityCache.remove(key);
    exoProfileCache.remove(key);
    clearCache();
    getCachedRelationshipStorage().clearAllRelationshipCache();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<IdentityWithRelationship> getIdentitiesWithRelationships(String identityId, int offset, int limit) {
    return storage.getIdentitiesWithRelationships(identityId, offset, limit);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<IdentityWithRelationship> getIdentitiesWithRelationships(String identityId,
                                                                       String firstCharFieldName,
                                                                       char firstChar,
                                                                       String sortFieldName,
                                                                       String sortDirection,
                                                                       int offset,
                                                                       int limit) {
    return storage.getIdentitiesWithRelationships(identityId,
                                                  firstCharFieldName,
                                                  firstChar,
                                                  sortFieldName,
                                                  sortDirection,
                                                  offset,
                                                  limit);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int countIdentitiesWithRelationships(String identityId) throws Exception {
    return storage.countIdentitiesWithRelationships(identityId);
  }
  
  /**
   * Gets a the avatar stream for a given identity
   *
   *
   * @param identity
   * @return
   */
  @Override
  public InputStream getAvatarInputStreamById(Identity identity) throws IOException {
    return storage.getAvatarInputStreamById(identity);
  }

  /**
   * Gets a the avatar stream for a given identity
   *
   *
   * @param identity
   * @return
   */
  @Override
  public InputStream getBannerInputStreamById(Identity identity) throws IOException {
    return storage.getBannerInputStreamById(identity);
  }

  @Override
  public int countSpaceMemberIdentitiesByProfileFilter(Space space, ProfileFilter profileFilter, Type type) {
    return storage.countSpaceMemberIdentitiesByProfileFilter(space, profileFilter, type);
  }

  @Override
  public List<Identity> getIdentities(String providerId,
                                      String firstCharacterFieldName,
                                      char firstCharacter,
                                      String sortField,
                                      String sortDirection,
                                      long offset,
                                      long limit) {
    ProfileFilter profileFilter = null;
    if (firstCharacter > 0 || StringUtils.isNotBlank(sortField)) {
      profileFilter = new ProfileFilter();
      profileFilter.setFirstCharFieldName(firstCharacterFieldName);
      profileFilter.setFirstCharacterOfName(firstCharacter);
      profileFilter.setSorting(Sorting.valueOf(sortField, sortDirection));
    }

    //
    IdentityFilterKey key = new IdentityFilterKey(providerId, profileFilter);
    ListIdentitiesKey listKey = new ListIdentitiesKey(key, offset, limit);

    //
    ListIdentitiesData keys = identitiesCache.get(new ServiceContext<ListIdentitiesData>() {
      public ListIdentitiesData execute() {
        List<Identity> got = storage.getIdentities(providerId,
                                                   firstCharacterFieldName,
                                                   firstCharacter,
                                                   sortField,
                                                   sortDirection,
                                                   offset,
                                                   limit);
        return buildIds(got);
      }
    }, listKey);

    //
    return buildIdentities(keys);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Identity> getIdentities(String providerId, long offset, long limit) {
    return this.getIdentities(providerId, null, '\u0000', null, null, offset, limit);
  }

  @Override
  public List<String> sortIdentities(List<String> identityRemoteIds,
                                     String firstCharacterFieldName,
                                     char firstCharacter,
                                     String sortField,
                                     String sortDirection) {
    return storage.sortIdentities(identityRemoteIds, firstCharacterFieldName, firstCharacter, sortField, sortDirection);
  }
}
