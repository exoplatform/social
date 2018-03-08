/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.storage.dao;

import java.util.Date;
import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.jpa.storage.entity.ActivityEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityStorageException;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 18, 2015  
 */
public interface ActivityDAO extends GenericDAO<ActivityEntity, Long> {
  
  /**
   * 
   * @param owner the identity
   * @param offset the offset index
   * @param limit the maximum number of ActivityEntity to load
   * @return the activity entities
   * @throws ActivityStorageException if has any error
   */
  List<Long> getUserActivities(Identity owner, long offset, long limit) throws ActivityStorageException;
  
  
  /**
   * Gets Ids for User stream
   * 
   * @param owner the Identity
   * @param offset the offset index
   * @param limit maximum number item to load
   * @return the list of activity id
   * @throws ActivityStorageException if has any error
   */
  List<String> getUserIdsActivities(Identity owner, long offset, long limit) throws ActivityStorageException;
  
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @return the number of activities
   */
  int getNumberOfUserActivities(Identity ownerIdentity);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the beginning time
   * @param limit the number of entities to load
   * @return list of activity entities
   */
  List<Long> getNewerOnUserActivities(Identity ownerIdentity, long sinceTime, int limit);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the beginning time
   * @return number of activities
   */
  int getNumberOfNewerOnUserActivities(Identity ownerIdentity, long sinceTime);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the ending time
   * @param limit the number of entities to load
   * @return list of activity entities
   */
  List<Long> getOlderOnUserActivities(Identity ownerIdentity, long sinceTime, int limit);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the end time
   * @return number of activities
   */
  int getNumberOfOlderOnUserActivities(Identity ownerIdentity, long sinceTime);
  
  /**
   * 
   * @param owner the Identity
   * @param offset the start index
   * @param limit number of activity entities to load
   * @return list of activity entities
   * @throws ActivityStorageException if has any error
   */
  List<Long> getSpaceActivities(Identity owner, long offset, long limit) throws ActivityStorageException;
  
  /**
   * 
   * @param owner the Identity
   * @param offset the start index
   * @param limit max number activity Id to load
   * @return list of activity Ids
   * @throws ActivityStorageException if has any error
   */
  List<String> getSpaceActivityIds(Identity owner, long offset, long limit) throws ActivityStorageException;
  
  /**
   * 
   * @param spaceIdentity the space Identity
   * @return number of activities
   */
  int getNumberOfSpaceActivities(Identity spaceIdentity);
  
  /**
   * 
   * @param spaceIdentity the space Identity
   * @param sinceTime the beginning time
   * @param limit max number of entities to load
   * @return list of activity entities
   */
  List<Long> getNewerOnSpaceActivities(Identity spaceIdentity, long sinceTime, int limit);
  
  /**
   * 
   * @param spaceIdentity the space Identity
   * @param sinceTime the beginning time
   * @return number of activities
   */
  int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity, long sinceTime);
  
  /**
   * 
   * @param spaceIdentity the space Identity
   * @param sinceTime the beginning time
   * @param limit max number of entities to load
   * @return list of activity entities
   */
  List<Long> getOlderOnSpaceActivities(Identity spaceIdentity, long sinceTime, int limit);
  
  /**
   * 
   * @param spaceIdentity the space Identity
   * @param sinceTime the beginning time
   * @return number of activities
   */
  int getNumberOfOlderOnSpaceActivities(Identity spaceIdentity, long sinceTime);
  
  /**
   * 
   * @param owner the Identity
   * @param viewer the viewer Identity
   * @param offset the start index
   * @param limit max number of entities to load
   * @return list of activity entities
   * @throws ActivityStorageException if has any error
   */
  List<Long> getActivities(Identity owner, Identity viewer, long offset, long limit) throws ActivityStorageException;

  /**
   * 
   * @param ownerIdentity the Identity
   * @param offset the start index
   * @param limit max number of entities to load
   * @param spaceIds list of space ids
   * @return list of activity entities
   */
  List<Long> getActivityFeed(Identity ownerIdentity, int offset, int limit, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param offset the start index
   * @param limit max number of ids to load
   * @param spaceIds list of space ids
   * @return list of activity ids
   */
  List<String> getActivityIdsFeed(Identity ownerIdentity, int offset, int limit, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param spaceIds list of space ids
   * @return number of activities
   */
  int getNumberOfActivitesOnActivityFeed(Identity ownerIdentity, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the beginning time
   * @param limit max number of entities to load
   * @param spaceIds list of space ids
   * @return list of activity entities
   */
  List<Long> getNewerOnActivityFeed(Identity ownerIdentity, long sinceTime, int limit, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the beginning time
   * @param spaceIds list of space ids
   * @return number of actvitites
   */
  int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, long sinceTime, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the end time
   * @param limit max number entities to load
   * @param spaceIds list of space ids
   * @return list of activity entities
   */
  List<Long> getOlderOnActivityFeed(Identity ownerIdentity, long sinceTime, int limit, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the end time
   * @param spaceIds list of space ids
   * @return number of activities
   */
  int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, long sinceTime, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param offset the start index
   * @param limit max number of entities to load
   * @param spaceIds list of space ids
   * @return lsit of activity entities
   */
  List<Long> getUserSpacesActivities(Identity ownerIdentity, int offset, int limit, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param offset the start index
   * @param limit max number ids to load
   * @param spaceIds list of space ids
   * @return list of activity ids
   */
  List<String> getUserSpacesActivityIds(Identity ownerIdentity, int offset, int limit, List<String> spaceIds);
  
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param spaceIds list of space ids
   * @return the number of activities
   */
  int getNumberOfUserSpacesActivities(Identity ownerIdentity, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the beginning time
   * @param limit max number item to load
   * @param spaceIds list of space ids
   * @return list of activity entities
   */
  List<Long> getNewerOnUserSpacesActivities(Identity ownerIdentity, long sinceTime, int limit, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the beginning time
   * @param spaceIds list of space ids
   * @return number of activities
   */
  int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, long sinceTime, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the end time
   * @param limit max number items to load
   * @param spaceIds list of space ids
   * @return list of activity entities
   */
  List<Long> getOlderOnUserSpacesActivities(Identity ownerIdentity, long sinceTime, int limit, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the end time
   * @param spaceIds list of space ids
   * @return number of activities
   */
  int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, long sinceTime, List<String> spaceIds);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param offset the start index
   * @param limit max number of items to load
   * @return list of activity entities
   */
  List<Long> getActivitiesOfConnections(Identity ownerIdentity, int offset, int limit);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param offset the start index
   * @param limit max number items to load
   * @return list of activity Ids
   */
  List<String> getActivityIdsOfConnections(Identity ownerIdentity, int offset, int limit);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @return number of activities
   */
  int getNumberOfActivitiesOfConnections(Identity ownerIdentity);
  
  /**
   * 
   * @param ownerIdentity  the Identity
   * @param sinceTime the beginning time
   * @param limit max number items to load
   * @return list of activity entities
   */
  List<Long> getNewerOnActivitiesOfConnections(Identity ownerIdentity, long sinceTime, long limit);
  
  /**
   * 
   * @param ownerIdentity  the Identity
   * @param sinceTime the start time
   * @return number of activities
   */
  int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, long sinceTime);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the end time
   * @param limit max items to load
   * @return list of activity entities
   */
  List<Long> getOlderOnActivitiesOfConnections(Identity ownerIdentity, long sinceTime, int limit);
  
  /**
   * 
   * @param ownerIdentity the Identity
   * @param sinceTime the end time
   * @return number of activities
   */
  int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, long sinceTime);

  /**
   * @param posterIdentity the Identity
   * @param offset the start index
   * @param limit max number items to load
   * @param activityTypes the activity Type
   * @return list of activities
   */
  List<Long> getActivitiesByPoster(Identity posterIdentity, int offset, int limit, String... activityTypes);

  /**
   * @param posterIdentity the Identity
   * @param activityTypes the activity Type
   * @return number of activities
   */
  int getNumberOfActivitiesByPoster(Identity posterIdentity, String... activityTypes);

  /**
   * @param activityId the Id of activity
   * @return number of comments
   */
  long getNumberOfComments(long activityId);

  /**
   *
   * @param activityId the Id of activity
   * @param offset the start index
   * @param limit max comments to load
   * @return list of activity entities represent comment
   */
  List<ActivityEntity> getComments(long activityId, int offset, int limit);

  /**
   *
   * @param activityId the id of activity
   * @param sinceTime the start time
   * @param offset the start index
   * @param limit max items to load
   * @return list of activity entities
   */
  List<ActivityEntity> getNewerComments(long activityId, Date sinceTime, int offset, int limit);

  /**
   *
   * @param activityId the Id of activity
   * @param sinceTime the end time
   * @param offset the start index
   * @param limit max items to load
   * @return list of activity entities
   */
  List<ActivityEntity> getOlderComments(long activityId, Date sinceTime, int offset, int limit);

  /**
   * Get Activity of comment
   * @param commentId the comment Id
   * @return activity entity
   */
  ActivityEntity getParentActivity(long commentId);

  /**
   * @return all activities
   */
  List<ActivityEntity> getAllActivities();

  /**
   * delete an activity by ownerId
   * @param ownerId the owner Id
   */
  void deleteActivitiesByOwnerId(String ownerId);


  /**
   * find Sub Comments of some comments
   * 
   * @param ids
   * @return
   */
  List<ActivityEntity> findCommentsOfActivities(List<Long> ids);


  /**
   * Get list of activities switch list of IDs
   * 
   * @param activityIds
   * @return
   */
  List<ActivityEntity> findActivities(List<Long> activityIds);

}
