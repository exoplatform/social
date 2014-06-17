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
package org.exoplatform.social.webui.space;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.common.router.ExoRouter.Route;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


@ComponentConfig(
  template = "war:/groovy/social/webui/space/UISpaceMenu.gtmpl",
  events = {
    @EventConfig(name = "RenameSpaceAppName", listeners = UISpaceMenu.RenameSpaceAppNameActionListener.class)
  }
)

public class UISpaceMenu extends UIContainer {

  /**
   * NEW SPACE APPLICATION NAME.
   */
  private static final String NEW_SPACE_APPLICATION_NAME = "newSpaceAppName";
  /**
   * INVALID APPLICATION NAME MESSAGE.
   */
  private static final String INVALID_APPLICATION_NAME_MSG = "UISpaceMenuPortlet.msg.invalidAppName";

  private static final String EXISTING_APPLICATION_NAME_MSG = "UISpaceMenuPortlet.msg.existingAppName";

  private static final String SPACE_HOME_APP_NAME = "UISpaceMenu.label.Home";

  private static final String SPACE_SETTINGS = "settings";
  
  public static final String HIDDEN = "HIDDEN";
  
  private static final String DEFAULT_APP_ID = "DefaultAppId";
  
  private static final String APP_NAME = "appName";

  private static final Log LOG = ExoLogger.getLogger(UISpaceMenu.class);
  
  /**
   * Stores SpaceService object.
   */
  private SpaceService spaceService = null;

  /**
   * Stores Space object.
   */
  private Space space = null;

  /**
   * Constructor.
   *
   * @throws Exception
   */
  public UISpaceMenu() throws Exception {
    spaceService = getSpaceService(); 
  }

  /**
   * Gets page node list for displaying as application links.
   *
   * @return page node list
   * @throws Exception
   */
  public List<UserNode> getApps() throws Exception {
    String spaceUrl = Utils.getSpaceUrlByContext();
    space = spaceService.getSpaceByUrl(spaceUrl);
    
    if (space == null) {
      return new ArrayList<UserNode>(0);
    }
    
    List<UserNode> userNodeArraySorted = new ArrayList<UserNode>();
    
    try {
      UserNode spaceUserNode = SpaceUtils.getSpaceUserNode(space);
      
      UserNode hiddenNode = spaceUserNode.getChild(SPACE_SETTINGS);
      
      if (!hasSettingPermission() && (hiddenNode != null)) {
        spaceUserNode.removeChild(hiddenNode.getName());
      }
      
      userNodeArraySorted.addAll(spaceUserNode.getChildren());
      
      removeNonePageNodes(userNodeArraySorted); 
    } catch (Exception e) {
      LOG.warn("Get UserNode of Space failed.");
    }
    
    //SOC-2290 Need to comment the bellow line, sort by in configuration XML file.
    //Collections.sort(userNodeArraySorted, new ApplicationComparator());
    return userNodeArraySorted;
  }

  /**
   * Gets AppId to build class name of applications in space.
   * Returns default id in case of not matching.
   * 
   * @param appName The application name.
   * @return The id of application.
   */
  protected String getAppIdByAppName(String appName) {
    String[] appInfos = space.getApp().split(",");
    for (String appInfo : appInfos) {
      String[] appInfoParts = appInfo.split(":");
      if (appInfoParts[0].equals(appName) || appInfoParts[1].equals(appName)) {
        return appInfoParts[0];
      }
    }
    
    return DEFAULT_APP_ID;
  }
  
  /**
   * Renames space application name.<br> - Gets selected node and selected page navigation.<br> -
   * Changes selected node information.<br> - Updates new information for selected page
   * navigation.<br>
   */
  static public class RenameSpaceAppNameActionListener extends EventListener<UISpaceMenu> {
    public void execute(Event<UISpaceMenu> event) throws Exception {
      UISpaceMenu spaceMenu = event.getSource();
      WebuiRequestContext context = event.getRequestContext();

      String newSpaceAppName = context.getRequestParameter(NEW_SPACE_APPLICATION_NAME);
      UIPortal uiPortal = Util.getUIPortal();
      PortalRequestContext prContext = Util.getPortalRequestContext();
      SpaceService spaceService = spaceMenu.getApplicationComponent(SpaceService.class);
      String spaceUrl = Utils.getSpaceUrlByContext();
      Space space = spaceService.getSpaceByUrl(spaceUrl);

      UserNode selectedNode = uiPortal.getSelectedUserNode();
      String pageRef = selectedNode.getPageRef().format();
      String appName = pageRef.substring(pageRef.lastIndexOf("::") + 2);
      
      UserNode homeNode = null;

      homeNode = SpaceUtils.getSpaceUserNode(space);
      
      if (homeNode == null) {
        throw new Exception("homeNode is null!");
      }

      String oldName = selectedNode.getName();
      if (selectedNode.getResolvedLabel().equals(newSpaceAppName)) {
        prContext.getResponse().sendRedirect(Utils.getSpaceURL(selectedNode));
        return;
      }
      UIApplication uiApp = context.getUIApplication();
      if (!spaceMenu.isValidAppName(newSpaceAppName)) {
        uiApp.addMessage(new ApplicationMessage(INVALID_APPLICATION_NAME_MSG, null, ApplicationMessage.WARNING));
        prContext.getResponse().sendRedirect(Utils.getSpaceURL(selectedNode));
        return;
      }
      String newNodeName = newSpaceAppName.trim().replace(' ', '_');
      if (spaceMenu.isAppNameExisted(homeNode, newNodeName)) {
        uiApp.addMessage(new ApplicationMessage(EXISTING_APPLICATION_NAME_MSG, null, ApplicationMessage.INFO));
        prContext.getResponse().sendRedirect(Utils.getSpaceURL(selectedNode));
        return;
      }

      String installedApps = space.getApp();
      String[] apps = installedApps.split(",");
      String[] appParts = null;
      String editedApp = null;
      String newInstalledApps = null;

      // Check and update new application name.
      for (String app : apps) {
        if (app.length() != 0) {
          appParts = app.split(":");
          if (appParts[0].equals(appName)) {
            editedApp = appParts[0] + ":" + newSpaceAppName + ":" + appParts[2] + ":" + appParts[3];
            newInstalledApps = installedApps.replaceAll(app, editedApp);
            space.setApp(newInstalledApps);
            spaceService.updateSpace(space);
            break;
          }
        }
      }

      // Change node and page of selected node.
      UserNode renamedNode = homeNode.getChild(oldName);
      renamedNode.setName(newNodeName);
      renamedNode.setLabel(newSpaceAppName);
      DataStorage dataService = spaceMenu.getApplicationComponent(DataStorage.class);
      Page page = dataService.getPage(renamedNode.getPageRef().format());
      if (page != null) {
        page.setTitle(newNodeName);
        dataService.save(page);
      }
      UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
      userPortal.saveNode(homeNode, null);
      
      String newUri = renamedNode.getURI();
      if (newUri != null) {
        prContext.getResponse().sendRedirect(Utils.getSpaceURL(renamedNode));
      }
    }
  }

  /**
   * Gets space name from space url.
   *
   * @return space's name.
   * @throws Exception
   */
  public String getSpaceName() throws Exception {
    space = getSpace();
    if (space == null) {
      return null;
    }
    return space.getDisplayName();
  }

  /**
   * Gets selected application page node.
   *
   * @return selected application page node
   * @throws Exception
   */
  public String getAppSelected() throws Exception {
    PortalRequestContext plcontext = Util.getPortalRequestContext();
    String requestPath = plcontext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    Route route = ExoRouter.route(requestPath);
    if (route == null) {
      return null;
    }
    //
    return route.localArgs.get(APP_NAME);
  }

  /**
   * Gets image source url.
   *
   * @return image source url
   * @throws Exception
   */
  protected String getImageSource() {
    Space space = getSpace();
    if (space != null) {
      return space.getAvatarUrl();
    } else {
      return "";
    }
  }

  /**
   * Checks if current user is leader or not.<br>
   *
   * @return true if current login user is leader.
   * @throws SpaceException
   */
  protected boolean hasSettingPermission() throws SpaceException {
    spaceService = getSpaceService();
    String userId = Util.getPortalRequestContext().getRemoteUser();
    Space space = getSpace();
    return spaceService.hasSettingPermission(space, userId);
  }
 
  /**
   * Gets spaceService.
   *
   * @return spaceService
   * @see SpaceService
   */
  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Returns space object if it is null then initialize before return.<br>
   *
   * @return space object.
   */
  private Space getSpace() {
    if (space == null) {
      space = Utils.getSpaceByContext();
    }
    return space;
  }

  /**
   * Checks the input name is existed or not.<br>
   *
   * @param pageNav
   * @param nodeName
   * @return true if input name is existed. false if it not.
   * @throws Exception
   */
  private boolean isAppNameExisted(UserNode homeNode, String nodeName) throws Exception {
    Collection<UserNode> nodes = homeNode.getChildren();

    // Check in case new name is duplicated with space name
    for (UserNode node : nodes) {
      if (node.getName().equals(nodeName)) {
        return true;
      }
    }
   
    //space home application name is not UserNode so we alse need to check this name
    String spaceHomeAppName = WebuiRequestContext.getCurrentInstance()
            .getApplicationResourceBundle().getString(SPACE_HOME_APP_NAME);
    return nodeName.equals(spaceHomeAppName);
  }

  /**
   * Checks the input new space application name is valid or not.<br>
   *
   * @param appName
   * @return true if input name is valid. false if not.
   */
  private boolean isValidAppName(String appName) {
    appName = appName.trim();

    if (appName == null || appName.length() < 1) {
      return false;
    }

    if (Character.isDigit(appName.charAt(0)) || appName.charAt(0) == '-') {
      return false;
    }
    for (int i = 0; i < appName.length(); i++) {
      char c = appName.charAt(i);
      if (Character.isLetter(c) || Character.isDigit(c) || c == '_' || c == '-' || Character.isSpaceChar(c)) {
        continue;
      }
      return false;
    }
    return true;
  }
  
  /**
   * Get the space from space url.
   * 
   * @param spaceUrl
   * @return
   * @since 1.2.1
   */
  protected Space getSpace(String spaceUrl) {
    return getSpaceService().getSpaceByUrl(spaceUrl);
  }
  
/**
 * Removes nodes that have no page.
 * 
 * @param nodes
 * @since 4.0.1-GA
 */
private void removeNonePageNodes(List<UserNode> nodes) {
  
  List<UserNode> nonePageNodes = new ArrayList<UserNode>();
  
  for (UserNode node : nodes) {
    if (node.getPageRef() == null) {
      nonePageNodes.add(node);
    }
  }
  
  nodes.removeAll(nonePageNodes);
} 
}
