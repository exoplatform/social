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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.social.webui.composer.UIComposerExtensionContainer.Extension;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.json.JSONObject;


/**
 * UIComposer.java
 *
 * <p>
 * Allows users to type messages and then postMessage is broadcasted to its parent.
 *
 * @author    <a href="http://hoatle.net">hoatle</a>
 * @since 	  Apr 6, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath://groovy/social/webui/composer/UIComposer.gtmpl",
  events = {
    @EventConfig(listeners = UIComposer.PostMessageActionListener.class)
  }
)
public class UIComposer extends UIForm {
  public static final String EXTENSION_KEY="extension";
  public static final String DATA_KEY = "data";
  public static final String COMMENT_KEY = "comment";

  private int minChactersRequired_ = 0;
  private int maxCharactersAllowed_ = 0;
  private UIFormTextAreaInput composerInput_;
  private String titleData_ = null;
  /**
   * Constructor
   * @throws Exception
   */
  public UIComposer() throws Exception {
    composerInput_ = new UIFormTextAreaInput("composerInput", "composerInput", null);
    addUIFormInput(composerInput_);
    List<Extension> enabledExtensionList = new ArrayList<Extension>();
    enabledExtensionList.add(Extension.LINK);
    addChild(UIComposerExtensionContainer.class, null, null).setEnabledExtensions(enabledExtensionList);
  }

  public void setStringLengthValidator(Integer minCharacters, Integer maxCharacters) throws Exception {
    minCharacters = minCharacters > 0 ? minCharacters : 0;
    maxCharacters = maxCharacters > 0 ? maxCharacters : 0;

    minChactersRequired_ = minCharacters;
    maxCharactersAllowed_ = maxCharacters;
    if (maxCharactersAllowed_ < minChactersRequired_) {
      throw new IllegalArgumentException("maxCharacters is smaller than minCharacters");
    }
    composerInput_.addValidator(StringLengthValidator.class, minChactersRequired_, maxCharactersAllowed_);
  }

  public int getMinCharactersRequired() {
    return minChactersRequired_;
  }

  public int getMaxCharactersAllowed() {
    return maxCharactersAllowed_;
  }

  public String getMessage() {
    return getChild(UIFormTextAreaInput.class).getValue();
  }

  public void setTitleData(String titleData) {
    titleData_ = titleData;
  }

  public String getTitleData() {
    return titleData_;
  }

  /**
   * Listener for postMessage
   * @author hoatle
   *
   */
  public static class PostMessageActionListener extends EventListener<UIComposer> {

    @Override
    public void execute(Event<UIComposer> event) throws Exception {
      UIComposer uiComposer = event.getSource();
      UIComposerExtensionContainer uiComposerExtensionContainer = uiComposer.getChild(UIComposerExtensionContainer.class);
      String message = uiComposer.getMessage().trim();
      String defaultInput = event.getRequestContext().getApplicationResourceBundle().getString(uiComposer.getId()+".Default_Input_Write_Something");
      if (message.equals(defaultInput)) {
        message = "";
      }
      if (uiComposerExtensionContainer.isExtensionAttached()) {
        Map<Extension, JSONObject> data = uiComposerExtensionContainer.getData();
        Iterator<Entry<Extension, JSONObject>> itr = data.entrySet().iterator();
        while (itr.hasNext()) {
          Entry<Extension, JSONObject> entry = itr.next();
          Extension extension = entry.getKey();
          JSONObject attachedData = entry.getValue();
          if (Extension.LINK == extension) {
            JSONObject titleData = new JSONObject();
            titleData.put(EXTENSION_KEY, Extension.LINK.getExtension());
            titleData.put(DATA_KEY, attachedData);
            titleData.put(COMMENT_KEY, message);
            uiComposer.setTitleData(titleData.toString());
          } /*else if (Extension.PHOTO == extension) {

          } else if (Extension.VIDEO == extension) {

          }*/
        }
        uiComposerExtensionContainer.setExtensionAttached(false);
        uiComposerExtensionContainer.setData(null);
      } else {
        uiComposer.setTitleData(message);
      }
      uiComposerExtensionContainer.setCurrentExtension(null);
      event.getSource().getParent().broadcast(event, event.getExecutionPhase());
    }
  }

  public String getUIFormTextAreaID() {
    return getChild(UIFormTextAreaInput.class).getId();
  }
}
