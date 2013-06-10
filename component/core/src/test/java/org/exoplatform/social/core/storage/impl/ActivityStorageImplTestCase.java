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

package org.exoplatform.social.core.storage.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.application.RelationshipPublisher.TitleId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;
import org.exoplatform.social.core.test.QueryNumberTest;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
// TODO :
// * Fix tests to not have to specify the order of execution like this
// * The order of tests execution changed in Junit 4.11 (https://github.com/KentBeck/junit/blob/master/doc/ReleaseNotes4.11.md)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@QueryNumberTest
public class ActivityStorageImplTestCase extends AbstractCoreTest {
  private IdentityStorage identityStorage;
  private ActivityStorageImpl activityStorage;
  private RelationshipStorageImpl relationshipStorage;
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Space>  tearDownSpaceList;
  private SpaceStorage spaceStorage;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityStorage = (ActivityStorageImpl) getContainer().getComponentInstanceOfType(ActivityStorageImpl.class);
    relationshipStorage = (RelationshipStorageImpl) getContainer().getComponentInstanceOfType(RelationshipStorageImpl.class);
    spaceStorage = (SpaceStorage) this.getContainer().getComponentInstanceOfType(SpaceStorage.class);
    
    assertNotNull(identityStorage);
    assertNotNull(activityStorage);
    assertNotNull(relationshipStorage);

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
    tearDownSpaceList = new ArrayList<Space>();
  }

  @Override
  protected void tearDown() throws Exception {

    for (ExoSocialActivity activity : tearDownActivityList) {
      activityStorage.deleteActivity(activity.getId());
    }

    for (Space sp : tearDownSpaceList) {
      spaceStorage.deleteSpace(sp.getId());
    }

    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);

    super.tearDown();
  }

  @MaxQueryNumber(100)
  public void testSaveActivity() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title ");
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());

    //
    ExoSocialActivity act = new ExoSocialActivityImpl();
    act.setTitle("@#$%^&*(()_+:<>?");
    activityStorage.saveActivity(rootIdentity, act);
    assertNotNull(act.getId());
  }

  /**
   * This is for the requirement from I18N activity type
   * <pre>
   *  if that resource bundle message is a compound resource bundle message, provide templateParams. The argument number will
   *  be counted as it appears on the map.
   *  For example: templateParams = {"key1": "value1", "key2": "value2"} => message bundle arguments = ["value1", "value2"].
   * </pre>
   */
  @MaxQueryNumber(58)
  public void testTemplateParams() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title ");
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("key1", "value1");
    templateParams.put("key2", "value2");
    templateParams.put("key3", "value3");
    activity.setTemplateParams(templateParams);
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    Map<String, String> gotTemplateParams = got.getTemplateParams();
    List<String> arrayList = new ArrayList(gotTemplateParams.values());
    assertEquals("value1", arrayList.get(0));
    assertEquals("value2", arrayList.get(1));
    assertEquals("value3", arrayList.get(2));

  }

  @MaxQueryNumber(80)
  public void testUpdateActivity() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("&");
    activity.setBody("test&amp;");
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    
    got.setBody(null);
    got.setTitle(null);
    
    activityStorage.updateActivity(got);
    
    ExoSocialActivity updatedActivity = activityStorage.getActivity(activity.getId());
    
    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());

  }

  @MaxQueryNumber(80)
  public void testUpdateActivityForLike() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("a");
    activity.setBody("test");
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    
    got.setBody(null);
    got.setTitle(null);
    got.setLikeIdentityIds(new String[] {maryIdentity.getId()});
    activityStorage.updateActivity(got);
    
    
    ExoSocialActivity updatedActivity = activityStorage.getActivity(activity.getId());
    
    assertEquals(got.getId(), updatedActivity.getId());
    assertEquals(got.getTitle(), updatedActivity.getTitle());
    assertEquals(got.getBody(), updatedActivity.getBody());

  }

  /**
   * Wrong due to not set:
   *                   got.setBody(null);
   *                   got.setTitle(null);
   * before invokes: activityStorage.updateActivity(got);
   * @throws Exception
   */
  @MaxQueryNumber(80)
  public void testUpdateActivityForWrong() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("&");
    activity.setBody("test&amp;");
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    
    got.setLikeIdentityIds(new String[] {maryIdentity.getId()});
    activityStorage.updateActivity(got);
    
    
    ExoSocialActivity updatedActivity = activityStorage.getActivity(activity.getId());
    
    assertEquals(got.getId(), updatedActivity.getId());
    assertNotSame(got.getTitle(), updatedActivity.getTitle());
    assertNotSame(got.getBody(), updatedActivity.getBody());

  }

  @MaxQueryNumber(80)
  public void testUpdateActivityForUnLike() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title ");
    activity.setLikeIdentityIds(new String[] {maryIdentity.getId()});
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    
    got.setBody(null);
    got.setTitle(null);
    got.setLikeIdentityIds(new String[] {});
    activityStorage.updateActivity(got);
    
    
    ExoSocialActivity updatedActivity = activityStorage.getActivity(activity.getId());
    
    assertEquals(got.getId(), updatedActivity.getId());
    assertEquals(got.getTitle(), updatedActivity.getTitle());
    assertEquals(got.getBody(), updatedActivity.getBody());

  }
  
  /**
   * Wrong due to not set:
   *                   got.setBody(null);
   *                   got.setTitle(null);
   * before invokes: activityStorage.updateActivity(got);
   * @throws Exception
   */
  @MaxQueryNumber(80)
  public void testUpdateActivityForUnLikeWrong() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("&");
    activity.setBody("test&amp;");
    activity.setLikeIdentityIds(new String[] {maryIdentity.getId()});
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    
    
    got.setLikeIdentityIds(new String[] {});
    activityStorage.updateActivity(got);
    
    
    ExoSocialActivity updatedActivity = activityStorage.getActivity(activity.getId());
    
    assertEquals(got.getId(), updatedActivity.getId());
    assertNotSame(got.getTitle(), updatedActivity.getTitle());
    assertNotSame(got.getBody(), updatedActivity.getBody());

  }

  @MaxQueryNumber(100)
  public void testSaveComment() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity ");
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());

    //
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("comment");
    comment.setUserId(rootIdentity.getId());
    activityStorage.saveComment(activity, comment);
    assertNotNull(comment.getId());

    //
    ExoSocialActivity gotComment = activityStorage.getActivity(comment.getId());
    assertEquals(comment.getId(), gotComment.getId());
    assertEquals(comment.getTitle(), gotComment.getTitle());

    //
    ExoSocialActivity gotParentActivity = activityStorage.getParentActivity(comment);
    assertEquals(activity.getId(), gotParentActivity.getId());
    assertEquals(activity.getTitle(), gotParentActivity.getTitle());
    assertEquals(1, activity.getReplyToId().length);
    assertEquals(comment.getId(), activity.getReplyToId()[0]);

  }

  @MaxQueryNumber(900)
  public void testActivityCount() throws Exception {

    // fill 10 activities
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(rootIdentity, activity);
    }

    //
    assertEquals(10, activityStorage.getNumberOfUserActivities(rootIdentity));

    // remove 5 activities
    Iterator<ExoSocialActivity> it = activityStorage.getUserActivities(rootIdentity, 0, 100).iterator();

    for (int i = 0; i < 5; ++i) {
      activityStorage.deleteActivity(it.next().getId());
    }
    
    it = activityStorage.getUserActivities(rootIdentity, 0, 100).iterator();
    
    //
    assertEquals(5, activityStorage.getNumberOfUserActivities(rootIdentity));
    
    while(it.hasNext()) {
      tearDownActivityList.add(it.next());
    }


  }

  /**
   * Test {@link org.exoplatform.social.core.storage.impl.ActivityStorageImpl#getActivity(String)}
   */
  @MaxQueryNumber(350)
  public void testUserPostActivityToSpace() throws ActivityStorageException {
    // Create new Space and its Identity
    Space space = getSpaceInstance();
    SpaceIdentityProvider spaceIdentityProvider = (SpaceIdentityProvider) getContainer().getComponentInstanceOfType(SpaceIdentityProvider.class);
    Identity spaceIdentity = spaceIdentityProvider.createIdentity(space);
    identityStorage.saveIdentity(spaceIdentity);
    
    // john posted activity on created Space
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("Space's Activity");
    activity.setUserId(johnIdentity.getId());

    activityStorage.saveActivity(spaceIdentity, activity);
    
    // Get posted Activity and check
    ExoSocialActivity gotActivity = activityStorage.getActivity(activity.getId());
    
    assertEquals("userId must be " + johnIdentity.getId(), johnIdentity.getId(), gotActivity.getUserId());
    
    //
    List<ExoSocialActivity> gotActivities = activityStorage.getUserActivities(johnIdentity, 0, 20);
    assertEquals(1, gotActivities.size());
    assertEquals(johnIdentity.getId(), gotActivities.get(0).getUserId());
    
    identityStorage.deleteIdentity(spaceIdentity);
  }

  @MaxQueryNumber(600)
  public void testActivityOrder() throws Exception {
    // fill 10 activities
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
    }

    int i = 9;
    for (ExoSocialActivity activity : activityStorage.getUserActivities(rootIdentity)) {
      assertEquals("title " + i, activity.getTitle());
      --i;
    }
  }

  @MaxQueryNumber(600)
  public void testActivityOrderByPostedTime() throws Exception {
    // fill 10 activities
    Calendar cal = Calendar.getInstance();
    long today = cal.getTime().getTime();
    cal.add(Calendar.DAY_OF_MONTH, -1);
    long yesterday = cal.getTime().getTime();
    //i > 5 PostedTime = currentDate + i;
    //else yesterdayDate = currentDate + i;
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activity.setPostedTime(i> 5 ? today + i : yesterday + i);
      activityStorage.saveActivity(rootIdentity, activity);
    }

    int i = 9;
    for (ExoSocialActivity activity : activityStorage.getUserActivities(rootIdentity)) {
      assertEquals("title " + i, activity.getTitle());
      
      if (i>5) {
        assertEquals(today + i, activity.getPostedTime().longValue());        
      } else {
        assertEquals(yesterday + i, activity.getPostedTime().longValue());
      }
      
      --i;
    }
  }

  @MaxQueryNumber(1502)
  public void testActivityOrder2() throws Exception {
    // fill 10 activities
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(rootIdentity, activity);
      
    }

    // remove 5 activities
    Iterator<ExoSocialActivity> it = activityStorage.getUserActivities(rootIdentity).iterator();

    for (int i = 0; i < 5; ++i) {
      activityStorage.deleteActivity(it.next().getId());
    }
    
    while (it.hasNext()) {
      tearDownActivityList.add(it.next());
    }

    // fill 10 others
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
    }

    List<ExoSocialActivity> activityies = activityStorage.getUserActivities(rootIdentity);
    int i = 0;
    int[] values = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 4, 3, 2, 1, 0};
    for (ExoSocialActivity activity : activityies) {
      assertEquals("title " + values[i], activity.getTitle());
      ++i;
    }
  }
  
  @MaxQueryNumber(200)
  public void testActivityHidden() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title");
    activity.setBody("body");
    activity.isHidden(false);
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    got.isHidden(true);

    activityStorage.updateActivity(got);

    ExoSocialActivity updatedActivity = activityStorage.getActivity(activity.getId());
    assertEquals(true, updatedActivity.isHidden());
  }
  
  @MaxQueryNumber(200)
  public void testActivityUnHidden() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title");
    activity.setBody("body");
    activity.isHidden(true);
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    got.isHidden(false);

    activityStorage.updateActivity(got);

    ExoSocialActivity updatedActivity = activityStorage.getActivity(activity.getId());
    assertEquals(false, updatedActivity.isHidden());
  }
  
  @MaxQueryNumber(200)
  public void testActivityLock() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title");
    activity.setBody("body");
    activity.isLocked(false);
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    got.isLocked(true);

    activityStorage.updateActivity(got);

    ExoSocialActivity updatedActivity = activityStorage.getActivity(activity.getId());
    assertEquals(true, updatedActivity.isLocked());
  }
  
  @MaxQueryNumber(200)
  public void testActivityUnLock() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title");
    activity.setBody("body");
    activity.isLocked(true);
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    got.isLocked(false);

    activityStorage.updateActivity(got);

    ExoSocialActivity updatedActivity = activityStorage.getActivity(activity.getId());
    assertEquals(false, updatedActivity.isLocked());
  }

  @MaxQueryNumber(4562)
  public void testCommentOrder() throws Exception {
    // fill 10 activities
    for (int i = 0; i < 10; ++i) {
      Thread.sleep(10);
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);

      // fill 10 comments for each activity
      for(int j = 0; j < 10; ++j) {
        Thread.sleep(10);
        ExoSocialActivity comment = new ExoSocialActivityImpl();
        comment.setTitle("title " + i + j);
        comment.setUserId(rootIdentity.getId());
        activityStorage.saveComment(activity, comment);
      }
    }

    int i = 9;
    for (ExoSocialActivity activity : activityStorage.getUserActivities(rootIdentity)) {
      int j = 0;
      for (String commentId : activity.getReplyToId()) {
        if (!"".equals(commentId)) {
          assertEquals("title " + i + j, activityStorage.getActivity(commentId).getTitle());
          ++j;
        }
      }
      --i;
    }
  }

  @MaxQueryNumber(366)
  public void testDeleteComment() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title");

    activityStorage.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);

    activity = activityStorage.getActivity(activity.getId());

    for (int i = 0; i < 10; ++i) {
      Thread.sleep(10);
      
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment title " + i);
      comment.setUserId(rootIdentity.getId());

      activityStorage.saveComment(activity, comment);
    }

    assertEquals(10, activityStorage.getActivity(activity.getId()).getReplyToId().length);

    int i = 0;
    activity = activityStorage.getActivity(activity.getId());
    for (String commentId : activity.getReplyToId()) {
      if (!"".equals(commentId) && i < 5) {
        activityStorage.deleteActivity(commentId);
        ++i;
      }
    }

    assertEquals(5, activityStorage.getActivity(activity.getId()).getReplyToId().length);
  }

  @MaxQueryNumber(100)
  public void testLike() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title");

    activityStorage.saveActivity(rootIdentity, activity);

    activity.setLikeIdentityIds(new String[] {rootIdentity.getId(), johnIdentity.getId(), demoIdentity.getId()});

    activityStorage.saveActivity(rootIdentity, activity);

    List<ExoSocialActivity> activities = activityStorage.getUserActivities(rootIdentity);

    assertEquals(1, activities.size());
    assertEquals(3, activities.get(0).getLikeIdentityIds().length);

    List<String> ids = Arrays.asList(activities.get(0).getLikeIdentityIds());

    assertTrue(ids.contains(rootIdentity.getId()));
    assertTrue(ids.contains(johnIdentity.getId()));
    assertTrue(ids.contains(demoIdentity.getId()));
    assertTrue(!ids.contains(maryIdentity.getId()));
  }

  @MaxQueryNumber(1600)
  public void testContactActivities() throws Exception {

    //
    assertEquals(0, activityStorage.getActivitiesOfIdentities(Arrays.asList(rootIdentity, johnIdentity), 0, 100).size());

    for (int i = 0; i < 10; ++i) {

      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("root activity " + i);
      activityStorage.saveActivity(rootIdentity, activity);

      ExoSocialActivity activity2 = new ExoSocialActivityImpl();
      activity2.setTitle("john activity " + i);
      activityStorage.saveActivity(johnIdentity, activity2);

      ExoSocialActivity activity3 = new ExoSocialActivityImpl();
      activity3.setTitle("mary activity " + i);
      activityStorage.saveActivity(maryIdentity, activity3);
    }

    //
    List<ExoSocialActivity> activities = activityStorage.getActivitiesOfIdentities(Arrays.asList(rootIdentity, johnIdentity), 0, 100);
    assertEquals(20, activities.size());

    int i = 9;
    Iterator<ExoSocialActivity> it = activities.iterator();
    while (it.hasNext()) {

      ExoSocialActivity activity = it.next();
      assertEquals("john activity " + i, activity.getTitle());

      activity = it.next();
      assertEquals("root activity " + i, activity.getTitle());
      --i;
    }

  }

  @MaxQueryNumber(100)
  public void testTimeStamp() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title");
    activityStorage.saveActivity(rootIdentity, activity);

    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setUserId(rootIdentity.getId());
    comment.setTitle("comment title");

    activityStorage.saveComment(activity, comment);

    List<ExoSocialActivity> activities = activityStorage.getUserActivities(rootIdentity);

    assertEquals(1, activities.size());
    assertFalse(activities.get(0).getPostedTime() == 0);
    assertEquals(1, activities.get(0).getReplyToId().length);

    ExoSocialActivity gotComment = activityStorage.getActivity(activities.get(0).getReplyToId()[0]);
    assertFalse(gotComment.getPostedTime() == 0);

  }

  @MaxQueryNumber(430)
  public void testManyDays() throws Exception {

    long timestamp111 = timestamp(2001, 1, 1);
    long timestamp112 = timestamp(2001, 1, 2);
    long timestamp121 = timestamp(2001, 2, 1);
    long timestamp122 = timestamp(2001, 2, 2);
    long timestamp211 = timestamp(2002, 1, 1);
    long timestamp212 = timestamp(2002, 1, 2);
    long timestamp221 = timestamp(2002, 2, 1);
    long timestamp222 = timestamp(2002, 2, 2);

    addActivity(rootIdentity, timestamp111);
    addActivity(rootIdentity, timestamp112);
    addActivity(rootIdentity, timestamp121);
    addActivity(rootIdentity, timestamp122);
    addActivity(rootIdentity, timestamp211);
    addActivity(rootIdentity, timestamp212);
    addActivity(rootIdentity, timestamp221);
    addActivity(rootIdentity, timestamp222);

    List<ExoSocialActivity> activities = activityStorage.getUserActivities(rootIdentity);
    assertEquals(8, activities.size());
    assertEquals(timestamp111, activities.get(7).getPostedTime().longValue());
    assertEquals(timestamp112, activities.get(6).getPostedTime().longValue());
    assertEquals(timestamp121, activities.get(5).getPostedTime().longValue());
    assertEquals(timestamp122, activities.get(4).getPostedTime().longValue());
    assertEquals(timestamp211, activities.get(3).getPostedTime().longValue());
    assertEquals(timestamp212, activities.get(2).getPostedTime().longValue());
    assertEquals(timestamp221, activities.get(1).getPostedTime().longValue());
    assertEquals(timestamp222, activities.get(0).getPostedTime().longValue());

  }

  @MaxQueryNumber(150)
  public void testManyDaysNoActivityOnDay() throws Exception {

    long timestamp1 = timestamp(2001, 1, 1);
    long timestamp2 = timestamp(2001, 1, 2);

    addActivity(rootIdentity, timestamp1);
    ExoSocialActivity activity2 = addActivity(rootIdentity, timestamp2);

    activityStorage.deleteActivity(activity2.getId());

    List<ExoSocialActivity> activities = activityStorage.getUserActivities(rootIdentity);
    assertEquals(1, activities.size());
    assertEquals(timestamp1, activities.get(0).getPostedTime().longValue());

  }

  @MaxQueryNumber(270)
  public void testManyDaysNoActivityOnMonth() throws Exception {

    long timestamp11 = timestamp(2001, 1, 1);
    long timestamp12 = timestamp(2001, 1, 2);
    long timestamp21 = timestamp(2001, 2, 1);
    long timestamp22 = timestamp(2001, 2, 2);

    addActivity(rootIdentity, timestamp11);
    addActivity(rootIdentity, timestamp12);
    ExoSocialActivity activity21 = addActivity(rootIdentity, timestamp21);
    ExoSocialActivity activity22 = addActivity(rootIdentity, timestamp22);

    activityStorage.deleteActivity(activity21.getId());
    activityStorage.deleteActivity(activity22.getId());

    List<ExoSocialActivity> activities = activityStorage.getUserActivities(rootIdentity);
    assertEquals(2, activities.size());
    assertEquals(timestamp11, activities.get(1).getPostedTime().longValue());
    assertEquals(timestamp12, activities.get(0).getPostedTime().longValue());

  }

  @MaxQueryNumber(540)
  public void testManyDaysNoActivityOnYear() throws Exception {

    long timestamp111 = timestamp(2001, 1, 1);
    long timestamp112 = timestamp(2001, 1, 2);
    long timestamp121 = timestamp(2001, 2, 1);
    long timestamp122 = timestamp(2001, 2, 2);
    long timestamp211 = timestamp(2002, 1, 1);
    long timestamp212 = timestamp(2002, 1, 2);
    long timestamp221 = timestamp(2002, 2, 1);
    long timestamp222 = timestamp(2002, 2, 2);

    addActivity(rootIdentity, timestamp111);
    addActivity(rootIdentity, timestamp112);
    addActivity(rootIdentity, timestamp121);
    addActivity(rootIdentity, timestamp122);
    ExoSocialActivity activity211 = addActivity(rootIdentity, timestamp211);
    ExoSocialActivity activity212 = addActivity(rootIdentity, timestamp212);
    ExoSocialActivity activity221 = addActivity(rootIdentity, timestamp221);
    ExoSocialActivity activity222 = addActivity(rootIdentity, timestamp222);

    activityStorage.deleteActivity(activity211.getId());
    activityStorage.deleteActivity(activity212.getId());
    activityStorage.deleteActivity(activity221.getId());
    activityStorage.deleteActivity(activity222.getId());

    List<ExoSocialActivity> activities = activityStorage.getUserActivities(rootIdentity);
    assertEquals(4, activities.size());
    assertEquals(timestamp111, activities.get(3).getPostedTime().longValue());
    assertEquals(timestamp112, activities.get(2).getPostedTime().longValue());
    assertEquals(timestamp121, activities.get(1).getPostedTime().longValue());
    assertEquals(timestamp122, activities.get(0).getPostedTime().longValue());

  }

  @MaxQueryNumber(650)
  public void testManyDaysNoActivityOnAll() throws Exception {

    long timestamp111 = timestamp(2001, 1, 1);
    long timestamp112 = timestamp(2001, 1, 2);
    long timestamp121 = timestamp(2001, 2, 1);
    long timestamp122 = timestamp(2001, 2, 2);
    long timestamp211 = timestamp(2002, 1, 1);
    long timestamp212 = timestamp(2002, 1, 2);
    long timestamp221 = timestamp(2002, 2, 1);
    long timestamp222 = timestamp(2002, 2, 2);

    ExoSocialActivity activity111 = addActivity(rootIdentity, timestamp111);
    ExoSocialActivity activity112 = addActivity(rootIdentity, timestamp112);
    ExoSocialActivity activity121 = addActivity(rootIdentity, timestamp121);
    ExoSocialActivity activity122 = addActivity(rootIdentity, timestamp122);
    ExoSocialActivity activity211 = addActivity(rootIdentity, timestamp211);
    ExoSocialActivity activity212 = addActivity(rootIdentity, timestamp212);
    ExoSocialActivity activity221 = addActivity(rootIdentity, timestamp221);
    ExoSocialActivity activity222 = addActivity(rootIdentity, timestamp222);

    activityStorage.deleteActivity(activity111.getId());
    activityStorage.deleteActivity(activity112.getId());
    activityStorage.deleteActivity(activity121.getId());
    activityStorage.deleteActivity(activity122.getId());
    activityStorage.deleteActivity(activity211.getId());
    activityStorage.deleteActivity(activity212.getId());
    activityStorage.deleteActivity(activity221.getId());
    activityStorage.deleteActivity(activity222.getId());

    List<ExoSocialActivity> activities = activityStorage.getUserActivities(rootIdentity);
    assertEquals(0, activities.size());

  }

  @MaxQueryNumber(55)
  public void testRelationshipActivity() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("I am now connected with @receiverRemoteId");
    activity.setType("exosocial:relationship");
    //Shindig's Activity's fields
    activity.setAppId("appId");
    activity.setBody("body");
    activity.setBodyId("bodyId");
    activity.setTitleId(TitleId.CONNECTION_REQUESTED.toString());
    activity.setExternalId("externalId");
    //activity.setId("id");
    activity.setUrl("http://www.exoplatform.org");
    
    Map<String,String> params = new HashMap<String,String>();
    params.put("SENDER", "senderRemoteId");
    params.put("RECEIVER", "receiverRemoteId");
    params.put("RELATIONSHIP_UUID", "relationship_id");
    activity.setTemplateParams(params);
    
    activityStorage.saveActivity(rootIdentity, activity);
    
    List<ExoSocialActivity> activities = activityStorage.getUserActivities(rootIdentity);
    assertNotNull(activities);
    assertEquals(1, activities.size());
    
    for(ExoSocialActivity element : activities) {
     
      //title
      assertNotNull(element.getTitle());
      //type
      assertNotNull(element.getType());
      //appId
      assertNotNull(element.getAppId());
      //body
      assertNotNull(element.getBody());
      //bodyId
      assertNotNull(element.getBodyId());
      //titleId
      assertEquals(TitleId.CONNECTION_REQUESTED.toString(), element.getTitleId());
      //externalId
      assertNotNull(element.getExternalId());
      //id
      //assertNotNull(element.getId());
      //url
      assertEquals("http://www.exoplatform.org", element.getUrl());
      //id
      assertNotNull(element.getUserId());
      //templateParams
      assertNotNull(element.getTemplateParams());
      
    }
    
    
  }

  @MaxQueryNumber(100)
  public void testActivityProcessing() throws Exception {

    //
    BaseActivityProcessorPlugin processor = new DummyProcessor(null);
    activityStorage.getActivityProcessors().add(processor);

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity");
    activityStorage.saveActivity(rootIdentity, activity);
    assertNotNull(activity.getId());

    //
    ExoSocialActivity got = activityStorage.getActivity(activity.getId());
    assertEquals(activity.getId(), got.getId());
    assertEquals("edited", got.getTitle());

    //
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("comment");
    comment.setUserId(rootIdentity.getId());
    activityStorage.saveComment(activity, comment);
    assertNotNull(comment.getId());

    //
    ExoSocialActivity gotComment = activityStorage.getActivity(comment.getId());
    assertEquals(comment.getId(), gotComment.getId());
    assertEquals("edited", gotComment.getTitle());

    //
    ExoSocialActivity gotParentActivity = activityStorage.getParentActivity(comment);
    assertEquals(activity.getId(), gotParentActivity.getId());
    assertEquals("edited", gotParentActivity.getTitle());
    assertEquals(1, activity.getReplyToId().length);
    assertEquals(comment.getId(), activity.getReplyToId()[0]);

    //
    activityStorage.getActivityProcessors().remove(processor);

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

  private ExoSocialActivity addActivity(Identity identity, long timestamp) {

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title");
    activity.setPostedTime(timestamp);
    activityStorage.saveActivity(identity, activity);

    return activity;

  }

  private long timestamp(int year, int month, int day) {

    Calendar cal = Calendar.getInstance();
    cal.set(year, month, day, 0, 0, 0);
    return cal.getTime().getTime();

  }

  class DummyProcessor extends BaseActivityProcessorPlugin {

    DummyProcessor(final InitParams params) {
      super(params);
    }

    @Override
    public void processActivity(final ExoSocialActivity activity) {
      activity.setTitle("edited");
    }
  }

  private Space getSpaceInstance(int number) {
    Space space = new Space();
    space.setApp("app");
    space.setDisplayName("myspace " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/spaces/space" + number);
    String[] managers = new String[] {"demo", "tom"};
    String[] members = new String[] {"raul", "ghost", "dragon"};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    return space;
  }
  
  private long getSinceTime() {
    Calendar cal = Calendar.getInstance();
    return cal.getTimeInMillis();
  }
}
