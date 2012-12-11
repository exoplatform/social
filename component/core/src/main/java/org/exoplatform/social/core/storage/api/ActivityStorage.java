/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.activity.filter.ActivityFilter;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.impl.ActivityBuilderWhere;

import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public interface ActivityStorage {

  enum TimestampType {
    NEWER, OLDER, UPDATED;

    private Long number;

    public TimestampType from(Long number) {
      this.number = number;
      return this;
    }

    public Long get() {
      return number;
    }
  }

  /**
   * Load an activity by its id.
   *
   * @param activityId the id of the activity. An UUID.
   * @return the activity
   */
  public ExoSocialActivity getActivity(String activityId) throws ActivityStorageException;

  /**
   * Gets all the activities by identity.
   *
   * @param owner the identity
   * @return the activities
   */
  public List<ExoSocialActivity> getUserActivities(Identity owner) throws ActivityStorageException;

  /**
   * Gets the activities by identity.
   *
   * Access a user's activity stream by specifying the offset and limit.
   *
   * @param owner the identity
   * @param offset
   * @param limit
   * @return the activities
   */
  public List<ExoSocialActivity> getUserActivities(
      Identity owner, long offset, long limit) throws ActivityStorageException;

  /**
   * Save comment to an activity.
   * activity's ownerstream has to be the same as ownerStream param here.
   *
   * @param activity
   * @param comment
   * @since 1.1.1
   */
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException;

  /**
     * Saves an activity into a stream.
     * Note that the field {@link org.exoplatform.social.core.activity.model.ExoSocialActivity#setUserId(String)}
     * should be the id of an identity {@link Identity#getId()}
     * @param owner owner of the stream where this activity is bound.
     *              Usually a user or space identity
     * @param activity the activity to save
     * @return stored activity
     * @throws ActivityStorageException activity storage exception with type:
     * ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY
     * @since 1.1.1
     */
  public ExoSocialActivity saveActivity(Identity owner, ExoSocialActivity activity) throws ActivityStorageException;

  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) throws ActivityStorageException;

  /**
   * Deletes activity by its id.
   * This will delete comments from this activity first, then delete the activity.
   *
   * @param activityId the activity id
   */
  public void deleteActivity(String activityId) throws ActivityStorageException;

  /**
   * Delete comment by its id.
   *
   * @param activityId
   * @param commentId
   */
  public void deleteComment(String activityId, String commentId) throws ActivityStorageException;

  /**
   * Gets the activities for a list of identities.
   *
   * Access a activity stream of a list of identities by specifying the offset and limit.
   *
   * @param connectionList the list of connections for which we want to get
   * the latest activities
   * @param offset
   * @param limit
   * @return the activities related to the list of connections
   * @since 1.2.0-GA
   */
  public List<ExoSocialActivity> getActivitiesOfIdentities(
      List<Identity> connectionList, long offset, long limit) throws ActivityStorageException;

  /**
   * Gets the activities for a list of identities.
   *
   * Access a activity stream of a list of identities by specifying the offset and limit.
   *
   * @param connectionList the list of connections for which we want to get
   * the latest activities
   * @param offset
   * @param limit
   * @return the activities related to the list of connections
   * @since 1.2.0-GA
   */
  public List<ExoSocialActivity> getActivitiesOfIdentities(
      List<Identity> connectionList, TimestampType type, long offset, long limit)
      throws ActivityStorageException;

  /**
   * Count the number of activities from an ownerIdentity
   *
   * @param owner
   * @return the number of activities
   */
  public int getNumberOfUserActivities(Identity owner) throws ActivityStorageException;

  /**
   * Gets the number of newer activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   */
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the list of newer activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getNewerOnUserActivities(
      Identity ownerIdentity, ExoSocialActivity baseActivity, int limit);

  /**
   * Gets the number of older activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   */
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the list of older activities based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getOlderOnUserActivities(
      Identity ownerIdentity, ExoSocialActivity baseActivity, int limit);

  /**
   * Gets activity feed from an identity.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getActivityFeed(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets the number of activities feed based from ownerIdentity.
   *
   * @param ownerIdentity
   * @return
   */
  public int getNumberOfActivitesOnActivityFeed(Identity ownerIdentity);

  /**
   * Gets the list of newer activities feed based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   */
  public int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the list of newer activities feed based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getNewerOnActivityFeed(
      Identity ownerIdentity, ExoSocialActivity baseActivity, int limit);

  /**
   * Gets the list of older activities feed based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   */
  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the list of older activities feed based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getOlderOnActivityFeed(
      Identity ownerIdentity, ExoSocialActivity baseActivity, int limit);

  /**
   * Gets activities of connections of an identity with offset, limit.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets the count of the activities of connections who connected with an identity.
   *
   * @param ownerIdentity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfActivitiesOfConnections(Identity ownerIdentity);

  /**
   * Gets the activities of the identity with offset, limit.
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @throws ActivityStorageException
   * @since 1.2.0-Beta3
    */
  public List<ExoSocialActivity> getActivitiesOfIdentity(Identity ownerIdentity, long offset, long limit);

  /**
   * Gets the number of newer activities based on base activity of connections.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the newer activities of connections based on baseActivity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getNewerOnActivitiesOfConnections(
      Identity ownerIdentity, ExoSocialActivity baseActivity, long limit);

  /**
   * Gets the number of older activities of connections based on baseActivity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the older of activities of connections based on base an activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getOlderOnActivitiesOfConnections(
      Identity ownerIdentity, ExoSocialActivity baseActivity, int limit);

  /**
   * Gets the activities of spaces where identity can access (manager or member).
   *
   * @param ownerIdentity
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getUserSpacesActivities(Identity ownerIdentity, int offset, int limit);

  /**
   * Gets the number of activities of spaces where identity can access (manager or member).
   *
   * @param ownerIdentity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfUserSpacesActivities(Identity ownerIdentity);

  /**
   * Gets the number of newer activities of spaces where the identity can access, based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the newer activities of spaces where identity can access (manager or member), based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getNewerOnUserSpacesActivities(
      Identity ownerIdentity, ExoSocialActivity baseActivity, int limit);

  /**
   * Gets the number of newer activities of spaces where the identity can access, based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity);

  /**
   * Gets the older activities of spaces where identity can access (manager or member), based on an existing activity.
   *
   * @param ownerIdentity
   * @param baseActivity
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getOlderOnUserSpacesActivities(
      Identity ownerIdentity, ExoSocialActivity baseActivity, int limit);

  /**
   * Gets the comments of an activity with offset, limit.
   *
   * @param existingActivity
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getComments(ExoSocialActivity existingActivity, int offset, int limit);

  /**
   * Gets the number of comments of an activity.
   *
   * @param existingActivity
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfComments(ExoSocialActivity existingActivity);

  /**
   * Gets the number of newer comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment);

  /**
   * Gets the newer comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @param limit
   * @return
   */
  public List<ExoSocialActivity> getNewerComments(
      ExoSocialActivity existingActivity, ExoSocialActivity baseComment, int limit);

  /**
   * Gets the number of newer comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @return
   * @since 1.2.0-Beta3
   */
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment);

  /**
   * Gets the older comments of an activity based on a comment.
   *
   * @param existingActivity
   * @param baseComment
   * @param limit
   * @return
   * @since 1.2.0-Beta3
   */
  public List<ExoSocialActivity> getOlderComments(
      ExoSocialActivity existingActivity, ExoSocialActivity baseComment, int limit);

  /**
   * Gets the activity processors.
   *
   * @return 1.2.0-GA
   */
  public SortedSet<ActivityProcessor> getActivityProcessors();

  /**
   * Updates an existing activity.
   *
   * @param existingActivity
   * @throws ActivityStorageException
   */
  public void updateActivity(ExoSocialActivity existingActivity) throws ActivityStorageException;
  
  /**
   * 
   * @param where
   * @param filter
   * @param offset
   * @param limit
   * @return
   * @throws ActivityStorageException
   */
  public List<ExoSocialActivity> getActivitiesOfIdentities(ActivityBuilderWhere where, ActivityFilter filter,
                                                           long offset, long limit) throws ActivityStorageException;
}
