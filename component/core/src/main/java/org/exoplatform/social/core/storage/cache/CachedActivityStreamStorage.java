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
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.impl.ActivityStreamStorageImpl;

public class CachedActivityStreamStorage implements ActivityStreamStorage {
  
  private final ActivityStreamStorageImpl storage;
  private final SocialStorageCacheService cacheService;
  
  public CachedActivityStreamStorage(final ActivityStreamStorageImpl storage, final SocialStorageCacheService cacheService) {
    this.storage = storage;
    this.cacheService = cacheService;
  }

  @Override
  public void save(Identity owner, ExoSocialActivity activity) {
    this.storage.save(owner, activity);
  }

  @Override
  public void delete(String activityId) {
    this.storage.delete(activityId);
  }

  @Override
  public void update(Identity owner) {
    
  }

  @Override
  public List<ExoSocialActivity> getFeed(Identity owner, int offset, int limit) {
    return storage.getFeed(owner, offset, limit);
  }

  @Override
  public int getNumberOfFeed(Identity owner) {
    return storage.getNumberOfFeed(owner);
  }

  @Override
  public List<ExoSocialActivity> getConnections(Identity owner, int offset, int limit) {
    return storage.getConnections(owner, offset, limit);
  }

  @Override
  public int getNumberOfConnections(Identity owner) {
    return storage.getNumberOfConnections(owner);
  }

  @Override
  public List<ExoSocialActivity> getSpaces(Identity owner, int offset, int limit) {
    return storage.getSpaces(owner, offset, limit);
  }

  @Override
  public int getNumberOfSpaces(Identity owner) {
    return storage.getNumberOfSpaces(owner);
  }

  @Override
  public List<ExoSocialActivity> getMyActivities(Identity owner, int offset, int limit) {
    return storage.getMyActivities(owner, offset, limit);
  }

  @Override
  public int getNumberOfMyActivities(Identity owner) {
    return storage.getNumberOfMyActivities(owner);
  }

  @Override
  public void connect(Identity sender, Identity receiver) {
    this.storage.connect(sender, receiver);
  }

  @Override
  public void deleteConnect(Identity sender, Identity receiver) {
    this.storage.deleteConnect(sender, receiver);
  }

  @Override
  public void addSpaceMember(Space space, Identity member) {
    this.storage.addSpaceMember(space, member);
  }

  @Override
  public void removeSpaceMember(Space space, Identity member) {
    this.storage.removeSpaceMember(space, member);
  }

}
