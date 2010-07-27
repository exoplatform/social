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

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * UIComposerExtention.java
 * <p>
 * This ui component contains action links to invoke corresponding ui extension (link, photo, video...)
 * </p>
 * @author    <a href="http://hoatle.net">hoatle</a>
 * @since     Apr 19, 2010
 * @copyright eXo Platform SAS
 */

public abstract class UIActivityComposer extends UIContainer {
  private UIContainer activityDisplay;
  private UIActivityComposerManager activityComposerManager;
  
  public abstract void postActivity(UIComposer.PostContext postContext, UIComponent source, WebuiRequestContext requestContext, String postedMessage) throws Exception;
  protected abstract void onClose(Event<UIActivityComposer> event);
  protected abstract void onSubmit(Event<UIActivityComposer> event);
  protected abstract void onActivate(Event<UIActivityComposer> event);

  public void setActivityDisplay(UIContainer activityDisplay) {
    this.activityDisplay = activityDisplay;
  }

  public UIContainer getActivityDisplay() {
    return activityDisplay;
  }

  public void setActivityComposerManager(UIActivityComposerManager activityComposerManager) {
    this.activityComposerManager = activityComposerManager;
  }

  public UIActivityComposerManager getActivityComposerManager() {
    return activityComposerManager;
  }

  public static class CloseActionListener extends EventListener<UIActivityComposer> {
    @Override
    public void execute(Event<UIActivityComposer> event) throws Exception {
      final UIActivityComposer activityComposer = event.getSource();
      final UIActivityComposerManager activityComposerManager = activityComposer.getActivityComposerManager();
      activityComposerManager.setDefaultActivityComposer();

      activityComposer.onClose(event);
    }
  }

  public static class SubmitContentActionListener extends EventListener<UIActivityComposer> {
    @Override
    public void execute(Event<UIActivityComposer> event) throws Exception {
      final UIActivityComposer activityComposer = event.getSource();
      activityComposer.onSubmit(event);
    }
  }

  public static class ActivateActionListener extends EventListener<UIActivityComposer> {
    @Override
    public void execute(Event<UIActivityComposer> event) throws Exception {
      final UIActivityComposer activityComposer = event.getSource();
      final UIActivityComposerManager activityComposerManager = activityComposer.getActivityComposerManager();
      activityComposerManager.setCurrentActivityComposer(activityComposer);

      activityComposer.onActivate(event);
    }
  }
}