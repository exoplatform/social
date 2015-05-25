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
package org.exoplatform.social.core.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.jboss.byteman.contrib.bmunit.BMUnit;

/**
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Jul 6, 2010
 * @copyright eXo SAS
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.common.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml")
})
public abstract class AbstractCoreTest extends BaseExoTestCase {

  public static boolean wantCount = false;
  private static int count;
  private int maxQuery;
  private final Log LOG = ExoLogger.getLogger(AbstractCoreTest.class);

  protected SpaceService spaceService;
  protected Session session;
  
  
  @Override
  protected void setUp() throws Exception {
    begin();
    session = getSession();

    // If is query number test, init byteman
    if (getClass().isAnnotationPresent(QueryNumberTest.class)) {
      count = 0;
      maxQuery = 0;
      BMUnit.loadScriptFile(getClass(), "queryCount", "src/test/resources");
    }

    //
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    
  }

  @Override
  protected void tearDown() throws Exception {
    wantCount = false;
    session = null;
    end();
  }
  
  protected <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }

  // Fork from Junit 3.8.2
  @Override
  /**
   * Override to run the test and assert its state.
   * @throws Throwable if any exception is thrown
   */
  protected void runTest() throws Throwable {
    String fName = getName();
    assertNotNull("TestCase.fName cannot be null", fName); // Some VMs crash when calling getMethod(null,null);
    Method runMethod= null;
    try {
      // use getMethod to get all public inherited
      // methods. getDeclaredMethods returns all
      // methods of this class but excludes the
      // inherited ones.
      runMethod= getClass().getMethod(fName, (Class[])null);
    } catch (NoSuchMethodException e) {
      fail("Method \""+fName+"\" not found");
    }
    if (!Modifier.isPublic(runMethod.getModifiers())) {
      fail("Method \""+fName+"\" should be public");
    }

    try {
      MaxQueryNumber queryNumber = runMethod.getAnnotation(MaxQueryNumber.class);
      if (queryNumber != null) {
        wantCount = true;
        maxQuery = queryNumber.value();
      }
      runMethod.invoke(this);
    }
    catch (InvocationTargetException e) {
      e.fillInStackTrace();
      throw e.getTargetException();
    }
    catch (IllegalAccessException e) {
      e.fillInStackTrace();
      throw e;
    }

    if (wantCount && count > maxQuery) {
      throw new AssertionFailedError(""+ count + " JDBC queries was executed but the maximum is : " + maxQuery);
    }
    
  }

  // Called by byteman
  public static void count() {
    ++count;
   }

  private Session getSession() throws RepositoryException {
    PortalContainer container = PortalContainer.getInstance();
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstance(RepositoryService.class);
    ManageableRepository repository = repositoryService.getCurrentRepository();
    return repository.getSystemSession("portal-test");
  }

  /**
   * Creates new space with out init apps.
   *
   * @param space
   * @param creator
   * @param invitedGroupId
   * @return
   * @since 1.2.0-GA
   */
  protected Space createSpaceNonInitApps(Space space, String creator, String invitedGroupId) {
    // Creates new space by creating new group
    String groupId = null;
    try {
      groupId = SpaceUtils.createGroup(space.getDisplayName(), creator);
    } catch (SpaceException e) {
      LOG.error("Error while creating group", e);
    }

    if (invitedGroupId != null) {
      // Invites user in group join to new created space.
      // Gets users in group and then invites user to join into space.
      OrganizationService org = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
      try {
        PageList<User> groupMembersAccess = org.getUserHandler().findUsersByGroup(invitedGroupId);
        List<User> users = groupMembersAccess.getAll();

        for (User user : users) {
          String userId = user.getUserName();
          if (!userId.equals(creator)) {
            String[] invitedUsers = space.getInvitedUsers();
            if (!ArrayUtils.contains(invitedUsers, userId)) {
              invitedUsers = (String[]) ArrayUtils.add(invitedUsers, userId);
              space.setInvitedUsers(invitedUsers);
            }
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to invite users from group " + invitedGroupId, e);
      }
    }
    String[] managers = new String[] { creator };
    space.setManagers(managers);
    space.setGroupId(groupId);
    space.setUrl(space.getPrettyName());
    try {
      spaceService.saveSpace(space, true);
    } catch (SpaceException e) {
      LOG.warn("Error while saving space", e);
    }
    return space;
  }

}
