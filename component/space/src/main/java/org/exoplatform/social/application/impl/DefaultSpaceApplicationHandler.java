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
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIControlWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.social.application.SpaceApplicationHandler;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.social.space.SpaceException;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Oct 17, 2008          
 */

public  class DefaultSpaceApplicationHandler implements SpaceApplicationHandler {
  public static final String NAME = "classic";
  private ExoContainer container = ExoContainerContext.getCurrentContainer() ;
  private UserPortalConfigService configService = (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
  
  public void activateApplication(Space space, String appId) throws SpaceException {
    activeApplicationClassic(space,appId);
  }

  public void deactiveApplication(Space space, String appId) throws SpaceException{
    deactiveApplicationClassic(space,appId);
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
    List<Application> apps;
    try {
      apps = SpaceUtils.getAllApplications(space.getId());
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_LIST_AVAILABLE_APPLICATIONS, e);
    }
    String spaceNav = space.getGroupId().substring(1);
    
    // create new page to group space
    Page page = new Page();
    page.setOwnerType(PortalConfig.GROUP_TYPE);
    page.setOwnerId(spaceNav);
    page.setName(appId);
    page.setAccessPermissions(new String[]{"*:" + space.getGroupId()});
    page.setEditPermission("manager:" + space.getGroupId());
    page.setModifiable(true);

    // mapping application registry -> application model for adding to page model, set child
    Application app = getApplication(apps, appId);
    org.exoplatform.portal.config.model.Application child = new org.exoplatform.portal.config.model.Application();
    StringBuilder windowId = new StringBuilder();
    windowId.append(PortalConfig.PORTAL_TYPE);
    windowId.append("#classic:/");
    windowId.append(app.getApplicationGroup() + "/" + app.getApplicationName()).append("/");
    windowId.append(app.hashCode());
    child.setInstanceId(windowId.toString());
    child.setTitle(app.getDisplayName());
    ArrayList<Object> applications = new ArrayList<Object>();
    applications.add(child);
    page.setChildren(applications);
    // end set child

    try {
      configService.create(page);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_APPLICATION, e);
    }

    PageNavigation nav;
    try {
      // create new pageNode
      PageNode pageNode = new PageNode();
      pageNode.setUri(app.getApplicationName());
      pageNode.setName(app.getApplicationName());
      pageNode.setLabel(app.getDisplayName());
      pageNode.setPageReference(page.getPageId());
      // get space navigation
      nav = configService.getPageNavigation(PortalConfig.GROUP_TYPE, spaceNav);
      PageNode homeNode = nav.getNode(space.getCleanedName());
      List<PageNode> childNodes = homeNode.getChildren();
      if(childNodes == null) childNodes = new ArrayList<PageNode>();
      childNodes.add(pageNode);
      homeNode.setChildren((ArrayList<PageNode>) childNodes);
      configService.update(nav);


    } catch (Exception e) {
      //if we can't update the navigation, we remove the page
      try {
        configService.remove(page);
      } catch (Exception e1) {}
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_APPLICATION, e);
    }

    // refresh portal
    updateNavigationPortlet(nav);
  }

  private void updateNavigationPortlet(PageNavigation nav){
    // set uiportal navigation
    UIPortal uiPortal = Util.getUIPortal();
    SpaceUtils.setNavigation(uiPortal.getNavigations(), nav) ;

    PortalRequestContext pcontext = Util.getPortalRequestContext();
    UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
    UIControlWorkspace uiControl = uiPortalApp.getChildById(UIPortalApplication.UI_CONTROL_WS_ID);
    if(uiControl != null) pcontext.addUIComponentToUpdateByAjax(uiControl);
  }

  
  private void deactiveApplicationClassic(Space space, String appId) {
    
  }

  
  private void removeApplicationClassic(Space space, String appId) throws SpaceException {
    try {
      // remove pagenode
      String spaceNav = space.getGroupId().substring(1);
      PageNavigation nav = configService.getPageNavigation(PortalConfig.GROUP_TYPE, spaceNav);
      PageNode homeNode = nav.getNode(space.getCleanedName());
      List<PageNode> childNodes = homeNode.getChildren();
      childNodes.remove(homeNode.getChild(appId));
      homeNode.setChildren((ArrayList<PageNode>) childNodes);
      configService.update(nav);
      // remove page
      Page page = configService.getPage(PortalConfig.GROUP_TYPE + "::" + spaceNav + "::" + appId);
      configService.remove(page);
      // set uiportal navigation
      UIPortal uiPortal = Util.getUIPortal();
      SpaceUtils.setNavigation(uiPortal.getNavigations(), nav) ;
      // refresh portal
      PortalRequestContext pcontext = Util.getPortalRequestContext();
      UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
      UIControlWorkspace uiControl = uiPortalApp.getChildById(UIPortalApplication.UI_CONTROL_WS_ID);
      if(uiControl != null) pcontext.addUIComponentToUpdateByAjax(uiControl);
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
}