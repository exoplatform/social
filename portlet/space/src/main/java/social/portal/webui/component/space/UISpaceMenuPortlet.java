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
import java.util.List;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.space.Space;
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
  public UISpaceMenuPortlet() throws  Exception { 
  }
  
  public List<PageNode> getApps() throws Exception {
    String spaceUrl = SpaceUtils.getSpaceUrl();
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    Space space = spaceSrc.getSpaceByUrl(spaceUrl);
    String spaceName = space.getShortName();
    
    UserPortalConfigService dataService = getApplicationComponent(UserPortalConfigService.class);
    PageNavigation pageNav = dataService.getPageNavigation(PortalConfig.GROUP_TYPE, "spaces/" + spaceName);
    
    PageNode homeNode = pageNav.getNode(spaceName);
    String userId = Util.getPortalRequestContext().getRemoteUser();       
    List<PageNode> list = homeNode.getChildren();
    List<PageNode> listAppforMember = new ArrayList<PageNode>();
    if(!spaceSrc.isLeader(space, userId)) {         
      list = homeNode.getChildren();    
      for(PageNode node:list){
        if(!node.getName().equals("SpaceSettingPortlet")){
          listAppforMember.add(node);
        }
      }           
      return listAppforMember;
    }    
    if(list == null) list = new ArrayList<PageNode>();
    return list;
  }
}