/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.service.rest;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManagerImpl;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.impl.SpaceServiceImpl;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class NotificationsRestServiceTest extends AbstractResourceTest {

  static private NotificationsRestService notificationsRestService;
  
  private IdentityStorage identityStorage;
  private ActivityManagerImpl activityManager;
  private SpaceServiceImpl spaceService;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityManager = (ActivityManagerImpl) getContainer().getComponentInstanceOfType(ActivityManagerImpl.class);
    spaceService = (SpaceServiceImpl) getContainer().getComponentInstanceOfType(SpaceServiceImpl.class);
    
    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");
    
    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    notificationsRestService = new NotificationsRestService();
    registry(notificationsRestService);
  }

  public void tearDown() throws Exception {
    
    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);
    
    super.tearDown();

    unregistry(notificationsRestService);
  }

  public void testJsonRightLink() throws Exception {
    assertNotNull(notificationsRestService);
  }
  
  public void testReplyActivity() throws Exception {
    startSessionAs("root");
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity");
    activityManager.saveActivity(rootIdentity, activity);
    ContainerResponse response = service("GET", "/social/notifications/redirectUrl/reply_activity/" + activity.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(303, response.getStatus());
    
    activityManager.deleteActivity(activity);
  }
  
  public void testViewFullDiscussion() throws Exception {
    startSessionAs("root");
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity");
    activityManager.saveActivity(rootIdentity, activity);
    ContainerResponse response = service("GET", "/social/notifications/redirectUrl/view_full_activity/" + activity.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(303, response.getStatus());
    
    activityManager.deleteActivity(activity);
  }
  
  public void testInviteToConnect() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", "/social/notifications/inviteToConnect/" + johnIdentity.getRemoteId() + "/" + rootIdentity.getRemoteId(), "", null, null);
    assertNotNull(response);
    assertEquals(303, response.getStatus());
  }
  
  public void testConfirmInvitationToConnect() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", "/social/notifications/confirmInvitationToConnect/" + johnIdentity.getRemoteId() +"/" + rootIdentity.getRemoteId(), "", null, null);
    assertNotNull(response);
    assertEquals(303, response.getStatus());
  }
  
  public void testIgnoreInvitationToConnect() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", "/social/notifications/ignoreInvitationToConnect/" + johnIdentity.getRemoteId() +"/" + rootIdentity.getRemoteId(), "", null, null);
    assertNotNull(response);
    assertEquals(303, response.getStatus());
  }
  
  public void testAcceptInvitationToJoinSpace() throws Exception {
    Space space = getSpaceInstance(1);
    List<String> listMembers = Arrays.asList(space.getMembers());
    assertFalse(listMembers.contains("root"));
    List<String> listInviteds = Arrays.asList(space.getInvitedUsers());
    assertTrue(listInviteds.contains("root"));
    
    startSessionAs("root");
    ContainerResponse response = service("GET", "/social/notifications/acceptInvitationToJoinSpace/" + space.getId() +"/" + rootIdentity.getRemoteId(), "", null, null);
    
    assertNotNull(response);
    assertEquals(303, response.getStatus());
    
    listMembers = Arrays.asList(spaceService.getSpaceById(space.getId()).getMembers());
    assertTrue(listMembers.contains("root"));
    listInviteds = Arrays.asList(spaceService.getSpaceById(space.getId()).getInvitedUsers());
    assertFalse(listInviteds.contains("root"));
    
    spaceService.deleteSpace(space.getId());
  }
  
  public void testIgnoreInvitationToJoinSpace() throws Exception {
    Space space = getSpaceInstance(1);
    List<String> listMembers = Arrays.asList(space.getMembers());
    assertFalse(listMembers.contains("root"));
    List<String> listInviteds = Arrays.asList(space.getInvitedUsers());
    assertTrue(listInviteds.contains("root"));
    
    startSessionAs("root");
    ContainerResponse response = service("GET", "/social/notifications/ignoreInvitationToJoinSpace/" + space.getId() +"/" + rootIdentity.getRemoteId(), "", null, null);
    
    assertNotNull(response);
    assertEquals(303, response.getStatus());
    
    listMembers = Arrays.asList(spaceService.getSpaceById(space.getId()).getMembers());
    assertFalse(listMembers.contains("root"));
    listInviteds = Arrays.asList(spaceService.getSpaceById(space.getId()).getInvitedUsers());
    assertFalse(listInviteds.contains("root"));
    
    spaceService.deleteSpace(space.getId());
  }
  
  public void testValidateRequestToJoinSpace() throws Exception {
    Space space = getSpaceInstance(1);
    List<String> listMembers = Arrays.asList(space.getMembers());
    assertFalse(listMembers.contains("root"));
    List<String> listPendings = Arrays.asList(space.getPendingUsers());
    assertTrue(listPendings.contains("root"));
    
    startSessionAs("root");
    ContainerResponse response = service("GET", "/social/notifications/validateRequestToJoinSpace/" + space.getId() +"/" + rootIdentity.getRemoteId(), "", null, null);
    
    assertNotNull(response);
    assertEquals(303, response.getStatus());
    
    listMembers = Arrays.asList(spaceService.getSpaceById(space.getId()).getMembers());
    assertTrue(listMembers.contains("root"));
    listPendings = Arrays.asList(spaceService.getSpaceById(space.getId()).getPendingUsers());
    assertFalse(listPendings.contains("root"));
    
    spaceService.deleteSpace(space.getId());
  }
  
  public void testRedirectUrl() throws Exception {
    startSessionAs("root");
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity");
    activityManager.saveActivity(rootIdentity, activity);
    ContainerResponse response = service("GET", "/social/notifications/redirectUrl/view_full_activity/" + activity.getId(), "", null, null);
    assertEquals(303, response.getStatus());
    
    response = service("GET", "/social/notifications/redirectUrl/user/" + demoIdentity.getRemoteId(), "", null, null);
    assertEquals(303, response.getStatus());
    
    Space space = getSpaceInstance(1);
    response = service("GET", "/social/notifications/redirectUrl/space/" + space.getId(), "", null, null);
    assertEquals(303, response.getStatus());
    
    activityManager.deleteActivity(activity);
    spaceService.deleteSpace(space.getId());
  }
  
  private Space getSpaceInstance(int number) throws Exception {
    Space space = new Space();
    space.setDisplayName("my_space_" + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/spaces/my_space_" + number);
    String[] managers = new String[] {"john"};
    String[] members = new String[] {};
    String[] invitedUsers = new String[] {"root"};
    String[] pendingUsers = new String[] {"root"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    this.spaceService.saveSpace(space, true);
    return space;
  }
}
