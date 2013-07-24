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
import java.util.Map;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.TemplateContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationMessage;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;

public class ActivityCommentPlugin extends AbstractNotificationPlugin {
  
  public ActivityCommentPlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "ActivityCommentProvider";

  @Override
  public NotificationMessage makeNotification(NotificationContext ctx) {
    ExoSocialActivity comment = ctx.value(SocialNotificationUtils.ACTIVITY);
    ExoSocialActivity activity = Utils.getActivityManager().getParentActivity(comment);
    List<String> sendToUsers = Utils.getDestinataires(activity.getCommentedIds(), comment.getPosterId());
    if (! sendToUsers.contains(activity.getStreamOwner()) && ! Utils.isSpaceActivity(activity) && ! activity.getPosterId().equals(comment.getPosterId())) {
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
    NotificationMessage notification = ctx.getNotificationMessage();
    String language = getLanguage(notification);

    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
    
    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
    ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
    Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
    
    templateContext.put("USER", identity.getProfile().getFullName());
    String subject = Utils.getTemplateGenerator().processSubject(templateContext);
    
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("COMMENT", activity.getTitle());
    templateContext.put("ACTIVITY", parentActivity.getTitle());
    templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", parentActivity.getId()));
    templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", parentActivity.getId()));
    String body = Utils.getTemplateGenerator().processTemplate(templateContext);
    
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
    
    Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
    
    try {
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      for (NotificationMessage message : notifications) {
        String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
        ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
        ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
        //
        SocialNotificationUtils.processInforSendTo(map, parentActivity.getId(), message.getValueOwnerParameter("poster"));
      }
      writer.append(SocialNotificationUtils.getMessageByIds(map, templateContext));
    } catch (IOException e) {
      ctx.setException(e);
      return false;
    }
    
    return true;
  }
  
  
}
