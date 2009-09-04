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
package org.exoplatform.social.portlet;

import org.exoplatform.portal.account.UIAccountSetting;
import org.exoplatform.social.relation.UIMyRelation;
import org.exoplatform.social.relation.UIPendingRelation;
import org.exoplatform.social.relation.UIPublicRelation;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Aug 25, 2009  
 */

@ComponentConfigs({
  @ComponentConfig(
   lifecycle = UIApplicationLifecycle.class,
   template = "app:/groovy/portal/webui/component/UIRelationshipPortlet.gtmpl"
  ),
  @ComponentConfig (
    id = "UIVerticalRelationTabPane",
    type = UITabPane.class,
    template =  "system:/groovy/social/webui/component/UIVerticalTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UITabPane.SelectTabActionListener.class)
    }                    
  )                
})
public class UIRelationshipPortlet extends UIPortletApplication {

  public UIRelationshipPortlet() throws Exception {
    UITabPane uiTabpane = createUIComponent(UITabPane.class, "UIVerticalRelationTabPane", null);
    uiTabpane.addChild(UIMyRelation.class, null, null);
    uiTabpane.addChild(UIPendingRelation.class, null, null);
    uiTabpane.addChild(UIPublicRelation.class, null, null);
    uiTabpane.setSelectedTab(1);
    addChild(uiTabpane);
  }
  
  public void renderPopupMessages() throws Exception {
    UIPopupMessages uiPopupMsg = getUIPopupMessages();
    if(uiPopupMsg == null)  return ;
    WebuiRequestContext  context =  WebuiRequestContext.getCurrentInstance() ;
    uiPopupMsg.processRender(context);
  }
}
