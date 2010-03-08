package org.exoplatform.social.space.lifecycle;

import org.exoplatform.social.space.ManagedPlugin;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.space.spi.SpaceLifeCycleListener;

/**
 * Base class for a manageable space listener plugin 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class SpaceListenerPlugin extends ManagedPlugin implements
    SpaceLifeCycleListener {

  public abstract void spaceCreated(SpaceLifeCycleEvent event);

  public abstract void spaceRemoved(SpaceLifeCycleEvent event);

  public abstract void applicationActivated(SpaceLifeCycleEvent event);

  public abstract void applicationAdded(SpaceLifeCycleEvent event);

  public abstract void applicationDeactivated(SpaceLifeCycleEvent event);

  public abstract void applicationRemoved(SpaceLifeCycleEvent event);

  public abstract void grantedLead(SpaceLifeCycleEvent event);

  public abstract void joined(SpaceLifeCycleEvent event);

  public abstract void left(SpaceLifeCycleEvent event);

  public abstract void revokedLead(SpaceLifeCycleEvent event);

}
