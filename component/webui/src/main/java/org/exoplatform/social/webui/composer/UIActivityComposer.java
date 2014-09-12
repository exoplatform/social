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

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;
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
  private static final String SELECT_DOCUMENT_ACTION = "SelectDocument";
  private static final String UI_DOCUMENT_ACTIVITY_COMPOSER = "UIDocActivityComposer";
  private static final String FILE_ALREADY_EXIST = "UIActivityComposer.message.alreadyAttached";
  
  private UIContainer activityDisplay;
  private UIActivityComposerManager activityComposerManager;
  private boolean isReady = false;
  private boolean isDisplayed = false;
  
  /**
   * Gets displayed information of component.
   * 
   * @return the isDisplayed
   */
  public boolean isDisplayed() {
    return isDisplayed;
  }

  /**
   * Sets displayed information of component.
   * 
   * @param isDisplayed the isDisplayed to set
   */
  public void setDisplayed(boolean isDisplayed) {
    this.isDisplayed = isDisplayed;
  }

  public boolean isReadyForPostingActivity() {
    return isReady;
  }

  public void setReadyForPostingActivity(boolean isReady) {
    this.isReady = isReady;
  }

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

  public void postActivity(UIComposer.PostContext postContext, UIComponent source,
                           WebuiRequestContext requestContext, String postedMessage) throws Exception {
    onPostActivity(postContext, source, requestContext, postedMessage);
    setReadyForPostingActivity(false);
    setDisplayed(false);
    activityComposerManager.setDefaultActivityComposer();
  }

  public static class CloseActionListener extends EventListener<UIActivityComposer> {
    @Override
    public void execute(Event<UIActivityComposer> event) throws Exception {
      final UIActivityComposer activityComposer = event.getSource();
      final UIActivityComposerManager activityComposerManager = activityComposer.getActivityComposerManager();
      activityComposerManager.setDefaultActivityComposer();

      activityComposer.onClose(event);
      activityComposer.setDisplayed(false);
      final UIComposer composer = activityComposerManager.getUIComposer();
      event.getRequestContext().addUIComponentToUpdateByAjax(composer);
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
      WebuiRequestContext ctx = event.getRequestContext();
      final UIActivityComposer activityComposer = event.getSource();
      final UIActivityComposerManager activityComposerManager = activityComposer.getActivityComposerManager();
      activityComposerManager.setCurrentActivityComposer(activityComposer);
      if ((activityComposer.getClass().getSimpleName().equals(UI_DOCUMENT_ACTIVITY_COMPOSER))&&(activityComposer.isDisplayed())) {
        UIApplication uiApp = ctx.getUIApplication();
        uiApp.addMessage(new ApplicationMessage(FILE_ALREADY_EXIST, null, ApplicationMessage.WARNING));
        return;
      }

      activityComposer.onActivate(event);
      if (!(activityComposer.getClass().getSimpleName().equals(UI_DOCUMENT_ACTIVITY_COMPOSER))) {
        activityComposer.setDisplayed(true);
      }
      final UIComposer composer = activityComposerManager.getUIComposer();
      if (activityComposer.getClass().getSimpleName().equals(UI_DOCUMENT_ACTIVITY_COMPOSER)) {
        Event<UIComponent> selectDocEvent = activityComposer.createEvent(SELECT_DOCUMENT_ACTION, event.getExecutionPhase(), ctx);
        if (selectDocEvent != null) {
          selectDocEvent.broadcast();
        }
      } 

      event.getRequestContext().addUIComponentToUpdateByAjax(composer);
    }
  }

  protected abstract void onPostActivity(UIComposer.PostContext postContext, UIComponent source,
                                         WebuiRequestContext requestContext, String postedMessage) throws Exception;

  protected abstract void onClose(Event<UIActivityComposer> event);

  protected abstract void onSubmit(Event<UIActivityComposer> event);

  protected abstract void onActivate(Event<UIActivityComposer> event);
}
