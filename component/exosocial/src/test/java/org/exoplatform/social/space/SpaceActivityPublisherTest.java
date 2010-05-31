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
package org.exoplatform.social.space;

import java.util.List;

import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.impl.SpaceIdentityProvider;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.test.AbstractExoSocialTest;

public class SpaceActivityPublisherTest extends  AbstractExoSocialTest {

  public void testSpaceCreation() throws Exception {
    ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull("ActivityManager not initialized. Check test configuration", activityManager);
    IdentityManager identityManager =  (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    SpaceService spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    SpaceActivityPublisher publisher = (SpaceActivityPublisher) getContainer().getComponentInstanceOfType(SpaceActivityPublisher.class);

    Space space = new Space();
    space.setName("Toto");
    spaceService.saveSpace(space, true);
    String spaceId = space.getId(); // set by storage
    SpaceLifeCycleEvent event  = new SpaceLifeCycleEvent(space, "root", SpaceLifeCycleEvent.Type.SPACE_CREATED);
    publisher.spaceCreated(event);

    Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceId);
    List<Activity> activities = activityManager.getActivities(identity);
    assertEquals(1, activities.size());
    assertTrue(activities.get(0).getTitle().contains(space.getName()));
    assertTrue(activities.get(0).getTitle().contains("root"));
  }
}
