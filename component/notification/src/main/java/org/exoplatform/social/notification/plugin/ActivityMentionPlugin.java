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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;

public class ActivityMentionPlugin extends AbstractNotificationPlugin {
  public final String ID = "ActivityMentionPlugin";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationMessage makeNotification(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    List<String> sendToUsers = Utils.getDestinataires(activity.getMentionedIds(), activity.getPosterId());
    
    return NotificationMessage.instance().key(getKey())
           .to(sendToUsers)
           .with("poster", Utils.getUserId(activity.getPosterId()))
           .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
           .end();
  }

  @Override
  public MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    Map<String, String> templateContext = new HashMap<String, String>();
    
    NotificationMessage notification = ctx.getNotificationMessage();
    
    String language = getLanguage(notification);
    
    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
    Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);

    templateContext.put("USER", identity.getProfile().getFullName());
    String subject = Utils.getTemplateGenerator().processSubjectIntoString(notification.getKey().getId(), templateContext, language);
    
    templateContext.put("ACTIVITY", activity.getTitle());
    templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getReplyActivityUrl(activity.getId(), notification.getSendToUserIds().get(0)));
    templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), notification.getSendToUserIds().get(0)));
    String body = Utils.getTemplateGenerator().processTemplate(notification.getKey().getId(), templateContext, language);
   
    return messageInfo.subject(subject).body(body).end();
  }

  @Override
  public boolean makeDigest(NotificationContext ctx, Writer writer) {
    List<NotificationMessage> notifications = ctx.getNotificationMessages();
    NotificationMessage first = notifications.get(0);

    String language = getLanguage(first);
    try {
      Map<String, String> templateContext = new HashMap<String, String>();
      for (NotificationMessage notification : notifications) {
        String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
        ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
        Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);

        templateContext.put("USER", identity.getProfile().getFullName());
        templateContext.put("ACTIVITY", activity.getTitle());
        String digester = Utils.getTemplateGenerator().processDigestIntoString(notification.getKey().getId(), templateContext, language, 0);

        writer.append(digester).append("</br>");

        templateContext.clear();
      }

    } catch (IOException e) {
      ctx.setException(e);
      return false;
    }
    
    return true;
  }
}
