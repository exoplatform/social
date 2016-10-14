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

import java.util.List;

import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.SpaceStorage;

public class RDBMSSpaceStorageTest extends SpaceStorageTest {
  private SpaceStorage spaceStorage;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);    
  }

  @Override
  protected void tearDown() throws Exception {
    for (Space space : spaceStorage.getAllSpaces()) {
      spaceStorage.deleteSpace(space.getId());
    }
    super.tearDown();
  }
  
  public void testVisited() throws Exception {
    Space space0 = getSpaceInstance(0);
    spaceStorage.saveSpace(space0, true);
    Space space1 = getSpaceInstance(1);
    spaceStorage.saveSpace(space1, true);
    
    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("ghost");
    filter.setAppId("app1, app2, app3");
    
    List<Space> result = spaceStorage.getVisitedSpaces(filter, 0, -1); 
    assertEquals(2, result.size());
    assertEquals(space0.getId(), result.get(0).getId());
        
    //user access to space1 2s after space1 has been created
    Thread.sleep(2000);
    spaceStorage.updateSpaceAccessed("ghost", space1);   
    
    //getVisitedSpaces return a list of spaces that
    //order by visited space then others
    result = spaceStorage.getVisitedSpaces(filter, 0, -1); 
    assertEquals(2, result.size());
    assertEquals(space1.getId(), result.get(0).getId());
  }
  
  public void testLastAccess() throws Exception {
    Space space0 = getSpaceInstance(0);
    spaceStorage.saveSpace(space0, true);
    Space space1 = getSpaceInstance(1);
    spaceStorage.saveSpace(space1, true);
    
    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("ghost");
    filter.setAppId("app1, app2, app3");
    
    List<Space> result = spaceStorage.getLastAccessedSpace(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space0.getId(), result.get(0).getId());

    Thread.sleep(1000);
    spaceStorage.updateSpaceAccessed("ghost", space1);    

    result = spaceStorage.getLastAccessedSpace(filter, 0, -1); 
    assertEquals(2, result.size());
    assertEquals(space1.getId(), result.get(0).getId());
  }
}