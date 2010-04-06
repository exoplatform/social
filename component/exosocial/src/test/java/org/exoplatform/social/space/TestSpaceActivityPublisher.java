package org.exoplatform.social.space;

import static org.testng.Assert.assertNotNull;

import java.util.List;

import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceActivityPublisher;
import org.exoplatform.social.space.SpaceIdentityProvider;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.impl.JCRStorage;
import org.exoplatform.social.space.impl.SpaceServiceImpl;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml")})
public class TestSpaceActivityPublisher extends  AbstractJCRTestCase {

  public TestSpaceActivityPublisher() {
    super();
    // TODO Auto-generated constructor stub
  }

  @Test
  public void testSpaceCreation() throws Exception {

    ActivityManager activityManager = super.getComponent(ActivityManager.class);
    
    assertNotNull(activityManager, "ActivityManager not initialized. Check test configuration");
    IdentityManager identityManager =  super.getComponent(IdentityManager.class);
    
    SpaceServiceImpl spaceService = (SpaceServiceImpl)super.getComponent(SpaceService.class);
    JCRStorage spaceStorage = spaceService.getStorage();
    
    SpaceActivityPublisher  publisher = new SpaceActivityPublisher(null, activityManager, identityManager);
    
    Space space = new Space();
    space.setUrl("toto");
    space.setName("Toto");
    space.setGroupId("/spaces/toto");
    spaceStorage.saveSpace(space, true);
    String spaceId = space.getId(); // set by storage

    
    SpaceLifeCycleEvent event  = new SpaceLifeCycleEvent(space, "root", SpaceLifeCycleEvent.Type.SPACE_CREATED);
    publisher.spaceCreated(event);
    
    Identity identity = identityManager.getIdentity(SpaceIdentityProvider.NAME + ":" + spaceId);
    List<Activity> activities = activityManager.getActivities(identity);
    assertEquals(1, activities.size());
    assertTrue(activities.get(0).getBody().contains(space.getName()));
    assertTrue(activities.get(0).getBody().contains("root"));
  }
  
}
