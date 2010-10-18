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
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Tests for {@link RelationshipStorage}
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Oct 17, 2010
 * @copyright eXo SAS
 */
public class RelationshipStorageTest extends AbstractCoreTest {

  private final Log LOG = ExoLogger.getLogger(RelationshipStorageTest.class);

  private RelationshipStorage relationshipStorage;

  private IdentityManager identityManger;

  private Identity rootIdentity,
                   johnIdentity,
                   maryIdentity,
                   demoIdentity;

  private List<Relationship> tearDownRelationshipList;


  @Override
  public void setUp() throws Exception {
    super.setUp();
    tearDownRelationshipList = new ArrayList<Relationship>();
    relationshipStorage = (RelationshipStorage) getContainer().getComponentInstanceOfType(RelationshipStorage.class);
    assertNotNull("relationshipStorage must not be null", relationshipStorage);
    identityManger = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull("identityManger must not be null", identityManger);
    rootIdentity = identityManger.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = identityManger.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = identityManger.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = identityManger.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");
  }

  @Override
  public void tearDown() throws Exception {
    for (Relationship relationship : tearDownRelationshipList) {
      relationshipStorage.removeRelationship(relationship);
    }

    identityManger.deleteIdentity(rootIdentity);
    identityManger.deleteIdentity(johnIdentity);
    identityManger.deleteIdentity(maryIdentity);
    identityManger.deleteIdentity(demoIdentity);

    super.tearDown();
  }

  /**
   * Test for {@link RelationshipStorage#saveRelationship(Relationship)}
   */
  public void testSaveRelationship() {
    {
      Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity);
      relationshipStorage.saveRelationship(rootToJohnRelationship);

      tearDownRelationshipList.add(rootToJohnRelationship);
    }
  }

  /**
   * Test for {@link RelationshipStorage#removeRelationship(Relationship)}
   */
  public void testRemoveRelationship() {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity);
    relationshipStorage.saveRelationship(rootToJohnRelationship);
    assertNotNull("rootToJohnRelationship.getId() must not be null", rootToJohnRelationship.getId());

    relationshipStorage.removeRelationship(rootToJohnRelationship);

    assertNull("relationshipStorage.getRelationship(rootToJohnRelationship.getId() must be null",
               relationshipStorage.getRelationship(rootToJohnRelationship.getId()));
  }

  /**
   * Test for {@link RelationshipStorage#getRelationship(String)}
   */
  public void testGetRelationship() {
    assert true;
  }


  /**
   * Test for {@link RelationshipStorage#getRelationshipByIdentity(org.exoplatform.social.core.identity.model.Identity)}
   */
  public void testGetRelationshipByIdentity() {
    assert true;
  }

  /**
   * Test for {@link RelationshipStorage#getRelationshipByIdentityId(String)}
   */
  public void testGetRelationshipByIdentityId() {
    assert true;
  }

  /**
   * Test for {@link RelationshipStorage#getRelationshipIdentitiesByIdentity(org.exoplatform.social.core.identity.model.Identity)}
   */
  public void testGetRelationshipIdentitiesByIdentity() {
    assert true;
  }


}
