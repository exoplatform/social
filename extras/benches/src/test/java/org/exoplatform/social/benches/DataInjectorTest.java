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
package org.exoplatform.social.benches;

import java.util.Collection;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.space.SpaceService;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.organization-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.benches.test.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.benches.configuration.xml")
})
public class DataInjectorTest extends  AbstractKernelTest  {

  DataInjector injector;
  OrganizationService organizationService;

  @Override
  protected void setUp() throws Exception {
    begin();
  }

  @Override
  protected void tearDown() throws Exception {
    end();
  }

  public void testInjectPeople() {

    initInjector();

    Collection<Identity> identities = injector.generatePeople(10);
    assertEquals(identities.size(), 10);
    for (Identity identity : identities) {
      assertNotNull(identity.getId());
    }
  }


  public void testInitRandomUser() {
    organizationService = getOrganizationService();
    injector = new DataInjector(null, null, null, null, organizationService);
    User user = organizationService.getUserHandler().createUserInstance("foo");
    injector.initRandomUser(user, "foo");
    assertNotNull(user.getFirstName());
    assertNotNull(user.getLastName());
  }

  public void testInjectRelations() {

    initInjector();

    injector.generatePeople(10);   /// injecting relations requires some pple
    Collection<Relationship> relationships = injector.generateRelations(10);
    assertEquals(relationships.size(), 10);
    for (Relationship relationship : relationships) {
      assertNotNull(relationship.getId());
    }
  }


  public void testInjectActivities() {

    initInjector();

    injector.generatePeople(10);   /// injecting activities requires some pple
    Collection<Activity> activities = injector.generateActivities(10);
    assertEquals(activities.size(), 10);
    for (Activity activity : activities) {
      assertNotNull(activity.getId());
    }
  }


  private void initInjector() {
    PortalContainer container = PortalContainer.getInstance();
    organizationService = getOrganizationService();
    IdentityManager identityManager = (IdentityManager)container.getComponentInstanceOfType(IdentityManager.class);
    RelationshipManager relationshipManager = (RelationshipManager)container.getComponentInstanceOfType(RelationshipManager.class);
    SpaceService spaceService = (SpaceService)container.getComponentInstanceOfType(SpaceService.class);
    ActivityManager activityManager = (ActivityManager)container.getComponentInstanceOfType(ActivityManager.class);
    injector = new DataInjector(activityManager, identityManager, relationshipManager, spaceService, organizationService);
  }

  private OrganizationService getOrganizationService() {
    PortalContainer container = PortalContainer.getInstance();
    OrganizationService organizationService = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
    return organizationService;
  }

}
