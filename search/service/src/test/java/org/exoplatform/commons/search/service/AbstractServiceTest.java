/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.search.service;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.ContainerResponseWriter;
import org.exoplatform.services.rest.impl.ContainerRequest;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.rest.impl.InputHeadersMap;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.RequestHandlerImpl;
import org.exoplatform.services.rest.impl.ResourceBinder;
import org.exoplatform.services.rest.impl.ResourcePublicationException;
import org.exoplatform.services.rest.tools.DummyContainerResponseWriter;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.commons.search.service.UnifiedSearchMockHttpServletRequest;
import org.exoplatform.commons.search.service.UnifiedSearchServiceTest;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Mar 25, 2013  
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml")
})
public abstract class  AbstractServiceTest extends BaseExoTestCase{

  protected static Log    LOG         = ExoLogger.getLogger(UnifiedSearchServiceTest.class);

  protected static String USER_ROOT   = "root";

  protected static String USER_JOHN   = "john";

  protected static String USER_DEMO   = "demo";
  
  protected RequestHandlerImpl requestHandler;  
  protected ResourceBinder resourceBinder;
  
  public AbstractServiceTest() throws Exception{
    
  }
  
  public void setUp() throws Exception {
    begin();
    ConversationState conversionState = ConversationState.getCurrent();
    if(conversionState == null) {
      conversionState = new ConversationState(new Identity(USER_ROOT));
      ConversationState.setCurrent(conversionState);
    }
    PortalContainer portalContainer = (PortalContainer)ExoContainerContext.getCurrentContainer();
    resourceBinder = (ResourceBinder) portalContainer.getComponentInstanceOfType(ResourceBinder.class);
    //resourceBinder = (ResourceBinder) getService(ResourceBinder.class);
    //requestHandler = (RequestHandlerImpl) portalContainer.getComponentInstanceOfType(RequestHandlerImpl.class);
    requestHandler = (RequestHandlerImpl) getService(RequestHandlerImpl.class);
  }

  public void tearDown() throws Exception {
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
    try {
      addResource(resource, null);
      return true;
    } catch (ResourcePublicationException e) {
      LOG.warn(e.getMessage());
      return false;
    }
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
 @SuppressWarnings("rawtypes")
 public void removeResource(Class clazz) {
   resourceBinder.removeResource(clazz);
 } 
  /**
   * gets response without provided writer
   * @param method
   * @param requestURI
   * @param baseURI
   * @param headers
   * @param data
   * @return
   * @throws Exception
   */
  public ContainerResponse service(String method,
                                   String requestURI,
                                   String baseURI,
                                   MultivaluedMap<String, String> headers,
                                   byte[] data) throws Exception {
    return service(method, requestURI, baseURI, headers, data, new DummyContainerResponseWriter());
  }  
  
  /**
   * gets response with provided writer
   * @param method
   * @param requestURI
   * @param baseURI
   * @param headers
   * @param data
   * @param writer
   * @return
   * @throws Exception
   */
  public ContainerResponse service(String method,
                                   String requestURI,
                                   String baseURI,
                                   Map<String, List<String>> headers,
                                   byte[] data,
                                   ContainerResponseWriter writer) throws Exception {

    if (headers == null) {
      headers = new MultivaluedMapImpl();
    }

    ByteArrayInputStream in = null;
    if (data != null) {
      in = new ByteArrayInputStream(data);
    }

    EnvironmentContext envctx = new EnvironmentContext();
    HttpServletRequest httpRequest = new UnifiedSearchMockHttpServletRequest("",
                                                                      in,
                                                                      in != null ? in.available() : 0,
                                                                      method,
                                                                      headers);
    envctx.put(HttpServletRequest.class, httpRequest);
    EnvironmentContext.setCurrent(envctx);
    ContainerRequest request = new ContainerRequest(method,
                                                    new URI(requestURI),
                                                    new URI(baseURI),
                                                    in,
                                                    new InputHeadersMap(headers));
    ContainerResponse response = new ContainerResponse(writer);
    requestHandler.handleRequest(request, response);
    return response;
  }  
  
  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }  
}
