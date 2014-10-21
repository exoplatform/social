package org.exoplatform.social.service.rest.impl.relationship;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.service.rest.RestProperties;
import org.exoplatform.social.service.rest.api.models.RelationshipsCollections;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class RelationshipsRestServiceTest extends AbstractResourceTest {

  private IdentityStorage identityStorage;
  private RelationshipManager relationshipManager;
  private RelationshipsRestServiceV1 relationshipsRestService;
  
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
    
    relationshipsRestService = new RelationshipsRestServiceV1();
    registry(relationshipsRestService);
  }

  public void tearDown() throws Exception {
    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);
    
    super.tearDown();
    unregistry(relationshipsRestService);
  }
  
  public void testGetRelationships() throws Exception {
    Relationship relationship1 = new Relationship(rootIdentity, demoIdentity, Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship1);
    Relationship relationship2 = new Relationship(rootIdentity, johnIdentity, Relationship.Type.PENDING);
    relationshipManager.update(relationship2);
    Relationship relationship3 = new Relationship(rootIdentity, maryIdentity, Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship3);
    
    startSessionAs("root");
    ContainerResponse response = service("GET", "/v1/social/relationships", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    RelationshipsCollections result = (RelationshipsCollections) response.getEntity();
    List<Map<String, Object>> relationships = result.getRelationships();
    assertEquals(3, relationships.size());
    assertEquals("localhost:8080/rest/v1/social/users/root", result.getRelationships().get(0).get(RestProperties.SENDER));
    assertEquals("localhost:8080/rest/v1/social/users/mary", result.getRelationships().get(0).get(RestProperties.RECEIVER));
    
    //clean
    relationshipManager.delete(relationship1);
    relationshipManager.delete(relationship2);
    relationshipManager.delete(relationship3);
  }
  
  public void testCreateRelationship() throws Exception {
    startSessionAs("root");
    String jsonEntity = "{\"sender\":root, \"receiver\":demo, \"status\":CONFIRMED}";
    byte[] jsonData = jsonEntity.getBytes("UTF-8");
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "" + jsonData.length);
    
    ContainerResponse response = service("POST", "/v1/social/relationships/", "", h, jsonData);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Map<String, Object> result = (Map<String, Object>) response.getEntity();
    assertEquals("localhost:8080/rest/v1/social/users/root", result.get(RestProperties.SENDER));
    assertEquals("localhost:8080/rest/v1/social/users/demo", result.get(RestProperties.RECEIVER));
    assertEquals("CONFIRMED", result.get(RestProperties.STATUS));
    
    Relationship relationship = relationshipManager.get(result.get(RestProperties.ID).toString());
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
    ContainerResponse response = service("GET", "/v1/social/relationships/" + relationship.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Map<String, Object> result = (Map<String, Object>) response.getEntity();
    assertEquals(result.get(RestProperties.STATUS), "CONFIRMED");
    
    //clean
    relationshipManager.delete(relationship);
  }
}
