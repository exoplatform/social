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
package org.exoplatform.social.notification.task;

import org.exoplatform.social.notification.SocialMessage;
import org.exoplatform.social.notification.context.NotificationContext;

public abstract class ActivityTask implements NotificationTask<NotificationContext>{

  @Override
  public void start(NotificationContext ctx) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void end(NotificationContext ctx) {
    // TODO Auto-generated method stub
    
  }
  
  /**
   * Someone @mentions the user in an activity.
   */
  public static ActivityTask MENTION_ACTIVITY = new ActivityTask() {

    @Override
    public SocialMessage execute(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }
    
  };

  /**
   * Someone comments on an activity posted by the user.
   */
  public static ActivityTask COMMENT_ACTIVITY = new ActivityTask() {

    @Override
    public SocialMessage execute(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }
    
  };
  
  /**
   * Someone posts an activity on the User's stream.
   */
  public static ActivityTask POST_ACTIVITY = new ActivityTask() {

    @Override
    public SocialMessage execute(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }
    
  };

  /**
   * Someone posts an activity on a space where the user is a member.
   */
  public static ActivityTask POST_ACTIVITY_ON_SPACE = new ActivityTask() {

    @Override
    public SocialMessage execute(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }
    
  };
}
