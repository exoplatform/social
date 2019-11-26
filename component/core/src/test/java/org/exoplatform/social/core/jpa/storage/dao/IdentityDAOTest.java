/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.core.jpa.storage.dao;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;
import org.exoplatform.social.core.relationship.model.Relationship.Type;

import javax.persistence.EntityExistsException;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class IdentityDAOTest extends BaseCoreTest {

  private IdentityDAO identityDAO;

  private ConnectionDAO connectionDAO;

  private List<IdentityEntity> deleteIdentities = new ArrayList<IdentityEntity>();
  private List<ConnectionEntity> connectionIdentities = new ArrayList<ConnectionEntity>();

  public void setUp() throws Exception {
    super.setUp();
    identityDAO = getService(IdentityDAO.class);
    connectionDAO = getService(ConnectionDAO.class);

    assertNotNull("IdentityDAO must not be null", identityDAO);
    deleteIdentities = new ArrayList<IdentityEntity>();
  }

  @Override
  public void tearDown() throws Exception {
    for (ConnectionEntity connectionEntity : connectionIdentities) {
      connectionDAO.delete(connectionEntity);
    }
    for (IdentityEntity e : deleteIdentities) {
      identityDAO.delete(e);
    }

    super.tearDown();
  }

  public void testGetAllIds() {
    // Given
    IdentityEntity identityUser1 = identityDAO.create(createIdentity(OrganizationIdentityProvider.NAME, "user1"));
    IdentityEntity identityUser2 = identityDAO.create(createIdentity(OrganizationIdentityProvider.NAME, "user2"));
    IdentityEntity identitySpace1 = identityDAO.create(createIdentity(SpaceIdentityProvider.NAME, "space1"));

    // When
    List<Long> allIds = identityDAO.getAllIds(0, 0);

    // Then
    assertNotNull(allIds);
    assertEquals(3, allIds.size());
    assertTrue(allIds.contains(identityUser1.getId()));
    assertTrue(allIds.contains(identityUser2.getId()));
    assertTrue(allIds.contains(identitySpace1.getId()));

    deleteIdentities.add(identityUser1);
    deleteIdentities.add(identityUser2);
    deleteIdentities.add(identitySpace1);
  }

  public void testFindAllIdentitiesWithConnections() {

    IdentityEntity identityUser0 = identityDAO.create(createIdentity(OrganizationIdentityProvider.NAME, "userWithConn0"));
    deleteIdentities.add(identityUser0);
    // Given
    for (int i = 1; i < 21; i++) {
      IdentityEntity identityUser = identityDAO.create(createIdentity(OrganizationIdentityProvider.NAME, "userWithConn" + i));
      deleteIdentities.add(identityUser);
      if (i % 2 == 0) {
        ConnectionEntity connectionEntity = new ConnectionEntity(identityUser0, identityUser);
        connectionEntity.setUpdatedDate(new Date());
        if (i % 3 == 0) {
          connectionEntity.setStatus(Type.CONFIRMED);
        } else {
          connectionEntity.setStatus(Type.PENDING);
        }
        connectionDAO.create(connectionEntity);
        connectionIdentities.add(connectionEntity);
      }
    }

    IdentityEntity space0 = identityDAO.create(createIdentity(SpaceIdentityProvider.NAME, "spaceAB"));
    deleteIdentities.add(space0);
    IdentityEntity space1 = identityDAO.create(createIdentity(SpaceIdentityProvider.NAME, "spaceABC"));
    deleteIdentities.add(space1);

    ListAccess<Entry<IdentityEntity, ConnectionEntity>> identities = identityDAO.findAllIdentitiesWithConnections(identityUser0.getId(), null, '\u0000', null, null);
    try {
      assertTrue("The identities count is incoherent", identities.getSize() >= 20L);
    } catch (Exception e) {
      fail("Can't get the identities count", e);
    }

    Entry<IdentityEntity, ConnectionEntity>[] identitiesArray = null;
    try {
      identitiesArray = identities.load(0, identities.getSize());
    } catch (Exception e) {
      fail("An error occured while getting identities from result query", e);
    }
    assertNotNull("Returned identities list is empty", identitiesArray);
    assertTrue("The identities count is incoherent", identitiesArray.length >= 20L);

    int count = 0;
    for (Entry<IdentityEntity, ConnectionEntity> tuple : identitiesArray) {
      assertNotNull("First element returnd in tuple is null", tuple.getKey());

      IdentityEntity identityEntity = (IdentityEntity) tuple.getKey();
      ConnectionEntity connectionEntity = (ConnectionEntity) tuple.getValue();

      String userId = identityEntity.getRemoteId();
      assertEquals("", identityEntity.getProviderId(), OrganizationIdentityProvider.NAME);

      if (!userId.startsWith("userWithConn") || userId.equals("userWithConn0")) {
        continue;
      }
      count++;
      int index = Integer.parseInt(userId.replace("userWithConn", ""));

      if (index % 2 == 0) {
        assertNotNull("The connection with user " + userId + " should exist", connectionEntity);
        if (index % 3 == 0) {
          assertEquals("The connection status is incoherent with user " + userId, Type.CONFIRMED, connectionEntity.getStatus());
        } else {
          assertEquals("The connection status is incoherent with user " + userId, Type.PENDING, connectionEntity.getStatus());
        }
      } else {
        assertNull("The connection with user " + userId + " shouldn't exist", connectionEntity);
      }
    }
    assertEquals("The returned number of users with prefix 'userWithConn' is incoherent", 20, count);
  }

  public void testFindAllIdentitiesSorted() {
    String userPrefix = "userSorted";
    for (int i = 20; i > 0; i--) {
      String remoteId = userPrefix + i;
      IdentityEntity identityUser = identityDAO.create(createIdentity(OrganizationIdentityProvider.NAME, remoteId));
      deleteIdentities.add(identityUser);
      identityUser.getProperties().put(Profile.FULL_NAME, remoteId);
      identityDAO.update(identityUser);
    }

    List<String> identitiesList = identityDAO.getAllIdsByProviderSorted(OrganizationIdentityProvider.NAME,
                                                                        null,
                                                                        '\0',
                                                                        null,
                                                                        null,
                                                                        0,
                                                                        Integer.MAX_VALUE);
    assertTrue(identitiesList.size() >= 20);
    Iterator<String> iterator = identitiesList.iterator();
    while (iterator.hasNext()) {
      String username = iterator.next();
      if (!username.startsWith(userPrefix)) {
        iterator.remove();
      }
    }
    List<String> identitiesListBackup = new ArrayList<>(identitiesList);
    Collections.sort(identitiesList);
    assertEquals("List '" + identitiesList + "' is not sorted", identitiesList, identitiesListBackup);
  }

  public void testFindAllIdentitiesWithConnectionsSorted() throws Exception {
    String userPrefix = "userSorted";
    for (int i = 20; i > 0; i--) {
      String remoteId = userPrefix + i;
      IdentityEntity identityUser = identityDAO.create(createIdentity(OrganizationIdentityProvider.NAME, remoteId));
      deleteIdentities.add(identityUser);
      identityUser.getProperties().put(Profile.FULL_NAME, remoteId);
      identityDAO.update(identityUser);
    }

    IdentityEntity identityUser0 = identityDAO.create(createIdentity(OrganizationIdentityProvider.NAME, "userWithConn0"));
    deleteIdentities.add(identityUser0);

    ListAccess<Entry<IdentityEntity, ConnectionEntity>> identitiesListAccess =
                                                                             identityDAO.findAllIdentitiesWithConnections(identityUser0.getId(),
                                                                                                                          null,
                                                                                                                          '\u0000',
                                                                                                                          null,
                                                                                                                          null);
    Entry<IdentityEntity, ConnectionEntity>[] identitiesEntries = identitiesListAccess.load(0, Integer.MAX_VALUE);
    List<String> identitiesList = Arrays.stream(identitiesEntries).map(entry -> entry.getKey().getRemoteId()).collect(Collectors.toList());
    assertTrue(identitiesList.size() >= 20);
    Iterator<String> iterator = identitiesList.iterator();
    while (iterator.hasNext()) {
      String username = iterator.next();
      if (!username.startsWith(userPrefix)) {
        iterator.remove();
      }
    }
    List<String> identitiesListBackup = new ArrayList<>(identitiesList);
    Collections.sort(identitiesList);
    assertEquals("List '" + identitiesList + "' is not sorted", identitiesList, identitiesListBackup);
  }

  public void testGetAllIdsByProvider() {
    // Given
    IdentityEntity identityUser1 = identityDAO.create(createIdentity(OrganizationIdentityProvider.NAME, "user1"));
    IdentityEntity identityUser2 = identityDAO.create(createIdentity(OrganizationIdentityProvider.NAME, "user2"));
    IdentityEntity identitySpace1 = identityDAO.create(createIdentity(SpaceIdentityProvider.NAME, "space1"));

    // When
    List<Long> allOrganizationIds = identityDAO.getAllIdsByProvider(OrganizationIdentityProvider.NAME, 0, 0);
    List<Long> allSpaceIds = identityDAO.getAllIdsByProvider(SpaceIdentityProvider.NAME, 0, 0);

    // Then
    assertNotNull(allOrganizationIds);
    assertEquals(2, allOrganizationIds.size());
    assertTrue(allOrganizationIds.contains(identityUser1.getId()));
    assertTrue(allOrganizationIds.contains(identityUser2.getId()));
    assertNotNull(allSpaceIds);
    assertEquals(1, allSpaceIds.size());
    assertTrue(allSpaceIds.contains(identitySpace1.getId()));

    deleteIdentities.add(identityUser1);
    deleteIdentities.add(identityUser2);
    deleteIdentities.add(identitySpace1);
  }

  public void testSaveNewIdentity() {
    IdentityEntity entity = createIdentity();

    identityDAO.create(entity);

    IdentityEntity e = identityDAO.find(entity.getId());

    assertNotNull(e);
    assertEquals("usera", e.getRemoteId());
    assertEquals(OrganizationIdentityProvider.NAME, e.getProviderId());

    deleteIdentities.add(e);
  }

  public void testDeleteIdentity() {
    IdentityEntity identity = createIdentity();
    identity = identityDAO.create(identity);

    long id = identity.getId();
    assertTrue(id > 0);

    identity = identityDAO.find(id);
    assertNotNull(identity);
    assertEquals(OrganizationIdentityProvider.NAME, identity.getProviderId());
    assertEquals("usera", identity.getRemoteId());

    identityDAO.delete(identity);

    assertNull(identityDAO.find(id));
  }

  public void testUpdateIdentity() {
    IdentityEntity identity = createIdentity();
    identityDAO.create(identity);

    identity = identityDAO.find(identity.getId());
    assertFalse(identity.isDeleted());
    assertTrue(identity.isEnabled());

    identity.setEnabled(false);
    identityDAO.update(identity);

    identity = identityDAO.find(identity.getId());
    assertFalse(identity.isDeleted());
    assertFalse(identity.isEnabled());

    deleteIdentities.add(identity);
  }

  public void testCreateDuplicateIdentity() {
    IdentityEntity identity1 = createIdentity();
    IdentityEntity identity2 = createIdentity();

    deleteIdentities.add(identityDAO.create(identity1));

    try {
      identityDAO.create(identity2);
      fail("EntityExistsException should be thrown");
    } catch (EntityExistsException ex) {
    }
  }

  private IdentityEntity createIdentity() {
    return createIdentity(OrganizationIdentityProvider.NAME, "usera");
  }

  private IdentityEntity createIdentity(String providerId, String remoteId) {
    IdentityEntity entity = new IdentityEntity();
    entity.setProviderId(providerId);
    entity.setRemoteId(remoteId);
    entity.setEnabled(true);
    entity.setDeleted(false);
    return entity;
  }
}
