/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.social.notification.web.template;

import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.ActivityReplyToCommentPlugin;

public class ActivityCommentReplyWebBuilderTest extends AbstractPluginTest {
  private final static String ACTIVITY_TITLE = "my activity title post today.";
  private final static String COMMENT_TITLE = "my comment title add today.";
  private final static String SUB_COMMENT_TITLE = "my comment reply title add today.";

  @Override
  public AbstractTemplateBuilder getTemplateBuilder() {
    return null;
  }

  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(ActivityReplyToCommentPlugin.ID));
  }

  /**
   * Just test for simple case when post an activity + comment + comment reply
   * 
   * @throws Exception
   */
  public void testSimpleCase() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(maryIdentity, ACTIVITY_TITLE);
    assertMadeWebNotifications(1);
    notificationService.clearAll();
    //STEP 2 add comment
    ExoSocialActivity comment = makeComment(activity, rootIdentity, COMMENT_TITLE);
    //STEP 3 add comment reply
    makeCommentReply(activity, demoIdentity, SUB_COMMENT_TITLE, comment.getId());

    List<NotificationInfo> list = assertMadeWebNotifications(rootIdentity.getRemoteId(), 1);
    NotificationInfo commentReplyNotification = list.get(0);
    //STEP 4 assert Message info

    assertEquals(ActivityReplyToCommentPlugin.ID, commentReplyNotification.getKey().getId());
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(commentReplyNotification.setTo(rootIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);

    assertBody(info, ACTIVITY_TITLE);
    assertBody(info, "New reply on your comment");
  }

}
