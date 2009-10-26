/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.application.impl;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.config.model.portlet.PortletApplication;
import org.exoplatform.portal.config.model.portlet.PortletId;
import org.exoplatform.portal.webui.application.PortletState;
import org.exoplatform.social.application.SpaceApplicationHandler;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceUtils;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Oct 17, 2008          
 */

public  class DefaultSpaceApplicationHandler implements SpaceApplicationHandler {
  public static final String NAME = "classic";
  public static final String APPLICATION = "Application";
  private ExoContainer container = ExoContainerContext.getCurrentContainer() ;
  private UserPortalConfigService configService = (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
  
  /**
   * {@inheritDoc}
   */
  public void initApp(Space space, String homeNodeApp, String[] apps) throws SpaceException {
    try {
      PageNavigation spaceNav = SpaceUtils.createGroupNavigation(space.getGroupId());
      PageNode homeNode = createPageNodeFromApplication(space, homeNodeApp, true);
      for (String app : apps) {
        app = app.trim();
        PageNode appNode = createPageNodeFromApplication(space, app, false);
        List<PageNode> childNodes = homeNode.getChildren();
        if(childNodes == null) childNodes = new ArrayList<PageNode>();
        childNodes.add(appNode);
        homeNode.setChildren((ArrayList<PageNode>) childNodes);
      }
      spaceNav.addNode(homeNode);
      configService.update(spaceNav);
      SpaceUtils.setNavigation(spaceNav);
    } catch(Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_INIT_APP, e);
    }
  }
  
  /**
   * De-initialize HomeSpacePortlet application.
   * @param space
   */
  public void deInitApp(Space space) throws SpaceException {
    try {
      String groupId = space.getGroupId();
      PageNavigation spaceNav = SpaceUtils.getGroupNavigation(groupId);
      ArrayList<PageNode> spaceNodes = spaceNav.getNodes();
      for (PageNode spaceNode : spaceNodes) {
        String pageId = spaceNode.getPageReference();
        Page page = configService.getPage(pageId);
        configService.remove(page);
      }
      SpaceUtils.removeGroupNavigation(groupId);
    } catch(Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_DEINIT_APP, e);
    }
  }
  
  
  /**
   * {@inheritDoc}
   */
  public void activateApplication(Space space, String appId) throws SpaceException {
    activeApplicationClassic(space,appId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void deactiveApplication(Space space, String appId) throws SpaceException{
    deactivateApplicationClassic(space,appId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void installApplication(Space space, String appId) throws SpaceException {

  }
  
  /**
   * {@inheritDoc}
   */
  public void removeApplication(Space space, String appId) throws SpaceException {
    removeApplicationClassic(space, appId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void removeApplications(Space space) throws SpaceException {
    List<Application> apps;
    try {
      apps = getSpaceApplications(space);
      for (Application app : apps) {
        removeApplication(space, app.getApplicationName());
      }
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_APPLICATIONS, e);
    }
  }
  
  /**
   * Get all applications installed in a space
   * @param space
   * @return
   * @throws Exception
   */
  private List<Application> getSpaceApplications(Space space) throws Exception {
    List<Application> allApps = SpaceUtils.getApplications(space.getGroupId());
    List<Application> appList = new ArrayList<Application>();
    String spaceApps = space.getApp();
    if(spaceApps != null) {
      String[] apps = spaceApps.split(",");
      for(String obj : apps) {
        String appId = obj.split(":")[0];
        for(Application app : allApps) {
          if(app.getApplicationName().equals(appId)) {
            appList.add(app);
            break;
          }
        }
      }
    }
    return appList;
  }
  
  /**
   * {@inheritDoc}
   */
  public String getName() {
    return NAME;
  }
  
  /**
   * Activate classic application type in a space
   * @param space
   * @param appId
   * @throws SpaceException
   */
  private void activeApplicationClassic(Space space, String appId) throws SpaceException {
    PageNavigation nav = SpaceUtils.createGroupNavigation(space.getGroupId());
    PageNode pageNode = createPageNodeFromApplication(space, appId, false);
    try {
      PageNode homeNode = nav.getNode(space.getUrl());
      List<PageNode> childNodes = homeNode.getChildren();
      if(childNodes == null) childNodes = new ArrayList<PageNode>();
      childNodes.add(pageNode);
      homeNode.setChildren((ArrayList<PageNode>) childNodes);
      configService.update(nav);
      SpaceUtils.setNavigation(nav);
    } catch (Exception e) {
      try {
        //TODO if we can't update the navigation, we remove the page
      } catch (Exception e1) {}
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_APPLICATION, e);
    }
  }

  /**
   * Deactivate an application in a space
   * @param space
   * @param appId
   */
  private void deactivateApplicationClassic(Space space, String appId) {
    
  }
  
  /**
   * Remove an classic-type application from a space
   * @param space
   * @param appId
   * @throws SpaceException
   */
  private void removeApplicationClassic(Space space, String appId) throws SpaceException {
    try {
      String spaceNav = space.getGroupId();
      PageNavigation nav = configService.getPageNavigation(PortalConfig.GROUP_TYPE, spaceNav);
      PageNode homeNode = nav.getNode(space.getShortName());
      List<PageNode> childNodes = homeNode.getChildren();
      childNodes.remove(homeNode.getChild(appId));
      homeNode.setChildren((ArrayList<PageNode>) childNodes);
      
      configService.update(nav);
      
      // remove page
      Page page = configService.getPage(PortalConfig.GROUP_TYPE + "::" + spaceNav + "::" + appId);
      configService.remove(page);
      
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_APPLICATION, e);
    }
    
  }
  
  
  /**
   * Get an application from a list
   * @param apps
   * @param appId
   * @return
   */
  private Application getApplication(List<Application> apps, String appId) {
    for(Application app : apps) {
      if(app.getApplicationName().equals(appId)) return app;
    }
    return null;
  }
  
  /**
   * Create page node from application
   * @param space
   * @param appId
   * @param isRoot
   * @return
   * @throws SpaceException
   */
  @SuppressWarnings("unchecked")
  private PageNode createPageNodeFromApplication(Space space, String appId, boolean isRoot) throws SpaceException {
    //create application
    List<Application> apps;
    try {
      apps = SpaceUtils.getApplications(space.getGroupId());
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_LIST_AVAILABLE_APPLICATIONS, e);
    }
    Application app = getApplication(apps, appId);
    
    PortletApplication child = new PortletApplication(app.getApplicationGroup(), app.getApplicationName());
    child.setShowInfoBar(false);
    
    // create state
    org.exoplatform.portal.config.model.TransientApplicationState<?> applicationState = new TransientApplicationState();
    applicationState.setOwnerType(PortalConfig.GROUP_TYPE);
    applicationState.setOwnerId(space.getGroupId());
    Object applicationId = new PortletId(app.getApplicationGroup(), app.getApplicationName());
    PortletState portletState = new PortletState(applicationState, ApplicationType.PORTLET, applicationId);
    child.setState(portletState.getApplicationState());

    //create new Page
    Page page = null;
    try {
      page = configService.createPageTemplate("space", PortalConfig.GROUP_TYPE, space.getGroupId());
    } catch (Exception e) {
      e.printStackTrace();
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREATE_PAGE,e);
    }
    String pageName;
    if(isRoot) 
      pageName = space.getUrl();
    else pageName = app.getApplicationName();
    
    String visibility = space.getVisibility();
    if(visibility.equals(Space.PUBLIC)) {
      page.setAccessPermissions(new String[]{UserACL.EVERYONE});
    } else {
      page.setAccessPermissions(new String[]{"*:" + space.getGroupId()});
    }
    
    page.setName(pageName);
    page.setEditPermission("manager:" + space.getGroupId());
    page.setModifiable(true);
    
    
    //add application to container
    ArrayList<ModelObject> pageChilds = page.getChildren();
    Container container = findContainerById(pageChilds, APPLICATION);
    ArrayList<ModelObject> childs = container.getChildren();
    childs.add(child);
    container.setChildren(childs);
    pageChilds = setContainerById(pageChilds, container);
    page.setChildren(pageChilds);
    
    try {
      configService.create(page);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREATE_PAGE,e);
    }
    
    // create new PageNode
    PageNode pageNode = new PageNode();
    String label = app.getDisplayName();
    if(isRoot) {
      label = pageName;
      pageNode.setUri(pageName);
    } else {
      pageNode.setUri(space.getUrl() + "/" + pageName);
    }
    pageNode.setName(pageName);
    pageNode.setLabel(label);
    pageNode.setPageReference(page.getPageId());
    return pageNode;
  }
  
  /**
   * Find container by Id
   * @param childs
   * @param id
   * @return
   */
  private Container findContainerById(ArrayList<ModelObject> childs, String id) {
    Container found = null;
    for(Object obj : childs) {
      if (org.exoplatform.portal.config.model.Application.class.isInstance(obj)) continue;
      Container child = (Container)obj;
      if(child.getId() == null) {
        found = findContainerById(child.getChildren(), id);
        if (found != null) return found;
      } else {
        if(child.getId().equals(id)) return child;
        else found = findContainerById(child.getChildren(), id);
        if(found != null) return found;
      }
    }
    return found;
  }
  
  /**
   * Set container by Id
   * @param childs
   * @param container
   * @return
   */
  private ArrayList<ModelObject> setContainerById(ArrayList<ModelObject> childs, Container container) {
    ArrayList<ModelObject> result = childs;
    int index = result.indexOf(container);
    if(index != -1) result.set(index, container);
    else {
      for(int i=0; i<result.size(); i++) {
        ModelObject obj = result.get(i);
        if (org.exoplatform.portal.config.model.Application.class.isInstance(obj)) continue;
        Container objContainer = (Container)obj;
        ArrayList<ModelObject> tmp = setContainerById(objContainer.getChildren(), container);
        objContainer.setChildren(tmp);
        result.set(i, objContainer);
      }
    }
    return result;
  }
  
}