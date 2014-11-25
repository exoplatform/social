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
 * Immutable relationship key.
 * This key is used to cache the relationship by identity.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class RelationshipIdentityKey extends ScopeCacheKey {

  private final String identityId1;
  private final String identityId2;

  public RelationshipIdentityKey(final String identityId1, final String identityId2) {
    this.identityId1 = identityId1;
    this.identityId2 = identityId2;
  }

  public String getIdentityId1() {
    return identityId1;
  }

  public String getIdentityId2() {
    return identityId2;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RelationshipIdentityKey)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    RelationshipIdentityKey that = (RelationshipIdentityKey) o;

    if (identityId1 != null ? !identityId1.equals(that.identityId1) : that.identityId1 != null) {
      return false;
    }
    
    if (identityId2 != null ? !identityId2.equals(that.identityId2) : that.identityId2 != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (identityId1 != null ? identityId1.hashCode() : 0);
    result = 31 * result + (identityId2 != null ? identityId2.hashCode() : 0);
    return result;
  }

}
