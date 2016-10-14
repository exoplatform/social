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
 * along with this program; if not, see<http:www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.jpa.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.exoplatform.social.core.jpa.search.ProfileSearchConnector;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.mockito.Mockito;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.impl.StorageUtils;

/**
 * Unit Test for {@link ActivityManager}, including cache tests.
 * @author hoat_le
 */
public class ActivityManagerMysqlTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(ActivityManagerMysqlTest.class);
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Space> tearDownSpaceList;
  private Identity ghostIdentity;
  private Identity raulIdentity;
  private Identity jameIdentity;
  private Identity paulIdentity;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();

    RDBMSIdentityStorageImpl identityStorage = (RDBMSIdentityStorageImpl) getService(IdentityStorage.class);
    identityStorage.setProfileSearchConnector(mockProfileSearch);

    ghostIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "ghost", true);
    raulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "raul", true);
    jameIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "jame", true);
    paulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul", true);
  }

  @Override
  public void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }
    
    //RealtimeListAccess<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    //assertEquals(0, demoActivities.getSize());

    for (Space space : spaceService.getAllSpaces()) {
      spaceService.deleteSpace(space);
    }
    
    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(jameIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);

    RDBMSIdentityStorageImpl identityStorage = (RDBMSIdentityStorageImpl) getService(IdentityStorage.class);
    identityStorage.setProfileSearchConnector(getService(ProfileSearchConnector.class));
    super.tearDown();
  }

  /**
   * Test {@link ActivityManager#saveActivityNoReturn(Identity, ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testSaveActivityNoReturn() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    //test for reserving order of map values for i18n activity
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("key1", "value 1");
    templateParams.put("key2", "value 2");
    templateParams.put("key3", "value 3");
    activity.setTemplateParams(templateParams);
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    
    //
    activity.isHidden(false);
    activity.isLocked(true);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    Map<String, String> gotTemplateParams = activity.getTemplateParams();
    assertEquals("value 1", gotTemplateParams.get("key1"));
    assertEquals("value 2", gotTemplateParams.get("key2"));
    assertEquals("value 3", gotTemplateParams.get("key3"));
    
    //
    assertTrue(activity.isLocked());
    assertFalse(activity.isHidden());
  }
  
  /**
   * Test {@link ActivityManager#saveActivity(ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testSaveActivityNoReturnNotStreamOwner() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(activity);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
  }
  
  /**
   * Test for {@link ActivityManager#saveActivity(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   * and {@link ActivityManager#saveActivity(Identity, org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   * 
   * @throws ActivityStorageException
   */
  public void testSaveActivity() throws ActivityStorageException {
    //save mal-formed activity
    {
      ExoSocialActivity malformedActivity = new ExoSocialActivityImpl();
      malformedActivity.setTitle("malform");
      try {
        activityManager.saveActivity(malformedActivity);
        fail("Expecting IllegalArgumentException.");
      } catch (IllegalArgumentException e) {
        LOG.info("test with malfomred activity passes.");
      }
    }

    {
      final String activityTitle = "root activity";
      ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
      rootActivity.setTitle(activityTitle);
      rootActivity.setUserId(rootIdentity.getId());
      activityManager.saveActivity(rootActivity);

      assertNotNull("rootActivity.getId() must not be null", rootActivity.getId());

      //updates
      rootActivity.setTitle("Hello World");
      activityManager.updateActivity(rootActivity);

      tearDownActivityList.add(rootActivity);
    }

    {
      final String title = "john activity";
      ExoSocialActivity johnActivity = new ExoSocialActivityImpl();
      johnActivity.setTitle(title);
      activityManager.saveActivity(johnIdentity, johnActivity);

      tearDownActivityList.add(johnActivity);

      assertNotNull("johnActivity.getId() must not be null", johnActivity.getId());
    }

    // updated and postedTime is optional
    {
      final String title = "test";
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(title);
      activityManager.saveActivity(demoIdentity, activity);
      tearDownActivityList.add(activity);
      assertNotNull("activity.getId() must not be null", activity.getId());
      assertNotNull("activity.getUpdated() must not be null", activity.getUpdated());
      assertNotNull("activity.getPostedTime() must not be null", activity.getPostedTime());
      assertEquals("activity.getTitle() must return: " + title, title, activity.getTitle());
    }
  }
  
  /**
   * Test {@link ActivityManager#saveActivity(Identity, ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testSaveActivityWithStreamOwner() throws Exception {
    String activityTitle = "activity title";
    String userId = demoIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivity(demoIdentity, activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    
    tearDownActivityList.add(activity);
  }
  
  /**
   * Test {@link ActivityManager#getActivities(Identity, long, long)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetActivitiesWithOffsetLimit() throws Exception {
    this.populateActivityMass(johnIdentity, 10);
    List<ExoSocialActivity> johnActivities = activityManager.getActivities(johnIdentity, 0, 5);
    assertNotNull("johnActivities must not be null", johnActivities);
    assertEquals("johnActivities.size() must return: 5", 5, johnActivities.size());
    
    johnActivities = activityManager.getActivities(johnIdentity, 0, 10);
    assertNotNull("johnActivities must not be null", johnActivities);
    assertEquals("johnActivities.size() must return: 0", 10, johnActivities.size());
    
    johnActivities = activityManager.getActivities(johnIdentity, 0, 20);
    assertNotNull("johnActivities must not be null", johnActivities);
    assertEquals("johnActivities.size() must return: 10", 10, johnActivities.size());
  }

  /**
   * Test {@link ActivityManager#getActivity(String)}
   * 
   * @throws ActivityStorageException
   */
  public void testGetActivity() throws ActivityStorageException {
      List<ExoSocialActivity> rootActivities = activityManager.getActivities(rootIdentity);
      assertEquals("user's activities should have 0 element.", 0, rootActivities.size());

      String activityTitle = "title";
      String userId = rootIdentity.getId();
      
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle);
      activity.setUserId(userId);

      activityManager.saveActivityNoReturn(rootIdentity, activity);

      activity = activityManager.getActivity(activity.getId());
      assertNotNull("activity must not be null", activity);
      assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
      assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
      
      rootActivities = activityManager.getActivities(rootIdentity);
      assertEquals("user's activities should have 1 element", 1, rootActivities.size());

      tearDownActivityList.addAll(rootActivities);
  }

  /**
   * Tests {@link ActivityManager#getParentActivity(ExoSocialActivity)}.
   */
  public void testGetParentActivity() {
    populateActivityMass(demoIdentity, 1);
    ExoSocialActivity demoActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    assertNotNull("demoActivity must be false", demoActivity);
    assertNull(activityManager.getParentActivity(demoActivity));

    //comment
    ExoSocialActivityImpl comment = new ExoSocialActivityImpl();
    comment.setTitle("comment");
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(demoActivity, comment);
    ExoSocialActivity gotComment = activityManager.getCommentsWithListAccess(demoActivity).load(0, 1)[0];
    assertNotNull("gotComment must not be null", gotComment);
    //
    ExoSocialActivity parentActivity = activityManager.getParentActivity(gotComment);
    assertNotNull(parentActivity);
    assertEquals("parentActivity.getId() must return: " + demoActivity.getId(),
                 demoActivity.getId(),
                 parentActivity.getId());
    assertEquals("parentActivity.getTitle() must return: " + demoActivity.getTitle(),
                 demoActivity.getTitle(),
                 parentActivity.getTitle());
    assertEquals("parentActivity.getUserId() must return: " + demoActivity.getUserId(),
                 demoActivity.getUserId(),
                 parentActivity.getUserId());
  }

  public void testGetActivitiiesByUser() throws ActivityStorageException {
    String activityTitle = "title";
    String userId = rootIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(rootIdentity, activity);
    //
    activity = activityManager.getActivity(String.valueOf(activity.getId()));

    assertNotNull(activity);
    assertEquals(activityTitle, activity.getTitle());
    assertEquals(userId, activity.getUserId());

    RealtimeListAccess<ExoSocialActivity> activities = activityManager.getActivitiesWithListAccess(rootIdentity);
    
    assertEquals(1, activities.load(0, 10).length);
    LOG.info("Create 100 activities...");
    //
    for (int i = 0; i < 100; i++) {
      activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle + " " + i);
      activity.setUserId(userId);
      //
      activityManager.saveActivityNoReturn(rootIdentity, activity);
    }
    activities = activityManager.getActivitiesWithListAccess(rootIdentity);
    //
    LOG.info("Loadding 20 activities...");
    assertEquals(20, activities.load(0, 20).length);
    //
    List<ExoSocialActivity> exoActivities = Arrays.asList(activities.load(0, activities.getSize()));
    LOG.info("Loadding 101 activities...");
    assertEquals(101, exoActivities.size());
    //
    tearDownActivityList.addAll(exoActivities);
  }

  /**
   * Test {@link ActivityManager#updateActivity(ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testUpdateActivity() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    
    String newTitle = "new activity title";
    activity.setTitle(newTitle);
    activityManager.updateActivity(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + newTitle, newTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
  }
  
  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#deleteActivity(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   * 
   * @throws Exception
   */
  public void testDeleteActivity() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    
    activity = activityManager.getActivity(activity.getId());
    
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    
    activityManager.deleteActivity(activity);
  }

  /**
   * Test {@link ActivityManager#deleteActivity(String)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDeleteActivityWithId() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    
    activity = activityManager.getActivity(activity.getId());
    
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    
    activityManager.deleteActivity(activity.getId());
  }
  
  /**
   * Test {@link ActivityManager#saveComment(ExoSocialActivity, ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testSaveComment() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    tearDownActivityList.add(activity);
    
    String commentTitle = "Comment title";
    
    //demo comments on john's activity
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(commentTitle);
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(activity, comment);
    
    List<ExoSocialActivity> demoComments = activityManager.getComments(activity);
    assertNotNull("demoComments must not be null", demoComments);
    assertEquals("demoComments.size() must return: 1", 1, demoComments.size());
    
    assertEquals("demoComments.get(0).getTitle() must return: " + commentTitle, 
                 commentTitle, demoComments.get(0).getTitle());
    assertEquals("demoComments.get(0).getUserId() must return: " + demoIdentity.getId(), demoIdentity.getId(), demoComments.get(0).getUserId());

    ExoSocialActivity gotParentActivity = activityManager.getParentActivity(comment);
    assertNotNull(gotParentActivity);
    assertEquals(activity.getId(), gotParentActivity.getId());
    assertEquals(1, gotParentActivity.getReplyToId().length);
    assertEquals(comment.getId(), gotParentActivity.getReplyToId()[0]);

  }
  
  /**
   * Test {@link ActivityManager#getCommentsWithListAccess(ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetCommentsWithListAccess() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);
    
    int total = 10;
    
    for (int i = 0; i < total; i ++) {
      ExoSocialActivity maryComment = new ExoSocialActivityImpl();
      maryComment.setUserId(maryIdentity.getId());
      maryComment.setTitle("mary comment");
      activityManager.saveComment(demoActivity, maryComment);
    }
    
    RealtimeListAccess<ExoSocialActivity> maryComments = activityManager.getCommentsWithListAccess(demoActivity);
    assertNotNull("maryComments must not be null", maryComments);
    assertEquals("maryComments.getSize() must return: 10", total, maryComments.getSize());
    
  }
  
  /**
   * Test {@link ActivityManager#deleteComment(ExoSocialActivity, ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDeleteComment() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);
    
    ExoSocialActivity maryComment = new ExoSocialActivityImpl();
    maryComment.setTitle("mary comment");
    maryComment.setUserId(maryIdentity.getId());
    activityManager.saveComment(demoActivity, maryComment);
    
    activityManager.deleteComment(demoActivity, maryComment);
    
    assertEquals("activityManager.getComments(demoActivity).size() must return: 0", 0, activityManager.getComments(demoActivity).size());
  }
  
  /**
   * Test {@link ActivityManager#saveLike(ExoSocialActivity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3s
   */
  public void testSaveLike() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("&\"demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds() must return: 0",
                 0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.saveLike(demoActivity, johnIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 1", 1, demoActivity.getLikeIdentityIds().length);
    assertEquals("&\"demo activity", demoActivity.getTitle());
  }
  /**
   * Test {@link ActivityManager#saveLike(ExoSocialActivity, Identity)}
   *  for case not change the template param after liked.
   * 
   * @throws Exception
   * @since 4.0.5
   */  
  public void testSaveLikeNotChangeTemplateParam() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("title");
    demoActivity.setUserId(demoActivity.getId());
    
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("link", "http://exoplatform.com?test=<script>");
    demoActivity.setTemplateParams(templateParams);
    
    
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds() must return: 0",
                 0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.saveLike(demoActivity, johnIdentity);
    
    ExoSocialActivity likedActivity = activityManager.getActivity(demoActivity.getId());
    
    assertEquals(1, likedActivity.getLikeIdentityIds().length);
    assertEquals(templateParams.get("link"), likedActivity.getTemplateParams().get("link"));
  }
  
  /**
   * Test {@link ActivityManager#deleteLike(ExoSocialActivity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDeleteLike() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds() must return: 0",
                 0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.saveLike(demoActivity, johnIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 1", 1, demoActivity.getLikeIdentityIds().length);
    
    activityManager.deleteLike(demoActivity, johnIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.deleteLike(demoActivity, maryIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.deleteLike(demoActivity, rootIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);
  }
  
  /**
   * Test {@link ActivityManager#deleteLike(ExoSocialActivity, Identity)}
   *  for case not change the template param after liked.
   * 
   * @throws Exception
   * @since 4.0.5
   */  
  public void testDeleteLikeNotChangeTemplateParam() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("title");
    demoActivity.setUserId(demoActivity.getId());
    
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("link", "http://exoplatform.com?test=<script>");
    demoActivity.setTemplateParams(templateParams);
    
    
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    activityManager.saveLike(demoActivity, johnIdentity);
    ExoSocialActivity likedActivity = activityManager.getActivity(demoActivity.getId());
    
    assertEquals(1, likedActivity.getLikeIdentityIds().length);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    activityManager.deleteLike(demoActivity, johnIdentity);
    ExoSocialActivity deleteLikeActivity = activityManager.getActivity(demoActivity.getId());
    
    assertEquals(0, deleteLikeActivity.getLikeIdentityIds().length);
    assertEquals(templateParams.get("link"), deleteLikeActivity.getTemplateParams().get("link"));
  }  
  /**
   * Test {@link ActivityManager#getActivitiesWithListAccess(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetActivitiesWithListAccess() throws Exception {
    int total = 10;
    for (int i = 0; i < total; i ++) {
      ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
      demoActivity.setTitle("demo activity");
      demoActivity.setUserId(demoActivity.getId());
      activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
      tearDownActivityList.add(demoActivity);
    }

    RealtimeListAccess<ExoSocialActivity> demoListAccess = activityManager.getActivitiesWithListAccess(demoIdentity);
    assertNotNull("demoListAccess must not be null", demoListAccess);
    assertEquals("demoListAccess.getSize() must return: 10", 10, demoListAccess.getSize());
  }
  
  
  /**
   * Test {@link ActivityManager#getActivitiesOfConnectionsWithListAccess(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  
  /**
  public void testGetActivitiesOfConnectionsWithListAccess() throws Exception {
    ExoSocialActivity baseActivity = null;
    for (int i = 0; i < 10; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(johnIdentity.getId());
      activityManager.saveActivityNoReturn(johnIdentity, activity);
      tearDownActivityList.add(activity);
      if (i == 5) {
        baseActivity = activity;
      }
    }
    
    RealtimeListAccess<ExoSocialActivity> demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities);
    assertEquals("demoConnectionActivities.getSize() must return: 0", 0, demoConnectionActivities.getSize());
    
    Relationship demoJohnRelationship = relationshipManager.invite(demoIdentity, johnIdentity);
    relationshipManager.confirm(demoJohnRelationship);
    
    demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities);
    assertEquals("demoConnectionActivities.getSize() must return: 10", 10, demoConnectionActivities.getSize());
    assertEquals("demoConnectionActivities.getNumberOfNewer(baseActivity)", 4,
                 demoConnectionActivities.getNumberOfNewer(baseActivity));
    assertEquals("demoConnectionActivities.getNumberOfOlder(baseActivity) must return: 5", 5,
                 demoConnectionActivities.getNumberOfOlder(baseActivity));
    
    for (int i = 0; i < 10; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(maryIdentity.getId());
      activityManager.saveActivityNoReturn(maryIdentity, activity);
      tearDownActivityList.add(activity);
      if (i == 5) {
        baseActivity = activity;
      }
    }
    
    Relationship demoMaryRelationship = relationshipManager.invite(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoMaryRelationship);
    
    demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities);
    assertEquals("demoConnectionActivities.getSize() must return: 20", 20, demoConnectionActivities.getSize());
    assertEquals("demoConnectionActivities.getNumberOfNewer(baseActivity)", 4,
                 demoConnectionActivities.getNumberOfNewer(baseActivity));
    assertEquals("demoConnectionActivities.getNumberOfOlder(baseActivity) must return: 15", 15,
                 demoConnectionActivities.getNumberOfOlder(baseActivity));
    
    relationshipManager.remove(demoJohnRelationship);
    relationshipManager.remove(demoMaryRelationship);
  }
  
  **/
  
  /**
   * Test {@link ActivityManager#getActivitiesOfUserSpacesWithListAccess(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3s
   */
  
  
  public void testGetActivitiesOfUserSpacesWithListAccess() throws Exception {
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    
    int totalNumber = 10;
    
    //demo posts activities to space
    for (int i = 0; i < totalNumber; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
      tearDownActivityList.add(activity);
    }
    
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("space must not be null", space);
    assertEquals("space.getDisplayName() must return: my space 0", "my space 0", space.getDisplayName());
    assertEquals("space.getDescription() must return: add new space 0", "add new space 0", space.getDescription());
    
    RealtimeListAccess<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertNotNull("demoActivities must not be null", demoActivities);
    assertEquals("demoActivities.getSize() must return: 10", 10, demoActivities.getSize());
    
    Space space2 = this.getSpaceInstance(spaceService, 1);
    Identity spaceIdentity2 = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space2.getPrettyName(), false);
    
    //demo posts activities to space2
    for (int i = 0; i < totalNumber; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity2, activity);
      tearDownActivityList.add(activity);
    }
    
    space2 = spaceService.getSpaceByDisplayName(space2.getDisplayName());
    assertNotNull("space2 must not be null", space2);
    assertEquals("space2.getDisplayName() must return: my space 1", "my space 1", space2.getDisplayName());
    assertEquals("space2.getDescription() must return: add new space 1", "add new space 1", space2.getDescription());
    
    demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertNotNull("demoActivities must not be null", demoActivities);
    assertEquals("demoActivities.getSize() must return: 20", 20, demoActivities.getSize());
    
    demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(maryIdentity);
    assertNotNull("demoActivities must not be null", demoActivities);
    assertEquals("demoActivities.getSize() must return: 0", 0, demoActivities.getSize());
    
  }
  
  public void testGetActivityFeedWithListAccess() throws Exception {
    this.populateActivityMass(demoIdentity, 3);
    this.populateActivityMass(maryIdentity, 3);
    this.populateActivityMass(johnIdentity, 2);
    
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    populateActivityMass(spaceIdentity, 5);

    RealtimeListAccess<ExoSocialActivity> demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals("demoActivityFeed.getSize() must be 8", 8, demoActivityFeed.getSize());
    assertEquals(8, demoActivityFeed.load(0, 10).length);
    
    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    assertEquals(8, activityManager.getActivityFeedWithListAccess(demoIdentity).getSize());

    relationshipManager.confirm(demoIdentity, maryIdentity);
    demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals("demoActivityFeed.getSize() must return 11", 11, demoActivityFeed.getSize());
    assertEquals(11, demoActivityFeed.load(0, 15).length);
    assertEquals(6, demoActivityFeed.load(5, 15).length);
    
    RealtimeListAccess<ExoSocialActivity> maryActivityFeed = activityManager.getActivityFeedWithListAccess(maryIdentity);
    assertEquals("maryActivityFeed.getSize() must return 6", 6, maryActivityFeed.getSize());
    assertEquals(6, maryActivityFeed.load(0, 10).length);
    
    // Create demo's activity on space
    createActivityToOtherIdentity(demoIdentity, spaceIdentity, 5);

    // after that the feed of demo with have 16
    demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(16, demoActivityFeed.getSize());

    // demo's Space feed must be be 5
    RealtimeListAccess<ExoSocialActivity> demoActivitiesSpaceFeed = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertEquals(10, demoActivitiesSpaceFeed.getSize());
    assertEquals(10, demoActivitiesSpaceFeed.load(0, 10).length);

    // the feed of mary must be the same because mary not the member of space
    maryActivityFeed = activityManager.getActivityFeedWithListAccess(maryIdentity);
    assertEquals(6, maryActivityFeed.getSize());

    // john not friend of demo but member of space
    RealtimeListAccess<ExoSocialActivity> johnSpaceActivitiesFeed = activityManager.getActivitiesOfUserSpacesWithListAccess(johnIdentity);
    assertEquals("johnSpaceActivitiesFeed.getSize() must return 10", 10, johnSpaceActivitiesFeed.getSize());

    relationshipManager.delete(demoMaryConnection);
    spaceService.deleteSpace(space);
  }
  
  public void testLoadMoreActivities() throws Exception {
    this.populateActivityMass(demoIdentity, 30);
    RealtimeListAccess<ExoSocialActivity> demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(30, demoActivityFeed.getSize());
    assertEquals(10, demoActivityFeed.load(0, 10).length);
    assertEquals(20, demoActivityFeed.load(0, 20).length);
    assertEquals(10, demoActivityFeed.load(20, 10).length);
    assertEquals(15, demoActivityFeed.load(15, 20).length);
  }
  
  /**
   * Test {@link ActivityManager#getComments(ExoSocialActivity)}
   * 
   * @throws ActivityStorageException
   */
  public  void testGetCommentWithHtmlContent() throws ActivityStorageException {
    String htmlString = "<span><strong>foo</strong>bar<script>zed</script></span>";
    String htmlRemovedString = "<span><strong>foo</strong>bar&lt;script&gt;zed&lt;/script&gt;</span>";
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("blah blah");
    activityManager.saveActivity(rootIdentity, activity);

    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(htmlString);
    comment.setUserId(rootIdentity.getId());
    comment.setBody(htmlString);
    activityManager.saveComment(activity, comment);
    assertNotNull("comment.getId() must not be null", comment.getId());

    List<ExoSocialActivity> comments = activityManager.getComments(activity);
    assertEquals(1, comments.size());
    assertEquals(htmlString, comments.get(0).getBody());
    assertEquals(htmlString, comments.get(0).getTitle());
    tearDownActivityList.add(activity);    
  }
  
  /**
   * 
   * 
   * @throws ActivityStorageException
   */
  public  void testGetComment() throws ActivityStorageException {
    ExoSocialActivity activity = new ExoSocialActivityImpl();;
    activity.setTitle("blah blah");
    activityManager.saveActivity(rootIdentity, activity);

    ExoSocialActivity comment = new ExoSocialActivityImpl();;
    comment.setTitle("comment blah blah");
    comment.setUserId(rootIdentity.getId());

    activityManager.saveComment(activity, comment);

    assertNotNull("comment.getId() must not be null", comment.getId());

    activity = activityManager.getActivity(activity.getId());
    String[] commentsId = activity.getReplyToId();
    assertEquals(comment.getId(), commentsId[0]);
    tearDownActivityList.add(activity);
  }

  /**
   * 
   * 
   * @throws ActivityStorageException
   */
  public  void testGetComments() throws ActivityStorageException {
    ExoSocialActivity activity = new ExoSocialActivityImpl();;
    activity.setTitle("blah blah");
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    List<ExoSocialActivity> comments = new ArrayList<ExoSocialActivity>();
    for (int i = 0; i < 3; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();;
      comment.setTitle("comment " + i);
      comment.setUserId(rootIdentity.getId());
      activityManager.saveComment(activity, comment);
      assertNotNull("comment.getId() must not be null", comment.getId());

      comments.add(comment);
      sleep(5);
    }

    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getCommentsWithListAccess(activity);
    assertEquals(3, listAccess.getSize());
    List<ExoSocialActivity> listComments = listAccess.loadAsList(0, 5);
    assertEquals(3, listComments.size());
    assertEquals("comment 0", listComments.get(0).getTitle());
    assertEquals("comment 1", listComments.get(1).getTitle());
    assertEquals("comment 2", listComments.get(2).getTitle());
    
    
    ExoSocialActivity assertActivity = activityManager.getActivity(activity.getId());
    String[] commentIds = assertActivity.getReplyToId();
    for (int i = 1; i < commentIds.length; i++) {
      assertEquals(comments.get(i - 1).getId(), commentIds[i - 1]);
    }
    tearDownActivityList.add(activity);
  }
  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#deleteComment(String, String)}
   * 
   * @throws ActivityStorageException
   */
  public void testDeleteCommentWithId() throws ActivityStorageException {
    final String title = "Activity Title";
    {
      //FIXBUG: SOC-1194
      //Case: a user create an activity in his stream, then give some comments on it.
      //Delete comments and check
      ExoSocialActivity activity1 = new ExoSocialActivityImpl();;
      activity1.setUserId(demoIdentity.getId());
      activity1.setTitle(title);
      activityManager.saveActivity(demoIdentity, activity1);

      final int numberOfComments = 10;
      final String commentTitle = "Activity Comment";
      for (int i = 0; i < numberOfComments; i++) {
        ExoSocialActivity comment = new ExoSocialActivityImpl();;
        comment.setUserId(demoIdentity.getId());
        comment.setTitle(commentTitle + i);
        activityManager.saveComment(activity1, comment);
      }

      List<ExoSocialActivity> storedCommentList = activityManager.getComments(activity1);

      assertEquals("storedCommentList.size() must return: " + numberOfComments, numberOfComments, storedCommentList.size());

      //delete random 2 comments
      int index1 = new Random().nextInt(numberOfComments - 1);
      int index2 = index1;
      while (index2 == index1) {
        index2 = new Random().nextInt(numberOfComments - 1);
      }

      ExoSocialActivity tobeDeletedComment1 = storedCommentList.get(index1);
      ExoSocialActivity tobeDeletedComment2 = storedCommentList.get(index2);

      activityManager.deleteComment(activity1.getId(), tobeDeletedComment1.getId());
      activityManager.deleteComment(activity1.getId(), tobeDeletedComment2.getId());

      List<ExoSocialActivity> afterDeletedCommentList = activityManager.getComments(activity1);

      assertEquals("afterDeletedCommentList.size() must return: " + (numberOfComments - 2), numberOfComments - 2, afterDeletedCommentList.size());


      tearDownActivityList.add(activity1);

    }
  }

 /**
  * Unit Test for:
  * {@link ActivityManager#getActivities(Identity)}
  * {@link ActivityManager#getActivities(Identity, long, long)}
  * 
  * @throws ActivityStorageException
  */
 public void testGetActivities() throws ActivityStorageException {
   List<ExoSocialActivity> rootActivityList = activityManager.getActivities(rootIdentity);
   assertNotNull("rootActivityList must not be null", rootActivityList);
   assertEquals(0, rootActivityList.size());

   populateActivityMass(rootIdentity, 30);
   List<ExoSocialActivity> activities = activityManager.getActivities(rootIdentity);
   assertNotNull("activities must not be null", activities);
   assertEquals(20, activities.size());

   List<ExoSocialActivity> allActivities = activityManager.getActivities(rootIdentity, 0, 30);

   assertEquals(30, allActivities.size());
 }

 /**
  * Unit Test for:
  * <p>
  * {@link ActivityManager#getActivitiesOfConnections(Identity)}
  * 
  * @throws Exception
  */
 public void testGetActivitiesOfConnections() throws Exception {
   this.populateActivityMass(johnIdentity, 10);
   
   ListAccess<ExoSocialActivity> demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
   assertEquals(0, demoConnectionActivities.load(0, 20).length);
   assertEquals(0, demoConnectionActivities.getSize());
   
   Relationship demoJohnRelationship = relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
   relationshipManager.confirm(johnIdentity, demoIdentity);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
   assertEquals(10, demoConnectionActivities.load(0, 20).length);
   assertEquals(10, demoConnectionActivities.getSize());
   
   this.populateActivityMass(maryIdentity, 10);
   
   Relationship demoMaryRelationship = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
   relationshipManager.confirm(maryIdentity, demoIdentity);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
   assertEquals(20, demoConnectionActivities.load(0, 20).length);
   assertEquals(20, demoConnectionActivities.getSize());
   
   relationshipManager.delete(demoJohnRelationship);
   relationshipManager.delete(demoMaryRelationship);
 }
 
 public void testGetActivitiesOfConnectionswithOffsetLimit() throws Exception {
   this.populateActivityMass(johnIdentity, 10);
   
   RealtimeListAccess<ExoSocialActivity> demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity); 
   assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities.load(0, 20));
   assertEquals("demoConnectionActivities.size() must return: 0", 0, demoConnectionActivities.getSize());
   
   Relationship demoJohnRelationship = relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
   relationshipManager.confirm(demoIdentity, johnIdentity);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
   assertEquals(10, demoConnectionActivities.load(0, 10).length);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
   assertEquals("demoConnectionActivities.size() must return: 10", 10, demoConnectionActivities.getSize());
   
   this.populateActivityMass(maryIdentity, 10);
   
   Relationship demoMaryRelationship = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
   relationshipManager.confirm(demoIdentity, maryIdentity);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
   assertEquals(20, demoConnectionActivities.load(0, 20).length);
   assertEquals("demoConnectionActivities.size() must return: 20", 20, demoConnectionActivities.getSize());
   
   relationshipManager.delete(demoJohnRelationship);
   relationshipManager.delete(demoMaryRelationship);
 }

 /**
  * Unit Test for:
  * <p>
  * {@link ActivityManager#getActivitiesOfUserSpaces(Identity)}
  * 
  * @throws Exception
  */
 public void testGetActivitiesOfUserSpaces() throws Exception {
   Space space = this.getSpaceInstance(spaceService, 0);
   Identity spaceIdentity = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
   
   int totalNumber = 10;
   
   this.populateActivityMass(spaceIdentity, totalNumber);
   
   List<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfUserSpaces(demoIdentity);
   assertNotNull("demoActivities must not be null", demoActivities);
   assertEquals("demoActivities.size() must return: 10", 10, demoActivities.size());
   
   Space space2 = this.getSpaceInstance(spaceService, 1);
   Identity spaceIdentity2 = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space2.getPrettyName(), false);
   
   this.populateActivityMass(spaceIdentity2, totalNumber);
   
   demoActivities = activityManager.getActivitiesOfUserSpaces(demoIdentity);
   assertNotNull("demoActivities must not be null", demoActivities);
   assertEquals("demoActivities.size() must return: 20", 20, demoActivities.size());
   
   demoActivities = activityManager.getActivitiesOfUserSpaces(maryIdentity);
   assertNotNull("demoActivities must not be null", demoActivities);
   assertEquals("demoActivities.size() must return: 0", 0, demoActivities.size());
   
   spaceService.deleteSpace(space);
   spaceService.deleteSpace(space2);
 }

  /**
   * Test {@link ActivityManager#getActivities(Identity, long, long)}
   * 
   * @throws ActivityStorageException
   */
  public void testGetActivitiesByPagingWithoutCreatingComments() throws ActivityStorageException {
    final int totalActivityCount = 9;
    final int retrievedCount = 7;

    this.populateActivityMass(johnIdentity, totalActivityCount);

    List<ExoSocialActivity> activities = activityManager.getActivities(johnIdentity, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());
  }

  public void testRemoveLike() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());

    assertEquals("demoActivity.getLikeIdentityIds() must return: 0",
                 0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.saveLike(demoActivity, johnIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 1", 1, demoActivity.getLikeIdentityIds().length);
    
    activityManager.removeLike(demoActivity, johnIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.removeLike(demoActivity, maryIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);
    
    activityManager.removeLike(demoActivity, rootIdentity);
    
    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);
  }
  
  /**
   * Test {@link ActivityManager#getActivitiesCount(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetActivitiesCount() throws Exception {
    int count = activityManager.getActivitiesCount(rootIdentity);
    assertEquals("count must be: 0", 0, count);

    populateActivityMass(rootIdentity, 30);
    count = activityManager.getActivitiesCount(rootIdentity);
    assertEquals("count must be: 30", 30, count);
  }
  
  public void testSaveManyComments() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);
    
    //john comments on demo's activity
    ExoSocialActivity comment1 = new ExoSocialActivityImpl();
    comment1.setTitle("john comment 1");
    comment1.setUserId(johnIdentity.getId());
    activityManager.saveComment(demoActivity, comment1);
    
    ListAccess<ExoSocialActivity> listAccess = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(1, listAccess.getSize());
    assertEquals(1, listAccess.load(0, 10).length);
    
    ExoSocialActivity comment2 = new ExoSocialActivityImpl();
    comment2.setTitle("john comment 2");
    comment2.setUserId(johnIdentity.getId());
    activityManager.saveComment(demoActivity, comment2);
    
    listAccess = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(1, listAccess.getSize());
    assertEquals(1, listAccess.load(0, 10).length);
  }
  
  public void testGetLastIdenties() throws Exception {
    Mockito.when(mockProfileSearch.search(Mockito.any(Identity.class), Mockito.any(ProfileFilter.class),
            Mockito.any(Relationship.Type.class), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(Arrays.asList(paulIdentity))
            .thenReturn(Arrays.asList(paulIdentity))
            .thenReturn(Arrays.asList(paulIdentity, jameIdentity, raulIdentity, ghostIdentity, demoIdentity));
    List<Identity> lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    Identity id1 = lastIds.get(0);
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(id1, lastIds.get(0));
    lastIds = identityManager.getLastIdentities(5);
    assertEquals(5, lastIds.size());
    assertEquals(id1, lastIds.get(0));
    OrganizationService os = (OrganizationService) getContainer().getComponentInstanceOfType(OrganizationService.class);
    User user1 = os.getUserHandler().createUserInstance("newId1");
    os.getUserHandler().createUser(user1, false);
    Identity newId1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "newId1", false);

    Mockito.when(mockProfileSearch.search(Mockito.any(Identity.class), Mockito.any(ProfileFilter.class),
            Mockito.any(Relationship.Type.class), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(Arrays.asList(newId1))
            .thenReturn(Arrays.asList(paulIdentity));

    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(newId1, lastIds.get(0));
    identityManager.deleteIdentity(newId1);
    assertTrue(identityManager.getIdentity(newId1.getId(), false).isDeleted());
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(id1, lastIds.get(0));
    User user2 = os.getUserHandler().createUserInstance("newId2");
    os.getUserHandler().createUser(user2, false);
    Identity newId2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "newId2", true);

    Mockito.when(mockProfileSearch.search(Mockito.any(Identity.class), Mockito.any(ProfileFilter.class),
            Mockito.any(Relationship.Type.class), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(Arrays.asList(newId2, paulIdentity, jameIdentity, raulIdentity, ghostIdentity))
            .thenReturn(Arrays.asList(paulIdentity, jameIdentity, raulIdentity, ghostIdentity, demoIdentity));

    lastIds = identityManager.getLastIdentities(5);
    assertEquals(5, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    identityManager.deleteIdentity(newId2);
    assertTrue(identityManager.getIdentity(newId2.getId(), true).isDeleted());
    lastIds = identityManager.getLastIdentities(5);
    assertEquals(5, lastIds.size());
    assertEquals(id1, lastIds.get(0));


    newId1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "newId1", false);
    newId2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "newId2", true);

    Mockito.when(mockProfileSearch.search(Mockito.any(Identity.class), Mockito.any(ProfileFilter.class),
            Mockito.any(Relationship.Type.class), Mockito.anyLong(), Mockito.anyLong()))
            .thenReturn(Arrays.asList(newId2))
            .thenReturn(Arrays.asList(newId2, newId1))
            .thenReturn(Arrays.asList(newId2))
            .thenReturn(Arrays.asList(newId2, paulIdentity))
            .thenReturn(Arrays.asList(paulIdentity));

    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    lastIds = identityManager.getLastIdentities(2);
    assertEquals(2, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    assertEquals(newId1, lastIds.get(1));
    identityManager.deleteIdentity(newId1);
    os.getUserHandler().removeUser("newId1", false);
    assertTrue(identityManager.getIdentity(newId1.getId(), true).isDeleted());
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    lastIds = identityManager.getLastIdentities(2);
    assertEquals(2, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    assertFalse(newId1.equals(lastIds.get(1)));
    identityManager.deleteIdentity(newId2);
    os.getUserHandler().removeUser("newId2", false);
    assertTrue(identityManager.getIdentity(newId2.getId(), false).isDeleted());
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(id1, lastIds.get(0));
  }

  public void testMentionActivity() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(rootIdentity, activity);
    tearDownActivityList.add(activity);
    
    ExoSocialActivity got = activityManager.getActivity(activity.getId());
    assertEquals(0, got.getMentionedIds().length);

    RealtimeListAccess<ExoSocialActivity> demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(0, demoActivityFeed.getSize());
    assertEquals(0, demoActivityFeed.load(0, 10).length);
    
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("mary mention @demo @john");
    comment.setUserId(maryIdentity.getId());
    activityManager.saveComment(activity, comment);
    
    got = activityManager.getActivity(activity.getId());
    assertEquals(2, got.getMentionedIds().length);
    
    demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(1, demoActivityFeed.getSize());
    assertEquals(1, demoActivityFeed.load(0, 10).length);
    
    activityManager.deleteComment(activity, comment);
    
    got = activityManager.getActivity(activity.getId());
    assertEquals(0, got.getMentionedIds().length);
    
    demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(0, demoActivityFeed.getSize());
    assertEquals(0, demoActivityFeed.load(0, 10).length);
  }
  
  public void testLikeCommentActivity() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(rootIdentity, activity);
    tearDownActivityList.add(activity);
    
    //demo comment on root's activity
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("demo comment");
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(activity, comment);
    
    //check feed of demo
    RealtimeListAccess<ExoSocialActivity> demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(1, demoActivities.getSize());
    assertEquals(1, demoActivities.load(0, 10).length);
    
    //check my activities of demo
    demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity);
    assertEquals(1, demoActivities.getSize());
    assertEquals(1, demoActivities.load(0, 10).length);

    //john like root activity
    activityManager.saveLike(activity, johnIdentity);
    
    RealtimeListAccess<ExoSocialActivity> johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(1, johnActivities.getSize());
    assertEquals(1, johnActivities.load(0, 10).length);
    
    //check my activities of demo
    johnActivities = activityManager.getActivitiesWithListAccess(johnIdentity);
    assertEquals(1, johnActivities.getSize());
    assertEquals(1, johnActivities.load(0, 10).length);
  }
  
  /**
   * Populates activity.
   * 
   * @param user
   * @param number
   */
  private void populateActivityMass(Identity user, int number) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();;
      activity.setTitle("title " + i);
      activity.setUserId(user.getId());
      try {
        activityManager.saveActivityNoReturn(user, activity);
        tearDownActivityList.add(activity);
      } catch (Exception e) {
        LOG.error("can not save activity.", e);
      }
    }
  }
  
  private void createActivityToOtherIdentity(Identity posterIdentity,
      Identity targetIdentity, int number) {

    // if(!relationshipManager.get(posterIdentity,
    // targetIdentity).getStatus().equals(Type.CONFIRMED)){
    // return;
    // }

    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();

      activity.setTitle("title " + i);
      activity.setUserId(posterIdentity.getId());
      try {
        activityManager.saveActivityNoReturn(targetIdentity, activity);
        tearDownActivityList.add(activity);
      } catch (Exception e) {
        LOG.error("can not save activity.", e);
      }
    }
  }
  
  /**
   * Gets an instance of the space.
   * 
   * @param spaceService
   * @param number
   * @return
   * @throws Exception
   * @since 1.2.0-GA
   */
  private Space getSpaceInstance(SpaceService spaceService, int number) throws Exception {
    try {
      Space space = new Space();
      space.setDisplayName("my space " + number);
      space.setPrettyName(space.getDisplayName());
      space.setRegistration(Space.OPEN);
      space.setDescription("add new space " + number);
      space.setType(DefaultSpaceApplicationHandler.NAME);
      space.setVisibility(Space.PUBLIC);
      space.setRegistration(Space.VALIDATION);
      space.setPriority(Space.INTERMEDIATE_PRIORITY);
      space.setGroupId(SpaceUtils.SPACE_GROUP + "/" + space.getPrettyName());
      space.setUrl(space.getPrettyName());
      String[] managers = new String[] { "demo", "john" };
      String[] members = new String[] { "raul", "ghost", "demo", "john" };
      String[] invitedUsers = new String[] { "mary", "paul"};
      String[] pendingUsers = new String[] { "jame"};
      space.setInvitedUsers(invitedUsers);
      space.setPendingUsers(pendingUsers);
      space.setManagers(managers);
      space.setMembers(members);
      spaceService.saveSpace(space, true);
      return space;
    } finally {
      StorageUtils.persist();
    }
  }
}