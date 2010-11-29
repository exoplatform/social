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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Test for {@link ActivityManager}, including cache tests.
 * @author hoat_le
 */
public class ActivityManagerTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(ActivityManagerTest.class);
  private List<ExoSocialActivity> tearDownActivityList;
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  private IdentityManager identityManager;
  private ActivityManager activityManager;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull("identityManager must not be null", identityManager);
    activityManager =  (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull("activityManager must not be null", activityManager);
    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");

    assertNotNull("rootIdentity.getId() must not be null", rootIdentity.getId());
    assertNotNull("johnIdentity.getId() must not be null", johnIdentity.getId());
    assertNotNull("maryIdentity.getId() must not be null", maryIdentity.getId());
    assertNotNull("demoIdentity.getId() must not be null", demoIdentity.getId());

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
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    super.tearDown();
  }

  /**
   * Test for {@link ActivityManager#saveActivity(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   *
   * and {@link ActivityManager#saveActivity(Identity, org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   */
  public void testSaveActivity() throws ActivityStorageException {
    //save mal-formed activity
    {
      ExoSocialActivity malformedActivity = new ExoSocialActivityImpl();;
      malformedActivity.setTitle("malform");
      try {
        activityManager.saveActivity(malformedActivity);
      } catch (IllegalArgumentException e) {
        LOG.info("test with malfomred activity passes.");
      }
    }

    {
      final String activityTitle = "root activity";
      ExoSocialActivity rootActivity = new ExoSocialActivityImpl();;
      rootActivity.setTitle(activityTitle);
      rootActivity.setUserId(rootIdentity.getId());
      activityManager.saveActivity(rootActivity);

      assertNotNull("rootActivity.getId() must not be null", rootActivity.getId());

      //updates
      rootActivity.setTitle("Hello World");
      activityManager.saveActivity(rootActivity);

      tearDownActivityList.add(rootActivity);
    }

    {
      final String title = "john activity";
      ExoSocialActivity johnActivity = new ExoSocialActivityImpl();;
      johnActivity.setTitle(title);
      activityManager.saveActivity(johnIdentity, johnActivity);

      tearDownActivityList.add(johnActivity);

      assertNotNull("johnActivity.getId() must not be null", johnActivity.getId());
    }

  }


  /**
   * Test {@link ActivityManager#getActivity(String)}
   */
  public void testGetActivity() throws ActivityStorageException {
      List<ExoSocialActivity> rootActivities = activityManager.getActivities(rootIdentity);
      assertEquals("user's activities should have 0 element.", 0, rootActivities.size());

      ExoSocialActivity activity = new ExoSocialActivityImpl();;
      activity.setTitle("title");
      activity.setUserId(rootIdentity.getId());

      activityManager.saveActivity(rootIdentity, activity);

      rootActivities = activityManager.getActivities(rootIdentity);
      assertEquals("user's activities should have 1 element", 1, rootActivities.size());

      tearDownActivityList.addAll(rootActivities);
  }

  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#deleteActivity(String)}
   * {@link ActivityManager#deleteActivity(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   */
  public void testDeleteActivity() {
    assert true;
  }

  public  void testGetCommentWithHtmlContent() throws ActivityStorageException {
    String htmlString = "<span><strong>foo</strong>bar<script>zed</script></span>";
    String htmlRemovedString = "<span><strong>foo</strong>bar&lt;script&gt;zed&lt;/script&gt;</span>";
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();;
    activity.setTitle("blah blah");
    activityManager.saveActivity(rootIdentity, activity);

    ExoSocialActivity comment = new ExoSocialActivityImpl();;
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
  
  public  void testGetComment() throws ActivityStorageException {
    ExoSocialActivity activity = new ExoSocialActivityImpl();;
    activity.setTitle("blah blah");
    activityManager.saveActivity(rootIdentity, activity);

    ExoSocialActivity comment = new ExoSocialActivityImpl();;
    comment.setTitle("comment blah blah");
    comment.setUserId(rootIdentity.getId());

    activityManager.saveComment(activity, comment);

    assertNotNull("comment.getId() must not be null", comment.getId());

    String[] commentsId = activity.getReplyToId().split(",");
    assertEquals(comment.getId(), commentsId[1]);
    tearDownActivityList.add(activity);
  }

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
    String rawCommentIds = assertActivity.getReplyToId();
    String[] commentIds = rawCommentIds.split(",");
    for (int i = 1; i < commentIds.length; i++) {
      assertEquals(comments.get(i - 1).getId(), commentIds[i]);
    }
    tearDownActivityList.add(activity);
  }
  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#deleteComment(String, String)}
   */
  public void testDeleteComment() throws ActivityStorageException {
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
  */
 public void testGetActivities() throws ActivityStorageException {
   populateActivityMass(rootIdentity, 30);
   List<ExoSocialActivity> activities = activityManager.getActivities(rootIdentity);
   assertNotNull("activities must not be null", activities);
   assertEquals(20, activities.size());

   List<ExoSocialActivity> allActivities = activityManager.getActivities(rootIdentity, 0, 30);

   assertEquals(30, allActivities.size());

   tearDownActivityList.addAll(allActivities);
 }

 /**
  * Unit Test for:
  * <p>
  * {@link ActivityManager#getActivitiesOfConnections(Identity)}
  */
 public void testGetActivitiesOfConnections() {
   assert true;
 }

 /**
  * Unit Test for:
  * <p>
  * {@link ActivityManager#getActivitiesOfUserSpaces(Identity)}
  */
 public void testGetActivitiesOfUserSpaces() {
   assert true;
 }

  /**
   *
   */
  public void testGetActivitiesByPagingWithoutCreatingComments() throws ActivityStorageException {
    final int totalActivityCount = 9;
    final int retrievedCount = 7;

    for (int i = 0; i < totalActivityCount; i++) {
      //root save on john's stream
      ExoSocialActivity activity = new ExoSocialActivityImpl();;
      activity.setTitle("blabla");
      activity.setUserId(rootIdentity.getId());

      activityManager.saveActivity(johnIdentity, activity);
    }

    List<ExoSocialActivity> activities = activityManager.getActivities(johnIdentity, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());

    tearDownActivityList.addAll(activityManager.getActivities(johnIdentity, 0, totalActivityCount));
  }

  /**
   *
   */
  public void testAddProviders() {
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

  private void populateActivityMass(Identity user, int number) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();;
      activity.setTitle("title " + i);
      activity.setUserId(user.getId());
      try {
        activityManager.saveActivity(user, activity);
      } catch (Exception e) {
        LOG.error("can not save activity.", e);
      }
    }
  }
}