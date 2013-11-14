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

package org.exoplatform.social.core.storage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;
import org.exoplatform.social.core.test.QueryNumberTest;

/**
 * Unit Tests for {@link org.exoplatform.social.core.storage.api.SpaceStorage}
 *
 * @since Nov 3, 2010
 * @copyright eXo SAS
 */
@QueryNumberTest
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
  private Identity newStranger;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = (SpaceStorage) this.getContainer().getComponentInstanceOfType(SpaceStorage.class);
    identityStorage = (IdentityStorage) this.getContainer().getComponentInstanceOfType(IdentityStorage.class);


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
    newStranger = new Identity("organization", "newStranger");

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
    identityStorage.saveIdentity(newStranger);

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
    tearDownIdentityList.add(newStranger);

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
    space.setApp("app1,app2");
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/spaces/space" + number);
    String[] managers = new String[] {"demo", "tom"};
    String[] members = new String[] {"raul", "ghost", "dragon"};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
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
  
  private List<Space> getSpaceWithRoot(SpaceFilter filter) {
    if (filter == null) {
      return spaceStorage.getAllSpaces();
    } else {
      return spaceStorage.getSpacesByFilter(filter, 0, 200);
    }
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAllSpaces()}
   *
   * @throws Exception
   */
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1006)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1006)
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
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(2026)
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
  
  @MaxQueryNumber(800)
  public void testGetAccessibleSpacesByFilterApp() throws Exception {
    Space space = this.getSpaceInstance(1);
    spaceStorage.saveSpace(space, true);
    tearDownSpaceList.add(space);
    
    SpaceFilter filter = new SpaceFilter("my space");
    filter.setAppId("app1,app2");
    
    List<Space> accessibleSpacesByFilter = spaceStorage.getAccessibleSpacesByFilter("demo", filter, 0, 10);
    assertEquals(1, accessibleSpacesByFilter.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  @MaxQueryNumber(2026)
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
  @MaxQueryNumber(1006)
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
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getAccessibleSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1006)
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
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getEditableSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1006)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPendingSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPublicSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  @MaxQueryNumber(1006)
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
  @MaxQueryNumber(1029)
  public void testGetPublicSpacesByFilter() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    for (int i = 0; i < countSpace; i ++) {
      listSpace[i] = this.getSpaceInstance(i);
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    List<Space> publicSpaces = spaceStorage.getPublicSpacesByFilter(mary.getRemoteId(), new SpaceFilter("add new"), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter(mary.getRemoteId(), new SpaceFilter("my space"), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter(mary.getRemoteId(), new SpaceFilter('m'), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter(mary.getRemoteId(), new SpaceFilter('M'), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter(demo.getRemoteId(), new SpaceFilter("my space"), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 0, 0, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter(newStranger.getRemoteId(), new SpaceFilter('m'), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 10, 10, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter(newStranger.getRemoteId(), new SpaceFilter('M'), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 10, 10, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter(newStranger.getRemoteId(), new SpaceFilter("add new "), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 10, 10, publicSpaces.size());

    publicSpaces = spaceStorage.getPublicSpacesByFilter(newStranger.getRemoteId(), new SpaceFilter("my space "), 0, 10);
    assertNotNull("publicSpaces must not be  null", publicSpaces);
    assertEquals("publicSpaces.size() must return: " + 10, 10, publicSpaces.size());
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getPublicSpacesByFilterCount(String, org.exoplatform.social.core.space.SpaceFilter)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1006)
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
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(1029)
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
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getMemberSpaces(String, long, long)}
   *
   * @throws Exception
   * @since 1.2.0-GA
   */
  @MaxQueryNumber(1029)
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
  @MaxQueryNumber(111)
  public void testGetSpaceById() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);

    spaceStorage.saveSpace(space, true);
    tearDownSpaceList.add(space);

    Space savedSpace = spaceStorage.getSpaceById(space.getId());
    assertNotNull("savedSpace must not be null", savedSpace);
    assertNotNull("savedSpace.getId() must not be null", savedSpace.getId());
    assertNotNull("savedSpace.getApp() must not be null", savedSpace.getApp());
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
  @MaxQueryNumber(111)
  public void testGetSpaceByGroupId() throws Exception {
    Space space = getSpaceInstance(1);
    spaceStorage.saveSpace(space, true);

    Space savedSpace = spaceStorage.getSpaceByGroupId(space.getGroupId());

    assertNotNull("savedSpace must not be null", savedSpace);
    assertEquals(space.getId(), savedSpace.getId());

    tearDownSpaceList.add(savedSpace);

  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getSpaceByUrl(String)}
   *
   * @throws Exception
   */
  @MaxQueryNumber(111)
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
  @MaxQueryNumber(111)
  public void testGetSpaceByPrettyName() throws Exception {
    // number for method getSpaceInstance(int number)
    int number = 1;
    // new space
    Space space = this.getSpaceInstance(number);

    // add to tearDownSpaceList
    tearDownSpaceList.add(space);
    // save to space activityStorage
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
  @MaxQueryNumber(471)
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
  @MaxQueryNumber(171)
  public void testSaveSpace() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    tearDownSpaceList.add(space);
    spaceStorage.saveSpace(space, true);
    assertNotNull("space.getId() must not be null", space.getId());
    String newName = "newnamespace";
    space.setDisplayName(newName);
    space.setPrettyName(space.getDisplayName());
    spaceStorage.saveSpace(space, false);
    assertEquals(newName, spaceStorage.getSpaceById(space.getId()).getName());
    assertEquals(newName, space.getName());

    Space got = spaceStorage.getSpaceById(space.getId());
    assertEquals(null, got.getAvatarUrl());
  }

  /**
   * Test {@link SpaceStorage#renameSpace(Space, String)}
   *
   * @throws Exception
   * @since 1.2.8
   */
  @MaxQueryNumber(495)
  public void testRenameSpace() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    tearDownSpaceList.add(space);
    spaceStorage.saveSpace(space, true);
    assertNotNull("space.getId() must not be null", space.getId());
    String newName = "newnamespace";
    space.setDisplayName(newName);
    space.setPrettyName(space.getDisplayName());
    spaceStorage.saveSpace(space, false);
    assertEquals("spaceStorage.getSpaceById(space.getId()).getName() must return: "
        + newName, newName, spaceStorage.getSpaceById(space.getId())
                                                                .getPrettyName());
    assertEquals("space.getName() must return: " + newName, newName, space.getPrettyName());

    Space got = spaceStorage.getSpaceById(space.getId());
    assertEquals(null, got.getAvatarUrl());
    
    Identity spaceIdentity = new Identity(SpaceIdentityProvider.NAME, got.getPrettyName());
    identityStorage.saveIdentity(spaceIdentity);
    tearDownIdentityList.add(spaceIdentity);
    
    String newDisplayName = "new display name";
    spaceStorage.renameSpace(space, newDisplayName);
    
    got = spaceStorage.getSpaceById(space.getId());
    assertEquals(newDisplayName, got.getDisplayName());
  }
  
  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#saveSpace(org.exoplatform.social.core.space.model.Space, boolean)}
   *
   * @throws Exception
   */
  @MaxQueryNumber(210)
  public void testSaveSpaceAvatar() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
    assertNotNull(avatarAttachment); 
    space.setAvatarAttachment(avatarAttachment);
    
    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    Profile profile = new Profile(identity);
    identity.setProfile(profile);
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    identityStorage.saveIdentity(identity);
    identityStorage.saveProfile(profile);
    
    tearDownIdentityList.add(identity);
    spaceStorage.saveSpace(space, true);

    Space got = spaceStorage.getSpaceByPrettyName(space.getPrettyName());
    tearDownSpaceList.add(got);

    assertNotNull(got.getAvatarUrl());
    String avatarRandomURL = got.getAvatarUrl();
    int indexOfLastupdatedParam = avatarRandomURL.indexOf("/?upd=");
    String avatarURL = null;
    if(indexOfLastupdatedParam != -1){
      avatarURL = avatarRandomURL.substring(0,indexOfLastupdatedParam);
    } else {
      avatarURL = avatarRandomURL;
    }
    assertEquals(LinkProvider.escapeJCRSpecialCharacters(
            String.format(
              "/rest/jcr/repository/portal-test/production/soc:providers/soc:space/soc:%s/soc:profile/soc:avatar",
              space.getPrettyName())),
              avatarURL);
    
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#saveSpace(org.exoplatform.social.core.space.model.Space, boolean)} with isNew is false
   *
   * @throws Exception
   */
  @MaxQueryNumber(288)
  public void testUpdateSpace() throws Exception {
    int number = 1;
    Space space = this.getSpaceInstance(number);
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
    assertNotNull("avatar attachment should not be null", avatarAttachment);
    space.setAvatarAttachment(avatarAttachment);
    tearDownSpaceList.add(space);
    spaceStorage.saveSpace(space, true);
    
    Identity identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    Profile profile = new Profile(identity);
    identity.setProfile(profile);
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    identityStorage.saveIdentity(identity);
    identityStorage.saveProfile(profile);
    tearDownIdentityList.add(identity);

    //
    Space spaceForUpdate = spaceStorage.getSpaceById(space.getId());
    spaceStorage.saveSpace(spaceForUpdate, false);

    //
    Space got = spaceStorage.getSpaceById(spaceForUpdate.getId());

    assertNotNull("avatar URL should not be null",got.getAvatarUrl());
  }
  
  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  @MaxQueryNumber(210)
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
      
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    
    //visible with remoteId = 'demo'  return 10 spaces with SpaceFilter = null
    {
      int countSpace1 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace1, countSpace1, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'demo'  return 10 spaces with SpaceFilter configured firstCharacter 'M'
    {
      int countSpace2 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter('M'));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace2, countSpace2, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'demo'  return 0 spaces with SpaceFilter configured firstCharacter 'A'
    {
      int countSpace3 = 0;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter('A'));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace3, countSpace3, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'mary'  return 6 spaces with SpaceFilter = null
    {
      int privateSpace1 = 6;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", null);
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace1, privateSpace1, privateSpaces.size());
    }
    
    //visible with remoteId = 'mary'  return 6 spaces with SpaceFilter configured firstCharacter 'M'
    {
      int privateSpace2 = 6;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", new SpaceFilter('M'));
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace2, privateSpace2, privateSpaces.size());
    }
    
    //visible with remoteId = 'mary'  return 0 spaces with SpaceFilter configured firstCharacter 'A'
    {
      int privateSpace3 = 0;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", new SpaceFilter('A'));
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace3, privateSpace3, privateSpaces.size());
    }
  }
  
  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  @MaxQueryNumber(210)
  public void testGetVisibleSpacesWithValidate() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    
    //there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i ++) {
    
      if (i < 6)
         //[0->5] :: there are 6 spaces with visible = 'private'
        listSpace[i] = this.getSpaceInstance(i, Space.PRIVATE, Space.VALIDATION, "demo");
      else
        //[6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.VALIDATION, "demo");
      
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    
    //visible with remoteId = 'demo'  return 10 spaces with SpaceFilter = null
    {
      int countSpace1 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace1, countSpace1, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'demo'  return 10 spaces with SpaceFilter configured firstCharacter 'M'
    {
      int countSpace2 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter('M'));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace2, countSpace2, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'demo'  return 0 spaces with SpaceFilter configured firstCharacter 'A'
    {
      int countSpace3 = 0;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter('A'));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace3, countSpace3, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'mary'  return 6 spaces with SpaceFilter = null
    {
      int privateSpace1 = 6;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", null);
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace1, privateSpace1, privateSpaces.size());
    }
    
    //visible with remoteId = 'mary'  return 6 spaces with SpaceFilter configured firstCharacter 'M'
    {
      int privateSpace2 = 6;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", new SpaceFilter('M'));
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace2, privateSpace2, privateSpaces.size());
    }
    
    //visible with remoteId = 'mary'  return 0 spaces with SpaceFilter configured firstCharacter 'A'
    {
      int privateSpace3 = 0;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", new SpaceFilter('A'));
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace3, privateSpace3, privateSpaces.size());
    }
  }
  
  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  @MaxQueryNumber(210)
  public void testGetVisibleSpacesFilterByName() throws Exception {
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
      
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    
    //visible with remoteId = 'demo'  return 10 spaces with SpaceFilter = null
    {
      int countSpace1 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace1, countSpace1, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'demo'  return 10 spaces with SpaceFilter configured firstCharacter 'M'
    {
      int countSpace2 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter("my space"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace2, countSpace2, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'demo'  return 0 spaces with SpaceFilter configured firstCharacter 'A'
    {
      int countSpace3 = 0;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter("your space"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace3, countSpace3, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'mary'  return 6 spaces with SpaceFilter = null
    {
      int privateSpace1 = 6;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", null);
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace1, privateSpace1, privateSpaces.size());
    }
    
    //visible with remoteId = 'mary'  return 6 spaces with SpaceFilter configured firstCharacter 'M'
    {
      int privateSpace2 = 6;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", new SpaceFilter("my space"));
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace2, privateSpace2, privateSpaces.size());
    }
    
    //visible with remoteId = 'mary'  return 0 spaces with SpaceFilter configured firstCharacter 'A'
    {
      int privateSpace3 = 0;
      List<Space> privateSpaces = spaceStorage.getVisibleSpaces("mary", new SpaceFilter("your space"));
      assertNotNull("visibleSpaces must not be  null", privateSpaces);
      assertEquals("visibleSpaces() must return: " + privateSpace3, privateSpace3, privateSpaces.size());
    }
  }
  
  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  @MaxQueryNumber(210)
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
      
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    
    //visible with remoteId = 'demo'  return 10 spaces
    {
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace, countSpace, visibleAllSpaces.size());
    }
    
    
    
    //visible with remoteId = 'mary'  return 6 spaces: can see
    {
      int registrationCloseSpaceCount = 6;
      List<Space> registrationCloseSpaces = spaceStorage.getVisibleSpaces("mary", null);
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
  @MaxQueryNumber(210)
  public void testGetVisibleSpacesCloseRegistrationByFilter() throws Exception {
    int countSpace = 10;
    Space []listSpace = new Space[10];
    
    //there are 6 spaces with visible = 'private'
    for (int i = 0; i < countSpace; i ++) {
    
      if (i < 6)
         //[0->5] :: there are 6 spaces with visible = 'private' and manager = "demo"
        listSpace[i] = this.getSpaceInstance(i, Space.PRIVATE, Space.CLOSE, "demo");
      else
        //[6->9]:: there are 4 spaces with visible = 'hidden'
        listSpace[i] = this.getSpaceInstance(i, Space.HIDDEN, Space.CLOSE, "demo");
      
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    
    //visible with remoteId = 'demo'  return 10 spaces with SpaceFilter configured firstCharacter 'M'
    {
      int countSpace1 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter('M'));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace1, countSpace1, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'demo'  return 10 spaces with SpaceFilter configured firstCharacter 'M'
    {
      
      int countSpace2 = 0;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter('A'));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace2, countSpace2, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'demo'  return 10 spaces with SpaceFilter configured name "my space"
    {
      
      int countSpace3 = 10;
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", new SpaceFilter("my space"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace3, countSpace3, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'root'  return 10 spaces with SpaceFilter configured name "my space"
    {
      
      int countSpace4 = 10;
      List<Space> visibleAllSpaces = getSpaceWithRoot(new SpaceFilter("my space"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace4, countSpace4, visibleAllSpaces.size());
    }
    
   //visible with remoteId = 'root'  return 10 spaces with SpaceFilter is null.
    {
      
      int countSpace5 = 10;
      List<Space> visibleAllSpaces = getSpaceWithRoot(null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace5, countSpace5, visibleAllSpaces.size());
    }
    
    //visible with remoteId = 'root'  return 0 spaces with SpaceFilter configured name "my space"
    {
      
      int countSpace6 = 0;
      List<Space> visibleAllSpaces = getSpaceWithRoot(new SpaceFilter("your space"));
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace6, countSpace6, visibleAllSpaces.size());
    }
    
       
    //visible with remoteId = 'mary'  return 6 spaces: see although with SpaceFilter configured firstCharacter 'M'
    {
      int registrationCloseSpaceCount1 = 6;
      List<Space> registrationCloseSpaces = spaceStorage.getVisibleSpaces("mary", new SpaceFilter('M'));
      assertNotNull("registrationCloseSpaces must not be  null", registrationCloseSpaces);
      assertEquals("registrationCloseSpaces must return: " + registrationCloseSpaceCount1, registrationCloseSpaceCount1, registrationCloseSpaces.size());
    }
    
    //visible with remoteId = 'root'  return 10 spaces: see all spaces:: check at SpaceServiceImpl
    {
      int registrationCloseSpaceCount2 = 10;
      List<Space> registrationCloseSpaces1 = spaceStorage.getSpacesByFilter(new SpaceFilter('M'), 0, 200);
      assertNotNull("registrationCloseSpaces must not be  null", registrationCloseSpaces1);
      assertEquals("registrationCloseSpaces must return: " + registrationCloseSpaceCount2, registrationCloseSpaceCount2, registrationCloseSpaces1.size());
    }
  }
  
  /**
   * Test {@link org.exoplatform.social.core.storage.SpaceStorage#getVisibleSpaces(String)}
   *
   * @throws Exception
   * @since 1.2.5-GA
   */
  @MaxQueryNumber(318)
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
      
      spaceStorage.saveSpace(listSpace[i], true);
      tearDownSpaceList.add(listSpace[i]);
    }
    
    //visible with remoteId = 'demo'  return 10 spaces
    {
      List<Space> visibleAllSpaces = spaceStorage.getVisibleSpaces("demo", null);
      assertNotNull("visibleSpaces must not be  null", visibleAllSpaces);
      assertEquals("visibleSpaces() must return: " + countSpace, countSpace, visibleAllSpaces.size());
    }
    
    //visible with invited = 'mary'  return 6 spaces
    {
      int invitedSpaceCount1 = 6;
      List<Space> invitedSpaces1 = spaceStorage.getVisibleSpaces("mary", null);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces1);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount1, invitedSpaceCount1, invitedSpaces1.size());
    }
    
    //visible with invited = 'hacker'  return 6 spaces
    {
      int invitedSpaceCount1 = 6;
      List<Space> invitedSpaces1 = spaceStorage.getVisibleSpaces("hacker", null);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces1);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount1, invitedSpaceCount1, invitedSpaces1.size());
    }
    
    //visible with invited = 'paul'  return 6 spaces
    {
      int invitedSpaceCount2 = 6;
      List<Space> invitedSpaces2 = spaceStorage.getVisibleSpaces("paul", null);
      assertNotNull("invitedSpaces must not be  null", invitedSpaces2);
      assertEquals("invitedSpaces must return: " + invitedSpaceCount2, invitedSpaceCount2, invitedSpaces2.size());
    }
  }
  
  // TODO : test getSpaceByGroupId without result
  // TODO : save space with null member[]
  // TODO : test space member number
  // TODO : test app data
  // TODO : test accepte invited / pending
}
