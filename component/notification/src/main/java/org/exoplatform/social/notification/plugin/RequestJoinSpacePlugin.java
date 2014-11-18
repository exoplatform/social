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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class RequestJoinSpacePlugin extends AbstractNotificationPlugin {
  
  public RequestJoinSpacePlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "RequestJoinSpacePlugin";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    Space space = ctx.value(SocialNotificationUtils.SPACE);
    String userId = ctx.value(SocialNotificationUtils.REMOTE_ID);
    
    return NotificationInfo.instance().key(getId())
           .with("request_from", userId)
           .with("spaceId", space.getId())
           .to(Arrays.asList(space.getManagers()));
  }

  @Override
  public MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    
    NotificationInfo notification = ctx.getNotificationInfo();
    
    String language = getLanguage(notification);
    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
    SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

    String spaceId = notification.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
    Space space = Utils.getSpaceService().getSpaceById(spaceId);
    Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getValueOwnerParameter("request_from"), true);
    Profile userProfile = identity.getProfile();
    
    templateContext.put("SPACE", space.getDisplayName());
    templateContext.put("USER", userProfile.getFullName());
    String subject = TemplateUtils.processSubject(templateContext);
    
    templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space_members", space.getId()));
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
    templateContext.put("VALIDATE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getValidateRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
    templateContext.put("REFUSE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getRefuseRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
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
        String spaceId = message.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
        String fromUser = message.getValueOwnerParameter("request_from");
        Space space = Utils.getSpaceService().getSpaceById(spaceId);
        if (ArrayUtils.contains(space.getPendingUsers(), fromUser) == false) {
          continue;
        }
        //
        SocialNotificationUtils.processInforSendTo(map, spaceId, fromUser);
      }
      writer.append(SocialNotificationUtils.getMessageByIds(map, templateContext));
    } catch (IOException e) {
      ctx.setException(e);
      return false;
    }
    
    
    return true;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    //only sent when the space has the registration option "Validation"
    Space space = ctx.value(SocialNotificationUtils.SPACE);
    if (space.getRegistration().equals(Space.VALIDATION)) {
      return true;
    }
    return false;
  }

  @Override
  protected String makeUIMessage(NotificationContext ctx) {
    NotificationInfo notification = ctx.getNotificationInfo();
    
    String language = getLanguage(notification);
    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

    String spaceId = notification.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
    Space space = Utils.getSpaceService().getSpaceById(spaceId);
    Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getValueOwnerParameter("request_from"), true);
    Profile userProfile = identity.getProfile();
    
    templateContext.put("NOTIFICATION_ID", notification.getId());
    templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(TimeConvertUtils.getGreenwichMeanTime().getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
    templateContext.put("SPACE", space.getDisplayName());
    templateContext.put("USER", userProfile.getFullName());
    templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space_members", space.getId()));
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
    templateContext.put("VALIDATE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getValidateRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
    templateContext.put("REFUSE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getRefuseRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
    return TemplateUtils.processIntranetGroovy(templateContext);
  }

}
