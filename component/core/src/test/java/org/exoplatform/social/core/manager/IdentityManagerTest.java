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
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Tests for {@link IdentityManager}
 *
 * @author hoat_le
 */
public class IdentityManagerTest extends AbstractCoreTest {
  private final Log       LOG = ExoLogger.getLogger(IdentityManagerTest.class);

  private IdentityManager identityManager;

  
  private List<Identity>  tearDownIdentityList;

  private ActivityManager activityManager;

  public void setUp() throws Exception {
    super.setUp();
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull(identityManager);

    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull(activityManager);

    tearDownIdentityList = new ArrayList<Identity>();
  }

  public void tearDown() throws Exception {
    for (Identity identity : tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
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
  public void testGetIdentityById() {
    final String username = "root";
    Identity foundIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username,
            true);

    // Gets Identity By Node Id
    {
      Identity gotIdentity = identityManager.getIdentity(foundIdentity.getId(), true);

      assertNotNull(gotIdentity);
      assertEquals(foundIdentity.getId(), gotIdentity.getId());
      assertEquals("gotIdentity.getProviderId() must return: " + OrganizationIdentityProvider.NAME,
                   OrganizationIdentityProvider.NAME,
                   gotIdentity.getProviderId());
      assertEquals("gotIdentity.getRemoteId() must return: " + username,
                   username,
                   gotIdentity.getRemoteId());
      // By default, when getIdentity(String nodeId) will have load profile by
      // default (means saved).
      assertNotNull("gotIdentity.getProfile().getId() must not return: null",
                    gotIdentity.getProfile().getId());
      
      assertNotNull("gotIdentity.getProfile().getProperty(Profile.FIRST_NAME) must not be null", gotIdentity.getProfile().getProperty(Profile.FIRST_NAME));
      assertFalse("gotIdentity.getProfile().getFullName().isEmpty() must be false", gotIdentity.getProfile().getFullName().isEmpty());


    }

    // Gets Identity By providerId and remoteId

    {
      // With the case of OrganizationIdentityProvider, make sure remoteId
      // already exists in OrganizationService
      //Does not support this anymore
      /*
      GlobalId globalId =  GlobalId.create(OrganizationIdentityProvider.NAME, username);
      Identity gotIdentity2 = identityManager.getIdentity(globalId.toString());
      // "root" is found on OrganizationIdentityProvider (OrganizationService)
      assertNotNull("gotIdentity2 must not be null", gotIdentity2);

      assertNotNull("gotIdentity2.getId() must not be null", gotIdentity2.getId());

      // TODO hoatle: do this for 1.2.x
      // "username" is not saved in JCR yet so its id should be
      // globalId.toString()
      // assertEquals("gotIdentity2.getId() must be: " + globalId.toString(),
      // globalId.toString(), gotIdentity2.getId());

      assertEquals("gotIdentity2.getProviderId() must return: " + OrganizationIdentityProvider.NAME,
                   OrganizationIdentityProvider.NAME,
                   gotIdentity2.getProviderId());
      assertEquals("gotIdentity2.getRemoteId() must return: " + username,
                   username,
                   gotIdentity2.getRemoteId());
      assertNotNull("gotIdentity2.getProfile().getId() must not be null", gotIdentity2.getProfile()
                                                                                    .getId());
      */
    }

    tearDownIdentityList.add(identityManager.getIdentity(foundIdentity.getId(), false));
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
  public void testGetOrCreateIdentity() {
    final String username1 = "john";
    final String username2 = "root";
    Identity gotIdentity1;
    Identity gotIdentity2;
    // load profile = true
    {
      gotIdentity1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                         username1);
      
      Profile profile1 = gotIdentity1.getProfile();


      assertNotNull("gotIdentity1.getId() must not be null", gotIdentity1.getId());
      assertNotNull("profile1.getId() must not be null", profile1.getId());
      assertNotNull("profile1.getProperty(Profile.FIRST_NAME) must not be null", profile1.getProperty(Profile.FIRST_NAME));
      assertNotNull("profile1.getProperty(Profile.LAST_NAME must not be null", profile1.getProperty(Profile.LAST_NAME));
      assertFalse("profile1.getFullName().isEmpty() must return false", profile1.getFullName().isEmpty());
      
      assertNotNull("gotIdentity1.getId() must not be null", gotIdentity1.getId());
      Identity regotIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username1);

      assertNotNull("regotIdentity.getId() must not be null", regotIdentity.getId());
      assertNotNull("regotIdentity.getProfile().getId() must not be null", regotIdentity.getProfile().getId());
      
    }

    // load profile = false
    {
      gotIdentity2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                         username2,
                                                         false);
      assertNotNull("gotIdentity2.getId() must not be null", gotIdentity2.getId());

      assertNotNull("gotIdentity2.getProfile().getId() must not be null", gotIdentity2.getProfile()
                                                                                      .getId());
    }

    ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);

    assertEquals("activityManager.getActivities(gotIdentity1).size() must be 0", 0, activityManager.getActivities(gotIdentity1).size());
    assertEquals("activityManager.getActivities(gotIdentity2).size() must be 0", 0, activityManager.getActivities(gotIdentity2).size());
    
    // FIXME hoatle fix the problem of getIdentity from a provider but also
    // saved on JCR
    /*
     * GlobalId globalId1 = new GlobalId(OrganizationIdentityProvider.NAME +
     * GlobalId.SEPARATOR + username1); GlobalId globalId2 = new
     * GlobalId(OrganizationIdentityProvider.NAME + GlobalId.SEPARATOR +
     * username2);
     * tearDownIdentityList.add(identityManager.getIdentity(globalId1
     * .toString())); //identity.getId() = null ????
     * tearDownIdentityList.add(identityManager
     * .getIdentity(globalId2.toString())); //identity.getId() = null ????
     */
    tearDownIdentityList.add(identityManager.getIdentity(gotIdentity1.getId()));
    tearDownIdentityList.add(identityManager.getIdentity(gotIdentity2.getId()));
  }

  /**
   * Test {@link IdentityManager#getProfile(Identity)}
   */
  public void testGetProfile() throws Exception {
    Identity identity = populateIdentity("root");
    assertNotNull("Identity must not be null.", identity);
    assertNull("Profile status must be not loaded yet.", identity.getProfile().getId());
    Profile profile = identityManager.getProfile(identity);
    assertNotNull("Profile must not be null.", profile);
    assertNotNull("Profile status must be loaded.", identity.getProfile().getId());
  }
  
  /**
   * Test {@link IdentityManager#getIdentitiesByProfileFilter(String, ProfileFilter, boolean)}
   */
  public void testGetIdentitiesByProfileFilter() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;
    populateData();
    
    ProfileFilter pf = new ProfileFilter();
    ListAccess idsListAccess = null;
    { // Test cases with name of profile.
      // Filter identity by first character.
      pf.setFirstCharacterOfName('F');
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setFirstCharacterOfName('L');
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 0, idsListAccess.getSize());
      
      // Filter identity by name.
      pf.setFirstCharacterOfName('\u0000');
      pf.setName("FirstName");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setName("FirstName2");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 1, idsListAccess.getSize());
      pf.setName("");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setName("*");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setName("n%me");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setName("n*me");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setName("%me");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setName("%name%");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setName("n%me");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setName("fir%n%me");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setName("noname");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities must be " + idsListAccess.getSize(), 0, idsListAccess.getSize());
    }
    
    { // Test cases with position of profile.
      pf.setName("");
      pf.setPosition("dev");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities get by position must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setPosition("d%v");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities get by position must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setPosition("test");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities get by position must be " + idsListAccess.getSize(), 0, idsListAccess.getSize());
    }
    
    { // Test cases with gender of profile.
      pf.setPosition("");
      pf.setGender("male");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities get by gender must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setGender("female");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities get by gender must be " + idsListAccess.getSize(), 0, idsListAccess.getSize());
    }
    
    { // Other test cases
      pf.setName("n**me%");
      pf.setPosition("*%");
      pf.setGender("male");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities get by profile filter must be " + idsListAccess.getSize(), 10, idsListAccess.getSize());
      pf.setName("noname");
      pf.setPosition("*%");
      pf.setGender("male");
      idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
      assertNotNull("Identity List Access must not be null", idsListAccess);
      assertEquals("The number of identities get by profile filter must be " + idsListAccess.getSize(), 0, idsListAccess.getSize());
    }

    //Tests with the case: add new identity and delete it after that to check
    {
      ProfileFilter profileFilter = new ProfileFilter();
      ListAccess<Identity> identityListAccess = identityManager.getIdentitiesByProfileFilter("organization", profileFilter, false);
      assertEquals("identityListAccess.getSize() must return 10", 10, identityListAccess.getSize());
      Identity testIdentity = populateIdentity("test", false);
      ListAccess<Identity> identityListAccess1 = identityManager.getIdentitiesByProfileFilter("organization", new ProfileFilter(), false);
      assertEquals("identityListAccess1.getSize() must return 11", 11, identityListAccess1.getSize());
      identityListAccess = identityManager.getIdentitiesByProfileFilter("organization", profileFilter, false);
      assertEquals("identityListAccess.getSize() must return 11", 11, identityListAccess.getSize());
      identityManager.deleteIdentity(testIdentity);
      identityListAccess = identityManager.getIdentitiesByProfileFilter("organization", profileFilter, false);
      assertEquals("identityListAccess.getSize() must return 10", 10, identityListAccess.getSize());
    }

    //Test with excluded identity list
    {
      Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
      List<Identity> excludedIdentities = new ArrayList<Identity>();
      excludedIdentities.add(rootIdentity);
      ProfileFilter profileFilter = new ProfileFilter();
      profileFilter.setExcludedIdentityList(excludedIdentities);
      ListAccess<Identity> identityListAccess = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, profileFilter, false);
      assertEquals("identityListAccess.getSize() must return 10", 10, identityListAccess.getSize());
      Identity[] identityArray = identityListAccess.load(0, 100);
      assertEquals("identityArray.length must be 10", 10, identityArray.length);
    }
  }

  /**
   * Test {@link IdentityManager#updateIdentity(Identity}
   */
  public void testUpdateIdentity() throws Exception {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", true);
    assertNotNull("Identity must not be null", identity);
    assertEquals("Identity status must be " + identity.isDeleted(), false, identity.isDeleted());
    identity.setDeleted(true);
    identityManager.updateIdentity(identity);
    Identity updatedIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    assertEquals("Identity status must be " + updatedIdentity.isDeleted(), true, updatedIdentity.isDeleted());
    tearDownIdentityList.add(identity);
  }
  
  /**
   * Test {@link IdentityManager#updateProfile(Profile)}
   */
  public void testUpdateProfile() throws Exception {
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Profile profile = rootIdentity.getProfile();
    profile.setProperty(Profile.POSITION, "CEO");
    identityManager.updateProfile(profile);
    
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
    populateIdentities(10, true);
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

    final String johnAvatarUrl = "http://domain.com/avatar/john.jpg";
    johnProfile.setProperty(Profile.AVATAR_URL, johnAvatarUrl);
    try {
      identityManager.updateAvatar(johnProfile);
    } catch (Exception e1) {
      assert false : "can't update avatar" + e1 ;
    }

    Identity gotJohnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                   "john");
    
    assertEquals("gotJohnIdentity.getProfile().getProperty(Profile.AVATAR_URL) must return "
        + johnAvatarUrl, johnAvatarUrl, gotJohnIdentity.getProfile()
                                                       .getProperty(Profile.AVATAR_URL));
    tearDownIdentityList.add(johnIdentity);
    tearDownIdentityList.add(rootIdentity);
    // an activity for avatar created, clean it up here

    ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);

    end();
    begin();

    List<ExoSocialActivity> johnActivityList = activityManager.getActivities(gotJohnIdentity, 0, 10);
    assertEquals("johnActivityList.size() must be 1", 1, johnActivityList.size());
  }
}
