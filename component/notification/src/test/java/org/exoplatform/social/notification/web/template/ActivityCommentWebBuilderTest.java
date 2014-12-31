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
import org.exoplatform.social.notification.plugin.ActivityCommentPlugin;

public class ActivityCommentWebBuilderTest extends AbstractPluginTest {
  private final static String ACTIVITY_TITLE = "my activity's title post today.";
  private final static String COMMENT_TITLE = "my comment's title add today.";
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
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(ActivityCommentPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(ActivityCommentPlugin.ID));
  }
  
  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(ActivityCommentPlugin.ID));
  }
  
  /**
   * Just test for simple case when you post the simple activity
   * @throws Exception
   */
  public void testSimpleCase() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(maryIdentity, ACTIVITY_TITLE);
    assertMadeWebNotifications(1);
    notificationService.clearAll();
    //STEP 2 add comment
    makeComment(activity, demoIdentity, COMMENT_TITLE);
    //assert equals = 2 because root is stream owner, and mary is activity's poster
    //then when add commnent need to notify to root and mary
    List<NotificationInfo> list = assertMadeWebNotifications(2);
    NotificationInfo commentNotification = list.get(0);
    //STEP 3 assert Message info
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(commentNotification.setTo(demoIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertBody(info, "has commented on your activity");
    assertBody(info, "my activity's title post today.");
  }
  
}
