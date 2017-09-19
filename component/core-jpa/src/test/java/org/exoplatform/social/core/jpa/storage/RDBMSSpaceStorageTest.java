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
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;

public class RDBMSSpaceStorageTest extends SpaceStorageTest {
  private SpaceStorage spaceStorage;
  private SocialStorageCacheService cacheService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);
    cacheService = getService(SocialStorageCacheService.class);
  }

  @Override
  protected void tearDown() throws Exception {
    for (Space space : spaceStorage.getAllSpaces()) {
      spaceStorage.deleteSpace(space.getId());
    }
    super.tearDown();
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
        
    //user access to space1 2s after space1 has been created
    Thread.sleep(2000);
    spaceStorage.updateSpaceAccessed("ghost", space1);
    
    //getVisitedSpaces return a list of spaces that
    //order by visited space then others
    result = spaceStorage.getVisitedSpaces(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space1.getId(), result.get(0).getId());

    spaceStorage.deleteSpace(space0.getId());
    spaceStorage.deleteSpace(space1.getId());
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

    Thread.sleep(1000);
    spaceStorage.updateSpaceAccessed("ghost", space3);

    result = spaceStorage.getLastAccessedSpace(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space3.getId(), result.get(0).getId());

    spaceStorage.deleteSpace(space2.getId());
    spaceStorage.deleteSpace(space3.getId());
  }

  public void testGetLastAccessedSpace() {
    //create a new space
    Space space = getSpaceInstance(1);
    spaceStorage.saveSpace(space,true);
    // Update space accessed
    spaceStorage.updateSpaceAccessed("raul",space);
    cacheService.getSpacesCache().clearCache();

    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("raul");
    // Get last accessed space list , this will fill all the caches with a complete SpaceData
    spaceStorage.getLastAccessedSpace(filter,0,10);
    // clear SpaceCache and SpaceSimpleCache
    cacheService.getSpaceCache().clearCache();
    cacheService.getSpaceSimpleCache().clearCache();
    //Get last accessed space list again, this will fill both SimpleSpaceCache and SpaceCache with a SpaceSimpleData
    //that has members and managers set to null by the function putSpaceInCacheIfNotExists
    spaceStorage.getLastAccessedSpace(filter,0,10);

    //Get the space from SpaceCache, this will retrieve a SpaceSimpleData object with managers and members set to null
    Space spaceFromCache = spaceStorage.getSpaceById(space.getId());

    // Check that the space should have 3 members and 2 managers
    assertNotNull(spaceFromCache.getMembers());
    assertEquals(5,spaceFromCache.getMembers().length);
    assertNotNull(spaceFromCache.getManagers());
    assertEquals(2,spaceFromCache.getManagers().length);
  }
}