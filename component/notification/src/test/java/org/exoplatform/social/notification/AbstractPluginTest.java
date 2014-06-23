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

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.plugin.ActivityCommentPlugin;
import org.exoplatform.social.notification.plugin.ActivityMentionPlugin;
import org.exoplatform.social.notification.plugin.LikePlugin;
import org.exoplatform.social.notification.plugin.NewUserPlugin;
import org.exoplatform.social.notification.plugin.PostActivityPlugin;
import org.exoplatform.social.notification.plugin.PostActivitySpaceStreamPlugin;
import org.exoplatform.social.notification.plugin.RelationshipRecievedRequestPlugin;
import org.exoplatform.social.notification.plugin.RequestJoinSpacePlugin;
import org.exoplatform.social.notification.plugin.SpaceInvitationPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 20, 2013  
 */
public abstract class AbstractPluginTest extends AbstractCoreTest {
  
  protected UserSettingService userSettingService;
  
  protected List<ExoSocialActivity> tearDownActivityList;
  protected List<Space>  tearDownSpaceList;
  protected List<Identity>  tearDownIdentityList;
  protected List<Relationship>  tearDownRelationshipList;
  
  public abstract AbstractNotificationPlugin getPlugin();
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    userSettingService = Utils.getService(UserSettingService.class);
    
    
    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);
    
    // each new identity created, a notification will be raised
    assertNotNull(rootIdentity.getId());
    assertNotNull(johnIdentity.getId());
    assertNotNull(maryIdentity.getId());
    assertNotNull(demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();
    tearDownIdentityList = new ArrayList<Identity>();
    tearDownRelationshipList = new ArrayList<Relationship>();
    
    tearDownIdentityList.add(rootIdentity);
    tearDownIdentityList.add(johnIdentity);
    tearDownIdentityList.add(maryIdentity);
    tearDownIdentityList.add(demoIdentity);
    notificationService.clearAll();
    initUserSetting();
    turnON(getPlugin());
  }
  
  @Override
  protected void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      activityManager.deleteActivity(activity.getId());
    }

    for (Space sp : tearDownSpaceList) {
      spaceService.deleteSpace(sp);
    }
    
    for (Relationship relationship : tearDownRelationshipList) {
      relationshipManager.remove(relationship);
    }
    
    for (Identity identity : tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
    }
    
    notificationService.clearAll();
    //
    turnON(getPlugin());
    turnFeatureOn();
    
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
    List<NotificationInfo> list = notificationService.storeDigestJCR();
    assertTrue(list.size() > 0);
    return list.get(0);
  }
  
  protected List<NotificationInfo> getNotificationInfos() {
    return notificationService.storeDigestJCR();
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
  
  /**
   * Validate the digest email
   * @param writer
   * @param includedString
   */
  protected void assertDigest(Writer writer, String includedString) {
    assertEquals(includedString, writer.toString().replaceAll("\\<.*?>", ""));
  }
  

  /**
   * Asserts the number of notification what made by the plugins.
   * @param number
   */
  protected List<NotificationInfo> assertMadeNotifications(int number) {
    //get notification then clear the notification list
    UserSetting setting = userSettingService.get(rootIdentity.getRemoteId());
    List<NotificationInfo> got = notificationService.storeDigestJCR();
    if (setting.isInInstantly(getPlugin().getKey().getId())) {
      got = notificationService.storeInstantly();
      assertEquals(number, got.size());
    }
    //
    if (setting.isInDaily(getPlugin().getKey().getId())) {
      got = notificationService.storeDigestJCR();
      assertEquals(number, got.size());
    }
    
    return got;
  }
  
  /**
   * Turn on the plug in
   * @param plugin
   */
  protected void turnON(AbstractNotificationPlugin plugin) {
    pluginSettingService.savePlugin(plugin.getId(), true);
  }
  
  protected void turnFeatureOn() {
    exoFeatureService.saveActiveFeature("notification", true);
  }
  
  protected void turnFeatureOff() {
    exoFeatureService.saveActiveFeature("notification", false);
  }
  
  /**
   * Turn off the plugin
   * @param plugin
   */
  protected void turnOFF(AbstractNotificationPlugin plugin) {
    pluginSettingService.savePlugin(plugin.getId(), false);
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
    activity.setUserId(owner.getId());
    activityManager.saveActivityNoReturn(rootIdentity, activity);
    tearDownActivityList.add(activity);
    
    return activity;
  }
  
  protected Relationship makeRelationship(Identity identity1, Identity identity2) {
    Relationship relationship = relationshipManager.inviteToConnect(identity1, identity2);
    tearDownRelationshipList.add(relationship);
    return relationship;
  }
  
  protected void cancelRelationship(Identity identity1, Identity identity2) {
    Relationship relationship = relationshipManager.get(identity1, identity2);
    if (relationship != null && relationship.getStatus() == Relationship.Type.PENDING) {
      relationshipManager.delete(relationship);
      tearDownRelationshipList.remove(relationship);
    }
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
    tearDownSpaceList.add(space);
    return space;
  }
  
  /**
   * Make Instantly setting
   * @param userId 
   * @param settings the list of plugins
   */
  protected void setInstantlySettings(String userId, List<String> settings) {
    UserSetting userSetting =  userSettingService.get(userId);
    
    if (userSetting == null) {
      userSetting = UserSetting.getInstance();
      userSetting.setUserId(userId);
    }
    userSetting.setActive(true);
    //
    userSetting.setInstantlyProviders(settings);
    userSettingService.save(userSetting);
  }
  
  /**
   * Make Daily setting
   * @param userId
   * @param settings
   */
  protected void setDailySetting(String userId, List<String> settings) {
    UserSetting userSetting =  userSettingService.get(userId);
    
    if (userSetting == null) {
      userSetting = UserSetting.getInstance();
      userSetting.setUserId(userId);
    }
    userSetting.setActive(true);
    
    userSetting.setDailyProviders(settings);
    userSettingService.save(userSetting);
  }
  
  /**
   * Make Weekly setting
   * @param userId
   * @param settings
   */
  protected void setWeeklySetting(String userId, List<String> settings) {
    UserSetting userSetting =  userSettingService.get(userId);
    
    if (userSetting == null) {
      userSetting = UserSetting.getInstance();
      userSetting.setUserId(userId);
    }
    userSetting.setActive(true);
    
    userSetting.setWeeklyProviders(settings);
    userSettingService.save(userSetting);
  }
  
  /**
   * Initialize the User Setting for root
   */
  private void initUserSetting() {

    List<String> instantly = new ArrayList<String>();
    instantly.add(PostActivityPlugin.ID);
    instantly.add(ActivityCommentPlugin.ID);
    instantly.add(ActivityMentionPlugin.ID);
    instantly.add(LikePlugin.ID);
    instantly.add(RequestJoinSpacePlugin.ID);
    instantly.add(SpaceInvitationPlugin.ID);
    instantly.add(RelationshipRecievedRequestPlugin.ID);
    instantly.add(PostActivitySpaceStreamPlugin.ID);
    
    List<String> daily = new ArrayList<String>();
    daily.add(PostActivityPlugin.ID);
    daily.add(ActivityCommentPlugin.ID);
    daily.add(ActivityMentionPlugin.ID);
    daily.add(LikePlugin.ID);
    daily.add(RequestJoinSpacePlugin.ID);
    daily.add(SpaceInvitationPlugin.ID);
    daily.add(RelationshipRecievedRequestPlugin.ID);
    daily.add(PostActivitySpaceStreamPlugin.ID);
    daily.add(NewUserPlugin.ID);
    
    List<String> weekly = new ArrayList<String>();
    weekly.add(PostActivityPlugin.ID);
    weekly.add(ActivityCommentPlugin.ID);
    weekly.add(ActivityMentionPlugin.ID);
    weekly.add(LikePlugin.ID);
    weekly.add(RequestJoinSpacePlugin.ID);
    weekly.add(SpaceInvitationPlugin.ID);
    weekly.add(RelationshipRecievedRequestPlugin.ID);
    weekly.add(PostActivitySpaceStreamPlugin.ID);
    
    // root
    saveSetting(instantly, daily, weekly, rootIdentity.getRemoteId());

    // mary
    saveSetting(instantly, daily, weekly, maryIdentity.getRemoteId());

    // john
    saveSetting(instantly, daily, weekly, johnIdentity.getRemoteId());

    // demo
    saveSetting(instantly, daily, weekly, demoIdentity.getRemoteId());
  }

  private void saveSetting(List<String> instantly, List<String> daily, List<String> weekly, String userId) {
    UserSetting model = UserSetting.getInstance();
    model.setUserId(userId).setActive(true);
    model.setInstantlyProviders(instantly);
    model.setDailyProviders(daily);
    model.setWeeklyProviders(weekly);
    userSettingService.save(model);
  }
  
}
