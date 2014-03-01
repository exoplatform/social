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
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;

/**
 * UIActivitiesContainer.java
 *
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since Apr 12, 2010
 */
@ComponentConfig(
  template = "war:/groovy/social/webui/UIActivitiesContainer.gtmpl"
)
public class UIActivitiesContainer extends UIContainer {
  private static final Log LOG = ExoLogger.getLogger(UIActivitiesContainer.class);
  
  public static final String ACTIVITY_STREAM_VISITED_PREFIX_COOKIED = "exo_social_activity_stream_%s_visited_%s";
  private static final String ACTIVITIES_NODE = "activities";
  
  private List<ExoSocialActivity> activityList;
  private PostContext postContext;
  //hold activities for user or space
  private Space space;
  private String ownerName;
  private String selectedDisplayMode;
  private long lastVisited = 0;

  /**
   * constructor
   */
  public UIActivitiesContainer() {
  }

  public PopupContainer getPopupContainer() {
    return getAncestorOfType(UIPortletApplication.class).findFirstComponentOfType(PopupContainer.class);
  }

  public UIPopupWindow getPopupWindow() {
    return getPopupContainer().getChild(UIPopupWindow.class).setRendered(true);
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

  protected long getCurrentServerTime() {
    return Calendar.getInstance().getTimeInMillis();
  }
  
  public boolean isOnMyActivities() {
    return (Utils.getSelectedNode() != null && Utils.getSelectedNode().startsWith(ACTIVITIES_NODE));
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
