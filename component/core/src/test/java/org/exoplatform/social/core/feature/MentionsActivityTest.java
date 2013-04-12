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
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;

public class MentionsActivityTest extends AbstractCoreTest {
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
  public void testOneMention() throws Exception {
    // one mentioner on title
    final String TITLE = "activity on root stream";
    final String TITLE1 = "activity on root stream that contain @mary as mentioner";
    
    // root post one activity on his stream
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle(TITLE);
    activityStorage.saveActivity(rootIdentity, rootActivity);
    
    List<ExoSocialActivity> rootActivities = activityStorage.getActivityFeed(rootIdentity, 0, 5);
    assertEquals(1, rootActivities.size());
    assertEquals(TITLE, rootActivities.get(0).getTitle());
    
    // not have mentions yet
    List<ExoSocialActivity> maryActivities = activityStorage.getActivityFeed(maryIdentity, 0, 5);
    assertEquals(0, maryActivities.size());
    
    // root post a comment that add mary as mentioner
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("Hello @mary");
    comment.setUserId(rootIdentity.getId());
    activityStorage.saveComment(rootActivity, comment);
    
    maryActivities = activityStorage.getActivityFeed(maryIdentity, 0, 5);
    assertEquals(1, maryActivities.size());
    assertEquals(TITLE, maryActivities.get(0).getTitle());
    
    // root post more activity on his stream and contain mention in the title
    ExoSocialActivity rootActivity1 = new ExoSocialActivityImpl();
    rootActivity1.setTitle(TITLE1);
    activityStorage.saveActivity(rootIdentity, rootActivity1);
    
    maryActivities = activityStorage.getActivityFeed(maryIdentity, 0, 5);
    assertEquals(2, maryActivities.size());
    assertEquals(TITLE, maryActivities.get(1).getTitle());
    assertEquals(TITLE1, maryActivities.get(0).getTitle());
    
    // root remove comment on first activity
    activityStorage.deleteComment(rootActivity.getId(), comment.getId());
    maryActivities = activityStorage.getActivityFeed(maryIdentity, 0, 5);
    assertEquals(1, maryActivities.size());
    assertEquals(TITLE1, maryActivities.get(0).getTitle());
    
    tearDownActivityList.add(rootActivity);
    tearDownActivityList.add(rootActivity1);
  }
  
  @MaxQueryNumber(5)
  public void testGreaterOneMention() throws Exception {
    // greater than one mentioner on title
    final String TITLE2 = "root add @mary as mentioner. Hi @mary and @demo";
    
    // root post one activity on his stream
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle(TITLE2);
    activityStorage.saveActivity(rootIdentity, rootActivity);
    
    List<ExoSocialActivity> r = activityStorage.getActivityFeed(maryIdentity, 0, 5);
    assertEquals(1, r.size());
    assertEquals(TITLE2, r.get(0).getTitle());
    
    // root post a comment that add marry as mentioner
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("Hello @mary");
    comment.setUserId(rootIdentity.getId());
    activityStorage.saveComment(rootActivity, comment);
    
    List<ExoSocialActivity> r2 = activityStorage.getActivityFeed(maryIdentity, 0, 5);
    assertEquals(1, r2.size());
    assertEquals(TITLE2, r2.get(0).getTitle());
    
    // root remove comment on activity
    activityStorage.deleteComment(rootActivity.getId(), comment.getId());
    List<ExoSocialActivity> r3 = activityStorage.getActivityFeed(maryIdentity, 0, 5);
    assertEquals(1, r3.size());
    assertEquals(TITLE2, r3.get(0).getTitle());
    
    // root remove activity
    activityStorage.deleteActivity(rootActivity.getId());
    List<ExoSocialActivity> r4 = activityStorage.getActivityFeed(maryIdentity, 0, 5);
    assertEquals(0, r4.size());
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
