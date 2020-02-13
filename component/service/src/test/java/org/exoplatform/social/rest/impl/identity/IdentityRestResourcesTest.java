package org.exoplatform.social.rest.impl.identity;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class IdentityRestResourcesTest extends AbstractResourceTest {
  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);
    identityManager.saveIdentity(new Identity(OrganizationIdentityProvider.NAME, "root"));
    identityManager.saveIdentity(new Identity(OrganizationIdentityProvider.NAME, "john"));
    identityManager.saveIdentity(new Identity(OrganizationIdentityProvider.NAME, "mary"));
    identityManager.saveIdentity(new Identity(OrganizationIdentityProvider.NAME, "demo"));

    addResource(IdentityRestResourcesV1.class, null);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(IdentityRestResourcesV1.class);
  }

  public void testGetIdentities() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities?limit=5&offset=0", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(4, collections.getEntities().size());
  }
  
  public void testGetCommonConnectionsWithIdentity() throws Exception {
    startSessionAs("root");
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    Identity maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    relationshipManager.inviteToConnect(johnIdentity, maryIdentity);
    relationshipManager.confirm(maryIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, rootIdentity);
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/identities/" + maryIdentity.getId() + "/commonConnections?returnSize=true", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
  }
}
