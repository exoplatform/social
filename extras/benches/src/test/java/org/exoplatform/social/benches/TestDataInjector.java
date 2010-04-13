package org.exoplatform.social.benches;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Collection;

import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.space.SpaceService;
import org.testng.annotations.Test;

@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.benches.configuration.xml")})
public class TestDataInjector extends  AbstractJCRTestCase  {
  
  DataInjector injector;
  OrganizationService organizationService;
  
  protected void afterContainerStart() {

    organizationService = new FakeOrganizationService();
    ExoContainerContext.getCurrentContainer().unregisterComponent(OrganizationService.class);
    ExoContainerContext.getCurrentContainer().registerComponentInstance(OrganizationService.class, organizationService); 

  }
  
  @Test
  public void injectPeople() {
    initInjector();     
    
    Collection<Identity> identities = injector.generatePeople(10);    
    assertEquals(identities.size(), 10);
    for (Identity identity : identities) {
      assertNotNull(identity.getId());
    }
  }

  
  @Test
  public void initRandomUser() {

    injector = new DataInjector(null, null, null, null, organizationService);
    User user = new FakeOrganizationService().new SimpleUser("foo");
    injector.initRandomUser(user, "foo");
    assertNotNull(user.getFirstName());
    assertNotNull(user.getLastName());
  }
  
  @Test
  public void injectRelations() {
    
    initInjector(); 
    
    injector.generatePeople(10);   /// injecting relations requires some pple
    Collection<Relationship> relationships = injector.generateRelations(10);    
    assertEquals(relationships.size(), 10);
    for (Relationship relationship : relationships) {
      assertNotNull(relationship.getId());
    }   
  }
  
  
  @Test
  public void injectActivities() {
    
    initInjector();     
    
    injector.generatePeople(10);   /// injecting activities requires some pple
    Collection<Activity> activities = injector.generateActivities(10);    
    assertEquals(activities.size(), 10);
    for (Activity activity : activities) {
      assertNotNull(activity.getId());
    }   
  }
  

  private void initInjector() {
    IdentityManager identityManager = getComponent(IdentityManager.class);
    RelationshipManager relationshipManager = getComponent(RelationshipManager.class);
    SpaceService spaceService = getComponent(SpaceService.class);
    ActivityManager activityManager = getComponent(ActivityManager.class);
    injector = new DataInjector(activityManager, identityManager, relationshipManager, spaceService, organizationService);
  }
  
  
  
}
