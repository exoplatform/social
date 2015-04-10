package org.exoplatform.social.rest.impl.userrelationship;

import java.util.List;
import java.util.Map;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.rest.entity.RelationshipsCollections;
import org.exoplatform.social.rest.impl.userrelationship.UsersRelationshipsRestResourcesV1;
import org.exoplatform.social.service.rest.RestProperties;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class UsersRelationshipsRestResourcesTest extends AbstractResourceTest {
  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;
  
  private UsersRelationshipsRestResourcesV1 usersRelationshipsRestService;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");
    
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    
    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);
    
    usersRelationshipsRestService = new UsersRelationshipsRestResourcesV1();
    registry(usersRelationshipsRestService);
  }

  public void tearDown() throws Exception {
    
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    
    super.tearDown();
    unregistry(usersRelationshipsRestService);
  }
  
  public void testGetUserRelationships() throws Exception {
    Relationship relationship1 = new Relationship(rootIdentity, demoIdentity, Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship1);
    Relationship relationship2 = new Relationship(rootIdentity, johnIdentity, Relationship.Type.PENDING);
    relationshipManager.update(relationship2);
    Relationship relationship3 = new Relationship(rootIdentity, maryIdentity, Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship3);
    
    startSessionAs("root");
    ContainerResponse response = service("GET", "/v1/social/usersRelationships", "", null, null);
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
  
  public void testCreateUserRelationship() throws Exception {
    startSessionAs("root");
    //
    String input = "{\"sender\":root, \"receiver\":demo, \"status\":CONFIRMED}";
    ContainerResponse response = getResponse("POST", "/v1/social/usersRelationships/", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Relationship rootDemo = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull(rootDemo);
    assertEquals("root", rootDemo.getSender().getRemoteId());
    assertEquals("demo", rootDemo.getReceiver().getRemoteId());
    assertEquals("CONFIRMED", rootDemo.getStatus().name());
    //
    input = "{\"sender\":mary, \"receiver\":root, \"status\":PENDING}";
    response = getResponse("POST", "/v1/social/usersRelationships/", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Relationship maryRoot = relationshipManager.get(maryIdentity, rootIdentity);
    assertNotNull(maryRoot);
    assertEquals("mary", maryRoot.getSender().getRemoteId());
    assertEquals("root", maryRoot.getReceiver().getRemoteId());
    assertEquals("PENDING", maryRoot.getStatus().name());
    
    //clean
    relationshipManager.delete(rootDemo);
    relationshipManager.delete(maryRoot);
  }
  
  public void testGetUpdateDeleteUserRelationship() throws Exception {
    Relationship relationship = new Relationship(rootIdentity, demoIdentity, Relationship.Type.PENDING);
    relationshipManager.update(relationship);
    //
    startSessionAs("root");
    ContainerResponse response = service("GET", "/v1/social/usersRelationships/" + relationship.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    //update
    String input = "{\"status\":CONFIRMED}";
    response = getResponse("PUT", "/v1/social/usersRelationships/" + relationship.getId(), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertEquals("CONFIRMED", relationship.getStatus().name());
    
    //delete
    response = service("DELETE", "/v1/social/usersRelationships/" + relationship.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNull(relationship);
  }

}
