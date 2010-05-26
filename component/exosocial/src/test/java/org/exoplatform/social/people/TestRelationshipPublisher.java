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
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.relationship.spi.RelationshipEvent;
import org.exoplatform.social.relationship.spi.RelationshipEvent.Type;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.exosocial.configuration.xml")
})
public class TestRelationshipPublisher extends  AbstractKernelTest {

  
  public void testConfirmed() throws Exception {
    
    PortalContainer container = super.getContainer();
    ActivityManager activityManager = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);
    IdentityManager identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    SimpleMockOrganizationService organizationService = (SimpleMockOrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    RelationshipManager relationshipManager = (RelationshipManager)container.getComponentInstanceOfType(RelationshipManager.class);
    
    // register users against the fake org service
    organizationService.addMemberships("mary", "*:/platform/users"); 
    organizationService.addMemberships("john", "*:/platform/users"); 
    
    // inits root and john's identities
    Identity mary = identityManager.getIdentity("organization:mary");
    Identity john = identityManager.getIdentity("organization:john");
  
    RelationshipPublisher publisher = new RelationshipPublisher(null, activityManager, identityManager);
    
    RelationshipEvent event = new RelationshipEvent(Type.CONFIRM, relationshipManager, new Relationship(mary, john));
    
    publisher.confirmed(event);
    
    
    List<Activity> maryActivities = activityManager.getActivities(mary);
    assertEquals(maryActivities.size(), 1);
    assertTrue(maryActivities.get(0).getTitleId().equals("CONNECTION_CONFIRMED"));
    assertTrue(maryActivities.get(0).getTemplateParams().get("Requester").contains("mary"));
    assertTrue(maryActivities.get(0).getTemplateParams().get("Accepter").contains("john"));
    
    List<Activity> johnActivities = activityManager.getActivities(john);
    assertEquals(johnActivities.size(), 1);
    assertTrue(johnActivities.get(0).getTitleId().equals("CONNECTION_CONFIRMED"));
    assertTrue(johnActivities.get(0).getTemplateParams().get("Requester").contains("john"));
    assertTrue(johnActivities.get(0).getTemplateParams().get("Accepter").contains("mary"));
  }
}
