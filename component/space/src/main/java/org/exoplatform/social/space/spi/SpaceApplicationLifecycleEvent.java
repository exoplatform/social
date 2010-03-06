package org.exoplatform.social.space.spi;

import org.exoplatform.social.space.Space;

/**
 * Event of the lifecycle of an application among a space.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class SpaceApplicationLifecycleEvent {
  
  public enum Type {ADDED,REMOVED,ACTIVATED,DEACTIVATED};

  private Space space;
  private String appId;
  private Type type;
  
  
  public SpaceApplicationLifecycleEvent(Space space, String appId, SpaceApplicationLifecycleEvent.Type eventType) {
    this.space = space;
    this.appId = appId;
    this.type = eventType;
  }
  
  
  public Space getSpace() {
    return space;
  }

  public String getAppId() {
    return appId;
  }
  
  public Type getType() {
    return type;
  }




  public String toString() {
    return type+":"+appId + "@" + space.getName(); 
  }

}
