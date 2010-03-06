package org.exoplatform.social.space.spi;

/**
 * A listener to follow the liecycle of a space.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface SpaceLifeCycleListener {
  
  
  /**
   * Invoked when the space is added
   * @param event
   */
  void spaceAdded(SpaceLifeCycleEvent event);
  
  /**
   * Invoked when the space is removed
   * @param event
   */
  void spaceRemoved(SpaceLifeCycleEvent event);

}
