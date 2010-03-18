/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.exoplatform.social.services.rest;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
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
 * BaseTest.java <br />
 * Setting up repository; container...
 * 
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since Mar 3, 2010
 */
@ConfiguredBy( {
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.organization-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration1.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.people.test-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.people.portal-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.application.rest.test-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.space.test-configuration.xml") })
public abstract class BaseTest extends AbstractKernelTest {
  protected static Log                  log                    = ExoLogger.getLogger(BaseTest.class.getName());

  protected static RepositoryService    repositoryService;

  protected static PortalContainer      container;

  protected final static String         REPO_NAME              = "repository".intern();

  protected final static String         SYSTEM_WS              = "system".intern();

  protected final static String         SOCIAL_WS              = "portal-test".intern();

  protected static Node                 root_                  = null;

  protected SessionProvider             sessionProvider;

  private static SessionProviderService sessionProviderService = null;

  protected static ChromatticManager    chromatticManager;

  protected ProviderBinder              providers;

  protected ResourceBinder              binder;

  protected RequestHandlerImpl          requestHandler;

  /**
   * setting up: initContainer; initJCR; init binder, request handler...
   */
  public void setUp() throws Exception {
    initContainer();
    initJCR();
    startSystemSession();
    begin();
    container = PortalContainer.getInstance();
    binder = (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    requestHandler = (RequestHandlerImpl) container.getComponentInstanceOfType(RequestHandlerImpl.class);
    // reset providers to be sure it is clean
    ProviderBinder.setInstance(new ProviderBinder());
    providers = ProviderBinder.getInstance();
    // System.out.println("##########################"+providers);
    ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providers));
    binder.clear();
  }

  public void tearDown() throws Exception {
    chromatticManager.getSynchronization().setSaveOnClose(false);
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
    return binder.bind(resource);
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
    return binder.bind(resourceClass);
  }

  /**
   * unregistry resource object
   * 
   * @param resource
   * @return
   */
  public boolean unregistry(Object resource) {
    // container.unregisterComponentByInstance(resource);
    return binder.unbind(resource.getClass());
  }

  /**
   * unregistry resource class
   * 
   * @param resourceClass
   * @return
   */
  public boolean unregistry(Class<?> resourceClass) {
    // container.unregisterComponent(resourceClass.getName());
    return binder.unbind(resourceClass);
  }

  protected void startSystemSession() {
    sessionProvider = sessionProviderService.getSystemSessionProvider(null);
  }

  protected void startSessionAs(String user) {
    Identity identity = new Identity(user);
    ConversationState state = new ConversationState(identity);
    sessionProviderService.setSessionProvider(null, new SessionProvider(state));
    sessionProvider = sessionProviderService.getSessionProvider(null);
  }

  protected void endSession() {
    sessionProviderService.removeSessionProvider(null);
    startSystemSession();
  }

  private void initContainer() {
    try {
      container = PortalContainer.getInstance();
      chromatticManager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config",
                           "src/test/java/conf/standalone/login.conf");
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize standalone container: " + e.getMessage(), e);
    }
  }

  private void initJCR() {
    try {
      repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
      log.info("repositoryService: " + repositoryService);
      // Initialize data
      Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(SOCIAL_WS);
      log.info("session: " + session);
      root_ = session.getRootNode();
      sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
      log.info("sessionProviderService: " + sessionProviderService);
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize JCR: " + e.getMessage(), e);
    }
  }

}
