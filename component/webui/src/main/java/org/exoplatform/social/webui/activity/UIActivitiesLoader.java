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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.ActivitiesRealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
  template = "war:/groovy/social/webui/activity/UIActivitiesLoader.gtmpl",
  events = {
    @EventConfig(listeners = UIActivitiesLoader.LoadMoreActionListener.class)
  }
)

public class UIActivitiesLoader extends UIContainer {
  private static final Log LOG = ExoLogger.getLogger(UIActivitiesLoader.class);
  public static final String ACTIVITY_POST_CONTEXT_KEY = "postContext";
  private int currentLoadIndex;
  private boolean hasMore;
  private UIActivitiesLoader lastActivitiesLoader;
  private ListAccess<ExoSocialActivity> activityListAccess;
  private String ownerName;
  private String selectedDisplayMode;

  public static String genereateId() {
    Random random = new Random();
    return "UIActivitiesLoader_"+ random.nextDouble();
  }

  private UIComposer.PostContext postContext;
  private boolean isExtendLoader;
  private UIActivitiesContainer activitiesContainer;
  private UIContainer extendContainer;
  private int loadingCapacity;
  private Space space;
  
  public UIActivitiesLoader() {
    try {
      activitiesContainer = addChild(UIActivitiesContainer.class, null, "UIActivitiesContainer_" + hashCode());
      extendContainer = addChild(UIContainer.class, null, "ExtendContainer_"+ hashCode());
    } catch (Exception e) {
      LOG.error(e);
    }
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context);
    if (getParent() instanceof AbstractActivitiesDisplay) {
      ((AbstractActivitiesDisplay) getParent()).setRenderFull(false);
    }
  }

  public ListAccess<ExoSocialActivity> getActivityListAccess() {
    return activityListAccess;
  }

  public void setActivityListAccess(ListAccess<ExoSocialActivity> activityListAccess) {
    this.activityListAccess = activityListAccess;
  }

  public void setSpace(Space space) {
    this.space = space;
  }

  public UIComposer.PostContext getPostContext() {
    return postContext;
  }

  public void setPostContext(UIComposer.PostContext postContext) {
    this.postContext = postContext;
  }

  public boolean isExtendLoader(){
    return isExtendLoader;
  }

  public void setExtendLoader(boolean extendLoader) {
    isExtendLoader = extendLoader;
  }

  public UIActivitiesContainer getActivitiesContainer(){
    return activitiesContainer;
  }

  public UIContainer getExtendContainer(){
    return extendContainer;
  }

  public void setLoadingCapacity(int loadingCapacity) {
    this.loadingCapacity = loadingCapacity;
  }

  public int getLoadingCapacity() {
    return loadingCapacity;
  }

  public boolean isHasMore() {
    return hasMore;
  }

  public void setHasMore(boolean hasMore) {
    this.hasMore = hasMore;
  }

  public UIActivitiesLoader getLastActivitiesLoader() {
    return lastActivitiesLoader;
  }

  public void setExtendContainer(UIContainer extendContainer) {
    this.extendContainer = extendContainer;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }
  
  public void setSelectedDisplayMode(String selectedDisplayMode) {
    this.selectedDisplayMode = selectedDisplayMode;
  }

  public String getSelectedDisplayMode() {
    return selectedDisplayMode;
  }

  protected boolean isUIUserActivityDisplay() {
    return getParent() instanceof UIUserActivitiesDisplay;
  }
  
  public void init() {
    try {
      hasMore = false;
      currentLoadIndex = 0;
      isExtendLoader = false;
      //first load
      String activityId = getSingleActivityId();
      if (activityId != null && activityId.length() > 0) {
        postContext = PostContext.SINGLE;
      }
      //
      WebuiRequestContext.getCurrentInstance().setAttribute(ACTIVITY_POST_CONTEXT_KEY, postContext);
      activitiesContainer.setPostContext(postContext);
      activitiesContainer.setOwnerName(ownerName);
      activitiesContainer.setSelectedDisplayMode(selectedDisplayMode);
      if (space != null) {
        activitiesContainer.setSpace(space);
      }

      List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>(0);
      
      if (this.postContext == PostContext.SINGLE) {
        activities = loadActivity();
        activitiesContainer.setActivityList(activities);
      } else if (isShowActivities(space)) {
        List<String> activityIds = loadActivities(currentLoadIndex, loadingCapacity);
        activitiesContainer.setActivityIdList(activityIds);
      } else {
        List<String> activityIds = loadActivityIds(currentLoadIndex, loadingCapacity);
        activitiesContainer.setActivityIdList(activityIds);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
  
  private String getSingleActivityId() {
    if ("activity".equals(Utils.getSelectedNode())) {
      if (Utils.getValueFromRequestParam("id") != null) {
        return Utils.getValueFromRequestParam("id");
      }
      return Utils.getValueFromRefererURI("id");
    }
    return null;
  }

  private void loadNext() throws Exception {
    currentLoadIndex += loadingCapacity;
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>(0);
    lastActivitiesLoader = extendContainer.addChild(UIActivitiesLoader.class, null, UIActivitiesLoader.genereateId());
    lastActivitiesLoader.setExtendLoader(true);
    
    if (this.postContext == PostContext.SINGLE) {
      activities = loadActivity();
      lastActivitiesLoader.getActivitiesContainer().setActivityList(activities);
    } else if (isShowActivities(space)) {
      List<String> activityIds = loadActivities(currentLoadIndex, loadingCapacity);
      lastActivitiesLoader.getActivitiesContainer().setActivityIdList(activityIds);
    } else {
      List<String> activityIds = loadActivityIds(currentLoadIndex, loadingCapacity);
      lastActivitiesLoader.getActivitiesContainer().setActivityIdList(activityIds);
    }

    lastActivitiesLoader.getActivitiesContainer().setPostContext(postContext);
    lastActivitiesLoader.getActivitiesContainer().setSpace(space);
    lastActivitiesLoader.setHasMore(isHasMore());
  }

  private List<String> loadActivities(int index, int length) throws Exception {
    if (activityListAccess != null) {
      if (activityListAccess instanceof ActivitiesRealtimeListAccess) {
        ActivitiesRealtimeListAccess listAccess = (ActivitiesRealtimeListAccess) activityListAccess;
        List<String> activityIds = listAccess.loadIdsAsList(index, length);
        if (activityIds != null) {
          setHasMore(activityIds.size() >= loadingCapacity);
          return activityIds;
        }
      }
    }
    return null;
  }
  
  private List<String> loadActivityIds(int index, int length) throws Exception {
    if (activityListAccess != null) {
      if (activityListAccess instanceof ActivitiesRealtimeListAccess) {
        ActivitiesRealtimeListAccess listAccess = (ActivitiesRealtimeListAccess) activityListAccess;
        List<String> activityIds = listAccess.loadIdsAsList(index, length);
        if (activityIds != null) {
          setHasMore(activityIds.size() >= loadingCapacity);
          return activityIds;
        }
      }
    }
    return null;
  }
  
  private List<ExoSocialActivity> loadActivity() throws Exception {
    ActivityManager activityManager = Utils.getActivityManager();
    String activityId = getSingleActivityId();
    ExoSocialActivity activity = (activityId != null) ? activityManager.getActivity(activityId) : null;
    if (activity == null)
      return Collections.emptyList();
    return new ArrayList<ExoSocialActivity>(Arrays.asList(activity));
  }
  
  private boolean isShowActivities(Space space) {
    if (space == null) {
      space = getSpaceByActivityId(Utils.getActivityID());
      if (space == null)
        return false;
    }
    
    String remoteId = Util.getPortalRequestContext().getRemoteUser();
    return Utils.getSpaceService().isMember(space, remoteId);
  }

  private Space getSpaceByActivityId(String activityId) {
    try {
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
      return Utils.getSpaceService().getSpaceByPrettyName(spaceIdentity.getRemoteId());
    } catch (Exception e) {
      return null;
    }
  }

  public static class LoadMoreActionListener extends EventListener<UIActivitiesLoader> {
    @Override
    public void execute(Event<UIActivitiesLoader> event) throws Exception {
      UIActivitiesLoader uiActivitiesLoader = event.getSource();
      uiActivitiesLoader.loadNext();
      WebuiRequestContext context = event.getRequestContext();
      context.addUIComponentToUpdateByAjax(uiActivitiesLoader.getExtendContainer());
      
      UIActivitiesLoader lastLoader = uiActivitiesLoader.getLastActivitiesLoader();
      uiActivitiesLoader.setExtendContainer(lastLoader.getExtendContainer());

      RequireJS require = context.getJavascriptManager()
                                 .require("SHARED/social-ui-activities-loader", "activitiesLoader");
      require.addScripts("activitiesLoader.setStatus('" + uiActivitiesLoader.isHasMore() + "');");
      //
      Utils.resizeHomePage();
    }
  }
}
