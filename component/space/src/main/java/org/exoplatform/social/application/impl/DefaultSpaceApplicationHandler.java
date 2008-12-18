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
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
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
  public static final String HOME_APPLICATION = "HomeSpacePortlet";
  private ExoContainer container = ExoContainerContext.getCurrentContainer() ;
  private UserPortalConfigService configService = (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
  
  public void activateApplication(Space space, String appId) throws SpaceException {
    activeApplicationClassic(space,appId);
  }

  public void deactiveApplication(Space space, String appId) throws SpaceException{
    deactiveApplicationClassic(space,appId);
  }

  public void initSpace(Space space) throws SpaceException {
    try {
      ExoContainer eXoContainer = ExoContainerContext.getCurrentContainer();
      UserPortalConfigService dataService = (UserPortalConfigService) eXoContainer.getComponentInstanceOfType(UserPortalConfigService.class);
      
      String groupId = space.getGroupId().substring(1);
      
      // create new space navigation
      PageNavigation spaceNav = new PageNavigation();
      spaceNav.setOwnerType(PortalConfig.GROUP_TYPE);
      spaceNav.setOwnerId(groupId);
      spaceNav.setModifiable(true);
      dataService.create(spaceNav);
      UIPortal uiPortal = Util.getUIPortal();
      List<PageNavigation> pnavigations = uiPortal.getNavigations();
      SpaceUtils.setNavigation(spaceNav);
      pnavigations.add(spaceNav) ;
      
      // default application
      PageNode pageNode = createPageNodeFromApplication(space, HOME_APPLICATION);
      
      spaceNav.addNode(pageNode) ;
      
      dataService.update(spaceNav) ;
      SpaceUtils.setNavigation(spaceNav) ;

    } catch (Exception e) {
      //TODO:should rollback what has to be rollback here
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREAT_NAV, e);
    }
  }

  public void installApplication(Space space, String appId) throws SpaceException {

  }
  
  public void removeApplication(Space space, String appId) throws SpaceException {
    removeApplicationClassic(space, appId);
  }

  public String getName() {
    return NAME;
  }
  
  private void activeApplicationClassic(Space space, String appId) throws SpaceException {
    String spaceNav = space.getGroupId().substring(1);
    PageNode pageNode = createPageNodeFromApplication(space, appId);
    
    PageNavigation nav;
    try {
      nav = configService.getPageNavigation(PortalConfig.GROUP_TYPE, spaceNav);
      PageNode homeNode = nav.getNode(space.getShortName());
      List<PageNode> childNodes = homeNode.getChildren();
      if(childNodes == null) childNodes = new ArrayList<PageNode>();
      childNodes.add(pageNode);
      homeNode.setChildren((ArrayList<PageNode>) childNodes);
      configService.update(nav);
    } catch (Exception e) {
      try {
        //TODO if we can't update the navigation, we remove the page
      } catch (Exception e1) {}
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_APPLICATION, e);
    }

    // refresh portal
    SpaceUtils.setNavigation(nav);
    //SpaceUtils.reloadPortal();
  }

  
  private void deactiveApplicationClassic(Space space, String appId) {
    
  }

  private void removeApplicationClassic(Space space, String appId) throws SpaceException {
    try {
      // remove pagenode
      String spaceNav = space.getGroupId().substring(1);
      
      PageNavigation nav = configService.getPageNavigation(PortalConfig.GROUP_TYPE, spaceNav);
      PageNode homeNode = nav.getNode(space.getShortName());
      List<PageNode> childNodes = homeNode.getChildren();
      childNodes.remove(homeNode.getChild(appId));
      homeNode.setChildren((ArrayList<PageNode>) childNodes);
      
      configService.update(nav);
      
      // remove page
      Page page = configService.getPage(PortalConfig.GROUP_TYPE + "::" + spaceNav + "::" + appId);
      configService.remove(page);
      
      SpaceUtils.setNavigation(nav) ;
      //SpaceUtils.reloadPortal();
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_APPLICATION, e);
    }
    
  }
  
  private Application getApplication(List<Application> apps, String appId) {
    for(Application app : apps) {
      if(app.getApplicationName().equals(appId)) return app;
    }
    return null;
  }
  
  private PageNode createPageNodeFromApplication(Space space, String appId) throws SpaceException {
    // create application
    List<Application> apps;
    try {
      apps = SpaceUtils.getAllApplications(space.getId());
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_LIST_AVAILABLE_APPLICATIONS, e);
    }
    
    Application app = getApplication(apps, appId);
    org.exoplatform.portal.config.model.Application child = new org.exoplatform.portal.config.model.Application();
    StringBuilder windowId = new StringBuilder();
    windowId.append(PortalConfig.PORTAL_TYPE);
    windowId.append("#classic:/");
    windowId.append(app.getApplicationGroup() + "/" + app.getApplicationName()).append("/");
    windowId.append(app.hashCode());
    child.setInstanceId(windowId.toString());
    child.setTitle(app.getDisplayName());
    child.setShowInfoBar(false);
    
    // create new Page
    Page page = new Page();
    try {
      page = configService.createPageTemplate("space", PortalConfig.GROUP_TYPE, space.getShortName());
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREATE_PAGE,e);
    }
    
    String pageName;
    if(appId.equals(HOME_APPLICATION)) 
      pageName = space.getShortName();
    else pageName = app.getApplicationName();

    page.setName(pageName);
    page.setAccessPermissions(new String[]{"*:" + space.getGroupId()});
    page.setEditPermission("manager:" + space.getGroupId());
    page.setModifiable(true);
    
    
    //add application to container
    ArrayList<Object> pageChilds = page.getChildren();
    Container container = findContainerById(pageChilds, APPLICATION);
    ArrayList<Object> childs = container.getChildren();
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
    String label = app.getDisplayName();
    if(appId.equals(HOME_APPLICATION)) label = pageName;
    PageNode pageNode = new PageNode();
    pageNode.setUri(pageName);
    pageNode.setName(pageName);
    pageNode.setLabel(label);
    pageNode.setPageReference(page.getPageId());
    return pageNode;
  }
  
  private Container findContainerById(ArrayList<Object> childs, String id) {
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
  
  private ArrayList<Object> setContainerById(ArrayList<Object> childs, Container container) {
    ArrayList<Object> result = childs;
    int index = result.indexOf(container);
    if(index != -1) result.set(index, container);
    else {
      for(int i=0; i<result.size(); i++) {
        Object obj = result.get(i);
        if (org.exoplatform.portal.config.model.Application.class.isInstance(obj)) continue;
        Container objContainer = (Container)obj;
        ArrayList<Object> tmp = setContainerById(objContainer.getChildren(), container);
        objContainer.setChildren(tmp);
        result.set(i, objContainer);
      }
    }
    return result;
  }
  
}