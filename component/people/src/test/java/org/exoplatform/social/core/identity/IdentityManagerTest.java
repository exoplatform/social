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
  private IdentityManager identityManager;

  @Override
  protected void beforeRunBare() throws Exception {
    super.beforeRunBare();
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
  }

  @Override
  protected void afterRunBare() {
    super.afterRunBare();
  }

  public void setUp() throws Exception {
    super.setUp();

    begin();
  }

  public void tearDown() throws Exception {
    end();
  }

  public void testIdentityManagerNotNull() {
    assertNotNull(identityManager);
  }

  public void testGetIdentityByRemoteId() throws Exception {
    String remoteId = "zuanoc";
    String providerId = OrganizationIdentityProvider.NAME;

    Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId);
    assertNotNull(identity);

    identityManager.saveIdentity(identity);
    String identityId = identity.getId();
    assertNotNull(identityId);

    //check if we load it a second time if we get the ID
    identity = identityManager.getOrCreateIdentity(providerId, remoteId);

    assertNotNull(identity);
    assertNotNull("This object should have an id since it has been saved", identity.getId());
    assertEquals("The id should not change after having been saved",identityId, identity.getId());
  }

  public void testGetIdentityById() throws Exception {
    String remoteId = "mary";
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId);
    assertNotNull(identity);
    assertNotNull(identity.getId());

    String oldId = identity.getId();
    Identity newIdentity = identityManager.getIdentity(oldId);
    assertNotNull(newIdentity);

    assertEquals("this id should still be the same",oldId, newIdentity.getId());
  }

  public void testGetIdentityByGlobalId() throws Exception {
    String remoteId = "demo";
    String providerId = OrganizationIdentityProvider.NAME;
    String globalId = providerId + ":" + remoteId;
    
    Identity demo = identityManager.getOrCreateIdentity(providerId, remoteId);
    identityManager.saveIdentity(demo);

    String demoId = demo.getId();
    Identity identityByGlobalId = identityManager.getIdentity(globalId);
    assertNotNull(identityByGlobalId);
    assertNotNull(identityByGlobalId.getId());
    assertEquals("ids should be identical",demoId, identityByGlobalId.getId());
  }

  public void testGetWrongId() throws Exception {
    String remoteId = "jack";
    String providerId = OrganizationIdentityProvider.NAME;
    Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId);
    assertNotNull(identity);

    identity = identityManager.getIdentity("wrongID");
    assertNull(identity);
  }

  public void testIsExistUser_false(){
    String remoteId = "zuanoc";
    String providerId = OrganizationIdentityProvider.NAME;
    final boolean isExist = identityManager.identityExisted(providerId, remoteId);
    assertFalse(isExist);
  }

  public void testIsExistUserTrue() throws Exception {
    //NOTE : we use root as remoteId here because root user is created in portal user system
    // and IdentityManager.identityExisted() just check a portal's user either exist or not..
    // ATTENTION : IdentityManager.identityExisted() is not depend on IdentityStorage, but only
    // on portal.
    String remoteId = "root";
    String providerId = OrganizationIdentityProvider.NAME;
    final boolean isExist = identityManager.identityExisted(providerId, remoteId);
    assertTrue(isExist);
  }
}