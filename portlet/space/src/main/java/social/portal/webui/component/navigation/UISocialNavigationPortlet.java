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
package social.portal.webui.component.navigation;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * {@link UISocialNavigationPortlet} used to manage social navigation link.
 *
 */
@ComponentConfigs({
  @ComponentConfig(
    lifecycle = UIApplicationLifecycle.class
  ),
  @ComponentConfig(
    type = UISocialNavigation.class,
    id = "UIHorizontalNavigation",
    events = @EventConfig(listeners = UISocialNavigation.SelectNodeActionListener.class)
  )
})

public class UISocialNavigationPortlet extends UIPortletApplication {
  /**
   * constructor
   * @throws Exception
   */
  public UISocialNavigationPortlet() throws  Exception { 
    PortletRequestContext context = (PortletRequestContext)  WebuiRequestContext.getCurrentInstance();
    PortletRequest prequest = context.getRequest();
    PortletPreferences prefers = prequest.getPreferences() ;
    String template =  prefers.getValue("template", "app:/groovy/portal/webui/navigation/UISocialNavigationPortlet.gtmpl");
    UISocialNavigation portalNavigation = addChild(UISocialNavigation.class, "UIHorizontalNavigation", null);
    portalNavigation.setUseAjax(Boolean.valueOf(prefers.getValue("useAJAX", "true")));
    //TODO dang.tung 3.0
    //portalNavigation.getComponentConfig().setTemplate(template);
    //TODO dang.tung
  }
}