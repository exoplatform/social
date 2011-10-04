/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
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
public class ExoSocialDataInjectionExecutor {

  private static Log          LOG = ExoLogger.getLogger(ExoSocialDataInjectionExecutor.class);
  
  private LoremIpsum4J lorem = new LoremIpsum4J();

  private ActivityManager     activityManager;

  private IdentityManager     identityManager;

  private RelationshipManager relationshipManager;

  private SpaceService        spaceService;

  private OrganizationService orgnizationservice;

  private UserHandler         userHandler;

  private AtomicInteger       userCount;
  private AtomicInteger       relationshipCount;
  private AtomicInteger       activityUserCount;
  private AtomicInteger       spaceCount;

  private NameGenerator nameGenerator;

  public ExoSocialDataInjectionExecutor(ActivityManager activityManager,
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
    activityUserCount = new AtomicInteger(0);
    spaceCount = new AtomicInteger(0);
    nameGenerator = new NameGenerator();
  }

  /**
   * Generates a variable amount of people
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

  /**
   * Generates a variable amount of Relations
   * 
   * @param count
   * @return
   */
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


  /**
   * Generates a variable amount of activity
   * @param count
   * @return
   */
  public Collection<ExoSocialActivity> generateActivities(long count) {

    Collection<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (int i = 0; i < count; i++) {
      ExoSocialActivity activity = generateActivity();
      if (activity != null) {
        activities.add(activity);
      }
    }
    return activities;
  }
  
  /**
   * Generates a variable amount of activity
   * @param count
   * @return
   */
  public Collection<ExoSocialActivity> generateActivities(Identity identity, long count) {

    Collection<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    for (int i = 0; i < count; i++) {
      ExoSocialActivity activity = generateActivity(identity);
      if (activity != null) {
        activities.add(activity);
      }
    }
    return activities;
  }

  /**
   * Generates activity for User
   * @param user
   * @param log count
   * @return
   */
  public Collection<ExoSocialActivity> generateActivities(String user, long count) {
    Collection<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    try {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user);
    for (int i = 0; i < count; i++) {
      ExoSocialActivity activity = generateActivity(identity);
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


  /**
   * Generates activity for Identity
   * 
   * @param id1
   * @return
   */
  private ExoSocialActivity generateActivity(Identity id1) {
    ExoSocialActivity activity = null;
    if (id1 != null) {
      try {
        int idx = activityUserCount.getAndIncrement();
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

  private ExoSocialActivity generateActivity() {
    Identity id1 = selectRandomUser(null);
    return generateActivity(id1);
  }
  
  private ExoSocialActivity generateRandomUserActivity() {
    Identity id1 = selectRandomUser(null);
    return generateActivity(id1);
  }

  /**
   * Gets the random value for Activity
   * @return
   */
  private ExoSocialActivity generateRandomActivity() {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    LoremIpsum4J lorem = new LoremIpsum4J();
    activity.setBody(lorem.getWords(10));
    activity.setTitle(lorem.getParagraphs());
    return activity;
  }

  /**
   * Creats Relationship data
   * @return
   */
  private Relationship generateRelationship() {
    Identity[] pple = selectUnrelatedUsers();
    Relationship relationship = null;
    if (pple != null) {

      try {
        int idx = relationshipCount.getAndIncrement();
        relationship = relationshipManager.inviteToConnect(pple[0], pple[1]);
        relationshipManager.confirm(pple[0], pple[1]);
      } catch (Exception e) {
        LOG.error("failed to create connection between " + pple[0] + " and " + pple[1] + ": " + e.getMessage());
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
        identity = identityManager.getIdentity(OrganizationIdentityProvider.NAME, username, false);
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
  
  /**
   * Generates a variable amount of Spaces
   * 
   * @param count
   * @return
   */
  public Map<Identity, Set<Space>> generateSpacesForRandomIdentity(Collection<Identity> identities, int count) {
    //ExoContainer pc = ExoContainerContext.getContainerByName(portalName);
    ExoContainer pc = ExoContainerContext.getCurrentContainer();
    RequestLifeCycle.begin(pc);
    
    Map<Identity, Set<Space>> identitySpacesMap = new HashMap<Identity, Set<Space>>();
    Set<Space> spaces = new HashSet<Space>(count);
    Identity identity = null;
    for (int i = 0; i < count; i++) {
      identity = selectRandomUser(null);
      Space space = generateSpace(identity.getRemoteId());
      LOG.info("creating space : " + space.getDisplayName() + " for: " + identity.getRemoteId());
      if (space != null) {
        spaces.add(space);
      }

      Set<Space> oldSpaces = identitySpacesMap.get(identity);
      //adds more space which belongs to the identity(key for hashmap)
      if (oldSpaces != null) {
        oldSpaces.addAll(spaces);
        identitySpacesMap.put(identity, oldSpaces);
      } else {
        identitySpacesMap.put(identity, spaces);
      }
      
      
      spaces = new HashSet<Space>(count);
    }
    
    RequestLifeCycle.end();
    return identitySpacesMap;
  }
  
  /**
   * Generates a variable amount of Spaces
   * 
   * @param count
   * @return
   */
  public Map<Space, Identity> generateSpaces(Collection<Identity> identities, long count) {
    //ExoContainer pc = ExoContainerContext.getContainerByName(portalName);
    ExoContainer pc = ExoContainerContext.getCurrentContainer();
    RequestLifeCycle.begin(pc);
    
    Map<Space, Identity> spaceIdentityMap = new HashMap<Space, Identity>();
    for (Identity identity : identities) {
      for (int i = 0; i < count; i++) {
        Space space = generateSpace(identity.getRemoteId());
        LOG.info("creating space : " + space.getDisplayName() + " for: " + identity.getRemoteId());
        if (space != null) {
          spaceIdentityMap.put(space, identity);
        }        
      }
      
    }
    
    RequestLifeCycle.end();
    return spaceIdentityMap;
  }
  
  /**
   * Generate the activity for Space.
   * @param spaceIdentityMap
   * @param count
   */
  public void generateActivitySpace(Space space, Identity identity, long count) {
    for (int i = 0; i < count; i++) {
      String activityMessage = lorem.getWords(10);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   space.getPrettyName(),
                                                                   false);
      activityManager.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, activityMessage);
      LOG.info("creating activity of  : " + identity.getRemoteId() + " with space: " + space.getDisplayName());
    }
  }
  
  /**
   * Generate a new or Space with name as bench.spaceXXX where XXX is an internal
   * counter. The method checks if the space exists and will attempt to find a
   * new name by incrementing the counter.
   * @return
   */
  public Space generateSpace(String username) {
    Space space = null;
    boolean avail = false;
    while (!avail) {
      int idx = spaceCount.getAndIncrement();
      String spacename = spacename(idx);

      LOG.info("creating space : " + spacename);
      try {

        space = spaceService.getSpaceByDisplayName(spacename);
      } catch (Exception e) {
        LOG.warn("failed to check existence of  " + spacename + ": " + e.getMessage());
      }
      if (space != null) {
        LOG.info(spacename + " already exists, skipping");
      } else {
        try {
          avail = true;
          space = new Space();
          space.setGroupId("organization");
          space.setDisplayName(spacename);
          space.setRegistration(Space.OPEN);
          LoremIpsum4J lorem = new LoremIpsum4J();
          space.setDescription(lorem.getWords(10));
          space.setType(DefaultSpaceApplicationHandler.NAME);
          space.setVisibility(Space.PUBLIC);
          space.setPriority(Space.INTERMEDIATE_PRIORITY);
          space = spaceService.createSpace(space, username, null);
        } catch (Exception e) {
          LOG.warn("failed to create space " + spacename + ": " + e.getMessage());
          return null;
        }
      }
    }
    return space;
  }

  /**
   * Builds the User information
   * @param user
   * @param username
   */
  void initRandomUser(User user, String username) {
    user.setEmail(username + "@exoplatform.int");
    user.setFirstName(nameGenerator.compose(3));
    user.setLastName(nameGenerator.compose(4));
    user.setPassword("exo");
  }

  private String username(int idx) {
    return "bench.user" + (idx);
  }
  
  private String spacename(int idx) {
    return "bench.space" + (idx);
  }

  public AtomicInteger getUserCount() {
    return userCount;
  }

  public AtomicInteger getRelationshipCount() {
    return relationshipCount;
  }

  public AtomicInteger getActivityCount() {
    return activityUserCount;
  }

  public AtomicInteger getSpaceCount() {
    return spaceCount;
  }

}