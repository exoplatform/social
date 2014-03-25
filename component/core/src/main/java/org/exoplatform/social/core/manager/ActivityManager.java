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
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorageException;

/**
 * Provides APIs to manage activities.
 * All methods to manipulate with activities, comments and likes are provided. 
 * With these API types, you can:

 * <ul>
 * <li>Store, get and update information of activities.</li>
 * <li>Get an activity by using its Id.</li>
 * <li>Get a list of activities by the returned result 
 * under <code>ListAccess</code> for lazy loading.</li>
 * </ul>
 *
 * Also, the API which adds processors to process activities content is also included.
 *
 */
public interface ActivityManager {

  /**
   * Saves a newly created activity to a stream. 
   * The stream owner will be <code>Activity.userId</code> in case that information has not already been set.
   *
   * @param streamOwner The activity stream owner.
   * @param activity The activity to be saved.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  void saveActivityNoReturn(Identity streamOwner, ExoSocialActivity activity);


   /**
   * Saves a newly created activity to the stream. In this case, information of the stream owner has been set in the activity.
   *
   * @param activity The activity to be saved.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void saveActivityNoReturn(ExoSocialActivity activity);

  /**
   * Saves a new activity by indicating stream owner, activity type and title. 
   * This is shorthand to save an activity without creating a new {@link ExoSocialActivity} instance.
   *
   * @param streamOwner The activity stream owner.
   * @param type Type of the activity.
   * @param title Title of the activity.
   * @LevelAPI Platform
   */
  void saveActivity(Identity streamOwner, String type, String title);

  /**
   * Gets an activity by its Id.
   *
   * @param activityId Id of the activity.
   * @return The activity.
   * @LevelAPI Platform
   */
  ExoSocialActivity getActivity(String activityId);

  /**
   * Gets an activity by its comment. The comments included in this activity are considered as its children.
   *
   * @param comment The comment.
   * @return The activity containing the comment.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ExoSocialActivity getParentActivity(ExoSocialActivity comment);

  /**
   * Updates an existing activity.
   *
   * @param activity The activity to be updated.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void updateActivity(ExoSocialActivity activity);

  /**
   * Deletes a specific activity.
   *
   * @param activity The activity to be deleted.
   * @LevelAPI Platform
   * @since 1.1.1
   */
  void deleteActivity(ExoSocialActivity activity);

  /**
   * Deletes an activity by its Id.
   *
   * @param activityId Id of the deleted activity.
   * @LevelAPI Platform
   */
  void deleteActivity(String activityId);

  /**
   * Saves a new comment to a specific activity.
   *
   * @param activity The activity.
   * @param newComment The comment to be saved.
   * @LevelAPI Platform
   */
  void saveComment(ExoSocialActivity activity, ExoSocialActivity newComment);

  /**
   * Gets comments of a specific activity. 
   * The type of returned result is <code>ListAccess</code> which can be lazy loaded.
   *
   *
   * @param activity The specific activity.
   * @return The comments.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity activity);

  /**
   * Deletes an existing comment of a specific activity by its Id.
   *
   * @param activityId Id of the activity containing the deleted comment.
   * @param commentId Id of the deleted comment.
   * @LevelAPI Platform
   */
  void deleteComment(String activityId, String commentId);

  /**
   * Deletes a comment of an activity.
   *
   * @param activity The activity containing the deleted comment.
   * @param comment The deleted comment.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void deleteComment(ExoSocialActivity activity, ExoSocialActivity comment);

  /**
   * Saves the like information of an identity to a specific activity.
   *
   * @param activity The activity containing the like information which is saved.
   * @param identity The identity who likes the activity.
   * @LevelAPI Platform
   */
  void saveLike(ExoSocialActivity activity, Identity identity);

  /**
   * Deletes a like of an identity from a specific activity.
   *
   * @param activity The activity containing the deleted like.
   * @param identity The identity of the deleted like.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void deleteLike(ExoSocialActivity activity, Identity identity);

  /**
   * Gets activities posted on the provided activity stream owner.
   * The type of returned result is <code>ListAccess</code> which can be lazy loaded.
   *
   * @param identity The provided activity stream owner.
   * @return The activities.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity identity);
  
  /**
   * Gets activities on the provided activity stream which is viewed by another.
   * The type of returned result is <code>ListAccess</code> which can be lazy loaded.
   * For example: Mary is connected to Demo, then Demo signs in and watches the activity stream of Mary.
   * 
   * @param ownerIdentity The provided activity stream owner.
   * @param viewerIdentity The identity who views the other stream.
   * @return Activities on the provided activity stream owner.
   * @LevelAPI Platform
   * @since 4.0.x
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity ownerIdentity, Identity viewerIdentity);

  /**
   * Gets activities posted by all connections with a given identity.
   * The type of returned result is <code>ListAccess</code> which can be lazy loaded.
   *
   * @param identity The identity.
   * @return The activities posted by all connections with the identity.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesOfConnectionsWithListAccess(Identity identity);

  /**
   * Gets activities posted on a space by its Id.
   * The type of returned result is <code>ListAccess</code> which can be lazy loaded.
   *
   * @param spaceIdentity The specific stream owner identity.
   * @return  The activities which belong to the space.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesOfSpaceWithListAccess(Identity spaceIdentity);
  
  /**
   * Gets activities posted on all space activity streams in which an identity joins.
   * The type of returned result is <code>ListAccess</code> which can be lazy loaded.
   *
   * @param identity The identity to get his activities on spaces.
   * @return The activities of the user on spaces.
   * @LevelAPI Platform
   * @since 4.0.x
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesOfUserSpacesWithListAccess(Identity identity);

  /**
   * Gets all activities accessible by a given identity.
   * The type of returned result is <code>ListAccess</code> which can be lazy loaded.
   *
   * @param identity The identity.
   * @return All activities of the identity.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivityFeedWithListAccess(Identity identity);

  /**
   * Gets activities of a given poster.
   * The type of returned result is <code>ListAccess</code> which can be lazy loaded.
   * 
   * @param poster The identity who posted activities.
   * @return The activities of the poster. 
   * @LevelAPI Platform
   * @since 4.0.1-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity poster);
  
  /**
   * Gets activities of a given poster that are specified by activity types.
   * The type of returned result is <code>ListAccess</code>  which can be lazy loaded.
   * 
   * @param poster The identity who posted activities.
   * @param activityTypes The types to get activities.
   * @return The activities of the poster. 
   * @LevelAPI Platform
   * @since 4.0.2-GA, 4.1.x
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, String ... activityTypes);
  
  /**
   * Adds a new activity processor.
   *
   * @param activityProcessor The activity processor to be added.
   * @LevelAPI Platform
   */
  void addProcessor(ActivityProcessor activityProcessor);

  /**
   * Adds a new activity processor plugin.
   *
   * @param activityProcessorPlugin The activity processor plugin to be added.
   * @LevelAPI Platform
   */
  void addProcessorPlugin(BaseActivityProcessorPlugin activityProcessorPlugin);

  void addActivityEventListener(ActivityListenerPlugin activityListenerPlugin);
  
  /**
   * Saves a newly created activity to a stream. Note that the Activity.userId will be set to the owner identity if it
   * has not already been set.
   *
   * @param streamOwner The activity stream owner.
   * @param activity The activity to be saved.
   * @return The saved activity.
   * @LevelAPI Provisional
   * @deprecated Use {@link #saveActivityNoReturn(Identity, ExoSocialActivity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  ExoSocialActivity saveActivity(Identity streamOwner, ExoSocialActivity activity);


  /**
   * Saves a newly created activity to the stream of that activity's userId stream. The userId of the created activity
   * must be set to indicate the owner stream.
   *
   * @param activity The activity to be saved.
   * @LevelAPI Provisional
   * @deprecated Use {@link #saveActivityNoReturn(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   * instead. Will be removed by 4.0.x.
   */
  @Deprecated
  ExoSocialActivity saveActivity(ExoSocialActivity activity);

  /**
   * Gets all activities by an identity.
   *
   * @param identity The identity.
   * @return The activities.
   * @see #getActivities(Identity, long, long)
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivitiesWithListAccess(org.exoplatform.social.core.identity.model.Identity)} instead.
   *             Will be removed by 4.0.x.
   */
  @Deprecated
  List<ExoSocialActivity> getActivities(Identity identity) throws ActivityStorageException;

  /**
   * Gets the latest activities of a given identity, specifying the start that is an offset index and the limit.
   *
   * @param identity The identity.
   * @param start The offset index.
   * @param limit The end-point index.
   * @return The activities.
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivitiesWithListAccess(Identity)} instead. Will be removed by 4.0.x.
   */
  @Deprecated
  List<ExoSocialActivity> getActivities(Identity identity, long start, long limit) throws ActivityStorageException;

  /**
   * Gets activities of connections from an identity.
   *
   * @param ownerIdentity The identity information to get activities.
   * @return The activities.
   * @since 1.1.1
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivitiesOfConnectionsWithListAccess(Identity)} instead. Will be removed by 4.0.x.
   */
  @Deprecated
  List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity) throws ActivityStorageException;


  /**
   * Gets activities of connections from an identity.
   *
   * @param ownerIdentity The identity information to get activities.
   * @return The activities.
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivitiesOfConnectionsWithListAccess(Identity)} instead. Will be removed by 4.0.x.
   */
  @Deprecated
  List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity,
                                                     int offset, int length) throws ActivityStorageException;

  /**
   * Gets activities from all spaces of a user.
   *
   * @param ownerIdentity The identity information to get activities.
   * @return The activities.
   * @since 1.1.1
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivitiesOfUserSpacesWithListAccess(Identity)} instead. Will be removed by 4.0.x.
   */
  @Deprecated
  List<ExoSocialActivity> getActivitiesOfUserSpaces(Identity ownerIdentity);

  /**
   * Gets activity feed of an identity. This feed is combination of all activities of his own activities,
   * his connections' activities and his spaces' activities, which are returned, are sorted starting from the most recent.
   *
   * @param identity The identity information to get the activity.
   * @return All related to the identity, such as his activities, connections' activities, and spaces' activities.
   * @since 1.1.2
   * @LevelAPI Provisional
   * @deprecated Use {@link #getActivityFeedWithListAccess(Identity)} instead. Will be removed by 4.0.x.
   */
  @Deprecated
  List<ExoSocialActivity> getActivityFeed(Identity identity) throws ActivityStorageException;

  /**
   * Removes an identity who likes an activity.
   *
   * @param activity The activity liked by the identity.
   * @param identity The identity who liked the activity.
   * @LevelAPI Provisional
   * @deprecated Use {@link #deleteLike(ExoSocialActivity, Identity)} instead. Will be removed by 4.0.x.
   */
  @Deprecated
  void removeLike(ExoSocialActivity activity, Identity identity) throws ActivityStorageException;

  /**
   * Gets all comments of an activity.
   *
   * @param activity The activity which you want to get comments.
   * @return The comments.
   * @LevelAPI Provisional
   * @deprecated Use {@link #getCommentsWithListAccess(ExoSocialActivity)} instead. Will be removed by 4.0.x.
   */
  @Deprecated
  List<ExoSocialActivity> getComments(ExoSocialActivity activity) throws ActivityStorageException;

  /**
   * Records an activity.
   *
   * @param owner Owner of the recorded activity.
   * @param type Type of the recorded activity.
   * @param title Title of the recorded activity.
   * @return The recorded activity.
   * @throws ActivityStorageException
   * @since 1.2.0-Beta1
   * @LevelAPI Provisional
   * @deprecated Use {@link #saveActivity(Identity, String, String)} instead. Will be removed by 4.0.x.
   */
  @Deprecated
  ExoSocialActivity recordActivity(Identity owner, String type, String title) throws ActivityStorageException;

  /**
   * Saves an activity.
   *
   * @param owner Owner of the saved activity.
   * @param activity The activity to be saved.
   * @return The saved activity.
   * @throws Exception
   * @LevelAPI Provisional
   * @deprecated use {@link #saveActivity(Identity, ExoSocialActivity)} instead. Will be removed by 4.0.x.
   */
  @Deprecated
  ExoSocialActivity recordActivity(Identity owner, ExoSocialActivity activity) throws Exception;

  /**
   * Records an activity.
   *
   * @param owner Owner of the target stream for the recorded activity.
   * @param type  Type of the activity which uses the custom UI for rendering.
   * @param title The title.
   * @param body  The body.
   * @return The recorded activity.
   * @LevelAPI Provisional
   * @deprecated Use {@link #saveActivity(Identity, ExoSocialActivity)} instead. Will be removed by 4.0.x.
   */
  @Deprecated
  ExoSocialActivity recordActivity(Identity owner, String type, String title,
                                   String body) throws ActivityStorageException;

  /**
   * Gets the number of activities from a stream owner.
   *
   * @param owner The identity.
   * @return The number of activities.
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  @Deprecated
  int getActivitiesCount(Identity owner) throws ActivityStorageException;

  /**
   * Processes an activity to some given rules.
   *
   * @param activity The activity to be processed.
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x.
   */
  @Deprecated
  void processActivitiy(ExoSocialActivity activity);
}
