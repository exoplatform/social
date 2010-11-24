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
package org.exoplatform.social.core.application;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;


/**
 * Publish updates onto the user's activity stream when his profile is updated.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ProfileUpdatesPublisher extends ProfileListenerPlugin {

  private static final Log LOG = ExoLogger.getLogger(ProfileUpdatesPublisher.class);
  private ActivityManager activityManager;
  private IdentityManager identityManager;

  public ProfileUpdatesPublisher(InitParams params, ActivityManager activityManager, IdentityManager identityManager) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
  }

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    publish(event, "@" + event.getUsername() + " has a new profile picture.");
  }


  @Override
  public void basicInfoUpdated(ProfileLifeCycleEvent event) {
    publish(event, "@" + event.getUsername() + " profile has updated his basic profile info.");
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
   publish(event, "@" + event.getUsername() + " profile has updated his contact info.");

  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
    publish(event, "@" + event.getUsername() + " profile has an updated experience section.");
  }

  @Override
  public void headerSectionUpdated(ProfileLifeCycleEvent event) {
    publish(event, "@" + event.getUsername() + " has updated his header info.");
  }

  private void publish(ProfileLifeCycleEvent event, String message) {
    try {
      //String username = event.getUsername();
      Profile profile = event.getProfile();
      Identity identity = profile.getIdentity();
      reloadIfNeeded(identity);
      activityManager.recordActivity(identity,
                                     PeopleService.PEOPLE_APP_ID,
                                     message);
    } catch (Exception e) {
      LOG.warn("Failed to publish event " + event + ": " + e.getMessage());
    }
  }

  private void reloadIfNeeded(Identity id1) throws Exception {
    if (id1.getId() == null || id1.getProfile().getFullName().length() == 0) {
      id1 = identityManager.getIdentity(id1.getGlobalId().toString(), true);
    }
  }

}
