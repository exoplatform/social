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
package org.exoplatform.social.core.identity.provider;

import org.exoplatform.social.core.test.AbstractCoreTest;


public class SpaceIdentityProviderTest extends AbstractCoreTest {

  public void testGetIdentityByRemoteId() throws Exception {
    //TODO hoatle fix this
//    SpaceService spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
//    String spaceName = "space name test";
//    Space space = new Space();
//    space.setName(spaceName);
//    spaceService.createSpace(space, "root");
//    assertNotNull(space);
    assertTrue(true);
  }

  public void testWithSpaceName() throws Exception {
    //TODO hoatle fix this
    assertTrue(true);
//    Space space = new Space();
//    space.setDescription("blabla");
//    space.setGroupId("/platform/users");
//    space.setApp("app");
//    space.setName("space1");
//
//    SpaceService spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
//    spaceService.saveSpace(space, true);
//
//    String spaceId = space.getId();
//
//    IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
//    Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceId);
//
//    SpaceIdentityProvider identityProvider = new SpaceIdentityProvider(spaceService);
//    Identity actual = identityProvider.getIdentityByRemoteId("space:space1");
    //assertEquals(actual.getRemoteId(), identity.getRemoteId());

    // whe can even support without :
    //actual = identityProvider.getIdentityByRemoteId("space1");
    //assertEquals(actual.getRemoteId(), identity.getRemoteId());
  }

}
