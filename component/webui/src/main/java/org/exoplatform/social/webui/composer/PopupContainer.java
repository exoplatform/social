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

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;

/**
 * Created by The eXo Platform SAS
 * Author : Zun
 *          exo@exoplatform.com
 * Jul 23, 2010
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/composer/PopupContainer.gtmpl"
)
public class PopupContainer extends UIContainer{
  private UIPopupWindow popupWindow;

  public PopupContainer() {
    try {
      popupWindow = addChild(UIPopupWindow.class, null, "UIPopupWindow_" + hashCode());
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public UIPopupWindow getPopupWindow() {
    return popupWindow;
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    if(!popupWindow.isRendered()){
      popupWindow.setRendered(true);
    }
    super.processRender(context);
  }
}
