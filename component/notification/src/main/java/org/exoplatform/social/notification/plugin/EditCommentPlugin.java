package org.exoplatform.social.notification.plugin;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.notification.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EditCommentPlugin  extends BaseNotificationPlugin {
    public static final String ID = "EditCommentPlugin";

    protected boolean isSubComment = false;

    public EditCommentPlugin(InitParams initParams) {
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
