package org.exoplatform.social.core.activitystream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.commons.testing.mock.SimpleMockOrganizationService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

@ConfiguredBy( {
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.people.configuration.xml") })
public class TestActivityManager extends AbstractJCRTestCase {

  public TestActivityManager() throws Exception {
    super();
  }

  @Test
  public void testSubstituteUsernames() throws Exception {
    ActivityManager activityManager = getComponent(ActivityManager.class);

    SimpleMockOrganizationService organizationService = (SimpleMockOrganizationService) getComponent(OrganizationService.class);
    organizationService.addMemberships("root", "member:/platform/users");
    organizationService.addMemberships("john", "member:/platform/users");
    OrganizationIdentityProvider provider = getComponent(IdentityProvider.class);

    Assert.assertNotNull(provider.getIdentityByRemoteId("root"));

    
    Activity activity = null;
    activityManager.substituteUsernames(activity);
    assertNull(activity);
    
    activity = new Activity();
    activityManager.substituteUsernames(activity);
    assertNull(activity.getTitle());
    assertNull(activity.getBody());

    activity.setTitle("single @root substitution");
    activityManager.substituteUsernames(activity);
    assertEquals(activity.getTitle(),
                 "single <a href=\"/portal/private/classic/profile/root\" class=\"link\" target=\"_parent\">root root</a> substitution");
    assertNull(activity.getBody());

    activity.setTitle("@root and @john title");
    activity.setBody("body with @root and @john");
    activityManager.substituteUsernames(activity);
    assertEquals(activity.getTitle(),
                 "<a href=\"/portal/private/classic/profile/root\" class=\"link\" target=\"_parent\">root root</a> and <a href=\"/portal/private/classic/profile/john\" class=\"link\" target=\"_parent\">john john</a> title");
    assertEquals(activity.getBody(),
                 "body with <a href=\"/portal/private/classic/profile/root\" class=\"link\" target=\"_parent\">root root</a> and <a href=\"/portal/private/classic/profile/john\" class=\"link\" target=\"_parent\">john john</a>");

  }


}
