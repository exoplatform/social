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

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.webui.utils.TimeConvertUtils;

public class PostActivitySpaceStreamPlugin extends AbstractNotificationPlugin {
  
  public PostActivitySpaceStreamPlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "PostActivitySpaceStreamPlugin";
  
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    try {
      
      ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
      Space space = Utils.getSpaceService().getSpaceByPrettyName(activity.getStreamOwner());
      String poster = Utils.getUserId(activity.getPosterId());
      
      return NotificationInfo.instance()
                                .key(getId())
                                .with(SocialNotificationUtils.POSTER.getKey(), poster)
                                .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                                .to(Utils.getDestinataires(activity, space)).end();
    } catch (Exception e) {
      ctx.setException(e);
    }
    
    return null;
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
    
    Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
    
    templateContext.put("USER", identity.getProfile().getFullName());
    templateContext.put("SPACE", spaceIdentity.getProfile().getFullName());
    templateContext.put("SUBJECT", activity.getTitle());
    String subject = TemplateUtils.processSubject(templateContext);
    
    Space space = Utils.getSpaceService().getSpaceByPrettyName(spaceIdentity.getRemoteId());
    templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
    templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));

    String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
    
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
        String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
        ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
        if (activity == null) {
          continue;
        }
        Space space = Utils.getSpaceService().getSpaceByPrettyName(activity.getStreamOwner());
        //
        SocialNotificationUtils.processInforSendTo(map, space.getId(), message.getValueOwnerParameter(SocialNotificationUtils.POSTER.getKey()));
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
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), false);
    //if the space is not null and it's not the default activity of space, then it's valid to make notification 
    if (spaceIdentity != null && activity.getPosterId().equals(spaceIdentity.getId()) == false) {
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
    
    Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
    Space space = Utils.getSpaceService().getSpaceByPrettyName(spaceIdentity.getRemoteId());
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(notification.getLastModifiedDate());
    templateContext.put("isIntranet", "true");
    templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(AbstractService.NTF_READ)) ? "read" : "unread");
    templateContext.put("NOTIFICATION_ID", notification.getId());
    templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
    templateContext.put("USER", identity.getProfile().getFullName());
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(identity.getProfile()));
    templateContext.put("ACTIVITY", NotificationUtils.removeLinkTitle(activity.getTitle()));
    templateContext.put("SPACE", spaceIdentity.getProfile().getFullName());
    templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
    templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
    templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));

    return TemplateUtils.processGroovy(templateContext);
  }

}
