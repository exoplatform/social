package org.exoplatform.social.webui.composer;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.webui.space.UISpaceActivitiesDisplay;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jun 29, 2010
 * Time: 10:36:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class UIActivityComposerManager extends BaseComponentPlugin {
  private static final Log LOG = ExoLogger.getLogger(UIActivityComposerManager.class);

  public static final String DEFAULT_ACTIVITY_COMPOSER = "DEFAULT_ACTIVITY_COMPOSER";
  
  private List<UIActivityComposer> activityComposers = new ArrayList<UIActivityComposer>();
  private UIActivityComposer currentActivityComposer = null;
  private UIActivityComposer defaultActivityComposer = null;
  private boolean initialized;

  public UIActivityComposerManager() {
  }

  public void setDefaultActivityComposer(UIActivityComposer activityComposer){
    defaultActivityComposer = activityComposer;
    setDefaultActivityComposer();
  }

  public void setDefaultActivityComposer(){
    currentActivityComposer = defaultActivityComposer;
  }

  public UIActivityComposer getCurrentActivityComposer() {
    return currentActivityComposer;
  }

  public void setCurrentActivityComposer(UIActivityComposer currentActivityComposer) {
    this.currentActivityComposer = currentActivityComposer;
  }

  public void registerActivityComposer(UIActivityComposer activityComposer){
    activityComposers.add(activityComposer);
  }

  public void removeActivityComposer(UIActivityComposer activityComposer){
    activityComposers.remove(activityComposer);
  }

  public List<UIActivityComposer> getAllComposers(){
    return activityComposers;
  }

//  public String getComposerName(UIActivityComposer activityComposer){
//    return activityComposer.getClass().getSimpleName();
//  }

  public void setActivityDisplay(UISpaceActivitiesDisplay uiDisplaySpaceActivities) {
    for (UIActivityComposer activityComposer : activityComposers) {
      activityComposer.setActivityDisplay(uiDisplaySpaceActivities);
    }

    defaultActivityComposer.setActivityDisplay(uiDisplaySpaceActivities);
  }

  public boolean isInitialized() {
    return initialized;
  }

  public void setInitialized() {
    initialized = true;
  }
}