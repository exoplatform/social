/*
getActivityComposers * Copyright (C) 2003-2010 eXo Platform SAS.
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


import java.util.List;

import org.exoplatform.commons.utils.HTMLSanitizer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.social.webui.activity.UIActivityLoader;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 *
 * @author    zun
 * @since   Apr 6, 2010
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "war:/groovy/social/webui/composer/UIComposer.gtmpl",
  events = {
    @EventConfig(listeners = UIComposer.PostMessageActionListener.class)
  }
)
public class UIComposer extends UIForm {

  public enum PostContext {
    SPACE,
    USER,
    SINGLE
  }

  private PostContext postContext;
  private UIActivityComposerContainer composerContainer;
  private UIActivityComposerManager activityComposerManager;
  private boolean isActivityStreamOwner;

  private static final String HTML_ATTRIBUTE_TITLE = "title";
  private static final String COMPOSER_TEXT_AREA_INPUT = "composerInput";
  private static final String HTML_AT_SYMBOL_PATTERN = "@";
  private static final String HTML_AT_SYMBOL_ESCAPED_PATTERN = "&#64;";
  
  /**
   * Constructor
   * @throws Exception
   */
  public UIComposer() throws Exception {
    if(this.getId() == null) this.setId("UIComposer");
    //add textbox for inputting message
    UIFormTextAreaInput messageInput = new UIFormTextAreaInput(COMPOSER_TEXT_AREA_INPUT, COMPOSER_TEXT_AREA_INPUT, null);
    addUIFormInput(messageInput);

    //add composer container
    composerContainer = addChild(UIActivityComposerContainer.class, null, null);

    //load UIActivityComposerManager via PortalContainer
    activityComposerManager = new UIActivityComposerManager();
    if(!activityComposerManager.isInitialized()){
      initActivityComposerManager();
    }

    //activityComposerManager.setDefaultActivityComposer();
  }

  public void isActivityStreamOwner(boolean isActivityStreamOwner) {
    this.isActivityStreamOwner = isActivityStreamOwner;
  }

  public boolean isActivityStreamOwner() {
    return isActivityStreamOwner;
  }

  protected void changeTitleTextAreInput() throws Exception {
    getUIFormTextAreaInput(COMPOSER_TEXT_AREA_INPUT).setHTMLAttribute(HTML_ATTRIBUTE_TITLE, getLabel("What_Are_You_Working_On"));
  }

  private void initActivityComposerManager() throws Exception {

    UIExtensionManager uiExtensionManager = (UIExtensionManager) PortalContainer.getInstance().
                                                                  getComponentInstanceOfType(UIExtensionManager.class);
    final List<UIExtension> extensionList = uiExtensionManager.getUIExtensions(UIActivityComposer.class.getName());
    if (extensionList != null) {
      for (int i = 0, j = extensionList.size(); i < j; i++) {
        final UIExtension composerExtension = extensionList.get(i);
        UIActivityComposer uiActivityComposer = (UIActivityComposer) uiExtensionManager.
                                               addUIExtension(composerExtension, null, composerContainer);
        if(composerExtension.getName().equals(UIActivityComposerManager.DEFAULT_ACTIVITY_COMPOSER)){
          activityComposerManager.setDefaultActivityComposer(uiActivityComposer);
        }
        uiActivityComposer.setActivityComposerManager(activityComposerManager);
        activityComposerManager.registerActivityComposer(uiActivityComposer);
      }
    }
    activityComposerManager.setUiComposer(this);
    activityComposerManager.setInitialized();
  }

  public void setActivityDisplay(UIContainer uiContainer) {
    activityComposerManager.setActivityDisplay(uiContainer);
  }

  public void setDefaultActivityComposer(){
    activityComposerManager.setDefaultActivityComposer();
  }

  public UIActivityComposerContainer getComposerContainer() {
    return composerContainer;
  }

  public UIActivityComposerManager getActivityComposerManager() {
    return activityComposerManager;
  }

  public List<UIActivityComposer> getActivityComposers() {
    return activityComposerManager.getAllComposers();
  }

  public String getMessage() {
    return getUIFormTextAreaInput(COMPOSER_TEXT_AREA_INPUT).getValue();
  }

  public PostContext getPostContext() {
    return postContext;
  }

  public void setPostContext(PostContext postContext) {
    this.postContext = postContext;
  }

  public String getActivateEvent(String activityComposerId){
    String activityComposerFormId;

    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context instanceof PortletRequestContext)
    {
      activityComposerFormId = ((PortletRequestContext)context).getWindowId() + "#" + activityComposerId;
    } else{
      activityComposerFormId = activityComposerId;
    }

    StringBuilder b = new StringBuilder();
    b.append("javascript:eXo.webui.UIForm.submitForm('").append(activityComposerFormId).append("','");
    b.append("Activate").append("',true)");
    return b.toString();
  }

  public static class PostMessageActionListener extends EventListener<UIComposer> {
    @Override
    public void execute(Event<UIComposer> event) throws Exception {
      //get current context
      UIComposer uiComposer = event.getSource();
      UIActivityComposerManager activityComposerManager = uiComposer.getActivityComposerManager();
      PostContext postContext = uiComposer.getPostContext();

      //get current activity composer
      UIActivityComposer activityComposer = activityComposerManager.getCurrentActivityComposer();
      
      if ( activityComposer.isDisplayed() && !activityComposer.isReadyForPostingActivity() ) {
        activityComposerManager.setDefaultActivityComposer();
      }
      activityComposer = activityComposerManager.getCurrentActivityComposer();
      
      //get posted message
      UIFormTextAreaInput textAreaInput = uiComposer.getUIFormTextAreaInput(COMPOSER_TEXT_AREA_INPUT);
      //--- Processing outcome here aims to avoid escaping '@' symbol while preventing any undesirable side effects due to CSS sanitization.
      //--- The goal is to avoid escape '@' occurrences in microblog application, this enables to keep mention feature working as expected in the specification
      String message = textAreaInput.getValue().replaceAll(HTML_AT_SYMBOL_ESCAPED_PATTERN,HTML_AT_SYMBOL_PATTERN);
      textAreaInput.setValue("");
      //
      message = message == null ? "" : message;

      //post activity via the current activity composer
      ExoSocialActivity activity = activityComposer.postActivity(postContext, message);
      
      //clear client cache
      Utils.clearUserProfilePopup();
      
      Utils.initUserProfilePopup(uiComposer.getId());
      Utils.resizeHomePage();
      //
      if(activity != null) {
        //
        WebuiRequestContext context = event.getRequestContext();
        UIPortletApplication uiPortlet = uiComposer.getAncestorOfType(UIPortletApplication.class);
        
        if(uiPortlet.findFirstComponentOfType(BaseUIActivity.class) == null) {
          context.addUIComponentToUpdateByAjax(uiPortlet);
        } else {
          UIActivitiesContainer uiActivitiesContainer = uiPortlet.findFirstComponentOfType(UIActivitiesContainer.class);         
          //
          uiActivitiesContainer.addFirstActivityId(activity.getId());
          UIActivityLoader uiActivityLoader = uiActivitiesContainer.addChild(UIActivityLoader.class, null,
                                                                             UIActivityLoader.buildComponentId(activity.getId()));
          //
          context.getJavascriptManager().getRequireJS().require("SHARED/social-ui-activities-loader", "activitiesLoader")
                 .addScripts("activitiesLoader.addTop('" + uiActivityLoader.getId() + "', '" + uiActivitiesContainer.getAncestorOfType(UIPortletApplication.class).getId() + "');");
          context.addUIComponentToUpdateByAjax(uiComposer);
        }
        activityComposerManager.clearComposerData();
      }
    }
  }
}
