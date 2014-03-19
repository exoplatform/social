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
package org.exoplatform.social.notification.plugin.child;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

public class DefaultActivityChildPlugin extends AbstractNotificationChildPlugin {

  public static final String ID = "DEFAULT_ACTIVITY";

  public DefaultActivityChildPlugin(InitParams initParams) {
    super(initParams);
  }
/*
  DEFAULT_ACTIVITY
  USER_PROFILE_ACTIVITY
  USER_ACTIVITIES_FOR_RELATIONSHIP
  SPACE_ACTIVITY
  LINK_ACTIVITY
  DOC_ACTIVITY
  files:spaces
  contents:spaces
  cs-calendar:spaces
  ks-forum:spaces
  ks-answer:spaces
  ks-poll:spaces
  ks-wiki:spaces
*/
  @Override
  public String makeContent(NotificationContext ctx) {
    NotificationInfo notification = ctx.getNotificationInfo();

    String language = getLanguage(notification);
    TemplateContext templateContext = new TemplateContext(ID, language);

    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
    if (activity.isComment()) {
      //we need to build the content of activity by type, so if it's a comment, we will get the parent activity
      activity = Utils.getActivityManager().getParentActivity(activity);
    }
    templateContext.put("ACTIVITY", NotificationUtils.processLinkTitle(activity.getTitle()));
    //
    String content = TemplateUtils.processGroovy(templateContext);
    return content;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return false;
  }

}