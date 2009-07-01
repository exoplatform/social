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
package social.portal.webui.component;

import java.util.List;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 *          hoat.le@exoplatform.com
 * Jun 23, 2009  
 */
@ComponentConfig(
  template = "app:/groovy/portal/webui/component/UIManagePendingSpaces.gtmpl",
  events = {
      @EventConfig(listeners = UIManagePendingSpaces.RevokePendingActionListener.class)
  }
)
public class UIManagePendingSpaces extends UIContainer {
  
  SpaceService spaceService = null;
  String userId = null;
  
  public UIManagePendingSpaces() throws Exception {
    
  }
  
  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }
  
  private String getUserId() {
    if (userId == null) {
      userId = Util.getPortalRequestContext().getRemoteUser();
    }
    return userId;
  }
  
  public List<Space> getPendingSpaces() throws SpaceException {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> pendingSpaces = spaceService.getPendingSpaces(userId);
    return pendingSpaces;
  }
  
  /*
   * This action is triggered when user clicks on RevokePending action
   */
  static public class RevokePendingActionListener extends EventListener<UIManagePendingSpaces> {

    @Override
    public void execute(Event<UIManagePendingSpaces> event) throws Exception {
      // TODO Auto-generated method stub
      
    }
    
  }

}
