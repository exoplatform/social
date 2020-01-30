package org.exoplatform.social.core.storage.cache.selector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;
import org.exoplatform.social.core.storage.cache.model.data.ListSpacesData;
import org.exoplatform.social.core.storage.cache.model.key.*;

/**
 * Cache selector for last accessed spaces.
 * It select all cache entries for the given userId and for space type LATEST_ACCESSED or VISITED.
 */
public class LastAccessedSpacesCacheSelector extends CacheSelector<ListSpacesKey, ListSpacesData> {

  private String                    remoteId;

  private Space                     space;

  private SocialStorageCacheService cacheService;

  private boolean                   updateStore = true;

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
    if (objectCacheInfo != null && objectCacheInfo.get() != null) {
      ListSpacesData listSpacesData = objectCacheInfo.get();
      List<SpaceKey> ids = listSpacesData.getIds();
      if (ids != null && !ids.isEmpty()) {
        if (ids.get(0).getId().equals(space.getId())) {
          updateStore = false;
          return;
        } else if (StringUtils.isBlank(listSpacesKey.getKey().getAppId()) && listSpacesKey.getOffset() == 0
            && SpaceType.LATEST_ACCESSED.equals(listSpacesKey.getKey().getType())) {
          SpaceKey spaceKey = new SpaceKey(space.getId());
          ids = new ArrayList<>(ids);
          if (ids.contains(spaceKey)) {
            ids.remove(spaceKey);
            ids.add(0, spaceKey);
            listSpacesData.setIds(ids);
            // Update cache after value change because ISPN returns a clone of
            // object And not the real cached object
            ((ExoCache<ListSpacesKey, ListSpacesData>) exoCache).put(listSpacesKey, listSpacesData);
            // Cache enry updated, so no need to clear it
            return;
          } else if (ids.size() == listSpacesKey.getLimit()) {
            ids.remove(ids.size() - 1);
            ids.add(0, spaceKey);
            listSpacesData.setIds(ids);
            cacheService.getSpacesCountCache().remove(listSpacesKey);
            ((ExoCache<ListSpacesKey, ListSpacesData>) exoCache).put(listSpacesKey, listSpacesData);
            // Cache enry updated, so no need to clear it
            return;
          } else {
            ids.add(0, spaceKey);
            listSpacesData.setIds(ids);
            cacheService.getSpacesCountCache().remove(listSpacesKey);
            ((ExoCache<ListSpacesKey, ListSpacesData>) exoCache).put(listSpacesKey, listSpacesData);
            // Cache enry updated, so no need to clear it
            return;
          }
        }
      }
      exoCache.remove(listSpacesKey);
      cacheService.getSpacesCountCache().remove(listSpacesKey);
    }
  }

  public boolean isUpdateStore() {
    return updateStore;
  }
}
