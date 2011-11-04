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

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.SpaceStorageException;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.data.ListIdentitiesData;
import org.exoplatform.social.core.storage.cache.model.data.ListSpacesData;
import org.exoplatform.social.core.storage.cache.model.key.ListIdentitiesKey;
import org.exoplatform.social.core.storage.cache.model.key.ListSpacesKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceType;
import org.exoplatform.social.core.storage.impl.SpaceStorageImpl;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.cache.loader.ServiceContext;
import org.exoplatform.social.core.storage.cache.model.data.SpaceData;
import org.exoplatform.social.core.storage.cache.model.key.SpaceKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceRefKey;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CachedSpaceStorage implements SpaceStorage {

  private final ExoCache<SpaceKey, SpaceData> exoSpaceCache;
  private final ExoCache<SpaceRefKey, SpaceKey> exoRefSpaceCache;
  private final ExoCache<SpaceFilterKey, IntegerData> exoSpacesCountCache;
  private final ExoCache<ListSpacesKey, ListSpacesData> exoSpacesCache;
  private final ExoCache<ListIdentitiesKey, ListIdentitiesData> exoIdentitiesCache;

  private final FutureExoCache<SpaceKey, SpaceData, ServiceContext<SpaceData>> spaceCache;
  private final FutureExoCache<SpaceRefKey, SpaceKey, ServiceContext<SpaceKey>> spaceRefCache;
  private final FutureExoCache<SpaceFilterKey, IntegerData, ServiceContext<IntegerData>> spacesCountCache;
  private final FutureExoCache<ListSpacesKey, ListSpacesData, ServiceContext<ListSpacesData>> spacesCache;

  private final SpaceStorageImpl storage;

  /**
   * Build the activity list from the caches Ids.
   *
   * @param data ids
   * @return activities
   */
  private List<Space> buildSpaces(ListSpacesData data) {

    List<Space> spaces = new ArrayList<Space>();
    for (SpaceKey k : data.getIds()) {
      Space s = getSpaceById(k.getId());
      spaces.add(s);
    }
    return spaces;

  }

  /**
   * Build the ids from the space list.
   *
   * @param spaces spaces
   * @return ids
   */
  private ListSpacesData buildIds(List<Space> spaces) {

    List<SpaceKey> data = new ArrayList<SpaceKey>();
    for (Space s : spaces) {
      SpaceKey k = new SpaceKey(s.getId());
      exoSpaceCache.put(k, new SpaceData(s));
      data.add(k);
    }
    return new ListSpacesData(data);

  }

  public CachedSpaceStorage(final SpaceStorageImpl storage, final SocialStorageCacheService cacheService) {

    this.storage = storage;

    this.exoSpaceCache = cacheService.getSpaceCache();
    this.exoRefSpaceCache = cacheService.getSpaceRefCache();
    this.exoSpacesCountCache = cacheService.getSpacesCountCache();
    this.exoSpacesCache = cacheService.getSpacesCache();
    this.exoIdentitiesCache = cacheService.getIdentitiesCache();

    this.spaceCache = CacheType.SPACE.createFutureCache(exoSpaceCache);
    this.spaceRefCache = CacheType.SPACE_REF.createFutureCache(exoRefSpaceCache);
    this.spacesCountCache = CacheType.SPACES_COUNT.createFutureCache(exoSpacesCountCache);
    this.spacesCache = CacheType.SPACES.createFutureCache(exoSpacesCache);

  }

  private void cleanRef(SpaceData removed) {
    exoRefSpaceCache.remove(new SpaceRefKey(removed.getDisplayName()));
    exoRefSpaceCache.remove(new SpaceRefKey(null, removed.getPrettyName()));
    exoRefSpaceCache.remove(new SpaceRefKey(null, null, removed.getGroupId()));
    exoRefSpaceCache.remove(new SpaceRefKey(null, null, null, removed.getUrl()));
  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByDisplayName(final String spaceDisplayName) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(spaceDisplayName);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = storage.getSpaceByDisplayName(spaceDisplayName);
            if (space != null) {
              SpaceKey key = new SpaceKey(space.getId());
              exoSpaceCache.put(key, new SpaceData(space));
              return key;
            }
            else {
              return null;
            }
          }
        },
        refKey);

    //
    if (key != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }

  }

  /**
   * {@inheritDoc}
   */
  public void saveSpace(final Space space, final boolean isNew) throws SpaceStorageException {

    //
    storage.saveSpace(space, isNew);

    //
    SpaceData removed = exoSpaceCache.remove(new SpaceKey(space.getId()));
    exoSpacesCountCache.clearCache();
    exoSpacesCache.clearCache();
    exoIdentitiesCache.clearCache();
    if (removed != null) {
      cleanRef(removed);
    }

  }

  /**
   * {@inheritDoc}
   */
  public void deleteSpace(final String id) throws SpaceStorageException {

    //
    storage.deleteSpace(id);

    //
    SpaceData removed = exoSpaceCache.remove(new SpaceKey(id));
    exoSpacesCountCache.clearCache();
    exoSpacesCache.clearCache();
    if (removed != null) {
      cleanRef(removed);
    }

  }

  /**
   * {@inheritDoc}
   */
  public int getMemberSpacesCount(final String userId) throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, null);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getMemberSpacesCount(userId));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getMemberSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.MEMBER);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getMemberSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getMemberSpaces(final String userId) throws SpaceStorageException {
    return storage.getMemberSpaces(userId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getMemberSpaces(final String userId, final long offset, final long limit)
      throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.MEMBER);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getMemberSpaces(userId, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getMemberSpacesByFilter(
      final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.MEMBER);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getMemberSpacesByFilter(userId, spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getPendingSpacesCount(final String userId) throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.PENDING);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getPendingSpacesCount(userId));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getPendingSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.PENDING);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getPendingSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getPendingSpaces(final String userId) throws SpaceStorageException {
    return storage.getPendingSpaces(userId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getPendingSpaces(final String userId, final long offset, final long limit)
      throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.PENDING);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getPendingSpaces(userId, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getPendingSpacesByFilter(
      final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.PENDING);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getPendingSpacesByFilter(userId, spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);
    
  }

  /**
   * {@inheritDoc}
   */
  public int getInvitedSpacesCount(final String userId) throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.INVITED);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getInvitedSpacesCount(userId));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getInvitedSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.INVITED);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getInvitedSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getInvitedSpaces(final String userId) throws SpaceStorageException {
    return storage.getInvitedSpaces(userId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getInvitedSpaces(final String userId, final long offset, final long limit)
      throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.INVITED);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getInvitedSpaces(userId, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getInvitedSpacesByFilter(
      final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.INVITED);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getInvitedSpacesByFilter(userId, spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);
    
  }

  /**
   * {@inheritDoc}
   */
  public int getPublicSpacesCount(final String userId) throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.PUBLIC);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getPublicSpacesCount(userId));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getPublicSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.PUBLIC);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getPublicSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getPublicSpacesByFilter(
      final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.PUBLIC);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getPublicSpacesByFilter(userId, spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getPublicSpaces(final String userId) throws SpaceStorageException {
    return storage.getPublicSpaces(userId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getPublicSpaces(final String userId, final long offset, final long limit)
      throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.PUBLIC);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getPublicSpaces(userId, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getAccessibleSpacesCount(final String userId) throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.ACCESSIBLE);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getAccessibleSpacesCount(userId));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getAccessibleSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.ACCESSIBLE);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getAccessibleSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();
    
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getAccessibleSpaces(final String userId) throws SpaceStorageException {
    return storage.getAccessibleSpaces(userId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getAccessibleSpaces(final String userId, final long offset, final long limit)
      throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.ACCESSIBLE);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getAccessibleSpaces(userId, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getAccessibleSpacesByFilter(
      final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.ACCESSIBLE);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getAccessibleSpacesByFilter(userId, spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getEditableSpacesCount(final String userId) throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.EDITABLE);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getEditableSpacesCount(userId));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public int getEditableSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.EDITABLE);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getEditableSpacesByFilterCount(userId, spaceFilter));
          }
        },
        key)
        .build();
    
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getEditableSpaces(final String userId) throws SpaceStorageException {
    return storage.getEditableSpaces(userId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getEditableSpaces(final String userId, final long offset, final long limit)
      throws SpaceStorageException {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, null, SpaceType.EDITABLE);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getEditableSpaces(userId, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getEditableSpacesByFilter(
      final String userId, final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(userId, spaceFilter, SpaceType.EDITABLE);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getEditableSpacesByFilter(userId, spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public int getAllSpacesCount() throws SpaceStorageException {
    return storage.getAllSpacesCount();
  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getAllSpaces() throws SpaceStorageException {
    return storage.getAllSpaces();
  }

  /**
   * {@inheritDoc}
   */
  public int getAllSpacesByFilterCount(final SpaceFilter spaceFilter) {

    //
    SpaceFilterKey key = new SpaceFilterKey(null, spaceFilter, null);

    //
    return spacesCountCache.get(
        new ServiceContext<IntegerData>() {
          public IntegerData execute() {
            return new IntegerData(storage.getAllSpacesByFilterCount(spaceFilter));
          }
        },
        key)
        .build();

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getSpaces(final long offset, final long limit) throws SpaceStorageException {

    //
    ListSpacesKey listKey = new ListSpacesKey(null, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getSpaces(offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public List<Space> getSpacesByFilter(final SpaceFilter spaceFilter, final long offset, final long limit) {

    //
    SpaceFilterKey key = new SpaceFilterKey(null, spaceFilter, null);
    ListSpacesKey listKey = new ListSpacesKey(key, offset, limit);

    //
    ListSpacesData keys = spacesCache.get(
        new ServiceContext<ListSpacesData>() {
          public ListSpacesData execute() {
            List<Space> got = storage.getSpacesByFilter(spaceFilter, offset, limit);
            return buildIds(got);
          }
        },
        listKey);

    //
    return buildSpaces(keys);

  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceById(final String id) throws SpaceStorageException {

    //
    SpaceKey key = new SpaceKey(id);

    //
    SpaceData data = spaceCache.get(
        new ServiceContext<SpaceData>() {
          public SpaceData execute() {
            Space space = storage.getSpaceById(id);
            if (space != null) {
              return new SpaceData(space);
            }
            else {
              return null;
            }
          }
        },
        key);

    if (data != null) {
      return data.build();
    }
    else {
      return null;
    }
    
  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByPrettyName(final String spacePrettyName) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, spacePrettyName);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = storage.getSpaceByPrettyName(spacePrettyName);
            if (space != null) {
              SpaceKey key = new SpaceKey(space.getId());
              exoSpaceCache.put(key, new SpaceData(space));
              return key;
            }
            else {
              return null;
            }
          }
        },
        refKey);

    //
    if (key != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }

  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByGroupId(final String groupId) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, null, groupId);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = storage.getSpaceByGroupId(groupId);
            if (space != null) {
              SpaceKey key = new SpaceKey(space.getId());
              exoSpaceCache.put(key, new SpaceData(space));
              return key;
            }
            else {
              return null;
            }
          }
        },
        refKey);

    //
    if (key != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }

  }

  /**
   * {@inheritDoc}
   */
  public Space getSpaceByUrl(final String url) throws SpaceStorageException {

    //
    SpaceRefKey refKey = new SpaceRefKey(null, null, null, url);

    //
    SpaceKey key = spaceRefCache.get(
        new ServiceContext<SpaceKey>() {
          public SpaceKey execute() {
            Space space = storage.getSpaceByUrl(url);
            if (space != null) {
              SpaceKey key = new SpaceKey(space.getId());
              exoSpaceCache.put(key, new SpaceData(space));
              return key;
            }
            else {
              return null;
            }
          }
        },
        refKey);

    //
    if (key != null) {
      return getSpaceById(key.getId());
    }
    else {
      return null;
    }
    
  }

}

