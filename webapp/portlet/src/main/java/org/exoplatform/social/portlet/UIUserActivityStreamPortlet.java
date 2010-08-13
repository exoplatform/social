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

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * UIUserActivityStreamPortlet.java
 * </p>
 * <p>
 * Display activity composer, and user's activities.
 * </p>
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Jul 25, 2010
 * @copyright eXo SAS
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/UIUserActivityStreamPortlet.gtmpl"
)
public class UIUserActivityStreamPortlet extends UIPortletApplication {
  private String ownerName;
  private UIComposer uiComposer;
  private PopupContainer hiddenContainer;
  UIUserActivitiesDisplay uiUserActivitiesDisplay;
  /**
   * constructor
   *
   * @throws Exception
   */
  public UIUserActivityStreamPortlet() throws Exception {
    ownerName = PortalRequestContext.getCurrentInstance().getRemoteUser();
    hiddenContainer = addChild(PopupContainer.class, null, "HiddenContainer");
    uiComposer = addChild(UIComposer.class, null, null);
    uiComposer.setPostContext(PostContext.USER);
    uiComposer.setOptionContainer(hiddenContainer);
    uiUserActivitiesDisplay = addChild(UIUserActivitiesDisplay.class, null, null);
    uiUserActivitiesDisplay.setOwnerName(ownerName);
    uiComposer.setActivityDisplay(uiUserActivitiesDisplay);
  }

  /**
   * resets to reload all activities
   *
   * @throws Exception
   */
  public void refresh() throws Exception {
    uiUserActivitiesDisplay.setOwnerName(ownerName);
  }

}
