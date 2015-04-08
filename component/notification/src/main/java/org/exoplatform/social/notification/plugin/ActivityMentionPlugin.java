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

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.Utils;

public class ActivityMentionPlugin extends BaseNotificationPlugin {
  
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
           .with(SocialNotificationUtils.POSTER.getKey(), Utils.getUserId(activity.getPosterId()))
           .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
           .end();
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
}
