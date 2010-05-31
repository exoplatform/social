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

import org.exoplatform.social.AbstractPeopleTest;
import org.exoplatform.social.core.activitystream.model.Activity;

public class ActivityManagerTest extends AbstractPeopleTest {


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
