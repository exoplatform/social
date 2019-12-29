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
package org.exoplatform.social.core.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.Application;
import org.exoplatform.social.core.identity.provider.FakeIdentityProvider;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 * Unit Tests for {@link IdentityManager}
 *
 * @author hoat_le
 */
// TODO :
// * Fix tests to not have to specify the order of execution like this
// * The order of tests execution changed in Junit 4.11 (https://github.com/KentBeck/junit/blob/master/doc/ReleaseNotes4.11.md)
@FixMethodOrder(MethodSorters.JVM)
public class IdentityManagerTest extends AbstractCoreTest {

  private IdentityManager identityManager;

  private List<Space> tearDownSpaceList;
  private List<Identity>  tearDownIdentityList;

  private ActivityManager activityManager;
  private SpaceService spaceService;
  private UserHandler userHandler;
  
  public void setUp() throws Exception {
    super.setUp();
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull(identityManager);
    
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    assertNotNull(spaceService);

    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull(activityManager);

    userHandler = SpaceUtils.getOrganizationService().getUserHandler();
    
    tearDownIdentityList = new ArrayList<Identity>();
    tearDownSpaceList = new ArrayList<Space>();
    org.exoplatform.services.security.Identity identity = getService(IdentityRegistry.class).getIdentity("root");
    ConversationState.setCurrent(new ConversationState(identity));
  }

  public void tearDown() throws Exception {
    for (Identity identity : tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
    }
    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
      if (spaceIdentity != null) {
        identityManager.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }
    super.tearDown();
  }

  /**
   * Test
   * {@link IdentityManager#registerIdentityProviders(org.exoplatform.social.core.identity.IdentityProviderPlugin)}
   */
  public void testRegisterIdentityProviders() {
    // TODO hoatle complete testRegisterIdentityProviders()
    assert true;
  }

  /**
   * Test {@link IdentityManager#saveIdentity(Identity)}
   */
  public void testSaveIdentity() {
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, "identity1");
    identityManager.saveIdentity(tobeSavedIdentity);

    assertNotNull(tobeSavedIdentity.getId());

    //final String updatedRemoteId = "identity-updated";

    //tobeSavedIdentity.setRemoteId(updatedRemoteId);

    //identityManager.saveIdentity(tobeSavedIdentity);

    //Identity gotIdentity = identityManager.getIdentity(tobeSavedIdentity.getId());

    //assertEquals(updatedRemoteId, gotIdentity.getRemoteId());

    tearDownIdentityList.add(tobeSavedIdentity);
  }

  /**
   * Test {@link IdentityManager#getIdentity(String)}
   */
//FIXME regression JCR to RDBMS migration
//  public void testGetIdentityById() {
//    final String username = "root";
//    Identity foundIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username,
//            true);
//
//    // Gets Identity By Node Id
//    {
//      Identity gotIdentity = identityManager.getIdentity(foundIdentity.getId(), true);
//
//      assertNotNull(gotIdentity);
//      assertEquals(foundIdentity.getId(), gotIdentity.getId());
//      assertEquals("gotIdentity.getProviderId() must return: " + OrganizationIdentityProvider.NAME,
//                   OrganizationIdentityProvider.NAME,
//                   gotIdentity.getProviderId());
//      assertEquals("gotIdentity.getRemoteId() must return: " + username,
//                   username,
//                   gotIdentity.getRemoteId());
//      // By default, when getIdentity(String nodeId) will have load profile by
//      // default (means saved).
//      assertNotNull("gotIdentity.getProfile().getId() must not return: null",
//                    gotIdentity.getProfile().getId());
//      
//      assertNotNull("gotIdentity.getProfile().getProperty(Profile.FIRST_NAME) must not be null", gotIdentity.getProfile().getProperty(Profile.FIRST_NAME));
//      assertFalse("gotIdentity.getProfile().getFullName().isEmpty() must be false", gotIdentity.getProfile().getFullName().isEmpty());
//
//
//    }
//
//    tearDownIdentityList.add(identityManager.getIdentity(foundIdentity.getId(), false));
//  }

  /**
   * 
   * @throws Exception
   */
  public void testGetSpaceMembers() throws Exception {
    
    Identity demoIdentity = populateIdentity("demo");
    Identity johnIdentity = populateIdentity("john");
    Identity maryIdentity = populateIdentity("mary");
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] {demoIdentity.getRemoteId()};
    String[] members = new String[] {demoIdentity.getRemoteId()};
    String[] invitedUsers = new String[] {};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);
    
    space = this.createSpaceNonInitApps(space, demoIdentity.getRemoteId(), null);

    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    //add member to space
    spaceService.addMember(savedSpace, johnIdentity.getRemoteId());
    spaceService.addMember(savedSpace, maryIdentity.getRemoteId());

    {
      ProfileFilter profileFilter = new ProfileFilter();
      ListAccess<Identity> spaceMembers = identityManager.getSpaceIdentityByProfileFilter(savedSpace, profileFilter, Type.MEMBER, true);
      assertEquals(3, spaceMembers.getSize());
    }
    
    //remove member to space
    spaceService.removeMember(savedSpace, johnIdentity.getRemoteId());
    {
      ProfileFilter profileFilter = new ProfileFilter();
      ListAccess<Identity> got = identityManager.getSpaceIdentityByProfileFilter(savedSpace, profileFilter, Type.MEMBER, true);
      assertEquals(2, got.getSize());
    }
    
    //clear space
    tearDownSpaceList.add(savedSpace);

  }

  /**
   * Test {@link IdentityManager#getIdentity(String, boolean)}
   */
  public void testGetIdentityByIdWithLoadProfile() {

    final String username = "root";
    Identity tobeSavedIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username,
            true);
    // loadProfile=false for identityId = uuid
    {
      Identity gotIdentity = identityManager.getIdentity(tobeSavedIdentity.getId(), false);

      assertNotNull(gotIdentity);
      assertEquals(tobeSavedIdentity.getId(), gotIdentity.getId());
      assertEquals("gotIdentity.getProviderId() must return: " + OrganizationIdentityProvider.NAME,
                   OrganizationIdentityProvider.NAME,
                   gotIdentity.getProviderId());
      assertEquals("gotIdentity.getRemoteId() must return: " + username,
                   username,
                   gotIdentity.getRemoteId());
      // does not load profile

      // assertNull("gotIdentity.getProfile().getId() must return: null",
      // gotIdentity.getProfile().getId());
    }
    // loadProfile=false for identityId = globalId
    /*
    {
      // With the case of OrganizationIdentityProvider, make sure remoteId
      // already exists in OrganizationService
      GlobalId globalId = new GlobalId(OrganizationIdentityProvider.NAME + GlobalId.SEPARATOR
          + username);
      Identity gotIdentity2 = identityManager.getIdentity(globalId.toString(), false);
      // "root" is found on OrganizationIdentityProvider (OrganizationService)
      assertNotNull("gotIdentity2 must not be null", gotIdentity2);
      assertNotNull("gotIdentity2.getId() must not be null", gotIdentity2.getId());
      // assertEquals("gotIdentity2.getId() must be: " + globalId.toString(),
      // globalId.toString(), gotIdentity2.getId());
      assertEquals("gotIdentity2.getProviderId() must return: " + OrganizationIdentityProvider.NAME,
                   OrganizationIdentityProvider.NAME,
                   gotIdentity2.getProviderId());
      assertEquals("gotIdentity2.getRemoteId() must return: " + username,
                   username,
                   gotIdentity2.getRemoteId());
       //Cached
      //assertNull("gotIdentity2.getProfile().getId() must return: null", gotIdentity2.getProfile()
                                                                                    //.getId());
    }
    */

    tearDownIdentityList.add(identityManager.getIdentity(tobeSavedIdentity.getId(), false));

  }

  /**
   * Test {@link IdentityManager#deleteIdentity(Identity)}
   */
  public void testDeleteIdentity() {
    final String username = "demo";
    Identity tobeSavedIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username,
            false);

    assertNotNull("tobeSavedIdentity.getId() must not be null", tobeSavedIdentity.getId());

    assertNotNull("tobeSavedIdentity.getProfile().getId() must not be null",
            tobeSavedIdentity.getProfile().getId());

    identityManager.deleteIdentity(tobeSavedIdentity);

//    assertNull("identityManager.getIdentity(tobeSavedIdentity.getId() must return null",
//               identityManager.getIdentity(tobeSavedIdentity.getId()));
    Identity gotIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
    assertNotNull("gotIdentity must not be null because " + username + " is in organizationService",
                  gotIdentity);
    assertNotNull("gotIdentity.getId() must not be null", gotIdentity.getId());
    // assertEquals("gotIdentity.getId() must be: " + globalId.toString(),
    // globalId.toString(), gotIdentity.getId());
    tearDownIdentityList.add(gotIdentity);
  }
  
  /**
   * Test {@link IdentityManager#deleteIdentity(Identity)}
   */
//FIXME regression JCR to RDBMS migration
//  public void testDisabledIdentity() throws Exception {
//    final String username = "demo";
//    Identity disabledIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
//    userHandler.setEnabled(username, false, true);
//    identityManager.processEnabledIdentity(username, false);
//    //
//    disabledIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
//    //
//    assertFalse(disabledIdentity.isDeleted());
//    assertFalse(disabledIdentity.isEnable());
//    
//    identityManager.deleteIdentity(disabledIdentity); 
//  }
  
  /**
   * Test {@link IdentityManager#deleteIdentity(Identity)}
   */
//FIXME regression JCR to RDBMS migration
//  public void testEnabledIdentity() throws Exception {
//    final String username = "demo";
//    Identity enbledIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
//    //disable demo
//    userHandler.setEnabled(username, false, true);
//    identityManager.processEnabledIdentity(username, false);
//    //
//    enbledIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
//    
//    //enable demo
//    userHandler.setEnabled(username, true, true);
//    identityManager.processEnabledIdentity(username, true);
//    
//    //
//    enbledIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
//    assertFalse(enbledIdentity.isDeleted());
//    assertTrue(enbledIdentity.isEnable());
//    
//    
//    identityManager.deleteIdentity(enbledIdentity);
//  }

  /**
   * Test
   * {@link IdentityManager#addIdentityProvider(org.exoplatform.social.core.identity.IdentityProvider)}
   */
  public void testAddIdentityProvider() {
    // TODO hoatle complete testAddIdentityProvider();
    assert true;
  }

  /**
   * Test {@link IdentityManager#getOrCreateIdentity(String, String)}
   */
//FIXME regression JCR to RDBMS migration
//  public void testGetOrCreateIdentity() {
//    final String username1 = "john";
//    final String username2 = "root";
//    Identity gotIdentity1;
//    Identity gotIdentity2;
//    // load profile = true
//    {
//      gotIdentity1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
//                                                         username1, true);
//      
//      Profile profile1 = gotIdentity1.getProfile();
//
//
//      assertNotNull("gotIdentity1.getId() must not be null", gotIdentity1.getId());
//      assertNotNull("profile1.getId() must not be null", profile1.getId());
//      assertNotNull("profile1.getProperty(Profile.FIRST_NAME) must not be null", profile1.getProperty(Profile.FIRST_NAME));
//      assertNotNull("profile1.getProperty(Profile.LAST_NAME must not be null", profile1.getProperty(Profile.LAST_NAME));
//      assertFalse("profile1.getFullName().isEmpty() must return false", profile1.getFullName().isEmpty());
//      
//      assertNotNull("gotIdentity1.getId() must not be null", gotIdentity1.getId());
//      Identity regotIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username1, true);
//
//      assertNotNull("regotIdentity.getId() must not be null", regotIdentity.getId());
//      assertNotNull("regotIdentity.getProfile().getId() must not be null", regotIdentity.getProfile().getId());
//      
//    }
//
//    // load profile = false
//    {
//      gotIdentity2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
//                                                         username2,
//                                                         false);
//      assertNotNull("gotIdentity2.getId() must not be null", gotIdentity2.getId());
//
//      assertNotNull("gotIdentity2.getProfile().getId() must not be null", gotIdentity2.getProfile()
//                                                                                      .getId());
//    }
//
//    ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
//
//    assertEquals("activityManager.getActivities(gotIdentity1).size() must be 0", 0, activityManager.getActivities(gotIdentity1).size());
//    assertEquals("activityManager.getActivities(gotIdentity2).size() must be 0", 0, activityManager.getActivities(gotIdentity2).size());
//
//    tearDownIdentityList.add(identityManager.getIdentity(gotIdentity1.getId()));
//    tearDownIdentityList.add(identityManager.getIdentity(gotIdentity2.getId()));
//  }

  /**
   * Test order {@link IdentityManager#getIdentitiesByProfileFilter(String, ProfileFilter, boolean)}
   */
  public void testOrderOfGetIdentitiesByProfileFilter() throws Exception {
    // Create new users
    String providerId = "organization";
    String[] FirstNameList = {"John","Bob","Alain"};
    String[] LastNameList = {"Smith","Dupond","Dupond"};
    for (int i = 0; i < 3; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId);
      identityManager.saveIdentity(identity);
      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, FirstNameList[i]);
      profile.setProperty(Profile.LAST_NAME, LastNameList[i]);
      profile.setProperty(Profile.FULL_NAME, FirstNameList[i] + " " +  LastNameList[i]);
      profile.setProperty(Profile.POSITION, "developer");
      profile.setProperty(Profile.GENDER, "male");

      identityManager.saveProfile(profile);
      identity.setProfile(profile);
      tearDownIdentityList.add(identity);
    }

    ProfileFilter pf = new ProfileFilter();
    ListAccess<Identity> idsListAccess = null;
    // Test order by last name
    pf.setFirstCharacterOfName('D');
    pf.setSorting(new Sorting(SortBy.FULLNAME, OrderBy.ASC));
    idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
    assertNotNull(idsListAccess);
    assertEquals(2, idsListAccess.getSize());
    assertEquals("Alain Dupond", idsListAccess.load(0, 20)[0].getProfile().getFullName());
    assertEquals("Bob Dupond", idsListAccess.load(0, 20)[1].getProfile().getFullName());

    pf = new ProfileFilter();
    idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
    assertNotNull(idsListAccess);
    assertEquals(3, idsListAccess.getSize());
    assertEquals("Alain Dupond", idsListAccess.load(0, 20)[0].getProfile().getFullName());
    assertEquals("Bob Dupond", idsListAccess.load(0, 20)[1].getProfile().getFullName());
    assertEquals("John Smith", idsListAccess.load(0, 20)[2].getProfile().getFullName());

    // Test order by first name if last name is equal
    Identity[] identityArray = idsListAccess.load(0, 2);
    assertEquals(tearDownIdentityList.get(2).getId(), identityArray[0].getId());

  }

  /**
   * Test {@link IdentityManager#updateIdentity(Identity}
   */
//FIXME regression JCR to RDBMS migration
//  public void testUpdateIdentity() throws Exception {
//    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", true);
//    assertNotNull("Identity must not be null", identity);
//    assertEquals("Identity status must be " + identity.isDeleted(), false, identity.isDeleted());
//    identity.setDeleted(true);
//    identityManager.updateIdentity(identity);
//    Identity updatedIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
//    assertEquals("Identity status must be " + updatedIdentity.isDeleted(), true, updatedIdentity.isDeleted());
//    tearDownIdentityList.add(identity);
//  }
  
  /**
   * Test {@link IdentityManager#updateProfile(Profile)}
   */
  public void testUpdateProfile() throws Exception {
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Profile profile = rootIdentity.getProfile();
    profile.setProperty(Profile.POSITION, "CEO");
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    identityManager.updateProfile(profile);
    
    Identity identityUpdated = identityManager.getOrCreateIdentity(rootIdentity.getProviderId(), rootIdentity.getRemoteId(), false);
    assertEquals("CEO", identityUpdated.getProfile().getProperty(Profile.POSITION));

    end();
    begin();

    List<ExoSocialActivity> rootActivityList = activityManager.getActivities(rootIdentity);

    tearDownIdentityList.add(rootIdentity);
  }
  
  public void testUpdateProfileActivity() throws Exception {
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    Profile profile = rootIdentity.getProfile();
    profile.setProperty(Profile.POSITION, "CEO");
    assertEquals(0, profile.getListUpdateTypes().size());
    identityManager.updateProfile(profile);
    assertEquals(0, profile.getListUpdateTypes().size());

    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    assertEquals(1, profile.getListUpdateTypes().size());

    Identity identityUpdated = identityManager.getOrCreateIdentity(rootIdentity.getProviderId(), rootIdentity.getRemoteId(), false);
    assertEquals("CEO", identityUpdated.getProfile().getProperty(Profile.POSITION));

    end();
    begin();

    List<ExoSocialActivity> rootActivityList = activityManager.getActivities(rootIdentity);

    tearDownIdentityList.add(rootIdentity);
  }

  /**
   * Populate list of identities.
   *
   */
  private void populateData() {
    populateIdentities(5, true);
  }
  
  /**
   * Populate list of identities.
   *
   */
  private void populateData(String remoteId) {
    String providerId = "organization";
    Identity identity = new Identity(providerId, remoteId);
    identityManager.saveIdentity(identity);
    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName " + remoteId);
    profile.setProperty(Profile.LAST_NAME, "LastName" + remoteId);
    profile.setProperty(Profile.FULL_NAME, "FirstName " + remoteId + " " +  "LastName" + remoteId);
    profile.setProperty(Profile.POSITION, "developer");
    profile.setProperty(Profile.GENDER, "male");

    identityManager.saveProfile(profile);
    identity.setProfile(profile);
    tearDownIdentityList.add(identity);
  }

  /**
   * Populates the list of identities by specifying the number of items and to indicate if they are added to
   * the tear-down list.
   *
   * @param numberOfItems
   * @param addedToTearDownList
   */
  private void populateIdentities(int numberOfItems, boolean addedToTearDownList) {
    String providerId = "organization";
    for (int i = 0; i < numberOfItems; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId);
      identityManager.saveIdentity(identity);
      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName" + i);
      profile.setProperty(Profile.LAST_NAME, "LastName" + i);
      profile.setProperty(Profile.FULL_NAME, "FirstName" + i + " " +  "LastName" + i);
      profile.setProperty(Profile.POSITION, "developer");
      profile.setProperty(Profile.GENDER, "male");

      identityManager.saveProfile(profile);
      identity.setProfile(profile);
      if (addedToTearDownList) {
        tearDownIdentityList.add(identity);
      }
    }
  }
  
  /**
   * Populate one identity with remoteId.
   * 
   * @param remoteId
   * @return
   */
  private Identity populateIdentity(String remoteId) {
    return populateIdentity(remoteId, true);
  }

  private Identity populateIdentity(String remoteId, boolean addedToTearDownList) {
    String providerId = "organization";
    Identity identity = new Identity(providerId, remoteId);
    identityManager.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, remoteId);
    profile.setProperty(Profile.LAST_NAME, "gtn");
    profile.setProperty(Profile.FULL_NAME, remoteId + " " +  "gtn");
    profile.setProperty(Profile.POSITION, "developer");
    profile.setProperty(Profile.GENDER, "male");

    identityManager.saveProfile(profile);

    if (addedToTearDownList) {
      tearDownIdentityList.add(identity);
    }
    return identity;
  }
  
  /**
   *
   */
  public void testGetIdentitiesByProfileFilterWithProviderId() {
    // TODO hoatle complete testGetIdentitiesByProfileFilterWithProviderId()
    assert true;
  }

  /**
   *
   */
  public void testGetIdentitiesByProfileFilterWithoutProviderId() {
    // TODO hoatle complete testGetIdentitiesByProfileFilterWithoutProviderId()
    assert true;
  }

  /**
   *
   */
  public void testGetIdentitiesFilterByAlphabetWithProviderId() {
    // TODO hoatle complete testGetIdentitiesFilterByAlphabetWithProviderId()
    assert true;
  }

  /**
   *
   */
  public void testGetIdentitiesFilterByAlphaBetWihthoutProviderId() {
    // TODO hoatle complete
    // testGetIdentitiesFilterByAlphaBetWihthoutProviderId()
    assert true;
  }

  /**
   *
   */
  public void testIdentityExisted() {
    // False case
    {
      String remoteId = "notfound";
      String providerId = OrganizationIdentityProvider.NAME;
      final boolean existed = identityManager.identityExisted(providerId, remoteId);
      assertFalse(existed);
    }

    // True case
    {
      // NOTE : we use root as remoteId here because root user is created in
      // portal user system
      // and IdentityManager.identityExisted() just check a portal's user either
      // exist or not..
      // ATTENTION : IdentityManager.identityExisted() depends on providerId,
      // not on identityStorage
      String remoteId = "root";
      String providerId = OrganizationIdentityProvider.NAME;
      final boolean existed = identityManager.identityExisted(providerId, remoteId);
      assertTrue(existed);
    }

  }

  /**
   *
   */
  public void testUpdateAvatar() {
    assert true;
  }

  /**
   *
   */
  public void testUpdateBasicInfo() {
    assert true;
  }

  /**
   *
   */
  public void testUpdateContactSection() {
    assert true;
  }

  /**
   *
   */
  public void testUpdateExperienceSection() {
    assert true;
  }

  /**
   *
   */
  public void testUpdateHeaderSection() {
    assert true;
  }

  /**
   *
   */
  public void testGetIdentities() {
    assert true;
  }

  /**
   *
   */
  public void testGetIdentitiesWithLoadProfile() {
    assert true;
  }

  /**
   *
   */
  public void testRegisterProfileListener() {
    assert true;
  }

  /**
   *
   */
  public void testUnregisterProfileListener() {
    assert true;
  }

  /**
   *
   */
  public void testAddProfileListener() {
    assert true;
  }

  /**
   * Test cache management
   */
  public void testCacheManagement() throws ActivityStorageException {
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                "root");
    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                "john");
    // Identity maryIdentity =
    // identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
    // "mary");
    // Identity demoIdentity =
    // identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
    // "demo");

    Profile rootProfile = rootIdentity.getProfile();
    Profile johnProfile = johnIdentity.getProfile();
    // Profile maryProfile = maryIdentity.getProfile();
    // Profile demoProfile = demoIdentity.getProfile();

    final String newFirstName = "New First Name";

    rootProfile.setProperty(Profile.FIRST_NAME, newFirstName);
    identityManager.saveProfile(rootProfile);
    Identity gotRootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                   "root",
                                                                   true);
    assertNotNull("gotRootIdentity.getId() must not be null", gotRootIdentity.getId());
    assertEquals("gotRootIdentity.getProfile().getProperty(Profile.FIRST_NAME) must be updated: "
        + newFirstName, newFirstName, gotRootIdentity.getProfile().getProperty(Profile.FIRST_NAME));

    try {
      johnProfile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.AVATAR));
      identityManager.updateAvatar(johnProfile);
    } catch (Exception e1) {
      assert false : "can't update avatar" + e1 ;
    }

    Identity gotJohnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                   "john");
    // an activity for avatar created, clean it up here
    ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);

    end();
    begin();

    List<ExoSocialActivity> johnActivityList = activityManager.getActivities(gotJohnIdentity, 0, 10);
    assertEquals("johnActivityList.size() must be 1", 1, johnActivityList.size());
  }
}
