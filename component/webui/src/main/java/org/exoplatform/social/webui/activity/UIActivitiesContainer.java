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
package org.exoplatform.social.webui.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.ActivitiesRealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupWindow;

/**
 * UIActivitiesContainer.java
 *
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since Apr 12, 2010
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/UIActivitiesContainer.gtmpl"
)
public class UIActivitiesContainer extends UIContainer {
  private static final Log LOG = ExoLogger.getLogger(UIActivitiesContainer.class);
  
  public static final String ACTIVITY_STREAM_VISITED_PREFIX_COOKIED = "exo_social_activity_stream_%s_visited_%s";
  private static final String ALL_ACTIVITIES = "ALL_ACTIVITIES";
  private static final String CONNECTIONS = "CONNECTIONS";
  private static final String MY_SPACE = "MY_SPACE";
  private static final String MY_ACTIVITIES = "MY_ACTIVITIES";
  
  private List<ExoSocialActivity> activityList;
  private PostContext postContext;
  //hold activities for user or space
  private Space space;
  private String ownerName;
  private String selectedDisplayMode;
  private UIPopupWindow popupWindow;
  private long lastVisited = 0;

  /**
   * constructor
   */
  public UIActivitiesContainer() {
    try {
      popupWindow = addChild(UIPopupWindow.class, null, "OptionPopupWindow");
      popupWindow.setShow(false);
    } catch (Exception e) {
      LOG.error(e);
    }
  }

  public UIPopupWindow getPopupWindow() {
    return popupWindow;
  }

  public List<ExoSocialActivity> getActivityList() {
    return activityList;
  }

  public UIActivitiesContainer setActivityList(List<ExoSocialActivity> activityList) throws Exception {
    this.activityList = activityList;
    init();
    return this;
  }

  public void setPostContext(PostContext postContext) {
    this.postContext = postContext;
  }

  public PostContext getPostContext() {
    return postContext;
  }

  public Space getSpace() {
    return space;
  }

  public void setSpace(Space space) {
    this.space = space;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }
  
  public String getSelectedDisplayMode() {
    return selectedDisplayMode;
  }

  public void setSelectedDisplayMode(String selectedDisplayMode) {
    this.selectedDisplayMode = selectedDisplayMode;
  }


  /**
   * Initializes ui component child
   *
   * @throws Exception
   */
  private void init() throws Exception {
    while (getChild(BaseUIActivity.class) != null) {
      removeChild(BaseUIActivity.class);
    }

    if (activityList == null) {
      return;
    }

    PortalContainer portalContainer = PortalContainer.getInstance();
    UIActivityFactory factory = (UIActivityFactory) portalContainer.getComponentInstanceOfType(UIActivityFactory.class);

    for (ExoSocialActivity activity : activityList) {
      factory.addChild(activity, this);
    }

    lastVisited = getLastVisited(this.selectedDisplayMode);
  }

  public void addActivity(ExoSocialActivity activity) throws Exception {
    if (activityList == null) {
      activityList = new ArrayList<ExoSocialActivity>();
    }
    activityList.add(0, activity);
    init();
  }

  public void removeActivity(ExoSocialActivity removedActivity) {
    for (ExoSocialActivity activity : activityList) {
      if (activity.getId().equals(removedActivity.getId())) {
        activityList.remove(activity);
        break;
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected int getNumberOfUpdatedActivities() {
    try {
      UIActivitiesLoader uiActivitiesLoader = this.getAncestorOfType(UIActivitiesLoader.class);
      ActivitiesRealtimeListAccess activitiesListAccess = (ActivitiesRealtimeListAccess) uiActivitiesLoader.getActivityListAccess();
      
      /**
       *  [All Activities]
       */
      if ( ALL_ACTIVITIES.equals(this.selectedDisplayMode) ) {
        // get last visit since time of each tab
        Map<String, Long> lastVisitedTabs = new HashMap<String, Long>();
        lastVisitedTabs.put(CONNECTIONS, getLastVisited(CONNECTIONS));
        lastVisitedTabs.put(MY_SPACE, getLastVisited(MY_SPACE));
        lastVisitedTabs.put(MY_ACTIVITIES, getLastVisited(MY_ACTIVITIES));
        
        return activitiesListAccess.getNumberOfMultiUpdated(lastVisitedTabs);
      }
      
      return activitiesListAccess.getNumberOfUpdated(lastVisited);
    } catch(Exception e) {
      return 0;
    }
  }
  
  private long getLastVisited(String mode) {
    long currentVisited = Calendar.getInstance().getTimeInMillis();
    String strValue = Utils.getCookies(getCookiesKey(mode));
    if(strValue == null) {
      return currentVisited;
    }
    
    return Long.parseLong(strValue);
  }
  
  public String getCookiesKey(String displayMode) {
    return String.format(ACTIVITY_STREAM_VISITED_PREFIX_COOKIED, displayMode, Utils.getViewerRemoteId());
  }
}
