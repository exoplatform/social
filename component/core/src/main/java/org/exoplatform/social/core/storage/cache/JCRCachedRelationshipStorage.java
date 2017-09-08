/*
 * Copyright (C) 2003-2017 eXo Platform SAS.
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

import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImpl;

/**
 *
 */
public class JCRCachedRelationshipStorage extends CachedRelationshipStorage {
  public JCRCachedRelationshipStorage(RelationshipStorageImpl relationshipStorage, IdentityStorageImpl identityStorage, SocialStorageCacheService cacheService) {
    super(relationshipStorage, identityStorage, cacheService);
  }
}
