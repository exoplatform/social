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

import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.component.test.KernelBootstrap;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.services.rest.impl.ProviderBinder;
import org.exoplatform.services.rest.impl.RequestHandlerImpl;
import org.exoplatform.services.rest.impl.ResourceBinder;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * AbstractServiceTest.java
 *
 * @author  <a href="http://hoatle.net">hoatle</a>
 * @since   May 27, 2010 3:26:01 PM
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.common.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.service.test.configuration.xml")
})
public abstract class AbstractServiceTest extends BaseExoTestCase {
  protected static Log log = ExoLogger.getLogger(AbstractServiceTest.class.getName());
  protected SessionProvider sessionProvider;
  protected ProviderBinder providerBinder;
  protected ResourceBinder resourceBinder;
  protected RequestHandlerImpl requestHandler;
  private static SessionProviderService sessionProviderService;
  /** . */
  public static KernelBootstrap socialBootstrap = null;


  protected void setUp() throws Exception {
    sessionProviderService = (SessionProviderService) getContainer().
                                                      getComponentInstanceOfType(SessionProviderService.class);
    resourceBinder = (ResourceBinder) getContainer().getComponentInstanceOfType(ResourceBinder.class);
    requestHandler = (RequestHandlerImpl) getContainer().getComponentInstanceOfType(RequestHandlerImpl.class);
    // Reset providers to be sure it is clean
    ProviderBinder.setInstance(new ProviderBinder());
    providerBinder = ProviderBinder.getInstance();
    ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providerBinder));
    resourceBinder.clear();
    configures();
    begin();
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

  protected void startSystemSession() {
    sessionProvider = sessionProviderService.getSystemSessionProvider(null);
  }

  protected void startSessionAs(String user) {
    Identity identity = new Identity(user);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
    sessionProviderService.setSessionProvider(null, new SessionProvider(state));
    sessionProvider = sessionProviderService.getSessionProvider(null);
  }

  protected void endSession() {
    sessionProviderService.removeSessionProvider(null);
    ConversationState.setCurrent(null);
    startSystemSession();
  }

}
