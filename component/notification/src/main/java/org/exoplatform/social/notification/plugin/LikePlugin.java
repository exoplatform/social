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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;

public class LikePlugin extends AbstractNotificationPlugin {
  
  public LikePlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "LikePlugin";
  
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    
    String[] likersId = activity.getLikeIdentityIds();
    String liker = Utils.getUserId(likersId[likersId.length - 1]);

    List<String> toUsers = new ArrayList<String>();
    toUsers.add(Utils.getUserId(activity.getPosterId()));
    if (Utils.isSpaceActivity(activity) == false && liker.equals(activity.getStreamOwner()) == false) {
      toUsers.add(activity.getStreamOwner());
    }

    return NotificationInfo.instance()
                               .to(Utils.getUserId(activity.getPosterId()))
                               .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                               .with("likersId", liker)
                               .key(getId()).end();
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
    Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getValueOwnerParameter("likersId"), true);
    
    templateContext.put("USER", identity.getProfile().getFullName());
    templateContext.put("SUBJECT", activity.getTitle());
    String subject = TemplateUtils.processSubject(templateContext);

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

        //
        if (activity == null) {
          continue;
        }

        //
        String fromUser = message.getValueOwnerParameter("likersId");

        Identity identityFrom = Utils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, fromUser, false);
        if (identityFrom == null || !Arrays.asList(activity.getLikeIdentityIds()).contains(identityFrom.getId())) {
          continue;
        }
        //
        SocialNotificationUtils.processInforSendTo(map, activityId, message.getValueOwnerParameter("likersId"));
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
    String[] likersId = activity.getLikeIdentityIds();
    if (activity.getPosterId().equals(likersId[likersId.length-1])) {
      return false;
    }
    return true;
  }

}
