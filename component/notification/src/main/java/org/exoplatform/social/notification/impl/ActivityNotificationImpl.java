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
import org.exoplatform.commons.api.notification.NotificationDataStorage;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.context.NotificationExecutor;
import org.exoplatform.social.notification.task.ActivityTask;

public class ActivityNotificationImpl extends ActivityListenerPlugin {

  @SuppressWarnings("unchecked")
  @Override
  public void saveActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getSource();    
    NotificationContext ctx = NotificationContextImpl.DEFAULT.append(ActivityTask.ACTIVITY, activity);
    
    // check if activity contain mentions then create mention task
    NotificationDataStorage storage = Utils.getSocialEmailStorage();
    
    Collection<NotificationMessage> messages = NotificationExecutor.execute(ctx, ActivityTask.POST_ACTIVITY, 
                                 ActivityTask.POST_ACTIVITY_ON_SPACE, ActivityTask.MENTION_ACTIVITY);
    
    if (messages.size() != 0) {
      // add all available types
      storage.addAll(messages);
    }
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getSource();    
    NotificationContext ctx = NotificationContextImpl.DEFAULT.append(ActivityTask.ACTIVITY, activity);
    
    NotificationDataStorage storage = Utils.getSocialEmailStorage();
    NotificationMessage message = NotificationExecutor.execute(ctx, ActivityTask.COMMENT_ACTIVITY);
    
    if (message != null) {
      // add all available types and will be ignored if value is null
      storage.add(message);
    }
  }

  @Override
  public void likeActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getSource();    
    NotificationContext ctx = NotificationContextImpl.DEFAULT.append(ActivityTask.ACTIVITY, activity);
    
    // check if activity contain mentions then create mention task
    NotificationDataStorage storage = Utils.getSocialEmailStorage();
    
    // add all available types and will be ignored if value is null
    storage.add(NotificationExecutor.execute(ctx, ActivityTask.LIKE));
    
  }
}
