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
   * Saves the activity into a streams only poster's stream
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
  public void savePoster(ProcessContext ctx);
  
  /**
   * Saves the activity into a streams for connection and mentions
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

  /**
   * Deletes the activity reference what relates the deleted Activity 
   * @param activityId
   * @since 4.0.2, 4.1.0
   * 
   */
  public void delete(String activityId);
  
  /**
   * Likes the activity then push the liked to Liker's streams
   *  
   * @param liker who likes the activity
   * @param activity the liked activity
   * @since 4.0.2, 4.1.0
   */
  public void like(Identity liker, ExoSocialActivity activity);
  
  /**
   * Unlikes the activity then push the liked to Liker's streams
   * 
   * @param removedLike who unlikes the activity
   * @param activity the unliked activity
   * @since 4.0.2, 4.1.0
   * 
   */
  public void unLike(Identity removedLike, ExoSocialActivity activity);
  
  /**
   * Makes relationship between sender and receiver identity
   * @param sender who send the relationship 
   * @param receiver who receiver the relationship
   * @since 4.0.2, 4.1.0
   */
  public void connect(Identity sender, Identity receiver);
  
  /**
   * Removes the relationship
   * @param sender who request the relationship
   * @param receiver who receives the relationship
   */
  public void deleteConnect(Identity sender, Identity receiver);
  
  /**
   * Updates the activity stream what relates to updated activity
   * It will run with asynchronous mode
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
   * Updates the activity references what relates to updated activity
   * It will run with asynchronous mode
   * 
   * @param ctx 
   * ctx.getActivity() given activity to record the streams
   * @since 4.0.4, 4.1.0
   */
  public void updateHidable(ProcessContext ctx);
  
  /**
   * Updates the activity when has actions such as add comment
   * 
   * @param ctx 
   * 1. ctx.getActivity() given activity to records the streams
   * 2. ctx.getOldUpdated() oldUpdated of given the Activity. 
   * 3. ctx.getMentioner() mentioners of given the Activity.
   * 4. ctx.getCommenters() commenters of given the Activity. 
   * @since 4.0.2, 4.1.0
   */
  public void updateCommenter(ProcessContext ctx);
  
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
  /**
   * Gets Feed stream by target identity
   * @param owner the owner's stream
   * @param offset
   * @param limit
   * @return
   * @since 4.0.2, 4.1.0
   */
  public List<ExoSocialActivity> getFeed(Identity owner, int offset, int limit);
  /**
   * Gets the number of owner's feed stream
   * @param owner
   * @return
   * 
   * @since 4.0.2, 4.1.0
   */
  public int getNumberOfFeed(Identity owner);
  
  /**
   * Determines whether Feed Stream's size or not 
   * @param owner
   * @return
   * 
   * @since 4.0.2, 4.1.0
   */
  public boolean hasSizeOfFeed(Identity owner);
  
  /**
   * Gets Connections stream by target identity
   * 
   * @param owner
   * @param offset
   * @param limit
   * @return
   * @since 4.0.2, 4.1.0
   */
  public List<ExoSocialActivity> getConnections(Identity owner, int offset, int limit);
  
  /**
   * The number of the activities on the owner's connection stream
   * @param owner
   * @return
   * @since 4.0.2, 4.1.0
   */
  public int getNumberOfConnections(Identity owner);
  
  /**
   * Determines whether Connection stream's size or not.
   * @param owner
   * @return
   * @since 4.0.2, 4.1.0
   */
  public boolean hasSizeOfConnections(Identity owner);
  
  /**
   * Gets the activities of My Space's stream by the owner
   * @param owner
   * @param offset
   * @param limit
   * @return
   * @since 4.0.2, 4.1.0
   */
  public List<ExoSocialActivity> getMySpaces(Identity owner, int offset, int limit);
  
  /**
   * The number of the activity on the owner's my space stream
   * 
   * @param owner
   * @return
   * @since 4.0.2, 4.1.0
   */
  public int getNumberOfMySpaces(Identity owner);
  
  /**
   * Determines whether My Space stream's size or not 
   * @param owner
   * @return
   * @since 4.0.2, 4.1.0
   */
  public boolean hasSizeOfMySpaces(Identity owner);
  
  /**
   * The activities of Space's stream by the owner
   * @param owner
   * @param offset
   * @param limit
   * @return
   * @since 4.0.2, 4.1.0
   */
  public List<ExoSocialActivity> getSpaceStream(Identity owner, int offset, int limit);
  
  /**
   * 
   * @param owner
   * @return
   * @since 4.0.2, 4.1.0
   */
  public int getNumberOfSpaceStream(Identity owner);
  
  /**
   * Determines whether the Space stream's size or not
   * 
   * @param owner
   * @return
   * @since 4.0.2, 4.1.0
   */
  public boolean hasSizeOfSpaceStream(Identity owner);
  
  /**
   *  Gets the activities of My Activities's stream by the owner
   *  
   * @param owner
   * @param offset
   * @param limit
   * @return
   * @since 4.0.2, 4.1.0
   */
  public List<ExoSocialActivity> getMyActivities(Identity owner, int offset, int limit);
  
  /**
   *  Gets the Owner Activities's stream by the viewer
   *  
   * @param owner
   * @param offset
   * @param limit
   * @return
   * @since 4.0.2, 4.1.0
   */
  public List<ExoSocialActivity> getViewerActivities(Identity owner, int offset, int limit);
  
  /**
   * 
   * @param owner
   * @return
   * @since 4.0.2, 4.1.0
   */
  public int getNumberOfMyActivities(Identity owner);
  
  /**
   * Determines whether Connection stream's size or not.
   * 
   * @param owner
   * @return
   * @since 4.0.2, 4.1.0
   */
  public boolean hasSizeOfMyActivities(Identity owner);
  
  /**
   * Creates the ActivityRef by the given ActivityRefType
   * 
   * @param owner
   * @param activities
   * @param type
   * @since 4.0.2, 4.1.0
   */
  void createActivityRef(Identity owner, List<ExoSocialActivity> activities, ActivityRefType type);
  
  /**
   * Creates the ActivityRef on the Feed stream by the given ActivityRefType
   * 
   * @param owner
   * @param activities
   * @since 4.0.2, 4.1.0
   */
  void createFeedActivityRef(Identity owner, List<ExoSocialActivity> activities);
  
  /**
   * Creates the ActivityRef on the Connections stream by the given ActivityRefType
   * 
   * @param owner
   * @param activities
   * @since 4.0.2, 4.1.0
   */
  void createConnectionsActivityRef(Identity owner, List<ExoSocialActivity> activities);
  
  /**
   * Creates the ActivityRef on the My spaces stream by the given ActivityRefType
   * 
   * @param owner
   * @param activities
   * @since 4.0.2, 4.1.0
   */
  void createMySpacesActivityRef(Identity owner, List<ExoSocialActivity> activities);
  
  /**
   * Creates the ActivityRef on the Space stream by the given ActivityRefType
   * 
   * @param owner
   * @param activities
   * @since 4.0.2, 4.1.0
   */
  void createSpaceActivityRef(Identity owner, List<ExoSocialActivity> activities);
  
  /**
   * Creates the ActivityRef on the My Activities stream by the given ActivityRefType
   * 
   * @param owner
   * @param activities
   * @since 4.0.2, 4.1.0
   */
  void createMyActivitiesActivityRef(Identity owner, List<ExoSocialActivity> activities);
  
  /**
   * Migrate the stream size
   * @param owner
   * @param size
   * @param type
   * @since 4.0.2, 4.1.0
   */
  void migrateStreamSize(Identity owner, int size, ActivityRefType type);
  
  /**
   * Updates the activity stream what related to mention activity/comment
   * It will run with asynchronous mode
   * 
   * @param ctx 
   * 1. ctx.getActivity() given activity to records the streams
   * 2. ctx.getMentioner() mentioners of given the Activity.
   */
  public void addMentioners(ProcessContext ctx);
}
