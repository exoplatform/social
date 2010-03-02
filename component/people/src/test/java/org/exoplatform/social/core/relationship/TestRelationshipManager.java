/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.core.relationship;

import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.test.BasicPeopleServiceTestCase;


public class TestRelationshipManager extends BasicPeopleServiceTestCase {
  
  RelationshipManager relationshipManager;
  IdentityManager identityManager;
  	
  public TestRelationshipManager() throws Exception {
	super();  
	// TODO Auto-generated constructor stub
  }

  public void setUp() throws Exception {
	super.setUp();
	identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
	relationshipManager = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
  }
  
  public void testRelationshipService() {
	assertNotNull(relationshipManager);
  }

  private void init() throws Exception {

    Identity identity1 = identityManager.getIdentityByRemoteId("organization", "john");
    identityManager.saveIdentity(identity1);
    assertNotNull(identity1.getId());

    Identity identity2 = identityManager.getIdentityByRemoteId("organization", "james");
    identityManager.saveIdentity(identity2);
    assertNotNull(identity2.getId());

    Identity identity3 = identityManager.getIdentityByRemoteId("organization", "root");
    identityManager.saveIdentity(identity3);
    assertNotNull(identity3.getId());
    
    Relationship relationship = relationshipManager.create(identity1, identity2);
    relationship.addProperty(new Property("friend", true, Relationship.Type.PENDING));
    relationship.addProperty(new Property("co-worker", true));
    relationshipManager.save(relationship);
    assertNotNull(relationship.getId());


    Relationship relationship2 = relationshipManager.create(identity1, identity3);
    relationship2.addProperty(new Property("friend", true, Relationship.Type.PENDING));
    relationship2.addProperty(new Property("relative", true));
    relationshipManager.save(relationship2);
    assertNotNull(relationship2.getId());
  }

  public void testSave() throws Exception {
    /*RelationshipManager relationshipManager = (RelationshipManager) StandaloneContainer.getInstance().getComponentInstanceOfType(RelationshipManager.class);
    assertNotNull(relationshipManager);

    init();

    Relationship relationshipBis = relationshipManager.getById(relationship.getId());
    assertEquals(relationship.getId(), relationshipBis.getId());
    assertEquals(relationship.getIdentity1().getId(), relationshipBis.getIdentity1().getId());
    assertEquals(relationship.getIdentity2().getId(), relationshipBis.getIdentity2().getId());
    assertEquals(relationship.getProperties().size(), relationshipBis.getProperties().size());

    for (Property prop : relationshipBis.getProperties()) {
      if (!((prop.getName().equals("friend")) || (prop.getName().equals("co-worker")))) {
        fail("wrong property");
      }
    }

    Relationship relationship2Bis = relationshipManager.getById(relationship2.getId());
    assertEquals(relationship2.getId(), relationship2Bis.getId());
    assertEquals(relationship2.getIdentity1().getId(), relationship2Bis.getIdentity1().getId());
    assertEquals(relationship2.getIdentity2().getId(), relationship2Bis.getIdentity2().getId());
    assertEquals(relationship2.getProperties().size(), relationship2Bis.getProperties().size());

    for (Property prop : relationship2Bis.getProperties()) {
      if (!((prop.getName().equals("friend")) || (prop.getName().equals("relative")))) {
        fail("wrong property");
      }
    }*/
  }


  public void testGet() throws Exception {
    /*RelationshipManager relationshipManager = (RelationshipManager) StandaloneContainer.getInstance().getComponentInstanceOfType(RelationshipManager.class);
    assertNotNull(relationshipManager);

    init();

    List<Relationship> rels = relationshipManager.get(relationship.getIdentity1());
    assertNotNull(rels);
    assertEquals(2, rels.size());

    rels = relationshipManager.get(relationship2.getIdentity2());
    assertNotNull(rels);
    assertEquals(1, rels.size());
    assertEquals(relationship2.getIdentity2().getId(), rels.get(0).getIdentity2().getId());
    assertEquals(relationship2.getIdentity1().getId(), rels.get(0).getIdentity1().getId());
    String idRel = rels.get(0).getId();

    rels = relationshipManager.get(relationship2.getIdentity2());
    assertNotNull(rels);
    assertEquals(1, rels.size());

    assertEquals("the relationship id should be the same as the previous since it's about the same relation",
        idRel, rels.get(0).getId());*/
  }
}
