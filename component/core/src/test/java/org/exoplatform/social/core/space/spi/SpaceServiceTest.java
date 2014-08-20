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
package org.exoplatform.social.core.space.spi;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceListAccess;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class SpaceServiceTest extends AbstractCoreTest {
  private IdentityStorage identityStorage;
  private List<Space> tearDownSpaceList;
  private List<Identity> tearDownUserList;

  private final Log       LOG = ExoLogger.getLogger(SpaceServiceTest.class);

  private Identity demo;
  private Identity tom;
  private Identity raul;
  private Identity ghost;
  private Identity dragon;
  private Identity register1;
  private Identity john;
  private Identity mary;
  private Identity harry;
  private Identity root;
  private Identity jame;
  private Identity paul;
  private Identity hacker;
  private Identity hearBreaker;
  private Identity newInvitedUser;
  private Identity newPendingUser;
  private Identity user_new;
  private Identity user_new1;
  private Identity user_new_dot;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    tearDownSpaceList = new ArrayList<Space>();
    tearDownUserList = new ArrayList<Identity>();
    
    user_new = new Identity(OrganizationIdentityProvider.NAME, "user-new");
    user_new1 = new Identity(OrganizationIdentityProvider.NAME, "user-new.1");
    user_new_dot = new Identity(OrganizationIdentityProvider.NAME, "user.new");
    demo = new Identity(OrganizationIdentityProvider.NAME, "demo");
    tom = new Identity(OrganizationIdentityProvider.NAME, "tom");
    raul = new Identity(OrganizationIdentityProvider.NAME, "raul");
    ghost = new Identity(OrganizationIdentityProvider.NAME, "ghost");
    dragon = new Identity(OrganizationIdentityProvider.NAME, "dragon");
    register1 = new Identity(OrganizationIdentityProvider.NAME, "register1");
    mary = new Identity(OrganizationIdentityProvider.NAME, "mary");
    john = new Identity(OrganizationIdentityProvider.NAME, "john");
    harry = new Identity(OrganizationIdentityProvider.NAME, "harry");
    root = new Identity(OrganizationIdentityProvider.NAME, "root");
    jame = new Identity(OrganizationIdentityProvider.NAME, "jame");
    paul = new Identity(OrganizationIdentityProvider.NAME, "paul");
    hacker = new Identity(OrganizationIdentityProvider.NAME, "hacker");
    hearBreaker = new Identity(OrganizationIdentityProvider.NAME, "hearBreaker");
    newInvitedUser = new Identity(OrganizationIdentityProvider.NAME, "newInvitedUser");
    newPendingUser = new Identity(OrganizationIdentityProvider.NAME, "newPendingUser");

    identityStorage.saveIdentity(demo);
    identityStorage.saveIdentity(tom);
    identityStorage.saveIdentity(raul);
    identityStorage.saveIdentity(ghost);
    identityStorage.saveIdentity(dragon);
    identityStorage.saveIdentity(register1);
    identityStorage.saveIdentity(mary);
    identityStorage.saveIdentity(harry);
    identityStorage.saveIdentity(john);
    identityStorage.saveIdentity(root);
    identityStorage.saveIdentity(jame);
    identityStorage.saveIdentity(paul);
    identityStorage.saveIdentity(hacker);
    identityStorage.saveIdentity(hearBreaker);
    identityStorage.saveIdentity(newInvitedUser);
    identityStorage.saveIdentity(newPendingUser);
    identityStorage.saveIdentity(user_new1);
    identityStorage.saveIdentity(user_new);
    identityStorage.saveIdentity(user_new_dot);

    tearDownUserList = new ArrayList<Identity>();
    tearDownUserList.add(demo);
    tearDownUserList.add(tom);
    tearDownUserList.add(raul);
    tearDownUserList.add(ghost);
    tearDownUserList.add(dragon);
    tearDownUserList.add(register1);
    tearDownUserList.add(mary);
    tearDownUserList.add(harry);
    tearDownUserList.add(john);
    tearDownUserList.add(root);
    tearDownUserList.add(jame);
    tearDownUserList.add(paul);
    tearDownUserList.add(hacker);
    tearDownUserList.add(hearBreaker);
    tearDownUserList.add(newInvitedUser);
    tearDownUserList.add(newPendingUser);
    tearDownUserList.add(user_new1);
    tearDownUserList.add(user_new);
    tearDownUserList.add(user_new_dot);
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();

    for (Identity identity : tearDownUserList) {
      identityStorage.deleteIdentity(identity);
    }
    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityStorage.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }
    super.tearDown();
  }

  /**
   * Test {@link SpaceService#getAllSpaces()}
   *
   * @throws Exception
   */
  public void testGetAllSpaces() throws Exception {
    tearDownSpaceList.add(populateData());
    tearDownSpaceList.add(createMoreSpace("Space2"));
    assertEquals(2, spaceService.getAllSpaces().size());
  }

  /**
   * Test {@link SpaceService#getAllSpacesWithListAccess()}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAllSpacesWithListAccess() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> allSpaces = spaceService.getAllSpacesWithListAccess();
    assertNotNull("allSpaces must not be null", allSpaces);
    assertEquals("allSpaces.getSize() must return: " + count, count, allSpaces.getSize());
    assertEquals("allSpaces.load(0, 1).length must return: 1", 1, allSpaces.load(0, 1).length);
    assertEquals("allSpaces.load(0, count).length must return: " + count, count, allSpaces.load(0, count).length);
  }

  /**
   * Test {@link SpaceService#getSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSpacesByUserId() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    List<Space> memberSpaces = spaceService.getSpaces("raul");
    assertNotNull("memberSpaces must not be null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + count, count, memberSpaces.size());

    memberSpaces = spaceService.getSpaces("ghost");
    assertNotNull("memberSpaces must not be null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + count, count, memberSpaces.size());

    memberSpaces = spaceService.getSpaces("dragon");
    assertNotNull("memberSpaces must not be null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + count, count, memberSpaces.size());

    memberSpaces = spaceService.getSpaces("nobody");
    assertNotNull("memberSpaces must not be null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + 0, 0, memberSpaces.size());
  }

  /**
   * Test {@link SpaceService#getSpaceByDisplayName(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceByDisplayName() throws Exception {
    Space space = populateData();
    tearDownSpaceList.add(space);
    Space gotSpace1 = spaceService.getSpaceByDisplayName("Space1");

    assertNotNull("gotSpace1 must not be null", gotSpace1);

    assertEquals(space.getDisplayName(), gotSpace1.getDisplayName());
  }

  public void testGetSpaceByName() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    Space foundSpace = spaceService.getSpaceByName("my_space_10");
    assertNotNull("foundSpace must not be null", foundSpace);
    assertEquals("foundSpace.getDisplayName() must return: my space 10", "my space 10", foundSpace.getDisplayName());
    assertEquals("foundSpace.getPrettyName() must return: my_space_10", "my_space_10", foundSpace.getPrettyName());

    foundSpace = spaceService.getSpaceByName("my_space_0");
    assertNotNull("foundSpace must not be null", foundSpace);
    assertEquals("foundSpace.getDisplayName() must return: my space 0", "my space 0", foundSpace.getDisplayName());
    assertEquals("foundSpace.getPrettyName() must return: my_space_0", "my_space_0", foundSpace.getPrettyName());

    foundSpace = spaceService.getSpaceByName("my_space_20");
    assertNull("foundSpace must be null", foundSpace);
  }

  /**
   * Test {@link SpaceService#getSpaceByPrettyName(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSpaceByPrettyName() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    Space foundSpace = spaceService.getSpaceByPrettyName("my_space_10");
    assertNotNull("foundSpace must not be null", foundSpace);
    assertEquals("foundSpace.getDisplayName() must return: my space 10", "my space 10", foundSpace.getDisplayName());
    assertEquals("foundSpace.getPrettyName() must return: my_space_10", "my_space_10", foundSpace.getPrettyName());

    foundSpace = spaceService.getSpaceByPrettyName("my_space_0");
    assertNotNull("foundSpace must not be null", foundSpace);
    assertEquals("foundSpace.getDisplayName() must return: my space 0", "my space 0", foundSpace.getDisplayName());
    assertEquals("foundSpace.getPrettyName() must return: my_space_0", "my_space_0", foundSpace.getPrettyName());

    foundSpace = spaceService.getSpaceByPrettyName("my_space_20");
    assertNull("foundSpace must be null", foundSpace);
  }

  /**
   * Test {@link SpaceService#getSpacesByFirstCharacterOfName(String)}
   *
   * @throws Exception
   */
  public void testGetSpacesByFirstCharacterOfName() throws Exception {
    tearDownSpaceList.add(populateData());
    tearDownSpaceList.add(createMoreSpace("Space2"));
    assertEquals(2, spaceService.getSpacesByFirstCharacterOfName("S").size());
  }

  /**
   * Test {@link SpaceService#getAllSpacesByFilter(SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAllSpacesByFilterWithFirstCharacterOfSpaceName() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter('m'));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter('M'));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
    assertEquals("foundSpaceListAccess.load(0, 1).length must return: 1", 1, foundSpaceListAccess.load(0, 1).length);
    assertEquals("foundSpaceListAccess.load(0, count).length must return: " + count,
                 count, foundSpaceListAccess.load(0, count).length);
    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter('H'));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + 0, 0, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter('k'));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + 0, 0, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter('*'));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + 0, 0, foundSpaceListAccess.getSize());
  }

  /**
   * Test {@link SpaceService#getSpacesBySearchCondition(String)}
   *
   * @throws Exception
   */
  public void testGetSpacesBySearchCondition() throws Exception {
    tearDownSpaceList.add(populateData());
    tearDownSpaceList.add(createMoreSpace("Space2"));
    assertEquals(2, spaceService.getSpacesBySearchCondition("Space").size());
    assertEquals(1, spaceService.getSpacesBySearchCondition("1").size());
  }

  /**
   * Test {@link SpaceService#getAllSpacesByFilter(SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAllSpacesByFilterWithSpaceNameSearchCondition() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }

    ListAccess<Space> foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("my space"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
    assertEquals("foundSpaceListAccess.load(0, 1).length must return: 1", 1, foundSpaceListAccess.load(0, 1).length);
    assertEquals("foundSpaceListAccess.load(0, count).length must return: " + count,
                 count, foundSpaceListAccess.load(0, count).length);

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("1"));
    assertEquals("foundSpaceListAccess.getSize() must return 11", 11, foundSpaceListAccess.getSize());
    assertEquals("foundSpaceListAccess.load(0, 10).length must return 10",
                 10, foundSpaceListAccess.load(0, 10).length);

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("add new space"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("space"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("*space"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("*space*"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("*a*e*"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("*a*e"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("a*e"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("a*"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("%a%e%"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("%a*e%"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("%a*e*"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("***"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + 0, 0, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("%%%%%"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + 0, 0, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("new"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + count, count, foundSpaceListAccess.getSize());
    
    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("<new>new(\"new\")</new>"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    //correct test case : the term  "new new new new" should not match the result
    assertEquals("foundSpaceListAccess.getSize() must return: " + 0, 0, foundSpaceListAccess.getSize());

    foundSpaceListAccess = spaceService.getAllSpacesByFilter(new SpaceFilter("what new space add"));
    assertNotNull("foundSpaceListAccess must not be null", foundSpaceListAccess);
    assertEquals("foundSpaceListAccess.getSize() must return: " + 0, 0, foundSpaceListAccess.getSize());
  }

  /**
   * Test {@link SpaceService#getSpaceByGroupId(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSpaceByGroupId() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    Space foundSpace = spaceService.getSpaceByGroupId("/space/space0");
    assertNotNull("foundSpace must not be null", foundSpace);
    assertEquals("foundSpace.getDisplayName() must return: my space 0", "my space 0", foundSpace.getDisplayName());
    assertEquals("foundSpace.getGroupId() must return: /space/space0", "/space/space0", foundSpace.getGroupId());
  }


  /**
   * Test {@link SpaceService#getSpaceById(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceById() throws Exception {
    Space space = populateData();
    tearDownSpaceList.add(space);
    tearDownSpaceList.add(createMoreSpace("Space2"));
    assertEquals(space.getDisplayName(), spaceService.getSpaceById(space.getId()).getDisplayName());
  }

  /**
   * Test {@link SpaceService#getSpaceByUrl(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceByUrl() throws Exception {
    Space space = populateData();
    tearDownSpaceList.add(space);
    assertEquals(space.getDisplayName(), spaceService.getSpaceByUrl("space1").getDisplayName());
  }

  /**
   * Test {@link SpaceService#getEditableSpaces(String)}
   *
   * @throws Exception
   */
  public void testGetEditableSpaces() throws Exception {
    tearDownSpaceList.add(populateData());
    assertEquals(1, spaceService.getEditableSpaces("root").size());
  }

  /**
   * Test {@link SpaceService#getSettingableSpaces(String))}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSettingableSpaces() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> editableSpaceListAccess = spaceService.getSettingableSpaces("demo");
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + count, count, editableSpaceListAccess.getSize());
    assertEquals("editableSpaceListAccess.load(0, 1).length must return: 1",
                 1, editableSpaceListAccess.load(0, 1).length);
    assertEquals("editableSpaceListAccess.load(0, count).length must return: " + count,
                 count, editableSpaceListAccess.load(0, count).length);

    editableSpaceListAccess = spaceService.getSettingableSpaces("tom");
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + count, count, editableSpaceListAccess.getSize());

    editableSpaceListAccess = spaceService.getSettingableSpaces("root");
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + count, count, editableSpaceListAccess.getSize());

    editableSpaceListAccess = spaceService.getSettingableSpaces("raul");
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + 0, 0, editableSpaceListAccess.getSize());

    editableSpaceListAccess = spaceService.getSettingableSpaces("ghost");
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + 0, 0, editableSpaceListAccess.getSize());
  }

  /**
   * Test {@link SpaceService#getSettingabledSpacesByFilter(String, SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSettingableSpacesByFilter() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> editableSpaceListAccess = spaceService.getSettingabledSpacesByFilter("demo", new SpaceFilter("add"));
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + count, count, editableSpaceListAccess.getSize());
    assertEquals("editableSpaceListAccess.load(0, 1).length must return: 1",
                 1, editableSpaceListAccess.load(0, 1).length);
    assertEquals("editableSpaceListAccess.load(0, count).length must return: " + count,
                 count, editableSpaceListAccess.load(0, count).length);
    editableSpaceListAccess = spaceService.getSettingabledSpacesByFilter(demo.getRemoteId(), new SpaceFilter("19"));
    assertEquals("editableSpaceListAccess.getSize() must return 1", 1, editableSpaceListAccess.getSize());
    assertEquals("editableSpaceListAccess.load(0, 1).length must return 1",
                 1, editableSpaceListAccess.load(0, 1).length);

    editableSpaceListAccess = spaceService.getSettingabledSpacesByFilter("demo", new SpaceFilter("my"));
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + count, count, editableSpaceListAccess.getSize());

    editableSpaceListAccess = spaceService.getSettingabledSpacesByFilter("demo", new SpaceFilter("new"));
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + count, count, editableSpaceListAccess.getSize());

    editableSpaceListAccess = spaceService.getSettingabledSpacesByFilter("demo", new SpaceFilter('m'));
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + count, count, editableSpaceListAccess.getSize());

    editableSpaceListAccess = spaceService.getSettingabledSpacesByFilter("demo", new SpaceFilter('M'));
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + count, count, editableSpaceListAccess.getSize());

    editableSpaceListAccess = spaceService.getSettingabledSpacesByFilter("demo", new SpaceFilter('k'));
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + 0, 0, editableSpaceListAccess.getSize());

    editableSpaceListAccess = spaceService.getSettingabledSpacesByFilter("tom", new SpaceFilter("new"));
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + count, count, editableSpaceListAccess.getSize());

    editableSpaceListAccess = spaceService.getSettingabledSpacesByFilter("root", new SpaceFilter("space"));
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + count, count, editableSpaceListAccess.getSize());

    editableSpaceListAccess = spaceService.getSettingabledSpacesByFilter("raul", new SpaceFilter("my"));
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + 0, 0, editableSpaceListAccess.getSize());

    editableSpaceListAccess = spaceService.getSettingabledSpacesByFilter("ghost", new SpaceFilter("space"));
    assertNotNull("editableSpaceListAccess must not be null", editableSpaceListAccess);
    assertEquals("editableSpaceListAccess.getSize() must return: " + 0, 0, editableSpaceListAccess.getSize());
  }

  /**
   * Test {@link SpaceService#getInvitedSpaces(String)}
   *
   * @throws Exception
   */
  public void testGetInvitedSpaces() throws Exception {
    tearDownSpaceList.add(populateData());
    assertEquals(0, spaceService.getInvitedSpaces("root").size());
    Space space = spaceService.getSpaceByDisplayName("Space1");
    spaceService.inviteMember(space, "root");
    assertEquals(1, spaceService.getInvitedSpaces("root").size());
  }

  /**
   * Test {@link SpaceService#getInvitedSpacesWithListAccess(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesWithListAccess() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> invitedSpaces = spaceService.getInvitedSpacesWithListAccess("register1");
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());
    assertEquals("invitedSpaces.load(0, 1).length must return: " + 1, 1, invitedSpaces.load(0, 1).length);
    assertEquals("invitedSpaces.load(0, count).length must return: " + count,
                 count, invitedSpaces.load(0, count).length);
    invitedSpaces = spaceService.getInvitedSpacesWithListAccess("mary");
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesWithListAccess("demo");
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + 0, 0, invitedSpaces.getSize());

  }

  /**
   * Test {@link SpaceService#getInvitedSpacesByFilter(String, SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesByFilterWithSpaceNameSearchCondition() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> invitedSpaces = spaceService.getInvitedSpacesByFilter("register1", new SpaceFilter("my space"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter(register1.getRemoteId(), new SpaceFilter("12"));
    assertEquals("invitedSpaces.getSize() must return 1", 1, invitedSpaces.getSize());
    assertEquals("invitedSpaces.load(0, 1).length must return 1", 1, invitedSpaces.load(0, 1).length);

    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter("my"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());
    assertEquals("invitedSpaces.load(0, 1).length must return: 1",
                 1, invitedSpaces.load(0, 1).length);
    assertEquals("invitedSpaces.load(0, count).length must return: " + count,
                 count, invitedSpaces.load(0, count).length);

    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter("*my"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter("*my*"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter("*my*e*"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter("%my%e%"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter("%my%e"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter("%my*e%"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter("%my*e*"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter("****"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + 0, 0, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter("%%%%%"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + 0, 0, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("demo", new SpaceFilter("my space"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + 0, 0, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("demo", new SpaceFilter("add new"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + 0, 0, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("john", new SpaceFilter("space"));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + 0, 0, invitedSpaces.getSize());
  }

  /**
   * Test {@link SpaceService#getInvitedSpacesByFilter(String, SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesByFilterWithFirstCharacterOfSpaceName() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> invitedSpaces = spaceService.getInvitedSpacesByFilter("register1", new SpaceFilter('m'));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());
    assertEquals("invitedSpaces.load(0, 1).length must return: 1", 1, invitedSpaces.load(0, 1).length);
    assertEquals("invitedSpaces.load(0, count).length must return: " + count,
                 count, invitedSpaces.load(0, count).length);
    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter('M'));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + count, count, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("mary", new SpaceFilter('H'));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + 0, 0, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("demo", new SpaceFilter('m'));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + 0, 0, invitedSpaces.getSize());

    invitedSpaces = spaceService.getInvitedSpacesByFilter("john", new SpaceFilter('M'));
    assertNotNull("invitedSpaces must not be null", invitedSpaces);
    assertEquals("invitedSpaces.getSize() must return: " + 0, 0, invitedSpaces.getSize());
  }

  /**
   * Test {@link SpaceService#getPublicSpaces(String)}
   *
   * @throws Exception
   */
  public void testGetPublicSpaces() throws Exception {
    tearDownSpaceList.add(populateData());
    assertEquals(0, spaceService.getPublicSpaces("root").size());
  }

  /**
   * Test {@link SpaceService#getPublicSpacesWithListAccess(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPublicSpacesWithListAccess() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> foundSpaces = spaceService.getPublicSpacesWithListAccess("tom");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: 0", 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesWithListAccess("hacker");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: 0", 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesWithListAccess("mary");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: 0", 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesWithListAccess("root");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: 0", 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesWithListAccess("nobody");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: 20", count, foundSpaces.getSize());
    assertEquals("foundSpaces.load(0, 1).length must return: 1", 1, foundSpaces.load(0, 1).length);
    assertEquals("foundSpaces.load(0, 20).length must return: 20",
                 20, foundSpaces.load(0, 20).length);
    foundSpaces = spaceService.getPublicSpacesWithListAccess("bluray");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: 20", count, foundSpaces.getSize());
  }


  /**
   * Test {@link SpaceService#getPublicSpacesByFilter(String, SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPublicSpacesByFilterWithSpaceNameSearchCondition() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    String nameSpace = "my space";
    ListAccess<Space> foundSpaces = spaceService.getPublicSpacesByFilter("tom", new SpaceFilter(nameSpace));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: 0", 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter(nameSpace));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());
    assertEquals("foundSpaces.load(0, 1).length must return: 1", 1, foundSpaces.load(0, 1).length);
    assertEquals("foundSpaces.load(0, count).length must return: " + count,
                 count, foundSpaces.load(0, count).length);
    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter("*m"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter("m*"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter("*my*"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter("*my*e"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter("*my*e*"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter("%my%e%"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter("%my*e%"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter("*my%e%"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter("***"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter("%%%"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());

    nameSpace = "my space 1";
    foundSpaces = spaceService.getPublicSpacesByFilter("stranger", new SpaceFilter(""));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());

    nameSpace = "my space 20";
    foundSpaces = spaceService.getPublicSpacesByFilter("hearBreaker", new SpaceFilter(nameSpace));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());
  }

  /**
   * Test {@link SpaceService#getPublicSpacesByFilter(String, SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPublicSpacesByFilterWithFirstCharacterOfSpaceName() throws Exception {
    int count = 10;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> foundSpaces = spaceService.getPublicSpacesByFilter("stranger", new SpaceFilter('m'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("stranger", new SpaceFilter('M'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("root", new SpaceFilter('M'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("stranger", new SpaceFilter('*'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("tom", new SpaceFilter('M'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: 0", 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("stranger", new SpaceFilter('y'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPublicSpacesByFilter("stranger", new SpaceFilter('H'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());

    ListAccess<Space> johnPublicSpaces = spaceService.getPublicSpacesByFilter("john", new SpaceFilter('m'));
    assertEquals("johnPublicSpaces.getSize() must return: 10", 10, johnPublicSpaces.getSize());
    assertEquals("johnPublicSpaces.load(0, 1).length must return: 1", 1, johnPublicSpaces.load(0, 1).length);
    Space[] johnPublicSpacesArray = johnPublicSpaces.load(0, 10);
    assertEquals("johnPublicSpaces.load(0, 10).length must return 10", 10, johnPublicSpacesArray.length);
    assertNotNull("johnPublicSpacesArray[0].getId() must not be null", johnPublicSpacesArray[0].getId());
    assertNotNull("johnPublicSpacesArray[0].getPrettyName() must not be null",
                  johnPublicSpacesArray[0].getPrettyName());
  }

  /**
   * Test {@link SpaceService#getAccessibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpaces() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    List<Space> accessibleSpaces = spaceService.getAccessibleSpaces("demo");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + count, count, accessibleSpaces.size());

    accessibleSpaces = spaceService.getAccessibleSpaces("tom");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + count, count, accessibleSpaces.size());

    accessibleSpaces = spaceService.getAccessibleSpaces("root");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 0, 0, accessibleSpaces.size());

    accessibleSpaces = spaceService.getAccessibleSpaces("dragon");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + count, count, accessibleSpaces.size());

    accessibleSpaces = spaceService.getAccessibleSpaces("hellgate");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 0, 0, accessibleSpaces.size());
  }

  /**
   * Test {@link SpaceService#getAccessibleSpacesWithListAccess(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpacesWithListAccess() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> accessibleSpaces = spaceService.getAccessibleSpacesWithListAccess("demo");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());
    assertEquals("accessibleSpaces.load(0, 1).length must return: 1", 1, accessibleSpaces.load(0, 1).length);
    assertEquals("accessibleSpaces.load(0, count).length must return: " + count,
                 count, accessibleSpaces.load(0, count).length);

    accessibleSpaces = spaceService.getAccessibleSpacesWithListAccess("tom");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesWithListAccess("root");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + 0, 0, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesWithListAccess("dragon");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesWithListAccess("ghost");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesWithListAccess("raul");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesWithListAccess("mary");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + 0, 0, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesWithListAccess("john");
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + 0, 0, accessibleSpaces.getSize());
  }

  /**
   * Test {@link SpaceService#getAccessibleSpacesByFilter(String, SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpacesByFilterWithSpaceNameSearchCondition() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> accessibleSpaces = spaceService.getAccessibleSpacesByFilter("demo", new SpaceFilter("my"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());
    assertEquals("accessibleSpaces.load(0, 1).length must return: 1", 1, accessibleSpaces.load(0, 1).length);
    assertEquals("accessibleSpaces.load(0, count).length must return: " + count,
                 count, accessibleSpaces.load(0, count).length);

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("tom", new SpaceFilter("space"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter("space"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter("*space"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter("space*"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter("*space*"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter("*a*e*"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter("%a%e%"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter("%a*e%"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter("%a*e*"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter("*****"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + 0, 0, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter("%%%%%%%"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + 0, 0, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter("add new"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("dragon", new SpaceFilter("my space"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("dragon", new SpaceFilter("add new"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("ghost", new SpaceFilter("my space "));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("hellgate", new SpaceFilter("my space"));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + 0, 0, accessibleSpaces.getSize());
  }

  /**
   * Test {@link SpaceService#getAccessibleSpacesByFilter(String, SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpacesByFilterWithFirstCharacterOfSpaceName() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> accessibleSpaces = spaceService.getAccessibleSpacesByFilter("demo", new SpaceFilter('m'));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());
    assertEquals("accessibleSpaces.load(0, 1).length must return: 1", 1, accessibleSpaces.load(0, 1).length);
    assertEquals("accessibleSpaces.load(0, count).length must return: " + count,
                 count, accessibleSpaces.load(0, count).length);
    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("tom", new SpaceFilter('M'));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter('M'));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("root", new SpaceFilter('*'));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + 0, 0, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("tom", new SpaceFilter('h'));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + 0, 0, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("dragon", new SpaceFilter('m'));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("ghost", new SpaceFilter('M'));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + count, count, accessibleSpaces.getSize());

    accessibleSpaces = spaceService.getAccessibleSpacesByFilter("hellgate", new SpaceFilter('m'));
    assertNotNull("accessibleSpaces must not be null", accessibleSpaces);
    assertEquals("accessibleSpaces.getSize() must return: " + 0, 0, accessibleSpaces.getSize());
  }

  /**
   * Test {@link SpaceService#getSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSpaces() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }

    List<Space> memberSpaceListAccess = spaceService.getSpaces("raul");
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.size());

    memberSpaceListAccess = spaceService.getSpaces("ghost");
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.size());

    memberSpaceListAccess = spaceService.getSpaces("dragon");
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.size());

    memberSpaceListAccess = spaceService.getSpaces("root");
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + 0, 0, memberSpaceListAccess.size());
  }

  /**
   * Test {@link SpaceService#getMemberSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetMemberSpaces() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }

    ListAccess<Space> memberSpaceListAccess = spaceService.getMemberSpaces("raul");
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.getSize());
    assertEquals("memberSpaceListAccess.load(0, 1).length must return: 1",
                 1, memberSpaceListAccess.load(0, 1).length);
    assertEquals("memberSpaceListAccess.load(0, count).length must return: " + count,
                 count, memberSpaceListAccess.load(0, count).length);
    memberSpaceListAccess = spaceService.getMemberSpaces("ghost");
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.getSize());

    memberSpaceListAccess = spaceService.getMemberSpaces("dragon");
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.getSize());

    memberSpaceListAccess = spaceService.getMemberSpaces("root");
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + 0, 0, memberSpaceListAccess.getSize());
  }

  /**
   * Test {@link SpaceService#getMemberSpacesByFilter(String, SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetMemberSpacesByFilter() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }

    ListAccess<Space> memberSpaceListAccess = spaceService.getMemberSpacesByFilter("raul", new SpaceFilter("add"));
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.getSize());
    assertEquals("memberSpaceListAccess.load(0, 1).length must return: 1",
                 1, memberSpaceListAccess.load(0, 1).length);
    assertEquals("memberSpaceListAccess.load(0, count).length must return: " + count,
                 count, memberSpaceListAccess.load(0, count).length);

    memberSpaceListAccess = spaceService.getMemberSpacesByFilter("raul", new SpaceFilter("new"));
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.getSize());

    memberSpaceListAccess = spaceService.getMemberSpacesByFilter("raul", new SpaceFilter("space"));
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.getSize());

    memberSpaceListAccess = spaceService.getMemberSpacesByFilter("raul", new SpaceFilter("my"));
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.getSize());

    memberSpaceListAccess = spaceService.getMemberSpacesByFilter("raul", new SpaceFilter('m'));
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.getSize());

    memberSpaceListAccess = spaceService.getMemberSpacesByFilter("raul", new SpaceFilter('M'));
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.getSize());

    memberSpaceListAccess = spaceService.getMemberSpacesByFilter("raul", new SpaceFilter('k'));
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + 0, 0, memberSpaceListAccess.getSize());

    memberSpaceListAccess = spaceService.getMemberSpacesByFilter("ghost", new SpaceFilter("my"));
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.getSize());

    memberSpaceListAccess = spaceService.getMemberSpacesByFilter("dragon", new SpaceFilter("space"));
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + count, count, memberSpaceListAccess.getSize());

    memberSpaceListAccess = spaceService.getMemberSpacesByFilter("root", new SpaceFilter("my space"));
    assertNotNull("memberSpaceListAccess must not be null", memberSpaceListAccess);
    assertEquals("memberSpaceListAccess.size() must return: " + 0, 0, memberSpaceListAccess.getSize());
  }

  /**
   * Test {@link SpaceService#getPendingSpaces(String)}
   *
   * @throws Exception
   */
  public void testGetPendingSpaces() throws Exception {
    tearDownSpaceList.add(populateData());
    Space space = spaceService.getSpaceByDisplayName("Space1");
    spaceService.requestJoin(space, "root");
    assertEquals(true, spaceService.isPending(space, "root"));
  }

  /**
   * Test {@link SpaceService#getPendingSpacesWithListAccess(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesWithListAccess() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> foundSpaces = spaceService.getPendingSpacesWithListAccess("jame");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());
    assertEquals("foundSpaces.load(0, 1).length must return: 1",
                 1, foundSpaces.load(0, 1).length);
    assertEquals("foundSpaces.load(0, count).length must return: " + count,
                 count, foundSpaces.load(0, count).length);

    foundSpaces = spaceService.getPendingSpacesWithListAccess("paul");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesWithListAccess("hacker");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesWithListAccess("ghost");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesWithListAccess("hellgate");
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());
  }

  /**
   * Test {@link SpaceService#getPendingSpacesByFilter(String, SpaceFilter))}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesByFilterWithSpaceNameSearchCondition() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    String nameSpace = "my space";
    ListAccess<Space> foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter(nameSpace));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());
    assertEquals("foundSpaces.load(0, 1).length must return: 1",
                 1, foundSpaces.load(0, 1).length);
    assertEquals("foundSpaces.load(0, count).length must return: " + count,
                 count, foundSpaces.load(0, count).length);

    foundSpaces = spaceService.getPendingSpacesByFilter("paul", new SpaceFilter(nameSpace));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("hacker", new SpaceFilter("space"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter("add new"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter("add*"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter("*add*"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter("*add"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter("*add*e"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter("*add*e*"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter("%add%e%"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter("%add*e%"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter("%add*e*"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter("no space"));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: 0", 0, foundSpaces.getSize());
  }

  /**
   * Test {@link SpaceService#getPendingSpacesByFilter(String, SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesByFilterWithFirstCharacterOfSpaceName() throws Exception {
    int count = 20;
    for (int i = 0; i < count; i ++) {
      tearDownSpaceList.add(this.getSpaceInstance(i));
    }
    ListAccess<Space> foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter('m'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());
    assertEquals("foundSpaces.load(0, 1).length must return: 1",
                 1, foundSpaces.load(0, 1).length);
    assertEquals("foundSpaces.load(0, count).length must return: " + count,
                 count, foundSpaces.load(0, count).length);

    foundSpaces = spaceService.getPendingSpacesByFilter("paul", new SpaceFilter('M'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + count, count, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter('*'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: " + 0, 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter('H'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: 0", 0, foundSpaces.getSize());

    foundSpaces = spaceService.getPendingSpacesByFilter("jame", new SpaceFilter('k'));
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.getSize() must return: 0", 0, foundSpaces.getSize());
  }

  /**
   * Test {@link SpaceService#createSpace(Space, String)}
   *
   * @throws Exception
   */
  public void testCreateSpace() throws Exception {
    tearDownSpaceList.add(populateData());
    tearDownSpaceList.add(createMoreSpace("Space2"));
    ListAccess<Space> spaceListAccess = spaceService.getAllSpacesWithListAccess();
    assertNotNull("spaceListAccess must not be null", spaceListAccess);
    assertEquals("spaceListAccess.getSize() must return: 2", 2, spaceListAccess.getSize());
  }

  /**
   * Test {@link SpaceService#saveSpace(Space, boolean)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testSaveSpace() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    String spaceDisplayName = space.getDisplayName();
    String spaceDescription = space.getDescription();
    String groupId = space.getGroupId();
    Space savedSpace = spaceService.getSpaceByDisplayName(spaceDisplayName);
    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals("savedSpace.getDisplayName() must return: " + spaceDisplayName, spaceDisplayName, savedSpace.getDisplayName());
    assertEquals("savedSpace.getDescription() must return: " + spaceDescription, spaceDescription, savedSpace.getDescription());
    assertEquals("savedSpace.getGroupId() must return: " + groupId, groupId, savedSpace.getGroupId());
    assertEquals(null, savedSpace.getAvatarUrl());
  }

  /**
   * Test {@link SpaceService#renameSpace(Space, String)}
   *
   * @throws Exception
   * @since 1.2.8
   */
  public void testRenameSpace() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    
    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    identityStorage.saveIdentity(identity);
    tearDownUserList.add(identity);
    
    String newDisplayName = "new display name";
    
    spaceService.renameSpace(space, newDisplayName);
    
    Space got = spaceService.getSpaceById(space.getId());
    assertEquals(newDisplayName, got.getDisplayName());
    
    {
      newDisplayName = "new display name with super admin";
      
      //
      spaceService.renameSpace(root.getRemoteId(), space, newDisplayName);
      
      got = spaceService.getSpaceById(space.getId());
      assertEquals(newDisplayName, got.getDisplayName());
    }
    
    {
      newDisplayName = "new display name with normal admin";
      
      //
      spaceService.renameSpace(mary.getRemoteId(), space, newDisplayName);
      
      got = spaceService.getSpaceById(space.getId());
      assertEquals(newDisplayName, got.getDisplayName());
    }
    
    {
      newDisplayName = "new display name with null remoteId";
      
      //
      spaceService.renameSpace(null, space, newDisplayName);
      
      got = spaceService.getSpaceById(space.getId());
      assertEquals(newDisplayName, got.getDisplayName());
    }
    
    Identity savedIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
    assertNotNull(savedIdentity);
  }
  
  /**
   * Test {@link SpaceService#saveSpace(Space, boolean)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testUpdateSpaceAvatar() throws Exception {

    Space space = this.getSpaceInstance(0);
    Identity spaceIdentity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    identityStorage.saveIdentity(spaceIdentity);

    tearDownSpaceList.add(space);
    tearDownUserList.add(spaceIdentity);

    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment =
        new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
    space.setAvatarAttachment(avatarAttachment);

    spaceService.updateSpaceAvatar(space);
    spaceService.updateSpace(space);

    Space savedSpace = spaceService.getSpaceById(space.getId());
    assertFalse(savedSpace.getAvatarUrl() == null);
    String avatarRandomURL = savedSpace.getAvatarUrl();
    int indexOfRandomVar = avatarRandomURL.indexOf("/?upd=");

    String avatarURL = null;
    if(indexOfRandomVar != -1){
      avatarURL = avatarRandomURL.substring(0,indexOfRandomVar);
    } else {
      avatarURL = avatarRandomURL;
  }
    assertEquals(LinkProvider.escapeJCRSpecialCharacters(
            String.format(
              "/rest/jcr/repository/portal-test/production/soc:providers/soc:space/soc:%s/soc:profile/soc:avatar",
              space.getPrettyName())
            ),avatarURL);

  }

  /**
   * Test {@link SpaceService#deleteSpace(Space)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testDeleteSpace() throws Exception {
    Space space = this.getSpaceInstance(0);
    String spaceDisplayName = space.getDisplayName();
    String spaceDescription = space.getDescription();
    String groupId = space.getGroupId();
    Space savedSpace = spaceService.getSpaceByDisplayName(spaceDisplayName);
    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals("savedSpace.getDisplayName() must return: " + spaceDisplayName, spaceDisplayName, savedSpace.getDisplayName());
    assertEquals("savedSpace.getDescription() must return: " + spaceDescription, spaceDescription, savedSpace.getDescription());
    assertEquals("savedSpace.getGroupId() must return: " + groupId, groupId, savedSpace.getGroupId());
    spaceService.deleteSpace(space);
    savedSpace = spaceService.getSpaceByDisplayName(spaceDisplayName);
    assertNull("savedSpace must be null", savedSpace);
  }

  /**
   * Test {@link SpaceService#updateSpace(Space)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testUpdateSpace() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    String spaceDisplayName = space.getDisplayName();
    String spaceDescription = space.getDescription();
    String groupId = space.getGroupId();
    Space savedSpace = spaceService.getSpaceByDisplayName(spaceDisplayName);
    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals("savedSpace.getDisplayName() must return: " + spaceDisplayName, spaceDisplayName, savedSpace.getDisplayName());
    assertEquals("savedSpace.getDescription() must return: " + spaceDescription, spaceDescription, savedSpace.getDescription());
    assertEquals("savedSpace.getGroupId() must return: " + groupId, groupId, savedSpace.getGroupId());

    String updateSpaceDisplayName = "update new space display name";
    space.setDisplayName(updateSpaceDisplayName);
    space.setPrettyName(space.getDisplayName());
    spaceService.updateSpace(space);
    savedSpace = spaceService.getSpaceByDisplayName(updateSpaceDisplayName);
    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals("savedSpace.getDisplayName() must return: " + updateSpaceDisplayName, updateSpaceDisplayName, savedSpace.getDisplayName());
    assertEquals("savedSpace.getDescription() must return: " + spaceDescription, spaceDescription, savedSpace.getDescription());
    assertEquals("savedSpace.getGroupId() must return: " + groupId, groupId, savedSpace.getGroupId());
  }

  /**
   * Test {@link SpaceService#addPendingUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testAddPendingUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.addPendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount + 1, pendingUsersCount + 1, space.getPendingUsers().length);
    assertTrue("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
               ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#removePendingUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRemovePendingUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.addPendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount + 1,
                 pendingUsersCount + 1, space.getPendingUsers().length);
    assertTrue("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
               ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));

    spaceService.removePendingUser(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount,
                 pendingUsersCount, space.getPendingUsers().length);
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
                 ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#isPendingUser(Space, String)}
   *
   * @throws Exception@since 1.2.0-GA
   * @since 1.2.0-GA
   */
  public void testIsPendingUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertTrue("spaceService.isPendingUser(savedSpace, \"jame\") must return true", spaceService.isPendingUser(savedSpace, "jame"));
    assertTrue("spaceService.isPendingUser(savedSpace, \"paul\") must return true", spaceService.isPendingUser(savedSpace, "paul"));
    assertTrue("spaceService.isPendingUser(savedSpace, \"hacker\") must return true", spaceService.isPendingUser(savedSpace, "hacker"));
    assertFalse("spaceService.isPendingUser(savedSpace, \"newpendinguser\") must return false", spaceService.isPendingUser(savedSpace, "newpendinguser"));
  }

  /**
   * Test {@link SpaceService#addInvitedUser(Space, String)}
   *
   * @throws Exception
   *
   */
  public void testAddInvitedUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    int invitedUsersCount = savedSpace.getInvitedUsers().length;
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.addInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount + 1,
                 invitedUsersCount + 1, savedSpace.getInvitedUsers().length);
    assertTrue("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return true",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#removeInvitedUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRemoveInvitedUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    int invitedUsersCount = savedSpace.getInvitedUsers().length;
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.addInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount + 1,
                 invitedUsersCount + 1, savedSpace.getInvitedUsers().length);
    assertTrue("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return true",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.removeInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount,
                 invitedUsersCount, savedSpace.getInvitedUsers().length);
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#isInvitedUser(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsInvitedUser() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertTrue("spaceService.isInvitedUser(savedSpace, \"register1\") must return true", spaceService.isInvitedUser(savedSpace, "register1"));
    assertTrue("spaceService.isInvitedUser(savedSpace, \"mary\") must return true", spaceService.isInvitedUser(savedSpace, "mary"));
    assertFalse("spaceService.isInvitedUser(savedSpace, \"hacker\") must return false", spaceService.isInvitedUser(savedSpace, "hacker"));
    assertFalse("spaceService.isInvitedUser(savedSpace, \"nobody\") must return false", spaceService.isInvitedUser(savedSpace, "nobody"));
  }

  /**
   * Test {@link SpaceService#setManager(Space, String, boolean)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testSetManager() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] {"demo", "tom"};
    String[] members = new String[] {"raul", "ghost", "dragon"};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);
    tearDownSpaceList.add(space);

    //Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    int managers = savedSpace.getManagers().length;
    spaceService.setManager(savedSpace, "demo", true);
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getManagers().length must return: " + managers, managers, savedSpace.getManagers().length);

    spaceService.setManager(savedSpace, "john", true);
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getManagers().length must return: " + managers + 1, managers + 1, savedSpace.getManagers().length);

    spaceService.setManager(savedSpace, "demo", false);
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getManagers().length must return: " + managers, managers, savedSpace.getManagers().length);

    // Wait 3 secs to have activity stored
    try {
      IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
      Thread.sleep(3000);
      List<ExoSocialActivity> broadCastActivities = activityManager.getActivities(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, savedSpace.getPrettyName(), false), 0, 10);
      for (ExoSocialActivity activity : broadCastActivities) {
        activityManager.deleteActivity(activity);
      }
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
  }


  /**
   * Test {@link SpaceService#isManager(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsManager() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertTrue("spaceService.isManager(savedSpace, \"demo\") must return true", spaceService.isManager(savedSpace, "demo"));
    assertTrue("spaceService.isManager(savedSpace, \"tom\") must return true", spaceService.isManager(savedSpace, "tom"));
    assertFalse("spaceService.isManager(savedSpace, \"mary\") must return false", spaceService.isManager(savedSpace, "mary"));
    assertFalse("spaceService.isManager(savedSpace, \"john\") must return false", spaceService.isManager(savedSpace, "john"));
  }

  /**
   * Test {@link SpaceService#isOnlyManager(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsOnlyManager() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertFalse("spaceService.isOnlyManager(savedSpace, \"tom\") must return false", spaceService.isOnlyManager(savedSpace, "tom"));
    assertFalse("spaceService.isOnlyManager(savedSpace, \"demo\") must return false", spaceService.isOnlyManager(savedSpace, "demo"));

    savedSpace.setManagers(new String[] {"demo"});
    spaceService.updateSpace(savedSpace);
    assertTrue("spaceService.isOnlyManager(savedSpace, \"demo\") must return true", spaceService.isOnlyManager(savedSpace, "demo"));
    assertFalse("spaceService.isOnlyManager(savedSpace, \"tom\") must return false", spaceService.isOnlyManager(savedSpace, "tom"));

    savedSpace.setManagers(new String[] {"tom"});
    spaceService.updateSpace(savedSpace);
    assertFalse("spaceService.isOnlyManager(savedSpace, \"demo\") must return false", spaceService.isOnlyManager(savedSpace, "demo"));
    assertTrue("spaceService.isOnlyManager(savedSpace, \"tom\") must return true", spaceService.isOnlyManager(savedSpace, "tom"));
  }

  /**
   * Test {@link SpaceService#hasSettingPermission(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testHasSettingPermission() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    assertTrue("spaceService.hasSettingPermission(savedSpace, \"demo\") must return true", spaceService.hasSettingPermission(savedSpace, "demo"));
    assertTrue("spaceService.hasSettingPermission(savedSpace, \"tom\") must return true", spaceService.hasSettingPermission(savedSpace, "tom"));
    assertTrue("spaceService.hasSettingPermission(savedSpace, \"root\") must return true", spaceService.hasSettingPermission(savedSpace, "root"));
    assertFalse("spaceService.hasSettingPermission(savedSpace, \"mary\") must return false", spaceService.hasSettingPermission(savedSpace, "mary"));
    assertFalse("spaceService.hasSettingPermission(savedSpace, \"john\") must return false", spaceService.hasSettingPermission(savedSpace, "john"));
  }

  /**
   * Test {@link SpaceService#registerSpaceListenerPlugin(org.exoplatform.social.core.space.SpaceListenerPlugin)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRegisterSpaceListenerPlugin() throws Exception {
    //TODO
  }

  /**
   * Test {@link SpaceService#unregisterSpaceListenerPlugin(org.exoplatform.social.core.space.SpaceListenerPlugin)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testUnregisterSpaceListenerPlugin() throws Exception {
    //TODO
  }

  /**
   * Test {@link SpaceService#initApp(Space)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testInitApp() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService#initApps(Space)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testInitApps() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService#deInitApps(Space)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testDeInitApps() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService#addMember(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testAddMember() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] {"demo"};
    String[] members = new String[] {};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);
    tearDownSpaceList.add(space);

    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    spaceService.addMember(savedSpace, "root");
    spaceService.addMember(savedSpace, "mary");
    spaceService.addMember(savedSpace, "john");
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getMembers().length must return 4", 4, savedSpace.getMembers().length);
    // Wait 3 secs to have activity stored
    try {
      IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
      Thread.sleep(3000);
      List<ExoSocialActivity> broadCastActivities = activityManager.getActivities(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, savedSpace.getPrettyName(), false), 0, 10);
      for (ExoSocialActivity activity : broadCastActivities) {
        activityManager.deleteActivity(activity);
      }
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
  }
  
  /**
   * Test {@link SpaceService#addMember(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testAddMemberSpecialCharacter() throws Exception {
    String reg = "^\\p{L}[\\p{L}\\d\\s._,-]+$";
    Pattern pattern = Pattern.compile(reg);
    assertTrue(pattern.matcher("user-new.1").matches());
    assertTrue(pattern.matcher("user.new").matches());
    assertTrue(pattern.matcher("user-new").matches());
  
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] {"demo"};
    String[] members = new String[] {};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);
    tearDownSpaceList.add(space);

    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    spaceService.addMember(savedSpace, "user-new.1");
    spaceService.addMember(savedSpace, "user.new");
    spaceService.addMember(savedSpace, "user-new");
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals(4, savedSpace.getMembers().length);
  }

  
  /**
   * Test {@link SpaceService#removeMember(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRemoveMember() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] {"demo"};
    String[] members = new String[] {};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    spaceService.addMember(savedSpace, "root");
    spaceService.addMember(savedSpace, "mary");
    spaceService.addMember(savedSpace, "john");
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getMembers().length must return 4", 4, savedSpace.getMembers().length);

    spaceService.removeMember(savedSpace, "root");
    spaceService.removeMember(savedSpace, "mary");
    spaceService.removeMember(savedSpace, "john");
    assertEquals("savedSpace.getMembers().length must return 1", 1, savedSpace.getMembers().length);
    // Wait 3 secs to have activity stored
    try {
      IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
      Thread.sleep(3000);
      List<ExoSocialActivity> broadCastActivities = activityManager.getActivities(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, savedSpace.getPrettyName(), false), 0, 10);
      for (ExoSocialActivity activity : broadCastActivities) {
        activityManager.deleteActivity(activity);
      }
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * Test {@link SpaceService#getMembers(Space)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetMembers() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals("spaceService.getMembers(savedSpace).size() must return: " + savedSpace.getMembers().length, savedSpace.getMembers().length, spaceService.getMembers(savedSpace).size());
  }

  /**
   * Test {@link SpaceService#setLeader(Space, String, boolean)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testSetLeader() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] {"demo", "tom"};
    String[] members = new String[] {"raul", "ghost", "dragon"};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);
    tearDownSpaceList.add(space);

    //Space space = this.getSpaceInstance(0);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    int managers = savedSpace.getManagers().length;
    spaceService.setLeader(savedSpace, "demo", true);
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getManagers().length must return: " + managers, managers, savedSpace.getManagers().length);

    spaceService.setLeader(savedSpace, "john", true);
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getManagers().length must return: " + managers + 1, managers + 1, savedSpace.getManagers().length);

    spaceService.setLeader(savedSpace, "demo", false);
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getManagers().length must return: " + managers, managers, savedSpace.getManagers().length);

    // Wait 3 secs to have activity stored
    try {
      IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
      Thread.sleep(3000);
      List<ExoSocialActivity> broadCastActivities = activityManager.getActivities(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, savedSpace.getPrettyName(), false), 0, 10);
      for (ExoSocialActivity activity : broadCastActivities) {
        activityManager.deleteActivity(activity);
      }
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * Test {@link SpaceService#isLeader(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsLeader() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertTrue("spaceService.isLeader(savedSpace, \"demo\") must return true", spaceService.isLeader(savedSpace, "demo"));
    assertTrue("spaceService.isLeader(savedSpace, \"tom\") must return true", spaceService.isLeader(savedSpace, "tom"));
    assertFalse("spaceService.isLeader(savedSpace, \"mary\") must return false", spaceService.isLeader(savedSpace, "mary"));
    assertFalse("spaceService.isLeader(savedSpace, \"john\") must return false", spaceService.isLeader(savedSpace, "john"));
  }

  /**
   * Test {@link SpaceService#isOnlyLeader(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsOnlyLeader() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertFalse("spaceService.isOnlyLeader(savedSpace, \"tom\") must return false", spaceService.isOnlyLeader(savedSpace, "tom"));
    assertFalse("spaceService.isOnlyLeader(savedSpace, \"demo\") must return false", spaceService.isOnlyLeader(savedSpace, "demo"));

    savedSpace.setManagers(new String[] {"demo"});
    spaceService.updateSpace(savedSpace);
    assertTrue("spaceService.isOnlyLeader(savedSpace, \"demo\") must return true", spaceService.isOnlyLeader(savedSpace, "demo"));
    assertFalse("spaceService.isOnlyLeader(savedSpace, \"tom\") must return false", spaceService.isOnlyLeader(savedSpace, "tom"));

    savedSpace.setManagers(new String[] {"tom"});
    spaceService.updateSpace(savedSpace);
    assertFalse("spaceService.isOnlyLeader(savedSpace, \"demo\") must return false", spaceService.isOnlyLeader(savedSpace, "demo"));
    assertTrue("spaceService.isOnlyLeader(savedSpace, \"tom\") must return true", spaceService.isOnlyLeader(savedSpace, "tom"));
  }

  /**
   * Test {@link SpaceService#isMember(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsMember() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    assertTrue("spaceService.isMember(savedSpace, \"raul\") must return true", spaceService.isMember(savedSpace, "raul"));
    assertTrue("spaceService.isMember(savedSpace, \"ghost\") must return true", spaceService.isMember(savedSpace, "ghost"));
    assertTrue("spaceService.isMember(savedSpace, \"dragon\") must return true", spaceService.isMember(savedSpace, "dragon"));
    assertFalse("spaceService.isMember(savedSpace, \"stranger\") must return true", spaceService.isMember(savedSpace, "stranger"));
  }

  /**
   * Test {@link SpaceService#hasAccessPermission(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testHasAccessPermission() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    assertTrue("spaceService.hasAccessPermission(savedSpace, \"raul\") must return true", spaceService.hasAccessPermission(savedSpace, "raul"));
    assertTrue("spaceService.hasAccessPermission(savedSpace, \"ghost\") must return true", spaceService.hasAccessPermission(savedSpace, "ghost"));
    assertTrue("spaceService.hasAccessPermission(savedSpace, \"dragon\") must return true", spaceService.hasAccessPermission(savedSpace, "dragon"));
    assertTrue("spaceService.hasAccessPermission(savedSpace, \"tom\") must return true", spaceService.hasAccessPermission(savedSpace, "tom"));
    assertTrue("spaceService.hasAccessPermission(savedSpace, \"demo\") must return true", spaceService.hasAccessPermission(savedSpace, "demo"));
    assertTrue("spaceService.hasAccessPermission(savedSpace, \"root\") must return true", spaceService.hasAccessPermission(savedSpace, "root"));
    assertFalse("spaceService.hasAccessPermission(savedSpace, \"mary\") must return false", spaceService.hasAccessPermission(savedSpace, "mary"));
    assertFalse("spaceService.hasAccessPermission(savedSpace, \"john\") must return false", spaceService.hasAccessPermission(savedSpace, "john"));
  }

  /**
   * Test {@link SpaceService#hasEditPermission(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testHasEditPermission() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    assertTrue("spaceService.hasEditPermission(savedSpace, \"root\") must return true", spaceService.hasEditPermission(savedSpace, "root"));
    assertTrue("spaceService.hasEditPermission(savedSpace, \"demo\") must return true", spaceService.hasEditPermission(savedSpace, "demo"));
    assertTrue("spaceService.hasEditPermission(savedSpace, \"tom\") must return true", spaceService.hasEditPermission(savedSpace, "tom"));
    assertFalse("spaceService.hasEditPermission(savedSpace, \"mary\") must return false", spaceService.hasEditPermission(savedSpace, "mary"));
    assertFalse("spaceService.hasEditPermission(savedSpace, \"john\") must return false", spaceService.hasEditPermission(savedSpace, "john"));
    assertFalse("spaceService.hasEditPermission(savedSpace, \"raul\") must return false", spaceService.hasEditPermission(savedSpace, "raul"));
    assertFalse("spaceService.hasEditPermission(savedSpace, \"ghost\") must return false", spaceService.hasEditPermission(savedSpace, "ghost"));
    assertFalse("spaceService.hasEditPermission(savedSpace, \"dragon\") must return false", spaceService.hasEditPermission(savedSpace, "dragon"));
  }

  /**
   * Test {@link SpaceService#isInvited(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsInvited() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    assertTrue("spaceService.isInvited(savedSpace, \"register1\") must return true", spaceService.isInvited(savedSpace, "register1"));
    assertTrue("spaceService.isInvited(savedSpace, \"mary\") must return true", spaceService.isInvited(savedSpace, "mary"));
    assertFalse("spaceService.isInvited(savedSpace, \"demo\") must return false", spaceService.isInvited(savedSpace, "demo"));
    assertFalse("spaceService.isInvited(savedSpace, \"john\") must return false", spaceService.isInvited(savedSpace, "john"));
  }

  /**
   * Test {@link SpaceService#isPending(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testIsPending() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    assertTrue("spaceService.isPending(savedSpace, \"jame\") must return true", spaceService.isPending(savedSpace, "jame"));
    assertTrue("spaceService.isPending(savedSpace, \"paul\") must return true", spaceService.isPending(savedSpace, "paul"));
    assertTrue("spaceService.isPending(savedSpace, \"hacker\") must return true", spaceService.isPending(savedSpace, "hacker"));
    assertFalse("spaceService.isPending(savedSpace, \"mary\") must return false", spaceService.isPending(savedSpace, "mary"));
    assertFalse("spaceService.isPending(savedSpace, \"john\") must return false", spaceService.isPending(savedSpace, "john"));
  }

  /**
   * Test {@link SpaceService#installApplication(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testInstallApplication() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService#activateApplication(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testActivateApplication() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService#deactivateApplication(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testDeactivateApplication() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService#removeApplication(Space, String, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRemoveApplication() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService#requestJoin(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRequestJoin() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.requestJoin(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount + 1,
                 pendingUsersCount + 1, space.getPendingUsers().length);
    assertTrue("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#revokeRequestJoin(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRevokeRequestJoin() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.requestJoin(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount + 1,
                 pendingUsersCount + 1, space.getPendingUsers().length);
    assertTrue("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
               ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));

    spaceService.revokeRequestJoin(space, newPendingUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount, pendingUsersCount, space.getPendingUsers().length);
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()) must be true",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#inviteMember(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testInviteMember() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    int invitedUsersCount = savedSpace.getInvitedUsers().length;
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.inviteMember(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount + 1,
                 invitedUsersCount + 1, savedSpace.getInvitedUsers().length);
    assertTrue("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return true",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#revokeInvitation(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRevokeInvitation() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);
    int invitedUsersCount = savedSpace.getInvitedUsers().length;
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.addInvitedUser(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount + 1,
                 invitedUsersCount + 1, savedSpace.getInvitedUsers().length);
    assertTrue("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return true",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
    spaceService.revokeInvitation(savedSpace, newInvitedUser.getRemoteId());
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getInvitedUsers().length must return: " + invitedUsersCount,
                 invitedUsersCount, savedSpace.getInvitedUsers().length);
    assertFalse("ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()) must return false",
                ArrayUtils.contains(savedSpace.getInvitedUsers(), newInvitedUser.getRemoteId()));
  }
  
  public void testGetVisibleSpacesWithCondition() throws Exception {
    Space sp1 = this.createSpace("space test", "demo");
    Space sp2 = this.createSpace("space 11", "demo");
    tearDownSpaceList.add(sp1);
    tearDownSpaceList.add(sp2);
    
    SpaceListAccess list = spaceService.getVisibleSpacesWithListAccess("demo", new SpaceFilter("space test"));
    assertEquals(1, list.getSize());
    assertEquals(1, list.load(0, 10).length);
    list = spaceService.getVisibleSpacesWithListAccess("demo", new SpaceFilter("space 11"));
    assertEquals(1, list.getSize());
    assertEquals(1, list.load(0, 10).length);
    list = spaceService.getVisibleSpacesWithListAccess("demo", new SpaceFilter("space_11"));
    assertEquals(1, list.getSize());
    assertEquals(1, list.load(0, 10).length);
    list = spaceService.getVisibleSpacesWithListAccess("demo", new SpaceFilter("space"));
    assertEquals(2, list.getSize());
    assertEquals(2, list.load(0, 10).length);
  }
  
  public void testGetVisibleSpacesWithSpecialCharacters() throws Exception {
    Space space1 = new Space();
    space1.setDisplayName("広いニーズ");
    space1.setPrettyName("広いニーズ");
    space1.setManagers(new String[]{"root"});
    space1.setMembers(new String[]{"root","mary"});
    space1.setType(DefaultSpaceApplicationHandler.NAME);
    space1.setRegistration(Space.OPEN);
    createSpaceNonInitApps(space1, "mary", null);
    tearDownSpaceList.add(space1);
    
    Space space2 = new Space();
    space2.setDisplayName("ça c'est la vie");
    space2.setPrettyName("ça c'est la vie");
    space2.setManagers(new String[]{"root"});
    space2.setMembers(new String[]{"root","mary"});
    space2.setType(DefaultSpaceApplicationHandler.NAME);
    space2.setRegistration(Space.OPEN);
    createSpaceNonInitApps(space2, "mary", null);
    tearDownSpaceList.add(space2);
    
    Space space3 = new Space();
    space3.setDisplayName("Đây là không gian tiếng Việt");
    space3.setPrettyName("Đây là không gian tiếng Việt");
    space3.setManagers(new String[]{"root"});
    space3.setMembers(new String[]{"root","mary"});
    space3.setType(DefaultSpaceApplicationHandler.NAME);
    space3.setRegistration(Space.OPEN);
    createSpaceNonInitApps(space3, "mary", null);
    tearDownSpaceList.add(space3);
    
    SpaceListAccess list = spaceService.getVisibleSpacesWithListAccess("root", new SpaceFilter("広いニーズ"));
    assertEquals(1, list.getSize());
    assertEquals(1, list.load(0, 10).length);
    list = spaceService.getVisibleSpacesWithListAccess("mary", new SpaceFilter("広いニーズ"));
    assertEquals(1, list.getSize());
    assertEquals(1, list.load(0, 10).length);
    
    list = spaceService.getVisibleSpacesWithListAccess("root", new SpaceFilter("ça c'est la vie"));
    assertEquals(1, list.getSize());
    assertEquals(1, list.load(0, 10).length);
    list = spaceService.getVisibleSpacesWithListAccess("mary", new SpaceFilter("ça c'est la vie"));
    assertEquals(1, list.getSize());
    assertEquals(1, list.load(0, 10).length);
    
    list = spaceService.getVisibleSpacesWithListAccess("root", new SpaceFilter("Đây là không gian tiếng Việt"));
    assertEquals(1, list.getSize());
    assertEquals(1, list.load(0, 10).length);
    list = spaceService.getVisibleSpacesWithListAccess("mary", new SpaceFilter("Đây là không gian tiếng Việt"));
    assertEquals(1, list.getSize());
    assertEquals(1, list.load(0, 10).length);
  }

  /**
   * Test {@link SpaceService#acceptInvitation(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testAcceptInvitation() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] {"demo"};
    String[] members = new String[] {};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);
    tearDownSpaceList.add(space);

    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    spaceService.acceptInvitation(savedSpace, "root");
    spaceService.acceptInvitation(savedSpace, "mary");
    spaceService.acceptInvitation(savedSpace, "john");
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getMembers().length must return 4", 4, savedSpace.getMembers().length);
    // Wait 3 secs to have activity stored
    try {
      IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
      Thread.sleep(3000);
      List<ExoSocialActivity> broadCastActivities = activityManager.getActivities(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, savedSpace.getPrettyName(), false), 0, 10);
      for (ExoSocialActivity activity : broadCastActivities) {
        activityManager.deleteActivity(activity);
      }
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * Test {@link SpaceService#denyInvitation(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testDenyInvitation() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    spaceService.denyInvitation(savedSpace, "new member 1");
    spaceService.denyInvitation(savedSpace, "new member 2");
    spaceService.denyInvitation(savedSpace, "new member 3");
    assertEquals("savedSpace.getMembers().length must return 2", 2, savedSpace.getInvitedUsers().length);

    spaceService.denyInvitation(savedSpace, "raul");
    spaceService.denyInvitation(savedSpace, "ghost");
    spaceService.denyInvitation(savedSpace, "dragon");
    assertEquals("savedSpace.getMembers().length must return 2", 2, savedSpace.getInvitedUsers().length);

    spaceService.denyInvitation(savedSpace, "register1");
    spaceService.denyInvitation(savedSpace, "mary");
    assertEquals("savedSpace.getMembers().length must return 0", 0, savedSpace.getInvitedUsers().length);
  }

  /**
   * Test {@link SpaceService#validateRequest(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testValidateRequest() throws Exception {
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] {"demo"};
    String[] members = new String[] {};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, "demo", null);
    tearDownSpaceList.add(space);

    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    spaceService.validateRequest(savedSpace, "root");
    spaceService.validateRequest(savedSpace, "mary");
    spaceService.validateRequest(savedSpace, "john");
    savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("savedSpace.getMembers().length must return 4", 4, savedSpace.getMembers().length);
    // Wait 3 secs to have activity stored
    try {
      IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
      Thread.sleep(3000);
      List<ExoSocialActivity> broadCastActivities = activityManager.getActivities(identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, savedSpace.getPrettyName(), false), 0, 10);
      for (ExoSocialActivity activity : broadCastActivities) {
        activityManager.deleteActivity(activity);
      }
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * Test {@link SpaceService#declineRequest(Space, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testDeclineRequest() throws Exception {
    Space space = this.getSpaceInstance(0);
    tearDownSpaceList.add(space);
    int pendingUsersCount = space.getPendingUsers().length;
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newPendingUser) must be false",
                ArrayUtils.contains(space.getPendingUsers(), newPendingUser.getRemoteId()));
    spaceService.addPendingUser(space, newInvitedUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount + 1,
                 pendingUsersCount + 1, space.getPendingUsers().length);
    assertTrue("ArrayUtils.contains(space.getPendingUsers(), newInvitedUser.getRemoteId()) must be true",
                ArrayUtils.contains(space.getPendingUsers(), newInvitedUser.getRemoteId()));

    spaceService.declineRequest(space, newInvitedUser.getRemoteId());
    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertEquals("space.getPendingUsers().length must return: " + pendingUsersCount,
                 pendingUsersCount, space.getPendingUsers().length);
    assertFalse("ArrayUtils.contains(space.getPendingUsers(), newInvitedUser.getRemoteId()) must be true",
                ArrayUtils.contains(space.getPendingUsers(), newInvitedUser.getRemoteId()));
  }

  /**
   * Test {@link SpaceService#registerSpaceLifeCycleListener(SpaceLifeCycleListener)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testRegisterSpaceLifeCybleListener() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService#unregisterSpaceLifeCycleListener(SpaceLifeCycleListener)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testUnRegisterSpaceLifeCycleListener() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService#setPortletsPrefsRequired(org.exoplatform.social.core.application.PortletPreferenceRequiredPlugin)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testSetPortletsPrefsRequired() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService#getPortletsPrefsRequired()}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPortletsPrefsRequired() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService#setSpaceApplicationConfigPlugin(org.exoplatform.social.core.space.SpaceApplicationConfigPlugin)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testSetSpaceApplicationConfigPlugin() throws Exception {
    //TODO Complete this
  }

  /**
   * Test {@link SpaceService}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSpaceApplicationConfigPlugin() throws Exception {
    //TODO Complete this
  }
  
  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  public void testGetVisibleSpaces() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    
    //there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i ++) {
    
      if (i < 6)
         //[0->5] :: there are 6 spaces with visible = 'private'
        listSpace[i] = this.getSpaceInstance(i, Space.PRIVATE, Space.OPEN, "demo");
      else
        //[6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.OPEN, "demo");
      
      spaceService.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    
    //visible with remoteId = 'demo'  return 10 spaces
    {
      List<Space> visibleAllSpaces = spaceService.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace, countSpace, visibleAllSpaces.size());
    }
    
    
  }
  
  
  
  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  public void testGetVisibleSpacesCloseRegistration() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    
    //there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i ++) {
    
      if (i < 6)
         //[0->5] :: there are 6 spaces with visible = 'private'
        listSpace[i] = this.getSpaceInstance(i, Space.PRIVATE, Space.CLOSE, "demo");
      else
        //[6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.CLOSE, "demo");
      
      spaceService.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    
    //visible with remoteId = 'demo'  return 10 spaces
    {
      List<Space> visibleAllSpaces = spaceService.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace, countSpace, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'mary'  return 6 spaces: can see
    {
      int registrationCloseSpaceCount = 6;
      List<Space> registrationCloseSpaces = spaceService.getVisibleSpaces("mary", null);
      assertNotNull("registrationCloseSpaces must not be  null", registrationCloseSpaces);
      assertEquals("registrationCloseSpaces must return: " + registrationCloseSpaceCount, registrationCloseSpaceCount, registrationCloseSpaces.size());
    }
    
   
  }
  
  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  public void testGetVisibleSpacesInvitedMember() throws Exception {
    int countSpace = 10;
    Space[] listSpace = new Space[10];
    
    //there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i ++) {
    
      if (i < 6)
         //[0->5] :: there are 6 spaces with visible = 'private'
        listSpace[i] = this.getSpaceInstanceInvitedMember(i, Space.PRIVATE, Space.CLOSE, new String[] {"mary", "hacker"}, "demo");
      else
        //[6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.CLOSE, "demo");
      
      spaceService.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    
    //visible with remoteId = 'demo'  return 10 spaces
    {
      List<Space> visibleAllSpaces = spaceService.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace, countSpace, visibleAllSpaces.size());
    }
    
    //visible with invited = 'mary'  return 6 spaces
    {
      int invitedSpaceCount1 = 6;
      List<Space> invitedSpaces1 = spaceService.getVisibleSpaces("mary", null);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces1);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount1, invitedSpaceCount1, invitedSpaces1.size());
    }
    
    //visible with invited = 'hacker'  return 6 spaces
    {
      int invitedSpaceCount1 = 6;
      List<Space> invitedSpaces1 = spaceService.getVisibleSpaces("hacker", null);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces1);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount1, invitedSpaceCount1, invitedSpaces1.size());
    }
    
    //visible with invited = 'paul'  return 6 spaces
    {
      int invitedSpaceCount2 = 6;
      List<Space> invitedSpaces2 = spaceService.getVisibleSpaces("paul", null);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces2);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount2, invitedSpaceCount2, invitedSpaces2.size());
    }
  }

  public void testGetLastSpaces() throws Exception {
    tearDownSpaceList.add(populateData());
    tearDownSpaceList.add(createMoreSpace("Space2"));
    List<Space> lastSpaces = spaceService.getLastSpaces(1);
    assertEquals(1, lastSpaces.size());
    Space sp1 = lastSpaces.get(0);
    lastSpaces = spaceService.getLastSpaces(1);
    assertEquals(1, lastSpaces.size());
    assertEquals(sp1, lastSpaces.get(0));
    lastSpaces = spaceService.getLastSpaces(5);
    assertEquals(2, lastSpaces.size());
    assertEquals(sp1, lastSpaces.get(0));
    Space newSp1 = createMoreSpace("newSp1");
    lastSpaces = spaceService.getLastSpaces(1);
    assertEquals(1, lastSpaces.size());
    assertEquals(newSp1, lastSpaces.get(0));
    lastSpaces = spaceService.getLastSpaces(5);
    assertEquals(3, lastSpaces.size());
    assertEquals(newSp1, lastSpaces.get(0));
    Space newSp2 = createMoreSpace("newSp2");
    lastSpaces = spaceService.getLastSpaces(1);
    assertEquals(1, lastSpaces.size());
    assertEquals(newSp2, lastSpaces.get(0));
    lastSpaces = spaceService.getLastSpaces(5);
    assertEquals(4, lastSpaces.size());
    assertEquals(newSp2, lastSpaces.get(0));
    assertEquals(newSp1, lastSpaces.get(1));
    spaceService.deleteSpace(newSp1);
    lastSpaces = spaceService.getLastSpaces(5);
    assertEquals(3, lastSpaces.size());
    assertEquals(newSp2, lastSpaces.get(0));
    assertFalse(newSp1.equals(lastSpaces.get(1)));
    spaceService.deleteSpace(newSp2);
    lastSpaces = spaceService.getLastSpaces(5);
    assertEquals(2, lastSpaces.size());
    assertEquals(sp1, lastSpaces.get(0));
  }

  private Space populateData() throws Exception {
    String spaceDisplayName = "Space1";
    Space space1 = new Space();
    space1.setApp("Calendar;FileSharing");
    space1.setDisplayName(spaceDisplayName);
    space1.setPrettyName(space1.getDisplayName());
    String shortName = SpaceUtils.cleanString(spaceDisplayName);
    space1.setGroupId("/spaces/" + shortName);
    space1.setUrl(shortName);
    space1.setRegistration("validation");
    space1.setDescription("This is my first space for testing");
    space1.setType("classic");
    space1.setVisibility("public");
    space1.setPriority("2");
    String[] manager = new String []{"root"};
    String[] members = new String []{"demo", "john", "mary", "tom", "harry"};
    space1.setManagers(manager);
    space1.setMembers(members);

    spaceService.saveSpace(space1, true);
    return space1;
  }
  
  private Space createSpace(String spaceName, String creator) throws Exception {
    Space space = new Space();
    space.setDisplayName(spaceName);
    space.setPrettyName(spaceName);
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setRegistration(Space.OPEN);
    space.setDescription("description of space" + spaceName);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    String[] managers = new String[] {creator};
    String[] members = new String[] {creator};
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.saveSpace(space, true);
    return space;
  }

  /**
   * Gets an instance of the space.
   *
   * @param number
   * @return
   * @throws Exception
   * @since 1.2.0-GA
   */
  private Space getSpaceInstance(int number) throws Exception {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    String[] managers = new String[] {"demo", "tom"};
    String[] members = new String[] {"demo", "raul", "ghost", "dragon"};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    this.spaceService.saveSpace(space, true);
    return space;
  }
  
  /**
   * Gets an instance of Space.
   *
   * @param number
   * @return an instance of space
   */
  private Space getSpaceInstance(int number, String visible, String registration, String manager, String...members) {
    Space space = new Space();
    space.setApp("app");
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(registration);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(visible);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/spaces/space" + number);
    String[] managers = new String[] {manager};
    String[] invitedUsers = new String[] {};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    return space;
  }
  
  /**
   * Gets an instance of Space.
   *
   * @param number
   * @return an instance of space
   */
  private Space getSpaceInstanceInvitedMember(int number, String visible, String registration, String[] invitedMember, String manager, String...members) {
    Space space = new Space();
    space.setApp("app");
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(registration);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(visible);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/spaces/space" + number);
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] {manager};
    //String[] invitedUsers = new String[] {invitedMember};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedMember);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    return space;
  }

  private Space createMoreSpace(String spaceName) throws Exception {
    Space space2 = new Space();
    space2.setApp("Contact,Forum");
    space2.setDisplayName(spaceName);
    space2.setPrettyName(space2.getDisplayName());
    String shortName = SpaceUtils.cleanString(spaceName);
    space2.setGroupId("/spaces/" + shortName );
    space2.setUrl(shortName);
    space2.setRegistration("open");
    space2.setDescription("This is my second space for testing");
    space2.setType("classic");
    space2.setVisibility("public");
    space2.setPriority("2");

    spaceService.saveSpace(space2, true);

    return space2;
  }
}