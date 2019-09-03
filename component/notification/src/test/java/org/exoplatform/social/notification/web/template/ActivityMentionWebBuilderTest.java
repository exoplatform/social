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
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.ActivityMentionPlugin;

public class ActivityMentionWebBuilderTest extends AbstractPluginTest {
  private ChannelManager manager;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    manager = getService(ChannelManager.class);
  }
  
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }
  

  @Override
  public AbstractTemplateBuilder getTemplateBuilder() {
    AbstractChannel channel = manager.getChannel(ChannelKey.key(WebChannel.ID));
    assertTrue(channel != null);
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(ActivityMentionPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(ActivityMentionPlugin.ID));
  }
  
  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(ActivityMentionPlugin.ID));
  }
  
  public void testActivityMention() throws Exception {
    //mary post activity on root stream and mention john and demo ==> 3 notifications
    makeActivity(maryIdentity, "hello @john and @demo");
    assertMadeWebNotifications(3);
    List<NotificationInfo> list = assertMadeWebNotifications(johnIdentity.getRemoteId(), 1);
    NotificationInfo mentionNotification = list.get(0);
    
    //assert Message info
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(mentionNotification.setTo(demoIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertBody(info, "has mentioned you");
  }

  public void testEditActivityMention() throws Exception {
    // mary posts an activity on stream and mention root ==> 1 notification
    ExoSocialActivity maryActivity = makeActivityOnStream(ghostIdentity, "Hello @root");
    assertMadeWebNotifications(1);

    List<NotificationInfo> toRoot = assertMadeWebNotifications(rootIdentity.getRemoteId(), 1);
    NotificationInfo mentionNotification = toRoot.get(0);

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(mentionNotification.setTo(rootIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);

    assertBody(info, "has mentioned you");
    notificationService.clearAll();

    // mary edits its activity and adds a mention to john => 1 notification
    maryActivity = editActivity(maryActivity, "Hello @root and @john");
    assertMadeWebNotifications(1);

    List<NotificationInfo> toJohn = assertMadeWebNotifications(johnIdentity.getRemoteId(), 1);
    mentionNotification = toJohn.get(0);

    ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(mentionNotification.setTo(johnIdentity.getRemoteId()));
    info = buildMessageInfo(ctx);

    assertBody(info, "has mentioned you");
    notificationService.clearAll();

    // mary re-edits its activity and adds a mention to demo => 1 notification
    editActivity(maryActivity, "Hello @demo @root and @john");
    assertMadeWebNotifications(1);

    List<NotificationInfo> toDemo = assertMadeWebNotifications(demoIdentity.getRemoteId(), 1);
    mentionNotification = toDemo.get(0);

    ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(mentionNotification.setTo(demoIdentity.getRemoteId()));
    info = buildMessageInfo(ctx);

    assertBody(info, "has mentioned you");
    notificationService.clearAll();
  }

  public void testCommentMention() throws Exception {
    ExoSocialActivity activity = makeActivity(maryIdentity, "mary post activity on root stream");
    assertMadeWebNotifications(1);
    notificationService.clearAll();
    //demo post comment on mary's activity and mention to john and root
    makeComment(activity, demoIdentity, "hello @john and @root");
    //2 messages to root and mary for comment and 2 messages to root and john for mention
    assertMadeWebNotifications(4);
    List<NotificationInfo> list = assertMadeWebNotifications(rootIdentity.getRemoteId(), 2);
    NotificationInfo mentionNotification = list.get(1);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(mentionNotification.setTo(johnIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertBody(info, "has mentioned you");
  }

  public void testEditCommentMention() throws Exception {
    ExoSocialActivity ghostActivity = makeActivityOnStream(ghostIdentity, "john post activity on activity stream");
    assertMadeWebNotifications(0);

    // mary post comment on ghost's activity and mention to ghost and demo
    ExoSocialActivity maryComment = makeComment(ghostActivity, maryIdentity, "hello @john and @demo");

    // 2 messages to john and demo for mention
    assertMadeWebNotifications(2);
    List<NotificationInfo> toJohn = assertMadeWebNotifications(johnIdentity.getRemoteId(), 1);
    NotificationInfo mentionNotification = toJohn.get(0);

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(mentionNotification.setTo(johnIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);

    assertBody(info, "has mentioned you");

    List<NotificationInfo> toDemo = assertMadeWebNotifications(demoIdentity.getRemoteId(), 1);
    mentionNotification = toDemo.get(0);

    ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(mentionNotification.setTo(demoIdentity.getRemoteId()));
    info = buildMessageInfo(ctx);

    assertBody(info, "has mentioned you");

    // clear notifications
    notificationService.clearAll();

    // mary edit comment on john's activity and mention root
    editComment(ghostActivity, maryComment, "hello @root @john and @demo");

    // Only 1 message to root for mention
    assertMadeWebNotifications(1);
    assertMadeWebNotifications(johnIdentity.getRemoteId(), 0);
    assertMadeWebNotifications(demoIdentity.getRemoteId(), 0);
    List<NotificationInfo> toRoot = assertMadeWebNotifications(rootIdentity.getRemoteId(), 1);
    mentionNotification = toRoot.get(0);

    ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(mentionNotification.setTo(rootIdentity.getRemoteId()));
    info = buildMessageInfo(ctx);

    assertBody(info, "has mentioned you");
  }
  
}
