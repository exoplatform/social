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

package org.exoplatform.social.core.storage.cache;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.RelationshipStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.IdentityData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.data.ListIdentitiesData;
import org.exoplatform.social.core.storage.cache.model.data.RelationshipData;
import org.exoplatform.social.core.storage.cache.model.data.SuggestionsData;
import org.exoplatform.social.core.storage.cache.model.key.IdentityFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.ListRelationshipsKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipCountKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipIdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipType;
import org.exoplatform.social.core.storage.cache.model.key.SuggestionKey;
import org.exoplatform.social.core.storage.cache.selector.RelationshipCacheSelector;
import org.exoplatform.social.core.storage.cache.selector.SuggestionCacheSelector;
import org.exoplatform.social.core.storage.impl.AbstractStorage;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImpl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

/**
 * Cache support for RelationshipStorage.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedRelationshipStorage extends AbstractStorage implements RelationshipStorage {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(CachedRelationshipStorage.class);

  //
  private final ExoCache<RelationshipKey, RelationshipData> exoRelationshipCache;
  private final ExoCache<RelationshipIdentityKey, RelationshipKey> exoRelationshipByIdentityCache;
  private final ExoCache<RelationshipCountKey, IntegerData> exoRelationshipCountCache;
  private final ExoCache<ListRelationshipsKey, ListIdentitiesData> exoRelationshipsCache;
  private final ExoCache<SuggestionKey, SuggestionsData> exoSuggestionCache;

  //
  private final FutureExoCache<RelationshipKey, RelationshipData, ServiceContext<RelationshipData>> relationshipCache;
  private final FutureExoCache<RelationshipIdentityKey,RelationshipKey,ServiceContext<RelationshipKey>> relationshipCacheIdentity;
  private final FutureExoCache<RelationshipCountKey, IntegerData, ServiceContext<IntegerData>> relationshipsCount;
  private final FutureExoCache<ListRelationshipsKey, ListIdentitiesData, ServiceContext<ListIdentitiesData>> relationshipsCache;
  private final FutureExoCache<SuggestionKey, SuggestionsData, ServiceContext<SuggestionsData>> suggestionCache;

  //
  private final ExoCache<IdentityKey, IdentityData> exoIdentityCache;

  //
  private final RelationshipStorageImpl storage;
  private final IdentityStorage identityStorage;
  private CachedActivityStorage cachedActivityStorage;

  //
  private static final RelationshipKey RELATIONSHIP_NOT_FOUND = new RelationshipKey(null);

  void clearCacheFor(Relationship r) {

    List<String> identities = new ArrayList<String>();

    if (r.getSender() != null) {
      identities.add(r.getSender().getId());
    }
    if (r.getReceiver() != null) {
      identities.add(r.getReceiver().getId());
    }

    try {
      exoRelationshipsCache.select(new RelationshipCacheSelector(identities.toArray(new String[]{})));
      exoRelationshipCountCache.select(new RelationshipCacheSelector(identities.toArray(new String[]{})));
      exoSuggestionCache.select(new SuggestionCacheSelector(identities.toArray(new String[]{})));
    }
    catch (Exception e) {
      LOG.error(e);
    }

  }
  
  /**
   * When enable/disable an user, we need to clear all cache associated to suggestion and relationship
   */
  public void clearAllRelationshipCache() {
    try {
      exoRelationshipsCache.clearCache();
      exoRelationshipCountCache.clearCache();
      exoSuggestionCache.clearCache();
    } catch (Exception e) {
      LOG.error(e);
    }
  }
  
  /**
   * Build the identity list from the caches Ids.
   *
   * @param data ids
   * @return identities
   */
  private List<Identity> buildRelationships(ListIdentitiesData data) {

    List<Identity> identities = new ArrayList<Identity>();
    for (IdentityKey k : data.getIds()) {
      Identity gotIdentity = identityStorage.findIdentityById(k.getId());
      identities.add(gotIdentity);
    }
    return identities;

  }

  /**
   * Build the ids from the identitiy list.
   *
   * @param identities identities
   * @return ids
   */
  private ListIdentitiesData buildIds(List<Identity> identities) {

    List<IdentityKey> data = new ArrayList<IdentityKey>();
    for (Identity i : identities) {
      IdentityKey k = new IdentityKey(i);
      exoIdentityCache.put(k, new IdentityData(i));
      data.add(new IdentityKey(i));
    }
    return new ListIdentitiesData(data);

  }
  
  /**
   * Build the suggestions from the identity map.
   *
   * @param map map of identity
   */
  private SuggestionsData buildIdMap(Map<Identity, Integer> map) {

    Map<String, Integer> data = new LinkedHashMap<String, Integer>();
    for (Entry<Identity, Integer> item : map.entrySet()) {
      data.put(item.getKey().getId(), item.getValue());
    }
    
    return new SuggestionsData(data);
  }
  
  /**
   * Build the suggestions map from the caches Ids.
   *
   * @param data map of identities
   * @return suggestions
   */
  private Map<Identity, Integer> buildSuggestions(SuggestionsData data) {
    Map<Identity, Integer> suggestions = new LinkedHashMap<Identity, Integer>();
    for (Entry<String, Integer> item : data.getMap().entrySet()) {
      Identity gotIdentity = identityStorage.findIdentityById(item.getKey());
      suggestions.put(gotIdentity, item.getValue());
    }
    return suggestions;

  }

  public CachedActivityStorage getCachedActivityStorage() {
    if (cachedActivityStorage == null) {
      cachedActivityStorage = (CachedActivityStorage)
          PortalContainer.getInstance().getComponentInstanceOfType(CachedActivityStorage.class);
    }
    return cachedActivityStorage;
  }

  public CachedRelationshipStorage(final RelationshipStorageImpl storage, final IdentityStorage identityStorage,
                                   final SocialStorageCacheService cacheService) {

    //
    this.storage = storage;
    this.storage.setStorage(this);
    this.identityStorage = identityStorage;

    //
    this.exoRelationshipCache = cacheService.getRelationshipCache();
    this.exoRelationshipByIdentityCache = cacheService.getRelationshipCacheByIdentity();
    this.exoRelationshipCountCache = cacheService.getRelationshipsCount();
    this.exoRelationshipsCache = cacheService.getRelationshipsCache();
    this.exoSuggestionCache = cacheService.getSuggestionCache();

    //
    this.relationshipCache = CacheType.RELATIONSHIP.createFutureCache(exoRelationshipCache);
    this.relationshipCacheIdentity = CacheType.RELATIONSHIP_FROM_IDENTITY.createFutureCache(exoRelationshipByIdentityCache);
    this.relationshipsCount = CacheType.RELATIONSHIPS_COUNT.createFutureCache(exoRelationshipCountCache);
    this.relationshipsCache = CacheType.RELATIONSHIPS.createFutureCache(exoRelationshipsCache);
    this.suggestionCache = CacheType.SUGGESTIONS.createFutureCache(exoSuggestionCache);

    //
    this.exoIdentityCache = cacheService.getIdentityCache();

  }

  /**
   * {@inheritDoc}
   */
  public Relationship saveRelationship(final Relationship relationship) throws RelationshipStorageException {

    Relationship r = storage.saveRelationship(relationship);

    RelationshipIdentityKey identityKey1 = new RelationshipIdentityKey(r.getSender().getId(), r.getReceiver().getId());
    RelationshipIdentityKey identityKey2 = new RelationshipIdentityKey(r.getReceiver().getId(), r.getSender().getId());
    RelationshipKey key = new RelationshipKey(relationship.getId());

    exoRelationshipCache.put(key, new RelationshipData(r));
    exoRelationshipByIdentityCache.put(identityKey1, key);
    exoRelationshipByIdentityCache.put(identityKey2, key);
    clearCacheFor(relationship);
    getCachedActivityStorage().clearCache();

    return r;

  }

  /**
   * {@inheritDoc}
   */
  public void removeRelationship(final Relationship relationship) throws RelationshipStorageException {

    storage.removeRelationship(relationship);

    //
    exoRelationshipCache.remove(new RelationshipKey(relationship.getId()));
    
    // clear caching.
    if (relationship.getSender() != null && relationship.getReceiver() != null) {
      RelationshipIdentityKey identityKey = new RelationshipIdentityKey(relationship.getSender().getId(), relationship.getReceiver().getId());
      relationshipCacheIdentity.remove(identityKey);
      
      identityKey = new RelationshipIdentityKey(relationship.getReceiver().getId(), relationship.getSender().getId());
      relationshipCacheIdentity.remove(identityKey);
    }
    
    //
    clearCacheFor(relationship);
    getCachedActivityStorage().clearCache();
    
  }

  /**
   * {@inheritDoc}
   */
  public Relationship getRelationship(final String uuid) throws RelationshipStorageException {

    //
    RelationshipKey key = new RelationshipKey(uuid);

    //
    RelationshipData data = relationshipCache.get(
        new ServiceContext<RelationshipData>() {
          public RelationshipData execute() {
            Relationship got = storage.getRelationship(uuid);
            if (got != null) {
              return new RelationshipData(storage.getRelationship(uuid));
            }
            return null;
          }
        },
        key
    );

    //
    if (data != null) {
      return data.build();
    }
    return null;
    
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getSenderRelationships(
      final Identity sender, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {
    return storage.getSenderRelationships(sender, type, listCheckIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getSenderRelationships(
      final String senderId, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {
    return storage.getSenderRelationships(senderId, type, listCheckIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getReceiverRelationships(
      final Identity receiver, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {
    return storage.getReceiverRelationships(receiver, type, listCheckIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public Relationship getRelationship(Identity identity1, Identity identity2)
      throws RelationshipStorageException {

    // We make sure to check the Relationship in the same order to improve
    // efficiency of the cache
    final Identity idFirst, idLast;
    if (identity1.getId().compareTo(identity2.getId()) > 0) {
      idFirst = identity1;
      idLast = identity2;
    } else {
      idFirst = identity2;
      idLast = identity1;
    }

    final RelationshipIdentityKey key = new RelationshipIdentityKey(idFirst.getId(), idLast.getId());

    //
    RelationshipKey gotKey = relationshipCacheIdentity.get(
        new ServiceContext<RelationshipKey>() {
          public RelationshipKey execute() {
            Relationship got = storage.getRelationship(idFirst, idLast);
            if (got != null) {
              RelationshipKey k = new RelationshipKey(got.getId());
              exoRelationshipByIdentityCache.put(key, k);
              return k;
            }
            else {
              exoRelationshipByIdentityCache.put(key, RELATIONSHIP_NOT_FOUND);
              return RELATIONSHIP_NOT_FOUND;
            }
          }
        },
        key
    );

    //
    if (gotKey != null && !gotKey.equals(RELATIONSHIP_NOT_FOUND)) {
      return getRelationship(gotKey.getId());
    }
    else {
      return null;
    }

  }
  
  @Override
  public boolean hasRelationship(Identity identity1, Identity identity2, String relationshipPath) throws RelationshipStorageException {
    RelationshipIdentityKey key = new RelationshipIdentityKey(identity2.getId(), identity1.getId());
    RelationshipKey gotKey = exoRelationshipByIdentityCache.get(key);
    if (gotKey != null && ! gotKey.equals(RELATIONSHIP_NOT_FOUND) && getRelationship(identity1, identity2).getStatus().equals(Relationship.Type.CONFIRMED)) {
      return true;
    }
    
    key = new RelationshipIdentityKey(identity1.getId(), identity2.getId());
    gotKey = exoRelationshipByIdentityCache.get(key);
    if (gotKey != null && ! gotKey.equals(RELATIONSHIP_NOT_FOUND) && getRelationship(identity1, identity2).getStatus().equals(Relationship.Type.CONFIRMED)) {
      return true;
    }
    
    try {
      Node relationshipNode = node(relationshipPath.toString());
      if (relationshipNode != null) {
        RelationshipKey k;

        k = new RelationshipKey(relationshipNode.getUUID());

        exoRelationshipByIdentityCache.put(key, k);
        return true;
      }
    } catch (RepositoryException e) {
      throw new RelationshipStorageException(RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP_OF_THEM, e.getMessage());
    }
    
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public List<Relationship> getRelationships(
      final Identity identity, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {
    return storage.getRelationships(identity, type, listCheckIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getRelationships(final Identity identity, final long offset, final long limit)
      throws RelationshipStorageException {

    //
    IdentityKey key = new IdentityKey(identity);
    ListRelationshipsKey<IdentityKey> listKey =
        new ListRelationshipsKey<IdentityKey>(key, RelationshipType.RELATIONSHIP, offset, limit);
    ListIdentitiesData keys = relationshipsCache.get(
        new ServiceContext<ListIdentitiesData>() {
          public ListIdentitiesData execute() {
            List<Identity> got = storage.getRelationships(identity, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildRelationships(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIncomingRelationships(final Identity receiver, final long offset, final long limit)
      throws RelationshipStorageException {

    //
    IdentityKey key = new IdentityKey(receiver);
    ListRelationshipsKey<IdentityKey> listKey =
        new ListRelationshipsKey<IdentityKey>(key, RelationshipType.INCOMMING, offset, limit);
    ListIdentitiesData keys = relationshipsCache.get(
        new ServiceContext<ListIdentitiesData>() {
          public ListIdentitiesData execute() {
            List<Identity> got = storage.getIncomingRelationships(receiver, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildRelationships(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getIncomingRelationshipsCount(final Identity receiver) throws RelationshipStorageException {

    //
    IdentityKey iKey = new IdentityKey(receiver);
    RelationshipCountKey<IdentityKey> key = new RelationshipCountKey<IdentityKey>(iKey, RelationshipType.INCOMMING);

    //
    return relationshipsCount.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getIncomingRelationshipsCount(receiver));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getOutgoingRelationships(final Identity sender, final long offset, final long limit)
      throws RelationshipStorageException {

    //
    IdentityKey key = new IdentityKey(sender);
    ListRelationshipsKey<IdentityKey> listKey =
        new ListRelationshipsKey<IdentityKey>(key, RelationshipType.OUTGOING, offset, limit);
    ListIdentitiesData keys = relationshipsCache.get(
        new ServiceContext<ListIdentitiesData>() {
          public ListIdentitiesData execute() {
            List<Identity> got = storage.getOutgoingRelationships(sender, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildRelationships(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getOutgoingRelationshipsCount(final Identity sender) throws RelationshipStorageException {

    //
    IdentityKey iKey = new IdentityKey(sender);
    RelationshipCountKey<IdentityKey> key = new RelationshipCountKey<IdentityKey>(iKey, RelationshipType.OUTGOING);

    //
    return relationshipsCount.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getOutgoingRelationshipsCount(sender));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getRelationshipsCount(final Identity identity) throws RelationshipStorageException {

    //
    IdentityKey iKey = new IdentityKey(identity);
    RelationshipCountKey<IdentityKey> key = new RelationshipCountKey<IdentityKey>(iKey, RelationshipType.RELATIONSHIP);

    //
    return relationshipsCount.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getRelationshipsCount(identity));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getConnections(final Identity identity, final long offset, final long limit)
      throws RelationshipStorageException {

    //
    IdentityKey key = new IdentityKey(identity);
    ListRelationshipsKey<IdentityKey> listKey = new ListRelationshipsKey<IdentityKey>(key, RelationshipType.CONNECTION, offset, limit);
    ListIdentitiesData keys = relationshipsCache.get(
        new ServiceContext<ListIdentitiesData>() {
          public ListIdentitiesData execute() {
            List<Identity> got = storage.getConnections(identity, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildRelationships(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getConnections(final Identity identity) throws RelationshipStorageException {
    return storage.getConnections(identity);
  }

  /**
   * {@inheritDoc}
   */
  public int getConnectionsCount(final Identity identity) throws RelationshipStorageException {

    //
    IdentityKey iKey = new IdentityKey(identity);
    RelationshipCountKey<IdentityKey> key = new RelationshipCountKey<IdentityKey>(iKey, RelationshipType.CONNECTION);

    //
    return relationshipsCount.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getConnectionsCount(identity));
          }
        },
        key)
        .build();

  }

  public List<Identity> getConnectionsByFilter(final Identity existingIdentity,
                                               final ProfileFilter profileFilter,
                                               final long offset,
                                               final long limit) throws RelationshipStorageException {

    //
    IdentityFilterKey key = new IdentityFilterKey(existingIdentity.getProviderId(), existingIdentity.getRemoteId(),
                                                  profileFilter);
    ListRelationshipsKey<IdentityFilterKey> listKey =
        new ListRelationshipsKey<IdentityFilterKey>(key, RelationshipType.CONNECTION_WITH_FILTER, offset, limit);

    //
    ListIdentitiesData keys = relationshipsCache.get(
        new ServiceContext<ListIdentitiesData>() {
          public ListIdentitiesData execute() {
            List<Identity> got = storage.getConnectionsByFilter(existingIdentity, profileFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildRelationships(keys);

  }

  public List<Identity> getIncomingByFilter(final Identity existingIdentity,
                                            final ProfileFilter profileFilter,
                                            final long offset,
                                            final long limit) throws RelationshipStorageException {

    //
    IdentityFilterKey key = new IdentityFilterKey(existingIdentity.getProviderId(), existingIdentity.getRemoteId(), profileFilter);
    ListRelationshipsKey<IdentityFilterKey> listKey =
        new ListRelationshipsKey<IdentityFilterKey>(key, RelationshipType.INCOMMING_WITH_FILTER, offset, limit);

    //
    ListIdentitiesData keys = relationshipsCache.get(
        new ServiceContext<ListIdentitiesData>() {
          public ListIdentitiesData execute() {
            List<Identity> got = storage.getIncomingByFilter(existingIdentity, profileFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildRelationships(keys);

  }

  public List<Identity> getOutgoingByFilter(final Identity existingIdentity,
                                            final ProfileFilter profileFilter,
                                            final long offset,
                                            final long limit) throws RelationshipStorageException {

    //
    IdentityFilterKey key = new IdentityFilterKey(existingIdentity.getProviderId(), existingIdentity.getRemoteId(), profileFilter);
    ListRelationshipsKey<IdentityFilterKey> listKey =
        new ListRelationshipsKey<IdentityFilterKey>(key, RelationshipType.OUTGOING_WITH_FILTER, offset, limit);

    //
    ListIdentitiesData keys = relationshipsCache.get(
        new ServiceContext<ListIdentitiesData>() {
          public ListIdentitiesData execute() {
            List<Identity> got = storage.getOutgoingByFilter(existingIdentity, profileFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildRelationships(keys);

  }

  public int getConnectionsCountByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter) throws RelationshipStorageException {

    //
    IdentityFilterKey iKey = new IdentityFilterKey(existingIdentity.getProviderId(), existingIdentity.getRemoteId(),
                                                   profileFilter);
    RelationshipCountKey<IdentityFilterKey> key =
        new RelationshipCountKey<IdentityFilterKey>(iKey, RelationshipType.CONNECTION_WITH_FILTER);

    //
    return relationshipsCount.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getConnectionsCountByFilter(existingIdentity, profileFilter));
          }
        },
        key).build();

  }

  public int getIncomingCountByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter) throws RelationshipStorageException {

    //
    IdentityFilterKey iKey = new IdentityFilterKey(existingIdentity.getProviderId(), existingIdentity.getRemoteId(), profileFilter);
    RelationshipCountKey<IdentityFilterKey> key =
        new RelationshipCountKey<IdentityFilterKey>(iKey, RelationshipType.INCOMMING_WITH_FILTER);

    //
    return relationshipsCount.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getIncomingCountByFilter(existingIdentity, profileFilter));
          }
        },
        key).build();

  }

  public int getOutgoingCountByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter) throws RelationshipStorageException {

    //
    IdentityFilterKey iKey = new IdentityFilterKey(existingIdentity.getProviderId(), existingIdentity.getRemoteId(), profileFilter);
    RelationshipCountKey<IdentityFilterKey> key =
        new RelationshipCountKey<IdentityFilterKey>(iKey, RelationshipType.OUTGOING_WITH_FILTER);

    //
    return relationshipsCount.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getOutgoingCountByFilter(existingIdentity, profileFilter));
          }
        },
        key).build();

  }

  public Map<Identity, Integer> getSuggestions(final Identity identity, final int maxConnections, 
                                                final int maxConnectionsToLoad, 
                                                final int maxSuggestions) 
                                                         throws RelationshipStorageException {
    //
    IdentityKey key = new IdentityKey(identity);
    SuggestionKey<IdentityKey> suggestKey = new SuggestionKey<IdentityKey>(key, maxConnections, 
                                                                           maxConnectionsToLoad, 
                                                                           maxSuggestions);
    
    SuggestionsData keys = suggestionCache.get(
        new ServiceContext<SuggestionsData>() {
          public SuggestionsData execute() {
            Map<Identity, Integer> got = storage.getSuggestions(identity, maxConnections, 
                                                                maxConnectionsToLoad, 
                                                                maxSuggestions);
            return buildIdMap(got);
          }
        },
        suggestKey);

    //
    return buildSuggestions(keys);
  }

  @Override
  public List<Identity> getLastConnections(final Identity identity, final int limit) throws RelationshipStorageException {
    //
    IdentityKey key = new IdentityKey(identity);
    ListRelationshipsKey<IdentityKey> listKey =
        new ListRelationshipsKey<IdentityKey>(key, RelationshipType.LAST_CONNECTIONS, 0, limit);
    ListIdentitiesData keys = relationshipsCache.get(
        new ServiceContext<ListIdentitiesData>() {
          public ListIdentitiesData execute() {
            List<Identity> got = storage.getLastConnections(identity, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildRelationships(keys);
  }
  
}
