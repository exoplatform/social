package org.exoplatform.social.core.identity.impl.organization;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.commons.testing.mock.SimpleMockOrganizationService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.JCRStorage;
import org.exoplatform.social.core.identity.model.Identity;
import org.testng.annotations.Test;

@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.people.configuration.xml")})
public class TestGroupIdentityProvider extends AbstractJCRTestCase {

  public TestGroupIdentityProvider() throws Exception {
    super();
  }

  private GroupIdentityProvider groupIdentityProvider;
  private JCRStorage identityStorage;

  protected void afterContainerStart() {

    groupIdentityProvider = getComponent(GroupIdentityProvider.class);
    identityStorage = getComponent(JCRStorage.class);
    SimpleMockOrganizationService  organizationService = (SimpleMockOrganizationService) getComponent(OrganizationService.class);
    organizationService.addMemberships("john", "member:/platform/users");
  }

  @Test
  public void testIsConfigured() {
    assertNotNull(groupIdentityProvider);
  }
  
  @Test
  public void testGetByRemoteId() throws Exception {
    Identity identity = new Identity(GroupIdentityProvider.NAME, "foobarfoo");

    // identity does not exist
    assertNull(groupIdentityProvider.getIdentityByRemoteId("foobarfoo"));
    
    // null identity
    assertNull(groupIdentityProvider.getIdentityByRemoteId(null));
    
    // identity for an existing group
    identityStorage.saveIdentity(identity);
    identity.setRemoteId("/platform/users");
    
    Identity actual = groupIdentityProvider.getIdentityByRemoteId("/platform/users");
    assertNotNull(actual);
    assertEquals(actual.getRemoteId(), identity.getRemoteId());
  
  }

}
