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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Test for {@link ActivityManager}, including cache tests.
 * @author hoat_le
 */
public class ActivityManagerTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(ActivityManagerTest.class);
  private List<Activity> tearDownActivityList;
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
    tearDownActivityList = new ArrayList<Activity>();
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
    for (Activity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    if (demoIdentity.getId() != null) {
      //FIXME hoatle this can be affected by other tests
      identityManager.deleteIdentity(demoIdentity);
    }
    super.tearDown();
  }

  /**
   * Test for {@link ActivityManager#saveActivity(Activity)}
   *
   * and {@link ActivityManager#saveActivity(Identity, Activity)}
   */
  public void testSaveActivity() {
    //save mal-formed activity
    {
      Activity malformedActivity = new Activity();
      malformedActivity.setTitle("malform");
      try {
        activityManager.saveActivity(malformedActivity);
      } catch (IllegalArgumentException e) {
        LOG.info("test with malfomred activity passes.");
      }
    }

    {
      final String activityTitle = "root activity";
      Activity rootActivity = new Activity();
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
      Activity johnActivity = new Activity();
      johnActivity.setTitle(title);
      activityManager.saveActivity(johnIdentity, johnActivity);

      tearDownActivityList.add(johnActivity);

      assertNotNull("johnActivity.getId() must not be null", johnActivity.getId());
    }

  }


  /**
   * Test {@link ActivityManager#getActivity(String)}
   */
  public void testGetActivity() {
      List<Activity> rootActivities = activityManager.getActivities(rootIdentity);
      assertEquals("user's activities should have 0 element.", 0, rootActivities.size());

      Activity activity = new Activity();
      activity.setTitle("title");
      activity.setUserId(rootIdentity.getId());

      activityManager.saveActivity(rootIdentity, activity);

      rootActivities = activityManager.getActivities(rootIdentity);
      assertEquals("user's activities should have 1 element", 1, rootActivities.size());

      tearDownActivityList.addAll(rootActivities);
  }


  /**
   *
   */
  public void testGetActivitiesByPagingWithoutCreatingComments() {
    final int totalActivityCount = 9;
    final int retrievedCount = 7;

    for (int i = 0; i < totalActivityCount; i++) {
      //root save on john's stream
      Activity activity = new Activity();
      activity.setTitle("blabla");
      activity.setUserId(rootIdentity.getId());

      activityManager.saveActivity(johnIdentity, activity);
    }

    List<Activity> activities = activityManager.getActivities(johnIdentity, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());

    tearDownActivityList.addAll(activities);
  }

  /**
   *
   */
  public void testGetActivities() {
    populateActivityMass(rootIdentity, 30);
    List<Activity> activities = activityManager.getActivities(rootIdentity);
    assertNotNull("activities must not be null", activities);
    assertEquals(20, activities.size());

    List<Activity> allActivities = activityManager.getActivities(rootIdentity, 0, 30);

    assertEquals(30, allActivities.size());

    tearDownActivityList.addAll(allActivities);
  }

  /**
   *
   */
  public void testAddProviders() {
    activityManager.addProcessor(new FakeProcessor(10));
    activityManager.addProcessor(new FakeProcessor(2));
    activityManager.addProcessor(new FakeProcessor(1));

    Activity activity = new Activity();
    activity.setTitle("Hello");
    activityManager.processActivitiy(activity);
    //just verify that we run in priority order
    assertEquals("Hello-1-2-10", activity.getTitle());
  }


  class FakeProcessor extends BaseActivityProcessorPlugin {
    public FakeProcessor(int priority) {
      super(null);
      super.priority = priority;
    }

    public void processActivity(Activity activity) {
      activity.setTitle(activity.getTitle() + "-" + priority);
    }
  }

  private void populateActivityMass(Identity user, int number) {
    for (int i = 0; i < number; i++) {
      Activity activity = new Activity();
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