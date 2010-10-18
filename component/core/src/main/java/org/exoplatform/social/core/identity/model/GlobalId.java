/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.identity.model;

import java.io.Serializable;

import org.exoplatform.services.cache.ExoCache;

/**
 * A GlobalId according to the definition of OpenSocial.
 * This class implements {@link Serializable} so that it can be used as key cache for {@link ExoCache}
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class GlobalId implements Serializable {
  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;

  /**
   * the ':' separator character
   */
  public static final String SEPARATOR = ":";

  /**
   * Domain-Name part
   */
  private String domain;

  /**
   * Local-Id part
   */
  private String localId;

  /**
   * Creates a new GlobalId
   * @param id string representation of the global id. Must be of the form domain:localId
   * @throws IllegalArgumentException if the id does not have the expected form
   */
  public GlobalId(String id) {
    if (!isValid(id)) {
      throw new IllegalArgumentException(id + " is not a valid GlobalId. "
                                         + "According to Opensocial specification, it should be of the form: "
                                         + "Global-Id   = Domain-Name \":\" Local-Id ");
    }
    String[] globalId = id.split(SEPARATOR);
    domain = globalId[0];
    localId = globalId[1];
  }

  public static boolean isValid(String id) {
    return (id!=null && id.indexOf(SEPARATOR) > 0);
  }

  public String getDomain() {
    return domain;
  }

  public String getLocalId() {
    return localId;
  }

  public String toString() {
    return domain + SEPARATOR + localId;
  }

  /**
   * creates a global id based on provider and remote id
   * @param providerId
   * @param remoteId
   * @return
   */
  public static GlobalId create(String providerId, String remoteId) {
    if (providerId == null ) {
      throw new IllegalArgumentException("Could not create a valid GlobalId with null providerId");
    }
    if (remoteId == null ) {
      throw new IllegalArgumentException("Could not create a valid GlobalId with null remoteId");
    }
    return new GlobalId(providerId + SEPARATOR + remoteId);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((domain == null) ? 0 : domain.hashCode());
    result = prime * result + ((localId == null) ? 0 : localId.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof GlobalId)) {
      return false;
    }
    GlobalId other = (GlobalId) obj;
    if (domain == null) {
      if (other.domain != null) {
        return false;
      }
    } else if (!domain.equals(other.domain)) {
      return false;
    }
    if (localId == null) {
      if (other.localId != null) {
        return false;
      }
    } else if (!localId.equals(other.localId)) {
      return false;
    }
    return true;
  }


}
