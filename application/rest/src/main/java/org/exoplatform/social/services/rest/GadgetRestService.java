/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.social.services.rest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.ws.frameworks.json.transformer.Bean2JsonOutputTransformer;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Jan 20, 2009          
 */

public class GadgetRestService implements ResourceContainer {

  /**
   * Return request with JSON body which represent application object.
   * @param key the key.
   * @return @see {@link Response} .
   * @throws Exception 
   */
  @HTTPMethod("GET")                                                                                                                                                                                           
  @URITemplate("/private/json/application/")                                                                                                                                                                                   
  @OutputTransformer(Bean2JsonOutputTransformer.class)                                                                                                                                                         
  public Response get() throws Exception{
    List<Model> models_ = new ArrayList<Model>();
    ListModel listModels = new ListModel();
    
    PortalContainer portalContainer = PortalContainer.getInstance();
    ApplicationRegistryService appRegistrySrc = (ApplicationRegistryService)portalContainer.getComponentInstanceOfType(ApplicationRegistryService.class);
    String[] applicationTypes = {org.exoplatform.web.application.Application.EXO_PORTLET_TYPE};
    List<ApplicationCategory> listCategory = appRegistrySrc.getApplicationCategories();
    Iterator<ApplicationCategory> cateItr = listCategory.iterator() ;
    while (cateItr.hasNext()) {
      ApplicationCategory cate = cateItr.next();
      List<Application> applications = appRegistrySrc.getApplications(cate, applicationTypes);
      Iterator<Application> appIterator = applications.iterator() ;
      while (appIterator.hasNext()) {
        Model model = new Model();
        Application app = appIterator.next();
        model.setAppId(app.getApplicationName());
        model.setAppName(app.getDisplayName());
        models_.add(model);
      }
    }
    
    listModels.setApps(models_);
    return Response.Builder.ok(listModels).mediaType("application/json").build();
  }
  
  public class Model {
    private String appId_;
    private String appName_;
    
    public void setAppId(String appId) { appId_ = appId; }
    public String getAppId() { return appId_; }
    
    public void setAppName(String appName) { appName_ = appName; }
    public String getAppName() {return appName_ ;}
  }
  
  public class ListModel {
    private List<Model> apps_;
    
    public void setApps(List<Model> apps) { apps_ = apps; }
    public List<Model> getApps() { return apps_; }
  }
}
