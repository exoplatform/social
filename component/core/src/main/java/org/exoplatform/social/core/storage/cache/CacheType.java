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

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.storage.cache.loader.CacheLoader;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.key.CacheKey;

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public enum CacheType {

  //
  IDENTITY("IdentityCache"),
  IDENTITY_INDEX("IdentityIndexCache"),
  PROFILE("ProfileCache"),
  IDENTITIES_COUNT("IdentitiesCountCache"),
  IDENTITIES("IdentitiesCache"),

  //
  RELATIONSHIP("RelationshipCache"),
  RELATIONSHIP_FROM_IDENTITY("RelationshipFromIdentityCache"),
  RELATIONSHIPS_COUNT("RelationshipsCountCache"),
  RELATIONSHIPS("RelationshipsCache"),
  SUGGESTIONS("SuggestionsCache"),

  //
  ACTIVITY("ActivityCache"),
  ACTIVITIES_COUNT("ActivitiesCountCache"),
  ACTIVITIES("ActivitiesCache"),

  //
  SPACE("SpaceCache"),
  SPACE_REF("SpaceRefCache"),
  SPACES_COUNT("SpacesCountCache"),
  SPACES("SpacesCache")

  ;

  private final String name;

  private CacheType(final String name) {
    this.name = name;
  }

  public <K extends CacheKey, V extends Serializable> ExoCache<K, V> getFromService(CacheService service) {
    return service.getCacheInstance(name);
  }

  public <K extends CacheKey, V extends Serializable> FutureExoCache<K, V, ServiceContext<V>> createFutureCache(
      ExoCache<K, V> cache) {

    return new FutureExoCache<K, V, ServiceContext<V>>(new CacheLoader<K, V>(), cache);

  }

}
