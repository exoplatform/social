/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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
package org.exoplatform.social.service.rest.notification;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.impl.SpaceServiceImpl;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.rest.impl.relationship.RelationshipsRestResourcesV1;
import org.exoplatform.social.service.rest.IntranetNotificationRestService;
import org.exoplatform.social.service.test.AbstractResourceTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Test class for Intranet Notifications REST API
 * This test class should be in social-component-notification module (it has probably been put here to easily
 * extends org.exoplatform.social.service.test.AbstractResourceTest)
 */
public class IntranetNotificationsRestServiceTest extends AbstractResourceTest {

  private IdentityStorage identityStorage;
  private SpaceServiceImpl spaceService;
  private RelationshipManager relationshipManager;
  private WebNotificationStorage notificationStorage;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    identityStorage = getContainer().getComponentInstanceOfType(IdentityStorage.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceServiceImpl.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);
    notificationStorage = getContainer().getComponentInstanceOfType(WebNotificationStorage.class);
    
    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");
    
    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    addResource(IntranetNotificationRestService.class, null);
  }

  public void tearDown() throws Exception {
    notificationStorage.remove(null);
    
    super.tearDown();

    removeResource(IntranetNotificationRestService.class);
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
    assertEquals(401, response.getStatus());
    
    //login as demo
    startSessionAs("demo");
    response = service("GET", "/social/intranet-notification/ignoreInvitationToConnect/" + johnIdentity.getRemoteId() +"/" + rootIdentity.getRemoteId() + "/" + createNotif() + "/message.json", "", null, null);
    assertEquals(401, response.getStatus());
    
    //login as root
    startSessionAs("root");
    response = service("GET", "/social/intranet-notification/ignoreInvitationToConnect/" + johnIdentity.getRemoteId() +"/" + rootIdentity.getRemoteId() + "/" + createNotif() + "/message.json", "", null, null);
    assertEquals(200, response.getStatus());
  }

  public void testShouldConfirmInvitationToConnectWhenReceiverConfirmsTheInvitation() throws Exception {
    end();
    begin();

    // Given
    Relationship invitation = relationshipManager.inviteToConnect(johnIdentity, maryIdentity);

    startSessionAs("mary");

    // When
    ContainerResponse response = service("GET", "/social/intranet-notification/confirmInvitationToConnect/" + johnIdentity.getRemoteId() +"/" + maryIdentity.getRemoteId() + "/" + createNotif() + "/message.json", "", null, null);

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    relationshipManager.delete(invitation);
  }

  public void testShouldNotConfirmInvitationToConnectWhenSenderConfirmsTheInvitation() throws Exception {
    end();
    begin();

    // Given
    Relationship invitation = relationshipManager.inviteToConnect(maryIdentity, johnIdentity);

    startSessionAs("mary");

    // When
    ContainerResponse response = service("GET", "/social/intranet-notification/confirmInvitationToConnect/" + johnIdentity.getRemoteId() +"/" + maryIdentity.getRemoteId() + "/" + createNotif() + "/message.json", "", null, null);

    // Then
    assertNotNull(response);
    assertEquals(403, response.getStatus());

    relationshipManager.delete(invitation);
  }
  
  public void testIgnoreInvitationToJoinSpace() throws Exception {
    Space space = getSpaceInstance(1);
    List<String> listMembers = Arrays.asList(space.getMembers());
    assertFalse(listMembers.contains("root"));
    List<String> listInviteds = Arrays.asList(space.getInvitedUsers());
    assertTrue(listInviteds.contains("root"));

    end();
    begin();

    startSessionAs("root");
    ContainerResponse response = service("GET", "/social/intranet-notification/ignoreInvitationToJoinSpace/" + space.getId() +"/" + rootIdentity.getRemoteId() + "/" + createNotif() + "/message.json", "", null, null);
    
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    Map<String, Boolean> map = (Map<String, Boolean>) response.getEntity();
    assertFalse(map.get("showViewAll"));

    end();
    begin();

    listMembers = Arrays.asList(spaceService.getSpaceById(space.getId()).getMembers());
    assertFalse(listMembers.contains("root"));
    listInviteds = Arrays.asList(spaceService.getSpaceById(space.getId()).getInvitedUsers());
    assertFalse(listInviteds.contains("root"));
    
    spaceService.deleteSpace(space.getId());
  }

  public void testUnauthorizedUserAcceptInvitation() throws Exception {
    Space space = getSpaceInstance(1);

    end();
    begin();

    startSessionAs("mary");

    //Given : mary is not invited in the space
    List<String> listInvited = Arrays.asList(space.getInvitedUsers());
    assertFalse(listInvited.contains("mary"));

    //When : she call the accepts endpoint for this space
    ContainerResponse response = service("GET", "/social/intranet-notification/acceptInvitationToJoinSpace/" + space.getId() +"/" + maryIdentity.getRemoteId() + "/"+ createNotif() + "/message.json", "", null, null);

    //Then : service return forbidden
    assertEquals(403, response.getStatus());


    spaceService.deleteSpace(space.getId());

  }

  public void testAuthorizedUserAcceptInvitation() throws Exception {
    Space space = getSpaceInstance(1);

    end();
    begin();

    startSessionAs("root");

    //Given : root is invited in the space
    List<String> listInvited = Arrays.asList(space.getInvitedUsers());
    assertTrue(listInvited.contains("root"));

    //When : he call the accepts endpoint for this space
    ContainerResponse response = service("GET", "/social/intranet-notification/acceptInvitationToJoinSpace/" + space.getId() +"/" + rootIdentity.getRemoteId() + "/"+ createNotif() + "/message.json", "", null, null);

    //Then : service return status ok, and root is space member, and not more invited
    assertEquals(200, response.getStatus());

    listInvited = Arrays.asList(spaceService.getSpaceById(space.getId()).getInvitedUsers());
    assertFalse(listInvited.contains("root"));
    List<String> listMembers = Arrays.asList(spaceService.getSpaceById(space.getId()).getMembers());
    assertTrue(listMembers.contains("root"));

    spaceService.deleteSpace(space.getId());
  }


  public void testUnauthorizedUserRequestValidationByManagerWithNonInvitedUser() throws Exception {
    Space space = getSpaceInstance(1);

    end();
    begin();

    startSessionAs("john");

    //Given : Mary is not pending in this space, and john is manager of the space
    List<String> listPendings = Arrays.asList(space.getPendingUsers());
    assertFalse(listPendings.contains("mary"));

    List<String> listManager = Arrays.asList(space.getManagers());
    assertTrue(listManager.contains("john"));

    //When : he call the service to accept the request
    ContainerResponse response = service("GET", "/social/intranet-notification/validateRequestToJoinSpace/" + space.getId() +"/" + maryIdentity.getRemoteId() + "/" + johnIdentity.getRemoteId() + "/"+ createNotif() + "/message.json", "", null, null);

    //Then : service return forbidden
    assertEquals(403, response.getStatus());

    spaceService.deleteSpace(space.getId());

  }

  public void testUnauthorizedUserRequestValidationByNonManagerWithNotInvitedTargetUser() throws Exception {
    Space space = getSpaceInstance(1);

    end();
    begin();

    startSessionAs("mary");

    //Given : mary is not pending in this space, and is not manager
    List<String> listPendings = Arrays.asList(space.getPendingUsers());
    assertFalse(listPendings.contains("mary"));

    List<String> listManager = Arrays.asList(space.getManagers());
    assertFalse(listManager.contains("mary"));


    //When : she call the service to accept the request
    ContainerResponse response = service("GET", "/social/intranet-notification/validateRequestToJoinSpace/" + space.getId() +"/" + maryIdentity.getRemoteId() + "/" + maryIdentity.getRemoteId() + "/"+ createNotif() + "/message.json", "", null, null);

    //Then : service return unauthorized
    assertEquals(401, response.getStatus());

    spaceService.deleteSpace(space.getId());
  }

  public void testUnauthorizedUserRequestValidationByNonManagerWithInvitedTargetUser() throws Exception {

    //caller is not manager
    //targer user is invited

    Space space = getSpaceInstance(1);

    end();
    begin();

    startSessionAs("mary");

    //Given : mary is not manager, and root is invited
    List<String> listPendings = Arrays.asList(space.getPendingUsers());
    assertTrue(listPendings.contains("root"));

    List<String> listManager = Arrays.asList(space.getManagers());
    assertFalse(listManager.contains("mary"));


    //When : she call the service to accept the request
    ContainerResponse response = service("GET", "/social/intranet-notification/validateRequestToJoinSpace/" + space.getId() +"/" + rootIdentity.getRemoteId() + "/" + maryIdentity.getRemoteId() + "/"+ createNotif() + "/message.json", "", null, null);

    //Then : service return unauthorized
    assertEquals(401, response.getStatus());

    spaceService.deleteSpace(space.getId());
  }


  public void testAuthorizedUserRequestValidationByManager() throws Exception {
    Space space = getSpaceInstance(1);

    end();
    begin();

    startSessionAs("john");

    //Given : root is pending in this space, and john is manager
    List<String> listPendings = Arrays.asList(space.getPendingUsers());
    assertTrue(listPendings.contains("root"));

    List<String> listManager = Arrays.asList(space.getManagers());
    assertTrue(listManager.contains("john"));

    //When : he call the service to accept the request
    ContainerResponse response = service("GET", "/social/intranet-notification/validateRequestToJoinSpace/" + space.getId() +"/" + rootIdentity.getRemoteId() + "/" + johnIdentity.getRemoteId() + "/"+ createNotif() + "/message.json", "", null, null);

    //Then : service return status ok and root is added as member
    assertEquals(200, response.getStatus());


    List<String> listMembers = Arrays.asList(spaceService.getSpaceById(space.getId()).getMembers());
    assertTrue(listMembers.contains("root"));
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
    this.spaceService.createSpace(space, "john");
    return space;
  }
  
  private String createNotif() {
    NotificationInfo info = NotificationInfo.instance();
    notificationStorage.save(info);
    return info.getId();
  }
}
