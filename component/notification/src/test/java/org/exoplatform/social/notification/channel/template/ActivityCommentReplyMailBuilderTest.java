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
package org.exoplatform.social.notification.channel.template;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.ActivityReplyToCommentPlugin;

public class ActivityCommentReplyMailBuilderTest extends AbstractPluginTest {
  private final static String ACTIVITY_TITLE = "my activity title post today.";
  private final static String COMMENT_TITLE = "my comment title add today.";
  private final static String SUB_COMMENT_TITLE = "my comment reply title add today.";

  private ChannelManager manager;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    manager = getService(ChannelManager.class);
  }

  @Override
  public AbstractTemplateBuilder getTemplateBuilder() {
    AbstractChannel channel = manager.getChannel(ChannelKey.key(MailChannel.ID));
    assertTrue(channel != null);
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(ActivityReplyToCommentPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(ActivityReplyToCommentPlugin.ID));
  }
  

  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(ActivityReplyToCommentPlugin.ID));
  }

  /**
   * Just test for simple case when you post the simple activity
   * @throws Exception
   */
  public void testSimpleCase() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(maryIdentity, ACTIVITY_TITLE);
    assertMadeMailDigestNotifications(1);
    notificationService.clearAll();
    //STEP 2 add comment (No notif)
    ExoSocialActivity comment = makeComment(activity, rootIdentity, COMMENT_TITLE);
    //STEP 3 add comment reply (notif sent to root)
    makeCommentReply(activity, demoIdentity, SUB_COMMENT_TITLE, comment.getId());

    end();
    begin();

    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    NotificationInfo commentReplyNotification = list.get(0);

    //STEP 4 assert Message info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(commentReplyNotification.setTo(demoIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, demoIdentity.getProfile().getFullName() + " has replied to one of your comments");
    assertBody(info, ACTIVITY_TITLE);
    assertBody(info, "New reply on your comment");
  }

  public void testDigest() throws Exception {
    //mary post activity on root stream ==> notify to root
    ExoSocialActivity maryActivity = makeActivity(maryIdentity, ACTIVITY_TITLE);
    assertMadeMailDigestNotifications(1);
    notificationService.clearAll();

    List<NotificationInfo> toRoot = new ArrayList<NotificationInfo>();

    //root add comment to maryActivity ==> notify mary
    ExoSocialActivity comment = makeComment(maryActivity, rootIdentity, "root add comment");

    //demo add comment reply to maryActivity ==> notify to root and mary
    makeCommentReply(maryActivity, demoIdentity, "demo add reply", comment.getId());

    end();
    begin();

    List<NotificationInfo> list1 = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    toRoot.add(list1.get(0));
    notificationService.clearAll();

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    toRoot.set(0, toRoot.get(0).setTo(rootIdentity.getRemoteId()));
    ctx.setNotificationInfos(toRoot);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, getFullName("demo") + " has replied to your comment: my activity title post today.demo add reply");
  }
}
