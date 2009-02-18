/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 11, 2008          
 */

@ComponentConfig(
    template = "app:/groovy/portal/webui/component/UIManageSpaceControlArea.gtmpl",
    events = {
        @EventConfig(listeners = UIManageSpaceControlArea.CreateSpaceActionListener.class)
    }
)
public class UIManageSpaceControlArea extends UIContainer {

  public UIManageSpaceControlArea() throws Exception {
    addChild(UIPopupWindow.class,null,"UICreateSpaceForm");
    
  }
  
  static public class CreateSpaceActionListener extends EventListener<UIManageSpaceControlArea> {
    public void execute(Event<UIManageSpaceControlArea> event) throws Exception {
      UIManageSpaceControlArea uiControlArea = event.getSource();
      UISpaceForm uiSpaceForm = uiControlArea.createUIComponent(UISpaceForm.class, null, null);
      UIPopupWindow uiPopup = uiControlArea.getChild(UIPopupWindow.class);
      uiPopup.setUIComponent(uiSpaceForm);
      uiPopup.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }
  
}
