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
package org.exoplatform.social.core.service;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class LinkProviderTest extends AbstractCoreTest {

  public void testGetProfileLink() throws Exception {
    IdentityManager identityManger = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    Identity rootIdentity = identityManger.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    String rootFullName = rootIdentity.getProfile().getFullName();
    assertNotNull("rootFullName must not be null.", rootFullName);
    LinkProvider provider = (LinkProvider) getContainer().getComponentInstanceOfType(LinkProvider.class);
    // but when we have the identity we generate a link
    String actualLink = provider.getProfileLink(rootIdentity.getRemoteId());
    String expected =  "<a href=\"/portal/private/classic/profile/" +
                        rootIdentity.getRemoteId()+"\" target=\"_parent\">"+rootFullName+"</a>";
    assertEquals(expected, actualLink);
  }

}
