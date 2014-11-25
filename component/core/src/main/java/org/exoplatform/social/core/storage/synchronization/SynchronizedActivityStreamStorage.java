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

import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.impl.SocialServiceContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.impl.ActivityStreamStorageImpl;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;

public class SynchronizedActivityStreamStorage extends ActivityStreamStorageImpl {

  public SynchronizedActivityStreamStorage(IdentityStorageImpl identityStorage) {
    
    super(identityStorage);
  }
  
  @Override
  public void save(ProcessContext ctx) {
    if (SocialServiceContextImpl.getInstance().isAsync()) {
      super.save(ctx);
    } else {
      boolean created = startSynchronization();
      try {
        super.save(ctx);
      } finally {
        stopSynchronization(created);
      }
    }
  }
  
  
  @Override
  public void unLike(Identity removedLike, ExoSocialActivity activity) {
    
    boolean created = startSynchronization();
    try {
      super.unLike(removedLike, activity);
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
  public List<ExoSocialActivity> getMySpaces(Identity owner, int offset, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getMySpaces(owner, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public List<ExoSocialActivity> getSpaceStream(Identity owner, int offset, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getSpaceStream(owner, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public void update(ProcessContext ctx) {
    boolean created = startSynchronization();
    try {
      super.update(ctx);
    } finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void updateCommenter(ProcessContext ctx) {
    boolean created = startSynchronization();
    try {
      super.updateCommenter(ctx);
    } finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public void addMentioners(ProcessContext ctx) {
    boolean created = startSynchronization();
    try {
      super.addMentioners(ctx);
    } finally {
      stopSynchronization(created);
    }
  }
}
