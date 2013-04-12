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

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.application.RelationshipPublisher;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.manager.RelationshipManagerImpl;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;

public class CommentedAndLikedActivitiesTest extends AbstractCoreTest {
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
  
  @MaxQueryNumber(5)
  public void testGetCommentedActivities() throws Exception {
    // root post one activity on his stream
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root's activity");
    activityStorage.saveActivity(rootIdentity, rootActivity);
    
    {
      List<ExoSocialActivity> rootActivities = activityStorage.getUserActivities(rootIdentity, 0, 5);
      assertEquals(1, rootActivities.size());
      
      List<ExoSocialActivity> maryActivities = activityStorage.getUserActivities(maryIdentity, 0, 5);
      assertEquals(0, maryActivities.size());
      
      // mary post a comment on root's activity
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("Hello world");
      comment.setUserId(maryIdentity.getId());
      activityStorage.saveComment(rootActivity, comment);
      
      maryActivities = activityStorage.getUserActivities(maryIdentity, 0, 5);
      assertEquals(1, maryActivities.size());
      
      activityStorage.deleteComment(rootActivity.getId(), comment.getId());
      maryActivities = activityStorage.getUserActivities(maryIdentity, 0, 5);
      assertEquals(0, maryActivities.size());
    }
    
    {
      List<ExoSocialActivity> rootActivities = activityStorage.getUserActivities(rootIdentity, 0, 5);
      assertEquals(1, rootActivities.size());
      
      List<ExoSocialActivity> maryActivities = activityStorage.getUserActivities(maryIdentity, 0, 5);
      assertEquals(0, maryActivities.size());
      
      // mary post a comment on root's activity
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("Comment 1");
      comment.setUserId(maryIdentity.getId());
      activityStorage.saveComment(rootActivity, comment);
      
      maryActivities = activityStorage.getUserActivities(maryIdentity, 0, 5);
      assertEquals(1, maryActivities.size());
      
      // mary post more comment on root's activity
      rootActivity = activityStorage.getUserActivities(rootIdentity, 0, 5).get(0);
      ExoSocialActivity comment1 = new ExoSocialActivityImpl();
      comment1.setTitle("Comment 2");
      comment1.setUserId(maryIdentity.getId());
      activityStorage.saveComment(rootActivity, comment1);
      
      maryActivities = activityStorage.getUserActivities(maryIdentity, 0, 5);
      assertEquals(1, maryActivities.size());
      
      activityStorage.deleteComment(rootActivity.getId(), comment1.getId());
      maryActivities = activityStorage.getUserActivities(maryIdentity, 0, 5);
      assertEquals(1, maryActivities.size());  
      
      activityStorage.deleteComment(rootActivity.getId(), comment.getId());
      maryActivities = activityStorage.getUserActivities(maryIdentity, 0, 5);
      assertEquals(0, maryActivities.size());  
    }
    
    tearDownActivityList.add(activityStorage.getUserActivities(rootIdentity, 0, 5).get(0));
  }
  
  @MaxQueryNumber(5)
  public void testGetLikedActivities() throws Exception {
    // root post one activity on his stream
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("Hello");
    activityStorage.saveActivity(rootIdentity, rootActivity);

    List<ExoSocialActivity> rootActivities = activityStorage.getUserActivities(rootIdentity, 0, 5);
    assertEquals(1, rootActivities.size());
    
    List<ExoSocialActivity> maryActivities = activityStorage.getUserActivities(maryIdentity, 0, 5);
    assertEquals(0, maryActivities.size());
    
    // mary like root's activity
    ExoSocialActivity gotActivity = activityStorage.getUserActivities(rootIdentity, 0, 5).get(0);
    String[] likeIdentityIds = gotActivity.getLikeIdentityIds();
    likeIdentityIds = (String[]) ArrayUtils.add(likeIdentityIds, maryIdentity.getId());
    gotActivity.setLikeIdentityIds(likeIdentityIds);
    activityStorage.updateActivity(gotActivity);
    
    maryActivities = activityStorage.getUserActivities(maryIdentity, 0, 5);
    assertEquals(1, maryActivities.size());
    
    rootActivity = activityStorage.getUserActivities(rootIdentity, 0, 5).get(0);
    
    rootActivity.setLikeIdentityIds(new String[] {});
    
    activityStorage.updateActivity(rootActivity);
    
    maryActivities = activityStorage.getUserActivities(maryIdentity, 0, 5);
    assertEquals(0, maryActivities.size());
    
    tearDownActivityList.add(gotActivity);
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
