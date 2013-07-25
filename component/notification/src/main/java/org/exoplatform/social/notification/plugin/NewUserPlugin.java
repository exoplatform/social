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
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.TemplateContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationMessage;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;

public class NewUserPlugin extends AbstractNotificationPlugin {

  public static final String ID = "NewUserJoinSocialIntranet";
  public NewUserPlugin(InitParams initParams) {
    super(initParams);
  }
  
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationMessage makeNotification(NotificationContext ctx) {
    Profile profile = ctx.value(SocialNotificationUtils.PROFILE);
    
    try {
      List<String> allUsers = new ArrayList<String>();
      
      //TODO : This type of notification need to get all users who want to receive this kind of notification, except the new created user
      
      /*ProfileFilter profileFilter = new ProfileFilter();
      profileFilter.setExcludedIdentityList(Arrays.asList(profile.getIdentity()));
      ListAccess<Identity> list = Utils.getIdentityManager().getIdentitiesByProfileFilter(profile.getIdentity().getProviderId(), profileFilter, false);
      
      for (Identity identity : list.load(0, list.getSize())) {
        allUsers.add(identity.getRemoteId());
      }*/
      
      return NotificationMessage.instance()
                                .key(getId())
                                .with("remoteId", profile.getIdentity().getRemoteId())
                                .to(allUsers);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    
    NotificationMessage notification = ctx.getNotificationMessage();
    
    String language = getLanguage(notification);
    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
    SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

    String remoteId = notification.getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());
    Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
    Profile userProfile = identity.getProfile();
    
    templateContext.put("USER", userProfile.getFullName());
    templateContext.put("PORTAL_NAME", System.getProperty("exo.notifications.portalname", "eXo"));
    String subject = Utils.getTemplateGenerator().processSubject(templateContext);
    
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
    templateContext.put("CONNECT_ACTION_URL", LinkProviderUtils.getInviteToConnectUrl(identity.getRemoteId()));
    String body = Utils.getTemplateGenerator().processTemplate(templateContext);
    
    return messageInfo.subject(subject).body(body).end();
  }

  @Override
  public boolean makeDigest(NotificationContext ctx, Writer writer) {
    List<NotificationMessage> notifications = ctx.getNotificationMessages();
    NotificationMessage first = notifications.get(0);

    String language = getLanguage(first);
    TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
    SocialNotificationUtils.addFooterAndFirstName(first.getTo(), templateContext);
    
    int count = notifications.size();
    String[] keys = {"USER", "USER_LIST", "LAST3_USERS"};
    String key = "";
    StringBuilder value = new StringBuilder();
    
    try {
      for (int i = 0; i < count && i < 3; i++) {
        String remoteId = notifications.get(i).getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());
        Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
        //
        if (i > 1 && count == 3) {
          key = keys[i - 1];
        } else {
          key = keys[i];
        }
        value.append(SocialNotificationUtils.buildRedirecUrl("user", identity.getRemoteId(), identity.getProfile().getFullName()));
        if (count > (i + 1) && i < 2) {
          value.append(", ");
        }
      }
      templateContext.put(key, value.toString());
      if(count > 3) {
        templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl("connections", null, String.valueOf((count - 3))));
      }
      
      templateContext.put("PORTAL_NAME", System.getProperty("exo.notifications.portalname", "eXo"));
      String digester = Utils.getTemplateGenerator().processDigest(templateContext.digestType(count));
      writer.append(digester).append("</br>");
      
    } catch (IOException e) {
      ctx.setException(e);
      return false;
    }
    
    return true;
  }

}
