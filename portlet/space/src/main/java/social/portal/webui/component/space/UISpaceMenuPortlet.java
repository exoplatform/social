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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceAttachment;
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
  
  /** Stores SpaceService object. */
  private SpaceService spaceService = null;
  
  /** Stores UIPortal object. */
  private UIPortal uiPortal = null;
  
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
    Space space = spaceSrc.getSpaceByUrl(spaceUrl);
    
    UserPortalConfigService dataService = getApplicationComponent(UserPortalConfigService.class);
    PageNavigation pageNav = dataService.getPageNavigation(PortalConfig.GROUP_TYPE, space.getGroupId());
    
    PageNode homeNode = pageNav.getNode(spaceUrl);
    String userId = Util.getPortalRequestContext().getRemoteUser();       
    List<PageNode> list = homeNode.getChildren();

    PageNode pageNode = null;
    StringBuffer sb = new StringBuffer("SpaceSettingPortlet");
    String spaceSettingAppName = sb.insert(0, SpaceUtils.getSpaceUrl()).toString();
    
    for(PageNode node:list){
      if(node.getName().equals(spaceSettingAppName)){
        pageNode = node;
        break;
      }
    }
    if(pageNode != null) list.remove(pageNode); 
    Collections.sort(list, new ApplicationComparator());
    if(spaceSrc.hasEditPermission(space, userId) && pageNode != null) list.add(pageNode);
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
      UserPortalConfigService dataService = spaceMenu.getApplicationComponent(UserPortalConfigService.class);
      SpaceService spaceService = spaceMenu.getApplicationComponent(SpaceService.class);
      String spaceUrl = SpaceUtils.getSpaceUrl();
      Space space = spaceService.getSpaceByUrl(spaceUrl);
      
      PageNode selectedNode = uiPortal.getSelectedNode();
      PageNavigation selectedNavigation = uiPortal.getSelectedNavigation();
      
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
      
      PageNavigation pageNav = dataService.getPageNavigation(PortalConfig.GROUP_TYPE, space.getGroupId());
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
        appParts = app.split(":");
        if (appParts[1].equals(oldName)) {
          editedApp = appParts[0] + ":" + newNodeName + ":" + appParts[2];
          newInstalledApps = installedApps.replaceAll(app, editedApp);
          space.setApp(newInstalledApps);
          spaceService.saveSpace(space, false);
          break;
        }
      }
      
      dataService.update(selectedNavigation);

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
    String spaceUrl = SpaceUtils.getSpaceUrl();
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    Space space = spaceSrc.getSpaceByUrl(spaceUrl);
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
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceByUrl(SpaceUtils.getSpaceUrl());
    SpaceAttachment spaceAtt = (SpaceAttachment) space.getSpaceAttachment();
    if (spaceAtt != null) {
      return "/" + getPortalName()+"/rest/jcr/" + getRepository()+ "/" + spaceAtt.getWorkspace()
              + spaceAtt.getDataPath() + "/?rnd=" + System.currentTimeMillis();
    }
    return null;
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
    if (appName == null || appName.length() < 3)
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