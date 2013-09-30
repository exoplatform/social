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
import java.util.List;
import java.util.Random;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.ActivitiesRealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
  template = "classpath:groovy/social/webui/activity/UIActivitiesLoader.gtmpl",
  events = {
    @EventConfig(listeners = UIActivitiesLoader.LoadMoreActionListener.class)
  }
)

public class UIActivitiesLoader extends UIContainer {
  private static final Log LOG = ExoLogger.getLogger(UIActivitiesLoader.class);

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
  private int activitiesCounter;
  
  public UIActivitiesLoader() {
    try {
      activitiesContainer = addChild(UIActivitiesContainer.class, null, "UIActivitiesContainer_" + hashCode());
      extendContainer = addChild(UIContainer.class, null, "ExtendContainer_"+ hashCode());
    } catch (Exception e) {
      LOG.error(e);
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
      activitiesCounter = 0;
      isExtendLoader = false;
      
      activitiesContainer.setPostContext(postContext);
      activitiesContainer.setOwnerName(ownerName);
      activitiesContainer.setSelectedDisplayMode(selectedDisplayMode);
      if (space != null) {
        activitiesContainer.setSpace(space);
      }

      List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>(0);
      
      if (isShowActivities(space)) {
        activities = loadActivities(currentLoadIndex, loadingCapacity);
      }
      
      activitiesContainer.setActivityList(activities);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  private void loadNext() throws Exception {
    if (activityListAccess != null && activityListAccess instanceof ActivitiesRealtimeListAccess) {
      ActivitiesRealtimeListAccess listAccess = (ActivitiesRealtimeListAccess) activityListAccess;
      listAccess.getNumberOfUpgrade();
    }
    currentLoadIndex += loadingCapacity;
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>(0);
    lastActivitiesLoader = extendContainer.addChild(UIActivitiesLoader.class, null, UIActivitiesLoader.genereateId());
    lastActivitiesLoader.setExtendLoader(true);
    
    if (isShowActivities(space)) {
      activities = loadActivities(currentLoadIndex, loadingCapacity);
    }

    UIActivitiesContainer lastActivitiesContainer = lastActivitiesLoader.getActivitiesContainer();
    lastActivitiesContainer.setPostContext(postContext);
    lastActivitiesContainer.setSpace(space);
    
    lastActivitiesLoader.setActivities(activities);
    if(activityListAccess != null) {
      lastActivitiesLoader.setHasMore(activityListAccess.getSize() > activitiesCounter);
    }
  }

  private void setActivities(List<ExoSocialActivity> activities) throws Exception {
    activitiesContainer.setActivityList(activities);
  }

  private List<ExoSocialActivity> loadActivities(int index, int length) throws Exception {
    if (activityListAccess != null) {
      ExoSocialActivity[] activities = activityListAccess.load(index, length);
      if (activities != null) {
        activitiesCounter += activities.length;
        setHasMore(activityListAccess.getSize() > activitiesCounter);

        return new ArrayList<ExoSocialActivity>(Arrays.asList(activities));
      }
    }
    return null;
  }
  
  private boolean isShowActivities(Space space) {
    if (space == null) return true;
    
    String remoteId = Util.getPortalRequestContext().getRemoteUser();
    return Utils.getSpaceService().isMember(space, remoteId);
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
      
      Utils.resizeHomePage();
    }
  }
}
