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
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class ActivityCommentPlugin extends AbstractNotificationPlugin {
  
  public ActivityCommentPlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "ActivityCommentPlugin";

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    ExoSocialActivity comment = ctx.value(SocialNotificationUtils.ACTIVITY);
    ExoSocialActivity activity = Utils.getActivityManager().getParentActivity(comment);
    //Send notification to all others users who have comment on this activity
    Set<String> receivers = new HashSet<String>();
    Utils.sendToCommeters(receivers, activity.getCommentedIds(), comment.getPosterId());
    Utils.sendToStreamOwner(receivers, activity.getStreamOwner(), comment.getPosterId());
    Utils.sendToActivityPoster(receivers, activity.getPosterId(), comment.getPosterId());

    //
    return NotificationInfo.instance()
           .to(new ArrayList<String>(receivers))
           .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), comment.getId())
           .with(SocialNotificationUtils.POSTER.getKey(), Utils.getUserId(comment.getUserId()))
           .key(getId());
  }

  @Override
  public MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    NotificationInfo notification = ctx.getNotificationInfo();
    String language = getLanguage(notification);

    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
    ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
    Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
    
    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
    templateContext.put("USER", identity.getProfile().getFullName());
    String subject = TemplateUtils.processSubject(templateContext);
    
    SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("COMMENT", NotificationUtils.processLinkTitle(activity.getTitle()));
    templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity_highlight_comment", parentActivity.getId() + "-" + activity.getId()));
    templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", parentActivity.getId() + "-" + activity.getId()));

    String body = SocialNotificationUtils.getBody(ctx, templateContext, parentActivity);
    
    return messageInfo.subject(subject).body(body).end();
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean makeDigest(NotificationContext ctx, Writer writer) {
    
    List<NotificationInfo> notifications = ctx.getNotificationInfos();
    NotificationInfo first = notifications.get(0);

    String language = getLanguage(first);
    TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
    SocialNotificationUtils.addFooterAndFirstName(first.getTo(), templateContext);
    
    //Store the activity id as key, and the list all identities who posted to the activity.
    Map<String, List<String>> receiverMap = new LinkedHashMap<String, List<String>>();
    
    try {
      for (NotificationInfo message : notifications) {
        String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
        ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
        if (activity == null) {
          continue;
        }
        ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
        //
        SocialNotificationUtils.processInforSendTo(receiverMap, parentActivity.getId(), message.getValueOwnerParameter("poster"));
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
    ExoSocialActivity comment = ctx.value(SocialNotificationUtils.ACTIVITY);
    ExoSocialActivity activity = Utils.getActivityManager().getParentActivity(comment);
    
    Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), false);
    //if the space is not null and it's not the default activity of space, then it's valid to make notification 
    if (spaceIdentity != null && activity.getPosterId().equals(spaceIdentity.getId())) {
      return false;
    }
    return true;
  }

  @Override
  protected String makeUIMessage(NotificationContext ctx) {
    NotificationInfo notification = ctx.getNotificationInfo();
    String language = getLanguage(notification);

    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
    ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
    Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
    
    TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
    templateContext.put("READ", (notification.isHasRead()) ? "read" : "unread");
    templateContext.put("NOTIFICATION_ID", notification.getId());
    templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(notification.getLastModifiedDate().getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
    templateContext.put("USER", identity.getProfile().getFullName());
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(identity.getProfile()));
    templateContext.put("ACTIVITY", NotificationUtils.removeLinkTitle(parentActivity.getTitle()));
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", parentActivity.getId() + "-" + activity.getId()));

    return TemplateUtils.processIntranetGroovy(templateContext);
  }
  
  
}
