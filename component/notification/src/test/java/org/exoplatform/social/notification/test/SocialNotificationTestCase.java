/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.ActivityCommentPlugin;
import org.exoplatform.social.notification.plugin.ActivityMentionPlugin;
import org.exoplatform.social.notification.plugin.LikePlugin;
import org.exoplatform.social.notification.plugin.NewUserPlugin;
import org.exoplatform.social.notification.plugin.PostActivityPlugin;
import org.exoplatform.social.notification.plugin.PostActivitySpaceStreamPlugin;
import org.exoplatform.social.notification.plugin.RelationshipReceivedRequestPlugin;
import org.exoplatform.social.notification.plugin.RequestJoinSpacePlugin;
import org.exoplatform.social.notification.plugin.SpaceInvitationPlugin;

public class SocialNotificationTestCase extends AbstractPluginTest {

  private List<ExoSocialActivity> tearDownActivityList;
  private List<Space>  tearDownSpaceList;

  private AbstractNotificationPlugin newUserPlugin;
  private AbstractNotificationPlugin commentPlugin;
  private AbstractNotificationPlugin postActivityPlugin;
  private AbstractNotificationPlugin mentionPlugin;
  private AbstractNotificationPlugin likePlugin;
  private AbstractNotificationPlugin postSpaceActivityPlugin;
  private AbstractNotificationPlugin relationshipReceivedRequestPlugin;
  private AbstractNotificationPlugin spaceInvitationPlugin;
  private AbstractNotificationPlugin requestJoinSpacePlugin;
  
  public static final String ACTIVITY_ID = "activityId";

  public static final String SPACE_ID    = "spaceId";

  public static final String IDENTITY_ID = "identityId";
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    initPlugins();
    
    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();
    
    System.setProperty("gatein.email.domain.url", "localhost");

  }
  
  @Override
  public AbstractNotificationPlugin getPlugin() {
    return null;
  }

  private void initPlugins() {
    newUserPlugin = pluginService.getPlugin(PluginKey.key(NewUserPlugin.ID));
    assertNotNull(newUserPlugin);
    commentPlugin = pluginService.getPlugin(PluginKey.key(ActivityCommentPlugin.ID));
    assertNotNull(commentPlugin);
    postActivityPlugin = pluginService.getPlugin(PluginKey.key(PostActivityPlugin.ID));
    assertNotNull(postActivityPlugin);
    mentionPlugin = pluginService.getPlugin(PluginKey.key(ActivityMentionPlugin.ID));
    assertNotNull(mentionPlugin);
    likePlugin = pluginService.getPlugin(PluginKey.key(LikePlugin.ID));
    assertNotNull(likePlugin);
    postSpaceActivityPlugin = pluginService.getPlugin(PluginKey.key(PostActivitySpaceStreamPlugin.ID));
    assertNotNull(postSpaceActivityPlugin);
    relationshipReceivedRequestPlugin = pluginService.getPlugin(PluginKey.key(RelationshipReceivedRequestPlugin.ID));
    assertNotNull(relationshipReceivedRequestPlugin);
    spaceInvitationPlugin = pluginService.getPlugin(PluginKey.key(SpaceInvitationPlugin.ID));
    assertNotNull(spaceInvitationPlugin);
    requestJoinSpacePlugin = pluginService.getPlugin(PluginKey.key(RequestJoinSpacePlugin.ID));
    assertNotNull(requestJoinSpacePlugin);
    
    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);
    
  }
  
  @Override
  protected void tearDown() throws Exception {

    for (ExoSocialActivity activity : tearDownActivityList) {
      activityManager.deleteActivity(activity.getId());
    }

    for (Space sp : tearDownSpaceList) {
      spaceService.deleteSpace(sp);
    }

    destroyPlugins();
    super.tearDown();
  }

  private void destroyPlugins() {
    newUserPlugin = null;
    commentPlugin = null;
    postActivityPlugin = null;
    mentionPlugin = null;
    likePlugin = null;
    postSpaceActivityPlugin = null;
    relationshipReceivedRequestPlugin = null;
    spaceInvitationPlugin = null;
    requestJoinSpacePlugin = null;
  }
  
  //TODO must remove this test out
  public void testGroovyTemplate() throws Exception {
    String templateText ="ifhsdiofhds ifds ifh  $abcx <%  String s= \" cong chua nho\";%> $s;";
    GroovyTemplate template = new GroovyTemplate(templateText);
    Map<String, String> binding = new HashMap<String, String>();
    binding.put("abcx", "the value of abcx ...");
    String actual = template.render(binding);
    assertNotNull(actual);
    assertTrue(actual.indexOf("the value of abcx") > 0);
  }
  
  public void testCreateNewUser() throws Exception {
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    
    Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
    assertEquals(1, messages.size());
    
    identityManager.deleteIdentity(ghostIdentity);
  }
  
  public void testSaveCommentWithMention() throws Exception {
    
    //root post an activity and mention demo
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title @demo");
    activityManager.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);

    //a notification will be send to demo
    Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
    assertEquals(1, messages.size());
    assertEquals(demoIdentity.getRemoteId(), messages.iterator().next().getSendToUserIds().get(0));
    
    //demo comment on root's activity and mention mary, root and john
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("comment title @root @john @mary");
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(activity, comment);
    
    //2 messages will be created : 1st for root to notify a comment on his activity, 2nd for root, john, mary to notify the mention's action 
    messages = notificationService.storeDigestJCR();
    assertEquals(2, messages.size());
    Iterator<NotificationInfo> iterators = messages.iterator();
    
    NotificationInfo message1 = iterators.next();
    assertEquals(1, message1.getSendToUserIds().size());
 
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(message1.setTo("mary"));
    MessageInfo info = commentPlugin.buildMessage(ctx);
    assertEquals(demoIdentity.getProfile().getFullName() + " commented one of your activities<br/>", info.getSubject());
    assertTrue(info.getBody().indexOf("New comment on your activity") > 0);
    
    NotificationInfo message2 = iterators.next();
    List<String> users = message2.getSendToUserIds();
    assertEquals(3, users.size());
    assertEquals(rootIdentity.getRemoteId(), users.get(0));
    assertEquals(johnIdentity.getRemoteId(), users.get(1));
    assertEquals(maryIdentity.getRemoteId(), users.get(2));
    
    ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(message2.setTo("mary"));
    MessageInfo info1 = mentionPlugin.buildMessage(ctx);
    assertEquals("You were mentioned by " + demoIdentity.getProfile().getFullName() + "<br/>", info1.getSubject());
    assertTrue(info1.getBody().indexOf("New mention of you") > 0);
    
  }
  
  public void testSaveComment() throws Exception {
    {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title");
      activityManager.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
      
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment title");
      comment.setUserId(demoIdentity.getId());
      activityManager.saveComment(activity, comment);
      
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
           assertEquals(1, messages.size());
      NotificationInfo message = messages.iterator().next();
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.setNotificationInfo(message.setTo("demo"));
      MessageInfo info = commentPlugin.buildMessage(ctx);
  
      assertEquals(demoIdentity.getProfile().getFullName() + " commented one of your activities<br/>", info.getSubject());
      assertTrue(info.getBody().indexOf("New comment on your activity") > 0);
    }
    
    {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title");
      activityManager.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
      
      ExoSocialActivity comment1 = new ExoSocialActivityImpl();
      activity = activityManager.getActivity(activity.getId());
      comment1.setTitle("comment title 1");
      comment1.setUserId(demoIdentity.getId());
      activityManager.saveComment(activity, comment1);
      
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
      assertEquals(1, messages.size());
      assertEquals(1, messages.iterator().next().getSendToUserIds().size());
      assertEquals(rootIdentity.getRemoteId(), messages.iterator().next().getSendToUserIds().get(0));
      
      ExoSocialActivity comment2 = new ExoSocialActivityImpl();
      activity = activityManager.getActivity(activity.getId());
      comment2.setTitle("comment title 2");
      comment2.setUserId(maryIdentity.getId());
      activityManager.saveComment(activity, comment2);
      
      messages = notificationService.storeDigestJCR();
      assertEquals(1, messages.size());
      assertEquals(2, messages.iterator().next().getSendToUserIds().size());
      
      //root comment on his activity, this will send notifications to others commenters but not him
      ExoSocialActivity comment3 = new ExoSocialActivityImpl();
      activity = activityManager.getActivity(activity.getId());
      comment3.setTitle("comment title 3");
      comment3.setUserId(rootIdentity.getId());
      activityManager.saveComment(activity, comment3);
      
      messages = notificationService.storeDigestJCR();
      assertEquals(1, messages.size());
      assertEquals(2, messages.iterator().next().getSendToUserIds().size());
    }
    
    {
      //Test case for SOC-3549
      
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title");
      activity.setUserId(demoIdentity.getId());
      activityManager.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
      
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
      assertEquals(1, messages.size());
      
      ExoSocialActivity comment1 = new ExoSocialActivityImpl();
      activity = activityManager.getActivity(activity.getId());
      comment1.setTitle("comment title 1");
      comment1.setUserId(rootIdentity.getId());
      activityManager.saveComment(activity, comment1);
      
      messages = notificationService.storeDigestJCR();
      assertEquals(1, messages.size());
      assertEquals(1, messages.iterator().next().getSendToUserIds().size());
      assertEquals(demoIdentity.getRemoteId(), messages.iterator().next().getSendToUserIds().get(0));
    }
  }

  public void testSaveActivity() throws Exception {
    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title ");
    activity.setUserId(demoIdentity.getId());
    activityManager.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);
    
       assertEquals(1, notificationService.storeDigestJCR().size());
    
    //
    ExoSocialActivity got = activityManager.getActivity(activity.getId());
    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());

    // mentions case
    ExoSocialActivity act = new ExoSocialActivityImpl();
    act.setTitle("hello @demo");
    act.setUserId(maryIdentity.getId());
    activityManager.saveActivity(rootIdentity, act);
    tearDownActivityList.add(act);
    assertNotNull(act.getId());
    assertEquals(2, notificationService.storeDigestJCR().size());
    
    // demo post activity on space
    Space space = getSpaceInstance(1);
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    ExoSocialActivity spaceActivity = new ExoSocialActivityImpl();
    spaceActivity.setTitle("space activity title");
    spaceActivity.setUserId(demoIdentity.getId());
    activityManager.saveActivity(spaceIdentity, spaceActivity);
    tearDownActivityList.add(spaceActivity);
    
    Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
    assertEquals(1, messages.size());
    assertEquals(1, messages.iterator().next().getSendToUserIds().size());
    
    //add 2 more members in space
    space.setMembers(new String[] {"mary", "john"});
    spaceService.saveSpace(space, false);
    ExoSocialActivity spaceActivity2 = new ExoSocialActivityImpl();
    spaceActivity2.setTitle("space activity2 title");
    spaceActivity2.setUserId(demoIdentity.getId());
    activityManager.saveActivity(spaceIdentity, spaceActivity2);
    tearDownActivityList.add(spaceActivity2);
    
    messages = notificationService.storeDigestJCR();
    assertEquals(1, messages.size());
    assertEquals(3, messages.iterator().next().getSendToUserIds().size());
    
    spaceService.deleteSpace(space);
  }
  
  public void testLikeActivity() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title ");
    activity.setUserId(demoIdentity.getId());
    activityManager.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);
    assertEquals(1, notificationService.storeDigestJCR().size());
    
    activityManager.saveLike(activity, maryIdentity);
    assertEquals(1, notificationService.storeDigestJCR().size());
    
  }
  
  public void testInviteToConnect() throws Exception {
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    
    Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
    assertEquals(1, messages.size());
    NotificationInfo message = messages.iterator().next();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(message.setTo("demo"));
    MessageInfo info = relationshipReceivedRequestPlugin.buildMessage(ctx);
    
    assertTrue(info.getBody().indexOf("New connection request") > 0);
  }
  
  public void testInvitedToJoinSpace() throws Exception {
    
    Space space = getSpaceInstance(1);
    spaceService.addInvitedUser(space, maryIdentity.getRemoteId());
    Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
       assertEquals(1, messages.size());
    NotificationInfo message = messages.iterator().next();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(message.setTo("mary"));
    MessageInfo info = spaceInvitationPlugin.buildMessage(ctx);
    assertEquals("You've been invited to join "+ space.getDisplayName() + " space<br/>", info.getSubject());
    spaceService.deleteSpace(space);
  }
  
  public void testAddPendingUser() throws Exception {
    Space space = getSpaceInstance(1);
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    
       assertEquals(1, notificationService.storeDigestJCR().size());
    spaceService.deleteSpace(space);
  }
  
  public void testBuildDigestMessage() throws Exception {
    {
      //ActivityCommentPlugin
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title");
      activityManager.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
      
      ExoSocialActivity comment1 = new ExoSocialActivityImpl();
      activity = activityManager.getActivity(activity.getId());
      comment1.setTitle("comment title 1");
      comment1.setUserId(demoIdentity.getId());
      activityManager.saveComment(activity, comment1);
      
      ExoSocialActivity comment2 = new ExoSocialActivityImpl();
      activity = activityManager.getActivity(activity.getId());
      comment2.setTitle("comment title 2");
      comment2.setUserId(demoIdentity.getId());
      activityManager.saveComment(activity, comment2);
      
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
           assertEquals(2, messages.size());
      
      List<NotificationInfo> list = new ArrayList<NotificationInfo>();
      for (NotificationInfo message : messages) {
        list.add(message.setTo(rootIdentity.getRemoteId()));
      }
      
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.setNotificationInfos(list);
      Writer writer = new StringWriter();
      commentPlugin.buildDigest(ctx, writer);

//      assertEquals("<a href=\"localhost/rest/social/notifications/redirectUrl/user/demo\">Demo gtn</a> commented on your activity : <a href=\"localhost/rest/social/notifications/redirectUrl/view_full_activity/" + activity.getId() + "\">activity title</a>.</br>", writer.toString());
    }
    
    {
      //PostActivityPlugin
      ExoSocialActivity activity1 = new ExoSocialActivityImpl();
      activity1.setTitle("activity1 title 1");
      activity1.setUserId(demoIdentity.getId());
      activityManager.saveActivity(rootIdentity, activity1);
      tearDownActivityList.add(activity1);
      
      ExoSocialActivity activity2 = new ExoSocialActivityImpl();
      activity2.setTitle("activity2 title 2");
      activity2.setUserId(maryIdentity.getId());
      activityManager.saveActivity(rootIdentity, activity2);
      tearDownActivityList.add(activity2);
      
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
      assertEquals(2, messages.size());
      
      List<NotificationInfo> list = new ArrayList<NotificationInfo>();
      for (NotificationInfo message : messages) {
        list.add(message.setTo(rootIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.setNotificationInfos(list);
      Writer writer = new StringWriter();
      postActivityPlugin.buildDigest(ctx, writer);

//      assertEquals("<a href=\"localhost/rest/social/notifications/redirectUrl/user/demo\">Demo gtn</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/mary\">Mary Kelly</a> posted on <a href=\"localhost/rest/social/notifications/redirectUrl/user_activity_stream/root\">your activity stream</a>.</br>", writer.toString());
    }
    
    {
      //ReceiceConnectionRequest
      
      relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
      relationshipManager.inviteToConnect(johnIdentity, demoIdentity);
      relationshipManager.inviteToConnect(maryIdentity, demoIdentity);
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
      assertEquals(3, messages.size());
      List<NotificationInfo> list = new ArrayList<NotificationInfo>();
      for (NotificationInfo message : messages) {
        list.add(message.setTo(demoIdentity.getRemoteId()));
      }
      //String digest = buildDigestMessageInfo(list);
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.setNotificationInfos(list);
      Writer writer = new StringWriter();
      relationshipReceivedRequestPlugin.buildDigest(ctx, writer);

//      assertEquals("You've received a connection request from <a href=\"localhost/rest/social/notifications/redirectUrl/user/root\">Root Root</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/john\">John Anthony</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/mary\">Mary Kelly</a>.</br>", writer.toString());
    }
    
    {
      //SpaceInvitationPlugin
      Space space1 = getSpaceInstance(1);
      spaceService.addInvitedUser(space1, maryIdentity.getRemoteId());
      Space space2 = getSpaceInstance(2);
      spaceService.addInvitedUser(space2, maryIdentity.getRemoteId());
      Space space3 = getSpaceInstance(3);
      spaceService.addInvitedUser(space3, maryIdentity.getRemoteId());
      Space space4 = getSpaceInstance(4);
      spaceService.addInvitedUser(space4, maryIdentity.getRemoteId());
      
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
      assertEquals(4, messages.size());
      List<NotificationInfo> list = new ArrayList<NotificationInfo>();
      for (NotificationInfo message : messages) {
        list.add(message.setTo(maryIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.setNotificationInfos(list);
      Writer writer = new StringWriter();
      spaceInvitationPlugin.buildDigest(ctx, writer);
      String result = "You have been asked to joing the following spaces: <a href=\"localhost/rest/social/notifications/redirectUrl/space/"+space1.getId()+"\">my space 1</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/space/"+space2.getId()+"\">my space 2</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/space/"+space3.getId()+"\">my space 3</a> and <a href=\"localhost/rest/social/notifications/redirectUrl/space_invitation/null\">1</a> others.</br>";
//      assertEquals(result, writer.toString());
      
      spaceService.deleteSpace(space1);
      spaceService.deleteSpace(space2);
      spaceService.deleteSpace(space3);
      spaceService.deleteSpace(space4);
    }
    
    {
      //RequestJoinSpacePlugin
      Space space = getSpaceInstance(1);
      spaceService.addPendingUser(space, maryIdentity.getRemoteId());
      spaceService.addPendingUser(space, johnIdentity.getRemoteId());
      spaceService.addPendingUser(space, demoIdentity.getRemoteId());
      
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
      assertEquals(3, messages.size());
      List<NotificationInfo> list = new ArrayList<NotificationInfo>();
      for (NotificationInfo message : messages) {
        list.add(message.setTo(rootIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.setNotificationInfos(list);
      Writer writer = new StringWriter();
      requestJoinSpacePlugin.buildDigest(ctx, writer);
      String result = "The following users have asked to join the <a href=\"localhost/rest/social/notifications/redirectUrl/space_members/"+space.getId()+"\">my space 1</a> space: <a href=\"localhost/rest/social/notifications/redirectUrl/user/demo\">Demo gtn</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/john\">John Anthony</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/mary\">Mary Kelly</a>.</br>";
//      assertEquals(result, writer.toString());
      
      spaceService.deleteSpace(space);
    }
    
    {
      //ActivityMentionPlugin
      ExoSocialActivity act = new ExoSocialActivityImpl();
      act.setTitle("hello @demo");
      activityManager.saveActivity(rootIdentity, act);
      tearDownActivityList.add(act);
      ExoSocialActivity act1 = new ExoSocialActivityImpl();
      act1.setTitle("hello @demo");
      activityManager.saveActivity(rootIdentity, act1);
      tearDownActivityList.add(act1);
      
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
      assertEquals(2, messages.size());
      
      List<NotificationInfo> list = new ArrayList<NotificationInfo>();
      for (NotificationInfo message : messages) {
        list.add(message.setTo(demoIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.setNotificationInfos(list);
      Writer writer = new StringWriter();
      mentionPlugin.buildDigest(ctx, writer);
      String result = "<a href=\"localhost/rest/social/notifications/redirectUrl/user/root\">Root Root</a> has mentioned you in an activity : <a href=\"localhost/rest/social/notifications/redirectUrl/view_full_activity/"+act.getId()+"\">hello <a href=\"/portal/classic/profile/demo\">Demo gtn</a></a></br><a href=\"localhost/rest/social/notifications/redirectUrl/user/root\">Root Root</a> has mentioned you in an activity : <a href=\"localhost/rest/social/notifications/redirectUrl/view_full_activity/"+act1.getId()+"\">hello <a href=\"/portal/classic/profile/demo\">Demo gtn</a></a></br>";
//      assertEquals(result, writer.toString());
      
      //mary and john post a comment for act and mention demo
      ExoSocialActivity maryComment = new ExoSocialActivityImpl();
      act = activityManager.getActivity(act.getId());
      maryComment.setTitle("hello @demo");
      maryComment.setUserId(maryIdentity.getId());
      activityManager.saveComment(act, maryComment);
      
      ExoSocialActivity johnComment = new ExoSocialActivityImpl();
      act = activityManager.getActivity(act.getId());
      johnComment.setTitle("hello @demo");
      johnComment.setUserId(johnIdentity.getId());
      activityManager.saveComment(act, johnComment);
      
      // 4 messages are created : 2 to root for comments on his activity, 2 to demo for mention him
      messages = notificationService.storeDigestJCR();
      assertEquals(4, messages.size());
      
      list = new ArrayList<NotificationInfo>();
      for (NotificationInfo message : messages) {
        if (message.getKey().getId().equals("ActivityMentionPlugin"))
          list.add(message.setTo(demoIdentity.getRemoteId()));
      }
      ctx = NotificationContextImpl.cloneInstance();
      ctx.setNotificationInfos(list);
      writer = new StringWriter();
      mentionPlugin.buildDigest(ctx, writer);
      result = "<a href=\"localhost/rest/social/notifications/redirectUrl/user/john\">John Anthony</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/mary\">Mary Kelly</a> have mentioned you in an activity : <a href=\"localhost/rest/social/notifications/redirectUrl/view_full_activity/"+act.getId()+"\">hello <a href=\"/portal/classic/profile/demo\">Demo gtn</a></a></br>";
//      assertEquals(result, writer.toString());
    }
    
    {
      //NewUserPlugin
      Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
      Identity paulIdentity = identityManager.getOrCreateIdentity("organization", "paul", true);
      Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
      Identity jameIdentity = identityManager.getOrCreateIdentity("organization", "jame", true);
      
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
      assertEquals(4, messages.size());
      List<NotificationInfo> list = new ArrayList<NotificationInfo>();
      for (NotificationInfo message : messages) {
        list.add(message.setTo(demoIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.setNotificationInfos(list);
      Writer writer = new StringWriter();
      newUserPlugin.buildDigest(ctx, writer);
      String result = "<a href=\"localhost/rest/social/notifications/redirectUrl/user/ghost\">Ghost gtn</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/paul\">Paul gtn</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/raul\">Raul gtn</a> and <a href=\"localhost/rest/social/notifications/redirectUrl/connections/null\">1</a> more have joined social intranet.</br>";
//      assertEquals(result, writer.toString());
      
      identityManager.deleteIdentity(ghostIdentity);
      identityManager.deleteIdentity(paulIdentity);
      identityManager.deleteIdentity(raulIdentity);
      identityManager.deleteIdentity(jameIdentity);
    }
    
    {
      //LikePlugin
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title");
      activityManager.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
      
      activityManager.saveLike(activity, maryIdentity);
      activityManager.saveLike(activity, johnIdentity);
      activityManager.saveLike(activity, demoIdentity);
      
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
      assertEquals(3, messages.size());
      List<NotificationInfo> list = new ArrayList<NotificationInfo>();
      for (NotificationInfo message : messages) {
        list.add(message.setTo(rootIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.setNotificationInfos(list);
      Writer writer = new StringWriter();
      likePlugin.buildDigest(ctx, writer);
//      assertEquals("<a href=\"localhost/rest/social/notifications/redirectUrl/user/demo\">Demo gtn</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/john\">John Anthony</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/mary\">Mary Kelly</a> like your activity : <a href=\"localhost/rest/social/notifications/redirectUrl/view_likers_activity/"+activity.getId()+"\">activity title</a>.</br>", writer.toString());
    }
    
    {
      //PostActivitySpaceStreamPlugin
      Space space1 = getSpaceInstance(1);
      Space space2 = getSpaceInstance(2);
      Identity spaceIdentity1 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space1.getPrettyName(), false);
      Identity spaceIdentity2 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space2.getPrettyName(), false);
      
      // demo post an activity on space1
      ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
      demoActivity.setTitle("demo activity title");
      demoActivity.setUserId(demoIdentity.getId());
      activityManager.saveActivity(spaceIdentity1, demoActivity);
      tearDownActivityList.add(demoActivity);
      
      // john and mary post an activity on space2
      ExoSocialActivity maryActivity = new ExoSocialActivityImpl();
      maryActivity.setTitle("mary activity title");
      maryActivity.setUserId(maryIdentity.getId());
      activityManager.saveActivity(spaceIdentity2, maryActivity);
      tearDownActivityList.add(maryActivity);
      
      ExoSocialActivity johnActivity = new ExoSocialActivityImpl();
      johnActivity.setTitle("john activity title");
      johnActivity.setUserId(johnIdentity.getId());
      activityManager.saveActivity(spaceIdentity2, johnActivity);
      tearDownActivityList.add(johnActivity);
      
      Collection<NotificationInfo> messages = notificationService.storeDigestJCR();
      assertEquals(3, messages.size());
      
      List<NotificationInfo> list = new ArrayList<NotificationInfo>();
      for (NotificationInfo message : messages) {
        list.add(message.setTo(rootIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.cloneInstance();
      ctx.setNotificationInfos(list);
      Writer writer = new StringWriter();
      postSpaceActivityPlugin.buildDigest(ctx, writer);
//      assertEquals("<a href=\"localhost/rest/social/notifications/redirectUrl/user/demo\">Demo gtn</a> posted in <a href=\"localhost/rest/social/notifications/redirectUrl/space/"+space1.getId()+"\">my space 1</a>.</br><a href=\"localhost/rest/social/notifications/redirectUrl/user/john\">John Anthony</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/mary\">Mary Kelly</a> posted in <a href=\"localhost/rest/social/notifications/redirectUrl/space/"+space2.getId()+"\">my space 2</a>.</br>", writer.toString());
    }
  }
  
  public void testMentionProcess() throws Exception {
    String title= "hello <a href=\"/portal/classic/profile/demo\">Demo gtn</a>"+
        ", <a href=\"/portal/classic/coucou/john\">John Smith</a>" +
        ", <a href=\"/portal/classic/profile/mary\">Mary Kelly</a>";
    String result = "hello <a href=\"localhost/portal/classic/profile/demo\">Demo gtn</a>"+
        ", <a href=\"/portal/classic/coucou/john\">John Smith</a>" +
        ", <a href=\"localhost/portal/classic/profile/mary\">Mary Kelly</a>";
    assertEquals(result, Utils.processMentions(title));
  }
  
}
