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

import javax.resource.NotSupportedException;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;

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
   */
  Relationship getRelationshipById(String id) throws Exception;

  /**
   * Creates a connection invitation between 2 identities.
   *
   * @param currIdentity inviter
   * @param requestedIdentity invitee
   * @return a PENDING relation
   * @throws Exception
   */
  Relationship invite(Identity currIdentity, Identity requestedIdentity) throws Exception;

  /**
   * Marks a relationship as confirmed.
   *
   * @param relationship the relationship
   * @throws Exception the exception
   */
  void confirm(Relationship relationship) throws Exception;

  /**
   * Denies a relationship.
   *
   * @param relationship
   * @throws Exception
   */
  void deny(Relationship relationship) throws Exception;

  /**
   * Remove a relationship.
   *
   * @param relationship the relationship
   * @throws Exception the exception
   */
  void remove(Relationship relationship) throws Exception;

  /**
   * Marks a relationship as ignored.
   *
   * @param relationship the relationship
   * @throws Exception the exception
   */
  void ignore(Relationship relationship) throws Exception;

  /**
   * Returns all the pending relationship: sent and received.
   *
   * @param identity the identity
   * @return the pending
   * @throws Exception the exception
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
   */
  List<Relationship> getPendingRelationships(Identity currIdentity,
                                             List<Identity> identities,
                                             boolean toConfirm) throws Exception;

  /**
   * Gets contacts that match the search result.
   *
   * @param currIdentity the curr identity
   * @param identities the identities
   * @return the contacts
   * @throws Exception the exception
   */
  List<Relationship> getContacts(Identity currIdentity, List<Identity> identities) throws Exception;

  /**
   * Gets the contacts.
   *
   * @param identity the identity
   * @return the contacts
   * @throws Exception the exception
   */
  List<Relationship> getContacts(Identity identity) throws Exception;

  /**
   * Returns all the relationship associated with a given identity.
   *
   * @param identity the identity
   * @return the list
   * @throws Exception the exception
   */
  List<Relationship> getAllRelationships(Identity identity) throws Exception;

  /**
   * Returns all the relationship associated with a given identityId.
   *
   * @param id the id
   * @return the by identity id
   * @throws Exception the exception
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
   */
  Relationship create(Identity sender, Identity receiver);

  /**
   * Saves a relationship.
   *
   * @param relationship the rel
   * @throws Exception the exception
   */
  void saveRelationship(Relationship relationship) throws Exception;

  /**
   * Finds route.
   *
   * @param sender the id1
   * @param receiver the id2
   * @return the list
   */
  List findRoute(Identity sender, Identity receiver) throws NotSupportedException;

  /**
   * Gets the relationship.
   *
   * @param sender the id1
   * @param receiver the id2
   * @return the relationship
   * @throws Exception the exception
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
   */
  Relationship.Type getRelationshipStatus(Relationship rel, Identity id);

  /**
   * Gets connection status.
   *
   * @param fromIdentity
   * @param toIdentity
   * @return relationshipType
   * @throws Exception
   * @since 1.1.1
   */
  Relationship.Type getConnectionStatus(Identity fromIdentity, Identity toIdentity) throws Exception;
}