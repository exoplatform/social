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
package org.exoplatform.social.core.manager;

import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.RelationshipStorageException;

/**
 * The RelationshipManager is used to work with connections between identities:
 *
 * <ul>
 *   <li>Get relationship between 2 identities.</li>
 *   <li>Interact between identities: invite, confirm, deny, ignore.</li>
 *   <li>Get list access to get list of connections, incoming, outgoing.</li>
 * </ul>
 */
public interface RelationshipManager {
  /**
   * Gets a relationship by its id.
   *
   * @param relationshipId the relationship id
   * @return an existing relationship or null
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  Relationship get(String relationshipId);

  /**
   * Gets a relationship between 2 identities.
   *
   * @param identity1 one identity
   * @param identity2 the other identity
   * @return the relationship between 2 identities
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  Relationship get(Identity identity1, Identity identity2);

  /**
   * Updates an existing relationship.
   *
   * @param existingRelationship the existing relationship
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void update(Relationship existingRelationship);

  /**
   * Deletes an existing relationship.
   *
   * @param existingRelationship the existing relationship
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void delete(Relationship existingRelationship);

  /**
   * Invites one identity to connected with an other identity. The first argument must be the sender identity. The
   * second argument must be the identity who is invited to connect.
   * <p/>
   * One identity is not allowed to invite himself to connect.
   *
   * @param invitingIdentity the sender identity
   * @param invitedIdentity  the identity who is invited
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Relationship inviteToConnect(Identity invitingIdentity, Identity invitedIdentity);

  /**
   * The invited identity confirms to connect to an inviting identity. The invited identity is not allowed to confirm if
   * he's not invited to connect by inviting identity.
   *
   * @param invitedIdentity  the invited identity
   * @param invitingIdentity the inviting identity
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void confirm(Identity invitedIdentity, Identity invitingIdentity);

  /**
   * The invited identity denies to connect to an inviting identity.
   * The invited identity is not allowed to deny if he's not invited to connect by inviting identity.
   *
   * @param invitedIdentity  the invited identity
   * @param invitingIdentity the inviting identity
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void deny(Identity invitedIdentity, Identity invitingIdentity);

  /**
   * The invited identity ignores to connect to an inviting identity.
   * <p/>
   * If the invited identity ignores the inviting identity, the inviting identity can not invite this invited identity
   * any more.
   *
   * @param invitedIdentity  the invited identity
   * @param invitingIdentity the inviting identity
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void ignore(Identity invitedIdentity, Identity invitingIdentity);

  /**
   * Gets the list access to get a list of identity who is connected with the provided identity.
   *
   * @param existingIdentity the existing provided identity
   * @return the list access
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  ListAccess<Identity> getConnections(Identity existingIdentity);

  /**
   * Gets the list access to get a list of identity who invited to connect to the provided identity.
   *
   * @param existingIdentity the existing provided identity
   * @return the list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Identity> getIncomingWithListAccess(Identity existingIdentity);

  /**
   * Gets the list access to get a list of identity who was invited to connect by the provided identity.
   *
   * @param existingIdentity the existing provided identity
   * @return the list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Identity> getOutgoing(Identity existingIdentity);

  /**
   * Gets the list access to get identities who is connected, was invited or invited the provided identity.
   *
   * @param existingIdentity the existing provided identity
   * @return the list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Identity> getAllWithListAccess(Identity existingIdentity);

  /**
   * Gets the relationship status between 2 identities.
   *
   * @param identity1 the identity1
   * @param identity2 the identity2
   * @return the relationship type
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Relationship.Type getStatus(Identity identity1, Identity identity2);

  /**
   * Creates a connection invitation between 2 identities.
   *
   * @param sender inviter
   * @param receiver invitee
   * @return a PENDING relation
   * @throws RelationshipStorageException
   * @LevelAPI Provisional
   * @deprecated Use {@link #inviteToConnect(Identity, Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  Relationship invite(Identity sender, Identity receiver) throws RelationshipStorageException;

  /**
   * Gets relationship the by id.
   *
   * @param id the id
   * @return the by id
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #get(String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  Relationship getRelationshipById(String id) throws RelationshipStorageException;

  /**
   * Saves a relationship.
   *
   * @param relationship the relationship
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use actions (inviteToConnect, confirm) instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void save(Relationship relationship) throws RelationshipStorageException;

  /**
   * Marks a relationship as confirmed.
   *
   * @param relationship the relationship
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #confirm(Identity, Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void confirm(Relationship relationship) throws RelationshipStorageException;

  /**
   * Denies a relationship.
   *
   * @param relationship
   * @throws RelationshipStorageException
   * @LevelAPI Provisional
   * @deprecated Use {@link #deny(Identity, Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void deny(Relationship relationship) throws RelationshipStorageException;

  /**
   * Remove a relationship.
   *
   * @param relationship the relationship
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #delete(Relationship)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void remove(Relationship relationship) throws RelationshipStorageException;

  /**
   * Marks a relationship as ignored.
   *
   * @param relationship the relationship
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #ignore(Identity, Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void ignore(Relationship relationship) throws RelationshipStorageException;

  /**
   * Returns all the pending relationship: sent.
   *
   * @param identity the identity
   * @return the pending
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getIncoming(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getPendingRelationships(Identity identity) throws RelationshipStorageException;

  /**
   * If toConfirm is true, it returns list of pending relationship received not confirmed if toConfirm is false, it
   * returns list of relationship sent not confirmed yet.
   *
   * @param identity  the identity
   * @param toConfirm the to confirm
   * @return the pending
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated When toConfirm=true, use {@link #getIncoming(Identity)} instead.
   *             When toConfirm=false, use {@link #getOutgoing(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getPendingRelationships(Identity identity, boolean toConfirm) throws RelationshipStorageException;

  /**
   * Gets pending relations in 2 case:
   * - if toConfirm is true, it returns list of pending relationship received not
   * confirmed
   * - if toConfirm is false, it returns list of relationship sent not confirmed yet.
   *
   * @param currIdentity the curr identity
   * @param identities   the identities
   * @param toConfirm    the to confirm
   * @return the pending
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated When toConfirm=true, use {@link #getIncoming(Identity)} instead.
   *             When toConfirm=false use {@link #getOutgoing(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getPendingRelationships(Identity currIdentity, List<Identity> identities,
                                             boolean toConfirm) throws RelationshipStorageException;

  /**
   * Gets contacts that match the search result.
   *
   * @param currIdentity the curr identity
   * @param identities the identities
   * @return the contacts
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getConnections(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getContacts(Identity currIdentity, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Gets the contacts.
   *
   * @param identity the identity
   * @return the contacts
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getConnections(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getContacts(Identity identity) throws RelationshipStorageException;

  /**
   * Returns all the relationships associated with a given identity.
   *
   * @param identity the identity
   * @return the list
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAllWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getAllRelationships(Identity identity) throws RelationshipStorageException;

  /**
   * Returns all the relationship associated with a given identityId.
   *
   * @param id the id
   * @return the by identity id
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAllWithListAccess(Identity)} with identity instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getRelationshipsByIdentityId(String id) throws RelationshipStorageException;

  /**
   * Returns all the identity associated with a given identity
   *
   * @param id the id
   * @return the identities
   * @throws Exception the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAllWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Identity> getIdentities(Identity id) throws Exception;

  /**
   * Creates a relationship.
   *
   * @param sender the sender
   * @param receiver the receiver
   * @return the relationship
   * @LevelAPI Provisional
   * @deprecated Use {@link #invite(Identity, Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  Relationship create(Identity sender, Identity receiver);

  /**
   * Saves a relationship.
   *
   * @param relationship the rel
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #update(Relationship)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void saveRelationship(Relationship relationship) throws RelationshipStorageException;

  /**
   * Finds route.
   *
   * @param sender the id1
   * @param receiver the id2
   * @return the list
   * @throws RelationshipStorageException
   * @LevelAPI Provisional
   * @deprecated Should use {@link #get(Identity, Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List findRoute(Identity sender, Identity receiver) throws RelationshipStorageException;

  /**
   * Gets the relationship.
   *
   * @param sender the id1
   * @param receiver the id2
   * @return the relationship
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #get(Identity, Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  Relationship getRelationship(Identity sender, Identity receiver) throws RelationshipStorageException;

  /**
   * Finds any identity having relationshipType with the ownerIdentity.
   * @param ownerIdentity
   * @param relationshipType
   * @return list of identites
   * @throws RelationshipStorageException
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getIncoming(Identity)} or {@link #getOutgoing(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Identity> findRelationships(Identity ownerIdentity, Relationship.Type relationshipType)
                                   throws RelationshipStorageException;

  /**
   * Gets the relationship status.
   *
   * @param rel the rel
   * @param id the id
   * @return the relationship status
   * @LevelAPI Provisional
   * @deprecated Now we don't use this method to get relationship we get status of relationship object
   * and depend on sender and receiver, we can define pending relationship or incoming relationship.
   * But we still keep this method for build, call {@link #getStatus(Identity, Identity)}.
   *            This method will be removed by 4.0.x
   */
  @Deprecated
  Relationship.Type getRelationshipStatus(Relationship rel, Identity id);

  /**
   * Gets connection status.
   *
   * @param fromIdentity
   * @param toIdentity
   * @return relationshipType
   * @throws Exception
   * @LevelAPI Provisional
   * @since 1.1.1
   * @deprecated Use {@link #getStatus(Identity, Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  Relationship.Type getConnectionStatus(Identity fromIdentity, Identity toIdentity) throws Exception;

  /**
   * Gets all the pending relationship of sender.
   *
   * @param sender the sender
   * @return the pending relationships
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getIncoming(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getPending(Identity sender) throws RelationshipStorageException;

  /**
   * Gets pending relationships of sender that match with identities.
   *
   * @param sender the sender
   * @param identities the identities
   * @return the pending relationships
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getIncomingWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getPending(Identity sender, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Gets list of required validation relationship of receiver
   *
   * @param receiver the receiver identity
   * @return the list of relationships
   * @throws RelationshipStorageException
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getIncomingWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getIncoming(Identity receiver) throws RelationshipStorageException;

  /**
   * Gets list of required validation relationship of receiver that match with
   * identities.
   *
   * @param receiver the receiver
   * @param identities the identities
   * @return the pending require validation relationships
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getIncomingWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getIncoming(Identity receiver, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Gets list of confirmed relationship of identity.
   *
   * @param identity the identity
   * @return the confirmed relationships
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getConnections(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getConfirmed(Identity identity) throws RelationshipStorageException;

  /**
   * Gets list of confirmed relationship of identity that match with identities.
   *
   * @param identity the identity
   * @param identities the identities
   * @return the list of confirmed relationship
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getConnections(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getConfirmed(Identity identity, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Returns all the relationship of a given identity with other identity.
   *
   * @param identity the identity
   * @return the list relationships
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getAllWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getAll(Identity identity) throws RelationshipStorageException;

  /**
   * Returns all the relationship of a given identity with other identity in identities.
   *
   * @param identity   the identity
   * @param identities the list identity the identities
   * @return the list relationships
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getAllWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getAll(Identity identity, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Returns all the relationship of a given identity with other identity in
   * identities.
   *
   * @param identity the identity
   * @param type the status
   * @param identities the list identity the identities
   * @return the list
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getAllWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Relationship> getAll(Identity identity, Relationship.Type type,
                            List<Identity> identities) throws RelationshipStorageException;
  
  
  /**
   * Gets the list access to get a list of identities who is connected with the provided identity
   * and filtered by profile filter.
   * 
   * @param existingIdentity the provided identity.
   * @param profileFilter the provided profile filter.
   * @LevelAPI Platform
   * @return the list access
   * @since 1.2.3
   * 
   */
  ListAccess<Identity> getConnectionsByFilter(Identity existingIdentity, ProfileFilter profileFilter);
  
  /**
   * Gets the list access to get a list of identities who invited to connect to the provided identity
   * and filtered by profile filter.
   *
   * @param existingIdentity the provided identity
   * @param profileFilter    the provided profile filter
   * @return the list access
   * @LevelAPI Platform
   * @since  1.2.3
   */
  ListAccess<Identity> getIncomingByFilter(Identity existingIdentity, ProfileFilter profileFilter);
  
  /**
   * Gets the list access to get a list of identities who was invited by the provided identity to connect
   * and filtered by profile filter.
   *
   * @param existingIdentity the provided identity
   * @param profileFilter    the provided profile filter
   * @return the list access
   * @LevelAPI Platform
   * @since  1.2.3
   */
  ListAccess<Identity> getOutgoingByFilter(Identity existingIdentity, ProfileFilter profileFilter);
  
  /**
   * Gets the suggestions with number of commons users relate to provided identity
   * @param identity the provided identity
   * @param offset the offset position to get
   * @param limit the limit of return result
   * @return the map of suggestion users and number of commons users
   * @since 4.0.x
   */
  public Map<Identity, Integer> getSuggestions(Identity identity, int offset, int limit);
  
}