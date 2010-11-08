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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * {@link UISpaceMenu} used as a portlet displaying space menu. <br />
 * Created by The eXo Platform SARL
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Dec 15, 2008
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "classpath:groovy/social/webui/space/UISpaceMenu.gtmpl",
  events = {
    @EventConfig(name = "RenameSpaceAppName", listeners = UISpaceMenu.RenameSpaceAppNameActionListener.class)
  }
)

public class UISpaceMenu extends UIContainer {
  /** NEW SPACE APPLICATION NAME. */
  private static final String NEW_SPACE_APPLICATION_NAME = "newSpaceAppName";
  /** INVALID APPLICATION NAME MESSAGE. */
  private static final String INVALID_APPLICATION_NAME_MSG = "UISpaceMenuPortlet.msg.invalidAppName";

  private static final String EXISTING_APPLICATION_NAME_MSG = "UISpaceMenuPortlet.msg.existingAppName";

  private static final String SPACE_HOME_APP_NAME = "UISpaceMenu.label.Home";

  private static final String SPACE_SETTING_PORTLET = "SpaceSettingPortlet";
  /** Stores SpaceService object. */
  private SpaceService spaceService = null;

  /** Stores Space object. */
  private Space space = null;

  /**
   * constructor
   * @throws Exception
   */
  public UISpaceMenu() throws  Exception {
  }

  /**
   * Gets page node list for displaying as application links
   * @return page node list
   * @throws Exception
   */
  public List<PageNode> getApps() throws Exception {
    String spaceUrl = SpaceUtils.getSpaceUrl();
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    space = spaceSrc.getSpaceByUrl(spaceUrl);
    if (space == null) {
      return new ArrayList<PageNode>(0);
    }
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    PageNavigation pageNav = dataStorage.getPageNavigation(PortalConfig.GROUP_TYPE, space.getGroupId());
    PageNode homeNode = SpaceUtils.getHomeNode(pageNav, space.getUrl());
    if (homeNode == null) {
      throw new Exception("homeNode is null!");
    }
    List<PageNode> list = homeNode.getNodes();
    PageNode pageNode = null;
    for(PageNode node:list){
      if(node.getName().contains(SPACE_SETTING_PORTLET)){
        pageNode = node;
        break;
      }
    }
    if(!isLeader() && (pageNode != null)) list.remove(pageNode);
    Collections.sort(list, new ApplicationComparator());
    return list;
  }

  /**
   * Renames space application name.<br>
   * - Gets selected node and selected page navigation.<br>
   * - Changes selected node information.<br>
   * - Updates new information for selected page navigation.<br>
   *
   */
  static public class RenameSpaceAppNameActionListener extends EventListener<UISpaceMenu> {
    public void execute(Event<UISpaceMenu> event) throws Exception
    {
      UISpaceMenu spaceMenu = event.getSource();
      WebuiRequestContext context = event.getRequestContext();

      String newSpaceAppName = context.getRequestParameter(NEW_SPACE_APPLICATION_NAME);
      UIPortal uiPortal = Util.getUIPortal();
      PortalRequestContext prContext = Util.getPortalRequestContext();
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      SpaceService spaceService = spaceMenu.getApplicationComponent(SpaceService.class);
      String spaceUrl = SpaceUtils.getSpaceUrl();
      Space space = spaceService.getSpaceByUrl(spaceUrl);

      PageNode selectedNode = uiPortal.getSelectedNode();
      PageNode homeNode = null;
      UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
      UserPortalConfig userPortalConfig = uiPortalApp.getUserPortalConfig();
      List<PageNavigation> navigations = userPortalConfig.getNavigations();
      PageNavigation spaceNavigation = SpaceUtils.getGroupNavigation(space.getGroupId());
      for (PageNavigation navi : navigations) {
    	  if ((navi.getOwner()).equals(spaceNavigation.getOwner())) {
    		  spaceNavigation = navi;
    		  break;
    	  }
      }
      homeNode = SpaceUtils.getHomeNode(spaceNavigation, spaceUrl);
      if (homeNode == null) {
    	  throw new Exception("homeNode is null!");
      }
      List<PageNode> childNodes = homeNode.getNodes();
      String oldName = selectedNode.getName();
      String oldUri = selectedNode.getUri();
      if (selectedNode.getResolvedLabel().equals(newSpaceAppName)) {
        prContext.getResponse().sendRedirect(prContext.getPortalURI() + oldUri);
        return;
      }
      UIApplication uiApp = context.getUIApplication();
      if (!spaceMenu.isValidAppName(newSpaceAppName)) {
        uiApp.addMessage(new ApplicationMessage(INVALID_APPLICATION_NAME_MSG, null, ApplicationMessage.WARNING));
        prContext.getResponse().sendRedirect(prContext.getPortalURI() + oldUri);
        return;
      }
      String newNodeName = newSpaceAppName.trim().replace(' ', '_');
      if (spaceMenu.isAppNameExisted(spaceNavigation, newNodeName)) {
    	  uiApp.addMessage(new ApplicationMessage(EXISTING_APPLICATION_NAME_MSG, null, ApplicationMessage.INFO));
    	  prContext.getResponse().sendRedirect(prContext.getPortalURI() + oldUri);
          return;
      }
      String newUri = oldUri.substring(0, oldUri.lastIndexOf("/") + 1) + newNodeName;
      PageNode childNode = null;
      for (int i = 0; i < childNodes.size(); i++) {
        childNode = childNodes.get(i);
        if (selectedNode.getName().equals(childNode.getName())) {
           childNode.setLabel(newSpaceAppName);
           childNode.setName(newNodeName);
           childNode.setUri(newUri);
           selectedNode = childNode;
           break;
        }
      }

      String installedApps = space.getApp();
      String[] apps = installedApps.split(",");
      String[] appParts = null;
      String editedApp = null;
      String newInstalledApps =null;

      // Check and update new application name.
      for (String app : apps) {
        if (app.length() != 0) {
          appParts = app.split(":");
          if (appParts[1].equals(oldName)) {
            editedApp = appParts[0] + ":" + newNodeName + ":" + appParts[2] + ":" + appParts[3];
            newInstalledApps = installedApps.replaceAll(app, editedApp);
            space.setApp(newInstalledApps);
            spaceService.saveSpace(space, false);
            break;
          }
        }
      }
      dataStorage.save(spaceNavigation);
      uiPortal.setSelectedNode(selectedNode);
      uiPortal.setSelectedNavigation(spaceNavigation);
      SpaceUtils.setNavigation(spaceNavigation);

      if (newUri != null) {
         prContext.getResponse().sendRedirect(prContext.getPortalURI() + newUri);
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
    return space.getName();
  }

  /**
   * Gets selected application page node
   * @return selected application page node
   * @throws Exception
   */
  public String getAppSelected() throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    PageNode selectedNode = uiPortal.getSelectedNode();
    String[] split = selectedNode.getUri().split("/");
    return split[split.length - 1];
  }

  /**
   * Gets image source url
   * @return image source url
   * @throws Exception
   */
  protected String getImageSource() throws Exception {
    Space space = getSpace();
    if (space != null) {
      return space.getImageSource();
    } else {
      return "";
    }
  }

  /**
   * Checks if current user is leader or not.<br>
   *
   * @return true if current login user is leader.
   *
   * @throws SpaceException
   */
  private boolean isLeader() throws SpaceException {
    spaceService = getSpaceService();
    String userId = Util.getPortalRequestContext().getRemoteUser();
    Space space = getSpace();
    if(spaceService.hasEditPermission(space.getId(), userId)) {
      return true;
    }

    return false;
  }

  /**
   * Application comparator
   * @author hoatle
   *
   */
  private class ApplicationComparator implements Comparator<PageNode> {
    public int compare(PageNode pageNode1, PageNode pageNode2) {
      return pageNode1.getResolvedLabel().compareToIgnoreCase(pageNode2.getResolvedLabel());
    }
  }

  /**
   * Gets spaceService
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
      try {
        spaceService = getSpaceService();
        String spaceUrl = SpaceUtils.getSpaceUrl();
        space = spaceService.getSpaceByUrl(spaceUrl);
      } catch (SpaceException e) {
        e.printStackTrace();
      }
    }
    return space;
  }

  /**
   * Checks the input name is existed or not.<br>
   *
   * @param pageNavigation
   *
   * @param nodeName
   *
   * @return true if input name is existed. false if it not.
   * @throws Exception
   */
  private boolean isAppNameExisted(PageNavigation pageNav, String nodeName) throws Exception
  {
    PageNode homeNode = pageNav.getNode(SpaceUtils.getSpaceUrl());
    if (homeNode == null) {
      throw new Exception("homeNode is null!");
    }
    List<PageNode> nodes = homeNode.getChildren();

    // Check in case new name is duplicated with space name
    for (PageNode node : pageNav.getNodes()) {
      if (node.getName().equals(nodeName)) {
         return true;
      }
    }
    for (PageNode node : nodes) {
      if (node.getName().equals(nodeName)) {
         return true;
      }
    }

    //space home application name is not PageNode so we alse need to check this name 
    String spaceHomeAppName = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle().getString(SPACE_HOME_APP_NAME);
    return nodeName.equals(spaceHomeAppName);
  }

  /**
   * Checks the input new space application name is valid or not.<br>
   *
   * @param appName
   *
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
}