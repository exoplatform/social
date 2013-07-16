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

import java.util.List;

import org.exoplatform.commons.api.notification.ArgumentLiteral;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.task.AbstractNotificationTask;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.Utils;

public abstract class ActivityTask extends AbstractNotificationTask<NotificationContext> {
  
  public final static ArgumentLiteral<String> ACTIVITY_ID = new ArgumentLiteral<String>(String.class, "activityId");
  public final static ArgumentLiteral<ExoSocialActivity> ACTIVITY = new ArgumentLiteral<ExoSocialActivity>(ExoSocialActivity.class, "activity");
  
  @Override
  public void start(NotificationContext ctx) {

  }

  @Override
  public void end(NotificationContext ctx) {

  }
  

  /**
   * Someone @mentions the user in an activity.
   */
  public static ActivityTask MENTION_ACTIVITY = new ActivityTask() {
    private final String PROVIDER_TYPE = "ActivityMentionProvider";

    @Override
    public String getId() {
      return PROVIDER_TYPE;
    }
    
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      ExoSocialActivity activity = ctx.value(ACTIVITY);
      List<String> sendToUsers = Utils.getDestinataires(activity.getMentionedIds(), activity.getPosterId());
      
      return NotificationMessage.getInstance().setProviderType(PROVIDER_TYPE)
             .setSendToUserIds(sendToUsers)
             .addOwnerParameter("poster", Utils.getUserId(activity.getPosterId()))
             .addOwnerParameter("activityId", activity.getId());
    }

    @Override
    public boolean isValid(NotificationContext ctx) {
      ExoSocialActivity activity = ctx.value(ACTIVITY);
      return activity.getMentionedIds().length > 0;
    }
  };

  /**
   * Someone comments on an activity posted by the user.
   */
  public static ActivityTask COMMENT_ACTIVITY = new ActivityTask() {
    private final String PROVIDER_TYPE = "ActivityCommentProvider";

    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      ExoSocialActivity comment = ctx.value(ACTIVITY);
      ExoSocialActivity activity = Utils.getActivityManager().getParentActivity(comment);
      List<String> sendToUsers = Utils.getDestinataires(activity.getCommentedIds(), comment.getPosterId());
      if (! sendToUsers.contains(activity.getStreamOwner()) && ! Utils.isSpaceActivity(activity) && ! activity.getPosterId().equals(comment.getPosterId())) {
        sendToUsers.add(activity.getStreamOwner());
      }
      //
      return NotificationMessage.getInstance()
             .setSendToUserIds(sendToUsers)
             .addOwnerParameter("activityId", comment.getId())
             .addOwnerParameter("poster", Utils.getUserId(comment.getUserId()))
             .setProviderType(PROVIDER_TYPE);
    }

    @Override
    public boolean isValid(NotificationContext ctx) {
      return true;
    }
    
    @Override
    public String getId() {
      return PROVIDER_TYPE;
    }

  };

  /**
   * Someone posts an activity on the User's stream.
   */
  public static ActivityTask POST_ACTIVITY = new ActivityTask() {
    private final String PROVIDER_TYPE = "ActivityPostProvider";

    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      try {
        ExoSocialActivity activity = ctx.value(ACTIVITY);
        
        return NotificationMessage.getInstance()
            .addSendToUserId(activity.getStreamOwner())
            .addOwnerParameter("poster", Utils.getUserId(activity.getPosterId()))
            .addOwnerParameter("activityId", activity.getId())
            .setProviderType(PROVIDER_TYPE);
      } catch (Exception e) {
        return null;
      }
    }
    
    @Override
    public boolean isValid(NotificationContext ctx) {
      ExoSocialActivity activity = ctx.value(ACTIVITY);
      try {
        if (Utils.isSpaceActivity(activity))
          return false;
        return ! activity.getStreamOwner().equals(Utils.getUserId(activity.getPosterId()));
      } catch (Exception e) {
        return false;
      }
    }
    
    @Override
    public String getId() {
      return PROVIDER_TYPE;
    }

  };
  
  /**
   * Someone likes an activity on the User's stream.
   */
  public static ActivityTask LIKE = new ActivityTask() {
    private final String PROVIDER_TYPE = "ActivityLikeProvider";

    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      
      ExoSocialActivity activity = ctx.value(ACTIVITY);
      String[] likersId = activity.getLikeIdentityIds();
      return NotificationMessage.getInstance()
             .addSendToUserId(activity.getStreamOwner())
             .addOwnerParameter("activityId", activity.getId())
             .addOwnerParameter("likersId", Utils.getUserId(likersId[likersId.length-1]))
             .setProviderType(PROVIDER_TYPE);
    }
    
    @Override
    public boolean isValid(NotificationContext ctx) {
      ExoSocialActivity activity = ctx.value(ACTIVITY);
      String[] likers = activity.getLikeIdentityIds();
      return ! activity.getPosterId().equals(likers[likers.length - 1]);
    }
    
    @Override
    public String getId() {
      return PROVIDER_TYPE;
    }

  };

  /**
   * Someone posts an activity on a space where the user is a member.
   */
  public static ActivityTask POST_ACTIVITY_ON_SPACE = new ActivityTask() {
    
    private final String PROVIDER_TYPE = "ActivityPostSpaceProvider";

    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      try {
        ExoSocialActivity activity = ctx.value(ACTIVITY);
        
        Space space = Utils.getSpaceService().getSpaceByPrettyName(activity.getStreamOwner());
        
        
        return NotificationMessage.getInstance().setProviderType(PROVIDER_TYPE)
                                  .addOwnerParameter("poster", Utils.getUserId(activity.getPosterId()))
                                  .addOwnerParameter("activityId", activity.getId())
                                  .setSendToUserIds(Utils.getDestinataires(activity, space));
      } catch (Exception e) {
        return null;
      }
    }

    @Override
    public boolean isValid(NotificationContext ctx) {
      ExoSocialActivity activity = ctx.value(ACTIVITY);
      if (! Utils.isSpaceActivity(activity))
        return false;
      Identity id = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
      return ! id.getRemoteId().equals(activity.getStreamOwner());
    }
    
    @Override
    public String getId() {
      return PROVIDER_TYPE;
    }

  };
}
