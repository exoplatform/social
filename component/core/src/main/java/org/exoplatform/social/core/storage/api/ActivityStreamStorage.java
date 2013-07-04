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
package org.exoplatform.social.core.storage.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.impl.ActivityStreamStorageImpl.ActivityRefType;


public interface ActivityStreamStorage {
  
  public static class UpdateContext {
    private List<Identity> added;
    private List<Identity> removed;

    public UpdateContext(List<Identity> added, List<Identity> removed) {
      this.added = added;
      this.removed = removed;
    }
    
    public UpdateContext(Identity added, Identity removed) {
      if (added != null) {
        this.added = new CopyOnWriteArrayList<Identity>();
        this.added.add(added);
      }
      
      //
      if (removed != null) {
        this.removed = new CopyOnWriteArrayList<Identity>();
        this.removed.add(removed);
      }
    }

    public List<Identity> getAdded() {
      return added == null ? new ArrayList<Identity>() : added;
    }

    public List<Identity> getRemoved() {
      return removed == null ? new ArrayList<Identity>() : removed;
    }
  }
  /**
   * Saves the activity into a streams.
   * 
   * @param ctx 
   * 1. ctx.getIdentity()  
   * Owner is User Identity: Adds given {@link org.exoplatform.social.core.activity.model.ExoSocialActivity}
   * to Connections's Feed and Connections streams.
   * 
   * Owner is Space Identity: Adds given {@link org.exoplatform.social.core.activity.model.ExoSocialActivity}
   * to Space's Member's Feed and My Spaces streams.
   * 2. ctx.getActivity() given activity to records the stream
   * 
   * @since 4.0.2, 4.1.0
   */
  public void save(ProcessContext ctx);
  

  public void delete(String activityId);
  
  public void unLike(Identity removedLike, ExoSocialActivity activity);
  
  public void connect(Identity sender, Identity receiver);
  
  public void deleteConnect(Identity sender, Identity receiver);
  
  /**
   * Updates the activity when has actions such as like, mentions, unlike, or add comment
   * 
   * @param ctx 
   * 1. ctx.getActivity() given activity to records the streams
   * 2. ctx.getOldUpdated() oldUpdated of given the Activity. 
   * 3. ctx.getMentioner() mentioners of given the Activity.
   * 4. ctx.getCommenters() commenters of given the Activity. 
   * @since 4.0.2, 4.1.0
   */
  public void update(ProcessContext ctx);
  
  /**
   * Deletes the activity ref when has actions such as delete comment, check has any mentioner and commenter were removed
   * @param ctx
   * 1. ctx.getMentioner() removed mentioners of given the Activity.
   * 2. ctx.getCommenters() removed commenters of given the Activity. 
   * @since 4.0.2, 4.1.0
   */
  void deleteComment(ProcessContext ctx);
  
  /**
   * Adds new member for existing space
   * 
   * @param ctx 
   * 1. ctx.getIdentity() the member is added to Space as member
   * 2. ctx.getIdentitySpace() Space Identity
   * 
   * @since 4.0.2, 4.1.0
   */
  public void addSpaceMember(ProcessContext ctx);
  
  /**
   * Removes the member from existing space
   * 
   * @param ctx 
   * 1. ctx.getIdentity() the member is removed from the Space.
   * 2. ctx.getIdentitySpace() Space Identity
   * 
   * @since 4.0.2, 4.1.0
   */
  public void removeSpaceMember(ProcessContext ctx);
  
  public List<ExoSocialActivity> getFeed(Identity owner, int offset, int limit);
  
  public int getNumberOfFeed(Identity owner);
  
  public boolean hasSizeOfFeed(Identity owner);
  
  public List<ExoSocialActivity> getConnections(Identity owner, int offset, int limit);
  
  public int getNumberOfConnections(Identity owner);
  
  public boolean hasSizeOfConnections(Identity owner);
  
  public List<ExoSocialActivity> getMySpaces(Identity owner, int offset, int limit);
  
  public int getNumberOfMySpaces(Identity owner);
  
  public boolean hasSizeOfMySpaces(Identity owner);
  
  public List<ExoSocialActivity> getSpaceStream(Identity owner, int offset, int limit);
  
  public int getNumberOfSpaceStream(Identity owner);
  
  public boolean hasSizeOfSpaceStream(Identity owner);
  
  public List<ExoSocialActivity> getMyActivities(Identity owner, int offset, int limit);
  
  public int getNumberOfMyActivities(Identity owner);
  
  public boolean hasSizeOfMyActivities(Identity owner);
  
  void createActivityRef(Identity owner, List<ExoSocialActivity> activities, ActivityRefType type);
  
  void createFeedActivityRef(Identity owner, List<ExoSocialActivity> activities);
  
  void createConnectionsActivityRef(Identity owner, List<ExoSocialActivity> activities);
  
  void createMySpacesActivityRef(Identity owner, List<ExoSocialActivity> activities);
  
  void createSpaceActivityRef(Identity owner, List<ExoSocialActivity> activities);
  
  void createMyActivitiesActivityRef(Identity owner, List<ExoSocialActivity> activities);
  
  void migrateStreamSize(Identity owner, int size, ActivityRefType type);
}
