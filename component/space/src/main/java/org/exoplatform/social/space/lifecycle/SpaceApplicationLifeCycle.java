package org.exoplatform.social.space.lifecycle;

import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.spi.SpaceApplicationLifeCycleListener;
import org.exoplatform.social.space.spi.SpaceApplicationLifecycleEvent;
import org.exoplatform.social.space.spi.SpaceApplicationLifecycleEvent.Type;

public class SpaceApplicationLifeCycle
                                      extends
                                      AbstractLifeCycle<SpaceApplicationLifeCycleListener, SpaceApplicationLifecycleEvent> {

  public void addApplication(Space space, String appId) {
    SpaceApplicationLifecycleEvent event = new SpaceApplicationLifecycleEvent(space,
                                                                              appId,
                                                                              Type.ADDED);
    broadcast(event);
  }

  public void deactivateApplication(Space space, String appId) {
    SpaceApplicationLifecycleEvent event = new SpaceApplicationLifecycleEvent(space,
                                                                              appId,
                                                                              Type.DEACTIVATED);
    broadcast(event);
  }

  public void activateApplication(Space space, String appId) {
    SpaceApplicationLifecycleEvent event = new SpaceApplicationLifecycleEvent(space,
                                                                              appId,
                                                                              Type.ACTIVATED);
    broadcast(event);
  }

  public void removeApplication(Space space, String appId) {
    SpaceApplicationLifecycleEvent event = new SpaceApplicationLifecycleEvent(space,
                                                                              appId,
                                                                              Type.REMOVED);
    broadcast(event);
  }

  @Override
  protected void dispatchEvent(final SpaceApplicationLifeCycleListener listener,
                               final SpaceApplicationLifecycleEvent event) {
    switch (event.getType()) {
    case ACTIVATED:
      listener.applicationActivated(event);
      break;
    case DEACTIVATED:
      listener.applicationDeactivated(event);
      break;
    case ADDED:
      listener.applicationAdded(event);
      break;
    case REMOVED:
      listener.applicationRemoved(event);
      break;
    default:
      break;
    }
  }
}
