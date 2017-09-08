/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.storage;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.BaseESTest;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("deprecation")
public class IdentityStorageTestIT extends BaseESTest {
  private IdentityStorage            identityStorage;

  private OrganizationService        organizationService;

  private List<User>                 tearDownUserList;

  public void setUp() throws Exception {
    super.setUp();
    identityManager = getService(IdentityManager.class);
    identityStorage = getService(IdentityStorage.class);
    organizationService = getService(OrganizationService.class);
    relationshipManager = getService(RelationshipManager.class);
    tearDownUserList = new ArrayList<User>();
  }

  public void tearDown() throws Exception {
    for (User user : tearDownUserList) {
      organizationService.getUserHandler().removeUser(user.getUserName(), true);
    }

    super.tearDown();
  }

  public void testFindIdentityByExistNameFromIndex() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;
    String remoteId = "username";

    User user = organizationService.getUserHandler().createUserInstance(remoteId);
    user.setFirstName("FirstName");
    user.setLastName("LastName");
    user.setEmail("user@exemple.com");
    user.setPassword("testuser");
    user.setCreatedDate(new Date());
    organizationService.getUserHandler().createUser(user, true);
    tearDownUserList.add(user);

    Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);

    Profile profile = identity.getProfile();

    profile.setProperty(Profile.FIRST_NAME, "FirstName");
    profile.setProperty(Profile.LAST_NAME, "LastName");
    profile.setProperty(Profile.FULL_NAME, "FirstName" + " " + "LastName");
    identityStorage.updateProfile(profile);
    identity.setProfile(profile);

    reindexProfileById(identity.getId());

    final ProfileFilter filter = new ProfileFilter();
    filter.setName("First");

    List<Identity> result = identityStorage.getIdentitiesForMentions(providerId, filter, null, 0, 10, false);
    assertEquals("identityStorage.getIdentitiesForMentions doesn't return the exact result", 1, result.size());
    assertEquals("identityStorage.getIdentitiesForMentionsCount doesn't return the exact result",
                 1,
                 identityStorage.getIdentitiesForMentionsCount(providerId, filter, null));

    result = identityStorage.getIdentitiesForUnifiedSearch(providerId, filter, 0, 1);
    assertEquals("identityStorage.getIdentitiesForUnifiedSearch doesn't return the exact result", 1, result.size());
  }

  public void testFindManyIdentitiesByExistNameFromIndex() throws Exception {
    final String providerId = OrganizationIdentityProvider.NAME;

    final int total = 10;
    for (int i = 0; i < total; i++) {
      String remoteId = "username" + i;
      User user = organizationService.getUserHandler().createUserInstance(remoteId);
      user.setFirstName("FirstName" + i);
      user.setLastName("LastName" + i);
      user.setEmail("user" + i + "@exemple.com");
      user.setPassword("testuser");
      user.setCreatedDate(new Date());
      organizationService.getUserHandler().createUser(user, true);
      tearDownUserList.add(user);

      Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);

      Profile profile = identity.getProfile();
      profile.setProperty(Profile.FIRST_NAME, "FirstName" + i);
      profile.setProperty(Profile.LAST_NAME, "LastName" + i);
      profile.setProperty(Profile.FULL_NAME, "FirstName" + i + " " + "LastName" + i);
      identityManager.updateProfile(profile);

      reindexProfileById(identity.getId());
    }

    final ProfileFilter filter = new ProfileFilter();
    filter.setName("FirstName");
    List<Identity> result = identityStorage.getIdentitiesForMentions(providerId, filter, null, 0, total, false);
    assertEquals("identityStorage.getIdentitiesForMentions doesn't return the exact result", total, result.size());
    assertEquals("identityStorage.getIdentitiesForMentionsCount doesn't return the exact result",
                 total,
                 identityStorage.getIdentitiesForMentionsCount(providerId, filter, null));
    result = identityStorage.getIdentitiesForUnifiedSearch(providerId, filter, 0, total);
    assertEquals("identityStorage.getIdentitiesForUnifiedSearch doesn't return the exact result", total, result.size());
  }

  public void testFindIdentityByNotExistNameFromIndex() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;
    String remoteId = "username";

    User user = organizationService.getUserHandler().createUserInstance(remoteId);

    user.setFirstName("FirstName");
    user.setLastName("LastName");
    user.setDisplayName("FirstName LastName");
    user.setEmail("user@exemple.com");
    user.setPassword("testuser");
    user.setCreatedDate(new Date());
    organizationService.getUserHandler().createUser(user, true);
    tearDownUserList.add(user);

    Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);

    Profile profile = identity.getProfile();
    profile.setProperty(Profile.FIRST_NAME, "FirstName");
    profile.setProperty(Profile.LAST_NAME, "LastName");
    profile.setProperty(Profile.FULL_NAME, "FirstName" + " " + "LastName");
    identityManager.updateProfile(profile);

    reindexProfileById(identity.getId());

    final ProfileFilter filter = new ProfileFilter();
    filter.setName("notfound");
    List<Identity> result = identityStorage.getIdentitiesForMentions(providerId, filter, null, 0, 1, false);
    assertEquals(0, result.size());
    result = identityStorage.getIdentitiesForUnifiedSearch(providerId, filter, 0, 1);
    assertEquals(0, result.size());
  }

  /**
   * Tests
   * {@link IdentityStorage#getIdentitiesForMentions(String, ProfileFilter, Relationship.Type, int, int, boolean)}
   */
  public void testFindIdentityForMentions() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;
    String remoteId = "username";

    final ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setPosition("developer");
    profileFilter.setName("prenom");


    refreshProfileIndices();

    assertEquals("Elastic Search should be empty before starting test",
                 0,
                 identityStorage.getIdentitiesForMentionsCount(providerId, profileFilter, null));

    User user = organizationService.getUserHandler().createUserInstance(remoteId);
    user.setFirstName("Prénom");
    user.setLastName("Nom");
    user.setEmail("user@exemple.com");
    user.setPassword("testuser");
    user.setCreatedDate(new Date());
    organizationService.getUserHandler().createUser(user, true);
    tearDownUserList.add(user);

    Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);

    Profile profile = identity.getProfile();
    profile.setProperty(Profile.FIRST_NAME, "Prénom");
    profile.setProperty(Profile.LAST_NAME, "LastName");
    profile.setProperty(Profile.FULL_NAME, "Prénom" + " " + "LastName");
    profile.setProperty("position", "developer");
    profile.setProperty("gender", "male");

    identityManager.updateProfile(profile);

    String id = identity.getId();

    reindexProfileById(id);

    assertEquals(1, identityStorage.getIdentitiesForMentions(providerId, profileFilter, null, 0, 10, false).size());

    // create a new identity
    Identity test2Identity = populateIdentity("test2", false);

    profileFilter.setPosition(null);
    profileFilter.setName(null);

    // check when new identity is not deleted
    List<Identity> foundIdentities = identityStorage.getIdentitiesForMentions(providerId, profileFilter, null, 0, 10, false);
    assertEquals("getIdentitiesForMentions must return 2 identities", 2, foundIdentities.size());

    foundIdentities = identityStorage.getIdentitiesForUnifiedSearch(providerId, profileFilter, 0, 10);
    assertEquals("getIdentitiesForUnifiedSearch must return 2 identities", 2, foundIdentities.size());

    // finds the second one
    profileFilter.setName("g");
    foundIdentities = identityStorage.getIdentitiesForMentions(providerId, profileFilter, null, 0, 10, false);
    assertEquals("getIdentitiesForMentions must return 1 identity", 1, foundIdentities.size());

    foundIdentities = identityStorage.getIdentitiesForUnifiedSearch(providerId, profileFilter, 0, 10);
    assertEquals("getIdentitiesForUnifiedSearch must return 1 identity", 1, foundIdentities.size());

    // check when new identity is deleted
    identityStorage.deleteIdentity(test2Identity);

    unindexProfileById(test2Identity.getId());

    foundIdentities = identityStorage.getIdentitiesForMentions(providerId, profileFilter, null, 0, 10, false);
    assertEquals("getIdentitiesForUnifiedSearch must not return an identity", 0, foundIdentities.size());

    foundIdentities = identityStorage.getIdentitiesForUnifiedSearch(providerId, profileFilter, 0, 10);
    assertEquals("foundIdentities.size() must not return an identity", 0, foundIdentities.size());
  }

  /**
   * Tests
   * {@link IdentityStorage#getIdentitiesForMentions(String, ProfileFilter, Relationship.Type, int, int, boolean)}
   */
  public void testFindManyIdentitiesForMentions() throws Exception {

    String providerId = OrganizationIdentityProvider.NAME;

    final ProfileFilter filter = new ProfileFilter();
    filter.setPosition("developer");
    filter.setName("prenom");

    refreshProfileIndices();

    assertEquals("Elastic Search should be empty before starting test",
                 0,
                 identityStorage.getIdentitiesForMentionsCount(providerId, filter, null));

    int total = 10;
    for (int i = 0; i < total; i++) {
      String remoteId = "username" + i;
      User user = organizationService.getUserHandler().createUserInstance(remoteId);
      user.setFirstName("Prénom" + i);
      user.setLastName("Nom" + i);
      user.setEmail("user" + i + "@exemple.com");
      user.setPassword("testuser");
      user.setCreatedDate(new Date());
      organizationService.getUserHandler().createUser(user, true);
      tearDownUserList.add(user);

      Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);

      Profile profile = identity.getProfile();
      profile.setProperty(Profile.FIRST_NAME, "Prénom" + i);

      profile.setProperty(Profile.LAST_NAME, "LastName");
      profile.setProperty(Profile.FULL_NAME, "Prénom" + i + " " + "LastName" + i);
      profile.setProperty(Profile.POSITION, "developer");
      profile.setProperty(Profile.GENDER, "male");
      identityManager.updateProfile(profile);

      reindexProfileById(identity.getId());
    }

    assertEquals(total, identityStorage.getIdentitiesForMentions(providerId, filter, null, 0, total, false).size());
  }

  /**
   * Tests
   * {@link IdentityStorage#getIdentitiesForMentionsCount(String, ProfileFilter, Relationship.Type)}
   */
  public void testCountIdentitiesForMentions() throws Exception {

    String providerId = OrganizationIdentityProvider.NAME;

    Identity identity1 = null;
    int total = 10;
    int incoming = 0;
    int outcoming = 0;
    int connections = 0;

    for (int i = 0; i < total; i++) {
      String remoteId = "username" + i;
      User user = organizationService.getUserHandler().createUserInstance(remoteId);
      user.setFirstName("Prénom" + i);
      user.setLastName("Nom" + i);
      user.setEmail("user" + i + "@exemple.com");
      user.setPassword("testuser");
      user.setCreatedDate(new Date());
      organizationService.getUserHandler().createUser(user, true);
      tearDownUserList.add(user);

      Identity identity = identityManager.getOrCreateIdentity(providerId, remoteId, true);
      if (identity1 == null) {
        identity1 = identity;
      } else {
        if (i % 3 == 0) {
          relationshipManager.inviteToConnect(identity1, identity);
          outcoming++;
        } else if (i % 3 == 1) {
          relationshipManager.inviteToConnect(identity, identity1);
          incoming++;
        } else {
          relationshipManager.inviteToConnect(identity, identity1);
          relationshipManager.confirm(identity1, identity);
          connections++;
        }
      }

      Profile profile = identity.getProfile();
      profile.setProperty(Profile.FIRST_NAME, "Prénom" + i);

      profile.setProperty(Profile.LAST_NAME, "LastName");
      profile.setProperty(Profile.FULL_NAME, "Prénom" + i + " " + "LastName" + i);
      profile.setProperty(Profile.POSITION, "developer");
      profile.setProperty(Profile.GENDER, "male");
      identityManager.updateProfile(profile);

      reindexProfileById(identity.getId());
    }

    final ProfileFilter filter = new ProfileFilter();
    filter.setPosition("developer");
    filter.setName("prenom");

    assertEquals(total, identityStorage.getIdentitiesForMentionsCount(providerId, filter, null));

    filter.setViewerIdentity(identity1);
    assertEquals(connections,
                 identityStorage.getIdentitiesForMentionsCount(providerId,
                                                               filter,
                                                               org.exoplatform.social.core.relationship.model.Relationship.Type.CONFIRMED));
    assertEquals(outcoming,
                 identityStorage.getIdentitiesForMentionsCount(providerId,
                                                               filter,
                                                               org.exoplatform.social.core.relationship.model.Relationship.Type.OUTGOING));
    assertEquals(incoming,
                 identityStorage.getIdentitiesForMentionsCount(providerId,
                                                               filter,
                                                               org.exoplatform.social.core.relationship.model.Relationship.Type.INCOMING));
  }

  /**
   * Populates one identity with remoteId.
   *
   * @param remoteId
   * @param addedToTearDown
   * @return
   * @throws IOException 
   */
  private Identity populateIdentity(String remoteId, boolean addedToTearDown) throws IOException {
    String providerId = "organization";
    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, remoteId);
    profile.setProperty(Profile.LAST_NAME, "gtn");
    profile.setProperty(Profile.FULL_NAME, remoteId + " " + "gtn");
    profile.setProperty(Profile.POSITION, "developer");
    profile.setProperty(Profile.GENDER, "male");
    identityStorage.saveProfile(profile);

    identity.setProfile(profile);

    reindexProfileById(identity.getId());
    return identity;
  }
}
