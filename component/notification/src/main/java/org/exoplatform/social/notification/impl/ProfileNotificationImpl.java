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

import java.util.Collection;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.context.NotificationExecutor;
import org.exoplatform.social.notification.task.ActivityTask;
import org.exoplatform.social.notification.task.ProfileTask;

public class ProfileNotificationImpl extends ProfileListenerPlugin {

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    Profile profile = event.getProfile();
    NotificationContext ctx = NotificationContextImpl.DEFAULT.append(ProfileTask.PROFILE, profile);
    NotificationExecutor.execute(ctx, ProfileTask.UPDATE_AVATAR, ProfileTask.UPDATE_DISPLAY_NAME);
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
    Profile profile = event.getProfile();
    NotificationContext ctx = NotificationContextImpl.DEFAULT.append(ProfileTask.PROFILE, profile);
    
    // check if activity contain mentions then create mention task
    NotificationDataStorage storage = Utils.getSocialEmailStorage();
    
    NotificationMessage message = NotificationExecutor.execute(ctx, ProfileTask.NEW_USER_JOIN_SOCIAL_INTRANET);
    
    if (message != null) {
      storage.add(message);
    }
  }

}
