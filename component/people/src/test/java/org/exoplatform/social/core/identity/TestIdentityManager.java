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

import static org.testng.Assert.*;

import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.commons.testing.mock.SimpleMockOrganizationService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.testng.annotations.Test;

@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.people.configuration.xml")})
public class TestIdentityManager extends  AbstractJCRTestCase{ 
  
  public TestIdentityManager() throws Exception {
	super();
  }

  private IdentityManager identityManager;

  
  protected void afterContainerStart() {
    identityManager = getComponent(IdentityManager.class);
    SimpleMockOrganizationService  organizationService = (SimpleMockOrganizationService) getComponent(OrganizationService.class);
    organizationService.addMemberships("john", "member:/platform/users");
    organizationService.addMemberships("demo", "member:/platform/users");    
    organizationService.addMemberships("mary", "member:/platform/users");   
  }

  @Test
  public void testIdentityManager() {
	assertNotNull(identityManager);
  }

  @Test
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
    assertEquals(identity.getId(), id, "The id should not change after having been saved");
  }

  @Test
  public void testGetIdentityById() throws Exception {

    Identity identity = identityManager.getOrCreateIdentity("organization", "mary");
    assertNotNull(identity);

    assertNotNull(identity.getId());
    identityManager.saveIdentity(identity);
    assertNotNull(identity.getId());

    String oldId = identity.getId();
    identity = identityManager.getIdentity(identity.getId());
    assertNotNull(identity);
    assertEquals(identity.getId(), oldId, "this id should still be the same");
    
  }
  
  @Test
  public void testGetIdentityByIdWithGlobalId() throws Exception {
    Identity demo = identityManager.getOrCreateIdentity("organization", "demo");
    identityManager.saveIdentity(demo);
    String demoId = demo.getId();

    Identity identity = identityManager.getIdentity("organization:demo"); 
    assertNotNull(identity);
    String id = identity.getId();
    assertNotNull(id);
    assertEquals(id, demoId,"ids should be identical");

    identity = identityManager.getIdentity(identity.getId());

  }

  @Test
  public void testGetWrongId() throws Exception {
    Identity identity = identityManager.getOrCreateIdentity("organization", "jack");
    assertNull(identity);

    identity = identityManager.getIdentity("wrongID");
    assertNull(identity);
  }

}