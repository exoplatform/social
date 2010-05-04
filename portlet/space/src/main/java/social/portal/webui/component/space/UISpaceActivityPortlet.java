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

import javax.portlet.PortletPreferences;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.application.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.social.space.impl.SpaceIdentityProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import social.portal.webui.component.UIDisplaySpaceActivities;
import social.portal.webui.component.composer.UIComposer;

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
  static private final Log LOG = ExoLogger.getLogger(UISpaceActivityPortlet.class);
  private Space space_;
  private UIDisplaySpaceActivities uiDisplaySpaceActivities_;
  //TODO hoatle need to specify reasonable characters length here
  private static final int MIN_CHARACTERS_REQUIRED = 0;
  private static final int MAX_CHARACTERS_ALLOWED = 500;
  /**
   * constructor
   */
  public UISpaceActivityPortlet() throws Exception {
    UIComposer uiComposer = addChild(UIComposer.class, null, null);
    uiComposer.setStringLengthValidator(MIN_CHARACTERS_REQUIRED, MAX_CHARACTERS_ALLOWED);
    uiDisplaySpaceActivities_ = addChild(UIDisplaySpaceActivities.class, null, null);
    space_ = getSpaceService().getSpaceByUrl(SpaceUtils.getSpaceUrl());
    uiDisplaySpaceActivities_.setSpace(space_);
  }

  
  public SpaceService getSpaceService() {
    return getApplicationComponent(SpaceService.class);
  }
  
  public Space getSpace() {
    return space_;
  }
  
  public void setSpace(Space space) {
    space_ = space;
  }
  
  /**
   * resets to reload all activities
   * @throws Exception
   */
  public void refresh() throws Exception {
    uiDisplaySpaceActivities_.setSpace(space_);
  }
  
  static public class PostMessageActionListener extends EventListener<UIComposer> {

    @Override
    public void execute(Event<UIComposer> event) throws Exception {
      UIComposer uiComposer = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApplication = requestContext.getUIApplication();
      String bodyData  = uiComposer.getBodyData();
      if (bodyData.equals("")) {
        uiApplication.addMessage(new ApplicationMessage("UIComposer.msg.error.Empty_Message", null, ApplicationMessage.ERROR));
        return;
      }
      String member = requestContext.getRemoteUser();
      UISpaceActivityPortlet uiSpaceActivityPortlet = uiComposer.getAncestorOfType(UISpaceActivityPortlet.class);
      UIDisplaySpaceActivities uiDisplaySpaceActivities = uiSpaceActivityPortlet.getChild(UIDisplaySpaceActivities.class);
      Space space = uiSpaceActivityPortlet.getSpace();
      uiComposer.reset();
      ActivityManager activityManager = uiComposer.getApplicationComponent(ActivityManager.class);
      IdentityManager identityManager = uiComposer.getApplicationComponent(IdentityManager.class);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
      Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, member);
      Activity activity = new Activity(userIdentity.getId(), SpaceService.SPACES_APP_ID, space.getName(), bodyData);
      activityManager.saveActivity(spaceIdentity, activity);
      uiDisplaySpaceActivities.setSpace(space);
    }
    
  }
}
