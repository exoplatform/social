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
package org.exoplatform.social.core.storage.synchronization;

import java.util.List;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.impl.ActivityStreamStorageImpl;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;

public class SynchronizedActivityStreamStorage extends ActivityStreamStorageImpl {

  public SynchronizedActivityStreamStorage(IdentityStorageImpl identityStorage,
                                           RelationshipStorage relationshipStorage) {
    
    super(identityStorage, relationshipStorage);
  }
  
  @Override
  public void save(Identity owner, ExoSocialActivity activity) {
    
    boolean created = startSynchronization();
    try {
      super.save(owner, activity);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public void delete(String activityId) {
    boolean created = startSynchronization();
    try {
      super.delete(activityId);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public List<ExoSocialActivity> getFeed(Identity owner, int offset, int limit) {
    
    boolean created = startSynchronization();
    try {
      return super.getFeed(owner, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public void connect(Identity sender, Identity receiver) {
    boolean created = startSynchronization();
    try {
      super.connect(sender, receiver);
    }
    finally {
      stopSynchronization(created);
    }
    
  }
  
  @Override
  public void deleteConnect(Identity sender, Identity receiver) {
    
    boolean created = startSynchronization();
    try {
      super.deleteConnect(sender, receiver);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public List<ExoSocialActivity> getConnections(Identity owner, int offset, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getConnections(owner, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
    
  }
  
  @Override
  public List<ExoSocialActivity> getMyActivities(Identity owner, int offset, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getMyActivities(owner, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public List<ExoSocialActivity> getSpaces(Identity owner, int offset, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getSpaces(owner, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }

}
