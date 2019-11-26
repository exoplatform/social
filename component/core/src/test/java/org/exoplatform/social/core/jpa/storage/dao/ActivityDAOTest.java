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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.entity.ActivityEntity;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;
import org.exoplatform.social.core.space.model.Space;

import java.util.*;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 18, 2015  
 */
public class ActivityDAOTest extends BaseCoreTest {
  private final Log LOG = ExoLogger.getLogger(ActivityDAOTest.class);
  private Set<ActivityEntity> tearDownActivityList;
  private List<Space> tearDownSpaceList;
  private Identity ghostIdentity;
  private Identity raulIdentity;
  private Identity jameIdentity;
  private Identity paulIdentity;
  
  private ActivityDAO activityDao;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    activityDao = getService(ActivityDAO.class);
    //
    tearDownActivityList = new HashSet<ActivityEntity>();
    tearDownSpaceList = new ArrayList<Space>();
    //
    ghostIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "ghost", true);
    raulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "raul", true);
    jameIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "jame", true);
    paulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul", true);
  }

  @Override
  public void tearDown() throws Exception {
    for (ActivityEntity activity : tearDownActivityList) {
      try {
        activityDao.delete(activity);
      } catch (Exception e) {
        LOG.warn("Can not delete activity with id: " + activity.getId(), e);
      }
    }

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
    //
    // logout
    ConversationState.setCurrent(null);
    super.tearDown();
  }
  
  public void testSaveActivity() throws Exception {
    
    String activityTitle = "activity title";
    String johnIdentityId = johnIdentity.getId();
    ActivityEntity activity = createActivity(activityTitle, maryIdentity.getId());
    activity.setLocked(true);
    
    activity.setPosterId(johnIdentityId);
    activity.setOwnerId(johnIdentityId);
    
    activity = activityDao.create(activity);
    
    ActivityEntity got = activityDao.find(activity.getId());
    assertNotNull(got);
    assertEquals(activityTitle, got.getTitle());
    assertEquals(johnIdentityId, got.getPosterId());
    assertEquals(johnIdentityId, got.getOwnerId());
    //
    Map<String, String> gotTemplateParams = activity.getTemplateParams();
    for (int i = 1; i < 4; i++) {
      assertTrue(gotTemplateParams.values().contains("value " + 1));
    }
    //
    assertTrue(activity.getLocked());
    assertFalse(activity.getHidden());
    tearDownActivityList.add(got);
  }
  
  private ActivityEntity createActivity(String activityTitle, String posterId) {
    ActivityEntity activity = new ActivityEntity();
    // test for reserving order of map values for i18n activity
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("key1", "value 1");
    templateParams.put("key2", "value 2");
    templateParams.put("key3", "value 3");
    activity.setTemplateParams(templateParams);
    activity.setTitle(activityTitle);
    activity.setBody("The body of " + activityTitle);
    activity.setPosterId(posterId);
    activity.setType("DEFAULT_ACTIVITY");

    //
    activity.setHidden(false);
    activity.setLocked(false);
    //
    return activity;
  }
  
  
  private ActivityEntity saveActivity(Identity ownerIdentity, ActivityEntity activity) {
    activity.setOwnerId(ownerIdentity.getId());
    activity.setPosterId(activity.getOwnerId());
    activity = activityDao.create(activity);
    tearDownActivityList.add(activity);
    //
    return activity;
  }
  /**
   * Test {@link activityDao#updateActivity(ActivityEntity)}
   * 
   * @throws Exception
   */
  public void testUpdateActivity() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ActivityEntity activity = createActivity(activityTitle, userId);
    saveActivity(johnIdentity, activity);
    //
    activity = activityDao.find(activity.getId());
    assertEquals(activityTitle, activity.getTitle());
    assertEquals(userId, activity.getOwnerId());

    String newTitle = "new activity title";
    activity.setTitle(newTitle);
    activityDao.update(activity);

    activity = activityDao.find(activity.getId());
    assertEquals(newTitle, activity.getTitle());
  }

  /**
   * Unit Test for:
   * <p>
   * {@link activityDao#deleteActivity(org.exoplatform.social.core.activity.model.Activity)}
   * 
   * @throws Exception
   */
  public void testDeleteActivity() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ActivityEntity activity = new ActivityEntity();
    activity.setTitle(activityTitle);
    activity.setOwnerId(userId);
    activity.setPosterId(userId);
    activity = activityDao.create(activity);
    //
    activity = activityDao.find(activity.getId());
    
    assertNotNull(activity);
    assertEquals(activityTitle, activity.getTitle());
    assertEquals(userId, activity.getOwnerId());
    activityDao.delete(activity);
    //
    assertNull(activityDao.find(activity.getId()));
  }

  /**
   * Test {@link activityDao#saveComment(ActivityEntity, ActivityEntity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testSaveComment() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ActivityEntity activity = new ActivityEntity();
    activity.setTitle(activityTitle);
    activity.setOwnerId(userId);
    saveActivity(johnIdentity, activity);
    String commentTitle = "Comment title";
    
    //demo comments on john's activity
    ActivityEntity comment = new ActivityEntity();
    comment.setTitle(commentTitle);
    comment.setOwnerId(demoIdentity.getId());
    comment.setPosterId(demoIdentity.getId());
    activity.addComment(comment);
    activityDao.create(comment);
    
    assertNotNull(comment.getId());
    activityDao.update(activity);
    //
    activity = activityDao.find(activity.getId());
    
    List<ActivityEntity> demoComments = activityDao.getComments(activity.getId(), 0, -1);
    assertNotNull(demoComments);
    assertEquals(1, demoComments.size());
    
    comment = demoComments.get(0);
    assertEquals(commentTitle, comment.getTitle());
    assertEquals(demoIdentity.getId(), comment.getOwnerId());
  }

  /**
   * Tests {@link activityDao#getActivityByComment(ActivityEntity)}.
   */
  public void testGetActivityByComment() {
    String activityTitle = "activity title";
    String identityId = johnIdentity.getId();
    ActivityEntity demoActivity = new ActivityEntity();
    demoActivity.setTitle(activityTitle);
    demoActivity.setOwnerId(identityId);
    saveActivity(johnIdentity, demoActivity);
    // comment
    ActivityEntity comment = new ActivityEntity();
    comment.setTitle("demo comment");
    comment.setOwnerId(demoIdentity.getId());
    comment.setPosterId(demoIdentity.getId());
    //
    demoActivity = activityDao.find(demoActivity.getId());
    //
    demoActivity.addComment(comment);
    activityDao.create(comment);
    activityDao.update(demoActivity);
    //
    demoActivity = activityDao.find(demoActivity.getId());
    List<ActivityEntity> demoComments = demoActivity.getComments();
    Long commentId = demoComments.get(0).getId();
    //
    comment = activityDao.find(commentId);
    assertEquals(1, demoActivity.getComments().size());
    assertEquals(demoIdentity.getId(), comment.getOwnerId());
  }

  /**
   * Tests {@link activityDao#getActivityByComment(ActivityEntity)}.
   */
  public void testGetActivityByCommentId() throws Exception {
    String activityTitle = "activity title";
    String identityId = johnIdentity.getId();
    ActivityEntity demoActivity = new ActivityEntity();
    demoActivity.setTitle(activityTitle);
    demoActivity.setOwnerId(identityId);
    saveActivity(johnIdentity, demoActivity);
    // comment
    ActivityEntity comment = new ActivityEntity();
    comment.setTitle("demo comment");
    comment.setOwnerId(demoIdentity.getId());
    comment.setPosterId(demoIdentity.getId());
    //
    demoActivity.addComment(comment);
    comment = activityDao.create(comment);
    activityDao.update(demoActivity);
    //
    ActivityEntity activityAdded = activityDao.getParentActivity(comment.getId());
    assertEquals(demoActivity.getId(), activityAdded.getId());
    //
    final Long acId = demoActivity.getId(), cmId = comment.getId();
    executeAsync(new Runnable() {
      @Override
      public void run() {
        ActivityEntity activityAdded = activityDao.getParentActivity(cmId);
        assertNotNull(activityAdded);
        assertEquals(acId, activityAdded.getId());
      }
    });
  }

  /**
   * Test {@link activityDao#getComments(ActivityEntity)}
   * 
   * @throws Exception
   */
  public void testGetComments() throws Exception {
    ActivityEntity demoActivity = new ActivityEntity();
    demoActivity.setTitle("demo activity");
    demoActivity.setOwnerId(demoIdentity.getId());
    saveActivity(johnIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);
    
    int total = 10;
    
    for (int i = 0; i < total; i ++) {
      ActivityEntity maryComment = new ActivityEntity();
      maryComment.setOwnerId(maryIdentity.getId());
      maryComment.setPosterId(maryIdentity.getId());
      maryComment.setTitle("mary comment");
      demoActivity.addComment(maryComment);
      activityDao.create(maryComment);
      activityDao.update(demoActivity);
    }
    
    demoActivity = activityDao.find(demoActivity.getId());
    List<ActivityEntity> maryComments = demoActivity.getComments();
    assertNotNull(maryComments);
    assertEquals(total, maryComments.size());
  }

  
  /**
   * Test {@link activityDao#deleteComment(ActivityEntity, ActivityEntity)}
   * 
   * @throws Exception
   * @since 4.3.x
   */
  public void testDeleteComment() throws Exception {
    ActivityEntity demoActivity = new ActivityEntity();
    demoActivity.setTitle("demo activity");
    demoActivity.setOwnerId(demoIdentity.getId());
    saveActivity(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);
    
    ActivityEntity maryComment = new ActivityEntity();
    maryComment.setTitle("mary comment");
    maryComment.setOwnerId(maryIdentity.getId());
    maryComment.setPosterId(maryIdentity.getId());
    demoActivity.addComment(maryComment);
    activityDao.create(maryComment);
    activityDao.update(demoActivity);
    //
    maryComment = activityDao.find(maryComment.getId());
    activityDao.delete(maryComment);
    demoActivity = activityDao.find(demoActivity.getId());
    demoActivity.getComments().remove(maryComment);
    activityDao.update(demoActivity);
    //
    assertNull(activityDao.find(maryComment.getId()));
    assertEquals(0, activityDao.find(demoActivity.getId()).getComments().size());
  }
}
