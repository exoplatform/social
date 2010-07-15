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
package org.exoplatform.social.core.space.impl;

import org.exoplatform.portal.pom.config.Utils;

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
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Dashboard;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceApplicationHandler;

/**
 * Created by The eXo Platform SARL Author : dang.tung tungcnw@gmail.com Oct 17,
 * 2008
 */

public class DefaultSpaceApplicationHandler implements SpaceApplicationHandler {
  private static final Log                                          LOG                    = ExoLogger.getLogger(DefaultSpaceApplicationHandler.class);

  public static final String                                 NAME                   = "classic";

  public static final String                                 SPACE_TEMPLATE_PAGE_ID = "portal::classic::spacetemplate";

  static public final String                                 APPLICATION_CONTAINER  = "Application";

  private ExoContainer                                       container              = ExoContainerContext.getCurrentContainer();

  private DataStorage                                        dataStorage            = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);

  private static Map<ApplicationCategory, List<Application>> appStoreCache          = null;

  private static List<Application>                           appCache               = new ArrayList<Application>();

  /**
   * Inits the app.
   *
   * @param space the space
   * @param homeNodeApp the home node app
   * @param apps the apps
   * @throws SpaceException the space exception {@inheritDoc}
   */
  public void initApp(Space space, String homeNodeApp, List<String> apps) throws SpaceException {
    try {
      PageNavigation spaceNav = SpaceUtils.createGroupNavigation(space.getGroupId());
      PageNode homeNode = createPageNodeFromApplication(space, homeNodeApp, null, true);

      List<PageNode> childNodes = homeNode.getChildren();
      if (childNodes == null)
        childNodes = new ArrayList<PageNode>();
      PageNode dashBoardPageNode = null;
      for (String app : apps) {
        app = (app.trim()).split(":")[0];
        PageNode appNode = createPageNodeFromApplication(space, app, null, false);
        // if current node is DashBoard application portlet
        if ("DashboardPortlet".equals(app))
          dashBoardPageNode = appNode;
        childNodes.add(appNode);
      }
      // homeNode.setChildren((ArrayList<PageNode>) childNodes);
      spaceNav.addNode(homeNode);
      dataStorage.save(spaceNav);
      // TODO. Change number of column into 2 in dashboard application in space
      // SOC-837
      changeDashBoardColumn(dashBoardPageNode);
      SpaceUtils.setNavigation(spaceNav);
      PortalConfig portalConfig = dataStorage.getPortalConfig(PortalConfig.GROUP_TYPE,
                                                              space.getGroupId());
      LOG.info("portalConfig: " + portalConfig);

    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_INIT_APP, e);
    }
  }

  /**
   * De-initialize HomeSpacePortlet application.
   *
   * @param space
   */
  public void deInitApp(Space space) throws SpaceException {
    try {
      String groupId = space.getGroupId();
      PageNavigation spaceNav = SpaceUtils.getGroupNavigation(groupId);
      // return in case group navigation was removed by portal SOC-548
      if (spaceNav == null)
        return;
      ArrayList<PageNode> spaceNodes = spaceNav.getNodes();
      for (PageNode spaceNode : spaceNodes) {
        String pageId = spaceNode.getPageReference();
        Page page = dataStorage.getPage(pageId);
        dataStorage.remove(page);
      }
      SpaceUtils.removeGroupNavigation(groupId);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_DEINIT_APP, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void activateApplication(Space space, String appId, String appName) throws SpaceException {
    activateApplicationClassic(space, appId, appName);
  }

  /**
   * {@inheritDoc}
   */
  public void deactiveApplication(Space space, String appId) throws SpaceException {
    deactivateApplicationClassic(space, appId);
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
    // List<Application> apps;
    try {
      // apps = getSpaceApplications(space);
      // for (Application app : apps) {
      // removeApplication(space, app.getApplicationName());
      // }
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
      if (childNodes == null)
        childNodes = new ArrayList<PageNode>();
      //
      childNodes.add(pageNode);
      dataStorage.save(nav);
      // TODO. change number of column into 2 in dashboard application in space
      // SOC-837
      if ("DashboardPortlet".equals(appId))
        changeDashBoardColumn(pageNode);
      uiPortal.setSelectedNode(selectedNode);
      uiPortal.setSelectedNavigation(nav);
      SpaceUtils.setNavigation(nav);
    } catch (Exception e) {
      try {
        // TODO if navigation can't be updated, remove the page
      } catch (Exception e1) {
      }
      throw new SpaceException(SpaceException.Code.UNABLE_TO_ADD_APPLICATION, e);
    }
  }

  /**
   * Deactivates an application in a space
   *
   * @param space
   * @param appId
   */
  private void deactivateApplicationClassic(Space space, String appId) {

  }

  /**
   * Removes an classic-type application from a space
   *
   * @param space
   * @param appId
   * @throws SpaceException
   */
  private void removeApplicationClassic(Space space, String appId, String appName) throws SpaceException {
    try {
      String groupId = space.getGroupId();
      PageNavigation nav = dataStorage.getPageNavigation(PortalConfig.GROUP_TYPE, groupId);

      // return in case group navigation was removed by portal SOC-548
      if (nav == null)
        return;
      PageNode homeNode = SpaceUtils.getHomeNode(nav, space.getUrl());
      if (homeNode == null) {
        throw new Exception("homeNode is null!");
      }
      List<PageNode> childNodes = homeNode.getChildren();
      String nodeName = appName;
      // nodeName = SpaceUtils.getAppNodeName(space, appName);
      // if (nodeName == null) nodeName = SpaceUtils.getAppNodeName(space,
      // appId);
      PageNode childNode = homeNode.getChild(nodeName);
      // bug from portal, gets by nodeUri instead
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
   *
   * @param apps
   * @param appId
   * @return
   */
  private Application getApplication(List<Application> apps, String appId) {
    for (Application app : apps) {
      if (app.getApplicationName().equals(appId))
        return app;
    }
    return null;
  }

  /**
   * Creates page node from application. - Creates Application instance from
   * appId. <br />
   * - Creates Page instance and set the newly-created application for that
   * page; adds application to container. <br />
   * - Creates PageNode instance and returns that pageNode.
   *
   * @param space
   * @param appId
   * @param isRoot
   * @return
   * @throws SpaceException
   */
  private PageNode createPageNodeFromApplication(Space space,
                                                 String appId,
                                                 String appName,
                                                 boolean isRoot) throws SpaceException {
    Application app = getApplication(appCache, appId);
    if (app == null) {
      try {
        if (appStoreCache == null) {
          appStoreCache = SpaceUtils.getAppStore(space);
        }
        app = getApplication(appStoreCache, appId);
        if (app == null) {
          // retry
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
    if (contentId == null) {
      contentId = app.getCategoryName() + "/" + app.getApplicationName();
    }
    String appInstanceId = PortalConfig.GROUP_TYPE + "#" + space.getGroupId() + ":/" + contentId
        + "/" + app.getApplicationName() + System.currentTimeMillis();
    org.exoplatform.portal.config.model.Application<Gadget> gadgetApplication = null;
    org.exoplatform.portal.config.model.Application<Portlet> portletApplication = null;

    if (app.getType() == ApplicationType.GADGET) {
      TransientApplicationState<Gadget> gadgetState = new TransientApplicationState<Gadget>(app.getApplicationName());
      gadgetApplication = org.exoplatform.portal.config.model.Application.createGadgetApplication();
      gadgetApplication.setState(gadgetState);
      gadgetApplication.setAccessPermissions(new String[] { "*:" + space.getGroupId() });
      gadgetApplication.setShowInfoBar(false);
    } else {
      portletApplication = createPortletApplication(appInstanceId, space, isRoot);
      portletApplication.setAccessPermissions(new String[] { "*:" + space.getGroupId() });
      portletApplication.setShowInfoBar(false);
    }

    String pageTitle = space.getName() + " - " + app.getDisplayName();
    String pageName = app.getApplicationName();
    if (SpaceUtils.isInstalledApp(space, appId) && (appName != null)) {
      pageName = appName;
    }
    UserPortalConfigService userPortalConfigService = getUserPortalConfigService();
    Page page = null;
    try {
      page = userPortalConfigService.createPageTemplate("space",
                                                        PortalConfig.GROUP_TYPE,
                                                        space.getGroupId());
      page.setName(pageName);
      page.setTitle(pageTitle);
      page.setModifiable(true);
      dataStorage.create(page);
      page = dataStorage.getPage(page.getPageId());
      setPage(space, app, gadgetApplication, portletApplication, page);
      dataStorage.save(page);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    PageNode pageNode = new PageNode();
    String label = app.getDisplayName();
    if (appName != null) {
      String sufixLabel = pageName.replaceAll(app.getApplicationName(), "");
      label = label + sufixLabel;
    }

    if (isRoot) {
      pageName = space.getUrl();
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

  @SuppressWarnings("unchecked")
  private void setPage(Space space,
                       Application app,
                       org.exoplatform.portal.config.model.Application<Gadget> gadgetApplication,
                       org.exoplatform.portal.config.model.Application<Portlet> portletApplication,
                       Page page) {
    String visibility = space.getVisibility();
    if (visibility.equals(Space.PUBLIC)) {
      page.setAccessPermissions(new String[] { UserACL.EVERYONE });
    } else {
      page.setAccessPermissions(new String[] { "*:" + space.getGroupId() });
    }
    page.setEditPermission("manager:" + space.getGroupId());

    ArrayList<ModelObject> pageChilds = page.getChildren();

    Container menuContainer = SpaceUtils.findContainerById(pageChilds, SpaceUtils.MENU_CONTAINER);
    org.exoplatform.portal.config.model.Application<Portlet> menuPortlet = (org.exoplatform.portal.config.model.Application<Portlet>) menuContainer.getChildren()
                                                                                                                                                   .get(0);
    ApplicationState<Portlet> state = menuPortlet.getState();
    Portlet portletPreference;
    try {
      portletPreference = dataStorage.load(state, ApplicationType.PORTLET);
      if (portletPreference == null) {
        portletPreference = new PortletBuilder().add(SpaceUtils.SPACE_URL, space.getUrl()).build();
      } else {
        portletPreference.setValue(SpaceUtils.SPACE_URL, space.getUrl());
      }
      dataStorage.save(state, portletPreference);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Container container = SpaceUtils.findContainerById(pageChilds, SpaceUtils.APPLICATION_CONTAINER);
    ArrayList<ModelObject> children = container.getChildren();
    if (app.getType() == ApplicationType.GADGET) {
      children.add(gadgetApplication);
    }
    else {
      children.add(portletApplication);
    }
    container.setChildren(children);
    pageChilds = setContainerById(pageChilds, container);
    page.setChildren(pageChilds);
    setPermissionForPage(page.getChildren(), "*:" + space.getGroupId());
  }

  /**
   * Gets application from appStore by appId
   *
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
      while (appListItr.hasNext()) {
        Application app = appListItr.next();
        if (app.getApplicationName().equals(appId))
          return app;
      }
    }
    return null;
  }

  /**
   * Sets permission for page
   *
   * @param childs
   * @param id
   * @return
   */
  @SuppressWarnings("unchecked")
  private void setPermissionForPage(ArrayList<ModelObject> childrens, String perm) {
    for (ModelObject modelObject : childrens) {
      if (modelObject instanceof org.exoplatform.portal.config.model.Application<?>) {
        ((org.exoplatform.portal.config.model.Application) modelObject).setAccessPermissions(new String[] { perm });
      } else if (modelObject instanceof Container) {
        ((Container) modelObject).setAccessPermissions(new String[] { perm });
        setPermissionForPage(((Container) modelObject).getChildren(), perm);
      }
    }
  }

  /**
   * Sets container by Id
   *
   * @param childs
   * @param container
   * @return
   */
  private ArrayList<ModelObject> setContainerById(ArrayList<ModelObject> childs, Container container) {
    ArrayList<ModelObject> result = childs;
    int index = result.indexOf(container);
    if (index != -1)
      result.set(index, container);
    else {
      for (int i = 0; i < result.size(); i++) {
        ModelObject obj = result.get(i);
        if (org.exoplatform.portal.config.model.Application.class.isInstance(obj))
          continue;
        Container objContainer = (Container) obj;
        ArrayList<ModelObject> tmp = setContainerById(objContainer.getChildren(), container);
        objContainer.setChildren(tmp);
        result.set(i, objContainer);
      }
    }
    return result;
  }

  /**
   * Creates portlet application from instanceId
   *
   * @param instanceId
   * @return
   */
  private org.exoplatform.portal.config.model.Application<Portlet> createPortletApplication(String instanceId,
                                                                                            Space space,
                                                                                            Boolean isRoot) {
    int i0 = instanceId.indexOf("#");
    int i1 = instanceId.indexOf(":/", i0 + 1);
    String ownerType = instanceId.substring(0, i0);
    String ownerId = instanceId.substring(i0 + 1, i1);
    String persistenceid = instanceId.substring(i1 + 2);
    String[] persistenceChunks = Utils.split("/", persistenceid);
    PortletBuilder pb = new PortletBuilder();
    for (String appName : SpaceUtils.PORTLETS_SPACE_URL_PREFERENCE_NEEDED) {
      if (instanceId.contains(appName)) {
        pb.add(SpaceUtils.SPACE_URL, space.getUrl());
        break;
      }
    }

    TransientApplicationState<Portlet> portletState = new TransientApplicationState<Portlet>(persistenceChunks[0]
                                                                                                 + "/"
                                                                                                 + persistenceChunks[1],
                                                                                             pb.build(),
                                                                                             ownerType,
                                                                                             ownerId,
                                                                                             persistenceChunks[2]);
    org.exoplatform.portal.config.model.Application<Portlet> portletApp = org.exoplatform.portal.config.model.Application.createPortletApplication();
    portletApp.setState(portletState);
    return portletApp;
  }

  /**
   * Change the number of columns in DashBoard of space into 2 instead of 3 by
   * default.
   *
   * @param spaceAppNode Dashboard page node.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private void changeDashBoardColumn(PageNode spaceAppNode) throws Exception {
    String pageId = spaceAppNode.getPageReference();
    Page page = dataStorage.getPage(pageId);
    ArrayList<ModelObject> pageChildren = page.getChildren();

    Container applicationContainer = SpaceUtils.findContainerById(pageChildren,
                                                                  APPLICATION_CONTAINER);

    try {
      org.exoplatform.portal.config.model.Application<org.exoplatform.portal.pom.spi.portlet.Portlet> applicationPortlet = (org.exoplatform.portal.config.model.Application<org.exoplatform.portal.pom.spi.portlet.Portlet>) applicationContainer.getChildren()
                                                                                                                                                                                                                                                 .get(0);

      // Get DashBoard storageId for load DashBoard container
      String dashboardId = applicationPortlet.getStorageId();
      Dashboard container = dataStorage.loadDashboard(dashboardId);
      ArrayList<ModelObject> containerDatas = container.getChildren();
      // Remove one child (one column) from children.
      containerDatas.remove(2);
      container.setChildren(containerDatas);
      dataStorage.saveDashboard(container);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets userPortalConfigService for the usage of creating new page from page
   * template
   *
   * @return
   */
  private UserPortalConfigService getUserPortalConfigService() {
    return (UserPortalConfigService) container.getComponentInstanceOfType(UserPortalConfigService.class);
  }

}
