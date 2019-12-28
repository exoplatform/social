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
package org.exoplatform.social.service.test;

import java.util.*;

import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.component.test.*;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.*;
import org.exoplatform.services.security.*;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.*;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * AbstractServiceTest.java
 *
 * @author  <a href="http://hoatle.net">hoatle</a>
 * @since   May 27, 2010 3:26:01 PM
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.common.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.application.registry.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.service.test.configuration.xml")
})
public abstract class AbstractServiceTest extends BaseExoTestCase {
  protected static Log log = ExoLogger.getLogger(AbstractServiceTest.class.getName());
  protected ProviderBinder providerBinder;
  protected ResourceBinder resourceBinder;
  protected RequestHandlerImpl requestHandler;
  /** . */
  public static KernelBootstrap socialBootstrap = null;


  protected void setUp() throws Exception {
    resourceBinder = getContainer().getComponentInstanceOfType(ResourceBinder.class);
    requestHandler = getContainer().getComponentInstanceOfType(RequestHandlerImpl.class);
    // Reset providers to be sure it is clean
    ProviderBinder.setInstance(new ProviderBinder());
    providerBinder = ProviderBinder.getInstance();
    ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providerBinder));
    resourceBinder.clear();
    configures();
    begin();

    deleteAllRelationships();
    deleteAllSpaces();
    deleteAllIdentitiesWithActivities();
  }
  
  /**
   * trick to configure for Unit Testing avoid NPE in SpaceUtils at line 850.
   */
  private void configures() {
    UserACL acl = (UserACL) getContainer().getComponentInstanceOfType(UserACL.class);
    //trick to configure for Unit Testing avoid NPE in SpaceUtils at line 850.
    acl.setAdminMSType("manager");
  }

  protected void tearDown() throws Exception {
    deleteAllRelationships();
    deleteAllSpaces();
    deleteAllIdentitiesWithActivities();

    endSession();
    end();
  }
 
  /**
   * registry resource object
   *
   * @param resource
   * @return
   * @throws Exception
   */
  public boolean registry(Object resource) throws Exception {
    // container.registerComponentInstance(resource);
    return resourceBinder.bind(resource);
  }
  /**
   * registry resource class
   *
   * @param resourceClass
   * @return
   * @throws Exception
   */
  public boolean registry(Class<?> resourceClass) throws Exception {
    // container.registerComponentImplementation(resourceClass.getName(),
    // resourceClass);
    return resourceBinder.bind(resourceClass);
  }

  /**
   * unregistry resource object
   *
   * @param resource
   * @return
   * @deprecated Use {@link #addResource(Object, javax.ws.rs.core.MultivaluedMap)} instead.
   *             Will be removed by 1.3.x.
   */
  @Deprecated
  public boolean unregistry(Object resource) {
    // container.unregisterComponentByInstance(resource);
    return resourceBinder.unbind(resource.getClass());
  }

  /**
   * unregistry resource class
   *
   * @param resourceClass
   * @return
   * @deprecated Use {@link #removeResource(Class)} instead.
   *             Will be removed by 1.3.x
   */
  @Deprecated
  public boolean unregistry(Class<?> resourceClass) {
    // container.unregisterComponent(resourceClass.getName());
    return resourceBinder.unbind(resourceClass);
  }

   /**
    * Registers supplied class as per-request root resource if it has valid
    * JAX-RS annotations and no one resource with the same UriPattern already
    * registered.
    *
    * @param resourceClass class of candidate to be root resource
    * @param properties optional resource properties. It may contains additional
    *        info about resource, e.g. description of resource, its
    *        responsibility, etc. This info can be retrieved
    *        {@link org.exoplatform.services.rest.ObjectModel#getProperties()}. This parameter may be
    *        <code>null</code>
    */
  public void addResource(final Class<?> resourceClass, MultivaluedMap<String, String> properties) {
    resourceBinder.addResource(resourceClass, properties);
  }

  /**
    * Registers supplied Object as singleton root resource if it has valid JAX-RS
    * annotations and no one resource with the same UriPattern already
    * registered.
    *
    * @param resource candidate to be root resource
    * @param properties optional resource properties. It may contains additional
    *        info about resource, e.g. description of resource, its
    *        responsibility, etc. This info can be retrieved
    *        {@link org.exoplatform.services.rest.ObjectModel#getProperties()}. This parameter may be
    *        <code>null</code>
   */
  public void addResource(final Object resource, MultivaluedMap<String, String> properties) {
    resourceBinder.addResource(resource, properties);
  }


  /**
   * Removes the resource instance of provided class from root resource container.
   *
   * @param clazz the class of resource
   */
  public void removeResource(Class clazz) {
    resourceBinder.removeResource(clazz);
  }

  protected void startSessionAs(String user) {
    startSessionAs(user, new HashSet<MembershipEntry>());
  }

  protected void startSessionAs(String user, Collection<MembershipEntry> memberships) {
    Identity identity = new Identity(user, memberships);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  protected void endSession() {
    ConversationState.setCurrent(null);
  }

  protected void deleteAllIdentitiesWithActivities() throws Exception {
    IdentityManager identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    ActivityManager activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);

    ListAccess<org.exoplatform.social.core.identity.model.Identity> organizationIdentities = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, new ProfileFilter(), true);
    Arrays.stream(organizationIdentities.load(0, organizationIdentities.getSize()))
            .forEach(identity -> {
              RealtimeListAccess<ExoSocialActivity> identityActivities = activityManager.getActivitiesWithListAccess(identity);
              Arrays.stream(identityActivities.load(0, identityActivities.getSize()))
                      .forEach(activity -> activityManager.deleteActivity(activity));
              identityManager.deleteIdentity(identity);
            });

    ListAccess<org.exoplatform.social.core.identity.model.Identity> spaceIdentities = identityManager.getIdentitiesByProfileFilter(SpaceIdentityProvider.NAME, new ProfileFilter(), true);
    Arrays.stream(spaceIdentities.load(0, spaceIdentities.getSize()))
            .forEach(identity -> {
              RealtimeListAccess<ExoSocialActivity> identityActivities = activityManager.getActivitiesOfSpaceWithListAccess(identity);
              Arrays.stream(identityActivities.load(0, identityActivities.getSize()))
                      .forEach(activity -> activityManager.deleteActivity(activity));
              identityManager.deleteIdentity(identity);
            });
  }

  protected void deleteAllSpaces() throws Exception {
    SpaceService spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    ListAccess<Space> spaces = spaceService.getAllSpacesWithListAccess();
    Arrays.stream(spaces.load(0, spaces.getSize())).forEach(space -> spaceService.deleteSpace(space));
  }

  protected void deleteAllRelationships() throws Exception {
    RelationshipManager relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);
    IdentityManager identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    ListAccess<org.exoplatform.social.core.identity.model.Identity> identities = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, new ProfileFilter(), true);
    for(org.exoplatform.social.core.identity.model.Identity identity : identities.load(0, identities.getSize())) {
      ListAccess<org.exoplatform.social.core.identity.model.Identity> relationships = relationshipManager.getAllWithListAccess(identity);
      Arrays.stream(relationships.load(0, relationships.getSize()))
              .forEach(relationship -> relationshipManager.deny(identity, relationship));
    }
  }
}
