package org.exoplatform.social.space;

import java.util.List;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceActivityPublisher;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.impl.JCRStorage;
import org.exoplatform.social.space.impl.SpaceIdentityProvider;
import org.exoplatform.social.space.impl.SpaceServiceImpl;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.exosocial.configuration.xml")
})
public class TestSpaceActivityPublisher extends  AbstractKernelTest {

  public TestSpaceActivityPublisher() {
    super();
  }

  public void testSpaceCreation() throws Exception {

    PortalContainer container = super.getContainer();
    
    IdentityManager identityManager = (IdentityManager)container.getComponentInstanceOfType(IdentityManager.class);
    ActivityManager activityManager = (ActivityManager)container.getComponentInstanceOfType(ActivityManager.class);
    SpaceServiceImpl spaceService = (SpaceServiceImpl)container.getComponentInstanceOfType(SpaceService.class);
    
    assertNotNull(activityManager);
    
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
    assertEquals(activities.size(), 1);
    assertTrue(activities.get(0).getTitle().contains(space.getName()));
    assertTrue(activities.get(0).getTitle().contains("root"));
  }
  
}
