/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 *          hoat.le@exoplatform.com
 * Jun 23, 2009  
 */
@ComponentConfigs({
  @ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/portal/webui/component/UIManageSpacesPortlet1.gtmpl"
  ),
  @ComponentConfig (
    id = "UIVerticalTabPane",
    type = UITabPane.class,
    template =  "system:/groovy/social/webui/component/UIVerticalTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UITabPane.SelectTabActionListener.class)
    }                    
  )                
})

public class UIManageSpacesPortlet1 extends UIPortletApplication {

  public UIManageSpacesPortlet1() throws Exception {
    UITabPane uiTabPane = createUIComponent(UITabPane.class, "UIVerticalTabPane", null);
    uiTabPane.addChild(UIManageMySpaces.class, null, null);
    uiTabPane.addChild(UIManagePublicSpaces.class, null, null);
    uiTabPane.addChild(UIManagePendingSpaces.class, null, null);
    uiTabPane.setSelectedTab(1);
    addChild(uiTabPane);
  }  
}
