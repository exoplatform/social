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
package org.exoplatform.social.service.rest;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManagerImpl;
import org.exoplatform.social.core.manager.RelationshipManagerImpl;
import org.exoplatform.social.core.space.impl.SpaceServiceImpl;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class NotificationsRestServiceTest extends AbstractResourceTest {

  static private NotificationsRestService notificationsRestService;
  
  private IdentityStorage identityStorage;
  private ActivityManagerImpl activityManager;
  private SpaceServiceImpl spaceService;
  private RelationshipManagerImpl relationshipManager;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityManager = (ActivityManagerImpl) getContainer().getComponentInstanceOfType(ActivityManagerImpl.class);
    spaceService = (SpaceServiceImpl) getContainer().getComponentInstanceOfType(SpaceServiceImpl.class);
    relationshipManager = (RelationshipManagerImpl) getContainer().getComponentInstanceOfType(RelationshipManagerImpl.class);
    
    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");
    
    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    notificationsRestService = new NotificationsRestService();
    registry(notificationsRestService);
  }

  public void tearDown() throws Exception {
    
    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);
    
    super.tearDown();

    unregistry(notificationsRestService);
  }

  public void testJsonRightLink() throws Exception {
    assertNotNull(notificationsRestService);
  }
  
  public void testInviteToConnect() throws Exception {
    ContainerResponse response = service("POST", "/social/notifications/inviteToConnect/" + rootIdentity.getRemoteId() +"/" + johnIdentity.getRemoteId(), "", null, null);
    assertNotNull(response);
    //assertEquals(200, response.getStatus());
  }
}
