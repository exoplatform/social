/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.listeners;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SocialMembershipListenerImplTest extends AbstractCoreTest {
  private final Log  LOG = ExoLogger.getLogger(SocialMembershipListenerImplTest.class);

  private OrganizationService organizationService;

  public void setUp() throws Exception {
    super.setUp();
    organizationService = (OrganizationService) getContainer().getComponentInstanceOfType(OrganizationService.class);

    IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    Identity root = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);
    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "ghost", false);
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testPostSave() {
    Space sFoo = new Space();
    sFoo.setDisplayName("postsave");
    sFoo.setPrettyName("postsave");
    sFoo.setDescription("postsave description");
    sFoo.setManagers(new String[]{"root"});
    sFoo.setType(DefaultSpaceApplicationHandler.NAME);
    sFoo.setRegistration(Space.OPEN);
    sFoo = createSpaceNonInitApps(sFoo, "root", null);

    addUserToGroupWithMembership("demo", sFoo.getGroupId(), "manager");
    sFoo = spaceService.getSpaceById(sFoo.getId());
    assertEquals(2, sFoo.getManagers().length);
    assertEquals(2, sFoo.getMembers().length);

    addUserToGroupWithMembership("john", sFoo.getGroupId(), "member");
    sFoo = spaceService.getSpaceById(sFoo.getId());
    assertEquals(2, sFoo.getManagers().length);
    assertEquals(3, sFoo.getMembers().length);

    //* membership-type is treat like space manager
    addUserToGroupWithMembership("mary", sFoo.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
    sFoo = spaceService.getSpaceById(sFoo.getId());
    assertEquals(3, sFoo.getManagers().length);
    assertEquals(4, sFoo.getMembers().length);

    //validator membership will not be added as space member
    addUserToGroupWithMembership("ghost", sFoo.getGroupId(), "validator");
    sFoo = spaceService.getSpaceById(sFoo.getId());
    assertEquals(3, sFoo.getManagers().length);
    assertEquals(4, sFoo.getMembers().length);
  }

  public void testPostDelete() throws Exception {
    Space sFoo = new Space();
    sFoo.setDisplayName("postdelete");
    sFoo.setPrettyName("postdelete");
    sFoo.setDescription("postdelete description");
    sFoo.setManagers(new String[]{"root"});
    sFoo.setType(DefaultSpaceApplicationHandler.NAME);
    sFoo.setRegistration(Space.OPEN);
    sFoo = createSpaceNonInitApps(sFoo, "root", null);

    //* membership-type is treat like space manager
    addUserToGroupWithMembership("mary", sFoo.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
    sFoo = spaceService.getSpaceById(sFoo.getId());
    assertEquals(2, sFoo.getManagers().length);
    assertEquals(2, sFoo.getMembers().length);

    MembershipHandler membershipHandler = organizationService.getMembershipHandler();
    membershipHandler.removeMembershipByUser("mary", true);
    //
    sFoo = spaceService.getSpaceById(sFoo.getId());
    assertEquals(1, sFoo.getManagers().length);
    assertEquals(2, sFoo.getMembers().length);


    addUserToGroupWithMembership("john", sFoo.getGroupId(), "member");
    sFoo = spaceService.getSpaceById(sFoo.getId());
    assertEquals(1, sFoo.getManagers().length);
    assertEquals(3, sFoo.getMembers().length);
    //
    membershipHandler.removeMembershipByUser("john", true);
    sFoo = spaceService.getSpaceById(sFoo.getId());
    assertEquals(1, sFoo.getManagers().length);
    assertEquals(2, sFoo.getMembers().length);

    addUserToGroupWithMembership("john", sFoo.getGroupId(), "manager");
    sFoo = spaceService.getSpaceById(sFoo.getId());
    assertEquals(2, sFoo.getManagers().length);
    assertEquals(3, sFoo.getMembers().length);
    //
    membershipHandler.removeMembershipByUser("john", true);
    sFoo = spaceService.getSpaceById(sFoo.getId());
    assertEquals(1, sFoo.getManagers().length);
    assertEquals(2, sFoo.getMembers().length);
  }

}
