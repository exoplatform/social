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

package org.exoplatform.social.core.storage.cache.model.key;

import org.exoplatform.social.core.storage.cache.model.data.ListIdentitiesData;

/**
 * Immutable activity list key.
 * This key is used to cache activity list.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ListActivitiesKey extends ListCacheKey {

  private final ActivityCountKey key;
  private final ListIdentitiesData identities;

  public ListActivitiesKey(final ActivityCountKey key, final long offset, final long limit) {
    super(offset, limit);
    this.key = key;
    this.identities = null;
  }

  public ListActivitiesKey(final ListIdentitiesData identities, final long offset, final long limit) {
    super(offset, limit);
    this.key = null;
    this.identities = identities;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ListActivitiesKey)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ListActivitiesKey that = (ListActivitiesKey) o;

    if (identities != null ? !identities.equals(that.identities) : that.identities != null) {
      return false;
    }
    if (key != null ? !key.equals(that.key) : that.key != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (key != null ? key.hashCode() : 0);
    result = 31 * result + (identities != null ? identities.hashCode() : 0);
    return result;
  }
  
}
