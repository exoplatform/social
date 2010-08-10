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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.common.jcr.JCRSessionManager;
import org.exoplatform.social.common.jcr.QueryBuilder;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.RelationshipStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class RelationshipManagerTest extends AbstractCoreTest {
  RelationshipManager relationshipManager;
  IdentityManager identityManager;
  JCRSessionManager sessionManager;

  @Override
  protected void beforeRunBare() throws Exception {
    super.beforeRunBare();
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);

    RepositoryService repositoryService = (RepositoryService) getContainer().getComponentInstanceOfType(RepositoryService.class);
    sessionManager = new JCRSessionManager("portal-test", repositoryService);
    assertNotNull("relationshipManager must not be null", relationshipManager);
    assertNotNull("identityManager must not be null", identityManager);
  }

  @Override
  protected void afterRunBare() {
    super.afterRunBare();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    try {
      Session session = sessionManager.openSession();
      final List<Node> nodes = new QueryBuilder(session)
        .select(RelationshipStorage.RELATION_NODETYPE).exec();

      for (Node node : nodes) {
        node.remove();
      }

      session.save();
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testGetRelationshipByIdentityId() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;

    Identity sender = identityManager.getOrCreateIdentity(providerId,"john");
    identityManager.saveIdentity(sender);
    assertNotNull(sender.getId());

    Identity receiver = identityManager.getOrCreateIdentity(providerId,"james");
    assertNotNull(receiver.getId());

    relationshipManager.invite(sender, receiver);
    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(sender);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(receiver);

    assertEquals(1, senderRelationships.size());
    assertEquals(1, receiverRelationships.size());
  }

  public void testGetManyRelationshipsByIdentityId() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;

    Identity sender = identityManager.getOrCreateIdentity(providerId,"john");
    identityManager.saveIdentity(sender);
    assertNotNull(sender.getId());

    Identity receiver = identityManager.getOrCreateIdentity(providerId,"james");
    assertNotNull(receiver.getId());

    int total = 20;
    for (int i = 0; i < total; i++) {
      relationshipManager.invite(sender, receiver);
    }

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(sender);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(receiver);

    assertEquals(total, senderRelationships.size());
    assertEquals(total, receiverRelationships.size());
  }

  public void testInviteRelationship() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;

    Identity sender = identityManager.getOrCreateIdentity(providerId,"john");
    identityManager.saveIdentity(sender);
    assertNotNull(sender.getId());

    Identity receiver = identityManager.getOrCreateIdentity(providerId,"james");
    assertNotNull(receiver.getId());

    Relationship relationship = relationshipManager.invite(sender, receiver);
    assertNotNull(relationship.getId());
    assertEquals(Relationship.Type.PENDING, relationship.getStatus());

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(sender);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(receiver);

    assertEquals(1, senderRelationships.size());
    assertEquals(1, receiverRelationships.size());
  }

  public void testConfirmRelationship() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;

    Identity sender = identityManager.getOrCreateIdentity(providerId,"john");
    identityManager.saveIdentity(sender);
    assertNotNull(sender.getId());

    Identity receiver = identityManager.getOrCreateIdentity(providerId,"james");
    assertNotNull(receiver.getId());

    Relationship relationship = relationshipManager.invite(sender, receiver);
    relationshipManager.confirm(relationship);
    assertNotNull(relationship.getId());
    assertEquals(Relationship.Type.CONFIRM, relationship.getStatus());

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(sender);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(receiver);

    assertEquals(1, senderRelationships.size());
    assertEquals(1, receiverRelationships.size());
  }

  public void testRemoveRelationship() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;

    Identity sender = identityManager.getOrCreateIdentity(providerId,"john");
    identityManager.saveIdentity(sender);
    assertNotNull(sender.getId());

    Identity receiver = identityManager.getOrCreateIdentity(providerId,"james");
    assertNotNull(receiver.getId());

    Relationship relationship = relationshipManager.invite(sender, receiver);
    relationshipManager.confirm(relationship);
    relationshipManager.remove(relationship);

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(sender);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(receiver);

    assertEquals(0, senderRelationships.size());
    assertEquals(0, receiverRelationships.size());
  }

  public void testIgnoreRelationship() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;

    Identity sender = identityManager.getOrCreateIdentity(providerId,"john");
    identityManager.saveIdentity(sender);
    assertNotNull(sender.getId());

    Identity receiver = identityManager.getOrCreateIdentity(providerId,"james");
    assertNotNull(receiver.getId());

    Relationship relationship = relationshipManager.invite(sender, receiver);
    relationshipManager.ignore(relationship);
    assertNotNull(relationship.getId());
    assertEquals(Relationship.Type.IGNORE, relationship.getStatus());

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(sender);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(receiver);

    assertEquals(1, senderRelationships.size());
    assertEquals(1, receiverRelationships.size());
  }

  public void testGetPendingRelationships() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;

    Identity sender = identityManager.getOrCreateIdentity(providerId,"john");
    identityManager.saveIdentity(sender);
    assertNotNull(sender.getId());

    Identity receiver = identityManager.getOrCreateIdentity(providerId,"james");
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
}