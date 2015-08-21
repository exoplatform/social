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
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * UIActivitiesContainer.java
 *
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since Apr 12, 2010
 */
@ComponentConfig(
  template = "war:/groovy/social/webui/UIActivitiesContainer.gtmpl",
    events = {
    @EventConfig(listeners = UIActivitiesContainer.LoadActivityActionListener.class)
  }
)
public class UIActivitiesContainer extends UIContainer {
  public static final String ACTIVITY_STREAM_VISITED_PREFIX_COOKIED = "exo_social_activity_stream_%s_visited_%s";
  private static final String ACTIVITIES_NODE = "activities";
  
  private List<ExoSocialActivity> activityList;
  
  private List<String> activityIdList;
  private PostContext postContext;
  //hold activities for user or space
  private Space space;
  private String ownerName;
  private String selectedDisplayMode;
  private boolean isRenderFull = false;

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
    return (activityList != null) ? activityList : new LinkedList<ExoSocialActivity>();
  }

  public UIActivitiesContainer setActivityList(List<ExoSocialActivity> activityList) throws Exception {
    this.activityList = activityList;
    this.activityIdList = new LinkedList<String>();
    for (ExoSocialActivity activity : activityList) {
      activityIdList.add(activity.getId());
    }
    init();
    return this;
  }
  
  public boolean isRenderFull() {
    return isRenderFull;
  }

  public void setRenderFull(boolean isRenderFull, boolean isFirst) {
    if (isFirst) {
      List<UIActivitiesContainer> containers = new ArrayList<UIActivitiesContainer>();
      getAncestorOfType(UIPortletApplication.class).findComponentOfType(containers, UIActivitiesContainer.class);
      for (UIActivitiesContainer uiActivitiesContainer : containers) {
        uiActivitiesContainer.setRenderFull(isRenderFull, false);
      }
    } else {
      this.isRenderFull = isRenderFull;
    }
  }
  
  public boolean isFirstUIActivitiesContainer() {
    return getParent().getId().equals("UIActivitiesLoader");
  }

  public List<String> getActivityIdList() {
    return (activityIdList != null) ? activityIdList : new LinkedList<String>();
  }

  public void addFirstActivityId(String activityId) {
    if (activityIdList.contains(activityId)) {
      activityIdList.remove(activityId);
    }
    //
    ((LinkedList<String>) activityIdList).addFirst(activityId);
  }

  public UIActivitiesContainer setActivityIdList(List<String> activityIdList) throws Exception {
    this.activityIdList = activityIdList;
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
    while (getChild(UIActivityLoader.class) != null) {
      removeChild(UIActivityLoader.class);
    }

    if (activityIdList == null) {
      return;
    }
    ActivityManager activityManager = getApplicationComponent(ActivityManager.class);
    for (String activityId : activityIdList) {
      UIActivityLoader uiActivityLoader = addChild(UIActivityLoader.class, null, "UIActivityLoader" + activityId);
      if (isRenderFull()) {
        UIActivityFactory factory = CommonsUtils.getService(UIActivityFactory.class);
        factory.addChild(activityManager.getActivity(activityId), uiActivityLoader);
      }
    }
  }

  public void addActivity(ExoSocialActivity activity) throws Exception {
    if (activityList == null) {
      activityList = new LinkedList<ExoSocialActivity>();
    }
    activityList.add(0, activity);
    addFirstActivityId(activity.getId());
    init();
  }

  public void removeActivity(ExoSocialActivity removedActivity) {
    if(activityIdList != null) {
      activityIdList.remove(removedActivity.getId());
    }
    if(activityList != null) {
      for (ExoSocialActivity activity : activityList) {
        if (activity.getId().equals(removedActivity.getId())) {
          activityList.remove(activity);
          break;
        }
      }
    }
  }

  public String getCookiesKey(String displayMode) {
    return String.format(ACTIVITY_STREAM_VISITED_PREFIX_COOKIED, displayMode, Utils.getViewerRemoteId());
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().getRequireJS().require("SHARED/social-ui-activities-loader", "activitiesLoader")
           .addScripts("activitiesLoader.loadingActivities('" + getId() + "');");
    super.processRender(context);
  }

  protected String getLoadActivityUrl() throws Exception {
    return event("LoadActivity").replace("javascript:ajaxGet('", "").replace("')", "&" + OBJECTID + "=");
  }

  protected List<String> getChildrenId() {
    List<String> ids = new LinkedList<String>();
    List<UIComponent> children = getChildren();
    for (UIComponent uiComponent : children) {
      ids.add(uiComponent.getId());
    }
    return ids;
  }

  public static class LoadActivityActionListener extends EventListener<UIActivitiesContainer> {
    @Override
    public void execute(Event<UIActivitiesContainer> event) throws Exception {
      UIActivitiesContainer uiActivitiesContainer = event.getSource();
      String uiActivityId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (uiActivityId == null || uiActivityId.isEmpty()) {
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
      }
      //
      String activityId = uiActivityId.replace("UIActivityLoader", "");
      ExoSocialActivity activity = CommonsUtils.getService(ActivityManager.class).getActivity(activityId);
      //
      UIActivityLoader uiActivityLoader = uiActivitiesContainer.getChildById(uiActivityId);
      if (activity != null) {
        if (uiActivityLoader == null) {
          uiActivityLoader = uiActivitiesContainer.addChild(UIActivityLoader.class, null, "UIActivityLoader" + activityId);
        } else if (uiActivityLoader.getChildren().size() > 0) {
          uiActivityLoader.removeChild(BaseUIActivity.class);
        }
        UIActivityFactory factory = CommonsUtils.getService(UIActivityFactory.class);
        factory.addChild(activity, uiActivityLoader);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiActivityLoader);
      } else {
        if (uiActivityLoader != null) {
          uiActivitiesContainer.removeChildById(uiActivityLoader.getId());
          activity = new ExoSocialActivityImpl();
          activity.setId(activityId);
          uiActivitiesContainer.removeActivity(activity);
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiActivitiesContainer.getParent());
      }
    }
  }
}
