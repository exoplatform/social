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

import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorageException;

/**
 * The interface ActivityManager manages activities.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @see org.exoplatform.social.core.activity.model.ExoSocialActivity
 * @see org.exoplatform.social.core.storage.ActivityStorage
 * @see IdentityManager
 */
public interface ActivityManager {
  /**
   * Saves an activity to the stream of an owner.<br/>
   * Note that the Activity.userId will be set to the owner's identity if it has not
   * already set.
   *
   * @param owner the owner of the activity stream. Usually a user or space
   * @param activity the activity to save
   * @return the activity saved
   */
  ExoSocialActivity saveActivity(Identity owner, ExoSocialActivity activity) throws ActivityStorageException;

  /**
   * Gets an activity by its Id.
   *
   * @param activityId the activity id
   * @return the activity
   */
  ExoSocialActivity getActivity(String activityId) throws ActivityStorageException;

  /**
   * Deletes an activity by its id.
   *
   * @param activityId the activity id
   */
  void deleteActivity(String activityId) throws ActivityStorageException;

  /**
   * Deletes a stored activity (id != null).
   *
   * @param activity
   * @since 1.1.1
   */
  void deleteActivity(ExoSocialActivity activity) throws ActivityStorageException;

  /**
   * Deletes a comment by its id.
   *
   * @param activityId
   * @param commentId
   */
  void deleteComment(String activityId, String commentId) throws ActivityStorageException;

  /**
   * Gets the latest activities by an identity with the default limit of 20 latest
   * activities.
   *
   * @param identity the identity
   * @return the activities
   * @see #getActivities(Identity, long, long)
   */
  List<ExoSocialActivity> getActivities(Identity identity) throws ActivityStorageException;

  /**
   * Gets the latest activities by an identity, specifying the start that is an offset index and
   * the limit.
   *
   * @param identity the identity
   * @param start offset index
   * @param limit
   * @return the activities
   */
  List<ExoSocialActivity> getActivities(Identity identity, long start, long limit) throws ActivityStorageException;

  /**
   * Gets activities of connections from an identity. The activities are sorted
   * by time. Though by using cache, this still can be considered as the cause
   * of the biggest performance problem.
   *
   * @param ownerIdentity
   * @return activityList
   * @since 1.1.1
   */
  List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity) throws ActivityStorageException;

  /**
   * Gets the activities from all spaces of a user. By default, the activity list
   * is composed of all spaces' activities. Each activity list of the space contains
   * maximum 20 activities and are sorted by time.
   *
   * @param ownerIdentity
   * @return list of activities
   * @since 1.1.1
   */
  List<ExoSocialActivity> getActivitiesOfUserSpaces(Identity ownerIdentity);

  /**
   * Gets the activity feed of an identity. This feed is the combination of all
   * the activities of his own activities, his connections' activities and his
   * spaces' activities which are sorted by time. The latest activity is the
   * first item in the activity list.
   *
   * @param identity
   * @return all related activities of identity such as his activities, his
   *         connections's activities, his spaces's activities
   * @since  1.1.2
   */
  List<ExoSocialActivity> getActivityFeed(Identity identity) throws ActivityStorageException;

  /**
   * Saves an activity into the stream for the activity's userId. The userId must
   * be set and this field is used to indicate the owner stream.
   *
   * @param activity the activity to save
   * @return the activity
   * @see #saveActivity(Identity,
   *      org.exoplatform.social.core.activity.model.ExoSocialActivity)
   * @see org.exoplatform.social.core.activity.model.ExoSocialActivity#getUserId()
   */
  ExoSocialActivity saveActivity(ExoSocialActivity activity) throws ActivityStorageException;

  /**
   * Saves a new comment or updates an existing comment that is an instance of
   * Activity with mandatory fields: userId, title.
   *
   * @param activity
   * @param comment
   */
  void saveComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException;

  /**
   * Saves an identity who likes an activity.
   *
   * @param activity
   * @param identity
   */
  void saveLike(ExoSocialActivity activity, Identity identity) throws ActivityStorageException;

  /**
   * Removes an indentity who likes an activity, if this activity is liked, it will be removed.
   *
   * @param activity
   * @param identity a user who dislikes an activity
   */
  void removeLike(ExoSocialActivity activity, Identity identity) throws ActivityStorageException;

  /**
   * Gets an activity's comment list.
   *
   * @param activity
   * @return commentList
   */
  List<ExoSocialActivity> getComments(ExoSocialActivity activity) throws ActivityStorageException;

  /**
   * Records an activity.
   *
   * @param owner
   * @param type
   * @param title
   * @return
   * @throws ActivityStorageException
   * @since 1.2.0-GA
   */
  ExoSocialActivity recordActivity(Identity owner, String type, String title) throws ActivityStorageException;

  /**
   * Saves an activity.
   *
   * @param owner
   * @param activity
   * @return the stored activity
   * @throws Exception
   * @deprecated use {@link #saveActivity(Identity, Activity)} instead. Will be
   *             removed by 1.3.x
   */
  ExoSocialActivity recordActivity(Identity owner, ExoSocialActivity activity) throws Exception;

  /**
   * Records an activity.
   *
   * @param owner the owner of the target stream for this activity
   * @param type the type of activity which will be used to use custom ui for
   *          rendering
   * @param title the title
   * @param body the body
   * @return the stored activity
   */
  ExoSocialActivity recordActivity(Identity owner, String type, String title, String body) throws ActivityStorageException;

  /**
   * Adds a new processor.
   *
   * @param processor
   */
  void addProcessor(ActivityProcessor processor);

  /**
   * Adds a new processor plugin.
   *
   * @param plugin
   */
  void addProcessorPlugin(BaseActivityProcessorPlugin plugin);

  /**
   * Gets the number of activities from a stream owner.
   *
   * @param owner
   * @return the number
   */
  int getActivitiesCount(Identity owner) throws ActivityStorageException;

  /**
   * Passes an activity through the chain of processors.
   *
   * @param activity
   */
  void processActivitiy(ExoSocialActivity activity);
}
