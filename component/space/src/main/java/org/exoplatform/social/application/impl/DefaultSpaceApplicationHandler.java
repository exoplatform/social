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

import static org.exoplatform.portal.pom.config.Utils.split;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
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
  public static final String SPACE_TEMPLATE_PAGE_ID = "portal::classic::spacetemplate";
  private ExoContainer container = ExoContainerContext.getCurrentContainer() ;
  private DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
  private static Map<ApplicationCategory, List<Application>> appStoreCache = null;
  private static List<Application> appCache = new ArrayList<Application>();
  
  /**
   * Inits the app.
   * 
   * @param space the space
   * @param homeNodeApp the home node app
   * @param apps the apps
   * @throws SpaceException the space exception
   * {@inheritDoc}
   */
  public void initApp(Space space, String homeNodeApp, List<String> apps) throws SpaceException {
    try {
      PageNavigation spaceNav = SpaceUtils.createGroupNavigation(space.getGroupId());
      PageNode homeNode = createPageNodeFromApplication(space, homeNodeApp, null, true);
      List<PageNode> childNodes = homeNode.getChildren();
      if(childNodes == null) childNodes = new ArrayList<PageNode>();
      for (String app : apps) {
        app = (app.trim()).split(":")[0];
        PageNode appNode = createPageNodeFromApplication(space, app, null, false);
        childNodes.add(appNode);
      }
      //homeNode.setChildren((ArrayList<PageNode>) childNodes);
      spaceNav.addNode(homeNode);
      dataStorage.save(spaceNav);
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
      // return in case group navigation was removed by portal SOC-548
      if (spaceNav == null) return;
      ArrayList<PageNode> spaceNodes = spaceNav.getNodes();
      for (PageNode spaceNode : spaceNodes) {
        String pageId = spaceNode.getPageReference();
        Page page = dataStorage.getPage(pageId);
        dataStorage.remove(page);
      }
      SpaceUtils.removeGroupNavigation(groupId);
    } catch(Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_DEINIT_APP, e);
    }
  }
  
  
  /**
   * {@inheritDoc}
   */
  public void activateApplication(Space space, String appId, String appName) throws SpaceException {
    activateApplicationClassic(space,appId,appName);
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
  public void removeApplication(Space space, String appId, String appName) throws SpaceException {
    removeApplicationClassic(space, appId, appName);
  }
  
  /**
   * {@inheritDoc}
   */
  public void removeApplications(Space space) throws SpaceException {
//    List<Application> apps;
    try {
//      apps = getSpaceApplications(space);
//      for (Application app : apps) {
//        removeApplication(space, app.getApplicationName());
//      }
      String[] apps = space.getApp().split(",");
      String[] appPart = null;
      for (int i = 0; i < apps.length; i++) {
    	appPart = apps[i].split(":");
    	removeApplication(space, appPart[0], appPart[1]);
      }
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_APPLICATIONS, e);
    }
  }
  
  /**
   * Gets all applications installed in a space
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
   * Activates classic application type in a space
   * 
   * @param space
   * @param appId
   * @throws SpaceException
   */
  private void activateApplicationClassic(Space space, String appId, String appName) throws SpaceException {
    PageNavigation nav = SpaceUtils.createGroupNavigation(space.getGroupId());
    PageNode pageNode = createPageNodeFromApplication(space, appId, appName, false);
    UIPortal uiPortal = Util.getUIPortal();
    PageNode selectedNode = null;
	try {
		selectedNode = uiPortal.getSelectedNode();
	} catch (Exception e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	
	UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
	UserPortalConfig userPortalConfig = uiPortalApp.getUserPortalConfig();
    List<PageNavigation> navigations = userPortalConfig.getNavigations();
    for (PageNavigation navi : navigations) {
  	  if ((navi.getOwner()).equals(nav.getOwner())) {
  		  nav = navi;
  		  break;
  	  }
    }
    
    try {
      PageNode homeNode = SpaceUtils.getHomeNode(nav, space.getUrl());
      if (homeNode == null) {
        throw new Exception("homeNode is null!");
      }
      List<PageNode> childNodes = homeNode.getChildren();
      if(childNodes == null) childNodes = new ArrayList<PageNode>();
      childNodes.add(pageNode);
      dataStorage.save(nav);
      uiPortal.setSelectedNode(selectedNode);
      uiPortal.setSelectedNavigation(nav);
      SpaceUtils.setNavigation(nav);
    } catch (Exception e) {
      try {
        //TODO if navigation can't be updated, remove the page
      } catch (Exception e1) {}
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_APPLICATION, e);
    }
  }

  /**
   * Deactivates an application in a space
   * @param space
   * @param appId
   */
  private void deactivateApplicationClassic(Space space, String appId) {
    
  }
  
  /**
   * Removes an classic-type application from a space
   * @param space
   * @param appId
   * @throws SpaceException
   */
  private void removeApplicationClassic(Space space, String appId, String appName) throws SpaceException {
    try {
      String groupId = space.getGroupId();
      PageNavigation nav = dataStorage.getPageNavigation(PortalConfig.GROUP_TYPE, groupId);
      
      // return in case group navigation was removed by portal SOC-548
      if (nav == null) return;
      PageNode homeNode = SpaceUtils.getHomeNode(nav, space.getUrl());
      if (homeNode == null) {
        throw new Exception("homeNode is null!");
      }
      List<PageNode> childNodes = homeNode.getChildren();
      String nodeName = appName;
//      nodeName = SpaceUtils.getAppNodeName(space, appName);
//      if (nodeName == null) nodeName = SpaceUtils.getAppNodeName(space, appId);
      PageNode childNode = homeNode.getChild(nodeName);
      //bug from portal, gets by nodeUri instead
      if (childNode == null) {
        for (PageNode pageNode : homeNode.getChildren()) {
          String nodeUri = pageNode.getUri();
          nodeUri = nodeUri.substring(nodeUri.indexOf("/") + 1);
          if (nodeUri.equals(nodeName)) {
            childNode = pageNode;
            break;
          }
        }
      }
      
      // In case bug SOC-674
      if (childNode == null) {
    	  nodeName = space.getName() + nodeName;
    	  childNode = homeNode.getChild(nodeName);
      }
      
      childNodes.remove(childNode);
      dataStorage.save(nav);
      SpaceUtils.setNavigation(nav);
      
      // remove page
      String pageId = childNode.getPageReference();
      Page page = dataStorage.getPage(pageId);
      dataStorage.remove(page);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_REMOVE_APPLICATION, e);
    }
    
  }
  
  
  /**
   * Gets an application from a list
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
   * Creates page node from application.
   * - Creates Application instance from appId. <br />
   * - Creates Page instance and set the newly-created application for that page; 
   * adds application to container. <br />
   * - Creates PageNode instance and returns that pageNode.
   * @param space
   * @param appId
   * @param isRoot
   * @return
   * @throws SpaceException
   */
  private PageNode createPageNodeFromApplication(Space space, String appId, String appName, boolean isRoot) throws SpaceException {
	String portalName = Util.getPortalRequestContext().getPortalOwner();
    Application app = getApplication(appCache, appId);
    if (app == null) {
      try {
        if (appStoreCache == null) {
          appStoreCache = SpaceUtils.getAppStore(space);
        }
        app = getApplication(appStoreCache, appId);
        if (app == null) {
          //retry
          appStoreCache = SpaceUtils.getAppStore(space);
          app = getApplication(appStoreCache, appId);
          if (app == null) {
            app = SpaceUtils.getAppFromPortalContainer(appId);
            if (app == null) {
              throw new Exception("app is null!");
            }
          }
        }
        appCache.add(app);
      } catch (Exception e) {
        throw new SpaceException(SpaceException.Code.UNABLE_TO_LIST_AVAILABLE_APPLICATIONS, e);
      }
    }
    String contentId = app.getContentId();
    if(contentId == null) contentId = app.getCategoryName() + "/" + app.getApplicationName();
    String appInstanceId = PortalConfig.GROUP_TYPE + "#" + space.getGroupId() + ":/" + contentId + "/" + app.getApplicationName() + app.hashCode();
    org.exoplatform.portal.config.model.Application<Gadget> gadgetApplication = null;
    org.exoplatform.portal.config.model.Application<Portlet> portletApplication = null;
    if (app.getType() == ApplicationType.GADGET) {
      TransientApplicationState<Gadget> state = new TransientApplicationState<Gadget>(app.getApplicationName());
      gadgetApplication = 
        org.exoplatform.portal.config.model.Application.createGadgetApplication();
      gadgetApplication.setState(state);
      gadgetApplication.setAccessPermissions(new String[]{"*:" + space.getGroupId()});
      gadgetApplication.setShowInfoBar(false);
    } else {
      portletApplication = createPortletApplication(appInstanceId);
      portletApplication.setAccessPermissions(new String[]{"*:" + space.getGroupId()});
      portletApplication.setShowInfoBar(false);
    }

    Page page = null;
    String pageName = null;
    if(isRoot) 
      pageName = space.getUrl();
    else {
      // Check application is installed or not yet
	  if (SpaceUtils.isInstalledApp(space, appId) && (appName != null)) {
		pageName = appName;
	  } else {
        pageName = app.getApplicationName();
	  }
    }
    String newName = space.getUrl();
    try {
      if (isRoot != true) {
        newName = pageName;
      }
      //String clonedName = SpaceUtils.cleanString(space.getName()) + "_" + newName.trim();
      page = dataStorage.clonePage(SPACE_TEMPLATE_PAGE_ID, PortalConfig.GROUP_TYPE, space.getGroupId(), newName.trim());
    } catch (Exception e) {
      //e.printStackTrace();
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREATE_PAGE, e);
    }
    setPage(space, app, gadgetApplication, portletApplication, page, newName);	
    try {
      try {
    	dataStorage.save(page);
      } catch (Exception e) {
    	Page clonedPage = dataStorage.getPage(PortalConfig.PORTAL_TYPE + "::" + portalName + "::" + pageName);
        dataStorage.remove(clonedPage);
        newName = space.getName() + newName;
        pageName = space.getName() + pageName;
        page = dataStorage.clonePage(SPACE_TEMPLATE_PAGE_ID, PortalConfig.GROUP_TYPE, space.getGroupId(), newName.trim());
        setPage(space, app, gadgetApplication, portletApplication, page, newName);
        dataStorage.save(page);
	  }
      //work around for BUG from GTNPORTAL for DataStorage.clonePage();
      Page clonedPage = dataStorage.getPage(PortalConfig.PORTAL_TYPE + "::" + portalName + "::" + pageName);
      dataStorage.remove(clonedPage);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_CREATE_PAGE,e);
    }
    
    PageNode pageNode = new PageNode();
    
    String label = app.getDisplayName();
    if (appName != null) {
    	String sufixLabel = pageName.replaceAll(app.getApplicationName(), "");
    	label = label + sufixLabel;
    }
    
    if(isRoot) {
      label = space.getName();
      pageNode.setUri(pageName);
    } else {
      pageNode.setUri(space.getUrl() + "/" + pageName);
    }
    pageNode.setName(pageName);
    pageNode.setLabel(label);
    pageNode.setPageReference(page.getPageId());
    pageNode.setModifiable(true);
    return pageNode;
  }

private void setPage(Space space, Application app, org.exoplatform.portal.config.model.Application<Gadget> gadgetApplication, 
		org.exoplatform.portal.config.model.Application<Portlet> portletApplication, 
		Page page, String newName) {
	page.setOwnerType(PortalConfig.GROUP_TYPE);
    page.setOwnerId(space.getGroupId());
    page.setTitle(space.getName() + " - " + newName);
    String visibility = space.getVisibility();
    if(visibility.equals(Space.PUBLIC)) {
      page.setAccessPermissions(new String[]{UserACL.EVERYONE});
    } else {
      page.setAccessPermissions(new String[]{"*:" + space.getGroupId()});
    }
    page.setEditPermission("manager:" + space.getGroupId());
    page.setModifiable(true);
    
    ArrayList<ModelObject> pageChilds = page.getChildren();
    Container container = findContainerById(pageChilds, APPLICATION);
    ArrayList<ModelObject> childs = container.getChildren();
    if(app.getType() == ApplicationType.GADGET) childs.add(gadgetApplication);
    else childs.add(portletApplication);
    container.setChildren(childs);
    pageChilds = setContainerById(pageChilds, container);
    page.setChildren(pageChilds);
    setPermissionForPage(page.getChildren(), "*:" + space.getGroupId());
}
  
  /**
   * Gets application from appStore by appId
   * @param appStore
   * @param appId
   * @return app or null
   */
  private Application getApplication(Map<ApplicationCategory, List<Application>> appStore,
                                     String appId) {
    Iterator<ApplicationCategory> categoryItr = appStore.keySet().iterator();
    while (categoryItr.hasNext()) {
      ApplicationCategory category = categoryItr.next();
      List<Application> appList = category.getApplications();
      Iterator<Application> appListItr = appList.iterator();
      while(appListItr.hasNext()) {
        Application app = appListItr.next();
        if (app.getApplicationName().equals(appId)) return app;
      }
    }
    return null;
  }

  /**
   * Finds container by id
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
   * Sets permission for page
   * @param childs
   * @param id
   * @return
   */
  private void setPermissionForPage(ArrayList<ModelObject> childrens, String perm) {
	  for (ModelObject modelObject : childrens) {
		if(modelObject instanceof org.exoplatform.portal.config.model.Application<?>) {
		  ((org.exoplatform.portal.config.model.Application) modelObject)
		.setAccessPermissions(new String[] {perm});
		}
		else if (modelObject instanceof Container) {
			((Container) modelObject).setAccessPermissions(new String[] {perm});
			setPermissionForPage(((Container) modelObject).getChildren(), perm);
		}
	}
  }
  
  /**
   * Sets container by Id
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
  
  /**
   * Creates portlet application from instanceId
   * @param instanceId
   * @return
   */
  private org.exoplatform.portal.config.model.Application<Portlet> createPortletApplication(String instanceId)
  {
     int i0 = instanceId.indexOf("#");
     int i1 = instanceId.indexOf(":/", i0 + 1);
     String ownerType = instanceId.substring(0, i0);
     String ownerId = instanceId.substring(i0 + 1, i1);
     String persistenceid = instanceId.substring(i1 + 2);
     String[] persistenceChunks = split("/", persistenceid);
     TransientApplicationState<Portlet> state = new TransientApplicationState<Portlet>(
        persistenceChunks[0] + "/" + persistenceChunks[1],
        null,
        ownerType,
        ownerId,
        persistenceChunks[2]);
     org.exoplatform.portal.config.model.Application<Portlet> portletApp = org.exoplatform.portal.config.model.Application.createPortletApplication();
     portletApp.setState(state);
     return portletApp;
  }
  
}