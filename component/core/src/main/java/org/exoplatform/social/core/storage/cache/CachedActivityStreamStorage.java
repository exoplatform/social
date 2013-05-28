/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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

import java.util.List;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.impl.ActivityStreamStorageImpl;

public class CachedActivityStreamStorage implements ActivityStreamStorage {
  
  private final ActivityStreamStorageImpl storage;
  
  public CachedActivityStreamStorage(final ActivityStreamStorageImpl storage, final SocialStorageCacheService cacheService) {
    this.storage = storage;
  }

  @Override
  public void save(Identity owner, ExoSocialActivity activity) {
    this.storage.save(owner, activity);
  }

  @Override
  public void delete(Identity owner, ExoSocialActivity activity) {
    
  }

  @Override
  public void update(Identity owners) {
    
  }

  @Override
  public List<ExoSocialActivity> getFeed(Identity owner, int offset, int limit) {
    return storage.getFeed(owner, offset, limit);
  }

  @Override
  public void getNumberOfFeed(Identity owners) {
    
  }

  @Override
  public List<ExoSocialActivity> getConnections(Identity owners, int offset, int limit) {
    return null;
  }

  @Override
  public int getNumberOfConnections(Identity owners) {
    return 0;
  }

  @Override
  public List<ExoSocialActivity> getSpaces(Identity owners, int offset, int limit) {
    return null;
  }

  @Override
  public int getNumberOfSpaces(Identity owners) {
    return 0;
  }

  @Override
  public List<ExoSocialActivity> getMyActivities(Identity owners, int offset, int limit) {
    return null;
  }

  @Override
  public int getNumberOfMyActivities(Identity owners) {
    return 0;
  }

  @Override
  public void connect(Identity sender, Identity receiver) {
    this.storage.connect(sender, receiver);
  }

  @Override
  public void deleteConnect(Identity sender, Identity receiver) {
    this.storage.deleteConnect(sender, receiver);
  }

}
