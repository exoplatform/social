package org.exoplatform.social.rest.impl.relationship;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.rest.api.RestProperties;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.RelationshipEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

import java.util.List;
import java.util.stream.Collectors;

public class RelationshipsRestResourcesTest extends AbstractResourceTest {

  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");
    
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);

    rootIdentity = new Identity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = new Identity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = new Identity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = new Identity(OrganizationIdentityProvider.NAME, "demo");

    identityManager.saveIdentity(rootIdentity);
    identityManager.saveIdentity(johnIdentity);
    identityManager.saveIdentity(maryIdentity);
    identityManager.saveIdentity(demoIdentity);
    
    addResource(RelationshipsRestResourcesV1.class, null);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(RelationshipsRestResourcesV1.class);
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

    List<Object> relationshipsIds = relationships.stream().map(relationship -> relationship.get(RestProperties.ID)).collect(Collectors.toList());
    assertTrue(relationshipsIds.contains(relationship1.getId()));
    assertTrue(relationshipsIds.contains(relationship2.getId()));
    assertTrue(relationshipsIds.contains(relationship3.getId()));
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
