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

/**
 * Immutable space reference key.
 * This key is used to cache space by displayName, prettyName, groupId or url.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SpaceRefKey implements CacheKey {
  private static final long serialVersionUID = -7953413006239399161L;

  private final String displayName;
  private final String prettyName;
  private final String groupId;
  private final String url;

  public SpaceRefKey(final String displayName) {
    this(displayName, null, null, null);
  }

  public SpaceRefKey(final String displayName, final String prettyName) {
    this(displayName, prettyName, null, null);
  }

  public SpaceRefKey(final String displayName, final String prettyName, final String groupId) {
    this(displayName, prettyName, groupId, null);
  }

  public SpaceRefKey(final String displayName, final String prettyName, final String groupId, final String url) {
    this.displayName = displayName;
    this.prettyName = prettyName;
    this.groupId = groupId;
    this.url = url;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SpaceRefKey)) {
      return false;
    }

    SpaceRefKey that = (SpaceRefKey) o;

    if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) {
      return false;
    }
    if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) {
      return false;
    }
    if (prettyName != null ? !prettyName.equals(that.prettyName) : that.prettyName != null) {
      return false;
    }
    if (url != null ? !url.equals(that.url) : that.url != null) {
      return false;
    }

    return true;

  }

  @Override
  public int hashCode() {
    int result = (displayName != null ? displayName.hashCode() : 0);
    result = 31 * result + (prettyName != null ? prettyName.hashCode() : 0);
    result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
    result = 31 * result + (url != null ? url.hashCode() : 0);
    return result;
  }
  
}
