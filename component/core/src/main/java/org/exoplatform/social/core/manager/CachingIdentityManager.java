/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.manager;

import java.util.List;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.GlobalId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.storage.IdentityStorage;

/**
 * Class CachingIdentityManager extends IdentityManagerImpl with caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @since Nov 24, 2010
 * @version 1.2.0-GA
 */
public class CachingIdentityManager extends IdentityManagerImpl {
  private static final Log                 LOG = ExoLogger.getExoLogger(CachingIdentityManager.class);

  private ExoCache<String, Identity>       identityCacheById;

  /**
   * identityCache
   */
  private ExoCache<GlobalId, Identity>     identityCache;

  /**
   * identityListCache with key = identityProvider
   */
  private ExoCache<String, List<Identity>> identityListCache;

  /**
   * Instantiates a new caching identity manager.
   *
   * @param identityStorage
   * @param defaultIdentityProvider the builtin default identity provider to use
   *          when when no other provider match
   * @param cacheService
   */
  public CachingIdentityManager(IdentityStorage identityStorage,
                                IdentityProvider<?> defaultIdentityProvider,
                                CacheService cacheService) {
    super(identityStorage, defaultIdentityProvider);
    this.identityCacheById = cacheService.getCacheInstance(getClass().getName()
        + "identityCacheById");
    this.identityCache = cacheService.getCacheInstance(getClass().getName() + "identityCache");
    this.identityListCache = cacheService.getCacheInstance(getClass().getName()
        + "identityListCache");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Identity getIdentity(String id, boolean loadProfile) {
    Identity cachedIdentity = null;
    // attempts to match a global id in the form "providerId:remoteId"
    if (GlobalId.isValid(id)) {
      GlobalId globalId = new GlobalId(id);
      String providerId = globalId.getDomain();
      String remoteId = globalId.getLocalId();
      cachedIdentity = identityCache.get(globalId);
      if (cachedIdentity == null) {
        cachedIdentity = getOrCreateIdentity(providerId, remoteId, loadProfile);
        if (cachedIdentity != null) {
          identityCache.put(globalId, cachedIdentity);
          return cachedIdentity;
        }
        // retry with providerId:nodeId
        if (cachedIdentity == null) {
          String tempId = globalId.getLocalId();
          cachedIdentity = this.getIdentityStorage().findIdentityById(tempId);
          if (cachedIdentity != null) {
            id = tempId;
          }
        }
      }

    } else {
      cachedIdentity = identityCacheById.get(id);
      if (cachedIdentity == null) {
        cachedIdentity = this.getIdentityStorage().findIdentityById(id);
      }
    }

    if (cachedIdentity != null) {
      if (loadProfile) {
        this.getIdentityStorage().loadProfile(cachedIdentity.getProfile());
      }
      identityCacheById.put(id, cachedIdentity);
    }
    if (cachedIdentity == null) {
      LOG.info("Can not get identity with id: " + id);
    }
    return cachedIdentity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Identity getIdentity(String providerId, String remoteId, boolean loadProfile) {
    GlobalId globalIdCacheKey = GlobalId.create(providerId, remoteId);
    Identity cachedIdentity = identityCache.get(globalIdCacheKey);
    if (cachedIdentity == null) {
      IdentityProvider<?> identityProvider = getIdentityProvider(providerId);
      cachedIdentity = identityProvider.getIdentityByRemoteId(remoteId);
      if (cachedIdentity != null) {
        Identity storedIdentity = this.getIdentityStorage().findIdentity(providerId, remoteId);
        if (storedIdentity != null) {
          cachedIdentity.setId(storedIdentity.getId());
        } else {
          // save new identity
          this.getIdentityStorage().saveIdentity(cachedIdentity);
        }
      }
      identityCache.put(globalIdCacheKey, cachedIdentity);
    }
    if (loadProfile && cachedIdentity.getProfile().getId() == null) {
      this.getIdentityStorage().loadProfile(cachedIdentity.getProfile());
    }

    return cachedIdentity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteIdentity(Identity identity) {
    if (identity.getId() == null) {
      LOG.warn("identity.getId() must not be null of [" + identity + "]");
      return;
    }
    this.getIdentityStorage().deleteIdentity(identity);
    identityCacheById.remove(identity.getId());
    identityCache.remove(identity.getGlobalId());
    identityListCache.remove(identity.getProviderId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Identity getOrCreateIdentity(String providerId, String remoteId, boolean loadProfile) {
    GlobalId globalIdCacheKey = GlobalId.create(providerId, remoteId);
    Identity cachedIdentity = identityCache.get(globalIdCacheKey);
    if (cachedIdentity == null) {
      IdentityProvider<?> identityProvider = this.getIdentityProvider(providerId);

      Identity identity1 = identityProvider.getIdentityByRemoteId(remoteId);
      Identity result = this.getIdentityStorage().findIdentity(providerId, remoteId);
      // FIXME make it clear here when both identity1 and result != null.
      if (result == null) {
        if (identity1 != null) {
          // identity is valid for provider, but no yet referenced in storage
          saveIdentity(identity1);
          this.getIdentityStorage().saveProfile(identity1.getProfile());
          result = identity1;
        } else {
          // Not found in provider, so return null
          return result;
        }
      } else {
        if (identity1 == null) {
          // in the case: identity is stored but identity is not found from
          // provider, delete that identity
          this.getIdentityStorage().deleteIdentity(result);
          return null;
        }
        if (loadProfile) {
          this.getIdentityStorage().loadProfile(result.getProfile());
        }
      }
      cachedIdentity = result;
      if (cachedIdentity.getId() != null) {
        identityCache.put(globalIdCacheKey, cachedIdentity);
      }
    } else if (loadProfile && cachedIdentity.getProfile().getId() == null) {
      this.getIdentityStorage().loadProfile(cachedIdentity.getProfile());
    }
    return cachedIdentity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveIdentity(Identity identity) {
    this.getIdentityStorage().saveIdentity(identity);
    this.getIdentityProvider(identity.getProviderId()).onSaveIdentity(identity);
    if (identity.getId() != null) {
      identityCacheById.remove(identity.getId());
      identityCache.remove(identity.getGlobalId());
    }
    identityListCache.remove(identity.getProviderId());
  }

  /**
   * Removes cache when there is changes in profile.
   *
   * @param profile
   */
  private void removeCacheForProfileChange(Profile profile) {
    Identity identity = profile.getIdentity();
    identityCacheById.remove(identity.getId());
    identityCache.remove(profile.getIdentity().getGlobalId());
    identityListCache.remove(identity.getProviderId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveProfile(Profile profile) {
    this.getIdentityStorage().saveProfile(profile);
    this.getIdentityProvider(profile.getIdentity().getProviderId()).onSaveProfile(profile);
    this.removeCacheForProfileChange(profile);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addOrModifyProfileProperties(Profile profile) throws Exception {
    this.getIdentityStorage().addOrModifyProfileProperties(profile);
    this.getIdentityProvider(profile.getIdentity().getProviderId()).onSaveProfile(profile);
    this.removeCacheForProfileChange(profile);
  }
}
