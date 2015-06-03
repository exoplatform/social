package org.exoplatform.social.core.storage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.core.chromattic.entity.DisabledEntity;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.ProviderRootEntity;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.storage.impl.SpaceStorageImpl;
import org.exoplatform.social.core.storage.impl.StorageUtils;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;
import org.exoplatform.social.core.test.QueryNumberTest;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jun 17, 2010
 * Time: 9:34:56 AM
 */
@QueryNumberTest
public class IdentityStorageTest extends AbstractCoreTest {
  private IdentityStorage identityStorage;
  private SpaceStorage spaceStorage;
  private List<Identity> tearDownIdentityList;
  private List<Space> tearDownSpaceList;
  private SocialChromatticLifeCycle lifecycle;

  public void setUp() throws Exception {
    super.setUp();
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorageImpl.class);
    spaceStorage = (SpaceStorage) getContainer().getComponentInstanceOfType(SpaceStorageImpl.class);
    lifecycle = lifecycleLookup();
    assertNotNull("identityStorage must not be null", identityStorage);
    tearDownIdentityList = new ArrayList<Identity>();
    tearDownSpaceList = new ArrayList<Space>();
  }

  public void tearDown() throws Exception {
    for (Identity identity : tearDownIdentityList) {
      identityStorage.deleteIdentity(identity);
    }
    for (Space space : tearDownSpaceList) {
      spaceStorage.deleteSpace(space.getId());
    }
    super.tearDown();
  }
  
  public void testUpgradeIdentity() {
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, "identity1");
    identityStorage.saveIdentity(tobeSavedIdentity);

    tearDownIdentityList.add(tobeSavedIdentity);

    // Get new session
    ChromatticSession session = lifecycle.getSession();
    synchronized (this) {
      try {
        System.out.println("waiting 60 seconds ...");
        this.wait(60 * 1000);
      } catch (InterruptedException e1) {
        System.out.println("InterruptedException but is Session close? " + session.isClosed());
      }
    }
    System.out.println("JCRSession is alive? " + session.getJCRSession().isLive());
    System.out.println("Session is close? " + session.isClosed());

    // System.out.println("New session with timeout = " +timeout);
    try {
      IdentityStorageImpl impl = (IdentityStorageImpl)identityStorage;
      ProviderRootEntity providerRoot = impl.getProviderRoot();
      String path = providerRoot.getProviders().get(OrganizationIdentityProvider.NAME).getPath();
      System.out.println(path);
    } catch (NullPointerException e) {
      e.printStackTrace();
    }
  }

  private static SocialChromatticLifeCycle lifecycleLookup() {
    PortalContainer container = PortalContainer.getInstance();
    ChromatticManager manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
    return (SocialChromatticLifeCycle) manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);
  }

  /**
   * Tests {@link IdenityStorage#saveIdentity(Identity)}
   *
   */
  @MaxQueryNumber(186)
  public void testSaveIdentity() {
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, "identity1");
    identityStorage.saveIdentity(tobeSavedIdentity);

    assertNotNull(tobeSavedIdentity.getId());

    final String updatedRemoteId = "identity-updated";

    tobeSavedIdentity.setRemoteId(updatedRemoteId);

    identityStorage.saveIdentity(tobeSavedIdentity);

    Identity gotIdentity = identityStorage.findIdentityById(tobeSavedIdentity.getId());

    assertEquals(updatedRemoteId, gotIdentity.getRemoteId());
    tearDownIdentityList.add(gotIdentity);
    
  }

  /**
   * Tests {@link IdenityStorage#processEnabledIdentity(Identity)}
   */
  @MaxQueryNumber(99)
  public void testEnableIdentity() {
    final String remoteUser = "user";
    Identity identity = new Identity(OrganizationIdentityProvider.NAME, remoteUser);
    identityStorage.saveIdentity(identity);
    //
    assertNotNull(identity.getId());
    //
    identityStorage.processEnabledIdentity(identity, false);
    IdentityEntity got = lifecycle.getSession().findById(IdentityEntity.class, identity.getId());
    //
    assertNotNull(got);
    //
    DisabledEntity mixin = lifecycle.getSession().getEmbedded(got, DisabledEntity.class);
    assertNotNull(mixin);
    //
    identityStorage.processEnabledIdentity(identity, true);
    //
    mixin = lifecycle.getSession().getEmbedded(got, DisabledEntity.class);
    assertNull(mixin);

    tearDownIdentityList.add(identity);
  }

  /**
   * Tests {@link IdenityStorage#deleteIdentity(Identity)}
   *
   */
  @MaxQueryNumber(807)
  public void testDeleteIdentity() {
    final String username = "username";
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, username);
    identityStorage.saveIdentity(tobeSavedIdentity);

    assertNotNull(tobeSavedIdentity.getId());

    identityStorage.deleteIdentity(tobeSavedIdentity);

    tobeSavedIdentity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, username);
    assertNull("tobeSavedIdentity must be null", tobeSavedIdentity);

    // Delete identity with loaded profile
    {
      tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, username);
      identityStorage.saveIdentity(tobeSavedIdentity);
      assertNotNull("tobeSavedIdentity.getId() must not be null.", tobeSavedIdentity.getId());
      assertNull("tobeSavedIdentity.getProfile().getId() must be null.", tobeSavedIdentity.getProfile().getId());
      Profile profile = identityStorage.loadProfile(tobeSavedIdentity.getProfile());
      tobeSavedIdentity.setProfile(profile);
      assertNotNull("tobeSavedIdentity.getProfile().getId() must not be null", tobeSavedIdentity.getProfile().getId());

      identityStorage.deleteIdentity(tobeSavedIdentity);
      assertNotNull("tobeSavedIdentity.getId() must not be null", tobeSavedIdentity.getId());
      try {
        identityStorage.findIdentityById(tobeSavedIdentity.getId());
      } catch (Exception e1) {
        assert false : "can't update avatar" + e1 ;
      }

    }
  }

  /**
   * Tests {@link IdenityStorage#findIdentityById(String)}
   *
   */
  @MaxQueryNumber(75)
  public void testFindIdentityById() {
    final String remoteUser = "identity1";
    Identity toSaveIdentity = new Identity(OrganizationIdentityProvider.NAME, remoteUser);
    identityStorage.saveIdentity(toSaveIdentity);

    assertNotNull(toSaveIdentity.getId());

    Identity gotIdentityById = identityStorage.findIdentityById(toSaveIdentity.getId());

    assertNotNull(gotIdentityById);
    assertEquals(toSaveIdentity.getId(), gotIdentityById.getId());
    assertEquals(toSaveIdentity.getProviderId(), gotIdentityById.getProviderId());
    assertEquals(toSaveIdentity.getRemoteId(), gotIdentityById.getRemoteId());

    Identity notFoundIdentityByRemoteid = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, "not-found");

    assertNull(notFoundIdentityByRemoteid);

    Identity gotIdentityByRemoteId = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, remoteUser);

    assertNotNull(gotIdentityByRemoteId);
    assertEquals(gotIdentityByRemoteId.getId(), toSaveIdentity.getId());
    assertEquals(gotIdentityByRemoteId.getProviderId(), toSaveIdentity.getProviderId());
    assertEquals(gotIdentityByRemoteId.getRemoteId(), toSaveIdentity.getRemoteId());
    
    tearDownIdentityList.add(gotIdentityByRemoteId);
  }

  /**
   * Tests {@link IdenityStorage#findIdentity(String, String)}
   *
   */
  @MaxQueryNumber(72)
  public void testFindIdentity() {
    final String userName = "username";

    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, userName);
    identityStorage.saveIdentity(tobeSavedIdentity);

    Identity foundIdentity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userName);

    assertNotNull(foundIdentity);
    assertNotNull(foundIdentity.getId());
    assertEquals(OrganizationIdentityProvider.NAME, foundIdentity.getProviderId());
    assertEquals(userName, foundIdentity.getRemoteId());
    tearDownIdentityList.add(foundIdentity);
  }

  /**
   * Tests {@link IdenityStorage#saveProfile(Profile)}
   *
   */
  @MaxQueryNumber(108)
  public void testSaveProfile() {
    final String userName = "username";
    final String firstName = "FirstName";
    final String lastName = "LastName";
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, userName);
    identityStorage.saveIdentity(tobeSavedIdentity);

    Profile tobeSavedProfile = tobeSavedIdentity.getProfile();

    tobeSavedProfile.setProperty(Profile.USERNAME, userName);
    tobeSavedProfile.setProperty(Profile.FIRST_NAME, firstName);
    tobeSavedProfile.setProperty(Profile.LAST_NAME, lastName);

    assertTrue(tobeSavedProfile.hasChanged());
    identityStorage.saveProfile(tobeSavedProfile);
    assertFalse(tobeSavedProfile.hasChanged());

    assertNotNull(tobeSavedProfile.getId());

    assertEquals(userName, tobeSavedProfile.getProperty(Profile.USERNAME));
    assertEquals(firstName, tobeSavedProfile.getProperty(Profile.FIRST_NAME));
    assertEquals(lastName, tobeSavedProfile.getProperty(Profile.LAST_NAME));
    assertEquals(firstName + " " + lastName, tobeSavedProfile.getFullName());
    tearDownIdentityList.add(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userName));
  }

  /**
   * Tests {@link IdenityStorage#loadProfile(Profile)}
   *
   */
  @MaxQueryNumber(210)
  public void testLoadProfile() throws Exception {
    final String username = "username";
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, username);
    identityStorage.saveIdentity(tobeSavedIdentity);
    Profile tobeSavedProfile = tobeSavedIdentity.getProfile();
    tobeSavedProfile.setProperty(Profile.USERNAME, username);

    assertTrue(tobeSavedProfile.hasChanged());
    tobeSavedProfile = identityStorage.loadProfile(tobeSavedProfile);
    assertFalse(tobeSavedProfile.hasChanged());

    assertNotNull(tobeSavedProfile.getId());
    assertEquals(username, tobeSavedProfile.getProperty(Profile.USERNAME));
    
    // Test in case loading an user has dot characters in name.
    {
      InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
      AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
      String userDotName = "user.name";
      Identity identity = new Identity(OrganizationIdentityProvider.NAME, userDotName);
      Profile profile = new Profile(identity);
      identity.setProfile(profile);
      profile.setProperty(Profile.AVATAR, avatarAttachment);
      
      identityStorage.saveIdentity(identity);
      identityStorage.saveProfile(profile);
      
      identityStorage.loadProfile(profile);
      
      String gotAvatarURL = profile.getAvatarUrl();
      int indexOfLastupdatedParam = gotAvatarURL.indexOf("/?upd=");
      String avatarURL = null;
      if(indexOfLastupdatedParam != -1){
        avatarURL = gotAvatarURL.substring(0,indexOfLastupdatedParam);
      } else {
        avatarURL = gotAvatarURL;
      }
      assertEquals(LinkProvider.escapeJCRSpecialCharacters(
                   StorageUtils.encodeUrl("/production/soc:providers/soc:organization/soc:user%02name/soc:profile/soc:avatar")), avatarURL);
      tearDownIdentityList.add(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userDotName));
    }
    tearDownIdentityList.add(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, username));
  }

  @MaxQueryNumber(99)
  public void testLoadProfileByReloadCreatedProfileNode() throws Exception {
    String providerId = "organization";
    String remoteId = "username";
    Identity identity = new Identity(providerId, remoteId);

    identityStorage.saveIdentity(identity);
    String profileId;
    //this code snippet will create profile node for test case
    {
      //create new profile in db without data (lazy creating)
      Profile profile = new Profile(identity);
      assertFalse(profile.hasChanged());
      profile = identityStorage.loadProfile(profile);
      assertFalse(profile.hasChanged());
      profileId = profile.getId();
    }

    //here is the testcase
    {
      Profile profile = new Profile(identity);
      assertFalse(profile.hasChanged());
      profile = identityStorage.loadProfile(profile);
      assertFalse(profile.hasChanged());
      assertEquals(profileId, profile.getId());
    }
    
    tearDownIdentityList.add(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, remoteId));
  }

  @MaxQueryNumber(108)
  public void testFindIdentityByExistName() throws Exception {
    String providerId = "organization";
    String remoteId = "username";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName");
    profile.setProperty(Profile.LAST_NAME, "LastName");
    profile.setProperty(Profile.FULL_NAME, "FirstName" + " " + "LastName");
    identityStorage.saveProfile(profile);
    identity.setProfile(profile);
    tearDownIdentityList.add(identity);
    final ProfileFilter filter = new ProfileFilter();
    filter.setName("First");
    final List<Identity> result = identityStorage.getIdentitiesByProfileFilter(providerId, filter, 0, 1, false);
    assertEquals(1, result.size());
  }

  @MaxQueryNumber(1080)
  public void testFindManyIdentitiesByExistName() throws Exception {
    final String providerId = "organization";

    final int total = 10;
    for (int i = 0; i <  total; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId+i);
      identityStorage.saveIdentity(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName"+ i);
      profile.setProperty(Profile.LAST_NAME, "LastName" + i);
      profile.setProperty(Profile.FULL_NAME, "FirstName" + i + " " + "LastName" + i);
      identityStorage.saveProfile(profile);
      identity.setProfile(profile);
      tearDownIdentityList.add(identity);
    }

    final ProfileFilter filter = new ProfileFilter();
    filter.setName("FirstName");
    final List<Identity> result = identityStorage.getIdentitiesByProfileFilter(providerId, filter, 0, total, false);
    assertEquals(total, result.size());
  }

  @MaxQueryNumber(99)
  public void testFindIdentityByNotExistName() throws Exception {
    String providerId = "organization";
    String remoteId = "username";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName");
    profile.setProperty(Profile.LAST_NAME, "LastName");
    profile.setProperty(Profile.FULL_NAME, "FirstName" + " " + "LastName");
    identityStorage.saveProfile(profile);
    identity.setProfile(profile);
    tearDownIdentityList.add(identity);
    final ProfileFilter filter = new ProfileFilter();
    filter.setName("notfound");
    final List<Identity> result = identityStorage.getIdentitiesByProfileFilter(providerId, filter, 0, 1, false);
    assertEquals(0, result.size());
  }

  /**
   * Tests {@link IdenityStorage#getIdentitiesByProfileFilter(String, ProfileFilter, int, int, boolean)}
   *
   */
  @MaxQueryNumber(582)
  public void testFindIdentityByProfileFilter() throws Exception {
    String providerId = "organization";
    String remoteId = "username";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName");
    profile.setProperty(Profile.LAST_NAME, "LastName");
    profile.setProperty(Profile.FULL_NAME, "FirstName" + " " + "LastName");
    profile.setProperty("position", "developer");
    profile.setProperty("gender", "male");

    identityStorage.saveProfile(profile);
    identity.setProfile(profile);
    tearDownIdentityList.add(identity);
    final ProfileFilter filter = new ProfileFilter();
    filter.setPosition("developer");
    filter.setName("First");
    final List<Identity> result = identityStorage.getIdentitiesByProfileFilter(providerId, filter, 0, 1, false);
    assertEquals(1, result.size());


    //create a new identity
    Identity test2Identity = populateIdentity("test2", false);

    //check when new identity is not deleted
    final ProfileFilter profileFilter2 = new ProfileFilter();
    List<Identity> foundIdentities = identityStorage.getIdentitiesByProfileFilter(providerId, profileFilter2, 0, 10, false);
    assertEquals("foundIdentities.size() must be 1", 2, foundIdentities.size());

    //finds the second one
    profileFilter2.setName("g");
    foundIdentities =  identityStorage.getIdentitiesByProfileFilter(providerId, profileFilter2, 0, 10, false);
    assertEquals("foundIdentities.size() must be 1", 1, foundIdentities.size());

    //check when new identity is deleted
    identityStorage.deleteIdentity(test2Identity);
    foundIdentities = identityStorage.getIdentitiesByProfileFilter(providerId, profileFilter2, 0, 10, false);
    assertEquals("foundIdentities.size() must be 0", 0, foundIdentities.size());
  }

  /**
   * Tests {@link IdenityStorage#getIdentitiesByProfileFilter(String, ProfileFilter, int, int, boolean)}
   *
   */
  @MaxQueryNumber(1140)
  public void testFindManyIdentitiesByProfileFilter() throws Exception {
    String providerId = "organization";

    int total = 10;
    for (int i = 0; i < total; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId);
      identityStorage.saveIdentity(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName" + i);

      profile.setProperty(Profile.LAST_NAME, "LastName");
      profile.setProperty(Profile.FULL_NAME, "FirstName" + i + " " + "LastName" + i);
      profile.setProperty(Profile.POSITION, "developer");
      profile.setProperty(Profile.GENDER, "male");
      identity.setProfile(profile);
      tearDownIdentityList.add(identity);
      identityStorage.saveProfile(profile);
    }

    final ProfileFilter filter = new ProfileFilter();
    filter.setPosition("developer");
    filter.setName("FirstN");
    final List<Identity> result = identityStorage.getIdentitiesByProfileFilter(providerId, filter, 0, total, false);
    assertEquals(total, result.size());
  }
  
  /**
   * Tests {@link IdenityStorage#getIdentitiesByFirstCharaterOfNameCount(String, char)}
   * 
   */
  @MaxQueryNumber(1200)
  public void testGetIdentitiesByFirstCharacterOfNameCount() throws Exception {
    populateData();
    final ProfileFilter filter = new ProfileFilter();
    filter.setFirstCharacterOfName('F');
    int idsCount = identityStorage.getIdentitiesByFirstCharacterOfNameCount("organization", filter);
    assertEquals("Number of identity must be " + idsCount, 0, idsCount);
    filter.setFirstCharacterOfName('L');
    idsCount = identityStorage.getIdentitiesByFirstCharacterOfNameCount("organization", filter);
    assertEquals("Number of identity must be " + idsCount, 5, idsCount);
    
    //disable username1
    Identity identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, "username1");
    identityStorage.processEnabledIdentity(identity, false);
    assertEquals(4, identityStorage.getIdentitiesByFirstCharacterOfNameCount("organization", filter));
    
    //enable username1
    identityStorage.processEnabledIdentity(identity, true);
    assertEquals(5, identityStorage.getIdentitiesByFirstCharacterOfNameCount("organization", filter));
  }

  /**
   * Tests {@link IdenityStorage#getIdentitiesByFirstCharaterOfName(String, char, int, int, boolean)}
   * 
   */
  @MaxQueryNumber(1100)
  public void testGetIdentitiesByFirstCharacterOfName() throws Exception {
    populateData();    
    final ProfileFilter filter = new ProfileFilter();
    filter.setFirstCharacterOfName('F');
    assertEquals(0, identityStorage.getIdentitiesByFirstCharacterOfName("organization", filter, 0, 1, false).size());
    filter.setFirstCharacterOfName('L');
    assertEquals(5, identityStorage.getIdentitiesByFirstCharacterOfName("organization", filter, 0, 10, false).size());
  }
  
  /**
   * Tests {@link IdenityStorage#getIdentitiesByProfileFilterCount(String, ProfileFilter)}
   * 
   */
  @MaxQueryNumber(2000)
  public void testGetIdentitiesByProfileFilterCount() throws Exception {
    populateData();

    ProfileFilter pf = new ProfileFilter();
    int idsCount = identityStorage.getIdentitiesByProfileFilterCount("organization", pf);
    assertEquals(5, idsCount);
    
    pf.setPosition("developer");
    pf.setName("FirstName");
    
    idsCount = identityStorage.getIdentitiesByProfileFilterCount("organization", pf);
    assertEquals(5, idsCount);
    
    pf.setName("LastN");
    idsCount = identityStorage.getIdentitiesByProfileFilterCount("organization", pf);
    assertEquals(5, idsCount);
    
    //disable username1
    Identity identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, "username1");
    identityStorage.processEnabledIdentity(identity, false);
    assertEquals(4, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));
    
    //enable username1
    identityStorage.processEnabledIdentity(identity, true);
    assertEquals(5, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));
  }
  
  /**
   * Tests {@link IdenityStorage#getIdentitiesByProfileFilterCount(String, ProfileFilter, int, int, boolean)}
   * 
   */
  @MaxQueryNumber(670)
  public void testGetIdentitiesByProfileFilterAccessList() throws Exception {
    populateData();
    ProfileFilter pf = new ProfileFilter();
    
    List<Identity> identities = identityStorage.getIdentitiesByProfileFilter("organization", pf, 0, 20, false);
    assertEquals("Number of identities must be " + identities.size(), 5, identities.size());
    
    pf.setPosition("developer");
    pf.setName("FirstName");
    identities = identityStorage.getIdentitiesByProfileFilter("organization", pf, 0, 20, false);
    assertEquals("Number of identities must be " + identities.size(), 5, identities.size());
    
    try {
      identities = identityStorage.getIdentitiesByProfileFilter("organization", pf, -1, 20, false);
    } catch (Exception ext) {
      assert false : "Can not get Identity by profile filter. " + ext ;
    } 
    
    try {
      identities = identityStorage.getIdentitiesByProfileFilter("organization", pf, 0, -1, false);
    } catch (Exception ext) {
      assert false : "Can not get Identity by profile filter. " + ext ;
    } 
    
    try {
      identities = identityStorage.getIdentitiesByProfileFilter("organization", pf, 30, 40, false);
    } catch (Exception ext) {
      assert false : "Can not get Identity by profile filter. " + ext ;
    } 
  }
  
  /**
   * Tests {@link IdenityStorage#findIdentityByProfileFilterCount(String, ProfileFilter)}
   * 
   */
  @MaxQueryNumber(264)
  public void testUpdateIdentity() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;
    String newProviderId = "space";
    String userName = "root";
    Identity identity = populateIdentity(userName);
    assertNotNull("Identity must not be null", identity);
    assertEquals("Identity status must be " + identity.isDeleted(), false, identity.isDeleted());
    identity.setDeleted(true);
    identityStorage.updateIdentity(identity);
    Identity updatedIdentity = identityStorage.findIdentity(providerId, userName);
    assertEquals("Identity status must be " + updatedIdentity.isDeleted(), true, updatedIdentity.isDeleted());
    identity.setProviderId(newProviderId);
    identity.setDeleted(false);
    identityStorage.updateIdentity(identity);
    updatedIdentity = identityStorage.findIdentity(newProviderId, userName);
    assertEquals("Identity status must be " + updatedIdentity.isDeleted(), false, updatedIdentity.isDeleted());
    assertEquals("Identity provider id must be " + updatedIdentity.getProviderId(), newProviderId, updatedIdentity.getProviderId());
  }
  
  /**
   *  Tests {@link IdenityStorage#getIdentitiesCount(String)}
   */
  @MaxQueryNumber(765)
  public void testGetIdentitiesCount() throws Exception {
    int numberUser = 10;
    int numberDisableUser = 5;
    // create user
    List<Identity> identities = new ArrayList<Identity>();
    for (int i = 0; i < numberUser; i++) {
      Identity identity = new Identity(OrganizationIdentityProvider.NAME, "user" + i);
      identityStorage.saveIdentity(identity);
      identities.add(identity);
    }
    assertEquals(10, identityStorage.getIdentitiesCount(OrganizationIdentityProvider.NAME));
    // disable users
    for (int i = 0; i < numberDisableUser; i++) {
      Identity identity = identities.get(i);
      identityStorage.processEnabledIdentity(identity, false);
    }
    assertEquals(numberUser - numberDisableUser, identityStorage.getIdentitiesCount(OrganizationIdentityProvider.NAME));
  }

  @MaxQueryNumber(2635)
  public void testGetSpaceMemberByProfileFilter() throws Exception {
    populateData();
    populateUser("username4");
    
    Space space = new Space();
    space.setApp("app");
    space.setDisplayName("my space");
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space ");
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId(SpaceUtils.createGroup(space.getPrettyName(), "username4"));
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] {};
    String[] members = new String[] {"username1", "username2", "username3"};
    String[] invitedUsers = new String[] {};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);

    spaceStorage.saveSpace(space, true);
    tearDownSpaceList.add(space);
    
    ProfileFilter profileFilter = new ProfileFilter();
    
    List<Identity> identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, profileFilter, Type.MEMBER, 0, 2);
    assertEquals(2, identities.size());
    
    profileFilter.setName("0");
    identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, profileFilter, Type.MEMBER, 0, 2);
    assertEquals(0, identities.size());
    
    profileFilter.setName("3");
    identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, profileFilter, Type.MEMBER, 0, 2);
    assertEquals(1, identities.size());
    
    addUserToGroupWithMembership("username4", space.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
    identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, new ProfileFilter(), Type.MANAGER, 0, 10);
    assertEquals(1, identities.size());
  }
  
  /**
   * Populate one identity with remoteId.
   * 
   * @param remoteId
   * @return
   */
  private Identity populateIdentity(String remoteId) {
    return populateIdentity(remoteId, false);
  }

  /**
   * Populates one identity with remoteId.
   *
   * @param remoteId
   * @param addedToTearDown
   * @return
   */
  private Identity populateIdentity(String remoteId, boolean addedToTearDown) {
    String providerId = "organization";
    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, remoteId);
    profile.setProperty(Profile.LAST_NAME, "gtn");
    profile.setProperty(Profile.FULL_NAME, remoteId + " " +  "gtn");
    profile.setProperty(Profile.POSITION, "developer");
    profile.setProperty(Profile.GENDER, "male");
    identityStorage.saveProfile(profile);

    identity.setProfile(profile);
    if (addedToTearDown) {
      tearDownIdentityList.add(identity);
    }
    return identity;
  }
  
  private void populateData() {
    populateData(5);
  }
  
  /**
   * Populate list of identities.
   *
   */
  private void populateData(String remoteId) {
    String providerId = "organization";
    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);
    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName " + remoteId);
    profile.setProperty(Profile.LAST_NAME, "LastName" + remoteId);
    profile.setProperty(Profile.FULL_NAME, "FirstName " + remoteId + " " +  "LastName" + remoteId);
    profile.setProperty(Profile.POSITION, "developer");
    profile.setProperty(Profile.GENDER, "male");

    identityStorage.saveProfile(profile);
    identity.setProfile(profile);
    tearDownIdentityList.add(identity);
  }
  
  private void populateData(int number) {
    String providerId = "organization";
    for (int i = 0; i < number; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId);
      identityStorage.saveIdentity(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName" + i);
      profile.setProperty(Profile.LAST_NAME, "LastName" + i);
      profile.setProperty(Profile.FULL_NAME, "FirstName" + i + " " +  "LastName" + i);
      profile.setProperty("position", "developer");
      profile.setProperty("gender", "male");
      identity.setProfile(profile);
      tearDownIdentityList.add(identity);
      identityStorage.saveProfile(profile);
    }
  }
  
  private User populateUser(String name) {
    OrganizationService os = SpaceUtils.getOrganizationService();
    User user = os.getUserHandler().createUserInstance(name);
    
    try {
      os.getUserHandler().createUser(user, false);
    } catch (Exception e) {
      return null;
    }
    return user;
  }
  
  private static void addUserToGroupWithMembership(String remoteId, String groupId, String membership) {
    OrganizationService organizationService = SpaceUtils.getOrganizationService();
    try {
      // TODO: checks whether user is already manager?
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
