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
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.Utils;

public class LikePlugin extends BaseNotificationPlugin {
  
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
                               .with(SocialNotificationUtils.LIKER.getKey(), liker)
                               .key(getId()).end();
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
