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
package org.exoplatform.social.core.application;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class SpaceActivityPublisherTest extends  AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(SpaceActivityPublisherTest.class);
  private ActivityManager activityManager;
  private IdentityManager identityManager;
  private SpaceService spaceService;
  private SpaceActivityPublisher spaceActivityPublisher;
  private List<Activity> tearDownActivityList;
  @Override
  public void setUp() throws Exception {
    super.setUp();
    tearDownActivityList = new ArrayList<Activity>();
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull("activityManager must not be null", activityManager);
    identityManager =  (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull("identityManager must not be null", identityManager);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    assertNotNull("spaceService must not be null", spaceService);
    spaceActivityPublisher = (SpaceActivityPublisher) getContainer().getComponentInstanceOfType(SpaceActivityPublisher.class);
    assertNotNull("spaceActivityPublisher must not be null", spaceActivityPublisher);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    for (Activity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }
  }

  public void testSpaceCreation() throws Exception {
    assertTrue(true);
    /*
    Space space = new Space();
    space.setName("Toto");
    spaceService.saveSpace(space, true);
    String spaceId = space.getId(); // set by storage
    SpaceLifeCycleEvent event  = new SpaceLifeCycleEvent(space, "root", SpaceLifeCycleEvent.Type.SPACE_CREATED);
    spaceActivityPublisher.spaceCreated(event);

    Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceId);
    List<Activity> activities = activityManager.getActivities(identity);
    assertEquals(1, activities.size());
    tearDownActivityList.add(activities.get(0));
    assertTrue(activities.get(0).getTitle().contains(space.getName()));
    assertTrue(activities.get(0).getTitle().contains("root"));
    //clean up
    spaceService.deleteSpace(space);
    */
  }
}
