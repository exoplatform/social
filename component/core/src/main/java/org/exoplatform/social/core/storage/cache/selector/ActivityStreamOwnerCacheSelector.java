/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;

/**
 * 
 */
public class ActivityStreamOwnerCacheSelector extends ScopeCacheSelector<ActivityKey, ActivityData> {

  private String streamOwner;

  public ActivityStreamOwnerCacheSelector(final String streamOwner) {
    if (streamOwner == null) {
      throw new NullPointerException();
    }
    
    this.streamOwner = streamOwner;
  }

  @Override
  public boolean select(final ActivityKey key, final ObjectCacheInfo<? extends ActivityData> ocinfo) {
    if (!super.select(key, ocinfo)) {
      return false;
    }

    ActivityData data = ocinfo.get();
    if (streamOwner.equals(data.getStreamOwner())) {
      return true;
    }

    return false;
  }

}
