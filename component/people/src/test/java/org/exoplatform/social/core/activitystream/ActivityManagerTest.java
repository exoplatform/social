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
package org.exoplatform.social.core.activitystream;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.AbstractPeopleTest;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.jcr.SocialDataLocation;

public class ActivityManagerTest extends AbstractPeopleTest {

  private final Log LOG = ExoLogger.getLogger(ActivityManagerTest.class);
  private List<Activity> tearDownActivityList;
  private String userName = "john";
  private Identity userIdentity;
  private IdentityManager identityManager;
  private ActivityManager activityManager;
  @Override
  public void setUp() {
    begin();

    SocialDataLocation dataLocation = (SocialDataLocation) getContainer().getComponentInstanceOfType(SocialDataLocation.class);
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager =  new ActivityManager(dataLocation, identityManager);
    tearDownActivityList = new ArrayList<Activity>();
    try {
      userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName);
    } catch (Exception e) {
      LOG.error("can not get or create userIdentity with remoteId: " + userName);
    }
  }

  @Override
  public void tearDown() {
    for (Activity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }
    end();
  }

  public void testGetActivity() {
    try {
      List<Activity> userActivities = activityManager.getActivities(userIdentity);
      assertNotNull(userActivities);
      assertEquals("user's activities should have 0 element.", 0, userActivities.size());

      Activity activity = new Activity();
      activity.setTitle("title");
      activity.setUserId(userIdentity.getRemoteId());
      activity.setUpdated(new Date());

      activityManager.saveActivity(userIdentity, activity);

      userActivities = activityManager.getActivities(userIdentity);
      assertEquals("user's activities should have 1 element", 1, userActivities.size());

      tearDownActivityList = userActivities;
    } catch (Exception e) {
      LOG.warn("exception happened!");
      e.printStackTrace();
    }
  }

  public void testGetActivitiesByPagingWithoutCreatingComments() throws Exception {
    final int totalActivityCount = 9;
    final int retrievedCount = 7;

    for (int i = 0; i < totalActivityCount; i++) {
      // root save on john's stream
      Activity activity = new Activity();
      activity.setTitle("blabla");
      activity.setUserId("root");
      activity.setUpdated(new Date());

      //save activity
      activityManager.saveActivity(userIdentity, activity);

      //for teardown cleanup
      tearDownActivityList.add(activity);
    }

    List<Activity> activities = activityManager.getActivities(userIdentity, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());
  }

  public void testGetActivities() {
    populateActivityMass(userIdentity, 30);
    try {
      List<Activity> activities = activityManager.getActivities(userIdentity);
      assertNotNull("activities must not be null", activities);
      assertEquals(20, activities.size());
    } catch (Exception e) {
      LOG.error("can not get activities");
      e.printStackTrace();
    }
  }

  private void populateActivityMass(Identity user, int number) {
    for (int i = 0; i < number; i++) {
      Activity activity = new Activity();
      activity.setTitle("title " + i);
      activity.setUserId(user.getRemoteId());
      try {
        activityManager.saveActivity(user, activity);
      } catch (Exception e) {
        LOG.error("can not save activity.");
        e.printStackTrace();
      }
      tearDownActivityList.add(activity);
    }
  }

  public void testAddProviders() throws Exception {
    ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    activityManager.addProcessor(new FakeProcessor(10));
    activityManager.addProcessor(new FakeProcessor(2));
    activityManager.addProcessor(new FakeProcessor(1));
    Activity activity = new Activity();
    activityManager.processActivitiy(activity);
    // just verify that we run in priority order
    assertEquals("null-1-2-10", activity.getTitle());
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
}