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

import javax.jcr.Session;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.social.space.test.SpaceServiceTestCase;
import org.exoplatform.test.BasicTestCase;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * September 3, 2008          
 */

public class TestSpaceService extends SpaceServiceTestCase {  

  private SpaceService spaceService;
  
  public TestSpaceService() throws Exception {	  
	super();
	// TODO Auto-generated constructor stub
  }
  
  public void setUp() throws Exception {
	spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
	SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
	sProvider = sessionProviderService.getSystemSessionProvider(null) ;
  }
  
  public void testGetAllSpaces() throws Exception{
    assertNotNull(spaceService) ;
  }
  
  public void testAddSpace() throws Exception {
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
    
    assertEquals(2, spaceService.getAllSpaces().size());
    
    Space space3 = spaceService.getSpaceById(space1.getId());
    assertNotNull(space3);
    assertEquals(space1.getId(), space3.getId());
    assertNotSame("Calendar", space3.getApp());
    assertEquals("Group1",space3.getGroupId());
  }
  
  public void testGetSpace() throws Exception {
    Space space = new Space();
    space.setApp("Calendar");
    space.setGroupId("Group1");
    space.setName("calendar");
    spaceService.saveSpace(space, true);
    
    Space s = spaceService.getSpaceById(space.getId());
    assertNotNull(s);
    assertEquals(3,spaceService.getAllSpaces().size());
    assertEquals("Calendar", s.getApp());
    assertEquals("Group1", s.getGroupId());
  }
  
}