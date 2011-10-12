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

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.cache.model.data.ListIdentitiesData;
import org.exoplatform.social.core.storage.cache.model.key.ListIdentitiesKey;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.IdentityData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.data.ProfileData;
import org.exoplatform.social.core.storage.cache.model.key.IdentityCompositeKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache support for IdentityStorage.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedIdentityStorage implements IdentityStorage {

  private final ExoCache<IdentityKey, IdentityData> exoIdentityCache;
  private final ExoCache<IdentityCompositeKey, IdentityKey> exoIdentityIndexCache;
  private final ExoCache<IdentityKey, ProfileData> exoProfileCache;
  private final ExoCache<IdentityFilterKey, IntegerData> exoIdentitiesCountCache;
  private final ExoCache<ListIdentitiesKey, ListIdentitiesData> exoIdentitiesCache;

  private final FutureExoCache<IdentityKey, IdentityData, ServiceContext<IdentityData>> identityCache;
  private final FutureExoCache<IdentityCompositeKey, IdentityKey, ServiceContext<IdentityKey>> identityIndexCache;
  private final FutureExoCache<IdentityKey, ProfileData, ServiceContext<ProfileData>> profileCache;
  private final FutureExoCache<IdentityFilterKey, IntegerData, ServiceContext<IntegerData>> identitiesCountCache;
  private final FutureExoCache<ListIdentitiesKey, ListIdentitiesData, ServiceContext<ListIdentitiesData>> identitiesCache;

  private final IdentityStorageImpl storage;

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
      exoIdentityCache.put(k, new IdentityData(i));
      exoProfileCache.put(k, new ProfileData(i.getProfile()));
      data.add(new IdentityKey(i));
    }
    return new ListIdentitiesData(data);

  }

  public CachedIdentityStorage(final IdentityStorageImpl storage, final SocialStorageCacheService cacheService) {

    //
    this.storage = storage;
    this.storage.setStorage(this);

    //
    this.exoIdentityCache = cacheService.getIdentityCache();
    this.exoIdentityIndexCache = cacheService.getIdentityIndexCache();
    this.exoProfileCache = cacheService.getProfileCache();
    this.exoIdentitiesCountCache = cacheService.getCountIdentitiesCache();
    this.exoIdentitiesCache = cacheService.getIdentitiesCache();

    //
    this.identityCache = CacheType.IDENTITY.createFutureCache(exoIdentityCache);
    this.identityIndexCache = CacheType.IDENTITY_INDEX.createFutureCache(exoIdentityIndexCache);
    this.profileCache = CacheType.PROFILE.createFutureCache(exoProfileCache);
    this.identitiesCountCache = CacheType.IDENTITIES_COUNT.createFutureCache(exoIdentitiesCountCache);
    this.identitiesCache = CacheType.IDENTITIES.createFutureCache(exoIdentitiesCache);

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
    exoIdentitiesCountCache.clearCache();
    exoIdentitiesCache.clearCache();
  }

  /**
   * {@inheritDoc}
   */
  public Identity updateIdentity(final Identity identity) throws IdentityStorageException {

    //
    IdentityKey key = new IdentityKey(new Identity(identity.getId()));
    exoIdentityCache.remove(key);

    //
    return storage.updateIdentity(identity);
  }

  /**
   * {@inheritDoc}
   */
  public Identity findIdentityById(final String nodeId) throws IdentityStorageException {

    IdentityKey key = new IdentityKey(new Identity(nodeId));
    Identity i = identityCache.get(
        new ServiceContext<IdentityData>() {

          public IdentityData execute() {
            return new IdentityData(storage.findIdentityById(nodeId));
          }
        },
        key)
        .build();

    //
    if (i != null) {
      i.setProfile(loadProfile(i.getProfile()));
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
    if (data != null) {
      exoIdentityIndexCache.remove(new IdentityCompositeKey(data.getProviderId(), data.getRemoteId()));
    }
    exoProfileCache.remove(key);
    exoIdentitiesCountCache.clearCache();
    exoIdentitiesCache.clearCache();

  }

  /**
   * {@inheritDoc}
   */
  public Profile loadProfile(final Profile profile) throws IdentityStorageException {

    IdentityKey key = new IdentityKey(new Identity(profile.getIdentity().getId()));
    return profileCache.get(
        new ServiceContext<ProfileData>() {

          public ProfileData execute() {
            return new ProfileData(storage.loadProfile(profile));
          }
        },
        key)
        .build();

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
            if (i == null) return null;
            IdentityKey key = new IdentityKey(i);
            exoIdentityCache.put(key, new IdentityData(i));
            return key;
          }
        },
        key);
    
    //
    if (k != null) {
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
}
