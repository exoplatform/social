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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class ActivityMentionPlugin extends AbstractNotificationPlugin {
  
  public static final String ID = "ActivityMentionPlugin";
  
  public ActivityMentionPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    
    Set<String> receivers = new HashSet<String>();
    if (activity.getMentionedIds().length > 0) {
      Utils.sendToMentioners(receivers, activity.getMentionedIds(), activity.getPosterId());
    } else {
      receivers = Utils.getMentioners(activity.getTemplateParams().get("comment"), activity.getPosterId());
    }

    return NotificationInfo.instance().key(getKey())
           .to(new ArrayList<String>(receivers))
           .with("poster", Utils.getUserId(activity.getPosterId()))
           .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
           .end();
  }

  @Override
  public MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    
    NotificationInfo notification = ctx.getNotificationInfo();
    String language = getLanguage(notification);

    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
    SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
    
    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
    Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);

    templateContext.put("USER", identity.getProfile().getFullName());
    String subject = TemplateUtils.processSubject(templateContext);
    
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(identity.getProfile()));
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    String body = "";
    
    // In case of mention on a comment, we need provide the id of the activity, not of the comment
    if (activity.isComment()) {
      ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
      activityId = parentActivity.getId();
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity_highlight_comment", activityId + "-" + activity.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", activityId + "-" + activity.getId()));
      templateContext.put("ACTIVITY", NotificationUtils.processLinkTitle(activity.getTitle()));
      body = TemplateUtils.processGroovy(templateContext);
    } else {
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activityId));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activityId));
      body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
    }
    
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
      for (NotificationInfo notification : notifications) {
        String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
        ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
        if (activity == null) {
          continue;
        }
        Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
        
        if (activity.isComment()) {
          activity = Utils.getActivityManager().getParentActivity(activity);
        }

        //make the list receivers who will send mail to them.
        SocialNotificationUtils.processInforSendTo(receiverMap, activity.getId(), identity.getRemoteId());
      }
      writer.append(SocialNotificationUtils.getMessageByIds(receiverMap, templateContext));
    } catch (IOException e) {
      ctx.setException(e);
      return false;
    }
    
    return true;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    if (activity.getMentionedIds().length > 0) {
      return true;
    }

    //Case of share link, the title of activity is the title of the link
    //so the process mention is not correct and no mention is saved to activity
    //We need to process the value stored in the template param of activity with key = comment
    String commentLinkActivity = activity.getTemplateParams().get("comment");
    if (commentLinkActivity != null && commentLinkActivity.length() > 0 &&
        Utils.getMentioners(commentLinkActivity, activity.getPosterId()).size() > 0) {
      return true;
    }

    return false;
  }

  @Override
  protected String makeUIMessage(NotificationContext ctx) {
    
    NotificationInfo notification = ctx.getNotificationInfo();
    String language = getLanguage(notification);

    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
    
    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
    Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
    templateContext.put("READ", (notification.isHasRead()) ? "read" : "unread");
    templateContext.put("NOTIFICATION_ID", notification.getId());
    templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(notification.getLastModifiedDate().getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
    templateContext.put("USER", identity.getProfile().getFullName());
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(identity.getProfile()));
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("ACTIVITY", NotificationUtils.processLinkTitle(activity.getTitle()));
    
    // In case of mention on a comment, we need provide the id of the activity, not of the comment
    if (activity.isComment()) {
      ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
      activityId = parentActivity.getId();
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", activityId + "-" + activity.getId()));
    } else {
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activityId));
    }
    return TemplateUtils.processIntranetGroovy(templateContext);
  }
}
