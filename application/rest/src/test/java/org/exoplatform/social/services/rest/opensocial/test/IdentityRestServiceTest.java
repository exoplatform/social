/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.services.rest.opensocial.test;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.services.rest.AbstractResourceTest;
import org.exoplatform.social.services.rest.opensocial.IdentityRestService;
import org.exoplatform.social.services.rest.opensocial.IdentityRestService.UserId;

/**
 * IdentityRestServiceTest.java
 *
 * @author     <a href="http://hoatle.net">hoatle</a>
 * @since      Mar 2, 2010
 * @copyright  eXo Platform SAS 
 */
public class IdentityRestServiceTest extends AbstractResourceTest {
  static private PortalContainer container;
  static private IdentityRestService identityRestService;
  static private IdentityManager identityManager;
  static private String rootId, johnId;
  static private final String PROVIDER_ID = "organization";
  
  public void setUp() throws Exception {
    super.setUp();
    container = PortalContainer.getInstance();
    identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    identityRestService = new IdentityRestService();
    registry(identityRestService);
    
    rootId = identityManager.getIdentityByRemoteId(PROVIDER_ID, "root").getId();
    johnId = identityManager.getIdentityByRemoteId(PROVIDER_ID, "john").getId();
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    
    unregistry(identityRestService);
  }
  
  public void testGetRootId() throws Exception {
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "/social/identity/root/id/show.json", "", null, null, writer);
    UserId returnedUserId = (UserId) response.getEntity();
    assertEquals(200, response.getStatus());
    assertEquals("application/json", response.getContentType().toString());
    assertEquals(rootId, returnedUserId.getId());
  }
  
  public void testGetJohnId() throws Exception {
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "/social/identity/john/id/show.json", "", null, null, writer);
    UserId returnedUserId = (UserId) response.getEntity();
    assertEquals(200, response.getStatus());
    assertEquals("application/json", response.getContentType().toString());
    assertEquals(johnId, returnedUserId.getId());
  }
}
