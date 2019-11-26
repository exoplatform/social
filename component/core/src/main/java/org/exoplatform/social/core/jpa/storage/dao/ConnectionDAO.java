/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.storage.dao;

import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.search.Sorting;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 4, 2015  
 */
public interface ConnectionDAO extends GenericDAO<ConnectionEntity, Long> {

  /**
   * Has the connections
   * 
   * @param identity the Identity
   * @param status the status of connection
   * @return number of connection
   */
  long count(Identity identity, Relationship.Type status);

  /**
   * Get connection of 2 users
   * 
   * @param identity1 The first Identity
   * @param identity2 the second Identity
   * @return the connection entity
   */
  ConnectionEntity getConnection(Identity identity1, Identity identity2);

  /**
   *
   * @param sender the id of sender Identity
   * @param receiver the id of receiver Identity
   * @return the connection entity
   */
  ConnectionEntity getConnection(Long sender, Long receiver);

  
  /**
   * @param identity the Identity
   * @param type type of connection
   * @param firstCharacterField first character field name to use
   * @param firstCharacter first character to filter on first character of name
   * @param offset the start index
   * @param limit the max items to load
   * @param sorting sortby field name and sort direction
   * @return list of connection entities
   */
  List<ConnectionEntity> getConnections(Identity identity, Type type, String firstCharacterField, char firstCharacter, long offset, long limit, Sorting sorting);

  /**
   *
   * @param sender the sender Identity
   * @param receiver the receiver Identity
   * @param status the connection status
   * @return list of connection entities
   */
  List<ConnectionEntity> getConnections(Identity sender, Identity receiver, Type status);

  /**
   * @param identity the Identity
   * @param type the connection type
   * @return number of connections
   */
  int getConnectionsCount(Identity identity, Type type);

  /**
   * @param identity the Identity
   * @param limit max item to load
   * @return list of connection entities
   */
  List<ConnectionEntity> getLastConnections(Identity identity, int limit);

  /**
   *
   * @param existingIdentity the Identity
   * @param profileFilter the ProfileFilter
   * @param type connection type
   * @param offset the start index
   * @param limit max items to load
   * @return list of connection entities
   */
  List<ConnectionEntity> getConnectionsByFilter(Identity existingIdentity, ProfileFilter profileFilter, Type type, long offset, long limit);

  /**
   *
   * @param identity the Identity
   * @param profileFilter the profile filter
   * @param type connection type
   * @return number of connections
   */
  int getConnectionsByFilterCount(Identity identity, ProfileFilter profileFilter, Type type);


  List<Long> getSenderIds(long receiverId, Type status, int offset, int limit);
  List<Long> getReceiverIds(long receiverId, Type status, int offset, int limit);
}
