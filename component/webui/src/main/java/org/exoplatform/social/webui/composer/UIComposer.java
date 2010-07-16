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


import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 *
 * @author    zun
 * @since 	  Apr 6, 2010
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/social/webui/composer/UIComposer.gtmpl",
  events = {
    @EventConfig(listeners = UIComposer.PostMessageActionListener.class),
    @EventConfig(listeners = UIComposer.ActivateActionListener.class)
  }
)
public class UIComposer extends UIForm {

  public static class PostContext{
    public final static String SPACE = "SPACE";
    public final static String PEOPLE = "PEOPLE";
  }

  public static final String EXTENSION_KEY="extension";
  public static final String DATA_KEY = "data";
  public static final String COMMENT_KEY = "comment";

  private String postContext;
  private UIFormTextAreaInput messageInput;
  private UIActivityComposerContainer composerContainer;
  private List<UIActivityComposer> activityComposers;
  /**
   * Constructor
   * @throws Exception
   */
  public UIComposer() throws Exception {
    //add textbox for inputting message
    messageInput = new UIFormTextAreaInput("composerInput", "composerInput", null);
    addUIFormInput(messageInput);

    //load UIActivityComposerManager via PortalContainer
    UIActivityComposerManager activityComposerManager = (UIActivityComposerManager) PortalContainer.getInstance().getComponentInstanceOfType(UIActivityComposerManager.class);
    activityComposerManager.setDefaultActivityComposer();

    //TODO : get all the composers and load their icon
    activityComposers = activityComposerManager.getAllComposers();

    //add composer container
    composerContainer = addChild(UIActivityComposerContainer.class, null, null);
    for (UIActivityComposer uiActivityComposer : activityComposers) {
      uiActivityComposer.setRendered(false);
      composerContainer.addChild(uiActivityComposer);
    }
  }

  public UIActivityComposerContainer getComposerContainer() {
    return composerContainer;
  }

  public String getMessage() {
    return getChild(UIFormTextAreaInput.class).getValue();
  }

  public String getPostContext() {
    return postContext;
  }

  public void setPostContext(String postContext) {
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
      final String postContext = uiComposer.getPostContext();

      //get current activity composer
      UIActivityComposerManager activityComposerManager = (UIActivityComposerManager) PortalContainer.getInstance().getComponentInstanceOfType(UIActivityComposerManager.class);
      final UIActivityComposer activityComposer = activityComposerManager.getCurrentActivityComposer();

      //get posted message
      String message = uiComposer.getMessage().trim();
      String defaultInput = event.getRequestContext().getApplicationResourceBundle().getString(uiComposer.getId()+".Default_Input_Write_Something");
      if (message.equals(defaultInput)) {
        message = "";
      }

      //post activity via the current activity composer
      WebuiRequestContext requestContext = event.getRequestContext();
      activityComposer.postActivity(postContext, uiComposer, requestContext, message);
    }
  }

  public static class ActivateActionListener extends EventListener<UIActivityComposer> {
    @Override
    public void execute(Event<UIActivityComposer> event) throws Exception {
      final UIComposer composer = event.getSource().getAncestorOfType(UIComposer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(composer);
    }
  }
}