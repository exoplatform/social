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
package org.exoplatform.social.webui.composer;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.core.UIContainer;

public class UIActivityComposerManager extends BaseComponentPlugin {
  private static final Log LOG = ExoLogger.getLogger(UIActivityComposerManager.class);

  public static final String DEFAULT_ACTIVITY_COMPOSER = "DEFAULT_ACTIVITY_COMPOSER";
  
  private List<UIActivityComposer> activityComposers = new ArrayList<UIActivityComposer>();
  private UIActivityComposer currentActivityComposer = null;
  private UIActivityComposer defaultActivityComposer = null;
  private boolean initialized;
  private UIComposer uiComposer;

  public UIActivityComposerManager() {
  }

  public void setDefaultActivityComposer(UIActivityComposer activityComposer){
    defaultActivityComposer = activityComposer;
    setDefaultActivityComposer();
  }

  public void setDefaultActivityComposer(){
    for (UIActivityComposer uiActivityComposer : activityComposers) {
      uiActivityComposer.setRendered(false);
    }
    currentActivityComposer = defaultActivityComposer;
  }

  public UIActivityComposer getCurrentActivityComposer() {
    return currentActivityComposer;
  }

  public void setCurrentActivityComposer(UIActivityComposer activityComposer) {
    for (UIActivityComposer uiActivityComposer : activityComposers) {
        uiActivityComposer.setRendered(false);
    }

    activityComposer.setRendered(true);
    this.currentActivityComposer = activityComposer;
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

  public void setActivityDisplay(UIContainer uiContainer) {
    for (UIActivityComposer activityComposer : activityComposers) {
      activityComposer.setActivityDisplay(uiContainer);
    }

    defaultActivityComposer.setActivityDisplay(uiContainer);
  }

  public boolean isInitialized() {
    return initialized;
  }

  public void setInitialized() {
    initialized = true;
  }

  public UIComposer getUIComposer() {
    return uiComposer;
  }

  public void setUiComposer(UIComposer uiComposer) {
    this.uiComposer = uiComposer;
  }
}