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

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.RelationshipStorageException;

/**
 * The Interface RelationshipManager manages connectionType between identities.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 */
public interface RelationshipManager {
  /**
   * Gets relationship the by id.
   *
   * @param id the id
   * @return the by id
   * @throws Exception the exception
   * @deprecated Use {@link #get(String)} instead. Will be removed at 1.2.x
   */
  Relationship getRelationshipById(String id) throws Exception;


  /**
   * Creates a connection invitation between 2 identities.
   * 
   * @param sender inviter
   * @param receiver invitee
   * @return a PENDING relation
   * @throws Exception
   */
  Relationship invite(Identity sender, Identity receiver) throws RelationshipStorageException;

  /**
   * Saves a relationship.
   * 
   * @param relationship the relationship
   * @throws Exception the exception
   */
  void save(Relationship relationship) throws RelationshipStorageException;

  /**
   * Marks a relationship as confirmed.
   * 
   * @param relationship the relationship
   * @throws Exception the exception
   */
  void confirm(Relationship relationship) throws RelationshipStorageException;

  /**
   * Denies a relationship.
   * 
   * @param relationship
   * @throws Exception
   */
  void deny(Relationship relationship) throws RelationshipStorageException;

  /**
   * Remove a relationship.
   * 
   * @param relationship the relationship
   * @throws Exception the exception
   */
  void remove(Relationship relationship) throws RelationshipStorageException;

  /**
   * Marks a relationship as ignored.
   * 
   * @param relationship the relationship
   * @throws Exception the exception
   */
  void ignore(Relationship relationship) throws RelationshipStorageException;

  /**
   * Returns all the pending relationship: sent and received.
   *
   * @param identity the identity
   * @return the pending
   * @throws Exception the exception
   * @deprecated Use {@link #getPending(Identity)} instead. Will be removed at 1.2.x
   */
  List<Relationship> getPendingRelationships(Identity identity) throws Exception;

  /**
   * If toConfirm is true, it returns list of pending relationship received not
   * confirmed if toConfirm is false, it returns list of relationship sent not
   * confirmed yet.
   *
   * @param identity the identity
   * @param toConfirm the to confirm
   * @return the pending
   * @throws Exception the exception
   * @deprecated Use {@link #getAll(Identity, org.exoplatform.social.core.relationship.model.Relationship.Type, List)} instead
   *  When toConfirm=true use above method with Type=PENDING and List=null 
   *  When toConfirm=false use {@link #getPending(Identity)}}. 
   *  This method will be removed at 1.2.x
   */
  List<Relationship> getPendingRelationships(Identity identity, boolean toConfirm) throws Exception;

  /**
   * Gets pending relations in 2 case: - if toConfirm is true, it returns list of
   * pending relationship received not confirmed - if toConfirm is false, it
   * returns list of relationship sent not confirmed yet.
   *
   * @param currIdentity the curr identity
   * @param identities the identities
   * @param toConfirm the to confirm
   * @return the pending
   * @throws Exception the exception
   * @deprecated Use {@link #getAll(Identity, org.exoplatform.social.core.relationship.model.Relationship.Type, List)} instead
   *  When toConfirm=true use above method with Type=PENDING 
   *  When toConfirm=false use {@link #getPending(Identity, List)}}. 
   *  This method will be removed at 1.2.x
   */
  List<Relationship> getPendingRelationships(Identity currIdentity, List<Identity> identities,
                                             boolean toConfirm) throws Exception;

  /**
   * Gets contacts that match the search result.
   *
   * @param currIdentity the curr identity
   * @param identities the identities
   * @return the contacts
   * @throws Exception the exception
   * @deprecated Use {@link #getConfirmed(Identity, List)} instead. Will be removed at 1.2.x
   */
  List<Relationship> getContacts(Identity currIdentity, List<Identity> identities) throws Exception;

  /**
   * Gets the contacts.
   *
   * @param identity the identity
   * @return the contacts
   * @throws Exception the exception
   * @deprecated Use {@link #getConfirmed(Identity)} instead. Will be removed at 1.2.x
   */
  List<Relationship> getContacts(Identity identity) throws Exception;

  /**
   * Returns all the relationship associated with a given identity.
   *
   * @param identity the identity
   * @return the list
   * @throws Exception the exception
   * @deprecated Use {@link #getAll(Identity)} instead. Will be removed at 1.2.x
   */
  List<Relationship> getAllRelationships(Identity identity) throws Exception;

  /**
   * Returns all the relationship associated with a given identityId.
   *
   * @param id the id
   * @return the by identity id
   * @throws Exception the exception
   * @deprecated Use {@link #getAll(Identity)} with identity instead. Will be removed at 1.2.x
   */
  List<Relationship> getRelationshipsByIdentityId(String id) throws Exception;

  /**
   * Returns all the identity associated with a given identity
   *
   * @param id the id
   * @return the identities
   * @throws Exception the exception
   */
  List<Identity> getIdentities(Identity id) throws Exception;

  /**
   * Creates a relationship.
   *
   * @param sender the sender
   * @param receiver the receiver
   * @return the relationship
   * @deprecated Use {@link #invite(Identity, Identity)} instead. Will be removed at 1.2.x
   */
  Relationship create(Identity sender, Identity receiver);

  /**
   * Saves a relationship.
   *
   * @param relationship the rel
   * @throws Exception the exception
   * @deprecated Use {@link #save(Relationship)} instead. Will be removed at 1.2.x
   */
  void saveRelationship(Relationship relationship) throws Exception;

  /**
   * Finds route.
   *
   * @param sender the id1
   * @param receiver the id2
   * @return the list
   * @deprecated Should use {@link #get(Identity, Identity)} instead. Will be removed at 1.2.x
   */
  List findRoute(Identity sender, Identity receiver) throws Exception;

  /**
   * Gets the relationship.
   *
   * @param sender the id1
   * @param receiver the id2
   * @return the relationship
   * @throws Exception the exception
   * @deprecated Use {@link #get(Identity, Identity)} instead. Will be removed at 1.2.x
   */
  Relationship getRelationship(Identity sender, Identity receiver) throws Exception;

  /**
   * Finds any identity having relationshipType with the ownerIdentity.
   * @param ownerIdentity
   * @param relationshipType
   * @return list of identites
   * @throws Exception
   * @since 1.2.0-GA
   */
  List<Identity> findRelationships(Identity ownerIdentity, Relationship.Type relationshipType) throws Exception;

  /**
   * Gets the relationship status.
   *
   * @param rel the rel
   * @param id the id
   * @return the relationship status
   * @deprecated Now we don't use this method to get relationship we get status of relationship object
   * and depend on sender and receiver, we can define pending relationship or incoming relationship.
   * But we still keep this method for build, call {@link #getStatus(Identity, Identity)}. This method will be removed at 1.2.x
   */
  Relationship.Type getRelationshipStatus(Relationship rel, Identity id);

  /**
   * Gets connection status.
   *
   * @param fromIdentity
   * @param toIdentity
   * @return relationshipType
   * @throws Exception
   * @deprecated Use {@link #getStatus(Identity, Identity)} instead. Will be removed at 1.2.x
   * @since 1.1.1
   */
  Relationship.Type getConnectionStatus(Identity fromIdentity, Identity toIdentity) throws Exception;

  /**
   * Gets relationship the by id.
   * 
   * @param relationshipId the relationshipId
   * @return the relationship
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  Relationship get(String relationshipId);

  /**
   * Gets all the pending relationship of sender
   * 
   * @param identity the sender
   * @return the pending relationships
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  List<Relationship> getPending(Identity sender) throws RelationshipStorageException;

  /**
   * Gets pending relationships of sender that match with identities.
   * 
   * @param identity the sender
   * @param identities the identities
   * @return the pending relationships
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  List<Relationship> getPending(Identity sender, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Gets list of required validation relationship of receiver
   * 
   * @param identity the receiver
   * @return the pending
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  List<Relationship> getIncoming(Identity receiver) throws RelationshipStorageException;

  /**
   * Gets list of required validation relationship of receiver that match with
   * identities.
   * 
   * @param identity the receiver
   * @param identities the identities
   * @return the pending require validation relationships
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  List<Relationship> getIncoming(Identity receiver, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Gets list of confirmed relationship of identity.
   * 
   * @param identity the identity
   * @return the confirmed relationships
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  List<Relationship> getConfirmed(Identity identity) throws RelationshipStorageException;

  /**
   * Gets list of confirmed relationship of identity that match with identities.
   * 
   * @param identity the identity
   * @param identities the identities
   * @return the list of confirmed relationship
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  List<Relationship> getConfirmed(Identity identity, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Returns all the relationship of a given identity with other identity.
   * 
   * @param identity the identity
   * @return the list relationships
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  List<Relationship> getAll(Identity identity) throws RelationshipStorageException;

  /**
   * Returns all the relationship of a given identity with other identity in
   * identities.
   * 
   * @param identity the identity
   * @param the list identity the identities
   * @return the list relationships
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  List<Relationship> getAll(Identity identity, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Returns all the relationship of a given identity with other identity in
   * identities.
   * 
   * @param identity the identity
   * @param type the status
   * @param the list identity the identities
   * @return the list
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  List<Relationship> getAll(Identity identity, Relationship.Type type, List<Identity> identities) throws RelationshipStorageException;

  /**
   * Gets the relationship.
   * 
   * @param identity the identity1
   * @param identity the identity2
   * @return the relationship
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  Relationship get(Identity identity1, Identity identity2) throws RelationshipStorageException;

  /**
   * Gets the relationship status.
   * 
   * @param identity the identity1
   * @param identity the identity2
   * @return the relationship
   * @throws Exception the exception
   * @since 1.2.0-GA
   */
  Relationship.Type getStatus(Identity identity1, Identity identity2) throws RelationshipStorageException;
}