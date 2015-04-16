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
package org.exoplatform.social.core.manager;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.test.AbstractCoreTest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Unit Tests for {@link RelationshipManager}
 *
 */
public class RelationshipManagerTest extends AbstractCoreTest {
  private RelationshipManager relationshipManager;
  private IdentityManager identityManager;

  private Identity rootIdentity,
                   johnIdentity,
                   maryIdentity,
                   demoIdentity,
                   ghostIdentity,
                   paulIdentity;

  private List<Relationship> tearDownRelationshipList;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    tearDownRelationshipList = new ArrayList<Relationship>();
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull("relationshipManager must not be null", relationshipManager);
    assertNotNull("identityManager must not be null", identityManager);
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");
    ghostIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "ghost");
    paulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul");
  }

  @Override
  protected void tearDown() throws Exception {
    for (Relationship relationship : tearDownRelationshipList) {
      relationshipManager.remove(relationship);
    }

    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(paulIdentity);

    super.tearDown();
  }

  /**
   * Test {@link RelationshipManager#getAll(Identity)}
   * 
   * @throws Exception
   */
  public void testGetAll() throws Exception {
    relationshipManager.invite(johnIdentity, demoIdentity);
    List<Relationship> senderRelationships = relationshipManager.getAll(johnIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAll(demoIdentity);

    assertEquals(1, senderRelationships.size());
    assertEquals(1, receiverRelationships.size());

    tearDownRelationshipList.addAll(senderRelationships);
  }

  /**
   * Test {@link RelationshipManager#getAll(Identity, List)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetAllWithListIdentities() throws Exception {
    List<Identity> listIdentities = new ArrayList<Identity>();
    listIdentities.add(rootIdentity);
    listIdentities.add(demoIdentity);
    listIdentities.add(johnIdentity);
    listIdentities.add(maryIdentity);
    
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    List<Relationship> rootRelationships = relationshipManager.getAll(rootIdentity, listIdentities);
    assertNotNull("rootRelationships must not be null", rootRelationships);
    assertEquals("rootRelationships.size() must return: 3", 3, rootRelationships.size());
    
    List<Relationship> maryRelationships = relationshipManager.getAll(maryIdentity, listIdentities);
    assertNotNull("maryRelationships must not be null", maryRelationships);
    assertEquals("maryRelationships.size() mut return: 1", 1, maryRelationships.size());
    
    List<Relationship> johnRelationships = relationshipManager.getAll(johnIdentity, listIdentities);
    assertNotNull("johnRelationships must not be null", johnRelationships);
    assertEquals("johnRelationships.size() mut return: 1", 1, johnRelationships.size());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getAll(Identity, Type, List)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetAllWithTypeAndListIdentities() throws Exception {
    List<Identity> listIdentities = new ArrayList<Identity>();
    listIdentities.add(rootIdentity);
    listIdentities.add(demoIdentity);
    listIdentities.add(johnIdentity);
    listIdentities.add(maryIdentity);
    
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    List<Relationship> rootPendingRelationships = relationshipManager.getAll(rootIdentity, Relationship.Type.PENDING, listIdentities);
    assertNotNull("rootPendingRelationships must not be null", rootPendingRelationships);
    assertEquals("rootPendingRelationships.size() must return: 3", 3, rootPendingRelationships.size());
    
    List<Relationship> maryPendingRelationships = relationshipManager.getAll(maryIdentity, Relationship.Type.PENDING, listIdentities);
    assertNotNull("maryPendingRelationships must not be null", maryPendingRelationships);
    assertEquals("maryPendingRelationships.size() mut return: 1", 1, maryPendingRelationships.size());
    
    List<Relationship> johnPendingRelationships = relationshipManager.getAll(maryIdentity, Relationship.Type.PENDING, listIdentities);
    assertNotNull("johnPendingRelationships must not be null", johnPendingRelationships);
    assertEquals("johnPendingRelationships.size() mut return: 1", 1, johnPendingRelationships.size());
    
    relationshipManager.confirm(demoIdentity, rootIdentity);
    
    List<Relationship> rootConfirmedRelationships = relationshipManager.getAll(rootIdentity, Relationship.Type.CONFIRMED, listIdentities);
    assertNotNull("rootConfirmedRelationships must not be null", rootConfirmedRelationships);
    assertEquals("rootConfirmedRelationships.size() must return: 1", 1, rootConfirmedRelationships.size());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#get(Identity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGet() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    rootToDemoRelationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull("rootToDemoRelationship must not be null", rootToDemoRelationship);
    assertEquals("rootToDemoRelationship.getSender() must return: " + rootIdentity, rootIdentity, rootToDemoRelationship.getSender());
    assertEquals("rootToDemoRelationship.getReceiver() must return: " + demoIdentity, demoIdentity, rootToDemoRelationship.getReceiver());
    assertEquals("rootToDemoRelationship.getStatus() must return: " + Relationship.Type.PENDING, 
                 Relationship.Type.PENDING, rootToDemoRelationship.getStatus());
    
    relationshipManager.confirm(johnIdentity, rootIdentity);
    rootToJohnRelationship = relationshipManager.get(johnIdentity, rootIdentity);
    assertEquals("rootToJohnRelationship.getStatus() must return: ", Relationship.Type.CONFIRMED, 
                 rootToJohnRelationship.getStatus());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#get(String)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetWithRelationshipId() throws Exception {
    Relationship relationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    String relationshipId = relationship.getId();
    
    relationshipManager.confirm(johnIdentity, rootIdentity);
    relationship = relationshipManager.get(relationship.getId());
    assertNotNull("relationship must not be null", relationship);
    assertEquals("relationship.getStatus() must return: " + Relationship.Type.CONFIRMED, Relationship.Type.CONFIRMED, relationship.getStatus());
    
    relationshipManager.delete(relationship);

    relationship = relationshipManager.get(rootIdentity, johnIdentity);
    assertNull("relationship must be null", relationship);
    
    relationship = relationshipManager.get(relationshipId);
    assertNull("relationship must be null", relationship);
  }
  
  /**
   * Test {@link RelationshipManager#update(Relationship)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testUpdate() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    assertEquals("rootToDemoRelationship.getStatus() must return: " + Relationship.Type.PENDING,
                 Relationship.Type.PENDING, rootToDemoRelationship.getStatus());
    rootToDemoRelationship.setStatus(Relationship.Type.CONFIRMED);
    relationshipManager.update(rootToDemoRelationship);
    rootToDemoRelationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertEquals("rootToDemoRelationship.getStatus() must return: " + Relationship.Type.CONFIRMED,
                 Relationship.Type.CONFIRMED, rootToDemoRelationship.getStatus());
    
    assertEquals("maryToRootRelationship.getStatus() must return: " + Relationship.Type.PENDING,
                 Relationship.Type.PENDING, maryToRootRelationship.getStatus());
    maryToRootRelationship.setStatus(Relationship.Type.IGNORED);
    relationshipManager.update(maryToRootRelationship);
    maryToRootRelationship = relationshipManager.get(maryIdentity, rootIdentity);
    assertEquals("maryToRootRelationship.getStatus() must return: " + Relationship.Type.IGNORED,
                 Relationship.Type.IGNORED, maryToRootRelationship.getStatus());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#inviteToConnect(Identity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testInviteToConnect() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    rootToDemoRelationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull("rootToDemoRelationship must not be null", rootToDemoRelationship);
    assertEquals("rootToDemoRelationship.getStatus() must return: " + Relationship.Type.PENDING,
                 Relationship.Type.PENDING, rootToDemoRelationship.getStatus());
    
    maryToRootRelationship = relationshipManager.get(maryIdentity, rootIdentity);
    assertNotNull("maryToRootRelationship must not be null", maryToRootRelationship);
    assertEquals("maryToRootRelationship.getStatus() must return: " + Relationship.Type.PENDING,
                 Relationship.Type.PENDING, maryToRootRelationship.getStatus());
    
    rootToJohnRelationship = relationshipManager.get(johnIdentity, rootIdentity);
    assertNotNull("rootToJohnRelationship must not be null", rootToJohnRelationship);
    assertEquals("rootToJohnRelationship.getStatus() must return: " + Relationship.Type.PENDING,
                 Relationship.Type.PENDING, rootToJohnRelationship.getStatus());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#inviteToConnect(Identity, Identity)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
  */
  public void testDupdicateInviteToConnect() throws Exception {
    Relationship relationship1 = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship relationship2 = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    assertEquals("relationShip1 and relationShip2 must be the same",relationship1.getId(), relationship2.getId());
    tearDownRelationshipList.add(relationship1);
  }
  
  /**
   * Test {@link RelationshipManager#inviteToConnect(Identity, Identity)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDupdicateInviteToConnectWithConfirmedRelationShip() throws Exception {
    Relationship relationship1 = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    assertEquals("RelationShip status must be PENDING",Relationship.Type.PENDING, relationship1.getStatus());
    relationshipManager.confirm(rootIdentity, demoIdentity);
    relationship1 = relationshipManager.get(rootIdentity, demoIdentity);
    assertEquals("RelationShip status must be CONFIRMED",Relationship.Type.CONFIRMED, relationship1.getStatus());
    Relationship relationship2 = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    assertEquals("RelationShip status must be CONFIRMED",Relationship.Type.CONFIRMED, relationship2.getStatus());
    
    assertEquals("relationShip1 and relationShip2 must be the same",relationship1.getId(), relationship2.getId());
    
    tearDownRelationshipList.add(relationship1);
  }
  
  /**
   * Test {@link RelationshipManager#confirm(Identity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testConfirmWithIdentity() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    rootToJohnRelationship = relationshipManager.get(rootToJohnRelationship.getId());
    
    relationshipManager.confirm(rootIdentity, demoIdentity);
    rootToDemoRelationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull("rootToDemoRelationship must not be null", rootToDemoRelationship);
    assertEquals("rootToDemoRelationship.getStatus() must return: " + Relationship.Type.CONFIRMED,
                 Relationship.Type.CONFIRMED, rootToDemoRelationship.getStatus());
    
    relationshipManager.confirm(maryIdentity, rootIdentity);
    maryToRootRelationship = relationshipManager.get(maryIdentity, rootIdentity);
    assertNotNull("maryToRootRelationship must not be null", maryToRootRelationship);
    assertEquals("maryToRootRelationship.getStatus() must return: " + Relationship.Type.CONFIRMED,
                 Relationship.Type.CONFIRMED, maryToRootRelationship.getStatus());
    
    relationshipManager.confirm(rootIdentity, johnIdentity);
    rootToJohnRelationship = relationshipManager.get(johnIdentity, rootIdentity);
    assertNotNull("rootToJohnRelationship must not be null", rootToJohnRelationship);
    assertEquals("rootToJohnRelationship.getStatus() must return: " + Relationship.Type.CONFIRMED,
                 Relationship.Type.CONFIRMED, rootToJohnRelationship.getStatus());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#deny(Identity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDeny() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    relationshipManager.confirm(johnIdentity, rootIdentity);
    relationshipManager.deny(johnIdentity, rootIdentity);
    assertNotNull(relationshipManager.get(rootToJohnRelationship.getId()));
    
    rootToJohnRelationship.setStatus(Relationship.Type.PENDING);
    relationshipManager.update(rootToJohnRelationship);
    
    relationshipManager.deny(demoIdentity, rootIdentity);
    rootToDemoRelationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNull("rootToDemoRelationship must be null", rootToDemoRelationship);
    
    relationshipManager.deny(maryIdentity, rootIdentity);
    maryToRootRelationship = relationshipManager.get(maryIdentity, rootIdentity);
    assertNull("maryToRootRelationship must be null", maryToRootRelationship);
    
    relationshipManager.deny(rootIdentity, johnIdentity);
    rootToJohnRelationship = relationshipManager.get(rootIdentity, johnIdentity);
    assertNull("rootToJohnRelationship must be null", rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#ignore(Identity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testIgnore() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    relationshipManager.ignore(demoIdentity, rootIdentity);
    rootToDemoRelationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNull("rootToDemoRelationship must be null", rootToDemoRelationship);
    
    relationshipManager.ignore(maryIdentity, rootIdentity);
    maryToRootRelationship = relationshipManager.get(maryIdentity, rootIdentity);
    assertNull("maryToRootRelationship must be null", maryToRootRelationship);
    
    relationshipManager.ignore(rootIdentity, johnIdentity);
    rootToJohnRelationship = relationshipManager.get(rootIdentity, johnIdentity);
    assertNull("rootToJohnRelationship must be null", rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getIncomingWithListAccess(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetIncomingWithListAccess() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToDemoRelationship = relationshipManager.inviteToConnect(maryIdentity, demoIdentity);
    Relationship johnToDemoRelationship = relationshipManager.inviteToConnect(johnIdentity, demoIdentity);
    
    ListAccess<Identity> demoIncoming = relationshipManager.getIncomingWithListAccess(demoIdentity);
    assertNotNull("demoIncoming must not be null", demoIncoming);
    assertEquals("demoIncoming.getSize() must return: 3", 3, demoIncoming.getSize());
    
    //Test change avatar
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
    assertNotNull(avatarAttachment);
    
    Profile profile = maryIdentity.getProfile();
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.AVATAR));
    identityManager.updateProfile(profile);
    
    Identity[] identities = demoIncoming.load(0, 10);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, demoIdentity.getRemoteId(), true);

    assertEquals(6, identities[0].getProfile().getProperties().size());
    assertEquals(6, identities[1].getProfile().getProperties().size());
    assertEquals(6, identities[2].getProfile().getProperties().size());
    
    for (Identity identity : demoIncoming.load(0, 10)) {
      assertNotNull("identity.getProfile() must not be null", identity.getProfile());
      Identity identityLoadProfile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, identity.getRemoteId(), true);
      assertEquals("identity.getProfile().getFullName() must return: " + identityLoadProfile.getProfile().getFullName(),
                   identityLoadProfile.getProfile().getFullName(), identity.getProfile().getFullName());
    }
    
    ListAccess<Identity> rootIncoming = relationshipManager.getIncomingWithListAccess(rootIdentity);
    assertNotNull("rootIncoming must not be null", rootIncoming);
    assertEquals("rootIncoming.getSize() must return: 0", 0, rootIncoming.getSize());
    
    ListAccess<Identity> maryIncoming = relationshipManager.getIncomingWithListAccess(maryIdentity);
    assertNotNull("maryIncoming must not be null", maryIncoming);
    assertEquals("maryIncoming.getSize() must return: 0", 0, maryIncoming.getSize());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToDemoRelationship);
    tearDownRelationshipList.add(johnToDemoRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getOutgoing(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetOutgoing() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship rootToMaryRelationship = relationshipManager.inviteToConnect(rootIdentity, maryIdentity);
    Relationship maryToDemoRelationship = relationshipManager.inviteToConnect(maryIdentity, demoIdentity);
    Relationship demoToJohnRelationship = relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
    
    ListAccess<Identity> rootOutgoing = relationshipManager.getOutgoing(rootIdentity);
    assertNotNull("rootOutgoing must not be null", rootOutgoing);
    assertEquals("rootOutgoing.getSize() must return: 2", 2, rootOutgoing.getSize());
    
    //Test change avatar
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
    assertNotNull(avatarAttachment);
    
    Profile profile = demoIdentity.getProfile();
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.AVATAR));
    identityManager.updateProfile(profile);
    
    rootOutgoing = relationshipManager.getOutgoing(rootIdentity);
    Identity[] identities = rootOutgoing.load(0, 10);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, demoIdentity.getRemoteId(), true);

    assertEquals(6, identities[0].getProfile().getProperties().size());
    assertEquals(6, identities[1].getProfile().getProperties().size());
    
    for (Identity identity : rootOutgoing.load(0, 10)) {
      Identity identityLoadProfile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, identity.getRemoteId(), true);
      assertNotNull("identity.getProfile() must not be nul", identity.getProfile());
      assertNotNull("temp must not be null", identityLoadProfile);
      assertEquals("identity.getProfile().getFullName() must return: " + identityLoadProfile.getProfile().getFullName(), 
                   identityLoadProfile.getProfile().getFullName(), 
                   identity.getProfile().getFullName());
    }
    
    ListAccess<Identity> maryOutgoing = relationshipManager.getOutgoing(maryIdentity);
    assertNotNull("maryOutgoing must not be null", maryOutgoing);
    assertEquals("maryOutgoing.getSize() must return: 1", 1, maryOutgoing.getSize());
    
    ListAccess<Identity> demoOutgoing = relationshipManager.getOutgoing(demoIdentity);
    assertNotNull("demoOutgoing must not be null", demoOutgoing);
    assertEquals("demoOutgoing.getSize() must return: 1", 1, demoOutgoing.getSize());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToDemoRelationship);
    tearDownRelationshipList.add(demoToJohnRelationship);
    tearDownRelationshipList.add(rootToMaryRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getStatus(Identity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetStatus() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    rootToDemoRelationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNotNull("rootToDemoRelationship must not be null", rootToDemoRelationship);
    assertEquals("rootToDemoRelationship.getStatus() must return: " + Relationship.Type.PENDING,
                 Relationship.Type.PENDING, rootToDemoRelationship.getStatus());
    assertEquals("relationshipManager.getStatus(rootIdentity, demoIdentity) must return: " + 
                 Relationship.Type.PENDING, Relationship.Type.PENDING, 
                 relationshipManager.getStatus(rootIdentity, demoIdentity));
    
    maryToRootRelationship = relationshipManager.get(maryIdentity, rootIdentity);
    assertNotNull("maryToRootRelationship must not be null", maryToRootRelationship);
    assertEquals("maryToRootRelationship.getStatus() must return: " + Relationship.Type.PENDING,
                 Relationship.Type.PENDING, maryToRootRelationship.getStatus());
    assertEquals("relationshipManager.getStatus(maryIdentity, rootIdentity) must return: " + 
                 Relationship.Type.PENDING, Relationship.Type.PENDING,
                 relationshipManager.getStatus(maryIdentity, rootIdentity));
    
    rootToJohnRelationship = relationshipManager.get(johnIdentity, rootIdentity);
    assertNotNull("rootToJohnRelationship must not be null", rootToJohnRelationship);
    assertEquals("rootToJohnRelationship.getStatus() must return: " + Relationship.Type.PENDING,
                 Relationship.Type.PENDING, rootToJohnRelationship.getStatus());
    assertEquals("relationshipManager.getStatus(rootIdentity, johnIdentity) must return: " +
                 Relationship.Type.PENDING, Relationship.Type.PENDING,
                 relationshipManager.getStatus(rootIdentity, johnIdentity));
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getAllWithListAccess(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetAllWithListAccess() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    ListAccess<Identity> rootRelationships = relationshipManager.getAllWithListAccess(rootIdentity);
    assertNotNull("rootRelationships must not be null", rootRelationships);
    assertEquals("rootRelationships.getSize() must return: 3", 3, rootRelationships.getSize());
    
    ListAccess<Identity> demoRelationships = relationshipManager.getAllWithListAccess(demoIdentity);
    assertNotNull("demoRelationships must not be null", demoRelationships);
    assertEquals("demoRelationships.getSize() must return: 1", 1, demoRelationships.getSize());
    
    ListAccess<Identity> johnRelationships = relationshipManager.getAllWithListAccess(johnIdentity);
    assertNotNull("johnRelationships must not be null", johnRelationships);
    assertEquals("johnRelationships.getSize() must return: 1", 1, johnRelationships.getSize());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getRelationshipById(String)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetRelationshipById() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    
    rootToDemoRelationship = relationshipManager.getRelationshipById(rootToDemoRelationship.getId());
    assertNotNull("rootToDemoRelationship must not be null", rootToDemoRelationship);
    assertEquals("rootToDemoRelationship.getSender() must return: " + rootIdentity, rootIdentity, rootToDemoRelationship.getSender());
    assertEquals("rootToDemoRelationship.getReceiver() must return: " + demoIdentity, demoIdentity, rootToDemoRelationship.getReceiver());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#deny(Relationship)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDenyWithRelationship() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    relationshipManager.deny(rootToDemoRelationship);
    rootToDemoRelationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNull("rootToDemoRelationship must be null", rootToDemoRelationship);
    
    relationshipManager.deny(maryToRootRelationship);
    maryToRootRelationship = relationshipManager.get(maryIdentity, rootIdentity);
    assertNull("maryToRootRelationship must be null", maryToRootRelationship);
    
    relationshipManager.deny(rootToJohnRelationship);
    rootToJohnRelationship = relationshipManager.get(rootIdentity, johnIdentity);
    assertNull("rootToJohnRelationship must be null", rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#ignore(Relationship)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testIgnoreWithRelationship() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    relationshipManager.ignore(rootToDemoRelationship);
    rootToDemoRelationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertNull("rootToDemoRelationship must be null", rootToDemoRelationship);
    
    relationshipManager.ignore(maryToRootRelationship);
    maryToRootRelationship = relationshipManager.get(maryIdentity, rootIdentity);
    assertNull("maryToRootRelationship must be null", maryToRootRelationship);
    
    relationshipManager.ignore(rootToJohnRelationship);
    rootToJohnRelationship = relationshipManager.get(rootIdentity, johnIdentity);
    assertNull("rootToJohnRelationship must be null", rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getPendingRelationships(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetPendingRelationships() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    List<Relationship> rootPendingRelationships = relationshipManager.getPendingRelationships(rootIdentity);
    assertNotNull("rootPendingRelationships must not be null", rootPendingRelationships);
    assertEquals("rootPendingRelationships.size() must return: 2", 2, rootPendingRelationships.size());
    
    List<Relationship> maryPendingRelationships = relationshipManager.getPendingRelationships(maryIdentity);
    assertNotNull("maryPendingRelationships must not be null", maryPendingRelationships);
    assertEquals("maryPendingRelationships.size() must return: 1", 1, maryPendingRelationships.size());
    
    List<Relationship> demoPendingRelationships = relationshipManager.getPendingRelationships(demoIdentity);
    assertNotNull("demoPendingRelationships must not be null", demoPendingRelationships);
    assertEquals("demoPendingRelationships.size() must return: 0", 0, demoPendingRelationships.size());
    
    List<Relationship> johnPendingRelationships = relationshipManager.getPendingRelationships(johnIdentity);
    assertNotNull("johnPendingRelationships must not be null", johnPendingRelationships);
    assertEquals("johnPendingRelationships.size() must return: 0", 0, johnPendingRelationships.size());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getPendingRelationships(Identity, boolean)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetPendingRelationshipWithSentOrReceived() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    List<Relationship> rootPendingRelationships = relationshipManager.getPendingRelationships(rootIdentity, true);
    assertNotNull("rootPendingRelationships must not be null", rootPendingRelationships);
    assertEquals("rootPendingRelationships.size() must return: 3", 3, rootPendingRelationships.size());
    
    List<Relationship> maryPendingRelationships = relationshipManager.getPendingRelationships(maryIdentity, true);
    assertNotNull("maryPendingRelationships must not be null", maryPendingRelationships);
    assertEquals("maryPendingRelationships.size() must return: 1", 1, maryPendingRelationships.size());
    
    List<Relationship> demoPendingRelationships = relationshipManager.getPendingRelationships(demoIdentity, true);
    assertNotNull("demoPendingRelationships must not be null", demoPendingRelationships);
    assertEquals("demoPendingRelationships.size() must return: 1", 1, demoPendingRelationships.size());
    
    List<Relationship> johnPendingRelationships = relationshipManager.getPendingRelationships(johnIdentity, true);
    assertNotNull("johnPendingRelationships must not be null", johnPendingRelationships);
    assertEquals("johnPendingRelationships.size() must return: 1", 1, johnPendingRelationships.size());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getPendingRelationships(Identity, List, boolean)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetPendingRealtionshipWithListIdentities() throws Exception {
    List<Identity> identities = new ArrayList<Identity> ();
    identities.add(rootIdentity);
    identities.add(demoIdentity);
    identities.add(johnIdentity);
    identities.add(maryIdentity);
    
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    List<Relationship> rootPendingRelationships = relationshipManager.getPendingRelationships(rootIdentity, identities, true);
    assertNotNull("rootPendingRelationships must not be null", rootPendingRelationships);
    assertEquals("rootPendingRelationships.size() must return: 3", 3, rootPendingRelationships.size());
    
    List<Relationship> maryPendingRelationships = relationshipManager.getPendingRelationships(maryIdentity, identities, true);
    assertNotNull("maryPendingRelationships must not be null", maryPendingRelationships);
    assertEquals("maryPendingRelationships.size() must return: 1", 1, maryPendingRelationships.size());
    
    List<Relationship> demoPendingRelationships = relationshipManager.getPendingRelationships(demoIdentity, identities, true);
    assertNotNull("demoPendingRelationships must not be null", demoPendingRelationships);
    assertEquals("demoPendingRelationships.size() must return: 1", 1, demoPendingRelationships.size());
    
    List<Relationship> johnPendingRelationships = relationshipManager.getPendingRelationships(johnIdentity, identities, true);
    assertNotNull("johnPendingRelationships must not be null", johnPendingRelationships);
    assertEquals("johnPendingRelationships.size() must return: 1", 1, johnPendingRelationships.size());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getContacts(Identity, List)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetContactsWithListIdentities() throws Exception {
    List<Identity> identities = new ArrayList<Identity> ();
    identities.add(rootIdentity);
    identities.add(demoIdentity);
    identities.add(johnIdentity);
    identities.add(maryIdentity);
    
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    relationshipManager.confirm(rootIdentity, demoIdentity);
    relationshipManager.confirm(maryIdentity, rootIdentity);
    relationshipManager.confirm(rootIdentity, johnIdentity);
    
    List<Relationship> rootContacts = relationshipManager.getContacts(rootIdentity, identities);
    assertNotNull("rootContacts must not be null", rootContacts);
    assertEquals("rootContacts.size() must return: 3", 3, rootContacts.size());
    
    List<Relationship> demoContacts = relationshipManager.getContacts(demoIdentity, identities);
    assertNotNull("demoContacts must not be null", demoContacts);
    assertEquals("demoContacts.size() must return: 1", 1, demoContacts.size());
    
    List<Relationship> maryContacts = relationshipManager.getContacts(maryIdentity, identities);
    assertNotNull("maryContacts must not be null", maryContacts);
    assertEquals("maryContacts.size() must return: 1", 1, maryContacts.size());
    
    List<Relationship> johnContacts = relationshipManager.getContacts(johnIdentity, identities);
    assertNotNull("johnContacts must not be null", johnContacts);
    assertEquals("johnContacts.size() must return: 1", 1, johnContacts.size());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getContacts(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetContacts() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    relationshipManager.confirm(rootIdentity, demoIdentity);
    relationshipManager.confirm(maryIdentity, rootIdentity);
    relationshipManager.confirm(rootIdentity, johnIdentity);
    
    List<Relationship> rootContacts = relationshipManager.getContacts(rootIdentity);
    assertNotNull("rootContacts must not be null", rootContacts);
    assertEquals("rootContacts.size() must return: 3", 3, rootContacts.size());
    
    List<Relationship> demoContacts = relationshipManager.getContacts(demoIdentity);
    assertNotNull("demoContacts must not be null", demoContacts);
    assertEquals("demoContacts.size() must return: 1", 1, demoContacts.size());
    
    List<Relationship> maryContacts = relationshipManager.getContacts(maryIdentity);
    assertNotNull("maryContacts must not be null", maryContacts);
    assertEquals("maryContacts.size() must return: 1", 1, maryContacts.size());
    
    List<Relationship> johnContacts = relationshipManager.getContacts(johnIdentity);
    assertNotNull("johnContacts must not be null", johnContacts);
    assertEquals("johnContacts.size() must return: 1", 1, johnContacts.size());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getAllRelationships(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetAllRelationships() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    List<Relationship> rootRelationships = relationshipManager.getAllRelationships(rootIdentity);
    assertNotNull("rootRelationships must not be null", rootRelationships);
    assertEquals("rootRelationships.size() must return: 3", 3, rootRelationships.size());
    
    List<Relationship> maryRelationships = relationshipManager.getAllRelationships(maryIdentity);
    assertNotNull("maryRelationships must not be null", maryRelationships);
    assertEquals("maryRelationships.size() must return: 1", 1, maryRelationships.size());
    
    List<Relationship> demoRelationships = relationshipManager.getAllRelationships(demoIdentity);
    assertNotNull("demoRelationships must not be null", demoRelationships);
    assertEquals("demoRelationships.size() must return: 1", 1, demoRelationships.size());
    
    List<Relationship> johnRelationships = relationshipManager.getAllRelationships(johnIdentity);
    assertNotNull("johnRelationships must not be null", johnRelationships);
    assertEquals("johnRelationships.size() must return: 1", 1, johnRelationships.size());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getRelationshipsByIdentityId(String)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetRelationshipsByIdentityId() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    List<Relationship> rootRelationships = relationshipManager.getRelationshipsByIdentityId(rootIdentity.getId());
    assertNotNull("rootRelationships must not be null", rootRelationships);
    assertEquals("rootRelationships.size() must return: 3", 3, rootRelationships.size());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getIdentities(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetIdentities() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    relationshipManager.confirm(rootIdentity, demoIdentity);
    relationshipManager.confirm(maryIdentity, rootIdentity);
    relationshipManager.confirm(rootIdentity, johnIdentity);
    
    List<Identity> rootConnections = relationshipManager.getIdentities(rootIdentity);
    assertNotNull("rootConnections must not be null", rootConnections);
    assertEquals("rootConnections.size() must return: 3", 3, rootConnections.size());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#create(Identity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testCreate() throws Exception {
    Relationship demoToJohnRelationship = relationshipManager.create(demoIdentity, johnIdentity);
    assertNotNull("demoToJohnRelationship must not be null", demoToJohnRelationship);
    assertEquals("demoToJohnRelationship.getSender() must return: " + demoIdentity, demoIdentity, demoToJohnRelationship.getSender());
    assertEquals("demoToJohnRelationship.getReceiver() must return: " + johnIdentity, johnIdentity, demoToJohnRelationship.getReceiver());
    assertEquals("demoToJohnRelationship.getStatus() must return: " + Relationship.Type.PENDING, Relationship.Type.PENDING, demoToJohnRelationship.getStatus());
  }
  
  /**
   * Test {@link RelationshipManager#getRelationship(Identity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetRelationship() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    
    rootToDemoRelationship = relationshipManager.getRelationship(rootIdentity, demoIdentity);
    assertNotNull("rootToDemoRelationship must not be null", rootToDemoRelationship);
    assertEquals("rootToDemoRelationship.getStatus() must return: " + Relationship.Type.PENDING, Relationship.Type.PENDING, rootToDemoRelationship.getStatus());
    
    relationshipManager.confirm(rootIdentity, demoIdentity);
    
    rootToDemoRelationship = relationshipManager.getRelationship(rootIdentity, demoIdentity);
    assertNotNull("rootToDemoRelationship must not be null", rootToDemoRelationship);
    assertEquals("rootToDemoRelationship.getStatus() must return: " + Relationship.Type.CONFIRMED, Relationship.Type.CONFIRMED, rootToDemoRelationship.getStatus());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#findRelationships(Identity, Type)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testFindRelationships() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    Relationship maryToRootRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    
    List<Identity> rootRelationships = relationshipManager.findRelationships(rootIdentity, Relationship.Type.PENDING);
    assertNotNull("rootRelationships must not be null", rootRelationships);
    assertEquals("rootRelationships.size() must return: 3", 3, rootRelationships.size());
    
    relationshipManager.confirm(rootIdentity, demoIdentity);
    
    rootRelationships = relationshipManager.findRelationships(rootIdentity, Relationship.Type.CONFIRMED);
    assertNotNull("rootRelationships must not be null", rootRelationships);
    assertEquals("rootRelationships.size() must return: 1", 1, rootRelationships.size());
    
    tearDownRelationshipList.add(rootToDemoRelationship);
    tearDownRelationshipList.add(maryToRootRelationship);
    tearDownRelationshipList.add(rootToJohnRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getRelationshipStatus(Relationship, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetRelationshipStatus() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    assertEquals("relationshipManager.getRelationshipStatus(rootToDemoRelationship, rootIdentity) must return: "
                 + Relationship.Type.PENDING, Relationship.Type.PENDING
                 , relationshipManager.getRelationshipStatus(rootToDemoRelationship, rootIdentity));
    
    relationshipManager.confirm(rootIdentity, demoIdentity);
    rootToDemoRelationship = relationshipManager.get(rootIdentity, demoIdentity);
    assertEquals("relationshipManager.getRelationshipStatus(rootToDemoRelationship, rootIdentity) must return: "
                 + Relationship.Type.PENDING, Relationship.Type.CONFIRMED
                 , relationshipManager.getRelationshipStatus(rootToDemoRelationship, rootIdentity));
    
    tearDownRelationshipList.add(rootToDemoRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#getConnectionStatus(Identity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetConnectionStatus() throws Exception {
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    
    assertEquals("relationshipManager.getConnectionStatus(rootIdentity, demoIdentity) must return: " + 
                 Relationship.Type.PENDING, Relationship.Type.PENDING, 
                 relationshipManager.getConnectionStatus(rootIdentity, demoIdentity));
    
    relationshipManager.confirm(rootIdentity, demoIdentity);
    assertEquals("relationshipManager.getConnectionStatus(rootIdentity, demoIdentity) must return: " + 
                 Relationship.Type.CONFIRMED, Relationship.Type.CONFIRMED, 
                 relationshipManager.getConnectionStatus(rootIdentity, demoIdentity));
    
    tearDownRelationshipList.add(rootToDemoRelationship);
  }
  
  /**
   * Test {@link RelationshipManager#invite(Identity, Identity) and RelationshipManager#get(String)}
   *
   * @throws Exception
   */
  public void testIntiveAndGetByRelationshipId() throws Exception {
    Relationship invitedRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    
    Relationship foundRelationship = relationshipManager.get(invitedRelationship.getId());
    assertNotNull("foundRelationship must not be null", foundRelationship);
    assertNotNull("foundRelationship.getId() must not be null", foundRelationship.getId());
    assertEquals(foundRelationship.getId(), invitedRelationship.getId());

    tearDownRelationshipList.add(invitedRelationship);
  }

  /**
   * Test {@link RelationshipManager#getPending(Identity)}
   *
   * @throws Exception
   */
  public void testGetPendingWithIdentity() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    List<Relationship> foundListRelationships = relationshipManager.getPending(johnIdentity);
    assertNotNull("foundListRelationships must not be null", foundListRelationships);
    assertEquals(3, foundListRelationships.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }

  /**
   * Test {@link RelationshipManager#getPending(Identity) and RelationshipManager#getIncoming(Identity)}
   *
   * @throws Exception
   */
  public void testGetPendingAndIncoming() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    List<Relationship> listPendingRelationship = relationshipManager.getPending(johnIdentity);
    assertNotNull("listRelationshipConfirm must not be null", listPendingRelationship);
    assertEquals(3, listPendingRelationship.size());

    List<Relationship> listMaryRequireValidationRelationship = relationshipManager.getIncoming(maryIdentity);
    assertEquals(1, listMaryRequireValidationRelationship.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }

  /**
   * Test {@link RelationshipManager#getPending(Identity) and RelationshipManager#getIncoming(Identity, List)}
   *
   * @throws Exception
   */
  public void testGetPendingAndIncomingWithListIdentities() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);
    Relationship maryDemoRelationship = relationshipManager.invite(maryIdentity, demoIdentity);

    List<Identity> listIdentities = new ArrayList<Identity>();
    listIdentities.add(demoIdentity);
    listIdentities.add(maryIdentity);
    listIdentities.add(johnIdentity);
    listIdentities.add(rootIdentity);

    List<Relationship> listRelationshipConfirm = relationshipManager.getPending(johnIdentity, listIdentities);
    assertEquals(3, listRelationshipConfirm.size());

    List<Relationship> listRelationshipNotConfirm = relationshipManager.getIncoming(demoIdentity, listIdentities);
    assertEquals(2, listRelationshipNotConfirm.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
    tearDownRelationshipList.add(maryDemoRelationship);
  }

  /**
   * Test {@link RelationshipManager#getConfirmed(Identity)}
   *
   * @throws Exception
   */
  public void testGetConfirmedWithIdentity() throws Exception {
    List<Relationship> johnContacts = relationshipManager.getConfirmed(johnIdentity);
    assertNotNull("johnContacts must not be null", johnContacts);
    assertEquals("johnContacts.size() must be 0", 0, johnContacts.size());

    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    relationshipManager.confirm(johnDemoRelationship);
    relationshipManager.confirm(johnMaryRelationship);
    relationshipManager.confirm(johnRootRelationship);

    List<Relationship> contactsList = relationshipManager.getConfirmed(johnIdentity);
    assertEquals(3, contactsList.size());

    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }

  /**
   * Test {@link RelationshipManager#getConfirmed(Identity, List)}
   *
   * @throws Exception
   */
  public void testGetConfirmedWithIdentityAndListIdentity() throws Exception {
    Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
    Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
    Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

    relationshipManager.confirm(johnDemoRelationship);
    relationshipManager.confirm(johnMaryRelationship);
    relationshipManager.confirm(johnRootRelationship);

    List<Identity> listIdentities = new ArrayList<Identity>();
    listIdentities.add(demoIdentity);
    listIdentities.add(maryIdentity);
    listIdentities.add(johnIdentity);
    listIdentities.add(rootIdentity);

    List<Relationship> contactsList = relationshipManager.getConfirmed(johnIdentity, listIdentities);
    assertEquals(3, contactsList.size());
    tearDownRelationshipList.add(johnDemoRelationship);
    tearDownRelationshipList.add(johnMaryRelationship);
    tearDownRelationshipList.add(johnRootRelationship);
  }

  /**
   * Test {@link RelationshipManager#save(Relationship)}
   *
   * @throws Exception
   */
  public void testSave() throws Exception {
    Relationship testRelationship = new Relationship(johnIdentity, demoIdentity, Type.PENDING);
    relationshipManager.save(testRelationship);
    assertNotNull("testRelationship.getId() must not be null", testRelationship.getId());

    tearDownRelationshipList.add(testRelationship);
  }

  /**
   *
   * @throws Exception
   */
  /*
  public void testGetManyRelationshipsByIdentityId() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;

    Identity sender = identityManager.getOrCreateIdentity(providerId,"john");
    identityManager.saveIdentity(sender);
    assertNotNull(sender.getId());

    Identity receiver = identityManager.getOrCreateIdentity(providerId,"mary");
    assertNotNull(receiver.getId());

    relationshipManager.invite(sender, receiver);

    List<Relationship> senderRelationships = relationshipManager.getAllRelationships(sender);
    List<Relationship> receiverRelationships = relationshipManager.getAllRelationships(receiver);

    assertEquals(total, senderRelationships.size());
    assertEquals(total, receiverRelationships.size());
  }
*/

  /**
   * Test {@link RelationshipManager#invite(Identity, Identity)}
   * 
   * @throws Exception
   */
  public void testInviteRelationship() throws Exception {
    Relationship relationship = relationshipManager.invite(johnIdentity, maryIdentity);
    assertNotNull(relationship.getId());
    assertEquals(Relationship.Type.PENDING, relationship.getStatus());

    List<Relationship> senderRelationships = relationshipManager.getAll(johnIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAll(maryIdentity);

    assertEquals(1, senderRelationships.size());
    assertEquals(1, receiverRelationships.size());

    tearDownRelationshipList.addAll(senderRelationships);
  }

  /**
   * Test {@link RelationshipManager#confirm(Relationship)}
   *
   * @throws Exception
   */
  public void testConfirm() throws Exception {
    Relationship relationship = relationshipManager.invite(johnIdentity, demoIdentity);
    relationshipManager.confirm(relationship);
    relationship = relationshipManager.get(johnIdentity, demoIdentity);
    assertNotNull(relationship.getId());
    assertEquals(Relationship.Type.CONFIRMED, relationship.getStatus());

    List<Relationship> senderRelationships = relationshipManager.getAll(johnIdentity);
    List<Relationship> receiverRelationships = relationshipManager.getAll(demoIdentity);

    assertEquals(1, senderRelationships.size());
    assertEquals(1, receiverRelationships.size());

    tearDownRelationshipList.addAll(senderRelationships);
  }

  /**
   * Test {@link RelationshipManager#delete(Relationship)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDelete() throws Exception {
    Relationship relationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, rootIdentity);
    relationshipManager.delete(relationship);

    relationship = relationshipManager.get(rootIdentity, johnIdentity);
    assertNull("relationship must be null", relationship);
  }
  
  /**
   * Test {@link RelationshipManager#remove(Relationship)}
   *
   * @throws Exception
   */
  public void testRemove() throws Exception {
    Relationship relationship = relationshipManager.invite(rootIdentity, johnIdentity);
    relationshipManager.confirm(relationship);
    relationshipManager.remove(relationship);

    relationship = relationshipManager.get(rootIdentity, johnIdentity);
    assertNull("relationship must be null", relationship);
  }

// TODO: Skip this, will be implement later
//  /**
//   *
//   * @throws Exception
//   */
//  public void testIgnore() throws Exception {
//
//    Relationship relationship = relationshipManager.invite(johnIdentity, rootIdentity);
//    relationshipManager.ignore(relationship);
//    assertNotNull(relationship.getId());
//    assertEquals(Relationship.Type.IGNORED, relationship.getStatus());
//
//    List<Relationship> senderRelationships = relationshipManager.getAll(johnIdentity);
//    List<Relationship> receiverRelationships = relationshipManager.getAll(rootIdentity);
//
//    assertEquals(1, senderRelationships.size());
//    assertEquals(1, receiverRelationships.size());
//
//    tearDownRelationshipList.addAll(senderRelationships);
//  }

  /**
   * Test {@link RelationshipManager#getPending(Identity)}
   * 
   * @throws Exception
   */
  public void testGetPending() throws Exception {
    Relationship rootDemo = relationshipManager.invite(rootIdentity, demoIdentity);
    assertNotNull("rootDemo.getId() must not be null", rootDemo.getId());
    Relationship rootJohn = relationshipManager.invite(rootIdentity, johnIdentity);
    assertNotNull("rootJohn.getId() must not be null", rootJohn.getId());
    Relationship rootMary = relationshipManager.invite(rootIdentity, maryIdentity);
    assertNotNull("rootMary.getId() must not be null", rootMary.getId());
    Relationship demoMary = relationshipManager.invite(demoIdentity, maryIdentity);
    assertNotNull("demoMary.getId() must not be null", demoMary.getId());
    Relationship demoJohn = relationshipManager.invite(demoIdentity, johnIdentity);
    assertNotNull("demoJohn.getId() must not be null", demoJohn.getId());
    Relationship johnDemo = relationshipManager.invite(johnIdentity, demoIdentity);
    assertNotNull("johnDemo.getId() must not be null", johnDemo.getId());

    List<Relationship> rootRelationships = relationshipManager.getPending(rootIdentity);
    List<Relationship> demoRelationships = relationshipManager.getPending(demoIdentity);
    List<Relationship> johnRelationships = relationshipManager.getPending(johnIdentity);

    assertEquals(3, rootRelationships.size());
    assertEquals(2, demoRelationships.size());
    assertEquals(0, johnRelationships.size());

    tearDownRelationshipList.add(rootDemo);
    tearDownRelationshipList.add(rootJohn);
    tearDownRelationshipList.add(rootMary);
    tearDownRelationshipList.add(demoMary);
    tearDownRelationshipList.add(demoJohn);
  }

  /**
   * Test relationship with caching.
   * 
   * @throws Exception
   */
  public void testSavedCached() throws Exception {
    Relationship rootDemo = relationshipManager.get(rootIdentity, demoIdentity);
    assertNull("rootDemo must be null", rootDemo);
    Relationship rootDemo2 = relationshipManager.get(demoIdentity, rootIdentity);
    assertNull("rootDemo must be null", rootDemo2);
    Relationship.Type rootDemoStatus = relationshipManager.getStatus(demoIdentity, rootIdentity);
    assertNull("rootDemoStatus must be null",rootDemoStatus);
    rootDemo = relationshipManager.invite(rootIdentity, demoIdentity);
    assertNotNull("rootDemo.getId() must not be null", rootDemo.getId());
    assertEquals(rootDemo.getStatus(), Relationship.Type.PENDING);
    tearDownRelationshipList.add(rootDemo);

    Relationship rootMary = relationshipManager.get(rootIdentity, maryIdentity);
    Relationship.Type rootMaryStatus = relationshipManager.getStatus(maryIdentity, rootIdentity);
    assertNull("rootMary must be null", rootMary);
    assertNull("rootMaryStatus must be null", rootMaryStatus);
    rootMary = relationshipManager.invite(rootIdentity, maryIdentity);
    assertNotNull("rootMary.getId() must not be null", rootMary.getId());
    assertEquals(Relationship.Type.PENDING, rootMary.getStatus());
    tearDownRelationshipList.add(rootMary);

    Relationship rootJohn = relationshipManager.get(rootIdentity, johnIdentity);
    assertNull("rootJohn must be null", rootJohn);
    assertNull("rootMaryStatus must be null", rootMaryStatus);
    rootJohn = relationshipManager.invite(rootIdentity, johnIdentity);
    assertNotNull("rootJohn.getId() must not be null", rootJohn.getId());
    assertEquals(Relationship.Type.PENDING, rootJohn.getStatus());
    tearDownRelationshipList.add(rootJohn);

    Relationship demoMary = relationshipManager.get(demoIdentity, maryIdentity);
    Relationship.Type demoMaryStatus = relationshipManager.getStatus(maryIdentity, demoIdentity);
    assertNull("demoMary must be null", demoMary);
    assertNull("demoMaryStatus must be null", demoMaryStatus);
    demoMary = relationshipManager.invite(demoIdentity, maryIdentity);
    assertNotNull("demoMary.getId() must not be null", demoMary.getId());
    assertEquals(Relationship.Type.PENDING, demoMary.getStatus());
    tearDownRelationshipList.add(demoMary);
  }
  

  /**
   * Tests getting connections of one identity with list access.
   * 
   * @throws Exception
   */
  public void testGetConnections() throws Exception {
     Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);
     Relationship johnMaryRelationship = relationshipManager.invite(johnIdentity, maryIdentity);
     Relationship johnRootRelationship = relationshipManager.invite(johnIdentity, rootIdentity);

     relationshipManager.confirm(johnDemoRelationship);
     relationshipManager.confirm(johnMaryRelationship);
     relationshipManager.confirm(johnRootRelationship);

     ListAccess<Identity> contactsList = relationshipManager.getConnections(johnIdentity);
     assertEquals(3, contactsList.getSize());
     
     //Test change avatar
     InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
     AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
     assertNotNull(avatarAttachment);
     
     Profile profile = demoIdentity.getProfile();
     profile.setProperty(Profile.AVATAR, avatarAttachment);
     profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.AVATAR));
     identityManager.updateProfile(profile);
     
     Identity[] identities = contactsList.load(0, 10);
     demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, demoIdentity.getRemoteId(), true);

     assertEquals(6, identities[0].getProfile().getProperties().size());
     assertEquals(6, identities[1].getProfile().getProperties().size());
     assertEquals(6, identities[2].getProfile().getProperties().size());
     
     for (Identity identity : contactsList.load(0, 10)) {
       assertNotNull("identity.getProfile() must not be null", identity.getProfile());
       Identity identityLoadProfile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, identity.getRemoteId(), true);
       assertEquals("identity.getProfile().getFullName() must return: " + identityLoadProfile.getProfile().getFullName(), identityLoadProfile.getProfile().getFullName(), identity.getProfile().getFullName());
     }

     tearDownRelationshipList.add(johnDemoRelationship);
     tearDownRelationshipList.add(johnMaryRelationship);
     tearDownRelationshipList.add(johnRootRelationship);
  }

  public void testOldGetSuggestions() throws Exception {
    Relationship maryToGhostRelationship = relationshipManager.inviteToConnect(ghostIdentity, maryIdentity);
    Relationship ghostToJohnRelationship = relationshipManager.inviteToConnect(ghostIdentity, johnIdentity);
    Relationship maryToDemoRelationship = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);

    Map<Identity, Integer> suggestions = relationshipManager.getSuggestions(ghostIdentity, 0, 10);
    // The relationships must be confirmed first
    assertTrue(suggestions.isEmpty());
    relationshipManager.confirm(ghostIdentity, maryIdentity);
    relationshipManager.confirm(ghostIdentity, johnIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);
    suggestions = relationshipManager.getSuggestions(ghostIdentity, 0, 10);
    Object[] objs = suggestions.entrySet().toArray();

    Entry<Identity, Integer> first = (Entry<Identity, Integer>) objs[0];

    assertEquals(1, first.getValue().intValue());
    assertEquals(demoIdentity.getRemoteId(), first.getKey().getRemoteId());

    //increase common users
    Relationship johnToDemoRelationship = relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
    Relationship paulToDemoRelationship = relationshipManager.inviteToConnect(paulIdentity, maryIdentity);
    suggestions = relationshipManager.getSuggestions(ghostIdentity, 0, 10);
    assertEquals(1, suggestions.size());
    relationshipManager.confirm(demoIdentity, johnIdentity);
    relationshipManager.confirm(paulIdentity, maryIdentity);

    suggestions = relationshipManager.getSuggestions(ghostIdentity, 0, 10);
    objs = suggestions.entrySet().toArray();
    first = (Entry<Identity, Integer>) objs[0];
    Entry<Identity, Integer> second = (Entry<Identity, Integer>) objs[1];

    assertEquals(demoIdentity.getRemoteId(), first.getKey().getRemoteId());
    assertEquals(paulIdentity.getRemoteId(), second.getKey().getRemoteId());
    assertEquals(2, first.getValue().intValue());
    assertEquals(demoIdentity.getRemoteId(), first.getKey().getRemoteId());
    assertEquals(1, second.getValue().intValue());
    assertEquals(paulIdentity.getRemoteId(), second.getKey().getRemoteId());

    //test with offset > 0
    suggestions = relationshipManager.getSuggestions(ghostIdentity, 1, 10);

    objs = suggestions.entrySet().toArray();
    first = (Entry<Identity, Integer>) objs[0];

    assertEquals(1, first.getValue().intValue());
    assertEquals(paulIdentity.getRemoteId(), first.getKey().getRemoteId());

    tearDownRelationshipList.add(maryToDemoRelationship);
    tearDownRelationshipList.add(johnToDemoRelationship);
    tearDownRelationshipList.add(maryToGhostRelationship);
    tearDownRelationshipList.add(ghostToJohnRelationship);
    tearDownRelationshipList.add(paulToDemoRelationship);
  }

  public void testGetSuggestions() throws Exception {
    Relationship maryToGhostRelationship = relationshipManager.inviteToConnect(ghostIdentity, maryIdentity);
    Relationship ghostToJohnRelationship = relationshipManager.inviteToConnect(ghostIdentity, johnIdentity);
    Relationship maryToDemoRelationship = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);

    Map<Identity, Integer> suggestions = relationshipManager.getSuggestions(ghostIdentity, -1, -1, 10); 
    // The relationships must be confirmed first
    assertTrue(suggestions.isEmpty());
    relationshipManager.confirm(ghostIdentity, maryIdentity);
    relationshipManager.confirm(ghostIdentity, johnIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);
    suggestions = relationshipManager.getSuggestions(ghostIdentity, -1, -1, 10); 
    assertEquals(1, suggestions.size());
    Object[] objs = suggestions.entrySet().toArray();
    
    Entry<Identity, Integer> first = (Entry<Identity, Integer>) objs[0];

    assertEquals(1, first.getValue().intValue());
    assertEquals(demoIdentity.getRemoteId(), first.getKey().getRemoteId());

    //increase common users
    Relationship johnToDemoRelationship = relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
    Relationship paulToDemoRelationship = relationshipManager.inviteToConnect(paulIdentity, maryIdentity);
    suggestions = relationshipManager.getSuggestions(ghostIdentity, -1, -1, 10); 
    assertEquals(1, suggestions.size());
    relationshipManager.confirm(demoIdentity, johnIdentity);
    relationshipManager.confirm(paulIdentity, maryIdentity);

    suggestions = relationshipManager.getSuggestions(ghostIdentity, -1, -1, 10); 
    assertEquals(2, suggestions.size());
    objs = suggestions.entrySet().toArray();
    first = (Entry<Identity, Integer>) objs[0];
    Entry<Identity, Integer> second = (Entry<Identity, Integer>) objs[1];
    
    assertEquals(demoIdentity.getRemoteId(), first.getKey().getRemoteId());
    assertEquals(paulIdentity.getRemoteId(), second.getKey().getRemoteId());
    assertEquals(2, first.getValue().intValue());
    assertEquals(demoIdentity.getRemoteId(), first.getKey().getRemoteId());
    assertEquals(1, second.getValue().intValue());
    assertEquals(paulIdentity.getRemoteId(), second.getKey().getRemoteId());

    relationshipManager.delete(paulToDemoRelationship);
    suggestions = relationshipManager.getSuggestions(ghostIdentity, -1, -1, 10); 
    assertEquals(1, suggestions.size());

    tearDownRelationshipList.add(maryToDemoRelationship);
    tearDownRelationshipList.add(johnToDemoRelationship);
    tearDownRelationshipList.add(maryToGhostRelationship);
    tearDownRelationshipList.add(ghostToJohnRelationship);
  }

  public void testGetSuggestionsWithParams() throws Exception {
    Relationship maryToGhostRelationship = relationshipManager.inviteToConnect(ghostIdentity, maryIdentity);
    Relationship maryToDemoRelationship = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    Relationship paulToMaryRelationship = relationshipManager.inviteToConnect(paulIdentity, maryIdentity);
    Relationship johnToMaryRelationship = relationshipManager.inviteToConnect(maryIdentity, johnIdentity);
    Relationship rootToMaryRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);

    Map<Identity, Integer> suggestions = relationshipManager.getSuggestions(ghostIdentity, -1, -1, 10); 
    // The relationships must be confirmed first
    assertTrue(suggestions.isEmpty());
    suggestions = relationshipManager.getSuggestions(ghostIdentity, 10, 10, 10);
    assertTrue(suggestions.isEmpty());
    suggestions = relationshipManager.getSuggestions(ghostIdentity, 10, 10, 10);
    assertTrue(suggestions.isEmpty());
    relationshipManager.confirm(ghostIdentity, maryIdentity);
    relationshipManager.confirm(paulIdentity, maryIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);
    relationshipManager.confirm(maryIdentity, johnIdentity);
    relationshipManager.confirm(maryIdentity, rootIdentity);

    suggestions = relationshipManager.getSuggestions(ghostIdentity, -1, -1, 10);
    assertFalse(suggestions.containsKey(ghostIdentity));
    assertEquals(4, suggestions.size());
    suggestions = relationshipManager.getSuggestions(ghostIdentity, -1, -1, 10);
    assertFalse(suggestions.containsKey(ghostIdentity));
    assertEquals(4, suggestions.size());
    suggestions = relationshipManager.getSuggestions(ghostIdentity, 2, 2, 10);
    assertFalse(suggestions.containsKey(ghostIdentity));
    // 1 or 2 depending on the connections loaded, if there is ghostIdentity, it will be one
    // otherwise it will be 2
    assertTrue(suggestions.size() > 0 && suggestions.size() <= 2);
    suggestions = relationshipManager.getSuggestions(ghostIdentity, 2, 3, 10);
    assertFalse(suggestions.containsKey(ghostIdentity));
    // 1 or 2 depending on the connections loaded, if there is ghostIdentity, it will be one
    // otherwise it will be 2
    assertTrue(suggestions.size() > 0 && suggestions.size() <= 2);
    suggestions = relationshipManager.getSuggestions(ghostIdentity, 2, 3, 10);
    assertFalse(suggestions.containsKey(ghostIdentity));
    // 1 or 2 depending on the connections loaded, if there is ghostIdentity, it will be one
    // otherwise it will be 2
    assertTrue(suggestions.size() > 0 && suggestions.size() <= 2);

    suggestions = relationshipManager.getSuggestions(ghostIdentity, 10, 10, 2);
    assertFalse(suggestions.containsKey(ghostIdentity));
    assertEquals(2, suggestions.size());

    suggestions = relationshipManager.getSuggestions(ghostIdentity, 10, 2, 2);
    assertFalse(suggestions.containsKey(ghostIdentity));
    assertEquals(2, suggestions.size());

    tearDownRelationshipList.add(maryToGhostRelationship);
    tearDownRelationshipList.add(maryToDemoRelationship);
    tearDownRelationshipList.add(paulToMaryRelationship);
    tearDownRelationshipList.add(johnToMaryRelationship);
    tearDownRelationshipList.add(rootToMaryRelationship);
  }
  
  public void testGetLastConnections() throws Exception {
    Relationship maryToGhostRelationship = relationshipManager.inviteToConnect(ghostIdentity, maryIdentity);
    relationshipManager.confirm(maryIdentity, ghostIdentity);
    Relationship maryToDemoRelationship = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(maryIdentity, demoIdentity);
    Relationship paulToMaryRelationship = relationshipManager.inviteToConnect(paulIdentity, maryIdentity);
    relationshipManager.confirm(maryIdentity, paulIdentity);
    
    List<Identity> identities = relationshipManager.getLastConnections(maryIdentity, 10);
    assertEquals(3, identities.size());
    assertEquals(paulIdentity.getRemoteId(), identities.get(0).getRemoteId());
    assertEquals(demoIdentity.getRemoteId(), identities.get(1).getRemoteId());
    assertEquals(ghostIdentity.getRemoteId(), identities.get(2).getRemoteId());
    
    Relationship johnToMaryRelationship = relationshipManager.inviteToConnect(maryIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, maryIdentity);
    identities = relationshipManager.getLastConnections(maryIdentity, 10);
    assertEquals(4, identities.size());
    assertEquals(johnIdentity.getRemoteId(), identities.get(0).getRemoteId());
    
    tearDownRelationshipList.add(maryToGhostRelationship);
    tearDownRelationshipList.add(maryToDemoRelationship);
    tearDownRelationshipList.add(paulToMaryRelationship);
    tearDownRelationshipList.add(johnToMaryRelationship);
  }
}
