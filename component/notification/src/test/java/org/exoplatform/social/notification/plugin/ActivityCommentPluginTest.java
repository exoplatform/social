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
 *          exo@exoplatform.com
 * Aug 20, 2013  
 */
public class ActivityCommentPluginTest extends AbstractPluginTest {
  private final static String ACTIVITY_TITLE = "my activity's title post today.";
  private final static String COMMENT_TITLE = "my comment's title add today.";
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  @Override
  public AbstractNotificationPlugin getPlugin() {
    return pluginService.getPlugin(NotificationKey.key(ActivityCommentPlugin.ID));
  }
  
  /**
   * Just test for simple case when you post the simple activity
   * @throws Exception
   */
  public void testSimpleCase() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(maryIdentity, ACTIVITY_TITLE);
    assertMadeNotifications(1);
    notificationService.clearAll();
    //STEP 2 add comment
    makeComment(activity, demoIdentity, COMMENT_TITLE);
    List<NotificationInfo> list = assertMadeNotifications(2);
    NotificationInfo commentNotification = list.get(0);
    //STEP 3 assert Message info
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(commentNotification.setTo(demoIdentity.getRemoteId()));
    MessageInfo info = getPlugin().buildMessage(ctx);
    
    assertSubject(info, demoIdentity.getProfile().getFullName() + " commented one of your activities<br/>");
    assertBody(info, "New comment on your activity");
  }
  
  /**
   * Just test for simple case when you post the simple activity
   * @throws Exception
   */
  public void testSimpleCaseTwoComments() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(maryIdentity, ACTIVITY_TITLE);
    assertMadeNotifications(1);
    notificationService.clearAll();
    //STEP 2 add comment
    makeComment(activity, maryIdentity, COMMENT_TITLE);
    List<NotificationInfo> list = assertMadeNotifications(1);
    NotificationInfo commentNotification = list.get(0);
    //STEP 3 assert Message info
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(commentNotification.setTo(maryIdentity.getRemoteId()));
    MessageInfo info = getPlugin().buildMessage(ctx);
    
    assertSubject(info, maryIdentity.getProfile().getFullName() + " commented one of your activities<br/>");
    assertBody(info, "New comment on your activity");
  }

}
