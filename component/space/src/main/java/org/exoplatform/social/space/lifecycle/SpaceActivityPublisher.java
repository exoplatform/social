package org.exoplatform.social.space.lifecycle;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;

/**
 * This listener is responsible for initializing and notifying activity stream for the space.
 * We create a special opensocial user (with a group provider) ready to receive new activities.
 * TODO : implement actually :-)
 * TODO : should be moved to an integration component to avoid pulling dependency
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class SpaceActivityPublisher extends SpaceListenerPlugin {

  private static Log LOG = ExoLogger.getExoLogger(SpaceActivityPublisher.class);
  
  public SpaceActivityPublisher(InitParams params) throws Exception {
    super();
  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String group = space.getGroupId();
    LOG.debug("space " + space.getName() + "was added for group " + group);
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    LOG.debug("space " + event.getSpace().getName() + "was removed!");
  }
  
  public void applicationActivated(SpaceLifeCycleEvent event) {

    LOG.debug("application "+ event.getTarget() +  " was activated in space " + event.getSpace().getName());
    
  }

  public void applicationAdded(SpaceLifeCycleEvent event) {
    LOG.debug("application "+ event.getTarget() +  " was added in space " + event.getSpace().getName());
    
  }

  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    LOG.debug("application "+ event.getTarget() +  " was deactivated in space " + event.getSpace().getName());
    
  }

  public void applicationRemoved(SpaceLifeCycleEvent event) {
    LOG.debug("application "+ event.getTarget() +  " was removed in space " + event.getSpace().getName());
  }

  public void grantedLead(SpaceLifeCycleEvent event) {
    LOG.debug("user "+ event.getTarget() +  " was granted lead of space " + event.getSpace().getName());
  }

  public void joined(SpaceLifeCycleEvent event) {
    LOG.debug("user "+ event.getTarget() +  " joined space " + event.getSpace().getName());
  }

  public void left(SpaceLifeCycleEvent event) {
    LOG.debug("user "+ event.getTarget() +  " has left of space " + event.getSpace().getName());
  }

  public void revokedLead(SpaceLifeCycleEvent event) {
    LOG.debug("user "+ event.getTarget() +  " was revoked lead privileges of space " + event.getSpace().getName());
  }

}
