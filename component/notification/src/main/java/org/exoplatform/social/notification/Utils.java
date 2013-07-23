/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.api.notification.service.TemplateGenerator;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class Utils {
  
  @SuppressWarnings("unchecked")
  public static <T> T getService(Class<T> clazz) {
    return (T) PortalContainer.getInstance().getComponentInstanceOfType(clazz);
  }
  
  public static NotificationDataStorage getSocialEmailStorage() {
    return getService(NotificationDataStorage.class);
  }

  public static String getUserId(String identityId) {
    return getIdentityManager().getIdentity(identityId, false).getRemoteId();
  }
  
  public static List<String> toListUserIds(String... userIds) {
    List<String> ids = new ArrayList<String>();

    for (String userId : userIds) {
      ids.add(userId);
    }
    
    return ids;
  }
  
  public static boolean isSpaceActivity(ExoSocialActivity activity) {
    Identity id = getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), false);
    return (id != null);
  }
  
  public static List<String> getDestinataires(String[] users, String poster) {
    List<String> destinataires = new ArrayList<String>();
    for (String user : users) {
      user = user.split("@")[0];
      String userName = getUserId(user);
      if (! destinataires.contains(userName) && ! user.equals(poster)) {
        destinataires.add(userName);
      }
    }
    return destinataires;
  }
  
  public static List<String> getDestinataires(ExoSocialActivity activity, Space space) {
    List<String> destinataires = Arrays.asList(space.getMembers());
    destinataires.remove(getUserId(activity.getPosterId()));
    return destinataires;
  }
  
  public static IdentityManager getIdentityManager() {
    return getService(IdentityManager.class);
  }
  
  public static SpaceService getSpaceService() {
    return getService(SpaceService.class);
  }
  
  public static ActivityManager getActivityManager() {
    return getService(ActivityManager.class);
  }
  
  public static TemplateGenerator getTemplateGenerator() {
    return getService(TemplateGenerator.class);
  }
  
}
