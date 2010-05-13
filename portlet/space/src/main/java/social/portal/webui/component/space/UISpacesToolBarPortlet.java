/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
import java.util.Iterator;
import java.util.List;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * {@link UISpaceToolBarPortlet} used as a portlet displaying spaces.<br />
 * @author <a href="mailto:hanhvq@gmail.com">hanhvq</a>
 * @since Oct 7, 2009
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/portal/webui/space/UISpacesToolBarPortlet.gtmpl"
)
public class UISpacesToolBarPortlet extends UIPortletApplication {
  
  private static final String SPACE_SETTING_PORTLET = "SpaceSettingPortlet";
  
  /**
   * constructor
   * @throws Exception
   */
  public UISpacesToolBarPortlet() throws Exception {  }
  
  private SpaceService spaceService = null;
  private String userId = null;
  
  public List<PageNavigation> getSpaceNavigations() throws Exception {
    String remoteUser = getUserId();
    SpaceService spaceService = getSpaceService();
    List<Space> spaces = spaceService.getAccessibleSpaces(remoteUser);
    List<PageNavigation>  navigations = new ArrayList<PageNavigation>();
    PageNavigation spaceNavigation = null;
    for (Space space : spaces) {
      spaceNavigation = SpaceUtils.getGroupNavigation(space.getGroupId());
      if (spaceNavigation == null) continue;
      navigations.add(PageNavigationUtils.filter(spaceNavigation, remoteUser));
    }
    return navigations;
  }
  
  public boolean isRender(PageNode spaceNode, PageNode applicationNode) throws SpaceException {
	 SpaceService spaceSrv = getSpaceService();
	 String remoteUser = getUserId();
	 String spaceUrl = spaceNode.getUri();
     if (spaceUrl.contains("/")) {
       spaceUrl = spaceUrl.split("/")[0];
     }
     
     Space space = spaceSrv.getSpaceByUrl(spaceUrl);
     
	 if (spaceSrv.hasEditPermission(space, remoteUser)) return true;
	 
	 String appName = applicationNode.getName();
	 if (!appName.contains(SPACE_SETTING_PORTLET)) {
	   return true;
	 }
	 
	 return false;
  }
  
  public PageNode getSelectedPageNode() throws Exception
  {
     return Util.getUIPortal().getSelectedNode();
  }
  /**
   * gets spaceService
   * @return spaceService
   * @see SpaceService
   */
  private SpaceService getSpaceService() {
    if(spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService; 
  }
  
  /**
   * gets remote user Id
   * @return userId
   */
  private String getUserId() {
    if(userId == null) {
      userId = Util.getPortalRequestContext().getRemoteUser();
    }
    return userId;
  }
}