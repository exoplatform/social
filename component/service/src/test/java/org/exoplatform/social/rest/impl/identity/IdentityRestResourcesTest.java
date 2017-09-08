package org.exoplatform.social.rest.impl.identity;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class IdentityRestResourcesTest extends AbstractResourceTest {
  private IdentityManager identityManager;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);

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
}
