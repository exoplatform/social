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

package org.exoplatform.social.core.storage.cache.model.data;

/**
 * Immutable simple data.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SimpleCacheData<T> implements CacheData<T> {
  private static final long serialVersionUID = 7655513073939723569L;

  private final T t;

  public SimpleCacheData(final T t) {
    this.t = t;
  }

  public T build() {
    return t;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SimpleCacheData)) return false;

    SimpleCacheData<?> that = (SimpleCacheData<?>) o;

    return t != null ? t.equals(that.t) : that.t == null;

  }

  @Override
  public int hashCode() {
    return t != null ? t.hashCode() : 0;
  }
}
