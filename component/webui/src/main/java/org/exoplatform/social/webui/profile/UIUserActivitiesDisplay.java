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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.Validate;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.social.webui.activity.UIActivitiesLoader;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Displays user's activities
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jul 30, 2010
 * @copyright eXo SAS
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/social/webui/profile/UIUserActivitiesDisplay.gtmpl",
  events = {
    @EventConfig(listeners = UIUserActivitiesDisplay.ChangeDisplayModeActionListener.class)
  }
)
public class UIUserActivitiesDisplay extends UIForm {

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

  private DisplayMode                selectedDisplayMode   = DisplayMode.ALL_UPDATES;
  private boolean                    isActivityStreamOwner = false;
  private UIActivitiesLoader         activitiesLoader;
  private String                     ownerName;
  private String                     viewerName;

  /** Store user's last visit stream. */
  private static Map<String, String> lastVisitStream = new HashMap<String, String>();

  /**
   * Default constructor.
   * 
   * @throws Exception 
   */
  public UIUserActivitiesDisplay() throws Exception {
    //
    ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
    List<SelectItemOption<String>> displayModes = new ArrayList<SelectItemOption<String>>(4);
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.All_Updates"), DisplayMode.ALL_UPDATES.toString()));
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.Network_Updates"), DisplayMode.NETWORK_UPDATES.toString()));
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.Space_Updates"), DisplayMode.SPACE_UPDATES.toString()));
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.My_Status"), DisplayMode.MY_STATUS.toString()));
    UIFormSelectBox uiFormSelectBox = new UIFormSelectBox("SelectBoxDisplayModes", null, displayModes);
    uiFormSelectBox.setOnChange("ChangeDisplayMode");
    setSelectedDisplayMode(uiFormSelectBox);
    addChild(uiFormSelectBox);

    //
    this.setOwnerName(Utils.getOwnerRemoteId());
    String selectedDisplayMode = this.getChild(UIFormSelectBox.class).getValue();
    if (DisplayMode.ALL_UPDATES.toString().equals(selectedDisplayMode)) {
      this.setSelectedDisplayMode(DisplayMode.ALL_UPDATES);
    } else if (DisplayMode.MY_STATUS.toString().equals(selectedDisplayMode)) {
      this.setSelectedDisplayMode(DisplayMode.MY_STATUS);
    } else if (DisplayMode.SPACE_UPDATES.toString().equals(selectedDisplayMode)) {
      this.setSelectedDisplayMode(DisplayMode.SPACE_UPDATES);
    } else {
      this.setSelectedDisplayMode(DisplayMode.NETWORK_UPDATES);
    }
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
    activitiesLoader.setSelectedDisplayMode(selectedDisplayMode.toString());
    
    //
    Identity ownerIdentity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, ownerName, false);
    ActivityManager activityManager = Utils.getActivityManager();
    ListAccess<ExoSocialActivity> activitiesListAccess = null;
    
    switch (this.selectedDisplayMode) {
    case MY_STATUS :
     activitiesListAccess = activityManager.getActivitiesWithListAccess(ownerIdentity);
     activitiesLoader.setActivityListAccess(activitiesListAccess);
     break;
    case OWNER_STATUS :
      activitiesListAccess = activityManager.getActivitiesWithListAccess(ownerIdentity);
      activitiesLoader.setActivityListAccess(activitiesListAccess);
      break;
    case NETWORK_UPDATES :
      activitiesListAccess = activityManager.getActivitiesOfConnectionsWithListAccess(ownerIdentity);
      activitiesLoader.setActivityListAccess(activitiesListAccess);
      break;
    case SPACE_UPDATES :
      activitiesListAccess = activityManager.getActivitiesOfUserSpacesWithListAccess(ownerIdentity);
      activitiesLoader.setActivityListAccess(activitiesListAccess);
      break;
    default :
      activitiesListAccess = activityManager.getActivityFeedWithListAccess(ownerIdentity);
      activitiesLoader.setActivityListAccess(activitiesListAccess);
      break;
  }
   
    //
    activitiesLoader.init();
  }

  public static class ChangeDisplayModeActionListener extends EventListener<UIUserActivitiesDisplay> {
    @Override
    public void execute(Event<UIUserActivitiesDisplay> event) throws Exception {
      //
      UIUserActivitiesDisplay uiUserActivitiesDisplay = event.getSource();
      UIFormSelectBox uiFormSelectBox = uiUserActivitiesDisplay.getChild(UIFormSelectBox.class);

      //
      String selectedDisplayMode = uiFormSelectBox.getValue();
      lastVisitStream.put(Utils.getOwnerRemoteId(), selectedDisplayMode);
      if (DisplayMode.ALL_UPDATES.toString().equals(selectedDisplayMode)) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.ALL_UPDATES);
      } else if (DisplayMode.MY_STATUS.toString().equals(selectedDisplayMode)) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.MY_STATUS);
      } else if (DisplayMode.SPACE_UPDATES.toString().equals(selectedDisplayMode)) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.SPACE_UPDATES);
      } else {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.NETWORK_UPDATES);
      }

      UIActivitiesContainer uiActivitiesContainer = uiUserActivitiesDisplay.getChild(UIActivitiesLoader.class).getChild(UIActivitiesContainer.class);
      if (selectedDisplayMode != null) {
        uiActivitiesContainer.storeStreamInfosCookie(uiActivitiesContainer.getOwnerName() + "_" + selectedDisplayMode,
                              uiActivitiesContainer.getActivityList().size() > 0 ? uiActivitiesContainer.getActivityList().get(0).getUpdated().getTime() : null);
      }
      
      //
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUserActivitiesDisplay.getChild(UIActivitiesLoader.class));
    }
  }

  private void setSelectedDisplayMode(UIFormSelectBox uiFormSelectBox) {
    String selectedDisplayMode = lastVisitStream.get(Utils.getOwnerRemoteId());
    if (selectedDisplayMode != null) {
      uiFormSelectBox.setValue(selectedDisplayMode);
    }
  }
}
