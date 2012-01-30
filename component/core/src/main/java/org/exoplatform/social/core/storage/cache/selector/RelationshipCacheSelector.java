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

import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.ListRelationshipsKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipCountKey;
import org.exoplatform.social.core.storage.cache.model.key.ScopeCacheKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class RelationshipCacheSelector extends ScopeCacheSelector<ScopeCacheKey, Object> {

  private String[] target;

  public RelationshipCacheSelector(final String... target) {
    this.target = target;
  }

  @Override
  public boolean select(final ScopeCacheKey key, final ObjectCacheInfo<? extends Object> ocinfo) {

    if (!super.select(key, ocinfo)) {
      return false;
    }

    if (key instanceof ListRelationshipsKey) {
      return select((ListRelationshipsKey) key);
    }

    if (key instanceof RelationshipCountKey) {
      return select((RelationshipCountKey) key);
    }

    return false;

  }

  private boolean select(final ListRelationshipsKey key) {

    if (key.getKey() instanceof IdentityKey) {
      String id = ((IdentityKey) key.getKey()).getId();
      return id.equals(target[0]) || id.equals(target[1]);
    }

    return true;

  }

  private boolean select(final RelationshipCountKey key) {

    if (key.getKey() instanceof IdentityKey) {
      String id = ((IdentityKey) key.getKey()).getId();
      return id.equals(target[0]) || id.equals(target[1]);
    }

    return true;

  }

}
