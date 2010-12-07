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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.RelationshipStorage;
import org.exoplatform.social.core.storage.RelationshipStorageException;

/**
 * Class CachingRelationshipManager extends RelationshipManagerImpl with
 * caching.
 * 
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @modifier tuan_nguyenxuan
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
   */
  public CachingRelationshipManager(RelationshipStorage relationshipStorage, CacheService cacheService) {
    super(relationshipStorage);
    this.relationshipListCache = cacheService.getCacheInstance(this.getClass().getName() + "relationshipListCache");
    this.relationshipIdCache = cacheService.getCacheInstance(this.getClass().getName() + "relationshipIdCache");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(Relationship relationship) throws RelationshipStorageException {
    removeCachedRelationship(relationship);
    super.remove(relationship);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Relationship get(Identity identity1, Identity identity2) throws RelationshipStorageException {
    List<Relationship> cachedRelationshipsByIdentity1 = relationshipListCache.get(identity1.getId());
    if (cachedRelationshipsByIdentity1 != null) {
      for (Relationship relationship : cachedRelationshipsByIdentity1) {
        if (relationship.getPartner(identity1).equals(identity2)) {
          return relationship;
        }
      }
    }

    List<Relationship> cachedRelationshipsByIdentity2 = relationshipListCache.get(identity2.getId());
    if (cachedRelationshipsByIdentity2 != null) {
      for (Relationship relationship : cachedRelationshipsByIdentity2) {
        if (relationship.getPartner(identity2).equals(identity1)) {
          return relationship;
        }
      }
    }

    return super.get(identity1, identity2);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Relationship get(String relationshipId) {
    Relationship relationship = relationshipIdCache.get(relationshipId);
    if (relationship != null) {
      return relationship;
    }
    relationship = super.get(relationshipId);
    if (relationship != null) {
      putRelationshipToCache(relationship);
    }
    return relationship;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void save(Relationship relationship) throws RelationshipStorageException {
    super.save(relationship);
    putRelationshipToCache(relationship);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Relationship> getAllRelationships(Identity identity) throws RelationshipStorageException {
    String identityId = identity.getId();
    List<Relationship> cachedRelationships = relationshipListCache.get(identityId);
    if (cachedRelationships == null) {
      cachedRelationships = super.getRelationships(identity, null, null);
      if (cachedRelationships == null || cachedRelationships.size() == 0) {
        return null;
      }
      this.relationshipListCache.put(identityId, cachedRelationships);
    }
    return cachedRelationships;
  }

  /**
   * Puts a relationship to relationshipIdCache and modify the list of sender
   * and receiver if cached in relationshipListCache
   * 
   * @param relationship
   * @throws RelationshipStorageException
   */
  private void putRelationshipToCache(Relationship relationship) throws RelationshipStorageException {
    String relationshipId = relationship.getId();
    if (this.relationshipIdCache.get(relationshipId) != null) {
      this.relationshipIdCache.remove(relationshipId);
    }
    this.relationshipIdCache.put(relationshipId, relationship);

    String senderId = relationship.getSender().getId();
    List<Relationship> cachedRelationshipsSender = this.relationshipListCache.get(senderId);
    if (cachedRelationshipsSender != null) {
      if (cachedRelationshipsSender.contains(relationship)) {
        cachedRelationshipsSender.remove(relationship);
      }
      cachedRelationshipsSender.add(relationship);
      this.relationshipListCache.put(relationship.getSender().getId(), cachedRelationshipsSender);
    }

    String receiverId = relationship.getReceiver().getId();
    List<Relationship> cachedRelationshipsReceiver = this.relationshipListCache.get(receiverId);
    if (cachedRelationshipsReceiver != null) {
      if (cachedRelationshipsReceiver.contains(relationship)) {
        cachedRelationshipsReceiver.remove(relationship);
      }
      cachedRelationshipsReceiver.add(relationship);
      this.relationshipListCache.put(relationship.getReceiver().getId(), cachedRelationshipsReceiver);
    }
  }

  /**
   * Removes cached relationship in relationshipListCache and
   * relationshipIdCache
   * 
   * @param relationship
   */
  private void removeCachedRelationship(Relationship relationship) {
    if (this.relationshipIdCache.get(relationship.getId()) != null) {
      this.relationshipIdCache.remove(relationship.getId());
    }

    String senderId = relationship.getSender().getId();
    List<Relationship> cachedRelationshipsSender = this.relationshipListCache.get(senderId);

    if (cachedRelationshipsSender != null) {
      if (cachedRelationshipsSender.contains(relationship)) {
        cachedRelationshipsSender.remove(relationship);
        this.relationshipListCache.put(senderId, cachedRelationshipsSender);
      }
    }

    String receiverId = relationship.getReceiver().getId();
    List<Relationship> cachedRelationshipsReceiver = this.relationshipListCache.get(receiverId);
    if (cachedRelationshipsReceiver != null) {
      if (cachedRelationshipsReceiver.contains(relationship)) {
        cachedRelationshipsReceiver.remove(relationship);
        this.relationshipListCache.put(receiverId, cachedRelationshipsReceiver);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<Relationship> getRelationships(Identity identity, Relationship.Type type,
                                                List<Identity> identities) throws RelationshipStorageException {
    List<Relationship> cachedRelationships = getAllRelationships(identity);
    if(cachedRelationships == null || cachedRelationships.size() == 0) {
      return null;
    }

    List<Relationship> filterCachedRelationships = new ArrayList<Relationship>();
    for (Relationship relationship : cachedRelationships) {
      if (type == null) {
        if (identities == null) {
          filterCachedRelationships.add(relationship);
        } else if (identities.contains(relationship.getPartner(identity))) {
          filterCachedRelationships.add(relationship);
        }
      } else if (relationship.getStatus().equals(type)) {
        if (identities == null) {
          filterCachedRelationships.add(relationship);
        } else if (identities.contains(relationship.getPartner(identity))) {
          filterCachedRelationships.add(relationship);
        }
      }
    }
    return filterCachedRelationships;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<Relationship> getSender(Identity sender, Relationship.Type type,
                                                      List<Identity> identities) throws RelationshipStorageException {
    List<Relationship> cachedRelationships = getAllRelationships(sender);
    if(cachedRelationships == null || cachedRelationships.size() == 0) {
      return null;
    }

    List<Relationship> filterCachedRelationships = new ArrayList<Relationship>();
    for (Relationship relationship : cachedRelationships) {
      if (!relationship.getSender().equals(sender))
        continue;
      if (type == null) {
        if (identities == null) {
          filterCachedRelationships.add(relationship);
        } else if (identities.contains(relationship.getReceiver())) {
          filterCachedRelationships.add(relationship);
        }
      } else if (relationship.getStatus().equals(type)) {
        if (identities == null) {
          filterCachedRelationships.add(relationship);
        } else if (identities.contains(relationship.getReceiver())) {
          filterCachedRelationships.add(relationship);
        }
      }
    }
    return filterCachedRelationships;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<Relationship> getReceiver(Identity receiver, Relationship.Type type,
                                                        List<Identity> identities) throws RelationshipStorageException {
    List<Relationship> cachedRelationships = getAllRelationships(receiver);
    if(cachedRelationships == null || cachedRelationships.size() == 0) {
      return null;
    }

    List<Relationship> filterCachedRelationships = new ArrayList<Relationship>();
    for (Relationship relationship : cachedRelationships) {
      if (!relationship.getReceiver().equals(receiver))
        continue;
      if (type == null) {
        if (identities == null) {
          filterCachedRelationships.add(relationship);
        } else if (identities.contains(relationship.getReceiver())) {
          filterCachedRelationships.add(relationship);
        }
      } else if (relationship.getStatus().equals(type)) {
        if (identities == null) {
          filterCachedRelationships.add(relationship);
        } else if (identities.contains(relationship.getReceiver())) {
          filterCachedRelationships.add(relationship);
        }
      }
    }
    return filterCachedRelationships;
  }
}