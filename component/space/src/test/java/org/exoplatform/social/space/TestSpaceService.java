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
package org.exoplatform.social.space;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;

import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.testng.annotations.Test;

@ConfiguredBy( {
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.space.configuration.xml") })
public class TestSpaceService extends AbstractJCRTestCase {

  protected void afterContainerStart() {

  }

  public TestSpaceService() throws Exception {
    super();
  }

  @Test
  public void testGetAllSpaces() throws Exception {
    SpaceService spaceService = getComponent(SpaceService.class);
    assertNotNull(spaceService);
  }

  @Test
  public void testAddSpace() throws Exception {

    SpaceService spaceService = getComponent(SpaceService.class);
    Space space1 = new Space();
    space1.setApp("Calendar;FileSharing");
    space1.setGroupId("Group1");
    space1.setName("Space1");

    Space space2 = new Space();
    space2.setApp("Contact,Forum");
    space2.setGroupId("Group2");
    space2.setParent(space1.getId());
    space2.setName("Space2");

    spaceService.saveSpace(space1, true);
    spaceService.saveSpace(space2, true);

    assertEquals(spaceService.getAllSpaces().size(), 2);

    Space space3 = spaceService.getSpaceById(space1.getId());
    assertNotNull(space3);
    assertEquals(space1.getId(), space3.getId());
    assertNotSame(space3.getApp(), "Calendar");
    assertEquals(space3.getGroupId(), "Group1");
  }

  @Test
  public void testGetSpace() throws Exception {
    SpaceService spaceService = getComponent(SpaceService.class);
    Space space = new Space();
    space.setApp("Calendar");
    space.setGroupId("Group1");
    space.setName("calendar");
    spaceService.saveSpace(space, true);
    Space s = spaceService.getSpaceById(space.getId());
    assertNotNull(s);
    assertEquals(3, spaceService.getAllSpaces().size());
    assertEquals("Calendar", s.getApp());
    assertEquals("Group1", s.getGroupId());
  }

}
