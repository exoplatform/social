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
import java.util.List;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.commons.api.notification.plugin.ProviderPlugin;
import org.exoplatform.commons.api.notification.service.AbstractNotificationProvider;
import org.exoplatform.commons.api.notification.service.NotificationProviderService;
import org.exoplatform.commons.api.notification.service.ProviderService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
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
    activityManager.saveActivity(rootIdentity, act);
    tearDownActivityList.add(act);
    assertNotNull(act.getId());
    assertEquals(2, Utils.getSocialEmailStorage().emails().size());
    
    // user post activity on space
  }
  
  public void testLikeActivity() throws Exception {

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title ");
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
    String[] members = new String[] {"demo"};
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
