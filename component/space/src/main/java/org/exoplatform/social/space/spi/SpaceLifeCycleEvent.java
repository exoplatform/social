package org.exoplatform.social.space.spi;

import org.exoplatform.social.space.Space;

/**
 * An event fired at different stages of the lifecycle of a space.
 * 
 * @see {@link SpaceLifeCycleListener}
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class SpaceLifeCycleEvent {

  public enum Type {
    SPACE_CREATED, SPACE_REMOVED, APP_ADDED, APP_REMOVED, APP_ACTIVATED, APP_DEACTIVATED, JOINED, LEFT, GRANTED_LEAD, REVOKED_LEAD
  };

  /**
   * Type of event
   */
  protected Type   type;

  /**
   * space where the event occurs
   */
  protected Space  space;

  /**
   * ID of the target of the event. May be an application or user ID
   */
  protected String target;

  public SpaceLifeCycleEvent(Space space, String target, Type eventType) {
    this.space = space;
    this.type = eventType;
    this.target = target;
  }

  public Type getType() {
    return type;
  }

  public Space getSpace() {
    return space;
  }

  public String getTarget() {
    return target;
  }

  public String toString() {
    return target + ":" + type + "@" + space.getName();
  }

}
