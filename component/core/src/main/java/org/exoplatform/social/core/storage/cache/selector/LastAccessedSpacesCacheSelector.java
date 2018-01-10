package org.exoplatform.social.core.storage.cache.selector;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;
import org.exoplatform.social.core.storage.cache.model.data.ListSpacesData;
import org.exoplatform.social.core.storage.cache.model.key.ListSpacesKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceType;

/**
 * Cache selector for last accessed spaces.
 * It select all cache entries for the given userId and for space type LATEST_ACCESSED or VISITED.
 */
public class LastAccessedSpacesCacheSelector extends ScopeCacheSelector<ListSpacesKey, ListSpacesData> {

  private String                    remoteId;

  private Space                     space;

  private SocialStorageCacheService cacheService;

  private boolean                   hasClearedCacheEntries = false;

  public LastAccessedSpacesCacheSelector(String remoteId, Space space, SocialStorageCacheService cacheService) {
    this.remoteId = remoteId;
    this.space = space;
    this.cacheService = cacheService;
  }

  @Override
  public boolean select(ListSpacesKey listSpacesKey, ObjectCacheInfo<? extends ListSpacesData> objectCacheInfo) {
    if(listSpacesKey == null) {
      return false;
    }

    SpaceFilterKey spaceFilterKey = listSpacesKey.getKey();
    if(spaceFilterKey == null) {
      return false;
    }

    return remoteId.equals(spaceFilterKey.getUserId())
            && (SpaceType.LATEST_ACCESSED.equals(spaceFilterKey.getType())
            || SpaceType.VISITED.equals(spaceFilterKey.getType()));
  }

  @Override
  public void onSelect(ExoCache<? extends ListSpacesKey, ? extends ListSpacesData> exoCache,
                       ListSpacesKey listSpacesKey,
                       ObjectCacheInfo<? extends ListSpacesData> objectCacheInfo) throws Exception {
    if(objectCacheInfo != null && objectCacheInfo.get() != null) {
      ListSpacesData listSpacesData = objectCacheInfo.get();
      if (listSpacesData.getIds() != null && !listSpacesData.getIds().isEmpty()
              && (SpaceType.LATEST_ACCESSED.equals(listSpacesKey.getKey().getType()) && !listSpacesData.getIds().get(0).getId().equals(space.getId())
              || SpaceType.VISITED.equals(listSpacesKey.getKey().getType()))) {
        exoCache.remove(listSpacesKey);
        hasClearedCacheEntries = true;
        cacheService.getSpacesCountCache().remove(listSpacesKey);
      }
    }
  }

  public boolean isHasClearedCacheEntries() {
    return hasClearedCacheEntries;
  }
}
