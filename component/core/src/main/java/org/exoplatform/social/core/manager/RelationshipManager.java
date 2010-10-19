/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import java.util.ArrayList;
import java.util.List;

import javax.resource.NotSupportedException;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.RelationshipLifeCycle;
import org.exoplatform.social.core.relationship.RelationshipListener;
import org.exoplatform.social.core.relationship.RelationshipListenerPlugin;
import org.exoplatform.social.core.relationship.model.Property;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.RelationshipStorage;

/**
 * The Class RelationshipManager to manage connectionType between identities.
 */
public class RelationshipManager {

  /** The storage. */
  private RelationshipStorage storage;

  /**
   * lifecycle of a relationship
   */
  private RelationshipLifeCycle lifeCycle = new RelationshipLifeCycle();

  /**
   * Instantiates a new relationship manager.
   * @param relationshipStorage
   */
  public RelationshipManager(RelationshipStorage relationshipStorage) {
    this.storage = relationshipStorage;
  }

  /**
   * Gets relationship the by id.
   *
   * @param id the id
   * @return the by id
   * @throws Exception the exception
   */
  public Relationship getRelationshipById(String id) throws Exception {
    return storage.getRelationship(id);
  }

  /**
   * Creates a connection invitation between 2 identities
   * @param currIdentity inviter
   * @param requestedIdentity invitee
   * @return a PENDING relation
   * @throws Exception
   */
  public Relationship invite(Identity currIdentity, Identity requestedIdentity) throws Exception {
    Relationship relationship = create(currIdentity, requestedIdentity);
    relationship.setStatus(Relationship.Type.PENDING);
    saveRelationship(relationship);
    lifeCycle.relationshipRequested(this, relationship);
    return relationship;
  }

  /**
   * mark a relationship as confirmed.
   *
   * @param relationship the relationship
   * @throws Exception the exception
   */
  public void confirm(Relationship relationship) throws Exception {
    relationship.setStatus(Relationship.Type.CONFIRM);
    for (Property prop : relationship.getProperties()) {
      prop.setStatus(Relationship.Type.CONFIRM);
    }
    saveRelationship(relationship);
    lifeCycle.relationshipConfirmed(this, relationship);
  }

  /**
   *
   * @param relationship
   * @throws Exception
   */
  public void deny(Relationship relationship) throws Exception {
    storage.removeRelationship(relationship);
    lifeCycle.relationshipDenied(this, relationship);
  }

  /**
   * remove a relationship.
   *
   * @param relationship the relationship
   * @throws Exception the exception
   */
  public void remove(Relationship relationship) throws Exception {
    storage.removeRelationship(relationship);
    lifeCycle.relationshipRemoved(this, relationship);
  }

  /**
   * mark a relationship as ignored.
   *
   * @param relationship the relationship
   * @throws Exception the exception
   */
  public void ignore(Relationship relationship) throws Exception {
    relationship.setStatus(Relationship.Type.IGNORE);
    for (Property prop : relationship.getProperties()) {
      prop.setStatus(Relationship.Type.IGNORE);
    }
    saveRelationship(relationship);
    lifeCycle.relationshipIgnored(this, relationship);
  }

  /**
   * return all the pending relationship: sent and received.
   *
   * @param identity the identity
   * @return the pending
   * @throws Exception the exception
   */
  public List<Relationship> getPendingRelationships(Identity identity) throws Exception {
    List<Relationship> relationships = getAllRelationships(identity);
    List<Relationship> pendingRel = new ArrayList<Relationship>();
    for (Relationship rel : relationships) {
      if (rel.getStatus() == Relationship.Type.PENDING) {
        pendingRel.add(rel);
      } else {
        if (rel.getProperties(Relationship.Type.PENDING).size() > 0)
          pendingRel.add(rel);
      }
    }
    return pendingRel;
  }

  /**
   * if toConfirm is true, it return list of pending relationship received not
   * confirmed if toConfirm is false, it return list of relationship sent not
   * confirmed yet.
   *
   * @param identity the identity
   * @param toConfirm the to confirm
   * @return the pending
   * @throws Exception the exception
   */
  public List<Relationship> getPendingRelationships(Identity identity, boolean toConfirm) throws Exception {
    List<Relationship> rels = getAllRelationships(identity);
    List<Relationship> pendingRel = new ArrayList<Relationship>();
    if(toConfirm) {
     for(Relationship rel : rels) {
       if(getRelationshipStatus(rel, identity).equals(Relationship.Type.PENDING))
         pendingRel.add(rel);
     }
     return pendingRel;
    }
    for (Relationship relationship : rels) {
      if(getRelationshipStatus(relationship, identity).equals(Relationship.Type.REQUIRE_VALIDATION))
        pendingRel.add(relationship);
    }
    return pendingRel;
  }

  /**
   * Get pending relations in 2 case:
   * - if toConfirm is true, it return list of pending relationship received not confirmed
   * - if toConfirm is false, it return list of relationship sent not confirmed yet.
   *
   * @param currIdentity the curr identity
   * @param identities the identities
   * @param toConfirm the to confirm
   * @return the pending
   * @throws Exception the exception
   */
  public List<Relationship> getPendingRelationships(Identity currIdentity, List<Identity> identities, boolean toConfirm) throws Exception {
    List<Relationship> pendingRels = getPendingRelationships(currIdentity, true);
    List<Relationship> invitedRels = getPendingRelationships(currIdentity, false);
    List<Relationship> pendingRel = new ArrayList<Relationship>();
    if (toConfirm) {
      for (Identity id : identities) {
        for (Relationship rel : pendingRels) {
          if (rel.getReceiver().getRemoteId().equals(id.getRemoteId())) {
            pendingRel.add(rel);
            break;
          }
        }
      }
      return pendingRel;
    }

    for (Identity id : identities) {
      for (Relationship rel : invitedRels) {
        if (rel.getSender().getRemoteId().equals(id.getRemoteId())) {
          pendingRel.add(rel);
          break;
        }
      }
    }

    return pendingRel;
  }

  /**
   * Get contacts that match the search result.
   *
   * @param currIdentity the curr identity
   * @param identities the identities
   * @return the contacts
   * @throws Exception the exception
   */
  public List<Relationship> getContacts(Identity currIdentity, List<Identity> identities) throws Exception {
    List<Relationship> contacts = getContacts(currIdentity);
    List<Relationship> relations = new ArrayList<Relationship>();
    Identity identityRel;
    for (Identity id : identities) {
      for (Relationship contact : contacts) {
        final Identity identity = contact.getSender();
        identityRel = identity.getRemoteId().equals(currIdentity.getRemoteId())
                      ? contact.getReceiver()
                      : contact.getSender();
        if (identityRel.getRemoteId().equals(id.getRemoteId())) {
          relations.add(contact);
          break;
        }
      }
    }
    return relations;
  }

  /**
   * Gets the contacts.
   *
   * @param identity the identity
   * @return the contacts
   * @throws Exception the exception
   */
  public List<Relationship> getContacts(Identity identity) throws Exception {
    List<Relationship> rels = getAllRelationships(identity);
    if(rels == null) return null;
    List<Relationship> contacts = new ArrayList<Relationship>();
    for (Relationship rel : rels) {
      if (rel.getStatus() == Relationship.Type.CONFIRM) {
        contacts.add(rel);
      }
    }
    return contacts;
  }

  /**
   * return all the relationship associated with a given identity.
   *
   * @param identity the identity
   * @return the list
   * @throws Exception the exception
   */
  public List<Relationship> getAllRelationships(Identity identity) throws Exception {
    return this.storage.getRelationshipByIdentity(identity);
  }

  /**
   * return all the relationship associated with a given identityId.
   *
   * @param id the id
   * @return the by identity id
   * @throws Exception the exception
   */
  public List<Relationship> getRelationshipsByIdentityId(String id) throws Exception {
    return this.storage.getRelationshipByIdentityId(id);
  }

  /**
   * return all the identity associated with a given identity TODO check if the
   * relation has been validated.
   *
   * @param id the id
   * @return the identities
   * @throws Exception the exception
   */
  public List<Identity> getIdentities(Identity id) throws Exception {
    return this.storage.getRelationshipIdentitiesByIdentity(id);
  }

  /**
   * Creates the.
   *
   * @param sender the sender
   * @param receiver the receiver
   * @return the relationship
   */
  public Relationship create(Identity sender, Identity receiver) {
    return new Relationship(sender, receiver);
  }

  /**
   * Save.
   *
   * @param relationship the rel
   * @throws Exception the exception
   */
  void saveRelationship(Relationship relationship) throws Exception {
    final Identity sender = relationship.getSender();
    final Identity receiver = relationship.getReceiver();
    final String senderId = sender.getId();
    final String receiverId = receiver.getId();

    if (senderId.equals(receiverId)){
      throw new Exception("the two identity are the same");
    }

    for (Property prop : relationship.getProperties()) {
      // if the initiator is not in the member of the relationship, we throw an exception
      final String initiatorId = prop.getInitiator().getId();
      if (!(initiatorId.equals(senderId) || initiatorId.equals(receiverId))) {
        throw new Exception("the property initiator is not member of the relationship");
      }
    }
    this.storage.saveRelationship(relationship);
  }

  /**
   * Find route.
   *
   * @param sender the id1
   * @param receiver the id2
   * @return the list
   */
  public List findRoute(Identity sender, Identity receiver) throws NotSupportedException {
    throw new NotSupportedException();
  }

  /**
   * Gets the relationship.
   *
   * @param sender the id1
   * @param receiver the id2
   * @return the relationship
   * @throws Exception the exception
   */
  public Relationship getRelationship(Identity sender, Identity receiver) throws Exception {
    List<Relationship> rels = getAllRelationships(sender);
    String sId2 = receiver.getId();
    for (Relationship rel : rels) {
      if (rel.getSender().getId().equals(sId2) || rel.getReceiver().getId().equals(sId2)) {
        return rel;
      }
    }
    return null;
  }

  /**
   * Gets the relationship status.
   *
   * @param rel the rel
   * @param id the id
   * @return the relationship status
   */
  public Relationship.Type getRelationshipStatus(Relationship rel, Identity id) {
    if (rel == null)
      return Relationship.Type.ALIEN;
    Identity identity1 = rel.getSender();
    if (rel.getStatus().equals(Relationship.Type.PENDING)) {
      if (identity1.getId().equals(id.getId()))
        return Relationship.Type.PENDING;
      else
        return Relationship.Type.REQUIRE_VALIDATION;
    } else if (rel.getStatus().equals(Relationship.Type.IGNORE)) {
      // TODO need to change in future
      return Relationship.Type.ALIEN;
    }
    return Relationship.Type.CONFIRM;
  }


  /**
   * Gets connection status
   * @param fromIdentity
   * @param toIdentity
   * @return relationshipType
   * @throws Exception
   * @since 1.1.1
   */
  public Relationship.Type getConnectionStatus(Identity fromIdentity, Identity toIdentity) throws Exception {
    if (fromIdentity.getId().equals(toIdentity.getId())) {
      return Relationship.Type.SELF;
    }
    Relationship relationship = getRelationship(fromIdentity, toIdentity);
    return getRelationshipStatus(relationship, toIdentity);
  }

  /**
   * @param listener
   */
  public void registerListener(RelationshipListener listener) {
    lifeCycle.addListener(listener);
  }

  /**
   * @param listener
   */
  public void unregisterListener(RelationshipListener listener) {
    lifeCycle.removeListener(listener);
  }

  /**
   * @param plugin
   */
  public void addListenerPlugin(RelationshipListenerPlugin plugin) {
    registerListener(plugin);
  }
}