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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceApplicationConfigPlugin;
import org.exoplatform.social.core.space.SpaceApplicationConfigPlugin.SpaceApplication;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceApplicationHandler;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * Default implementation for working with space applications.
 *
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since OCt 17, 2008
 */

public class DefaultSpaceApplicationHandler implements SpaceApplicationHandler {
  private static final Log LOG = ExoLogger.getLogger(DefaultSpaceApplicationHandler.class);

  public static final String NAME = "classic";

  public static final String SPACE_TEMPLATE_PAGE_ID = "portal::classic::spacetemplate";

  public static final String APPLICATION_CONTAINER = "Application";

  private static final String SPACE_DEFAULT_ICON = "SpaceDefaultIcon";

  /**
   * The {groupId} preference value pattern
   *
   * @since 1.2.0-GA
   */
  private static final String GROUP_ID_PREFERENCE = "{groupId}";

  /**
   * The {modifiedGroupId} preference value pattern
   *
   * @since 1.2.0-GA
   */
  private static final String MODIFIED_GROUP_ID_PREFERENCE = "{modifiedGroupId}";

  /**
   * The {pageName} preference value pattern
   *
   * @since 1.2.0-GA
   */
  private static final String PAGE_NAME_PREFERENCE = "{pageName}";

  /**
   * The {pageUrl} preference value pattern
   *
   * @since 1.2.0-GA
   */
  private static final String PAGE_URL_PREFERENCE = "{pageUrl}";

  /**
   * The {spaceUrl} preference value pattern
   *
   * @since 1.2.0-GA
   */
  private static final String SPACE_URL_PREFERENCE = "{spaceUrl}";

  private PortalContainer container = PortalContainer.getInstance();

  private DataStorage dataStorage = null;

  private SpaceService spaceService;

  private static Map<ApplicationCategory, List<Application>> appStoreCache = null;

  private static List<Application> appCache = new ArrayList<Application>();
  
  private List<SpaceApplication> spaceApplications = new ArrayList<SpaceApplication>();

  /**
   * Constructor.
   *
   * @param dataStorage
   */
  public DefaultSpaceApplicationHandler(DataStorage dataStorage) {
    this.dataStorage = dataStorage;
  }

  /**
   * {@inheritDoc}
   */
  public void initApps(Space space, SpaceApplicationConfigPlugin spaceApplicationConfigPlugin) throws SpaceException {
    try {
      UserNavigation spaceNav = SpaceUtils.createGroupNavigation(space.getGroupId());
      UserPortal userPortal = SpaceUtils.getUserPortal();
      UserNode parentNode = userPortal.getNode(spaceNav, Scope.CHILDREN, null, null);
        
      UserNode homeNode = createPageNodeFromApplication(parentNode, space, spaceApplicationConfigPlugin.getHomeApplication(),
                                                        null, true);
      SpaceService spaceService = getSpaceService();

      userPortal.updateNode(parentNode, Scope.CHILDREN, null);

      spaceApplications = spaceApplicationConfigPlugin.getSpaceApplicationList();
      for (SpaceApplication spaceApplication : spaceApplications) {
        UserNode appNode = createPageNodeFromApplication(homeNode, space, spaceApplication, null, false);
        spaceService.installApplication(space, spaceApplication.getPortletName());
      }
      
      //commit the parentNode to JCR 
      userPortal.saveNode(parentNode, null);
    } catch (Exception e) {
      throw new SpaceException(SpaceException.Code.UNABLE_TO_INIT_APP, e);
    }
  }
   
  
  /**
   * {@inheritDoc}
   */
  public void deInitApp(Space space) throws SpaceException {
    try {
      String groupId = space.getGroupId();
      UserNavigation spaceNav = SpaceUtils.getGroupNavigation(groupId);
      // return in case group navigation was removed by portal SOC-548
      if (spaceNav == null) {
        return;
      }
      UserNode userNode = SpaceUtils.getHomeNodeWithChildren(spaceNav, space.getUrl());

      Collection<UserNode> spaceNodes = userNode.getChildren();
      for (UserNode spaceNode : spaceNodes) {
        String pageId = spaceNode.getPageRef();
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
    try {
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
    UserNavigation nav = SpaceUtils.createGroupNavigation(space.getGroupId());
    SpaceApplication spaceApplication = new SpaceApplication();
    spaceApplication.setPortletName(appId);
    UserNode parentNode = SpaceUtils.getHomeNode(nav, space.getUrl());
    UserNode pageNode = createPageNodeFromApplication(parentNode, space, spaceApplication, appName, false);
    SpaceUtils.getUserPortal().saveNode(parentNode, null);
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
      UserNavigation nav = SpaceUtils.getGroupNavigation(groupId);
      
      UserNode homeNode = SpaceUtils.getHomeNodeWithChildren(nav, space.getUrl());
      String nodeName = appName;
           
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserPortalConfigService configService = (UserPortalConfigService)
                                              container.getComponentInstanceOfType(UserPortalConfigService.class);

      UserNode removedNode = null;
      removedNode = homeNode.getChild(nodeName);
      if (removedNode == null) {
        removedNode = homeNode.getChild(nodeName.toLowerCase());
      }
      
      // In case of cannot find the removed node, try one more time
      if (removedNode == null) {
        for (SpaceApplication spaceApplication : spaceApplications) {
          if (appId.equals(spaceApplication.getPortletName())) {
            removedNode = homeNode.getChild(spaceApplication.getUri());
          }
        }
      }
      
      if (removedNode != null) {
        homeNode.removeChild(removedNode.getName());
      }
      
      //remove page
      if (removedNode != null) {
        String pageRef = removedNode.getPageRef();
        if (pageRef != null && pageRef.length() > 0) {
          Page page = configService.getPage(pageRef);
          if (page != null)
            dataStorage.remove(page);
          UIPortal uiPortal = Util.getUIPortal();
          // Remove from cache
          uiPortal.setUIPage(pageRef, null);
        }
      }
      getUserPortal().saveNode(homeNode, null);
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
      if (app.getApplicationName().equals(appId)) {
        return app;
      }
    }
    return null;
  }

  /**
   * Creates page node from application. - Creates Application instance from appId. <br /> - Creates Page instance and set
   * the newly-created application for that page; adds application to container. <br /> - Creates PageNode instance and
   * returns that pageNode.
   *
   * @param space
   * @param spaceApplication
   * @param isRoot
   * @return
   * @since 1.2.0-GA
   */
  private UserNode createPageNodeFromApplication(UserNode parentNode, Space space,
                                                 SpaceApplication spaceApplication,
                                                 String appName,
                                                 boolean isRoot) throws SpaceException {
    String appId = spaceApplication.getPortletName();
    Application app = getApplication(space, appId);
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
      gadgetApplication.setAccessPermissions(new String[]{"*:" + space.getGroupId()});
      gadgetApplication.setShowInfoBar(false);
    } else {
      portletApplication = createPortletApplication(appInstanceId, space, isRoot);
      portletApplication.setAccessPermissions(new String[]{"*:" + space.getGroupId()});
      portletApplication.setShowInfoBar(false);
    }

    String pageTitle = space.getDisplayName() + " - " + app.getDisplayName();
    String pageName = app.getApplicationName();
    //is the application installed?
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
      
      dataStorage.create(page);
      page = dataStorage.getPage(page.getPageId());
      //setting some data to page.
      setPage(space, app, gadgetApplication, portletApplication, page);
      dataStorage.save(page);
    } catch (Exception e) {
      LOG.warn(e.getMessage(), e);
    }
    
    
    String label = SpaceUtils.getDisplayAppName(app.getDisplayName());
    if (spaceApplication.getAppTitle() != null && !spaceApplication.getAppTitle().isEmpty()) {
      label = spaceApplication.getAppTitle();
    }

    if (isRoot) {
      pageName = space.getUrl();
      label = space.getDisplayName();
    } else {
      if (spaceApplication.getUri() != null && !spaceApplication.getUri().isEmpty()) {
        pageName = spaceApplication.getUri();
      }
      
    }
    UserPortal userPortal = SpaceUtils.getUserPortal();
    userPortal.updateNode(parentNode, Scope.CHILDREN, null);
    UserNode childNode = parentNode.addChild(pageName);
    //when apply Navigation, can not need to setName for childNode. It's automatically.
    //childNode.setName(pageName);
    if (spaceApplication.getIcon() != null && !spaceApplication.getIcon().isEmpty()) {
      childNode.setIcon(spaceApplication.getIcon());
    }
    childNode.setLabel(label);
    childNode.setPageRef(page.getPageId());
    
    try {
      //only save with type is transient status
      userPortal.saveNode(parentNode, null);
    } catch (NavigationServiceException nex) {
      LOG.info("Could not create page node from Application " + nex.toString());
      return null;
    } catch (Exception ex) {
       LOG.info("Could not create page node from Application ", ex);
       return null;
    }
    return childNode;
  }
  
  

  /**
   * Retrieving the UserPortal
   * @return
   */
  private UserPortal getUserPortal() {
    return Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
  }
  /**
   * Gets an application by its id.
   *
   * @param space
   * @param appId
   * @return
   * @throws SpaceException
   */
  private Application getApplication(Space space, String appId) throws SpaceException {
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
    return app;
  }

  @SuppressWarnings("unchecked")
  private void setPage(Space space,
                       Application app,
                       org.exoplatform.portal.config.model.Application<Gadget> gadgetApplication,
                       org.exoplatform.portal.config.model.Application<Portlet> portletApplication,
                       Page page) {
    String visibility = space.getVisibility();
    if (visibility.equals(Space.PUBLIC)) {
      page.setAccessPermissions(new String[]{UserACL.EVERYONE});
    } else {
      page.setAccessPermissions(new String[]{"*:" + space.getGroupId()});
    }
    page.setEditPermission("manager:" + space.getGroupId());

    ArrayList<ModelObject> pageChilds = page.getChildren();

    Container menuContainer = SpaceUtils.findContainerById(pageChilds, SpaceUtils.MENU_CONTAINER);
    org.exoplatform.portal.config.model.Application<Portlet> menuPortlet =
            (org.exoplatform.portal.config.model.Application<Portlet>) menuContainer.getChildren()
                    .get(0);
    ApplicationState<Portlet> state = menuPortlet.getState();
    Portlet portletPreference = null;
    try {
      portletPreference = dataStorage.load(state, ApplicationType.PORTLET);
      if (portletPreference == null) {
        portletPreference = new PortletBuilder().add(SpaceUtils.SPACE_URL, space.getUrl()).build();
      } else {
        portletPreference.setValue(SpaceUtils.SPACE_URL, space.getUrl());
      }
      dataStorage.save(state, portletPreference);
    } catch (Exception e) {
      LOG.warn("Failed to setPage", e);
    }

    Container container = SpaceUtils.findContainerById(pageChilds, SpaceUtils.APPLICATION_CONTAINER);
    ArrayList<ModelObject> children = container.getChildren();
    if (app.getType() == ApplicationType.GADGET) {
      children.add(gadgetApplication);
    } else {
      children.add(portletApplication);
    }
    container.setChildren(children);
    pageChilds = setContainerById(pageChilds, container);
    page.setChildren(pageChilds);
    setPermissionForPage(page.getChildren(), "*:" + space.getGroupId());
  }

  /**
   * Gets an application from appStore by appId.
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
        if (app.getApplicationName().equals(appId)) {
          return app;
        }
      }
    }
    return null;
  }

  /**
   * Sets permission for page.
   *
   * @param children
   * @param perm
   * @return
   */
  @SuppressWarnings("unchecked")
  private void setPermissionForPage(ArrayList<ModelObject> children, String perm) {
    for (ModelObject modelObject : children) {
      if (modelObject instanceof org.exoplatform.portal.config.model.Application<?>) {
        ((org.exoplatform.portal.config.model.Application) modelObject).setAccessPermissions(new String[]{perm});
      } else if (modelObject instanceof Container) {
        ((Container) modelObject).setAccessPermissions(new String[]{perm});
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
    //if container existing and child of the page
    if (index != -1) {
      result.set(index, container);
    } else {
      for (int i = 0; i < result.size(); i++) {
        ModelObject obj = result.get(i);
        if (org.exoplatform.portal.config.model.Application.class.isInstance(obj)) {
          continue;
        }
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
                                                                                            boolean isRoot) {
    int i0 = instanceId.indexOf("#");
    int i1 = instanceId.indexOf(":/", i0 + 1);
    String ownerType = instanceId.substring(0, i0);
    String ownerId = instanceId.substring(i0 + 1, i1);
    String persistenceid = instanceId.substring(i1 + 2);
    String[] persistenceChunks = Utils.split("/", persistenceid);
    PortletBuilder pb = new PortletBuilder();
    SpaceService spaceSrv = getSpaceService();
    String[] appNames = spaceSrv.getPortletsPrefsRequired();
    for (String appName : appNames) {
      if (instanceId.contains(appName)) {
        pb.add(SpaceUtils.SPACE_URL, space.getUrl());
        break;
      }
    }

    List<SpaceApplication> spaceApplicationList = spaceService.getSpaceApplicationConfigPlugin().getSpaceApplicationList();
    SpaceApplication spaceApplication = null;
    for (Iterator<SpaceApplication> iterator = spaceApplicationList.iterator(); iterator.hasNext() && spaceApplication == null;) {
      SpaceApplication tmpSpaceApplication = iterator.next();
      if (instanceId.contains(tmpSpaceApplication.getPortletName())) {
        spaceApplication = tmpSpaceApplication;
      }
    }
    if (spaceApplication != null && spaceApplication.getPreferences() != null) {
      Set<Entry<String, String>> entrySet = spaceApplication.getPreferences().entrySet();
      try {
        for (Map.Entry<String, String> preference : entrySet) {
          pb.add(preference.getKey(), getSubstituteValueFromPattern(space, spaceApplication, preference.getValue()));
        }
      } catch (Exception exception) {
        LOG.warn(exception.getMessage(), exception);
      }
    }

    TransientApplicationState<Portlet> portletState = new TransientApplicationState<Portlet>(persistenceChunks[0]
            + "/"
            + persistenceChunks[1],
            pb.build(),
            ownerType,
            ownerId,
            persistenceChunks[2]);
    org.exoplatform.portal.config.model.Application<Portlet> portletApp =
            org.exoplatform.portal.config.model.Application.createPortletApplication();
    portletApp.setState(portletState);
    return portletApp;
  }

  private String getSubstituteValueFromPattern(Space space, SpaceApplication spaceApplication, String pattern) {
    if (!pattern.contains("{") || !pattern.contains("}")) {
      return pattern;
    }
    if (pattern.contains(SPACE_URL_PREFERENCE)) {
      pattern = pattern.replace(SPACE_URL_PREFERENCE, space.getUrl());
    } else if (pattern.contains(GROUP_ID_PREFERENCE)) {
      pattern = pattern.replace(GROUP_ID_PREFERENCE, space.getGroupId());
    } else if (pattern.contains(MODIFIED_GROUP_ID_PREFERENCE)) {
      String modifiedGroupId = space.getGroupId().replace("/", ".");
      pattern = pattern.replace(MODIFIED_GROUP_ID_PREFERENCE, modifiedGroupId);
    } else if (pattern.contains(PAGE_NAME_PREFERENCE)) {
      pattern = pattern.replace(PAGE_NAME_PREFERENCE, spaceApplication.getAppTitle());
    } else if (pattern.contains(PAGE_URL_PREFERENCE)) {
      pattern = pattern.replace(PAGE_URL_PREFERENCE, spaceApplication.getUri());
    }
    return pattern;
  }

  /**
   * Gets userPortalConfigService for the usage of creating new page from page template
   *
   * @return
   */
  private UserPortalConfigService getUserPortalConfigService() {
    return (UserPortalConfigService) container.getComponentInstanceOfType(UserPortalConfigService.class);
  }

  private SpaceService getSpaceService() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (spaceService == null) {
      spaceService = (SpaceService) container.getComponentInstance(SpaceService.class);
    }

    return spaceService;
  }
}
