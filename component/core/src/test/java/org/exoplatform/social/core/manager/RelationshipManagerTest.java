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
package org.exoplatform.social.core.manager;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Tests for {@link RelationshipManager}
 *
 */
public class RelationshipManagerTest extends AbstractCoreTest {
  private RelationshipManager relationshipManager;
  private IdentityManager identityManager;

  private Identity rootIdentity,
                   johnIdentity,
                   maryIdentity,
                   demoIdentity;
  
  private List<Relationship> tearDownRelationshipList;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    tearDownRelationshipList = new ArrayList<Relationship>();
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull("relationshipManager must not be null", relationshipManager);
    assertNotNull("identityManager must not be null", identityManager);
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");

    assertEquals("setUp: relationshipManager.getAllRelationships(rootIdentity).size() must return 0",
            0,
            relationshipManager.getAllRelationships(rootIdentity).size());
    assertEquals("setUp: relationshipManager.getAllRelationships(johnIdentity).size() must return 0",
            0,
            relationshipManager.getAllRelationships(johnIdentity).size());
    assertEquals("setUp: relationshipManager.getAllRelationships(maryIdentity).size() must return 0",
            0,
            relationshipManager.getAllRelationships(maryIdentity).size());
    assertEquals("setUp: relationshipManager.getAllRelationships(demoIdentity).size() must return 0",
            0,
            relationshipManager.getAllRelationships(demoIdentity).size());
  }

  @Override
  protected void tearDown() throws Exception {
    for (Relationship relationship : tearDownRelationshipList) {
      relationshipManager.remove(relationship);
    }
    assertEquals("tearDown: relationshipManager.getAllRelationships(rootIdentity).size() must return 0",
            0,
            relationshipManager.getAllRelationships(rootIdentity).size());
    assertEquals("tearDown: relationshipManager.getAllRelationships(johnIdentity).size() must return 0",
            0,
            relationshipManager.getAllRelationships(johnIdentity).size());
    assertEquals("tearDown: relationshipManager.getAllRelationships(maryIdentity).size() must return 0",
            0,
            relationshipManager.getAllRelationships(maryIdentity).size());
    assertEquals("tearDown: relationshipManager.getAllRelationships(demoIdentity).size() must return 0",
            0,
            relationshipManager.getAllRelationships(demoIdentity).size());

    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);

    super.tearDown();
  }


  /**
   * @throws Exception
   *
   */
  public void testGetRelationshipByIdentityId() throws Exception {

    Relationship relationship = relationshipManager.invite(johnIdentity, demoIdentity);
    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(johnIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(demoIdentity);

    assertEquals("senderRelationships.size() must return 1", 1, senderRelationships.size());
    assertEquals("senderRelationships.size() must return 1", 1, receiverRelationships.size());

    tearDownRelationshipList.add(relationship);
  }

  /**
   *
   * @throws Exception
   */
  /*
  public void testGetManyRelationshipsByIdentityId() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;

    Identity sender = identityManager.getOrCreateIdentity(providerId,"john");
    identityManager.saveIdentity(sender);
    assertNotNull(sender.getId());

    Identity receiver = identityManager.getOrCreateIdentity(providerId,"mary");
    assertNotNull(receiver.getId());

    relationshipManager.invite(sender, receiver);

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(sender);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(receiver);

    assertEquals(total, senderRelationships.size());
    assertEquals(total, receiverRelationships.size());
  }
*/

  /**
   * Test {@link RelationshipManager#invite(Identity, Identity)}
   * 
   * @throws Exception
   */
  public void testInvite() throws Exception {
    Relationship relationship = relationshipManager.invite(johnIdentity, maryIdentity);
    assertNotNull("relationship.getId() must not be null", relationship.getId());
    assertEquals("relationship.getStatus() must return " + Relationship.Type.PENDING, Relationship.Type.PENDING, relationship.getStatus());
    
    Relationship demoMaryRelationship = relationshipManager.invite(demoIdentity, maryIdentity);
    assertNotNull("demoMaryRelationship.getId() must not be null", demoMaryRelationship.getId());
    assertEquals("demoMaryRelationship.getStatus() must return " + Relationship.Type.PENDING, Relationship.Type.PENDING, demoMaryRelationship.getStatus());

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(johnIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(maryIdentity);

    assertEquals("senderRelationships.size() must return 1", 1, senderRelationships.size());
    assertEquals("receiverRelationships.size() must return 2", 2, receiverRelationships.size());

    tearDownRelationshipList.add(relationship);
    tearDownRelationshipList.add(demoMaryRelationship);
  }

  /**
   * Test {@link RelationshipManager#confirm(org.exoplatform.social.core.relationship.model.Relationship)}
   *
   * @throws Exception
   */
  public void testConfirm() throws Exception {
    Relationship relationship = relationshipManager.invite(johnIdentity, demoIdentity);
    relationshipManager.confirm(relationship);
    assertNotNull("relationship.getId() must not be null", relationship.getId());
    assertEquals("relationship.getStatus() must return " + Relationship.Type.CONFIRM, Relationship.Type.CONFIRM, relationship.getStatus());

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(johnIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(demoIdentity);

    assertEquals("senderRelationships.size() must return 1", 1, senderRelationships.size());
    assertEquals("receiverRelationships.size() must return 1", 1, receiverRelationships.size());

    Relationship rootToDemoRelationship = relationshipManager.invite(rootIdentity, demoIdentity);
    relationshipManager.confirm(rootToDemoRelationship);

    List<Relationship> demoContacts = relationshipManager.getContacts(demoIdentity);
    assertEquals("demoContacts.size() must return 2", 2, demoContacts.size());

    receiverRelationships = relationshipManager.getAllRelationships(demoIdentity);
    assertEquals("receiverRelationships.size() must return 2", 2, receiverRelationships.size());

    tearDownRelationshipList.addAll(receiverRelationships);
  }

  /**
   * Test {@link RelationshipManager#remove(Relationship)}
   *
   * @throws Exception
   */
  public void testRemove() throws Exception {
    Relationship relationship = relationshipManager.invite(rootIdentity, johnIdentity);
    relationshipManager.confirm(relationship);
    relationshipManager.remove(relationship);

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(rootIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(johnIdentity);

    assertEquals("senderRelationships.size() must return 0", 0, senderRelationships.size());
    assertEquals("receiverRelationships.size() must return 0", 0, receiverRelationships.size());

    tearDownRelationshipList.addAll(receiverRelationships);
  }

  /**
   * Test {@link RelationshipManager#ignore(Relationship)}
   *
   * @throws Exception
   */
  public void testIgnore() throws Exception {

    Relationship relationship = relationshipManager.invite(johnIdentity, rootIdentity);
    relationshipManager.ignore(relationship);
    assertNotNull("relationship.getId() must not be null", relationship.getId());
    assertEquals("relationship.getStatus() must return " + Relationship.Type.IGNORE, Relationship.Type.IGNORE, relationship.getStatus());

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(johnIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(rootIdentity);

    assertEquals("senderRelationships.size() must return 1", 1, senderRelationships.size());
    assertEquals("receiverRelationships.size() must return 1", 1, receiverRelationships.size());

    tearDownRelationshipList.addAll(senderRelationships);
  }
  
  /**
   * Test {@link RelationshipManager#getPendingRelationships(Identity)}
   * 
   * @throws Exception
   */
  public void testGetPendingRelationships() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    List<Relationship> foundListRelationships = relationshipManager.getPendingRelationships(johnIdentity);
    assertNotNull("foundListRelationships must not be null", foundListRelationships);
    assertEquals("foundListRelationships.size() must return 3", 3, foundListRelationships.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getPendingRelationships(Identity, boolean)}
   * 
   * @throws Exception
   */
  public void testGetPendingRelationshipsWithIncoming() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    List<Relationship> listPendingRelationship = relationshipManager.getPendingRelationships(johnIdentity, true);
    assertNotNull("listRelationshipConfirm must not be null", listPendingRelationship);
    assertEquals("listPendingRelationship.size() must return 3", 3, listPendingRelationship.size());

    List<Relationship> listMaryRequireValidationRelationship = relationshipManager.getPendingRelationships(maryIdentity, false);
    assertEquals("listMaryRequireValidationRelationship.size() must return 1", 1, listMaryRequireValidationRelationship.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getPendingRelationships(Identity, List, boolean)}
   * 
   * @throws Exception
   */
  public void testGetPendingRelationshipsWithIcomingAndMatchIdentities() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);
    Relationship maryDemoRelationship = relationshipManager.invite(maryIdentity, demoIdentity);

    List<Identity> listIdentities = new ArrayList<Identity>();
    listIdentities.add(demoIdentity);
    listIdentities.add(maryIdentity);
    listIdentities.add(johnIdentity);
    listIdentities.add(rootIdentity);

    List<Relationship> listRelationshipConfirm = relationshipManager.getPendingRelationships(johnIdentity, listIdentities, true);
    assertEquals("listRelationshipConfirm.size() must return 3", 3, listRelationshipConfirm.size());

    List<Relationship> listRelationshipNotConfirm = relationshipManager.getPendingRelationships(demoIdentity, listIdentities, false);
    assertEquals("listRelationshipNotConfirm.size() must return 2", 2, listRelationshipNotConfirm.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
    tearDownRelationshipList.add(maryDemoRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getContacts(Identity, List)}
   *
   * @throws Exception
   */
  public void testGetContacts() throws Exception {
    Relationship demoJohnRelationship = relationshipManager.invite(demoIdentity, johnIdentity);
    relationshipManager.confirm(demoJohnRelationship);
    
    Relationship maryJohnRelationship = relationshipManager.invite(maryIdentity, johnIdentity);
    relationshipManager.confirm(maryJohnRelationship);
    
    Relationship rootJohnRelationship = relationshipManager.invite(rootIdentity, johnIdentity);
    relationshipManager.confirm(rootJohnRelationship);
    
    List<Relationship> johnContacts = relationshipManager.getContacts(johnIdentity);
    assertNotNull("johnContacts must not be null", johnContacts);
    assertEquals("johnContacts.size() must return: " + 3, 3, johnContacts.size());
    
    tearDownRelationshipList.add(demoJohnRelationship);
    tearDownRelationshipList.add(maryJohnRelationship);
    tearDownRelationshipList.add(rootJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getContacts(Identity, List)}
   * 
   * @throws Exception
   */
  public void testGetContactsWithListAccess() throws Exception {
    Relationship demoJohnRelationship = relationshipManager.invite(demoIdentity, johnIdentity);
    relationshipManager.confirm(demoJohnRelationship);
    
    Relationship maryJohnRelationship = relationshipManager.invite(maryIdentity, johnIdentity);
    relationshipManager.confirm(maryJohnRelationship);
    
    Relationship rootJohnRelationship = relationshipManager.invite(rootIdentity, johnIdentity);
    relationshipManager.confirm(rootJohnRelationship);
    
    List<Identity> listIdentities = new ArrayList<Identity>();
    listIdentities.add(demoIdentity);
    listIdentities.add(maryIdentity);
    listIdentities.add(johnIdentity);
    listIdentities.add(rootIdentity);
    
    List<Relationship> johnContacts = relationshipManager.getContacts(johnIdentity, listIdentities);
    assertNotNull("johnContacts must not be null", johnContacts);
    assertEquals("johnContacts.size() must return: " + 3, 3, johnContacts.size());
    
    List<Relationship> maryContacts = relationshipManager.getContacts(maryIdentity, listIdentities);
    assertNotNull("johnContacts must not be null", maryContacts);
    assertEquals("maryContacts.size() must return: " + 1, 1, maryContacts.size());
    
    tearDownRelationshipList.add(demoJohnRelationship);
    tearDownRelationshipList.add(maryJohnRelationship);
    tearDownRelationshipList.add(rootJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getAllRelationships(Identity)}
   * 
   * @throws Exception
   */
  public void testGetAllRelationship() throws Exception {
    Relationship demoJohnRelationship = relationshipManager.invite(demoIdentity, johnIdentity);
    relationshipManager.confirm(demoJohnRelationship);
    
    Relationship maryJohnRelationship = relationshipManager.invite(maryIdentity, johnIdentity);
    relationshipManager.confirm(maryJohnRelationship);
    
    Relationship rootJohnRelationship = relationshipManager.invite(rootIdentity, johnIdentity);
    relationshipManager.confirm(rootJohnRelationship);
    
    List<Relationship> johnRelationships = relationshipManager.getAllRelationships(johnIdentity);
    assertNotNull("johnRelationships must not be null", johnRelationships);
    assertEquals("johnRelationships.size() must return: " + 3, 3, johnRelationships.size());
    
    Relationship rootMaryRelationship = relationshipManager.invite(rootIdentity, maryIdentity);
    relationshipManager.confirm(rootMaryRelationship);
    
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    relationshipManager.confirm(johnMaryRelationship);
    
    Relationship demoMaryRelationship = relationshipManager.invite(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoMaryRelationship);
    
    List<Relationship> maryRelationships = relationshipManager.getAllRelationships(maryIdentity);
    assertNotNull("maryRelationships must not be null", maryRelationships);
    assertEquals("maryRelationships.size()" + 4, 4, maryRelationships.size());
    
    tearDownRelationshipList.add(demoJohnRelationship);
    tearDownRelationshipList.add(rootJohnRelationship);
    tearDownRelationshipList.add(rootMaryRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(demoMaryRelationship);
    tearDownRelationshipList.add(maryJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getIdentities(Identity)}
   * 
   * @throws Exception
   */
  public void testGetIdentities() throws Exception {
    Relationship demoJohnRelationship = relationshipManager.invite(demoIdentity, johnIdentity);
    relationshipManager.confirm(demoJohnRelationship);
    
    Relationship maryJohnRelationship = relationshipManager.invite(maryIdentity, johnIdentity);
    relationshipManager.confirm(maryJohnRelationship);
    
    Relationship rootJohnRelationship = relationshipManager.invite(rootIdentity, johnIdentity);
    relationshipManager.confirm(rootJohnRelationship);
    
    List<Identity> listIdentities = relationshipManager.getIdentities(johnIdentity);
    assertNotNull("listIdentities must not be null", listIdentities);
    assertEquals("listIdentities.size() must return: " + 3, 3, listIdentities.size());
    
    listIdentities = relationshipManager.getIdentities(maryIdentity);
    assertNotNull("listIdentities must not be null", listIdentities);
    assertEquals("listIdentities.size() must return: " + 1, 1, listIdentities.size());
    
    tearDownRelationshipList.add(demoJohnRelationship);
    tearDownRelationshipList.add(maryJohnRelationship);
    tearDownRelationshipList.add(rootJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getRelationshipStatus(Relationship, Identity)}
   * 
   * @throws Exception
   */
  public void testGetRelationshipStatus() throws Exception {
    Relationship.Type status = relationshipManager.getRelationshipStatus(null, demoIdentity);
    assertNotNull("status must not be null", status);
    assertEquals("status must be " + Relationship.Type.ALIEN, Relationship.Type.ALIEN, status);
    
    Relationship demoJohnRelationship = relationshipManager.invite(demoIdentity, johnIdentity);
    status = relationshipManager.getRelationshipStatus(demoJohnRelationship, demoIdentity);
    assertNotNull("status must not be null", status);
    assertEquals("status must be " + Relationship.Type.PENDING, Relationship.Type.PENDING, status);
    
    status = relationshipManager.getRelationshipStatus(demoJohnRelationship, johnIdentity);
    assertNotNull("status must not be null", status);
    assertEquals("status must be " + Relationship.Type.REQUIRE_VALIDATION, Relationship.Type.REQUIRE_VALIDATION, status);
    
    relationshipManager.confirm(demoJohnRelationship);
    status = relationshipManager.getRelationshipStatus(demoJohnRelationship, johnIdentity);
    assertNotNull("status must not be null", status);
    assertEquals("status must be " + Relationship.Type.CONFIRM, Relationship.Type.CONFIRM, status);
    
    status = relationshipManager.getRelationshipStatus(demoJohnRelationship, demoIdentity);
    assertNotNull("status must not be null", status);
    assertEquals("status must be " + Relationship.Type.CONFIRM, Relationship.Type.CONFIRM, status);
    
    tearDownRelationshipList.add(demoJohnRelationship);
  }

  /**
   *
   * @throws Exception
   */
  /*
  public void testGetPendingRelationships() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;

    Identity sender = identityManager.getOrCreateIdentity(providerId,"john");
    identityManager.saveIdentity(sender);
    assertNotNull(sender.getId());

    Identity receiver = identityManager.getOrCreateIdentity(providerId,"demo");
    assertNotNull(receiver.getId());

    int total = 20;
    for (int i = 0; i < total; i++) {
      relationshipManager.invite(sender, receiver);
    }

    List<Relationship> senderRelationships = relationshipManager.getPendingRelationships(sender);
    List<Relationship> receiverRelationships = relationshipManager.getPendingRelationships(receiver);

    assertEquals(total, senderRelationships.size());
    assertEquals(total, receiverRelationships.size());
  }
  */
}