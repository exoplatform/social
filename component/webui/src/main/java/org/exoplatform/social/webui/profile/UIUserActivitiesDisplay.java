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
import java.util.Iterator;
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
  private static final String   SELECT_BOX_DISPLAY_MODE = "SelectBoxDisplayModes";
  private Object locker = new Object();

  public enum DisplayMode {
    OWNER_STATUS,
    ALL_ACTIVITIES,
    CONNECTIONS,
    MY_SPACE,
    MY_ACTIVITIES
  }

  private DisplayMode                selectedDisplayMode   = DisplayMode.ALL_ACTIVITIES;
  private boolean                   isActivityStreamOwner = false;
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
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.All_Updates"), DisplayMode.ALL_ACTIVITIES.toString()));
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.Network_Updates"), DisplayMode.CONNECTIONS.toString()));
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.Space_Updates"), DisplayMode.MY_SPACE.toString()));
    displayModes.add(new SelectItemOption<String>(resourceBundle.getString("UIUserActivitiesDisplay.label.My_Status"), DisplayMode.MY_ACTIVITIES.toString()));
    UIFormSelectBox uiFormSelectBox = new UIFormSelectBox(SELECT_BOX_DISPLAY_MODE, SELECT_BOX_DISPLAY_MODE, displayModes);
    uiFormSelectBox.setOnChange("ChangeDisplayMode");
    addChild(uiFormSelectBox);

    // TODO: init() run two time when initiation this form.
    String remoteId = Utils.getOwnerRemoteId();
    this.setOwnerName(remoteId);
    String selectedDisplayMode = lastVisitStream.get(remoteId);
    selectedDisplayMode = (selectedDisplayMode != null) ? selectedDisplayMode : DisplayMode.ALL_ACTIVITIES.name();

    //
    setSelectedDisplayMode(selectedDisplayMode);
    
  }
  
  public UIActivitiesLoader getActivitiesLoader() {
    return activitiesLoader;
  }

  public boolean isActivityStreamOwner() {
    return isActivityStreamOwner;
  }

  public void setSelectedDisplayMode(DisplayMode displayMode) {
    selectedDisplayMode = displayMode;
    getUIFormSelectBox(SELECT_BOX_DISPLAY_MODE).setValue(displayMode.name());
    try {
      init();
    } catch (Exception e) {
      LOG.error("Failed to init()");
    }
  }

  public void setSelectedDisplayMode(String selectedDisplayMode) {
    DisplayMode[] displayModes = DisplayMode.values();
    for (int i = 0; i < displayModes.length; ++i) {
      if (displayModes[i].name().equals(selectedDisplayMode)) {
        setSelectedDisplayMode(displayModes[i]);
        break;
      }
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
    case MY_ACTIVITIES :
     activitiesListAccess = activityManager.getActivitiesWithListAccess(ownerIdentity);
     activitiesLoader.setActivityListAccess(activitiesListAccess);
     break;
    case OWNER_STATUS :
      activitiesListAccess = activityManager.getActivitiesWithListAccess(ownerIdentity);
      activitiesLoader.setActivityListAccess(activitiesListAccess);
      break;
    case CONNECTIONS :
      activitiesListAccess = activityManager.getActivitiesOfConnectionsWithListAccess(ownerIdentity);
      activitiesLoader.setActivityListAccess(activitiesListAccess);
      break;
    case MY_SPACE :
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
      UIUserActivitiesDisplay uiUserActivities = event.getSource();

      //
      String selectedDisplayMode = uiUserActivities.getUIFormSelectBox(SELECT_BOX_DISPLAY_MODE).getValue();
      if (selectedDisplayMode != null) {
        lastVisitStream.put(Utils.getOwnerRemoteId(), selectedDisplayMode);
        uiUserActivities.setSelectedDisplayMode(selectedDisplayMode);
        
        UIActivitiesLoader activitiesLoader = uiUserActivities.getChild(UIActivitiesLoader.class);
        UIActivitiesContainer activitiesContainer = activitiesLoader.getChild(UIActivitiesContainer.class);
        Iterator<ExoSocialActivity> activityList = activitiesContainer.getActivityList().iterator();
        activitiesContainer.storeStreamInfosCookie(activitiesContainer.getOwnerName() + "_" + selectedDisplayMode,
                                                     activityList.hasNext() ? activityList.next().getUpdated().getTime() : null);

          event.getRequestContext().addUIComponentToUpdateByAjax(activitiesLoader);
      }
      
      //
      event.getRequestContext().addUIComponentToUpdateByAjax(uiUserActivities);
    }
  }
}
