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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
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
import org.exoplatform.social.notification.DefaultDataTest;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.provider.SocialProviderImpl;

public class SocialNotificationTestCase extends AbstractCoreTest {
  private TemplateGenerator templateGenerator;
  private IdentityManager identityManager;
  private ActivityManagerImpl activityManager;
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Space>  tearDownSpaceList;
  private SpaceServiceImpl spaceService;
  private RelationshipManagerImpl relationshipManager;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;
  
  private SocialProviderImpl socialProviderImpl = null;
  
  public static final String ACTIVITY_ID = "activityId";

  public static final String SPACE_ID    = "spaceId";

  public static final String IDENTITY_ID = "identityId";
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    templateGenerator = Utils.getService(TemplateGenerator.class);
    identityManager = Utils.getService(IdentityManager.class);
    activityManager = Utils.getService(ActivityManagerImpl.class);
    spaceService = Utils.getService(SpaceServiceImpl.class);
    relationshipManager = Utils.getService(RelationshipManagerImpl.class);
    
    assertNotNull(activityManager);

    rootIdentity = identityManager.getOrCreateIdentity("organization", "root", true);
    johnIdentity = identityManager.getOrCreateIdentity("organization", "john", true);
    maryIdentity = identityManager.getOrCreateIdentity("organization", "mary", true);
    demoIdentity = identityManager.getOrCreateIdentity("organization", "demo", true);

    assertNotNull(rootIdentity.getId());
    assertNotNull(johnIdentity.getId());
    assertNotNull(maryIdentity.getId());
    assertNotNull(demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();
    
    System.setProperty("gatein.email.domain.url", "localhost");

    if(socialProviderImpl == null) {
      socialProviderImpl = DefaultDataTest.getSocialProviderImpl(activityManager, identityManager, spaceService, templateGenerator);
    }
  }
  
  public MessageInfo buildMessageInfo(NotificationMessage message) {

    return  socialProviderImpl.buildMessageInfo(message);
  }
  
  public String buildDigestMessageInfo(List<NotificationMessage> messages) {

    return  socialProviderImpl.buildDigestMessageInfo(messages);
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
  
  public void testSaveComment() throws Exception {
    {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title");
      activity.setUserId(demoIdentity.getId());
      activityManager.saveActivity(rootIdentity, activity);
      tearDownActivityList.add(activity);
      
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      assertEquals(1, messages.size());
      
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment title");
      comment.setUserId(demoIdentity.getId());
      activityManager.saveComment(activity, comment);
      
      messages = Utils.getSocialEmailStorage().emails();
      assertEquals(1, messages.size());
      NotificationMessage message = messages.iterator().next();
      MessageInfo info = buildMessageInfo(message.setTo("demo"));
  
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
      assertEquals(rootIdentity.getId(), messages.iterator().next().getSendToUserIds().get(0));
      
      ExoSocialActivity comment2 = new ExoSocialActivityImpl();
      activity = activityManager.getActivity(activity.getId());
      comment2.setTitle("comment title 2");
      comment2.setUserId(maryIdentity.getId());
      activityManager.saveComment(activity, comment2);
      
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
    assertNotNull(activity.getId());
    
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
    
    activityManager.saveLike(activity, demoIdentity);
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
    MessageInfo info = buildMessageInfo(message.setTo(maryIdentity.getRemoteId()));

    assertEquals("You've been invited to join "+ space.getPrettyName() + " space", info.getSubject());
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
      String digest = buildDigestMessageInfo(list);

      assertEquals("Demo gtn posted on your activity stream : activity1 title 1.</br>Mary Kelly posted on your activity stream : activity2 title 2.</br>", digest);
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
      String digest = buildDigestMessageInfo(list);

      assertEquals("You've received a connection request from Root Root, John Anthony, Mary Kelly.</br>", digest);
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
      String digest = buildDigestMessageInfo(list);
      assertEquals("You have been asked to joing the following spaces: my_space_1, my_space_2, my_space_3 and 1 others.</br>", digest);
      
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
      String digest = buildDigestMessageInfo(list);

      assertEquals("The following users have asked to join the my_space_1 space: Demo gtn, John Anthony, Mary Kelly.</br>", digest);
      
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
      ExoSocialActivity act2 = new ExoSocialActivityImpl();
      act2.setTitle("hello @demo");
      activityManager.saveActivity(rootIdentity, act2);
      tearDownActivityList.add(act2);
      ExoSocialActivity act3 = new ExoSocialActivityImpl();
      act3.setTitle("hello @demo");
      activityManager.saveActivity(rootIdentity, act3);
      tearDownActivityList.add(act3);
      ExoSocialActivity act4 = new ExoSocialActivityImpl();
      act4.setTitle("hello @demo");
      activityManager.saveActivity(rootIdentity, act4);
      tearDownActivityList.add(act4);
      
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      assertEquals(5, messages.size());
      
      List<NotificationMessage> list = new ArrayList<NotificationMessage>();
      for (NotificationMessage message : messages) {
        list.add(message.setTo(demoIdentity.getRemoteId()));
      }
      String digest = buildDigestMessageInfo(list);

      assertEquals("Root Root has mentioned you in an activity : hello <a href=\"/portal/classic/profile/demo\">Demo gtn</a></br>" +
                   "Root Root has mentioned you in an activity : hello <a href=\"/portal/classic/profile/demo\">Demo gtn</a></br>" +
                   "Root Root has mentioned you in an activity : hello <a href=\"/portal/classic/profile/demo\">Demo gtn</a></br>" +
                   "Root Root has mentioned you in an activity : hello <a href=\"/portal/classic/profile/demo\">Demo gtn</a></br>" +
                   "Root Root has mentioned you in an activity : hello <a href=\"/portal/classic/profile/demo\">Demo gtn</a></br>"
      		         , digest);
      
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
