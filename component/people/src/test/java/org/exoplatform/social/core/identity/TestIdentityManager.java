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
package org.exoplatform.social.core.identity;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.test.BasicPeopleServiceTestCase;

public class TestIdentityManager extends  BasicPeopleServiceTestCase{ 
  
  public TestIdentityManager() throws Exception {
	super();
	// TODO Auto-generated constructor stub
  }

  private IdentityManager identityManager;

  public void setUp() throws Exception {
	super.setUp();	
	identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
  }
  
  public void testIdentityManager() {
	assertNotNull(identityManager);
  }

  public void testGetIdentityByRemoteId() throws Exception {
    Identity identity = identityManager.getOrCreateIdentity("organization", "john");
    assertNotNull(identity);
    //assertEquals("john", identity.getProfile().getNickname());
    //assertEquals("john", identity.getDisplayName());

    identityManager.saveIdentity(identity);
    assertNotNull(identity.getId());

    //check if we load it a second time if we get the ID
    identity = identityManager.getOrCreateIdentity("organization", "john");
    assertNotNull(identity);
    assertNotNull("This object should have an id since it has been saved", identity.getId());

    String id = identity.getId();
    identityManager.saveIdentity(identity);
    assertEquals("The id should not change after having been saved", id, identity.getId());
  }

  public void testGetIdentityById() throws Exception {

    Identity identity = identityManager.getOrCreateIdentity("organization", "mary");
    assertNotNull(identity);

    assertNotNull(identity.getId());
    identityManager.saveIdentity(identity);
    assertNotNull(identity.getId());

    String oldId = identity.getId();
    identity = identityManager.getIdentityById(identity.getId());
    assertNotNull(identity);
    assertEquals("this id should still be the same", oldId, identity.getId());
    
  }
  
  public void testGetIdentityByIdWithGlobalId() throws Exception {
    Identity demo = identityManager.getOrCreateIdentity("organization", "demo");
    identityManager.saveIdentity(demo);
    String demoId = demo.getId();

    Identity identity = identityManager.getIdentityById("organization:demo"); 
    assertNotNull(identity);
    String id = identity.getId();
    assertNotNull(id);
    assertEquals("ids should be identical", demoId, id);

    identity = identityManager.getIdentityById(identity.getId());

  }

  public void testGetWrongId() throws Exception {
    Identity identity = identityManager.getOrCreateIdentity("organization", "jack");
    assertNull(identity);

    identity = identityManager.getIdentityById("wrongID");
    assertNull(identity);
  }

}