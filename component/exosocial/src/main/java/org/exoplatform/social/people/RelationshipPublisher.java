package org.exoplatform.social.people;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
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

  private IdentityManager identityManager;

  public RelationshipPublisher(InitParams params, ActivityManager activityManager, IdentityManager identityManager) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
  }

  @Override
  public void confirmed(RelationshipEvent event) {
    Relationship relationship = event.getPayload();

    try {
      Identity id1 = relationship.getIdentity1();
      reloadIfNeeded(id1);
      Identity id2 = relationship.getIdentity1();
      reloadIfNeeded(id2);
      String user1 = id1.getProfile().getFullName();
      String user2 = id2.getProfile().getFullName();
      activityManager.recordActivity(id1.getId(), PeopleService.PEOPLE_APP_ID, "New Relation", user1 + " is now following " + user2);
      activityManager.recordActivity(id2.getId(), PeopleService.PEOPLE_APP_ID, "New Relation", user2 + " is now following " + user1);
      
    } catch (Exception e) {
      LOG.warn("Failed to publish event " + event + ": " + e.getMessage());
    }
  }

  private void reloadIfNeeded(Identity id1) throws Exception {
    if (id1.getId() == null || id1.getProfile().getFullName().length() == 0) {
      id1 = identityManager.getIdentity(id1.getGlobalId().toString(), true);
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

  public void denied(RelationshipEvent event) {
    ;// void on purpose
    
  }

  public void requested(RelationshipEvent event) {
    ;// void on purpose 
  }

}
