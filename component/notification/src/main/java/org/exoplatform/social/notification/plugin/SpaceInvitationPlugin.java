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
import java.util.Calendar;
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
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class SpaceInvitationPlugin extends AbstractNotificationPlugin {
  
  private static final String ACCEPT_SPACE_INVITATION = "social/intranet-notification/acceptInvitationToJoinSpace";

  private static final String REFUSE_SPACE_INVITATION = "social/intranet-notification/ignoreInvitationToJoinSpace";
  
  public static final String ID = "SpaceInvitationPlugin";

  public SpaceInvitationPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    Space space = ctx.value(SocialNotificationUtils.SPACE);
    String userId = ctx.value(SocialNotificationUtils.REMOTE_ID);
    
    return NotificationInfo.instance().key(getId())
           .with(SocialNotificationUtils.PRETTY_NAME.getKey(), space.getPrettyName())
           .with(SocialNotificationUtils.SPACE_ID.getKey(), space.getId())
           .to(userId).end();
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
    
    templateContext.put("SPACE", space.getDisplayName());
    templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
    String subject = TemplateUtils.processSubject(templateContext);
    
    templateContext.put("SPACE_AVATAR", LinkProviderUtils.getSpaceAvatarUrl(space));
    templateContext.put("ACCEPT_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getAcceptInvitationToJoinSpaceUrl(space.getId(), notification.getTo()));
    templateContext.put("REFUSE_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToJoinSpaceUrl(space.getId(), notification.getTo()));
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
        String spaceId = message.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
        Space space = Utils.getSpaceService().getSpaceById(spaceId);
        if (ArrayUtils.contains(space.getInvitedUsers(), first.getTo()) == false) {
          continue;
        }

        SocialNotificationUtils.processInforSendTo(receiverMap, first.getTo(), spaceId);
      }
      writer.append(SocialNotificationUtils.getMessageByIds(receiverMap, templateContext, "space"));
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

    String status = notification.getValueOwnerParameter("status");
    String spaceId = notification.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
    Space space = Utils.getSpaceService().getSpaceById(spaceId);
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(notification.getLastModifiedDate());
    templateContext.put("isIntranet", "true");
    templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(AbstractService.NTF_READ)) ? "read" : "unread");
    templateContext.put("STATUS", status != null && status.equals("accepted") ? "ACCEPTED" : "PENDING");
    templateContext.put("NOTIFICATION_ID", notification.getId());
    templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
    templateContext.put("SPACE", space.getDisplayName());
    templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
    templateContext.put("SPACE_AVATAR", LinkProviderUtils.getSpaceAvatarUrl(space));
    templateContext.put("ACCEPT_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getRestUrl(ACCEPT_SPACE_INVITATION, space.getId(), notification.getTo()));
    templateContext.put("REFUSE_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getRestUrl(REFUSE_SPACE_INVITATION, space.getId(), notification.getTo()));
    return TemplateUtils.processGroovy(templateContext);
    
  }

}