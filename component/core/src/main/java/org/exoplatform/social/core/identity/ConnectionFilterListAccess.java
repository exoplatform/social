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
package org.exoplatform.social.core.identity;

import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.common.ListAccessValidator;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.storage.api.RelationshipStorage;

/**
 * ListAccess is used in loading connection with the input providerId, existing Identity and ProfileFilter, .
 * With this list we can manage the size of returned list by offset and limit.
 * 
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 22, 2011  
 */
public class ConnectionFilterListAccess implements ListAccess<Identity> {
  
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
   * providerId for profile filter.
   */
  String providerId;
  
  /**
   * profile filter for searching connections.
   */
  ProfileFilter profileFilter;
  
  /**
   * The connection list access Type Enum.
   */
  public enum Type {
    /** Gets a list of identities who invited to connect to the provided identity and profile filter */
    PROFILE_FILTER_INCOMMING,
    /** Gets a list of identities who were invited to connect by the provided identity and profile filter */
    PROFILE_FILTER_OUTGOING,
    /** Gets a list of identities who were connected with the provided identity and profile filter */
    PROFILE_FILTER_CONNECTION
  }
  
  /**
   * The constructor.
   * 
   * @param relationshipStorage Storage object of Relationship.
   * @param identity Identity to get connection. 
   */
  public ConnectionFilterListAccess(RelationshipStorage relationshipStorage, Identity identity, ProfileFilter filter) {
    this.relationshipStorage = relationshipStorage;
    this.identity = identity;
    this.profileFilter = filter;
  }
  
  /**
   * The constructor.
   * 
   * @param relationshipStorage
   * @param identity
   * @param type
   * @since 1.2.3
   */
  public ConnectionFilterListAccess(RelationshipStorage relationshipStorage, Identity identity, ProfileFilter filter, Type type) {
    this(relationshipStorage, identity, filter);
    this.type = type;
  }
  
  /**
   * {@inheritDoc}
   */
  public Identity[] load(int offset, int limit) throws Exception, IllegalArgumentException {
    ListAccessValidator.validateIndex(offset, limit, getSize());
    List<Identity> identities = null;
    switch (type) {
      case PROFILE_FILTER_CONNECTION: identities = relationshipStorage.
                                                   getConnectionsByFilter(identity, profileFilter, offset, limit);
        break;
      case PROFILE_FILTER_INCOMMING: identities = relationshipStorage.getIncomingByFilter(identity, profileFilter, offset, limit);
        break;
      case PROFILE_FILTER_OUTGOING: identities = relationshipStorage.getOutgoingByFilter(identity, profileFilter, offset, limit);
        break;
    }
    return identities.toArray(new Identity[identities.size()]);
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() throws Exception {
    switch (type) {
      case PROFILE_FILTER_CONNECTION: return relationshipStorage.getConnectionsCountByFilter(identity, profileFilter);
      case PROFILE_FILTER_INCOMMING: return relationshipStorage.getIncomingCountByFilter(identity, profileFilter);
      case PROFILE_FILTER_OUTGOING: return relationshipStorage.getOutgoingCountByFilter(identity, profileFilter);
      default:
        return 0;
    }
  }

  /**
   * Gets the type of connection list access.
   * 
   * @return
   * @since 1.2.3
   */
  public Type getType() {
    return type;
  }

  /**
   * Sets the type of connection list access.
   * 
   * @param type
   * @since 1.2.3
   */
  public void setType(Type type) {
    this.type = type;
  }
}

