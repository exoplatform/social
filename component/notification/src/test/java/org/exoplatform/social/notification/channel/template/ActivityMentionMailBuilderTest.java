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
import org.exoplatform.social.notification.plugin.ActivityMentionPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public class ActivityMentionMailBuilderTest extends AbstractPluginTest {
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
    AbstractChannel channel = manager.getChannel(ChannelKey.key(MailChannel.ID));
    assertTrue(channel != null);
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(ActivityMentionPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(ActivityMentionPlugin.ID));
  }
  
  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(ActivityMentionPlugin.ID));
  }
  
  public void testActivityMention() throws Exception {
    makeActivity(maryIdentity, "hello @john and @demo");
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(johnIdentity.getRemoteId(), 1);
    NotificationInfo mentionNotification = list.get(0);
    
    //assert Message info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(mentionNotification.setTo(demoIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, "You were mentioned by " + maryIdentity.getProfile().getFullName());
    assertBody(info, "New mention of you");
  }
  
  public void TestCommentMention() throws Exception {
    ExoSocialActivity activity = makeActivity(maryIdentity, "mary post activity on root stream");
    assertMadeMailDigestNotifications(1);
    assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    notificationService.clearAll();
    //demo post comment on mary's activity and mention to john and root
    makeComment(activity, demoIdentity, "hello @john and @root");
    //2 messages to root and mary for comment and 2 messages to root and john for mention
    assertMadeMailDigestNotifications(4);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 2);
    NotificationInfo commentNotification = list.get(3);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(commentNotification.setTo(johnIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, "You were mentioned by " + demoIdentity.getProfile().getFullName());
    assertBody(info, "New mention of you");
  }
  
  public void testDigest() throws Exception {
    List<NotificationInfo> toJohn = new ArrayList<NotificationInfo>();
    
    //mary post activity on root stream and mention john, demo
    ExoSocialActivity maryActivity = makeActivity(maryIdentity, "mary mention @john and @demo");
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(johnIdentity.getRemoteId(), 1);
    toJohn.add(list.get(0));
    notificationService.clearAll();
    
    //demo add comment to maryActivity and mention john
    makeComment(maryActivity, demoIdentity, "demo mention @john");
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list1 = assertMadeMailDigestNotifications(johnIdentity.getRemoteId(), 1);
    toJohn.add(list1.get(0));
    notificationService.clearAll();
    
    //root add comment to maryActivity and mention john
    makeComment(activityManager.getActivity(maryActivity.getId()), rootIdentity, "root mention @john");
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list2 = assertMadeMailDigestNotifications(johnIdentity.getRemoteId(), 1);
    toJohn.add(list2.get(0));
    notificationService.clearAll();

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    toJohn.set(0, toJohn.get(0).setTo(johnIdentity.getRemoteId()));
    ctx.setNotificationInfos(toJohn);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, getFullName("mary") + ", " + getFullName("demo") + ", " + getFullName("root") + " have mentioned you in an activity: mary mention " + getFullName("john") + " and " + getFullName("demo") + "");
  }
  
  public void testDigestWithDuplicateUser() throws Exception {
    List<NotificationInfo> toJohn = new ArrayList<NotificationInfo>();
    
    //mary post activity on root stream and mention john, demo
    ExoSocialActivity maryActivity = makeActivity(maryIdentity, "mary mention @john and @demo");
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(johnIdentity.getRemoteId(), 1);
    toJohn.add(list.get(0));
    notificationService.clearAll();
    
    //demo add comment to maryActivity and mention john
    makeComment(maryActivity, demoIdentity, "demo mention @john");
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list1 = assertMadeMailDigestNotifications(johnIdentity.getRemoteId(), 1);
    toJohn.add(list1.get(0));
    notificationService.clearAll();
    
    //root add comment to maryActivity and mention john
    makeComment(activityManager.getActivity(maryActivity.getId()), demoIdentity, "root mention @john");
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list2 = assertMadeMailDigestNotifications(johnIdentity.getRemoteId(), 1);
    toJohn.add(list2.get(0));
    notificationService.clearAll();

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    toJohn.set(0, toJohn.get(0).setTo(johnIdentity.getRemoteId()));
    ctx.setNotificationInfos(toJohn);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, getFullName("mary") + ", " + getFullName("demo") + " have mentioned you in an activity: mary mention " + getFullName("john") + " and " + getFullName("demo") + "");
  }
  
  public void testDigestWithDeletedActivity() throws Exception {
    List<NotificationInfo> toJohn = new ArrayList<NotificationInfo>();
    
    //mary post activity on root stream and mention john, demo
    ExoSocialActivity maryActivity = makeActivity(maryIdentity, "mary mention @john and @demo");
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(johnIdentity.getRemoteId(), 1);
    toJohn.add(list.get(0));
    notificationService.clearAll();
    
    //demo add comment to maryActivity and mention john
    ExoSocialActivity demoComment = makeComment(maryActivity, demoIdentity, "demo mention @john");
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list1 = assertMadeMailDigestNotifications(johnIdentity.getRemoteId(), 1);
    toJohn.add(list1.get(0));
    notificationService.clearAll();
    
    //root add comment to maryActivity and mention john
    makeComment(activityManager.getActivity(maryActivity.getId()), rootIdentity, "root mention @john");
    
    end();
    begin();

    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list2 = assertMadeMailDigestNotifications(johnIdentity.getRemoteId(), 1);
    toJohn.add(list2.get(0));
    notificationService.clearAll();
    
    //demo delete his comment
    activityManager.deleteComment(maryActivity, demoComment);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    toJohn.set(0, toJohn.get(0).setTo(johnIdentity.getRemoteId()));
    ctx.setNotificationInfos(toJohn);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, getFullName("mary") + ", " + getFullName("root") + " have mentioned you in an activity: mary mention " + getFullName("john") + " and " + getFullName("demo") + "");
  }
}
