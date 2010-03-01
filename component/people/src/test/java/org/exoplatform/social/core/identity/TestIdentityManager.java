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

import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.social.core.test.PeopleServiceTestCase;

public class TestIdentityManager extends PeopleServiceTestCase {
  public TestIdentityManager() throws Exception {
	super();
	// TODO Auto-generated constructor stub
  }

  private IdentityManager identityManager;

  public void setUp() throws Exception {
		identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
		SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
		sProvider = sessionProviderService.getSystemSessionProvider(null) ;
  }
  
  public void testIdentityManager() {
	  assertNotNull(identityManager);
  }

  public void testGetIdentityByRemoteId() throws Exception {
    /*IdentityManager iManager = (IdentityManager) StandaloneContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull(iManager);
    //iManager.addIdentityProvider(new OrganizationIdentityProvider());

    Identity identity = iManager.getIdentityByRemoteId("organization", "john");
    assertNotNull(identity);
    //assertEquals("john", identity.getProfile().getNickname());
    assertEquals("john", identity.getDisplayName());

    iManager.saveIdentity(identity);
    assertNotNull(identity.getId());

    //check if we load it a second time if we get the ID
    identity = iManager.getIdentityByRemoteId("organization", "john");
    assertNotNull(identity);
    assertNotNull("This object should have an id since it has been saved", identity.getId());

    String id = identity.getId();
    iManager.saveIdentity(identity);
    assertEquals("The id should not change after having been saved", id, identity.getId());*/
  }

  public void testGetIdentityById() throws Exception {
    /*IdentityManager iManager = (IdentityManager) StandaloneContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
    //iManager.addIdentityProvider(new OrganizationIdentityProvider());

    Identity identity = iManager.getIdentityByRemoteId("organization", "james");
    assertNotNull(identity);

    assertNull(identity.getId());
    iManager.saveIdentity(identity);
    assertNotNull(identity.getId());

    String oldId = identity.getId();
    identity = iManager.getIdentityById(identity.getId());
    assertNotNull(identity);
    assertEquals("this id should still be the same", oldId, identity.getId());*/
  }

  public void testGetWrongId() throws Exception {
    /*IdentityManager iManager = (IdentityManager) StandaloneContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
    //iManager.addIdentityProvider(new OrganizationIdentityProvider());

    Identity identity = iManager.getIdentityByRemoteId("organization", "jack");
    assertNull(identity);

    identity = iManager.getIdentityById("wrongID");
    assertNull(identity);*/
  }

}