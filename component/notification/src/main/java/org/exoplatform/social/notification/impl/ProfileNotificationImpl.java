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

import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListener;
import org.exoplatform.social.notification.SocialEmailStorage.CONNECTOR_TYPE;
import org.exoplatform.social.notification.SocialEmailUtils;
import org.exoplatform.social.notification.SocialMessage;
import org.exoplatform.social.notification.context.NotificationContext;
import org.exoplatform.social.notification.context.NotificationExecutor;
import org.exoplatform.social.notification.task.ProfileTask;

public class ProfileNotificationImpl implements ProfileListener {

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    Profile profile = event.getProfile();
    NotificationContext ctx = NotificationContext.makeProfileNofification(profile);
    ProfileTask task = ProfileTask.UPDATE_AVATAR;
    SocialMessage msg = NotificationExecutor.execute(task, ctx);
    SocialEmailUtils.getSocialEmailStorage().addEmailNotification(msg, CONNECTOR_TYPE.PROFILE);
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

}
