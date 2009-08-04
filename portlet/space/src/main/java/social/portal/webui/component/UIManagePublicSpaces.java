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
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * UIManagePublicSpaces: list all spaces where user can request to join. 
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 *          hoat.le@exoplatform.com
 * Jun 23, 2009  
 */
@ComponentConfig(
  template = "app:/groovy/portal/webui/component/UIManagePublicSpaces.gtmpl",
  events = {
      @EventConfig(listeners = UIManagePublicSpaces.RequestJoinActionListener.class)
  }
)
public class UIManagePublicSpaces extends UIContainer {
  static private final String MSG_ERROR_REQUEST_JOIN = "UIManagePublicSpaces.msg.error_request_join"; 
  static private final String MSG_REQUEST_JOIN_SUCCESS = "UIManagePublicSpaces.msg.request_join_success";
  private SpaceService spaceService = null;
  private String userId = null;
  
  private String getUserId() {
    if(userId == null) userId = Util.getPortalRequestContext().getRemoteUser();
    return userId;
  }
  
  private SpaceService getSpaceService() {
    if(spaceService == null) spaceService = getApplicationComponent(SpaceService.class);
    return spaceService;
  }

  public List<Space> getPublicSpaces() throws SpaceException {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> publicSpaces = spaceService.getPublicSpaces(userId);
    return publicSpaces;
  }
  
  /**
   * Class listener for request to join space action
   */
  static public class RequestJoinActionListener extends EventListener<UIManagePublicSpaces> {
    public void execute(Event<UIManagePublicSpaces> event) throws Exception {
     UIManagePublicSpaces uiPublicSpaces = event.getSource();
     WebuiRequestContext ctx = event.getRequestContext();
     UIApplication uiApp = ctx.getUIApplication();
     SpaceService spaceService = uiPublicSpaces.getSpaceService();
     String spaceId = ctx.getRequestParameter(OBJECTID);
     String userId = uiPublicSpaces.getUserId();
     try {
       spaceService.requestJoin(spaceId, userId);
     } catch(SpaceException se) {
       uiApp.addMessage(new ApplicationMessage(MSG_ERROR_REQUEST_JOIN, null, ApplicationMessage.ERROR));
       return;
     }
     uiApp.addMessage(new ApplicationMessage(MSG_REQUEST_JOIN_SUCCESS, null, ApplicationMessage.INFO));
    }
  }
  
}  