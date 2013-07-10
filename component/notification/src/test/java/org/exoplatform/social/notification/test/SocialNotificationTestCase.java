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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.commons.api.notification.ProviderData.DIGEST_TYPE;
import org.exoplatform.commons.api.notification.plugin.ProviderPlugin;
import org.exoplatform.commons.api.notification.service.AbstractNotificationProvider;
import org.exoplatform.commons.api.notification.service.NotificationProviderService;
import org.exoplatform.commons.api.notification.service.ProviderService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.organization.OrganizationService;
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
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.notification.AbstractCoreTest;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.impl.RelationshipNotifictionImpl;
import org.exoplatform.social.notification.provider.SocialProviderImpl;

public class SocialNotificationTestCase extends AbstractCoreTest {
  private IdentityStorage identityStorage;
  private ActivityManagerImpl activityManager;
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Space>  tearDownSpaceList;
  private SpaceServiceImpl spaceService;
  private RelationshipManagerImpl relationshipManager;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;
  
  public static final String ACTIVITY_ID = "activityId";

  public static final String SPACE_ID    = "spaceId";

  public static final String IDENTITY_ID = "identityId";

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    identityStorage = Utils.getService(IdentityStorage.class);
    activityManager = Utils.getService(ActivityManagerImpl.class);
    spaceService = Utils.getService(SpaceServiceImpl.class);
    relationshipManager = (RelationshipManagerImpl) getContainer().getComponentInstanceOfType(RelationshipManagerImpl.class);
    
    assertNotNull(identityStorage);
    assertNotNull(activityManager);

    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");

    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    assertNotNull(rootIdentity.getId());
    assertNotNull(johnIdentity.getId());
    assertNotNull(maryIdentity.getId());
    assertNotNull(demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();
    
    System.setProperty("gatein.email.domain.url", "localhost");
  }
  
  public MessageInfo buildMessageInfo(NotificationMessage message) {
    
    NotificationProviderService providerService = CommonsUtils.getService(NotificationProviderService.class);
    
    SocialProviderImpl providerImpl = new SocialProviderImpl(
               activityManager,
               CommonsUtils.getService(IdentityManager.class),
               spaceService, new ProviderService() {
                
                @Override
                public void saveProvider(ProviderData provider) {
                }
                
                @Override
                public void registerProviderPlugin(ProviderPlugin providerPlugin) {
                }
                
                @Override
                public ProviderData getProvider(String providerType) {
                  ProviderData provider = new ProviderData();
                  //
                  provider.addSubject(SocialProviderImpl.DEFAULT_LANGUAGE, "$space-name $other_user_name");
                  provider.addTemplate(SocialProviderImpl.DEFAULT_LANGUAGE, "$space-name $activity_message");
                  provider.setType(providerType);
                  return provider;
                }
                
                @Override
                public List<ProviderData> getAllProviders() {
                  return null;
                }
              },
              
              CommonsUtils.getService(OrganizationService.class)
    );
    providerService.addSupportProviderImpl((AbstractNotificationProvider)providerImpl);

    return  providerImpl.buildMessageInfo(message);
  }
  
public String buildDigestMessageInfo(List<NotificationMessage> messages) {
    
    NotificationProviderService providerService = CommonsUtils.getService(NotificationProviderService.class);
    
    SocialProviderImpl providerImpl = new SocialProviderImpl(
               activityManager,
               CommonsUtils.getService(IdentityManager.class),
               spaceService, new ProviderService() {
                
                @Override
                public void saveProvider(ProviderData provider) {
                }
                
                @Override
                public void registerProviderPlugin(ProviderPlugin providerPlugin) {
                }
                
                @Override
                public ProviderData getProvider(String providerType) {
                  ProviderData provider = new ProviderData();
                  //
                  provider.addDigester(SocialProviderImpl.DEFAULT_LANGUAGE, "$user1-fullname $activity");
                  provider.addDigester(SocialProviderImpl.DEFAULT_LANGUAGE, "$user1-fullname $user2-fullname $user3-fullname $activity $space-name", DIGEST_TYPE.THREE);
                  provider.addDigester(SocialProviderImpl.DEFAULT_LANGUAGE, "$space1-name $space2-name $space3-name $number-others", DIGEST_TYPE.MORE);
                  provider.setType(providerType);
                  return provider;
                }
                
                @Override
                public List<ProviderData> getAllProviders() {
                  return null;
                }
              },
              
              CommonsUtils.getService(OrganizationService.class)
    );
    providerService.addSupportProviderImpl((AbstractNotificationProvider)providerImpl);

    return  providerImpl.buildDigestMessageInfo(messages);
  }

  @Override
  protected void tearDown() throws Exception {

    for (ExoSocialActivity activity : tearDownActivityList) {
      activityManager.deleteActivity(activity.getId());
    }

    for (Space sp : tearDownSpaceList) {
      spaceService.deleteSpace(sp);
    }

    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);

    super.tearDown();
  }
  
  public void testSaveComment() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title");
    activity.setUserId(demoIdentity.getId());
    activityManager.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);
    
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("comment title");
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(activity, comment);
    
    Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
    assertEquals(2, messages.size());
    NotificationMessage message = messages.iterator().next();
    MessageInfo info = buildMessageInfo(message.setTo("demo"));
    assertEquals("$space-name " + demoIdentity.getProfile().getFullName(), info.getSubject());
    assertEquals("$space-name " + activity.getTitle(), info.getBody());
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
    Identity spaceIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
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
    
    RelationshipNotifictionImpl relationshipNotifiction = (RelationshipNotifictionImpl) getContainer().getComponentInstanceOfType(RelationshipNotifictionImpl.class);
    relationshipManager.addListenerPlugin(relationshipNotifiction);
    
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    assertEquals(1, Utils.getSocialEmailStorage().emails().size());
    
    relationshipManager.unregisterListener(relationshipNotifiction);
  }
  
  public void testInvitedToJoinSpace() throws Exception {
    Space space = getSpaceInstance(1);
    spaceService.addInvitedUser(space, maryIdentity.getRemoteId());
    Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
    assertEquals(1, messages.size());
    NotificationMessage message = messages.iterator().next();
    MessageInfo info = buildMessageInfo(message.setTo(maryIdentity.getRemoteId()));
    assertEquals(space.getPrettyName() + " $other_user_name", info.getSubject());
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
      list.addAll(messages);
      String digest = buildDigestMessageInfo(list);
      //identity.getProfile().getFullName() is not initialized, this value will return an empty string
      assertEquals(" activity1 title 1</br> activity2 title 2</br>", digest);
    }
    
    {
      //ReceiceConnectionRequest
      RelationshipNotifictionImpl relationshipNotifiction = (RelationshipNotifictionImpl) getContainer().getComponentInstanceOfType(RelationshipNotifictionImpl.class);
      relationshipManager.addListenerPlugin(relationshipNotifiction);
      
      relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
      relationshipManager.inviteToConnect(johnIdentity, demoIdentity);
      relationshipManager.inviteToConnect(maryIdentity, demoIdentity);
      
      Collection<NotificationMessage> messages = Utils.getSocialEmailStorage().emails();
      assertEquals(3, messages.size());
      List<NotificationMessage> list = new ArrayList<NotificationMessage>();
      list.addAll(messages);
      String digest = buildDigestMessageInfo(list);
      //identity.getProfile().getFullName() is not initialized, this value will return an empty string
      assertEquals("   $activity $space-name</br>", digest);
      
      relationshipManager.unregisterListener(relationshipNotifiction);
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
      list.addAll(messages);
      String digest = buildDigestMessageInfo(list);
      assertEquals("my_space_1 my_space_2 my_space_3 1</br>", digest);
      
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
      list.addAll(messages);
      String digest = buildDigestMessageInfo(list);
      //identity.getProfile().getFullName() is not initialized, this value will return an empty string
      assertEquals("   $activity my_space_1</br>", digest);
      
      spaceService.deleteSpace(space);
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
    String[] managers = new String[] {"root"};
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
