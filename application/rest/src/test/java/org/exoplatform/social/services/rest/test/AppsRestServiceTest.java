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
package org.exoplatform.social.services.rest.test;

import java.util.Iterator;
import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.social.services.rest.AbstractResourceTest;
import org.exoplatform.social.services.rest.AppsRestService;
import org.exoplatform.social.services.rest.AppsRestService.App;
import org.exoplatform.social.services.rest.AppsRestService.AppList;

/**
 * AppsRestServiceTest.java
 *
 * @author     <a href="http://hoatle.net">hoatle</a>
 * @since      Mar 2, 2010
 * @copyright  eXo Platform SAS 
 */
public class AppsRestServiceTest extends AbstractResourceTest {
  static private AppsRestService appsRestService;
  static private ApplicationRegistryService applicationRegistryService;
  static private PortalContainer container;
  static private AppList serverAppList;
  
  public void setUp() throws Exception {
    super.setUp();
    
    appsRestService = new AppsRestService();
    registry(appsRestService);
    //TODO hoatle can not get appList to test
//    serverAppList = getAppList();
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    
    unregistry(appsRestService);
  }
  
  public void testJsonShowApps() throws Exception {
/*    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "social/apps/show.json", "", null, null, writer);
    AppList appList = (AppList) response.getEntity();
    assertEquals(200, response.getStatus());
    assertEquals("application/json", response.getContentType().toString());
    assertEquals(serverAppList.getApps().size(), appList.getApps().size());*/
  }
  
  public void testXmlShowApps() throws Exception {
    /*    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "social/apps/show.xml", "", null, null, writer);
    AppList appList = (AppList) response.getEntity();
    assertEquals(200, response.getStatus());
    assertEquals("application/xml", response.getContentType().toString());
    assertEquals(serverAppList.getApps().size(), appList.getApps().size());*/
  }
  
  private AppList getAppList() throws Exception {
    container = PortalContainer.getInstance();
    AppList appList = new AppList();
    applicationRegistryService = (ApplicationRegistryService) container.getComponentInstanceOfType(ApplicationRegistryService.class);
    try {
      List<ApplicationCategory> applicationCategoryList = applicationRegistryService.getApplicationCategories();
      Iterator<ApplicationCategory> applicationCategoryItr = applicationCategoryList.iterator();
      ApplicationCategory applicationCategory;
      while (applicationCategoryItr.hasNext()) {
        applicationCategory = applicationCategoryItr.next();
        ApplicationType<org.exoplatform.portal.pom.spi.portlet.Portlet> portletType = ApplicationType.PORTLET;
        List<Application> applications = applicationRegistryService.getApplications(applicationCategory, portletType);
        Iterator<Application> applicationItr = applications.iterator();
        Application application;
        while (applicationItr.hasNext()) {
          App app = new App();
          application = applicationItr.next();
          app.setAppId(application.getId());
          app.setAppName(application.getDisplayName());
          appList.addApp(app);
        }
      }
    } catch(Exception ex) {
      throw new RuntimeException("can not get appList", ex);
    }
    return appList;
  }
}
