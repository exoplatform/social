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

import org.apache.commons.lang3.StringUtils;

/**
 * Immutable identity composite key.
 * This key is used to index the identities by remoteId.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class IdentityCompositeKey implements CacheKey {
  private static final long serialVersionUID = -3140083161724994169L;

  private final String providerId;

  private final String remoteId;

  public IdentityCompositeKey(final String providerId, final String remoteId) {
    this.providerId = providerId;
    this.remoteId = remoteId;
  }

  public String getProviderId() {
    return providerId;
  }

  public String getRemoteId() {
    return remoteId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IdentityCompositeKey)) {
      return false;
    }
    IdentityCompositeKey that = (IdentityCompositeKey) o;
    return StringUtils.equals(providerId, that.providerId) && StringUtils.equals(remoteId, that.remoteId);
  }

  @Override
  public int hashCode() {
    int result = (providerId != null ? providerId.hashCode() : 0);
    result = 31 * result + (remoteId != null ? remoteId.hashCode() : 0);
    return result;
  }

}
