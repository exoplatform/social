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
import org.exoplatform.social.AbstractPeopleTest;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityStorage;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.jcr.SocialDataLocation;

public class JCRStorageTest extends AbstractPeopleTest {

  private SocialDataLocation dataLocation;
  private IdentityStorage identityStorage;
  private ActivityStorage activityStorage;
  private ArrayList<String> activityIds = new ArrayList<String>();

  @Override
  protected void setUp() throws Exception {
    dataLocation = (SocialDataLocation) getContainer().getComponentInstanceOfType(SocialDataLocation.class);
    activityStorage = new ActivityStorage(dataLocation);
    identityStorage = new IdentityStorage(dataLocation);

    begin();
  }

  @Override
  protected void tearDown() throws Exception {
    for (int i = 0, j = activityIds.size(); i < j; i++) {
      String id = activityIds.get(i);
      activityStorage.deleteActivity(id);
    }

    dataLocation = null;
    identityStorage = null;
    activityStorage = null;

    end();
  }

  public void testGetStreamInfo() throws Exception {
    Identity root = new Identity(OrganizationIdentityProvider.NAME, "root");
    identityStorage.saveIdentity(root);

    // root save on root's stream
    Activity activity = new Activity();
    activity.setTitle("blabla");
    activity.setUserId("root");
    activity.setUpdated(new Date());
    activityStorage.save(root, activity);

    //for teardown func
    activityIds.add(activity.getId());

    String streamId = activity.getStreamId();
    assertNotNull(streamId);
    assertEquals(activity.getStreamOwner(), "root");

    List<Activity> activities = activityStorage.getActivities(root);
    assertEquals(1, activities.size());
    assertEquals(activities.get(0).getStreamOwner(), "root");
    assertEquals(activities.get(0).getStreamId(), streamId);

    Activity loaded = activityStorage.load(activity.getId());
    assertEquals(loaded.getStreamOwner(), "root");
    assertEquals(loaded.getStreamId(), streamId);
  }

  public void testGetAllActivities() throws Exception {
    int totalActivityCount = 27;

    Identity john = new Identity(OrganizationIdentityProvider.NAME, "john");
    identityStorage.saveIdentity(john);

    assertNotNull(identityStorage.findIdentityById(john.getId()));

    for (int i = 0; i < totalActivityCount; i++) {
      // root save on john's stream
      Activity activity = new Activity();
      activity.setTitle("blabla");
      activity.setUserId("root");
      activity.setUpdated(new Date());

      //save activity
      activityStorage.save(john, activity);

      //for teardown cleanup
      activityIds.add(activity.getId());

      //test activity has been created
      String streamId = activity.getStreamId();
      assertNotNull(streamId);
      assertEquals(activity.getStreamOwner(), "john");

      Activity comment = new Activity();
      comment.setTitle("this is activity " + i);
      comment.setUserId("root");
      comment.setUpdated(new Date());
      comment.setReplyToId(Activity.IS_COMMENT);
      activityStorage.save(john, comment);
      activityIds.add(comment.getId());
    }

    List<Activity> activities = activityStorage.getActivities(john);
    assertEquals(totalActivityCount, activities.size());
  }


  public void testGetActivitiesByPagingWithoutCreatingComments() throws Exception {
    final int totalActivityCount = 9;
    final int retrievedCount = 7;

    Identity john = new Identity(OrganizationIdentityProvider.NAME, "john");
    identityStorage.saveIdentity(john);
    assertNotNull(identityStorage.findIdentityById(john.getId()));

    for (int i = 0; i < totalActivityCount; i++) {
      // root save on john's stream
      Activity activity = new Activity();
      activity.setTitle("blabla");
      activity.setUserId("root");
      activity.setUpdated(new Date());

      //save activity
      activityStorage.save(john, activity);

      //for teardown cleanup
      activityIds.add(activity.getId());
    }

    List<Activity> activities = activityStorage.getActivities(john, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());
  }

  public void testGetActivitiesByPagingWithCreatingComments() throws Exception {
    final int totalActivityCount = 2;
    final int retrievedCount = 1;

    Identity john = new Identity(OrganizationIdentityProvider.NAME, "john");
    identityStorage.saveIdentity(john);
    assertNotNull(identityStorage.findIdentityById(john.getId()));

    for (int i = 0; i < totalActivityCount; i++) {
      // root save on john's stream
      Activity activity = new Activity();
      activity.setTitle("blabla");
      activity.setUserId("root");
      activity.setUpdated(new Date());

      //save activity
      activityStorage.save(john, activity);

      //for teardown cleanup
      activityIds.add(activity.getId());

      //test activity has been created
      String streamId = activity.getStreamId();
      assertNotNull(streamId);
      assertEquals(activity.getStreamOwner(), "john");

      Activity comment = new Activity();
      comment.setTitle("this is comment " + i);
      comment.setUserId("root");
      comment.setUpdated(new Date());
      comment.setReplyToId(Activity.IS_COMMENT);
      activityStorage.save(john, comment);
      activityIds.add(comment.getId());
    }

    List<Activity> activities = activityStorage.getActivities(john, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());
  }
}