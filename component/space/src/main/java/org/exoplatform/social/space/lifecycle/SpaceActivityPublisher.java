package org.exoplatform.social.space.lifecycle;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;

/**
 * This listener is responsible for initializing the activity stream for the space.
 * We create a special opensocial user (with a group provider) ready to receive new activities.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class SpaceActivityPublisher extends AbstractSpaceListenerPlugin {

  public SpaceActivityPublisher(InitParams params) throws Exception {
    super();
  }

  @Override
  public void spaceAdded(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String group = space.getGroupId();
    System.out.println("space " + space.getName() + "was added for group " + group);
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    System.out.println("space " + event.getSpace().getName() + "was removed!");
  }

}
