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
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.ActivitiesRealtimeListAccess;
import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter;
import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter.ActivityFilterType;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.webui.URLUtils;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.social.webui.activity.UIActivitiesLoader;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIDropDownControl;
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
                   template = "war:/groovy/social/webui/profile/UIUserActivitiesDisplay.gtmpl",
                   events = {
                       @EventConfig(listeners = UIUserActivitiesDisplay.RefreshStreamActionListener.class)
                   }
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
  private static final String   SLASH = "/";
  public static final String ACTIVITY_STREAM_VISITED_PREFIX_COOKIED = "exo_social_activity_stream_%s_visited_%s";
  
  private Object locker = new Object();
  private boolean notChangedMode;
  private boolean postActivity;
  private int numberOfUpdatedActivities;
  
  public enum DisplayMode {
    OWNER_STATUS,
    ALL_ACTIVITIES,
    CONNECTIONS,
    MY_SPACE,
    MY_ACTIVITIES,
    POSTER_ACTIVITIES
  }

  private DisplayMode                selectedDisplayMode   = DisplayMode.ALL_ACTIVITIES;
  private boolean                   isActivityStreamOwner = false;
  private UIActivitiesLoader         activitiesLoader;
  private String                     ownerName;
  private String                     viewerName;

  /**
   * constructor
   * @throws Exception 
   */
  public UIUserActivitiesDisplay() throws Exception {
    List<SelectItemOption<String>> displayModes = new ArrayList<SelectItemOption<String>>(4);
    displayModes.add(new SelectItemOption<String>("All_Updates", DisplayMode.ALL_ACTIVITIES.toString()));
    displayModes.add(new SelectItemOption<String>("Space_Updates", DisplayMode.MY_SPACE.toString()));
    displayModes.add(new SelectItemOption<String>("Network_Updates", DisplayMode.CONNECTIONS.toString()));
    displayModes.add(new SelectItemOption<String>("My_Status", DisplayMode.MY_ACTIVITIES.toString()));
    
    UIDropDownControl uiDropDownControl = addChild(UIDropDownControl.class, "DisplayModesDropDown", null);
    uiDropDownControl.setOptions(displayModes);
    
    setSelectedMode(uiDropDownControl);
    
    addChild(uiDropDownControl);

    // TODO: init() run two time when initiation this form.
    //String remoteId = Utils.getOwnerRemoteId();
    //this.setOwnerName(remoteId);
    String selectedDisplayMode = Utils.getCookies(String.format(Utils.ACTIVITY_STREAM_TAB_SELECTED_COOKIED, Utils.getViewerRemoteId()));
    selectedDisplayMode = (selectedDisplayMode != null) ? selectedDisplayMode : DisplayMode.ALL_ACTIVITIES.name();

    //
    setSelectedDisplayMode(selectedDisplayMode);
    
    // set lastUpdatedNumber after init() method invoked inside setSelectedDisplayMode() method
    //int numberOfUpdates = this.getNumberOfUpdatedActivities();
    //setLastUpdatedNum(selectedDisplayMode.toString(), "" + numberOfUpdates);
  }

  public UIActivitiesLoader getActivitiesLoader() {
    return activitiesLoader;
  }

  public boolean isActivityStreamOwner() {
    return isActivityStreamOwner;
  }

  public void setNumberOfUpdatedActivities(int numberOfUpdatedActivities) {
    this.numberOfUpdatedActivities = numberOfUpdatedActivities;
  }

  public int getNumberOfUpdatedActivities() {
    return numberOfUpdatedActivities;
  }
  
  public void setSelectedDisplayMode(DisplayMode displayMode) {
    selectedDisplayMode = displayMode;
    getChild(UIDropDownControl.class).setValue(selectedDisplayMode.toString());
    try {
      //init();
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
  
  public String getCookiesKey(String displayMode) {
    return String.format(ACTIVITY_STREAM_VISITED_PREFIX_COOKIED, displayMode, Utils.getViewerRemoteId());
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
    
    //
    //int numberOfUpdates = this.getNumberOfUpdatedActivities();
    //setLastUpdatedNum(selectedDisplayMode.toString(), "" + numberOfUpdates);
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
    //
    String activityId = Utils.getActivityID();
    if (activityId != null && activityId.length() > 0) {
      activitiesLoader.setPostContext(PostContext.SINGLE);
    } else {
      activitiesLoader.setPostContext(PostContext.USER);
    }   
    
    // Check if current display page is My Activity Stream
    String currentUserName = URLUtils.getCurrentUser();
    if (currentUserName != null) {
      selectedDisplayMode = DisplayMode.OWNER_STATUS;
    }
    
    activitiesLoader.setLoadingCapacity(ACTIVITY_PER_PAGE);
    activitiesLoader.setOwnerName(ownerName);
    activitiesLoader.setSelectedDisplayMode(selectedDisplayMode.toString());
    
    //
//    UIActivitiesContainer activitiesContainer = activitiesLoader.getChild(UIActivitiesContainer.class);
    
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
    	if (isActivityStreamOwner == false) {
    	  Identity viewerIdentity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, viewerName, false);
        activitiesListAccess = activityManager.getActivitiesWithListAccess(ownerIdentity, viewerIdentity);
    	  activitiesLoader.setActivityListAccess(activitiesListAccess);
    	} else {
    	  activitiesListAccess = activityManager.getActivitiesWithListAccess(ownerIdentity);
    	  activitiesLoader.setActivityListAccess(activitiesListAccess);
    	}
      
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
    String lastVisitedModeCookieKey = String.format(Utils.ACTIVITY_STREAM_TAB_SELECTED_COOKIED,
                                                    Utils.getViewerRemoteId());
    String lastVisitedMode = Utils.getCookies(lastVisitedModeCookieKey);
    
    this.notChangedMode = lastVisitedMode == null ? true : this.selectedDisplayMode.toString().equals(lastVisitedMode.trim());   

    //setNumberOfUpdatedActivities(getActivigetActivitiesUpdatedNumtiesUpdatedNum(notChangedMode));
    setNumberOfUpdatedActivities(0);
    
    //
    activitiesLoader.init();
  }
  
  private void setSelectedMode(UIDropDownControl uiDropDownControl) {
    if (selectedDisplayMode != null) {
      uiDropDownControl.setValue(selectedDisplayMode.toString());
    }
  }

  public void setChangedMode(boolean changedMode) {
    this.notChangedMode = changedMode;
  }

  protected long getCurrentServerTime() {
    return Calendar.getInstance().getTimeInMillis();
  }
  
  protected String getSitePath() {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    String siteName = portalRequestContext.getSiteName();
    SiteType siteType = portalRequestContext.getSiteType();
    return SLASH + siteType.getName() + SLASH + siteName + SLASH;
  }
  
  protected boolean hasActivities() {
    UIActivitiesLoader uiActivitiesLoader = getChild(UIActivitiesLoader.class);
    UIActivitiesContainer activitiesContainer = uiActivitiesLoader.getChild(UIActivitiesContainer.class);
    return activitiesContainer.getChildren().size() > 0; 
  }
  
  public static class ChangeOptionActionListener extends EventListener<UIDropDownControl> {

    public void execute(Event<UIDropDownControl> event) throws Exception {
     UIDropDownControl uiDropDown = event.getSource();
     UIUserActivitiesDisplay uiUserActivities = uiDropDown.getParent();
     WebuiRequestContext requestContext = event.getRequestContext();
     String selectedDisplayMode = requestContext.getRequestParameter(OBJECTID);

     if (selectedDisplayMode.equals(DisplayMode.ALL_ACTIVITIES.toString())) {
       uiUserActivities.setSelectedDisplayMode(DisplayMode.ALL_ACTIVITIES);
     } else if (selectedDisplayMode.equals(DisplayMode.MY_ACTIVITIES.toString())) {
       uiUserActivities.setSelectedDisplayMode(DisplayMode.MY_ACTIVITIES);
     } else if (selectedDisplayMode.equals(DisplayMode.MY_SPACE.toString())) {
       uiUserActivities.setSelectedDisplayMode(DisplayMode.MY_SPACE);
     } else {
       uiUserActivities.setSelectedDisplayMode(DisplayMode.CONNECTIONS);
     }
     
     if (selectedDisplayMode != null) {
       uiUserActivities.setSelectedDisplayMode(selectedDisplayMode);
       uiUserActivities.init();
       
       uiUserActivities.setChangedMode(false);
       
       //int numberOfUpdates = uiUserActivities.getNumberOfUpdatedActivities();
       
       //
       event.getRequestContext().getJavascriptManager()
       .require("SHARED/social-ui-activity-updates", "activityUpdates").addScripts("activityUpdates.resetCookie('" + String.format(Utils.ACTIVITY_STREAM_TAB_SELECTED_COOKIED, Utils.getViewerRemoteId()) + "','" + selectedDisplayMode + "');");
//       
//       event.getRequestContext().getJavascriptManager()
//       .require("SHARED/social-ui-activity-updates", "activityUpdates").addScripts("activityUpdates.resetCookie('" + String.format(Utils.LAST_UPDATED_ACTIVITIES_NUM, selectedDisplayMode, Utils.getViewerRemoteId()) + "','" + numberOfUpdates + "');");

     }
     
     requestContext.addUIComponentToUpdateByAjax(uiUserActivities);
     
     Utils.resizeHomePage();
   }
 }
  
  public static class RefreshStreamActionListener extends EventListener<UIUserActivitiesDisplay> {
    public void execute(Event<UIUserActivitiesDisplay> event) throws Exception {
     UIUserActivitiesDisplay uiUserActivities = event.getSource();
     uiUserActivities.init();
     event.getRequestContext().addUIComponentToUpdateByAjax(uiUserActivities);
     
     Utils.resizeHomePage();
   }
 }
  
  private int getActivitiesUpdatedNum(boolean hasRefresh) {
    if (this.postActivity) {
      resetCookies();
      this.postActivity = false;
      
      return 0;
    }

    //
    UIActivitiesLoader activitiesLoader = getChild(UIActivitiesLoader.class);
    ActivitiesRealtimeListAccess activitiesListAccess = (ActivitiesRealtimeListAccess) activitiesLoader.getActivityListAccess();
    
    String mode = DisplayMode.ALL_ACTIVITIES.toString();
    ActivityFilterType.ACTIVITY_FEED
                    .oldFromSinceTime(Utils.getLastVisited(Utils.OLD_FROM, mode))
                    .fromSinceTime(Utils.getLastVisited(Utils.FROM, mode))
                    .toSinceTime(Utils.getLastVisited(Utils.TO, mode)).lastNumberOfUpdated(getLastUpdatedNum(mode));
    
    //
    mode = DisplayMode.CONNECTIONS.toString();
    ActivityFilterType.CONNECTIONS_ACTIVITIES
                   .oldFromSinceTime(Utils.getLastVisited(Utils.OLD_FROM, mode))
                   .fromSinceTime(Utils.getLastVisited(Utils.FROM, mode))
                   .toSinceTime(Utils.getLastVisited(Utils.TO, mode))
                   .lastNumberOfUpdated(getLastUpdatedNum(mode));
    
    //
    mode = DisplayMode.MY_ACTIVITIES.toString();
    ActivityFilterType.USER_ACTIVITIES
                   .oldFromSinceTime(Utils.getLastVisited(Utils.OLD_FROM, mode))
                   .fromSinceTime(Utils.getLastVisited(Utils.FROM, mode))
                   .toSinceTime(Utils.getLastVisited(Utils.TO, mode))
                   .lastNumberOfUpdated(getLastUpdatedNum(mode));
    
    //
    mode = DisplayMode.MY_SPACE.toString();
    ActivityFilterType.USER_SPACE_ACTIVITIES
                  .oldFromSinceTime(Utils.getLastVisited(Utils.OLD_FROM, mode))
                  .fromSinceTime(Utils.getLastVisited(Utils.FROM, mode))
                  .toSinceTime(Utils.getLastVisited(Utils.TO, mode))
                  .lastNumberOfUpdated(getLastUpdatedNum(mode));
    
    ActivityUpdateFilter updatedFilter = new ActivityUpdateFilter(hasRefresh);
   
    int gotNumber = activitiesListAccess.getNumberOfUpdated(updatedFilter);
    
    
    //
    if (gotNumber == 0 && hasRefresh) {
      //only in case lastUpdatedNumber > 0 then reset cookies
      long lastNumber = getLastUpdatedNum(selectedDisplayMode.toString());
      if (lastNumber > 0) {
        resetCookies();
      }
      
    }
    
    
    return gotNumber;
  }
  
  public void resetCookies() {
    Utils.setLastVisited(this.selectedDisplayMode.toString());
    
    //
    if (this.selectedDisplayMode == DisplayMode.ALL_ACTIVITIES) {
      Utils.setLastVisited(DisplayMode.CONNECTIONS.toString());
      
      //
      Utils.setLastVisited(DisplayMode.MY_ACTIVITIES.toString());
      
      //
      Utils.setLastVisited(DisplayMode.MY_SPACE.toString());
    }
  }
  
  
  
  public void setPostActivity(boolean postActivity) {
    this.postActivity = postActivity;
  }

  protected boolean isWelcomeActivity() {
    viewerName = PortalRequestContext.getCurrentInstance().getRemoteUser();
    if ( !viewerName.equals(ownerName) ) return false;
    
    boolean hasActivities = getActivitiesLoader().getActivitiesContainer().getChildren().size() > 0;
    boolean isAllActivitiesModeOnHomePage = DisplayMode.ALL_ACTIVITIES.equals(getSelectedDisplayMode());
    
    return Utils.isHomePage() ? !hasActivities && isAllActivitiesModeOnHomePage : !hasActivities;
  }
  
  private long getLastUpdatedNum(String mode) {
    String cookieKey = String.format(Utils.LAST_UPDATED_ACTIVITIES_NUM, mode, Utils.getViewerRemoteId());
    String strValue = Utils.getCookies(cookieKey);
    boolean refreshPage = Utils.isRefreshPage();
    
    if(strValue == null || (refreshPage == false && mode.equals(selectedDisplayMode.toString()))) {
      return 0;
    }
    
    return Long.parseLong(strValue);
  }
  
  private void setLastUpdatedNum(String mode, String value) {
    String cookieKey = String.format(Utils.LAST_UPDATED_ACTIVITIES_NUM, mode, Utils.getViewerRemoteId());
    Utils.setCookies(cookieKey, value);
  }
}
