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

import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceUtils;
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

  public void testGetAllSpaces() throws Exception {
    populateData();
    createMoreSpace("Space2");
    assertEquals(2, spaceService.getAllSpaces().size());
    destroyData();
  }

  public void testGetSpaceByName() throws Exception {
    Space space = populateData();
    assertEquals(space.getName(), spaceService.getSpaceByName("Space1").getName());
    destroyData();
  }

  public void testGetSpacesByFirstCharacterOfName() throws Exception {
    populateData();
    createMoreSpace("Space2");
    assertEquals(2, spaceService.getSpacesByFirstCharacterOfName("S").size());
    destroyData();
  }

  public void testGetSpacesBySearchCondition() throws Exception {
    populateData();
    createMoreSpace("Space2");
    assertEquals(2, spaceService.getSpacesBySearchCondition("Space").size());
    assertEquals(1, spaceService.getSpacesBySearchCondition("1").size());
    destroyData();
  }

  public void testGetSpaceById() throws Exception {
    Space space = populateData();
    createMoreSpace("Space2");
    assertEquals(space.getName(), spaceService.getSpaceById(space.getId()).getName());
    destroyData();
  }

  public void testGetSpaceByUrl() throws Exception {
    Space space = populateData();
    assertEquals(space.getName(), spaceService.getSpaceByUrl("space1").getName());
    destroyData();
  }
  
// //Relate to navigation and page maybe tested later 
//  public void testGetSpaces() throws Exception {
//    Space space = populateData();
//    spaceService.addMember(space, "root");
//    assertEquals(1, spaceService.getSpaces("root").size());
//    destroyData();
//  }
//
//  public void testGetAccessibleSpaces() throws Exception {
//    populateData();
//    Space space = spaceService.getSpaceByName("Space1");
//    spaceService.setLeader(space, "root", true);
//    assertEquals(1, spaceService.getAccessibleSpaces("root").size());
//    destroyData();
//  }

  public void testGetEditableSpaces() throws Exception {
    populateData();
    assertEquals(1, spaceService.getEditableSpaces("root").size());
    destroyData();
  }

  public void testGetInvitedSpaces() throws Exception {
    populateData();
    assertEquals(0, spaceService.getInvitedSpaces("root").size());
    Space space = spaceService.getSpaceByName("Space1");
    spaceService.inviteMember(space, "root");
    assertEquals(1, spaceService.getInvitedSpaces("root").size());
    destroyData();
  }

  public void testGetPublicSpaces() throws Exception {
    populateData();
    assertEquals(1, spaceService.getPublicSpaces("root").size());
    destroyData();
  }

  public void testGetPendingSpaces() throws Exception {
    populateData();
    Space space = spaceService.getSpaceByName("Space1");
    spaceService.requestJoin(space, "root");
    assertEquals(true, spaceService.isPending(space, "root"));
    destroyData();
  }

  public void testCreateSpace() throws Exception {
    populateData();
    createMoreSpace("Space2");
    assertEquals(2, spaceService.getAllSpaces().size());
    destroyData();
  }

  private Space populateData() throws Exception {
    String spaceName = "Space1";
    Space space1 = new Space();
    space1.setApp("Calendar;FileSharing");
    space1.setName(spaceName);
    String shortName = SpaceUtils.cleanString(spaceName);
    space1.setGroupId("/spaces/" + shortName);
    space1.setUrl(shortName);
    space1.setRegistration("validation");
    space1.setDescription("This is my first space for testing"); 
    space1.setType("classic");
    space1.setVisibility("public");
    space1.setPriority("2");

    spaceService.saveSpace(space1, true);
    return space1;
  }
  
  private Space createMoreSpace(String spaceName) throws Exception {
    Space space2 = new Space();
    space2.setApp("Contact,Forum");
    space2.setName(spaceName);
    String shortName = SpaceUtils.cleanString(spaceName);
    space2.setGroupId("/spaces/" + shortName );
    space2.setUrl(shortName);
    space2.setRegistration("open");
    space2.setDescription("This is my second space for testing"); 
    space2.setType("classic");
    space2.setVisibility("public");
    space2.setPriority("2");
    
    spaceService.saveSpace(space2, true);
    
    return space2;
  }
  
  private void destroyData() throws Exception {
    List<Space> spaces = spaceService.getAllSpaces();
    for (Space space : spaces) {
      spaceService.deleteSpace(space);
    }
  }
}
