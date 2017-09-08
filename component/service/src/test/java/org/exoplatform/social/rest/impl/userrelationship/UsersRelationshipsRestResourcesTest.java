package org.exoplatform.social.rest.impl.userrelationship;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.rest.api.RestProperties;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

import java.util.List;
import java.util.stream.Collectors;

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
    
    usersRelationshipsRestService = new UsersRelationshipsRestResourcesV1();
    registry(usersRelationshipsRestService);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(usersRelationshipsRestService.getClass());
  }

  public void testGetUserRelationships() throws Exception {
    Relationship relationship1 = new Relationship(rootIdentity, demoIdentity, Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship1);
    Relationship relationship2 = new Relationship(rootIdentity, johnIdentity, Relationship.Type.PENDING);
    relationshipManager.update(relationship2);
    Relationship relationship3 = new Relationship(rootIdentity, maryIdentity, Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship3);
    
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("usersRelationships"), "", null, null);
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

  public void testCreateUserRelationship() throws Exception {
    startSessionAs("root");

    //
    String input = "{\"sender\":root, \"receiver\":demo, \"status\":CONFIRMED}";
    ContainerResponse response = getResponse("POST", getURLResource("usersRelationships/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Relationship rootDemo = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull(rootDemo);
    assertEquals("root", rootDemo.getSender().getRemoteId());
    assertEquals("demo", rootDemo.getReceiver().getRemoteId());
    assertEquals("CONFIRMED", rootDemo.getStatus().name());

    //
    input = "{\"sender\":mary, \"receiver\":root, \"status\":PENDING}";
    response = getResponse("POST", getURLResource("usersRelationships/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Relationship maryRoot = relationshipManager.get(maryIdentity, rootIdentity);
    assertNotNull(maryRoot);
    assertEquals("mary", maryRoot.getSender().getRemoteId());
    assertEquals("root", maryRoot.getReceiver().getRemoteId());
    assertEquals("PENDING", maryRoot.getStatus().name());

    //
    input = "{\"sender\":root, \"receiver\":john, \"status\":IGNORED}";
    response = getResponse("POST", getURLResource("usersRelationships/"), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Relationship rootJohn = relationshipManager.get(rootIdentity, johnIdentity);
    assertNotNull(rootJohn);
    assertEquals("root", rootJohn.getSender().getRemoteId());
    assertEquals("john", rootJohn.getReceiver().getRemoteId());
    assertEquals("IGNORED", rootJohn.getStatus().name());
  }

  public void testGetUpdateDeleteUserRelationship() throws Exception {
    Relationship relationship = new Relationship(demoIdentity, rootIdentity, Relationship.Type.PENDING);
    relationshipManager.update(relationship);
    //
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("usersRelationships/" + relationship.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    //update
    String input = "{\"status\":CONFIRMED}";
    response = getResponse("PUT", getURLResource("usersRelationships/" + relationship.getId()), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertEquals("CONFIRMED", relationship.getStatus().name());
    
    //delete
    response = service("DELETE", getURLResource("usersRelationships/" + relationship.getId()), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    relationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNull(relationship);
  }

}
