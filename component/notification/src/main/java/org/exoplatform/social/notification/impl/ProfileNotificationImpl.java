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
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.notification.plugin.NewUserPlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

public class ProfileNotificationImpl extends ProfileListenerPlugin {
  
  private static final Log LOG = ExoLogger.getLogger(ProfileNotificationImpl.class);

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
  }

  @Override
  public void basicInfoUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void headerSectionUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void createProfile(ProfileLifeCycleEvent event) {
    Profile profile = event.getProfile();
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.PROFILE, profile);
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(NewUserPlugin.ID))).execute(ctx);
  }

}
