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
package org.exoplatform.social.notification;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 20, 2013  
 */
public abstract class AbstractPluginTest extends AbstractCoreTest {
  
  protected List<ExoSocialActivity> tearDownActivityList;
  protected List<Space>  tearDownSpaceList;
  
  public abstract AbstractNotificationPlugin getPlugin();
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);
    
    // each new identity created, a notification will be raised
    notificationService.clear();
    
    assertNotNull(rootIdentity.getId());
    assertNotNull(johnIdentity.getId());
    assertNotNull(maryIdentity.getId());
    assertNotNull(demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();
    
  }
  
  @Override
  protected void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      activityManager.deleteActivity(activity.getId());
    }

    for (Space sp : tearDownSpaceList) {
      spaceService.deleteSpace(sp);
    }
    
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    
    notificationService.clear();
    
    super.tearDown();
  }
  
  public void destroyPlugins(AbstractNotificationPlugin plugin) {
    plugin = null;
  }
  
  /**
   * It will be invoked after make Activity, Relationship and New User also.
   * 
   * Makes the notification message and retrieve from MockNotificationService
   * @return
   */
  protected NotificationInfo getNotificationInfo() {
    List<NotificationInfo> list = notificationService.emails();
    assertTrue(list.size() > 0);
    return list.get(0);
  }
  
  /**
   * It will be invoked after the notification will be created.
   * 
   * Makes the Message Info by the plugin and NotificationContext
   * @ctx the provided NotificationContext
   * @return
   */
  protected MessageInfo buildMessageInfo(NotificationContext ctx) {
    AbstractNotificationPlugin plugin = getPlugin();
    MessageInfo massage = plugin.buildMessage(ctx);
    assertNotNull(massage);
    return massage;
  }
  
  /**
   * Validates the Message's subject
   * @param message
   * @param includedString
   */
  protected void assertSubject(MessageInfo message, String validatedString) {
    assertEquals(validatedString, message.getSubject());
  }
  
  /**
   * Validates the Message's body
   * @param message
   * @param includedString
   */
  protected void assertBody(MessageInfo message, String includedString) {
    assertTrue(message.getBody().indexOf(includedString) > 0);
  }
  
  protected void turnON(AbstractNotificationPlugin plugin) {
    pluginSettingService.savePlugin(plugin.getId(), true);
  }
  
  /**
   * Makes the activity for Test Case
   * @param owner
   * @param activityTitle
   * @return
   */
  protected ExoSocialActivity makeActivity(Identity owner, String activityTitle) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityManager.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);
    
    return activity;
  }
  
  /**
   * Makes the comment for Test Case
   * @param activity
   * @param commenter
   * @param commentTitle
   * @return
   */
  protected ExoSocialActivity makeComment(ExoSocialActivity activity, Identity commenter, String commentTitle) {
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(commentTitle);
    comment.setUserId(commenter.getId());
    activityManager.saveComment(activity, comment);
    tearDownActivityList.add(comment);
    
    return comment;
  }
  
  /**
   * Make space with space name
   * @param number
   * @return
   * @throws Exception
   */
  protected Space getSpaceInstance(int number) throws Exception {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setAvatarUrl("my-avatar-url");
    String[] managers = new String[] {rootIdentity.getRemoteId()};
    String[] members = new String[] {rootIdentity.getRemoteId()};
    String[] invitedUsers = new String[] {};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    space.setAvatarLastUpdated(System.currentTimeMillis());
    this.spaceService.saveSpace(space, true);
    return space;
  }
  
  
}
