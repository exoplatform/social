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
  }

  @Override
  protected void tearDown() throws Exception {
    for (Relationship relationship : tearDownRelationshipList) {
      relationshipManager.remove(relationship);
    }

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

    relationshipManager.invite(johnIdentity, demoIdentity);
    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(johnIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(demoIdentity);

    assertEquals(1, senderRelationships.size());
    assertEquals(1, receiverRelationships.size());

    tearDownRelationshipList.addAll(senderRelationships);
  }

  /**
   *
   * @throws Exception
   */
  public void testgetRelationshipByRelationshipId() throws Exception {
    Relationship invitedRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship foundRelationship = relationshipManager.getRelationshipById(invitedRelationship.getId());
    assertNotNull("foundRelationship must not be null", foundRelationship);
    assertNotNull("foundRelationship.getId() must not be null", foundRelationship.getId());
    assertEquals(foundRelationship.getId(), invitedRelationship.getId());

    tearDownRelationshipList.add(invitedRelationship);
  }

  /**
   *
   * @throws Exception
   */
  public void testGetPendingRelationshipsWithIdentity() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    List<Relationship> foundListRelationships = relationshipManager.getPendingRelationships(johnIdentity);
    assertNotNull("foundListRelationships must not be null", foundListRelationships);
    assertEquals(3, foundListRelationships.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }

  /**
   *
   * @throws Exception
   */
  public void testGetPedingRelationshipWithIdentityAndConfirm() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    List<Relationship> listRelationshipConfirm = relationshipManager.getPendingRelationships(johnIdentity, true);
    assertNotNull("listRelationshipConfirm must not be null", listRelationshipConfirm);
    assertEquals(3, listRelationshipConfirm.size());

    List<Relationship> listRelationshipNotConfirm = relationshipManager.getPendingRelationships(johnIdentity, false);
    assertEquals(0, listRelationshipNotConfirm.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }

  /**
   *
   * @throws Exception
   */
  public void testGetPendingRelationshipsWithListIdentities() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    List<Identity> listIdentities = new ArrayList<Identity>();
    listIdentities.add(demoIdentity);
    listIdentities.add(maryIdentity);
    listIdentities.add(johnIdentity);
    listIdentities.add(rootIdentity);

    List<Relationship> listRelationshipConfirm = relationshipManager.getPendingRelationships(johnIdentity, listIdentities, true);
    assertEquals(3, listRelationshipConfirm.size());

    List<Relationship> listRelationshipNotConfirm = relationshipManager.getPendingRelationships(johnIdentity, listIdentities, false);
    assertEquals(0, listRelationshipNotConfirm.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }

  /**
   *
   * @throws Exception
   */
  public void testGetContactsWithIdentity() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    relationshipManager.confirm(johnDemoRelationship);
    relationshipManager.confirm(johnMaryRelationship);
    relationshipManager.confirm(johnRootRelationship);

    List<Relationship> contactsList = relationshipManager.getContacts(johnIdentity);
    assertEquals(3, contactsList.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }

  /**
   *
   * @throws Exception
   */
  public void testGetContactsWithIdentityAndListIdentity() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    relationshipManager.confirm(johnDemoRelationship);
    relationshipManager.confirm(johnMaryRelationship);
    relationshipManager.confirm(johnRootRelationship);

    List<Identity> listIdentities = new ArrayList<Identity>();
    listIdentities.add(demoIdentity);
    listIdentities.add(maryIdentity);
    listIdentities.add(johnIdentity);
    listIdentities.add(rootIdentity);

    List<Relationship> contactsList = relationshipManager.getContacts(johnIdentity, listIdentities);
    assertEquals(3, contactsList.size());
    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }

  /**
   *
   * @throws Exception
   */
  public void testGetAllRelationship() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    List<Relationship> listAllRelationship = relationshipManager.getAllRelationships(johnIdentity);
    assertEquals(3, listAllRelationship.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }

  /**
   *
   * @throws Exception
   */
  public void testGetRelationshipByIdIdentity() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    List<Relationship> foundRelationship = relationshipManager.getRelationshipsByIdentityId(johnIdentity.getId());
    assertEquals(3, foundRelationship.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }

  /**
   *
   * @throws Exception
   */
  public void testSaveRelationship() throws Exception {
    Relationship testRelationship = new Relationship(johnIdentity, demoIdentity);
    relationshipManager.saveRelationship(testRelationship);
    assertNotNull("testRelationship.getId() must not be null", testRelationship.getId());

    tearDownRelationshipList.add(testRelationship);
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

  public void testInviteRelationship() throws Exception {

    Relationship relationship = relationshipManager.invite(johnIdentity, maryIdentity);
    assertNotNull(relationship.getId());
    assertEquals(Relationship.Type.PENDING, relationship.getStatus());

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(johnIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(maryIdentity);

    assertEquals(1, senderRelationships.size());
    assertEquals(1, receiverRelationships.size());

    tearDownRelationshipList.addAll(senderRelationships);
  }

  /**
   *
   * @throws Exception
   */
  public void testConfirmRelationship() throws Exception {

    Relationship relationship = relationshipManager.invite(johnIdentity, demoIdentity);
    relationshipManager.confirm(relationship);
    assertNotNull(relationship.getId());
    assertEquals(Relationship.Type.CONFIRM, relationship.getStatus());

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(johnIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(demoIdentity);

    assertEquals(1, senderRelationships.size());
    assertEquals(1, receiverRelationships.size());

    tearDownRelationshipList.addAll(senderRelationships);
  }

  /**
   *
   * @throws Exception
   */
  public void testRemoveRelationship() throws Exception {

    Relationship relationship = relationshipManager.invite(rootIdentity, johnIdentity);
    relationshipManager.confirm(relationship);
    relationshipManager.remove(relationship);

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(rootIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(johnIdentity);

    assertEquals(0, senderRelationships.size());
    assertEquals(0, receiverRelationships.size());

    tearDownRelationshipList.addAll(receiverRelationships);
  }

  /**
   *
   * @throws Exception
   */
  public void testIgnoreRelationship() throws Exception {

    Relationship relationship = relationshipManager.invite(johnIdentity, rootIdentity);
    relationshipManager.ignore(relationship);
    assertNotNull(relationship.getId());
    assertEquals(Relationship.Type.IGNORE, relationship.getStatus());

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(johnIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(rootIdentity);

    assertEquals(1, senderRelationships.size());
    assertEquals(1, receiverRelationships.size());

    tearDownRelationshipList.addAll(senderRelationships);
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