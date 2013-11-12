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

package org.exoplatform.social.core.storage.synchronization;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SynchronizedActivityStorage extends ActivityStorageImpl {

  public SynchronizedActivityStorage( final RelationshipStorage relationshipStorage,
      final IdentityStorage identityStorage,
      final SpaceStorage spaceStorage,
      final ActivityStreamStorage streamStorage) {

    super(relationshipStorage, identityStorage, spaceStorage, streamStorage);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExoSocialActivity getActivity(final String activityId) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getActivity(activityId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getUserActivities(final Identity owner) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getUserActivities(owner);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getUserActivities(final Identity owner, final long offset, final long limit)
                                                                                            throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getUserActivities(owner, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  @Override
  public List<ExoSocialActivity> getUserActivitiesForUpgrade(Identity owner, long offset, long limit) throws ActivityStorageException {
    boolean created = startSynchronization();
    try {
      return super.getUserActivitiesForUpgrade(owner, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
    
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveComment(final ExoSocialActivity activity, final ExoSocialActivity comment) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      super.saveComment(activity, comment);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExoSocialActivity saveActivity(final Identity owner, final ExoSocialActivity activity) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      return super.saveActivity(owner, activity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExoSocialActivity getParentActivity(final ExoSocialActivity comment) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getParentActivity(comment);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteActivity(final String activityId) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      super.deleteActivity(activityId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteComment(final String activityId, final String commentId) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      super.deleteComment(activityId, commentId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getActivitiesOfIdentities(final List<Identity> connectionList, final long offset,
                                                           final long limit) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getActivitiesOfIdentities(connectionList, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getActivitiesOfIdentities(final List<Identity> connectionList, final TimestampType type,
                                                           final long offset, final long limit) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getActivitiesOfIdentities(connectionList, type, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfUserActivities(final Identity owner) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfUserActivities(owner);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  @Override
  public int getNumberOfUserActivitiesForUpgrade(Identity owner) throws ActivityStorageException {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfUserActivitiesForUpgrade(owner);
    }
    finally {
      stopSynchronization(created);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNewerOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerOnUserActivities(ownerIdentity, baseActivity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getNewerOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity,
                                                          final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getNewerOnUserActivities(ownerIdentity, baseActivity, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfOlderOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderOnUserActivities(ownerIdentity, baseActivity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getOlderOnUserActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity,
                                                          final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getOlderOnUserActivities(ownerIdentity, baseActivity, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getActivityFeed(final Identity ownerIdentity, final int offset, final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getActivityFeed(ownerIdentity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  @Override
  public List<ExoSocialActivity> getActivityFeedForUpgrade(final Identity ownerIdentity,
                                                           final int offset,
                                                           final int limit) {
    
    boolean created = startSynchronization();
    try {
      return super.getActivityFeedForUpgrade(ownerIdentity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfActivitesOnActivityFeed(final Identity ownerIdentity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfActivitesOnActivityFeed(ownerIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  @Override
  public int getNumberOfActivitesOnActivityFeedForUpgrade(Identity ownerIdentity) {
    
    boolean created = startSynchronization();
    try {
      return super.getNumberOfActivitesOnActivityFeedForUpgrade(ownerIdentity);
    }
    finally {
      stopSynchronization(created);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNewerOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerOnActivityFeed(ownerIdentity, baseActivity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNewerOnActivityFeed(final Identity ownerIdentity,final Long sinceTime) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerOnActivityFeed(ownerIdentity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, Long sinceTime) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerOnUserActivities(ownerIdentity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, Long sinceTime) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerOnActivitiesOfConnections(ownerIdentity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, Long sinceTime) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerOnUserSpacesActivities(ownerIdentity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }

  }
  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getNewerOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity,
                                                        final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getNewerOnActivityFeed(ownerIdentity, baseActivity, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfOlderOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderOnActivityFeed(ownerIdentity, baseActivity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getOlderOnActivityFeed(final Identity ownerIdentity, final ExoSocialActivity baseActivity,
                                                        final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getOlderOnActivityFeed(ownerIdentity, baseActivity, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getActivitiesOfConnections(final Identity ownerIdentity, final int offset, final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getActivitiesOfConnections(ownerIdentity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  @Override
  public List<ExoSocialActivity> getActivitiesOfConnectionsForUpgrade(Identity ownerIdentity,
                                                                      int offset,
                                                                      int limit) {
    boolean created = startSynchronization();
    try {
      return super.getActivitiesOfConnectionsForUpgrade(ownerIdentity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfActivitiesOfConnections(final Identity ownerIdentity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfActivitiesOfConnections(ownerIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  @Override
  public int getNumberOfActivitiesOfConnectionsForUpgrade(Identity ownerIdentity) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfActivitiesOfConnectionsForUpgrade(ownerIdentity);
    }
    finally {
      stopSynchronization(created);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getActivitiesOfIdentity(final Identity ownerIdentity, final long offset, final long limit)
                                                                                       throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getActivitiesOfIdentity(ownerIdentity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNewerOnActivitiesOfConnections(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerOnActivitiesOfConnections(ownerIdentity, baseActivity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getNewerOnActivitiesOfConnections(final Identity ownerIdentity,
                                                                   final ExoSocialActivity baseActivity, final long limit) {

    boolean created = startSynchronization();
    try {
      return super.getNewerOnActivitiesOfConnections(ownerIdentity, baseActivity, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfOlderOnActivitiesOfConnections(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderOnActivitiesOfConnections(ownerIdentity, baseActivity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getOlderOnActivitiesOfConnections(final Identity ownerIdentity,
                                                                   final ExoSocialActivity baseActivity, final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getOlderOnActivitiesOfConnections(ownerIdentity, baseActivity, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getUserSpacesActivities(final Identity ownerIdentity, final int offset, final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getUserSpacesActivities(ownerIdentity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  @Override
  public List<ExoSocialActivity> getUserSpacesActivitiesForUpgrade(Identity ownerIdentity,
                                                                   int offset,
                                                                   int limit) {
    boolean created = startSynchronization();
    try {
      return super.getUserSpacesActivitiesForUpgrade(ownerIdentity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
    
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfUserSpacesActivities(final Identity ownerIdentity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfUserSpacesActivities(ownerIdentity);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  @Override
  public int getNumberOfUserSpacesActivitiesForUpgrade(Identity ownerIdentity) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfUserSpacesActivitiesForUpgrade(ownerIdentity);
    }
    finally {
      stopSynchronization(created);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNewerOnUserSpacesActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerOnUserSpacesActivities(ownerIdentity, baseActivity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getNewerOnUserSpacesActivities(final Identity ownerIdentity,
                                                                final ExoSocialActivity baseActivity, final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getNewerOnUserSpacesActivities(ownerIdentity, baseActivity, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfOlderOnUserSpacesActivities(final Identity ownerIdentity, final ExoSocialActivity baseActivity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderOnUserSpacesActivities(ownerIdentity, baseActivity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getOlderOnUserSpacesActivities(final Identity ownerIdentity,
                                                                final ExoSocialActivity baseActivity, final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getOlderOnUserSpacesActivities(ownerIdentity, baseActivity, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getComments(final ExoSocialActivity existingActivity, final int offset, final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getComments(existingActivity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfComments(final ExoSocialActivity existingActivity) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfComments(existingActivity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNewerComments(final ExoSocialActivity existingActivity, final ExoSocialActivity baseComment) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerComments(existingActivity, baseComment);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getNewerComments(final ExoSocialActivity existingActivity, final ExoSocialActivity baseComment,
                                                  final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getNewerComments(existingActivity, baseComment, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfOlderComments(final ExoSocialActivity existingActivity, final ExoSocialActivity baseComment) {

    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderComments(existingActivity, baseComment);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getOlderComments(final ExoSocialActivity existingActivity, final ExoSocialActivity baseComment,
                                                  final int limit) {

    boolean created = startSynchronization();
    try {
      return super.getOlderComments(existingActivity, baseComment, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SortedSet<ActivityProcessor> getActivityProcessors() {

    boolean created = startSynchronization();
    try {
      return super.getActivityProcessors();
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateActivity(final ExoSocialActivity existingActivity) throws ActivityStorageException {

    boolean created = startSynchronization();
    try {
      super.updateActivity(existingActivity);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfSpaceActivities(Identity spaceIdentity) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfSpaceActivities(spaceIdentity);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfSpaceActivitiesForUpgrade(Identity spaceIdentity) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfSpaceActivitiesForUpgrade(spaceIdentity);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getSpaceActivities(Identity spaceIdentity, int index, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getSpaceActivities(spaceIdentity, index, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getSpaceActivitiesForUpgrade(Identity spaceIdentity, int index, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getSpaceActivitiesForUpgrade(spaceIdentity, index, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, int offset, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getActivitiesByPoster(posterIdentity, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfActivitiesByPoster(Identity posterIdentity) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfActivitiesByPoster(posterIdentity);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfActivitiesByPoster(Identity posterIdentity, Identity ownerIdentity) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfActivitiesByPoster(posterIdentity, ownerIdentity);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getNewerOnSpaceActivities(Identity spaceIdentity,
                                                           ExoSocialActivity baseActivity,
                                                           int limit) {
    boolean created = startSynchronization();
    try {
      return super.getNewerOnSpaceActivities(spaceIdentity, baseActivity, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity,
                                               ExoSocialActivity baseActivity) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerOnSpaceActivities(spaceIdentity, baseActivity);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getOlderOnSpaceActivities(Identity spaceIdentity,
                                                           ExoSocialActivity baseActivity,
                                                           int limit) {
    boolean created = startSynchronization();
    try {
      return super.getOlderOnSpaceActivities(spaceIdentity, baseActivity, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfOlderOnSpaceActivities(Identity spaceIdentity,
                                               ExoSocialActivity baseActivity) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderOnSpaceActivities(spaceIdentity, baseActivity);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity, Long sinceTime) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerOnSpaceActivities(spaceIdentity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public int getNumberOfUpdatedOnActivityFeed(Identity owner, ActivityUpdateFilter filter) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfUpdatedOnActivityFeed(owner, filter);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public int getNumberOfMultiUpdated(Identity owner, Map<String, Long> sinceTimes) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfMultiUpdated(owner, sinceTimes);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public List<ExoSocialActivity> getNewerUserActivities(Identity owner, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getNewerUserActivities(owner, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  
  @Override
  public List<ExoSocialActivity> getNewerUserSpacesActivities(Identity owner, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getNewerUserSpacesActivities(owner, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  
  @Override
  public List<ExoSocialActivity> getNewerActivitiesOfConnections(Identity owner, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getNewerActivitiesOfConnections(owner, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  public List<ExoSocialActivity> getNewerFeedActivities(Identity owner, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getNewerFeedActivities(owner, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  public List<ExoSocialActivity> getNewerSpaceActivities(Identity owner, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getNewerSpaceActivities(owner, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public int getNumberOfUpdatedOnUserActivities(Identity owner, ActivityUpdateFilter filter) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfUpdatedOnUserActivities(owner, filter);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public int getNumberOfUpdatedOnSpaceActivities(Identity owner, ActivityUpdateFilter filter) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfUpdatedOnSpaceActivities(owner, filter);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public int getNumberOfUpdatedOnActivitiesOfConnections(Identity owner, ActivityUpdateFilter filter) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfUpdatedOnActivitiesOfConnections(owner, filter);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public int getNumberOfUpdatedOnUserSpacesActivities(Identity owner, ActivityUpdateFilter filter) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfUpdatedOnUserSpacesActivities(owner, filter);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public List<ExoSocialActivity> getOlderFeedActivities(Identity owner, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getOlderFeedActivities(owner, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public List<ExoSocialActivity> getOlderUserActivities(Identity owner, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getOlderUserActivities(owner, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public List<ExoSocialActivity> getOlderUserSpacesActivities(Identity owner, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getOlderUserSpacesActivities(owner, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public List<ExoSocialActivity> getOlderActivitiesOfConnections(Identity owner, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getOlderActivitiesOfConnections(owner, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public List<ExoSocialActivity> getOlderSpaceActivities(Identity owner, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getOlderSpaceActivities(owner, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, Long sinceTime) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderOnActivityFeed(ownerIdentity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, Long sinceTime) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderOnUserActivities(ownerIdentity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, Long sinceTime) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderOnActivitiesOfConnections(ownerIdentity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, Long sinceTime) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderOnUserSpacesActivities(ownerIdentity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public int getNumberOfOlderOnSpaceActivities(Identity ownerIdentity, Long sinceTime) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderOnSpaceActivities(ownerIdentity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public List<ExoSocialActivity> getNewerComments(ExoSocialActivity existingActivity, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getNewerComments(existingActivity, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public List<ExoSocialActivity> getOlderComments(ExoSocialActivity existingActivity, Long sinceTime, int limit) {
    boolean created = startSynchronization();
    try {
      return super.getOlderComments(existingActivity, sinceTime, limit);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, Long sinceTime) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfNewerComments(existingActivity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }
  }

  @Override
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, Long sinceTime) {
    boolean created = startSynchronization();
    try {
      return super.getNumberOfOlderComments(existingActivity, sinceTime);
    }
    finally {
      stopSynchronization(created);
    }
  }
}
