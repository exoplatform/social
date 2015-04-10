package org.exoplatform.social.rest.impl.relationship;

import java.util.List;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.rest.api.RestProperties;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.RelationshipEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class RelationshipsRestResourcesTest extends AbstractResourceTest {

  private IdentityStorage identityStorage;
  private RelationshipManager relationshipManager;
  private RelationshipsRestResourcesV1  relationshipsRestResources;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");
    
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    
    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");
    
    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);
    
    relationshipsRestResources = new RelationshipsRestResourcesV1();
    registry(relationshipsRestResources);
  }

  public void tearDown() throws Exception {
    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);
    
    super.tearDown();
    removeResource(relationshipsRestResources.getClass());
  }
  
  public void testGetRelationships() throws Exception {
    Relationship relationship1 = new Relationship(rootIdentity, demoIdentity, Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship1);
    Relationship relationship2 = new Relationship(rootIdentity, johnIdentity, Relationship.Type.PENDING);
    relationshipManager.update(relationship2);
    Relationship relationship3 = new Relationship(rootIdentity, maryIdentity, Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship3);
    
    startSessionAs("root");
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/relationships", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    List<? extends DataEntity> relationships = collections.getEntities();
    assertEquals(3, relationships.size());
    
    assertEquals("/rest/" + VersionResources.VERSION_ONE + "/social/users/root", relationships.get(0).get(RestProperties.SENDER));
    assertEquals("/rest/" + VersionResources.VERSION_ONE + "/social/users/mary", relationships.get(0).get(RestProperties.RECEIVER));
    
    //clean
    relationshipManager.delete(relationship1);
    relationshipManager.delete(relationship2);
    relationshipManager.delete(relationship3);
  }
  
  public void testCreateRelationship() throws Exception {
    startSessionAs("root");
    //
    String input = "{\"sender\":\"root\", \"receiver\":\"demo\", \"status\":\"CONFIRMED\"}";
    ContainerResponse response = getResponse("POST", "/" + VersionResources.VERSION_ONE + "/social/relationships/", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    RelationshipEntity result = getBaseEntity(response.getEntity(), RelationshipEntity.class);
    assertEquals("/rest/" + VersionResources.VERSION_ONE + "/social/users/root", result.getSender());
    assertEquals("/rest/" + VersionResources.VERSION_ONE + "/social/users/demo", result.getReceiver());
    assertEquals("CONFIRMED", result.getStatus());
    
    Relationship relationship = relationshipManager.get(result.getId());
    assertNotNull(relationship);
    assertEquals("root", relationship.getSender().getRemoteId());
    assertEquals("demo", relationship.getReceiver().getRemoteId());
    assertEquals("CONFIRMED", relationship.getStatus().name());
    
    //clean
    relationshipManager.delete(relationship);
  }
  
  public void testGetRelationshipById() throws Exception {
    Relationship relationship = new Relationship(rootIdentity, demoIdentity, Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship);
    
    startSessionAs("root");
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationship.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    RelationshipEntity result = getBaseEntity(response.getEntity(), RelationshipEntity.class) ;
    assertEquals("CONFIRMED", result.getStatus());
    
    //clean
    relationshipManager.delete(relationship);
  }
  
  public void testDeleteRelationshipById() throws Exception {
    Relationship relationship = new Relationship(rootIdentity, demoIdentity, Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship);
    
    startSessionAs("root");
    ContainerResponse response = service("DELETE", "/" + VersionResources.VERSION_ONE + "/social/relationships/" + relationship.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    RelationshipEntity entity = getBaseEntity(response.getEntity(), RelationshipEntity.class) ;
    relationship = relationshipManager.get(entity.getId());
    assertNull(relationship);
  }
}
