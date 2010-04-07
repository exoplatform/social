/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceIdentityProvider;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import social.portal.webui.component.UIComposer;
import social.portal.webui.component.UIDisplaySpaceActivities;

/**
 * UISpaceActivityPortlet.java
 * <p>
 * Displaying space activities and its member's posts
 *
 * @author    <a href="http://hoatle.net">hoatle</a>
 * @since 	  Apr 6, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/portal/webui/space/UISpaceActivityPortlet.gtmpl",
  events = {
    @EventConfig (listeners = UISpaceActivityPortlet.PostMessageActionListener.class)
  }
) 
public class UISpaceActivityPortlet extends UIPortletApplication {

  private Space space_;
  /**
   * constructor
   */
  public UISpaceActivityPortlet() throws Exception {
    addChild(UIComposer.class, null, null);
    UIDisplaySpaceActivities uiDisplaySpaceActivities = addChild(UIDisplaySpaceActivities.class, null, null);
    space_ = getSpaceService().getSpaceByUrl(SpaceUtils.getSpaceUrl());
    uiDisplaySpaceActivities.setSpace(space_);
    
  }
  
  public SpaceService getSpaceService() {
    return getApplicationComponent(SpaceService.class);
  }
  
  public Space getSpace() {
    return space_;
  }
  
  static public class PostMessageActionListener extends EventListener<UIComposer> {

    @Override
    public void execute(Event<UIComposer> event) throws Exception {
      UIComposer uiComposer = event.getSource();
      String message = uiComposer.getMessage();
      if (message == null || message.length() == 0) {
        return;
      }
      UISpaceActivityPortlet uiPortlet = uiComposer.getAncestorOfType(UISpaceActivityPortlet.class);
      
      Space space = uiPortlet.getSpace();
      uiComposer.reset();
      ActivityManager activityManager = uiComposer.getApplicationComponent(ActivityManager.class);
      IdentityManager identityManager = uiComposer.getApplicationComponent(IdentityManager.class);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
      
      String member = event.getRequestContext().getRemoteUser();
      message = member + " posted '" + message + "'";
      activityManager.recordActivity(spaceIdentity.getId(), SpaceService.SPACES_APP_ID, space.getName(), message);
    }
    
  }
}
