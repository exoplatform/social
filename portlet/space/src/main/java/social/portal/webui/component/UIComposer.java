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
package social.portal.webui.component;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;

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
  template = "app://groovy/portal/webui/component/UIComposer.gtmpl",
  events = {
    @EventConfig(listeners = UIComposer.PostMessageActionListener.class)
  }
)
public class UIComposer extends UIForm {

  /**
   * Constructor
   * @throws Exception
   */
  public UIComposer() throws Exception {
	// TODO: should change value to resource bundle.
    UIFormTextAreaInput composerInput = new UIFormTextAreaInput("composerInput", "composerInput", null);
    addChild(composerInput);
  }
  
  public String getMessage() {
    return getChild(UIFormTextAreaInput.class).getValue();
  }
  
  /**
   * Listener for postMessage
   * @author hoatle
   *
   */
  public static class PostMessageActionListener extends EventListener<UIComposer> {

    @Override
    public void execute(Event<UIComposer> event) throws Exception {
      event.getSource().getParent().broadcast(event, event.getExecutionPhase());
    }
  }
  
  public String getUIFormTextAreaID() {
	return getChild(UIFormTextAreaInput.class).getId();  
  }
}
