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

package org.exoplatform.social.core.storage.cache.selector;

import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.social.core.storage.cache.model.key.ScopeCacheKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ScopeCacheSelector<T extends ScopeCacheKey, U> implements CachedObjectSelector<T, U> {
  
  public boolean select(final T key, final ObjectCacheInfo<? extends U> ocinfo) {
    return ScopeCacheKey.getCurrentRepositoryName().equals(key.getScope());
  }

  public void onSelect(final ExoCache<? extends T, ? extends U> exoCache, final T key, final ObjectCacheInfo<? extends U> ocinfo) throws Exception {
    exoCache.remove(key);
  }

}
