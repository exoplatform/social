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

package org.exoplatform.social.core.storage.api;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.RelationshipStorageException;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public interface RelationshipStorage {

  /**
   * Saves relationship.
   *
   * @param relationship the relationship
   * @throws org.exoplatform.social.core.storage.RelationshipStorageException
   */
  public Relationship saveRelationship(final Relationship relationship) throws RelationshipStorageException;

  /**
   * Removes the relationship.
   *
   * @param relationship the relationship
   * @throws RelationshipStorageException
   */
  public void removeRelationship(Relationship relationship) throws RelationshipStorageException;

  /**
   * Gets the relationship.
   *
   * @param uuid the uuid
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public Relationship getRelationship(String uuid) throws RelationshipStorageException;

  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   *
   * @param sender the identity
   * @param type
   * @param listCheckIdentity identity the checking identities
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getSenderRelationships(
      final Identity sender, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException;

  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   *
   * @param senderId the identity id
   * @param type
   * @param listCheckIdentity identity the checking identities
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getSenderRelationships(
      final String senderId, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException;

  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   *
   * @param receiver the identity id
   * @param type
   * @param listCheckIdentity identityId the checking identity ids
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getReceiverRelationships(
      final Identity receiver, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException;

  /**
   * Gets the relationship of 2 identities.
   *
   * @param identity1 the identity1
   * @param identity2 the identity2
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public Relationship getRelationship(final Identity identity1, final Identity identity2)
      throws RelationshipStorageException;

  /**
   * Gets the list of relationship by identity matching with checking
   * identity ids
   *
   * @param identity the identity
   * @param type
   * @param listCheckIdentity identity the checking identities
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getRelationships(
      final Identity identity, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException;

  /**
   * Gets the list of relationship by identity matching with checking
   * identity ids
   *
   * @param identity the identity
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Identity> getRelationships(final Identity identity, long offset, long limit)
      throws RelationshipStorageException;

  /**
   * Gets the list of relationship by identity matching with checking
   * identity ids with offset, limit.
   *
   * @param receiver the identity
   * @param offset
   * @param limit
   * @return the identities
   * @throws RelationshipStorageException
   */
  public List<Identity> getIncomingRelationships(Identity receiver,
                                                 long offset, long limit) throws RelationshipStorageException;

  /**
   * Gets the count of the list of relationship by identity matching with checking
   * identity ids.
   *
   * @param receiver the identity
   * @return the relationship number
   * @throws RelationshipStorageException
   */
   public int getIncomingRelationshipsCount(Identity receiver) throws RelationshipStorageException;

  /**
   * Gets the list of relationship by identity matching with checking
   * identity ids with offset, limit.
   *
   * @param sender the identity
   * @param offset
   * @param limit
   * @return the identities
   * @throws RelationshipStorageException
   */
  public List<Identity> getOutgoingRelationships(Identity sender,
                                                 long offset, long limit) throws RelationshipStorageException;

  /**
   * Gets the count of the list of relationship by identity matching with checking
   * identity ids.
   *
   * @param sender the identity
   * @return the relationship number
   * @throws RelationshipStorageException
   */
  public int getOutgoingRelationshipsCount(Identity sender) throws RelationshipStorageException;

  /**
   * Gets the count of identities by identity matching with checking
   * identity ids.
   *
   * @param identity the identity id
   * @return the relationships number
   * @throws RelationshipStorageException
   * @since 1.2.0-Beta3
   */
   public int getRelationshipsCount(Identity identity) throws RelationshipStorageException;

  /**
   * Gets connections with the identity.
   *
   * @param identity
   * @param offset
   * @param limit
   * @return number of connections belong to limitation of offset and limit.
   * @throws RelationshipStorageException
   * @since 1.2.0-GA
   */
  public List<Identity> getConnections(Identity identity, long offset, long limit) throws RelationshipStorageException;

  /**
   * Gets connections with the identity.
   *
   * @param identity
   * @return number of connections belong to limitation of offset and limit.
   * @throws RelationshipStorageException
   * @since 1.2.0-GA
   */
  public List<Identity> getConnections(Identity identity) throws RelationshipStorageException;

  /**
   * Gets count of connection with the identity.
   *
   * @param identity
   * @return
   * @throws RelationshipStorageException
   * @since 1.2.0-GA
   */
  public int getConnectionsCount(Identity identity) throws RelationshipStorageException;


  /**
   * Gets the list access to get a list of identities who is connected with the provided identity
   * and filtered by profile filter.
   *
   * @param existingIdentity the provided identity.
   * @param profileFilter the provided profile filter.
   * @param offset
   * @param limit
   * @return the list of identity
   * @since 1.2.3
   * 
   */
  public List<Identity> getConnectionsByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter, final long offset, final long limit)
      throws RelationshipStorageException;
  
  /**
   * Gets the list access to get a list of identities who invited to connect to the provided identity
   * and filtered by profile filter.
   *
   * @param existingIdentity the provided identity
   * @param profileFilter    the provided profile filter
   * @param offset
   * @param limit
   * @return the list of identity
   * @since  1.2.3
   */
  public List<Identity> getIncomingByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter, final long offset, final long limit)
      throws RelationshipStorageException;
  
  /**
   * Gets the list access to get a list of identities who was invited by the provided identity to connect
   * and filtered by profile filter.
   *
   * @param existingIdentity the provided identity
   * @param profileFilter    the provided profile filter
   * @param offset
   * @param limit
   * @return the list of identity
   * @since  1.2.3
   */
  public List<Identity> getOutgoingByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter, final long offset, final long limit)
      throws RelationshipStorageException;
  
  
  /**
   * Gets the count of identities who is connected with the provided identity and filtered by profile filter.
   *
   * @param existingIdentity
   * @param profileFilter
   * @return count of identities
   * @throws RelationshipStorageException
   * @since  1.2.3
   */
  public int getConnectionsCountByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter) throws RelationshipStorageException;
  /**
   * 
   * Gets count of identities who invited to connect to the provided identity
   * and filtered by profile filter.
   *
   * @param existingIdentity
   * @param profileFilter
   * @return count of identities
   * @throws RelationshipStorageException
   * @since  1.2.3
   */
  public int getIncomingCountByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter) throws RelationshipStorageException;
  
  /**
   * Gets count of identities who was invited by the provided identity to connect
   * and filtered by profile filter.
   *
   * @param existingIdentity
   * @param profileFilter
   * @return count of identities
   * @throws RelationshipStorageException
   * @since  1.2.3
   */
  public int getOutgoingCountByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter) throws RelationshipStorageException;
  
  /**
   * Gets the suggestions with number of commons users relate to provided identity
   * @param identity the provided identity
   * @param offset the offset position to get
   * @param limit the limit of return result
   * @return the map of suggestion users and number of commons users
   * @throws RelationshipStorageException
   * @since 4.0.x
   */
  public Map<Identity, Integer> getSuggestions(Identity identity, int offset, int limit) throws RelationshipStorageException;
  
}