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

package org.exoplatform.social.extras.injection;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml")
})
public class InjectorTestCase extends AbstractKernelTest {

  private IdentityInjector identityInjector;
  private SpaceInjector spaceInjector;
  private ActivityInjector activityInjector;
  private RelationshipInjector relationshipInjector;

  private OrganizationService organizationService;
  private IdentityManager identityManager;
  private IdentityStorage identityStorage;
  private SpaceService spaceService;
  private ActivityManager activityManager;
  private RelationshipManager relationshipManager;

  private HashMap<String, String> params;
  private List<String> users;
  private List<String> spaces;

  @Override
  public void setUp() throws Exception {

    //
    begin();

    //
    identityInjector = (IdentityInjector) getContainer().getComponentInstanceOfType(IdentityInjector.class);
    spaceInjector = (SpaceInjector) getContainer().getComponentInstanceOfType(SpaceInjector.class);
    activityInjector = (ActivityInjector) getContainer().getComponentInstanceOfType(ActivityInjector.class);
    relationshipInjector = (RelationshipInjector) getContainer().getComponentInstanceOfType(RelationshipInjector.class);

    organizationService = (OrganizationService) getContainer().getComponentInstanceOfType(OrganizationService.class);
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);

    params = new HashMap<String, String>();
    users = new ArrayList<String>();
    spaces = new ArrayList<String>();

  }

  @Override
  public void tearDown() throws Exception {

    //
    for(String space : spaces) {
      spaceService.deleteSpace(spaceService.getSpaceByPrettyName(space));
      Identity i = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space);
      identityStorage.deleteIdentity(i);
    }

    //
    for(String user : users) {
      organizationService.getUserHandler().removeUser(user, false);
      Identity i = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, user);
      identityStorage.deleteIdentity(i);
    }

    //
    end();
    
  }

  public void testIdentity() throws Exception {

    //
    assertClean();

    //
    params.put("number", "2");
    identityInjector.inject(params);

    //
    assertNotNull(organizationService.getUserHandler().findUserByName("bench.user0"));
    assertNotNull(organizationService.getUserHandler().findUserByName("bench.user1"));
    assertEquals(null, organizationService.getUserHandler().findUserByName("bench.user2"));

    //
    params.put("number", "2");
    identityInjector.inject(params);

    //
    assertNotNull(organizationService.getUserHandler().findUserByName("bench.user0"));
    assertNotNull(organizationService.getUserHandler().findUserByName("bench.user1"));
    assertNotNull(organizationService.getUserHandler().findUserByName("bench.user2"));
    assertNotNull(organizationService.getUserHandler().findUserByName("bench.user3"));
    assertEquals(null, organizationService.getUserHandler().findUserByName("bench.user4"));

    //
    cleanIdentity(4);

  }

  public void testSpace() throws Exception {

    //
    assertClean();

    //
    params.put("number", "5");
    identityInjector.inject(params);

    //
    assertNotNull(organizationService.getUserHandler().findUserByName("bench.user0"));
    assertNotNull(organizationService.getUserHandler().findUserByName("bench.user1"));
    assertNotNull(organizationService.getUserHandler().findUserByName("bench.user2"));
    assertNotNull(organizationService.getUserHandler().findUserByName("bench.user3"));
    assertNotNull(organizationService.getUserHandler().findUserByName("bench.user4"));
    assertEquals(null, organizationService.getUserHandler().findUserByName("bench.user5"));

    //
    params.put("number", "2");
    params.put("fromUser", "1");
    params.put("toUser", "3");
    spaceInjector.inject(params);

    //
    Space space0 = spaceService.getSpaceByPrettyName("benchspace0");
    Space space1 = spaceService.getSpaceByPrettyName("benchspace1");
    Space space2 = spaceService.getSpaceByPrettyName("benchspace2");
    Space space3 = spaceService.getSpaceByPrettyName("benchspace3");
    Space space4 = spaceService.getSpaceByPrettyName("benchspace4");
    Space space5 = spaceService.getSpaceByPrettyName("benchspace5");
    Space space6 = spaceService.getSpaceByPrettyName("benchspace6");

    //
    assertNotNull(space0);
    assertEquals("bench.user1", space0.getManagers()[0]);
    assertNotNull(space1);
    assertEquals("bench.user1", space1.getManagers()[0]);
    assertNotNull(space2);
    assertEquals("bench.user2", space2.getManagers()[0]);
    assertNotNull(space3);
    assertEquals("bench.user2", space3.getManagers()[0]);
    assertNotNull(space4);
    assertEquals("bench.user3", space4.getManagers()[0]);
    assertNotNull(space5);
    assertEquals("bench.user3", space5.getManagers()[0]);
    assertEquals(null, space6);
    
    spaceInjector.inject(params);

    //
    space6 = spaceService.getSpaceByPrettyName("benchspace6");
    Space space7 = spaceService.getSpaceByPrettyName("benchspace7");
    Space space8 = spaceService.getSpaceByPrettyName("benchspace8");
    Space space9 = spaceService.getSpaceByPrettyName("benchspace9");
    Space space10 = spaceService.getSpaceByPrettyName("benchspace10");
    Space space11 = spaceService.getSpaceByPrettyName("benchspace11");
    Space space12 = spaceService.getSpaceByPrettyName("benchspace12");

    //
    assertNotNull(space6);
    assertEquals("bench.user1", space6.getManagers()[0]);
    assertNotNull(space7);
    assertEquals("bench.user1", space7.getManagers()[0]);
    assertNotNull(space8);
    assertEquals("bench.user2", space8.getManagers()[0]);
    assertNotNull(space9);
    assertEquals("bench.user2", space9.getManagers()[0]);
    assertNotNull(space10);
    assertEquals("bench.user3", space10.getManagers()[0]);
    assertNotNull(space11);
    assertEquals("bench.user3", space11.getManagers()[0]);
    assertEquals(null, space12);

    //
    cleanIdentity(5);
    cleanSpace(12);

  }

  public void testActivity() throws Exception {

    //
    assertClean();

    //
    params.put("number", "5");
    identityInjector.inject(params);

    //
    Identity user0 = identityManager.getOrCreateIdentity("organization", "bench.user0", false);
    Identity user1 = identityManager.getOrCreateIdentity("organization", "bench.user1", false);
    Identity user2 = identityManager.getOrCreateIdentity("organization", "bench.user2", false);
    Identity user3 = identityManager.getOrCreateIdentity("organization", "bench.user3", false);
    Identity user4 = identityManager.getOrCreateIdentity("organization", "bench.user4", false);

    //
    params.put("number", "5");
    params.put("fromUser", "1");
    params.put("toUser", "3");
    params.put("type", "user");
    activityInjector.inject(params);

    //
    assertEquals(0, activityManager.getActivitiesWithListAccess(user0).getSize());
    assertEquals(5, activityManager.getActivitiesWithListAccess(user1).getSize());
    assertEquals(5, activityManager.getActivitiesWithListAccess(user2).getSize());
    assertEquals(5, activityManager.getActivitiesWithListAccess(user3).getSize());
    assertEquals(0, activityManager.getActivitiesWithListAccess(user4).getSize());

    //
    params.put("number", "2");
    params.put("fromUser", "1");
    params.put("toUser", "3");
    params.put("type", "user");
    spaceInjector.inject(params);

    //
    Identity space_user0 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "benchspace0", false);
    Identity space_user1 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "benchspace1", false);
    Identity space_user2 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "benchspace2", false);
    Identity space_user3 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "benchspace3", false);
    Identity space_user4 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "benchspace4", false);

    //
    params.put("number", "5");
    params.put("fromUser", "1");
    params.put("toUser", "3");
    params.put("type", "space");
    activityInjector.inject(params);

    //
    assertEquals(1, activityManager.getActivitiesWithListAccess(space_user0).getSize());
    assertEquals(6, activityManager.getActivitiesWithListAccess(space_user1).getSize());
    assertEquals(6, activityManager.getActivitiesWithListAccess(space_user2).getSize());
    assertEquals(6, activityManager.getActivitiesWithListAccess(space_user3).getSize());
    assertEquals(1, activityManager.getActivitiesWithListAccess(space_user4).getSize());


    //
    cleanIdentity(5);
    cleanSpace(6);

  }

  public void testRelationship() throws Exception {

    //
    assertClean();

    //
    params.put("number", "10");
    identityInjector.inject(params);

    //
    Identity user0 = identityManager.getOrCreateIdentity("organization", "bench.user0", false);
    Identity user1 = identityManager.getOrCreateIdentity("organization", "bench.user1", false);
    Identity user2 = identityManager.getOrCreateIdentity("organization", "bench.user2", false);
    Identity user3 = identityManager.getOrCreateIdentity("organization", "bench.user3", false);
    Identity user4 = identityManager.getOrCreateIdentity("organization", "bench.user4", false);
    Identity user5 = identityManager.getOrCreateIdentity("organization", "bench.user5", false);
    Identity user6 = identityManager.getOrCreateIdentity("organization", "bench.user6", false);
    Identity user7 = identityManager.getOrCreateIdentity("organization", "bench.user7", false);
    Identity user8 = identityManager.getOrCreateIdentity("organization", "bench.user8", false);
    Identity user9 = identityManager.getOrCreateIdentity("organization", "bench.user9", false);

    //
    params.put("number", "3");
    params.put("fromUser", "2");
    params.put("toUser", "8");
    relationshipInjector.inject(params);

    //
    assertEquals(0, relationshipManager.getConnections(user0).getSize());
    assertEquals(0, relationshipManager.getConnections(user1).getSize());
    assertEquals(3, relationshipManager.getConnections(user2).getSize());
    assertEquals(3, relationshipManager.getConnections(user3).getSize());
    assertEquals(3, relationshipManager.getConnections(user4).getSize());
    assertEquals(3, relationshipManager.getConnections(user5).getSize());
    assertEquals(2, relationshipManager.getConnections(user6).getSize());
    assertEquals(2, relationshipManager.getConnections(user7).getSize());
    assertEquals(2, relationshipManager.getConnections(user8).getSize());
    assertEquals(0, relationshipManager.getConnections(user9).getSize());

  }

  public void testPossibility() throws Exception {

    assertEquals(true, relationshipInjector.isPossible(0, 3, 1));
    assertEquals(false, relationshipInjector.isPossible(0, 3, 2));
    assertEquals(true, relationshipInjector.isPossible(0, 3, 3));

    assertEquals(false, relationshipInjector.isPossible(0, 4, 1));
    assertEquals(false, relationshipInjector.isPossible(0, 4, 2));
    assertEquals(false, relationshipInjector.isPossible(0, 4, 3));
    assertEquals(true, relationshipInjector.isPossible(0, 4, 4));

    assertEquals(true, relationshipInjector.isPossible(0, 9, 1));
    assertEquals(false, relationshipInjector.isPossible(0, 9, 2));
    assertEquals(false, relationshipInjector.isPossible(0, 9, 3));
    assertEquals(true, relationshipInjector.isPossible(0, 9, 4));
    assertEquals(false, relationshipInjector.isPossible(0, 9, 5));
    assertEquals(false, relationshipInjector.isPossible(0, 9, 6));
    assertEquals(false, relationshipInjector.isPossible(0, 9, 7));
    assertEquals(false, relationshipInjector.isPossible(0, 9, 8));
    assertEquals(true, relationshipInjector.isPossible(0, 9, 9));

  }

  public void testCompute() throws Exception {

    Map<Integer, Integer> r = relationshipInjector.compute(0, 3, 1);
    assertEquals(1, r.size());
    assertEquals(1, r.get(4).intValue());

    r = relationshipInjector.compute(0, 3, 2);
    assertEquals(2, r.size());
    assertEquals(0, r.get(1).intValue());
    assertEquals(2, r.get(3).intValue());

    r = relationshipInjector.compute(0, 3, 3);
    assertEquals(1, r.size());
    assertEquals(3, r.get(4).intValue());

    r = relationshipInjector.compute(0, 4, 1);
    assertEquals(2, r.size());
    assertEquals(0, r.get(1).intValue());
    assertEquals(1, r.get(4).intValue());

    r = relationshipInjector.compute(0, 4, 2);
    assertEquals(2, r.size());
    assertEquals(1, r.get(2).intValue());
    assertEquals(2, r.get(3).intValue());

    r = relationshipInjector.compute(0, 4, 3);
    assertEquals(2, r.size());
    assertEquals(0, r.get(1).intValue());
    assertEquals(3, r.get(4).intValue());

    r = relationshipInjector.compute(0, 4, 4);
    assertEquals(1, r.size());
    assertEquals(4, r.get(5).intValue());

    r = relationshipInjector.compute(0, 9, 1);
    assertEquals(1, r.size());
    assertEquals(1, r.get(10).intValue());

    r = relationshipInjector.compute(0, 9, 2);
    assertEquals(2, r.size());
    assertEquals(0, r.get(1).intValue());
    assertEquals(2, r.get(9).intValue());

    r = relationshipInjector.compute(0, 9, 3);
    assertEquals(2, r.size());
    assertEquals(1, r.get(2).intValue());
    assertEquals(3, r.get(8).intValue());

    r = relationshipInjector.compute(0, 9, 4);
    assertEquals(1, r.size());
    assertEquals(4, r.get(10).intValue());

    r = relationshipInjector.compute(0, 9, 5);
    assertEquals(2, r.size());
    assertEquals(3, r.get(4).intValue());
    assertEquals(5, r.get(6).intValue());

    r = relationshipInjector.compute(0, 9, 6);
    assertEquals(2, r.size());
    assertEquals(2, r.get(3).intValue());
    assertEquals(6, r.get(7).intValue());

    r = relationshipInjector.compute(0, 9, 7);
    assertEquals(2, r.size());
    assertEquals(1, r.get(2).intValue());
    assertEquals(7, r.get(8).intValue());

    r = relationshipInjector.compute(0, 9, 8);
    assertEquals(2, r.size());
    assertEquals(0, r.get(1).intValue());
    assertEquals(8, r.get(9).intValue());

    r = relationshipInjector.compute(0, 9, 9);
    assertEquals(1, r.size());
    assertEquals(9, r.get(10).intValue());

    r = relationshipInjector.compute(0, 1, 1);
    assertEquals(1, r.size());
    assertEquals(1, r.get(2).intValue());

    r = relationshipInjector.compute(0, 1, 2);
    assertEquals(1, r.size());
    assertEquals(1, r.get(2).intValue());


    r = relationshipInjector.compute(0, 20, 50);
    assertEquals(1, r.size());
    assertEquals(20, r.get(21).intValue());

    r = relationshipInjector.compute(1, 20, 50);
    assertEquals(1, r.size());
    assertEquals(19, r.get(20).intValue());

  }

  private void assertClean() throws Exception {
   
    assertEquals(null, organizationService.getUserHandler().findUserByName("bench.user0"));
    assertEquals(null, identityStorage.findIdentity(OrganizationIdentityProvider.NAME, "bench.user0"));
    assertEquals(null, spaceService.getSpaceByPrettyName("benchspace0"));
    assertEquals(null, identityStorage.findIdentity(SpaceIdentityProvider.NAME, "benchspace0"));

  }

  private void cleanIdentity(int number) {

    for (int i = 0; i < number; ++i) {
      users.add("bench.user" + i);
    }

  }

  private void cleanSpace(int number) {

    for (int i = 0; i < number; ++i) {
      spaces.add("benchspace" + i);
    }

  }
  
}
