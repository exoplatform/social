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
package org.exoplatform.social.core.feature;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter;
import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter.ActivityFilterType;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.manager.RelationshipManagerImpl;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class ActivityUpdateFilterTest extends AbstractCoreTest {
  private IdentityStorage identityStorage;
  private ActivityStorageImpl activityStorage;
  private RelationshipStorageImpl relationshipStorage;
  private RelationshipManagerImpl relationshipManager;
  private SpaceStorage spaceStorage;
  
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Relationship> tearDownRelationshipList;
  private List<Space>  tearDownSpaceList;

  private Identity maryIdentity;
  private Identity demoIdentity;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityStorage = (ActivityStorageImpl) getContainer().getComponentInstanceOfType(ActivityStorageImpl.class);
    relationshipStorage = (RelationshipStorageImpl) getContainer().getComponentInstanceOfType(RelationshipStorageImpl.class);
    relationshipManager = (RelationshipManagerImpl) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceStorage = (SpaceStorage) this.getContainer().getComponentInstanceOfType(SpaceStorage.class);
    
    assertNotNull(identityStorage);
    assertNotNull(activityStorage);
    assertNotNull(relationshipManager);
    assertNotNull(relationshipStorage);
    assertNotNull(spaceStorage);
    
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");

    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    assertNotNull(maryIdentity.getId());
    assertNotNull(demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownRelationshipList = new ArrayList<Relationship>();
    tearDownSpaceList = new ArrayList<Space>();
  }

  @Override
  protected void tearDown() throws Exception {

    for (ExoSocialActivity activity : tearDownActivityList) {
      activityStorage.deleteActivity(activity.getId());
    }
    
    for (Relationship relationship : tearDownRelationshipList) {
      relationshipManager.delete(relationship);
    }

    for (Space space : tearDownSpaceList) {
      spaceStorage.deleteSpace(space.getId());
    }
    
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);

    super.tearDown();
  }

  
  public void testConnectionsActivities() throws Exception {
    Long fromSinceTime = Calendar.getInstance().getTimeInMillis();
    Long toSinceTime = fromSinceTime + 1000;
    ActivityFilterType.CONNECTIONS_ACTIVITIES.fromSinceTime(fromSinceTime).toSinceTime(toSinceTime);
    
    assertEquals(fromSinceTime, ActivityFilterType.CONNECTIONS_ACTIVITIES.fromSinceTime());
    assertEquals(toSinceTime, ActivityFilterType.CONNECTIONS_ACTIVITIES.toSinceTime());
    
    // check mary' activities
    ActivityUpdateFilter filter = new ActivityUpdateFilter(false);
    int numberOfActivitiesUpdated = activityStorage.getNumberOfUpdatedOnActivitiesOfConnections(maryIdentity, filter);
    assertEquals(0, numberOfActivitiesUpdated);
    
    // make connection demo and mary
    Relationship rel = relationshipManager.create(demoIdentity, maryIdentity);
    relationshipManager.save(rel);
    //
    relationshipManager.confirm(rel);
    tearDownRelationshipList.add(rel);
    
    fromSinceTime = Calendar.getInstance().getTimeInMillis();
    ActivityFilterType.CONNECTIONS_ACTIVITIES.fromSinceTime(fromSinceTime);
    
    // fill 10 activities
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(demoIdentity, activity);
      tearDownActivityList.add(activity);
    }
    
    //
    toSinceTime = Calendar.getInstance().getTimeInMillis();
    ActivityFilterType.CONNECTIONS_ACTIVITIES.toSinceTime(toSinceTime);
    
    // check mary' activities
    filter = new ActivityUpdateFilter(false);
    numberOfActivitiesUpdated = activityStorage.getNumberOfUpdatedOnActivitiesOfConnections(maryIdentity, filter);
    assertEquals(10, numberOfActivitiesUpdated);
    
    // refresh activities
    fromSinceTime = toSinceTime = Calendar.getInstance().getTimeInMillis();
    ActivityFilterType.CONNECTIONS_ACTIVITIES.fromSinceTime(fromSinceTime).toSinceTime(toSinceTime);
    filter = new ActivityUpdateFilter(false);
    numberOfActivitiesUpdated = activityStorage.getNumberOfUpdatedOnActivitiesOfConnections(maryIdentity, filter);
    assertEquals(0, numberOfActivitiesUpdated);
  }
  
  public void testUserActivities() throws Exception {
    Long fromSinceTime = Calendar.getInstance().getTimeInMillis();
    Long toSinceTime = fromSinceTime + 1000;
    ActivityFilterType.USER_ACTIVITIES.fromSinceTime(fromSinceTime).toSinceTime(toSinceTime);
    
    assertEquals(fromSinceTime, ActivityFilterType.USER_ACTIVITIES.fromSinceTime());
    assertEquals(toSinceTime, ActivityFilterType.USER_ACTIVITIES.toSinceTime());
  }
  
  public void testUserSpaceActivities() throws Exception {
    Long fromSinceTime = Calendar.getInstance().getTimeInMillis();
    Long toSinceTime = fromSinceTime + 1000;
    ActivityFilterType.USER_SPACE_ACTIVITIES.fromSinceTime(fromSinceTime).toSinceTime(toSinceTime);
    
    assertEquals(fromSinceTime, ActivityFilterType.USER_SPACE_ACTIVITIES.fromSinceTime());
    assertEquals(toSinceTime, ActivityFilterType.USER_SPACE_ACTIVITIES.toSinceTime());
    
    // check mary' activities
    ActivityUpdateFilter filter = new ActivityUpdateFilter(false);
    int numberOfActivitiesUpdated = activityStorage.getNumberOfUpdatedOnUserSpacesActivities(demoIdentity, filter);
    assertEquals(0, numberOfActivitiesUpdated);
    
    Space space = getSpaceInstance();
    spaceStorage.saveSpace(space, true);
    tearDownSpaceList.add(space);
    
    //
    SpaceIdentityProvider spaceIdentityProvider = (SpaceIdentityProvider) getContainer().getComponentInstanceOfType(SpaceIdentityProvider.class);
    Identity spaceIdentity = spaceIdentityProvider.createIdentity(space);
    identityStorage.saveIdentity(spaceIdentity);
    
    // demo post 5 activities on space
    for (int i = 0; i < 5; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("post on space " + i);
      activity.setUserId(demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity, activity);
      tearDownActivityList.add(activity);
    }
    
    // check demo' activities
    toSinceTime = Calendar.getInstance().getTimeInMillis();
    ActivityFilterType.USER_SPACE_ACTIVITIES.toSinceTime(toSinceTime);
    
    filter = new ActivityUpdateFilter(false);
    numberOfActivitiesUpdated = activityStorage.getNumberOfUpdatedOnUserSpacesActivities(demoIdentity, filter);
    assertEquals(5, numberOfActivitiesUpdated);
    
    // refresh activities
    fromSinceTime = toSinceTime = Calendar.getInstance().getTimeInMillis();
    ActivityFilterType.USER_SPACE_ACTIVITIES.fromSinceTime(fromSinceTime).toSinceTime(toSinceTime);
    filter = new ActivityUpdateFilter(false);
    numberOfActivitiesUpdated = activityStorage.getNumberOfUpdatedOnUserSpacesActivities(demoIdentity, filter);
    assertEquals(0, numberOfActivitiesUpdated);
  }
  
  public void testSpaceActivities() throws Exception {
    Long fromSinceTime = Calendar.getInstance().getTimeInMillis();
    Long toSinceTime = fromSinceTime + 1000;
    ActivityFilterType.SPACE_ACTIVITIES.fromSinceTime(fromSinceTime).toSinceTime(toSinceTime);
    
    assertEquals(fromSinceTime, ActivityFilterType.SPACE_ACTIVITIES.fromSinceTime());
    assertEquals(toSinceTime, ActivityFilterType.SPACE_ACTIVITIES.toSinceTime());
  }
  
  public void testAddExcludedActivities() throws Exception {
    ActivityUpdateFilter filter = new ActivityUpdateFilter(false);
    
    filter.addExcludedActivities("1", "2", "3");
    
    assertEquals(3, filter.excludedActivities().length);
    filter.addExcludedActivities("1", "2", "3");
    assertEquals(3, filter.excludedActivities().length);
    filter.addExcludedActivities("1", "2", "4");
    assertEquals(4, filter.excludedActivities().length);
  }
  
  /**
   * Gets an instance of Space.
   *
   * @return an instance of space
   */
  private Space getSpaceInstance() {
    Space space = new Space();
    space.setDisplayName("myspace");
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space");
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space");
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] {"john", "demo"};
    space.setManagers(managers);
    return space;
  }
  
  
}
