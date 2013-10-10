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
package org.exoplatform.social.core.updater;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.impl.ActivityStreamStorageImpl.ActivityRefType;

public abstract class StreamUpgradeProcessor {
  /**
   * Retrieves the activities list from JCR
   * @param owner
   * @param offset
   * @param limit
   * @return
   */
  abstract List<ExoSocialActivity> load(Identity owner, int offset, int limit);
  
  /**
   * Gets total of activities for given Identity
   * @param owner
   * @return
   */
  abstract int size(Identity owner);
  /**
   * Fetches all of activities and migrate 
   */
  public void upgrade() {
    
    int offset = 0;
    int limit = 100;
    int totalSize = size(this.owner);
    
    limit = Math.min(limit, totalSize);
    int loaded = upgradeRange(offset, limit);
    
    if (limit != totalSize) {
      while (loaded == 100) {
        offset += limit;
        
        //prevent to over totalSize
        if (offset + limit > totalSize) {
          limit = totalSize - offset;
        }
        
        //
        loaded = upgradeRange(offset, limit);
      }
    }

  }
  
  /**
   * Fetches all of activities with offset and limit and migrate
   *  
   * @param offset
   * @param limit
   */
  public void upgrade(int offset, int limit) {
    if (limit == -1) {
      upgrade();
    } else {
      upgradeRange(offset, limit);
      upgradeStreamSize();
    }
    
  }
  
  protected IdentityStorage getIdentityStorage() {
    if (this.identityStorage == null) {
       this.identityStorage = (IdentityStorage) PortalContainer.getInstance().getComponentInstanceOfType(IdentityStorage.class);
    }
    
    return identityStorage;
  }
  
  protected ActivityStorage getActivityStorage() {
    if (this.activityStorage == null) {
       this.activityStorage = (ActivityStorage) PortalContainer.getInstance().getComponentInstanceOfType(ActivityStorage.class);
    }
    
    return activityStorage;
  }
  
  protected ActivityStreamStorage getStreamStorage() {
    if (this.streamStorage == null) {
       this.streamStorage = (ActivityStreamStorage) PortalContainer.getInstance().getComponentInstanceOfType(ActivityStreamStorage.class);
    }
    
    return streamStorage;
  }
  
  
  public static StreamUpgradeProcessor feed(Identity owner) {
    return new StreamUpgradeProcessor(owner, ActivityRefType.FEED) {

      @Override
      List<ExoSocialActivity> load(Identity owner, int offset, int limit) {
        return getActivityStorage().getActivityFeedForUpgrade(owner, offset, limit);
      }

      @Override
      int size(Identity owner) {
        return getActivityStorage().getNumberOfActivitesOnActivityFeedForUpgrade(owner);
      }
      
    };
  }
  
  public static StreamUpgradeProcessor connection(Identity owner) {
    return new StreamUpgradeProcessor(owner, ActivityRefType.CONNECTION) {

      @Override
      List<ExoSocialActivity> load(Identity owner, int offset, int limit) {
        return getActivityStorage().getActivityFeedForUpgrade(owner, offset, limit);
      }
      
      @Override
      int size(Identity owner) {
        return getActivityStorage().getNumberOfActivitiesOfConnectionsForUpgrade(owner);
      }
      
    };
  }
  
  public static StreamUpgradeProcessor user(Identity owner) {
    return new StreamUpgradeProcessor(owner, ActivityRefType.MY_ACTIVITIES) {

      @Override
      List<ExoSocialActivity> load(Identity owner, int offset, int limit) {
        return getActivityStorage().getUserActivitiesForUpgrade(owner, offset, limit);
      }
      
      @Override
      int size(Identity owner) {
        return getActivityStorage().getNumberOfUserActivitiesForUpgrade(owner);
      }
      
    };
  }
  
  public static StreamUpgradeProcessor myspaces(Identity owner) {
    return new StreamUpgradeProcessor(owner, ActivityRefType.MY_SPACES) {

      @Override
      List<ExoSocialActivity> load(Identity owner, int offset, int limit) {
        return getActivityStorage().getUserSpacesActivitiesForUpgrade(owner, offset, limit);
      }
      
      @Override
      int size(Identity owner) {
        return getActivityStorage().getNumberOfUserSpacesActivitiesForUpgrade(owner);
      }
      
    };
  }
  
  public static StreamUpgradeProcessor space(Identity owner) {
    return new StreamUpgradeProcessor(owner, ActivityRefType.SPACE_STREAM) {

      @Override
      List<ExoSocialActivity> load(Identity owner, int offset, int limit) {
        return getActivityStorage().getSpaceActivitiesForUpgrade(owner, offset, limit);
      }
      
      @Override
      int size(Identity owner) {
        return getActivityStorage().getNumberOfSpaceActivitiesForUpgrade(owner);
      }
      
    };
  }
  
  private IdentityStorage identityStorage = null;
  private ActivityStorage activityStorage = null;
  private ActivityStreamStorage streamStorage = null;
  
  private final Identity owner;
  private final ActivityRefType type;
  
  public StreamUpgradeProcessor(Identity owner, ActivityRefType type) {
    this.owner = owner;
    this.type = type;
  }
  
  private int upgradeRange(int  offset, int limit) {
    List<ExoSocialActivity> got = load(this.owner, offset, limit);
    if (got.size() > 0) {
      getStreamStorage().createActivityRef(owner, got, type);
    }
    
    
    //
    return got.size();
  }
  
  private int upgradeStreamSize() {
    int got = size(this.owner);
    if (got > 0) {
      getStreamStorage().migrateStreamSize(owner, got, this.type);
    }
    //
    return got;
  }
}
