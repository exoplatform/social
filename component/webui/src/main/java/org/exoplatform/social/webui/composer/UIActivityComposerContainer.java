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

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;

@ComponentConfig(
  template = "war:/groovy/social/webui/composer/UIActivityComposerContainer.gtmpl"
)
public class UIActivityComposerContainer extends UIContainer {
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    UIComposer uiComposer = getParent();
    UIActivityComposer uiActivityComposer = uiComposer.getActivityComposerManager().getCurrentActivityComposer();
    
    List<UIComponent> children = getChildren();
    for(UIComponent child : children) { 
      if (child instanceof UIActivityComposer) {
          UIActivityComposer ui = (UIActivityComposer) child;
          if (ui.getClass().getSimpleName().equals(uiActivityComposer.getClass().getSimpleName())) {
            ui.setDisplayed(true);
          } else {
            ui.setDisplayed(false);
          }
      }
    }
    super.processRender(context);
  }
}
