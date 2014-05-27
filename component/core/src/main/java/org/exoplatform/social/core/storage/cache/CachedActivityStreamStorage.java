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

import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.impl.ActivityStreamStorageImpl;
import org.exoplatform.social.core.storage.impl.ActivityStreamStorageImpl.ActivityRefType;

public class CachedActivityStreamStorage implements ActivityStreamStorage {
  
  private final ActivityStreamStorageImpl storage;
  private final SocialStorageCacheService cacheService;
  
  public CachedActivityStreamStorage(final ActivityStreamStorageImpl storage, final SocialStorageCacheService cacheService) {
    this.storage = storage;
    this.cacheService = cacheService;
  }

  @Override
  public void save(ProcessContext ctx) {
    this.storage.save(ctx);
  }
  
  @Override
  public void savePoster(ProcessContext ctx) {
    this.storage.savePoster(ctx);
  }

  @Override
  public void delete(String activityId) {
    this.storage.delete(activityId);
  }
  
  @Override
  public void unLike(Identity removedLike, ExoSocialActivity activity) {
    this.storage.unLike(removedLike, activity);
  }
  
  @Override
  public void like(Identity liker, ExoSocialActivity activity) {
    this.storage.like(liker, activity);
  }

  @Override
  public void update(ProcessContext ctx) {
    this.storage.update(ctx);
  }
  
  @Override
  public void updateCommenter(ProcessContext ctx) {
    this.storage.updateCommenter(ctx);
  }
  
  @Override
  public void deleteComment(ProcessContext ctx) {
    this.storage.deleteComment(ctx);
  }
  
  @Override
  public void addSpaceMember(ProcessContext ctx) {
    this.storage.addSpaceMember(ctx);
  }
  
  @Override
  public void removeSpaceMember(ProcessContext ctx) {
    this.storage.removeSpaceMember(ctx);
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
  public List<ExoSocialActivity> getMySpaces(Identity owner, int offset, int limit) {
    return storage.getMySpaces(owner, offset, limit);
  }

  @Override
  public int getNumberOfMySpaces(Identity owner) {
    return storage.getNumberOfMySpaces(owner);
  }
  
  @Override
  public List<ExoSocialActivity> getSpaceStream(Identity owner, int offset, int limit) {
    return storage.getSpaceStream(owner, offset, limit);
  }

  @Override
  public int getNumberOfSpaceStream(Identity owner) {
    return storage.getNumberOfSpaceStream(owner);
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
  public List<ExoSocialActivity> getViewerActivities(Identity owner, int offset, int limit) {
    return this.storage.getViewerActivities(owner, offset, limit);
  }

  @Override
  public void deleteConnect(Identity sender, Identity receiver) {
    this.storage.deleteConnect(sender, receiver);
  }
  
  @Override
  public void createActivityRef(Identity owner,
                                List<ExoSocialActivity> activities,
                                ActivityRefType type) {
    
    this.storage.createActivityRef(owner, activities, type);
    
  }

  @Override
  public void createFeedActivityRef(Identity owner, List<ExoSocialActivity> activities) {
    this.storage.createFeedActivityRef(owner, activities);
  }

  @Override
  public void createConnectionsActivityRef(Identity owner, List<ExoSocialActivity> activities) {
    this.storage.createConnectionsActivityRef(owner, activities);
  }

  @Override
  public void createMySpacesActivityRef(Identity owner, List<ExoSocialActivity> activities) {
    this.storage.createMySpacesActivityRef(owner, activities);
  }
  
  @Override
  public void createSpaceActivityRef(Identity owner, List<ExoSocialActivity> activities) {
    this.storage.createSpaceActivityRef(owner, activities);
  }

  @Override
  public void createMyActivitiesActivityRef(Identity owner, List<ExoSocialActivity> activities) {
    this.storage.createMyActivitiesActivityRef(owner, activities);
  }
  
  @Override
  public boolean hasSizeOfConnections(Identity owner) {
    return this.storage.hasSizeOfConnections(owner);
  }
  
  @Override
  public boolean hasSizeOfFeed(Identity owner) {
    return this.storage.hasSizeOfFeed(owner);
  }
  
  @Override
  public boolean hasSizeOfMyActivities(Identity owner) {
    return this.storage.hasSizeOfMyActivities(owner);
  }
  
  @Override
  public boolean hasSizeOfMySpaces(Identity owner) {
    return this.storage.hasSizeOfMySpaces(owner);
  }
  
  @Override
  public boolean hasSizeOfSpaceStream(Identity owner) {
    return this.storage.hasSizeOfSpaceStream(owner);
  }
  
  @Override
  public void migrateStreamSize(Identity owner, int size, ActivityRefType type) {
    this.storage.migrateStreamSize(owner, size, type);
    
  }

  @Override
  public void updateHidable(ProcessContext ctx) {
    storage.updateHidable(ctx);
  }

 
}
