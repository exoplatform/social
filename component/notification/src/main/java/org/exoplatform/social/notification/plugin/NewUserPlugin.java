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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.plugin;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.task.ProfileTask;

public class NewUserPlugin extends AbstractNotificationPlugin {

  public final String ID = "NewUserJoinSocialIntranet";
  
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationMessage makeNotification(NotificationContext ctx) {
    Profile profile = ctx.value(ProfileTask.PROFILE);
    
    try {
      
      //This type of notification need to get all users of the system, except the new created user
      ProfileFilter profileFilter = new ProfileFilter();
      profileFilter.setExcludedIdentityList(Arrays.asList(profile.getIdentity()));
      ListAccess<Identity> list = Utils.getIdentityManager().getIdentitiesByProfileFilter(profile.getIdentity().getProviderId(), profileFilter, false);
      
      List<String> allUsers = new ArrayList<String>();
      
      for (Identity identity : list.load(0, list.getSize())) {
        allUsers.add(identity.getRemoteId());
      }
      
      return NotificationMessage.instance()
             .with("remoteId", profile.getIdentity().getRemoteId())
             .to(allUsers);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    Map<String, String> templateContext = new HashMap<String, String>();
    
    NotificationMessage notification = ctx.getNotificationMessage();
    
    String language = getLanguage(notification);
    
    String remoteId = notification.getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());
    Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
    Profile userProfile = identity.getProfile();
    
    templateContext.put("USER", userProfile.getFullName());
    templateContext.put("PORTAL_NAME", System.getProperty("exo.notifications.portalname", "eXo"));
    String subject = Utils.getTemplateGenerator().processSubjectIntoString(notification.getKey().getId(), templateContext, language);
    
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
    templateContext.put("CONNECT_ACTION_URL", LinkProviderUtils.getInviteToConnectUrl(identity.getRemoteId()));
    String body = Utils.getTemplateGenerator().processTemplate(notification.getKey().getId(), templateContext, language);
    
    return messageInfo.subject(subject).body(body).end();
  }

  @Override
  public boolean makeDigest(NotificationContext ctx, Writer writer) {
    List<NotificationMessage> notifications = ctx.getNotificationMessages();
    NotificationMessage first = notifications.get(0);

    String language = getLanguage(first);
    
    Map<String, String> templateContext = new HashMap<String, String>();
    int count = notifications.size();
    String[] keys = {"USER", "USER_LIST", "LAST3_USERS"};
    String key = "";
    StringBuilder value = new StringBuilder();
    
    try {
      for (int i = 0; i < count && i < 3; i++) {
        String remoteId = notifications.get(i).getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());
        Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
        Profile userProfile = identity.getProfile();
        //
        if (i > 1 && count == 3) {
          key = keys[i - 1];
        } else {
          key = keys[i];
        }
        value.append(userProfile.getFullName());
        if (count > (i + 1) && i < 2) {
          value.append(", ");
        }
      }
      templateContext.put(key, value.toString());
      if(count > 3) {
        templateContext.put("COUNT", String.valueOf((count - 3)));
      }
      
      templateContext.put("PORTAL_NAME", System.getProperty("exo.notifications.portalname", "eXo"));
      String digester = Utils.getTemplateGenerator().processDigestIntoString(first.getKey().getId(), templateContext, language, count);
      writer.append(digester).append("</br>");
      
    } catch (IOException e) {
      ctx.setException(e);
      return false;
    }
    
    return true;
  }

}
