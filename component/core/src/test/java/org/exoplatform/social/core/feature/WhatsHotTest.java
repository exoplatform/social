/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
import java.util.Iterator;
import java.util.List;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.application.RelationshipPublisher;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.manager.RelationshipManagerImpl;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.storage.impl.StorageUtils;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;

public class WhatsHotTest extends AbstractCoreTest {
  private IdentityStorage identityStorage;
  private ActivityStorageImpl activityStorage;
  private RelationshipManagerImpl relationshipManager;
  private RelationshipPublisher publisher;
  
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Relationship> tearDownRelationshipList;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityStorage = (ActivityStorageImpl) getContainer().getComponentInstanceOfType(ActivityStorageImpl.class);
    relationshipManager = (RelationshipManagerImpl) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    

    assertNotNull(identityStorage);
    assertNotNull(activityStorage);
    assertNotNull(relationshipManager);
    
    
    

    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");

    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    assertNotNull(rootIdentity.getId());
    assertNotNull(johnIdentity.getId());
    assertNotNull(maryIdentity.getId());
    assertNotNull(demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownRelationshipList = new ArrayList<Relationship>();
  }

  @Override
  protected void tearDown() throws Exception {

    for (ExoSocialActivity activity : tearDownActivityList) {
      activityStorage.deleteActivity(activity.getId());
    }
    
    for (Relationship relationship : tearDownRelationshipList) {
      relationshipManager.delete(relationship);
    }

    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);

    super.tearDown();
  }
  
  @MaxQueryNumber(1500)
  public void testUserActivityTab() throws Exception {
    // fill 10 activities
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(rootIdentity, activity);
      
    }

    // remove 5 activities
    List<ExoSocialActivity> result = activityStorage.getUserActivities(rootIdentity);
    Iterator<ExoSocialActivity> it = result.iterator();

    for (int i = 0; i < 5; ++i) {
      activityStorage.deleteActivity(it.next().getId());
    }
    
    // fill 10 others
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
    }
    
    while (it.hasNext()) {
      ExoSocialActivity activity = it.next();
      createComment(activity, rootIdentity, 1);
      tearDownActivityList.add(activity);
    }

    List<ExoSocialActivity> activityies = activityStorage.getUserActivities(rootIdentity);
    int i = 0;
    int[] values = {0, 1, 2, 3, 4, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
    for (ExoSocialActivity activity : activityies) {
      assertEquals("title " + values[i], activity.getTitle());
      ++i;
    }
  }
  
  @MaxQueryNumber(4500)
  public void testAllActivityTab() throws Exception {
    // fill 5 activities
    for (int i = 0; i < 5; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(rootIdentity, activity);
    }

    Iterator<ExoSocialActivity> it = activityStorage.getActivityFeed(rootIdentity, 0, 5).iterator();

    // fill 10 others
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
    }
    
    //creates comments
    while (it.hasNext()) {
      ExoSocialActivity activity = it.next();
      createComment(activity, rootIdentity, 1);
      tearDownActivityList.add(activity);
    }

    List<ExoSocialActivity> activityies = activityStorage.getActivityFeed(rootIdentity, 0, 15);
    int i = 0;
    //int[] values = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 4, 3, 2, 1, 0};
    int[] values = {0, 1, 2, 3, 4, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
    for (ExoSocialActivity activity : activityies) {
      assertEquals("title " + values[i], activity.getTitle());
      ++i;
    }
  }
  
  @MaxQueryNumber(1500)
  public void testMySpaceTab() throws Exception {
    // fill 5 activities
    for (int i = 0; i < 5; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(rootIdentity, activity);
      
    }

    //5 activities
    Iterator<ExoSocialActivity> it = activityStorage.getUserActivities(rootIdentity).iterator();

    // fill 10 others
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
    }
    
    //creates comments
    while (it.hasNext()) {
      ExoSocialActivity activity = it.next();
      createComment(activity, rootIdentity, 1);
      tearDownActivityList.add(activity);
    }

    List<ExoSocialActivity> activityies = activityStorage.getUserActivities(rootIdentity);
    int i = 0;
    int[] values = {0, 1, 2, 3, 4, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
    for (ExoSocialActivity activity : activityies) {
      assertEquals("title " + values[i], activity.getTitle());
      ++i;
    }
  }
  
  @MaxQueryNumber(500)
  public void testConnectionsTab() throws Exception {
    publisher = (RelationshipPublisher) this.getContainer().getComponentInstanceOfType(RelationshipPublisher.class);
    assertNotNull(publisher);
    relationshipManager.addListenerPlugin(publisher);
    connectIdentities(demoIdentity, johnIdentity, true);

    List<ExoSocialActivity> list = activityStorage.getActivitiesOfConnections(demoIdentity, 0, 2);
    
    assertEquals(1, list.size());
    ExoSocialActivity firstActivity = list.get(0);
    
    //
    list = activityStorage.getActivitiesOfConnections(demoIdentity, 0, 2);
    assertEquals(1, list.size());
    
    assertEquals(firstActivity.getTitle(), list.get(0).getTitle());
   
    tearDownActivityList.add(firstActivity);
    relationshipManager.unregisterListener(publisher);
  }
  
  @MaxQueryNumber(500)
  public void testViewerOwnerActivities() throws Exception {
    publisher = (RelationshipPublisher) this.getContainer().getComponentInstanceOfType(RelationshipPublisher.class);
    assertNotNull(publisher);
    relationshipManager.addListenerPlugin(publisher);
    connectIdentities(demoIdentity, johnIdentity, true);

    List<ExoSocialActivity> list = activityStorage.getActivities(demoIdentity, johnIdentity, 0, 2);
    
    //only show demo's activity when John is viewer
    assertEquals(1, list.size());
    
    tearDownActivityList.addAll(list);
    relationshipManager.unregisterListener(publisher);
  }
  
  @MaxQueryNumber(500)
  public void testViewerOwnerActivitiesSpecialCase() throws Exception {
    publisher = (RelationshipPublisher) this.getContainer().getComponentInstanceOfType(RelationshipPublisher.class);
    assertNotNull(publisher);
    relationshipManager.addListenerPlugin(publisher);
    connectIdentities(demoIdentity, johnIdentity, true);

    List<ExoSocialActivity> list = activityStorage.getActivities(demoIdentity, johnIdentity, 0, 10);
    
    //only show demo's activity when John is viewer
    assertEquals(1, list.size());
    
    tearDownActivityList.addAll(list);
    relationshipManager.unregisterListener(publisher);
  }
  
  @MaxQueryNumber(500)
  public void testViewerOwnerMentionerActivities() throws Exception {
    publisher = (RelationshipPublisher) this.getContainer().getComponentInstanceOfType(RelationshipPublisher.class);
    assertNotNull(publisher);
    relationshipManager.addListenerPlugin(publisher);
    connectIdentities(demoIdentity, johnIdentity, true);

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title @demo hi");
    activityStorage.saveActivity(rootIdentity, activity);
    
    List<ExoSocialActivity> list = activityStorage.getActivities(demoIdentity, johnIdentity, 0, 10);
    
    //only show demo's activity when John is viewer
    assertEquals(1, list.size());
    
    tearDownActivityList.addAll(list);
    relationshipManager.unregisterListener(publisher);
  }
  
  @MaxQueryNumber(500)
  public void testViewerOwnerPosterActivities() throws Exception {

    //
    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    activity1.setTitle("title @demo hi");
    activityStorage.saveActivity(rootIdentity, activity1);
    
    //owner poster comment
    ExoSocialActivity activity2 = new ExoSocialActivityImpl();
    activity2.setTitle("john title");
    activityStorage.saveActivity(rootIdentity, activity2);
    
    createComment(activity2, demoIdentity, 2);
    
    List<ExoSocialActivity> list = activityStorage.getActivities(demoIdentity, johnIdentity, 0, 10);
    
    //only show activity (not comment) posted by demo
    assertEquals(0, list.size());
    
    //john view root'as --> only show activity (not comment) posted by root
    list = activityStorage.getActivities(rootIdentity, johnIdentity, 0, 10);
    assertEquals(2, list.size());
    
    tearDownActivityList.addAll(list);
  }
  
  @MaxQueryNumber(500)
  public void testViewerOwnerAllCases() throws Exception {
    publisher = (RelationshipPublisher) this.getContainer().getComponentInstanceOfType(RelationshipPublisher.class);
    assertNotNull(publisher);
    relationshipManager.addListenerPlugin(publisher);
    connectIdentities(demoIdentity, johnIdentity, true);

    //
    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    activity1.setTitle("title @demo hi");
    activityStorage.saveActivity(rootIdentity, activity1);
    
    //
    ExoSocialActivity activity2 = new ExoSocialActivityImpl();
    activity2.setTitle("john title");
    activityStorage.saveActivity(rootIdentity, activity2);
    
    //owner poster comment
    createComment(activity2, demoIdentity, 2);
    
    List<ExoSocialActivity> list = activityStorage.getActivities(demoIdentity, johnIdentity, 0, 10);
    //only show demo's activity when John is viewer
    assertEquals(1, list.size());
    
    tearDownActivityList.addAll(list);
    relationshipManager.unregisterListener(publisher);
  }
  
  /**
   * Creates a comment to an existing activity.
   *
   * @param existingActivity the existing activity
   * @param posterIdentity the identity who comments
   * @param number the number of comments
   */
  private void createComment(ExoSocialActivity existingActivity, Identity posterIdentity, int number) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment " + i);
      comment.setUserId(posterIdentity.getId());
      activityStorage.saveComment(existingActivity, comment);
    }
  }
  
  /**
   * Connects 2 identities, if toConfirm = true, they're connected. If false, in pending connection type.
   *
   * @param senderIdentity the identity who sends connection request
   * @param receiverIdentity the identity who receives connection request
   * @param beConfirmed boolean value
   */
  private void connectIdentities(Identity senderIdentity, Identity receiverIdentity, boolean beConfirmed) {
    relationshipManager.inviteToConnect(senderIdentity, receiverIdentity);
    if (beConfirmed) {
      relationshipManager.confirm(receiverIdentity, senderIdentity);
    }

    tearDownRelationshipList.add(relationshipManager.get(senderIdentity, receiverIdentity));
  }
  
}
