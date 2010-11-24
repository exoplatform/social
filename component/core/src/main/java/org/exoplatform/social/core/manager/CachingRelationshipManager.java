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

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Property;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.RelationshipStorage;

/**
 * Class CachingRelationshipManager extends RelationshipManagerImpl with caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @since Nov 24, 2010
 * @version 1.2.0-GA
 */
public class CachingRelationshipManager extends RelationshipManagerImpl {
  /** Cache list of relationships by its id identity */
  private ExoCache<String, List<Relationship>> relationshipListCache;

  /** Cache a relationship by its id relationship */
  private ExoCache<String, Relationship>       relationshipIdCache;

  /**
   * {@inheritDoc}
   *
   * @param cacheService
   */
  public CachingRelationshipManager(RelationshipStorage relationshipStorage,
                                    CacheService cacheService) {
    super(relationshipStorage);
    this.relationshipListCache = cacheService.getCacheInstance(this.getClass().getName()
        + "relationshipListCache");
    this.relationshipIdCache = cacheService.getCacheInstance(this.getClass().getName()
        + "relationshipIdCache");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deny(Relationship relationship) throws Exception {
    List<Relationship> cachedRelationshipsReceiver = this.relationshipListCache.get(relationship.getReceiver()
                                                                                                .getId());
    List<Relationship> cachedRelationshipsSender = this.relationshipListCache.get(relationship.getSender()
                                                                                              .getId());
    if (cachedRelationshipsReceiver != null) {
      if (cachedRelationshipsReceiver.contains(relationship)) {
        cachedRelationshipsReceiver.remove(relationship);
        this.relationshipListCache.put(relationship.getReceiver().getId(),
                                       cachedRelationshipsReceiver);
      }
    } else if (cachedRelationshipsSender != null) {
      if (cachedRelationshipsSender.contains(relationship)) {
        cachedRelationshipsSender.remove(relationship);
        this.relationshipListCache.put(relationship.getSender().getId(), cachedRelationshipsSender);
      }
    } else if (this.relationshipIdCache.get(relationship.getId()) != null) {
      this.relationshipIdCache.remove(relationship.getId());
    }
    this.getStorage().removeRelationship(relationship);
    this.getLifeCycle().relationshipDenied(this, relationship);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(Relationship relationship) throws Exception {
    List<Relationship> cachedRelationshipsReceiver = this.relationshipListCache.get(relationship.getReceiver()
                                                                                                .getId());
    List<Relationship> cachedRelationshipsSender = this.relationshipListCache.get(relationship.getSender()
                                                                                              .getId());
    if (cachedRelationshipsReceiver != null) {
      if (cachedRelationshipsReceiver.contains(relationship)) {
        cachedRelationshipsReceiver.remove(relationship);
        this.relationshipListCache.put(relationship.getReceiver().getId(),
                                       cachedRelationshipsReceiver);
      }
    } else if (cachedRelationshipsSender != null) {
      if (cachedRelationshipsSender.contains(relationship)) {
        cachedRelationshipsSender.remove(relationship);
        this.relationshipListCache.put(relationship.getSender().getId(), cachedRelationshipsSender);
      }
    } else if (this.relationshipIdCache.get(relationship.getId()) != null) {
      this.relationshipIdCache.remove(relationship.getId());
    }
    this.getStorage().removeRelationship(relationship);
    this.getLifeCycle().relationshipRemoved(this, relationship);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Relationship> getAllRelationships(Identity identity) throws Exception {
    List<Relationship> cachedRelationship = this.relationshipListCache.get(identity.getId());
    if (cachedRelationship == null) {
      cachedRelationship = this.getStorage().getRelationshipByIdentity(identity);
      if (cachedRelationship != null) {
        this.relationshipListCache.put(identity.getId(), cachedRelationship);
      }
    }
    return cachedRelationship;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Relationship> getRelationshipsByIdentityId(String id) throws Exception {
    List<Relationship> relationshipListCached = this.relationshipListCache.get(id);
    if (relationshipListCached == null) {
      relationshipListCached = this.getStorage().getRelationshipByIdentityId(id);
      if (relationshipListCached != null) {
        this.relationshipListCache.put(id, relationshipListCached);
      }
    }
    return relationshipListCached;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveRelationship(Relationship relationship) throws Exception {
    final Identity sender = relationship.getSender();
    final Identity receiver = relationship.getReceiver();
    final String senderId = sender.getId();
    final String receiverId = receiver.getId();

    if (senderId.equals(receiverId)) {
      throw new Exception("the two identity are the same");
    }

    for (Property prop : relationship.getProperties()) {
      // if the initiator is not in the member of the relationship, we throw an
      // exception
      final String initiatorId = prop.getInitiator().getId();
      if (!(initiatorId.equals(senderId) || initiatorId.equals(receiverId))) {
        throw new Exception("the property initiator is not member of the relationship");
      }
    }

    List<Relationship> cachedRelationshipsReceiver = this.relationshipListCache.get(relationship.getReceiver()
                                                                                                .getId());
    List<Relationship> cachedRelationshipsSender = this.relationshipListCache.get(relationship.getSender()
                                                                                              .getId());
    if (cachedRelationshipsReceiver != null) {
      if (cachedRelationshipsReceiver.contains(relationship)) {
        cachedRelationshipsReceiver.remove(relationship);
        this.relationshipListCache.put(relationship.getReceiver().getId(),
                                       cachedRelationshipsReceiver);
      }
    } else if (cachedRelationshipsSender != null) {
      if (cachedRelationshipsSender.contains(relationship)) {
        cachedRelationshipsSender.remove(relationship);
        this.relationshipListCache.put(relationship.getSender().getId(), cachedRelationshipsSender);
      }
    } else if (this.relationshipIdCache.get(relationship.getId()) != null) {
      this.relationshipIdCache.remove(relationship.getId());
    }
    this.getStorage().saveRelationship(relationship);
  }
}