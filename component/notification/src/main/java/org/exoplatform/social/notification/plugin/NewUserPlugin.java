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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class NewUserPlugin extends AbstractNotificationPlugin {

  public static final String ID = "NewUserPlugin";
  public NewUserPlugin(InitParams initParams) {
    super(initParams);
  }
  
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    Profile profile = ctx.value(SocialNotificationUtils.PROFILE);
    String remoteId = profile.getIdentity().getRemoteId();
    try {
      UserSettingService userSettingService = CommonsUtils.getService(UserSettingService.class);
      //
      userSettingService.addMixin(remoteId);
      //

      return NotificationInfo.instance()
                              .key(getId())
                              .with(SocialNotificationUtils.REMOTE_ID.getKey(), remoteId)
                              .setSendAll(true)
                              .setFrom(remoteId)
                              .end();
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    
    NotificationInfo notification = ctx.getNotificationInfo();
    
    String language = getLanguage(notification);
    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
    SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

    String remoteId = notification.getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());
    Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
    Profile userProfile = identity.getProfile();
    
    templateContext.put("USER", userProfile.getFullName());
    templateContext.put("PORTAL_NAME", NotificationPluginUtils.getBrandingPortalName());
    templateContext.put("PORTAL_HOME", NotificationUtils.getPortalHome(NotificationPluginUtils.getBrandingPortalName()));
    String subject = TemplateUtils.processSubject(templateContext);
    
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
    templateContext.put("CONNECT_ACTION_URL", LinkProviderUtils.getInviteToConnectUrl(identity.getRemoteId(), notification.getTo()));
    String body = TemplateUtils.processGroovy(templateContext);
    
    return messageInfo.subject(subject).body(body).end();
  }

  @Override
  public boolean makeDigest(NotificationContext ctx, Writer writer) {
    List<NotificationInfo> notifications = ctx.getNotificationInfos();
    NotificationInfo first = notifications.get(0);

    String language = getLanguage(first);
    TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
    
    Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
    try {
      for (NotificationInfo message : notifications) {
        String remoteId = message.getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());

        Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
        //
        if (identity.isDeleted() == true) {
          continue;
        }
        
        SocialNotificationUtils.processInforSendTo(map, ID, remoteId);
      }
      
      String portalName = System.getProperty("exo.notifications.portalname", "eXo");
      
      templateContext.put("PORTAL_NAME", portalName);
      templateContext.put("PORTAL_HOME", SocialNotificationUtils.buildRedirecUrl("portal_home", portalName, portalName));

      writer.append(SocialNotificationUtils.getMessageByIds(map, templateContext, "new_user"));
    } catch (IOException e) {
      ctx.setException(e);
      return false;
    }
    
    return true;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }

  @Override
  protected String makeUIMessage(NotificationContext ctx) {
    NotificationInfo notification = ctx.getNotificationInfo();
    
    String language = getLanguage(notification);
    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

    String remoteId = notification.getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());
    Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
    Profile userProfile = identity.getProfile();
    templateContext.put("isIntranet", "true");
    templateContext.put("READ", (notification.isHasRead()) ? "read" : "unread");
    templateContext.put("NOTIFICATION_ID", notification.getId());
    templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(TimeConvertUtils.getGreenwichMeanTime().getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
    templateContext.put("USER", userProfile.getFullName());
    templateContext.put("PORTAL_NAME", NotificationPluginUtils.getBrandingPortalName());
    //templateContext.put("PORTAL_HOME", NotificationUtils.getPortalHome(NotificationPluginUtils.getBrandingPortalName()));
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
    return TemplateUtils.processGroovy(templateContext);
  }

}
