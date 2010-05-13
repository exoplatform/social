package org.exoplatform.social.space.lifecycle;

import org.exoplatform.social.lifecycle.AbstractLifeCycle;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent.Type;

/**
 * Implementation of the lifecycle of spaces. <br/>
 * Events are dispatched asynchronously but sequentially to their listeners
 * according to their type.<br/>
 * Listeners may fail, this is safe for the lifecycle, subsequent listeners will
 * still be called.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class SpaceLifecycle extends AbstractLifeCycle<SpaceLifeCycleListener, SpaceLifeCycleEvent> {

  @Override
  protected void dispatchEvent(SpaceLifeCycleListener listener, SpaceLifeCycleEvent event) {
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

  public void spaceCreated(Space space, String creator) {
    broadcast(new SpaceLifeCycleEvent(space, creator, Type.SPACE_CREATED));
  }

  public void spaceRemoved(Space space, String remover) {
    broadcast(new SpaceLifeCycleEvent(space, remover, Type.SPACE_REMOVED));
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

  public void memberJoined(Space space, String userId) {
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
