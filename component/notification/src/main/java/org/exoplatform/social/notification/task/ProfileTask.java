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

import org.exoplatform.commons.api.notification.ArgumentLiteral;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.task.AbstractNotificationTask;
import org.exoplatform.social.core.identity.model.Profile;

public abstract class ProfileTask extends AbstractNotificationTask<NotificationContext> {
  
  public final static ArgumentLiteral<Profile> PROFILE = new ArgumentLiteral<Profile>(Profile.class, "profile");
  

  @Override
  public void start(NotificationContext ctx) {
  }

  @Override
  public void end(NotificationContext ctx) {
  }

  /**
   * 
   */
  public static ProfileTask UPDATE_AVATAR = new ProfileTask() {
    private final String PROVIDER_TYPE = "ProfileUpdateAvatarProvider";

    @Override
    public String getId() {
      return PROVIDER_TYPE;
    }
    
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      return null;
    }

    @Override
    public boolean isValid(NotificationContext ctx) {
      return true;
    }

  };

  /**
   * 
   */
  public static ProfileTask UPDATE_DISPLAY_NAME = new ProfileTask() {
    private final String PROVIDER_TYPE = "ProfileUpdateNameProvider";

    @Override
    public String getId() {
      return PROVIDER_TYPE;
    }
    
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      return null;
    }

    @Override
    public boolean isValid(NotificationContext ctx) {
      return true;
    }

  };

}
