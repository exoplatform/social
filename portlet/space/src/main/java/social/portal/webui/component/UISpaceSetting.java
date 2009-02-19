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
package social.portal.webui.component;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UITabPane;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 12, 2008          
 */

@ComponentConfig(
    template =  "app:/groovy/portal/webui/uiform/UISpaceSetting.gtmpl"
)
public class UISpaceSetting extends UIContainer {

  private Space space;
  
  public UISpaceSetting() throws Exception {
    UITabPane uiTabPane = addChild(UITabPane.class, null, null);
    uiTabPane.addChild(UISpaceInfo.class, null, null);
    uiTabPane.addChild(UISpaceMember.class, null, null);
    uiTabPane.addChild(UISpaceApplication.class, null, null);
    uiTabPane.addChild(UISpacePermission.class, null, null);
    uiTabPane.setSelectedTab(1);
  }
  
  public void setValues(Space space) throws Exception {
    UISpaceInfo uiSpaceInfo = getChild(UITabPane.class).getChild(UISpaceInfo.class);
    uiSpaceInfo.setValue(space);
    UISpaceMember uiSpaceMember = getChild(UITabPane.class).getChild(UISpaceMember.class);
    uiSpaceMember.setValue(space.getId());
    UISpaceApplication uiSpaceApplication = getChild(UITabPane.class).getChild(UISpaceApplication.class);
    uiSpaceApplication.setValue(space);
    UISpacePermission uiSpacePermission = getChild(UITabPane.class).getChild(UISpacePermission.class);
    uiSpacePermission.setValue(space);
    this.space = space;
  }
  
  public String getSpaceName() {
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);
    try {
      return spaceSrc.getSpaceById(space.getId()).getName();
    } catch (SpaceException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public boolean isLeader() throws Exception{    
    SpaceService spaceSrc = getApplicationComponent(SpaceService.class);    
    String userId = Util.getPortalRequestContext().getRemoteUser(); 
    if(spaceSrc.isLeader(space, userId)) {                       
      return true;      
    } else {
      return false;
    }
  }
  
}