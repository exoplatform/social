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

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
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

  /** Cache list of relationships by its id identity */
  private ExoCache<String, List<Relationship>> relationshipListCache;

  /** Cache a relationship by its id relationship */
  private ExoCache<String, Relationship>       relationshipIdCache;

  /**
   * Instantiates a new relationship manager.
   * @param relationshipStorage
   */
  public RelationshipManager(RelationshipStorage relationshipStorage, CacheService cacheService) {
    this.storage = relationshipStorage;
    this.relationshipListCache = cacheService.getCacheInstance(this.getClass().getName() + "relationshipListCache");
    this.relationshipIdCache = cacheService.getCacheInstance(this.getClass().getName() + "relationshipIdCache");
  }

  /**
   * Gets relationship the by id.
   *
   * @param id the id
   * @return the by id
   * @throws Exception the exception
   */
  public Relationship getRelationshipById(String id) throws Exception {
    Relationship cachedRelationship = this.relationshipIdCache.get(id);
    if (cachedRelationship == null) {
      cachedRelationship = this.storage.getRelationship(id);
      if (cachedRelationship != null) {
        this.relationshipIdCache.put(id, cachedRelationship);
      }
    }
    return cachedRelationship;
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
    final Identity sender = relationship.getSender();
    final Identity receiver = relationship.getReceiver();
    final String senderId = sender.getId();
    final String receiverId = receiver.getId();

    List<Relationship> cachedRelationshipsSender = this.relationshipListCache.get(senderId);
    List<Relationship> cachedRelationshipReceiver = this.relationshipListCache.get(receiverId);

    // remove in sender
    if (cachedRelationshipsSender != null && cachedRelationshipsSender.size() > 0) {
      for (int i = 0; i < cachedRelationshipsSender.size(); i++) {
        Relationship temp = cachedRelationshipsSender.get(i);
        if (temp.getSender().getId().equals(senderId)
            && temp.getReceiver().getId().equals(receiverId)) {
          cachedRelationshipsSender.remove(i);
          break;
        }
      }
      this.relationshipListCache.remove(senderId);
      if (cachedRelationshipsSender.size() > 0) {
        this.relationshipListCache.put(senderId, cachedRelationshipsSender);
      }
    }

    // remove in receiver
    if (cachedRelationshipReceiver != null && cachedRelationshipReceiver.size() > 0) {
      for (int i = 0; i < cachedRelationshipReceiver.size(); i++) {
        Relationship temp = cachedRelationshipReceiver.get(i);
        if (temp.getSender().getId().equals(senderId)
            && temp.getReceiver().getId().equals(receiverId)) {
          cachedRelationshipReceiver.remove(i);
          break;
        }
      }
      this.relationshipListCache.remove(receiverId);
      if (cachedRelationshipReceiver.size() > 0) {
        this.relationshipListCache.put(receiverId, cachedRelationshipReceiver);
      }
    }
    if (relationship.getId() != null) {
      if (this.relationshipIdCache.get(relationship.getId()) != null) {
        this.relationshipIdCache.remove(relationship.getId());
      }
    }
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
    final Identity sender = relationship.getSender();
    final Identity receiver = relationship.getReceiver();
    final String senderId = sender.getId();
    final String receiverId = receiver.getId();
    List<Relationship> cachedRelationshipsSender = this.relationshipListCache.get(senderId);
    List<Relationship> cachedRelationshipReceiver = this.relationshipListCache.get(receiverId);

    // remove in sender
    if (cachedRelationshipsSender != null && cachedRelationshipsSender.size() > 0) {
      for (int i = 0; i < cachedRelationshipsSender.size(); i++) {
        Relationship temp = cachedRelationshipsSender.get(i);
        if (temp.getSender().getId().equals(senderId)
            && temp.getReceiver().getId().equals(receiverId)) {
          cachedRelationshipsSender.remove(i);
          break;
        }
      }
      this.relationshipListCache.remove(senderId);
      if (cachedRelationshipsSender.size() > 0) {
        this.relationshipListCache.put(senderId, cachedRelationshipsSender);
      }
    }

    // remove in receiver
    if (cachedRelationshipReceiver != null && cachedRelationshipReceiver.size() > 0) {
      for (int i = 0; i < cachedRelationshipReceiver.size(); i++) {
        Relationship temp = cachedRelationshipReceiver.get(i);
        if (temp.getSender().getId().equals(senderId)
            && temp.getReceiver().getId().equals(receiverId)) {
          cachedRelationshipReceiver.remove(i);
          break;
        }
      }
      this.relationshipListCache.remove(receiverId);
      if (cachedRelationshipReceiver.size() > 0) {
        this.relationshipListCache.put(receiverId, cachedRelationshipReceiver);
      }
    }
    if (relationship.getId() != null) {
      if (this.relationshipIdCache.get(relationship.getId()) != null) {
        this.relationshipIdCache.remove(relationship.getId());
      }
    }
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
    List<Relationship> cachedRelationship = this.relationshipListCache.get(identity.getId());
    if (cachedRelationship == null) {
      cachedRelationship = this.storage.getRelationshipByIdentity(identity);
    }
    return cachedRelationship;
  }

  /**
   * return all the relationship associated with a given identityId.
   *
   * @param id the id
   * @return the by identity id
   * @throws Exception the exception
   */
  public List<Relationship> getRelationshipsByIdentityId(String id) throws Exception {
    List<Relationship> listCached = this.relationshipListCache.get(id);
    if (listCached == null) {
      listCached = this.storage.getRelationshipByIdentityId(id);
    }
    return listCached;
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
    this.updateRelationshipCached(relationship);
    this.storage.saveRelationship(relationship);
  }

  /**
   * Updates the relationship cached.
   * @param relationship
   */
  public void updateRelationshipCached(Relationship relationship) {
    final Identity sender = relationship.getSender();
    final Identity receiver = relationship.getReceiver();
    final String senderId = sender.getId();
    final String receiverId = receiver.getId();
    List<Relationship> cachedRelationshipsSender = this.relationshipListCache.get(senderId);
    List<Relationship> cachedRelationshipReceiver = this.relationshipListCache.get(receiverId);

    boolean updateCache;
    // update in sender
    if (cachedRelationshipsSender != null && cachedRelationshipsSender.size() > 0) {
      updateCache = true;
      for (int i = 0; i < cachedRelationshipsSender.size(); i++) {
        Relationship temp = cachedRelationshipsSender.get(i);
        if (temp.getSender().getId().equals(senderId)
            && temp.getReceiver().getId().equals(receiverId)) {
          cachedRelationshipsSender.remove(i);
          cachedRelationshipsSender.add(temp);
          updateCache = false;
          break;
        }
      }
      if (updateCache) {
        cachedRelationshipsSender.add(relationship);
      }
      this.relationshipListCache.remove(senderId);
      this.relationshipListCache.put(senderId, cachedRelationshipsSender);
    }
    if (cachedRelationshipsSender == null) {
      List<Relationship> listRelationship = new ArrayList<Relationship>();
      listRelationship.add(relationship);
      this.relationshipListCache.put(senderId, listRelationship);
    }

    // update in receiver
    if (cachedRelationshipReceiver != null && cachedRelationshipReceiver.size() > 0) {
      updateCache = true;
      for (int i = 0; i < cachedRelationshipReceiver.size(); i++) {
        Relationship temp = cachedRelationshipReceiver.get(i);
        if (temp.getSender().getId().equals(senderId)
            && temp.getReceiver().getId().equals(receiverId)) {
          cachedRelationshipReceiver.remove(i);
          cachedRelationshipReceiver.add(temp);
          updateCache = false;
          break;
        }
      }
      if (updateCache) {
        cachedRelationshipReceiver.add(relationship);
        this.relationshipListCache.remove(receiverId);
        this.relationshipListCache.put(receiverId, cachedRelationshipReceiver);
      }
    }
    if (cachedRelationshipReceiver == null) {
      List<Relationship> listRelationship = new ArrayList<Relationship>();
      listRelationship.add(relationship);
      this.relationshipListCache.put(receiverId, listRelationship);
    }
    if (cachedRelationshipsSender != null && cachedRelationshipsSender.size() == 0) {
      this.relationshipListCache.remove(senderId);
    }
    if (cachedRelationshipReceiver != null && cachedRelationshipReceiver.size() == 0) {
      this.relationshipListCache.remove(receiverId);
    }
    if (relationship.getId() != null) {
      if (this.relationshipIdCache.get(relationship.getId()) != null) {
        this.relationshipIdCache.remove(relationship.getId());
        this.relationshipIdCache.put(relationship.getId(), relationship);
      } else {
        this.relationshipIdCache.put(relationship.getId(), relationship);
      }
    }
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
   * Finds any identity having relationshipType with the ownerIdentity.
   * @param ownerIdentity
   * @param relationshipType
   * @return list of identites
   * @throws Exception
   * @since 1.1.2
   */
  List<Identity> findRelationships(Identity ownerIdentity, Relationship.Type relationshipType) throws Exception {
    return storage.findRelationships(ownerIdentity.getId(), relationshipType.name());
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