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
package org.exoplatform.social.core.listeners;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;

/**
 * Listens to user updating events.
 * 
 * Created by hanh.vi@exoplatform.com
 * 
 * Jan 6, 2011
 * @since 1.1.3
 */
public class SocialUserEventListenerImpl extends UserEventListener {

  private static final Log LOG = ExoLogger.getExoLogger(SocialUserEventListenerImpl.class);
  
  /**
   * Listens to postSave action for updating profile.
   *
   * @param user
   * @param isNew
   * @throws Exception
   */
  public void postSave(User user, boolean isNew) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager idm = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    Identity identity = idm.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user.getUserName(), true);

    if (!isNew) {
      Profile profile = identity.getProfile();
      String uFirstName = user.getFirstName();
      String uLastName = user.getLastName();
      String uEmail = user.getEmail();

      String pFirstName = (String) profile.getProperty(Profile.FIRST_NAME);
      String pLastName = (String) profile.getProperty(Profile.LAST_NAME);
      String pEmail = (String) profile.getProperty(Profile.EMAIL);
      boolean hasUpdated = false;

      if ((pFirstName == null) || (!pFirstName.equals(uFirstName))) {
        profile.setProperty(Profile.FIRST_NAME, uFirstName);
        hasUpdated = true;
      }

      if ((pLastName == null) || (!pLastName.equals(uLastName))) {
        profile.setProperty(Profile.LAST_NAME, uLastName);
        hasUpdated = true;
      }

      if ((pEmail == null) || (!pEmail.equals(uEmail))) {
        profile.setProperty(Profile.EMAIL, uEmail);
        hasUpdated = true;
      }

      if (hasUpdated) {
        idm.saveProfile(profile);
      }
    }
  }

  /**
   * Deletes the associated identity and profile when a user is deleted.
   *
   * @param deletedUser the deleted user
   */
  @Override
  public void postDelete(User deletedUser) {
    IdentityManager identityManager = getIdentityManager();
    Identity id = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, deletedUser.getUserName());
    if (id != null) {
      deleteAllActivities(id);
      deleteAllConnections(id);
      identityManager.deleteIdentity(id);
    }
  }

  /**
   * Delete all activities of the identity that will be deleted.
   *
   * @param deletedIdentity the identity that will be deleted.
   */
  private void deleteAllActivities(Identity deletedIdentity) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    ActivityManager activityManager = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);
    //this could be a big performance problem. ActivityManager#deleteAll(Identity) should be introduced?
    List<Activity> activities = activityManager.getActivities(deletedIdentity,
                                                              0,
                                                              activityManager.getActivitiesCount(deletedIdentity));
    for (Activity activity : activities) {
      activityManager.deleteActivity(activity);
    }
  }

  /**
   * Deletes all connections with the identity that will be deleted.
   *
   * @param deletedIdentity the identity that will be deleted
   */
  private void deleteAllConnections(Identity deletedIdentity) {
    RelationshipManager relationshipManager = getRelationshipManager();
    List<Relationship> allRelationships = new ArrayList<Relationship>();
    try {
      allRelationships = relationshipManager.getAllRelationships(deletedIdentity);
    } catch (Exception e) {
      LOG.warn("Failed to get all relationships", e);
    }
    for (Relationship relationship : allRelationships) {
      try {
        relationshipManager.remove(relationship);
      } catch (Exception e) {
        LOG.warn("Failed to remove relationship: " + relationship, e);
      }
    }

  }

  /**
   * Gets the identity manager.
   *
   * @return the identity manager
   */
  private IdentityManager getIdentityManager() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    return (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
  }

  /**
   * Gets the relationship manager.
   *
   * @return the relationship manager
   */
  private RelationshipManager getRelationshipManager() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    return (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
  }

}
