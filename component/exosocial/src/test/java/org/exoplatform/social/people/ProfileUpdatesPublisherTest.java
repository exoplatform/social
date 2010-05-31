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
package org.exoplatform.social.people;

import java.util.List;

import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.spi.ProfileLifeCycleEvent;
import org.exoplatform.social.core.identity.spi.ProfileLifeCycleEvent.Type;
import org.exoplatform.social.test.AbstractExoSocialTest;

public class ProfileUpdatesPublisherTest extends AbstractExoSocialTest {

  public void testPublishActivity() throws Exception {
    ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull("activityManager must not be null", activityManager);
    IdentityManager identityManager =  (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull("identityManager must not be null", identityManager);
    ProfileUpdatesPublisher publisher = (ProfileUpdatesPublisher) getContainer().getComponentInstanceOfType(ProfileUpdatesPublisher.class);
    assertNotNull("profileUpdatesPublisher must not be null", publisher);
    // create an identity
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Profile profile = identity.getProfile();
    assertNotNull("profile can not be null", profile);
    profile.setProperty(Profile.FIRST_NAME, "First Name");
    ProfileLifeCycleEvent event = new ProfileLifeCycleEvent(Type.BASIC_UPDATED, "root", profile);
    publisher.basicInfoUpdated(event);
    // check that the activity was created and that it contains what we expect
    List<Activity> activities = activityManager.getActivities(identity);
    assertEquals(1, activities.size());
    assertTrue(activities.get(0).getTitle().contains("basic"));
  }
}
