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
 * Public APIs to manage activities.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @see org.exoplatform.social.core.activity.model.ExoSocialActivity
 * @see org.exoplatform.social.core.storage.ActivityStorage
 * @see IdentityManager
 */
public interface ActivityManager {

  /**
   * Saves a new created activity to a stream. Note that the Activity.userId will be set to the owner's identity if it
   * has not already set.
   *
   * @param streamOwner the activity stream owner
   * @param newActivity the activity to be saved
   * @since 1.3.0-GA
   */
  //void saveActivity(Identity streamOwner, ExoSocialActivity newActivity);

  /**
   * Saves a new created activity to a stream. Note that the Activity.userId will be set to the owner's identity if it
   * has not already set.
   *
   * @param streamOwner the activity stream owner
   * @param newActivity the activity to be saved
   * @return the saved activity
   * @since  1.2.0-GA
   */
  void saveActivityNoReturn(Identity streamOwner, ExoSocialActivity newActivity);


  /**
   * Saves a new created activity to the stream of that activity's userId stream. The userId of the created activity
   * must be set to indicate the owner stream.
   *
   * @param newActivity the activity to be saved
   * @since 1.3.0-GA
   */
  //void saveActivity(ExoSocialActivity newActivity);

   /**
   * Saves a new created activity to the stream of that activity's userId stream. The userId of the created activity
   * must be set to indicate the owner stream.
   *
   * @param newActivity the activity to be saved
   * @since 1.2.0-GA
   */
  void saveActivityNoReturn(ExoSocialActivity newActivity);

  /**
   * Saves a new activity by indicating the stream owner, the activity type and activityTitle. This is shorthand to save
   * a new created activity without create a new {@link ExoSocialActivity} instance.
   *
   * @param streamOwner   the activity stream owner
   * @param activityType  the activity type
   * @param activityTitle the activity title
   */
  void saveActivity(Identity streamOwner, String activityType, String activityTitle);

  /**
   * Gets an activity by its id.
   *
   * @param activityId the activity id
   * @return the activity matching provided activityId
   */
  ExoSocialActivity getActivity(String activityId);

  /**
   * Gets the activity associated with an existing comment.
   *
   * @param comment the existing comment
   * @return the associated activity
   * @since  1.2.0-GA
   */
  ExoSocialActivity getParentActivity(ExoSocialActivity comment);

  /**
   * Updates an existing activity.
   *
   * @param existingActivity the existing activity
   * @since 1.2.0-GA
   */
  void updateActivity(ExoSocialActivity existingActivity);

  /**
   * Deletes an existing activity.
   *
   * @param existingActivity the existing activity
   * @since 1.1.1
   */
  void deleteActivity(ExoSocialActivity existingActivity);

  /**
   * Deletes an activity by its id.
   *
   * @param activityId the activity id
   */
  void deleteActivity(String activityId);

  /**
   * Saves a new comment to an existing activity.
   *
   * @param existingActivity the existing activity
   * @param newComment       the new comment to be saved
   */
  void saveComment(ExoSocialActivity existingActivity, ExoSocialActivity newComment);

  /**
   * Gets the comments of an existing activity via {@link org.exoplatform.social.common.RealtimeListAccess}.
   *
   * @param existingActivity the existing activity
   * @return the realtime list access for these comments
   * @since 1.3.0-GA
   */
  //RealtimeListAccess<ExoSocialActivity> getComments(ExoSocialActivity existingActivity);

  /**
   * Gets the comments of an existing activity via {@link org.exoplatform.social.common.RealtimeListAccess}.
   *
   *
   * @param existingActivity the existing activity
   * @return the real time list access for these comments
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity existingActivity);

  /**
   * Deletes a existing comment of an existing activity by their ids.
   *
   * @param activityId
   * @param commentId
   */
  void deleteComment(String activityId, String commentId);

  /**
   * Deletes an exising comment of an existing activity.
   *
   * @param existingActivity the existing activity
   * @param existingComment  the existing comment
   * @since 1.2.0-GA
   */
  void deleteComment(ExoSocialActivity existingActivity, ExoSocialActivity existingComment);

  /**
   * Saves a like of an identity to an existing activity.
   *
   * @param existingActivity the existing activity
   * @param identity         the existing identity who likes this activity
   */
  void saveLike(ExoSocialActivity existingActivity, Identity identity);

  /**
   * Deletes an existing like of an identity from an existing identity.
   *
   * @param existingActivity the existing activity
   * @param identity         the existing identity
   * @since 1.2.0-GA
   */
  void deleteLike(ExoSocialActivity existingActivity, Identity identity);

  /**
   * Gets the activities posted on the provided activity stream owner via {@link RealtimeListAccess}.
   *
   * @param ownerIdentity the provided activity stream owner
   * @return the real time list access for activities on the provided activity stream owner
   * @since 1.3.0-GA
   */
  //RealtimeListAccess<ExoSocialActivity> getActivities(Identity ownerIdentity);


  /**
   * Gets the activities posted on the provided activity stream owner via {@link RealtimeListAccess}.
   *
   * @param ownerIdentity the provided activity stream owner
   * @return the real time list access for activities on the provided activity stream owner
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity ownerIdentity);

  /**
   * Gets the activities posted by all connections with an existing identity via {@link RealtimeListAccess}
   *
   * @param existingIdentity the existing identity
   * @return the real time list access for activities posted by all connections with an existing identity
   * @since 1.3.0-GA
   */
  //RealtimeListAccess<ExoSocialActivity> getActivitiesOfIdentities(Identity existingIdentity);

  /**
   * Gets the activities posted by all connections with an existing identity via {@link RealtimeListAccess}
   *
   * @param existingIdentity the existing identity
   * @return the real time list access for activities posted by all connections with an existing identity
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesOfConnectionsWithListAccess(Identity existingIdentity);

  /**
   * Gets the activities posted on all space activity streams in which the provided identity joins via {@link
   * RealtimeListAccess}.
   *
   * @param existingIdentity the existing identity
   * @return the real time list access for activities
   * @since 1.3.0-GA
   */
  //RealtimeListAccess<ExoSocialActivity> getActivitiesOfUserSpaces(Identity existingIdentity);

  /**
   * Gets the activities posted on all space activity streams in which the provided identity joins via {@link
   * RealtimeListAccess}.
   *
   * @param existingIdentity the existing identity
   * @return the real time list access for activities
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivitiesOfUserSpacesWithListAccess(Identity existingIdentity);

  /**
   * Gets all the activities accessible by an existing identity via {@link RealtimeListAccess}
   *
   * @param existingIdentity the existing identity
   * @return the real time list access for activities
   * @since 1.3.0-GA
   */
  //RealtimeListAccess<ExoSocialActivity> getActivityFeed(Identity existingIdentity);

  /**
   * Gets all the activities accessible by an existing identity via {@link RealtimeListAccess}
   *
   * @param existingIdentity the existing identity
   * @return the real time list access for activities
   * @since 1.2.0-GA
   */
  RealtimeListAccess<ExoSocialActivity> getActivityFeedWithListAccess(Identity existingIdentity);

  /**
   * Adds a new activity processor.
   *
   * @param newActivityProcessor a new activity processor
   */
  void addProcessor(ActivityProcessor newActivityProcessor);

  /**
   * Adds a new activity processor plugin.
   *
   * @param newActivityProcessorPlugin the new activity processor plugin
   */
  void addProcessorPlugin(BaseActivityProcessorPlugin newActivityProcessorPlugin);


  /**
   * Saves a new created activity to a stream. Note that the Activity.userId will be set to the owner's identity if it
   * has not already set.
   *
   * @param streamOwner the activity stream owner
   * @param newActivity the activity to be saved
   * @return the saved activity
   * @deprecated Use {@link #saveActivityNoReturn(Identity, ExoSocialActivity)} instead.
   *             Will be removed by 1.3.x
   */
  @Deprecated
  ExoSocialActivity saveActivity(Identity streamOwner, ExoSocialActivity newActivity);


  /**
   * Saves a new created activity to the stream of that activity's userId stream. The userId of the created activity
   * must be set to indicate the owner stream.
   *
   * @param newActivity the activity to be saved
   * @deprecated Use {@link #saveActivityNoReturn(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   * instead. Will be removed by 1.3.x
   */
  @Deprecated
  ExoSocialActivity saveActivity(ExoSocialActivity newActivity);

  /**
   * Gets the latest activities by an identity with the default limit of 20 latest activities.
   *
   * @param identity the identity
   * @return the activities
   * @see #getActivities(Identity, long, long)
   * @deprecated Use {@link #getActivitiesWithListAccess(org.exoplatform.social.core.identity.model.Identity)} instead.
   *             Will be removed by 1.3.x
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
   * @deprecated Use {@link #getActivitiesWithListAccess(Identity)} instead. Will be removed by 1.3.x
   */
  @Deprecated
  List<ExoSocialActivity> getActivities(Identity identity, long start, long limit) throws ActivityStorageException;

  /**
   * Gets activities of connections from an identity. The activities are returned sorted starting from the most recent.
   *
   * @param ownerIdentity
   * @return activityList
   * @since 1.1.1
   * @deprecated Use {@link #getActivitiesOfConnectionsWithListAccess(Identity)} instead. Will be removed by 1.3.x
   */
  @Deprecated
  List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity) throws ActivityStorageException;


  /**
   * Gets activities of connections from an identity. The activities are returned sorted starting from the most recent.
   *
   * @param ownerIdentity
   * @return activityList
   * @deprecated Use {@link #getActivitiesOfConnectionsWithListAccess(Identity)} instead. Will be removed by 1.3.x
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
   * @deprecated Use {@link #getActivitiesOfUserSpacesWithListAccess(Identity)} instead. Will be removed by 1.3.x
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
   * @deprecated Use {@link #getActivityFeedWithListAccess(Identity)} instead. Will be removed by 1.3.x
   */
  @Deprecated
  List<ExoSocialActivity> getActivityFeed(Identity identity) throws ActivityStorageException;

  /**
   * Removes an identity who likes an activity, if this activity is liked, it will be removed.
   *
   * @param activity
   * @param identity a user who dislikes an activity
   * @deprecated Use {@link #deleteLike(ExoSocialActivity, Identity)} instead. Will be removed by 1.3.x
   */
  @Deprecated
  void removeLike(ExoSocialActivity activity, Identity identity) throws ActivityStorageException;

  /**
   * Gets an activity's comment list.
   *
   * @param activity
   * @return commentList
   * @deprecated Use {@link #getCommentsWithListAccess(ExoSocialActivity)} instead. Will be removed by 1.3.x
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
   * @deprecated Use {@link #saveActivity(Identity, String, String)} instead. Will be removed by 1.3.x
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
   * @deprecated use {@link #saveActivity(Identity, ExoSocialActivity)} instead. Will be removed by 1.3.x
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
   * @deprecated Use {@link #saveActivity(Identity, ExoSocialActivity)} instead. Will be removed by 1.3.x
   */
  @Deprecated
  ExoSocialActivity recordActivity(Identity owner, String type, String title,
                                   String body) throws ActivityStorageException;

  /**
   * Gets the number of activities from a stream owner.
   *
   * @param owner
   * @return the number
   * @deprecated Will be removed by 1.3.x
   */
  @Deprecated
  int getActivitiesCount(Identity owner) throws ActivityStorageException;

  /**
   * Does nothing, for backward compatible.
   *
   * @param activity
   * @deprecated Will be removed by 1.3.x
   */
  @Deprecated
  void processActivitiy(ExoSocialActivity activity);
}
