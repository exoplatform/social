package org.exoplatform.social.people;



import java.util.List;

import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.commons.testing.mock.SimpleMockOrganizationService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.spi.ProfileLifeCycleEvent;
import org.exoplatform.social.core.identity.spi.ProfileLifeCycleEvent.Type;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.exosocial.configuration.xml")})
public class TestProfileUpdatesPublisher extends  AbstractJCRTestCase {

  
  @Test
  public void publishActivity() throws Exception {
    
    ActivityManager activityManager = super.getComponent(ActivityManager.class);
    IdentityManager identityManager =  super.getComponent(IdentityManager.class);
    SimpleMockOrganizationService organizationService = super.getComponent(OrganizationService.class);
    
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
    assertTrue(activities.get(0).getBody().contains("basic"));
    
  }
}
