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
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Oct 17, 2008          
 */

public  class SpaceApplicationHandlerImpl implements SpaceApplicationHandler {
 
  private ExoContainer container = ExoContainerContext.getCurrentContainer() ;
  private UserPortalConfigService configService = (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
  private SpaceService spaceService = (SpaceService)container.getComponentInstanceOfType(SpaceService.class);
  
  public void activeApplication(String spaceId, String appId) throws Exception {
    Space space = spaceService.getSpace(spaceId);
    space = setApp(space, appId, Space.ACTIVE_STATUS);
    spaceService.saveSpace(space, false);
    if(space.getType().equals(Space.CLASSIC)) activeApplicationClassic(space,appId);
    else activeApplicationWebOS(space,appId);
  }

  public void deactiveApplication(String spaceId, String appId) throws Exception{
    Space space = spaceService.getSpace(spaceId);
    space = setApp(space, appId, Space.DEACTIVE_STATUS);
    spaceService.saveSpace(space, false);
    if(space.getType().equals(Space.CLASSIC)) deactiveApplicationClassic(space,appId);
    else deactiveApplicationWebOS(space,appId);
  }

  public void installApplication(String spaceId, String appId) throws Exception {
    Space space = spaceService.getSpace(spaceId);
    space = setApp(space, appId, Space.INSTALL_STATUS);
    spaceService.saveSpace(space, false);
  }
  
  public void removeApplication(String spaceId, String appId) throws Exception {
    Space space = spaceService.getSpace(spaceId);
    String apps = space.getApp();
    String oldStatus = apps.substring(apps.indexOf(appId));
    if(oldStatus.indexOf(",") != -1) oldStatus = oldStatus.substring(0,oldStatus.indexOf(","));
    apps = apps.replaceFirst(oldStatus, "");
    if(apps.equals("")) apps = null;
    space.setApp(apps);
    spaceService.saveSpace(space, false);
    if(space.getType().equals(Space.CLASSIC)) removeApplicationClassic(space, appId);
    else removeApplicationWebOS(space, appId);
  }
  
  private Space setApp(Space space, String appId, String status) {
    String apps = space.getApp();
    if(apps == null) apps = appId + ":" + status;
    else {
      if(status.equals(Space.INSTALL_STATUS)) apps = apps + "," + appId + ":" + status;
      else {
        String oldStatus = apps.substring(apps.indexOf(appId));
        if(oldStatus.indexOf(",") != -1) oldStatus = oldStatus.substring(0, oldStatus.indexOf(",")-1);
        apps = apps.replaceFirst(oldStatus, appId + ":" + status);
      }
    }
    space.setApp(apps);
    return space;
  }
  
  private void activeApplicationClassic(Space space, String appId) throws Exception {
    List<Application> apps = SpaceUtils.getAllApplications();
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
    windowId.append('#').append(Space.CLASSIC).append(":/");
    windowId.append(app.getApplicationGroup() + "/" + app.getApplicationName()).append("/");
    windowId.append(app.hashCode());
    child.setInstanceId(windowId.toString());
    child.setTitle(app.getDisplayName());
    ArrayList<Object> applications = new ArrayList<Object>();
    applications.add(child);
    page.setChildren(applications);
    // end set child
    configService.create(page);
    
    // create new pageNode
    PageNode pageNode = new PageNode();
    pageNode.setUri(app.getApplicationName());
    pageNode.setName(app.getApplicationName());
    pageNode.setLabel(app.getDisplayName());
    pageNode.setPageReference(page.getPageId());
    // get space navigation
    PageNavigation nav = configService.getPageNavigation(PortalConfig.GROUP_TYPE, spaceNav);
    PageNode homeNode = nav.getNode(space.getName());
    List<PageNode> childNodes = homeNode.getChildren();
    if(childNodes == null) childNodes = new ArrayList<PageNode>();
    childNodes.add(pageNode);
    homeNode.setChildren((ArrayList<PageNode>) childNodes);
    configService.update(nav);
    
    // set uiportal navigation
    UIPortal uiPortal = Util.getUIPortal();
    setNavigation(uiPortal.getNavigations(), nav) ;
    // refresh portal
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
    UIControlWorkspace uiControl = uiPortalApp.getChildById(UIPortalApplication.UI_CONTROL_WS_ID);
    if(uiControl != null) pcontext.addUIComponentToUpdateByAjax(uiControl);
  }
  
  private void activeApplicationWebOS(Space space, String appId) {
    
  }
  
  private void deactiveApplicationClassic(Space space, String appId) {
    
  }
  
  private void deactiveApplicationWebOS(Space space, String appId) {
    
  }
  
  private void removeApplicationClassic(Space space, String appId) throws Exception {
    // remove pagenode
    String spaceNav = space.getGroupId().substring(1);
    PageNavigation nav = configService.getPageNavigation(PortalConfig.GROUP_TYPE, spaceNav);
    PageNode homeNode = nav.getNode(space.getName());
    List<PageNode> childNodes = homeNode.getChildren();
    childNodes.remove(homeNode.getChild(appId));
    homeNode.setChildren((ArrayList<PageNode>) childNodes);
    configService.update(nav);
    // remove page
    Page page = configService.getPage(PortalConfig.GROUP_TYPE + "::" + spaceNav + "::" + appId);
    configService.remove(page);
    // set uiportal navigation
    UIPortal uiPortal = Util.getUIPortal();
    setNavigation(uiPortal.getNavigations(), nav) ;
    // refresh portal
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
    UIControlWorkspace uiControl = uiPortalApp.getChildById(UIPortalApplication.UI_CONTROL_WS_ID);
    if(uiControl != null) pcontext.addUIComponentToUpdateByAjax(uiControl);
    
  }
  
  private void removeApplicationWebOS(Space space, String appId) {
    
  }
  
  private Application getApplication(List<Application> apps, String appId) {
    for(Application app : apps) {
      if(app.getApplicationName().equals(appId)) return app;
    }
    return null;
  }
  
  private static void setNavigation(List<PageNavigation> navs, PageNavigation nav) {
    for(int i = 0; i < navs.size(); i++) {
      if(navs.get(i).getId() == nav.getId()) {
        navs.set(i, nav);
        return;
      }
    }
  }
}