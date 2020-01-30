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

import java.util.ArrayList;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.service.test.AbstractResourceTest;

import junit.framework.AssertionFailedError;

public class PeopleRestServiceTest extends AbstractResourceTest {
  private IdentityManager     identityManager;

  private SpaceService        spaceService;

  private RelationshipManager relationshipManager;

  private ActivityStorage     activityStorage;

  private Identity            rootIdentity;

  private Identity            demoIdentity;

  private Identity            maryIdentity;

  private Identity            johnIdentity;

  public void setUp() throws Exception {
    super.setUp();

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    activityStorage = getContainer().getComponentInstanceOfType(ActivityStorage.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);

    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", true);

    addResource(PeopleRestService.class, null);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(PeopleRestService.class);
  }

  public void testSuggestUsernames() throws Exception {
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String username = "root";
    h.putSingle("username", username);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response =
                               service("GET", "/social/people/suggest.json?nameToSearch=R&currentUser=root", "", h, null, writer);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    assertEquals("application/json;charset=utf-8", response.getContentType().toString());
    if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode())
      throw new AssertionFailedError("Service not found");
  }

  public void testUserMentionInComment() throws Exception {
    // Given
    final String TITLE = "activity on root stream";
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle(TITLE);
    activityStorage.saveActivity(demoIdentity, demoActivity);
    Relationship relationship = new Relationship(rootIdentity, maryIdentity);
    relationship.setStatus(Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship);
    MultivaluedMap<String, String> h2 = new MultivaluedMapImpl();
    String username = "root";
    h2.putSingle("username", username);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

    // When
    ContainerResponse response =
                               service("GET",
                                       "/social/people/suggest.json?nameToSearch=m&currentUser=root&typeOfRelation=mention_comment&activityId="
                                           + demoActivity.getId() + "&spaceURL=null",
                                       "",
                                       h2,
                                       null,
                                       writer);

    // Then
    assertEquals(200, response.getStatus());
    assertTrue(((ArrayList) response.getEntity()).size() == 2);

    relationshipManager.delete(relationship);
  }

  public void testUserMentionInActivityStream() throws Exception {
    // Given
    Relationship relationship = new Relationship(rootIdentity, maryIdentity);
    relationship.setStatus(Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship);
    MultivaluedMap<String, String> h3 = new MultivaluedMapImpl();
    String username = "root";
    h3.putSingle("username", username);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

    // When
    ContainerResponse response =
                               service("GET",
                                       "/social/people/suggest.json?nameToSearch=m&currentUser=root&typeOfRelation=mention_activity_stream&activityId=null&spaceURL=null",
                                       "",
                                       h3,
                                       null,
                                       writer);

    // Then
    assertEquals(200, response.getStatus());
    assertTrue(((ArrayList) response.getEntity()).size() == 2);

    relationshipManager.delete(relationship);
  }

  public void testUserMentionInSpaceComment() throws Exception {
    // Given
    Relationship relationship = new Relationship(rootIdentity, maryIdentity);
    relationship.setStatus(Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship);
    Space space = new Space();
    space.setPrettyName("space1");
    space.setDisplayName("space1");
    space.setGroupId("/platform/users");
    space.setVisibility(Space.PUBLIC);
    space.setManagers(new String[] { rootIdentity.getRemoteId() });
    String[] spaceMembers = new String[] { rootIdentity.getRemoteId(), demoIdentity.getRemoteId() };
    space.setMembers(spaceMembers);
    spaceService.createSpace(space, rootIdentity.getRemoteId());
    final String TITLE = "activity of root in the space activity stream";
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle(TITLE);
    activityStorage.saveActivity(demoIdentity, demoActivity);
    MultivaluedMap<String, String> h4 = new MultivaluedMapImpl();
    String username = "root";
    h4.putSingle("username", username);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

    // When
    ContainerResponse response =
                               service("GET",
                                       "/social/people/suggest.json?nameToSearch=m&currentUser=root&typeOfRelation=mention_comment&activityId="
                                           + demoActivity.getId() + "&spaceURL=" + space.getUrl(),
                                       "",
                                       h4,
                                       null,
                                       writer);

    // Then
    assertEquals(200, response.getStatus());
    assertTrue(((ArrayList) response.getEntity()).size() == 2);

    spaceService.deleteSpace(space);
    relationshipManager.delete(relationship);
  }

  public void testUserMentionInSpaceActivityStream() throws Exception {
    // Given
    Relationship relationship = new Relationship(rootIdentity, maryIdentity);
    relationship.setStatus(Relationship.Type.CONFIRMED);
    relationshipManager.update(relationship);
    Space space = new Space();
    space.setPrettyName("space1");
    space.setDisplayName("space1");
    space.setGroupId("/platform/users");
    space.setVisibility(Space.PUBLIC);
    space.setManagers(new String[] { rootIdentity.getRemoteId() });
    String[] spaceMembers = new String[] { rootIdentity.getRemoteId(), demoIdentity.getRemoteId() };
    space.setMembers(spaceMembers);
    spaceService.createSpace(space, rootIdentity.getRemoteId());
    MultivaluedMap<String, String> h4 = new MultivaluedMapImpl();
    String username = "root";
    h4.putSingle("username", username);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

    // When
    ContainerResponse response =
                               service("GET",
                                       "/social/people/suggest.json?nameToSearch=m&currentUser=root&typeOfRelation=mention_activity_stream&activityId=null&spaceURL="
                                           + space.getUrl(),
                                       "",
                                       h4,
                                       null,
                                       writer);

    // Then
    assertEquals(200, response.getStatus());
    assertTrue(((ArrayList) response.getEntity()).size() == 2);

    relationshipManager.delete(relationship);
    spaceService.deleteSpace(space);
  }
}
