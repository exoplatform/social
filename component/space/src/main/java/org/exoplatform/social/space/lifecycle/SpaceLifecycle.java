package org.exoplatform.social.space.lifecycle;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent.Type;

/**
 * Implementation of the lifecycle of spaces.
 * Events are dispatched asynchronously but sequentially to their listeners according to their type. 
 * Listeners may fail, this is safe for the lifecycle, subsequent listenrs cill still be called.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class SpaceLifecycle {

  private Set<SpaceLifeCycleListener>                      listeners = new HashSet<SpaceLifeCycleListener>();

  ExecutorService                                executor  = Executors.newSingleThreadExecutor();

  protected ExecutorCompletionService<SpaceLifeCycleEvent> ecs;

  /**
   * {@inheritDoc}
   */
  public void addListener(SpaceLifeCycleListener listener) {
    listeners.add(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void removeListener(SpaceLifeCycleListener listener) {
    listeners.remove(listener);
  }

  /**
   * Broadcasts an event to the registered listeners. The event is broadcasted
   * asynchronously but sequentially.
   * 
   * @see #dispatchEvent(Object, Object)
   * @param event
   */
  protected void broadcast(final SpaceLifeCycleEvent event) {
    if (ecs == null) {
      ecs = new ExecutorCompletionService<SpaceLifeCycleEvent>(executor);
    }
    for (final SpaceLifeCycleListener listener : listeners) {
      ecs.submit(new Callable<SpaceLifeCycleEvent>() {
        public SpaceLifeCycleEvent call() throws Exception {
          dispatchEvent(listener, event);
          return event;
        }
      });
    }
  }

  protected <E extends SpaceLifeCycleEvent> void dispatchEvent(final SpaceLifeCycleListener listener,
                                                               final E event) {

    switch (event.getType()) {
    case SPACE_CREATED:
      listener.spaceCreated(event);
      break;
    case SPACE_REMOVED:
      listener.spaceRemoved(event);
      break;
    case APP_ACTIVATED:
      listener.applicationActivated(event);
      break;
    case APP_DEACTIVATED:
      listener.applicationDeactivated(event);
      break;
    case APP_ADDED:
      listener.applicationAdded(event);
      break;
    case APP_REMOVED:
      listener.applicationRemoved(event);
      break;
    case JOINED:
      listener.joined(event);
      break;
    case LEFT:
      listener.left(event);
      break;
    case GRANTED_LEAD:
      listener.grantedLead(event);
      break;
    case REVOKED_LEAD:
      listener.revokedLead(event);
      break;
    default:
      break;
    }
  }

  public void spaceCreated(Space space) {
    broadcast(new SpaceLifeCycleEvent(space, null, Type.SPACE_CREATED));
  }

  public void spaceRemoved(Space space) {
    broadcast(new SpaceLifeCycleEvent(space, null, Type.SPACE_REMOVED));
  }

  public void addApplication(Space space, String appId) {
    SpaceLifeCycleEvent event = new SpaceLifeCycleEvent(space, appId, Type.APP_ADDED);
    broadcast(event);
  }

  public void deactivateApplication(Space space, String appId) {
    SpaceLifeCycleEvent event = new SpaceLifeCycleEvent(space, appId, Type.APP_DEACTIVATED);
    broadcast(event);
  }

  public void activateApplication(Space space, String appId) {
    SpaceLifeCycleEvent event = new SpaceLifeCycleEvent(space, appId, Type.APP_ACTIVATED);
    broadcast(event);
  }

  public void removeApplication(Space space, String appId) {
    SpaceLifeCycleEvent event = new SpaceLifeCycleEvent(space, appId, Type.APP_REMOVED);
    broadcast(event);
  }

  public void memberJoigned(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.JOINED));
  }

  public void memberLeft(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.LEFT));
  }

  public void grantedLead(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.GRANTED_LEAD));
  }

  public void revokedLead(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.REVOKED_LEAD));
  }

}
