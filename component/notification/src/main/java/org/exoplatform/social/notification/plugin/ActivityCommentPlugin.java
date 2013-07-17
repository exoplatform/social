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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.SocialMessageBuilder;
import org.exoplatform.social.notification.Utils;

public class ActivityCommentPlugin extends AbstractNotificationPlugin {
  public final String ID = "ActivityCommentProvider";

  @Override
  public NotificationMessage makeNotification(NotificationContext ctx) {
    ExoSocialActivity comment = ctx.value(SocialMessageBuilder.ACTIVITY);
    ExoSocialActivity activity = Utils.getActivityManager().getParentActivity(comment);
    List<String> sendToUsers = Utils.getDestinataires(activity.getCommentedIds(), comment.getPosterId());
    if (! sendToUsers.contains(activity.getStreamOwner())) {
      sendToUsers.add(activity.getStreamOwner());
    }
    //
    return NotificationMessage.instance()
           .to(sendToUsers)
           .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), comment.getId())
           .with(SocialNotificationUtils.POSTER.getKey(), Utils.getUserId(comment.getUserId()))
           .key(getId());
  }

  @Override
  public MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    Map<String, String> templateContext = new HashMap<String, String>();
    
    NotificationMessage notification = ctx.getNotificationMessage();
    
    String language = getLanguage(notification);
    
    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
    ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
    Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
    
    templateContext.put("USER", identity.getProfile().getFullName());
    String subject = Utils.getTemplateGenerator().processSubjectIntoString(notification.getKey().getId(), templateContext, language);
    
    templateContext.put("COMMENT", activity.getTitle());
    templateContext.put("ACTIVITY", parentActivity.getTitle());
    templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getReplyActivityUrl(activity.getId(), notification.getSendToUserIds().get(0)));
    templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), notification.getSendToUserIds().get(0)));
    String body = Utils.getTemplateGenerator().processTemplateIntoString(notification.getKey().getId(), templateContext, language);
    
    return messageInfo.subject(subject).body(body).end();
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean makeDigest(NotificationContext ctx, Writer writer) {
    
    List<NotificationMessage> notifications = ctx.getNotificationMessages();
    NotificationMessage first = notifications.get(0);

    String language = getLanguage(first);
    ProviderData providerData = Utils.getProviderService().getProvider(first.getKey().getId());
    
    Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
    
    try {
      for (NotificationMessage message : notifications) {
        String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
        ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
        ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
        //
        SocialNotificationUtils.processInforSendTo(map, parentActivity.getId(), message.getValueOwnerParameter("poster"));
      }
      writer.append(SocialNotificationUtils.getMessageByIds(map, providerData, language));
    } catch (IOException e) {
      ctx.setException(e);
      return false;
    }
    
    return true;
  }
  
  
}
