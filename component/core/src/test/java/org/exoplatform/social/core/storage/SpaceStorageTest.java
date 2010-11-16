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
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Tests for {@link SpaceStorage}
 *
 * @author vien_levan
 * @since Nov 3, 2010
 * @copyright eXo SAS
 */

public class SpaceStorageTest extends AbstractCoreTest {

  // list of spaces for deleting in tearDown
  private List<Space>  tearDownSpaceList;

  private SpaceStorage spaceStorage;

  /**
   *initialize properties
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = (SpaceStorage) this.getContainer()
                                      .getComponentInstanceOfType(SpaceStorage.class);
    assertNotNull("spaceStorage must not be null", spaceStorage);
    tearDownSpaceList = new ArrayList<Space>();
  }

  /**
   * Clean up
   */
  @Override
  protected void tearDown() throws Exception {
    for (Space sp : tearDownSpaceList) {
      spaceStorage.deleteSpace(sp.getId());
    }
    super.tearDown();
  }

  // get an instance of Space
  private Space getSpaceInstance(int number) {
    Space space = new Space();
    space.setName("my space " + number);
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    return space;
  }

  /**
   * Test {@link SpaceStorage#getAllSpaces()}
   *
   * @throws Exception
   */
  public void testGetAllSpaces() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      tearDownSpaceList.add(space);
    }
    assertEquals("spaceStorage.getAllSpaces().size() must return: 10", 10, spaceStorage.getAllSpaces().size());
  }

  /**
   * Test {@link SpaceStorage#getSpaceById(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceById() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);

    spaceStorage.saveSpace(space, true);
    tearDownSpaceList.add(space);

    Space savedSpace = spaceStorage.getSpaceById(space.getId());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertNotNull("savedSpace.getId() must not be null", savedSpace.getId());
    assertEquals("space.getId() must return: " + space.getId(), space.getId(), savedSpace.getId());
    assertEquals("space.getName() must return: " + space.getName(),
                 space.getName(),
                 savedSpace.getName());
    assertEquals("space.getRegistration() must return: " + space.getRegistration(),
                 space.getRegistration(),
                 savedSpace.getRegistration());
    assertEquals("space.getDescription() must return: " + space.getDescription(),
                 space.getDescription(),
                 savedSpace.getDescription());
    assertEquals("space.getType() must return: " + space.getType(),
                 space.getType(),
                 savedSpace.getType());
    assertEquals("space.getVisibility() must return: " + space.getVisibility(),
                 space.getVisibility(),
                 savedSpace.getVisibility());
    assertEquals("space.getPriority() must return: " + space.getPriority(),
                 space.getPriority(),
                 savedSpace.getPriority());
  }

  /**
   * Test {@link SpaceStorage#getSpacesBySearchCondition(String)}, parameter is
   * name or description of space}
   *
   * @throws Exception
   */
  public void testGetSpaceBySearchCondition() throws Exception {
    int totalNumber = 10;
    for (int i = 0; i < totalNumber; i++) {
      Space sp = this.getSpaceInstance(i);
      spaceStorage.saveSpace(sp, true);
      tearDownSpaceList.add(sp);
    }

    // get space by search name
    String nameSpace = "my space";
    List<Space> foundSpaceList = spaceStorage.getSpacesBySearchCondition(nameSpace);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber,
                 totalNumber,
                 foundSpaceList.size());

    // get space by search description
    String description = "add new space ";
    List<Space> descriptionSearch = new ArrayList<Space>();
    descriptionSearch = spaceStorage.getSpacesBySearchCondition(description);
    assertNotNull("descriptionSearch must not be null", descriptionSearch);
    assertEquals("tearDownSpaceList.size() must return: " + totalNumber,
                 totalNumber,
                 descriptionSearch.size());

  }

  /**
   * Test {@link SpaceStorage#getSpaceByUrl(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceByUrl() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    space.setUrl("http://fake.com.vn");
    spaceStorage.saveSpace(space, true);
    tearDownSpaceList.add(space);

    // get saved space
    Space savedSpace = spaceStorage.getSpaceByUrl(space.getUrl());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertNotNull("savedSpace.getId() must not be null", savedSpace.getId());
    assertEquals("space.getId() must return: " + space.getId(), space.getId(), savedSpace.getId());
    assertEquals("space.getName() must return: " + space.getName(),
                 space.getName(),
                 savedSpace.getName());
    assertEquals("space.getRegistration() must return: " + space.getRegistration(),
                 space.getRegistration(),
                 savedSpace.getRegistration());
    assertEquals("space.getDescription() must return: " + space.getDescription(),
                 space.getDescription(),
                 savedSpace.getDescription());
    assertEquals("space.getType() must return: " + space.getType(),
                 space.getType(),
                 savedSpace.getType());
    assertEquals("space.getVisibility() must equal savedSpace.getVisibility() = "
        + space.getVisibility(), space.getVisibility(), savedSpace.getVisibility());
    assertEquals("space.getPriority() must return: " + space.getPriority(),
                 space.getPriority(),
                 savedSpace.getPriority());
    assertEquals("space.getUrl() must return: " + space.getUrl(),
                 space.getUrl(),
                 savedSpace.getUrl());
  }

  /**
   * Test {@link SpaceStorage#getSpaceByName(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceByName() throws Exception {
    // number for method getSpaceInstance(int number)
    int number = 1;
    // new space
    Space space = this.getSpaceInstance(number);

    // add to tearDownSpaceList
    tearDownSpaceList.add(space);
    // save to space storage
    spaceStorage.saveSpace(space, true);

    // get space saved by name
    Space foundSpaceList = spaceStorage.getSpaceByName(space.getName());
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertNotNull("foundSpaceList.getId() must not be null", foundSpaceList.getId());
    assertEquals("space.getId() must return: " + space.getId(),
                 space.getId(),
                 foundSpaceList.getId());
    assertEquals("space.getName() must return: " + space.getName(),
                 space.getName(),
                 foundSpaceList.getName());
    assertEquals("space.getRegistration() must return: " + space.getRegistration(),
                 space.getRegistration(),
                 foundSpaceList.getRegistration());
    assertEquals("space.getDescription() must return: " + space.getDescription(),
                 space.getDescription(),
                 foundSpaceList.getDescription());
    assertEquals("space.getType() must return: " + space.getType(),
                 space.getType(),
                 foundSpaceList.getType());
    assertEquals("space.getVisibility() must return: " + space.getVisibility(),
                 space.getVisibility(),
                 foundSpaceList.getVisibility());
    assertEquals("space.getPriority() must return: " + space.getPriority(),
                 space.getPriority(),
                 foundSpaceList.getPriority());
  }

  /**
   * Test {@link SpaceStorage#deleteSpace(String)}
   */
  public void testDeleteSpace() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    spaceStorage.saveSpace(space, true);
    spaceStorage.deleteSpace(space.getId());
    assertNull("spaceStorage.getSpaceById() must be null", spaceStorage.getSpaceById(space.getId()));
  }

  public void testSaveSpace() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    tearDownSpaceList.add(space);
    spaceStorage.saveSpace(space, true);
    assertNotNull("space.getId() must not be null", space.getId());
    String newName = "newnamespace";
    space.setName(newName);
    spaceStorage.saveSpace(space, false);
    assertEquals("spaceStorage.getSpaceById(space.getId()).getName() must return: "
        + newName, newName, spaceStorage.getSpaceById(space.getId())
                                                                .getName());
    assertEquals("space.getName() must return: " + newName, newName, space.getName());
  }
}
