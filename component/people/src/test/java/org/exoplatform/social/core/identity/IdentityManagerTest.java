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

import org.exoplatform.social.AbstractPeopleTest;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;

public class IdentityManagerTest extends AbstractPeopleTest {

  public IdentityManagerTest() throws Exception {
    super();
  }

  private IdentityManager identityManager;

  public void setUp() throws Exception {
    super.setUp();
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    begin();
  }

  public void tearDown() throws Exception {
    end();
  }

  // protected void afterContainerStart() {
  // identityManager = (IdentityManager)
  // getContainer().getComponentInstanceOfType(IdentityManager.class);
  // SimpleMockOrganizationService organizationService =
  // (SimpleMockOrganizationService)
  // getContainer().getComponentInstanceOfType(OrganizationService.class);
  // organizationService.addMemberships("john", "member:/platform/users");
  // organizationService.addMemberships("demo", "member:/platform/users");
  // organizationService.addMemberships("mary", "member:/platform/users");
  // }

  public void testIdentityManager() {
    assertNotNull(identityManager);
  }

  public void testGetIdentityByRemoteId() throws Exception {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    assertNotNull(identity);

    identityManager.saveIdentity(identity);
    assertNotNull(identity.getId());

    // check if we load it a second time if we get the ID
    identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    assertNotNull(identity);
    assertNotNull("This object should have an id since it has been saved", identity.getId());

    String id = identity.getId();
    identityManager.saveIdentity(identity);
    assertTrue("The id should not change after having been saved", identity.getId().equals(id));
  }

  public void testGetIdentityById() throws Exception {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    assertNotNull(identity);

    assertNotNull(identity.getId());
    identityManager.saveIdentity(identity);
    assertNotNull(identity.getId());

    String oldId = identity.getId();
    identity = identityManager.getIdentity(identity.getId());
    assertNotNull(identity);
    log.info("new id = " + identity.getId());

    String retrievedId = identity.getId();
    System.out.println(retrievedId);
    assertTrue("this id should still be the same", retrievedId.equals(oldId));

  }

  public void testGetIdentityByIdWithGlobalId() throws Exception {
    Identity demo = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");
    identityManager.saveIdentity(demo);
    String demoId = demo.getId();

    Identity identity = identityManager.getIdentity("organization:demo");
    assertNotNull(identity);
    String id = identity.getId();
    assertNotNull(id);
    assertTrue("ids should be identical", id.equals(demoId));

    identity = identityManager.getIdentity(identity.getId());
  }

  public void testGetWrongId() throws Exception {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "jack");
    assertNotNull(identity);

    identity = identityManager.getIdentity("wrongID");
    assertNull(identity);
  }
}
