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

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.plugin.NotificationKey;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
import org.exoplatform.commons.api.notification.service.setting.NotificationPluginService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManagerImpl;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManagerImpl;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.impl.SpaceServiceImpl;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.AbstractCoreTest;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.ActivityCommentPlugin;
import org.exoplatform.social.notification.plugin.ActivityMentionPlugin;
import org.exoplatform.social.notification.plugin.LikePlugin;
import org.exoplatform.social.notification.plugin.NewUserPlugin;
import org.exoplatform.social.notification.plugin.PostActivityPlugin;
import org.exoplatform.social.notification.plugin.PostActivitySpaceStreamPlugin;
import org.exoplatform.social.notification.plugin.RelationshipRecievedRequestPlugin;
import org.exoplatform.social.notification.plugin.RequestJoinSpacePlugin;
import org.exoplatform.social.notification.plugin.SpaceInvitationPlugin;

public class SocialNotificationTestCase extends AbstractCoreTest {
  private TemplateGenerator templateGenerator;
  private IdentityManager identityManager;
  private ActivityManagerImpl activityManager;
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Space>  tearDownSpaceList;
  private SpaceServiceImpl spaceService;
  private RelationshipManagerImpl relationshipManager;
  private NotificationPluginService pluginService;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;
  
  private AbstractNotificationPlugin creatUserPlugin;
  private AbstractNotificationPlugin commentPlugin;
  private AbstractNotificationPlugin postActivityPlugin;
  private AbstractNotificationPlugin mentionPlugin;
  private AbstractNotificationPlugin likePlugin;
  private AbstractNotificationPlugin postSpaceActivityPlugin;
  private AbstractNotificationPlugin inviteToConnectPlugin;
  private AbstractNotificationPlugin invitedJoinSpacePlugin;
  private AbstractNotificationPlugin spaceJoinRequestPlugin;
  private NotificationKey creatUserKey;
  private NotificationKey commentKey;
  private NotificationKey postKey;
  private NotificationKey mentionKey;
  private NotificationKey likeKey;
  private NotificationKey postSpaceKey;
  private NotificationKey relationKey;
  private NotificationKey invitedJoinSpaceKey;
  private NotificationKey spaceJoinRequestKey;
  
  public static final String ACTIVITY_ID = "activityId";

  public static final String SPACE_ID    = "spaceId";

  public static final String IDENTITY_ID = "identityId";
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    pluginService = Utils.getService(NotificationPluginService.class);
    templateGenerator = Utils.getService(TemplateGenerator.class);
    identityManager = Utils.getService(IdentityManager.class);
    activityManager = Utils.getService(ActivityManagerImpl.class);
    spaceService = Utils.getService(SpaceServiceImpl.class);
    relationshipManager = Utils.getService(RelationshipManagerImpl.class);
    
    assertNotNull(templateGenerator);
    assertNotNull(pluginService);
    
    //init all plugins (by default, we must init them from xml config)
    creatUserPlugin = new NewUserPlugin();
    creatUserKey = new NotificationKey(creatUserPlugin);
    
    commentPlugin = new ActivityCommentPlugin();
    commentKey = new NotificationKey(commentPlugin);
    
    postActivityPlugin = new PostActivityPlugin();
    postKey = new NotificationKey(postActivityPlugin);
    
    mentionPlugin = new ActivityMentionPlugin();
    mentionKey = new NotificationKey(mentionPlugin);
    
    likePlugin = new LikePlugin();
    likeKey = new NotificationKey(likePlugin);
    
    postSpaceActivityPlugin = new PostActivitySpaceStreamPlugin();
    postSpaceKey = new NotificationKey(postSpaceActivityPlugin);
    
    inviteToConnectPlugin = new RelationshipRecievedRequestPlugin();
    relationKey = new NotificationKey(inviteToConnectPlugin);
    
    invitedJoinSpacePlugin = new SpaceInvitationPlugin();
    invitedJoinSpaceKey = new NotificationKey(invitedJoinSpacePlugin);
    
    spaceJoinRequestPlugin = new RequestJoinSpacePlugin();
    spaceJoinRequestKey = new NotificationKey(spaceJoinRequestPlugin);
    
    pluginService.add(likePlugin);
    pluginService.add(creatUserPlugin);
    pluginService.add(commentPlugin);
    pluginService.add(postActivityPlugin);
    pluginService.add(postSpaceActivityPlugin);
    pluginService.add(mentionPlugin);
    pluginService.add(inviteToConnectPlugin);
    pluginService.add(invitedJoinSpacePlugin);
    pluginService.add(spaceJoinRequestPlugin);
    
    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);
    
    // each new identity created, a notification will be raised
    Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
    assertEquals(4, messages.size());
    
    assertNotNull(rootIdentity.getId());
    assertNotNull(johnIdentity.getId());
    assertNotNull(maryIdentity.getId());
    assertNotNull(demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();
    
    System.setProperty("gatein.email.domain.url", "localhost");

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
    
    pluginService.remove(creatUserKey);
    pluginService.remove(commentKey);
    pluginService.remove(postKey);
    pluginService.remove(mentionKey);
    pluginService.remove(likeKey);
    pluginService.remove(postSpaceKey);
    pluginService.remove(relationKey);
    pluginService.remove(invitedJoinSpaceKey);
    pluginService.remove(spaceJoinRequestKey);

    super.tearDown();
  }
  
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
    
    Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
    assertEquals(1, messages.size());
    assertEquals(4, messages.iterator().next().getSendToUserIds().size());
    
    identityManager.deleteIdentity(ghostIdentity);
  }
  
  public void testSaveCommentWithMention() throws Exception {
    
    //root post an activity and mention demo
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title @demo");
    activityManager.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);

    //a notification will be send to demo
    Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
    assertEquals(1, messages.size());
    assertEquals(demoIdentity.getRemoteId(), messages.iterator().next().getSendToUserIds().get(0));
    
    //demo comment on root's activity and mention mary, root and john
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("comment title @root @john @mary");
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(activity, comment);
    
    //2 messages will be created : 1st for root to notify a comment on his activity, 2nd for root, john, mary to notify the mention's action 
    messages = Utils.getSocialEmailStorage().emails();
    assertEquals(2, messages.size());
    Iterator<NotificationMessage> iterators = messages.iterator();
    
    NotificationMessage message1 = iterators.next();
    assertEquals(1, message1.getSendToUserIds().size());
 
    NotificationContext ctx = NotificationContextImpl.DEFAULT;
    ctx.setNotificationMessage(message1.setTo("mary"));
    MessageInfo info = commentPlugin.buildMessage(ctx);
    assertEquals(demoIdentity.getProfile().getFullName() + " commented one of your activities", info.getSubject());
    assertEquals("activity title <a href=\"/portal/classic/profile/demo\">Demo gtn</a>", info.getBody());
    
    NotificationMessage message2 = iterators.next();
    List<String> users = message2.getSendToUserIds();
    assertEquals(3, users.size());
    assertEquals(rootIdentity.getRemoteId(), users.get(0));
    assertEquals(johnIdentity.getRemoteId(), users.get(1));
    assertEquals(maryIdentity.getRemoteId(), users.get(2));
    
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
      
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      assertEquals(1, messages.size());
      NotificationMessage message = messages.iterator().next();
      NotificationContext ctx = NotificationContextImpl.DEFAULT;
      ctx.setNotificationMessage(message.setTo("demo"));
      MessageInfo info = commentPlugin.buildMessage(ctx);
  
      assertEquals(demoIdentity.getProfile().getFullName() + " commented one of your activities", info.getSubject());
      assertEquals(activity.getTitle(), info.getBody());
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
      
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      assertEquals(1, messages.size());
      assertEquals(1, messages.iterator().next().getSendToUserIds().size());
      assertEquals(rootIdentity.getRemoteId(), messages.iterator().next().getSendToUserIds().get(0));
      
      ExoSocialActivity comment2 = new ExoSocialActivityImpl();
      activity = activityManager.getActivity(activity.getId());
      comment2.setTitle("comment title 2");
      comment2.setUserId(maryIdentity.getId());
      activityManager.saveComment(activity, comment2);
      
      messages = Utils.getSocialEmailStorage().emails();
      assertEquals(1, messages.size());
      assertEquals(2, messages.iterator().next().getSendToUserIds().size());
      
      //root comment on his activity, this will send notifications to others commenters but not him
      ExoSocialActivity comment3 = new ExoSocialActivityImpl();
      activity = activityManager.getActivity(activity.getId());
      comment3.setTitle("comment title 3");
      comment3.setUserId(rootIdentity.getId());
      activityManager.saveComment(activity, comment3);
      
      messages = Utils.getSocialEmailStorage().emails();
      assertEquals(1, messages.size());
      assertEquals(2, messages.iterator().next().getSendToUserIds().size());
    }
  }

  public void testSaveActivity() throws Exception {
    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title ");
    activity.setUserId(demoIdentity.getId());
    activityManager.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);
    
    assertEquals(1, Utils.getSocialEmailStorage().emails().size());
    
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
    assertEquals(2, Utils.getSocialEmailStorage().emails().size());
    
    // demo post activity on space
    Space space = getSpaceInstance(1);
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    ExoSocialActivity spaceActivity = new ExoSocialActivityImpl();
    spaceActivity.setTitle("space activity title");
    spaceActivity.setUserId(demoIdentity.getId());
    activityManager.saveActivity(spaceIdentity, spaceActivity);
    tearDownActivityList.add(spaceActivity);
    
    Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
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
    
    messages = Utils.getSocialEmailStorage().emails();
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
    assertEquals(1, Utils.getSocialEmailStorage().emails().size());
    
    activityManager.saveLike(activity, maryIdentity);
    assertEquals(1, Utils.getSocialEmailStorage().emails().size());
  }
  
  public void testInviteToConnect() throws Exception {
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    
    assertEquals(1, Utils.getSocialEmailStorage().emails().size());
    
  }
  
  public void testInvitedToJoinSpace() throws Exception {
    
    Space space = getSpaceInstance(1);
    spaceService.addInvitedUser(space, maryIdentity.getRemoteId());
    Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
    assertEquals(1, messages.size());
    NotificationMessage message = messages.iterator().next();
    NotificationContext ctx = NotificationContextImpl.DEFAULT;
    ctx.setNotificationMessage(message.setTo("mary"));
    MessageInfo info = invitedJoinSpacePlugin.buildMessage(ctx);
    assertEquals("You've been invited to join "+ space.getDisplayName() + " space", info.getSubject());
    spaceService.deleteSpace(space);
  }
  
  public void testAddPendingUser() throws Exception {
    Space space = getSpaceInstance(1);
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    
    assertEquals(1, Utils.getSocialEmailStorage().emails().size());
    spaceService.deleteSpace(space);
  }
  
  public void testBuildDigestMessage() throws Exception {
    {
      //ActivityCommentProvider
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
      
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      assertEquals(2, messages.size());
      
      List<NotificationMessage> list = new ArrayList<NotificationMessage>();
      for (NotificationMessage message : messages) {
        list.add(message.setTo(rootIdentity.getRemoteId()));
      }
      
      NotificationContext ctx = NotificationContextImpl.DEFAULT;
      ctx.setNotificationMessages(list);
      Writer writer = new StringWriter();
      commentPlugin.buildDigest(ctx, writer);

      assertEquals("<a href=\"localhost/rest/social/notifications/redirectUrl/user/demo\">Demo gtn</a> commented on your activity : <a href=\"localhost/rest/social/notifications/redirectUrl/activity/" + activity.getId() + "\">activity title</a>.</br>", writer.toString());
    }
    
    {
      //ActivityPostProvider
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
      
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      assertEquals(2, messages.size());
      
      List<NotificationMessage> list = new ArrayList<NotificationMessage>();
      for (NotificationMessage message : messages) {
        list.add(message.setTo(rootIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.DEFAULT;
      ctx.setNotificationMessages(list);
      Writer writer = new StringWriter();
      postActivityPlugin.buildDigest(ctx, writer);

      assertEquals("<a href=\"localhost/rest/social/notifications/redirectUrl/user/demo\">Demo gtn</a> posted on your activity stream : <a href=\"localhost/rest/social/notifications/redirectUrl/activity/" + activity1.getId() +"\">activity1 title 1</a>.</br>" + 
                   "<a href=\"localhost/rest/social/notifications/redirectUrl/user/mary\">Mary Kelly</a> posted on your activity stream : <a href=\"localhost/rest/social/notifications/redirectUrl/activity/" + activity2.getId() +"\">activity2 title 2</a>.</br>", writer.toString());
    }
    
    {
      //ReceiceConnectionRequest
      
      relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
      relationshipManager.inviteToConnect(johnIdentity, demoIdentity);
      relationshipManager.inviteToConnect(maryIdentity, demoIdentity);
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      
      assertEquals(3, messages.size());
      List<NotificationMessage> list = new ArrayList<NotificationMessage>();
      for (NotificationMessage message : messages) {
        list.add(message.setTo(demoIdentity.getRemoteId()));
      }
      //String digest = buildDigestMessageInfo(list);
      NotificationContext ctx = NotificationContextImpl.DEFAULT;
      ctx.setNotificationMessages(list);
      Writer writer = new StringWriter();
      inviteToConnectPlugin.buildDigest(ctx, writer);

      assertEquals("You've received a connection request from <a href=\"localhost/rest/social/notifications/redirectUrl/user/root\">Root Root</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/john\">John Anthony</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/mary\">Mary Kelly</a>.</br>", writer.toString());
    }
    
    {
      //InvitedJoinSpace
      Space space1 = getSpaceInstance(1);
      spaceService.addInvitedUser(space1, maryIdentity.getRemoteId());
      Space space2 = getSpaceInstance(2);
      spaceService.addInvitedUser(space2, maryIdentity.getRemoteId());
      Space space3 = getSpaceInstance(3);
      spaceService.addInvitedUser(space3, maryIdentity.getRemoteId());
      Space space4 = getSpaceInstance(4);
      spaceService.addInvitedUser(space4, maryIdentity.getRemoteId());
      
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      assertEquals(4, messages.size());
      List<NotificationMessage> list = new ArrayList<NotificationMessage>();
      for (NotificationMessage message : messages) {
        list.add(message.setTo(maryIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.DEFAULT;
      ctx.setNotificationMessages(list);
      Writer writer = new StringWriter();
      invitedJoinSpacePlugin.buildDigest(ctx, writer);
      String result = "You have been asked to joing the following spaces: <a href=\"localhost/rest/social/notifications/redirectUrl/space/"+space1.getId()+"\">my space 1</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/space/"+space2.getId()+"\">my space 2</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/space/"+space3.getId()+"\">my space 3</a> and <a href=\"localhost/rest/social/notifications/redirectUrl/space_invitation/null\">1</a> others.</br>";
      assertEquals(result, writer.toString());
      
      spaceService.deleteSpace(space1);
      spaceService.deleteSpace(space2);
      spaceService.deleteSpace(space3);
      spaceService.deleteSpace(space4);
    }
    
    {
      //RequestJoinSpace
      Space space = getSpaceInstance(1);
      spaceService.addPendingUser(space, maryIdentity.getRemoteId());
      spaceService.addPendingUser(space, johnIdentity.getRemoteId());
      spaceService.addPendingUser(space, demoIdentity.getRemoteId());
      
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      assertEquals(3, messages.size());
      List<NotificationMessage> list = new ArrayList<NotificationMessage>();
      for (NotificationMessage message : messages) {
        list.add(message.setTo(rootIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.DEFAULT;
      ctx.setNotificationMessages(list);
      Writer writer = new StringWriter();
      spaceJoinRequestPlugin.buildDigest(ctx, writer);
      String result = "The following users have asked to join the <a href=\"localhost/rest/social/notifications/redirectUrl/space/"+space.getId()+"\">my space 1</a> space: <a href=\"localhost/rest/social/notifications/redirectUrl/user/demo\">Demo gtn</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/john\">John Anthony</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/mary\">Mary Kelly</a>.</br>";
      assertEquals(result, writer.toString());
      
      spaceService.deleteSpace(space);
    }
    
    {
      //ActivityMentionProvider
      ExoSocialActivity act = new ExoSocialActivityImpl();
      act.setTitle("hello @demo");
      activityManager.saveActivity(rootIdentity, act);
      tearDownActivityList.add(act);
      ExoSocialActivity act1 = new ExoSocialActivityImpl();
      act1.setTitle("hello @demo");
      activityManager.saveActivity(rootIdentity, act1);
      tearDownActivityList.add(act1);
      
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      assertEquals(2, messages.size());
      
      List<NotificationMessage> list = new ArrayList<NotificationMessage>();
      for (NotificationMessage message : messages) {
        list.add(message.setTo(demoIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.DEFAULT;
      ctx.setNotificationMessages(list);
      Writer writer = new StringWriter();
      mentionPlugin.buildDigest(ctx, writer);
      String result = "<a href=\"localhost/rest/social/notifications/redirectUrl/user/root\">Root Root</a> has mentioned you in an activity : <a href=\"localhost/rest/social/notifications/redirectUrl/activity/"+act.getId()+"\">hello <a href=\"/portal/classic/profile/demo\">Demo gtn</a></a></br><a href=\"localhost/rest/social/notifications/redirectUrl/user/root\">Root Root</a> has mentioned you in an activity : <a href=\"localhost/rest/social/notifications/redirectUrl/activity/"+act1.getId()+"\">hello <a href=\"/portal/classic/profile/demo\">Demo gtn</a></a></br>";
      assertEquals(result, writer.toString());
      
    }
    
    {
      //NewUserJoinSocialIntranet
      Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
      Identity paulIdentity = identityManager.getOrCreateIdentity("organization", "paul", true);
      Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
      Identity jameIdentity = identityManager.getOrCreateIdentity("organization", "jame", true);
      
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      
      assertEquals(4, messages.size());
      List<NotificationMessage> list = new ArrayList<NotificationMessage>();
      for (NotificationMessage message : messages) {
        list.add(message.setTo(demoIdentity.getRemoteId()));
      }
      NotificationContext ctx = NotificationContextImpl.DEFAULT;
      ctx.setNotificationMessages(list);
      Writer writer = new StringWriter();
      creatUserPlugin.buildDigest(ctx, writer);
      String result = "<a href=\"localhost/rest/social/notifications/redirectUrl/user/ghost\">Ghost gtn</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/paul\">Paul gtn</a>, <a href=\"localhost/rest/social/notifications/redirectUrl/user/raul\">Raul gtn</a> and <a href=\"localhost/rest/social/notifications/redirectUrl/connections/null\">1</a> more have joined social intranet.</br>";
      assertEquals(result, writer.toString());
      
      identityManager.deleteIdentity(ghostIdentity);
      identityManager.deleteIdentity(paulIdentity);
      identityManager.deleteIdentity(raulIdentity);
      identityManager.deleteIdentity(jameIdentity);
    }
  }
  
  private Space getSpaceInstance(int number) throws Exception {
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
    String[] members = new String[] {};
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
