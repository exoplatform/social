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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.api.notification.ArgumentLiteral;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.task.AbstractNotificationTask;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.notification.Utils;

public abstract class ProfileTask extends AbstractNotificationTask<NotificationContext> {
  
  public final static ArgumentLiteral<Profile> PROFILE = new ArgumentLiteral<Profile>(Profile.class, "profile");
  public final static ArgumentLiteral<String> REMOTE_ID = new ArgumentLiteral<String>(String.class, "remoteId");

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
  
  /**
   * 
   */
  public static ProfileTask NEW_USER_JOIN_SOCIAL_INTRANET = new ProfileTask() {
    private final String PROVIDER_TYPE = "NewUserJoinSocialIntranet";

    @Override
    public String getId() {
      return PROVIDER_TYPE;
    }
    
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      
      Profile profile = ctx.value(ProfileTask.PROFILE);
      
      try {
        //This type of notification need to get all users of the system, except the new created user
        // TODO : what's the solution with a big data???
        ProfileFilter profileFilter = new ProfileFilter();
        profileFilter.setExcludedIdentityList(Arrays.asList(profile.getIdentity()));
        ListAccess<Identity> list = Utils.getIdentityManager().getIdentitiesByProfileFilter(profile.getIdentity().getProviderId(), profileFilter, false);
        List<String> allUsers = new ArrayList<String>();
        
        for (Identity identity : list.load(0, list.getSize())) {
          allUsers.add(identity.getRemoteId());
        }
        
        message.key(PROVIDER_TYPE)
               .with("remoteId", profile.getIdentity().getRemoteId())
               .to(allUsers);
      } catch (Exception e) {
        return null;
      }
      return message;
    }

    @Override
    public boolean isValid(NotificationContext ctx) {
      return true;
    }

  };

}
