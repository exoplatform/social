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
import org.exoplatform.social.notification.plugin.ActivityCommentPlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public class ActivityCommentMailBuilderTest extends AbstractPluginTest {
  private final static String IMAGE_HTML = "<img data-plugin-name='insertImage' src='test' />";
  private final static String ACTIVITY_TITLE_WITH_IMAGE = "my activity's title" + IMAGE_HTML + ", for today";
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
    AbstractChannel channel = manager.getChannel(ChannelKey.key(MailChannel.ID));
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
    assertMadeMailDigestNotifications(1);
    notificationService.clearAll();
    //STEP 2 add comment
    makeComment(activity, demoIdentity, COMMENT_TITLE);
    //assert equals = 2 because root is stream owner, and mary is activity's poster
    //then when add commnent need to notify to root and mary
    assertMadeMailDigestNotifications(2);

    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    NotificationInfo commentNotification = list.get(0);
    //STEP 3 assert Message info

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(commentNotification.setTo(demoIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, demoIdentity.getProfile().getFullName() + " commented one of your activities");
    assertBody(info, "New comment on your activity");
  }
  
  /**
   * Just test for simple case when you post the simple activity
   * @throws Exception
   */
  public void testSimpleCaseInlineImage() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(maryIdentity, ACTIVITY_TITLE_WITH_IMAGE);
    assertMadeMailDigestNotifications(1);
    notificationService.clearAll();
    //STEP 2 add comment
    makeComment(activity, demoIdentity, COMMENT_TITLE);
    //assert equals = 2 because root is stream owner, and mary is activity's poster
    //then when add commnent need to notify to root and mary
    assertMadeMailDigestNotifications(2);

    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    NotificationInfo commentNotification = list.get(0);
    //STEP 3 assert Message info

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(commentNotification.setTo(demoIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);

    assertSubject(info, demoIdentity.getProfile().getFullName() + " commented one of your activities");
    assertBody(info, "New comment on your activity");
    assertBody(info, SocialNotificationUtils.getImagePlaceHolder("en"));
  }
  
  /**
   * Just test for simple case when you post the simple activity
   * @throws Exception
   */
  public void testSimpleCaseTwoComments() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(maryIdentity, ACTIVITY_TITLE);
    assertMadeMailDigestNotifications(1);
    notificationService.clearAll();
    //STEP 2 add comment
    makeComment(activity, maryIdentity, COMMENT_TITLE);

    //assert equals = 1 because root is stream owner, and mary is activity's poster
    assertMadeMailDigestNotifications(1);
    List<NotificationInfo> list = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    NotificationInfo commentNotification = list.get(0);

    //STEP 3 assert Message info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(commentNotification.setTo(maryIdentity.getRemoteId()));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, maryIdentity.getProfile().getFullName() + " commented one of your activities");
    assertBody(info, "New comment on your activity");
  }
  
  public void testDigest() throws Exception {
    //mary post activity on root stream ==> notify to root
    ExoSocialActivity maryActivity = makeActivity(maryIdentity, ACTIVITY_TITLE);
    assertMadeMailDigestNotifications(1);
    notificationService.clearAll();
    
    List<NotificationInfo> toRoot = new ArrayList<NotificationInfo>();

    //demo add comment to maryActivity ==> notify to root and mary
    makeComment(maryActivity, demoIdentity, "demo add comment");
    assertMadeMailDigestNotifications(2);
    List<NotificationInfo> list1 = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    toRoot.add(list1.get(0));
    notificationService.clearAll();
    
    makeComment(activityManager.getActivity(maryActivity.getId()), johnIdentity, "john add comment");
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list2 = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    toRoot.add(list2.get(0));
    notificationService.clearAll();

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    toRoot.set(0, toRoot.get(0).setTo(rootIdentity.getRemoteId()));
    ctx.setNotificationInfos(toRoot);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, getFullName("demo") + ", " + getFullName("john") + " have commented on your activity: my activity&#39;s title post today.demo : demo add comment...");
  }
  
  public void testDigestWithDeletedComment() throws Exception {
    //mary post activity on root stream ==> notify to root
    ExoSocialActivity maryActivity = makeActivity(maryIdentity, ACTIVITY_TITLE);
    assertMadeMailDigestNotifications(1);
    notificationService.clearAll();
    
    List<NotificationInfo> toRoot = new ArrayList<NotificationInfo>();

    //demo add comment to maryActivity ==> notify to root and mary
    makeComment(maryActivity, demoIdentity, "demo add comment");
    assertMadeMailDigestNotifications(2);
    List<NotificationInfo> list1 = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    toRoot.add(list1.get(0));
    notificationService.clearAll();
    
    //john add comment to maryActivity ==> notify to root, mary and demo
    ExoSocialActivity johnComment = makeComment(activityManager.getActivity(maryActivity.getId()), johnIdentity, "john add comment");
    assertMadeMailDigestNotifications(3);
    List<NotificationInfo> list2 = assertMadeMailDigestNotifications(rootIdentity.getRemoteId(), 1);
    toRoot.add(list2.get(0));

    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(toRoot);
    Writer writer = new StringWriter();
    buildDigest(ctx, writer);
    assertDigest(writer, getFullName("demo") + ", " + getFullName("john") + " have commented on your activity: my activity&#39;s title post today.demo : demo add comment...");
    
    notificationService.clearAll();
    
    //john delete his comment
    activityManager.deleteComment(maryActivity, johnComment);
    
    assertMadeMailDigestNotifications(0);
    
  }
}
