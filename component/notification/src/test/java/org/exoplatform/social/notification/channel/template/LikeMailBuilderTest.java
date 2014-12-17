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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.LikePlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public class LikeMailBuilderTest extends AbstractPluginTest {
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
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(LikePlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(LikePlugin.ID));
  }
  
  @Override
  public AbstractNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(LikePlugin.ID));
  }
  
  public void testSimpleCase() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");
    
    //STEP 2 like activity
    activityManager.saveLike(activity, demoIdentity);
    
    List<NotificationInfo> list = assertMadeNotifications(1);
    NotificationInfo likeNotification = list.get(0);
    
    //STEP 3 assert Message info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(likeNotification.setTo("root"));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, "Demo gtn likes one of your activities");
    assertBody(info, "New like on your activity stream");
  }
  
  public void testDigest() throws Exception {
    ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");
    activityManager.saveLike(activity, maryIdentity);
    activityManager.saveLike(activity, demoIdentity);
    activityManager.saveLike(activity, johnIdentity);
    //
    List<NotificationInfo> list = assertMadeNotifications(3);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    list.set(0, list.get(0).setTo(rootIdentity.getRemoteId()));
    ctx.setNotificationInfos(list);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, "Mary Kelly, Demo gtn, John Anthony like your activity: root post an activity.");
  }
  
  public void testLikeLinkActivity() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("Link Activity");
    activity.setType("LINK_ACTIVITY");
    
    Map<String, String> tempParams = new LinkedHashMap<String, String>();
    tempParams.put("link", "link");
    tempParams.put("title", "link title");
    tempParams.put("comment", "message activity");
    tempParams.put("description", "description of activity");
    activity.setTemplateParams(tempParams);
    
    activityManager.saveActivityNoReturn(rootIdentity, activity);
    tearDownActivityList.add(activity);
    
    activityManager.saveLike(activity, maryIdentity);
    
    List<NotificationInfo> list = assertMadeNotifications(1);
    NotificationInfo likeNotification = list.get(0);
    
    //STEP 3 assert Message info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(likeNotification.setTo("root"));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, "Mary Kelly likes one of your activities");
    assertBody(info, "New like on your activity stream");
    assertBody(info, "link title");
    assertBody(info, "message activity");
    assertBody(info, "description of activity");
  }
  
  public void testDigestWithUnLike() throws Exception {
    // mary post activity on her stream
    ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");
    notificationService.clearAll();

    List<NotificationInfo> list = assertMadeNotifications(0);
    activityManager.saveLike(activity, demoIdentity);
    activityManager.saveLike(activity, johnIdentity);

    list = assertMadeNotifications(2);

    // john unlike
    activityManager.deleteLike(activity, johnIdentity);

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    list.set(0, list.get(0).setTo(rootIdentity.getRemoteId()));
    ctx.setNotificationInfos(list);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, "Demo gtn likes your activity: root post an activity.");
  }
}
