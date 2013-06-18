/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.task;

import org.exoplatform.commons.api.notification.NotificationMessage;

import java.util.Arrays;

import org.exoplatform.commons.api.notification.task.NotificationTask;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.context.NotificationContext;

public abstract class ActivityTask implements NotificationTask<NotificationContext> {
  public enum PROVIDER_TYPE {
    MENTION("ActivityMentionProvider"), COMMENT("ActivityCommentProvider"),
    POST("ActivityPostProvider"), POST_SPACE("ActivityPostSpaceProvider");
    private final String name;

    PROVIDER_TYPE(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  };
  
  @Override
  public void initSupportProvider() {
  }

  @Override
  public void start(NotificationContext ctx) {
    // TODO Auto-generated method stub

  }

  @Override
  public void end(NotificationContext ctx) {
    // TODO Auto-generated method stub

  }

  /**
   * Someone @mentions the user in an activity.
   */
  public static ActivityTask MENTION_ACTIVITY       = new ActivityTask() {

    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      // method stub
      ExoSocialActivity activity = ctx.getActivity();
      String[] mentionerIds = activity.getMentionedIds();
      if (mentionerIds.length > 0) {
        //
        message.setProviderType(PROVIDER_TYPE.MENTION.getName())
        
               .setFrom(Utils.getUserId(activity.getPosterId()))
        
               .setSendToUserIds(Arrays.asList(mentionerIds))

               .addOwnerParameter("activityId", activity.getId());
        
        //
        return message;
      }
      
      return null;
    }

  };

  /**
   * Someone comments on an activity posted by the user.
   */
  public static ActivityTask COMMENT_ACTIVITY       = new ActivityTask() {

    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      
      // which? activity or comment
      ExoSocialActivity activity = ctx.getActivity();
      
      //
      message.setFrom(Utils.getUserId(activity.getPosterId()))
      
             .setSendToUserIds(Utils.toListUserIds(activity.getStreamOwner()))
      
             .setProviderType(PROVIDER_TYPE.COMMENT.toString());
      
      //
      return message;
    }

  };

  /**
   * Someone posts an activity on the User's stream.
   */
  public static ActivityTask POST_ACTIVITY          = new ActivityTask() {

    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();

      //
      ExoSocialActivity activity = ctx.getActivity();
      
      //
      message.setFrom(Utils.getUserId(activity.getPosterId()))
      
             .setSendToUserIds(Utils.toListUserIds(activity.getStreamOwner()))
      
             .setProviderType(PROVIDER_TYPE.POST.toString());
      
      return message;
    }

  };

  /**
   * Someone posts an activity on a space where the user is a member.
   */
  public static ActivityTask POST_ACTIVITY_ON_SPACE = new ActivityTask() {

    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      
      //
      ExoSocialActivity activity = ctx.getActivity();
      
      //
      message.setFrom(Utils.getUserId(activity.getPosterId()));
      
      //
      String ownerStream = activity.getStreamOwner();
      
      Identity id = Utils.getIdentityManager().getIdentity(ownerStream, false);
      
      if (Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, id.getRemoteId(), false) != null) {
      
        Space space = Utils.getSpaceService().getSpaceByPrettyName(id.getRemoteId());
        message.setSendToUserIds(Arrays.asList(space.getMembers()));
        
        //
        message.setProviderType(PROVIDER_TYPE.POST_SPACE.toString());
        
        return message;
        
      }
      
      return null;
    }

  };
}
