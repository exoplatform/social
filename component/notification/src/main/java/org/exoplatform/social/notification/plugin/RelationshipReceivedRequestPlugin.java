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
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class RelationshipReceivedRequestPlugin extends AbstractNotificationPlugin {
  
  public RelationshipReceivedRequestPlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "RelationshipReceivedRequestPlugin";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    Relationship relation = ctx.value(SocialNotificationUtils.RELATIONSHIP);
    return NotificationInfo.instance()
                              .key(getId())
                              .to(relation.getReceiver().getRemoteId())
                              .with("sender", relation.getSender().getRemoteId())
                              .with(SocialNotificationUtils.RELATIONSHIP_ID.getKey(), relation.getId())
                              .end();
  }

  @Override
  public MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    
    NotificationInfo notification = ctx.getNotificationInfo();
    
    String language = getLanguage(notification);
    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

    String sender = notification.getValueOwnerParameter("sender");
    String toUser = notification.getTo();
    SocialNotificationUtils.addFooterAndFirstName(toUser, templateContext);
    Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, sender, true);
    Profile userProfile = identity.getProfile();
    
    templateContext.put("PORTAL_NAME", System.getProperty("exo.notifications.portalname", "eXo"));
    templateContext.put("USER", userProfile.getFullName());
    String subject = TemplateUtils.processSubject(templateContext);
    
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
    templateContext.put("ACCEPT_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getConfirmInvitationToConnectUrl(sender, toUser));
    templateContext.put("REFUSE_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToConnectUrl(sender, toUser));
    String body = TemplateUtils.processGroovy(templateContext);

    return messageInfo.subject(subject).body(body).end();
  }

  @Override
  public boolean makeDigest(NotificationContext ctx, Writer writer) {
    List<NotificationInfo> notifications = ctx.getNotificationInfos();
    NotificationInfo first = notifications.get(0);
    String language = getLanguage(first);
    
    TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
    Map<String, List<String>> receiverMap = new LinkedHashMap<String, List<String>>();

    try {
      for (NotificationInfo message : notifications) {
        Relationship relationship = Utils.getRelationshipManager().get(message.getValueOwnerParameter(SocialNotificationUtils.RELATIONSHIP_ID.getKey()));
        if (relationship == null || relationship.getStatus().name().equals("PENDING") == false) {
          continue;
        }
        SocialNotificationUtils.processInforSendTo(receiverMap, first.getTo(), message.getValueOwnerParameter(SocialNotificationUtils.SENDER.getKey()));
      }
      writer.append(SocialNotificationUtils.getMessageByIds(receiverMap, templateContext, "connections_request"));
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

    String sender = notification.getValueOwnerParameter("sender");
    String toUser = notification.getTo();
    Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, sender, true);
    Profile userProfile = identity.getProfile();
    
    templateContext.put("NOTIFICATION_ID", notification.getId());
    templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(TimeConvertUtils.getGreenwichMeanTime().getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
    templateContext.put("PORTAL_HOME", NotificationUtils.getPortalHome(NotificationPluginUtils.getBrandingPortalName()));
    templateContext.put("USER", userProfile.getFullName());
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
    templateContext.put("ACCEPT_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getConfirmInvitationToConnectUrl(sender, toUser));
    templateContext.put("REFUSE_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToConnectUrl(sender, toUser));
    return TemplateUtils.processIntranetGroovy(templateContext);
  }

}
