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

import org.apache.commons.lang.StringUtils;
import org.exoplatform.social.core.relationship.model.Relationship;

/**
 * Immutable relationship data.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class RelationshipData implements CacheData<Relationship> {
  private static final long serialVersionUID = -4556401100496633230L;

  private final String id;

  private final IdentityData sender;
  private final IdentityData receiver;

  private final Relationship.Type type;

  public RelationshipData(final Relationship relationship) {
    this.id = relationship.getId();
    this.sender = new IdentityData(relationship.getSender());
    this.receiver = new IdentityData(relationship.getReceiver());
    this.type = relationship.getStatus();
  }

  public Relationship.Type getType() {
    return type;
  }

  public Relationship build() {
    Relationship relationship = new Relationship(sender.build(), receiver.build(), type);
    relationship.setId(this.id);
    return relationship;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RelationshipData)) return false;

    RelationshipData that = (RelationshipData) o;

    if (sender != null ? !sender.equals(that.sender) : that.sender != null) return false;
    if (receiver != null ? !receiver.equals(that.receiver) : that.receiver != null) return false;
    return StringUtils.equals(id, that.id) && type == that.type;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (sender != null ? sender.hashCode() : 0);
    result = 31 * result + (receiver != null ? receiver.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }
}
