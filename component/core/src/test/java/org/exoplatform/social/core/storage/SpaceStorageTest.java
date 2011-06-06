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
package org.exoplatform.social.core.storage;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit Tests for {@link org.exoplatform.social.core.storage.SpaceStorage}
 *
 * @since Nov 3, 2010
 * @copyright eXo SAS
 */

public class SpaceStorageTest extends AbstractCoreTest {

  private List<Space>  tearDownSpaceList;
  private List<Identity>  tearDownIdentityList;

  private SpaceStorage spaceStorage;
  private IdentityStorage identityStorage;

  private Identity demo;
  private Identity tom;
  private Identity raul;
  private Identity ghost;
  private Identity dragon;
  private Identity register1;
  private Identity mary;
  private Identity jame;
  private Identity paul;
  private Identity hacker;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = (SpaceStorage) this.getContainer().getComponentInstanceOfType(SpaceStorage.class);
    identityStorage = (IdentityStorage) this.getContainer().getComponentInstanceOfType(IdentityStorage.class);

    assertNotNull("spaceStorage must not be null", spaceStorage);
    assertNotNull("identityStorage must not be null", identityStorage);

    demo = new Identity("organization", "demo");
    tom = new Identity("organization", "tom");
    raul = new Identity("organization", "raul");
    ghost = new Identity("organization", "ghost");
    dragon = new Identity("organization", "dragon");
    register1 = new Identity("organization", "register1");
    mary = new Identity("organization", "mary");
    jame = new Identity("organization", "jame");
    paul = new Identity("organization", "paul");
    hacker = new Identity("organization", "hacker");

    identityStorage.saveIdentity(demo);
    identityStorage.saveIdentity(tom);
    identityStorage.saveIdentity(raul);
    identityStorage.saveIdentity(ghost);
    identityStorage.saveIdentity(dragon);
    identityStorage.saveIdentity(register1);
    identityStorage.saveIdentity(mary);
    identityStorage.saveIdentity(jame);
    identityStorage.saveIdentity(paul);
    identityStorage.saveIdentity(hacker);

    tearDownIdentityList = new ArrayList<Identity>();
    tearDownIdentityList.add(demo);
    tearDownIdentityList.add(tom);
    tearDownIdentityList.add(raul);
    tearDownIdentityList.add(ghost);
    tearDownIdentityList.add(dragon);
    tearDownIdentityList.add(register1);
    tearDownIdentityList.add(mary);
    tearDownIdentityList.add(jame);
    tearDownIdentityList.add(paul);
    tearDownIdentityList.add(hacker);

    tearDownSpaceList = new ArrayList<Space>();
  }

  /**
   * Cleans up.
   */
  @Override
  protected void tearDown() throws Exception {
    for (Space sp : tearDownSpaceList) {
      spaceStorage.deleteSpace(sp.getId());
    }

    for (Identity id : tearDownIdentityList) {
      identityStorage.deleteIdentity(id);
    }

    assertTrue("spaceStorage.getAllSpaces().isEmpty() must return true",
           spaceStorage.getAllSpaces().isEmpty());

    super.tearDown();
  }

  /**
   * Gets an instance of Space.
   *
   * @param number
   * @return an instance of space
   */
  private Space getSpaceInstance(int number) {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    String[] managers = new String[] {"demo", "tom"};
    String[] members = new String[] {"raul", "ghost", "dragon"};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    return space;
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAllSpaces()}
   *
   * @throws Exception
   */
  public void testGetAllSpaces() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      tearDownSpaceList.add(space);
    }
    assertEquals("spaceStorage.getAllSpaces().size() must return: " + totalSpaces,
            totalSpaces, spaceStorage.getAllSpaces().size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getSpaces(long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSpaces() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      tearDownSpaceList.add(space);
    }
    int offset = 0;
    int limit = 10;
    List<Space> spaceListAccess = spaceStorage.getSpaces(offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must be: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpaces(offset, 5);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must be: " + 5, 5, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpaces(offset, 20);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must be: " + totalSpaces, totalSpaces, spaceListAccess.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAllSpacesCount()}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAllSpacesCount() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      tearDownSpaceList.add(space);
    }
    int spacesCount = spaceStorage.getAllSpacesCount();
    assertEquals("spacesCount must be: ", totalSpaces, spacesCount);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getSpacesByFilter(org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetSpacesByFilter() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      tearDownSpaceList.add(space);
    }
    List<Space> foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter("add"), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + totalSpaces, totalSpaces, foundSpaces.size());

    foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter("my"), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + totalSpaces, totalSpaces, foundSpaces.size());

    foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter("my space"), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + totalSpaces, totalSpaces, foundSpaces.size());

    foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter("hell gate"), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + 0, 0, foundSpaces.size());

    foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter('m'), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + totalSpaces, totalSpaces, foundSpaces.size());

    foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter('M'), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + totalSpaces, totalSpaces, foundSpaces.size());

    foundSpaces = spaceStorage.getSpacesByFilter(new SpaceFilter('k'), 0, 10);
    assertNotNull("foundSpaces must not be null", foundSpaces);
    assertEquals("foundSpaces.size() must return: " + 0, 0, foundSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAllSpacesByFilterCount(org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAllSpacesByFilterCount() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      tearDownSpaceList.add(space);
    }
    int count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter("add"));
    assertEquals("count must be: " + totalSpaces, totalSpaces, count);

    count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter("my"));
    assertEquals("count must be: " + totalSpaces, totalSpaces, count);

    count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter("my space"));
    assertEquals("count must be: " + totalSpaces, totalSpaces, count);

    count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter("hell gate"));
    assertEquals("count must be: " + 0, 0, count);

    count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter('m'));
    assertEquals("count must be: " + totalSpaces, totalSpaces, count);

    count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter('M'));
    assertEquals("count must be: " + totalSpaces, totalSpaces, count);

    count = spaceStorage.getAllSpacesByFilterCount(new SpaceFilter('k'));
    assertEquals("count must be: " + 0, 0, count);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getSpacesBySearchCondition(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetSpacesBySearchConditionWithListAccess() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      tearDownSpaceList.add(space);
    }
    int offset = 0;
    int limit = 10;
    List<Space> spaceListAccess = spaceStorage.getSpacesBySearchCondition("my", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("space", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("*", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + 0, 0, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("m*", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("add new", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("a*", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("a*n", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("a%n", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("*a%n", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("***", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + 0, 0, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("*%*%", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + 0, 0, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("%%%%", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + 0, 0, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesBySearchCondition("nothing", offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + 0, 0, spaceListAccess.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getSpacesByFirstCharacterOfName(char, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetSpacesByFirstCharacterOfNameWithListAccess() throws Exception {
    int totalSpaces = 10;
    for (int i = 1; i <= totalSpaces; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      tearDownSpaceList.add(space);
    }
    int offset = 0;
    int limit = 10;
    List<Space> spaceListAccess = spaceStorage.getSpacesByFirstCharacterOfName('m', offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesByFirstCharacterOfName('M', offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + totalSpaces, totalSpaces, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesByFirstCharacterOfName('h', offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + 0, 0, spaceListAccess.size());

    spaceListAccess = spaceStorage.getSpacesByFirstCharacterOfName('*', offset, limit);
    assertNotNull("spaceListAccess must not be  null", spaceListAccess);
    assertEquals("spaceListAccess.size() must return: " + 0, 0, spaceListAccess.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpaces() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> accessibleSpaces = spaceStorage.getAccessibleSpaces("demo");
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + countSpace, countSpace, accessibleSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpacesByFilter() throws Exception {
    int countSpace = 20;
    Space []listSpace = new Space[countSpace];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("demo", new SpaceFilter("my space"), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("tom", new SpaceFilter("my space"), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("ghost", new SpaceFilter("my space"), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("demo", new SpaceFilter("add new"), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("demo", new SpaceFilter('m'), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("demo", new SpaceFilter('M'), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 10, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("demo", new SpaceFilter('K'), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 0, accessibleSpacesByFilter.size());

    accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("newperson", new SpaceFilter("my space"), 0, 10);
    assertNotNull("accessibleSpacesByFilter must not be null", accessibleSpacesByFilter);
    assertEquals("accessibleSpacesByFilter.size() must return: ", 0, accessibleSpacesByFilter.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpacesByFilterCount() throws Exception {
    int countSpace = 20;
    Space []listSpace = new Space[countSpace];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("demo", new SpaceFilter("my space"));
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("tom", new SpaceFilter("my space"));
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("tom", new SpaceFilter('m'));
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("tom", new SpaceFilter('M'));
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("tom", new SpaceFilter('k'));
    assertEquals("accessibleSpacesByFilterCount must be: ", 0, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("ghost", new SpaceFilter("my space"));
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("demo", new SpaceFilter("add new"));
    assertEquals("accessibleSpacesByFilterCount must be: ", countSpace, accessibleSpacesByFilterCount);

    accessibleSpacesByFilterCount = spaceStorage.getAccessibleSpacesByFilterCount("newperson", new SpaceFilter("my space"));
    assertEquals("accessibleSpacesByFilterCount must be: ", 0, accessibleSpacesByFilterCount);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpacesCount(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testgetAccessibleSpacesCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int accessibleSpacesCount = spaceStorage.getAccessibleSpacesCount("demo");
    assertEquals("accessibleSpacesCount mus be: " + countSpace, countSpace, accessibleSpacesCount);

    accessibleSpacesCount = spaceStorage.getAccessibleSpacesCount("dragon");
    assertEquals("accessibleSpacesCount must be: " + countSpace, countSpace, accessibleSpacesCount);

    accessibleSpacesCount = spaceStorage.getAccessibleSpacesCount("nobody");
    assertEquals("accessibleSpacesCount must be: 0", 0, accessibleSpacesCount);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpacesBySearchCondition(String, String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetAccessibleSpacesBySearchCondition() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("demo", "my space", 0, countSpace);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + countSpace, countSpace, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("tom", "my space", 0, countSpace);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + countSpace, countSpace, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("tom", "*", 0, countSpace);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 0, 0, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("demo", "add new", 0, countSpace);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + countSpace, countSpace, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("demo", null, 0, countSpace);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 0, 0, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("demo", null, 0, 5);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 0, 0, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("demo", "m*e", 0, 5);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 5, 5, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("demo", "a*e", 0, 5);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 5, 5, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("demo", "m*", 0, 5);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 5, 5, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("demo", "a*", 0, 5);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 5, 5, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("demo", "*a*", 0, 5);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 5, 5, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("demo", "*****", 0, 5);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 0, 0, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesBySearchCondition("demo", "%%%%", 0, 5);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 0, 0, accessibleSpaces.size());

  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpacesByFirstCharacterOfName(char, String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetAccessibleSpacesByFirstCharacterOfName() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> accessibleSpaces = spaceStorage.getAccessibleSpacesByFirstCharacterOfName("tom", 'M', 0, countSpace);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + countSpace, countSpace, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesByFirstCharacterOfName("demo", '*', 0, countSpace);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 0, 0, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesByFirstCharacterOfName("demo", 'm', 0, countSpace);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + countSpace, countSpace, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesByFirstCharacterOfName("demo", 'M', 0, countSpace);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + countSpace, countSpace, accessibleSpaces.size());

    accessibleSpaces = spaceStorage.getAccessibleSpacesByFirstCharacterOfName("demo", 'A', 0, countSpace);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 0, 0, accessibleSpaces.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetAccessibleSpacesWithOffset() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> accessibleSpaces = spaceStorage.getAccessibleSpaces("demo", 0, 5);
    assertNotNull("accessibleSpaces must not be  null", accessibleSpaces);
    assertEquals("accessibleSpaces.size() must return: " + 5, 5, accessibleSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetEditableSpaces () throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> editableSpaces = spaceStorage.getEditableSpaces("demo");
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpaces("top");
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpaces("dragon");
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetEditableSpacesByFilter() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter("add new"), 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter("m"), 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter("M"), 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter('m'), 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter('M'), 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter('K'), 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("demo", new SpaceFilter("add new"), 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("top", new SpaceFilter("my space"), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("dragon", new SpaceFilter("m"), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("dragon", new SpaceFilter('m'), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("dragon", new SpaceFilter('M'), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFilter("dragon", new SpaceFilter('k'), 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetEditableSpacesByFilterCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("demo", new SpaceFilter("add new"));
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("demo", new SpaceFilter("m"));
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("demo", new SpaceFilter("M"));
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("demo", new SpaceFilter('m'));
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("demo", new SpaceFilter('M'));
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("demo", new SpaceFilter('K'));
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("tom", new SpaceFilter("add new"));
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("top", new SpaceFilter("my space"));
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("dragon", new SpaceFilter("m"));
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("dragon", new SpaceFilter('m'));
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("dragon", new SpaceFilter('M'));
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFilterCount("dragon", new SpaceFilter('k'));
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpacesBySearchCondition(String, String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetEditableSpacesBySpaceNameSearchCondition() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> editableSpaces = spaceStorage.getEditableSpacesBySearchCondition("demo", "add new", 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesBySearchCondition("demo", "m", 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesBySearchCondition("demo", "M", 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesBySearchCondition("tom", "add new", 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesBySearchCondition("top", "my space", 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesBySearchCondition("dragon", "m", 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpacesBySearchConditionCount(String, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetEditableSpacesBySpaceNameSearchConditionCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int editableSpacesCount = spaceStorage.getEditableSpacesBySearchConditionCount("demo", "add new");
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesBySearchConditionCount("demo", "m");
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesBySearchConditionCount("demo", "M");
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesBySearchConditionCount("tom", "add new");
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesBySearchConditionCount("top", "my space");
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesBySearchConditionCount("dragon", "m");
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpacesByFirstCharacterOfSpaceName(String, char, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetEditableSpacesByFirstCharacterOfSpaceName() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }

    List<Space> editableSpaces = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceName("demo", 'm', 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceName("demo", 'M', 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceName("demo", 'K', 0 , 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceName("dragon", 'm', 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceName("dragon", 'M', 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceName("dragon", 'k', 0, 10);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpacesByFirstCharacterOfSpaceNameCount(String, char)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetEditableSpacesByFirstCharacterOfSpaceNameCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }

    int editableSpacesCount = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceNameCount("demo", 'm');
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceNameCount("demo", 'M');
    assertEquals("editableSpacesCount must be: " + countSpace, countSpace, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceNameCount("demo", 'K');
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceNameCount("dragon", 'm');
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceNameCount("dragon", 'M');
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);

    editableSpacesCount = spaceStorage.getEditableSpacesByFirstCharacterOfSpaceNameCount("dragon", 'k');
    assertEquals("editableSpacesCount must be: " + 0, 0, editableSpacesCount);
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetEditableSpacesWithListAccess() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> editableSpaces = spaceStorage.getEditableSpaces("demo", 0, countSpace);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + countSpace, countSpace, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpaces("top", 0, countSpace);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());

    editableSpaces = spaceStorage.getEditableSpaces("dragon", 0, 5);
    assertNotNull("editableSpaces must not be  null", editableSpaces);
    assertEquals("editableSpaces.size() must return: " + 0, 0, editableSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpaces() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> invitedSpaces = spaceStorage.getInvitedSpaces("register1");
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("register");
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("mary");
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("demo");
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesByFilter() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> invitedSpaces = spaceStorage.getInvitedSpacesByFilter("register1", new SpaceFilter("add new"), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFilter("register1", new SpaceFilter('m'), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFilter("register1", new SpaceFilter('M'), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFilter("register1", new SpaceFilter('k'), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFilter("register", new SpaceFilter("my space "), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFilter("mary", new SpaceFilter("add"), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFilter("demo", new SpaceFilter("my"), 0, 10);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesByFilterCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("register1", new SpaceFilter("add new"));
    assertEquals("invitedSpacesCount must be: " + countSpace, countSpace, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("register1", new SpaceFilter('m'));
    assertEquals("invitedSpacesCount must be: " + countSpace, countSpace, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("register1", new SpaceFilter('M'));
    assertEquals("invitedSpacesCount must be: " + countSpace, countSpace, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("register1", new SpaceFilter('k'));
    assertEquals("invitedSpacesCount must be: " + 0, 0, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("register", new SpaceFilter("my space "));
    assertEquals("invitedSpacesCount must be: " + 0, 0, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("mary", new SpaceFilter("add"));
    assertEquals("invitedSpacesCount must be: " + countSpace, countSpace, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesByFilterCount("demo", new SpaceFilter("my"));
    assertEquals("invitedSpacesCount must be: " + 0, 0, invitedSpacesCount);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesWithOffset() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> invitedSpaces = spaceStorage.getInvitedSpaces("register1", 0, 5);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 5, 5, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("register", 0, 5);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("mary", 0, 5);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 5, 5, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpaces("demo", 0, 5);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpacesCount(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetInvitedSpacesCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int invitedSpacesCount = spaceStorage.getInvitedSpacesCount("register1");
    assertEquals("invitedSpacesCount must be: " + countSpace, countSpace, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesCount("mary");
    assertEquals("invitedSpacesCount must be: " + countSpace, countSpace, invitedSpacesCount);

    invitedSpacesCount = spaceStorage.getInvitedSpacesCount("nobody");
    assertEquals("invitedSpacesCount must be: 0", 0, invitedSpacesCount);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpacesBySearchCondition(String, String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetInvitedSpacesBySearchCondition() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("register1", "my", 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("mary", "my space", 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("mary", "add new", 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("mary", "*a*", 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("mary", "a*w", 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("mary", "%a%", 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("mary", "***", 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("mary", "*%*%*%*%", 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("mary", "%%%%%%%", 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("mary", "%a*w", 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("mary", null, 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("mary", null, 0, 5);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesBySearchCondition("tom", "my space", 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getInvitedSpacesByFirstCharacterOfName(String, char, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetInvitedSpacesByFirstCharacterOfName() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> invitedSpaces = spaceStorage.getInvitedSpacesByFirstCharacterOfName("register1", 'm', 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFirstCharacterOfName("register1", 'M', 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFirstCharacterOfName("mary", 'm', 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFirstCharacterOfName("mary", 'M', 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + countSpace, countSpace, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFirstCharacterOfName("mary", '*', 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFirstCharacterOfName("mary", 'H', 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());

    invitedSpaces = spaceStorage.getInvitedSpacesByFirstCharacterOfName("tom", 'm', 0, countSpace);
    assertNotNull("invitedSpaces must not be  null", invitedSpaces);
    assertEquals("invitedSpaces.size() must return: " + 0, 0, invitedSpaces.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpaces() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> pendingSpaces = spaceStorage.getPendingSpaces("hacker");
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("hack");
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("paul");
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("jame");
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("victory");
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesByFilter() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> pendingSpaces = spaceStorage.getPendingSpacesByFilter("hacker", new SpaceFilter("add new"), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("hacker", new SpaceFilter('m'), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("hacker", new SpaceFilter('M'), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("hacker", new SpaceFilter('k'), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("hack", new SpaceFilter("my space"), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("hack", new SpaceFilter('m'), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("hack", new SpaceFilter('M'), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("hack", new SpaceFilter('K'), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("paul", new SpaceFilter("add"), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("jame", new SpaceFilter("my"), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFilter("victory", new SpaceFilter("my space "), 0, 10);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesByFilterCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("hacker", new SpaceFilter("add new"));
    assertEquals("pendingSpacesCount must be: " + countSpace, countSpace, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("hacker", new SpaceFilter('m'));
    assertEquals("pendingSpacesCount must be: " + countSpace, countSpace, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("hacker", new SpaceFilter('M'));
    assertEquals("pendingSpacesCount must be: " + countSpace, countSpace, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("hacker", new SpaceFilter('k'));
    assertEquals("pendingSpacesCount must be: " + 0, 0, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("hack", new SpaceFilter("my space"));
    assertEquals("pendingSpacesCount must be: " + 0, 0, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("hack", new SpaceFilter('m'));
    assertEquals("pendingSpacesCount must be: " + 0, 0, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("hack", new SpaceFilter('M'));
    assertEquals("pendingSpacesCount must be: " + 0, 0, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("hack", new SpaceFilter('K'));
    assertEquals("pendingSpacesCount must be: " + 0, 0, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("paul", new SpaceFilter("add"));
    assertEquals("pendingSpacesCount must be: " + countSpace, countSpace, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("jame", new SpaceFilter("my"));
    assertEquals("pendingSpacesCount must be: " + countSpace, countSpace, pendingSpacesCount);

    pendingSpacesCount = spaceStorage.getPendingSpacesByFilterCount("victory", new SpaceFilter("my space "));
    assertEquals("pendingSpacesCount must be: " + 0, 0, pendingSpacesCount);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesWithOffset() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> pendingSpaces = spaceStorage.getPendingSpaces("hacker", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("paul", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("jame", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpaces("victory", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpacesCount(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPendingSpacesCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int pendingSpaceCount = spaceStorage.getPendingSpacesCount("jame");
    assertEquals("pendingSpaceCount must be: " + countSpace, countSpace, pendingSpaceCount);

    pendingSpaceCount = spaceStorage.getPendingSpacesCount("paul");
    assertEquals("pendingSpaceCount must be: " + countSpace, countSpace, pendingSpaceCount);

    pendingSpaceCount = spaceStorage.getPendingSpacesCount("hacker");
    assertEquals("pendingSpaceCount must be: " + countSpace, countSpace, pendingSpaceCount);

    pendingSpaceCount = spaceStorage.getPendingSpacesCount("nobody");
    assertEquals("pendingSpaceCount must be: 0", 0, pendingSpaceCount);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpacesBySearchCondition(String, String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetPendingSpacesBySearchCondition() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("hacker", "my", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("hacker", "*", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("paul", "space", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "my space", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "*m*e*", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "*m%e*", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "*m%", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "*m", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "m%", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "%m%", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "%*%*", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "%%%%%%%%", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "*******", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "my space", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", null, 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("jame", "add ", 0, countSpace);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesBySearchCondition("victory", "my space ", 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpacesByFirstCharacterOfName(String, char, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetPendingSpacesByFirstCharacterOfName() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> pendingSpaces = spaceStorage.getPendingSpacesByFirstCharacterOfName("hacker", 'm', 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 5, 5, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFirstCharacterOfName("hacker", 'M', 0, countSpace);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + countSpace, countSpace, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFirstCharacterOfName("jame", '*', 0, countSpace);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());

    pendingSpaces = spaceStorage.getPendingSpacesByFirstCharacterOfName("victory", 'm', 0, 5);
    assertNotNull("pendingSpaces must not be  null", pendingSpaces);
    assertEquals("pendingSpaces.size() must return: " + 0, 0, pendingSpaces.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPublicSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPublicSpaces() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> publicSpaces = spaceStorage.getPublicSpaces("mary");
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpaces("demo");
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPublicSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPublicSpacesByFilter() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> publicSpaces = spaceStorage.getPublicSpacesByFilter("mary", new SpaceFilter("add new"), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter("mary", new SpaceFilter("my space"), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter("mary", new SpaceFilter('m'), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter("mary", new SpaceFilter('M'), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter("demo", new SpaceFilter("my space"), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter("newstranger", new SpaceFilter('m'), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 10, 10, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter("newstranger", new SpaceFilter('M'), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 10, 10, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter("newstranger", new SpaceFilter("add new "), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 10, 10, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter("newstranger", new SpaceFilter("my space "), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 10, 10, publicSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPublicSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPublicSpacesByFilterCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int publicSpacesByFilterCount = spaceStorage.getPublicSpacesByFilterCount("mary", new SpaceFilter("add new"));
    assertEquals("publicSpacesByFilterCount must be: " + 0, 0, publicSpacesByFilterCount);

    publicSpacesByFilterCount = spaceStorage.getPublicSpacesByFilterCount("mary", new SpaceFilter("my space"));
    assertEquals("publicSpacesByFilterCount must be: " + 0, 0, publicSpacesByFilterCount);

    publicSpacesByFilterCount = spaceStorage.getPublicSpacesByFilterCount("mary", new SpaceFilter('m'));
    assertEquals("publicSpacesByFilterCount must be: " + 0, 0, publicSpacesByFilterCount);

    publicSpacesByFilterCount = spaceStorage.getPublicSpacesByFilterCount("mary", new SpaceFilter('M'));
    assertEquals("publicSpacesByFilterCount must be: " + 0, 0, publicSpacesByFilterCount);

    publicSpacesByFilterCount = spaceStorage.getPublicSpacesByFilterCount("mary", new SpaceFilter("my space"));
    assertEquals("publicSpacesByFilterCount must be: " + 0, 0, publicSpacesByFilterCount);

    publicSpacesByFilterCount = spaceStorage.getPublicSpacesByFilterCount("newstranger", new SpaceFilter('m'));
    assertEquals("publicSpacesByFilterCount must be: " + 10, 10, publicSpacesByFilterCount);

    publicSpacesByFilterCount = spaceStorage.getPublicSpacesByFilterCount("newstranger", new SpaceFilter('M'));
    assertEquals("publicSpacesByFilterCount must be: " + 10, 10, publicSpacesByFilterCount);

    publicSpacesByFilterCount = spaceStorage.getPublicSpacesByFilterCount("newstranger", new SpaceFilter("add new "));
    assertEquals("publicSpacesByFilterCount must be: " + 10, 10, publicSpacesByFilterCount);

    publicSpacesByFilterCount = spaceStorage.getPublicSpacesByFilterCount("newstranger", new SpaceFilter("my space "));
    assertEquals("publicSpacesByFilterCount must be: " + 10, 10, publicSpacesByFilterCount);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPublicSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetPublicSpacesWithOffset() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> publicSpaces = spaceStorage.getPublicSpaces("mary", 0, 5);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpaces("demo", 0, 5);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpaces("headshot", 0, 5);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 5, 5, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpaces("hellgate", 0, countSpace);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + countSpace, countSpace, publicSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPublicSpacesCount(String)}
   *
   * @since 1.20.-GA
   * @throws Exception
   */
  public void testGetPublicSpacesCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int publicSpacesCount = spaceStorage.getPublicSpacesCount("jame");
    assertEquals("publicSpacesCount must be: 0", 0, publicSpacesCount);

    publicSpacesCount = spaceStorage.getPublicSpacesCount("paul");
    assertEquals("publicSpacesCount must be: 0", 0, publicSpacesCount);

    publicSpacesCount = spaceStorage.getPublicSpacesCount("hacker");
    assertEquals("publicSpacesCount must be: 0", 0, publicSpacesCount);

    publicSpacesCount = spaceStorage.getPublicSpacesCount("nobody");
    assertEquals("publicSpacesCount must be: " + countSpace, countSpace, publicSpacesCount);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPublicSpacesBySearchCondition(String, String, long, long)}, parameter is
   * name or description of space
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetPublicSpacesBySearchCondition() throws Exception {
    int totalNumber = 10;
    for (int i = 0; i < totalNumber; i++) {
      Space sp = this.getSpaceInstance(i);
      spaceStorage.saveSpace(sp, true);
      tearDownSpaceList.add(sp);
    }

    // get space by search name
    String nameSpace = "my space";
    List<Space> foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("register1", nameSpace, 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: 0 ", 0, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("demo", nameSpace, 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: 0 ", 0, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("kratos", nameSpace, 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("kratos", null, 0, 5);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + 0, 0, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("stranger", nameSpace, 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("stranger", "m*", 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("stranger", "*m", 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("stranger", "*m*s", 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("stranger", "*m*s*", 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("stranger", "*m%s%", 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("stranger", "%m*s%", 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("stranger", "%m%s%", 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("stranger", "****", 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + 0, 0, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("stranger", "%%%%%%%", 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + 0, 0, foundSpaceList.size());

    foundSpaceList = spaceStorage.getPublicSpacesBySearchCondition("stranger", "*%*%%%%**", 0, totalNumber);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + 0, 0, foundSpaceList.size());

    // get space by search description
    String description = "add new space ";
    List<Space> descriptionSearch = new ArrayList<Space>();
    descriptionSearch = spaceStorage.getPublicSpacesBySearchCondition("dontcare", description, 0, totalNumber);
    assertNotNull("descriptionSearch must not be null", descriptionSearch);
    assertEquals("tearDownSpaceList.size() must return: " + totalNumber, totalNumber, descriptionSearch.size());

    descriptionSearch = spaceStorage.getPublicSpacesBySearchCondition("tom", description, 0, totalNumber);
    assertNotNull("descriptionSearch must not be null", descriptionSearch);
    assertEquals("tearDownSpaceList.size() must return: 0" + 0, 0, descriptionSearch.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPublicSpacesByFirstCharacterOfName(String, char)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetPublicSpacesByFirstCharacterOfName() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      listSpace[i].setDisplayName("hand of god");
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }

    List<Space> foundSpacesList = spaceStorage.getPublicSpacesByFirstCharacterOfName("tom", 'H', 0, countSpace);
    assertNotNull("foundSpacesList must not be null", foundSpacesList);
    assertEquals("savedSpaces.size() must return: 0", 0, foundSpacesList.size());

    foundSpacesList = spaceStorage.getPublicSpacesByFirstCharacterOfName("tomhank", 'h', 0, countSpace);
    assertNotNull("foundSpacesList must not be null", foundSpacesList);
    assertEquals("savedSpaces.size() must return: " + countSpace, countSpace, foundSpacesList.size());

    foundSpacesList = spaceStorage.getPublicSpacesByFirstCharacterOfName("tomhank", 'h', 0, 5);
    assertNotNull("foundSpacesList must not be null", foundSpacesList);
    assertEquals("savedSpaces.size() must return: " + 5, 5, foundSpacesList.size());

    foundSpacesList = spaceStorage.getPublicSpacesByFirstCharacterOfName("tomhank", '*', 0, countSpace);
    assertNotNull("foundSpacesList must not be null", foundSpacesList);
    assertEquals("savedSpaces.size() must return: " + 0, 0, foundSpacesList.size());

    foundSpacesList = spaceStorage.getPublicSpacesByFirstCharacterOfName("tomhank", 'H', 0, countSpace);
    assertNotNull("foundSpacesList must not be null", foundSpacesList);
    assertEquals("savedSpaces.size() must return: " + countSpace, countSpace, foundSpacesList.size());

    foundSpacesList = spaceStorage.getPublicSpacesByFirstCharacterOfName("bigbang", 'I', 0, countSpace);
    assertNotNull("foundSpacesList must not be null", foundSpacesList);
    assertEquals("savedSpaces.size() must return: " + 0, 0, foundSpacesList.size());

    foundSpacesList = spaceStorage.getPublicSpacesByFirstCharacterOfName("bigbang", 'I', 0, 5);
    assertNotNull("foundSpacesList must not be null", foundSpacesList);
    assertEquals("savedSpaces.size() must return: " + 0, 0, foundSpacesList.size());

    foundSpacesList = spaceStorage.getPublicSpacesByFirstCharacterOfName("bigbang", '*', 0, countSpace);
    assertNotNull("foundSpacesList must not be null", foundSpacesList);
    assertEquals("savedSpaces.size() must return: " + 0, 0, foundSpacesList.size());

    foundSpacesList = spaceStorage.getPublicSpacesByFirstCharacterOfName("bigbang", 'H', 0, 5);
    assertNotNull("foundSpacesList must not be null", foundSpacesList);
    assertEquals("savedSpaces.size() must return: " + 5, 5, foundSpacesList.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetMemberSpaces() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }

    List<Space> memberSpaces = spaceStorage.getMemberSpaces("raul");
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("ghost");
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("dragon");
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("demo");
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: 0", 0, memberSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpacesByFilter(String, org.exoplatform.social.core.space.SpaceFilter, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetMemberSpacesByFilter() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }

    List<Space> memberSpaces = spaceStorage.getMemberSpacesByFilter("raul", new SpaceFilter("my space"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("ghost", new SpaceFilter("add new"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("ghost", new SpaceFilter("space"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("ghost", new SpaceFilter("new"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("ghost", new SpaceFilter('m'), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("ghost", new SpaceFilter('M'), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("ghost", new SpaceFilter('K'), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + 0, 0, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("dragon", new SpaceFilter("add"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFilter("demo", new SpaceFilter("space"), 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: 0", 0, memberSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetMemberSpacesByFilterCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }

    int memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("raul", new SpaceFilter("my space"));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("ghost", new SpaceFilter("add new"));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("ghost", new SpaceFilter("space"));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("ghost", new SpaceFilter("new"));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("ghost", new SpaceFilter('m'));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("ghost", new SpaceFilter('M'));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("ghost", new SpaceFilter('K'));
    assertEquals("memberSpacesCount must be: " + 0, 0, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("dragon", new SpaceFilter("add"));
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFilterCount("demo", new SpaceFilter("space"));
    assertEquals("memberSpacesCount must be: 0", 0, memberSpacesCount);
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpacesBySpaceNameSearchCondition(String, String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetMemberSpacesBySpaceNameSearchCondition() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> memberSpaces = spaceStorage.getMemberSpacesBySpaceNameSearchCondition("raul", "my space", 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesBySpaceNameSearchCondition("ghost", "add new", 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesBySpaceNameSearchCondition("ghost", "space", 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesBySpaceNameSearchCondition("ghost", "new", 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesBySpaceNameSearchCondition("dragon", "add", 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesBySpaceNameSearchCondition("demo", "space", 0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: 0", 0, memberSpaces.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpacesBySpaceNameSearchConditionCount(String, String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetMemberSpacesBySpaceNameSearchConditionCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int memberSpacesCount = spaceStorage.getMemberSpacesBySpaceNameSearchConditionCount("raul", "my space");
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesBySpaceNameSearchConditionCount("ghost", "add new");
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesBySpaceNameSearchConditionCount("ghost", "space");
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesBySpaceNameSearchConditionCount("ghost", "new");
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesBySpaceNameSearchConditionCount("dragon", "add");
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesBySpaceNameSearchConditionCount("demo", "space");
    assertEquals("memberSpacesCount must be: 0", 0, memberSpacesCount);
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpacesByFirstCharacterOfSpaceName(String, char, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetMemberSpacesByFirstCharacterOfSpaceName() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> memberSpaces = spaceStorage.getMemberSpacesByFirstCharacterOfSpaceName("ghost", 'm',  0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFirstCharacterOfSpaceName("ghost", 'M',  0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFirstCharacterOfSpaceName("raul", 'M',  0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFirstCharacterOfSpaceName("dragon", 'M',  0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpacesByFirstCharacterOfSpaceName("ghost", 'K',  0, 10);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + 0, 0, memberSpaces.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpacesByFirstCharacterOfSpaceNameCount(String, char)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetMemberSpacesByFirstCharacterOfSpaceNameCount() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    int memberSpacesCount = spaceStorage.getMemberSpacesByFirstCharacterOfSpaceNameCount("ghost", 'm');
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFirstCharacterOfSpaceNameCount("ghost", 'M');
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFirstCharacterOfSpaceNameCount("raul", 'M');
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFirstCharacterOfSpaceNameCount("dragon", 'M');
    assertEquals("memberSpacesCount must be: " + countSpace, countSpace, memberSpacesCount);

    memberSpacesCount = spaceStorage.getMemberSpacesByFirstCharacterOfSpaceNameCount("ghost", 'K');
    assertEquals("memberSpacesCount must be: " + 0, 0, memberSpacesCount);
  }*/


  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  public void testGetMemberSpacesWithListAccess() throws Exception {
    int countSpace = 10;
    for (int i = 0; i < countSpace; i++) {
      Space space = this.getSpaceInstance(i);
      spaceStorage.saveSpace(space, true);
      tearDownSpaceList.add(space);
    }

    List<Space> memberSpaces = spaceStorage.getMemberSpaces("raul", 0, 5);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + 5, 5, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("ghost", 0, countSpace);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + countSpace, countSpace, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("dragon", 0, 6);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: " + 6, 6, memberSpaces.size());

    memberSpaces = spaceStorage.getMemberSpaces("demo", 0, countSpace);
    assertNotNull("memberSpaces must not be  null", memberSpaces);
    assertEquals("memberSpaces.size() must return: 0", 0, memberSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getSpaceById(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceById() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);

    spaceStorage.saveSpace(space, true);
    tearDownSpaceList.add(space);

    Space savedSpace = spaceStorage.getSpaceById(space.getId());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertNotNull("savedSpace.getId() must not be null", savedSpace.getId());
    assertEquals("space.getId() must return: " + space.getId(), space.getId(), savedSpace.getId());
    assertEquals("space.getPrettyName() must return: " + space.getPrettyName(), space.getPrettyName(), savedSpace.getPrettyName());
    assertEquals("space.getRegistration() must return: " + space.getRegistration(), space.getRegistration(), savedSpace.getRegistration());
    assertEquals("space.getDescription() must return: " + space.getDescription(), space.getDescription(), savedSpace.getDescription());
    assertEquals("space.getType() must return: " + space.getType(), space.getType(), savedSpace.getType());
    assertEquals("space.getVisibility() must return: " + space.getVisibility(), space.getVisibility(), savedSpace.getVisibility());
    assertEquals("space.getPriority() must return: " + space.getPriority(), space.getPriority(), savedSpace.getPriority());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getSpaceByGroupId(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceByGroupId() throws Exception {
    Space space = getSpaceInstance(1);
    spaceStorage.saveSpace(space, true);

    Space savedSpace = spaceStorage.getSpaceByGroupId(space.getGroupId());

    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals(space.getId(), savedSpace.getId());

    tearDownSpaceList.add(savedSpace);

  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getSpacesBySearchCondition(String)}, parameter is
   * name or description of space}
   *
   * @throws Exception
   */
  /*public void testGetSpacesBySearchCondition() throws Exception {
    int totalNumber = 10;
    for (int i = 0; i < totalNumber; i++) {
      Space sp = this.getSpaceInstance(i);
      spaceStorage.saveSpace(sp, true);
      tearDownSpaceList.add(sp);
    }

    // get space by search name
    String nameSpace = "my space";
    List<Space> foundSpaceList = spaceStorage.getSpacesBySearchCondition(nameSpace);
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("*");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + 0, 0, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("m*e");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("y *e");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("m*ce");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("*m*ce*");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("*m*ce");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("*m*");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("*m");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("%m");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("%m%");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("%m*e");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("%m*e%");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("%m*e%");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + totalNumber, totalNumber, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("******");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + 0, 0, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("%%%%%%%%");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + 0, 0, foundSpaceList.size());

    foundSpaceList = spaceStorage.getSpacesBySearchCondition("*%*%*%*%*%");
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertEquals("foundSpaceList.size() must return: " + 0, 0, foundSpaceList.size());

    // get space by search description
    String description = "add new space ";
    List<Space> descriptionSearch = new ArrayList<Space>();
    descriptionSearch = spaceStorage.getSpacesBySearchCondition(description);
    assertNotNull("descriptionSearch must not be null", descriptionSearch);
    assertEquals("tearDownSpaceList.size() must return: " + totalNumber, totalNumber, descriptionSearch.size());

    descriptionSearch = new ArrayList<Space>();
    descriptionSearch = spaceStorage.getSpacesBySearchCondition("add*");
    assertNotNull("descriptionSearch must not be null", descriptionSearch);
    assertEquals("tearDownSpaceList.size() must return: " + totalNumber, totalNumber, descriptionSearch.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getSpacesByFirstCharacterOfName(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  /*public void testGetSpacesByFirstCharacterOfName() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      listSpace[i].setDisplayName("hand of god");
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }

    List<Space> foundSpacesList = spaceStorage.getSpacesByFirstCharacterOfName("H");
    assertNotNull("foundSpacesList must not be null", foundSpacesList);
    assertEquals("savedSpaces.size() must return: " + countSpace, countSpace, foundSpacesList.size());

    foundSpacesList = spaceStorage.getSpacesByFirstCharacterOfName("I");
    assertNotNull("foundSpacesList must not be null", foundSpacesList);
    assertEquals("savedSpaces.size() must return: " + 0, 0, foundSpacesList.size());
  }*/

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getSpaceByUrl(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceByUrl() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    space.setUrl("http://fake.com.vn");
    spaceStorage.saveSpace(space, true);
    tearDownSpaceList.add(space);

    // get saved space
    Space savedSpace = spaceStorage.getSpaceByUrl(space.getUrl());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertNotNull("savedSpace.getId() must not be null", savedSpace.getId());
    assertEquals("space.getId() must return: " + space.getId(), space.getId(), savedSpace.getId());
    assertEquals("space.getName() must return: " + space.getName(),
                 space.getName(),
                 savedSpace.getName());

    //Show that getName() is the same as getPrettyname
    assertTrue("savedSpace.getName().equals(savedSpace.getPrettyName()) must return true",
            savedSpace.getName().equals(savedSpace.getPrettyName()));

    assertEquals("space.getRegistration() must return: " + space.getRegistration(),
                 space.getRegistration(),
                 savedSpace.getRegistration());
    assertEquals("space.getDescription() must return: " + space.getDescription(),
                 space.getDescription(),
                 savedSpace.getDescription());
    assertEquals("space.getType() must return: " + space.getType(),
                 space.getType(),
                 savedSpace.getType());
    assertEquals("space.getVisibility() must equal savedSpace.getVisibility() = "
        + space.getVisibility(), space.getVisibility(), savedSpace.getVisibility());
    assertEquals("space.getPriority() must return: " + space.getPriority(),
                 space.getPriority(),
                 savedSpace.getPriority());
    assertEquals("space.getUrl() must return: " + space.getUrl(),
                 space.getUrl(),
                 savedSpace.getUrl());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getSpaceByPrettyName(String)}
   *
   * @throws Exception
   */
  public void testGetSpaceByPrettyName() throws Exception {
    // number for method getSpaceInstance(int number)
    int number = 1;
    // new space
    Space space = this.getSpaceInstance(number);

    // add to tearDownSpaceList
    tearDownSpaceList.add(space);
    // save to space storage
    spaceStorage.saveSpace(space, true);

    // get space saved by name
    Space foundSpaceList = spaceStorage.getSpaceByPrettyName(space.getPrettyName());
    assertNotNull("foundSpaceList must not be null", foundSpaceList);
    assertNotNull("foundSpaceList.getId() must not be null", foundSpaceList.getId());
    assertEquals("space.getId() must return: " + space.getId(),
                 space.getId(),
                 foundSpaceList.getId());
    assertEquals("space.getPrettyName() must return: " + space.getPrettyName(),
                 space.getPrettyName(),
                 foundSpaceList.getPrettyName());
    assertEquals("space.getRegistration() must return: " + space.getRegistration(),
                 space.getRegistration(),
                 foundSpaceList.getRegistration());
    assertEquals("space.getDescription() must return: " + space.getDescription(),
                 space.getDescription(),
                 foundSpaceList.getDescription());
    assertEquals("space.getType() must return: " + space.getType(),
                 space.getType(),
                 foundSpaceList.getType());
    assertEquals("space.getVisibility() must return: " + space.getVisibility(),
                 space.getVisibility(),
                 foundSpaceList.getVisibility());
    assertEquals("space.getPriority() must return: " + space.getPriority(),
                 space.getPriority(),
                 foundSpaceList.getPriority());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#deleteSpace(String)}
   *
   * @throws Exception
   */
  public void testDeleteSpace() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    spaceStorage.saveSpace(space, true);
    spaceStorage.deleteSpace(space.getId());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#saveSpace(org.exoplatform.social.core.space.model.Space, boolean)}
   *
   * @throws Exception
   */
  public void testSaveSpace() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    tearDownSpaceList.add(space);
    spaceStorage.saveSpace(space, true);
    assertNotNull("space.getId() must not be null", space.getId());
    String newName = "newnamespace";
    space.setDisplayName(newName);
    spaceStorage.saveSpace(space, false);
    assertEquals("spaceStorage.getSpaceById(space.getId()).getName() must return: "
        + newName, newName, spaceStorage.getSpaceById(space.getId())
                                                                .getName());
    assertEquals("space.getName() must return: " + newName, newName, space.getName());
  }

  // TODO : test getSpaceByGroupId without result
  // TODO : save space with null member[]
  // TODO : test space member number
  // TODO : test app data
  // TODO : test accepte invited / pending
}
