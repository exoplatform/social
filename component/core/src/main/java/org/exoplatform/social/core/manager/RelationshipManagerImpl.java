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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.identity.ConnectionFilterListAccess;
import org.exoplatform.social.core.identity.ConnectionListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.RelationshipLifeCycle;
import org.exoplatform.social.core.relationship.RelationshipListener;
import org.exoplatform.social.core.relationship.RelationshipListenerPlugin;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.RelationshipStorageException;

/**
 * The Class RelationshipManager implements RelationshipManager without caching.
 * 
 * @modifier tuan_nguyenxuan
 * @since Nov 24, 2010
 * @version 1.2.0-GA
 */
public class RelationshipManagerImpl implements RelationshipManager {
  /** The activityStorage. */
  protected RelationshipStorage   storage;

  /**
   * lifecycle of a relationship.
   */
  protected RelationshipLifeCycle lifeCycle = new RelationshipLifeCycle();

  private IdentityManager identityManager;
  
  /**
   * The default offset when get list identities with connection list access.
   * 
   * @since 1.2.0-Beta3
   */
  protected static final int OFFSET = 0;
  
  /**
   * The default limit when get list identities with connection list access.
   * 
   * @since 1.2.0-Beta3
   */
  protected static final int LIMIT = 200;

  /**
   * Instantiates a new relationship manager.
   * 
   * @param relationshipStorage
   */
  public RelationshipManagerImpl(RelationshipStorage relationshipStorage) {
    this.storage = relationshipStorage;
  }

  /**
   * {@inheritDoc}
   */
  public Relationship get(String relationshipId) {
    Relationship relationship = null;
    try {
      relationship = storage.getRelationship(relationshipId);
    } catch (Exception e) {
      return null;
    }
    return relationship;
  }

  /**
   * {@inheritDoc}
   */
  public Relationship invite(Identity sender, Identity receiver) throws RelationshipStorageException {
    return this.inviteToConnect(sender, receiver);
  }

  /**
   * {@inheritDoc}
   */
  public void save(Relationship relationship) throws RelationshipStorageException {
    this.update(relationship);
  }

  /**
   * {@inheritDoc}
   */
  public void confirm(Relationship relationship) throws RelationshipStorageException {
    this.confirm(relationship.getReceiver(), relationship.getSender());
  }

  /**
   * {@inheritDoc}
   */
  public void deny(Relationship relationship) throws RelationshipStorageException {
    this.deny(relationship.getReceiver(), relationship.getSender());
  }

  /**
   * {@inheritDoc}
   */
  public void remove(Relationship relationship) throws RelationshipStorageException {
    this.delete(relationship);
  }

  /**
   * {@inheritDoc}
   */
  public void ignore(Relationship relationship) throws RelationshipStorageException {
    this.ignore(relationship.getSender(), relationship.getReceiver());
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getPending(Identity sender) throws RelationshipStorageException {
    return getSender(sender, Relationship.Type.PENDING, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getPending(Identity sender, List<Identity> identities) throws RelationshipStorageException {
    return getSender(sender, Relationship.Type.PENDING, identities);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getIncoming(Identity receiver) throws RelationshipStorageException {
    return getReceiver(receiver, Relationship.Type.PENDING, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getIncoming(Identity receiver, List<Identity> identities) throws RelationshipStorageException {
    return getReceiver(receiver, Relationship.Type.PENDING, identities);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getConfirmed(Identity identity) throws RelationshipStorageException {
    return getRelationships(identity, Relationship.Type.CONFIRMED, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getConfirmed(Identity identity, List<Identity> identities) throws RelationshipStorageException {
    return getRelationships(identity, Relationship.Type.CONFIRMED, identities);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getAll(Identity identity) throws RelationshipStorageException {
    return getRelationships(identity, null, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getAll(Identity identity, List<Identity> identities) throws RelationshipStorageException {
    return getRelationships(identity, null, identities);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getAll(Identity identity, Relationship.Type type,
                                                List<Identity> identities) throws RelationshipStorageException {
    return getRelationships(identity, type, identities);
  }

  /**
   * {@inheritDoc}
   */
  public Relationship get(Identity identity1, Identity identity2) throws RelationshipStorageException {
    return storage.getRelationship(identity1, identity2);
  }

  /**
   * {@inheritDoc}
   */
  public Relationship.Type getStatus(Identity identity1, Identity identity2) throws RelationshipStorageException {
    Relationship relationship = get(identity1, identity2);
    return relationship == null ? null : relationship.getStatus();
  }

  /**
   * Registers a RelationshipLister.
   *
   * @param listener
   */
  public void registerListener(RelationshipListener listener) {
    lifeCycle.addListener(listener);
  }

  /**
   * Unregisters a RelationshipLister.
   *
   * @param listener
   */
  public void unregisterListener(RelationshipListener listener) {
    lifeCycle.removeListener(listener);
  }

  /**
   * Adds a RelationshipListenerPlugin.
   *
   * @param plugin
   */
  public void addListenerPlugin(RelationshipListenerPlugin plugin) {
    registerListener(plugin);
  }

  /**
   * Gets the relationship activityStorage.
   *
   * @return storage
   */
  protected RelationshipStorage getStorage() {
    return storage;
  }

  /**
   * Sets the relationship activityStorage.
   *
   * @param storage
   */
  protected void setStorage(RelationshipStorage storage) {
    this.storage = storage;
  }

  /**
   * Gets the relationship life cycle.
   *
   * @return lifeCycle
   */
  protected RelationshipLifeCycle getLifeCycle() {
    return lifeCycle;
  }

  /**
   * Sets thre relationship life cycle.
   *
   * @param lifeCycle
   */
  protected void setLifeCycle(RelationshipLifeCycle lifeCycle) {
    this.lifeCycle = lifeCycle;
  }

  /**
   * Get relationships by identity and status from cache or activityStorage
   * 
   * @param identity
   * @param identities
   * @return list of relationship
   * @throws RelationshipStorageException
   */
  protected List<Relationship> getRelationships(Identity identity, Relationship.Type type,
                                                List<Identity> identities) throws RelationshipStorageException {
    return storage.getRelationships(identity, type, identities);
  }

  /**
   * Get relationships by identity and status and identities from activityStorage
   * 
   * @param sender
   * @return list of relationship
   * @throws RelationshipStorageException
   */
  protected List<Relationship> getSender(Identity sender, Relationship.Type type,
                                         List<Identity> identities) throws RelationshipStorageException {
    return storage.getSenderRelationships(sender, type, identities);
  }

  /**
   * Get relationships by identity and status from cache or activityStorage
   * 
   * @param receiver
   * @param identities
   * @return list of relationship
   * @throws RelationshipStorageException
   */
  protected List<Relationship> getReceiver(Identity receiver, Relationship.Type type,
                                           List<Identity> identities) throws RelationshipStorageException {
    return storage.getReceiverRelationships(receiver, type, identities);
  }

  /**
   * {@inheritDoc}
   */
  public Relationship create(Identity sender, Identity receiver) {
    return new Relationship(sender, receiver);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> findRelationships(Identity ownerIdentity, Type relationshipType) throws RelationshipStorageException {
    List<Relationship> allRelationships = getAll(ownerIdentity, relationshipType, null);
    List<Identity> identities = new ArrayList<Identity>();
    if (allRelationships == null || allRelationships.size() == 0) {
      return identities;
    }
    for(Relationship relationship : allRelationships) {
      identities.add(relationship.getPartner(ownerIdentity));
    }
    return identities;
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> findRoute(Identity sender, Identity receiver) throws RelationshipStorageException {
    List<Relationship> route = new ArrayList<Relationship>();
    route.add(get(sender,receiver));
    return route;
  }

  /**
   * {@inheritDoc}
   */
  public Type getConnectionStatus(Identity fromIdentity, Identity toIdentity) throws Exception {
    return getStatus(fromIdentity, toIdentity);
  }
  
  /**
   * {@inheritDoc}
   */
  public ListAccess<Identity> getConnections(Identity identity) {
    return (new ConnectionListAccess(storage, identity, ConnectionListAccess.Type.CONNECTION));
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Relationship> getContacts(Identity currIdentity, List<Identity> identities) throws RelationshipStorageException {
    return getAll(currIdentity, Type.CONFIRMED, identities);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getContacts(Identity identity) throws RelationshipStorageException {
    return getAll(identity, Type.CONFIRMED, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentities(Identity id) throws Exception {
    return Arrays.asList(this.getConnections(id).load(OFFSET, LIMIT));
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getPendingRelationships(Identity identity) throws RelationshipStorageException {
    return getPending(identity);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getPendingRelationships(Identity identity, boolean toConfirm) throws RelationshipStorageException {
    return getAll(identity, Type.PENDING, null);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getPendingRelationships(Identity currIdentity, List<Identity> identities,
                                                    boolean toConfirm) throws RelationshipStorageException {
    return getAll(currIdentity, Type.PENDING, identities);
  }

  /**
   * {@inheritDoc}
   */
  public Relationship getRelationship(Identity sender, Identity receiver) throws RelationshipStorageException {
    return get(sender, receiver);
  }

  /**
   * {@inheritDoc}
   */
  public Relationship getRelationshipById(String id) throws RelationshipStorageException {
    return get(id);
  }

  /**
   * {@inheritDoc}
   */
  public Type getRelationshipStatus(Relationship rel, Identity id) {
    return rel.getStatus();
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getRelationshipsByIdentityId(String id) throws RelationshipStorageException {
    return getAll(getIdentityManager().getIdentity(id));
  }

  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }

  /**
   * {@inheritDoc}
   */
  public void saveRelationship(Relationship relationship) throws RelationshipStorageException {
    save(relationship);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getAllRelationships(Identity identity) throws RelationshipStorageException {
    return getAll(identity);
  }

  /**
   * {@inheritDoc}
   */
  public void confirm(Identity invitedIdentity, Identity invitingIdentity) {
    Relationship relationship = get(invitedIdentity, invitingIdentity);
    if (relationship != null && relationship.getStatus() == Relationship.Type.PENDING) {
      relationship.setStatus(Relationship.Type.CONFIRMED);
      this.update(relationship);
      lifeCycle.relationshipConfirmed(this, relationship);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void delete(Relationship existingRelationship) {
    storage.removeRelationship(existingRelationship);
    lifeCycle.relationshipRemoved(this, existingRelationship);
  }

  /**
   * {@inheritDoc}
   */
  public void deny(Identity invitedIdentity, Identity invitingIdentity) {
    Relationship relationship = this.get(invitedIdentity, invitingIdentity);
    if (relationship != null && relationship.getStatus() == Relationship.Type.PENDING) {
      //    relationship.setStatus(Relationship.Type.IGNORED);
      //  save(relationship);
      // TODO: now just remove, implement later
      this.delete(relationship);
      lifeCycle.relationshipDenied(this, relationship);
    }
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Identity> getAllWithListAccess(Identity existingIdentity) {
    return new ConnectionListAccess(this.storage, existingIdentity, ConnectionListAccess.Type.ALL);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Identity> getIncomingWithListAccess(Identity existingIdentity) {
    return new ConnectionListAccess(this.storage, existingIdentity, ConnectionListAccess.Type.INCOMING);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Identity> getOutgoing(Identity existingIdentity) {
    return new ConnectionListAccess(this.storage, existingIdentity, ConnectionListAccess.Type.OUTGOING);
  }

  /**
   * {@inheritDoc}
   */
  public void ignore(Identity invitedIdentity, Identity invitingIdentity) {
    Relationship relationship = this.get(invitedIdentity, invitingIdentity);
    if (relationship != null) {
      //    relationship.setStatus(Relationship.Type.IGNORED);
      //  save(relationship);
      // TODO: now just remove, implement later
      this.delete(relationship);
      lifeCycle.relationshipIgnored(this, relationship);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Relationship inviteToConnect(Identity invitingIdentity, Identity invitedIdentity) {
    Relationship relationship = get(invitingIdentity, invitedIdentity);
    if (relationship == null) {
      relationship = new Relationship(invitingIdentity, invitedIdentity);
      relationship.setStatus(Type.PENDING);
      this.update(relationship);
      //lifeCycle.relationshipRequested(this, relationship);
    }
    return relationship;
  }

  /**
   * {@inheritDoc}
   */
  public void update(Relationship existingRelationship) {
    String senderId = existingRelationship.getSender().getId();
    String receiverId = existingRelationship.getReceiver().getId();
    if (senderId.equals(receiverId)) {
      throw new RelationshipStorageException(RelationshipStorageException.Type.FAILED_TO_SAVE_RELATIONSHIP,
                                             "the two identity are the same");
    }
    storage.saveRelationship(existingRelationship);
  }

  public ListAccess<Identity> getConnectionsByFilter(Identity existingIdentity, ProfileFilter profileFilter) {
    return new ConnectionFilterListAccess(this.storage, existingIdentity, profileFilter,
                                          ConnectionFilterListAccess.Type.PROFILE_FILTER_CONNECTION);
  }

  public ListAccess<Identity> getIncomingByFilter(Identity existingIdentity, ProfileFilter profileFilter) {
    return new ConnectionFilterListAccess(this.storage, existingIdentity, profileFilter,
                                          ConnectionFilterListAccess.Type.PROFILE_FILTER_INCOMMING);
  }

  public ListAccess<Identity> getOutgoingByFilter(Identity existingIdentity, ProfileFilter profileFilter) {
    
    return new ConnectionFilterListAccess(this.storage, existingIdentity, profileFilter,
                                          ConnectionFilterListAccess.Type.PROFILE_FILTER_OUTGOING);
  }

  public Map<Identity, Integer> getSuggestions(Identity identity, int offset, int limit) {
    return this.storage.getSuggestions(identity, offset, limit);
  }
  
}
