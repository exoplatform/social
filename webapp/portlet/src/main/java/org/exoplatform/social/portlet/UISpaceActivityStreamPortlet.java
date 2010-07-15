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
package org.exoplatform.social.portlet;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.space.UISpaceActivitiesDisplay;
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
 * UISpaceActivityPortlet.java
 * <p/>
 * Displaying space activities and its member's posts
 *
 * @author <a href="http://hoatle.net">hoatle</a>
 * @copyright eXo Platform SAS
 * @since Apr 6, 2010
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/UISpaceActivityStreamPortlet.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceActivityStreamPortlet.PostMessageActionListener.class)
  }
)
public class UISpaceActivityStreamPortlet extends UIPortletApplication {
  static private final Log         LOG                     = ExoLogger.getLogger(UISpaceActivityStreamPortlet.class);

  private Space                    space_;

  private UISpaceActivitiesDisplay uiSpaceActivitiesDisplay_;

  // TODO hoatle need to specify reasonable characters length here
  private static final int         MIN_CHARACTERS_REQUIRED = 0;

  private static final int         MAX_CHARACTERS_ALLOWED  = 500;

  /**
   * constructor
   */
  public UISpaceActivityStreamPortlet() throws Exception {
    UIComposer uiComposer = addChild(UIComposer.class, null, null);
    uiComposer.setStringLengthValidator(MIN_CHARACTERS_REQUIRED, MAX_CHARACTERS_ALLOWED);
    uiSpaceActivitiesDisplay_ = addChild(UISpaceActivitiesDisplay.class, null, null);
    space_ = getSpaceService().getSpaceByUrl(SpaceUtils.getSpaceUrl());
    uiSpaceActivitiesDisplay_.setSpace(space_);
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
   *
   * @throws Exception
   */
  public void refresh() throws Exception {
    uiSpaceActivitiesDisplay_.setSpace(space_);
  }

  static public class PostMessageActionListener extends EventListener<UIComposer> {

    @Override
    public void execute(Event<UIComposer> event) throws Exception {
      UIComposer uiComposer = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApplication = requestContext.getUIApplication();
      String titleData = uiComposer.getTitleData();
      if (titleData.equals("")) {
        uiApplication.addMessage(new ApplicationMessage("UIComposer.msg.error.Empty_Message",
                                                        null,
                                                        ApplicationMessage.ERROR));
        return;
      }
      String member = requestContext.getRemoteUser();
      UISpaceActivityStreamPortlet uiSpaceActivityPortlet = uiComposer.getAncestorOfType(UISpaceActivityStreamPortlet.class);
      UISpaceActivitiesDisplay uiSpaceActivitiesDisplay = uiSpaceActivityPortlet.getChild(UISpaceActivitiesDisplay.class);
      Space space = uiSpaceActivityPortlet.getSpace();
      uiComposer.reset();
      ActivityManager activityManager = uiComposer.getApplicationComponent(ActivityManager.class);
      IdentityManager identityManager = uiComposer.getApplicationComponent(IdentityManager.class);
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   space.getId(),
                                                                   false);
      Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                  member);
      Activity activity = new Activity(userIdentity.getId(),
                                       SpaceService.SPACES_APP_ID,
                                       titleData,
                                       null);
      activityManager.saveActivity(spaceIdentity, activity);
      uiSpaceActivitiesDisplay.setSpace(space);
    }
  }
}
