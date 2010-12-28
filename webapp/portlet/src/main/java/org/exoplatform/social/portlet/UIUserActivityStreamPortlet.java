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

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.webui.Utils;
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
  private static final Log LOG = ExoLogger.getLogger(UIUserActivityStreamPortlet.class);
  private String ownerName;
  private String viewerName;
  private UIComposer uiComposer;
  private PopupContainer hiddenContainer;
  private boolean composerDisplayed = false;
  UIUserActivitiesDisplay uiUserActivitiesDisplay;
  /**
   * constructor
   *
   * @throws Exception
   */
  public UIUserActivityStreamPortlet() throws Exception {
    viewerName = Utils.getViewerRemoteId();
    ownerName = Utils.getOwnerRemoteId();
    hiddenContainer = addChild(PopupContainer.class, null, "HiddenContainer");
    uiComposer = addChild(UIComposer.class, null, null);
    uiComposer.setPostContext(PostContext.USER);
    uiComposer.setOptionContainer(hiddenContainer);
    uiUserActivitiesDisplay = addChild(UIUserActivitiesDisplay.class, null, "UIUserActivitiesDisplay");
    uiComposer.setActivityDisplay(uiUserActivitiesDisplay);
  }

  public boolean isComposerDisplayed() {
    return composerDisplayed;
  }

  /**
   * resets to reload all activities
   *
   * @throws Exception
   */
  public void refresh() throws Exception {
    viewerName = Utils.getViewerRemoteId();
    ownerName = Utils.getOwnerRemoteId();
    if (viewerName.equals(ownerName)) {
      uiComposer.isActivityStreamOwner(true);
      uiComposer.setRendered(true);
    } else {
      uiComposer.isActivityStreamOwner(false);

      Relationship relationship = Utils.getRelationshipManager().get(Utils.getViewerIdentity(), Utils.getOwnerIdentity());
      if (relationship != null && (relationship.getStatus() == Relationship.Type.CONFIRMED)) {
        uiComposer.setRendered(true);
      } else {
        uiComposer.setRendered(false);
      }
    }
    uiUserActivitiesDisplay.setOwnerName(ownerName);
  }

  /**
   * Checks if title is displayed or not. This title is only displayed when
   * matching url: /activities
   *
   * @return
   */
  public final boolean isTitleDisplayed() {
    final String activities = "/activities";
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    HttpServletRequest request = portalRequestContext.getRequest();
    String str = request.getRequestURL().toString();
    return str.contains(activities);
  }

}
