package org.exoplatform.social.people;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.lifecycle.RelationshipListenerPlugin;
import org.exoplatform.social.relationship.spi.RelationshipEvent;

/**
 * Publish a status update in activity streams of 2 confirmed relations.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class RelationshipPublisher extends RelationshipListenerPlugin {

  private static final Log LOG = ExoLogger.getLogger(RelationshipPublisher.class);

  private ActivityManager  activityManager;

  public RelationshipPublisher(InitParams params, ActivityManager activityManager) {
    this.activityManager = activityManager;
  }

  @Override
  public void confirmed(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    String user1 = relationship.getIdentity1().getDisplayName();
    String user2 = relationship.getIdentity2().getDisplayName();

    try {
      activityManager.recordActivity(user1, "people", "I am now in relation with " + user2, "");
      activityManager.recordActivity(user2, "people", "I am now in relation with " + user1, "");
    } catch (Exception e) {
      LOG.warn("Failed to publish event " + event + ": " + e.getMessage());
    }
  }

  @Override
  public void ignored(RelationshipEvent event) {
    ;// void on purpose
  }

  @Override
  public void removed(RelationshipEvent event) {
    ;// void on purpose
  }

}
