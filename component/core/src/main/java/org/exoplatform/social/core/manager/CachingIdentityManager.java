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
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.IdentityStorage;

/**
 * Class CachingIdentityManager extends IdentityManagerImpl with caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @since Nov 24, 2010
 * @version 1.2.0-GA
 */
public class CachingIdentityManager extends IdentityManagerImpl {

  /**
   * identityCacheById with key = uuid/ identity.id
   */
  private ExoCache<String, Identity>       identityCacheById;

  /**
   * identityListCache with key = identityProvider
   */
  private ExoCache<String, List<Identity>> identityListCacheByIdentityProvider;

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
    this.identityCacheById = cacheService.getCacheInstance("exo.social.IdentityManager.IdentityCacheById");
    this.identityListCacheByIdentityProvider = cacheService.getCacheInstance("exo.social" +
            ".IdentityManager.IdentityListCacheByIdentityProvider");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Identity getIdentity(String identityId, boolean forceLoadOrReloadProfile) {
    Identity cachedIdentity = identityCacheById.get(identityId);
    if (cachedIdentity != null && !forceLoadOrReloadProfile) {
      return cachedIdentity;
    }
    Identity foundIdentity = super.getIdentity(identityId, forceLoadOrReloadProfile);
    if (foundIdentity != null) {
      identityCacheById.put(identityId, foundIdentity);
      cachedIdentity = foundIdentity;
    }
    return cachedIdentity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Identity updateIdentity(Identity identity) {
    updateIdentityCaches(identity);
    return super.updateIdentity(identity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteIdentity(Identity identity) {
    super.deleteIdentity(identity);
    updateIdentityCaches(identity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveIdentity(Identity identity) {
    super.saveIdentity(identity);
    updateIdentityCaches(identity);
  }

  private void updateIdentityCaches(Identity identity) {
    if (identityCacheById.get(identity.getId()) != null) {
      identityCacheById.remove(identity.getId());
    }
    if (identityListCacheByIdentityProvider.get(identity.getProviderId()) != null) {
      identityListCacheByIdentityProvider.remove(identity.getProviderId());
    }
  }
}
