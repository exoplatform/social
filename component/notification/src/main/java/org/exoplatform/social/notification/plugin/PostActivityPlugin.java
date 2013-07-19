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
import java.util.List;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.TemplateContext;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.SocialMessageBuilder;
import org.exoplatform.social.notification.Utils;

public class PostActivityPlugin extends AbstractNotificationPlugin {
  public static final String ID = "ActivityPostProvider";
  
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationMessage makeNotification(NotificationContext ctx) {
    try {
      ExoSocialActivity activity = ctx.value(SocialMessageBuilder.ACTIVITY);
      
      return NotificationMessage.instance()
          .to(activity.getStreamOwner())
          .with(SocialNotificationUtils.POSTER.getKey(), Utils.getUserId(activity.getPosterId()))
          .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
          .key(getId()).end();
      
    } catch (Exception e) {
      ctx.setException(e);
    }
    
    return null;
  }

  @Override
  public MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    
    NotificationMessage notification = ctx.getNotificationMessage();
    
    String language = getLanguage(notification);
    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
    Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
    
    
    templateContext.put("USER", identity.getProfile().getFullName());
    String subject = Utils.getTemplateGenerator().processSubject(templateContext);
    
    templateContext.put("ACTIVITY", activity.getTitle());
    templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getReplyActivityUrl(activity.getId()));
    templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId()));
    String body = Utils.getTemplateGenerator().processTemplate(templateContext);
    
    return messageInfo.subject(subject).body(body).end();
  }

  @Override
  public boolean makeDigest(NotificationContext ctx, Writer writer) {
    List<NotificationMessage> notifications = ctx.getNotificationMessages();
    NotificationMessage first = notifications.get(0);

    String language = getLanguage(first);
    
    
    try {
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      for (NotificationMessage message : notifications) {
        String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
        ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
        Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
        
        templateContext.put("USER", SocialNotificationUtils.buildRedirecUrl("user", identity.getRemoteId(), identity.getProfile().getFullName()));
        templateContext.put("ACTIVITY", SocialNotificationUtils.buildRedirecUrl("activity", activity.getId(), activity.getTitle()));
        String digester = Utils.getTemplateGenerator().processDigest(templateContext.digestType(0));
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
