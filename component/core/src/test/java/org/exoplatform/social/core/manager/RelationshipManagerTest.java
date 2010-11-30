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
   * @throws Exception
   */
  public void testGetPendingRelationships() throws Exception {
    Relationship rootDemo = relationshipManager.invite(rootIdentity, demoIdentity);
    assertNotNull("rootDemo.getId() must not be null", rootDemo.getId());
    Relationship rootJohn = relationshipManager.invite(rootIdentity, johnIdentity);
    assertNotNull("rootJohn.getId() must not be null", rootJohn.getId());
    Relationship rootMary = relationshipManager.invite(rootIdentity, maryIdentity);
    assertNotNull("rootMary.getId() must not be null", rootMary.getId());
    Relationship demoMary = relationshipManager.invite(demoIdentity, maryIdentity);
    assertNotNull("demoMary.getId() must not be null", demoMary.getId());
    Relationship demoJohn = relationshipManager.invite(demoIdentity, johnIdentity);
    assertNotNull("demoJohn.getId() must not be null", demoJohn.getId());
    Relationship johnDemo = relationshipManager.invite(johnIdentity, demoIdentity);
    assertNotNull("johnDemo.getId() must not be null", johnDemo.getId());

    List<Relationship> rootRelationships = relationshipManager.getPendingRelationships(rootIdentity);
    List<Relationship> demoRelationships = relationshipManager.getPendingRelationships(demoIdentity);
    List<Relationship> johnRelationships = relationshipManager.getPendingRelationships(johnIdentity);

    assertEquals(3, rootRelationships.size());
    assertEquals(4, demoRelationships.size());
    assertEquals(3, johnRelationships.size());

    tearDownRelationshipList.add(rootDemo);
    tearDownRelationshipList.add(rootJohn);
    tearDownRelationshipList.add(rootMary);
    tearDownRelationshipList.add(demoMary);
    tearDownRelationshipList.add(demoJohn);
    tearDownRelationshipList.add(johnDemo);
  }

  /**
   * Test relationship with caching.
   * @throws Exception
   */
  public void testSavedCachedRelationship() throws Exception {
    Relationship rootDemo = relationshipManager.getRelationship(rootIdentity, demoIdentity);
    assertNull("rootDemo must be null", rootDemo);
    Relationship rootDemo2 = relationshipManager.getRelationship(demoIdentity, rootIdentity);
    assertNull("rootDemo must be null", rootDemo2);
    Relationship.Type rootDemoStatus = relationshipManager.getRelationshipStatus(rootDemo2,
                                                                                 rootIdentity);
    assertEquals(Relationship.Type.ALIEN, rootDemoStatus);
    rootDemo = relationshipManager.invite(rootIdentity, demoIdentity);
    assertNotNull("rootDemo.getId() must not be null", rootDemo.getId());
    assertEquals(rootDemo.getStatus(), Relationship.Type.PENDING);
    tearDownRelationshipList.add(rootDemo);

    Relationship rootMary = relationshipManager.getRelationship(rootIdentity, maryIdentity);
    Relationship.Type rootMaryStatus = relationshipManager.getRelationshipStatus(rootMary,
                                                                                 rootIdentity);
    assertNull("rootMary must be null", rootMary);
    assertEquals(Relationship.Type.ALIEN, rootMaryStatus);
    rootMary = relationshipManager.invite(rootIdentity, maryIdentity);
    assertNotNull("rootMary.getId() must not be null", rootMary.getId());
    assertEquals(Relationship.Type.PENDING, rootMary.getStatus());
    tearDownRelationshipList.add(rootMary);

    Relationship rootJohn = relationshipManager.getRelationship(rootIdentity, johnIdentity);
    Relationship.Type rootJohnStatus = relationshipManager.getRelationshipStatus(rootJohn,
                                                                                 rootIdentity);
    assertNull("rootJohn must be null", rootJohn);
    assertEquals(Relationship.Type.ALIEN, rootJohnStatus);
    rootJohn = relationshipManager.invite(rootIdentity, johnIdentity);
    assertNotNull("rootJohn.getId() must not be null", rootJohn.getId());
    assertEquals(Relationship.Type.PENDING, rootJohn.getStatus());
    tearDownRelationshipList.add(rootJohn);

    Relationship demoMary = relationshipManager.getRelationship(demoIdentity, maryIdentity);
    Relationship.Type demoMaryStatus = relationshipManager.getRelationshipStatus(demoMary,
                                                                                 demoIdentity);
    assertNull("demoMary must be null", demoMary);
    assertEquals(Relationship.Type.ALIEN, demoMaryStatus);
    demoMary = relationshipManager.invite(demoIdentity, maryIdentity);
    assertNotNull("demoMary.getId() must not be null", demoMary.getId());
    assertEquals(Relationship.Type.PENDING, demoMary.getStatus());
    tearDownRelationshipList.add(demoMary);
  }
}