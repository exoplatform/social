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
import org.exoplatform.social.notification.plugin.LikePlugin;

public class LikeWebBuilderTest extends AbstractPluginTest {
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
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(LikePlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(LikePlugin.ID));
  }
  
  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(LikePlugin.ID));
  }
  
  public void testLikeActivity() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");
    
    //STEP 2 like activity
    activityManager.saveLike(activity, demoIdentity);
    
    assertMadeWebNotifications(1);
    List<NotificationInfo> list = assertMadeWebNotifications(rootIdentity.getRemoteId(), 1);
    NotificationInfo likeNotification = list.get(0);
    
    //STEP 3 assert Message info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(likeNotification.setTo("root"));
    MessageInfo info = buildMessageInfo(ctx);

    assertBody(info, "likes your activity");
    assertBody(info, "data-link=\"/portal/classic/activity?id=" + activity.getId() + "\"");
  }

  public void testLikeAndDislikeActivity() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");

    //STEP 2 another user comments the activity
    makeComment(activity, demoIdentity, "demo post a comment");

    assertMadeWebNotifications(0);

    //STEP 3 a third user comments the activity
    makeComment(activity, maryIdentity, "mary post a comment");
    
    assertMadeWebNotifications(0);

    //STEP 4 demo likes the activity
    activityManager.saveLike(activity, demoIdentity);

    assertMadeWebNotifications(1);
    assertMadeWebNotifications(maryIdentity.getRemoteId(), 0);
    
    // Verify web notifications for root
    List<NotificationInfo> list = assertMadeWebNotifications(rootIdentity.getRemoteId(), 1);
    NotificationInfo likeNotification = list.get(0);

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(likeNotification.setTo("root"));
    MessageInfo info = buildMessageInfo(ctx);

    assertBody(info, "likes your activity");
    assertBody(info, "data-link=\"/portal/classic/activity?id=" + activity.getId() + "\"");

    //STEP 5 mary likes the activity
    activityManager.saveLike(activity, maryIdentity);

    assertMadeWebNotifications(2);
    assertMadeWebNotifications(demoIdentity.getRemoteId(), 0);

    // Verify web notifications for root
    list = assertMadeWebNotifications(rootIdentity.getRemoteId(), 2);
    likeNotification = list.get(1);

    ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(likeNotification.setTo("root"));
    info = buildMessageInfo(ctx);

    assertBody(info, "likes your activity");
    assertBody(info, "data-link=\"/portal/classic/activity?id=" + activity.getId() + "\"");

    activityManager.deleteLike(activity, demoIdentity);
    
    // make sure no added notification for root
    assertMadeWebNotifications(2);

    activityManager.deleteLike(activity, maryIdentity);

    // make sure no added notification for root
    assertMadeWebNotifications(2);
  }
  
}
