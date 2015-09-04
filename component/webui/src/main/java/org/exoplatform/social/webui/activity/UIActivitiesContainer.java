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

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.PrivilegedSystemHelper;
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

  private static final String ACTIVITIES_FIRST_AJAX_LOAD_KEY = "social.activities.ajax.loader";
  private static int ACTIVITIES_FIRST_AJAX_LOAD = 7;
  
  private List<ExoSocialActivity> activityList;
  
  private List<String> activityIdList;
  private List<String> uiActivityIdFirstList;
  private List<String> uiActivityIdNextList;
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
    ACTIVITIES_FIRST_AJAX_LOAD = Integer.valueOf(PrivilegedSystemHelper.getProperty(ACTIVITIES_FIRST_AJAX_LOAD_KEY, "7").trim());
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

  public List<String> getUiActivityIdFirstList() {
    return new LinkedList<String>(uiActivityIdFirstList);
  }

  public List<String> getUiActivityIdNextList() {
    return new LinkedList<String>(uiActivityIdNextList);
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
    UIActivitiesLoader activitiesLoader = getParent();
    int activityFullRender = activitiesLoader.getLoadingCapacity() - ACTIVITIES_FIRST_AJAX_LOAD;
    boolean isFirstLoader = (activitiesLoader.getClass().getSimpleName().equals(activitiesLoader.getId()));
    int index = 0;
    uiActivityIdFirstList = new LinkedList<String>();
    uiActivityIdNextList = new LinkedList<String>();
    
    ActivityManager activityManager = getApplicationComponent(ActivityManager.class);
    for (String activityId : activityIdList) {
      UIActivityLoader uiActivityLoader = addChild(UIActivityLoader.class, null, UIActivityLoader.buildComponentId(activityId));
      if (isRenderFull()) {
        UIActivityFactory factory = CommonsUtils.getService(UIActivityFactory.class);
        factory.addChild(activityManager.getActivity(activityId), uiActivityLoader);
      } else if (activityFullRender > 0 && isFirstLoader) {
        if (index < activityFullRender) {
          UIActivityFactory factory = CommonsUtils.getService(UIActivityFactory.class);
          factory.addChild(activityManager.getActivity(activityId), uiActivityLoader);
          uiActivityIdFirstList.add(uiActivityLoader.getId());
        } else {
          uiActivityIdNextList.add(uiActivityLoader.getId());
        }
        ++index;
      } else {
        uiActivityIdNextList.add(uiActivityLoader.getId());
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
    //
    super.processRender(context);
  }

  protected String getLoadActivityUrl() throws Exception {
    return event("LoadActivity").replace("javascript:ajaxGet('", StringUtils.EMPTY).replace("')", "&" + OBJECTID + "=");
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
      String activityId = uiActivityId.replace(UIActivityLoader.COMPONENT_ID, StringUtils.EMPTY);
      ExoSocialActivity activity = CommonsUtils.getService(ActivityManager.class).getActivity(activityId);
      //
      UIActivityLoader uiActivityLoader = uiActivitiesContainer.getChildById(uiActivityId);
      if (uiActivityLoader != null) {
        uiActivitiesContainer.removeChildById(uiActivityId);
      }
      if (activity != null) {
        uiActivityLoader = uiActivitiesContainer.addChild(UIActivityLoader.class, null, UIActivityLoader.buildComponentId(activityId));
        UIActivityFactory factory = CommonsUtils.getService(UIActivityFactory.class);
        factory.addChild(activity, uiActivityLoader);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiActivityLoader);
      } else {
        activity = new ExoSocialActivityImpl();
        activity.setId(activityId);
        uiActivitiesContainer.removeActivity(activity);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiActivitiesContainer.getParent());
      }
    }
  }
}
