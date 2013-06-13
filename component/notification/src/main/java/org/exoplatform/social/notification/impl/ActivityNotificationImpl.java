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

import java.util.regex.Pattern;

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListener;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.context.NotificationContext;
import org.exoplatform.social.notification.task.ActivityTask;

public class ActivityNotificationImpl implements ActivityListener {

  private static final Pattern MENTION_PATTERN = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");
  
  @Override
  public void saveActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getSource();    
    NotificationContext ctx = NotificationContext.makeActivityNofification(activity);
    ActivityTask saveTask = ActivityTask.POST_ACTIVITY;
    
    // check if activity contain mentions then create mention task
    ActivityTask mentionTask;
    if (hasContainMentions(activity)) {
      mentionTask = ActivityTask.MENTION_ACTIVITY;
    }
    
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    // TODO Auto-generated method stub
    
  }
  
  private boolean hasContainMentions(ExoSocialActivity activity) {
    return MENTION_PATTERN.matcher(activity.getTitle()).find();
  }
}
