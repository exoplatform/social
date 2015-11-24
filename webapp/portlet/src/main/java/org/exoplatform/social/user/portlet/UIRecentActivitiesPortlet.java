/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.user.portlet;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.user.UIRecentActivity;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/user/UIRecentActivitiesPortlet.gtmpl",
  events = {
    @EventConfig(listeners = UIRecentActivitiesPortlet.LoadActivityActionListener.class)
  }
)
public class UIRecentActivitiesPortlet extends UIAbstractUserPortlet {
  private static int LATEST_ACTIVITIES_NUM = 5;
  private static int ACTIVITIES_NUM_TO_CHECK = 6;
  protected boolean hasActivityBottomIcon = false;

  public UIRecentActivitiesPortlet() throws Exception {
  }

  protected List<String> getRecentActivities() throws Exception {
    RealtimeListAccess<ExoSocialActivity> activitiesListAccess = null;
    if (currentProfile.getIdentity().getId().equals(Utils.getViewerIdentity().getId())) {
      activitiesListAccess = Utils.getActivityManager().getActivitiesWithListAccess(currentProfile.getIdentity());
    } else {
      activitiesListAccess = Utils.getActivityManager().getActivitiesWithListAccess(currentProfile.getIdentity(), Utils.getViewerIdentity());
    }
    List<String> results = activitiesListAccess.loadIdsAsList(0, ACTIVITIES_NUM_TO_CHECK);
    hasActivityBottomIcon = (results.size() <= LATEST_ACTIVITIES_NUM);
    if (!hasActivityBottomIcon) {
      results = results.subList(0, LATEST_ACTIVITIES_NUM);
    }
    for (String activityId : results) {
      String childId = UIRecentActivity.buildComponentId(activityId);
      if (getChildById(childId) == null) {
        addChild(UIRecentActivity.class, null, childId);
      }
    }
    //
    removeIfExisting(results);
    //
    return results;
  }

  protected String getLoadActivityUrl() throws Exception {
    return event("LoadActivity").replace("javascript:ajaxGet('", StringUtils.EMPTY).replace("')", "&" + OBJECTID + "=");
  }

  protected Identity getOwnerActivity(ExoSocialActivity activity) {
    return Utils.getIdentityManager().getIdentity(activity.getUserId(), true);
  }

  private void removeIfExisting(List<String> results) {
    List<UIComponent> removeChilds = new ArrayList<UIComponent>();
    for (UIComponent uiComponent : getChildren()) {
      if (results.contains(uiComponent.getId())) {
        continue;
      }
      removeChilds.add(uiComponent);
    }
    for (UIComponent uiComponent : removeChilds) {
      uiComponent.setParent(null);
      getChildren().remove(uiComponent);
    }
  }

  @Override
  public void initProfilePopup() throws Exception {
    super.initProfilePopup();
  }

  public static class LoadActivityActionListener extends EventListener<UIRecentActivitiesPortlet> {
    @Override
    public void execute(Event<UIRecentActivitiesPortlet> event) throws Exception {
      UIRecentActivitiesPortlet uiPortlet = event.getSource();
      String uiActivityId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (uiActivityId == null || uiActivityId.isEmpty()) {
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
      }
      //
      String activityId = uiActivityId.replace(UIRecentActivity.COMPONENT_ID, StringUtils.EMPTY);
      ExoSocialActivity activity = CommonsUtils.getService(ActivityManager.class).getActivity(activityId);
      //
      if (activity != null) {
        UIRecentActivity uiRecentActivity = uiPortlet.getChildById(uiActivityId);
        if (uiRecentActivity == null) {
          uiRecentActivity = uiPortlet.addChild(UIRecentActivity.class, null, uiActivityId);
        }
        uiRecentActivity.setActivity(activity);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiRecentActivity);
      } else {
        uiPortlet.removeChildById(uiActivityId);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
      }
    }
  }
}