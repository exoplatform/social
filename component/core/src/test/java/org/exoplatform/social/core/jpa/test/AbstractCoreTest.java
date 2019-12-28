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

package org.exoplatform.social.core.jpa.test;

import junit.framework.AssertionFailedError;
import org.apache.commons.lang.ArrayUtils;

import org.exoplatform.commons.persistence.impl.EntityManagerHolder;
import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileSearchConnector;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.webui.exception.MessageException;

import org.jboss.byteman.contrib.bmunit.BMUnit;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.common.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.application.registry.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/component.search.configuration.xml"),
})
public abstract class AbstractCoreTest extends BaseExoTestCase {
  protected final Log LOG = ExoLogger.getLogger(AbstractCoreTest.class);
  protected SpaceService spaceService;
  protected IdentityManager identityManager;
  protected RelationshipManager relationshipManager;
  protected ActivityManager activityManager;
  protected ActivityStorage activityStorage;

  protected ProfileSearchConnector mockProfileSearch = Mockito.mock(ProfileSearchConnector.class);

  @Override
  protected void setUp() throws Exception {
    begin();
    
    identityManager = getService(IdentityManager.class);
    activityManager =  getService(ActivityManager.class);
    activityStorage = getService(ActivityStorage.class);
    relationshipManager = getService(RelationshipManager.class);
    spaceService = getService(SpaceService.class);

    deleteAllRelationships();
    deleteAllSpaces();
    deleteAllIdentitiesWithActivities();
  }

  @Override
  protected void tearDown() throws Exception {
    EntityManagerHolder.get().clear();
    deleteAllRelationships();
    deleteAllSpaces();
    deleteAllIdentitiesWithActivities();

    //
    end();
  }  
  
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }
  
  
//Fork from Junit 3.8.2
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
        ListAccess<User> groupMembersAccess = org.getUserHandler().findUsersByGroupId(invitedGroupId);
        List<User> users = Arrays.asList(groupMembersAccess.load(0, groupMembersAccess.getSize()));

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
      spaceService.createSpace(space, creator);
    } catch (Exception e) {
      LOG.warn("Error while saving space", e);
    }
    return space;
  }

  protected void loginUser(String userId) {
    MembershipEntry membershipEntry = new MembershipEntry("/platform/user", "*");
    Collection<MembershipEntry> membershipEntries = new ArrayList<MembershipEntry>();
    membershipEntries.add(membershipEntry);
    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity(userId, membershipEntries);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  protected void sleep(int millis) {
    try {
      LOG.info("Wait {} ms!", millis);
      Thread.sleep(millis);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  protected Identity createUserAndIdentity(String username) throws Exception {
    OrganizationService organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.findUserByName(username);
    if (user == null) {
      user = userHandler.createUserInstance(username);
      user.setFirstName(username);
      user.setLastName(username);
      user.setEmail(username + "@test.com");
      userHandler.createUser(user, true);
    }
    return createIdentity(username);
  }

  protected Identity createIdentity(String username) {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, username);
    profile.setProperty(Profile.LAST_NAME, username);
    identity.setProfile(profile);
    identityManager.saveProfile(profile);
    if(identity.isDeleted() || !identity.isEnable()) {
      identity.setDeleted(false);
      identity.setEnable(true);
      identity = identityManager.updateIdentity(identity);
    }

    return identity;
  }

  protected Identity createIdentity(String username, String email) throws MessageException {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, username);
    profile.setProperty(Profile.LAST_NAME, username);
    identity.setProfile(profile);
    if(identity.isDeleted() || !identity.isEnable()) {
      identity.setDeleted(false);
      identity.setEnable(true);
      identity.getProfile().setProperty(Profile.EMAIL, email);
      identity = identityManager.updateIdentity(identity);
      identityManager.updateProfile(profile);
      identity.setProfile(profile);
    }

    return identity;
  }

  protected void deleteAllIdentitiesWithActivities() throws Exception {
    ListAccess<org.exoplatform.social.core.identity.model.Identity> organizationIdentities = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, new ProfileFilter(), false);
    Arrays.stream(organizationIdentities.load(0, organizationIdentities.getSize()))
            .forEach(identity -> {
              RealtimeListAccess<ExoSocialActivity> identityActivities = activityManager.getActivitiesWithListAccess(identity);
              Arrays.stream(identityActivities.load(0, identityActivities.getSize()))
                      .forEach(activity -> activityManager.deleteActivity(activity));
              identityManager.deleteIdentity(identity);
            });

    ListAccess<Identity> spaceIdentities = identityManager.getIdentitiesByProfileFilter(SpaceIdentityProvider.NAME, new ProfileFilter(), false);
    Arrays.stream(spaceIdentities.load(0, spaceIdentities.getSize()))
            .forEach(identity -> {
              RealtimeListAccess<ExoSocialActivity> identityActivities = activityManager.getActivitiesOfSpaceWithListAccess(identity);
              Arrays.stream(identityActivities.load(0, identityActivities.getSize()))
                      .forEach(activity -> activityManager.deleteActivity(activity));
              identityManager.deleteIdentity(identity);
            });
  }

  protected void deleteAllSpaces() throws Exception {
    SpaceService spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    ListAccess<Space> spaces = spaceService.getAllSpacesWithListAccess();
    Arrays.stream(spaces.load(0, spaces.getSize())).forEach(space -> spaceService.deleteSpace(space));
  }

  protected void deleteAllRelationships() throws Exception {
    RelationshipManager relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);
    IdentityManager identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    ListAccess<org.exoplatform.social.core.identity.model.Identity> identities = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, new ProfileFilter(), true);
    for(org.exoplatform.social.core.identity.model.Identity identity : identities.load(0, identities.getSize())) {
      ListAccess<Identity> relationships = relationshipManager.getAllWithListAccess(identity);
      Arrays.stream(relationships.load(0, relationships.getSize()))
              .forEach(relationship -> relationshipManager.deny(identity, relationship));
    }
  }

  public static void persist() {
    RequestLifeCycle.end();
    RequestLifeCycle.begin(PortalContainer.getInstance());
  }

}
