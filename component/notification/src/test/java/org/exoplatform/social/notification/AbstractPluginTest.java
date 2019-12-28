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
import java.util.Locale;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.plugin.*;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 20, 2013  
 */
public abstract class AbstractPluginTest extends AbstractCoreTest {

  protected Locale initialDefaultLocale;

  protected UserSettingService userSettingService;

  public abstract BaseNotificationPlugin getPlugin();
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    userSettingService = Utils.getService(UserSettingService.class);

    // set default locale to en (used for notification wording) to have deterministic tests
    initialDefaultLocale = Locale.getDefault();
    Locale.setDefault(new Locale("en", "US"));

    initUserSetting();
    turnON(getPlugin());
  }
  
  @Override
  protected void tearDown() throws Exception {
    //
    turnOFF(getPlugin());

    if(initialDefaultLocale != null) {
      Locale.setDefault(initialDefaultLocale);
    }

    super.tearDown();
  }

  public void destroyPlugins(BaseNotificationPlugin plugin) {
    plugin = null;
  }
  
  /**
   * It will be invoked after make Activity, Relationship and New User also.
   * 
   * Makes the notification message and retrieve from MockNotificationService
   * @return
   */
  protected NotificationInfo getNotificationInfo(String username) {
    List<NotificationInfo> list = notificationService.storeDigest(username);
    assertTrue(list.size() > 0);
    return list.get(0);
  }
  
  protected List<NotificationInfo> getNotificationInfos(String username) {
    return notificationService.storeDigest(username);
  }
  
  /**
   * Validates the Message's subject
   * @param message
   * @param validatedString
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
    assertTrue("body = '" + message.getBody() + "' \r\n doesn't contain\r\n " + includedString,
               message.getBody().indexOf(includedString) > 0);
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
  protected void assertMadeMailDigestNotifications(int number) {
    UserSetting setting = userSettingService.get(rootIdentity.getRemoteId());
    if (setting.isInDaily(getPlugin().getKey().getId())) {
      assertEquals(number, notificationService.sizeOfStoredDigest());
    }
  }

  /**
   * Asserts the number of notification what made by the plugins.
   * @param number
   */
  protected List<NotificationInfo> assertMadeMailDigestNotifications(String username, int number) {
    UserSetting setting = userSettingService.get(username);
    List<NotificationInfo> got = notificationService.storeDigest(username);
    if (setting.isActive(UserSetting.EMAIL_CHANNEL, getPlugin().getKey().getId())) {
      got = notificationService.storeInstantly(username);
      assertEquals(number, got.size());
    }
    return got;
  }

  /**
   * Asserts the number of web's notifications what made by the plugins.
   * @param number
   */
  protected void assertMadeWebNotifications(int number) {
    UserSetting setting = userSettingService.get(rootIdentity.getRemoteId());
    if (setting.isActive(WebChannel.ID, getPlugin().getKey().getId())) {
      assertEquals(number, notificationService.sizeOfWebNotifs());
    }
  }

  /**
   * Asserts the number of web's notifications what made by the plugins.
   * @param number
   */
  protected List<NotificationInfo> assertMadeWebNotifications(String username, int number) {
    UserSetting setting = userSettingService.get(username);
    List<NotificationInfo> got = notificationService.storeWebNotifs(username);
    if (setting.isActive(WebChannel.ID, getPlugin().getKey().getId())) {
      got = notificationService.storeWebNotifs(username);
      assertEquals(number, got.size());
    }
    return got;
  }
  
  /**
   * Turn on the plug in
   * @param plugin
   */
  protected void turnON(BaseNotificationPlugin plugin) {
    pluginSettingService.saveActivePlugin(UserSetting.EMAIL_CHANNEL, plugin.getId(), true);
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
  protected void turnOFF(BaseNotificationPlugin plugin) {
    pluginSettingService.saveActivePlugin(UserSetting.EMAIL_CHANNEL, plugin.getId(), false);
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
   * Makes a comment reply used for Test Case
   * 
   * @param activity
   * @param commenter
   * @param commentTitle
   * @param parentCommentId
   * @return
   */
  protected ExoSocialActivity makeCommentReply(ExoSocialActivity activity, Identity commenter, String commentTitle, String parentCommentId) {
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(commentTitle);
    comment.setUserId(commenter.getId());
    comment.setParentCommentId(parentCommentId);
    activityManager.saveComment(activity, comment);

    return comment;
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
   * Edit the comment for Test Case
   * 
   * @param activity
   * @param comment
   * @param commentTitle
   * @return
   */
  protected ExoSocialActivity editComment(ExoSocialActivity activity, ExoSocialActivity comment, String commentTitle) {
    comment.setTitle(commentTitle);
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
    userSetting.setChannelActive(UserSetting.EMAIL_CHANNEL);
    //
    userSetting.setChannelPlugins(UserSetting.EMAIL_CHANNEL, settings);
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
    userSetting.setChannelActive(UserSetting.EMAIL_CHANNEL);
    
    userSetting.setDailyPlugins(settings);
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
    userSetting.setChannelActive(UserSetting.EMAIL_CHANNEL);
    
    userSetting.setWeeklyPlugins(settings);
    userSettingService.save(userSetting);
  }
  
  /**
   * Initialize the User Setting for root
   */
  private void initUserSetting() {

    List<String> instantly = new ArrayList<String>();
    instantly.add(PostActivityPlugin.ID);
    instantly.add(ActivityCommentPlugin.ID);
    instantly.add(ActivityReplyToCommentPlugin.ID);
    instantly.add(ActivityMentionPlugin.ID);
    instantly.add(LikePlugin.ID);
    instantly.add(EditCommentPlugin.ID);
    instantly.add(EditActivityPlugin.ID);
    instantly.add(LikeCommentPlugin.ID);
    instantly.add(RequestJoinSpacePlugin.ID);
    instantly.add(SpaceInvitationPlugin.ID);
    instantly.add(RelationshipReceivedRequestPlugin.ID);
    instantly.add(PostActivitySpaceStreamPlugin.ID);

    List<String> daily = new ArrayList<String>();
    daily.add(PostActivityPlugin.ID);
    daily.add(ActivityCommentPlugin.ID);
    daily.add(ActivityReplyToCommentPlugin.ID);
    daily.add(ActivityMentionPlugin.ID);
    daily.add(LikePlugin.ID);
    daily.add(EditActivityPlugin.ID);
    daily.add(EditCommentPlugin.ID);
    daily.add(RequestJoinSpacePlugin.ID);
    daily.add(SpaceInvitationPlugin.ID);
    daily.add(RelationshipReceivedRequestPlugin.ID);
    daily.add(PostActivitySpaceStreamPlugin.ID);
    daily.add(NewUserPlugin.ID);
    
    List<String> weekly = new ArrayList<String>();
    weekly.add(PostActivityPlugin.ID);
    weekly.add(ActivityCommentPlugin.ID);
    daily.add(ActivityReplyToCommentPlugin.ID);
    weekly.add(ActivityMentionPlugin.ID);
    weekly.add(LikePlugin.ID);
    weekly.add(EditCommentPlugin.ID);
    weekly.add(EditActivityPlugin.ID);
    weekly.add(RequestJoinSpacePlugin.ID);
    weekly.add(SpaceInvitationPlugin.ID);
    weekly.add(RelationshipReceivedRequestPlugin.ID);
    weekly.add(PostActivitySpaceStreamPlugin.ID);
    
    List<String> webNotifs = new ArrayList<String>();
    webNotifs.add(NewUserPlugin.ID);
    webNotifs.add(PostActivityPlugin.ID);
    webNotifs.add(ActivityCommentPlugin.ID);
    webNotifs.add(ActivityReplyToCommentPlugin.ID);
    webNotifs.add(ActivityMentionPlugin.ID);
    webNotifs.add(LikePlugin.ID);
    webNotifs.add(EditActivityPlugin.ID);
    webNotifs.add(EditCommentPlugin.ID);
    webNotifs.add(LikeCommentPlugin.ID);
    webNotifs.add(RequestJoinSpacePlugin.ID);
    webNotifs.add(SpaceInvitationPlugin.ID);
    webNotifs.add(RelationshipReceivedRequestPlugin.ID);
    webNotifs.add(PostActivitySpaceStreamPlugin.ID);
    
    // root
    saveSetting(instantly, daily, weekly, webNotifs, rootIdentity.getRemoteId());

    // mary
    saveSetting(instantly, daily, weekly, webNotifs, maryIdentity.getRemoteId());

    // john
    saveSetting(instantly, daily, weekly, webNotifs, johnIdentity.getRemoteId());

    // demo
    saveSetting(instantly, daily, weekly, webNotifs, demoIdentity.getRemoteId());
  }

  private void saveSetting(List<String> instantly, List<String> daily, List<String> weekly, List<String> webNotifs, String userId) {
    UserSetting model = UserSetting.getInstance();
    model.setUserId(userId).setChannelActive(UserSetting.EMAIL_CHANNEL);
    model.setChannelPlugins(UserSetting.EMAIL_CHANNEL, instantly);
    model.setDailyPlugins(daily);
    model.setWeeklyPlugins(weekly);
    model.setChannelPlugins(WebChannel.ID, webNotifs);
    userSettingService.save(model);
  }
  
  protected AbstractTemplateBuilder getTemplateBuilder(NotificationContext ctx) {
    //
    AbstractChannel channel = ctx.getChannelManager().getChannel(ChannelKey.key(MailChannel.ID));
    assertNotNull(channel);
    return channel.getTemplateBuilder(ctx.getNotificationInfo().getKey());
  }
  
  /**
   * It will be invoked after the notification will be created.
   * 
   * Makes the Message Info by the plugin and NotificationContext
   * @ctx the provided NotificationContext
   * @return
   */
  protected MessageInfo buildMessageInfo(NotificationContext ctx) {
    AbstractTemplateBuilder templateBuilder = getTemplateBuilder();
    if (templateBuilder == null) {
      templateBuilder = getTemplateBuilder(ctx);
    }
    MessageInfo massage = templateBuilder.buildMessage(ctx);
    assertNotNull(massage);
    return massage;
  }

  protected void buildDigest(NotificationContext ctx, Writer writer) {
    AbstractTemplateBuilder templateBuilder = getTemplateBuilder();
    if (templateBuilder == null) {
      templateBuilder = getTemplateBuilder(ctx);
    }
    templateBuilder.buildDigest(ctx, writer);
  }

  public abstract AbstractTemplateBuilder getTemplateBuilder();
}
