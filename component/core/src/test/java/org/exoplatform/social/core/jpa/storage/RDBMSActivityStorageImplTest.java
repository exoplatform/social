/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.jpa.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.StreamItemDAO;
import org.exoplatform.social.core.jpa.storage.entity.StreamItemEntity;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.jpa.test.MaxQueryNumber;
import org.exoplatform.social.core.jpa.test.QueryNumberTest;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;

@QueryNumberTest
public class RDBMSActivityStorageImplTest extends AbstractCoreTest {
  
  private IdentityStorage identityStorage;
  
  private StreamItemDAO streamItemDAO;
  
  private List<ExoSocialActivity> tearDownActivityList;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;


  @Override
  protected void setUp() throws Exception {
    super.setUp();
    identityStorage = getService(IdentityStorage.class);
    streamItemDAO = getService(StreamItemDAO.class);

    assertNotNull(identityStorage);
    assertNotNull(activityStorage);

    rootIdentity = createIdentity("root");
    johnIdentity = createIdentity("john");
    maryIdentity = createIdentity("mary");
    demoIdentity = createIdentity("demo");

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
  }

  @MaxQueryNumber(522)
  public void testGetActivitiesByPoster() {
    ExoSocialActivity activity1 = createActivity(1);
    activity1.setType("TYPE1");
    activityStorage.saveActivity(demoIdentity, activity1);
    tearDownActivityList.add(activity1);
    
    ExoSocialActivity activity2 = createActivity(2);
    activity2.setType("TYPE2");
    activityStorage.saveActivity(demoIdentity, activity2);
    tearDownActivityList.add(activity2);
    
    //
    List<ExoSocialActivity> activities = activityStorage.getActivitiesByPoster(demoIdentity, 0, 10);
    assertEquals(2, activities.size());
    assertEquals(2, activityStorage.getNumberOfActivitiesByPoster(demoIdentity));
    activities = activityStorage.getActivitiesByPoster(demoIdentity, 0, 10, new String[] {"TYPE1"});
    assertEquals(1, activities.size());
  }
  
  @MaxQueryNumber(516)
  public void testSaveActivity() {
    
    ExoSocialActivity activity = createActivity(0);
    //
    activityStorage.saveActivity(demoIdentity, activity);
    
    assertNotNull(activity.getId());
    
    ExoSocialActivity rs = activityStorage.getActivity(activity.getId());
    
    //
    assertTrue(Arrays.asList(rs.getLikeIdentityIds()).contains("demo"));
    
    //
    tearDownActivityList.add(activity);
    
  }
  @MaxQueryNumber(516)
  public void testUpdateActivity() {
    ExoSocialActivity activity = createActivity(1);
    //
    ExoSocialActivity activityCreated = activityStorage.saveActivity(demoIdentity, activity);
    List<StreamItemEntity> streamItems = streamItemDAO.findStreamItemByActivityId(Long.parseLong(activityCreated.getId()));
    
    activityCreated.setTitle("Title after updated");
    
    //update
    
    activityStorage.updateActivity(activityCreated);
    
    ExoSocialActivity res = activityStorage.getActivity(activity.getId());
    // Check that stream Item update date is not modified
    
    List<StreamItemEntity> streamItemsRes = streamItemDAO.findStreamItemByActivityId(Long.parseLong(activityCreated.getId()));

    assertEquals(streamItemsRes.get(0).getUpdatedDate(),streamItems.get(0).getUpdatedDate());

    assertEquals("Title after updated", res.getTitle());
    //
    tearDownActivityList.add(activity);
  }

  @MaxQueryNumber(60)
  public void testUpdateComment() {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("Initial Activity");
    activityStorage.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);

    //
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("comment");
    comment.setUserId(johnIdentity.getId());
    comment.setPosterId(johnIdentity.getId());
    activityStorage.saveComment(activity, comment);

    assertTrue(comment.getPostedTime() == comment.getUpdated().getTime());

    comment.setTitle("comment updated");
    comment.setUpdated(new Date().getTime());
    activityStorage.saveComment(activity, comment);
    comment = activityStorage.getActivity(comment.getId());
    assertEquals("comment updated", comment.getTitle());
    assertTrue(comment.getPostedTime() < comment.getUpdated().getTime());
  }

  @MaxQueryNumber(123)
  public void testUpdateActivityMention() {
    ExoSocialActivity activity = createActivity(1);
    activityStorage.saveActivity(demoIdentity, activity);
    //
    activity = activityStorage.getActivity(activity.getId());
    assertEquals(0, activity.getMentionedIds().length);

    //update
    String processedTitle = "test <a href=\"/portal/classic/profile/root\" rel=\"nofollow\" target=\"_blank\">Root Root</a> " +
        "<a href=\"/portal/classic/profile/john\" rel=\"nofollow\" target=\"_blank\">" + johnIdentity.getProfile().getFullName()
        + "</a>";
    activity.setTitle("test @root @john");
    activityStorage.updateActivity(activity);
    //
    activity = activityStorage.getActivity(activity.getId());
    assertEquals(processedTitle, activity.getTitle());
    assertEquals(2, activity.getMentionedIds().length);
    List<ExoSocialActivity> list = activityStorage.getActivities(rootIdentity,rootIdentity, 0, 10);
    assertEquals(1, list.size());
    list = activityStorage.getActivities(johnIdentity,johnIdentity, 0, 10);
    assertEquals(1, list.size());

    //update remove mention
    activity.setTitle("test @root");
    activityStorage.updateActivity(activity);
    //
    activity = activityStorage.getActivity(activity.getId());
    assertEquals(1, activity.getMentionedIds().length);
    list = activityStorage.getActivities(johnIdentity,johnIdentity, 0, 10);
    assertEquals(0, list.size());

    //add comment
    ExoSocialActivity comment1 = new ExoSocialActivityImpl();
    comment1.setTitle("comment @root @john");
    comment1.setUserId(johnIdentity.getId());
    comment1.setPosterId(johnIdentity.getId());
    activityStorage.saveComment(activity, comment1);
    //
    activity = activityStorage.getActivity(activity.getId());
    assertEquals(2, activity.getMentionedIds().length);
    list = activityStorage.getActivities(rootIdentity,rootIdentity, 0, 10);
    assertEquals(1, list.size());
    list = activityStorage.getActivities(johnIdentity,johnIdentity, 0, 10);
    assertEquals(1, list.size());

    //
    tearDownActivityList.add(activity);
  }
  
  @MaxQueryNumber(520)
  public void testGetUserActivities() {
    ExoSocialActivity activity = createActivity(1);
    //
    activityStorage.saveActivity(demoIdentity, activity);
    List<ExoSocialActivity> got = activityStorage.getUserActivities(demoIdentity, 0, 20);
    assertEquals(1, got.size());
    tearDownActivityList.addAll(got);
  }
  
  @MaxQueryNumber(520)
  public void testGetUserIdsActivities() {
    ExoSocialActivity activity = createActivity(1);
    //
    activityStorage.saveActivity(demoIdentity, activity);
    List<String> got = activityStorage.getUserIdsActivities(demoIdentity, 0, 20);
    assertEquals(1, got.size());
    tearDownActivityList.add(activityStorage.getActivity(got.get(0)));
  }

  @MaxQueryNumber(520)
  public void testGetActivitiesByIDs() {
    ExoSocialActivity activity1 = createActivity(10);
    activityStorage.saveActivity(demoIdentity, activity1);
    tearDownActivityList.add(activity1);
    ExoSocialActivity activity2 = createActivity(20);
    activityStorage.saveActivity(demoIdentity, activity2);
    tearDownActivityList.add(activity2);
    ExoSocialActivity activity3 = createActivity(30);
    activityStorage.saveActivity(demoIdentity, activity3);
    tearDownActivityList.add(activity3);
    //
    List<ExoSocialActivity> got = activityStorage.getActivities(Arrays.asList(new String[] {activity1.getId(), activity2.getId(), activity3.getId()}));
    assertEquals(3, got.size());
    assertEquals(activity1.getId(), got.get(0).getId());
    assertEquals(activity2.getId(), got.get(1).getId());
    assertEquals(activity3.getId(), got.get(2).getId());
  }
  
  @MaxQueryNumber(530)
  public void testGetActivityIdsFeed() {
    createActivities(3, demoIdentity);
    List<String> got = activityStorage.getActivityIdsFeed(demoIdentity, 0, 10);
    assertEquals(3, got.size());
  }
  
  @MaxQueryNumber(650)
  public void testGetSpaceActivityIds() throws Exception {
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    
    int totalNumber = 5;
    
    //demo posts activities to space
    for (int i = 0; i < totalNumber; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
      tearDownActivityList.add(activity);
    }
    
    List<String> got = activityStorage.getSpaceActivityIds(spaceIdentity, 0, 10);
    assertEquals(5, got.size());
  }
  
  
  @MaxQueryNumber(516)
  public void testGetActivity() {
    ExoSocialActivity activity = createActivity(1);
    //
    activity = activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
  }
  
  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#getActivitiesOfConnections(Identity)}
   * 
   * @throws Exception
   */
  @MaxQueryNumber(540)
  public void testGetActivityIdsOfConnections() throws Exception {
    createActivities(5, johnIdentity);
    
    ListAccess<ExoSocialActivity> demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertEquals(0, demoConnectionActivities.load(0, 10).length);
    assertEquals(0, demoConnectionActivities.getSize());
    
    Relationship demoJohnRelationship = relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, demoIdentity);

    createActivities(1, johnIdentity);

    ListAccess<ExoSocialActivity> got = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertEquals(6, got.load(0, 10).length);
    assertEquals(6, got.getSize());
  }
  
  @MaxQueryNumber(530)
  public void testGetNewerOnUserActivities() {
    createActivities(2, demoIdentity);
    ExoSocialActivity firstActivity = activityStorage.getUserActivities(demoIdentity, 0, 10).get(0);
    assertEquals(0, activityStorage.getNewerOnUserActivities(demoIdentity, firstActivity, 10).size());
    assertEquals(0, activityStorage.getNumberOfNewerOnUserActivities(demoIdentity, firstActivity));
    //
    createActivities(2, maryIdentity);
    assertEquals(0, activityStorage.getNewerOnUserActivities(demoIdentity, firstActivity, 10).size());
    assertEquals(0, activityStorage.getNumberOfNewerOnUserActivities(demoIdentity, firstActivity));
    //
    createActivities(2, demoIdentity);
    assertEquals(2, activityStorage.getNewerOnUserActivities(demoIdentity, firstActivity, 10).size());
    assertEquals(2, activityStorage.getNumberOfNewerOnUserActivities(demoIdentity, firstActivity));
  }
  @MaxQueryNumber(532)
  public void testGetOlderOnUserActivities() {
    createActivities(2, demoIdentity);
    ExoSocialActivity baseActivity = activityStorage.getUserActivities(demoIdentity, 0, 10).get(0);
    assertEquals(1, activityStorage.getOlderOnUserActivities(demoIdentity, baseActivity, 10).size());
    assertEquals(1, activityStorage.getNumberOfOlderOnUserActivities(demoIdentity, baseActivity));
    //
    createActivities(2, maryIdentity);
    assertEquals(1, activityStorage.getOlderOnUserActivities(demoIdentity, baseActivity, 10).size());
    assertEquals(1, activityStorage.getNumberOfOlderOnUserActivities(demoIdentity, baseActivity));
    //
    createActivities(2, demoIdentity);
    baseActivity = activityStorage.getUserActivities(demoIdentity, 0, 10).get(0);
    assertEquals(3, activityStorage.getOlderOnUserActivities(demoIdentity, baseActivity, 10).size());
    assertEquals(3, activityStorage.getNumberOfOlderOnUserActivities(demoIdentity, baseActivity));
  }
  @MaxQueryNumber(695)
  public void testGetNewerOnActivityFeed() {
    createActivities(3, demoIdentity);
    ExoSocialActivity demoBaseActivity = activityStorage.getActivityFeed(demoIdentity, 0, 10).get(0);
    assertEquals(0, activityStorage.getNewerOnActivityFeed(demoIdentity, demoBaseActivity, 10).size());
    assertEquals(0, activityStorage.getNumberOfNewerOnActivityFeed(demoIdentity, demoBaseActivity));
    //
    createActivities(1, demoIdentity);
    assertEquals(1, activityStorage.getNewerOnActivityFeed(demoIdentity, demoBaseActivity, 10).size());
    assertEquals(1, activityStorage.getNumberOfNewerOnActivityFeed(demoIdentity, demoBaseActivity));
    //
    createActivities(2, maryIdentity);
    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(maryIdentity, demoIdentity);
    createActivities(2, maryIdentity);
    assertEquals(5, activityStorage.getNewerOnActivityFeed(demoIdentity, demoBaseActivity, 10).size());
    assertEquals(5, activityStorage.getNumberOfNewerOnActivityFeed(demoIdentity, demoBaseActivity));
  }
  @MaxQueryNumber(695)
  public void testGetOlderOnActivityFeed() throws Exception {
    createActivities(3, demoIdentity);
    createActivities(2, maryIdentity);
    Relationship maryDemoConnection = relationshipManager.inviteToConnect(maryIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);
    
    List<ExoSocialActivity> demoActivityFeed = activityStorage.getActivityFeed(demoIdentity, 0, 10);
    ExoSocialActivity baseActivity = demoActivityFeed.get(4);
    assertEquals(0, activityStorage.getNumberOfOlderOnActivityFeed(demoIdentity, baseActivity));
    assertEquals(0, activityStorage.getOlderOnActivityFeed(demoIdentity, baseActivity, 10).size());
    //
    createActivities(1, johnIdentity);
    assertEquals(0, activityStorage.getNumberOfOlderOnActivityFeed(demoIdentity, baseActivity));
    assertEquals(0, activityStorage.getOlderOnActivityFeed(demoIdentity, baseActivity, 10).size());
    //
    baseActivity = demoActivityFeed.get(2);
    assertEquals(2, activityStorage.getNumberOfOlderOnActivityFeed(demoIdentity, baseActivity));
    assertEquals(2, activityStorage.getOlderOnActivityFeed(demoIdentity, baseActivity, 10).size());
  }
  @MaxQueryNumber(1129)
  public void testGetNewerOnActivitiesOfConnections() throws Exception {
    List<Relationship> relationships = new ArrayList<Relationship> ();
    createActivities(3, maryIdentity);
    createActivities(1, demoIdentity);
    createActivities(2, johnIdentity);
    createActivities(2, rootIdentity);
    
    List<ExoSocialActivity> maryActivities = activityStorage.getActivitiesOfIdentity(maryIdentity, 0, 10);
    assertEquals(3, maryActivities.size());
    
    //base activity is the second activity created by mary
    ExoSocialActivity baseActivity = maryActivities.get(1);

    //As mary has no connections, there are any activity on her connection stream
    assertEquals(0, activityStorage.getNewerOnActivitiesOfConnections(maryIdentity, baseActivity, 10).size());
    assertEquals(0, activityStorage.getNumberOfNewerOnActivitiesOfConnections(maryIdentity, baseActivity));
    
    //demo connect with mary
    Relationship maryDemoRelationship = relationshipManager.inviteToConnect(maryIdentity, demoIdentity);
    relationshipManager.confirm(maryIdentity, demoIdentity);
    relationships.add(maryDemoRelationship);

    // add 1 activity to make sure cache is updated
    createActivities(1, demoIdentity);

    //mary has 2 activities created by demo (1 at the beginning + 1 after the connection confirmation) newer than the base activity
    assertEquals(2, activityStorage.getNewerOnActivitiesOfConnections(maryIdentity, baseActivity, 10).size());
    assertEquals(2, activityStorage.getNumberOfNewerOnActivitiesOfConnections(maryIdentity, baseActivity));
    
    //demo has  activity created by mary newer than the base activity
    assertEquals(1, activityStorage.getNewerOnActivitiesOfConnections(demoIdentity, baseActivity, 10).size());
    assertEquals(1, activityStorage.getNumberOfNewerOnActivitiesOfConnections(demoIdentity, baseActivity));
    
    //john connects with mary
    Relationship maryJohnRelationship = relationshipManager.inviteToConnect(maryIdentity, johnIdentity);
    relationshipManager.confirm(maryIdentity, johnIdentity);
    relationships.add(maryJohnRelationship);

    // add 1 activity to make sure cache is updated
    createActivities(1, johnIdentity);

    //mary has 2 activities created by demo and 3 activities created by john (2 at the beginning + 1 after the connection confirmation) newer than the base activity
    assertEquals(5, activityStorage.getNewerOnActivitiesOfConnections(maryIdentity, baseActivity, 10).size());
    assertEquals(5, activityStorage.getNumberOfNewerOnActivitiesOfConnections(maryIdentity, baseActivity));

    //john has 1 activity created by mary newer than the base activity
    assertEquals(1, activityStorage.getNewerOnActivitiesOfConnections(johnIdentity, baseActivity, 10).size());
    assertEquals(1, activityStorage.getNumberOfNewerOnActivitiesOfConnections(johnIdentity, baseActivity));
    
    //mary connects with root
    Relationship maryRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    relationshipManager.confirm(maryIdentity, rootIdentity);
    relationships.add(maryRootRelationship);

    // add 1 activity to make sure cache is updated
    createActivities(1, rootIdentity);

    //mary has 2 activities created by demo, 3 activities created by john (2 at the beginning + 1 after the connection confirmation)
    //and 3 activities created by root (2 at the beginning + 1 after the connection confirmation) newer than the base activity
    assertEquals(8, activityStorage.getNewerOnActivitiesOfConnections(maryIdentity, baseActivity, 10).size());
    assertEquals(8, activityStorage.getNumberOfNewerOnActivitiesOfConnections(maryIdentity, baseActivity));
  }
  @MaxQueryNumber(1135)
  public void testGetOlderOnActivitiesOfConnections() throws Exception {
    List<Relationship> relationships = new ArrayList<Relationship> ();
    createActivities(3, maryIdentity);
    createActivities(1, demoIdentity);
    createActivities(2, johnIdentity);
    createActivities(2, rootIdentity);
    
    List<ExoSocialActivity> maryActivities = activityStorage.getActivitiesOfIdentity(maryIdentity, 0, 10);
    assertEquals(3, maryActivities.size());
    
    //base activity is the first activity created by mary
    ExoSocialActivity baseActivity = maryActivities.get(2);
    
    //As mary has no connections, there are any activity on her connection stream
    assertEquals(0, activityStorage.getOlderOnActivitiesOfConnections(maryIdentity, baseActivity, 10).size());
    assertEquals(0, activityStorage.getNumberOfOlderOnActivitiesOfConnections(maryIdentity, baseActivity));
    
    //demo connect with mary
    Relationship maryDemoRelationship = relationshipManager.inviteToConnect(maryIdentity, demoIdentity);
    relationshipManager.confirm(maryIdentity, demoIdentity);
    relationships.add(maryDemoRelationship);
    
    baseActivity = activityStorage.getActivitiesOfIdentity(demoIdentity, 0, 10).get(0);
    LOG.info("demo::sinceTime = " + baseActivity.getPostedTime());
    assertEquals(0, activityStorage.getOlderOnActivitiesOfConnections(maryIdentity, baseActivity, 10).size());
    assertEquals(0, activityStorage.getNumberOfOlderOnActivitiesOfConnections(maryIdentity, baseActivity));
    
    assertEquals(3, activityStorage.getOlderOnActivitiesOfConnections(demoIdentity, baseActivity, 10).size());
    assertEquals(3, activityStorage.getNumberOfOlderOnActivitiesOfConnections(demoIdentity, baseActivity));
    
    //john connects with mary
    Relationship maryJohnRelationship = relationshipManager.inviteToConnect(maryIdentity, johnIdentity);
    relationshipManager.confirm(maryIdentity, johnIdentity);
    relationships.add(maryJohnRelationship);
    
    baseActivity = activityStorage.getActivitiesOfIdentity(johnIdentity, 0, 10).get(0);
    LOG.info("john::sinceTime = " + baseActivity.getPostedTime());
    assertEquals(2, activityStorage.getOlderOnActivitiesOfConnections(maryIdentity, baseActivity, 10).size());
    assertEquals(2, activityStorage.getNumberOfOlderOnActivitiesOfConnections(maryIdentity, baseActivity));
    
    assertEquals(3, activityStorage.getOlderOnActivitiesOfConnections(johnIdentity, baseActivity, 10).size());
    assertEquals(3, activityStorage.getNumberOfOlderOnActivitiesOfConnections(johnIdentity, baseActivity));
    
    //mary connects with root
    Relationship maryRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    relationshipManager.confirm(maryIdentity, rootIdentity);
    relationships.add(maryRootRelationship);
  
    
    baseActivity = activityStorage.getActivitiesOfIdentity(rootIdentity, 0, 10).get(0);
    LOG.info("root::sinceTime = " + baseActivity.getPostedTime());    
    assertEquals(4, activityStorage.getOlderOnActivitiesOfConnections(maryIdentity, baseActivity, 10).size());
    assertEquals(4, activityStorage.getNumberOfOlderOnActivitiesOfConnections(maryIdentity, baseActivity));
  }
  @MaxQueryNumber(835)
  public void testGetNewerOnUserSpacesActivities() throws Exception {
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    
    int totalNumber = 10;
    ExoSocialActivity baseActivity = null;
    //demo posts activities to space
    for (int i = 0; i < totalNumber; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity, activity);
      tearDownActivityList.add(activity);
      if (i == 0) {
        baseActivity = activity;
      }
    }
    
    assertEquals(9, activityStorage.getNewerOnUserSpacesActivities(demoIdentity, baseActivity, 10).size());
    assertEquals(9, activityStorage.getNumberOfNewerOnUserSpacesActivities(demoIdentity, baseActivity));
    //
    assertEquals(9, activityStorage.getNewerOnSpaceActivities(spaceIdentity, baseActivity, 10).size());
    assertEquals(9, activityStorage.getNumberOfNewerOnSpaceActivities(spaceIdentity, baseActivity));
    
    Space space2 = this.getSpaceInstance(spaceService, 1);
    Identity spaceIdentity2 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space2.getPrettyName(), false);
    //demo posts activities to space2
    for (int i = 0; i < totalNumber; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity2, activity);
      tearDownActivityList.add(activity);
    }

    assertEquals(19, activityStorage.getNewerOnUserSpacesActivities(demoIdentity, baseActivity, 20).size());
    assertEquals(19, activityStorage.getNumberOfNewerOnUserSpacesActivities(demoIdentity, baseActivity));
  }
  @MaxQueryNumber(820)
  public void testGetOlderOnUserSpacesActivities() throws Exception {
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    
    int totalNumber = 5;
    ExoSocialActivity baseActivity = null;
    //demo posts activities to space
    for (int i = 0; i < totalNumber; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity, activity);
      LOG.info("user = " + demoIdentity.getRemoteId() + " activity's postedTime = " + activity.getPostedTime());
      sleep(10);
      tearDownActivityList.add(activity);
      if (i == 4) {
        baseActivity = activity;
      }
    }
    
    LOG.info("user = " + demoIdentity.getRemoteId() + " sinceTime = " + baseActivity.getPostedTime());
    
    assertEquals(4, activityStorage.getOlderOnUserSpacesActivities(demoIdentity, baseActivity, 10).size());
    assertEquals(4, activityStorage.getNumberOfOlderOnUserSpacesActivities(demoIdentity, baseActivity));
    //
    assertEquals(4, activityStorage.getOlderOnSpaceActivities(spaceIdentity, baseActivity, 10).size());
    assertEquals(4, activityStorage.getNumberOfOlderOnSpaceActivities(spaceIdentity, baseActivity));
    
    Space space2 = this.getSpaceInstance(spaceService, 1);
    Identity spaceIdentity2 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space2.getPrettyName(), false);
    //demo posts activities to space2
    for (int i = 0; i < totalNumber; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity2, activity);
      sleep(10);
      tearDownActivityList.add(activity);
    }
    assertEquals(4, activityStorage.getOlderOnUserSpacesActivities(demoIdentity, baseActivity, 10).size());
    assertEquals(4, activityStorage.getNumberOfOlderOnUserSpacesActivities(demoIdentity, baseActivity));
  }
  @MaxQueryNumber(213)
  public void testGetNewerComments() {
    int totalNumber = 10;
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title");
    activity.setUserId(rootIdentity.getId());
    activityStorage.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);
    
    for (int i = 0; i < totalNumber; i ++) {
      //John comments on Root's activity
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("john comment " + i);
      comment.setUserId(johnIdentity.getId());
      activityStorage.saveComment(activity, comment);
    }
    
    for (int i = 0; i < totalNumber; i ++) {
      //John comments on Root's activity
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("demo comment " + i);
      comment.setUserId(demoIdentity.getId());
      activityStorage.saveComment(activity, comment);
    }
    
    List<ExoSocialActivity> comments = activityStorage.getComments(activity, false,0, 20);
    assertEquals(20, comments.size());
    
    ExoSocialActivity baseComment = comments.get(0);
    
    assertEquals(19, activityStorage.getNewerComments(activity, baseComment, 20).size());
    assertEquals(19, activityStorage.getNumberOfNewerComments(activity, baseComment));
    
    baseComment = comments.get(9);
    assertEquals(10, activityStorage.getNewerComments(activity, baseComment, 20).size());
    assertEquals(10, activityStorage.getNumberOfNewerComments(activity, baseComment));
    
    baseComment = comments.get(19);
    assertEquals(0, activityStorage.getNewerComments(activity, baseComment, 20).size());
    assertEquals(0, activityStorage.getNumberOfNewerComments(activity, baseComment));
  }
  @MaxQueryNumber(690)
  public void testGetOlderComments() {
    int totalNumber = 10;
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title");
    activity.setUserId(rootIdentity.getId());
    activityStorage.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);
    
    for (int i = 0; i < totalNumber; i ++) {
      //John comments on Root's activity
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("john comment " + i);
      comment.setUserId(johnIdentity.getId());
      activityStorage.saveComment(activity, comment);
    }
    
    for (int i = 0; i < totalNumber; i ++) {
      //John comments on Root's activity
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("demo comment " + i);
      comment.setUserId(demoIdentity.getId());
      activityStorage.saveComment(activity, comment);
    }
    
    List<ExoSocialActivity> comments = activityStorage.getComments(activity, false, 0, 20);
    assertEquals(20, comments.size());
    
    ExoSocialActivity baseComment = comments.get(19);
    
    assertEquals(19, activityStorage.getOlderComments(activity, baseComment, 20).size());
    assertEquals(19, activityStorage.getNumberOfOlderComments(activity, baseComment));
    
    baseComment = comments.get(10);
    assertEquals(10, activityStorage.getOlderComments(activity, baseComment, 20).size());
    assertEquals(10, activityStorage.getNumberOfOlderComments(activity, baseComment));
    
    baseComment = comments.get(0);
    assertEquals(0, activityStorage.getOlderComments(activity, baseComment, 20).size());
    assertEquals(0, activityStorage.getNumberOfOlderComments(activity, baseComment));
  }

  @MaxQueryNumber(1281)
  public void testMentionersAndCommenters() throws Exception {
    ExoSocialActivity activity = createActivity(1);
    activity.setTitle("hello @demo @john");
    activityStorage.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);
    
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    assertNotNull(got);
    assertEquals(2, got.getMentionedIds().length);
    
    ExoSocialActivity comment1 = new ExoSocialActivityImpl();
    comment1.setTitle("comment 1");
    comment1.setUserId(demoIdentity.getId());
    activityStorage.saveComment(activity, comment1);
    ExoSocialActivity comment2 = new ExoSocialActivityImpl();
    comment2.setTitle("comment 2");
    comment2.setUserId(johnIdentity.getId());
    activityStorage.saveComment(activity, comment2);
    
    got = activityStorage.getActivity(activity.getId());
    assertEquals(2, got.getReplyToId().length);
    assertEquals(2, got.getCommentedIds().length);
    
    ExoSocialActivity comment3 = new ExoSocialActivityImpl();
    comment3.setTitle("hello @mary");
    comment3.setUserId(johnIdentity.getId());
    activityStorage.saveComment(activity, comment3);
    
    got = activityStorage.getActivity(activity.getId());
    assertEquals(3, got.getReplyToId().length);
    assertEquals(2, got.getCommentedIds().length);
    assertEquals(3, got.getMentionedIds().length);
    
    activityStorage.deleteComment(activity.getId(), comment3.getId());
    
    got = activityStorage.getActivity(activity.getId());
    assertEquals(2, got.getReplyToId().length);
    assertEquals(2, got.getCommentedIds().length);
    assertEquals(2, got.getMentionedIds().length);
  }

  @MaxQueryNumber(57)
  public void testShouldMoveActivityUpWhenMentionedInAPost() {
    // Given
    ExoSocialActivity activity1 = createActivity(1);
    activity1.setTitle("hello world");
    activityStorage.saveActivity(demoIdentity, activity1);
    tearDownActivityList.add(activity1);

    ExoSocialActivity activity2 = createActivity(1);
    activity2.setTitle("hello mention @demo");
    activityStorage.saveActivity(rootIdentity, activity2);
    tearDownActivityList.add(activity2);

    ExoSocialActivity activity3 = createActivity(1);
    activity3.setTitle("bye world");
    activityStorage.saveActivity(demoIdentity, activity3);
    tearDownActivityList.add(activity3);

    // When
    List<ExoSocialActivity> activities = activityStorage.getActivities(demoIdentity, demoIdentity, 0, 10);

    // Then
    assertNotNull(activities);
    assertEquals(3, activities.size());
    assertEquals("bye world", activities.get(0).getTitle());
    assertTrue("Title '" + activities.get(1).getTitle() + "' doesn't start with 'hello mention'", activities.get(1).getTitle().startsWith("hello mention"));
    assertEquals("hello world", activities.get(2).getTitle());
  }

  @MaxQueryNumber(1293)
  public void testViewerOwnerPosterActivities() throws Exception {
    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    // Demo mentionned here
    activity1.setTitle("title @demo hi");
    activityStorage.saveActivity(rootIdentity, activity1);
    tearDownActivityList.add(activity1);

    // owner poster comment
    ExoSocialActivity activity2 = new ExoSocialActivityImpl();
    activity2.setTitle("root title");
    activityStorage.saveActivity(rootIdentity, activity2);
    tearDownActivityList.add(activity2);

    Space space = this.getSpaceInstance(spaceService, 1);
    Identity spaceIdentity2 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    //demo posts activities to space2
    for (int i = 0; i < 5; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity2, activity);
      tearDownActivityList.add(activity);
    }

    createComment(activity2, demoIdentity, null, 5, 0);

    List<ExoSocialActivity> list = activityStorage.getActivities(demoIdentity, johnIdentity, 0, 10);

    // Return the activity that demo has commented on it and where he's mentionned
    assertEquals(2, list.size());

    list = activityStorage.getActivities(johnIdentity, demoIdentity, 0, 10);

    // John hasn't added, commeted or was menionned in an activity
    assertEquals(0, list.size());

    list = activityStorage.getActivities(demoIdentity, demoIdentity, 0, 10);

    // Demo can see his activities 'Space' & 'User activities'
    assertEquals(7, list.size());
  }
  
  @MaxQueryNumber(1293)
  public void testViewerOwnerPosterInSpaceSubComments() throws Exception {
    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    // Demo mentionned here
    activity1.setTitle("title @mary hi");
    activityStorage.saveActivity(rootIdentity, activity1);
    tearDownActivityList.add(activity1);

    // owner poster comment
    ExoSocialActivity activity2 = new ExoSocialActivityImpl();
    activity2.setTitle("root title");
    activityStorage.saveActivity(rootIdentity, activity2);
    tearDownActivityList.add(activity2);

    Space space = this.getSpaceInstance(spaceService, 1);
    Identity spaceIdentity2 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    //demo posts activities to space2
    for (int i = 0; i < 5; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity2, activity);
      tearDownActivityList.add(activity);
    }

    createComment(activity2, demoIdentity, maryIdentity, 5, 5);

    List<ExoSocialActivity> list = activityStorage.getActivities(maryIdentity, johnIdentity, 0, 10);

    // Return the activity that mary has commented on it and where he's mentionned
    assertEquals(2, list.size());

    list = activityStorage.getActivities(johnIdentity, maryIdentity, 0, 10);

    // John hasn't added, commeted or was menionned in an activity
    assertEquals(0, list.size());

    list = activityStorage.getActivities(maryIdentity, maryIdentity, 0, 10);

    // Demo can see his activities 'Space' & 'User activities'
    assertEquals(2, list.size());
  }

  @MaxQueryNumber(1329)
  public void testViewerOwnerPosterSubComments() throws Exception {
    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    // Demo mentionned here
    activity1.setTitle("title @mary hi");
    activityStorage.saveActivity(rootIdentity, activity1);
    tearDownActivityList.add(activity1);

    // owner poster comment
    ExoSocialActivity activity2 = new ExoSocialActivityImpl();
    activity2.setTitle("root title");
    activityStorage.saveActivity(rootIdentity, activity2);
    activity2.setLikeIdentityIds(new String[]{maryIdentity.getId()});
    activityStorage.updateActivity(activity2);
    tearDownActivityList.add(activity2);

    //demo posts activities to his stream
    for (int i = 0; i < 5; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(rootIdentity.getId());
      activityStorage.saveActivity(rootIdentity, activity);

      createComment(activity, demoIdentity, maryIdentity, 5, 5);
      tearDownActivityList.add(activity);
    }

    // Demo post a comment
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("comment demo");
    comment.setUserId(demoIdentity.getId());
    comment.setPosterId(demoIdentity.getId());
    activityStorage.saveComment(activity1, comment);

    // Demo post a comment reply by mentioning mary
    ExoSocialActivity commentReply = new ExoSocialActivityImpl();
    commentReply.setTitle("comment reply @mary");
    commentReply.setUserId(demoIdentity.getId());
    commentReply.setPosterId(demoIdentity.getId());
    commentReply.setParentCommentId(comment.getId());
    activityStorage.saveComment(activity1, commentReply);

    List<ExoSocialActivity> list = activityStorage.getActivities(maryIdentity, maryIdentity, 0, 10);

    // Demo can see his activities 'User activities'
    assertEquals(7, list.size());

    list = activityStorage.getActivities(johnIdentity, maryIdentity, 0, 10);

    // John hasn't added, commeted or was menionned in an activity
    assertEquals(0, list.size());

    list = activityStorage.getActivities(maryIdentity, johnIdentity, 0, 10);

    // Return the activity that demo has commented on it and where he's mentionned
    assertEquals(7, list.size());
  }

  @MaxQueryNumber(129)
  public void testSaveCommentWithAlreadyMentionedUsers() throws Exception {
    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    activity1.setTitle("Initial Activity");
    activityStorage.saveActivity(rootIdentity, activity1);
    tearDownActivityList.add(activity1);

    // mention root
    ExoSocialActivity comment1 = new ExoSocialActivityImpl();
    comment1.setTitle("comment @root");
    comment1.setUserId(johnIdentity.getId());
    comment1.setPosterId(johnIdentity.getId());
    activityStorage.saveComment(activity1, comment1);

    // mention john
    ExoSocialActivity comment2 = new ExoSocialActivityImpl();
    comment2.setTitle("comment @john");
    comment2.setUserId(rootIdentity.getId());
    comment2.setPosterId(rootIdentity.getId());
    activityStorage.saveComment(activity1, comment2);

    // mention root
    ExoSocialActivity comment3 = new ExoSocialActivityImpl();
    comment3.setTitle("comment @root");
    comment3.setUserId(johnIdentity.getId());
    comment3.setPosterId(johnIdentity.getId());
    activityStorage.saveComment(activity1, comment3);

    // mention john
    ExoSocialActivity comment4 = new ExoSocialActivityImpl();
    comment4.setTitle("comment @john");
    comment4.setUserId(rootIdentity.getId());
    comment4.setPosterId(rootIdentity.getId());
    activityStorage.saveComment(activity1, comment4);

    List<ExoSocialActivity> list = activityStorage.getActivities(rootIdentity,rootIdentity, 0, 10);
    assertEquals(1, list.size());
    assertEquals(2, list.get(0).getMentionedIds().length);

    List<ExoSocialActivity> comments = activityStorage.getComments(list.get(0),true,0,10);
    assertEquals(4,comments.size());

    tearDownActivityList.add(activity1);
  }

  
  private Space getSpaceInstance(SpaceService spaceService, int number) throws Exception {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] {"demo"};
    String[] members = new String[] {"demo"};
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.saveSpace(space, true);
    return space;
  }
  
  private void createActivities(int number, Identity owner) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(owner.getId());
      activityStorage.saveActivity(owner, activity);
      LOG.info("owner = " + owner.getRemoteId() + " PostedTime = " + activity.getPostedTime());
      sleep(10);
    }
  }
  
  private ExoSocialActivity createActivity(int num) {
    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("Activity "+ num);
    activity.setTitleId("TitleID: "+ activity.getTitle());
    activity.setType("UserActivity");
    activity.setBody("Body of "+ activity.getTitle());
    activity.setBodyId("BodyId of "+ activity.getTitle());
    activity.setLikeIdentityIds(new String[]{"demo", "mary"});
    activity.setMentionedIds(new String[]{"demo", "john"});
    activity.setCommentedIds(new String[]{});
    activity.setReplyToId(new String[]{});
    activity.setAppId("AppID");
    activity.setExternalId("External ID");
    
    return activity;
  }

  /**
   * Creates a comment to an existing activity.
   *
   * @param existingActivity the existing activity
   * @param posterIdentity the identity who comments
   * @param commentReplyIdentity 
   * @param number the number of comments
   */
  private void createComment(ExoSocialActivity existingActivity, Identity posterIdentity, Identity commentReplyIdentity, int number, int numberOfSubComments) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment " + i);
      comment.setUserId(posterIdentity.getId());
      comment.setPosterId(posterIdentity.getId());
      activityStorage.saveComment(existingActivity, comment);

      for (int j = 0; j < numberOfSubComments; j++) {
        ExoSocialActivity commentReply = new ExoSocialActivityImpl();
        commentReply.setTitle("comment reply " + i + " " +j);
        commentReply.setUserId(commentReplyIdentity.getId());
        commentReply.setPosterId(commentReplyIdentity.getId());
        commentReply.setParentCommentId(comment.getId());
        activityStorage.saveComment(existingActivity, commentReply);
      }
    }
  }
}
