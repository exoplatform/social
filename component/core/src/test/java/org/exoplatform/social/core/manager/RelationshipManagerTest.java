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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Tests for {@link RelationshipManager}
 *
 */
public class RelationshipManagerTest extends AbstractCoreTest {
  private Log LOG = ExoLogger.getLogger(RelationshipManagerTest.class);
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