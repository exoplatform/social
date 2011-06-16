/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.core.identity;

import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.common.ListAccessValidator;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.RelationshipStorage;

/**
 * ListAccess is used in loading connection with the input identity.
 * With this list we can manage the size of returned list by offset and limit.
 * 
 * @author <a href="http://hanhvq@gmail.com">hanhvq(gmail dot com)</a>
 * @since 1.2.0-GA
 */
public class ConnectionListAccess implements ListAccess<Identity> {
  
  private RelationshipStorage relationshipStorage; 
  
  /**
   * Identity use for load.
   */
  Identity identity;
  
  /** 
   * The type of connection list access.
   */
  Type type;
  
  /**
   * The connection list access Type Enum.
   */
  public enum Type {
    /** 
     * Gets all the identities who were connected, invited by the provided identity 
     * and who invited to connect to the provided identity. 
     */
    ALL,
    /** Gets a list of identities who were connected with the provided identity. */
    CONNECTION,
    /** Gets a list of identities who invited to connect to the provided identity. */
    INCOMING,
    /** Gets a list of identities who were invited to connect by the provided identity. */
    OUTGOING
  }
  
  /**
   * The constructor.
   * 
   * @param relationshipStorage Storage object of Relationship.
   * @param identity Identity to get connection. 
   */
  public ConnectionListAccess(RelationshipStorage relationshipStorage, Identity identity) {
    this.relationshipStorage = relationshipStorage;
    this.identity = identity;
  }
  
  /**
   * The constructor.
   * 
   * @param relationshipStorage
   * @param identity
   * @param type
   * @since 1.2.0-Beta3
   */
  public ConnectionListAccess(RelationshipStorage relationshipStorage, Identity identity, Type type) {
    this(relationshipStorage, identity);
    this.type = type;
  }
  
  /**
   * {@inheritDoc}
   */
  public Identity[] load(int offset, int limit) throws Exception, IllegalArgumentException {
    ListAccessValidator.validateIndex(offset, limit, getSize());
    List<Identity> identities = null;
    switch (type) {
      case ALL: identities = relationshipStorage.getRelationships(identity, offset, limit);
        break;
      case CONNECTION: identities = relationshipStorage.getConnections(identity, offset, limit);
        break;
      case INCOMING: identities = relationshipStorage.getIncomingRelationships(identity, offset, limit);
        break;
      case OUTGOING: identities = relationshipStorage.getOutgoingRelationships(identity, offset, limit);
        break;
    }
    return identities.toArray(new Identity[identities.size()]);
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() throws Exception {
    switch (type) {
      case ALL: return relationshipStorage.getRelationshipsCount(identity);
      case CONNECTION: return relationshipStorage.getConnectionsCount(identity);
      case INCOMING: return relationshipStorage.getIncomingRelationshipsCount(identity);
      case OUTGOING: return relationshipStorage.getOutgoingRelationshipsCount(identity);
      default:
        return 0;
    }
  }

  /**
   * Gets the type of connection list access.
   * 
   * @return
   * @since 1.2.0-Beta3
   */
  public Type getType() {
    return type;
  }

  /**
   * Sets the type of connection list access.
   * 
   * @param type
   * @since 1.2.0-Beta3
   */
  public void setType(Type type) {
    this.type = type;
  }
}
