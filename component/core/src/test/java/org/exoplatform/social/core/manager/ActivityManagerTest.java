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
package org.exoplatform.social.core.manager;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.application.RelationshipPublisher;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.relationship.RelationshipEvent;
import org.exoplatform.social.core.relationship.RelationshipEvent.Type;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Unit Test for {@link ActivityManager}, including cache tests.
 * @author hoat_le
 */
public class ActivityManagerTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(ActivityManagerTest.class);
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Space> tearDownSpaceList;
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;
  private Identity ghostIdentity;
  private Identity raulIdentity;
  private Identity jameIdentity;
  private Identity paulIdentity;

  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;
  private ActivityManager activityManager;
  private SpaceService spaceService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager =  (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);
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
    
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(jameIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);
    
    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
      if (spaceIdentity != null) {
        identityManager.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }
    
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
    List<String> values = new ArrayList(gotTemplateParams.values());
    assertEquals("value 1", values.get(0));
    assertEquals("value 2", values.get(1));
    assertEquals("value 3", values.get(2));
    
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
    ExoSocialActivity parentActivity = activityManager.getParentActivity(gotComment);
    assertNotNull("parentActivity must not be null", parentActivity);
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
    assertEquals(1, activity.getReplyToId().length);
    assertEquals(comment.getId(), activity.getReplyToId()[0]);

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
    
    ExoSocialActivity baseActivity = new ExoSocialActivityImpl();
    
    for (int i = 0; i < total; i ++) {
      ExoSocialActivity maryComment = new ExoSocialActivityImpl();
      maryComment.setUserId(maryIdentity.getId());
      maryComment.setTitle("mary comment");
      activityManager.saveComment(demoActivity, maryComment);
      if (i == 5) {
        baseActivity = maryComment;
      }
    }
    
    RealtimeListAccess<ExoSocialActivity> maryComments = activityManager.getCommentsWithListAccess(demoActivity);
    assertNotNull("maryComments must not be null", maryComments);
    assertEquals("maryComments.getSize() must return: 10", total, maryComments.getSize());
    
    assertEquals("maryComments.getNumberOfNewer(baseActivity, 10) must return: 5", 5,
                 maryComments.getNumberOfNewer(baseActivity));
    assertEquals("maryComments.getNumberOfOlder(baseActivity) must return: 4", 4,
                 maryComments.getNumberOfOlder(baseActivity));
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
    assertEquals("&amp;&quot;demo activity", demoActivity.getTitle());
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
    ExoSocialActivity baseActivity = null;
    for (int i = 0; i < total; i ++) {
      ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
      demoActivity.setTitle("demo activity");
      demoActivity.setUserId(demoActivity.getId());
      activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
      tearDownActivityList.add(demoActivity);
      if (i == 5) {
        baseActivity = demoActivity;
      }
    }

    RealtimeListAccess<ExoSocialActivity> demoListAccess = activityManager.getActivitiesWithListAccess(demoIdentity);
    assertNotNull("demoListAccess must not be null", demoListAccess);
    assertEquals("demoListAccess.getSize() must return: 10", 10, demoListAccess.getSize());
    assertEquals("demoListAccess.getNumberOfNewer(baseActivity) must return: 4", 4,
                 demoListAccess.getNumberOfNewer(baseActivity));
    assertEquals("demoListAccess.getNumberOfOlder(baseActivity) must return: 5", 5,
                 demoListAccess.getNumberOfOlder(baseActivity));
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
    tearDownSpaceList.add(space);
    Identity spaceIdentity = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    
    int totalNumber = 10;
    
    ExoSocialActivity baseActivity = null;
    
    //demo posts activities to space
    for (int i = 0; i < totalNumber; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
      tearDownActivityList.add(activity);
      if (i == 5) {
        baseActivity = activity;
      }
    }
    
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("space must not be null", space);
    assertEquals("space.getDisplayName() must return: my space 0", "my space 0", space.getDisplayName());
    assertEquals("space.getDescription() must return: add new space 0", "add new space 0", space.getDescription());
    
    RealtimeListAccess<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertNotNull("demoActivities must not be null", demoActivities);
    assertEquals("demoActivities.getSize() must return: 10", 10, demoActivities.getSize());
    assertEquals("demoActivities.getNumberOfNewer(baseActivity) must return: 4", 4,
                 demoActivities.getNumberOfNewer(baseActivity));
    assertEquals("demoActivities.getNumberOfOlder(baseActivity) must return: 5", 5,
                 demoActivities.getNumberOfOlder(baseActivity));
    
    Space space2 = this.getSpaceInstance(spaceService, 1);
    tearDownSpaceList.add(space2);
    Identity spaceIdentity2 = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space2.getPrettyName(), false);
    
    //demo posts activities to space2
    for (int i = 0; i < totalNumber; i ++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity2, activity);
      tearDownActivityList.add(activity);
      if (i == 5) {
        baseActivity = activity;
      }
    }
    
    space2 = spaceService.getSpaceByDisplayName(space2.getDisplayName());
    assertNotNull("space2 must not be null", space2);
    assertEquals("space2.getDisplayName() must return: my space 1", "my space 1", space2.getDisplayName());
    assertEquals("space2.getDescription() must return: add new space 1", "add new space 1", space2.getDescription());
    
    demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertNotNull("demoActivities must not be null", demoActivities);
    assertEquals("demoActivities.getSize() must return: 20", 20, demoActivities.getSize());
    assertEquals("demoActivities.getNumberOfNewer(baseActivity) must return 4", 4, 
                 demoActivities.getNumberOfNewer(baseActivity));
    assertEquals("demoActivities.getNumberOfOlder(baseActivity) must return 15", 15, 
                 demoActivities.getNumberOfOlder(baseActivity));
    
    demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(maryIdentity);
    assertNotNull("demoActivities must not be null", demoActivities);
    assertEquals("demoActivities.getSize() must return: 0", 0, demoActivities.getSize());
    
  }
  
  /**
   * Test {@link ActivityManager#getActivityFeedWithListAccess(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetActivityFeedWithListAccess() throws Exception {
    this.populateActivityMass(demoIdentity, 3);
    this.populateActivityMass(maryIdentity, 3);
    this.populateActivityMass(johnIdentity, 2);
    
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    populateActivityMass(spaceIdentity, 5);

    RealtimeListAccess<ExoSocialActivity> demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals("demoActivityFeed.getSize() must be 8", 8, demoActivityFeed.getSize());

    Relationship demoMaryConnection = relationshipManager.invite(demoIdentity, maryIdentity);
    assertEquals(8, activityManager.getActivityFeedWithListAccess(demoIdentity).getSize());

    relationshipManager.confirm(demoMaryConnection);
    RealtimeListAccess<ExoSocialActivity> demoActivityFeed2 = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals("demoActivityFeed2.getSize() must return 11", 11, demoActivityFeed2.getSize());
    RealtimeListAccess<ExoSocialActivity> maryActivityFeed = activityManager.getActivityFeedWithListAccess(maryIdentity);
    assertEquals("maryActivityFeed.getSize() must return 6", 6, maryActivityFeed.getSize());
    
    // Create demo's activity on space
    createActivityToOtherIdentity(demoIdentity, spaceIdentity, 5);

    // after that the feed of demo with have 16
    RealtimeListAccess<ExoSocialActivity> demoActivityFeed3 = activityManager
        .getActivityFeedWithListAccess(demoIdentity);
    assertEquals("demoActivityFeed3.getSize() must return 16", 16,
        demoActivityFeed3.getSize());

    // demo's Space feed must be be 5
    RealtimeListAccess demoActivitiesSpaceFeed = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertEquals("demoActivitiesSpaceFeed.getSize() must return 10", 10, demoActivitiesSpaceFeed.getSize());

    // the feed of mary must be the same because mary not the member of space
    RealtimeListAccess<ExoSocialActivity> maryActivityFeed2 = activityManager.getActivityFeedWithListAccess(maryIdentity);
    assertEquals("maryActivityFeed2.getSize() must return 6", 6, maryActivityFeed2.getSize());

    // john not friend of demo but member of space
    RealtimeListAccess johnSpaceActivitiesFeed = activityManager.getActivitiesOfUserSpacesWithListAccess(johnIdentity);
    assertEquals("johnSpaceActivitiesFeed.getSize() must return 10", 10, johnSpaceActivitiesFeed.getSize());

    relationshipManager.remove(demoMaryConnection);
    spaceService.deleteSpace(space);
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
    assertEquals(htmlRemovedString, comments.get(0).getBody());
    assertEquals(htmlRemovedString, comments.get(0).getTitle());
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
    activityManager.saveActivity(rootIdentity, activity);

    List<ExoSocialActivity> comments = new ArrayList<ExoSocialActivity>();
    for (int i = 0; i < 10; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();;
      comment.setTitle("comment blah blah");
      comment.setUserId(rootIdentity.getId());
      activityManager.saveComment(activity, comment);
      assertNotNull("comment.getId() must not be null", comment.getId());

      comments.add(comment);
    }

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
   
   List<ExoSocialActivity> demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity);
   assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities);
   assertEquals("demoConnectionActivities.size() must return: 0", 0, demoConnectionActivities.size());
   
   Relationship demoJohnRelationship = relationshipManager.invite(demoIdentity, johnIdentity);
   relationshipManager.confirm(demoJohnRelationship);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity);
   assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities);
   assertEquals("demoConnectionActivities.size() must return: 10", 10, demoConnectionActivities.size());
   
   this.populateActivityMass(maryIdentity, 10);
   
   Relationship demoMaryRelationship = relationshipManager.invite(demoIdentity, maryIdentity);
   relationshipManager.confirm(demoMaryRelationship);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity);
   assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities);
   assertEquals("demoConnectionActivities.size() must return: 20", 20, demoConnectionActivities.size());
   
   relationshipManager.remove(demoJohnRelationship);
   relationshipManager.remove(demoMaryRelationship);
 }
 
 public void testRelationshipActivities() throws Exception {
   RelationshipPublisher relationshipPublisher = (RelationshipPublisher) getContainer().getComponentInstanceOfType(RelationshipPublisher.class);
   Relationship rootDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
   relationshipManager.confirm(demoIdentity, rootIdentity);
   relationshipPublisher.confirmed(new RelationshipEvent(Type.CONFIRM, relationshipManager, rootDemoRelationship));
   Relationship demoJohnRelationship = relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
   relationshipManager.confirm(johnIdentity, demoIdentity);
   relationshipPublisher.confirmed(new RelationshipEvent(Type.CONFIRM, relationshipManager, demoJohnRelationship));
   
   IdentityStorage identityStorage =  (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
   String johnActivityId =  identityStorage.getProfileActivityId(johnIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
   ExoSocialActivity johnActivity = activityManager.getActivity(johnActivityId);
   assertNotNull(johnActivity);
   tearDownActivityList.add(johnActivity);
   String rootActivityId =  identityStorage.getProfileActivityId(rootIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
   ExoSocialActivity rootActivity = activityManager.getActivity(rootActivityId);
   tearDownActivityList.add(rootActivity);
   assertNotNull(rootActivity);
   String demoActivityId =  identityStorage.getProfileActivityId(demoIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
   ExoSocialActivity maryActivity = activityManager.getActivity(demoActivityId);
   tearDownActivityList.add(maryActivity);
   assertNotNull(maryActivity);
   
   List<ExoSocialActivity> activities = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 10);
   assertEquals(3, activities.size());
   activities = activityManager.getActivityFeedWithListAccess(rootIdentity).loadAsList(0, 10);
   assertEquals(2, activities.size());
   
   //on john's stream, there are 2 activities, one of john and one of demo
   activities = activityManager.getActivityFeedWithListAccess(johnIdentity).loadAsList(0, 10);
   assertEquals(2, activities.size());
   assertEquals(johnActivityId, activities.get(0).getId());
   assertEquals(demoActivityId, activities.get(1).getId());
   
   //root posts on demo stream
   ExoSocialActivity activity = new ExoSocialActivityImpl();
   activity.setTitle("Root posts on demo");
   activity.setUserId(rootIdentity.getId());
   activityManager.saveActivityNoReturn(demoIdentity, activity);
   tearDownActivityList.add(activity);
   
   //john must see this activity but NOK
   activities = activityManager.getActivityFeedWithListAccess(johnIdentity).loadAsList(0, 10);
   assertEquals(3, activities.size());

   //delete 2 activities on john's stream, the activity posted by root appears now ==> NOK
   activityManager.deleteActivity(demoActivityId);
   activityManager.deleteActivity(johnActivityId);
   activities = activityManager.getActivityFeedWithListAccess(johnIdentity).loadAsList(0, 10);
   assertEquals(1, activities.size());
   
   relationshipManager.delete(rootDemoRelationship);
   relationshipManager.delete(demoJohnRelationship);
 }
 
 /**
  * Test {@link ActivityManager#getActivitiesOfConnections(Identity, int, int)}
  * 
  * @throws Exception
  * @since 1.2.0-Beta3
  */
 public void testGetActivitiesOfConnectionswithOffsetLimit() throws Exception {
   this.populateActivityMass(johnIdentity, 10);
   
   List<ExoSocialActivity> demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity, 0, 20);
   assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities);
   assertEquals("demoConnectionActivities.size() must return: 0", 0, demoConnectionActivities.size());
   
   Relationship demoJohnRelationship = relationshipManager.invite(demoIdentity, johnIdentity);
   relationshipManager.confirm(demoJohnRelationship);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity, 0, 5);
   assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities);
   assertEquals("demoConnectionActivities.size() must return: 5", 5, demoConnectionActivities.size());
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity, 0, 20);
   assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities);
   assertEquals("demoConnectionActivities.size() must return: 10", 10, demoConnectionActivities.size());
   
   this.populateActivityMass(maryIdentity, 10);
   
   Relationship demoMaryRelationship = relationshipManager.invite(demoIdentity, maryIdentity);
   relationshipManager.confirm(demoMaryRelationship);
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity, 0, 10);
   assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities);
   assertEquals("demoConnectionActivities.size() must return: 10", 10, demoConnectionActivities.size());
   
   demoConnectionActivities = activityManager.getActivitiesOfConnections(demoIdentity, 0, 20);
   assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities);
   assertEquals("demoConnectionActivities.size() must return: 20", 20, demoConnectionActivities.size());
   
   relationshipManager.remove(demoJohnRelationship);
   relationshipManager.remove(demoMaryRelationship);
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

  /**
   * Test {@link ActivityManager#getActivityFeed(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetActivityFeed() throws Exception {
    this.populateActivityMass(demoIdentity, 3);
    this.populateActivityMass(maryIdentity, 3);
    this.populateActivityMass(johnIdentity, 2);
    
    List<ExoSocialActivity> demoActivityFeed = activityManager.getActivityFeed(demoIdentity);
    assertEquals(3, demoActivityFeed.size());

    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    populateActivityMass(spaceIdentity, 5);

    demoActivityFeed = activityManager.getActivityFeed(demoIdentity);
    assertEquals("demoActivityFeed.size() must be 8", 8, demoActivityFeed.size());

    Relationship demoMaryConnection = relationshipManager.invite(demoIdentity, maryIdentity);
    assertEquals(8, activityManager.getActivityFeedWithListAccess(demoIdentity).getSize());

    relationshipManager.confirm(demoIdentity, maryIdentity);
    List<ExoSocialActivity> demoActivityFeed2 = activityManager.getActivityFeed(demoIdentity);
    assertEquals("demoActivityFeed2.size() must return 11", 11, demoActivityFeed2.size());
    List<ExoSocialActivity> maryActivityFeed = activityManager.getActivityFeed(maryIdentity);
    assertEquals("maryActivityFeed.size() must return 6", 6, maryActivityFeed.size());

    relationshipManager.remove(demoMaryConnection);
    spaceService.deleteSpace(space);
  }
  
  /**
   * Test {@link ActivityManager#removeLike(ExoSocialActivity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
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
   * Test {@link ActivityManager#recordActivity(Identity, String, String)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testRecordActivityWithTypeTitle() throws Exception {
    String DEFAULT_ACTIVITY_TYPE = "DEFAULT_ACTIVITY";
    String RELATIONSHIP_ACTIVITY_TYPE = "exosocial:relationship";
    String DOC_ACTIVITY_TYPE = "DOC_ACTIVITY";
    String LINK_ACTIVITY_TYPE = "LINK_ACTIVITY";
    String EMOTION_ACTIVITY_TYPE = "EMOTION_ACTIVITY";
    
    String activityTitle = "activity title";
    String userId = demoIdentity.getId();
    
    ExoSocialActivity activity = null;
    
    activity = activityManager.recordActivity(demoIdentity, DEFAULT_ACTIVITY_TYPE, activityTitle);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    assertEquals("activity.getType() must return: " + DEFAULT_ACTIVITY_TYPE, DEFAULT_ACTIVITY_TYPE, activity.getType());
    
    activity = activityManager.recordActivity(demoIdentity, RELATIONSHIP_ACTIVITY_TYPE, activityTitle);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    assertEquals("activity.getType() must return: " + RELATIONSHIP_ACTIVITY_TYPE, RELATIONSHIP_ACTIVITY_TYPE, activity.getType());
    
    activity = activityManager.recordActivity(demoIdentity, DOC_ACTIVITY_TYPE, activityTitle);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    assertEquals("activity.getType() must return: " + DOC_ACTIVITY_TYPE, DOC_ACTIVITY_TYPE, activity.getType());
    
    activity = activityManager.recordActivity(demoIdentity, LINK_ACTIVITY_TYPE, activityTitle);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    assertEquals("activity.getType() must return: " + LINK_ACTIVITY_TYPE, LINK_ACTIVITY_TYPE, activity.getType());
    
    activity = activityManager.recordActivity(demoIdentity, EMOTION_ACTIVITY_TYPE, activityTitle);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    assertEquals("activity.getType() must return: " + EMOTION_ACTIVITY_TYPE, EMOTION_ACTIVITY_TYPE, activity.getType());
  }
  
  /**
   * Test {@link ActivityManager#recordActivity(Identity, String, String, String)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testRecordActivityWithTypeTitleBody() throws Exception {
    String DEFAULT_ACTIVITY_TYPE = "DEFAULT_ACTIVITY";
    String RELATIONSHIP_ACTIVITY_TYPE = "exosocial:relationship";
    String DOC_ACTIVITY_TYPE = "DOC_ACTIVITY";
    String LINK_ACTIVITY_TYPE = "LINK_ACTIVITY";
    String EMOTION_ACTIVITY_TYPE = "EMOTION_ACTIVITY";
    
    String activityTitle = "activity title";
    String userId = demoIdentity.getId();
    String activityBody = "activity body";
    
    ExoSocialActivity activity = null;
    
    activity = activityManager.recordActivity(demoIdentity, DEFAULT_ACTIVITY_TYPE, activityTitle, activityBody);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    assertEquals("activity.getType() must return: " + DEFAULT_ACTIVITY_TYPE, DEFAULT_ACTIVITY_TYPE, activity.getType());
    assertEquals("activity.getBody() must return: " + activityBody, activityBody, activity.getBody());
    
    activity = activityManager.recordActivity(demoIdentity, RELATIONSHIP_ACTIVITY_TYPE, activityTitle, activityBody);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    assertEquals("activity.getType() must return: " + RELATIONSHIP_ACTIVITY_TYPE, RELATIONSHIP_ACTIVITY_TYPE, activity.getType());
    assertEquals("activity.getBody() must return: " + activityBody, activityBody, activity.getBody());
    
    activity = activityManager.recordActivity(demoIdentity, DOC_ACTIVITY_TYPE, activityTitle, activityBody);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    assertEquals("activity.getType() must return: " + DOC_ACTIVITY_TYPE, DOC_ACTIVITY_TYPE, activity.getType());
    assertEquals("activity.getBody() must return: " + activityBody, activityBody, activity.getBody());
    
    activity = activityManager.recordActivity(demoIdentity, LINK_ACTIVITY_TYPE, activityTitle, activityBody);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    assertEquals("activity.getType() must return: " + LINK_ACTIVITY_TYPE, LINK_ACTIVITY_TYPE, activity.getType());
    assertEquals("activity.getBody() must return: " + activityBody, activityBody, activity.getBody());
    
    activity = activityManager.recordActivity(demoIdentity, EMOTION_ACTIVITY_TYPE, activityTitle, activityBody);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    assertEquals("activity.getType() must return: " + EMOTION_ACTIVITY_TYPE, EMOTION_ACTIVITY_TYPE, activity.getType());
    assertEquals("activity.getBody() must return: " + activityBody, activityBody, activity.getBody());
  }
  
  /**
   * Test {@link ActivityManager#recordActivity(Identity, ExoSocialActivity))}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testRecordActivity() throws Exception {
    String activityTitle = "activity title";
    String userId = demoIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.recordActivity(demoIdentity, activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    
    tearDownActivityList.add(activity);
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
  
  public void testGetLastIdenties() throws Exception {
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
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(newId1, lastIds.get(0));
    identityManager.deleteIdentity(newId1);
    assertNull(identityManager.getIdentity(newId1.getId(), false));
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(id1, lastIds.get(0));
    User user2 = os.getUserHandler().createUserInstance("newId2");
    os.getUserHandler().createUser(user2, false);
    Identity newId2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "newId2", true);
    lastIds = identityManager.getLastIdentities(5);
    assertEquals(5, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    identityManager.deleteIdentity(newId2);
    assertNull(identityManager.getIdentity(newId2.getId(), true));
    lastIds = identityManager.getLastIdentities(5);
    assertEquals(5, lastIds.size());
    assertEquals(id1, lastIds.get(0));
    newId1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "newId1", false);
    newId2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "newId2", true);
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    lastIds = identityManager.getLastIdentities(2);
    assertEquals(2, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    assertEquals(newId1, lastIds.get(1));
    identityManager.deleteIdentity(newId1);
    os.getUserHandler().removeUser("newId1", false);
    assertNull(identityManager.getIdentity(newId1.getId(), true));
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    lastIds = identityManager.getLastIdentities(2);
    assertEquals(2, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    assertFalse(newId1.equals(lastIds.get(1)));
    identityManager.deleteIdentity(newId2);
    os.getUserHandler().removeUser("newId2", false);
    assertNull(identityManager.getIdentity(newId2.getId(), false));
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(id1, lastIds.get(0));
  }
  
  public void testMentionersWhenAddComment() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title");
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    tearDownActivityList.add(activity);
    
    //demo add 2 comments on john's activity
    int numberOfComments = 2;
    ExoSocialActivity deleteComment = null;
    for (int i = 0; i < numberOfComments; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("@demo on comment " + i);
      comment.setUserId(johnIdentity.getId());
      activityManager.saveComment(activity, comment);
      deleteComment = comment;
    }
    
    ExoSocialActivity got = activityManager.getActivity(activity.getId());
    String[] commenters = got.getCommentedIds();
    String[] mentioners = got.getMentionedIds();
    assertEquals(1, commenters.length);
    assertEquals(1, mentioners.length);
    //as demo posts 2 comments, the number associated to his id in the commenter's and mentioners's list must be 2
    assertEquals("2", commenters[0].split("@")[1]);
    assertEquals("2", mentioners[0].split("@")[1]);
    
    //delete a comment
    activityManager.deleteComment(got, deleteComment);
    
    got = activityManager.getActivity(activity.getId());
    commenters = got.getCommentedIds();
    mentioners = got.getMentionedIds();
    assertEquals(1, commenters.length);
    assertEquals(1, mentioners.length);
    assertEquals("1", commenters[0].split("@")[1]);
    assertEquals("1", mentioners[0].split("@")[1]);
  }
  
  public void testMentionActivityOnOthersStream() throws Exception {
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);
    
    List<ExoSocialActivity> activities = activityManager.getActivityFeedWithListAccess(rootIdentity).loadAsList(0, 10);
    assertEquals(0, activities.size());
    activities = activityManager.getActivitiesWithListAccess(rootIdentity).loadAsList(0, 10);
    assertEquals(0, activities.size());
    activities = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 10);
    assertEquals(0, activities.size());
    activities = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, 10);
    assertEquals(0, activities.size());
    
    //root post on demo's stream
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello @demo");
    activity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    activity = activityManager.getActivity(activity.getId());
    assertEquals(1, activity.getMentionedIds().length);
    
    activities = activityManager.getActivityFeedWithListAccess(rootIdentity).loadAsList(0, 10);
    assertEquals(1, activities.size());
    activities = activityManager.getActivitiesWithListAccess(rootIdentity).loadAsList(0, 10);
    assertEquals(1, activities.size());
    activities = activityManager.getActivityFeedWithListAccess(demoIdentity).loadAsList(0, 10);
    assertEquals(1, activities.size());
    activities = activityManager.getActivitiesWithListAccess(demoIdentity).loadAsList(0, 10);
    assertEquals(1, activities.size());
  }

  /**
   *  Test posting activities in case of user is disabled.
   *  
   *  Activity streams of disabled users should not receive any new activity 
   *  until the user account is re-enabled.
   *  - john and demo post some activities on each user's stream.
   *  - make connection between demo and john and check activities count.
   *  - disable demo and check the number of activities of john.
   *  - demo and john post activities on demo (disabled) and check if posting successfully or not.
   *  - re-enable demo and post activities on demo stream to check if posting successfully or not.
   */
  public void testActivitiesOfDisableUsers() throws Exception {
    this.populateActivityMass(demoIdentity, 3);
    this.populateActivityMass(johnIdentity, 3);
    
    RealtimeListAccess<ExoSocialActivity> demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(3, demoActivities.getSize());
    
    RealtimeListAccess<ExoSocialActivity> johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(3, johnActivities.getSize());
    
    // john post activity with mention case.
    createActivityHasMention(johnIdentity, demoIdentity);
    
    demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(4, demoActivities.getSize());
    
    // demo connect to john
    Relationship demoJohnConnection = relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
    relationshipManager.confirm(demoIdentity, johnIdentity);
    
    demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(7, demoActivities.getSize());
    
    johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(7, johnActivities.getSize());
    
    // john post activity.
    createActivity(johnIdentity);
    
    demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(8, demoActivities.getSize());
    johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(8, johnActivities.getSize());
    
    // disable demo
    identityManager.processEnabledIdentity(demoIdentity.getRemoteId(), false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, demoIdentity.getRemoteId(), true);
    
    // john get all activities. existing activities of demo still included.
    johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(8, johnActivities.getSize());
    
    // john post activity, demo is in disabling status so activity reference is not created.
    createActivity(johnIdentity);
    
    // john post activity with mention case, demo is in disabling status so activity reference is not created.
    createActivityHasMention(johnIdentity, demoIdentity);
    
    //
    demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(8, demoActivities.getSize());
    johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(10, johnActivities.getSize());
    
    // check if john can post on demo stream
    ExoSocialActivity johnPostOnDemoActivity = new ExoSocialActivityImpl();

    johnPostOnDemoActivity.setTitle("john post on demo's stream.");
    johnPostOnDemoActivity.setUserId(johnIdentity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, johnPostOnDemoActivity);
    
    demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(8, demoActivities.getSize());
    johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(10, johnActivities.getSize());
    
    // check if demo still can post activity on his stream
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("demo post on his stream.");
    activity.setUserId(demoIdentity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, activity);
    
    demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(8, demoActivities.getSize());
    johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(10, johnActivities.getSize());
    
    // re-enable user
    identityManager.processEnabledIdentity(demoIdentity.getRemoteId(), true);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, demoIdentity.getRemoteId(), true);
    
    demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(8, demoActivities.getSize());
    johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(10, johnActivities.getSize());
    
    // demo post on his stream
    createActivity(demoIdentity);
    
    demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(9, demoActivities.getSize());
    johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(11, johnActivities.getSize());
    
    // john post on demo stream
    createActivityToOtherIdentity(johnIdentity, demoIdentity, 1);
    demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(10, demoActivities.getSize());
    johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(12, johnActivities.getSize());
    
    //
    relationshipManager.delete(demoJohnConnection);
  }
  
  /**
   *
   */
  /*public void testAddProviders() {
    activityManager.addProcessor(new FakeProcessor(10));
    activityManager.addProcessor(new FakeProcessor(9));
    activityManager.addProcessor(new FakeProcessor(8));

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("Hello");
    activityManager.processActivitiy(activity);
    //just verify that we run in priority order
    assertEquals("Hello-8-9-10", activity.getTitle());
  }


  class FakeProcessor extends BaseActivityProcessorPlugin {
    public FakeProcessor(int priority) {
      super(null);
      super.priority = priority;
    }

    @Override
    public void processActivity(ExoSocialActivity activity) {
      activity.setTitle(activity.getTitle() + "-" + priority);
    }
  }
*/
  
  private void createActivityHasMention(Identity poster, Identity mentionedUser) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("Hi @" + mentionedUser.getRemoteId());
    activity.setUserId(poster.getId());
    try {
      activityManager.saveActivityNoReturn(poster, activity);
      tearDownActivityList.add(activity);
    } catch (Exception e) {
      LOG.error("can not save activity.", e);
    }
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
  
  /**
   * Creates activity on user's stream.
   * 
   * @param user
   */
  private void createActivity(Identity user) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title " + System.currentTimeMillis());
    activity.setUserId(user.getId());
    try {
      activityManager.saveActivityNoReturn(user, activity);
      tearDownActivityList.add(activity);
    } catch (Exception e) {
      LOG.error("can not save activity.", e);
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
  private Space getSpaceInstance(SpaceService spaceService, int number)
      throws Exception {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.OPEN);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId(SpaceUtils.SPACE_GROUP + "/" + space.getPrettyName());
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] { "demo", "john" };
    String[] members = new String[] { "raul", "ghost" };
    String[] invitedUsers = new String[] { "mary", "paul"};
    String[] pendingUsers = new String[] { "jame"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.saveSpace(space, true);
    return space;
  }
}
