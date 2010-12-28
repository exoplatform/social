package org.exoplatform.social.core.storage;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.social.common.jcr.JCRSessionManager;
import org.exoplatform.social.common.jcr.QueryBuilder;
import org.exoplatform.social.common.jcr.SocialDataLocation;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jun 17, 2010
 * Time: 9:34:56 AM
 */
public class IdentityStorageTest extends AbstractCoreTest {
  private IdentityStorage identityStorage;


  public void setUp() throws Exception {
    super.setUp();
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    assertNotNull("identityStorage must not be null", identityStorage);
  }

  public void tearDown() throws Exception {
    List<Identity> storedIdentityList = identityStorage.getAllIdentities();
    for (Identity identity : storedIdentityList) {
      identityStorage.deleteIdentity(identity);
    }
    super.tearDown();
  }

  public void testSaveIdentity() {
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, "identity1");
    identityStorage.saveIdentity(tobeSavedIdentity);

    assertNotNull(tobeSavedIdentity.getId());

    final String updatedRemoteId = "identity-updated";

    tobeSavedIdentity.setRemoteId(updatedRemoteId);

    identityStorage.saveIdentity(tobeSavedIdentity);

    Identity gotIdentity = identityStorage.findIdentityById(tobeSavedIdentity.getId());

    assertEquals(updatedRemoteId, gotIdentity.getRemoteId());

  }

  public void testDeleteIdentity() {
    final String username = "username";
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, username);
    identityStorage.saveIdentity(tobeSavedIdentity);

    assertNotNull(tobeSavedIdentity.getId());

    identityStorage.deleteIdentity(tobeSavedIdentity);

    // Delete identity with loaded profile
    {
      tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, username);
      identityStorage.saveIdentity(tobeSavedIdentity);
      assertNotNull("tobeSavedIdentity.getId() must not be null.", tobeSavedIdentity.getId());
      assertNull("tobeSavedIdentity.getProfile().getId() must be null.", tobeSavedIdentity.getProfile().getId());
      identityStorage.loadProfile(tobeSavedIdentity.getProfile());
      assertNotNull("tobeSavedIdentity.getProfile().getId() must not be null", tobeSavedIdentity.getProfile().getId());

      identityStorage.deleteIdentity(tobeSavedIdentity);
      assertNotNull("tobeSavedIdentity.getId() must not be null", tobeSavedIdentity.getId());
      assertNull("must return null with identityId: " + tobeSavedIdentity.getId(), identityStorage.findIdentityById(tobeSavedIdentity.getId()));

    }
  }

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

  }

  public void testGetAllIdentities() {
    List<Identity> emptyIdentityList = identityStorage.getAllIdentities();
    assertEquals(0, emptyIdentityList.size());
    final String prefixUserName = "user";
    final String providerId = "providerTest";
    final int numberOfIdentities = 30;
    for (int i = 0; i < numberOfIdentities; i++) {
      Identity toSaveIdentity = new Identity(providerId, prefixUserName + i);
      identityStorage.saveIdentity(toSaveIdentity);
    }

    List<Identity> gotIdentityList = identityStorage.getAllIdentities();

    assertNotNull(gotIdentityList);
    assertEquals(numberOfIdentities, gotIdentityList.size());

  }

  public void testFindIdentity() {
    final String userName = "username";

    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, userName);
    identityStorage.saveIdentity(tobeSavedIdentity);

    Identity foundIdentity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userName);

    assertNotNull(foundIdentity);
    assertNotNull(foundIdentity.getId());
    assertEquals(OrganizationIdentityProvider.NAME, foundIdentity.getProviderId());
    assertEquals(userName, foundIdentity.getRemoteId());
  }

  public void testGetIdentitiesByProfileFilter() {
    //TODO hoatle complete testGetIdentitiesByProfileFilter
    assert true;
  }

  public void testGetIdentitiesFilterByAlphaBet() {
    //TODO hoatle complete testGetIdentitiesFilterByAlphaBet
    assert true;
  }

  public void testSaveProfile() {
    final String userName = "username";
    final String firstName = "FirstName";
    final String lastName = "LastName";
    final String avatarUrl = "http://localhost:8080/rest-socialdemo/username/avatar.jpg";
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, userName);
    identityStorage.saveIdentity(tobeSavedIdentity);

    Profile tobeSavedProfile = tobeSavedIdentity.getProfile();

    tobeSavedProfile.setProperty(Profile.USERNAME, userName);
    tobeSavedProfile.setProperty(Profile.FIRST_NAME, firstName);
    tobeSavedProfile.setProperty(Profile.LAST_NAME, lastName);
    tobeSavedProfile.setProperty(Profile.AVATAR_URL, avatarUrl);

    assertTrue(tobeSavedProfile.hasChanged());
    identityStorage.saveProfile(tobeSavedProfile);
    assertFalse(tobeSavedProfile.hasChanged());

    assertNotNull(tobeSavedProfile.getId());

    assertEquals(userName, tobeSavedProfile.getProperty(Profile.USERNAME));
    assertEquals(firstName, tobeSavedProfile.getProperty(Profile.FIRST_NAME));
    assertEquals(lastName, tobeSavedProfile.getProperty(Profile.LAST_NAME));
    assertEquals(avatarUrl, tobeSavedProfile.getProperty(Profile.AVATAR_URL));
    assertEquals(firstName + " " + lastName, tobeSavedProfile.getFullName());
  }

  public void testLoadProfile() {
    final String username = "username";
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, username);
    identityStorage.saveIdentity(tobeSavedIdentity);
    Profile tobeSavedProfile = tobeSavedIdentity.getProfile();
    tobeSavedProfile.setProperty(Profile.USERNAME, username);

    assertTrue(tobeSavedProfile.hasChanged());
    identityStorage.loadProfile(tobeSavedProfile);
    assertFalse(tobeSavedProfile.hasChanged());

    assertNotNull(tobeSavedProfile.getId());
    assertEquals(username, tobeSavedProfile.getProperty(Profile.USERNAME));

    SocialDataLocation dataLocation = (SocialDataLocation) getContainer().getComponentInstanceOfType(SocialDataLocation.class);
    JCRSessionManager sessionManager = dataLocation.getSessionManager();

    //query created profile
    try {
      final Session session = sessionManager.getOrOpenSession();
      final List<Node> nodes = new QueryBuilder(session)
        .select(IdentityStorage.PROFILE_NODETYPE).exec();

      assertEquals(1, nodes.size());

      final Node profileNode = nodes.get(0);
      final Node identityNode = profileNode.getProperty(IdentityStorage.PROFILE_IDENTITY).getNode();
      final Identity identityByProfile = identityStorage.getIdentity(identityNode);
      assertEquals(tobeSavedIdentity.getId(), identityByProfile.getId());
    } catch (RepositoryException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      sessionManager.closeSession();
    }
  }


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
      identityStorage.loadProfile(profile);
      assertFalse(profile.hasChanged());
      profileId = profile.getId();
    }

    //here is the testcase
    {
      Profile profile = new Profile(identity);
      assertFalse(profile.hasChanged());
      identityStorage.loadProfile(profile);
      assertFalse(profile.hasChanged());
      assertEquals(profileId, profile.getId());
    }
  }

  public void testFindIdentityByExistName() throws Exception {
    String providerId = "organization";
    String remoteId = "username";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName");
    identityStorage.saveProfile(profile);

    final ProfileFilter filter = new ProfileFilter();
    filter.setName("First");
    final List<Identity> result = identityStorage.getIdentitiesFilterByAlphaBet(providerId, filter, 0, 1);
    assertEquals(1, result.size());
  }

  public void testFindManyIdentitiesByExistName() throws Exception {
    final String providerId = "organization";

    final int total = 10;
    for (int i = 0; i <  total; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId+i);
      identityStorage.saveIdentity(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName"+ i);
      identityStorage.saveProfile(profile);
    }

    final ProfileFilter filter = new ProfileFilter();
    filter.setName("FirstName");
    final List<Identity> result = identityStorage.getIdentitiesFilterByAlphaBet(providerId, filter, 0, total);
    assertEquals(total, result.size());
  }

  public void testFindIdentityByNotExistName() throws Exception {
    String providerId = "organization";
    String remoteId = "username";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName");
    identityStorage.saveProfile(profile);

    final ProfileFilter filter = new ProfileFilter();
    filter.setName("notfound");
    final List<Identity> result = identityStorage.getIdentitiesFilterByAlphaBet(providerId, filter, 0, 1);
    assertEquals(0, result.size());
  }

  public void testFindIdentityByProfileFilter() throws Exception {
    String providerId = "organization";
    String remoteId = "username";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName");
    profile.setProperty("postition", "developer");
    profile.setProperty("gender", "male");

    identityStorage.saveProfile(profile);

    final ProfileFilter filter = new ProfileFilter();
    filter.setPosition("developer");
    filter.setGender("male");
    filter.setName("First");
    final List<Identity> result = identityStorage.getIdentitiesFilterByAlphaBet(providerId, filter, 0, 1);
    assertEquals(1, result.size());
  }

  public void testFindManyIdentitiesByProfileFilter() throws Exception {
    String providerId = "organization";

    int total = 10;
    for (int i = 0; i < total; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId);
      identityStorage.saveIdentity(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName" + i);
      profile.setProperty("postition", "developer");
      profile.setProperty("gender", "male");

      identityStorage.saveProfile(profile);
    }

    final ProfileFilter filter = new ProfileFilter();
    filter.setPosition("developer");
    filter.setGender("male");
    filter.setName("FirstN");
    final List<Identity> result = identityStorage.getIdentitiesFilterByAlphaBet(providerId, filter, 0, total);
    assertEquals(total, result.size());
  }
}