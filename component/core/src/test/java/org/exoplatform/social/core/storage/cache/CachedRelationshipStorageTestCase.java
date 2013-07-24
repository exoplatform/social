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

package org.exoplatform.social.core.storage.cache;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;
import org.exoplatform.social.core.test.QueryNumberTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@QueryNumberTest
public class CachedRelationshipStorageTestCase extends AbstractCoreTest {

  private CachedRelationshipStorage relationshipStorage;
  private IdentityStorageImpl identityStorage;
  private SocialStorageCacheService cacheService;

  private List<String> tearDownIdentityList;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    relationshipStorage = (CachedRelationshipStorage) getContainer().getComponentInstanceOfType(CachedRelationshipStorage.class);
    identityStorage = (IdentityStorageImpl) getContainer().getComponentInstanceOfType(IdentityStorageImpl.class);

    cacheService = (SocialStorageCacheService) getContainer().getComponentInstanceOfType(SocialStorageCacheService.class);
    cacheService.getRelationshipCache().clearCache();
    cacheService.getRelationshipCacheByIdentity().clearCache();
    cacheService.getRelationshipsCache().clearCache();
    cacheService.getRelationshipsCount().clearCache();

    tearDownIdentityList = new ArrayList<String>();
  }

  @Override
  public void tearDown() throws Exception {
    for (String id : tearDownIdentityList) {
      identityStorage.deleteIdentity(new Identity(id));
    }
    super.tearDown();
  }

  @MaxQueryNumber(150)
  public void testSaveRelationship() throws Exception {

    Identity i1 = new Identity("p", "i1");
    identityStorage.saveIdentity(i1);
    tearDownIdentityList.add(i1.getId());
    Identity i2 = new Identity("p", "i2");
    identityStorage.saveIdentity(i2);
    tearDownIdentityList.add(i2.getId());
    
    assertEquals(0, cacheService.getRelationshipCache().getCacheSize());
    relationshipStorage.saveRelationship(new Relationship(i1, i2));
    assertEquals(1, cacheService.getRelationshipCache().getCacheSize());
    assertEquals(0, cacheService.getRelationshipsCache().getCacheSize());
    assertEquals(0, cacheService.getRelationshipsCount().getCacheSize());

  }

  @MaxQueryNumber(234)
  public void testRemoveRelationship() throws Exception {

    Identity i1 = new Identity("p", "i1");
    identityStorage.saveIdentity(i1);
    tearDownIdentityList.add(i1.getId());
    Identity i2 = new Identity("p", "i2");
    identityStorage.saveIdentity(i2);
    tearDownIdentityList.add(i2.getId());

    assertEquals(0, cacheService.getRelationshipCache().getCacheSize());
    Relationship r = relationshipStorage.saveRelationship(new Relationship(i1, i2));
    assertEquals(1, cacheService.getRelationshipCache().getCacheSize());
    relationshipStorage.getRelationshipsCount(i1);
    relationshipStorage.getRelationships(i1, 0, 10);
    relationshipStorage.getRelationships(i2, 0, 10);
    relationshipStorage.removeRelationship(r);
    assertEquals(0, cacheService.getRelationshipCache().getCacheSize());
    assertEquals(0, cacheService.getRelationshipsCount().getCacheSize());

  }

  @MaxQueryNumber(150)
  public void testGetRelationship() throws Exception {

    Identity i1 = new Identity("p", "i1");
    identityStorage.saveIdentity(i1);
    tearDownIdentityList.add(i1.getId());
    Identity i2 = new Identity("p", "i2");
    identityStorage.saveIdentity(i2);
    tearDownIdentityList.add(i2.getId());

    assertEquals(0, cacheService.getRelationshipCache().getCacheSize());
    Relationship r = relationshipStorage.saveRelationship(new Relationship(i1, i2));
    assertEquals(1, cacheService.getRelationshipCache().getCacheSize());
    cacheService.getRelationshipCache().clearCache();
    cacheService.getRelationshipCacheByIdentity().clearCache();
    assertEquals(0, cacheService.getRelationshipCache().getCacheSize());

    relationshipStorage.getRelationship(r.getId());
    assertEquals(1, cacheService.getRelationshipCache().getCacheSize());
    assertEquals(0, cacheService.getRelationshipCacheByIdentity().getCacheSize());
    
  }

  @MaxQueryNumber(150)
  public void testName() throws Exception {

    Identity i1 = new Identity("p", "i1");
    identityStorage.saveIdentity(i1);
    tearDownIdentityList.add(i1.getId());
    Identity i2 = new Identity("p", "i2");
    identityStorage.saveIdentity(i2);
    tearDownIdentityList.add(i2.getId());

    assertEquals(0, cacheService.getRelationshipCache().getCacheSize());
    Relationship r = relationshipStorage.saveRelationship(new Relationship(i1, i2));
    assertEquals(1, cacheService.getRelationshipCache().getCacheSize());
    cacheService.getRelationshipCache().clearCache();
    cacheService.getRelationshipCacheByIdentity().clearCache();
    assertEquals(0, cacheService.getRelationshipCache().getCacheSize());

    relationshipStorage.getRelationship(i1, i2);
    assertEquals(1, cacheService.getRelationshipCache().getCacheSize());
    assertEquals(1, cacheService.getRelationshipCacheByIdentity().getCacheSize());

  }
  
  @MaxQueryNumber(220)
  public void testGetRelationshipIdentity() throws Exception {

    Identity i1 = new Identity("p", "i1");
    identityStorage.saveIdentity(i1);
    tearDownIdentityList.add(i1.getId());
    Identity i2 = new Identity("p", "i2");
    identityStorage.saveIdentity(i2);
    tearDownIdentityList.add(i2.getId());

    assertEquals(0, cacheService.getRelationshipCache().getCacheSize());
    Relationship r = relationshipStorage.saveRelationship(new Relationship(i1, i2));
    assertEquals(1, cacheService.getRelationshipCache().getCacheSize());
    cacheService.getRelationshipCache().clearCache();
    cacheService.getRelationshipCacheByIdentity().clearCache();
    assertEquals(0, cacheService.getRelationshipCache().getCacheSize());

    relationshipStorage.getRelationship(i1, i2);
    assertEquals(1, cacheService.getRelationshipCache().getCacheSize());
    assertEquals(1, cacheService.getRelationshipCacheByIdentity().getCacheSize());

    
    relationshipStorage.removeRelationship(r);
    assertEquals(0, cacheService.getRelationshipCache().getCacheSize());
    assertEquals(0, cacheService.getRelationshipCacheByIdentity().getCacheSize());
  }

}
