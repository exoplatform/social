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

public class EditActivityPlugin extends BaseNotificationPlugin {

    public EditActivityPlugin(InitParams initParams) {
        super(initParams);
    }

    public static final String ID = "EditActivityPlugin";

    @Override
    public String getId() {
        return ID;
    }

    protected boolean isSubComment = false;

    @Override
    public NotificationInfo makeNotification(NotificationContext ctx) {
        ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);

        Set<String> receivers = new HashSet<String>();
        if (activity.getStreamOwner() != null) {
            Utils.sendToStreamOwner(receivers, activity.getStreamOwner(), activity.getPosterId());
        }
        // Send notification to all others users who have comment on this activity
        Utils.sendToCommeters(receivers, activity.getCommentedIds(), activity.getPosterId());
        Utils.sendToActivityPoster(receivers, activity.getPosterId(), activity.getPosterId());

        //
        return NotificationInfo.instance()
                .to(new ArrayList<String>(receivers))
                .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                .with(SocialNotificationUtils.POSTER.getKey(), Utils.getUserId(activity.getUserId()))
                .key(getId());
    }


    @Override
    public boolean isValid(NotificationContext ctx) {
        ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);



        Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), false);
        //if the space is not null and it's not the default activity of space, then it's valid to make notification
        if (spaceIdentity != null && activity.getPosterId().equals(spaceIdentity.getId())) {
            return false;
        }
        return true;
    }

}
