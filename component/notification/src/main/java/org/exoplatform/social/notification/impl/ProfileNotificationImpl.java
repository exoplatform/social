/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.impl;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.plugin.NotificationKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.notification.SocialMessageBuilder;
import org.exoplatform.social.notification.plugin.NewUserPlugin;

public class ProfileNotificationImpl extends ProfileListenerPlugin {
  
  private static final Log LOG = ExoLogger.getLogger(ProfileNotificationImpl.class);

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
  }

  @Override
  public void basicInfoUpdated(ProfileLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void headerSectionUpdated(ProfileLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void createProfile(ProfileLifeCycleEvent event) {
    try {
      Profile profile = event.getProfile();
      NotificationContext ctx = NotificationContextImpl.DEFAULT.append(SocialMessageBuilder.PROFILE, profile);
      ctx.getNotificationExecutor().with(ctx.makeCommand(NotificationKey.key(NewUserPlugin.ID)))
                                   .execute(ctx);
    } catch (Exception e) {
      LOG.warn("Failed to get invite to connect information of " + event + ": " + e.getMessage());
    }
  }

}
