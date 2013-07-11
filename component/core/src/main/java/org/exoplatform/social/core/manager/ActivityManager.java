/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.manager;

import java.util.List;

import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorageException;

/**
 * The <code>ActivityManager</code> class provides APIs to manage activities.
 * All methods to manipulate with activities, comments and likes are provided. 
 * With these kind of APIs we can store, get and update activities information.
 * To get one activity we can get by its id; To get a list of activity, result 
 * returned under <code>ListAccess</code> for lazy loading.
 * Beside that API to add processors to process activities content are also included.
 *  
 */
public interface ActivityManager {

  /**
   * Saves a new created activity to a stream. 
   * Stream owner is <code>Activity.userId</code> in case of that information has not already set.
   *
   * @param streamOwner The activity stream owner
   * @param activity The activity to be saved
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  void saveActivityNoReturn(Identity streamOwner, ExoSocialActivity activity);


   /**
   * Saves a newly created activity to the stream. Stream owner information has been set in activity.
   *
   * @param activity The activity to be saved
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void saveActivityNoReturn(ExoSocialActivity activity);

  /**
   * Saves a new activity by indicating the stream owner, the activity type and title. 
   * This is shorthand to save an activity without creating a new {@link ExoSocialActivity} instance.
   *
   * @param streamOwner The activity stream owner
   * @param type The type of activity
   * @param title the title of activity
   * @LevelAPI Platform
   */
  void saveActivity(Identity streamOwner, String type, String title);

  /**
   * Gets an activity by its id.
   *
   * @param activityId The id of activity
   * @return the activity that matched provided id
   * @LevelAPI Platform
   */
  ExoSocialActivity getActivity(String activityId);

  /**
   * Gets an activity by its comment. Comments is considered as children of activities.
   *
   * @param comment The specific comment
   * @return the activity which contains provided comment
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ExoSocialActivity getParentActivity(ExoSocialActivity comment);

  /**
   * Updates an existing activity.
   *
   * @param activity The activity to be updated
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void updateActivity(ExoSocialActivity activity);

  /**
   * Deletes a specific activity.
   *
   * @param activity The activity to be deleted
   * @LevelAPI Platform
   * @since 1.1.1
   */
  void deleteActivity(ExoSocialActivity activity);

  /**
   * Deletes an activity by its id.
   *
   * @param activityId The id of activity to be deleted
   * @LevelAPI Platform
   */
  void deleteActivity(String activityId);

  /**
   * Saves a new comment to an specific activity.
   *
   * @param activity The specific activity
   * @param newComment The new comment to be saved
   * @LevelAPI Platform
   */
  void saveComment(ExoSocialActivity activity, ExoSocialActivity newComment);

  /**
   * Gets the comments of an specific activity. 
   * Returned result with type is <code>ListAccess</code> then it can be lazy loaded.
   *
   *
   * @param activity The specific activity
   * @return list of comments that matched condition
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity activity);

  /**
   * Deletes a existing comment of an specific activity by their id.
   *
   * @param activityId The id of activity that contain comment to be delete.
   * @param commentId The id of comment to be deleted.
   * @LevelAPI Platform
   */
  void deleteComment(String activityId, String commentId);

  /**
   * Deletes an specific comment of an specific activity.
   *
   * @param activity The specific activity that contain comment to be delete.
   * @param comment The specific comment to be deleted.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void deleteComment(ExoSocialActivity activity, ExoSocialActivity comment);

  /**
   * Saves like information of an identity to an specific activity.
   *
   * @param activity The specific activity
   * @param identity The specific identity who likes this activity
   * @LevelAPI Platform
   */
  void saveLike(ExoSocialActivity activity, Identity identity);

  /**
   * Deletes a like of an identity from an specific identity.
   *
   * @param activity The specific activity
   * @param identity The specific identity
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void deleteLike(ExoSocialActivity activity, Identity identity);

  /**
   * Gets the activities posted on the provided activity stream owner.
   * Returned result with type is <code>ListAccess</code> then it can be lazy loaded.
   *
   * @param identity The provided activity stream owner
   * @return activities on the provided activity stream owner
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity identity);
  
  /**
   * Gets the activities on the provided activity stream which viewed by another.
   * Returned result with type is <code>ListAccess</code> then it can be lazy loaded.
   * <p><blockquote><pre>
   *    Example: Mary connected with Demo, signed in Demo, and then watch Mary's activity stream
   *</pre></blockquote><p>
   * 
   * @param ownerIdentity The provided activity stream owner
   * @param viewerIdentity Identity who views other stream
   * @return the activities on the provided activity stream owner
   * @LevelAPI Platform
   * @since 4.0.x
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity ownerIdentity, Identity viewerIdentity);

  /**
   * Gets the activities posted by all connections with an specific identity.
   * Returned result with type is <code>ListAccess</code> then it can be lazy loaded.
   *
   * @param identity The specific identity
   * @return the activities posted by all connections with an specific identity
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesOfConnectionsWithListAccess(Identity identity);

  /**
   * Gets the activities posted on spaces by provided id of space.
   * Returned result with type is <code>ListAccess</code> then it can be lazy loaded.
   *
   * @param spaceIdentity The specific stream owner identity
   * @return  activities belong to provided space
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesOfSpaceWithListAccess(Identity spaceIdentity);
  
  /**
   * Gets the activities posted on all space activity streams in which the provided identity joins.
   * Returned result with type is <code>ListAccess</code> then it can be lazy loaded.
   *
   * @param identity The specific user identity to get his activities on spaces
   * @return activities of provided user on spaces
   * @LevelAPI Platform
   * @since 4.0.x
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesOfUserSpacesWithListAccess(Identity identity);

  /**
   * Gets all the activities accessible by an specific identity.
   * Returned result with type is <code>ListAccess</code> then it can be lazy loaded.
   *
   * @param identity The specific identity
   * @return all activities of provided identity
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivityFeedWithListAccess(Identity identity);

  /**
   * Gets activities by an individual given poster.
   * Returned result with type is <code>ListAccess<code> then it can be lazy loaded.
   * 
   * @param poster The identity who posted activities.
   * @return Activities of user who is poster. 
   * @LevelAPI Platform
   * @since 4.0.1-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity poster);
  
  /**
   * Gets activities by input provided types.
   * Returned result with type is <code>ListAccess<code> then it can be lazy loaded.
   * 
   * @param poster The identity who posted activities.
   * @param activityTypes Provided types to get activities.
   * @return Activities of user who is poster. 
   * @LevelAPI Platform
   * @since 4.0.2-GA, 4.1.x
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, String ... activityTypes);
  
  /**
   * Adds a new activity processor.
   *
   * @param activityProcessor Activity processor
   * @LevelAPI Platform
   */
  void addProcessor(ActivityProcessor activityProcessor);

  /**
   * Adds a new activity processor plugin.
   *
   * @param activityProcessorPlugin Activity processor plugin
   * @LevelAPI Platform
   */
  void addProcessorPlugin(BaseActivityProcessorPlugin activityProcessorPlugin);

  /**
   * Saves a new created activity to a stream. Note that the Activity.userId will be set to the owner's identity if it
   * has not already set.
   *
   * @param streamOwner the activity stream owner
   * @param activity the activity to be saved
   * @return the saved activity
   * @LevelAPI Provisional
   * @deprecated Use {@link #saveActivityNoReturn(Identity, ExoSocialActivity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  ExoSocialActivity saveActivity(Identity streamOwner, ExoSocialActivity activity);


  /**
   * Saves a new created activity to the stream of that activity's userId stream. The userId of the created activity
   * must be set to indicate the owner stream.
   *
   * @param activity the activity to be saved
   * @LevelAPI Provisional
   * @deprecated Use {@link #saveActivityNoReturn(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   * instead. Will be removed by 4.0.x
   */
  @Deprecated
  ExoSocialActivity saveActivity(ExoSocialActivity activity);

  /**
   * Gets the latest activities by an identity with the default limit of 20 latest activities.
   *
   * @param identity the identity
   * @return the activities
   * @see #getActivities(Identity, long, long)
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivitiesWithListAccess(org.exoplatform.social.core.identity.model.Identity)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<ExoSocialActivity> getActivities(Identity identity) throws ActivityStorageException;

  /**
   * Gets the latest activities by an identity, specifying the start that is an offset index and the limit.
   *
   * @param identity the identity
   * @param start    offset index
   * @param limit
   * @return the activities
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivitiesWithListAccess(Identity)} instead. Will be removed by 4.0.x
   */
  @Deprecated
  List<ExoSocialActivity> getActivities(Identity identity, long start, long limit) throws ActivityStorageException;

  /**
   * Gets activities of connections from an identity. The activities are returned sorted starting from the most recent.
   *
   * @param ownerIdentity
   * @return activityList
   * @since 1.1.1
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivitiesOfConnectionsWithListAccess(Identity)} instead. Will be removed by 4.0.x
   */
  @Deprecated
  List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity) throws ActivityStorageException;


  /**
   * Gets activities of connections from an identity. The activities are returned sorted starting from the most recent.
   *
   * @param ownerIdentity
   * @return activityList
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivitiesOfConnectionsWithListAccess(Identity)} instead. Will be removed by 4.0.x
   */
  @Deprecated
  List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity,
                                                     int offset, int length) throws ActivityStorageException;

  /**
   * Gets the activities from all spaces of a user.
   *
   * @param ownerIdentity
   * @return list of activities
   * @since 1.1.1
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivitiesOfUserSpacesWithListAccess(Identity)} instead. Will be removed by 4.0.x
   */
  @Deprecated
  List<ExoSocialActivity> getActivitiesOfUserSpaces(Identity ownerIdentity);

  /**
   * Gets the activity feed of an identity. This feed is the combination of all the activities of his own activities,
   * his connections' activities and his spaces' activities which are returned sorted starting from the most recent.
   *
   * @param identity
   * @return all related activities of identity such as his activities, his connections's activities, his spaces's
   *         activities
   * @since 1.1.2
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivityFeedWithListAccess(Identity)} instead. Will be removed by 4.0.x
   */
  @Deprecated
  List<ExoSocialActivity> getActivityFeed(Identity identity) throws ActivityStorageException;

  /**
   * Removes an identity who likes an activity, if this activity is liked, it will be removed.
   *
   * @param activity
   * @param identity a user who dislikes an activity
   * @LevelAPI Provisional
   * @deprecated Use {@link #deleteLike(ExoSocialActivity, Identity)} instead. Will be removed by 4.0.x
   */
  @Deprecated
  void removeLike(ExoSocialActivity activity, Identity identity) throws ActivityStorageException;

  /**
   * Gets an activity's comment list.
   *
   * @param activity
   * @return commentList
   * @LevelAPI Provisional
   * @deprecated Use {@link #getCommentsWithListAccess(ExoSocialActivity)} instead. Will be removed by 4.0.x
   */
  @Deprecated
  List<ExoSocialActivity> getComments(ExoSocialActivity activity) throws ActivityStorageException;

  /**
   * Records an activity.
   *
   * @param owner
   * @param type
   * @param title
   * @return
   * @throws ActivityStorageException
   * @since 1.2.0-Beta1
   * @LevelAPI Provisional
   * @deprecated Use {@link #saveActivity(Identity, String, String)} instead. Will be removed by 4.0.x
   */
  @Deprecated
  ExoSocialActivity recordActivity(Identity owner, String type, String title) throws ActivityStorageException;

  /**
   * Saves an activity.
   *
   * @param owner
   * @param activity
   * @return the stored activity
   * @throws Exception
   * @LevelAPI Provisional
   * @deprecated use {@link #saveActivity(Identity, ExoSocialActivity)} instead. Will be removed by 4.0.x
   */
  @Deprecated
  ExoSocialActivity recordActivity(Identity owner, ExoSocialActivity activity) throws Exception;

  /**
   * Records an activity.
   *
   * @param owner the owner of the target stream for this activity
   * @param type  the type of activity which will be used to use custom ui for rendering
   * @param title the title
   * @param body  the body
   * @return the stored activity
   * @LevelAPI Provisional
   * @deprecated Use {@link #saveActivity(Identity, ExoSocialActivity)} instead. Will be removed by 4.0.x
   */
  @Deprecated
  ExoSocialActivity recordActivity(Identity owner, String type, String title,
                                   String body) throws ActivityStorageException;

  /**
   * Gets the number of activities from a stream owner.
   *
   * @param owner
   * @return the number
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x
   */
  @Deprecated
  int getActivitiesCount(Identity owner) throws ActivityStorageException;

  /**
   * Does nothing, for backward compatible.
   *
   * @param activity
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x
   */
  @Deprecated
  void processActivitiy(ExoSocialActivity activity);
}
