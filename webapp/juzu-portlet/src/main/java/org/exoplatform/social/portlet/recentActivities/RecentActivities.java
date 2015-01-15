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

package org.exoplatform.social.portlet.recentActivities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import juzu.Path;
import juzu.View;
import juzu.request.RenderContext;
import juzu.template.Template;

import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.portlet.userprofile.helpers.UserProfileHelper;
import org.exoplatform.social.webui.Utils;

public class RecentActivities {
  private static int LATEST_ACTIVITIES_NUM = 5;
  private static int ACTIVITIES_NUM_TO_CHECK = 6;
  private boolean hasViewAll = false;
  
  @Inject
  @Path("index.gtmpl") Template index;
  
  @View
  public void index(RenderContext renderContext) {
    
    Map<String, Object> parameters = new HashMap<String, Object>();
    
    parameters.put("_ctx", UserProfileHelper.getContext(renderContext));
    parameters.put("activities", getRecentActivities());
    parameters.put("hasViewAll", this.hasViewAll);
    index.render(parameters);
  }
  
  public void setHasViewAll(boolean hasViewAll) {
    this.hasViewAll = hasViewAll;
  }

  private List<ExoSocialActivity> getRecentActivities() {
    List<ExoSocialActivity> results = new ArrayList<ExoSocialActivity>();
    RealtimeListAccess<ExoSocialActivity> activitiesListAccess = null;
    Identity currentIdentity = UserProfileHelper.getCurrentProfile().getIdentity();
    if (currentIdentity.getId().equals(Utils.getViewerIdentity().getId())) {
      activitiesListAccess = Utils.getActivityManager().getActivitiesWithListAccess(currentIdentity);
    } else {
      activitiesListAccess = Utils.getActivityManager().getActivitiesWithListAccess(currentIdentity, Utils.getViewerIdentity());
    }
    
    results = activitiesListAccess.loadAsList(0, LATEST_ACTIVITIES_NUM);
    setHasViewAll(activitiesListAccess.loadAsList(0, ACTIVITIES_NUM_TO_CHECK).size() > LATEST_ACTIVITIES_NUM);
    return results; 
  }
}
