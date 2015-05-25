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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;
import org.exoplatform.social.core.test.QueryNumberTest;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@QueryNumberTest
public class CachedRelationshipStorageTestCase extends AbstractCoreTest {

  private CachedRelationshipStorage relationshipStorage;
  private IdentityStorageImpl identityStorage;
  private SocialStorageCacheService cacheService;
  private IdentityManager identityManager;

  private List<String> tearDownIdentityList;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    relationshipStorage = (CachedRelationshipStorage) getContainer().getComponentInstanceOfType(CachedRelationshipStorage.class);
    identityStorage = (IdentityStorageImpl) getContainer().getComponentInstanceOfType(IdentityStorageImpl.class);
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);

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

  @MaxQueryNumber(156)
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

  @MaxQueryNumber(294)
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

  @MaxQueryNumber(156)
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

  @MaxQueryNumber(165)
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

    Relationship r1 = relationshipStorage.getRelationship(i1, i2);
    assertNotNull(r1);
    assertEquals(1, cacheService.getRelationshipCache().getCacheSize());
    assertEquals(1, cacheService.getRelationshipCacheByIdentity().getCacheSize());

    Relationship r2 = relationshipStorage.getRelationship(i2, i1);
    assertNotNull(r2);
    assertEquals("Same Id", r1.getId(), r2.getId());
    assertEquals("Same symetric", r1.isSymetric(), r2.isSymetric());
    assertEquals("Same receiver", r1.getReceiver(), r2.getReceiver());
    assertEquals("Same sender", r1.getSender(), r2.getSender());
    assertEquals("Same status", r1.getStatus(), r2.getStatus());
    assertEquals(1, cacheService.getRelationshipCache().getCacheSize());
    assertEquals(1, cacheService.getRelationshipCacheByIdentity().getCacheSize());
  }
  
  @MaxQueryNumber(288)
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
  
  public void testGetSuggestion() throws Exception {
    
    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    Identity maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    Identity demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");
    Identity ghostIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "ghost");
    Identity paulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul");
    
    tearDownIdentityList.add(johnIdentity.getId());
    tearDownIdentityList.add(maryIdentity.getId());
    tearDownIdentityList.add(demoIdentity.getId());
    tearDownIdentityList.add(ghostIdentity.getId());
    tearDownIdentityList.add(paulIdentity.getId());
    
    assertEquals(0, cacheService.getSuggestionCache().getCacheSize());
    
    relationshipStorage.getSuggestions(johnIdentity, -1, -1, 10);
    relationshipStorage.getSuggestions(maryIdentity, -1, -1, 10);
    relationshipStorage.getSuggestions(demoIdentity, -1, -1, 10);
    
    assertEquals(3, cacheService.getSuggestionCache().getCacheSize());
    
    relationshipStorage.getSuggestions(ghostIdentity, -1, -1, 10);
    relationshipStorage.getSuggestions(paulIdentity, -1, -1, 10);
    
    assertEquals(5, cacheService.getSuggestionCache().getCacheSize());
    
    //different parameters
    relationshipStorage.getSuggestions(paulIdentity, 5, 5, 10);
    relationshipStorage.getSuggestions(paulIdentity, 5, 10, 10);
    
    assertEquals(7, cacheService.getSuggestionCache().getCacheSize());
    
    // same identity and parameters
    relationshipStorage.getSuggestions(paulIdentity, 5, 5, 10);
    
    assertEquals(7, cacheService.getSuggestionCache().getCacheSize());
    
    // same identity and different parameters
    relationshipStorage.getSuggestions(paulIdentity, 5, 5, 15);
    relationshipStorage.getSuggestions(paulIdentity, 5, 15, 10);
    relationshipStorage.getSuggestions(paulIdentity, 15, 5, 10);
    
    assertEquals(10, cacheService.getSuggestionCache().getCacheSize());
    
    cacheService.getSuggestionCache().clearCache();
    
    assertEquals(0, cacheService.getSuggestionCache().getCacheSize());
    
    relationshipStorage.getSuggestions(maryIdentity, -1, -1, 10);
    relationshipStorage.getSuggestions(maryIdentity, -1, -1, 10);
    
    assertEquals(1, cacheService.getSuggestionCache().getCacheSize());
    
    relationshipStorage.getSuggestions(maryIdentity, -1, -1, 1);
    
    assertEquals(2, cacheService.getSuggestionCache().getCacheSize());
  }
  
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
    createRelationship(kellyIdentity, spearsIdentity, Relationship.Type.PENDING);
    createRelationship(tweedyIdentity, kellyIdentity, Relationship.Type.PENDING);
    
    //
    assertEquals(0, cacheService.getRelationshipsCache().getCacheSize());
    
    //
    relationshipStorage.getOutgoingByFilter(spearsIdentity, new ProfileFilter(), 0, 5);
    assertEquals(2, relationshipStorage.getOutgoingCountByFilter(spearsIdentity, new ProfileFilter()));
    assertEquals(1, relationshipStorage.getOutgoingCountByFilter(kellyIdentity, new ProfileFilter()));
    
    assertEquals(1, cacheService.getRelationshipsCache().getCacheSize());
    assertEquals(2, cacheService.getRelationshipsCount().getCacheSize());
    
    //different limit
    relationshipStorage.getOutgoingByFilter(spearsIdentity, new ProfileFilter(), 0, 10);
    
    //check cache size
    assertEquals(2, cacheService.getRelationshipsCache().getCacheSize());
    
    //clear cache
    cacheService.getRelationshipsCache().clearCache();
    cacheService.getRelationshipsCount().clearCache();
    assertEquals(0, cacheService.getRelationshipsCache().getCacheSize());
    assertEquals(0, cacheService.getRelationshipsCount().getCacheSize());
    
    //check with ProfileFilter is not empty
    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setName("spears");
    
    relationshipStorage.getOutgoingByFilter(spearsIdentity, profileFilter, 0, 10);
    relationshipStorage.getOutgoingByFilter(christmasIdentity, profileFilter, 0, 10);
    relationshipStorage.getOutgoingByFilter(kellyIdentity, profileFilter, 0, 10);
    
    assertEquals(6, cacheService.getRelationshipsCache().getCacheSize());
    
    relationshipStorage.getOutgoingCountByFilter(tweedyIdentity, profileFilter);
    assertEquals(1, cacheService.getRelationshipsCount().getCacheSize());
  }
  
  public void testGetIncomingByFilter()  throws Exception {
    //
    Identity spearsIdentity = createIdentity("spears");
    Identity williamsIdentity = createIdentity("williams");
    Identity christmasIdentity = createIdentity("christmas");
    Identity kellyIdentity = createIdentity("kelly");
    
    //
    createRelationship(williamsIdentity, spearsIdentity, Relationship.Type.PENDING);
    createRelationship(spearsIdentity, christmasIdentity, Relationship.Type.PENDING);
    createRelationship(christmasIdentity, kellyIdentity, Relationship.Type.PENDING);
    
    //
    assertEquals(0, cacheService.getRelationshipsCache().getCacheSize());
    
    relationshipStorage.getIncomingByFilter(spearsIdentity, new ProfileFilter(), 0, 5);
    assertEquals(1, relationshipStorage.getIncomingCountByFilter(spearsIdentity, new ProfileFilter()));
    assertEquals(1, relationshipStorage.getIncomingCountByFilter(kellyIdentity, new ProfileFilter()));
    
    assertEquals(1, cacheService.getRelationshipsCache().getCacheSize());
    assertEquals(2, cacheService.getRelationshipsCount().getCacheSize());
    
    //different limit
    relationshipStorage.getIncomingByFilter(spearsIdentity, new ProfileFilter(), 0, 10);
    assertEquals(2, cacheService.getRelationshipsCache().getCacheSize());
    
    //clear cache
    cacheService.getRelationshipsCache().clearCache();
    cacheService.getRelationshipsCount().clearCache();
    assertEquals(0, cacheService.getRelationshipsCache().getCacheSize());
    assertEquals(0, cacheService.getRelationshipsCount().getCacheSize());
    
    //check with ProfileFilter is not empty
    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setName("spears");
    
    relationshipStorage.getOutgoingByFilter(spearsIdentity, profileFilter, 0, 10);
    relationshipStorage.getOutgoingByFilter(christmasIdentity, profileFilter, 0, 10);
    relationshipStorage.getOutgoingByFilter(kellyIdentity, profileFilter, 0, 10);
    assertEquals(6, cacheService.getRelationshipsCache().getCacheSize());
    
    relationshipStorage.getIncomingCountByFilter(kellyIdentity, profileFilter);
    assertEquals(1, cacheService.getRelationshipsCount().getCacheSize());
  }
  
  
  private Identity createIdentity(String remoteId) {
    Identity identity = new Identity("organization", remoteId);
    identityStorage.saveIdentity(identity);
    tearDownIdentityList.add(identity.getId());
    return identity;
  }

  private Relationship createRelationship(Identity sender, Identity receiver, Relationship.Type status) throws Exception {
    Relationship relationship = new Relationship(sender, receiver);
    relationship.setStatus(status);
    relationshipStorage.saveRelationship(relationship);
    
    return relationship;
  }
  
}
