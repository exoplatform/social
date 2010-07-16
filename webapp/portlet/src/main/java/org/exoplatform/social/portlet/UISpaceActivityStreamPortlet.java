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
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.space.UISpaceActivitiesDisplay;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

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
  template = "app:/groovy/social/portlet/UISpaceActivityStreamPortlet.gtmpl"
)
public class UISpaceActivityStreamPortlet extends UIPortletApplication {
  static private final Log         LOG                     = ExoLogger.getLogger(UISpaceActivityStreamPortlet.class);

  private Space                    space_;

  private UISpaceActivitiesDisplay uiDisplaySpaceActivities_;

  // TODO hoatle need to specify reasonable characters length here
  private static final int         MIN_CHARACTERS_REQUIRED = 0;

  private static final int         MAX_CHARACTERS_ALLOWED  = 500;

  /**
   * constructor
   */
  public UISpaceActivityStreamPortlet() throws Exception {
    UIComposer uiComposer = addChild(UIComposer.class, null, null);
    uiComposer.setPostContext(UIComposer.PostContext.SPACE);

//    uiComposer.setStringLengthValidator(MIN_CHARACTERS_REQUIRED, MAX_CHARACTERS_ALLOWED);
    uiDisplaySpaceActivities_ = addChild(UISpaceActivitiesDisplay.class, null, null);
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
   *
   * @throws Exception
   */
  public void refresh() throws Exception {
    uiDisplaySpaceActivities_.setSpace(space_);
  }
}