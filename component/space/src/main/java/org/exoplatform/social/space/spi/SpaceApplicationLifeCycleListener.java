package org.exoplatform.social.space.spi;

/**
 * An Application listener can follow the lifecycle of an application within a space.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface SpaceApplicationLifeCycleListener {
  
  /**
   * Invoked when an application is added to a space
   * @param event
   */
  void applicationAdded(SpaceApplicationLifecycleEvent event);
  

  /**
   * Invoked when an application is removed from a space
   * @param event
   */
  void applicationRemoved(SpaceApplicationLifecycleEvent event);
  
  
  
  /**
   * Invoked when an application is activated
   * @param event
   */
  void applicationActivated(SpaceApplicationLifecycleEvent event);
  
  
  /**
   * Invoked when an application is deactivated from the space
   * @param event
   */
  void applicationDeactivated(SpaceApplicationLifecycleEvent event);
  
}
