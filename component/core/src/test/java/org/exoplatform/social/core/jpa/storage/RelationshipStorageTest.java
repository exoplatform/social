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
package org.exoplatform.social.core.jpa.storage;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.jpa.test.MaxQueryNumber;
import org.exoplatform.social.core.jpa.test.QueryNumberTest;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.storage.RelationshipStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit Tests for {@link RelationshipStorage}
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Oct 17, 2010
 */
@QueryNumberTest
public class RelationshipStorageTest extends AbstractCoreTest {

  private final Log LOG = ExoLogger.getLogger(RelationshipStorageTest.class);

  private RelationshipStorage relationshipStorage;

  private IdentityStorage identityStorage;
  private List<Identity> tearDownIdentityList;

  private Identity rootIdentity,
                   johnIdentity,
                   maryIdentity,
                   demoIdentity;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    relationshipStorage = (RelationshipStorage) getContainer().getComponentInstanceOfType(RelationshipStorage.class);
    assertNotNull("relationshipStorage must not be null", relationshipStorage);
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    assertNotNull("identityManger must not be null", identityStorage);

    rootIdentity = createIdentity("root");
    johnIdentity = createIdentity("john");
    maryIdentity = createIdentity("mary");
    demoIdentity = createIdentity("demo");
    
    tearDownIdentityList = new ArrayList<Identity>();
  }

  /**
   * Test for {@link org.exoplatform.social.core.storage.api.RelationshipStorage#saveRelationship(Relationship)}
   *
   * @throws RelationshipStorageException
   */
  @MaxQueryNumber(63)
  public void testSaveRelationship() throws RelationshipStorageException {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.PENDING);
    rootToJohnRelationship = relationshipStorage.saveRelationship(rootToJohnRelationship);
    assertNotNull(rootToJohnRelationship.getId());
  }

  /**
   * Test for {@link org.exoplatform.social.core.storage.api.RelationshipStorage#removeRelationship(Relationship)}
   */
  @MaxQueryNumber(69)
  public void testRemoveRelationship() {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.PENDING);
    try {
      relationshipStorage.saveRelationship(rootToJohnRelationship);
      assertNotNull("rootToJohnRelationship.getId() must not be null", rootToJohnRelationship.getId());

      relationshipStorage.removeRelationship(rootToJohnRelationship);
      assertNull("relationshipStorage.getRelationship(rootToJohnRelationship.getId() must be null",
                 relationshipStorage.getRelationship(rootToJohnRelationship.getId()));
    } catch (RelationshipStorageException e) {
      LOG.error(e);
    }
  }

  /**
   * Test for {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getRelationship(String)}
   * @throws RelationshipStorageException
   */
  @MaxQueryNumber(66)
  public void testGetRelationship() throws RelationshipStorageException {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.PENDING);
    rootToJohnRelationship = relationshipStorage.saveRelationship(rootToJohnRelationship);
    rootToJohnRelationship = relationshipStorage.getRelationship(rootToJohnRelationship.getId());
    assertNotNull(rootToJohnRelationship.getId());
    assertEquals(rootIdentity.getRemoteId(), rootToJohnRelationship.getSender().getRemoteId());
    assertEquals(rootIdentity.getProviderId(), rootToJohnRelationship.getSender().getProviderId());
    assertEquals(johnIdentity.getRemoteId(), rootToJohnRelationship.getReceiver().getRemoteId());
    assertEquals(johnIdentity.getProviderId(), rootToJohnRelationship.getReceiver().getProviderId());
    assertEquals(Relationship.Type.PENDING, rootToJohnRelationship.getStatus());

    rootToJohnRelationship.setStatus(Relationship.Type.CONFIRMED);
    relationshipStorage.saveRelationship(rootToJohnRelationship);
    rootToJohnRelationship = relationshipStorage.getRelationship(rootToJohnRelationship.getId());
    assertNotNull(rootToJohnRelationship.getId());
    assertEquals(rootIdentity.getRemoteId(), rootToJohnRelationship.getSender().getRemoteId());
    assertEquals(rootIdentity.getProviderId(), rootToJohnRelationship.getSender().getProviderId());
    assertEquals(johnIdentity.getRemoteId(), rootToJohnRelationship.getReceiver().getRemoteId());
    assertEquals(johnIdentity.getProviderId(), rootToJohnRelationship.getReceiver().getProviderId());
    assertEquals(Relationship.Type.CONFIRMED, rootToJohnRelationship.getStatus());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getConnections(Identity, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  @MaxQueryNumber(96)
  public void testGetConnections() throws Exception {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.CONFIRMED);
    rootToJohnRelationship = relationshipStorage.saveRelationship(rootToJohnRelationship);
    assertNotNull("rootToJohnRelationship.getId() must not be null", rootToJohnRelationship.getId());

    Relationship maryToRootRelationship = new Relationship(maryIdentity, rootIdentity, Type.CONFIRMED);
    maryToRootRelationship = relationshipStorage.saveRelationship(maryToRootRelationship);
    assertNotNull("rootToMaryRelationship.getId() must not be null", maryToRootRelationship.getId());

    Relationship rootToDemoRelationship = new Relationship(rootIdentity, demoIdentity, Type.PENDING);
    rootToDemoRelationship = relationshipStorage.saveRelationship(rootToDemoRelationship);
    assertNotNull("rootToDemoRelationship.getId() must not be null", rootToDemoRelationship.getId());

    //Test change avatar
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
    assertNotNull(avatarAttachment);

    //Test change banner
    InputStream inputStreamBanner = getClass().getResourceAsStream("/eXo-Social.png");
    BannerAttachment bannerAttachment = new BannerAttachment(null, "banner", "png", inputStreamBanner, null, System.currentTimeMillis());
    assertNotNull(bannerAttachment);

    Profile profile = johnIdentity.getProfile();
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    profile.setProperty(Profile.BANNER, bannerAttachment);
    identityStorage.updateProfile(profile);

    List<Identity> listIdentities = relationshipStorage.getConnections(rootIdentity, 0, 10);
    assertNotNull("listIdentities must not be null", listIdentities);
    assertEquals("listIdentities.size() must return: 2", 2, listIdentities.size());

    // Check john has avatar
    assertNotNull(getInList(listIdentities, "john").getProfile());
    assertNotNull(getInList(listIdentities, "john").getProfile().getAvatarUrl());
    assertNotNull(getInList(listIdentities, "john").getProfile().getBannerUrl());

    // Check mary hasn't avatar but empty profile
    assertNotNull(getInList(listIdentities, "mary").getProfile());
    assertNull(getInList(listIdentities, "mary").getProfile().getAvatarUrl());
    assertNull(getInList(listIdentities, "mary").getProfile().getBannerUrl());

    for (Identity identity : listIdentities) {
      assertNotNull("identity.getProfile() must not be null", identity.getProfile());
      Identity identityLoadProfile = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, identity.getRemoteId());
      assertEquals("identity.getProfile().getFullName() must return: " + identityLoadProfile.getProfile().getFullName(), identityLoadProfile.getProfile().getFullName(), identity.getProfile().getFullName());
    }
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getConnectionsCount(Identity)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  @MaxQueryNumber(63)
  public void testGetConnectionsCount() throws Exception {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.CONFIRMED);
    rootToJohnRelationship = relationshipStorage.saveRelationship(rootToJohnRelationship);
    assertNotNull("rootToJohnRelationship.getId() must not be null", rootToJohnRelationship.getId());

    Relationship maryToRootRelationship = new Relationship(maryIdentity, rootIdentity, Type.CONFIRMED);
    maryToRootRelationship = relationshipStorage.saveRelationship(maryToRootRelationship);
    assertNotNull("rootToMaryRelationship.getId() must not be null", maryToRootRelationship.getId());

    Relationship rootToDemoRelationship = new Relationship(rootIdentity, demoIdentity, Type.PENDING);
    rootToDemoRelationship = relationshipStorage.saveRelationship(rootToDemoRelationship);
    assertNotNull("rootToDemoRelationship.getId() must not be null", rootToDemoRelationship.getId());

    List<Identity> listIdentities = relationshipStorage.getConnections(rootIdentity, 0, 10);
    assertNotNull("listIdentities must not be null", listIdentities);
    assertEquals("listIdentities.size() must return: 2", 2, listIdentities.size());

    int count = relationshipStorage.getConnectionsCount(rootIdentity);
    assertEquals("count must be: 2", 2, count);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getRelationships(Identity, Type, List)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  @MaxQueryNumber(84)
  public void testGetRelationshipsWithListCheck() throws Exception {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.CONFIRMED);
    rootToJohnRelationship = relationshipStorage.saveRelationship(rootToJohnRelationship);
    assertNotNull("rootToJohnRelationship.getId() must not be null", rootToJohnRelationship.getId());

    Relationship maryToRootRelationship = new Relationship(maryIdentity, rootIdentity, Type.PENDING);
    maryToRootRelationship = relationshipStorage.saveRelationship(maryToRootRelationship);
    assertNotNull("rootToMaryRelationship.getId() must not be null", maryToRootRelationship.getId());

    Relationship rootToDemoRelationship = new Relationship(rootIdentity, demoIdentity, Type.IGNORED);
    rootToDemoRelationship = relationshipStorage.saveRelationship(rootToDemoRelationship);
    assertNotNull("rootToDemoRelationship.getId() must not be null", rootToDemoRelationship.getId());

    List<Identity> listCheckIdentity = new ArrayList<Identity>();
    listCheckIdentity.add(rootIdentity);
    listCheckIdentity.add(demoIdentity);
    listCheckIdentity.add(maryIdentity);
    listCheckIdentity.add(johnIdentity);

    List<Relationship> rootConfirmedRelationships = relationshipStorage.getRelationships(rootIdentity, Relationship.Type.CONFIRMED, listCheckIdentity);
    assertNotNull("rootConfirmedRelationships must not be null", rootConfirmedRelationships);
    assertEquals("rootConfirmedRelationships.size() must return: 1", 1, rootConfirmedRelationships.size());

    List<Relationship> johnConfirmedRelationships = relationshipStorage.getRelationships(johnIdentity, Relationship.Type.CONFIRMED, listCheckIdentity);
    assertNotNull("johnConfirmedRelationships must not be null", johnConfirmedRelationships);
    assertEquals("johnConfirmedRelationships.size() must return: 1", 1, johnConfirmedRelationships.size());

    List<Relationship> johnPendingRelationships = relationshipStorage.getRelationships(johnIdentity, Relationship.Type.PENDING, listCheckIdentity);
    assertNotNull("johnPendingRelationships must not be null", johnPendingRelationships);
    assertEquals("johnPendingRelationships.size() must return: 0", 0, johnPendingRelationships.size());

    List<Relationship> maryPendingRelationships = relationshipStorage.getRelationships(maryIdentity, Relationship.Type.PENDING, listCheckIdentity);
    assertNotNull("maryPendingRelationships must not be null", maryPendingRelationships);
    assertEquals("maryPendingRelationships.size() must return: 1", 1, maryPendingRelationships.size());

    List<Relationship> demoIgnoredRelationships = relationshipStorage.getRelationships(demoIdentity, Relationship.Type.IGNORED, listCheckIdentity);
    assertNotNull("demoIgnoredRelationships must not be null", demoIgnoredRelationships);
    assertEquals("demoIgnoredRelationships.size() must return: 1", 1, demoIgnoredRelationships.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getSenderRelationships(Identity, Type, List)}
   *
   * @throws RelationshipStorageException
   */
  @MaxQueryNumber(72)
  public void testGetSenderRelationshipsByIdentityAndType() throws RelationshipStorageException {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.PENDING);
    Relationship rootToDemoRelationship = new Relationship(rootIdentity, demoIdentity, Type.PENDING);
    relationshipStorage.saveRelationship(rootToJohnRelationship);
    relationshipStorage.saveRelationship(rootToDemoRelationship);

    List<Relationship> relationships = relationshipStorage.getSenderRelationships(rootIdentity, Type.PENDING, null);
    assertNotNull(relationships);
    assertEquals(2, relationships.size());

    Relationship rootToMaryRelationship = new Relationship(rootIdentity, maryIdentity, Type.CONFIRMED);
    relationshipStorage.saveRelationship(rootToMaryRelationship);

    relationships = relationshipStorage.getSenderRelationships(rootIdentity, Type.CONFIRMED, null);
    assertNotNull(relationships);
    assertEquals(1, relationships.size());

    relationships = relationshipStorage.getSenderRelationships(rootIdentity, null, null);
    assertNotNull(relationships);
    assertEquals(3, relationships.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getSenderRelationships(String, Type, List)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  @MaxQueryNumber(78)
  public void testGetSenderRelationships() throws Exception {
    String rootId = rootIdentity.getId();

    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.PENDING);
    Relationship rootToDemoRelationship = new Relationship(rootIdentity, demoIdentity, Type.PENDING);
    relationshipStorage.saveRelationship(rootToJohnRelationship);
    relationshipStorage.saveRelationship(rootToDemoRelationship);

    List<Relationship> relationships = relationshipStorage.getSenderRelationships(rootId, Type.PENDING, null);
    assertNotNull(relationships);
    assertEquals(2, relationships.size());

    Relationship rootToMaryRelationship = new Relationship(rootIdentity, maryIdentity, Type.CONFIRMED);
    relationshipStorage.saveRelationship(rootToMaryRelationship);

    relationships = relationshipStorage.getSenderRelationships(rootId, Type.CONFIRMED, null);
    assertNotNull(relationships);
    assertEquals(1, relationships.size());

    relationships = relationshipStorage.getSenderRelationships(rootId, null, null);
    assertNotNull(relationships);
    assertEquals(3, relationships.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getRelationships(Identity, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  @MaxQueryNumber(60)
  public void testGetRelationships() throws Exception {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.PENDING);
    rootToJohnRelationship = relationshipStorage.saveRelationship(rootToJohnRelationship);
    assertNotNull("rootToJohnRelationship.getId() must not be null", rootToJohnRelationship.getId());

    Relationship rootToMaryRelationship = new Relationship(rootIdentity, maryIdentity, Type.CONFIRMED);
    rootToMaryRelationship = relationshipStorage.saveRelationship(rootToMaryRelationship);
    assertNotNull("rootToMaryRelationship.getId() must not be null", rootToMaryRelationship.getId());

    Relationship rootToDemoRelationship = new Relationship(rootIdentity, demoIdentity, Type.IGNORED);
    rootToDemoRelationship = relationshipStorage.saveRelationship(rootToDemoRelationship);
    assertNotNull("rootToDemoRelationship.getId() must not be null", rootToDemoRelationship.getId());

    List<Identity> listIdentities = relationshipStorage.getRelationships(rootIdentity, 0, 10);
    assertNotNull("listIdentities must not be null", listIdentities);
    assertEquals("listIdentities.size() must return: 3", 3, listIdentities.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getRelationshipsCount(Identity)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  @MaxQueryNumber(60)
  public void testGetRelationshipsCount() throws Exception {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.PENDING);
    rootToJohnRelationship = relationshipStorage.saveRelationship(rootToJohnRelationship);
    assertNotNull("rootToJohnRelationship.getId() must not be null", rootToJohnRelationship.getId());

    Relationship rootToMaryRelationship = new Relationship(rootIdentity, maryIdentity, Type.CONFIRMED);
    rootToMaryRelationship = relationshipStorage.saveRelationship(rootToMaryRelationship);
    assertNotNull("rootToMaryRelationship.getId() must not be null", rootToMaryRelationship.getId());

    Relationship rootToDemoRelationship = new Relationship(rootIdentity, demoIdentity, Type.IGNORED);
    rootToDemoRelationship = relationshipStorage.saveRelationship(rootToDemoRelationship);
    assertNotNull("rootToDemoRelationship.getId() must not be null", rootToDemoRelationship.getId());

    int count = relationshipStorage.getRelationshipsCount(rootIdentity);
    assertEquals("count must be: 3", 3, count);
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.RelationshipStorage.getConnectionsByFilter(Identity,
   * ProfileFilter, long, long)}
   *
   * @throws Exception
   */
  public void testGetConnectionsByFilterAndSort() {
    relationshipStorage.saveRelationship(new Relationship(rootIdentity, johnIdentity, Type.CONFIRMED));
    relationshipStorage.saveRelationship(new Relationship(rootIdentity, maryIdentity, Type.CONFIRMED));
    relationshipStorage.saveRelationship(new Relationship(rootIdentity, demoIdentity, Type.CONFIRMED));

    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setSorting(new Sorting(SortBy.LASTNAME, OrderBy.ASC));
    List<Identity> connections = relationshipStorage.getConnectionsByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections count is not consistent", 3, connections.size());
    assertEquals("Returned connections seems not sorted correctly",
                 "demo",
                 connections.get(0).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "john",
                 connections.get(1).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "mary",
                 connections.get(2).getProfile().getProperty(Profile.LAST_NAME));

    profileFilter.setSorting(new Sorting(SortBy.LASTNAME, OrderBy.DESC));
    connections = relationshipStorage.getConnectionsByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections seems not sorted correctly",
                 "demo",
                 connections.get(2).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "john",
                 connections.get(1).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "mary",
                 connections.get(0).getProfile().getProperty(Profile.LAST_NAME));

    profileFilter.setFirstCharFieldName(SortBy.LASTNAME.getFieldName());
    profileFilter.setFirstCharacterOfName('D');
    connections = relationshipStorage.getConnectionsByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections count is not consistent", 1, connections.size());
    assertEquals("Returned connections seems not sorted correctly",
                 "demo",
                 connections.get(0).getProfile().getProperty(Profile.LAST_NAME));

    profileFilter.setFirstCharFieldName("NotExistingField");
    profileFilter.setFirstCharacterOfName('J');
    connections = relationshipStorage.getConnectionsByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections must be 0 if filtering on not existing field",
                 0,
                 connections.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.RelationshipStorage.getIncomingByFilter(Identity,
   * ProfileFilter, long, long)}
   *
   * @throws Exception
   */
  public void testGetIncomingByFilterAndSort() {
    relationshipStorage.saveRelationship(new Relationship(johnIdentity, rootIdentity, Type.PENDING));
    relationshipStorage.saveRelationship(new Relationship(maryIdentity, rootIdentity, Type.PENDING));
    relationshipStorage.saveRelationship(new Relationship(demoIdentity, rootIdentity, Type.PENDING));

    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setSorting(new Sorting(SortBy.LASTNAME, OrderBy.ASC));
    List<Identity> connections = relationshipStorage.getIncomingByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections count is not consistent", 3, connections.size());
    assertEquals("Returned connections seems not sorted correctly",
                 "demo",
                 connections.get(0).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "john",
                 connections.get(1).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "mary",
                 connections.get(2).getProfile().getProperty(Profile.LAST_NAME));

    profileFilter.setSorting(new Sorting(SortBy.LASTNAME, OrderBy.DESC));
    connections = relationshipStorage.getIncomingByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections seems not sorted correctly",
                 "demo",
                 connections.get(2).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "john",
                 connections.get(1).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "mary",
                 connections.get(0).getProfile().getProperty(Profile.LAST_NAME));

    profileFilter.setFirstCharFieldName(SortBy.LASTNAME.getFieldName());
    profileFilter.setFirstCharacterOfName('D');
    connections = relationshipStorage.getIncomingByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections count is not consistent", 1, connections.size());
    assertEquals("Returned connections seems not sorted correctly",
                 "demo",
                 connections.get(0).getProfile().getProperty(Profile.LAST_NAME));

    profileFilter.setFirstCharFieldName("NotExistingField");
    profileFilter.setFirstCharacterOfName('J');
    connections = relationshipStorage.getIncomingByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections must be 0 if filtering on not existing field",
                 0,
                 connections.size());
  }

  /**
   * Test
   * {@link org.exoplatform.social.core.storage.api.RelationshipStorage.getConnectionsByFilter(Identity,
   * ProfileFilter, long, long)}
   *
   * @throws Exception
   */
  public void testGetOutgoingByFilterAndSort() {
    relationshipStorage.saveRelationship(new Relationship(rootIdentity, johnIdentity, Type.PENDING));
    relationshipStorage.saveRelationship(new Relationship(rootIdentity, maryIdentity, Type.PENDING));
    relationshipStorage.saveRelationship(new Relationship(rootIdentity, demoIdentity, Type.PENDING));

    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setSorting(new Sorting(SortBy.LASTNAME, OrderBy.ASC));
    List<Identity> connections = relationshipStorage.getOutgoingByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections count is not consistent", 3, connections.size());
    assertEquals("Returned connections seems not sorted correctly",
                 "demo",
                 connections.get(0).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "john",
                 connections.get(1).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "mary",
                 connections.get(2).getProfile().getProperty(Profile.LAST_NAME));

    profileFilter.setSorting(new Sorting(SortBy.LASTNAME, OrderBy.DESC));
    connections = relationshipStorage.getOutgoingByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections seems not sorted correctly",
                 "demo",
                 connections.get(2).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "john",
                 connections.get(1).getProfile().getProperty(Profile.LAST_NAME));
    assertEquals("Returned connections seems not sorted correctly",
                 "mary",
                 connections.get(0).getProfile().getProperty(Profile.LAST_NAME));

    profileFilter.setFirstCharFieldName(SortBy.LASTNAME.getFieldName());
    profileFilter.setFirstCharacterOfName('D');
    connections = relationshipStorage.getOutgoingByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections count is not consistent", 1, connections.size());
    assertEquals("Returned connections seems not sorted correctly",
                 "demo",
                 connections.get(0).getProfile().getProperty(Profile.LAST_NAME));

    profileFilter.setFirstCharFieldName("NotExistingField");
    profileFilter.setFirstCharacterOfName('J');
    connections = relationshipStorage.getOutgoingByFilter(rootIdentity, profileFilter, 0, Integer.MAX_VALUE);
    assertEquals("Returned connections must be 0 if filtering on not existing field",
                 0,
                 connections.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getIncomingRelationships(Identity, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  @MaxQueryNumber(108)
  public void testGetIncomingRelationships() throws Exception {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.PENDING);
    rootToJohnRelationship = relationshipStorage.saveRelationship(rootToJohnRelationship);
    assertNotNull("rootToJohnRelationship.getId() must not be null", rootToJohnRelationship.getId());

    Relationship maryToJohnRelationship = new Relationship(maryIdentity, johnIdentity, Type.PENDING);
    maryToJohnRelationship = relationshipStorage.saveRelationship(maryToJohnRelationship);
    assertNotNull("rootToMaryRelationship.getId() must not be null", maryToJohnRelationship.getId());

    Relationship demoToJohnRelationship = new Relationship(demoIdentity, johnIdentity, Type.CONFIRMED);
    demoToJohnRelationship = relationshipStorage.saveRelationship(demoToJohnRelationship);
    assertNotNull("rootToDemoRelationship.getId() must not be null", demoToJohnRelationship.getId());

    //Test change banner
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
    assertNotNull(avatarAttachment);

    //Test change avatar
    InputStream inputStreamBanner = getClass().getResourceAsStream("/eXo-Social.png");
    BannerAttachment bannerAttachment = new BannerAttachment(null, "banner", "png", inputStreamBanner, null, System.currentTimeMillis());
    assertNotNull(bannerAttachment);

    Profile profile = maryIdentity.getProfile();
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    profile.setProperty(Profile.BANNER, bannerAttachment);
    identityStorage.updateProfile(profile);

    List<Identity> listIdentities = relationshipStorage.getIncomingRelationships(johnIdentity, 0, 10);
    assertNotNull("listIdentities must not be null", listIdentities);
    assertEquals("listIdentities.size() must return: 2", 2, listIdentities.size());

    // Check root hasn't avatar but empty profile
    assertNotNull(getInList(listIdentities, "root").getProfile());
    assertNull(getInList(listIdentities, "root").getProfile().getAvatarUrl());
    assertNull(getInList(listIdentities, "root").getProfile().getBannerUrl());

    // Check mary has avatar
    assertNotNull(getInList(listIdentities, "mary").getProfile());
    assertNotNull(getInList(listIdentities, "mary").getProfile().getAvatarUrl());
    assertNotNull(getInList(listIdentities, "mary").getProfile().getBannerUrl());

    for (Identity identity : listIdentities) {
      assertNotNull("identity.getProfile() must not be null", identity.getProfile());
      Identity identityLoadProfile = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, identity.getRemoteId());
      assertEquals("identity.getProfile().getFullName() must return: " + identityLoadProfile.getProfile().getFullName(),
                   identityLoadProfile.getProfile().getFullName(), identity.getProfile().getFullName());
    }
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getIncomingRelationshipsCount(Identity)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  @MaxQueryNumber(63)
  public void testGetIncomingRelationshipsCount() throws Exception {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.PENDING);
    rootToJohnRelationship = relationshipStorage.saveRelationship(rootToJohnRelationship);
    assertNotNull("rootToJohnRelationship.getId() must not be null", rootToJohnRelationship.getId());

    Relationship maryToJohnRelationship = new Relationship(maryIdentity, johnIdentity, Type.PENDING);
    maryToJohnRelationship = relationshipStorage.saveRelationship(maryToJohnRelationship);
    assertNotNull("rootToMaryRelationship.getId() must not be null", maryToJohnRelationship.getId());

    Relationship demoToJohnRelationship = new Relationship(demoIdentity, johnIdentity, Type.CONFIRMED);
    demoToJohnRelationship = relationshipStorage.saveRelationship(demoToJohnRelationship);
    assertNotNull("rootToDemoRelationship.getId() must not be null", demoToJohnRelationship.getId());

    List<Identity> listIdentities = relationshipStorage.getIncomingRelationships(johnIdentity, 0, 10);
    assertNotNull("listIdentities must not be null", listIdentities);
    assertEquals("listIdentities.size() must return: 2", 2, listIdentities.size());

    int count = relationshipStorage.getIncomingRelationshipsCount(johnIdentity);
    assertEquals("count must be: 2", 2, count);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getOutgoingRelationships(Identity, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  @MaxQueryNumber(66)
  public void testGetOutgoingRelationships() throws Exception {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.PENDING);
    rootToJohnRelationship = relationshipStorage.saveRelationship(rootToJohnRelationship);
    assertNotNull("rootToJohnRelationship.getId() must not be null", rootToJohnRelationship.getId());

    Relationship rootToMaryRelationship = new Relationship(rootIdentity, maryIdentity, Type.PENDING);
    rootToMaryRelationship = relationshipStorage.saveRelationship(rootToMaryRelationship);
    assertNotNull("rootToMaryRelationship.getId() must not be null", rootToMaryRelationship.getId());

    Relationship rootToDemoRelationship = new Relationship(rootIdentity, demoIdentity, Type.IGNORED);
    rootToDemoRelationship = relationshipStorage.saveRelationship(rootToDemoRelationship);
    assertNotNull("rootToDemoRelationship.getId() must not be null", rootToDemoRelationship.getId());

    //Test change avatar
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
    assertNotNull(avatarAttachment);

    //Test change banner
    InputStream inputStreamBanner = getClass().getResourceAsStream("/eXo-Social.png");
    BannerAttachment bannerAttachment = new BannerAttachment(null, "banner", "png", inputStreamBanner, null, System.currentTimeMillis());
    assertNotNull(bannerAttachment);

    Profile profile = johnIdentity.getProfile();
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    profile.setProperty(Profile.BANNER, bannerAttachment);
    identityStorage.updateProfile(profile);

    List<Identity> listIdentities = relationshipStorage.getOutgoingRelationships(rootIdentity, 0, 10);
    assertNotNull("listIdentities must not be null", listIdentities);
    assertEquals("listIdentities.size() must return: 2", 2, listIdentities.size());

    listIdentities = relationshipStorage.getOutgoingRelationships(rootIdentity, 0, 10);
    demoIdentity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, demoIdentity.getRemoteId());

    // Check john has avatar
    assertNotNull(getInList(listIdentities, "john").getProfile());
    assertNotNull(getInList(listIdentities, "john").getProfile().getAvatarUrl());
    assertNotNull(getInList(listIdentities, "john").getProfile().getBannerUrl());

    // Check mary hasn't avatar but empty profile
    assertNotNull(getInList(listIdentities, "mary").getProfile());
    assertNull(getInList(listIdentities, "mary").getProfile().getAvatarUrl());
    assertNull(getInList(listIdentities, "mary").getProfile().getBannerUrl());

    for (Identity identity : listIdentities) {
      Identity identityLoadProfile = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, identity.getRemoteId());
      assertNotNull("identity.getProfile() must not be nul", identity.getProfile());
      assertNotNull("temp must not be null", identityLoadProfile);
      assertEquals("identity.getProfile().getFullName() must return: " + identityLoadProfile.getProfile().getFullName(),
                   identityLoadProfile.getProfile().getFullName(),
                   identity.getProfile().getFullName());
    }
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getOutgoingRelationshipsCount(Identity)}
   *
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  @MaxQueryNumber(63)
  public void testGetOutgoingRelationshipsCount() throws Exception {
    Relationship rootToJohnRelationship = new Relationship(rootIdentity, johnIdentity, Type.PENDING);
    rootToJohnRelationship = relationshipStorage.saveRelationship(rootToJohnRelationship);
    assertNotNull("rootToJohnRelationship.getId() must not be null", rootToJohnRelationship.getId());

    Relationship rootToMaryRelationship = new Relationship(rootIdentity, maryIdentity, Type.PENDING);
    rootToMaryRelationship = relationshipStorage.saveRelationship(rootToMaryRelationship);
    assertNotNull("rootToMaryRelationship.getId() must not be null", rootToMaryRelationship.getId());

    Relationship rootToDemoRelationship = new Relationship(rootIdentity, demoIdentity, Type.IGNORED);
    rootToDemoRelationship = relationshipStorage.saveRelationship(rootToDemoRelationship);
    assertNotNull("rootToDemoRelationship.getId() must not be null", rootToDemoRelationship.getId());

    List<Identity> listIdentities = relationshipStorage.getOutgoingRelationships(rootIdentity, 0, 10);
    assertNotNull("listIdentities must not be null", listIdentities);
    assertEquals("listIdentities.size() must return: 2", 2, listIdentities.size());

    int count = relationshipStorage.getOutgoingRelationshipsCount(rootIdentity);
    assertEquals("count must be: 2", 2, count);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getConnectionsByFilter(providerId, Identity, ProfileFilter)}
   * in case Identity had no connection yet
   * @throws Exception
   */
  @MaxQueryNumber(111)
  public void testGetConnectionsByFilterEmpty() throws Exception {
    populateData();
    ProfileFilter pf = new ProfileFilter();
    pf = buildProfileFilterWithExcludeIdentities(pf);
    List<Identity> identities = relationshipStorage.getConnectionsByFilter(tearDownIdentityList.get(0), pf, 0, 20);
    assertEquals("Number of identities must be " + identities.size(), 0, identities.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getConnectionsByFilter(providerId, Identity, ProfileFilter)}
   *
   * @throws Exception
   * @since 1.2.3
   */
  @MaxQueryNumber(192)
  public void testGetConnectionsByFilter() throws Exception {
    populateData();
    populateRelationshipData(Type.CONFIRMED);
    ProfileFilter pf = new ProfileFilter();
    pf = buildProfileFilterWithExcludeIdentities(pf);
    List<Identity> identities = relationshipStorage.getConnectionsByFilter(tearDownIdentityList.get(0), pf, 0, 20);
    assertEquals("Number of identities must be 8", 8, identities.size());
    pf.setCompany("exo");
    identities = relationshipStorage.getConnectionsByFilter(tearDownIdentityList.get(0), pf, 0, 20);
    assertEquals("Number of identities must be 2", 2, identities.size());
    pf.setPosition("developer");
    pf.setName("FirstName9");
    pf.setCompany("");
    identities = relationshipStorage.getConnectionsByFilter(tearDownIdentityList.get(0), pf, 0, 20);
    assertEquals("Number of identities must be 1", 1, identities.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getConnectionsByFilter(providerId, Identity, ProfileFilter)}
   *
   * @throws Exception
   * @since 1.2.3
   */
  @MaxQueryNumber(177)
  public void testGetConnectionsWithDisabledUser() throws Exception {
    populateData();
    populateRelationshipData(Type.CONFIRMED);
    ProfileFilter pf = new ProfileFilter();
    //pf = buildProfileFilterWithExcludeIdentities(pf);
    List<Identity> identities = relationshipStorage.getConnectionsByFilter(tearDownIdentityList.get(0), pf, 0, 20);
    assertEquals("Number of identities must be 8", 8, identities.size());

    Identity id1 = identities.get(2);
    Identity id2 = identities.get(3);

    String disabledUserName = id1.getRemoteId();
    String deletedUserName = id2.getRemoteId();

    id1.setEnable(false);
    identityStorage.saveIdentity(id1);

    id2.setDeleted(true);
    identityStorage.saveIdentity(id2);

    identities = relationshipStorage.getConnectionsByFilter(tearDownIdentityList.get(0), pf, 0, 10);
    assertEquals("Number of identities must be 6", 6, identities.size());
    assertNull("User " + disabledUserName + " must not be found in connections", getInList(identities, disabledUserName));
    assertNull("User " + deletedUserName + " must not be found in connections", getInList(identities, deletedUserName));
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getIncomingByFilter(providerId, Identity, ProfileFilter)}
   *
   * @throws Exception
   * @since 1.2.3
   */
  @MaxQueryNumber(186)
  public void testGetIncomingByFilter() throws Exception {
    populateData();
    populateRelationshipIncommingData();
    ProfileFilter pf = new ProfileFilter();
    pf = buildProfileFilterWithExcludeIdentities(pf);
    List<Identity> identities = relationshipStorage.getIncomingByFilter(tearDownIdentityList.get(0), pf, 0, 20);
    assertEquals("Number of identities must be " + identities.size(), 8, identities.size());

    pf.setPosition("developer");
    pf.setName("FirstName6");
    identities = relationshipStorage.getIncomingByFilter(tearDownIdentityList.get(0), pf, 0, 20);
    assertEquals("Number of identities must be " + identities.size(), 1, identities.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getOutgoingByFilter(providerId, Identity, ProfileFilter)}
   *
   * @throws Exception
   * @since 1.2.3
   */
  @MaxQueryNumber(174)
  public void testGetOutgoingByFilter() throws Exception {
    populateData();
    populateRelationshipData(Type.PENDING);
    ProfileFilter pf = new ProfileFilter();
    pf = buildProfileFilterWithExcludeIdentities(pf);
    List<Identity> identities = relationshipStorage.getOutgoingByFilter(tearDownIdentityList.get(0), pf, 0, 20);
    assertEquals("Number of identities must be 8", 8, identities.size());

    pf.setPosition("developer");
    pf.setName("FirstName8");
    identities = relationshipStorage.getOutgoingByFilter(tearDownIdentityList.get(0), pf, 0, 20);
    assertEquals("Number of identities must be 1", 1, identities.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getIncomingCountByFilter(providerId, Identity, ProfileFilter)}
   *
   * @throws Exception
   * @since 1.2.3
   */
  @MaxQueryNumber(198)
  public void testGetIncomingCountByFilter() throws Exception {
    populateData();
    populateRelationshipIncommingData();
    ProfileFilter pf = new ProfileFilter();
    pf = buildProfileFilterWithExcludeIdentities(pf);
    int countIdentities = relationshipStorage.getIncomingCountByFilter(tearDownIdentityList.get(0), pf);
    assertEquals("Number of identities must be 8", 8, countIdentities);

    pf.setPosition("developer");
    pf.setName("FirstName6");
    countIdentities = relationshipStorage.getIncomingCountByFilter(tearDownIdentityList.get(0), pf);
    assertEquals("Number of identities must be 1", 1, countIdentities);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getConnectionsCountByFilter(providerId, Identity, ProfileFilter)}
   *
   * @throws Exception
   * @since 1.2.2
   */
  @MaxQueryNumber(204)
  public void testGetConnectionsCountByFilter() throws Exception {
    populateData();
    populateRelationshipData(Type.CONFIRMED);
    ProfileFilter pf = new ProfileFilter();
    pf = buildProfileFilterWithExcludeIdentities(pf);
    int countIdentities = relationshipStorage.getConnectionsCountByFilter(tearDownIdentityList.get(0), pf);
    assertEquals("Number of identities must be 8", 8, countIdentities);

    pf.setPosition("developer");
    pf.setName("FirstName6");
    countIdentities = relationshipStorage.getConnectionsCountByFilter(tearDownIdentityList.get(0), pf);
    assertEquals("Number of identities must be 1", 1, countIdentities);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.api.RelationshipStorage#getOutgoingCountByFilter(providerId, Identity, ProfileFilter)}
   *
   * @throws Exception
   * @since 1.2.3
   */
  @MaxQueryNumber(174)
  public void testGetOutgoingCountByFilter() throws Exception {
    populateData();
    populateRelationshipData(Type.PENDING);
    ProfileFilter pf = new ProfileFilter();
    pf = buildProfileFilterWithExcludeIdentities(pf);
    int countIdentities = relationshipStorage.getOutgoingCountByFilter(tearDownIdentityList.get(0), pf);
    assertEquals("Number of identities must be 8", 8, countIdentities);

    pf.setPosition("developer");
    pf.setName("FirstName8");
    countIdentities = relationshipStorage.getOutgoingCountByFilter(tearDownIdentityList.get(0), pf);
    assertEquals("Number of identities must be 1", 1, countIdentities);
  }

  /**
   * Builds the ProfileFilter and exclude the Identity.
   * @param filter
   * @return
   */
  private ProfileFilter buildProfileFilterWithExcludeIdentities(ProfileFilter filter) {

    ProfileFilter result = filter;
    if (result == null) {
      result = new ProfileFilter();
    }

    List<Identity> excludeIdentities = new ArrayList<Identity>();
    if (tearDownIdentityList.size() > 1) {
      Identity identity0 = tearDownIdentityList.get(0);
      excludeIdentities.add(identity0);
      result.setExcludedIdentityList(excludeIdentities);
    }

    return result;

  }

  /**
   * Creates the relationship to connect from 0 to [2, 9].
   * @param type
   */
  private void populateRelationshipData(Relationship.Type type) {
    if (tearDownIdentityList.size() > 1) {
      Identity identity0 = tearDownIdentityList.get(0);

      Relationship firstToSecondRelationship = null;
      for (int i = 2; i< tearDownIdentityList.size(); i++) {
        firstToSecondRelationship = new Relationship(identity0, tearDownIdentityList.get(i), type);
        relationshipStorage.saveRelationship(firstToSecondRelationship);
      }
    }
  }

  /**
   * Creates the relationship to connect from 0 to [2, 9].
   */
  private void populateRelationshipIncommingData() {
    if (tearDownIdentityList.size() > 1) {
      Identity identity0 = tearDownIdentityList.get(0);

      Relationship firstToSecondRelationship = null;
      for (int i = 2; i< tearDownIdentityList.size(); i++) {
        firstToSecondRelationship = new Relationship(tearDownIdentityList.get(i), identity0, Relationship.Type.PENDING);
        relationshipStorage.saveRelationship(firstToSecondRelationship);
      }
    }
  }
  
  /**
   * Creates the identity data index in range [0,9]  
   */
  private void populateData() {
    String providerId = "organization";
    int total = 10;
    Map<String, String> xp = new HashMap<String, String>();
    List<Map<String, String>> xps = new ArrayList<Map<String, String>>();
    xp.put(Profile.EXPERIENCES_COMPANY, "exo");
    xps.add(xp);
    for (int i = 0; i < total; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId);
      identityStorage.saveIdentity(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName" + i);
      profile.setProperty(Profile.LAST_NAME, "LastName" + i);
      profile.setProperty(Profile.FULL_NAME, "FirstName" + i + " " +  "LastName" + i);
      profile.setProperty("position", "developer");
      profile.setProperty("gender", "male");
      if (i == 3 || i == 4) {
        profile.setProperty(Profile.EXPERIENCES, xps);
      }
      identity.setProfile(profile);
      tearDownIdentityList.add(identity);
      identityStorage.saveProfile(profile);
    }
  }

  private Identity getInList(List<Identity> identities, String username) {
    if (identities != null) {
      for (Identity id : identities) {
        if (id.getRemoteId().equals(username)) {
          return id;
        }
      }
    }
    return null;
  }
  
}
