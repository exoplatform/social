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
   * Constructor.
   * 
   * @param relationshipStorage Storage object of Relationship.
   * @param identity Identity to get connection. 
   */
  public ConnectionListAccess(RelationshipStorage relationshipStorage, Identity identity) {
    this.relationshipStorage = relationshipStorage;
    this.identity = identity;
  }

  /**
   * {@inheritDoc}
   */
  public Identity[] load(int offset, int limit) throws Exception, IllegalArgumentException {
    ListAccessValidator.validateIndex(offset, limit, getSize());
    List<Identity> identities = relationshipStorage.getConnections(identity, offset, limit);
    return identities.toArray(new Identity[identities.size()]);
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() throws Exception {
    return relationshipStorage.getConnectionsCount(identity);
  }
}
