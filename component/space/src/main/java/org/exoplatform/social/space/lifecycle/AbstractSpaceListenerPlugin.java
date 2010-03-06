package org.exoplatform.social.space.lifecycle;

import org.exoplatform.social.space.ManagedPlugin;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.space.spi.SpaceLifeCycleListener;

public abstract class AbstractSpaceListenerPlugin extends ManagedPlugin implements SpaceLifeCycleListener {

  public abstract void spaceAdded(SpaceLifeCycleEvent event);

  public abstract void spaceRemoved(SpaceLifeCycleEvent event) ;

}
