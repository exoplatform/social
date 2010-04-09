package org.exoplatform.social.people;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
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
      Identity id2 = relationship.getIdentity2();
      reloadIfNeeded(id2);
      String user1 = id1.getProfile().getFullName();
      String user2 = id2.getProfile().getFullName();
      // RELATION_CONFIRMED=<a href="${Requester.ProfileUrl}">${Requester.DisplayName}</a> is now connected to <a href="${Accepter.ProfileUrl}">${Accepter.DisplayName}</a>.
      // RELATION_CONFIRMED=${Requester} is now connected to ${Accepter}</a>.
      Activity activity = new Activity(id1.getId(), PeopleService.PEOPLE_APP_ID, user1, user1 + " is now connected to " + user2);
      activity.setTitleId("RELATION_CONFIRMED");
      Map<String,String> params = new HashMap<String,String>();
      params.put("Requester", user1);
      params.put("Accepter", user2);
      activity.setTemplateParams(params);
      activityManager.saveActivity(activity);
      
      Activity activity2 = new Activity(id2.getId(), PeopleService.PEOPLE_APP_ID, user2, user2 + " is now connected to " + user1);
      activity2.setTitleId("RELATION_CONFIRMED");
      Map<String,String> params2 = new HashMap<String,String>();
      params2.put("Requester", user2);
      params2.put("Accepter", user1);
      activity2.setTemplateParams(params);
      activityManager.saveActivity(activity2);      
      
    } catch (Exception e) {
      LOG.warn("Failed to publish event " + event + ": " + e.getMessage());
    }
  }

  private void reloadIfNeeded(Identity identity) throws Exception {
    if (identity.getId() == null || identity.getProfile().getFullName().length() == 0) {
      identity = identityManager.getIdentity(identity.getGlobalId().toString(), true);
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
