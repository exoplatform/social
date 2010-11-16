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
import org.exoplatform.social.core.activity.model.Activity;
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
  private List<Activity> tearDownActivityList;

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

    tearDownActivityList = new ArrayList<Activity>();
  }

  @Override
  protected void tearDown() throws Exception {
    for (Activity activity : tearDownActivityList) {
      activityStorage.deleteActivity(activity);
    }
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    super.tearDown();
  }


  /**
   * Test {@link ActivityStorage#saveActivity(Identity, Activity)}
   */
  public void testSaveActivity() {
    final String activityTitle = "activity Title";
    //test wrong
    {
      Activity wrongActivity = new Activity();
      try {
        activityStorage.saveActivity(demoIdentity, null);
        activityStorage.saveActivity(null, wrongActivity);
      } catch (IllegalArgumentException e) {
        LOG.info("wrong argument tests passed.");
      }
    }
    //test with only mandatory fields
    {
      Activity activity = new Activity();
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


      Activity johnActivity = new Activity();
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
   * Test {@link ActivityStorage#save(Identity, Activity)}
   * @author vien_levan
   */
  @SuppressWarnings("deprecation")
  public void testSave() {
    //test for root
    Activity saveActivity = new Activity();
    saveActivity.setTitle("save activity");
    activityStorage.save(rootIdentity, saveActivity);
    tearDownActivityList.add(saveActivity);

    assertNotNull("saveActivity.getId() must not be null", saveActivity.getId());

    //test for normal user
    Activity normalActivity = new Activity();
    normalActivity.setTitle("normal activity");
    activityStorage.save(johnIdentity, normalActivity);
    tearDownActivityList.add(normalActivity);

    assertNotNull("normalActivity.getId() must not null", normalActivity.getId());
  }

  /**
   * Test {@link ActivityStorage#deleteActivity(String)}
   * and {@link ActivityStorage#deleteActivity(Activity)}
   */
  public void testDeleteActivity() {
    final String activityTitle = "activity Title";

    //Test deleteActivity(String)
    {
      Activity activity = new Activity();
      activity.setTitle(activityTitle);
      activityStorage.saveActivity(maryIdentity, activity);

      assertNotNull("activity.getId() must not be null", activity.getId());

      activityStorage.deleteActivity(activity.getId());

      assertNull("activityStorage.getActivity(activity.getId()) must return null", activityStorage.getActivity(activity.getId()));
    }
    //Test deleteActivity(Activity)
    {
      Activity activity2 = new Activity();
      activity2.setTitle(activityTitle);
      activityStorage.saveActivity(demoIdentity, activity2);

      assertNotNull("activity2.getId() must not be null", activity2.getId());
      activityStorage.deleteActivity(activity2);
    }

  }

  /**
   * Test {@link ActivityStorage#load(String)}}
   * @author vien_levan
   */
  @SuppressWarnings("deprecation")
  public void testLoad() {
    //test for user root
    Activity loadActivity = new Activity();
    loadActivity.setTitle("load activity");
    activityStorage.saveActivity(rootIdentity, loadActivity);
    tearDownActivityList.add(loadActivity);
    assertNotNull("loadActivity.getId() must not be null", loadActivity.getId());

    //load
    Activity loadedActivity = activityStorage.load(loadActivity.getId());
    assertNotNull("loadedActivity must not be null", loadedActivity);
    assertNotNull("loadedActivity.getId() must not be null", loadedActivity.getId());
    assertEquals("loadActivity.getId() must return: " + loadActivity.getId(), loadActivity.getId(), loadedActivity.getId());
    assertEquals("loadedActivity.getStreamOwner() must return: " + loadedActivity.getStreamOwner(), loadedActivity.getStreamOwner(), loadActivity.getStreamOwner());
    assertEquals("loadActivity.getTitle() must return: " + loadActivity.getTitle(), loadActivity.getTitle(), loadedActivity.getTitle());

    //test for normal user
    Activity normalActivity = new Activity();
    normalActivity.setTitle("normal activity");
    activityStorage.saveActivity(johnIdentity, normalActivity);
    tearDownActivityList.add(normalActivity);
    assertNotNull("normalActivity.getId() must not be null", normalActivity.getId());

    //load
    Activity normalLoadedActivity = activityStorage.load(normalActivity.getId());
    assertNotNull("normalLoadedActivity must not be null", normalLoadedActivity);
    assertNotNull("normalLoadedActivity.getId() must not be null", normalLoadedActivity.getId());
    assertEquals("normalActivity.getTitle() must return: " + normalActivity.getTitle(), normalActivity.getTitle(), normalLoadedActivity.getTitle());
    assertEquals("normalActivity.getId() must return: " + normalActivity.getId(), normalActivity.getId(), normalLoadedActivity.getId());
    assertEquals("normalActivity.getStreamOwner() must return: " + normalActivity.getStreamOwner(), normalActivity.getStreamOwner(), normalLoadedActivity.getStreamOwner());
  }

  /**
   * Test {@link ActivityStorage#saveComment(Activity, Activity)}
   */
  public void testSaveComment() {

    //comment on his own activity
    {
      Activity activity = new Activity();
      activity.setTitle("blah blah");
      activityStorage.saveActivity(rootIdentity, activity);

      Activity comment = new Activity();
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
  public void testDeleteComment() {

    Activity activity = new Activity();
    activity.setTitle("blah blah");
    activityStorage.saveActivity(rootIdentity, activity);

    Activity comment = new Activity();
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
  public void testGetActivity() {
    final String activityTitle = "activity title";
    Activity activity = new Activity();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);

    assertNotNull("activity.getId() must not be null", activity.getId());

    assertEquals("demoIdentity.getRemoteId() must return: " + demoIdentity.getRemoteId(), demoIdentity.getRemoteId(), activity.getStreamOwner());

    Activity gotActivity = activityStorage.getActivity(activity.getId());

    assertNotNull("gotActivity.getId() must not be null", gotActivity.getId());

    assertEquals("activity.getId() must return: " + activity.getId(), activity.getId(), gotActivity.getId());

    assertEquals("gotActivity.getTitle() must return: " + gotActivity.getTitle(), activityTitle, gotActivity.getTitle());
  }

  /**
   * Test {@link ActivityStorage#getActivities(Identity, long, long)}
   *
   * and {@link ActivityStorage#getActivities(Identity)}
   *
   */
  public void testGetActivities() {
    final int totalNumber = 20;
    final String activityTitle = "activity title";
    //John posts activity to root's activity stream
    for (int i = 0; i < totalNumber; i++) {
      Activity activity = new Activity();
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
      Activity activity = new Activity();
      activity.setTitle(activityTitle + i);
      activity.setUserId(rootIdentity.getId());
      activityStorage.saveActivity(rootIdentity, activity);

      //John comments on Root's activity
      Activity comment = new Activity();
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
      List<Activity> gotJohnActivityList = activityStorage.getActivities(johnIdentity, 0, 50);
      assertEquals("gotJohnActivityList.size() should return 0", 0, gotJohnActivityList.size());

      final int limit = 34;
      assertTrue("root's activities should be greater than " + limit + " for passing test below", activityStorage.getActivities(rootIdentity).size() > limit);
      List<Activity> gotRootActivityList = activityStorage.getActivities(rootIdentity, 0, limit);
      assertEquals("gotRootActivityList.size() must return " + limit, limit, gotRootActivityList.size());
    }

  }

  /**
   * Test {@link ActivityStorage#getActivitiesCount(Identity)}
   */
  public void testGetActivitiesCount() {

    final int totalNumber = 20;
    //create 20 activities each for root, john, mary, demo.
    for (int i = 0; i < totalNumber; i++) {
      Activity rootActivity = new Activity();
      rootActivity.setTitle("Root activity" + i);
      activityStorage.saveActivity(rootIdentity, rootActivity);

      tearDownActivityList.add(rootActivity);

      Activity johnActivity = new Activity();
      johnActivity.setTitle("John activity" + i);
      activityStorage.saveActivity(johnIdentity, johnActivity);

      tearDownActivityList.add(johnActivity);

      Activity maryActivity = new Activity();
      maryActivity.setTitle("Mary activity" + i);
      activityStorage.saveActivity(maryIdentity, maryActivity);

      tearDownActivityList.add(maryActivity);

      Activity demoActivity = new Activity();
      demoActivity.setTitle("Demo activity" + i);
      activityStorage.saveActivity(demoIdentity, demoActivity);

      tearDownActivityList.add(demoActivity);

      //John comments demo's activities
      Activity johnComment = new Activity();
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
  public void testGetStreamInfo() {

    // root save on root's stream
    Activity activity = new Activity();
    activity.setTitle("blabla");
    activity.setUpdated(new Date());
    activityStorage.saveActivity(demoIdentity, activity);

    String streamId = activity.getStreamId();
    assertNotNull(streamId);
    assertEquals(activity.getStreamOwner(), demoIdentity.getRemoteId());

    List<Activity> activities = activityStorage.getActivities(demoIdentity);
    assertEquals(1, activities.size());
    assertEquals(demoIdentity.getRemoteId(), activities.get(0).getStreamOwner());
    assertEquals(streamId, activities.get(0).getStreamId());

    Activity loaded = activityStorage.getActivity(activity.getId());
    assertEquals(demoIdentity.getRemoteId(), loaded.getStreamOwner());
    assertEquals(streamId, loaded.getStreamId());

    tearDownActivityList.add(activity);
  }


  /**
   *
   */
  public void testGetActivitiesByPagingWithoutCreatingComments() {
    final int totalActivityCount = 9;
    final int retrievedCount = 7;

    for (int i = 0; i < totalActivityCount; i++) {
      Activity activity = new Activity();
      activity.setTitle("blabla");
      activityStorage.saveActivity(demoIdentity, activity);
      tearDownActivityList.add(activity);
    }

    List<Activity> activities = activityStorage.getActivities(demoIdentity, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());

  }

  /**
   */
  public void testGetActivitiesByPagingWithCreatingComments() {
    final int totalActivityCount = 2;
    final int retrievedCount = 1;

    for (int i = 0; i < totalActivityCount; i++) {
      // root save on john's stream
      Activity activity = new Activity();
      activity.setTitle("blabla");
      activity.setUserId(johnIdentity.getId());

      activityStorage.saveActivity(johnIdentity, activity);

      //for teardown cleanup
      tearDownActivityList.add(activity);

      //test activity has been created
      String streamId = activity.getStreamId();
      assertNotNull(streamId);
      assertEquals(activity.getStreamOwner(), johnIdentity.getRemoteId());

      Activity comment = new Activity();
      comment.setTitle("this is comment " + i);
      comment.setUserId(johnIdentity.getId());
      activityStorage.saveComment(activity, comment);

    }

    List<Activity> activities = activityStorage.getActivities(johnIdentity, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());

  }

  /**
   */
  public void testTemplateParams() {
    final String URL_PARAMS = "URL";
    Activity activity = new Activity();
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
