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

import java.util.*;
import java.util.stream.Collectors;

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
    // We retrieve the previous mentions from the activity's template params in
    // order to get the added mention ids to notify
    Map<String, String> templateParams = activity.getTemplateParams() != null ? activity.getTemplateParams() : new HashMap<>();
    String[] actualMentions = activity.getMentionedIds();
    String[] previousMentions = templateParams.containsKey("PreviousMentions") ? templateParams.get("PreviousMentions").split(",")
                                                                               : new String[0];
    String[] mentionedIds = getAddedMentions(previousMentions, actualMentions);

    Set<String> receivers = new HashSet<>();
    if (actualMentions.length > 0) {
      Utils.sendToMentioners(receivers, mentionedIds, activity.getPosterId());
    } else {
      receivers = Utils.getMentioners(activity.getTemplateParams().get("comment"), activity.getPosterId());
    }

    return NotificationInfo.instance()
                           .key(getKey())
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

  private String[] getAddedMentions(String[] previousMentions, String[] actualMentions) {
    List<String> previousMentionsList = Arrays.asList(previousMentions);
    List<String> actualMentionsList = Arrays.asList(actualMentions);
    List<String> addedMentionsList = actualMentionsList.stream().filter(s -> !previousMentionsList.contains(s)).collect(Collectors.toList());
    return addedMentionsList.toArray(new String[addedMentionsList.size()]);
  }
}
