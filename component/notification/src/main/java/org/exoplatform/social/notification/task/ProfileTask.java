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

public abstract class ProfileTask implements NotificationTask<NotificationContext>{

  @Override
  public void start(NotificationContext ctx) {
  }

  @Override
  public void end(NotificationContext ctx) {
  }
  
  public static ProfileTask UPDATE_AVATAR = new ProfileTask() {
    @Override
    public SocialMessage execute(NotificationContext ctx) {
//      Profile profile = ctx.getProfile();
      // TODO continue..
      return null;
    }
  };

}
