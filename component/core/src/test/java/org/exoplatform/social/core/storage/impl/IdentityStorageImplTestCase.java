/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.core.storage.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.FakeIdentityProvider;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.cache.CachedIdentityStorage;
import org.exoplatform.social.core.storage.exception.NodeAlreadyExistsException;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;
import org.exoplatform.social.core.test.QueryNumberTest;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@QueryNumberTest
public class IdentityStorageImplTestCase extends AbstractCoreTest {
  private IdentityStorageImpl storage;
  private RelationshipStorage relationshipStorage;
  private CachedIdentityStorage cachedIdentityStorage;
  OrganizationService orgSrv;
  private ActivityStorage activityStorage;
  private SpaceStorage spaceStorage;
  private List<String> tearDownIdentityList;
  private SocialChromatticLifeCycle lifecycle;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    storage = (IdentityStorageImpl) getContainer().getComponentInstanceOfType(IdentityStorageImpl.class);
    relationshipStorage = (RelationshipStorage) getContainer().getComponentInstanceOfType(RelationshipStorageImpl.class);
    cachedIdentityStorage = (CachedIdentityStorage) getContainer().getComponentInstanceOfType(CachedIdentityStorage.class);
    orgSrv = (OrganizationService) getContainer().getComponentInstanceOfType(OrganizationService.class);
    activityStorage = (ActivityStorage) getContainer().getComponentInstanceOfType(ActivityStorageImpl.class);
    spaceStorage = (SpaceStorage) getContainer().getComponentInstanceOfType(SpaceStorageImpl.class);
    lifecycle = lifecycleLookup();
    tearDownIdentityList = new ArrayList<String>();
    assertNotNull(storage);
  }

  @Override
  public void tearDown() throws Exception {
    for (String id : tearDownIdentityList) {
      storage.deleteIdentity(new Identity(id));
    }
    
    super.tearDown();
  }
  
  private static SocialChromatticLifeCycle lifecycleLookup() {
    PortalContainer container = PortalContainer.getInstance();
    ChromatticManager manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
    return (SocialChromatticLifeCycle) manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);
  }

  @MaxQueryNumber(114)
  public void testCreateIdentitty() throws Exception {
    Identity newIdentity = new Identity("organization", "new");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("new", newIdentity.getRemoteId());
    tearDownIdentityList.add(newIdentity.getId());

    //
    newIdentity.setRemoteId("new2");
    storage._createIdentity(newIdentity);
    assertNotNull(newIdentity.getId());
    assertNotSame(generatedId, newIdentity.getId());
    generatedId = newIdentity.getId();
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("new2", newIdentity.getRemoteId());
    tearDownIdentityList.add(newIdentity.getId());
  }


  @MaxQueryNumber(72)
  public void testCreateIdentittyExits() throws Exception {
    Identity newIdentity = new Identity("organization", "newDuplicate");

    //
    storage._createIdentity(newIdentity);
    tearDownIdentityList.add(newIdentity.getId());

    //
    try {
      storage._createIdentity(newIdentity);
      fail();
    }
    catch (NodeAlreadyExistsException e) {
      // ok
    }
  }

  @MaxQueryNumber(6)
  public void testFindByIdDoesntExists() throws Exception {

    try {
      storage._findById(IdentityEntity.class, "doesn't exists");
      fail();
    }
    catch (NodeNotFoundException e) {
      // ok
    }
  }

  @MaxQueryNumber(72)
  public void testFindByIdExists() throws Exception {
    Identity newIdentity = new Identity("organization", "exists");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("exists", newIdentity.getRemoteId());

    //
    IdentityEntity got = storage._findById(IdentityEntity.class, generatedId);
    assertEquals("organization", got.getProviderId());
    assertEquals(Boolean.FALSE, got.isDeleted());
    assertEquals("exists", got.getRemoteId());

    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(366)
  public void testDeleteIdentityExists() throws Exception {
    Identity newIdentity = new Identity("organization", "newToDelete");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("newToDelete", newIdentity.getRemoteId());

    //
    storage._deleteIdentity(newIdentity);

    //
    try {
      storage._findIdentity("organization", "newToDelete");
      fail();
    }
    catch (NodeNotFoundException e) {
      // ok
    }
  }

  @MaxQueryNumber(6)
  public void testDeleteIdentityDoesntExists() throws Exception {
    Identity newIdentity = new Identity("organization", "doesn't exists");
    newIdentity.setId("fakeId");

    //
    try {
      storage._deleteIdentity(newIdentity);
      fail();
    }
    catch (NodeNotFoundException e) {
      // ok
    }
  }

  @MaxQueryNumber(0)
  public void testDeleteInvalidIdentity() throws Exception {
    Identity newIdentity = new Identity("organization", "doesn't exists");

    //
    try {
      storage._deleteIdentity(newIdentity);
      fail();
    }
    catch (IllegalArgumentException e) {
      // ok
    }

    //
    try {
      storage._deleteIdentity(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      // ok
    }
  }

  @MaxQueryNumber(144)
  public void testHardDelete() throws Exception {

    Identity user1 = new Identity("organization", "user1");

    //
    storage._createIdentity(user1);

    //
    storage._hardDeleteIdentity(user1);

    //
    Identity got = storage._findIdentity("organization", "user1");
    assertEquals(true, got.isDeleted());

    tearDownIdentityList.add(user1.getId());

  }

  @MaxQueryNumber(798)
  public void testHardDeleteRelationship() throws Exception {

    Identity user1 = new Identity("organization", "user1");
    Identity user2 = new Identity("organization", "user2");
    Identity user3 = new Identity("organization", "user3");
    Identity user4 = new Identity("organization", "user4");

    //
    storage._createIdentity(user1);
    storage._createIdentity(user2);
    storage._createIdentity(user3);
    storage._createIdentity(user4);

    //
    Relationship r1 = new Relationship(user1, user2, Relationship.Type.CONFIRMED);
    Relationship r2 = new Relationship(user1, user3, Relationship.Type.PENDING);
    Relationship r3 = new Relationship(user4, user1, Relationship.Type.PENDING);
    relationshipStorage.saveRelationship(r1);
    relationshipStorage.saveRelationship(r2);
    relationshipStorage.saveRelationship(r3);

    //
    storage._hardDeleteIdentity(user1);

    //
    Identity got = storage._findIdentity("organization", "user1");
    assertEquals(true, got.isDeleted());

    assertEquals(null, relationshipStorage.getRelationship(r1.getId()));
    assertEquals(null, relationshipStorage.getRelationship(r2.getId()));
    assertEquals(null, relationshipStorage.getRelationship(r3.getId()));

    tearDownIdentityList.add(user1.getId());
    tearDownIdentityList.add(user2.getId());
    tearDownIdentityList.add(user3.getId());
    tearDownIdentityList.add(user4.getId());


  }

  @MaxQueryNumber(573)
  public void testHardDeleteSpace() throws Exception {

    Identity user1 = new Identity("organization", "user1");
    Identity user2 = new Identity("organization", "user2");

    //
    storage._createIdentity(user1);
    storage._createIdentity(user2);

    //
    Space space = new Space();
    space.setDisplayName("space name");
    space.setPrettyName(space.getDisplayName());
    space.setMembers(new String[]{user1.getRemoteId()});
    space.setManagers(new String[]{user1.getRemoteId(), user2.getRemoteId()});
    space.setPendingUsers(new String[]{user1.getRemoteId()});
    space.setInvitedUsers(new String[]{user1.getRemoteId()});

    spaceStorage.saveSpace(space, true);

    //
    storage._hardDeleteIdentity(user1);

    //
    Identity got = storage._findIdentity("organization", "user1");
    assertEquals(true, got.isDeleted());

    assertEquals(1, spaceStorage.getSpaceById(space.getId()).getMembers().length);
    assertEquals(user2.getRemoteId(), spaceStorage.getSpaceById(space.getId()).getMembers()[0]);
    assertEquals(1, spaceStorage.getSpaceById(space.getId()).getManagers().length);
    assertEquals(user2.getRemoteId(), spaceStorage.getSpaceById(space.getId()).getManagers()[0]);
    assertEquals(0, spaceStorage.getSpaceById(space.getId()).getPendingUsers().length);
    assertEquals(0, spaceStorage.getSpaceById(space.getId()).getInvitedUsers().length);

    spaceStorage.deleteSpace(space.getId());
    tearDownIdentityList.add(user1.getId());
    tearDownIdentityList.add(user2.getId());

  }

  @MaxQueryNumber(144)
  public void testHardDeleteSpaceLastManager() throws Exception {

    Identity user1 = new Identity("organization", "user1");

    //
    storage._createIdentity(user1);

    //
    Space space = new Space();
    space.setDisplayName("space name");
    space.setPrettyName(space.getDisplayName());
    space.setManagers(new String[]{user1.getRemoteId()});

    spaceStorage.saveSpace(space, true);

    //
    Identity got = storage._findIdentity("organization", "user1");
    assertEquals(false, got.isDeleted());

    spaceStorage.deleteSpace(space.getId());
    tearDownIdentityList.add(user1.getId());

  }

  @MaxQueryNumber(78)
  public void testCreateProfile() throws Exception {
    Identity newIdentity = new Identity("organization", "identityForProfile");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("identityForProfile", newIdentity.getRemoteId());

    //
    Profile profile = new Profile(newIdentity);
    storage._createProfile(profile);
    assertNotNull(profile.getId());

    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(117)
  public void testLoadProfileExists() throws Exception {
    Identity newIdentity = new Identity("organization", "identityForLoadProfile");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("identityForLoadProfile", newIdentity.getRemoteId());

    //
    Profile profile = new Profile(newIdentity);
    Map<String, String> xp = new HashMap<String, String>();
    List<Map<String, String>> xps = new ArrayList<Map<String, String>>();
    xp.put(Profile.EXPERIENCES_SKILLS, "java scrum groovy");
    xp.put(Profile.EXPERIENCES_POSITION, "dev");
    xp.put(Profile.EXPERIENCES_COMPANY, "exo");
    xps.add(xp);
    profile.setProperty(Profile.EXPERIENCES, xps);

    storage._createProfile(profile);
    assertNotNull(profile.getId());

    //
    profile = storage._loadProfile(profile);
    assertNotNull(profile.getId());
    assertEquals("java scrum groovy", profile.getProperty("skills"));

    //
    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(60)
  public void testLoadProfileDoesntExists() throws Exception {
    Identity newIdentity = new Identity("organization", "identityForLoadProfile");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("identityForLoadProfile", newIdentity.getRemoteId());

    //
    Profile profile = new Profile(newIdentity);
    try {
      storage._loadProfile(profile);
      fail();
    }
    catch (NodeNotFoundException e) {
      // ok
    }

    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(60)
  public void testGetIdentityNoProvider() throws Exception {
    Identity newIdentity = new Identity("organization", "checkProviderNotFound");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("checkProviderNotFound", newIdentity.getRemoteId());

    //
    try {
      storage._findIdentity("providerDoesntExists", "checkProviderNotFound");
      fail();
    }
    catch (NodeNotFoundException e) {
      // ok
    }

    //
    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(60)
  public void testGetIdentityNoRemote() throws Exception {
    Identity newIdentity = new Identity("organization", "checkRemoteNotFound");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("checkRemoteNotFound", newIdentity.getRemoteId());

    //
    try {
      storage._findIdentity("organization", "not-found");
      fail();
    }
    catch (NodeNotFoundException e) {
      // ok
    }

    //
    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(60)
  public void testGetIdentity() throws Exception {
    Identity newIdentity = new Identity("organization", "remoteid");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("remoteid", newIdentity.getRemoteId());

    //
    Identity got = storage._findIdentity("organization", "remoteid");
    assertNotNull(got.getId());
    assertEquals("organization", got.getProviderId());
    assertEquals(false, got.isDeleted());
    assertEquals("remoteid", got.getRemoteId());

    //
    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(105)
  public void testProfile() throws Exception {
    Identity newIdentity = new Identity("organization", "remoteid");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("remoteid", newIdentity.getRemoteId());
    assertNotNull(newIdentity.getProfile());
    assertNull(newIdentity.getProfile().getId());

    //
    storage._createProfile(newIdentity.getProfile());
    assertNotNull(newIdentity.getProfile().getId());

    //
    Profile profile = newIdentity.getProfile();
    profile.setProperty(Profile.USERNAME, "user");
    profile.setProperty(Profile.FIRST_NAME, "first");
    profile.setProperty(Profile.LAST_NAME, "last");
    storage._saveProfile(profile);

    //
    Profile toLoadProfile = new Profile(newIdentity);
    assertNull(toLoadProfile.getProperty(Profile.USERNAME));
    assertNull(toLoadProfile.getProperty(Profile.FIRST_NAME));
    assertNull(toLoadProfile.getProperty(Profile.LAST_NAME));
    assertNull(toLoadProfile.getAvatarUrl());
    storage._loadProfile(toLoadProfile);
    assertNotNull(toLoadProfile.getId());
    assertNotNull(toLoadProfile.getProperty(Profile.USERNAME));
    assertNotNull(toLoadProfile.getProperty(Profile.FIRST_NAME));
    assertNotNull(toLoadProfile.getProperty(Profile.LAST_NAME));
    assertEquals("/portal/classic/profile/remoteid", toLoadProfile.getUrl());

    // No avatar saved
    assertNull(toLoadProfile.getAvatarUrl());

    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(189)
  public void testMoveIdentity() throws Exception {
    Identity newIdentity = new Identity("organization", "checkMove");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("checkMove", newIdentity.getRemoteId());

    //
    newIdentity.setProviderId("newProviderId");
    newIdentity.setRemoteId("newRemoteId");

    //
    assertEquals("newProviderId", newIdentity.getProviderId());
    assertEquals("newRemoteId", newIdentity.getRemoteId());

    //
    storage._saveIdentity(newIdentity);
    Identity got = storage._findIdentity(newIdentity.getProviderId(), newIdentity.getRemoteId());
    assertEquals(generatedId, got.getId());
    assertEquals("newProviderId", got.getProviderId());
    assertEquals(false, got.isDeleted());
    assertEquals("newRemoteId", got.getRemoteId());

    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(0)
  public void testGetType() throws Exception {
    assertEquals("String", storage.getType("soc:identitydefinition", "soc:providerId"));
    assertNull(storage.getType("soc:profiledefinition", "doesn't exists"));
    assertNull(storage.getType("doesn't exists", "doesn't exists"));
    assertNull(storage.getType("soc:profiledefinition", null));
    assertNull(storage.getType(null, null));
  }

  @MaxQueryNumber(195)
  public void testUpdateProfileProperties() throws Exception {
   Identity newIdentity = new Identity("organization", "checksaveprofile");

    //
   storage._createIdentity(newIdentity);
   String generatedId = newIdentity.getId();
   assertNotNull(generatedId);
   assertEquals("organization", newIdentity.getProviderId());
   assertEquals(false, newIdentity.isDeleted());
   assertEquals("checksaveprofile", newIdentity.getRemoteId());
   assertNotNull(newIdentity.getProfile());
   assertNull(newIdentity.getProfile().getId());

   //
   storage._createProfile(newIdentity.getProfile());
   assertNotNull(newIdentity.getProfile().getId());

   //
   Profile profile = newIdentity.getProfile();
   profile.setProperty(Profile.USERNAME, "user");
   profile.setProperty(Profile.FIRST_NAME, "first");
   profile.setProperty(Profile.LAST_NAME, "last");
   storage._saveProfile(profile);

   //
   Profile toLoadProfile = new Profile(newIdentity);
   assertNull(toLoadProfile.getProperty(Profile.USERNAME));
   assertNull(toLoadProfile.getProperty(Profile.FIRST_NAME));
   assertNull(toLoadProfile.getProperty(Profile.LAST_NAME));
   assertNull(toLoadProfile.getAvatarUrl());

   //
   storage._loadProfile(toLoadProfile);
   assertNotNull(toLoadProfile.getId());
   assertNotNull(toLoadProfile.getProperty(Profile.USERNAME));
   assertNotNull(toLoadProfile.getProperty(Profile.FIRST_NAME));
   assertNotNull(toLoadProfile.getProperty(Profile.LAST_NAME));

   // No avatar saved
   assertNull(toLoadProfile.getAvatarUrl());

   { // test case url property is modified when update other fields
     Identity got = storage._findIdentity(newIdentity.getProviderId(), newIdentity.getRemoteId()); 
     profile = got.getProfile();
     ArrayList<HashMap<String, String>> profileMap = new ArrayList<HashMap<String, String>>();
     HashMap<String, String> uiMap = new HashMap<String, String>();
     uiMap.put("key", "url");
     uiMap.put("value", StringEscapeUtils.escapeHtml("http://google.com"));
     profileMap.add(uiMap);
     profile.setProperty(Profile.CONTACT_URLS, profileMap);
     storage._saveProfile(profile);
     
     //
     Identity got1 = storage._findIdentity(newIdentity.getProviderId(), newIdentity.getRemoteId());
     ArrayList<HashMap<String, String>> pr = (ArrayList<HashMap<String, String>>)got1.getProfile().getProperty(Profile.CONTACT_URLS);
     assertEquals("http://google.com", pr.get(0).get("value"));
     
     //
     Profile profile1 = got1.getProfile();
     profile1.setProperty(Profile.POSITION, "CEO");
     storage._saveProfile(profile1);
     
     //
     Identity got2 = storage._findIdentity(newIdentity.getProviderId(), newIdentity.getRemoteId());
     pr = (ArrayList<HashMap<String, String>>)got2.getProfile().getProperty(Profile.CONTACT_URLS);
     assertEquals("http://google.com", pr.get(0).get("value"));
   }
   
   //
   Profile updaterProfile = new Profile(newIdentity);
   updaterProfile.setId(toLoadProfile.getId());
   updaterProfile.setProperty(Profile.USERNAME, "updated user");
   updaterProfile.setProperty(Profile.LAST_NAME, "updated last");
   updaterProfile.setProperty(Profile.FULL_NAME, "new full");
   assertNull(toLoadProfile.getProperty(Profile.FULL_NAME));
   storage.addOrModifyProfileProperties(updaterProfile);

   //
   Profile toLoadAfterUpdateProfile = new Profile(newIdentity);
   toLoadAfterUpdateProfile = storage.loadProfile(toLoadAfterUpdateProfile);
   assertEquals("updated user", toLoadAfterUpdateProfile.getProperty(Profile.USERNAME));
   assertEquals("updated last", toLoadAfterUpdateProfile.getProperty(Profile.LAST_NAME));

   // No avatar saved
   assertNull(toLoadAfterUpdateProfile.getAvatarUrl());

   assertEquals("first", toLoadAfterUpdateProfile.getProperty(Profile.FIRST_NAME));
   assertEquals("new full", toLoadAfterUpdateProfile.getProperty(Profile.FULL_NAME));

   tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(408)
  public void testFindIdentityWithFilter() throws Exception {

    addIdentity("o", "toto", "male", "cadre");
    Identity itotota = addIdentity("o", "totota", "female", "dev");
    addIdentity("o", "tata", "male", "cadre");

    Profile profile = storage.loadProfile(new Profile(itotota));
    Map<String, String> xp = new HashMap<String, String>();
    List<Map<String, String>> xps = new ArrayList<Map<String, String>>();
    xp.put(Profile.EXPERIENCES_SKILLS, "java scrum groovy");
    xps.add(xp);
    profile.setProperty(Profile.EXPERIENCES, xps);
    storage.saveProfile(profile);

    ProfileFilter t = createFilter('\u0000', "t", "", "", null);
    ProfileFilter to = createFilter('\u0000', "to", "", "", null);
    ProfileFilter toto = createFilter('\u0000', "toto", "", "", null);
    ProfileFilter totota = createFilter('\u0000', "totota", "", "", null);
    ProfileFilter unknown = createFilter('\u0000', "unknown", "", "", null);

    ProfileFilter cadre = createFilter('\u0000', "", "", "cadre", null);
    ProfileFilter dev = createFilter('\u0000', "", "", "dev", null);

    ProfileFilter t2 = createFilter('\u0000', "t", "", "", itotota);

    ProfileFilter filterB2Skills = new ProfileFilter();
    filterB2Skills.setSkills("scrum");

    assertEquals(3, storage.getIdentitiesByProfileFilter("o", t, 0, 10, false).size());
    assertEquals(3, storage.getIdentitiesByProfileFilter("o", t, 0, 3, false).size());
    assertEquals(1, storage.getIdentitiesByProfileFilter("o", t, 0, 1, false).size());
    assertEquals(3, storage.getIdentitiesByProfileFilter("o", t, 0, 0, false).size());
    assertEquals(3, storage.getIdentitiesByProfileFilter("o", t, 0, -1, false).size());
    assertEquals(2, storage.getIdentitiesByProfileFilter("o", to, 0, 10, false).size());
    assertEquals(2, storage.getIdentitiesByProfileFilter("o", toto, 0, 10, false).size());
    assertEquals(1, storage.getIdentitiesByProfileFilter("o", totota, 0, 10, false).size());
    assertEquals(0, storage.getIdentitiesByProfileFilter("o", unknown, 0, 10, false).size());
    assertEquals(2, storage.getIdentitiesByProfileFilter("o", cadre, 0, 10, false).size());
    assertEquals(1, storage.getIdentitiesByProfileFilter("o", dev, 0, 10, false).size());

    assertEquals(2, storage.getIdentitiesByProfileFilter("o", t2, 0, 10, false).size());
    assertEquals(1, storage.getIdentitiesByProfileFilter("o", filterB2Skills, 0, 10, false).size());
    assertEquals("totota", storage.getIdentitiesByProfileFilter("o", filterB2Skills, 0, 10, false).get(0).getRemoteId());
    
    //disable user totota
    IdentityStorage identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    identityStorage.processEnabledIdentity(itotota, false);
    assertEquals(2, storage.getIdentitiesByProfileFilter("o", t, 0, 10, false).size());
    
    //enable totota
    identityStorage.processEnabledIdentity(itotota, true);
    assertEquals(3, storage.getIdentitiesByProfileFilter("o", t, 0, 10, false).size());
  }

  @MaxQueryNumber(105)
  public void testAvatar() throws Exception {
    Identity newIdentity = new Identity("organization", "remoteid");

    //
    storage._createIdentity(newIdentity);
    tearDownIdentityList.add(newIdentity.getId());
    
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("remoteid", newIdentity.getRemoteId());
    assertNotNull(newIdentity.getProfile());
    assertNull(newIdentity.getProfile().getId());

    //
    storage._createProfile(newIdentity.getProfile());
    assertNotNull(newIdentity.getProfile().getId());

    //
    Profile profile = newIdentity.getProfile();
    AvatarAttachment avatar = new AvatarAttachment();
    avatar.setMimeType("plain/text");
    avatar.setInputStream(new ByteArrayInputStream("Attachment content".getBytes()));
    profile.setProperty(Profile.AVATAR, avatar);

    //
    storage._saveProfile(profile);

    //
    Profile loadedProfile = new Profile(newIdentity);
    storage._loadProfile(loadedProfile);
    String avatarRandomURL = loadedProfile.getAvatarUrl();
    int indexOfRandomVar = avatarRandomURL.indexOf("/?upd=");
    String avatarURL = null;
    if(indexOfRandomVar != -1){
      avatarURL = avatarRandomURL.substring(0,indexOfRandomVar);
    } else {
      avatarURL = avatarRandomURL;
    }
    assertEquals(
        escapeJCRSpecialCharacters("/rest/jcr/repository/portal-test/production/soc:providers/soc:organization/soc:remoteid/soc:profile/soc:avatar"),
        avatarURL);

  }

  private Identity addIdentity(String provider, String name, String gender, String position) throws Exception {
    Identity newIdentity = new Identity(provider, name);
    storage._createIdentity(newIdentity);
    Profile p = new Profile(newIdentity);
    p.setProperty(Profile.LAST_NAME, name);
    p.setProperty(Profile.FULL_NAME, name);
    p.setProperty(Profile.GENDER, gender);
    p.setProperty(Profile.POSITION, position);
    newIdentity.setProfile(p);
    storage._createProfile(p);
    tearDownIdentityList.add(newIdentity.getId());

    return newIdentity;
  }

  private ProfileFilter createFilter(char c, String name, String gender, String position, Identity exclude) throws Exception {
    ProfileFilter filter = new ProfileFilter();
    filter.setFirstCharacterOfName(c);
    filter.setName(name);
    filter.setPosition(position);

    if (exclude != null) {
      List<Identity> excludeList = new ArrayList<Identity>();
      excludeList.add(exclude);
      filter.setExcludedIdentityList(excludeList);
    }

    return filter;
  }

  @MaxQueryNumber(159)
  public void testProfileContact() throws Exception {
    Identity newIdentity = new Identity("organization", "withcontact");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("withcontact", newIdentity.getRemoteId());
    assertNotNull(newIdentity.getProfile());
    assertNull(newIdentity.getProfile().getId());

    //
    storage._createProfile(newIdentity.getProfile());
    assertNotNull(newIdentity.getProfile().getId());

    //
    Profile profile = newIdentity.getProfile();
    profile.setProperty(Profile.USERNAME, "user");
    profile.setProperty(Profile.FIRST_NAME, "first");
    profile.setProperty(Profile.LAST_NAME, "last");
    profile.setProperty(Profile.ABOUT_ME, "About me test");

    // urls
    List<Map<String, String>> urls = new ArrayList<Map<String, String>>();
    Map<String, String> url1 = new HashMap<String, String>();
    url1.put("key", "url");
    url1.put("value", "http://www.toto.com");
    Map<String, String> url2 = new HashMap<String, String>();
    url2.put("key", "url");
    url2.put("value", "http://www.tata.com");
    urls.add(url1);
    urls.add(url2);
    profile.setProperty(Profile.CONTACT_URLS, urls);

    // ims
    List<Map<String, String>> ims = new ArrayList<Map<String, String>>();
    Map<String, String> im1 = new HashMap<String, String>();
    im1.put("key", "GTalk");
    im1.put("value", "nickname1");
    Map<String, String> im2 = new HashMap<String, String>();
    im2.put("key", "GTalk");
    im2.put("value", "nickname2");
    Map<String, String> im3 = new HashMap<String, String>();
    im3.put("key", "MSN");
    im3.put("value", "nickname3");
    ims.add(im1);
    ims.add(im2);
    ims.add(im3);
    profile.setProperty(Profile.CONTACT_IMS, ims);

    // phones
    List<Map<String, String>> phones = new ArrayList<Map<String, String>>();
    Map<String, String> phone1 = new HashMap<String, String>();
    phone1.put("key", "Work");
    phone1.put("value", "1234567890");
    Map<String, String> phone2 = new HashMap<String, String>();
    phone2.put("key", "Work");
    phone2.put("value", "2345678901");
    Map<String, String> phone3 = new HashMap<String, String>();
    phone3.put("key", "Home");
    phone3.put("value", "3456789012");
    Map<String, String> phone4 = new HashMap<String, String>();
    phone4.put("key", "Other");
    phone4.put("value", "4567890123");
    phones.add(phone1);
    phones.add(phone2);
    phones.add(phone3);
    phones.add(phone4);
    profile.setProperty(Profile.CONTACT_PHONES, phones);

    storage._saveProfile(profile);

    //
    Profile toLoadProfile = new Profile(newIdentity);
    assertNull(toLoadProfile.getProperty(Profile.USERNAME));
    assertNull(toLoadProfile.getProperty(Profile.FIRST_NAME));
    assertNull(toLoadProfile.getProperty(Profile.LAST_NAME));
    assertNull(toLoadProfile.getAvatarUrl());
    storage._loadProfile(toLoadProfile);
    assertNotNull(toLoadProfile.getId());
    assertNotNull(toLoadProfile.getProperty(Profile.USERNAME));
    assertNotNull(toLoadProfile.getProperty(Profile.FIRST_NAME));
    assertNotNull(toLoadProfile.getProperty(Profile.LAST_NAME));
    //
    assertEquals("About me test", toLoadProfile.getProperty(Profile.ABOUT_ME));

    // No avatar saved
    assertNull(toLoadProfile.getAvatarUrl());

    List<Map<String, String>> loadedIms = (List<Map<String, String>>) toLoadProfile.getProperty(Profile.CONTACT_IMS);
    List<Map<String, String>> loadedUrls = (List<Map<String, String>>) toLoadProfile.getProperty(Profile.CONTACT_URLS);
    List<Map<String, String>> loadedPhones = (List<Map<String, String>>) toLoadProfile.getProperty(Profile.CONTACT_PHONES);

    assertEquals(3, loadedIms.size());
    assertEquals(2, loadedUrls.size());
    assertEquals(4, loadedPhones.size());

    //
    profile.setProperty(Profile.CONTACT_PHONES, new ArrayList<Map<String, String>>());
    storage._saveProfile(profile);

    Profile toLoadProfile2 = new Profile(newIdentity);
    storage._loadProfile(toLoadProfile2);
    List<Map<String, String>> loadedPhones2 = (List<Map<String, String>>) toLoadProfile2.getProperty(Profile.CONTACT_PHONES);
    assertNull(loadedPhones2);

    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(300)
  public void testProfileXp() throws Exception {
    Identity newIdentity = new Identity("organization", "withxp1");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("withxp1", newIdentity.getRemoteId());
    assertNotNull(newIdentity.getProfile());
    assertNull(newIdentity.getProfile().getId());

    //
    storage._createProfile(newIdentity.getProfile());
    assertNotNull(newIdentity.getProfile().getId());

    //
    Profile profile = newIdentity.getProfile();
    profile.setProperty(Profile.USERNAME, "user");
    profile.setProperty(Profile.FIRST_NAME, "first");
    profile.setProperty(Profile.LAST_NAME, "last");

    // xps
    List<Map<String, Object>> xps = new ArrayList<Map<String, Object>>();
    Map<String, Object> xp1 = new HashMap<String, Object>();
    xp1.put(Profile.EXPERIENCES_SKILLS, "skills 1");
    xp1.put(Profile.EXPERIENCES_POSITION, "position 1");
    xp1.put(Profile.EXPERIENCES_COMPANY, "company 1");
    xp1.put(Profile.EXPERIENCES_DESCRIPTION, "description 1");
    xp1.put(Profile.EXPERIENCES_START_DATE, "01/01/2010");
    xp1.put(Profile.EXPERIENCES_END_DATE, null);
    xp1.put(Profile.EXPERIENCES_IS_CURRENT, Boolean.TRUE);
    xps.add(xp1);

    profile.setProperty(Profile.EXPERIENCES, xps);

    //
    storage._saveProfile(profile);
    
    //
    Profile toLoadProfile = new Profile(newIdentity);
    storage._loadProfile(toLoadProfile);
    List<Map<String, String>> loadedXp = (List<Map<String, String>>) toLoadProfile.getProperty(Profile.EXPERIENCES);

    assertEquals(1, loadedXp.size());
    
    //remove one
    xps.remove(xp1);
    profile.setProperty(Profile.EXPERIENCES, xps);
    storage._saveProfile(profile);

    // reload
    Profile toLoadProfile2 = new Profile(newIdentity);
    storage._loadProfile(toLoadProfile2);
    List<Map<String, String>> loadedXp2 = (List<Map<String, String>>) toLoadProfile2.getProperty(Profile.EXPERIENCES);

    assertEquals(0, loadedXp2.size());

    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(120)
  public void testProfileXpWithSkillsNull() throws Exception {
    Identity newIdentity = new Identity("organization", "withxp2");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("withxp2", newIdentity.getRemoteId());
    assertNotNull(newIdentity.getProfile());
    assertNull(newIdentity.getProfile().getId());

    //
    storage._createProfile(newIdentity.getProfile());
    assertNotNull(newIdentity.getProfile().getId());

    //
    Profile profile = newIdentity.getProfile();
    profile.setProperty(Profile.USERNAME, "user");
    profile.setProperty(Profile.FIRST_NAME, "first");
    profile.setProperty(Profile.LAST_NAME, "last");
    // xps
    List<Map<String, Object>> xps = new ArrayList<Map<String, Object>>();
    Map<String, Object> xp1 = new HashMap<String, Object>();
    xp1.put(Profile.EXPERIENCES_SKILLS, null);
    xp1.put(Profile.EXPERIENCES_POSITION, "position 1");
    xp1.put(Profile.EXPERIENCES_COMPANY, "company 1");
    xp1.put(Profile.EXPERIENCES_DESCRIPTION, "description 1");
    xp1.put(Profile.EXPERIENCES_START_DATE, "01/01/2010");
    xp1.put(Profile.EXPERIENCES_END_DATE, null);
    xp1.put(Profile.EXPERIENCES_IS_CURRENT, Boolean.TRUE);
    xps.add(xp1);
    
   
    profile.setProperty(Profile.EXPERIENCES, xps);

    //
    storage._saveProfile(profile);

    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(120)
  public void testProfileXpWithDescriptionNull() throws Exception {
    Identity newIdentity = new Identity("organization", "withxp3");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("withxp3", newIdentity.getRemoteId());
    assertNotNull(newIdentity.getProfile());
    assertNull(newIdentity.getProfile().getId());

    //
    storage._createProfile(newIdentity.getProfile());
    assertNotNull(newIdentity.getProfile().getId());

    //
    Profile profile = newIdentity.getProfile();
    profile.setProperty(Profile.USERNAME, "user");
    profile.setProperty(Profile.FIRST_NAME, "first");
    profile.setProperty(Profile.LAST_NAME, "last");

    // xps
    List<Map<String, Object>> xps = new ArrayList<Map<String, Object>>();
    Map<String, Object> xp1 = new HashMap<String, Object>();
    xp1.put(Profile.EXPERIENCES_SKILLS, "java");
    xp1.put(Profile.EXPERIENCES_POSITION, "position 1");
    xp1.put(Profile.EXPERIENCES_COMPANY, "company 1");
    xp1.put(Profile.EXPERIENCES_DESCRIPTION, null);
    xp1.put(Profile.EXPERIENCES_START_DATE, "01/01/2010");
    xp1.put(Profile.EXPERIENCES_END_DATE, null);
    xp1.put(Profile.EXPERIENCES_IS_CURRENT, Boolean.TRUE);
    xps.add(xp1);
    
   
    profile.setProperty(Profile.EXPERIENCES, xps);

    //
    storage._saveProfile(profile);

    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(120)
  public void testProfileXpWithSkillDescNull() throws Exception {
    Identity newIdentity = new Identity("organization", "withxp4");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("withxp4", newIdentity.getRemoteId());
    assertNotNull(newIdentity.getProfile());
    assertNull(newIdentity.getProfile().getId());

    //
    storage._createProfile(newIdentity.getProfile());
    assertNotNull(newIdentity.getProfile().getId());

    //
    Profile profile = newIdentity.getProfile();
    profile.setProperty(Profile.USERNAME, "user");
    profile.setProperty(Profile.FIRST_NAME, "first");
    profile.setProperty(Profile.LAST_NAME, "last");

    // xps
    List<Map<String, Object>> xps = new ArrayList<Map<String, Object>>();
    Map<String, Object> xp1 = new HashMap<String, Object>();
    xp1.put(Profile.EXPERIENCES_SKILLS, null);
    xp1.put(Profile.EXPERIENCES_POSITION, "position 1");
    xp1.put(Profile.EXPERIENCES_COMPANY, "company 1");
    xp1.put(Profile.EXPERIENCES_DESCRIPTION, null);
    xp1.put(Profile.EXPERIENCES_START_DATE, "01/01/2010");
    xp1.put(Profile.EXPERIENCES_END_DATE, null);
    xp1.put(Profile.EXPERIENCES_IS_CURRENT, Boolean.TRUE);
    xps.add(xp1);
    
   
    profile.setProperty(Profile.EXPERIENCES, xps);

    //
    storage._saveProfile(profile);

    tearDownIdentityList.add(newIdentity.getId());
  }

  @MaxQueryNumber(111)
  public void testProfileAvatarURL() throws Exception{
    Identity newIdentity = new Identity(FakeIdentityProvider.NAME, "externalIdentity");
    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals(FakeIdentityProvider.NAME, newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertNotNull(newIdentity.getProfile());
    assertNull(newIdentity.getProfile().getId());
    
    //
    storage._createProfile(newIdentity.getProfile());
    assertNotNull(newIdentity.getProfile().getId());
    
    Profile profile = newIdentity.getProfile();
    profile.setProperty(Profile.FULL_NAME, "eXo Social");
    profile.setAvatarUrl("http://avatar.com/myavatar.jpg");
    profile.setUrl("http://avatar.com/myHome");
    

    storage._saveProfile(profile);
    assertEquals("http://avatar.com/myavatar.jpg", profile.getAvatarUrl());
    assertEquals("http://avatar.com/myHome", profile.getUrl());
        
    Identity identityRecheck = storage._findIdentity(FakeIdentityProvider.NAME, newIdentity.getRemoteId());
    Profile profileRecheck = identityRecheck.getProfile();

    assertEquals("eXo Social", profileRecheck.getProperty(Profile.FULL_NAME));
    assertEquals(profile.getAvatarUrl(), profileRecheck.getAvatarUrl());
    assertEquals(profile.getUrl(), profileRecheck.getUrl());
    
    tearDownIdentityList.add(newIdentity.getId());
    
  }

  /**
   * Test with case of get identities relate to disabled user.
   * 
   */
  @MaxQueryNumber(4740)
  public void TestIdentitiesOfDisabledUser() throws Exception {
    IdentityStorage identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    final String PROVIDER_ID = "organization";
    
    { // disabled user is not included in identities counter
      Identity testIdentity = new Identity(PROVIDER_ID, "test");
      storeUserInfo(testIdentity);
      
      assertEquals(1, storage.getIdentitiesCount(PROVIDER_ID));
      
      //
      identityStorage.processEnabledIdentity(testIdentity, false);
      assertEquals(0, storage.getIdentitiesCount(PROVIDER_ID));

      //
      identityStorage.processEnabledIdentity(testIdentity, true);
      assertEquals(1, storage.getIdentitiesCount(PROVIDER_ID));
      
      removeUserInfo(testIdentity);
    }
    { // disabled users not included in connections
      Identity idA = new Identity(PROVIDER_ID, "userA");
      Identity idB = new Identity(PROVIDER_ID, "userB");
      storeUserInfo(idA, idB);
      
      assertEquals(2, storage.getIdentitiesCount(PROVIDER_ID));
      
      //
      assertEquals(0, relationshipStorage.getConnectionsCount(idA));
      assertEquals(0, relationshipStorage.getConnectionsCount(idB));
      
      //
      Relationship relationship = new Relationship(idA, idB, Relationship.Type.CONFIRMED);
      relationshipStorage.saveRelationship(relationship);

      assertEquals(1, relationshipStorage.getConnectionsCount(idA));
      assertEquals(1, relationshipStorage.getConnectionsCount(idB));
      
      // disable user idA
      identityStorage.processEnabledIdentity(idA, false);
      assertEquals(0, relationshipStorage.getConnectionsCount(idB));
      
      // enable user idA
      identityStorage.processEnabledIdentity(idA, true);
      assertEquals(1, relationshipStorage.getConnectionsCount(idA));
      assertEquals(1, relationshipStorage.getConnectionsCount(idB));
      
      //
      relationshipStorage.removeRelationship(relationship);
      removeUserInfo(idA, idB);
    }
//    { // disabled user is not included in suggestions
//      Identity idA = new Identity(PROVIDER_ID, "userA");
//      Identity idB = new Identity(PROVIDER_ID, "userB");
//      storeUserInfo(idA, idB);
//      
//      assertEquals(1, relationshipStorage.getSuggestions(idB, 0, 5).size());
//      
//      identityStorage.processEnabledIdentity(idA, false);
//      
//      IdentityEntity got = lifecycle.getSession().findById(IdentityEntity.class, idA.getId());
//      DisabledEntity mixin = lifecycle.getSession().getEmbedded(got, DisabledEntity.class);
//      assertNotNull(mixin);
//      
//      assertEquals(0, relationshipStorage.getSuggestions(idB, 0, 5).size());
//      
//      identityStorage.processEnabledIdentity(idA, true);
//      removeUserInfo(idA, idB);
//    }
    { // disabled user is not included in Invtation's list
      Identity idA = new Identity(PROVIDER_ID, "userA");
      Identity idB = new Identity(PROVIDER_ID, "userB");
      storeUserInfo(idA, idB);
      
      Relationship relationship = new Relationship(idA, idB, Relationship.Type.PENDING);
      relationshipStorage.saveRelationship(relationship);
      
      // idB
      assertEquals(1, relationshipStorage.getIncomingRelationshipsCount(idB));
      
      // disable user who makes the invitation
      identityStorage.processEnabledIdentity(idA, false);
      assertEquals(0, relationshipStorage.getIncomingRelationshipsCount(idB));
      
      // re-enable invitor 
      identityStorage.processEnabledIdentity(idA, true);
      assertEquals(1, relationshipStorage.getIncomingRelationshipsCount(idB));
      
      //
      relationshipStorage.removeRelationship(relationship);
      removeUserInfo(idA, idB);
    }
  }
  
  /**
   * The member portlet of a space filter its members by calling this method, that's why it need an unit test
   * 
   * @throws Exception
   */
  public void testGetIdentitiesForMentions() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;
    Identity identity1 = addIdentity(providerId, "user1", "male", "developer");
    Identity identity2 = addIdentity(providerId, "user2", "female", "tester");
    Identity identity3 = addIdentity(providerId, "user3", "female", "designer");
    Identity identity4 = addIdentity(providerId, "user4", "male", "leader");

    Identity identity = storage.findIdentity(providerId, "user1");
    assertNotNull(identity.getProfile());
    assertEquals(true, identity.isEnable());
    ProfileFilter filter = new ProfileFilter();
    filter.setName("u");
    filter.setCompany("");
    filter.setPosition("");
    filter.setSkills("");
    
    List<Identity> identities = storage.getIdentitiesForMentions(providerId, filter, 0, 10, true);
    assertEquals(4, identities.size());
    
    //disable user1
    IdentityStorage identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    identityStorage.processEnabledIdentity(identity1, false);
    
    identities = storage.getIdentitiesForMentions(providerId, filter, 0, 10, true);
    assertEquals(3, identities.size());
    
    //re-enable user1
    identityStorage.processEnabledIdentity(identity1, true);
    
    identities = storage.getIdentitiesForMentions(providerId, filter, 0, 10, true);
    assertEquals(4, identities.size());
  }

  private void storeUserInfo(Identity... identities) throws Exception {
    for (Identity identity : identities) {
      storage._createIdentity(identity);
      storage._createProfile(identity.getProfile());
      
      User user = orgSrv.getUserHandler().createUserInstance(identity.getRemoteId());
      user.setDisplayName(identity.getProfile().getFullName());
      user.setPassword("gtn");
      user.setFirstName(identity.getRemoteId());
      user.setLastName(identity.getRemoteId());
      user.setEmail(identity.getRemoteId() + "@gmail.com");
      orgSrv.getUserHandler().createUser(user, false);
    }
  }
  
  private void removeUserInfo(Identity... identities) throws Exception {
    for (Identity identity : identities) {
      orgSrv.getUserHandler().removeUser(identity.getRemoteId(), false);
      storage.deleteIdentity(identity);
    }
  }

  private static String escapeJCRSpecialCharacters(String string) {
    if (string == null) {
      return null;
    }
    return string.replace("[", "%5B").replace("]", "%5D").replace(":", "%3A");
  }
  
  @MaxQueryNumber(132)
  public void testSearchByPositions() throws Exception {
    Identity newIdentity = new Identity("organization", "withPositions");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("withPositions", newIdentity.getRemoteId());
    assertNotNull(newIdentity.getProfile());
    assertNull(newIdentity.getProfile().getId());
  
    //
    storage._createProfile(newIdentity.getProfile());
    assertNotNull(newIdentity.getProfile().getId());
  
    //
    Profile profile = newIdentity.getProfile();
    profile.setProperty(Profile.USERNAME, "user");
    profile.setProperty(Profile.FIRST_NAME, "first");
    profile.setProperty(Profile.LAST_NAME, "last");
    profile.setProperty(Profile.POSITION, "dev");
    // xps
    List<Map<String, Object>> xps = new ArrayList<Map<String, Object>>();
    Map<String, Object> xp1 = new HashMap<String, Object>();
    xp1.put(Profile.EXPERIENCES_SKILLS, null);
    xp1.put(Profile.EXPERIENCES_POSITION, "dev");
    xp1.put(Profile.EXPERIENCES_COMPANY, "exo");
    xp1.put(Profile.EXPERIENCES_DESCRIPTION, "description 1");
    xp1.put(Profile.EXPERIENCES_START_DATE, "01/01/2010");
    xp1.put(Profile.EXPERIENCES_END_DATE, null);
    xp1.put(Profile.EXPERIENCES_IS_CURRENT, Boolean.TRUE);
    xps.add(xp1);
         
    profile.setProperty(Profile.EXPERIENCES, xps);
  
    //
    storage._saveProfile(profile);
  
    ProfileFilter dev = createFilter('\u0000', "", "", "dev", null);
    assertEquals(1, storage.getIdentitiesByProfileFilter("organization", dev, 0, 10, false).size());
  
    tearDownIdentityList.add(newIdentity.getId());    
  }
}
