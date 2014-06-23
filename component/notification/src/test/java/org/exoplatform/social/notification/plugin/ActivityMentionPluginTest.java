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

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.AbstractPluginTest;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 20, 2013  
 */
public class ActivityMentionPluginTest extends AbstractPluginTest {
  
  public void testActivityMention() throws Exception {
    //mary post activity on root stream and mention john and demo ==> 3 notifications
    makeActivity(maryIdentity, "hello @john and @demo");
    List<NotificationInfo> list = assertMadeNotifications(3);
    NotificationInfo mentionNotification = list.get(2);
    
    //assert Message info
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(mentionNotification.setTo(demoIdentity.getRemoteId()));
    MessageInfo info = getPlugin().buildMessage(ctx);
    
    assertSubject(info, "You were mentioned by " + maryIdentity.getProfile().getFullName());
    assertBody(info, "New mention of you");
  }
  
  public void testCommentMention() throws Exception {
    ExoSocialActivity activity = makeActivity(maryIdentity, "mary post activity on root stream");
    assertMadeNotifications(1);
    notificationService.clearAll();
    //demo post comment on mary's activity and mention to john and root
    makeComment(activity, demoIdentity, "hello @john and @root");
    //2 messages to root and mary for comment and 2 messages to root and john for mention
    List<NotificationInfo> list = assertMadeNotifications(4);
    NotificationInfo commentNotification = list.get(3);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(commentNotification.setTo(johnIdentity.getRemoteId()));
    MessageInfo info = getPlugin().buildMessage(ctx);
    
    assertSubject(info, "You were mentioned by " + demoIdentity.getProfile().getFullName());
    assertBody(info, "New mention of you");
  }
  
  public void testPluginONOFF() throws Exception {
    //by default the plugin is on
    
    //mary post activity on root stream and mention john and demo ==> 3 notifications
    makeActivity(maryIdentity, "first hello to @john and @demo");
    assertMadeNotifications(3);
    notificationService.clearAll();
    
    //turn of the plugin
    turnOFF(getPlugin());
    
    //mary post new activity on root stream and mention john and demo
    makeActivity(maryIdentity, "second hello to @john and @demo");
    //only one notification for create activity, mentionPlugin is off
    assertMadeNotifications(1);
    notificationService.clearAll();
    
    //turn of the plugin
    turnON(getPlugin());
    
    //mary post another new activity on root stream and mention john and demo ==> 3 notifications
    makeActivity(maryIdentity, "third hello to @john and @demo");
    //only one notification for create activity, mentionPlugin is off
    assertMadeNotifications(3);
  }
  
  public void testFeatureONOFF() throws Exception {
    //by default the plugin is on
    
    //mary post activity on root stream and mention john and demo ==> 3 notifications
    makeActivity(maryIdentity, "first hello to @john and @demo");
    assertMadeNotifications(3);
    notificationService.clearAll();
    
    //turn off the feature
    turnFeatureOff();
    
    //mary post new activity on root stream and mention john and demo
    makeActivity(maryIdentity, "second hello to @john and @demo");
    //no notification all plugins are off
    assertMadeNotifications(0);
    
    //turn on the feature
    turnFeatureOn();
    
    //mary post another new activity on root stream and mention john and demo ==> 3 notifications
    makeActivity(maryIdentity, "third hello to @john and @demo");
    //only one notification for create activity, mentionPlugin is off
    assertMadeNotifications(3);
  }
  
  public void testDigest() throws Exception {
    List<NotificationInfo> toJohn = new ArrayList<NotificationInfo>();
    
    //mary post activity on root stream and mention john, demo
    ExoSocialActivity maryActivity = makeActivity(maryIdentity, "mary mention @john and @demo");
    List<NotificationInfo> list = assertMadeNotifications(3);
    toJohn.add(list.get(1));
    notificationService.clearAll();
    
    //demo add comment to maryActivity and mention john
    makeComment(maryActivity, demoIdentity, "demo mention @john");
    List<NotificationInfo> list1 = assertMadeNotifications(3);
    toJohn.add(list1.get(2));
    notificationService.clearAll();
    
    //root add comment to maryActivity and mention john
    makeComment(activityManager.getActivity(maryActivity.getId()), rootIdentity, "root mention @john");
    List<NotificationInfo> list2 = assertMadeNotifications(3);
    toJohn.add(list2.get(2));
    notificationService.clearAll();
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    toJohn.set(0, toJohn.get(0).setTo(johnIdentity.getRemoteId()));
    ctx.setNotificationInfos(toJohn);
    Writer writer = new StringWriter();
    getPlugin().buildDigest(ctx, writer);
    assertDigest(writer, "Mary Kelly, Demo gtn, Root Root have mentioned you in an activity: mary mention John Anthony and Demo gtn.");
  }
  
  public void testDigestWithDuplicateUser() throws Exception {
    List<NotificationInfo> toJohn = new ArrayList<NotificationInfo>();
    
    //mary post activity on root stream and mention john, demo
    ExoSocialActivity maryActivity = makeActivity(maryIdentity, "mary mention @john and @demo");
    List<NotificationInfo> list = assertMadeNotifications(3);
    toJohn.add(list.get(1));
    notificationService.clearAll();
    
    //demo add comment to maryActivity and mention john
    makeComment(maryActivity, demoIdentity, "demo mention @john");
    List<NotificationInfo> list1 = assertMadeNotifications(3);
    toJohn.add(list1.get(2));
    notificationService.clearAll();
    
    //root add comment to maryActivity and mention john
    makeComment(activityManager.getActivity(maryActivity.getId()), demoIdentity, "root mention @john");
    List<NotificationInfo> list2 = assertMadeNotifications(3);
    toJohn.add(list2.get(2));
    notificationService.clearAll();
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    toJohn.set(0, toJohn.get(0).setTo(johnIdentity.getRemoteId()));
    ctx.setNotificationInfos(toJohn);
    Writer writer = new StringWriter();
    getPlugin().buildDigest(ctx, writer);
    assertDigest(writer, "Mary Kelly, Demo gtn have mentioned you in an activity: mary mention John Anthony and Demo gtn.");
  }
  
  public void testDigestWithDeletedActivity() throws Exception {
    List<NotificationInfo> toJohn = new ArrayList<NotificationInfo>();
    
    //mary post activity on root stream and mention john, demo
    ExoSocialActivity maryActivity = makeActivity(maryIdentity, "mary mention @john and @demo");
    List<NotificationInfo> list = assertMadeNotifications(3);
    toJohn.add(list.get(1));
    notificationService.clearAll();
    
    //demo add comment to maryActivity and mention john
    ExoSocialActivity demoComment = makeComment(maryActivity, demoIdentity, "demo mention @john");
    List<NotificationInfo> list1 = assertMadeNotifications(3);
    toJohn.add(list1.get(2));
    notificationService.clearAll();
    
    //root add comment to maryActivity and mention john
    makeComment(activityManager.getActivity(maryActivity.getId()), rootIdentity, "root mention @john");
    List<NotificationInfo> list2 = assertMadeNotifications(3);
    toJohn.add(list2.get(2));
    notificationService.clearAll();
    
    //demo delete his comment
    activityManager.deleteActivity(demoComment);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    toJohn.set(0, toJohn.get(0).setTo(johnIdentity.getRemoteId()));
    ctx.setNotificationInfos(toJohn);
    Writer writer = new StringWriter();
    getPlugin().buildDigest(ctx, writer);
    assertDigest(writer, "Mary Kelly, Root Root have mentioned you in an activity: mary mention John Anthony and Demo gtn.");
  }

  @Override
  public AbstractNotificationPlugin getPlugin() {
    return pluginService.getPlugin(NotificationKey.key(ActivityMentionPlugin.ID));
  }

}
