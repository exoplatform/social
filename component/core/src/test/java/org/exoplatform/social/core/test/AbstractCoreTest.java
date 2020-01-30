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
package org.exoplatform.social.core.test;

import java.lang.reflect.*;
import java.util.*;

import org.apache.commons.lang.ArrayUtils;

import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.commons.utils.*;
import org.exoplatform.component.test.*;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.security.*;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.cache.CachedSpaceStorage;

import junit.framework.AssertionFailedError;

/**
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Jul 6, 2010
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-dependencies-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-local-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-configuration.xml"),
})
public abstract class AbstractCoreTest extends BaseExoTestCase {

  public static boolean wantCount = false;
  private static int count;
  private int maxQuery;
  private final Log LOG = ExoLogger.getLogger(AbstractCoreTest.class);

  protected SpaceService spaceService;
  protected IdentityManager identityManager;
  
  
  @Override
  protected void setUp() throws Exception {
    begin();

    // If is query number test, init byteman
//    if (getClass().isAnnotationPresent(QueryNumberTest.class)) {
//      count = 0;
//      maxQuery = 0;
//      BMUnit.loadScriptFile(getClass(), "queryCount", "src/test/resources");
//    }

    //
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);

    deleteAllSpaces();
  }

  @Override
  protected void tearDown() throws Exception {
    deleteAllSpaces();

    wantCount = false;
    end();
  }

  protected void deleteAllSpaces() throws SpaceException {
    List<Space> allSpaces = spaceService.getAllSpaces();
    if(allSpaces != null && !allSpaces.isEmpty()) {
      for (Space space : allSpaces) {
        try {
          spaceService.deleteSpace(space);
          end();
          begin();
          LOG.warn("The space " + space.getDisplayName() + " wasn't cleaned up properly");
        } catch (Throwable e) {
          // The space is already deleted
        }
      }
    }

    CachedSpaceStorage spaceStorage = (CachedSpaceStorage) CommonsUtils.getService(SpaceStorage.class);
    spaceStorage.clearCaches();
  }

  protected <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }

  // Fork from Junit 3.8.2
  @Override
  /**
   * Override to run the test and assert its state.
   * @throws Throwable if any exception is thrown
   */
  protected void runTest() throws Throwable {
    String fName = getName();
    assertNotNull("TestCase.fName cannot be null", fName); // Some VMs crash when calling getMethod(null,null);
    Method runMethod= null;
    try {
      // use getMethod to get all public inherited
      // methods. getDeclaredMethods returns all
      // methods of this class but excludes the
      // inherited ones.
      runMethod= getClass().getMethod(fName, (Class[])null);
    } catch (NoSuchMethodException e) {
      fail("Method \""+fName+"\" not found");
    }
    if (!Modifier.isPublic(runMethod.getModifiers())) {
      fail("Method \""+fName+"\" should be public");
    }

    try {
//      MaxQueryNumber queryNumber = runMethod.getAnnotation(MaxQueryNumber.class);
//      if (queryNumber != null) {
//        wantCount = true;
//        maxQuery = queryNumber.value();
//      }
      runMethod.invoke(this);
    }
    catch (InvocationTargetException e) {
      e.fillInStackTrace();
      throw e.getTargetException();
    }
    catch (IllegalAccessException e) {
      e.fillInStackTrace();
      throw e;
    }

    if (wantCount && count > maxQuery) {
      throw new AssertionFailedError(""+ count + " JDBC queries was executed but the maximum is : " + maxQuery);
    }
    
  }

  protected void startSessionAs(String user) {
    Identity identity = new Identity(user, Collections.EMPTY_LIST);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  protected void startSessionAs(String user, Collection<MembershipEntry> memberships) {
    Identity identity = new Identity(user, memberships);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  // Called by byteman
  public static void count() {
    ++count;
   }

  /**
   * Creates new space with out init apps.
   *
   * @param space
   * @param creator
   * @param invitedGroupId
   * @return
   * @since 1.2.0-GA
   */
  protected Space createSpaceNonInitApps(Space space, String creator, String invitedGroupId) {
    // Creates new space by creating new group
    String groupId = null;
    try {
      groupId = SpaceUtils.createGroup(space.getDisplayName(), creator);
    } catch (SpaceException e) {
      LOG.error("Error while creating group", e);
    }

    if (invitedGroupId != null) {
      // Invites user in group join to new created space.
      // Gets users in group and then invites user to join into space.
      OrganizationService org = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
      try {
        PageList<User> groupMembersAccess = org.getUserHandler().findUsersByGroup(invitedGroupId);
        List<User> users = groupMembersAccess.getAll();

        for (User user : users) {
          String userId = user.getUserName();
          if (!userId.equals(creator)) {
            String[] invitedUsers = space.getInvitedUsers();
            if (!ArrayUtils.contains(invitedUsers, userId)) {
              invitedUsers = (String[]) ArrayUtils.add(invitedUsers, userId);
              space.setInvitedUsers(invitedUsers);
            }
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to invite users from group " + invitedGroupId, e);
      }
    }
    String[] managers = new String[] { creator };
    space.setManagers(managers);
    space.setGroupId(groupId);
    space.setUrl(space.getPrettyName());
    try {
      spaceService.saveSpace(space, true);
    } catch (SpaceException e) {
      LOG.warn("Error while saving space", e);
    }
    return space;
  }

  protected org.exoplatform.social.core.identity.model.Identity createOrUpdateIdentity(String remoteId) {
    org.exoplatform.social.core.identity.model.Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId);
    identity.setDeleted(false);
    identity.setEnable(true);
    identityManager.updateIdentity(identity);
    return identity;
  }

  protected void addUserToGroupWithMembership(String remoteId, String groupId, String membership) {
    OrganizationService organizationService = SpaceUtils.getOrganizationService();
    try {
      MembershipHandler membershipHandler = organizationService.getMembershipHandler();
      Membership found = membershipHandler.findMembershipByUserGroupAndType(remoteId, groupId, membership);
      if (found != null) {
        return;
      }
      User user = organizationService.getUserHandler().findUserByName(remoteId);
      MembershipType membershipType = organizationService.getMembershipTypeHandler().findMembershipType(membership);
      GroupHandler groupHandler = organizationService.getGroupHandler();
      Group existingGroup = groupHandler.findGroupById(groupId);
      membershipHandler.linkMembership(user, existingGroup, membershipType, true);
    } catch (Exception e) {
      return;
    }
  }
}
