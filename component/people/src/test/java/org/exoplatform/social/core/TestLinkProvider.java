package org.exoplatform.social.core;

import static org.testng.Assert.assertEquals;

import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.commons.testing.mock.SimpleMockOrganizationService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.organization.OrganizationService;
import org.testng.annotations.Test;

@ConfiguredBy( {
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.people.configuration.xml") })

public class TestLinkProvider  extends AbstractJCRTestCase {

  
  @Test
  public void testGetProfileLink() throws Exception {
    LinkProvider provider = getComponent(LinkProvider.class);

    SimpleMockOrganizationService organizationService = (SimpleMockOrganizationService) getComponent(OrganizationService.class);
    organizationService.addMemberships("root", "member:/platform/users");

    assertEquals(null, provider.getProfileLink(null));

    // but when we have the identity we generate a link
    String link = provider.getProfileLink("root");
    assertEquals(link, "<a href=\"/portal/private/classic/profile/root\" class=\"link\" target=\"_parent\">root root</a>");
  }
}
