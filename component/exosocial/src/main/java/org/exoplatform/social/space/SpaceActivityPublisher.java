package org.exoplatform.social.space;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.lifecycle.SpaceListenerPlugin;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;

/**
 * This listener is responsible for initializing and notifying activity stream
 * for the space. We create a special opensocial user (with a group provider)
 * ready to receive new activities.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class SpaceActivityPublisher  extends SpaceListenerPlugin {

  private static Log      LOG = ExoLogger.getExoLogger(SpaceActivityPublisher.class);

  private ActivityManager activityManager;

  private IdentityManager identityManager;


  public SpaceActivityPublisher(InitParams params,
                                ActivityManager activityManager,
                                IdentityManager identityManager) throws Exception {
    this.activityManager = activityManager;
    this.identityManager = identityManager;

  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String spaceId = space.getId();
    try {
      // this should create the identity for the space
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceId, false);
       String creator = event.getTarget();
      activityManager.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, space.getName(),  space.getName() + " was created by @" + creator + " .");
      LOG.info("space " + space.getName() + " was added for group " + space.getGroupId());
    } catch (Exception e) {
      LOG.error("Failed to initialize space activity stream ", e);
    }
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    LOG.info("space " + event.getSpace().getName() + " was removed!");
  }

  public void applicationActivated(SpaceLifeCycleEvent event) {

    LOG.info("application <b>" + event.getTarget() + "</b> was activated in space "
        + event.getSpace().getName());

  }

  public void applicationAdded(SpaceLifeCycleEvent event) {
    LOG.info("application <b>" + event.getTarget() + "</b> was added in space "
        + event.getSpace().getName());

  }

  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    LOG.info("application " + event.getTarget() + " was deactivated in space "
        + event.getSpace().getName());

  }

  public void applicationRemoved(SpaceLifeCycleEvent event) {
    LOG.info("application " + event.getTarget() + " was removed in space "
        + event.getSpace().getName());
  }

  public void grantedLead(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    try {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
      String member = event.getTarget();
      activityManager.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, space.getName(), "@" + member
          + " was granted lead.");
    } catch (Exception e) {
      LOG.error("Failed to grant lead ", e);
    }   
    LOG.info("user " + event.getTarget() + " was granted lead of space "
        + space.getName());
  }

  public void joined(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    try {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
      String member = event.getTarget();
      activityManager.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, space.getName(),"@" + member
          + " has joined.");
    } catch (Exception e) {
      LOG.error("Failed to log join activity ", e);
    }

    LOG.info("user " + event.getTarget() + " joined space " + event.getSpace().getName());
  }

  public void left(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    try {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
      String member = event.getTarget();
      activityManager.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, space.getName(), "@" + member
          + " has left the space.");
    } catch (Exception e) {
      LOG.error("Failed to log leave activity ", e);
    }

    LOG.info("user " + event.getTarget() + " has left of space " + event.getSpace().getName());
  }

  public void revokedLead(SpaceLifeCycleEvent event) {

    LOG.info("user " + event.getTarget() + " was revoked lead privileges of space "
        + event.getSpace().getName());
  }

}
