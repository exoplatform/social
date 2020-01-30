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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;

public class RDBMSSpaceStorageTest extends SpaceStorageTest {
  private SpaceStorage spaceStorage;
  private SocialStorageCacheService cacheService;

  private List<Space> tearDownSpaceList = new ArrayList<>();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);
    cacheService = getService(SocialStorageCacheService.class);
  }

  @Override
  protected void tearDown() throws Exception {
    for (Space space : tearDownSpaceList) {
      spaceService.deleteSpace(space);
    }
    super.tearDown();
  }

  public void testVisited() throws Exception {
    Space space0 = getSpaceInstance(5);
    spaceStorage.saveSpace(space0, true);
    tearDownSpaceList.add(space0);
    Space space1 = getSpaceInstance(6);
    spaceStorage.saveSpace(space1, true);
    tearDownSpaceList.add(space1);

    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("ghost");
    filter.setAppId("app1, app2, app3");
    
    List<Space> result = spaceStorage.getVisitedSpaces(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space0.getId(), result.get(0).getId());
        
    restartTransaction();
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
    tearDownSpaceList.add(space2);
    Space space3 = getSpaceInstance(8);
    spaceStorage.saveSpace(space3, true);
    tearDownSpaceList.add(space3);

    SpaceFilter filter = new SpaceFilter();
    filter.setRemoteId("ghost");
    filter.setAppId("app1, app2, app3");
    
    List<Space> result = spaceStorage.getLastAccessedSpace(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space2.getId(), result.get(0).getId());

    restartTransaction();
    spaceStorage.updateSpaceAccessed("ghost", space3);

    result = spaceStorage.getLastAccessedSpace(filter, 0, -1);
    assertEquals(2, result.size());
    assertEquals(space3.getId(), result.get(0).getId());
  }

  public void testGetLastAccessedSpace() {
    //create a new space
    Space space = getSpaceInstance(1);
    spaceStorage.saveSpace(space,true);
    tearDownSpaceList.add(space);
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

  public void testGetLastAccessedPagination() throws Exception {
    List<Space> spaces = spaceService.getLastAccessedSpace("raul", null, 0, 10);
    assertEquals(0, spaces.size());

    int numberOfSpaces = 5;
    for(int i = 0; i < numberOfSpaces; i++) {
      Space s = getSpaceInstance(i);
      spaceStorage.saveSpace(s, true);
      tearDownSpaceList.add(s);
      spaceService.updateSpaceAccessed("raul", s);
      spaces = spaceService.getLastAccessedSpace("raul", null, 0, 10);
      assertEquals(s.getPrettyName(), spaces.get(0).getPrettyName());

      spaces = spaceService.getLastAccessedSpace("raul", null, 0, i + 1);
      assertEquals(s.getPrettyName(), spaces.get(0).getPrettyName());
    }

    spaces = spaceService.getLastAccessedSpace("raul", null, 0, 2);
    assertEquals(2, spaces.size());
    assertEquals("my_space_test_4", spaces.get(0).getPrettyName());
    assertEquals("my_space_test_3", spaces.get(1).getPrettyName());

    Space space6 = getSpaceInstance(6);
    spaceStorage.saveSpace(space6, true);
    tearDownSpaceList.add(space6);
    spaces = spaceService.getLastAccessedSpace("raul", null, 0, 2);
    assertEquals(2, spaces.size());
    assertEquals("my_space_test_4", spaces.get(0).getPrettyName());
    assertEquals("my_space_test_3", spaces.get(1).getPrettyName());

    spaces = spaceService.getLastAccessedSpace("raul", null, 2, 10);
    assertEquals(4, spaces.size());
    assertEquals("my_space_test_2", spaces.get(0).getPrettyName());
    assertEquals("my_space_test_1", spaces.get(1).getPrettyName());
    assertEquals("my_space_test_0", spaces.get(2).getPrettyName());
    assertEquals("my_space_test_6", spaces.get(3).getPrettyName());

    spaceService.updateSpaceAccessed("raul", space6);

    spaces = spaceService.getLastAccessedSpace("raul", null, 0, 2);
    assertEquals(2, spaces.size());
    assertEquals("my_space_test_6", spaces.get(0).getPrettyName());
    assertEquals("my_space_test_4", spaces.get(1).getPrettyName());

    spaces = spaceService.getLastAccessedSpace("raul", null, 2, 10);
    assertEquals(4, spaces.size());
    assertEquals("my_space_test_3", spaces.get(0).getPrettyName());
    assertEquals("my_space_test_2", spaces.get(1).getPrettyName());
    assertEquals("my_space_test_1", spaces.get(2).getPrettyName());
    assertEquals("my_space_test_0", spaces.get(3).getPrettyName());
  }

}