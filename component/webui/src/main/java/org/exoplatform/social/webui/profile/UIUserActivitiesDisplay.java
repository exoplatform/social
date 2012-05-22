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
package org.exoplatform.social.webui.profile;

import org.apache.commons.lang.Validate;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.activity.UIActivitiesLoader;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Displays user's activities
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jul 30, 2010
 * @copyright eXo SAS
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/profile/UIUserActivitiesDisplay.gtmpl",
  events = {
    @EventConfig(listeners = UIUserActivitiesDisplay.ChangeDisplayModeActionListener.class)
  }
)
public class UIUserActivitiesDisplay extends UIContainer {

  static private final Log      LOG = ExoLogger.getLogger(UIUserActivitiesDisplay.class);
  private static final int      ACTIVITY_PER_PAGE = 20;


  public enum DisplayMode {
    OWNER_STATUS,
    ALL_UPDATES,
    NETWORK_UPDATES,
    SPACE_UPDATES,
    MY_STATUS
  }
  private DisplayMode selectedDisplayMode = DisplayMode.ALL_UPDATES;
  private UIActivitiesLoader activitiesLoader;
  private String                ownerName;
  private String                viewerName;
  private boolean               isActivityStreamOwner = false;

  /**
   * constructor
   */
  public UIUserActivitiesDisplay() {

  }

  public UIActivitiesLoader getActivitiesLoader() {
    return activitiesLoader;
  }

  public boolean isActivityStreamOwner() {
    return isActivityStreamOwner;
  }

  public void setSelectedDisplayMode(DisplayMode displayMode) {
    selectedDisplayMode = displayMode;
    try {
      init();
    } catch (Exception e) {
      LOG.error("Failed to init()");
    }
  }

  public DisplayMode getSelectedDisplayMode() {
    return selectedDisplayMode;
  }
  /**
   * sets activity stream owner (user remote Id)
   *
   * @param ownerName
   * @throws Exception
   */
  public void setOwnerName(String ownerName) throws Exception {
    this.ownerName = ownerName;
    viewerName = PortalRequestContext.getCurrentInstance().getRemoteUser();
    isActivityStreamOwner = viewerName.equals(ownerName);
    if (!isActivityStreamOwner) {
      selectedDisplayMode = DisplayMode.OWNER_STATUS;
    }
    init();
  }

  public String getOwnerName() {
    return ownerName;
  }

  public static class ChangeDisplayModeActionListener extends EventListener<UIUserActivitiesDisplay> {
    @Override
    public void execute(Event<UIUserActivitiesDisplay> event) throws Exception {
      UIUserActivitiesDisplay uiUserActivitiesDisplay = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String selectedDisplayMode = requestContext.getRequestParameter(OBJECTID);

      if (selectedDisplayMode.equals(DisplayMode.ALL_UPDATES.toString())) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.ALL_UPDATES);
      } else if (selectedDisplayMode.equals(DisplayMode.MY_STATUS.toString())) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.MY_STATUS);
      } else if (selectedDisplayMode.equals(DisplayMode.SPACE_UPDATES.toString())) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.SPACE_UPDATES);
      } else {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.NETWORK_UPDATES);
      }
      requestContext.addUIComponentToUpdateByAjax(uiUserActivitiesDisplay);
    }

  }
  /**
   * initialize
   *
   * @throws Exception
   */
  public void init() throws Exception {
    Validate.notNull(ownerName, "ownerName must not be null.");
    Validate.notNull(viewerName, "viewerName must not be null.");
    //
    removeChild(UIActivitiesLoader.class);
    activitiesLoader = addChild(UIActivitiesLoader.class, null, "UIActivitiesLoader");
    activitiesLoader.setPostContext(PostContext.USER);
    activitiesLoader.setLoadingCapacity(ACTIVITY_PER_PAGE);
    activitiesLoader.setOwnerName(ownerName);

    //
    Identity ownerIdentity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, ownerName, false);
    ActivityManager activityManager = Utils.getActivityManager();

    switch (this.selectedDisplayMode) {
      case MY_STATUS :
       activitiesLoader.setActivityListAccess(activityManager.getActivitiesWithListAccess(ownerIdentity));
       break;
      case OWNER_STATUS :
        activitiesLoader.setActivityListAccess(activityManager.getActivitiesWithListAccess(ownerIdentity));
        break;
      case NETWORK_UPDATES :
        activitiesLoader.setActivityListAccess(activityManager.getActivitiesOfConnectionsWithListAccess(ownerIdentity));
        break;
      case SPACE_UPDATES :
        activitiesLoader.setActivityListAccess(activityManager.getActivitiesOfUserSpacesWithListAccess(ownerIdentity));
        break;
      default :
        activitiesLoader.setActivityListAccess(activityManager.getActivityFeedWithListAccess(ownerIdentity));
        break;
    }
   
    //
    activitiesLoader.init();
  }
}
