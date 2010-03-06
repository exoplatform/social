package org.exoplatform.social.space.spi;

import org.exoplatform.social.space.Space;


/**
 * An event fired at different stages of the lifecycle of a space.
 * @see {@link SpaceLifeCycleListener}
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class SpaceLifeCycleEvent {


  public enum Type {ADDED,REMOVED};
  private Type type;
  private Space space;
  
  
  public SpaceLifeCycleEvent(Space space, Type eventType) {
    this.space = space;
    this.type = eventType;
  }


  public Type getType() {
    return type;
  }


  public Space getSpace() {
    return space;
  }
  
  
}
