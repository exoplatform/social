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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.RelationshipStorageException;

import java.util.List;
import java.util.Map;

/**
 * Controls connections between identities.
 * This includes:
 * <ul>
 *   <li>Getting relationship between 2 identities.</li>
 *   <li>Interacting between identities: inviting, confirming, denying and ignoring.</li>
 *   <li>Getting a list access which contains connections, incoming and outgoing.</li>
 * </ul>
 */
public interface RelationshipManager {
  /**
   * Gets a relationship by its Id.
   *
   * @param relationshipId Id of the relationship.
   * @return The existing relationship or null.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  Relationship get(String relationshipId);

  /**
   * Gets a relationship between 2 identities.
   *
   * @param identity1 The identity 1.
   * @param identity2 The identity 2.
   * @return The relationship between 2 identities.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  Relationship get(Identity identity1, Identity identity2);

  /**
   * Updates an existing relationship.
   *
   * @param existingRelationship The existing relationship to be updated.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void update(Relationship existingRelationship);

  /**
   * Deletes an existing relationship.
   *
   * @param existingRelationship The existing relationship to be deleted.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void delete(Relationship existingRelationship);

  /**
   * Invites one identity to connect to the other. The first argument must be the sender identity. The
   * second argument must be the identity who is invited to connect.
   * <p/>
   * One identity is not allowed to invite himself to connect.
   *
   * @param invitingIdentity The sender identity.
   * @param invitedIdentity The identity who is invited.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Relationship inviteToConnect(Identity invitingIdentity, Identity invitedIdentity);

  /**
   * Confirms to connect to an identity who sent invitation.
   *
   * @param invitedIdentity The identity who gets invitation.
   * @param invitingIdentity The identity who invites another.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void confirm(Identity invitedIdentity, Identity invitingIdentity);

  /**
   * Denies to connect to an identity who sent invitation.
   * 
   * @param invitedIdentity The identity who gets invitation.
   * @param invitingIdentity The identity who invites another.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void deny(Identity invitedIdentity, Identity invitingIdentity);

  /**
   * Ignores to connect to an identity who sent invitation.
   * Once being ignored, the inviting identity cannot invite any more.
   *
   * @param invitedIdentity The identity who gets invitation.
   * @param invitingIdentity The identity who invites another.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void ignore(Identity invitedIdentity, Identity invitingIdentity);

  /**
   * Gets a list access which contains all identities who are connected to a provided identity.
   *
   * @param existingIdentity The existing provided identity.
   * @return The list access.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  ListAccess<Identity> getConnections(Identity existingIdentity);

  /**
   * Gets a list access which contains all identities who invited to connect to a provided identity.
   *
   * @param existingIdentity The existing provided identity.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Identity> getIncomingWithListAccess(Identity existingIdentity);

  /**
   * Gets a list access which contains all identities who were invited to connect by a provided identity.
   *
   * @param existingIdentity The existing provided identity.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Identity> getOutgoing(Identity existingIdentity);

  /**
   * Gets a list access which contains all identities who were connected, were invited or invited a provided identity.
   *
   * @param existingIdentity The existing provided identity.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Identity> getAllWithListAccess(Identity existingIdentity);

  /**
   * Gets the relationship status between 2 identities.
   *
   * @param identity1 The identity 1.
   * @param identity2 The identity 2.
   * @return The relationship type.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Relationship.Type getStatus(Identity identity1, Identity identity2);

  /**
   * Creates a connection invitation between 2 identities.
   *
   * @param sender The inviter.
   * @param receiver The invitee.
   * @return The PENDING relation.
   * @throws RelationshipStorageException
   * @LevelAPI Provisional
   * @deprecated Use {@link #inviteToConnect(Identity, Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  Relationship invite(Identity sender, Identity receiver) throws RelationshipStorageException;

  /**
   * Gets a relationship by a given Id.
   *
   * @param id The given Id.
   * @return The relationship.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #get(String)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  Relationship getRelationshipById(String id) throws RelationshipStorageException;

  /**
   * Saves a relationship.
   *
   * @param relationship The relationship to be saved.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use actions (inviteToConnect, confirm) instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void save(Relationship relationship) throws RelationshipStorageException;

  /**
   * Marks a relationship as "confirmed".
   *
   * @param relationship The relationship to be marked as "confirmed".
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #confirm(Identity, Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void confirm(Relationship relationship) throws RelationshipStorageException;

  /**
   * Denies a relationship.
   *
   * @param relationship The relationship to be denied.
   * @throws RelationshipStorageException
   * @LevelAPI Provisional
   * @deprecated Use {@link #deny(Identity, Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void deny(Relationship relationship) throws RelationshipStorageException;

  /**
   * Removes a relationship.
   *
   * @param relationship The relationship to be removed.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #delete(Relationship)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void remove(Relationship relationship) throws RelationshipStorageException;

  /**
   * Marks a relationship as "ignored".
   *
   * @param relationship The relationship to be marked as "ignored".
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #ignore(Identity, Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void ignore(Relationship relationship) throws RelationshipStorageException;

  /**
   * Returns all pending relationships: sent.
   *
   * @param identity The identity.
   * @return The pending relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getIncoming(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getPendingRelationships(Identity identity) throws RelationshipStorageException;

  /**
   * Gets a list of pending relationships that were received but not confirmed or were sent but not confirmed.
   *
   * @param identity The identity.
   * @param toConfirm If "true", it returns a list of pending relationships received but not confirmed. If "false", it
   * returns a list of pending relationships sent but not confirmed.
   * @return The list of pending relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated When toConfirm=true, use {@link #getIncoming(Identity)} instead.
   *             When toConfirm=false, use {@link #getOutgoing(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getPendingRelationships(Identity identity, boolean toConfirm) throws RelationshipStorageException;

  /**
   *Gets a list of pending relationships that were received but not confirmed or were sent but not confirmed.
   *
   * @param currIdentity The current identity.
   * @param identities  The identities.
   * @param toConfirm  If "true", it returns a list of pending relationships received but not confirmed. If "false", it
   * returns a list of pending relationships sent but not confirmed.
   * @return The list of pending relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated When toConfirm=true, use {@link #getIncoming(Identity)} instead.
   *             When toConfirm=false use {@link #getOutgoing(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getPendingRelationships(Identity currIdentity, List<Identity> identities,
                                             boolean toConfirm) throws RelationshipStorageException;

  /**
   * Gets contacts matching with the current identity.
   *
   * @param currIdentity The current identity.
   * @param identities The identities.
   * @return The contacts.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getConnections(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getContacts(Identity currIdentity, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Gets contacts of an identity.
   *
   * @param identity The identity.
   * @return The contacts.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getConnections(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getContacts(Identity identity) throws RelationshipStorageException;

  /**
   * Returns all relationships associated with a given identity.
   *
   * @param identity The identity.
   * @return The relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAllWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getAllRelationships(Identity identity) throws RelationshipStorageException;

  /**
   * Returns all relationships associated with a given identityId.
   *
   * @param id Id of the identity.
   * @return The relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAllWithListAccess(Identity)} with identity instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getRelationshipsByIdentityId(String id) throws RelationshipStorageException;

  /**
   * Returns all identities associated with a given identity.
   *
   * @param id The given identity.
   * @return The identities.
   * @throws Exception the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAllWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Identity> getIdentities(Identity id) throws Exception;

  /**
   * Creates a relationship.
   *
   * @param sender The sender.
   * @param receiver The receiver.
   * @return The relationship.
   * @LevelAPI Provisional
   * @deprecated Use {@link #invite(Identity, Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  Relationship create(Identity sender, Identity receiver);

  /**
   * Saves a relationship.
   *
   * @param relationship The relationship to be saved.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #update(Relationship)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  void saveRelationship(Relationship relationship) throws RelationshipStorageException;

  /**
   * Gets a relationship between 2 identities.
   *
   * @param sender The identity 1.
   * @param receiver The identity 2.
   * @return The relationship.
   * @throws RelationshipStorageException
   * @LevelAPI Provisional
   * @deprecated Should use {@link #get(Identity, Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List findRoute(Identity sender, Identity receiver) throws RelationshipStorageException;

  /**
   * Gets a relationship between 2 identities.
   *
   * @param sender The identity 1.
   * @param receiver The identity 2.
   * @return The relationship.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #get(Identity, Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  Relationship getRelationship(Identity sender, Identity receiver) throws RelationshipStorageException;

  /**
   * Finds all identities having relationshipType with the ownerIdentity.
   * @param ownerIdentity The owner identity.
   * @param relationshipType The relationship type.
   * @return The identities.
   * @throws RelationshipStorageException
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getIncoming(Identity)} or {@link #getOutgoing(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Identity> findRelationships(Identity ownerIdentity, Relationship.Type relationshipType)
                                   throws RelationshipStorageException;

  /**
   * Gets the relationship status.
   *
   * @param rel The relationship.
   * @param id The identity.
   * @return The relationship status.
   * @LevelAPI Provisional
   * @deprecated Now we don't use this method to get relationship we get status of relationship object
   * and depend on sender and receiver, we can define pending relationship or incoming relationship.
   * But we still keep this method for build, call {@link #getStatus(Identity, Identity)}.
   *            This method will be removed by 4.0.x.
   */
  @Deprecated
  Relationship.Type getRelationshipStatus(Relationship rel, Identity id);

  /**
   * Gets the connection status between 2 identities.
   * 
   * @param fromIdentity The identity 1.
   * @param toIdentity The identity 2.
   * @return The connection status.
   * @throws Exception
   * @LevelAPI Provisional
   * @since 1.1.1
   * @deprecated Use {@link #getStatus(Identity, Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  Relationship.Type getConnectionStatus(Identity fromIdentity, Identity toIdentity) throws Exception;

  /**
   * Gets all pending relationships of a sender.
   *
   * @param sender The sender.
   * @return The pending relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getIncoming(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getPending(Identity sender) throws RelationshipStorageException;

  /**
   * Gets pending relationships of a sender that match with identities.
   *
   * @param sender The sender.
   * @param identities The identities.
   * @return The pending relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getIncomingWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getPending(Identity sender, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Gets relationships of a receiver that are waiting for validation.
   *
   * @param receiver Identity of the receiver.
   * @return The relationships.
   * @throws RelationshipStorageException
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getIncomingWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getIncoming(Identity receiver) throws RelationshipStorageException;

  /**
   * Gets relationships of a receiver that are waiting for validation and match with identities.
   *
   * @param receiver Identity of the receiver.
   * @param identities The identities.
   * @return The list of relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getIncomingWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getIncoming(Identity receiver, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Gets relationships confirmed by an identity.
   *
   * @param identity The identity.
   * @return The confirmed relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getConnections(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getConfirmed(Identity identity) throws RelationshipStorageException;

  /**
   * Gets relationships confirmed by an identity that match with identities.
   *
   * @param identity The identity.
   * @param identities The identities.
   * @return The confirmed relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getConnections(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getConfirmed(Identity identity, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Gets all relationships of a given identity with other identity.
   *
   * @param identity The identity.
   * @return The relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getAllWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getAll(Identity identity) throws RelationshipStorageException;

  /**
   * Gets all relationships of a given identity.
   *
   * @param identity The identity.
   * @param identities The identities.
   * @return The relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getAllWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getAll(Identity identity, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Gets all relationships of a given identity.
   *
   * @param identity The identity.
   * @param type The status.
   * @param identities The identities.
   * @return The relationships.
   * @throws RelationshipStorageException the exception
   * @LevelAPI Provisional
   * @since 1.2.0-Beta1
   * @deprecated Use {@link #getAllWithListAccess(Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<Relationship> getAll(Identity identity, Relationship.Type type,
                            List<Identity> identities) throws RelationshipStorageException;
  
  
  /**
   * Gets a list access which contains all identities who are connected to a provided identity
   * and filtered by a profile filter.
   * 
   * @param existingIdentity The provided identity.
   * @param profileFilter The provided profile filter.
   * @LevelAPI Platform
   * @return The list access.
   * @since 1.2.3
   * 
   */
  ListAccess<Identity> getConnectionsByFilter(Identity existingIdentity, ProfileFilter profileFilter);
  
  /**
   * Gets a list access which contains all identities who invited to connect to a provided identity
   * and filtered by a profile filter.
   *
   * @param existingIdentity The provided identity.
   * @param profileFilter The provided profile filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.3
   */
  ListAccess<Identity> getIncomingByFilter(Identity existingIdentity, ProfileFilter profileFilter);
  
  /**
   * Gets a list access which contains all identities who were invited by a provided identity to connect
   * and filtered by a profile filter.
   *
   * @param existingIdentity The provided identity.
   * @param profileFilter The provided profile filter.
   * @return The list access.
   * @LevelAPI Platform
   * @since  1.2.3
   */
  ListAccess<Identity> getOutgoingByFilter(Identity existingIdentity, ProfileFilter profileFilter);
  
  /**
   * Gets suggestions having common users with the provided identity.
   * @param identity The provided identity.
   * @param maxConnections Maximum of connections that we can treat per identity. If set
   * to a value <= 0, the limit will be disabled
   * @param maxConnectionsToLoad In case, the maxConnections are not enough to find enough suggestion, 
   * we load more connections at the first level. If maxConnectionsToLoad or maxConnections has been 
   * set to a value <= 0, the limit will be disabled
   * @param maxSuggestions The total amount of expected suggestions. If set to a value <= 0, the limit 
   * will be disabled
   * @return The suggestions.
   * @LevelAPI Experimental
   * @since 4.0.6
   */
  public Map<Identity, Integer> getSuggestions(Identity identity, int maxConnections, 
                                               int maxConnectionsToLoad, 
                                               int maxSuggestions);

  /**
   * Gets suggestions having common users with the provided identity.
   * @param identity The provided identity.
   * @param offset The starting point from which suggestions are got.
   * @param limit The limitation of suggestions.
   * @return The suggestions.
   * @LevelAPI Experimental
   * @since 4.0.2
   * @deprecated Use {@link #getSuggestions(org.exoplatform.social.core.identity.model.Identity, int, int, int)} instead
   */
  @Deprecated
  public Map<Identity, Integer> getSuggestions(Identity identity, int offset, int limit);
  
  /**
   * Get the list of identities who are most recently connected with given user
   * the limit number of results must be greater than 0 or an empty list will be returned   
   * 
   * @param identity
   * @param limit 
   * @return
   */
  List<Identity> getLastConnections(Identity identity, int limit);
}