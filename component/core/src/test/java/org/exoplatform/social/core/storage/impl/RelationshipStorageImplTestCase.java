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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;
import org.exoplatform.social.core.test.QueryNumberTest;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@QueryNumberTest
public class RelationshipStorageImplTestCase extends AbstractCoreTest {
  private RelationshipStorageImpl storage;

  private IdentityStorageImpl identityStorage;

  private List<String> tearDownIdentityList;

  @Override
  protected void setUp() throws Exception {

    storage = (RelationshipStorageImpl) getContainer().getComponentInstanceOfType(RelationshipStorageImpl.class);
    assertNotNull(storage);

    identityStorage = (IdentityStorageImpl) getContainer().getComponentInstanceOfType(IdentityStorageImpl.class);
    assertNotNull("identityManger must not be null", identityStorage);

    tearDownIdentityList = new ArrayList<String>();
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    for (String id : tearDownIdentityList) {
      identityStorage.deleteIdentity(new Identity(id));
    }
    super.tearDown();

  }

  @MaxQueryNumber(156)
  public void testCreateRelationship() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp1, tmp2, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);
    assertNotNull(newRelationship.getId());

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(156)
  public void testCreateRelationshipExists() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp1, tmp2, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);
    assertNotNull(newRelationship.getId());

    //
    try {
      storage._createRelationship(newRelationship);
      fail();
    }
    catch (Exception e) {
      // test ok
    }

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(147)
  public void testGetRelationshipDoesntExists() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    try {
      storage._getRelationship(tmp1, tmp2);
      fail();
    }
    catch (Exception e) {
      // test ok
    }

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(246)
  public void testSaveRelationship() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp1, tmp2, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);
    assertNotNull(newRelationship.getId());

    //
    newRelationship.setStatus(Relationship.Type.CONFIRMED);
    storage._saveRelationship(newRelationship);

    //
    Relationship got = storage._getRelationship(tmp1, tmp2);
    assertNotNull(got);
    assertEquals(Relationship.Type.CONFIRMED, got.getStatus());
    assertEquals(tmp1.getId(), got.getSender().getId());
    assertEquals(tmp2.getId(), got.getReceiver().getId());

    //
    Relationship gotInvert = storage._getRelationship(tmp2, tmp1);
    assertNotNull(gotInvert);
    assertEquals(Relationship.Type.CONFIRMED, gotInvert.getStatus());
    assertEquals(tmp1.getId(), gotInvert.getReceiver().getId());
    assertEquals(tmp2.getId(), gotInvert.getSender().getId());

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(198)
  public void testFindRelationship() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp1, tmp2, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);
    assertNotNull(newRelationship.getId());
    assertEquals(tmp1.getId(), newRelationship.getSender().getId());
    assertEquals(tmp2.getId(), newRelationship.getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, newRelationship.getStatus());

    //
    Relationship got = storage._getRelationship(newRelationship.getId());
    assertNotNull(got);
    assertEquals(tmp1.getId(), got.getSender().getId());
    assertEquals(tmp2.getId(), got.getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, got.getStatus());

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(276)
  public void testDeleteRelationship() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp1, tmp2, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);
    assertNotNull(newRelationship.getId());
    assertEquals(tmp1.getId(), newRelationship.getSender().getId());
    assertEquals(tmp2.getId(), newRelationship.getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, newRelationship.getStatus());

    //
    storage.removeRelationship(newRelationship);

    //
    try {
      storage._getRelationship(newRelationship.getId());
      fail();
    } catch (NodeNotFoundException e) {
      // test ok
    }

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(366)
  public void testRemoveAcceptedRelationship() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp1, tmp2, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);
    assertNotNull(newRelationship.getId());
    assertEquals(tmp1.getId(), newRelationship.getSender().getId());
    assertEquals(tmp2.getId(), newRelationship.getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, newRelationship.getStatus());

    //
    newRelationship.setStatus(Relationship.Type.CONFIRMED);
    storage._saveRelationship(newRelationship);

    Relationship gotRelationship = storage.getRelationship(tmp1, tmp2);
    assertNotNull(gotRelationship.getId());
    assertEquals(tmp1.getId(), gotRelationship.getSender().getId());
    assertEquals(tmp2.getId(), gotRelationship.getReceiver().getId());
    assertEquals(Relationship.Type.CONFIRMED, gotRelationship.getStatus());

    storage.removeRelationship(newRelationship);

    //
    try {
      storage._getRelationship(newRelationship.getId());
      fail();
    } catch (NodeNotFoundException e) {
      // test ok
    }

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(165)
  public void testGetSenderRelationship() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp1, tmp2, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);
    assertNotNull(newRelationship.getId());
    assertEquals(tmp1.getId(), newRelationship.getSender().getId());
    assertEquals(tmp2.getId(), newRelationship.getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, newRelationship.getStatus());

    //
    List<Relationship> relationshipList = storage._getSenderRelationships(tmp1, Relationship.Type.PENDING, null);
    assertNotNull(relationshipList);
    assertEquals(1, relationshipList.size());
    assertNotNull(relationshipList.get(0));
    assertNotNull(relationshipList.get(0).getSender());
    assertNotNull(relationshipList.get(0).getReceiver());
    assertEquals(tmp1.getId(), relationshipList.get(0).getSender().getId());
    assertEquals(tmp2.getId(), relationshipList.get(0).getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, relationshipList.get(0).getStatus());
    assertNull(relationshipList.get(0).getSender().getProfile().getId());
    assertNull(relationshipList.get(0).getReceiver().getProfile().getId());

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(231)
  public void testGetSenderRelationshipWithProfile() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp1, tmp2, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);
    assertNotNull(newRelationship.getId());
    assertEquals(tmp1.getId(), newRelationship.getSender().getId());
    assertEquals(tmp2.getId(), newRelationship.getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, newRelationship.getStatus());

    //
    Profile profile1 = new Profile(tmp1);
    profile1.setProperty(Profile.FIRST_NAME, "p1 first");
    profile1.setProperty(Profile.LAST_NAME, "p1 last");
    tmp1.setProfile(profile1);
    identityStorage._createProfile(profile1);

    Profile profile2 = new Profile(tmp2);
    profile2.setProperty(Profile.FIRST_NAME, "p2 first");
    profile2.setProperty(Profile.LAST_NAME, "p2 last");
    tmp2.setProfile(profile2);
    identityStorage._createProfile(profile2);

    //
    List<Relationship> relationshipList = storage._getSenderRelationships(tmp1, Relationship.Type.PENDING, null);
    assertNotNull(relationshipList);
    assertEquals(1, relationshipList.size());
    assertNotNull(relationshipList.get(0));
    assertNotNull(relationshipList.get(0).getSender());
    assertNotNull(relationshipList.get(0).getReceiver());
    assertEquals(tmp1.getId(), relationshipList.get(0).getSender().getId());
    assertEquals(tmp2.getId(), relationshipList.get(0).getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, relationshipList.get(0).getStatus());
    assertNotNull(relationshipList.get(0).getSender().getProfile().getId());
    assertEquals("p1 first", relationshipList.get(0).getSender().getProfile().getProperty(Profile.FIRST_NAME));
    assertEquals("p1 last", relationshipList.get(0).getSender().getProfile().getProperty(Profile.LAST_NAME));
    assertNotNull(relationshipList.get(0).getReceiver().getProfile().getId());
    assertEquals("p2 first", relationshipList.get(0).getReceiver().getProfile().getProperty(Profile.FIRST_NAME));
    assertEquals("p2 last", relationshipList.get(0).getReceiver().getProfile().getProperty(Profile.LAST_NAME));

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(165)
  public void testGetReceiverRelationship() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp1, tmp2, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);
    assertNotNull(newRelationship.getId());
    assertEquals(tmp1.getId(), newRelationship.getSender().getId());
    assertEquals(tmp2.getId(), newRelationship.getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, newRelationship.getStatus());

    //
    List<Relationship> relationshipList = storage._getReceiverRelationships(tmp2, Relationship.Type.PENDING, null);
    assertNotNull(relationshipList);
    assertEquals(1, relationshipList.size());
    assertNotNull(relationshipList.get(0));
    assertNotNull(relationshipList.get(0).getSender());
    assertNotNull(relationshipList.get(0).getReceiver());
    assertEquals(tmp1.getId(), relationshipList.get(0).getSender().getId());
    assertEquals(tmp2.getId(), relationshipList.get(0).getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, relationshipList.get(0).getStatus());
    assertNull(relationshipList.get(0).getSender().getProfile().getId());
    assertNull(relationshipList.get(0).getReceiver().getProfile().getId());

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(231)
  public void testGetReceiverRelationshipWithProfile() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp1, tmp2, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);
    assertNotNull(newRelationship.getId());
    assertEquals(tmp1.getId(), newRelationship.getSender().getId());
    assertEquals(tmp2.getId(), newRelationship.getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, newRelationship.getStatus());
    
    //
    Profile profile1 = new Profile(tmp1);
    profile1.setProperty(Profile.FIRST_NAME, "p1 first");
    profile1.setProperty(Profile.LAST_NAME, "p1 last");
    tmp1.setProfile(profile1);
    identityStorage._createProfile(profile1);

    Profile profile2 = new Profile(tmp2);
    profile2.setProperty(Profile.FIRST_NAME, "p2 first");
    profile2.setProperty(Profile.LAST_NAME, "p2 last");
    tmp2.setProfile(profile2);
    identityStorage._createProfile(profile2);

    //
    List<Relationship> relationshipList = storage._getReceiverRelationships(tmp2, Relationship.Type.PENDING, null);
    assertNotNull(relationshipList);
    assertEquals(1, relationshipList.size());
    assertNotNull(relationshipList.get(0));
    assertNotNull(relationshipList.get(0).getSender());
    assertNotNull(relationshipList.get(0).getReceiver());
    assertEquals(tmp1.getId(), relationshipList.get(0).getSender().getId());
    assertEquals(tmp2.getId(), relationshipList.get(0).getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, relationshipList.get(0).getStatus());
    assertNotNull(relationshipList.get(0).getSender().getProfile().getId());
    assertEquals("p1 first", relationshipList.get(0).getSender().getProfile().getProperty(Profile.FIRST_NAME));
    assertEquals("p1 last", relationshipList.get(0).getSender().getProfile().getProperty(Profile.LAST_NAME));
    assertNotNull(relationshipList.get(0).getReceiver().getProfile().getId());
    assertEquals("p2 first", relationshipList.get(0).getReceiver().getProfile().getProperty(Profile.FIRST_NAME));
    assertEquals("p2 last", relationshipList.get(0).getReceiver().getProfile().getProperty(Profile.LAST_NAME));


    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(165)
  public void testGetRelationship() throws Exception {
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp1, tmp2, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);
    assertNotNull(newRelationship.getId());
    assertEquals(tmp1.getId(), newRelationship.getSender().getId());
    assertEquals(tmp2.getId(), newRelationship.getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, newRelationship.getStatus());

    //
    Relationship rel = storage.getRelationship(tmp1, tmp2);
    assertEquals(tmp1.getId(), rel.getSender().getId());
    assertEquals(tmp2.getId(), rel.getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, rel.getStatus());

    //
    Relationship rel2 = storage.getRelationship(tmp2, tmp1);
    assertEquals(tmp1.getId(), rel2.getSender().getId());
    assertEquals(tmp2.getId(), rel2.getReceiver().getId());
    assertEquals(Relationship.Type.PENDING, rel2.getStatus());

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
  }

  @MaxQueryNumber(534)
  public void testGetConnectionsCount() throws Exception {

    //
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");
    Identity tmp3 = new Identity("organization", "tmp3");
    Identity tmp4 = new Identity("organization", "tmp4");
    Identity tmp5 = new Identity("organization", "tmp5");

    //
    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);
    identityStorage.saveIdentity(tmp3);
    identityStorage.saveIdentity(tmp4);
    identityStorage.saveIdentity(tmp5);

    //
    Relationship r12 = new Relationship(tmp1, tmp2);
    Relationship r13 = new Relationship(tmp1, tmp3);
    Relationship r14 = new Relationship(tmp1, tmp4);
    Relationship r32 = new Relationship(tmp3, tmp2);
    Relationship r42 = new Relationship(tmp4, tmp2);

    //
    r12.setStatus(Relationship.Type.CONFIRMED);
    r13.setStatus(Relationship.Type.CONFIRMED);
    r14.setStatus(Relationship.Type.CONFIRMED);
    r32.setStatus(Relationship.Type.CONFIRMED);
    r42.setStatus(Relationship.Type.CONFIRMED);

    //
    storage.saveRelationship(r12);
    storage.saveRelationship(r13);
    storage.saveRelationship(r14);
    storage.saveRelationship(r32);
    storage.saveRelationship(r42);

    //
    assertEquals(3, storage.getConnectionsCount(tmp1));
    assertEquals(3, storage.getConnectionsCount(tmp2));
    assertEquals(2, storage.getConnectionsCount(tmp3));
    assertEquals(2, storage.getConnectionsCount(tmp4));
    assertEquals(0, storage.getConnectionsCount(tmp5));

    //
    assertEquals(3, storage.getConnections(tmp1, 0, 100).size());
    assertEquals(3, storage.getConnections(tmp2, 0, 100).size());
    assertEquals(2, storage.getConnections(tmp3, 0, 100).size());
    assertEquals(2, storage.getConnections(tmp4, 0, 100).size());
    assertEquals(0, storage.getConnections(tmp5, 0, 100).size());

    assertEquals(3, storage.getConnections(tmp1, 0, 3).size());
    assertEquals(2, storage.getConnections(tmp1, 0, 2).size());
    assertEquals(3, storage.getConnections(tmp1, 0, 0).size());
    assertEquals(3, storage.getConnections(tmp1, 0, -1).size());

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
    tearDownIdentityList.add(tmp3.getId());
    tearDownIdentityList.add(tmp4.getId());
    tearDownIdentityList.add(tmp5.getId());
  }

  @MaxQueryNumber(555)
  public void testGetConnections() throws Exception {

    //
    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");
    Identity tmp3 = new Identity("organization", "tmp3");
    Identity tmp4 = new Identity("organization", "tmp4");
    Identity tmp5 = new Identity("organization", "tmp5");

    //
    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);
    identityStorage.saveIdentity(tmp3);
    identityStorage.saveIdentity(tmp4);
    identityStorage.saveIdentity(tmp5);

    Profile profile = new Profile(tmp1);
    profile = identityStorage.loadProfile(profile);

    profile.setProperty(Profile.POSITION, "my position");
    identityStorage.saveProfile(profile);

    //
    Relationship r12 = new Relationship(tmp1, tmp2);
    Relationship r13 = new Relationship(tmp1, tmp3);
    Relationship r14 = new Relationship(tmp1, tmp4);
    Relationship r32 = new Relationship(tmp3, tmp2);
    Relationship r42 = new Relationship(tmp4, tmp2);

    //
    r12.setStatus(Relationship.Type.CONFIRMED);
    r13.setStatus(Relationship.Type.CONFIRMED);
    r14.setStatus(Relationship.Type.CONFIRMED);
    r32.setStatus(Relationship.Type.CONFIRMED);
    r42.setStatus(Relationship.Type.CONFIRMED);

    //
    storage.saveRelationship(r12);
    storage.saveRelationship(r13);
    storage.saveRelationship(r14);
    storage.saveRelationship(r32);
    storage.saveRelationship(r42);

    //
    assertTrue(storage.getConnections(tmp1, 0, 10).contains(tmp2));
    assertTrue(storage.getConnections(tmp1, 0, 10).contains(tmp3));
    assertTrue(storage.getConnections(tmp1, 0, 10).contains(tmp4));
    assertTrue(!storage.getConnections(tmp1, 0, 10).contains(tmp5));

    assertTrue(storage.getConnections(tmp2, 0, 10).contains(tmp1));
    assertTrue(storage.getConnections(tmp2, 0, 10).contains(tmp3));
    assertTrue(storage.getConnections(tmp2, 0, 10).contains(tmp4));
    assertTrue(!storage.getConnections(tmp2, 0, 10).contains(tmp5));

    assertTrue(storage.getConnections(tmp3, 0, 10).contains(tmp1));
    assertTrue(storage.getConnections(tmp3, 0, 10).contains(tmp2));
    assertTrue(!storage.getConnections(tmp3, 0, 10).contains(tmp4));
    assertTrue(!storage.getConnections(tmp3, 0, 10).contains(tmp5));

    assertTrue(storage.getConnections(tmp4, 0, 10).contains(tmp1));
    assertTrue(storage.getConnections(tmp4, 0, 10).contains(tmp2));
    assertTrue(!storage.getConnections(tmp4, 0, 10).contains(tmp3));
    assertTrue(!storage.getConnections(tmp4, 0, 10).contains(tmp5));

    assertTrue(!storage.getConnections(tmp5, 0, 10).contains(tmp1));
    assertTrue(!storage.getConnections(tmp5, 0, 10).contains(tmp2));
    assertTrue(!storage.getConnections(tmp5, 0, 10).contains(tmp3));
    assertTrue(!storage.getConnections(tmp5, 0, 10).contains(tmp4));

    //
    for (Identity currentIdentity : storage.getConnections(tmp1, 0, 10)) {
      if (currentIdentity.getId().equals(tmp1)) {
        assertEquals(currentIdentity.getProfile().getProperty(Profile.POSITION), "my position");
        assertNotNull(currentIdentity.getProfile().getAvatarUrl());
      }
    }

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());
    tearDownIdentityList.add(tmp3.getId());
    tearDownIdentityList.add(tmp4.getId());
    tearDownIdentityList.add(tmp5.getId());
  }
  
  @MaxQueryNumber(591)
  public void testGetConnectionsByFilter() throws Exception {
    
    //
    Identity spearsIdentity = createIdentity("spears");
    Identity williamsIdentity = createIdentity("williams");
    Identity christmasIdentity = createIdentity("christmas");
    Identity kellyIdentity = createIdentity("kelly");
    Identity tweedyIdentity = createIdentity("tweedy");
    
    //
    createRelationship(spearsIdentity, williamsIdentity, Relationship.Type.CONFIRMED);
    createRelationship(spearsIdentity, christmasIdentity, Relationship.Type.CONFIRMED);
    createRelationship(spearsIdentity, kellyIdentity, Relationship.Type.CONFIRMED);
    createRelationship(spearsIdentity, tweedyIdentity, Relationship.Type.CONFIRMED);
    
    List<Identity> got = storage.getConnectionsByFilter(spearsIdentity, new ProfileFilter(), 0, 10);
    
    assertEquals(4, got.size());
    assertEquals(christmasIdentity, got.get(0));
    assertEquals(kellyIdentity, got.get(1));
    assertEquals(tweedyIdentity, got.get(2));
    assertEquals(williamsIdentity, got.get(3));
  }
  
  @MaxQueryNumber(591)
  public void testGetIncomingByFilter() throws Exception {
    
    //
    Identity spearsIdentity = createIdentity("spears");
    Identity williamsIdentity = createIdentity("williams");
    Identity christmasIdentity = createIdentity("christmas");
    Identity kellyIdentity = createIdentity("kelly");
    Identity tweedyIdentity = createIdentity("tweedy");
    
    //
    createRelationship(williamsIdentity, spearsIdentity, Relationship.Type.PENDING);
    createRelationship(christmasIdentity, spearsIdentity, Relationship.Type.PENDING);
    createRelationship(kellyIdentity, spearsIdentity, Relationship.Type.PENDING);
    createRelationship(tweedyIdentity, spearsIdentity, Relationship.Type.PENDING);
    
    List<Identity> got = storage.getIncomingByFilter(spearsIdentity, new ProfileFilter(), 0, 10);
    
    assertEquals(4, got.size());
    assertEquals(christmasIdentity, got.get(0));
    assertEquals(kellyIdentity, got.get(1));
    assertEquals(tweedyIdentity, got.get(2));
    assertEquals(williamsIdentity, got.get(3));
  }
  
  @MaxQueryNumber(591)
  public void testGetOutgoingByFilter() throws Exception {
    
    //
    Identity spearsIdentity = createIdentity("spears");
    Identity williamsIdentity = createIdentity("williams");
    Identity christmasIdentity = createIdentity("christmas");
    Identity kellyIdentity = createIdentity("kelly");
    Identity tweedyIdentity = createIdentity("tweedy");
    
    //
    createRelationship(spearsIdentity, williamsIdentity, Relationship.Type.PENDING);
    createRelationship(spearsIdentity, christmasIdentity, Relationship.Type.PENDING);
    createRelationship(spearsIdentity, kellyIdentity, Relationship.Type.PENDING);
    createRelationship(spearsIdentity, tweedyIdentity, Relationship.Type.PENDING);
    
    List<Identity> got = storage.getOutgoingByFilter(spearsIdentity, new ProfileFilter(), 0, 10);
    
    assertEquals(4, got.size());
    assertEquals(christmasIdentity, got.get(0));
    assertEquals(kellyIdentity, got.get(1));
    assertEquals(tweedyIdentity, got.get(2));
    assertEquals(williamsIdentity, got.get(3));
  }
  
  private Identity createIdentity(String remoteId) throws Exception {
    Identity identity = new Identity("organization", remoteId);
    identityStorage.saveIdentity(identity);
    identity.getProfile().setProperty(Profile.LAST_NAME, remoteId);
    identity.getProfile().setProperty(Profile.FULL_NAME, remoteId);
    identityStorage._createProfile(identity.getProfile());
    tearDownIdentityList.add(identity.getId());
    
    return identity;
  }
  
  private Relationship createRelationship(Identity sender, Identity receiver, Relationship.Type status) throws Exception {
    Relationship relationship = new Relationship(sender, receiver);
    relationship.setStatus(status);
    storage.saveRelationship(relationship);
    
    return relationship;
  }
  
  public void testGetRelationships() throws Exception {

    Identity tmp1 = new Identity("organization", "tmp1");
    Identity tmp2 = new Identity("organization", "tmp2");

    identityStorage.saveIdentity(tmp1);
    identityStorage.saveIdentity(tmp2);

    Relationship newRelationship = new Relationship(tmp2, tmp1, Relationship.Type.PENDING);

    //
    storage._createRelationship(newRelationship);

    //read from receiver
    List<Identity> identities = storage.getRelationships(tmp2, 0, 10);
    assertEquals(tmp1.getRemoteId(), identities.get(0).getRemoteId());

    //read from sender
    identities = storage.getRelationships(tmp1, 0, 10);
    assertEquals(tmp2.getRemoteId(), identities.get(0).getRemoteId());

    //
    tearDownIdentityList.add(tmp1.getId());
    tearDownIdentityList.add(tmp2.getId());

  }
}
