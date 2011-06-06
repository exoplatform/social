/*
* Copyright (C) 2003-2009 eXo Platform SAS.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.exoplatform.social.core.storage;

import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.storage.exception.NodeAlreadyExistsException;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.test.AbstractCoreTest;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class IdentityStorageNewTestCase extends AbstractCoreTest {
  private IdentityStorage storage;
  private List<String> tearDownIdentityList;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
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

  public void testFindByIdDoesntExists() throws Exception {

    try {
      storage._findById(IdentityEntity.class, "doesn't exists");
      fail();
    }
    catch (NodeNotFoundException e) {
      // ok
    }
  }

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
    storage._createProfile(profile);
    assertNotNull(profile.getId());

    //
    storage._loadProfile(profile);
    assertNotNull(profile.getId());

    //
    tearDownIdentityList.add(newIdentity.getId());
  }

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
    profile.setProperty(Profile.AVATAR_URL, "avatarurl");
    storage._saveProfile(profile);

    //
    Profile toLoadProfile = new Profile(newIdentity);
    assertNull(toLoadProfile.getProperty(Profile.USERNAME));
    assertNull(toLoadProfile.getProperty(Profile.FIRST_NAME));
    assertNull(toLoadProfile.getProperty(Profile.LAST_NAME));
    assertNull(toLoadProfile.getProperty(Profile.AVATAR_URL));
    storage._loadProfile(toLoadProfile);
    assertNotNull(toLoadProfile.getId());
    assertNotNull(toLoadProfile.getProperty(Profile.USERNAME));
    assertNotNull(toLoadProfile.getProperty(Profile.FIRST_NAME));
    assertNotNull(toLoadProfile.getProperty(Profile.LAST_NAME));
    assertNotNull(toLoadProfile.getProperty(Profile.AVATAR_URL));

    tearDownIdentityList.add(newIdentity.getId());
  }

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

  public void testGetType() throws Exception {
    assertEquals("String", storage.getType("soc:identitydefinition", "soc:providerId"));
    assertNull(storage.getType("soc:profiledefinition", "doesn't exists"));
    assertNull(storage.getType("doesn't exists", "doesn't exists"));
    assertNull(storage.getType("soc:profiledefinition", null));
    assertNull(storage.getType(null, null));
  }

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
   profile.setProperty(Profile.AVATAR_URL, "avatarurl");
   storage._saveProfile(profile);

   //
   Profile toLoadProfile = new Profile(newIdentity);
   assertNull(toLoadProfile.getProperty(Profile.USERNAME));
   assertNull(toLoadProfile.getProperty(Profile.FIRST_NAME));
   assertNull(toLoadProfile.getProperty(Profile.LAST_NAME));
   assertNull(toLoadProfile.getProperty(Profile.AVATAR_URL));

   //
   storage._loadProfile(toLoadProfile);
   assertNotNull(toLoadProfile.getId());
   assertNotNull(toLoadProfile.getProperty(Profile.USERNAME));
   assertNotNull(toLoadProfile.getProperty(Profile.FIRST_NAME));
   assertNotNull(toLoadProfile.getProperty(Profile.LAST_NAME));
   assertNotNull(toLoadProfile.getProperty(Profile.AVATAR_URL));

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
   storage.loadProfile(toLoadAfterUpdateProfile);
   assertEquals("updated user", toLoadAfterUpdateProfile.getProperty(Profile.USERNAME));
   assertEquals("updated last", toLoadAfterUpdateProfile.getProperty(Profile.LAST_NAME));
   assertEquals("avatarurl", toLoadAfterUpdateProfile.getProperty(Profile.AVATAR_URL));
   assertEquals("first", toLoadAfterUpdateProfile.getProperty(Profile.FIRST_NAME));
   assertEquals("new full", toLoadAfterUpdateProfile.getProperty(Profile.FULL_NAME));

   tearDownIdentityList.add(newIdentity.getId());
  }

  public void testFindIdentityByFirstCharCount() throws Exception {

    addIdentity("o", "a1", "male", "");
    Identity a2 = addIdentity("o", "a2", "male", "");
    addIdentity("o", "a3", "male", "");
    addIdentity("o", "a4", "male", "");
    addIdentity("o", "b1", "male", "");
    addIdentity("o", "b2", "male", "");
    addIdentity("o", "b3", "male", "");
    addIdentity("o", "z", "male", "");

    ProfileFilter filterA = createFilter('a', "", "", "", null);
    ProfileFilter filterB = createFilter('b', "", "", "", null);
    ProfileFilter filterC = createFilter('c', "", "", "", null);
    ProfileFilter filterZ = createFilter('z', "", "", "", null);
    ProfileFilter filterA2 = createFilter('a', "", "", "", a2);

    assertEquals(4, storage.getIdentitiesByFirstCharacterOfNameCount("o", filterA));
    assertEquals(3, storage.getIdentitiesByFirstCharacterOfNameCount("o", filterB));
    assertEquals(0, storage.getIdentitiesByFirstCharacterOfNameCount("o", filterC));
    assertEquals(1, storage.getIdentitiesByFirstCharacterOfNameCount("o", filterZ));
    assertEquals(3, storage.getIdentitiesByFirstCharacterOfNameCount("o", filterA2));
  }

  public void testFindIdentityByFirstChar() throws Exception {

    addIdentity("o", "a1", "", "");
    addIdentity("o", "a2", "", "");
    addIdentity("o", "a3", "", "");
    addIdentity("o", "a4", "", "");
    addIdentity("o", "b1", "", "");
    Identity b2 = addIdentity("o", "b2", "", "");
    addIdentity("o", "b3", "", "");
    addIdentity("o", "z", "", "");

    ProfileFilter filterA = createFilter('a', "", "", "", null);
    ProfileFilter filterB = createFilter('b', "", "", "", null);
    ProfileFilter filterC = createFilter('c', "", "", "", null);
    ProfileFilter filterZ = createFilter('z', "", "", "", null);
    ProfileFilter filterB2 = createFilter('b', "", "", "", b2);

    assertEquals(4, storage.getIdentitiesByFirstCharacterOfName("o", filterA, 0, -1, false).size());
    assertEquals(4, storage.getIdentitiesByFirstCharacterOfName("o", filterA, 0, 4, false).size());
    assertEquals(4, storage.getIdentitiesByFirstCharacterOfName("o", filterA, 0, 4, false).size());
    assertEquals(3, storage.getIdentitiesByFirstCharacterOfName("o", filterA, 0, 3, false).size());
    assertEquals(4, storage.getIdentitiesByFirstCharacterOfName("o", filterA, 0, 10, false).size());
    assertEquals(3, storage.getIdentitiesByFirstCharacterOfName("o", filterB, 0, 10, false).size());
    assertEquals(0, storage.getIdentitiesByFirstCharacterOfName("o", filterC, 0, 10, false).size());
    assertEquals(1, storage.getIdentitiesByFirstCharacterOfName("o", filterZ, 0, 10, false).size());
    assertEquals(2, storage.getIdentitiesByFirstCharacterOfName("o", filterB2, 0, 10, false).size());
  }

  public void testFindIdentityWithFilterCount() throws Exception {

    addIdentity("o", "toto", "male", "cadre");
    Identity itotota = addIdentity("o", "totota", "female", "dev");
    addIdentity("o", "tata", "male", "cadre");

    ProfileFilter t = createFilter('\u0000', "t", "", "", null);
    ProfileFilter to = createFilter('\u0000', "to", "", "", null);
    ProfileFilter toto = createFilter('\u0000', "toto", "", "", null);
    ProfileFilter totota = createFilter('\u0000', "totota", "", "", null);
    ProfileFilter unknown = createFilter('\u0000', "unknown", "", "", null);

    ProfileFilter male = createFilter('\u0000', "", "male", "", null);
    ProfileFilter female = createFilter('\u0000', "", "female", "", null);

    ProfileFilter cadre = createFilter('\u0000', "", "", "cadre", null);
    ProfileFilter dev = createFilter('\u0000', "", "", "dev", null);

    ProfileFilter tmale = createFilter('\u0000', "t", "male", "", null);
    ProfileFilter tmaledev = createFilter('\u0000', "t", "male", "dev", null);
    ProfileFilter tmalecadre = createFilter('\u0000', "t", "male", "cadre", null);

    ProfileFilter t2 = createFilter('\u0000', "t", "", "", itotota);


    assertEquals(3, storage.getIdentitiesByProfileFilterCount("o", t));
    assertEquals(2, storage.getIdentitiesByProfileFilterCount("o", to));
    assertEquals(2, storage.getIdentitiesByProfileFilterCount("o", toto));
    assertEquals(1, storage.getIdentitiesByProfileFilterCount("o", totota));
    assertEquals(0, storage.getIdentitiesByProfileFilterCount("o", unknown));
    assertEquals(2, storage.getIdentitiesByProfileFilterCount("o", male));
    assertEquals(1, storage.getIdentitiesByProfileFilterCount("o", female));
    assertEquals(2, storage.getIdentitiesByProfileFilterCount("o", cadre));
    assertEquals(1, storage.getIdentitiesByProfileFilterCount("o", dev));
    assertEquals(2, storage.getIdentitiesByProfileFilterCount("o", tmale));
    assertEquals(0, storage.getIdentitiesByProfileFilterCount("o", tmaledev));
    assertEquals(2, storage.getIdentitiesByProfileFilterCount("o", tmalecadre));

    assertEquals(2, storage.getIdentitiesByProfileFilterCount("o", t2));
  }

  public void testFindIdentityWithFilter() throws Exception {

    addIdentity("o", "toto", "male", "cadre");
    Identity itotota = addIdentity("o", "totota", "female", "dev");
    addIdentity("o", "tata", "male", "cadre");

    ProfileFilter t = createFilter('\u0000', "t", "", "", null);
    ProfileFilter to = createFilter('\u0000', "to", "", "", null);
    ProfileFilter toto = createFilter('\u0000', "toto", "", "", null);
    ProfileFilter totota = createFilter('\u0000', "totota", "", "", null);
    ProfileFilter unknown = createFilter('\u0000', "unknown", "", "", null);

    ProfileFilter male = createFilter('\u0000', "", "male", "", null);
    ProfileFilter female = createFilter('\u0000', "", "female", "", null);

    ProfileFilter cadre = createFilter('\u0000', "", "", "cadre", null);
    ProfileFilter dev = createFilter('\u0000', "", "", "dev", null);

    ProfileFilter tmale = createFilter('\u0000', "t", "male", "", null);
    ProfileFilter tmaledev = createFilter('\u0000', "t", "male", "dev", null);
    ProfileFilter tmalecadre = createFilter('\u0000', "t", "male", "cadre", null);

    ProfileFilter t2 = createFilter('\u0000', "t", "", "", itotota);


    assertEquals(3, storage.getIdentitiesByProfileFilter("o", t, 0, 10, false).size());
    assertEquals(3, storage.getIdentitiesByProfileFilter("o", t, 0, 3, false).size());
    assertEquals(1, storage.getIdentitiesByProfileFilter("o", t, 0, 1, false).size());
    assertEquals(3, storage.getIdentitiesByProfileFilter("o", t, 0, 0, false).size());
    assertEquals(3, storage.getIdentitiesByProfileFilter("o", t, 0, -1, false).size());
    assertEquals(2, storage.getIdentitiesByProfileFilter("o", to, 0, 10, false).size());
    assertEquals(2, storage.getIdentitiesByProfileFilter("o", toto, 0, 10, false).size());
    assertEquals(1, storage.getIdentitiesByProfileFilter("o", totota, 0, 10, false).size());
    assertEquals(0, storage.getIdentitiesByProfileFilter("o", unknown, 0, 10, false).size());
    assertEquals(2, storage.getIdentitiesByProfileFilter("o", male, 0, 10, false).size());
    assertEquals(1, storage.getIdentitiesByProfileFilter("o", female, 0, 10, false).size());
    assertEquals(2, storage.getIdentitiesByProfileFilter("o", cadre, 0, 10, false).size());
    assertEquals(1, storage.getIdentitiesByProfileFilter("o", dev, 0, 10, false).size());
    assertEquals(2, storage.getIdentitiesByProfileFilter("o", tmale, 0, 10, false).size());
    assertEquals(0, storage.getIdentitiesByProfileFilter("o", tmaledev, 0, 10, false).size());
    assertEquals(2, storage.getIdentitiesByProfileFilter("o", tmalecadre, 0, 10, false).size());

    assertEquals(2, storage.getIdentitiesByProfileFilter("o", t2, 0, 10, false).size());
  }

  public void testAvatar() throws Exception {
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
    AvatarAttachment avatar = new AvatarAttachment();
    avatar.setMimeType("plain/text");
    avatar.setInputStream(new ByteArrayInputStream("Attachment content".getBytes()));
    profile.setProperty(Profile.AVATAR, avatar);

    //
    storage._saveProfile(profile);

    //
    Profile loadedProfile = new Profile(newIdentity);
    storage._loadProfile(loadedProfile);
    assertEquals(
        "/rest/jcr/repository/social/production/soc:providers/soc:organization/soc:remoteid/soc:profile/soc:avatar",
        loadedProfile.getAvatarUrl()
    );

    
  }

  private Identity addIdentity(String provider, String name, String gender, String position) throws Exception {
    Identity newIdentity = new Identity(provider, name);
    storage._createIdentity(newIdentity);
    Profile p = new Profile(newIdentity);
    p.setProperty(Profile.FIRST_NAME, name);
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
    filter.setGender(gender);
    filter.setPosition(position);

    if (exclude != null) {
      List<Identity> excludeList = new ArrayList<Identity>();
      excludeList.add(exclude);
      filter.setExcludedIdentityList(excludeList);
    }

    return filter;
  }

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
    profile.setProperty(Profile.AVATAR_URL, "avatarurl");

    // urls
    List<Map<String, String>> urls = new ArrayList<Map<String, String>>();
    Map<String, String> url1 = new HashMap<String, String>();
    url1.put("key", "http://www.toto.com");
    url1.put("value", "http://www.toto.com");
    Map<String, String> url2 = new HashMap<String, String>();
    url2.put("key", "http://www.tata.com");
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
    assertNull(toLoadProfile.getProperty(Profile.AVATAR_URL));
    storage._loadProfile(toLoadProfile);
    assertNotNull(toLoadProfile.getId());
    assertNotNull(toLoadProfile.getProperty(Profile.USERNAME));
    assertNotNull(toLoadProfile.getProperty(Profile.FIRST_NAME));
    assertNotNull(toLoadProfile.getProperty(Profile.LAST_NAME));
    assertNotNull(toLoadProfile.getProperty(Profile.AVATAR_URL));

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

  public void testProfileXp() throws Exception {
    Identity newIdentity = new Identity("organization", "withxp");

    //
    storage._createIdentity(newIdentity);
    String generatedId = newIdentity.getId();
    assertNotNull(generatedId);
    assertEquals("organization", newIdentity.getProviderId());
    assertEquals(false, newIdentity.isDeleted());
    assertEquals("withxp", newIdentity.getRemoteId());
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
    profile.setProperty(Profile.AVATAR_URL, "avatarurl");

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
    Map<String, Object> xp2 = new HashMap<String, Object>();
    xp2.put(Profile.EXPERIENCES_SKILLS, "skills 2");
    xp2.put(Profile.EXPERIENCES_POSITION, "position 2");
    xp2.put(Profile.EXPERIENCES_COMPANY, "company 2");
    xp2.put(Profile.EXPERIENCES_DESCRIPTION, "description 2");
    xp2.put(Profile.EXPERIENCES_START_DATE, "01/01/2002");
    xp2.put(Profile.EXPERIENCES_END_DATE, "01/01/2003");
    xp2.put(Profile.EXPERIENCES_IS_CURRENT, Boolean.FALSE);
    Map<String, Object> xp3 = new HashMap<String, Object>();
    xp3.put(Profile.EXPERIENCES_SKILLS, "skills 3");
    xp3.put(Profile.EXPERIENCES_POSITION, "position3");
    xp3.put(Profile.EXPERIENCES_COMPANY, "company 3");
    xp3.put(Profile.EXPERIENCES_DESCRIPTION, "description 3");
    xp3.put(Profile.EXPERIENCES_START_DATE, "01/01/2002");
    xp3.put(Profile.EXPERIENCES_END_DATE, "01/01/2003");
    xp3.put(Profile.EXPERIENCES_IS_CURRENT, Boolean.FALSE);
    xps.add(xp1);
    xps.add(xp2);
    xps.add(xp3);
    profile.setProperty(Profile.EXPERIENCES, xps);

    //
    storage._saveProfile(profile);

    //
    Profile toLoadProfile = new Profile(newIdentity);
    storage._loadProfile(toLoadProfile);
    List<Map<String, String>> loadedXp = (List<Map<String, String>>) toLoadProfile.getProperty(Profile.EXPERIENCES);

    assertEquals(3, loadedXp.size());

    // remove one
    xps.remove(xp2);
    profile.setProperty(Profile.EXPERIENCES, xps);
    storage._saveProfile(profile);

    // reload
    Profile toLoadProfile2 = new Profile(newIdentity);
    storage._loadProfile(toLoadProfile2);
    List<Map<String, String>> loadedXp2 = (List<Map<String, String>>) toLoadProfile2.getProperty(Profile.EXPERIENCES);

    assertEquals(2, loadedXp2.size());

    tearDownIdentityList.add(newIdentity.getId());
  }

}
