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
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Dec 15, 2008          
 */

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/portal/webui/space/UISpaceMenuPortlet.gtmpl"
)

public class UISpaceMenuPortlet extends UIPortletApplication {
  private SpaceService spaceService = null;
  private UIPortal uiPortal = null;
  
  public UISpaceMenuPortlet() throws  Exception { 
    uiPortal = Util.getUIPortal();
  }
  
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
   * Get space name from space url.
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
  
  private class ApplicationComparator implements Comparator<PageNode> {
    public int compare(PageNode pageNode1, PageNode pageNode2) {
      return pageNode1.getResolvedLabel().compareToIgnoreCase(pageNode2.getResolvedLabel());
    }
  }
  
  public String getAppSelected() throws Exception {
    PageNode selectedNode = uiPortal.getSelectedNode();
    String[] split = selectedNode.getUri().split("/");
    return split[split.length - 1];
  }
  
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
  
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }
  
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);    
    return rService.getCurrentRepository().getConfiguration().getName();
  }
  
  /**
   * get {@SpaceService}
   * @return spaceService
   */
  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }
}