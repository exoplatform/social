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
* along with this program; if not, see<http://www.gnu.org/licenses/>.
*/
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Test for {@link ActivityStorage}
 *
 * @author hoat_le
 *
 */
public class ActivityStorageTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(ActivityStorageTest.class);
  private IdentityManager identityManager;
  private ActivityStorage activityStorage;
  private List<ExoSocialActivity> tearDownActivityList;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityStorage = (ActivityStorage) getContainer().getComponentInstanceOfType(ActivityStorage.class);
    assertNotNull("identityManager must not be null", identityManager);
    assertNotNull("activityStorage must not be null", activityStorage);
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");

    assertNotNull("rootIdentity.getId() must not be null", rootIdentity.getId());
    assertNotNull("johnIdentity.getId() must not be null", johnIdentity.getId());
    assertNotNull("maryIdentity.getId() must not be null", maryIdentity.getId());
    assertNotNull("demoIdentity.getId() must not be null", demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
  }

  @Override
  protected void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      activityStorage.deleteActivity(activity);
    }
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    assertEquals("assertEquals(activityStorage.getActivities(rootIdentity).size() must be 0",
           0, activityStorage.getActivities(rootIdentity).size());
    assertEquals("assertEquals(activityStorage.getActivities(johnIdentity).size() must be 0",
           0, activityStorage.getActivities(johnIdentity).size());
    assertEquals("assertEquals(activityStorage.getActivities(maryIdentity).size() must be 0",
           0, activityStorage.getActivities(maryIdentity).size());
    assertEquals("assertEquals(activityStorage.getActivities(demoIdentity).size() must be 0",
           0, activityStorage.getActivities(demoIdentity).size());
    super.tearDown();
  }


  /**
   * Test {@link ActivityStorage#saveActivity(Identity, org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   */
  public void testSaveActivity() throws ActivityStorageException {
    final String activityTitle = "activity Title";
    //test wrong
    {
      ExoSocialActivity wrongActivity = new ExoSocialActivityImpl();
      try {
        activityStorage.saveActivity(demoIdentity, null);
        activityStorage.saveActivity(null, wrongActivity);
      } catch (ActivityStorageException e) {
        LOG.info("wrong argument tests passed.");
      }
    }
    //test with only mandatory fields
    {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle);
      activityStorage.saveActivity(rootIdentity, activity);

      assertNotNull("activity.getId() must not be null", activity.getId());

      //Update
      activity.setTitle("New Title");
      activity.setUpdated(new Date());

      activityStorage.saveActivity(rootIdentity, activity);

      tearDownActivityList.add(activity);

      //FIXME hoatle: BUG here when updating an activity but not the same as origin stream owner.
      //activityStorage.saveActivity(johnIdentity, activity);


      ExoSocialActivity johnActivity = new ExoSocialActivityImpl();
      johnActivity.setTitle(activityTitle);
      activityStorage.saveActivity(johnIdentity, johnActivity);
      assertNotNull("johnActivity.getId() must not be null", johnActivity.getId());

      tearDownActivityList.add(johnActivity);
    }
    //Test with full fields.
    {

    }

    //Test mail-formed activityId
    {

    }

  }

  /**
   * Test {@link ActivityStorage#deleteActivity(String)}
   * and {@link ActivityStorage#deleteActivity(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   */
  public void testDeleteActivity() throws ActivityStorageException {
    final String activityTitle = "activity Title";

    //Test deleteActivity(String)
    {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle);
      activityStorage.saveActivity(maryIdentity, activity);

      assertNotNull("activity.getId() must not be null", activity.getId());

      activityStorage.deleteActivity(activity.getId());
      boolean exceptionThrown = false;
      try {
        activityStorage.getActivity(activity.getId());
      } catch (ActivityStorageException ase) {
        exceptionThrown = true;
      }
      assertTrue("exceptionThrows must be true", exceptionThrown);
    }
    //Test deleteActivity(Activity)
    {
      ExoSocialActivity activity2 = new ExoSocialActivityImpl();
      activity2.setTitle(activityTitle);
      activityStorage.saveActivity(demoIdentity, activity2);

      assertNotNull("activity2.getId() must not be null", activity2.getId());
      activityStorage.deleteActivity(activity2);
    }

  }

  /**
   * Test {@link ActivityStorage#saveComment(org.exoplatform.social.core.activity.model.ExoSocialActivity , org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   */
  public void testSaveComment() throws ActivityStorageException {

    //comment on his own activity
    {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("blah blah");
      activityStorage.saveActivity(rootIdentity, activity);

      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment blah");
      comment.setUserId(rootIdentity.getId());

      activityStorage.saveComment(activity, comment);

      tearDownActivityList.add(activity);
    }

    // comment on other users' activity
    {

    }

  }

  /**
   * Test {@link ActivityStorage#deleteComment(String, String)}
   */
  public void testDeleteComment() throws ActivityStorageException {

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("blah blah");
    activityStorage.saveActivity(rootIdentity, activity);

    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("coment blah blah");
    comment.setUserId(rootIdentity.getId());

    activityStorage.saveComment(activity, comment);

    assertNotNull("comment.getId() must not be null", comment.getId());

    activityStorage.deleteComment(activity.getId(), comment.getId());

    tearDownActivityList.add(activity);
  }

  /**
   * Test {@link ActivityStorage#getActivity(String)}
   */
  public void testGetActivity() throws ActivityStorageException {
    final String activityTitle = "activity title";
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);

    assertNotNull("activity.getId() must not be null", activity.getId());

    assertEquals("demoIdentity.getRemoteId() must return: " + demoIdentity.getRemoteId(), demoIdentity.getRemoteId(), activity.getStreamOwner());

    ExoSocialActivity gotActivity = activityStorage.getActivity(activity.getId());

    assertNotNull("gotActivity.getId() must not be null", gotActivity.getId());

    assertEquals("activity.getId() must return: " + activity.getId(), activity.getId(), gotActivity.getId());

    assertEquals("gotActivity.getTitle() must return: " + gotActivity.getTitle(), activityTitle, gotActivity.getTitle());

    ActivityStream activityStream = activity.getActivityStream();
    assertNotNull("activityStream.getId() must not be null", activityStream.getId());
    assertEquals("activityStream.getPrettyId() must return: " + demoIdentity.getRemoteId(), demoIdentity.getRemoteId(), activityStream.getPrettyId());
    assertEquals(ActivityStream.Type.USER, activityStream.getType());
    assertNotNull("activityStream.getPermaLink() must not be null", activityStream.getPermaLink());

  }

  /**
   * Test {@link ActivityStorage#getActivities(Identity, long, long)}
   *
   * and {@link ActivityStorage#getActivities(Identity)}
   *
   */
  public void testGetActivities() throws ActivityStorageException {
    final int totalNumber = 20;
    final String activityTitle = "activity title";
    //John posts activity to root's activity stream
    for (int i = 0; i < totalNumber; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle + i);
      activity.setUserId(johnIdentity.getId());

      activityStorage.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
    }

    //Till now Root's activity stream has 20 activities posted by John
    assertEquals("John must have zero activity", 0, activityStorage.getActivities(johnIdentity).size());
    assertEquals("Root must have " + totalNumber + " activities", totalNumber, activityStorage.getActivities(rootIdentity).size());

    //Root posts activities to his stream
    for (int i = 0; i < totalNumber; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle + i);
      activity.setUserId(rootIdentity.getId());
      activityStorage.saveActivity(rootIdentity, activity);

      //John comments on Root's activity
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("Comment " + i);
      comment.setUserId(johnIdentity.getId());
      activityStorage.saveComment(activity, comment);
      tearDownActivityList.add(activity);
    }
    //Till now Root's activity stream has 40 activities: 20 posted by John and 20 posted by Root
    //, each of those activities posted by Root has 1 comment by John.
    assertEquals("John must have zero activity", 0, activityStorage.getActivities(johnIdentity).size());
    assertEquals("Root must have " + totalNumber*2 + " activities", totalNumber*2, activityStorage.getActivities(rootIdentity).size());


    // Test ActivityStorage#getActivities(Identity, long, long)
    {
      List<ExoSocialActivity> gotJohnActivityList = activityStorage.getActivities(johnIdentity, 0, 50);
      assertEquals("gotJohnActivityList.size() should return 0", 0, gotJohnActivityList.size());

      final int limit = 34;
      assertTrue("root's activities should be greater than " + limit + " for passing test below", activityStorage.getActivities(rootIdentity).size() > limit);
      List<ExoSocialActivity> gotRootActivityList = activityStorage.getActivities(rootIdentity, 0, limit);
      assertEquals("gotRootActivityList.size() must return " + limit, limit, gotRootActivityList.size());
    }

  }

  /**
   * Test {@link ActivityStorage#getActivitiesCount(Identity)}
   */
  public void testGetActivitiesCount() throws ActivityStorageException {

    final int totalNumber = 20;
    //create 20 activities each for root, john, mary, demo.
    for (int i = 0; i < totalNumber; i++) {
      ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
      rootActivity.setTitle("Root activity" + i);
      activityStorage.saveActivity(rootIdentity, rootActivity);

      tearDownActivityList.add(rootActivity);

      ExoSocialActivity johnActivity = new ExoSocialActivityImpl();
      johnActivity.setTitle("John activity" + i);
      activityStorage.saveActivity(johnIdentity, johnActivity);

      tearDownActivityList.add(johnActivity);

      ExoSocialActivity maryActivity = new ExoSocialActivityImpl();
      maryActivity.setTitle("Mary activity" + i);
      activityStorage.saveActivity(maryIdentity, maryActivity);

      tearDownActivityList.add(maryActivity);

      ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
      demoActivity.setTitle("Demo activity" + i);
      activityStorage.saveActivity(demoIdentity, demoActivity);

      tearDownActivityList.add(demoActivity);

      //John comments demo's activities
      ExoSocialActivity johnComment = new ExoSocialActivityImpl();
      johnComment.setTitle("John's comment " + i);
      johnComment.setUserId(johnIdentity.getId());
      activityStorage.saveComment(demoActivity, johnComment);

    }

    assertEquals("activityStorage.getActivitiesCount(rootIdentity) must return " + totalNumber, totalNumber, activityStorage.getActivitiesCount(rootIdentity));
    assertEquals("activityStorage.getActivitiesCount(johnIdentity) must return " + totalNumber, totalNumber, activityStorage.getActivitiesCount(johnIdentity));
    assertEquals("activityStorage.getActivitiesCount(maryIdentity) must return " + totalNumber, totalNumber, activityStorage.getActivitiesCount(maryIdentity));
    assertEquals("activityStorage.getActivitiesCount(demoIdentity) must return " + totalNumber, totalNumber, activityStorage.getActivitiesCount(demoIdentity));

  }


  /**
   *
   */
  public void testGetStreamInfo() throws ActivityStorageException {

    // root save on root's stream
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("blabla");
    activity.setUpdated(new Date());
    activity.setUserId(demoIdentity.getId());
    activityStorage.saveActivity(demoIdentity, activity);

    String streamId = activity.getStreamId();
    assertNotNull("streamId must not be null", streamId);
    assertEquals(activity.getStreamOwner(), demoIdentity.getRemoteId());

    ActivityStream activityStream = activity.getActivityStream();

    assertEquals("activityStream.getId() must return: " + streamId, streamId, activityStream.getId());

    assertEquals("activityStream.getPrettyId() must return: " + demoIdentity.getRemoteId(), demoIdentity.getRemoteId(), activityStream.getPrettyId());

    //assertNotNull(activityStream.getPermaLink());

    List<ExoSocialActivity> activities = activityStorage.getActivities(demoIdentity);
    assertEquals(1, activities.size());
    assertEquals(demoIdentity.getRemoteId(), activities.get(0).getStreamOwner());
    assertEquals(streamId, activities.get(0).getStreamId());

    ExoSocialActivity loaded = activityStorage.getActivity(activity.getId());
    assertEquals(demoIdentity.getRemoteId(), loaded.getStreamOwner());
    assertEquals(streamId, loaded.getStreamId());

    tearDownActivityList.add(activity);
  }


  /**
   *
   */
  public void testGetActivitiesByPagingWithoutCreatingComments() throws ActivityStorageException {
    final int totalActivityCount = 9;
    final int retrievedCount = 7;

    for (int i = 0; i < totalActivityCount; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("blabla");
      activityStorage.saveActivity(demoIdentity, activity);
      tearDownActivityList.add(activity);
    }

    List<ExoSocialActivity> activities = activityStorage.getActivities(demoIdentity, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());

  }

  /**
   */
  public void testGetActivitiesByPagingWithCreatingComments() throws ActivityStorageException {
    final int totalActivityCount = 2;
    final int retrievedCount = 1;

    for (int i = 0; i < totalActivityCount; i++) {
      // root save on john's stream
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("blabla");
      activity.setUserId(johnIdentity.getId());

      activityStorage.saveActivity(johnIdentity, activity);

      //for teardown cleanup
      tearDownActivityList.add(activity);

      //test activity has been created
      String streamId = activity.getStreamId();
      assertNotNull(streamId);
      assertEquals(activity.getStreamOwner(), johnIdentity.getRemoteId());

      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("this is comment " + i);
      comment.setUserId(johnIdentity.getId());
      activityStorage.saveComment(activity, comment);

    }

    List<ExoSocialActivity> activities = activityStorage.getActivities(johnIdentity, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());

  }

  /**
   */
  public void testTemplateParams() throws ActivityStorageException {
    final String URL_PARAMS = "URL";
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("blabla");
    activity.setUserId("root");
    activity.setUpdated(new Date());

    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put(URL_PARAMS, "http://xxxxxxxxxxxxxxxx/xxxx=xxxxx");
    activity.setTemplateParams(templateParams);

    activityStorage.saveActivity(rootIdentity, activity);

    tearDownActivityList.add(activity);

    activity = activityStorage.getActivities(rootIdentity).get(0);
    assertNotNull("activity must not be null", activity);
    assertNotNull("activity.getTemplateParams() must not be null", activity.getTemplateParams());
    assertEquals("http://xxxxxxxxxxxxxxxx/xxxx=xxxxx", activity.getTemplateParams().get(URL_PARAMS));
  }
}
