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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIDropDownControl;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Displays user's activities
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jul 30, 2010
 * @copyright eXo SAS
 */
@ComponentConfigs({
  @ComponentConfig(
                   lifecycle = UIFormLifecycle.class,
                   template = "classpath:groovy/social/webui/profile/UIUserActivitiesDisplay.gtmpl"
                 ),
  @ComponentConfig(
    type = UIDropDownControl.class, 
    id = "DisplayModesDropDown", 
    template = "system:/groovy/webui/core/UIDropDownControl.gtmpl",
    events = {
      @EventConfig(listeners = UIUserActivitiesDisplay.ChangeOptionActionListener.class)
    }
  )
})
public class UIUserActivitiesDisplay extends UIContainer {

  static private final Log      LOG = ExoLogger.getLogger(UIUserActivitiesDisplay.class);
  private static final int      ACTIVITY_PER_PAGE = 20;
  private Object locker = new Object();

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
   * @throws Exception 
   */
  public UIUserActivitiesDisplay() throws Exception {
    ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
    List<SelectItemOption<String>> displayModes = new ArrayList<SelectItemOption<String>>(4);
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.All_Updates"), DisplayMode.ALL_UPDATES.toString()));
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.Network_Updates"), DisplayMode.NETWORK_UPDATES.toString()));
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.Space_Updates"), DisplayMode.SPACE_UPDATES.toString()));
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.My_Status"), DisplayMode.MY_STATUS.toString()));
    
    UIDropDownControl uiDropDownControl = addChild(UIDropDownControl.class, "DisplayModesDropDown", null);
    uiDropDownControl.setOptions(displayModes);
    
    setSelectedMode(uiDropDownControl);
    
    addChild(uiDropDownControl);
  }

  public UIActivitiesLoader getActivitiesLoader() {
    return activitiesLoader;
  }

  public boolean isActivityStreamOwner() {
    return isActivityStreamOwner;
  }

  public void setSelectedDisplayMode(DisplayMode displayMode) {
    selectedDisplayMode = displayMode;
    
    getChild(UIDropDownControl.class).setValue(selectedDisplayMode.toString());
    
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

  public static class ChangeOptionActionListener extends EventListener<UIDropDownControl> {

     public void execute(Event<UIDropDownControl> event) throws Exception {
      UIDropDownControl uiDropDown = event.getSource();
      UIUserActivitiesDisplay uiUserActivitiesDisplay = uiDropDown.getParent();
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
    synchronized (locker) {
      removeChild(UIActivitiesLoader.class);
      activitiesLoader = addChild(UIActivitiesLoader.class, null, "UIActivitiesLoader");
    }
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
  
  private void setSelectedMode(UIDropDownControl uiDropDownControl) {
    if (selectedDisplayMode != null) {
      uiDropDownControl.setValue(selectedDisplayMode.toString());
    }
  }
}
