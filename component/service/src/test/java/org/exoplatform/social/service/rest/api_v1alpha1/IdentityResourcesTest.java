/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.service.rest.api_v1alpha1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.api_v1alpha1.ActivityResources;
import org.exoplatform.social.service.rest.api_v1alpha1.IdentityResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

/**
 * Unit Test for {@link ActivityResources}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @author <a href="http://phuonglm.net">PhuongLM</a>
 * @since Jun 16, 2011
 */
public class IdentityResourcesTest extends AbstractResourceTest {

  private final String RESOURCE_URL = "/api/social/v1-alpha1/portal/";

  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;

  private Identity rootIdentity, johnIdentity, maryIdentity, demoIdentity;

  private List<Identity> tearDownIdentityList;
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Relationship> tearDownRelationshipList;
  private List<Space> tearDownSpaceList;

  /**
   * Adds {@link ActivityResources}.
   *
   * @throws Exception
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();

    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);

    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);

    tearDownIdentityList = new ArrayList<Identity>();
    tearDownIdentityList.add(rootIdentity);
    tearDownIdentityList.add(johnIdentity);
    tearDownIdentityList.add(maryIdentity);
    tearDownIdentityList.add(demoIdentity);

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownRelationshipList = new ArrayList<Relationship>();
    tearDownSpaceList = new ArrayList<Space>();


    addResource(IdentityResources.class, null);
  }

  /**
   * Removes {@link ActivityResources}.
   *
   * @throws Exception
   */
  @Override
  public void tearDown() throws Exception {

    //Removing the relationships
    for (Relationship relationship: tearDownRelationshipList) {
      relationshipManager.delete(relationship);
    }
    
    //Removing the activitys
    for (ExoSocialActivity activity: tearDownActivityList) {
      activityManager.deleteActivity(activity);
    }

    //Removing the spaces
    for (Space space: tearDownSpaceList) {
      spaceService.deleteSpace(space);
    }
    for (Identity identity: tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
    }
    removeResource(IdentityResources.class);

    super.tearDown();
  }

  
  /**
   * Test {@link IdentityResources#getIdentityById(javax.ws.rs.core.UriInfo, String, String, String)
   */
  public void testGetIdentityByID() throws Exception{
    startSessionAs("demo");
    ContainerResponse response =
      service("GET", RESOURCE_URL+"identity/" + demoIdentity.getId() + ".json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    HashMap entityHashMap = (HashMap) response.getEntity();
    HashMap profileHashMap = (HashMap) entityHashMap.get("profile");
    
    assertEquals(demoIdentity.getId(), entityHashMap.get("id"));
    assertEquals(demoIdentity.getProviderId(), entityHashMap.get("providerId"));
    assertEquals(demoIdentity.getRemoteId(), entityHashMap.get("remoteId"));
    
    assertEquals(demoIdentity.getProfile().getFullName(), profileHashMap.get("fullName"));
    assertEquals(demoIdentity.getProfile().getAvatarUrl(), profileHashMap.get("avatarUrl"));
  }
  
  /**
   * An identity posts an activity to an identity's activity stream with a number of activities.
   *
   * @param posterIdentity the identity who posts activity
   * @param identityStream the identity who has activity stream to be posted.
   * @param number the number of activities
   */
  private void createActivities(Identity posterIdentity, Identity identityStream, int number) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setType("exosocial:core");
      activity.setTitle("title " + i);
      activity.setUserId(posterIdentity.getId());
      activity = activityManager.saveActivity(identityStream, activity);
      tearDownActivityList.add(activity);
    }
  }

  /**
   * Creates a comment to an existing activity.
   *
   * @param existingActivity the existing activity
   * @param posterIdentity the identity who comments
   * @param number the number of comments
   */
  private void createComment(ExoSocialActivity existingActivity, Identity posterIdentity, int number) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment " + i);
      comment.setUserId(posterIdentity.getId());
      activityManager.saveComment(existingActivity, comment);
      comment = activityManager.getComments(existingActivity).get(0);
    }
  }

  /**
   * Gets an instance of the space.
   *
   * @param number the number to be created
   */
  private void createSpaces(int number) {
    for (int i = 0; i < number; i++) {
      Space space = new Space();
      space.setDisplayName("my space " + number);
      space.setRegistration(Space.OPEN);
      space.setDescription("add new space " + number);
      space.setType(DefaultSpaceApplicationHandler.NAME);
      space.setVisibility(Space.PUBLIC);
      space.setRegistration(Space.VALIDATION);
      space.setPriority(Space.INTERMEDIATE_PRIORITY);
      space.setGroupId("/space/space" + number);
      String[] managers = new String[]{"demo", "tom"};
      String[] members = new String[]{"raul", "ghost", "dragon", "demo", "mary"};
      String[] invitedUsers = new String[]{"register1", "john"};
      String[] pendingUsers = new String[]{"jame", "paul", "hacker"};
      space.setInvitedUsers(invitedUsers);
      space.setPendingUsers(pendingUsers);
      space.setManagers(managers);
      space.setMembers(members);
      try {
        spaceService.saveSpace(space, true);
        tearDownSpaceList.add(space);
      } catch (SpaceException e) {
        fail("Could not create a new space");
      }
    }
  }

  /**
   * Connects 2 identities, if toConfirm = true, they're connected. If false, in pending connection type.
   *
   * @param senderIdentity the identity who sends connection request
   * @param receiverIdentity the identity who receives connnection request
   * @param beConfirmed boolean value
   */
  private void connectIdentities(Identity senderIdentity, Identity receiverIdentity, boolean beConfirmed) {
    relationshipManager.inviteToConnect(senderIdentity, receiverIdentity);
    if (beConfirmed) {
      relationshipManager.confirm(receiverIdentity, senderIdentity);
    }
    tearDownRelationshipList.add(relationshipManager.get(senderIdentity, receiverIdentity));
  }

}
