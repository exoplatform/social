/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.feature;

import java.util.Calendar;

import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter.ActivityFilterType;

import junit.framework.TestCase;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 2, 2013  
 */
public class ActivityUpdateFilterTest extends TestCase {

  
  public void testConnectionsActivities() throws Exception {
    Long fromSinceTime = Calendar.getInstance().getTimeInMillis();
    Long toSinceTime = fromSinceTime + 1000;
    ActivityFilterType.CONNECTIONS_ACTIVITIES.fromSinceTime(fromSinceTime).toSinceTime(toSinceTime);
    
    assertEquals(fromSinceTime, ActivityFilterType.CONNECTIONS_ACTIVITIES.fromSinceTime());
    assertEquals(toSinceTime, ActivityFilterType.CONNECTIONS_ACTIVITIES.toSinceTime());
  }
  
  public void testUserActivities() throws Exception {
    Long fromSinceTime = Calendar.getInstance().getTimeInMillis();
    Long toSinceTime = fromSinceTime + 1000;
    ActivityFilterType.USER_ACTIVITIES.fromSinceTime(fromSinceTime).toSinceTime(toSinceTime);
    
    assertEquals(fromSinceTime, ActivityFilterType.USER_ACTIVITIES.fromSinceTime());
    assertEquals(toSinceTime, ActivityFilterType.USER_ACTIVITIES.toSinceTime());
  }
  
  public void testUserSpaceActivities() throws Exception {
    Long fromSinceTime = Calendar.getInstance().getTimeInMillis();
    Long toSinceTime = fromSinceTime + 1000;
    ActivityFilterType.USER_SPACE_ACTIVITIES.fromSinceTime(fromSinceTime).toSinceTime(toSinceTime);
    
    assertEquals(fromSinceTime, ActivityFilterType.USER_SPACE_ACTIVITIES.fromSinceTime());
    assertEquals(toSinceTime, ActivityFilterType.USER_SPACE_ACTIVITIES.toSinceTime());
  }
  
  public void testSpaceActivities() throws Exception {
    Long fromSinceTime = Calendar.getInstance().getTimeInMillis();
    Long toSinceTime = fromSinceTime + 1000;
    ActivityFilterType.SPACE_ACTIVITIES.fromSinceTime(fromSinceTime).toSinceTime(toSinceTime);
    
    assertEquals(fromSinceTime, ActivityFilterType.SPACE_ACTIVITIES.fromSinceTime());
    assertEquals(toSinceTime, ActivityFilterType.SPACE_ACTIVITIES.toSinceTime());
  }
}
