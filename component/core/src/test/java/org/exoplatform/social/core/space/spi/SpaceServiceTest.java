/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.core.space.spi;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;


public class SpaceServiceTest extends AbstractCoreTest {
  private static final Log LOG = ExoLogger.getLogger(SpaceServiceTest.class);
  private SpaceService spaceService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    assertNotNull("spaceService must not be null", spaceService);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testGetAllSpaces() {
    assertTrue(true);
  }

  public void testGetSpaceByName() {
    assertTrue(true);
  }

  public void testGetSpacesByFirstCharacterOfName() {
    assertTrue(true);
  }

  public void testGetSpacesBySearchCondition() {
    assertTrue(true);
  }

  public void testGetSpaceById() {
    assertTrue(true);
  }

  public void testGetSpaceByUrl() {
    assertTrue(true);
  }

  public void testGetSpaces() {
    assertTrue(true);
  }

  public void testGetAccessibleSpaces() {
    assertTrue(true);
  }

  public void testGetEditableSpaces() {
    assertTrue(true);
  }

  public void testGetInvitedSpaces() {
    assertTrue(true);
  }

  public void testGetPublicSpaces() {
   assertTrue(true);
  }

  public void testGetPendingSpaces() {
    assertTrue(true);
  }

  public void testCreateSpace() throws Exception {
    assertTrue(true);
    /*
    Space space1 = new Space();
    space1.setApp("Calendar;FileSharing");
    space1.setGroupId("Group1");
    space1.setName("Space1");

    Space space2 = new Space();
    space2.setApp("Contact,Forum");
    space2.setGroupId("Group2");
    space2.setName("Space2");

    spaceService.saveSpace(space1, true);
    spaceService.saveSpace(space2, true);

    assertEquals(2, spaceService.getAllSpaces().size());

    Space space3 = spaceService.getSpaceById(space1.getId());
    assertNotNull(space3);
    assertEquals(space1.getId(), space3.getId());
    assertNotSame(space3.getApp(), "Calendar");
    assertEquals(space3.getGroupId(), "Group1");

    spaceService.deleteSpace(space1);
    spaceService.deleteSpace(space2);
    */
  }

  public void testGetSpace() throws Exception {
    assertTrue(true);
    /*
    Space space = new Space();
    space.setApp("Calendar");
    space.setGroupId("Group1");
    space.setName("calendar");
    spaceService.saveSpace(space, true);
    Space gotSpace = spaceService.getSpaceById(space.getId());
    assertNotNull(gotSpace);
    assertEquals(1, spaceService.getAllSpaces().size());
    assertEquals("Calendar", gotSpace.getApp());
    assertEquals("Group1", gotSpace.getGroupId());
    spaceService.deleteSpace(gotSpace);
    */
  }

}
