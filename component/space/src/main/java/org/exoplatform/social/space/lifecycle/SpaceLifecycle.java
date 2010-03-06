package org.exoplatform.social.space.lifecycle;

import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent.Type;

public class SpaceLifecycle extends AbstractLifeCycle<SpaceLifeCycleListener, SpaceLifeCycleEvent> {

  @Override
  protected void dispatchEvent(final SpaceLifeCycleListener listener,
                               final SpaceLifeCycleEvent event) {

    switch (event.getType()) {
    case ADDED:
      listener.spaceAdded(event);
      break;
    case REMOVED:
      listener.spaceRemoved(event);
      break;
    default:
      break;
    }
  }

  public void spaceCreated(Space space) {
    broadcast(new SpaceLifeCycleEvent(space, Type.ADDED));
  }

  public void spaceRemoved(Space space) {
    broadcast(new SpaceLifeCycleEvent(space, Type.REMOVED));
  }

}
