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
import java.util.List;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.space.Space;
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
                 template = "app:/groovy/portal/webui/space/UISpaceToolBarPortlet.gtmpl"
)
public class UISpaceToolBarPortlet extends UIPortletApplication {
  
  /**
   * constructor
   * @throws Exception
   */
  public UISpaceToolBarPortlet() throws Exception {  }
  
  private SpaceService spaceService = null;
  private String userId = null;
  
  /**
   * gets all user spaces
   * @return
   * @throws Exception
   */
  @SuppressWarnings("unused")
  private List<Space> getAllUserSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> userSpaces = spaceService.getAccessibleSpaces(userId);
    return SpaceUtils.getOrderedSpaces(userSpaces);
  }
  
  
  public List<PageNavigation> getSpaceNavigations() throws Exception
  {
     String remoteUser = Util.getPortalRequestContext().getRemoteUser();
     List<PageNavigation> allNavigations = Util.getUIPortalApplication().getNavigations();
     List<PageNavigation> navigations = new ArrayList<PageNavigation>();
     SpaceService spaceSrv = getSpaceService();
     List<Space> spaces = spaceSrv.getAllSpaces();
     
     for (Space space : spaces) 
	  {
	      for (PageNavigation navigation : allNavigations)
	      {
	         if (navigation.getOwnerId().equals(space.getGroupId()))
	         {
	        	 navigations.add(PageNavigationUtils.filter(navigation, remoteUser));
	            break;
	         }
   	 }
     }
     
     return navigations;
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
    if(userId == null) 
      userId = Util.getPortalRequestContext().getRemoteUser();
    return userId;
  }
}