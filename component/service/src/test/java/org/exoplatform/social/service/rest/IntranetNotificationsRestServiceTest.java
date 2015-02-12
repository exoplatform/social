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
import java.util.Map;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.impl.SpaceServiceImpl;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class IntranetNotificationsRestServiceTest extends AbstractResourceTest {

  static private IntranetNotificationRestService notificationsRestService;
  
  private IdentityStorage identityStorage;
  private SpaceServiceImpl spaceService;
  private WebNotificationStorage notificationStorage;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    spaceService = (SpaceServiceImpl) getContainer().getComponentInstanceOfType(SpaceServiceImpl.class);
    notificationStorage = (WebNotificationStorage) getContainer().getComponentInstanceOfType(WebNotificationStorage.class);
    
    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");
    
    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    notificationsRestService = new IntranetNotificationRestService();
    registry(notificationsRestService);
  }

  public void tearDown() throws Exception {
    
    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);
    notificationStorage.remove(null);
    
    super.tearDown();

    unregistry(notificationsRestService);
  }

  public void testIgnoreInvitationToConnect() throws Exception {
    startSessionAs("root");
    
    //when there is only one notif then click on refuse, the view all link will be hidden
    ContainerResponse response = service("GET", "/social/intranet-notification/ignoreInvitationToConnect/" + johnIdentity.getRemoteId() +"/" + rootIdentity.getRemoteId() + "/" + createNotif() + "/message.json", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Map<String, Boolean> map = (Map<String, Boolean>) response.getEntity();
    assertFalse(map.get("showViewAll"));
    
    //when there are more than 1 notif, view all link will not be hidden
    createNotif();
    response = service("GET", "/social/intranet-notification/ignoreInvitationToConnect/" + johnIdentity.getRemoteId() +"/" + rootIdentity.getRemoteId() + "/" + createNotif() + "/message.json", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    map = (Map<String, Boolean>) response.getEntity();
    assertTrue(map.get("showViewAll"));
  }
  
  public void testSecurityRestService() throws Exception {
    //No user logged in
    ContainerResponse response = service("GET", "/social/intranet-notification/ignoreInvitationToConnect/" + johnIdentity.getRemoteId() +"/" + rootIdentity.getRemoteId() + "/" + createNotif() + "/message.json", "", null, null);
    assertEquals(500, response.getStatus());
    
    //login as demo
    startSessionAs("demo");
    response = service("GET", "/social/intranet-notification/ignoreInvitationToConnect/" + johnIdentity.getRemoteId() +"/" + rootIdentity.getRemoteId() + "/" + createNotif() + "/message.json", "", null, null);
    assertEquals(401, response.getStatus());
    
    //login as root
    startSessionAs("root");
    response = service("GET", "/social/intranet-notification/ignoreInvitationToConnect/" + johnIdentity.getRemoteId() +"/" + rootIdentity.getRemoteId() + "/" + createNotif() + "/message.json", "", null, null);
    assertEquals(200, response.getStatus());
  }
  
  public void testIgnoreInvitationToJoinSpace() throws Exception {
    Space space = getSpaceInstance(1);
    List<String> listMembers = Arrays.asList(space.getMembers());
    assertFalse(listMembers.contains("root"));
    List<String> listInviteds = Arrays.asList(space.getInvitedUsers());
    assertTrue(listInviteds.contains("root"));
    
    startSessionAs("root");
    ContainerResponse response = service("GET", "/social/intranet-notification/ignoreInvitationToJoinSpace/" + space.getId() +"/" + rootIdentity.getRemoteId() + "/" + createNotif() + "/message.json", "", null, null);
    
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    Map<String, Boolean> map = (Map<String, Boolean>) response.getEntity();
    assertFalse(map.get("showViewAll"));
    
    listMembers = Arrays.asList(spaceService.getSpaceById(space.getId()).getMembers());
    assertFalse(listMembers.contains("root"));
    listInviteds = Arrays.asList(spaceService.getSpaceById(space.getId()).getInvitedUsers());
    assertFalse(listInviteds.contains("root"));
    
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
  
  private String createNotif() {
    NotificationInfo info = NotificationInfo.instance();
    notificationStorage.save(info);
    return info.getId();
  }
}
