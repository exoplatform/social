package org.exoplatform.social.people;



import java.util.List;

import org.exoplatform.commons.testing.mock.SimpleMockOrganizationService;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.spi.ProfileLifeCycleEvent;
import org.exoplatform.social.core.identity.spi.ProfileLifeCycleEvent.Type;

@ConfiguredBy({
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.exosocial.configuration.xml")
  })
public class TestProfileUpdatesPublisher extends  AbstractKernelTest {

  
  public void testPublishActivity() throws Exception {
    
    PortalContainer container = super.getContainer();
    ActivityManager activityManager = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);
    IdentityManager identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    SimpleMockOrganizationService organizationService = (SimpleMockOrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    
    organizationService.addMemberships("root", "*:/platform/users"); // register root against the fake org service
    
    ProfileUpdatesPublisher publisher = new ProfileUpdatesPublisher(null, activityManager, identityManager);
    
    
    // create an identity
    Identity identity = identityManager.getIdentity("organization:root", true);
    Profile profile = identity.getProfile();
    //profile.setProperty(Profile.FIRST_NAME, "foo");
    //identityManager.getIdentityStorage().saveProfile(profile);
    
    ProfileLifeCycleEvent event = new ProfileLifeCycleEvent(Type.BASIC_UPDATED, "root", profile);
    publisher.basicInfoUpdated(event);
    
    // check that the activity was created and that it contains what we expect
    List<Activity> activities = activityManager.getActivities(identity);
    assertEquals(1, activities.size());
    assertTrue(activities.get(0).getTitle().contains("basic"));
    
  }
}
