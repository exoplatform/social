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
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ListSpaceMembersKey extends ListIdentitiesKey {

  private final SpaceKey spaceKey;

  public ListSpaceMembersKey(final SpaceKey spaceKey, final IdentityFilterKey identityKey, final long offset, final long limit) {
    super(identityKey, offset, limit);
    this.spaceKey = spaceKey;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ListSpaceMembersKey)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ListSpaceMembersKey that = (ListSpaceMembersKey) o;

    if (spaceKey != null ? !spaceKey.equals(that.spaceKey) : that.spaceKey != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (spaceKey != null ? spaceKey.hashCode() : 0);
    return result;
  }
}
