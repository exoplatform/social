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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;


/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Jan 20, 2009          
 */
@Path("/social/")
public class GadgetRestService implements ResourceContainer {

  /**
   * Return request with JSON body which represent application object.<br>
   * 
   * @return Registried applications list.
   * @throws Exception When getApplicationCategories() method throw an Exception.
   */
  @GET
  @Path("/json/application/")
  @Produces({MediaType.APPLICATION_JSON})
  public ListModel get() throws Exception{
    List<Model> models_ = new ArrayList<Model>();
    ListModel listModels = new ListModel();
    
    PortalContainer portalContainer = PortalContainer.getInstance();
    ApplicationRegistryService appRegistrySrc = (ApplicationRegistryService)portalContainer.getComponentInstanceOfType(ApplicationRegistryService.class);
    String[] applicationTypes = {org.exoplatform.web.application.Application.EXO_PORTLET_TYPE};
    List<ApplicationCategory> listCategory = appRegistrySrc.getApplicationCategories();
    Iterator<ApplicationCategory> cateItr = listCategory.iterator();

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
    return listModels;
  }
  
  /**
   * Return request with JSON body contains space information.<br>
   * 
   * @return List of user's space.
   * @throws Exception When getAllSpaces() method throw an Exception.
   */
  @GET
  @Path("/space/getMySpace/{userId}/")
  @Produces({MediaType.APPLICATION_JSON})
  public ListSpace getMySpace(@PathParam("userId") String userId) throws Exception {
    ListSpace listSpaces = new ListSpace();
    PortalContainer portalContainer = PortalContainer.getInstance();
    SpaceService spaceSrc = (SpaceService)portalContainer.getComponentInstanceOfType(SpaceService.class);
    List<Space> mySpace = spaceSrc.getSpaces(userId);
    
    listSpaces.setSpaces(mySpace);
    
    return listSpaces;
  }
  
  /**
   * Return request with JSON body contains pending space information.<br>
   * 
   * @return List of user's pending space.
   * @throws Exception When getPendingSpaces() method throw an Exception.
   */
  @GET
  @Path("/space/getPendingSpace/{userId}/")
  @Produces({MediaType.APPLICATION_JSON})
  public ListSpace getPendingSpace(@PathParam("userId") String userId) throws Exception {
    ListSpace listSpaces = new ListSpace();
    PortalContainer portalContainer = PortalContainer.getInstance();
    SpaceService spaceSrc = (SpaceService)portalContainer.getComponentInstanceOfType(SpaceService.class);
    List<Space> pendingSpaces = spaceSrc.getPendingSpaces(userId);
    
    listSpaces.setSpaces(pendingSpaces);
    
    return listSpaces;
  }
  
  /**
   * Describe an Application entity from application registry service of portal.<br>
   * We have to need it for model of converter from rest service.
   */
  public class Model {
    /** Application Id. */
    private String appId_;
    
    /** Application name. */
    private String appName_;
    
    public void setAppId(String appId) { appId_ = appId; }
    public String getAppId() { return appId_; }
    
    public void setAppName(String appName) { appName_ = appName; }
    public String getAppName() {return appName_ ;}
  }
  
  /**
   * List that contains applications from application registry service of portal<br>
   * Need this class for converter from rest service.
   */
  public class ListModel {
    /** Application list variable */
    private List<Model> apps_;
    
    public void setApps(List<Model> apps) { apps_ = apps; }
    public List<Model> getApps() { return apps_; }
  }
  
  /**
   * List that contains space from space service.<br>
   * Need this class for converter from rest service.
   */
  public class ListSpace {
    private List<Space> spaces_;
    
    public void setSpaces(List<Space> spaces) { spaces_ = spaces; }
    public List<Space> getSpaces() { return spaces_; }
  }
}
