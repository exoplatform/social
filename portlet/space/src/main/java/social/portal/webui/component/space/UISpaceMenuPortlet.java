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
package social.portal.webui.component.space;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceAttachment;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * {@link UISpaceMenuPortlet} used as a portlet displaying space menu. <br />
 * Created by The eXo Platform SARL
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Dec 15, 2008
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/portal/webui/space/UISpaceMenuPortlet.gtmpl",
  events = { @EventConfig(name = "RenameSpaceAppName",
                          listeners = UISpaceMenuPortlet.RenameSpaceAppNameActionListener.class)}
)

public class UISpaceMenuPortlet extends UIPortletApplication {
  /** NEW SPACE APPLICATION NAME. */
  private static final String NEW_SPACE_APPLICATION_NAME = "newSpaceAppName";
  
  /** INVALID APPLICATION NAME MESSAGE. */
  private static final String INVALID_APPLICATION_NAME_MSG = "UISpaceMenuPortlet.msg.invalidAppName";
  
  private static final String SPACE_SETTING_PORTLET = "SpaceSettingPortlet";
  /** Stores SpaceService object. */
  private SpaceService spaceService = null;
  
  /** Stores UIPortal object. */
  private UIPortal uiPortal = null;
  
  /** Stores Space object. */
  private Space space = null;
  
  /**
   * constructor
   * @throws Exception
   */
  public UISpaceMenuPortlet() throws  Exception { 
    uiPortal = Util.getUIPortal();
  }
  
  /**
   * gets page node list
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
    UserPortalConfigService userPortalConfigService = getApplicationComponent(UserPortalConfigService.class);
    PageNavigation pageNav = userPortalConfigService.getPageNavigation(PortalConfig.GROUP_TYPE, space.getGroupId());

    PageNode homeNode = pageNav.getNode(spaceUrl);
    if (homeNode == null) {
      PageNavigation selectedNavigation = getUIPortal().getSelectedNavigation(); 
      homeNode = selectedNavigation.getNodes().get(0);
    }
    List<PageNode> list = homeNode.getChildren();
    PageNode pageNode = null;
    for(PageNode node:list){
      if(node.getName().equals(SPACE_SETTING_PORTLET)){
        pageNode = node;
        break;
      }
    }
    if(!isLeader() && (pageNode != null)) list.remove(pageNode); 
    Collections.sort(list, new ApplicationComparator());
    return list;
  }
  
  /**
   * Rename space application name.<br>
   * - Get selected node and selected page navigation.<br>
   * - Change selected node information.<br>
   * - Update new information for selected page navigation.<br> 
   *
   */
  static public class RenameSpaceAppNameActionListener extends EventListener<UISpaceMenuPortlet> {
    public void execute(Event<UISpaceMenuPortlet> event) throws Exception
    {
      UISpaceMenuPortlet spaceMenu = event.getSource();
      WebuiRequestContext context = event.getRequestContext();
      String newSpaceAppName = context.getRequestParameter(NEW_SPACE_APPLICATION_NAME);
      UIPortal uiPortal = spaceMenu.getUIPortal();
      PortalRequestContext prContext = Util.getPortalRequestContext();
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      SpaceService spaceService = spaceMenu.getApplicationComponent(SpaceService.class);
      String spaceUrl = SpaceUtils.getSpaceUrl();
      Space space = spaceService.getSpaceByUrl(spaceUrl);
      PageNavigation pageNav = uiPortal.getSelectedNavigation();
      PageNode selectedNode = uiPortal.getSelectedNode();
      
      String oldName = selectedNode.getName();
      String oldUri = selectedNode.getUri();
      
      if (selectedNode.getResolvedLabel().equals(newSpaceAppName)) {
        prContext.getResponse().sendRedirect(prContext.getPortalURI() + oldUri);
        return;
      }
      UIApplication uiApp = context.getUIApplication();
      if (!spaceMenu.isValidAppName(newSpaceAppName))
      {
        uiApp.addMessage(new ApplicationMessage(INVALID_APPLICATION_NAME_MSG, null, ApplicationMessage.ERROR));
        prContext.getResponse().sendRedirect(prContext.getPortalURI() + oldUri);
        return;
      }
      
      selectedNode.setLabel(newSpaceAppName);
      
      String newNodeName = newSpaceAppName.replace(' ', '_');
      
      if (spaceMenu.isAppNameExisted(pageNav, newNodeName))
      {
         newNodeName = newNodeName + "_" + System.currentTimeMillis();
      }
      
      selectedNode.setName(newNodeName);
      
      String newUri = oldUri.substring(0, oldUri.lastIndexOf("/") + 1) + newNodeName;
      
      selectedNode.setUri(newUri);
      
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
            editedApp = appParts[0] + ":" + newNodeName + ":" + appParts[2];
            newInstalledApps = installedApps.replaceAll(app, editedApp);
            space.setApp(newInstalledApps);
            spaceService.saveSpace(space, false);
            break;
          }
        }
      }
      
      dataStorage.save(pageNav);
      
      if (newUri != null)
      {
         prContext.getResponse().sendRedirect(prContext.getPortalURI() + newUri);
      }
    }
  }
  
  /**
   * gets space name from space url.
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
   * gets selected application page node
   * @return selected application page node
   * @throws Exception
   */
  public String getAppSelected() throws Exception {
    PageNode selectedNode = uiPortal.getSelectedNode();
    String[] split = selectedNode.getUri().split("/");
    return split[split.length - 1];
  }
  
  /**
   * gets image source url
   * @return image source url
   * @throws Exception
   */
  protected String getImageSource() throws Exception {
    Space space = getSpace();
    if (space == null) {
      return null;
    }
    SpaceAttachment spaceAtt = (SpaceAttachment) space.getSpaceAttachment();
    if (spaceAtt != null) {
      return "/" + getPortalName()+"/rest/jcr/" + getRepository()+ "/" + spaceAtt.getWorkspace()
              + spaceAtt.getDataPath() + "/?rnd=" + System.currentTimeMillis();
    }
    return null;
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
  
  private UIPortal getUIPortal() {
    return uiPortal;
  }
  
  /**
   * application comparator
   * @author hoatle
   *
   */
  private class ApplicationComparator implements Comparator<PageNode> {
    public int compare(PageNode pageNode1, PageNode pageNode2) {
      return pageNode1.getResolvedLabel().compareToIgnoreCase(pageNode2.getResolvedLabel());
    }
  }
  /**
   * gets current portal name
   * @return current portal name
   */
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }
  /**
   * gets current repository name
   * @return
   * @throws Exception
   */
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);    
    return rService.getCurrentRepository().getConfiguration().getName();
  }
  
  /**
   * get spaceService
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
   * Check the input name is existed or not.<br>
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
    List<PageNode> nodes = homeNode.getChildren();

    // Check in case new name is duplicate with space name
     for (PageNode node : pageNav.getNodes())
     {
        if (node.getName().equals(nodeName))
        {
           return true;
        }
     }
     
     //Check in case new name is existed
     for (PageNode node : nodes)
     {
        if (node.getName().equals(nodeName))
        {
           return true;
        }
     }
     return false;
  }
  
  /**
   * Check the input new space application name is valid or not.<br>
   * 
   * @param appName
   * 
   * @return true if input name is valid. false if not.
   */
  private boolean isValidAppName(String appName) {
    if (appName == null || appName.length() < 1)
    {
       return false;
    }
    appName = appName.trim();
    if (Character.isDigit(appName.charAt(0)) || appName.charAt(0) == '-')
    {
       return false;
    }
    for (int i = 0; i < appName.length(); i++)
    {
       char c = appName.charAt(i);
       if (Character.isLetter(c) || Character.isDigit(c) || c == '_' || c == '-' || Character.isSpaceChar(c))
       {
          continue;
       }
       return false;
    }
    return true;
  }
}