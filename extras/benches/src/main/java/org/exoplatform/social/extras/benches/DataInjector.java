/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.extras.benches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.GlobalId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.extras.benches.util.LoremIpsum4J;
import org.exoplatform.social.extras.benches.util.NameGenerator;
/**
 * Injects variable amounts of social key data.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class DataInjector {

  private static Log          LOG = ExoLogger.getLogger(DataInjector.class);

  private ActivityManager     activityManager;

  private IdentityManager     identityManager;

  private RelationshipManager relationshipManager;

  private SpaceService        spaceService;

  private OrganizationService orgnizationservice;

  private UserHandler         userHandler;

  private AtomicInteger       userCount;
  private AtomicInteger       relationshipCount;
  private AtomicInteger       activityCount;
  private AtomicInteger       spaceCount;

  private NameGenerator nameGenerator;

  public DataInjector(ActivityManager activityManager,
                      IdentityManager identityManager,
                      RelationshipManager relationshipManager,
                      SpaceService spaceService,
                      OrganizationService organizationService) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.relationshipManager = relationshipManager;
    this.spaceService = spaceService;
    this.orgnizationservice = organizationService;
    userHandler = orgnizationservice.getUserHandler();
    userCount = new AtomicInteger(0);
    relationshipCount = new AtomicInteger(0);
    activityCount = new AtomicInteger(0);
    spaceCount = new AtomicInteger(0);
    nameGenerator = new NameGenerator();
  }

  /**
   * Generate a variable amount of people
   *
   * @param count
   * @return identities created
   */
  public Collection<Identity> generatePeople(long count) {
    Collection<Identity> identities = new ArrayList<Identity>();
    for (int i = 0; i < count; i++) {
      Identity identity = generateUser();
      if (identity != null) {
        identities.add(identity);
      }
    }
    return identities;
  }

  public Collection<Relationship> generateRelations(long count) {
    Collection<Relationship> relationships = new ArrayList<Relationship>();
    for (int i = 0; i < count; i++) {
      Relationship relationship = generateRelationship();
      if (relationship != null) {
        relationships.add(relationship);
      }
    }
    return relationships;
  }


  public Collection<Activity> generateActivities(long count) {
    Collection<Activity> activities = new ArrayList<Activity>();
    for (int i = 0; i < count; i++) {
      Activity activity = generateActivity();
      if (activity != null) {
        activities.add(activity);
      }
    }
    return activities;
  }

  public Collection<Activity> generateActivities(String user, long count) {
    Collection<Activity> activities = new ArrayList<Activity>();
    try {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user);
    for (int i = 0; i < count; i++) {
      Activity activity = generateActivity(identity);
      if (activity != null) {
        activities.add(activity);
      }
    }
    }
    catch (Exception e) {
      LOG.warn("Failed to generate activities for " + user, e);
    }
    return activities;
  }


  private Activity generateActivity(Identity id1) {
    Activity activity = null;
    if (id1 != null) {
      try {
        int idx = activityCount.getAndIncrement();
        activity = generateRandomActivity();
        activity.setExternalId("benches:"+ idx);
        activityManager.saveActivity(id1, activity);
        LOG.info("created activity " + activity.getExternalId());
      } catch (Exception e) {
        LOG.error("failed to save activity for " + id1 + ": "  + e.getMessage());
      }

    }
    return activity;
  }

  private Activity generateActivity() {
    Identity id1 = selectRandomUser(null);
    return generateActivity(id1);
  }

  private Activity generateRandomActivity() {
    Activity activity = new Activity();
    LoremIpsum4J lorem = new LoremIpsum4J();
    activity.setBody(lorem.getWords(10));
    activity.setTitle(lorem.getParagraphs());
    return activity;
  }

  private Relationship generateRelationship() {
    Identity[] pple = selectUnrelatedUsers();
    Relationship relationship = null;
    if (pple != null) {

      try {
        int idx = relationshipCount.getAndIncrement();
        relationship = relationshipManager.create(pple[0], pple[1]);
        relationshipManager.confirm(relationship);
      } catch (Exception e) {
        LOG.error("failed to create connection between " + pple[0] + " and " + pple[1] + ": "
            + e.getMessage());
      }
      LOG.info("created connection " + relationship + ".");
    }
    return relationship;
  }

  private Identity[] selectUnrelatedUsers() {

    Identity id1 = selectRandomUser(null);
    Identity id2 = selectRandomUser(id1);

    if (id1 != null && id2 != null) {
      return new Identity[] { id1, id2 };
    } else {
      return null;
    }

  }

  private Identity selectRandomUser(Identity except) {

    Identity identity = null;
    int limit = 10;
    String username = null;
    while (identity == null) {
      try {
        username = username(getRandomUserIndex());
        String id = GlobalId.create(OrganizationIdentityProvider.NAME, username).toString();
        identity = identityManager.getIdentity(id);
        if (except != null && except.getId().equals(identity.getId())) {
          identity = null; // continue
        }
      } catch (Exception e) {
        LOG.warn("failed to get identity for " + username + ": " + e.getMessage());
      }
      if (--limit == 0) {
        LOG.warn("failed to select a random user after 10 attempts. Make sure enough identities are populated.");
        break;
      }
    }
    return identity;
  }

  private int getRandomUserIndex() {
    Random rnd = new Random();
    return rnd.nextInt(userCount.get());
  }

  /**
   * Generate a new user
   *
   * @return
   */
  private Identity generateUser() {
    User user = generateOrgUser();
    Identity identity = generateSocialIdentity(user);
    return identity;
  }

  /**
   * Generate or get an identity for the given user
   *
   * @param user
   * @return
   */
  private Identity generateSocialIdentity(User user) {
    Identity identity = null;
    if (user != null) {
      String username = null;
      try {
        username = user.getUserName();
        identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username);
      } catch (Exception e) {
        LOG.error("Failed to generate social identity for " + username + ": " + e.getMessage());
      }
    }
    return identity;
  }

  /**
   * generate a new or user with name as bench.userXXX where XXX is an internal
   * counter. The method checks if the user exists and will attempt to find a
   * new name by incrementing the counter
   *
   * @return
   */
  private User generateOrgUser() {
    User user = null;
    boolean avail = false;
    while (!avail) {
      int idx = userCount.getAndIncrement();
      String username = username(idx);

      LOG.info("creating org user : " + username);
      try {

        user = userHandler.findUserByName(username);
      } catch (Exception e) {
        LOG.warn("failed to check existence of  " + username + ": " + e.getMessage());
      }
      if (user != null) {
        LOG.info(username + " already exists, skipping");
      } else {
        try {
          avail = true;
          user = userHandler.createUserInstance(username);
          initRandomUser(user, username);
          userHandler.createUser(user, true);
        } catch (Exception e) {
          LOG.warn("failed to create user " + username + ": " + e.getMessage());
          return null;
        }
      }
    }
    return user;
  }

  void initRandomUser(User user, String username) {
    user.setEmail(username + "@exoplatform.int");
    user.setFirstName(nameGenerator.compose(3));
    user.setLastName(nameGenerator.compose(4));
    user.setPassword("exo");
  }

  private String username(int idx) {
    return "bench.user" + (idx);
  }

  public AtomicInteger getUserCount() {
    return userCount;
  }

  public AtomicInteger getRelationshipCount() {
    return relationshipCount;
  }

  public AtomicInteger getActivityCount() {
    return activityCount;
  }

  public AtomicInteger getSpaceCount() {
    return spaceCount;
  }

}
