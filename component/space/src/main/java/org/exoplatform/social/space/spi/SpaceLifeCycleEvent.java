package org.exoplatform.social.space.spi;

import org.exoplatform.social.lifecycle.LifeCycleEvent;
import org.exoplatform.social.space.Space;

/**
 * An event fired at different stages of the lifecycle of a space.
 * 
 * @see {@link SpaceLifeCycleListener}
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class SpaceLifeCycleEvent  extends LifeCycleEvent<String,Space>{

  public enum Type {
    SPACE_CREATED, SPACE_REMOVED, APP_ADDED, APP_REMOVED, APP_ACTIVATED, APP_DEACTIVATED, JOINED, LEFT, GRANTED_LEAD, REVOKED_LEAD
  };

  /**
   * Type of event
   */
  protected Type   type;


  public SpaceLifeCycleEvent(Space space, String target, Type eventType) {
    super(target, space);
    this.type = eventType;
  }

  public Type getType() {
    return type;
  }

  /**
   * space where the event occurs
   */
  public Space getSpace() {
    return payload;
  }

  /**
   * ID of the target of the event. May be an application or user ID
   */
  public String getTarget() {
    return source;
  }

  public String toString() {
    return source + ":" + type + "@" + payload.getName();
  }

}
