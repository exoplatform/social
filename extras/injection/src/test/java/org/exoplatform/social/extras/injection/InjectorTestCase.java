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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;

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
  private MembershipInjector membershipInjector;
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
    membershipInjector = (MembershipInjector) getContainer().getComponentInstanceOfType(MembershipInjector.class);
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
      spaceService.deleteSpace(spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(space)));
      Identity i = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.toLowerCase());
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

  public void testDefaultIdentity() throws Exception {
    performIdentityTest(null);
  }

  public void testPrefixIdentity() throws Exception {
    performIdentityTest("foo");
  }
  
  public void testPrefixCaseSensitiveIdentity() throws Exception {
    performIdentityTest("Foo");
  }

  public void testDefaultSpace() throws Exception {
    performSpaceTest(null, null);
  }

  public void testPrefixSpace() throws Exception {
    performSpaceTest("foo", "bar");
  }
  
  public void testDefaultSpaceMember() throws Exception {
    performMembershipTest(null, null);
  }
  
  public void testPrefixSpaceMember() throws Exception {
    performMembershipTest("foo", "bar");
  }
  
  public void testDefaultActivity() throws Exception {
    performActivityTest(null, null);
  }

  public void testPrefixActivity() throws Exception {
    performActivityTest("foo", "bar");
  }

  public void testDefaultRelationship() throws Exception {
    performRelationshipTest(null);
  }

  public void testPrefixRelationship() throws Exception {
    performRelationshipTest("foo");
  }

  private void performIdentityTest(String prefix) throws Exception {

    //
    String baseName = (prefix == null ? "bench.user" : prefix);
    assertClean(baseName, null);

    //
    params.put("number", "2");
    if (prefix != null) {
      params.put("prefix", prefix);
    }
    identityInjector.inject(params);

    //
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "1"));
    assertEquals(null, organizationService.getUserHandler().findUserByName(baseName + "2"));

    //
    params.put("number", "2");
    if (prefix != null) {
      params.put("prefix", prefix);
    }
    identityInjector.inject(params);

    //
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "1"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "2"));
    assertNotNull(organizationService.getUserHandler().findUserByName(baseName + "3"));
    assertEquals(null, organizationService.getUserHandler().findUserByName(baseName + "4"));

    //
    cleanIdentity(baseName, 4);

  }

  private void performSpaceTest(String userPrefix, String spacePrefix) throws Exception {

    //
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String spaceBaseName = (spacePrefix == null ? "bench.space" : spacePrefix);
    String spacePrettyBaseName = spaceBaseName.replace(".", "");
    assertClean(userBaseName, spacePrettyBaseName);

    //
    params.put("number", "5");
    if (userPrefix != null) {
      params.put("prefix", userPrefix);
    }
    identityInjector.inject(params);

    //
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "1"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "2"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "3"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "4"));
    assertEquals(null, organizationService.getUserHandler().findUserByName(userBaseName + "5"));

    //
    params.put("number", "2");
    params.put("fromUser", "1");
    params.put("toUser", "3");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (spacePrefix != null) {
      params.put("spacePrefix", spacePrefix);
    }
    spaceInjector.inject(params);

    //
    Space space0 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "0");
    Space space1 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "1");
    Space space2 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "2");
    Space space3 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "3");
    Space space4 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "4");
    Space space5 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "5");
    Space space6 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "6");

    //
    assertNotNull(space0);
    assertEquals(userBaseName + "1", space0.getManagers()[0]);
    assertNotNull(space1);
    assertEquals(userBaseName + "1", space1.getManagers()[0]);
    assertNotNull(space2);
    assertEquals(userBaseName + "2", space2.getManagers()[0]);
    assertNotNull(space3);
    assertEquals(userBaseName + "2", space3.getManagers()[0]);
    assertNotNull(space4);
    assertEquals(userBaseName + "3", space4.getManagers()[0]);
    assertNotNull(space5);
    assertEquals(userBaseName + "3", space5.getManagers()[0]);
    assertEquals(null, space6);

    spaceInjector.inject(params);

    //
    space6 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "6");
    Space space7 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "7");
    Space space8 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "8");
    Space space9 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "9");
    Space space10 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "10");
    Space space11 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "11");
    Space space12 = spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "12");

    //
    assertNotNull(space6);
    assertEquals(userBaseName + "1", space6.getManagers()[0]);
    assertNotNull(space7);
    assertEquals(userBaseName + "1", space7.getManagers()[0]);
    assertNotNull(space8);
    assertEquals(userBaseName + "2", space8.getManagers()[0]);
    assertNotNull(space9);
    assertEquals(userBaseName + "2", space9.getManagers()[0]);
    assertNotNull(space10);
    assertEquals(userBaseName + "3", space10.getManagers()[0]);
    assertNotNull(space11);
    assertEquals(userBaseName + "3", space11.getManagers()[0]);
    assertEquals(null, space12);

    //
    cleanIdentity(userBaseName, 5);
    cleanSpace(spacePrettyBaseName, 12);

  }
  
  private void performMembershipTest(String userPrefix, String spacePrefix) throws Exception {

    //
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String spaceBaseName = (spacePrefix == null ? "bench.space" : spacePrefix);
    String spacePrettyBaseName = spaceBaseName.replace(".", "");
    assertClean(userBaseName, spacePrettyBaseName);

    //
    params.put("number", "10");
    if (userPrefix != null) {
      params.put("prefix", userPrefix);
    }
    identityInjector.inject(params);

    //
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "0"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "1"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "2"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "3"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "4"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "5"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "6"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "7"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "8"));
    assertNotNull(organizationService.getUserHandler().findUserByName(userBaseName + "9"));
    assertEquals(null, organizationService.getUserHandler().findUserByName(userBaseName + "10"));

    //
    params.put("number", "2");
    params.put("fromUser", "1");
    params.put("toUser", "3");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (spacePrefix != null) {
      params.put("spacePrefix", spacePrefix);
    }
    spaceInjector.inject(params);

    //
    Space space0 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "0");
    Space space1 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "1");
    Space space2 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "2");
    Space space3 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "3");
    Space space4 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "4");
    Space space5 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "5");
    Space space6 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "6");

    //
    assertNotNull(space0);
    assertEquals(userBaseName + "1", space0.getManagers()[0]);
    assertEquals(1, space0.getManagers().length);
    assertEquals(1, space0.getMembers().length);
    
    assertNotNull(space1);
    assertEquals(userBaseName + "1", space1.getManagers()[0]);
    assertEquals(1, space1.getManagers().length);
    assertEquals(1, space1.getMembers().length);
    
    assertNotNull(space2);
    assertEquals(userBaseName + "2", space2.getManagers()[0]);
    assertEquals(1, space2.getManagers().length);
    assertEquals(1, space2.getMembers().length);
    
    assertNotNull(space3);
    assertEquals(userBaseName + "2", space3.getManagers()[0]);
    assertEquals(1, space3.getManagers().length);
    assertEquals(1, space3.getMembers().length);
    
    assertNotNull(space4);
    assertEquals(userBaseName + "3", space4.getManagers()[0]);
    assertEquals(1, space4.getManagers().length);
    assertEquals(1, space4.getMembers().length);
    
    assertNotNull(space5);
    assertEquals(userBaseName + "3", space5.getManagers()[0]);
    assertEquals(1, space5.getManagers().length);
    assertEquals(1, space5.getMembers().length);
    
    assertEquals(null, space6);

    spaceInjector.inject(params);

    //
    space6 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "6");
    Space space7 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "7");
    Space space8 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "8");
    Space space9 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "9");
    Space space10 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "10");
    Space space11 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "11");
    Space space12 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "12");

    //
    assertNotNull(space6);
    assertEquals(userBaseName + "1", space6.getManagers()[0]);
    assertEquals(1, space6.getManagers().length);
    assertEquals(1, space6.getMembers().length);
    
    assertNotNull(space7);
    assertEquals(userBaseName + "1", space7.getManagers()[0]);
    assertEquals(1, space7.getManagers().length);
    assertEquals(1, space7.getMembers().length);
    
    assertNotNull(space8);
    assertEquals(userBaseName + "2", space8.getManagers()[0]);
    assertEquals(1, space8.getManagers().length);
    assertEquals(1, space8.getMembers().length);
    
    assertNotNull(space9);
    assertEquals(userBaseName + "2", space9.getManagers()[0]);
    assertEquals(1, space9.getManagers().length);
    assertEquals(1, space9.getMembers().length);
    
    assertNotNull(space10);
    assertEquals(userBaseName + "3", space10.getManagers()[0]);
    assertEquals(1, space10.getManagers().length);
    assertEquals(1, space10.getMembers().length);
    
    assertNotNull(space11);
    assertEquals(userBaseName + "3", space11.getManagers()[0]);
    assertEquals(1, space11.getManagers().length);
    assertEquals(1, space11.getMembers().length);
    
    assertEquals(null, space12);

    //inject member
    params.clear();
    
    params.put("type", "member");
    params.put("fromUser", "7");
    params.put("toUser", "9");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    
    params.put("fromSpace", "5");
    params.put("toSpace", "9");
    if (spacePrefix != null) {
      params.put("spacePrefix", spacePrefix);
    }
    
    membershipInjector.inject(params);

    space5 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "5");
    space6 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "6");
    space7 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "7");
    space8 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "8");
    
    assertEquals(4, space5.getMembers().length);
    assertEquals(4, space6.getMembers().length);
    assertEquals(4, space7.getMembers().length);
    assertEquals(4, space8.getMembers().length);
    
    //inject manager
    params.clear();
    
    params.put("type", "manager");
    params.put("fromUser", "7");
    params.put("toUser", "9");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    
    params.put("fromSpace", "5");
    params.put("toSpace", "9");
    if (spacePrefix != null) {
      params.put("spacePrefix", spacePrefix);
    }
    
    membershipInjector.inject(params);

    space5 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "5");
    space6 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "6");
    space7 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "7");
    space8 = spaceService.getSpaceByPrettyName(spacePrettyBaseName + "8");
    
    assertEquals(4, space5.getManagers().length);
    assertEquals(4, space6.getManagers().length);
    assertEquals(4, space7.getManagers().length);
    assertEquals(4, space8.getManagers().length);
    
    //
    cleanIdentity(userBaseName, 10);
    cleanSpace(spacePrettyBaseName, 12);

  }
  
  private void performActivityTest(String userPrefix, String spacePrefix) throws Exception {

    //
    String userBaseName = (userPrefix == null ? "bench.user" : userPrefix);
    String spaceBaseName = (spacePrefix == null ? "bench.space" : spacePrefix);
    String spacePrettyBaseName = spaceBaseName.replace(".", "");
    assertClean(userBaseName, spacePrettyBaseName);

    //
    params.put("number", "5");
    if (userPrefix != null) {
      params.put("prefix", userPrefix);
    }
    identityInjector.inject(params);

    //
    Identity user0 = identityManager.getOrCreateIdentity("organization", userBaseName + "0", false);
    Identity user1 = identityManager.getOrCreateIdentity("organization", userBaseName + "1", false);
    Identity user2 = identityManager.getOrCreateIdentity("organization", userBaseName + "2", false);
    Identity user3 = identityManager.getOrCreateIdentity("organization", userBaseName + "3", false);
    Identity user4 = identityManager.getOrCreateIdentity("organization", userBaseName + "4", false);

    //
    params.put("number", "5");
    params.put("fromUser", "1");
    params.put("toUser", "3");
    params.put("type", "user");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (spacePrefix != null) {
      params.put("spacePrefix", spacePrefix);
    }
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
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (spacePrefix != null) {
      params.put("spacePrefix", spacePrefix);
    }
    spaceInjector.inject(params);

    //
    Identity space_user0 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spacePrettyBaseName + "0", false);
    Identity space_user1 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spacePrettyBaseName + "1", false);
    Identity space_user2 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spacePrettyBaseName + "2", false);
    Identity space_user3 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spacePrettyBaseName + "3", false);
    Identity space_user4 = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spacePrettyBaseName + "4", false);

    //
    params.put("number", "5");
    params.put("fromUser", "1");
    params.put("toUser", "3");
    params.put("type", "space");
    if (userPrefix != null) {
      params.put("userPrefix", userPrefix);
    }
    if (spacePrefix != null) {
      params.put("spacePrefix", spacePrefix);
    }
    activityInjector.inject(params);

    //
    assertEquals(1, activityManager.getActivitiesWithListAccess(space_user0).getSize());
    assertEquals(6, activityManager.getActivitiesWithListAccess(space_user1).getSize());
    assertEquals(6, activityManager.getActivitiesWithListAccess(space_user2).getSize());
    assertEquals(6, activityManager.getActivitiesWithListAccess(space_user3).getSize());
    assertEquals(1, activityManager.getActivitiesWithListAccess(space_user4).getSize());


    //
    cleanIdentity(userBaseName, 5);
    cleanSpace(spacePrettyBaseName, 6);

  }

  private void performRelationshipTest(String prefix) throws Exception {

    //
    String baseName = (prefix == null ? "bench.user" : prefix);
    assertClean(baseName, null);

    //
    params.put("number", "10");
    if (prefix != null) {
      params.put("prefix", prefix);
    }
    identityInjector.inject(params);

    //
    Identity user0 = identityManager.getOrCreateIdentity("organization", baseName + "0", false);
    Identity user1 = identityManager.getOrCreateIdentity("organization", baseName + "1", false);
    Identity user2 = identityManager.getOrCreateIdentity("organization", baseName + "2", false);
    Identity user3 = identityManager.getOrCreateIdentity("organization", baseName + "3", false);
    Identity user4 = identityManager.getOrCreateIdentity("organization", baseName + "4", false);
    Identity user5 = identityManager.getOrCreateIdentity("organization", baseName + "5", false);
    Identity user6 = identityManager.getOrCreateIdentity("organization", baseName + "6", false);
    Identity user7 = identityManager.getOrCreateIdentity("organization", baseName + "7", false);
    Identity user8 = identityManager.getOrCreateIdentity("organization", baseName + "8", false);
    Identity user9 = identityManager.getOrCreateIdentity("organization", baseName + "9", false);

    //
    params.put("number", "3");
    params.put("fromUser", "2");
    params.put("toUser", "8");
    if (prefix != null) {
      params.put("prefix", prefix);
    }
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

    //
    cleanIdentity(baseName, 10);

  }

  private void assertClean(String userBaseName, String spacePrettyBaseName) throws Exception {

    if (userBaseName != null) {
      assertEquals(null, organizationService.getUserHandler().findUserByName(userBaseName + "0"));
      assertEquals(null, identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userBaseName + "0"));
    }

    if (spacePrettyBaseName != null) {
      assertEquals(null, spaceService.getSpaceByPrettyName(SpaceUtils.cleanString(spacePrettyBaseName) + "0"));
      assertEquals(null, identityStorage.findIdentity(SpaceIdentityProvider.NAME, spacePrettyBaseName.toLowerCase() + "0"));
    }

  }

  private void cleanIdentity(String prefix, int number) {

    for (int i = 0; i < number; ++i) {
      users.add(prefix + i);
    }

  }

  private void cleanSpace(String prefix, int number) {

    for (int i = 0; i < number; ++i) {
      spaces.add(prefix + i);
    }

  }
  
}
