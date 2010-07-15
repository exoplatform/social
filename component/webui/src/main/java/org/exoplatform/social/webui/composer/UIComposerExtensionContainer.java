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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.json.JSONObject;

/**
 * UIComposerExtention.java
 * <p>
 * This ui component contains action links to invoke corresponding uiextension (link, photo, video...)
 * </p>
 * @author    <a href="http://hoatle.net">hoatle</a>
 * @since 	  Apr 19, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  template = "classpath:/groovy/social/webui/composer/UIComposerExtensionContainer.gtmpl",
  events = {
   @EventConfig(listeners = UIComposerExtensionContainer.InvokeExtensionActionListener.class),
   @EventConfig(listeners = UIComposerExtensionContainer.CloseExtensionActionListener.class),
   @EventConfig(listeners = UIComposerExtensionContainer.ChangeLinkContentActionListener.class)
  }
)
public class UIComposerExtensionContainer extends UIContainer {
  private List<Extension> enabledExtensionList_;
  private Extension currentExtension_ = null;

  private boolean extensionAttached_ = false;
  private Map<Extension, JSONObject> data_;

  static public enum Extension {
    LINK("link"),
    PHOTO("photo"),
    VIDEO("video");

    private Extension(String extension) {
     extension_ = extension;
    }
    public String getExtension() {
      return extension_;
    }
    private String extension_;
  }

  /**
   * constructor
   */
  public UIComposerExtensionContainer() {

  }

  public void setExtensionAttached(boolean attached) {
    extensionAttached_ = attached;
  }

  public boolean isExtensionAttached() {
    return extensionAttached_;
  }

  public void setData(Map<Extension, JSONObject> data) {
    data_ = data;
  }

  public Map<Extension, JSONObject> getData() {
    return data_;
  }

  public void setCurrentExtension(Extension extension) {
    currentExtension_ = extension;
    if (extension == Extension.LINK) {
      getChild(UIComposerLinkExtension.class).setRendered(true);
    } else if (extension == Extension.PHOTO) {

    } else if (extension == Extension.VIDEO) {

    } else {
      currentExtension_ = null;
      for (UIComponent uiComponent: getChildren()) {
        uiComponent.setRendered(false);
      }
    }
  }

  public Extension getCurrentExtension() {
    return currentExtension_;
  }

  public void setEnabledExtensions(List<Extension> enabledExtensionList) throws Exception {
    enabledExtensionList_ = enabledExtensionList;
    init();
  }

  public List<Extension> getEnabledExtensionList() {
    return enabledExtensionList_;
  }

  /**
   * initialize ui components
   * @throws Exception
   */
  private void init() throws Exception {
    if (enabledExtensionList_.contains(Extension.LINK)) {
      addChild(UIComposerLinkExtension.class, null, "UIComposerLinkExtension").setRendered(false);
    }

    if (enabledExtensionList_.contains(Extension.PHOTO)) {
      //TODO hoatle needs UIComposerPhotoExtension
    }

    if (enabledExtensionList_.contains(Extension.VIDEO)) {
      //TODO hoatle needs UIComposerVideoExtension
    }
  }

  static public class CloseExtensionActionListener extends EventListener<UIComposerExtensionContainer> {

    @Override
    public void execute(Event<UIComposerExtensionContainer> event) throws Exception {
      UIComposerExtensionContainer uiComposerExtension = event.getSource();
      uiComposerExtension.setCurrentExtension(null);
      uiComposerExtension.setExtensionAttached(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComposerExtension);
    }

  }

  static public class InvokeExtensionActionListener extends EventListener<UIComposerExtensionContainer> {

    @Override
    public void execute(Event<UIComposerExtensionContainer> event) throws Exception {
      WebuiRequestContext requestContext = event.getRequestContext();
      UIComposerExtensionContainer uiComposerExtension = event.getSource();
      String extension = requestContext.getRequestParameter(OBJECTID);
      if (Extension.LINK.getExtension().equals(extension)) {
        uiComposerExtension.setCurrentExtension(Extension.LINK);
      } else if (Extension.PHOTO.getExtension().equals(extension)) {
        uiComposerExtension.setCurrentExtension(Extension.PHOTO);
      } else if (Extension.VIDEO.getExtension().equals(extension)) {
        uiComposerExtension.setCurrentExtension(Extension.VIDEO);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComposerExtension);
    }

  }

  static public class ChangeLinkContentActionListener extends EventListener<UIComposerLinkExtension> {

    @Override
    public void execute(Event<UIComposerLinkExtension> event) throws Exception {
      UIComposerLinkExtension uiComposerLinkExtension = event.getSource();
      UIComposerExtensionContainer uiComposerExtensionContainer = uiComposerLinkExtension.getAncestorOfType(UIComposerExtensionContainer.class);
      uiComposerExtensionContainer.setExtensionAttached(true);
      Map<Extension, JSONObject> data = new HashMap<Extension, JSONObject>();
      data.put(Extension.LINK, uiComposerLinkExtension.getDataLink());
      uiComposerExtensionContainer.setData(data);
    }

  }
}