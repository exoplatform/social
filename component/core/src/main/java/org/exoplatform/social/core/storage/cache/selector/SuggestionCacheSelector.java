/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.storage.cache.selector;

import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.ScopeCacheKey;
import org.exoplatform.social.core.storage.cache.model.key.SuggestionKey;

public class SuggestionCacheSelector extends ScopeCacheSelector<ScopeCacheKey, Object> {
  
  private String[] target;

  public SuggestionCacheSelector(final String... target) {
    this.target = target;
  }
  
  @Override
  public boolean select(final ScopeCacheKey key, final ObjectCacheInfo<? extends Object> ocinfo) {
    
    if (!super.select(key, ocinfo)) {
      return false;
    }

    if (key instanceof SuggestionKey) {
      return select((SuggestionKey) key);
    }

    return false;

  }

  private boolean select(final SuggestionKey key) {

    if (key.getKey() instanceof IdentityKey) {
      String id = ((IdentityKey) key.getKey()).getId();
      for (String i : target) {
        if (id.equals(i)) return true;
      }
    }

    return true;

  }
  
}
