package org.exoplatform.social.webui.composer;
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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

@ComponentConfig(lifecycle = Lifecycle.class)
public class PopupContainer extends UIPopupContainer {

  public PopupContainer() throws Exception {
    UIPopupWindow popupWindow = addChild(UIPopupWindow.class, null, null);
    popupWindow.setRendered(false);
  }
  
  @Override
  public void activate(UIComponent uiComponent, int width, int height, boolean isResizeable) throws Exception {
    activate(uiComponent, width, height, isResizeable, null);
  }

  @Override
  public void activate(UIComponent uiComponent, int width, int height) throws Exception {
    activate(uiComponent, width, height, true);
  }

  public <T extends UIComponent> T activate(Class<T> type, int width, String popupWindowId) throws Exception {
    T comp = createUIComponent(type, null, null);
    activate(comp, width, 0, true, popupWindowId);
    return comp;
  }

  public void activate(UIComponent uiComponent, int width, int height, boolean isResizeable, String popupId) throws Exception {
    UIPopupWindow popup = getChild(UIPopupWindow.class);
    if (popupId == null || popupId.trim().length() == 0) {
      popupId = "UISocialPopupWindow";
    }
    popup.setId(popupId);
    popup.setUIComponent(uiComponent);
    popup.setWindowSize(width, height);
    popup.setRendered(true);
    popup.setShow(true);
    popup.setResizable(isResizeable);
  }

}