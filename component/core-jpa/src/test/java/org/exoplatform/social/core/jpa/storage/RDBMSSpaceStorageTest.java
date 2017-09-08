/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.storage;

import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.SpaceStorage;

import java.util.List;

public class RDBMSSpaceStorageTest extends SpaceStorageTest {
  private SpaceStorage spaceStorage;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);
  }
  
  public void testVisited() throws Exception {
    Space space0 = getSpaceInstance(5);
    spaceStorage.saveSpace(space0, true);
    Space space1 = getSpaceInstance(6);
    spaceStorage.saveSpace(space1, true);
    
    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("ghost");
    filter.setAppId("app1, app2, app3");
    
    List<Space> result = spaceStorage.getVisitedSpaces(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space0.getId(), result.get(0).getId());
        
    //user access to space1 2.5s after space1 has been created
    Thread.sleep(2500);
    spaceStorage.updateSpaceAccessed("ghost", space1);
    
    //getVisitedSpaces return a list of spaces that
    //order by visited space then others
    result = spaceStorage.getVisitedSpaces(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space1.getId(), result.get(0).getId());
  }
  
  public void testLastAccess() throws Exception {
    Space space2 = getSpaceInstance(7);
    spaceStorage.saveSpace(space2, true);
    Space space3 = getSpaceInstance(8);
    spaceStorage.saveSpace(space3, true);

    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("ghost");
    filter.setAppId("app1, app2, app3");
    
    List<Space> result = spaceStorage.getLastAccessedSpace(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space2.getId(), result.get(0).getId());

    Thread.sleep(2500);
    spaceStorage.updateSpaceAccessed("ghost", space3);

    result = spaceStorage.getLastAccessedSpace(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space3.getId(), result.get(0).getId());
  }
}