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

import java.util.Date;
import java.util.List;

import org.exoplatform.social.AbstractPeopleTest;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.JCRStorage;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.jcr.SocialDataLocation;

public class JCRStorageTest extends AbstractPeopleTest {

  public void testGetStreamInfo() throws Exception {

    SocialDataLocation dataLocation = (SocialDataLocation) getContainer().getComponentInstanceOfType(SocialDataLocation.class);
    org.exoplatform.social.core.activitystream.JCRStorage storage = new org.exoplatform.social.core.activitystream.JCRStorage(dataLocation);
    JCRStorage identityStorage = new JCRStorage(dataLocation);
    // root save on john's stream
    Activity activity = new Activity();
    activity.setTitle("blabla");
    activity.setUserId("root");
    activity.setUpdated(new Date());
    Identity root = new Identity(OrganizationIdentityProvider.NAME, "root");
    identityStorage.saveIdentity(root);
    storage.save(root, activity);

    String streamId = activity.getStreamId();
    assertNotNull(streamId);
    assertEquals(activity.getStreamOwner(), "root");

    List<Activity> activities = storage.getActivities(root);
    assertEquals(1, activities.size());
    assertEquals(activities.get(0).getStreamOwner(), "root");
    assertEquals(activities.get(0).getStreamId(), streamId);

    Activity loaded = storage.load(activity.getId());
    assertEquals(loaded.getStreamOwner(), "root");
    assertEquals(loaded.getStreamId(), streamId);
  }

  public void testGetActivities() throws Exception {

    SocialDataLocation dataLocation = (SocialDataLocation) getContainer().getComponentInstanceOfType(SocialDataLocation.class);
    org.exoplatform.social.core.activitystream.JCRStorage storage = new org.exoplatform.social.core.activitystream.JCRStorage(dataLocation);
    JCRStorage identityStorage = new JCRStorage(dataLocation);
    // root save on john's stream
    Activity activity = new Activity();
    activity.setTitle("blabla");
    activity.setUserId("root");
    activity.setUpdated(new Date());
    Identity john = new Identity(OrganizationIdentityProvider.NAME, "john");
    identityStorage.saveIdentity(john);
    assertNotNull(identityStorage.findIdentityById(john.getId()));
    storage.save(john, activity);

    Activity comment = new Activity();
    comment.setTitle("re:title");
    comment.setUserId("root");
    comment.setUpdated(new Date());
    comment.setReplyToId(Activity.IS_COMMENT);
    storage.save(john, comment);

    String streamId = activity.getStreamId();
    assertNotNull(streamId);
    assertEquals(activity.getStreamOwner(), "john");

    List<Activity> activities = storage.getActivities(john, 0, 20);
    assertEquals(1, activities.size());
    assertEquals(activities.get(0).getStreamOwner(), "john");
    assertEquals(activities.get(0).getStreamId(), streamId);

    Activity loaded = storage.load(activity.getId());
    assertEquals(loaded.getStreamOwner(), "john");
    assertEquals(loaded.getStreamId(), streamId);
  }

}
