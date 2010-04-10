package org.exoplatform.social.benches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.space.SpaceService;

/**
 * Injects variable amounts of social key data.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class DataInjector {

  private static Log LOG = ExoLogger.getLogger(DataInjector.class);
  
  private ActivityManager activityManager;
  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;
  private OrganizationService orgnizationservice;
  private UserHandler userHandler;
  private AtomicLong userCount;

  public DataInjector(ActivityManager activityManager, IdentityManager identityManager, RelationshipManager relationshipManager, SpaceService spaceService, OrganizationService organizationService) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.relationshipManager = relationshipManager;
    this.spaceService = spaceService;
    this.orgnizationservice = organizationService;
    userHandler = orgnizationservice.getUserHandler();
    userCount = new AtomicLong(0);
  }
  
  
  /**
   * Generate a variable amount of people
   * @param count
   * @return identities created
   */
  public Collection<Identity> generatePeople(long count) {
    Collection<Identity> identities = new ArrayList<Identity>();
    for (int i = 0; i< count ; i++) {
      Identity identity = generateUser();
      if (identity != null) {
        identities.add(identity);
      }
    }
    return identities;
  }


  /**
   * Generate a new user
   * @return
   */
  private Identity generateUser() {
    User user = generateOrgUser();
    Identity identity = generateSocialIdentity(user);
    return identity;    
  }


  /**
   * Generate or get an identity for the given user
   * @param user
   * @return
   */
  private Identity generateSocialIdentity(User user) {
    Identity identity = null;
    if (user!=null) {
      String username = null;
      try {
        username =  user.getUserName();
      identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username);
      }
      catch (Exception e) {
        LOG.error("Failed to generate social identity for " + username + ": " + e.getMessage());
      }
    }
    return identity;
  }


  /**
   * generate a new or user with name as bench.userXXX where XXX is an internal counter.
   * The method checks if the user exists and will attempt to find a new name by incrementing the counter
   * @return
   */
  private User generateOrgUser() {
    User user = null;
    boolean avail = false;
    while (!avail) {
      long idx = userCount.getAndIncrement();
      String username = "bench.user"+(idx);

      LOG.info("creating org user : " + username);
      try {

      user = userHandler.findUserByName(username);
      } catch (Exception e) {
        LOG.warn("failed to check existence of  "+ username +": " + e.getMessage() );
      }
      if (user != null) {
        LOG.info(username + " already exists, skipping");
      } else {
        try {
        avail = true;
        user = userHandler.createUserInstance(username);
        user.setEmail(username + "@exoplatform.int");
        user.setFirstName(username);
        user.setLastName(username);
        user.setPassword("exo");
        userHandler.createUser(user, true);
        }catch (Exception e) {
          LOG.warn("failed to create user " + username + ": " + e.getMessage());
          return null;
        }
      }
    }
    return user;
  }
  
}
