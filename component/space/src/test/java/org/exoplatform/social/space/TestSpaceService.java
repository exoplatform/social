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
import org.exoplatform.test.BasicTestCase;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * September 3, 2008          
 */

public class TestSpaceService extends BasicTestCase {
  private SpaceService spaceService;
  protected Session session;
  protected ManageableRepository repository;
  protected RepositoryService repositoryService;
  protected StandaloneContainer container;
  
  
  
  public void setUp() throws Exception {
    StandaloneContainer.addConfigurationPath("src/test/java/conf/portal/test-configuration.xml");

    container = StandaloneContainer.getInstance();
    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "src/test/java/conf/portal/login.conf");
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    repository = repositoryService.getDefaultRepository();

    SessionProviderService spService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider sessionProvider = spService.getSystemSessionProvider(null);
    session = sessionProvider.getSession("social", repository);
    
    spaceService = (SpaceService)container.getComponentInstanceOfType(SpaceService.class);
  }
  
  public void testGetAllSpaces() throws Exception{
    assertNotNull(spaceService) ;
  }
  
  public void testAddSpace() throws Exception {
    Space space1 = new Space();
    space1.setId("Space1");
    space1.setApp("Calendar;FileSharing");
    space1.setGroupId("Group1");
    
    Space space2 = new Space();
    space2.setId("Space2");
    space2.setApp("Contact,Forum");
    space2.setGroupId("Group2");
    space2.setParent(space1.getId());
    
    spaceService.saveSpace(space1, true);
    spaceService.saveSpace(space2, true);
    
    assertEquals(2, spaceService.getAllSpaces().size());
    
    Space space3 = spaceService.getSpace(space1.getId());
    assertNotNull(space3);
    assertEquals("Space1", space3.getId());
    assertNotSame("Calendar", space3.getApp());
    assertEquals("Group1",space3.getGroupId());
    
    Space space4 = spaceService.getSpace(space2.getId());
    assertNotNull(space4);
    assertEquals(space1.getId(),space4.getParent());
    
  }
  
  public void testGetSpace() throws Exception {
    Space space = new Space();
    space.setApp("Calendar");
    space.setGroupId("Group1");
    spaceService.saveSpace(space, true);
    
    Space s = spaceService.getSpace(space.getId());
    assertNotNull(s);
    assertEquals(3,spaceService.getAllSpaces().size());
    assertEquals("Calendar", s.getApp());
    assertEquals("Group1", s.getGroupId());
  }
  
  @Override
  protected void tearDown() throws Exception {
//    if (session != null) {
//      Node node = null;
//      try {
//        session.refresh(false);
//
//        Node rootNode = session.getRootNode();
//
//        if (rootNode.getNode("exo:applications").hasNode("Social_Space")) {
//          NodeIterator children = rootNode.getNode("exo:applications").getNode("Social_Space").getNode("Space").getNodes();
//          while (children.hasNext()) {
//            node = children.nextNode();
//            System.out.println("DELETing Social_Space ------------- " + node.getPath());
//            node.remove();
//          }
//        }
//
//        session.save();
//        session.refresh(false);
//
//      } catch (Exception e) {
//          e.printStackTrace();
//      } finally {
//        session.logout();
//      }
//    }
    super.tearDown();
  }
  
}