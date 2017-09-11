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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.notification.Utils;

public class ActivityCommentPlugin extends BaseNotificationPlugin {

  public static final String ID = "ActivityCommentPlugin";

  protected boolean isSubComment = false;

  public ActivityCommentPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    ExoSocialActivity comment = ctx.value(SocialNotificationUtils.ACTIVITY);
    ExoSocialActivity activity = Utils.getActivityManager().getParentActivity(comment);

    Set<String> receivers = new HashSet<String>();
    if (StringUtils.isNotBlank(comment.getParentCommentId())) {
      ExoSocialActivity parentComment = Utils.getActivityManager().getActivity(comment.getParentCommentId());
      String parentCommentUserPosterId = Utils.getUserId(parentComment.getPosterId());
      if (isSubComment) {
        // Send notification to parent comment poster
        Utils.sendToActivityPoster(receivers, parentComment.getPosterId(), comment.getPosterId());
      } else {
        // Send notification to all others users who have commented on this activity
        // except parent comment poster
        Utils.sendToCommeters(receivers, activity.getCommentedIds(), comment.getPosterId());
        Utils.sendToStreamOwner(receivers, activity.getStreamOwner(), comment.getPosterId());
        Utils.sendToActivityPoster(receivers, activity.getPosterId(), comment.getPosterId());
        receivers.remove(parentCommentUserPosterId);
      }
    } else {
      // Send notification to all others users who have comment on this activity
      Utils.sendToCommeters(receivers, activity.getCommentedIds(), comment.getPosterId());
      Utils.sendToStreamOwner(receivers, activity.getStreamOwner(), comment.getPosterId());
      Utils.sendToActivityPoster(receivers, activity.getPosterId(), comment.getPosterId());
    }
    //
    return NotificationInfo.instance()
           .to(new ArrayList<String>(receivers))
           .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
           .with(SocialNotificationUtils.COMMENT_ID.getKey(), comment.getId())
           .with(SocialNotificationUtils.POSTER.getKey(), Utils.getUserId(comment.getUserId()))
           .key(getId());
  }


  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    ExoSocialActivity comment = ctx.value(SocialNotificationUtils.ACTIVITY);
    ExoSocialActivity activity = Utils.getActivityManager().getParentActivity(comment);

    if(isSubComment && comment.getParentCommentId() == null) {
      return false;
    }

    Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), false);
    //if the space is not null and it's not the default activity of space, then it's valid to make notification 
    if (spaceIdentity != null && activity.getPosterId().equals(spaceIdentity.getId())) {
      return false;
    }
    return true;
  }

}
