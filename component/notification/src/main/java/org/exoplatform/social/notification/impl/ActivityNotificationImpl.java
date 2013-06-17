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

import org.exoplatform.commons.api.notification.NotificationDataStorage;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListener;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.SocialEmailUtils;
import org.exoplatform.social.notification.context.NotificationContext;
import org.exoplatform.social.notification.context.NotificationExecutor;
import org.exoplatform.social.notification.task.ActivityTask;

public class ActivityNotificationImpl implements ActivityListener {

  @Override
  public void saveActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getSource();    
    NotificationContext ctx = NotificationContext.makeActivityNofification(activity);
    
    // check if activity contain mentions then create mention task
    NotificationDataStorage storage = SocialEmailUtils.getSocialEmailStorage();
    
    // add all available types and will be ignored if value is null
    storage.addAll(NotificationExecutor.execute(ctx, ActivityTask.POST_ACTIVITY, 
      ActivityTask.POST_ACTIVITY_ON_SPACE, ActivityTask.MENTION_ACTIVITY));
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getSource();    
    NotificationContext ctx = NotificationContext.makeActivityNofification(activity);
    
    // check if activity contain mentions then create mention task
    NotificationDataStorage storage = SocialEmailUtils.getSocialEmailStorage();
    
    // add all available types and will be ignored if value is null
    storage.addAll(NotificationExecutor.execute(ctx, ActivityTask.COMMENT_ACTIVITY));
  }
}
